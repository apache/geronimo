/**
 *
 * Copyright 2005 The Apache Software Foundation
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.apache.geronimo.connector.outbound;

import org.apache.geronimo.connector.ResourceAdapterWrapper;
import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.gbean.GBeanInfoBuilder;
import org.apache.geronimo.j2ee.j2eeobjectnames.NameFactory;
import org.apache.geronimo.kernel.Kernel;
import org.apache.geronimo.transaction.manager.ResourceManager;
import org.apache.geronimo.management.JCAManagedConnectionFactory;

/**
 *
 * @version $Revision$
 */
public class ManagedConnectionFactoryWrapperGBean {

    public static final GBeanInfo GBEAN_INFO;

    static {
        GBeanInfoBuilder infoFactory = new GBeanInfoBuilder(ManagedConnectionFactoryWrapper.class, NameFactory.JCA_MANAGED_CONNECTION_FACTORY);

        infoFactory.addAttribute("managedConnectionFactoryClass", String.class, true);
        infoFactory.addAttribute("connectionFactoryInterface", String.class, true);
        infoFactory.addAttribute("implementedInterfaces", String[].class, true);
        infoFactory.addAttribute("connectionFactoryImplClass", String.class, true);
        infoFactory.addAttribute("connectionInterface", String.class, true);
        infoFactory.addAttribute("connectionImplClass", String.class, true);
        infoFactory.addAttribute("kernel", Kernel.class, false);
        infoFactory.addAttribute("objectName", String.class, false);
        infoFactory.addAttribute("classLoader", ClassLoader.class, false);

        infoFactory.addOperation("$getResource");
        infoFactory.addOperation("$getConnectionFactory");
        infoFactory.addOperation("$getManagedConnectionFactory");

        infoFactory.addInterface(ResourceManager.class);
        infoFactory.addInterface(JCAManagedConnectionFactory.class);

        infoFactory.addReference("ResourceAdapterWrapper", ResourceAdapterWrapper.class, NameFactory.RESOURCE_ADAPTER);
        infoFactory.addReference("ConnectionManagerContainer", ConnectionManagerContainer.class, NameFactory.JCA_CONNECTION_MANAGER);

        infoFactory.setConstructor(new String[]{
            "managedConnectionFactoryClass",
            "connectionFactoryInterface",
            "implementedInterfaces",
            "connectionFactoryImplClass",
            "connectionInterface",
            "connectionImplClass",
            "ResourceAdapterWrapper",
            "ConnectionManagerContainer",
            "kernel",
            "objectName",
            "classLoader"});

        GBEAN_INFO = infoFactory.getBeanInfo();
    }

    public static GBeanInfo getGBeanInfo() {
        return GBEAN_INFO;
    }

}
