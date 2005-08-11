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

package org.apache.geronimo.connector;

import javax.resource.spi.endpoint.MessageEndpointFactory;

import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.gbean.GBeanInfoBuilder;
import org.apache.geronimo.j2ee.j2eeobjectnames.NameFactory;
import org.apache.geronimo.transaction.manager.ResourceManager;

/**
 * 
 * @version $Revision$
 */
public class ActivationSpecWrapperGBean {
    
    public static final GBeanInfo GBEAN_INFO;

    static {
        GBeanInfoBuilder infoBuilder = new GBeanInfoBuilder(ActivationSpecWrapper.class, NameFactory.JCA_ACTIVATION_SPEC);
        infoBuilder.addAttribute("activationSpecClass", String.class, true);
        infoBuilder.addAttribute("containerId", String.class, true);
        infoBuilder.addAttribute("classLoader", ClassLoader.class, false);

        infoBuilder.addReference("ResourceAdapterWrapper", ResourceAdapterWrapper.class, NameFactory.RESOURCE_ADAPTER);

        infoBuilder.addOperation("activate", new Class[]{MessageEndpointFactory.class});
        infoBuilder.addOperation("deactivate", new Class[]{MessageEndpointFactory.class});

        infoBuilder.addInterface(ResourceManager.class);

        infoBuilder.setConstructor(new String[]{
            "activationSpecClass",
            "containerId",
            "ResourceAdapterWrapper",
            "classLoader"});

        GBEAN_INFO = infoBuilder.getBeanInfo();
    }

    public static GBeanInfo getGBeanInfo() {
        return GBEAN_INFO;
    }

}
