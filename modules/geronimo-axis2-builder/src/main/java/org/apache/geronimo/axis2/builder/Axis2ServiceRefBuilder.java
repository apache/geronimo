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

import org.apache.geronimo.jaxws.builder.JAXWSServiceRefBuilder;
import org.apache.geronimo.kernel.GBeanAlreadyExistsException;
import org.apache.geronimo.kernel.GBeanNotFoundException;
import org.apache.geronimo.kernel.repository.Environment;
import org.apache.geronimo.xbeans.javaee.PortComponentRefType;
import org.apache.geronimo.xbeans.javaee.ServiceRefType;
import org.apache.geronimo.xbeans.geronimo.naming.GerServiceRefType;
import org.apache.geronimo.j2ee.deployment.EARContext;
import org.apache.geronimo.j2ee.deployment.Module;
import org.apache.geronimo.j2ee.j2eeobjectnames.NameFactory;
import org.apache.geronimo.axis2.client.Axis2ConfigGBean;
import org.apache.geronimo.common.DeploymentException;
import org.apache.geronimo.gbean.AbstractName;
import org.apache.geronimo.gbean.GBeanData;
import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.gbean.GBeanInfoBuilder;
import org.apache.geronimo.naming.deployment.ServiceRefBuilder;

import javax.xml.namespace.QName;
import java.net.URI;
import java.util.Map;

public class Axis2ServiceRefBuilder extends JAXWSServiceRefBuilder {

    private final Axis2Builder axis2Builder;

    public Axis2ServiceRefBuilder(Environment defaultEnvironment,
                                String[] eeNamespaces,
                                Axis2Builder axis2Builder) {
        super(defaultEnvironment, eeNamespaces);
        this.axis2Builder = axis2Builder;
    }

    public Object createService(ServiceRefType serviceRef, GerServiceRefType gerServiceRef,
                                Module module, ClassLoader cl, Class serviceInterfaceClass,
                                QName serviceQName, URI wsdlURI, Class serviceReferenceType,
                                Map<Class, PortComponentRefType> portComponentRefMap) throws DeploymentException {
        registerConfigGBean(module);
        return this.axis2Builder.createService(serviceInterfaceClass, serviceReferenceType, wsdlURI,
                                             serviceQName, portComponentRefMap, serviceRef.getHandlerChains(),
                                             gerServiceRef, module, cl);
    }

    private void registerConfigGBean(Module module) throws DeploymentException {
        EARContext context = module.getEarContext();
        AbstractName containerFactoryName = context.getNaming().createChildName(
                module.getModuleName(), Axis2ConfigGBean.GBEAN_INFO.getName(),
                NameFactory.GERONIMO_SERVICE);

        try {
            context.getGBeanInstance(containerFactoryName);
        } catch (GBeanNotFoundException e1) {
            GBeanData configGBeanData = new GBeanData(containerFactoryName, Axis2ConfigGBean.GBEAN_INFO);
            configGBeanData.setAttribute("moduleName", module.getModuleName());
            
            try {
                context.addGBean(configGBeanData);
            } catch (GBeanAlreadyExistsException e) {
                throw new DeploymentException("Could not add config gbean", e);
            }
        }
    }
    
    public static final GBeanInfo GBEAN_INFO;

    static {
        GBeanInfoBuilder infoBuilder = GBeanInfoBuilder.createStatic(
                Axis2ServiceRefBuilder.class, NameFactory.MODULE_BUILDER);
        infoBuilder.addInterface(ServiceRefBuilder.class);
        infoBuilder.addAttribute("defaultEnvironment", Environment.class, true,
                true);
        infoBuilder.addAttribute("eeNamespaces", String[].class, true, true);
        infoBuilder.addReference("Axis2Builder", Axis2Builder.class,
                NameFactory.MODULE_BUILDER);

        infoBuilder.setConstructor(new String[] { "defaultEnvironment",
                "eeNamespaces", "Axis2Builder" });

        GBEAN_INFO = infoBuilder.getBeanInfo();
    }

    public static GBeanInfo getGBeanInfo() {
        return Axis2ServiceRefBuilder.GBEAN_INFO;
    }
}
