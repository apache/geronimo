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

package org.apache.geronimo.cxf;

import org.apache.cxf.Bus;
import org.apache.cxf.bus.cxf.CXFBusFactory;
import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.gbean.GBeanInfoBuilder;
import org.apache.geronimo.j2ee.j2eeobjectnames.NameFactory;
import org.apache.geronimo.webservices.WebServiceContainer;
import org.apache.geronimo.webservices.WebServiceContainerFactory;

/**
 * @version $Rev$ $Date$
 */
public class CXFWebServiceContainerFactoryGBean implements WebServiceContainerFactory {

    private final PortInfo portInfo;
    private final Bus bus;
    private final Object endpointInstance;

    public CXFWebServiceContainerFactoryGBean(PortInfo portInfo, String endpointClassName, ClassLoader classLoader) throws ClassNotFoundException, IllegalAccessException, InstantiationException {
        this.portInfo = portInfo;
        this.bus = new CXFBusFactory().getDefaultBus();
        Class endpointClass = classLoader.loadClass(endpointClassName);
        endpointInstance = endpointClass.newInstance();
    }

    public WebServiceContainer getWebServiceContainer() {
        return new CXFWebServiceContainer(portInfo, endpointInstance, bus);
    }

    public static final GBeanInfo GBEAN_INFO;

    static {
        GBeanInfoBuilder infoBuilder = GBeanInfoBuilder.createStatic(CXFWebServiceContainerFactoryGBean.class, NameFactory.GERONIMO_SERVICE);
        infoBuilder.addAttribute("portInfo", PortInfo.class, true, true);
        infoBuilder.addAttribute("endpointClassName", String.class, true, true);
        infoBuilder.addAttribute("classLoader", ClassLoader.class, false);
        infoBuilder.setConstructor(new String[] {"portInfo", "endpointClassName", "classLoader"});
        GBEAN_INFO = infoBuilder.getBeanInfo();
    }

    public static GBeanInfo getGBeanInfo() {
        return GBEAN_INFO;
    }
}
