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
     * plus GeronimoManagedBean.
     *
     * @param type the type of the proxies to create
     * @return the proxy factory
     */
    public ProxyFactory createProxyFactory(Class type);

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
     * Create a proxy for the specified target, implementing a variable
     * number of interfaces.  It's possible to specify one interface that must
     * be included, and also to specify a number of variable interfaces that
     * the proxy should implement if the underlying GBean supports them.  Each
     * proxy will also implement GeronimoManagedBean in addition to any
     * interfaces provided as arguments.
     *
     * @param target the target object name
     * @param required an interface that the proxy must implement.  This may be
     *                 null in which case only the optional interfaces will be
     *                 evaluated.  However, it's recommended that you always
     *                 provide one in case none of the optional interfaces
     *                 are a match.
     * @param optional Interfaces that the proxy may implement.  For each
     *                 of these interfaces, the proxy must implement it if the
     *                 underlying GBean declares that it implements it (by
     *                 declaring the interface in its GBeanInfo), and otherwise
     *                 the interface will be ignored.
     * @return the proxy, or null if no required interfaces was specified and
     *         none of the optional interfaces match the GBeanInfo
     */
    public Object createProxy(ObjectName target, Class required, Class[] optional);

    /**
     * Create a proxy for the specified target, implementing a variable
     * number of interfaces.  It's possible to specify one interface that must
     * be included, and also to specify a number of variable interfaces that
     * the proxy should implement if the underlying GBean supports them.  Each
     * proxy will also implement GeronimoManagedBean in addition to any
     * interfaces provided as arguments.
     *
     * @param objectNameStrings An array of ObjectNames, each in String form
     * @param required an interface that the proxies must implement.  This may
     *                 be null in which case only the optional interfaces will
     *                 be evaluated.  However, it's recommended that you
     *                 always provide one in case none of the optional
     *                 interfaces are a match for a particular target.
     * @param optional Interfaces that the proxies may implement.  For each
     *                 proxy for each of these interfaces, the proxy must
     *                 implement it if the underlying GBean declares that it
     *                 implements it (by declaring the interface in its
     *                 GBeanInfo), and otherwise the interface will be
     *                 ignored for that GBean.  Note that different result
     *                 proxies may implement different interfaces, depending
     *                 on the underlyings GBeans.
     * @return an array of proxies of the same length as the argument array,
     *         where each value is a proxy or null if no required interface
     *         was provided and none of the optional interfaces match.
     */
    public Object[] createProxies(String[] objectNameStrings, Class required, Class[] optional) throws MalformedObjectNameException;

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
