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

import java.net.URI;
import java.util.Map;

import javax.xml.namespace.QName;

import org.apache.geronimo.common.DeploymentException;
import org.apache.geronimo.cxf.client.CXFServiceReference;
import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.gbean.GBeanInfoBuilder;
import org.apache.geronimo.j2ee.deployment.Module;
import org.apache.geronimo.j2ee.j2eeobjectnames.NameFactory;
import org.apache.geronimo.jaxws.builder.EndpointInfoBuilder;
import org.apache.geronimo.jaxws.builder.JAXWSBuilderUtils;
import org.apache.geronimo.jaxws.builder.JAXWSServiceRefBuilder;
import org.apache.geronimo.jaxws.client.EndpointInfo;
import org.apache.geronimo.jaxws.info.HandlerChainsInfo;
import org.apache.geronimo.kernel.repository.Environment;
import org.apache.geronimo.naming.deployment.ServiceRefBuilder;
import org.apache.geronimo.naming.reference.JndiReference;
import org.apache.geronimo.xbeans.geronimo.naming.GerServiceRefType;
import org.apache.openejb.jee.PortComponentRef;
import org.apache.openejb.jee.ServiceRef;
import org.osgi.framework.Bundle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CXFServiceRefBuilder extends JAXWSServiceRefBuilder {

    private static final Logger LOG = LoggerFactory.getLogger(CXFServiceRefBuilder.class);

    public CXFServiceRefBuilder(Environment defaultEnvironment,
                                String[] eeNamespaces) {
        super(defaultEnvironment, eeNamespaces);
    }

    @Override
    public Object createService(ServiceRef serviceRef, GerServiceRefType gerServiceRef,
                                Module module, Bundle bundle, Class serviceInterface,
                                QName serviceQName, URI wsdlURI, Class serviceReference,
                                Map<Class<?>, PortComponentRef> portComponentRefMap) throws DeploymentException {

        if(serviceRef.getLookupName() != null && !serviceRef.getLookupName().isEmpty()) {
            return new JndiReference(serviceRef.getLookupName());
        }

        EndpointInfoBuilder builder = new EndpointInfoBuilder(serviceInterface,
                gerServiceRef, portComponentRefMap, module, bundle,
                wsdlURI, serviceQName);
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
        String serviceReferenceName = (serviceReference == null) ? null : serviceReference.getName();
        return new CXFServiceReference(serviceInterface.getName(), serviceReferenceName, wsdlURI, serviceQName, module.getModuleName(), handlerChainsInfo, seiInfoMap);
    }

    public static final GBeanInfo GBEAN_INFO;

    static {
        GBeanInfoBuilder infoBuilder = GBeanInfoBuilder.createStatic(
                CXFServiceRefBuilder.class, NameFactory.MODULE_BUILDER);
        infoBuilder.addInterface(ServiceRefBuilder.class);
        infoBuilder.addAttribute("defaultEnvironment", Environment.class, true,
                true);
        infoBuilder.addAttribute("eeNamespaces", String[].class, true, true);

        infoBuilder.setConstructor(new String[] { "defaultEnvironment",
                                                  "eeNamespaces"});

        GBEAN_INFO = infoBuilder.getBeanInfo();
    }

    public static GBeanInfo getGBeanInfo() {
        return GBEAN_INFO;
    }
}
