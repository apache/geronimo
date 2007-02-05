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
import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.gbean.GBeanInfoBuilder;
import org.apache.geronimo.j2ee.deployment.Module;
import org.apache.geronimo.j2ee.j2eeobjectnames.NameFactory;
import org.apache.geronimo.jaxws.builder.JAXWSServiceRefBuilder;
import org.apache.geronimo.kernel.repository.Environment;
import org.apache.geronimo.naming.deployment.ServiceRefBuilder;
import org.apache.geronimo.xbeans.geronimo.naming.GerServiceRefType;
import org.apache.geronimo.xbeans.javaee.ServiceRefType;

public class CXFServiceRefBuilder extends JAXWSServiceRefBuilder {

    private final CXFBuilder cxfBuilder;
    
    public CXFServiceRefBuilder(Environment defaultEnvironment,
                                String[] eeNamespaces,
                                CXFBuilder cxfBuilder) {
        super(defaultEnvironment, eeNamespaces);
        this.cxfBuilder = cxfBuilder;
    }
       
    public Object createService(ServiceRefType serviceRef, GerServiceRefType gerServiceRef, 
                                Module module, ClassLoader cl, Class serviceInterfaceClass, 
                                QName serviceQName, URI wsdlURI, Class serviceReferenceType, 
                                Map portComponentRefMap) throws DeploymentException {   
        return this.cxfBuilder.createService(serviceInterfaceClass, serviceReferenceType, wsdlURI, 
                                             serviceQName, portComponentRefMap, serviceRef.getHandlerChains(),
                                             gerServiceRef, module, cl);
    }
    
    public static final GBeanInfo GBEAN_INFO;

    static {
        GBeanInfoBuilder infoBuilder = GBeanInfoBuilder.createStatic(
                CXFServiceRefBuilder.class, NameFactory.MODULE_BUILDER);
        infoBuilder.addInterface(ServiceRefBuilder.class);
        infoBuilder.addAttribute("defaultEnvironment", Environment.class, true,
                true);
        infoBuilder.addAttribute("eeNamespaces", String[].class, true, true);
        infoBuilder.addReference("CXFBuilder", CXFBuilder.class,
                NameFactory.MODULE_BUILDER);

        infoBuilder.setConstructor(new String[] { "defaultEnvironment",
                "eeNamespaces", "CXFBuilder" });

        GBEAN_INFO = infoBuilder.getBeanInfo();
    }

    public static GBeanInfo getGBeanInfo() {
        return GBEAN_INFO;
    }
}
