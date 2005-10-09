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
package org.apache.geronimo.system.properties;

import java.util.Iterator;
import java.util.Properties;
import java.util.Map;

import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.gbean.GBeanInfoBuilder;
import org.apache.geronimo.system.serverinfo.ServerInfo;

/**
 * @version $Rev:  $ $Date:  $
 */
public class SystemProperties {


    public SystemProperties(Properties properties, Properties pathProperties, ServerInfo serverInfo) {
        if (properties != null) {
            for (Iterator iterator = properties.entrySet().iterator(); iterator.hasNext();) {
                Map.Entry entry = (Map.Entry) iterator.next();
                String propertyName = (String) entry.getKey();
                String propertyValue = (String) entry.getValue();
                if (System.getProperty(propertyName) == null) {
                    System.setProperty(propertyName, propertyValue);
                }
            }
        }
        if (pathProperties != null && serverInfo != null) {
            for (Iterator iterator = pathProperties.entrySet().iterator(); iterator.hasNext();) {
                Map.Entry entry = (Map.Entry) iterator.next();
                String propertyName = (String) entry.getKey();
                String propertyValue = (String) entry.getValue();
                propertyValue = serverInfo.resolvePath(propertyValue);
                if (System.getProperty(propertyName) == null) {
                    System.setProperty(propertyName, propertyValue);
                }
            }
        }
    }

    public static final GBeanInfo GBEAN_INFO;

    static {
        GBeanInfoBuilder infoBuilder = new GBeanInfoBuilder(SystemProperties.class, "GBean");
        infoBuilder.addAttribute("systemProperties", Properties.class, true, true);
        infoBuilder.addAttribute("systemPathProperties", Properties.class, true, true);
        infoBuilder.addReference("ServerInfo", ServerInfo.class, "GBean");
        infoBuilder.setConstructor(new String[] {"systemProperties", "systemPathProperties", "ServerInfo"});

        GBEAN_INFO = infoBuilder.getBeanInfo();
    }

    public static GBeanInfo getGBeanInfo() {
        return GBEAN_INFO;
    }

}
