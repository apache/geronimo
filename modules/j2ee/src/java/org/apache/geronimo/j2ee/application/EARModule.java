/**
 *
 * Copyright 2004 The Apache Software Foundation
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

package org.apache.geronimo.j2ee.application;

import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.gbean.GBeanInfoFactory;
import org.apache.geronimo.j2ee.management.impl.DeployedObjectImpl;
import org.apache.geronimo.j2ee.management.J2EEServer;

/**
 *
 *
 * @version $Revision: 1.2 $ $Date: 2004/02/25 09:57:41 $
 */
public class EARModule extends DeployedObjectImpl {
    private final String[] modules;

    public EARModule(String dd, J2EEServer server, String[] modules) {
        super(dd, server);
        this.modules = modules;
    }

    /**
     * Return modules in this application
     * @see "JSR77.3.6.1.1"
     * @return the modules in this application
     */
    public String[] getmodules() {
        return (String[]) modules.clone();
    }

    public static final GBeanInfo GBEAN_INFO;

    static {
        GBeanInfoFactory infoFactory = new GBeanInfoFactory(EARModule.class, DeployedObjectImpl.GBEAN_INFO);
        infoFactory.addAttribute("modules", true);
        infoFactory.setConstructor(
                new String[]{"deploymentDescriptor", "server", "modules"},
                new Class[]{String.class, J2EEServer.class, String[].class}
        );
        GBEAN_INFO = infoFactory.getBeanInfo();
    }

    public static GBeanInfo getGBeanInfo() {
        return GBEAN_INFO;
    }
}
