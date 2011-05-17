/**
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.apache.geronimo.cxf.builder;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.stream.StreamSource;

import org.apache.cxf.jaxws.javaee.HandlerChainsType;
import org.apache.cxf.jaxws.javaee.PortComponentType;
import org.apache.cxf.jaxws.javaee.ServiceImplBeanType;
import org.apache.cxf.jaxws.javaee.WebserviceDescriptionType;
import org.apache.cxf.jaxws.javaee.WebservicesType;
import org.apache.cxf.jaxws.support.JaxWsImplementorInfo;
import org.apache.geronimo.common.DeploymentException;
import org.apache.geronimo.cxf.pojo.POJOWebServiceContainerFactoryGBean;
import org.apache.geronimo.deployment.Deployable;
import org.apache.geronimo.gbean.GBeanData;
import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.gbean.GBeanInfoBuilder;
import org.apache.geronimo.j2ee.deployment.Module;
import org.apache.geronimo.j2ee.deployment.WebServiceBuilder;
import org.apache.geronimo.j2ee.j2eeobjectnames.NameFactory;
import org.apache.geronimo.jaxws.JAXWSUtils;
import org.apache.geronimo.jaxws.PortInfo;
import org.apache.geronimo.jaxws.builder.JAXWSBuilderUtils;
import org.apache.geronimo.jaxws.builder.JAXWSServiceBuilder;
import org.apache.geronimo.jaxws.builder.WARWebServiceFinder;
import org.apache.geronimo.jaxws.builder.wsdl.WsdlGenerator;
import org.apache.geronimo.jaxws.builder.wsdl.WsdlGeneratorOptions;
import org.apache.geronimo.jaxws.feature.MTOMFeatureInfo;
import org.apache.geronimo.kernel.repository.Environment;
import org.osgi.framework.Bundle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CXFBuilder extends JAXWSServiceBuilder {
    private static final Logger LOG = LoggerFactory.getLogger(CXFBuilder.class);

    private static final boolean ignoreEmptyWebServiceProviderWSDL = Boolean.getBoolean("org.apache.geronimo.webservice.provider.wsdl.ignore");

    /**
     * This property if enabled will cause the Sun wsgen tool to be used to
     * generate the WSDL for servies without WSDL. By default CXF tooling
     * will be used the generate the WSDL.
     */
    private static final String USE_WSGEN_PROPERTY =
        "org.apache.geronimo.cxf.use.wsgen";

    protected Collection<WsdlGenerator> wsdlGenerators;

    public CXFBuilder() {
        super(null);
    }

    public CXFBuilder(Environment defaultEnvironment, Collection<WsdlGenerator> wsdlGenerators) {
        super(defaultEnvironment);
        this.wsdlGenerators = wsdlGenerators;
        this.webServiceFinder = new WARWebServiceFinder();
    }

    protected GBeanInfo getContainerFactoryGBeanInfo() {
        return POJOWebServiceContainerFactoryGBean.GBEAN_INFO;
    }

    @Override
    protected void initialize(GBeanData targetGBean, Class serviceClass, PortInfo portInfo, Module module, Bundle bundle) throws DeploymentException {
        if (Boolean.getBoolean(USE_WSGEN_PROPERTY)) {
            generateWSDL(serviceClass, portInfo, module, bundle);
        }
    }

    protected WsdlGenerator getWsdlGenerator() throws DeploymentException {
        if (this.wsdlGenerators == null || this.wsdlGenerators.isEmpty()) {
            throw new DeploymentException("Wsdl generator not found");
        } else {
            return this.wsdlGenerators.iterator().next();
        }
    }

    private void generateWSDL(Class serviceClass, PortInfo portInfo, Module module, Bundle bundle)
        throws DeploymentException {
        String serviceName = (portInfo.getServiceName() == null ? serviceClass.getName() : portInfo.getServiceName());
        String wsdlFile = portInfo.getWsdlFile();
        if (isWsdlSet(portInfo, serviceClass, bundle)) {

            portInfo.setWsdlFile(JAXWSBuilderUtils.normalizeWsdlPath(module, wsdlFile));

            if (LOG.isDebugEnabled()) {
                LOG.debug("Service " + serviceName + " has WSDL.");
            }
            return;
        }

        if (isHTTPBinding(portInfo, serviceClass)) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Service " + serviceName + " has HTTPBinding.");
            }
            return;
        }

        if (JAXWSUtils.isWebServiceProvider(serviceClass)) {
            if (ignoreEmptyWebServiceProviderWSDL) {
                LOG.warn("WSDL is not specified for @WebServiceProvider service " + serviceName);
                //TODO Generate a dummy WSDL for it ?
                return;
            } else {
                throw new DeploymentException("WSDL must be specified for @WebServiceProvider service " + serviceName);
            }
        }

        if (LOG.isDebugEnabled()) {
            LOG.debug("Service " + serviceName + " does not have WSDL. Generating WSDL...");
        }

        WsdlGenerator wsdlGenerator = getWsdlGenerator();

        WsdlGeneratorOptions options = new WsdlGeneratorOptions();
        options.setSAAJ(WsdlGeneratorOptions.SAAJ.SUN);

        JaxWsImplementorInfo serviceInfo = new JaxWsImplementorInfo(serviceClass);

        // set wsdl service
        if (portInfo.getWsdlService() == null) {
            options.setWsdlService(serviceInfo.getServiceName());
        } else {
            options.setWsdlService(portInfo.getWsdlService());
        }

        // set wsdl port
        if (portInfo.getWsdlPort() != null) {
            options.setWsdlPort(portInfo.getWsdlPort());
        }

        wsdlFile = wsdlGenerator.generateWsdl(module, serviceClass.getName(), module.getEarContext(), options);
        portInfo.setWsdlFile(wsdlFile);
        if (LOG.isDebugEnabled()) {
            LOG.debug("Generated " + wsdlFile + " for service " + serviceName);
        }
    }

    public static final GBeanInfo GBEAN_INFO;

    static {
        GBeanInfoBuilder infoBuilder = GBeanInfoBuilder.createStatic(CXFBuilder.class, NameFactory.MODULE_BUILDER);
        infoBuilder.addInterface(WebServiceBuilder.class);
        infoBuilder.addAttribute("defaultEnvironment", Environment.class, true, true);
        infoBuilder.addReference("WsdlGenerator", WsdlGenerator.class, GBeanInfoBuilder.DEFAULT_J2EE_TYPE);
        infoBuilder.setConstructor(new String[]{"defaultEnvironment", "WsdlGenerator"});

        GBEAN_INFO = infoBuilder.getBeanInfo();
    }

    public static GBeanInfo getGBeanInfo() {
        return GBEAN_INFO;
    }

}
