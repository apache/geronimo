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
package org.apache.geronimo.system.properties;

import java.util.Iterator;
import java.util.Properties;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.gbean.GBeanInfoBuilder;
import org.apache.geronimo.system.serverinfo.ServerInfo;

/**
 * @version $Rev$ $Date$
 */
public class SystemProperties
{
    private static final Logger log = LoggerFactory.getLogger(SystemProperties.class);

    public SystemProperties(Properties systemProperties, Properties systemPathProperties, ServerInfo serverInfo, Properties sunSystemProperties, Properties ibmSystemProperties, Properties apacheSystemProperties) {
        if (log.isDebugEnabled()) log.debug("Setting systemProperties");
        setProperties(systemProperties, null);

        if (JvmVendor.isIBM()) {
            if (log.isDebugEnabled()) log.debug("Setting ibmSystemProperties for the IBM JVM");
            setProperties(ibmSystemProperties, null);
        } else if (JvmVendor.isApache()) {
            if (log.isDebugEnabled()) log.debug("Setting apacheSystemProperties for the Apache Harmony JVM");
            setProperties(apacheSystemProperties, null);
        } else {
            if (log.isDebugEnabled()) log.debug("Setting sunSystemProperties for the Sun JVM");
            setProperties(sunSystemProperties, null);
        }

        if (serverInfo != null) {
            if (log.isDebugEnabled()) log.debug("Setting systemPathProperties");
            setProperties(systemPathProperties, serverInfo);
        }
    }

    private void setProperties(Properties properties, ServerInfo serverInfo) {
        if (properties != null) {
            for (Iterator iterator = properties.entrySet().iterator(); iterator.hasNext();) {
                Map.Entry entry = (Map.Entry) iterator.next();
                String propertyName = (String) entry.getKey();
                String propertyValue = (String) entry.getValue();
                if (serverInfo != null) {
                    propertyValue = serverInfo.resolvePath(propertyValue);
                }
                String currentPropertyValue = System.getProperty(propertyName);
                if (currentPropertyValue == null) {
                    System.setProperty(propertyName, propertyValue);
                    log.info("Setting Property=" + propertyName + " to Value=" + propertyValue);
                } else {
                    if (currentPropertyValue.equals(propertyValue)) {
                        log.warn("Existing Property=" + propertyName + " is already set to Value=" + currentPropertyValue);
                    } else {
                        log.error("Not updating existing Property=" + propertyName + " to Value=" + propertyValue + ".  Property is already set to " + currentPropertyValue);
                    }
                }
            }
        }
    }

    public static final GBeanInfo GBEAN_INFO;

    static {
        GBeanInfoBuilder infoBuilder = GBeanInfoBuilder.createStatic(SystemProperties.class, "GBean");
        infoBuilder.addAttribute("systemProperties", Properties.class, true, true);
        infoBuilder.addAttribute("systemPathProperties", Properties.class, true, true);
        infoBuilder.addReference("ServerInfo", ServerInfo.class, "GBean");
        infoBuilder.addAttribute("sunSystemProperties", Properties.class, true, true);
        infoBuilder.addAttribute("ibmSystemProperties", Properties.class, true, true);
        infoBuilder.addAttribute("apacheSystemProperties", Properties.class, true, true);
        infoBuilder.setConstructor(new String[] { "systemProperties", "systemPathProperties", "ServerInfo", "sunSystemProperties", "ibmSystemProperties", "apacheSystemProperties" });

        GBEAN_INFO = infoBuilder.getBeanInfo();
    }

    public static GBeanInfo getGBeanInfo() {
        return GBEAN_INFO;
    }

}
