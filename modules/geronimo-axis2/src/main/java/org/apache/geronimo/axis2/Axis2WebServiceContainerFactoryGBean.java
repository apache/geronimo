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

package org.apache.geronimo.axis2;

import javax.wsdl.Definition;

import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.gbean.GBeanInfoBuilder;
import org.apache.geronimo.j2ee.j2eeobjectnames.NameFactory;
import org.apache.geronimo.webservices.WebServiceContainer;
import org.apache.geronimo.webservices.WebServiceContainerFactory;

public class Axis2WebServiceContainerFactoryGBean implements WebServiceContainerFactory {

    private final PortInfo portInfo;
    private final String endpointClassName;
    private final ClassLoader classLoader;
    private final Definition wsdlDefinition;

    public Axis2WebServiceContainerFactoryGBean(PortInfo portInfo, String endpointClassName, Definition wsdlDefinition, ClassLoader classLoader) throws InstantiationException, IllegalAccessException, ClassNotFoundException {
        this.portInfo = portInfo;
        this.classLoader = classLoader;
        this.endpointClassName = endpointClassName;
        this.wsdlDefinition = wsdlDefinition;
    }

    public WebServiceContainer getWebServiceContainer() {
        return new Axis2WebServiceContainer(portInfo, endpointClassName, wsdlDefinition, classLoader);
    }

    public static final GBeanInfo GBEAN_INFO;

    static {
        GBeanInfoBuilder infoBuilder = GBeanInfoBuilder.createStatic(Axis2WebServiceContainerFactoryGBean.class, NameFactory.GERONIMO_SERVICE);
        infoBuilder.addAttribute("portInfo", PortInfo.class, true, true);
        infoBuilder.addAttribute("endpointClassName", String.class, true, true);
        infoBuilder.addAttribute("wsdlDefinition", Definition.class, true, true);
        infoBuilder.addAttribute("classLoader", ClassLoader.class, false);
        infoBuilder.setConstructor(new String[]{"portInfo", "endpointClassName", "wsdlDefinition", "classLoader"});
        GBEAN_INFO = infoBuilder.getBeanInfo();
    }

    public static GBeanInfo getGBeanInfo() {
        return GBEAN_INFO;
    }
}
