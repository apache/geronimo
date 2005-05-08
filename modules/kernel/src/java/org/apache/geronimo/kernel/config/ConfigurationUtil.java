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
package org.apache.geronimo.kernel.config;

import java.util.Set;
import javax.management.ObjectName;

import org.apache.geronimo.kernel.jmx.JMXUtil;
import org.apache.geronimo.kernel.Kernel;

/**
 * @version $Rev$ $Date$
 */
public final class ConfigurationUtil {
    private static final ObjectName CONFIGURATION_MANAGER_QUERY = JMXUtil.getObjectName("*:j2eeType=ConfigurationManager,*");

    private ConfigurationUtil() {
    }

    public static ConfigurationManager getConfigurationManager(Kernel kernel) {
        // todo cache this
        Set names = kernel.listGBeans(CONFIGURATION_MANAGER_QUERY);
        if (names.isEmpty()) {
            throw new IllegalStateException("Configuration mananger could not be found in kernel: " + CONFIGURATION_MANAGER_QUERY);
        }
        if (names.size() > 1) {
            throw new IllegalStateException("More then one configuration mananger was found in kernel: " + CONFIGURATION_MANAGER_QUERY);
        }
        ObjectName configurationManagerName = (ObjectName) names.iterator().next();
        return (ConfigurationManager) kernel.getProxyManager().createProxy(configurationManagerName, ConfigurationManager.class);
    }
}
