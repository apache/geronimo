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
package org.apache.geronimo.kernel.proxy;

import org.apache.geronimo.gbean.AbstractName;

/**
 * Manages kernel proxies.  Note that all proxies will include an
 * implementation of GeronimoManagedBean.
 *
 * @see org.apache.geronimo.kernel.proxy.GeronimoManagedBean
 *
 * @version $Rev$ $Date$
 */
public interface ProxyManager {


    /**
     * Creates a proxy factory for GBeans which will implement the specified types.  The proxy class will be created
     * within the specified class loader.  All of the specified types must be visible from the class loader.
     *
     * @param types the type of the proxies this factory should create
     * @param classLoader the class loader in which the proxy class will be registered
     * @return the proxy factory
     */
    ProxyFactory createProxyFactory(Class[] types, ClassLoader classLoader);

    public Object createProxy(AbstractName target, ClassLoader loader);

    public <T> T createProxy(AbstractName target, Class<T> type);

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
     * TODO convert to abstractName
     * @param proxy the proxy to get the target object name from
     * @return the object name of the target
     */
    public AbstractName getProxyTarget(Object proxy);
}
