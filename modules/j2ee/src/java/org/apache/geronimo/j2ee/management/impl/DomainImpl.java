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

package org.apache.geronimo.j2ee.management.impl;

import java.util.Collection;

import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.gbean.GBeanInfoFactory;
import org.apache.geronimo.j2ee.management.J2EEServer;

/**
 * 
 * 
 * @version $Revision: 1.2 $ $Date: 2004/02/25 09:57:41 $
 */
public class DomainImpl {
    private final Collection servers;

    public DomainImpl(Collection servers) {
        this.servers = servers;
    }

    public String[] getservers() {
        return Util.getObjectNames(servers);
    }

    public static final GBeanInfo GBEAN_INFO;

    static {
        GBeanInfoFactory infoFactory = new GBeanInfoFactory(DomainImpl.class);
        infoFactory.addAttribute("servers", false);
        infoFactory.addReference("Servers", J2EEServer.class);
        infoFactory.setConstructor(
                new String[] {"Servers"},
                new Class[] {Collection.class}
        );
        GBEAN_INFO = infoFactory.getBeanInfo();
    }

    public static GBeanInfo getGBeanInfo() {
        return GBEAN_INFO;
    }
}
