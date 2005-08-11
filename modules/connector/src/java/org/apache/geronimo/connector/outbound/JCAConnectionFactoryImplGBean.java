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

import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.gbean.GBeanInfoBuilder;
import org.apache.geronimo.j2ee.j2eeobjectnames.NameFactory;
import org.apache.geronimo.j2ee.management.J2EEServer;

/**
 * 
 * @version $Revision$
 */
public class JCAConnectionFactoryImplGBean {
    
    public static final GBeanInfo GBEAN_INFO;

    static {
        GBeanInfoBuilder infoFactory = new GBeanInfoBuilder(JCAConnectionFactoryImpl.class, NameFactory.JCA_CONNECTION_FACTORY);
        infoFactory.addReference("J2EEServer", J2EEServer.class);

        infoFactory.addAttribute("objectName", String.class, false);
//        infoFactory.addAttribute("server", String.class, false);
        infoFactory.addAttribute("managedConnectionFactory", String.class, true);

        infoFactory.setConstructor(new String[]{"objectName", "J2EEServer", "managedConnectionFactory"});

        GBEAN_INFO = infoFactory.getBeanInfo();
    }

    public static GBeanInfo getGBeanInfo() {
        return GBEAN_INFO;
    }

}
