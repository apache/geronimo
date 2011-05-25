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

import java.net.URI;
import java.util.Collections;
import java.util.Map;

import javax.xml.namespace.QName;

import org.apache.geronimo.axis2.client.Axis2ConfigGBean;
import org.apache.geronimo.axis2.client.Axis2ServiceReference;
import org.apache.geronimo.axis2.osgi.Axis2ModuleRegistry;
import org.apache.geronimo.common.DeploymentException;
import org.apache.geronimo.gbean.AbstractName;
import org.apache.geronimo.gbean.AbstractNameQuery;
import org.apache.geronimo.gbean.GBeanData;
import org.apache.geronimo.gbean.GBeanInfoBuilder;
import org.apache.geronimo.gbean.annotation.GBean;
import org.apache.geronimo.gbean.annotation.ParamAttribute;
import org.apache.geronimo.j2ee.deployment.EARContext;
import org.apache.geronimo.j2ee.deployment.Module;
import org.apache.geronimo.j2ee.j2eeobjectnames.NameFactory;
import org.apache.geronimo.jaxws.builder.EndpointInfoBuilder;
import org.apache.geronimo.jaxws.builder.JAXWSBuilderUtils;
import org.apache.geronimo.jaxws.builder.JAXWSServiceRefBuilder;
import org.apache.geronimo.jaxws.client.EndpointInfo;
import org.apache.geronimo.jaxws.info.HandlerChainsInfo;
import org.apache.geronimo.kernel.GBeanAlreadyExistsException;
import org.apache.geronimo.kernel.GBeanNotFoundException;
import org.apache.geronimo.kernel.repository.Artifact;
import org.apache.geronimo.kernel.repository.Environment;
import org.apache.geronimo.naming.reference.JndiReference;
import org.apache.geronimo.xbeans.geronimo.naming.GerServiceRefType;
import org.apache.openejb.jee.PortComponentRef;
import org.apache.openejb.jee.ServiceRef;
import org.osgi.framework.Bundle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@GBean(j2eeType = NameFactory.MODULE_BUILDER)
public class Axis2ServiceRefBuilder extends JAXWSServiceRefBuilder {

    private static final Logger log = LoggerFactory.getLogger(Axis2ServiceRefBuilder.class);

    public Axis2ServiceRefBuilder(@ParamAttribute(name = "defaultEnvironment") Environment defaultEnvironment, @ParamAttribute(name = "eeNamespaces") String[] eeNamespaces) {
        super(defaultEnvironment, eeNamespaces);
    }

    @Override
    protected Object createService(ServiceRef serviceRef, GerServiceRefType gerServiceRef, Module module, Bundle bundle, Class serviceInterfaceClass, QName serviceQName, URI wsdlURI,
            Class serviceReferenceType, Map<Class<?>, PortComponentRef> portComponentRefMap) throws DeploymentException {
        registerConfigGBean(module);

        if(serviceRef.getLookupName() != null && !serviceRef.getLookupName().isEmpty()) {
            return new JndiReference(serviceRef.getLookupName());
        }

        EndpointInfoBuilder builder = new EndpointInfoBuilder(serviceInterfaceClass, gerServiceRef, portComponentRefMap, module, bundle, wsdlURI, serviceQName);
        builder.build();

        wsdlURI = builder.getWsdlURI();
        //TODO For non standalone web application, it is embbed of directory style in the EAR package
        wsdlURI = JAXWSBuilderUtils.normalizeWsdlPath(module, wsdlURI);

        serviceQName = builder.getServiceQName();
        Map<Object, EndpointInfo> seiInfoMap = builder.getEndpointInfo();

        HandlerChainsInfo handlerChainsInfo = null;
        if(serviceRef.getHandlerChains() != null) {
            handlerChainsInfo = handlerChainsInfoBuilder.build(serviceRef.getHandlerChains());
        }

        String serviceReferenceName = (serviceReferenceType == null) ? null : serviceReferenceType.getName();

        return new Axis2ServiceReference(serviceInterfaceClass.getName(), serviceReferenceName, wsdlURI, serviceQName, module.getModuleName(), handlerChainsInfo, seiInfoMap);
    }

    private void registerConfigGBean(Module module) throws DeploymentException {
        EARContext context = module.getEarContext();
        AbstractName containerFactoryName = context.getNaming().createChildName(module.getModuleName(), "Axis2ConfigGBean", GBeanInfoBuilder.DEFAULT_J2EE_TYPE);

        try {
            context.getGBeanInstance(containerFactoryName);
        } catch (GBeanNotFoundException e1) {
            GBeanData configGBeanData = new GBeanData(containerFactoryName, Axis2ConfigGBean.class);
            configGBeanData.setAttribute("moduleName", module.getModuleName());
            configGBeanData.setReferencePattern("Axis2ModuleRegistry", new AbstractNameQuery(Artifact.create("org.apache.geronimo.configs/axis2//car"), Collections.emptyMap(),
                    Axis2ModuleRegistry.class.getName()));
            try {
                context.addGBean(configGBeanData);
            } catch (GBeanAlreadyExistsException e) {
                throw new DeploymentException("Could not add config gbean", e);
            }
        }
    }

}
