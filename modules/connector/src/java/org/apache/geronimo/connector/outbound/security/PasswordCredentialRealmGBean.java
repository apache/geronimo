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

package org.apache.geronimo.connector.outbound.security;

import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.gbean.GBeanInfoBuilder;
import org.apache.geronimo.j2ee.j2eeobjectnames.NameFactory;
import org.apache.geronimo.kernel.Kernel;
import org.apache.geronimo.security.jaas.ConfigurationEntryFactory;

/**
 * 
 * @version $Revision$
 */
public class PasswordCredentialRealmGBean {

    private static final GBeanInfo GBEAN_INFO;

    static {
        GBeanInfoBuilder infoFactory = new GBeanInfoBuilder(PasswordCredentialRealm.class, NameFactory.SECURITY_REALM);

        infoFactory.addInterface(ManagedConnectionFactoryListener.class);
        infoFactory.addInterface(ConfigurationEntryFactory.class);
        infoFactory.addAttribute("kernel", Kernel.class, false);
        infoFactory.addAttribute("realmName", String.class, true);

        infoFactory.setConstructor(new String[]{"kernel", "realmName"});

        GBEAN_INFO = infoFactory.getBeanInfo();
    }

    public static GBeanInfo getGBeanInfo() {
        return GBEAN_INFO;
    }

}
