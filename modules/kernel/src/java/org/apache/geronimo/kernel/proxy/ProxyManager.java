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
package org.apache.geronimo.kernel.proxy;

import javax.management.ObjectName;

/**
 * Manages kernel proxies
 * @version $Rev$ $Date$
 */
public interface ProxyManager {
    /**
     * Create a proxy factory which will generate proxies of the specified type
     * @param type the type of the proxies to create
     * @return the proxy factory
     */
    public ProxyFactory createProxyFactory(Class type);

    /**
     * Create a proxy implementing the class to the specified target.
     * @param target the target object name
     * @param type the type of the proxy to create
     * @return the proxy
     */
    public Object createProxy(ObjectName target, Class type);

    /**
     * Cleans up and resources associated with the proxy
     * @param proxy the proxy to destroy
     */
    public void destroyProxy(Object proxy);

    /**
     * Is the specified object a proxy
     * @param object the object to determin if it is a proxy
     * @return true if the object is a proxy
     */
    public boolean isProxy(Object object);

    /**
     * Get the object name of the specified proxy
     * @param proxy the proxy to get the target object name from
     * @return the object name of the target
     */
    public ObjectName getProxyTarget(Object proxy);
}
