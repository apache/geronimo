/**
 *
 * Copyright 2003-2004 The Apache Software Foundation
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

package org.apache.geronimo.core.service;


import javax.management.ObjectName;

import org.apache.geronimo.core.service.Container;
import org.apache.geronimo.core.service.Interceptor;
import org.apache.geronimo.core.service.Invocation;
import org.apache.geronimo.core.service.InvocationResult;

/**
 *
 *
 *
 * @version $Rev$ $Date$
 */
public interface RPCContainer extends Container {
    //
    //  Main entry point
    //
    InvocationResult invoke(Invocation invocation) throws Throwable;


    /**
     * Get the JMX object name of the logical plugin.
     * @param logicalPluginName the logical name of the desired plugin
     * @return the JMX object name associated with the logical plugin, or null if a name is not found
     */
    ObjectName getPlugin(String logicalPluginName);

    /**
     * Puts the objectName in the container.
     * @param logicalPluginName the logical name of the plugin to set
     * @param objectName the JMX object name to set
     */
    void putPlugin(String logicalPluginName, ObjectName objectName);

    /**
     * Gets the named plugin as an Object.
     * @deprecated Switch plugin to a JMX object an use 'ObjectName getPlugin(String name)' instead
     * @param logicalPluginName the name of the plugin to get
     * @return the actual plugin object
     */
    Object getPluginObject(String logicalPluginName);

    /**
     * Puts the named plugin Object in the container.
     * @deprecated Switch plugin to a JMX object an use 'void putPlugin(String name, ObjectName objectName)' instead
     * @param logicalPluginName the name of the plugin to get
     * @param plugin the plugin obect or null to remove an existing plugin
     */
    void putPluginObject(String logicalPluginName, Object plugin);
}
