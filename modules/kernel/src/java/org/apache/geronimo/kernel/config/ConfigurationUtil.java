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
import java.util.Iterator;
import javax.management.ObjectName;

import org.apache.geronimo.kernel.jmx.JMXUtil;
import org.apache.geronimo.kernel.Kernel;
import org.apache.geronimo.kernel.GBeanNotFoundException;
import org.apache.geronimo.kernel.management.State;
import org.apache.geronimo.gbean.GBeanQuery;

/**
 * @version $Rev$ $Date$
 */
public final class ConfigurationUtil {
    private ConfigurationUtil() {
    }

    /**
     * Gets a reference or proxy to the ConfigurationManager running in the specified kernel.
     *
     * @return The ConfigurationManager
     *
     * @throws IllegalStateException Occurs if a ConfigurationManager cannot be identified
     */
    public static ConfigurationManager getConfigurationManager(Kernel kernel) {
        Set names = kernel.listGBeans(new GBeanQuery(null, ConfigurationManager.class.getName()));
        for (Iterator iterator = names.iterator(); iterator.hasNext();) {
            ObjectName objectName = (ObjectName) iterator.next();
            try {
                if (kernel.getGBeanState(objectName) != State.RUNNING_INDEX) {
                    iterator.remove();
                }
            } catch (GBeanNotFoundException e) {
                // bean died
                iterator.remove();
            }
        }
        if (names.isEmpty()) {
            throw new IllegalStateException("Configuration mananger could not be found in kernel");
        }
        if (names.size() > 1) {
            throw new IllegalStateException("More than one configuration mananger was found in kernel");
        }
        ObjectName configurationManagerName = (ObjectName) names.iterator().next();
        return (ConfigurationManager) kernel.getProxyManager().createProxy(configurationManagerName, ConfigurationManager.class);
    }

    /**
     * Gets a reference or proxy to an EditableConfigurationManager running in the specified kernel, if there is one.
     *
     * @return The EdtiableConfigurationManager, or none if there is not one available.
     *
     * @throws IllegalStateException Occurs if there are multiple EditableConfigurationManagers in the kernel.
     */
    public static EditableConfigurationManager getEditableConfigurationManager(Kernel kernel) {
        Set names = kernel.listGBeans(new GBeanQuery(null, EditableConfigurationManager.class.getName()));
        for (Iterator iterator = names.iterator(); iterator.hasNext();) {
            ObjectName objectName = (ObjectName) iterator.next();
            try {
                if (kernel.getGBeanState(objectName) != State.RUNNING_INDEX) {
                    iterator.remove();
                }
            } catch (GBeanNotFoundException e) {
                // bean died
                iterator.remove();
            }
        }
        if (names.isEmpty()) {
            return null; // may be one, just not editable
        }
        if (names.size() > 1) {
            throw new IllegalStateException("More than one configuration mananger was found in kernel");
        }
        ObjectName configurationManagerName = (ObjectName) names.iterator().next();
        return (EditableConfigurationManager) kernel.getProxyManager().createProxy(configurationManagerName, EditableConfigurationManager.class);
    }

    public static void releaseConfigurationManager(Kernel kernel, ConfigurationManager configurationManager) {
        kernel.getProxyManager().destroyProxy(configurationManager);
    }
    
}
