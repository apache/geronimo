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
import javax.management.MalformedObjectNameException;

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
     * Create a proxy factory which will generate proxies of the specified type,
     * plus GeronimoManagedBean. The proxy class will be created within the class
     * loader from which the specified type was loaded, or from the system class
     * loader if the specified type has a null class loader.
     *
     * @param type the type of the proxies to create
     * @return the proxy factory
     */
    public ProxyFactory createProxyFactory(Class type);


    /**
     * Creates a proxy factory for GBeans which will implement the specified types.  The proxy class will be created
     * within the specified class loader.  All of the specified types must be visible from the class loader.
     *
     * @param types the type of the proxies this factory should create
     * @param classLoader the class loader in which the proxy class will be registered
     * @return the proxy factory
     */
    ProxyFactory createProxyFactory(Class[] types, ClassLoader classLoader);

    /**
     * Create a proxy for the specified target.  The proxy will implement
     * all of the interfaces that the underlying GBean specifies in its
     * GBeanInfo, plus GeronimoManagedBean.  If there are no interfaces in
     * the GBeanInfo, this method will return null.
     *
     * @param target the target object name
     * @param loader the ClassLoader used to load the interfaces used by the
     *        proxy
     * @return the proxy, or null if the GBeanInfo declares no interfaces
     */
    public Object createProxy(ObjectName target, ClassLoader loader);

    /**
     * Create proxies for the specified targets.  The proxies will implement
     * all of the interfaces that the underlying GBeans specify in their
     * GBeanInfo, plus GeronimoManagedBean.  If there are no interfaces in the
     * GBeanInfo, this method will return a null in that spot in the array.
     *
     * @param objectNameStrings An array of ObjectNames, each in String form
     * @param loader the ClassLoader used to load the interfaces used by the
     *               proxies
     * @return an array of proxies of the same length as the argument array,
     *         where each value is a proxy or null if the corresponding
     *         GBeanInfo declares no interfaces
     */
    public Object[] createProxies(String[] objectNameStrings, ClassLoader loader) throws MalformedObjectNameException;

    /**
     * Create a proxy for the specified target, implementing the specified
     * interface, plus GeronimoManagedBean.
     *
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
