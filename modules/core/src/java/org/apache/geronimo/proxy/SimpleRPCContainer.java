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

package org.apache.geronimo.proxy;

import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;

import javax.management.ObjectName;

import org.apache.geronimo.core.service.Component;
import org.apache.geronimo.core.service.Interceptor;
import org.apache.geronimo.core.service.Invocation;
import org.apache.geronimo.core.service.InvocationResult;
import org.apache.geronimo.core.service.RPCContainer;

/**
 * @version $Revision: 1.6 $ $Date: 2004/03/10 09:58:43 $
 */
public class SimpleRPCContainer extends SimpleContainer implements RPCContainer {

    private final Map plugins = new LinkedHashMap();
    private final Map pluginObjects = new LinkedHashMap();
    private final LinkedList interceptors = new LinkedList();
    private Interceptor firstInterceptor;

    public SimpleRPCContainer(Interceptor firstInterceptor) {
        this.firstInterceptor = firstInterceptor;
    }

    /**
    * @see org.apache.geronimo.core.service.RPCContainer#invoke(org.apache.geronimo.core.service.Invocation)
    */
    public final InvocationResult invoke(Invocation invocation) throws Throwable {
        return firstInterceptor.invoke(invocation);
    }

    public final ObjectName getPlugin(String logicalPluginName) {
        return (ObjectName) plugins.get(logicalPluginName);
    }

    public final void putPlugin(String logicalPluginName, ObjectName objectName) {
        plugins.put(logicalPluginName, objectName);
    }

    /**
     * @deprecated
     */
    public final Object getPluginObject(String logicalPluginName) {
        return pluginObjects.get(logicalPluginName);
    }

    /**
     * @deprecated
     */
    public final void putPluginObject(String logicalPluginName, Object plugin) {
        pluginObjects.put(logicalPluginName, plugin);
    }

}
