/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.geronimo.axis2.builder;

import java.util.Collection;
import java.util.Collections;
import org.apache.geronimo.axis2.osgi.Axis2ModuleRegistry;
import org.apache.geronimo.axis2.pojo.POJOWebServiceContainerFactoryGBean;
import org.apache.geronimo.common.DeploymentException;
import org.apache.geronimo.gbean.AbstractNameQuery;
import org.apache.geronimo.gbean.GBeanData;
import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.gbean.GBeanInfoBuilder;
import org.apache.geronimo.gbean.annotation.AnnotationGBeanInfoBuilder;
import org.apache.geronimo.gbean.annotation.GBean;
import org.apache.geronimo.gbean.annotation.ParamAttribute;
import org.apache.geronimo.gbean.annotation.ParamReference;
import org.apache.geronimo.j2ee.deployment.Module;
import org.apache.geronimo.j2ee.j2eeobjectnames.NameFactory;
import org.apache.geronimo.jaxws.JAXWSUtils;
import org.apache.geronimo.jaxws.PortInfo;
import org.apache.geronimo.jaxws.builder.JAXWSBuilderUtils;
import org.apache.geronimo.jaxws.builder.JAXWSServiceBuilder;
import org.apache.geronimo.jaxws.builder.WARWebServiceFinder;
import org.apache.geronimo.jaxws.builder.wsdl.WsdlGenerator;
import org.apache.geronimo.jaxws.builder.wsdl.WsdlGeneratorOptions;
import org.apache.geronimo.kernel.repository.Artifact;
import org.apache.geronimo.kernel.repository.Environment;
import org.osgi.framework.Bundle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @version $Rev$ $Date$
 */

@GBean(j2eeType = NameFactory.MODULE_BUILDER)
public class Axis2Builder extends JAXWSServiceBuilder {

    private static final Logger log = LoggerFactory.getLogger(Axis2Builder.class);

    private static final boolean ignoreEmptyWebServiceProviderWSDL = Boolean.getBoolean("org.apache.geronimo.webservice.provider.wsdl.ignore");

    protected Collection<WsdlGenerator> wsdlGenerators;

    private GBeanInfo defaultContainerFactoryGBeanInfo;

    public Axis2Builder(@ParamAttribute(name = "defaultEnvironment") Environment defaultEnviroment,
            @ParamReference(name = "WsdlGenerator", namingType = GBeanInfoBuilder.DEFAULT_J2EE_TYPE) Collection<WsdlGenerator> wsdlGenerators) {
        super(defaultEnviroment);
        this.wsdlGenerators = wsdlGenerators;
        this.webServiceFinder = new WARWebServiceFinder();
        AnnotationGBeanInfoBuilder annotationGBeanInfoBuilder = new AnnotationGBeanInfoBuilder(POJOWebServiceContainerFactoryGBean.class);
        defaultContainerFactoryGBeanInfo = annotationGBeanInfoBuilder.buildGBeanInfo();
    }

    public Axis2Builder() {
        super(null);
    }

    protected GBeanInfo getContainerFactoryGBeanInfo() {
        return defaultContainerFactoryGBeanInfo;
    }

    protected WsdlGenerator getWsdlGenerator() throws DeploymentException {
        if (this.wsdlGenerators == null || this.wsdlGenerators.isEmpty()) {
            throw new DeploymentException("Wsdl generator not found");
        } else {
            return this.wsdlGenerators.iterator().next();
        }
    }

    @Override
    protected void initialize(GBeanData targetGBean, Class serviceClass, PortInfo portInfo, Module module, Bundle bundle) throws DeploymentException {
        targetGBean.setReferencePattern("Axis2ModuleRegistry",
                new AbstractNameQuery(Artifact.create("org.apache.geronimo.configs/axis2//car"), Collections.emptyMap(), Axis2ModuleRegistry.class.getName()));
        String serviceName = (portInfo.getServiceName() == null ? serviceClass.getName() : portInfo.getServiceName());
        String wsdlFile = portInfo.getWsdlFile();
        if (wsdlFile != null && wsdlFile.trim().length() > 0) {
            //TODO Workaround codes for web modules in the EAR package, need to add web module name prefix
            portInfo.setWsdlFile(JAXWSBuilderUtils.normalizeWsdlPath(module, wsdlFile));

            if (log.isDebugEnabled()) {
                log.debug("Service " + serviceName + " has WSDL. " + portInfo.getWsdlFile());
            }
            return;
        }

        if (JAXWSUtils.containsWsdlLocation(serviceClass, bundle)) {
            wsdlFile = JAXWSUtils.getServiceWsdlLocation(serviceClass, bundle);
            //TODO Workaround codes for web modules in the EAR package, need to add web module name prefix
            portInfo.setWsdlFile(JAXWSBuilderUtils.normalizeWsdlPath(module, wsdlFile));

            if (log.isDebugEnabled()) {
                log.debug("Service " + serviceName + " has WSDL configured in annotation " + wsdlFile + " and is resolved as " + portInfo.getWsdlFile());
            }
            return;
        }

        if (isHTTPBinding(portInfo, serviceClass)) {
            if (log.isDebugEnabled()) {
                log.debug("Service " + serviceName + " has HTTPBinding.");
            }
            return;
        }

        if (JAXWSUtils.isWebServiceProvider(serviceClass)) {
            if (ignoreEmptyWebServiceProviderWSDL) {
                log.warn("WSDL is not specified for @WebServiceProvider service " + serviceName);
                //TODO Generate a dummy WSDL for it ?
                return;
            } else {
                throw new DeploymentException("WSDL must be specified for @WebServiceProvider service " + serviceName);
            }
        }

        if (log.isDebugEnabled()) {
            log.debug("Service " + serviceName + " does not have WSDL. Generating WSDL...");
        }

        WsdlGenerator wsdlGenerator = getWsdlGenerator();

        WsdlGeneratorOptions options = new WsdlGeneratorOptions();
        options.setSAAJ(WsdlGeneratorOptions.SAAJ.Axis2);

        // set wsdl service
        if (portInfo.getWsdlService() == null) {
            options.setWsdlService(JAXWSUtils.getServiceQName(serviceClass));
        } else {
            options.setWsdlService(portInfo.getWsdlService());
        }

        // set wsdl port
        if (portInfo.getWsdlPort() != null) {
            options.setWsdlPort(portInfo.getWsdlPort());
        }

        wsdlFile = wsdlGenerator.generateWsdl(module, serviceClass.getName(), module.getEarContext(), options);
        portInfo.setWsdlFile(wsdlFile);

        if (log.isDebugEnabled()) {
            log.debug("Generated " + wsdlFile + " for service " + serviceName);
        }
    }

}
