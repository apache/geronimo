/* ====================================================================
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 2003 The Apache Software Foundation.  All rights
 * reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. The end-user documentation included with the redistribution,
 *    if any, must include the following acknowledgment:
 *       "This product includes software developed by the
 *        Apache Software Foundation (http://www.apache.org/)."
 *    Alternately, this acknowledgment may appear in the software itself,
 *    if and wherever such third-party acknowledgments normally appear.
 *
 * 4. The names "Apache" and "Apache Software Foundation" and
 *    "Apache Geronimo" must not be used to endorse or promote products
 *    derived from this software without prior written permission. For
 *    written permission, please contact apache@apache.org.
 *
 * 5. Products derived from this software may not be called "Apache",
 *    "Apache Geronimo", nor may "Apache" appear in their name, without
 *    prior written permission of the Apache Software Foundation.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE APACHE SOFTWARE FOUNDATION OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 *
 * ====================================================================
 */

package org.apache.geronimo.common.jmx;

import java.lang.reflect.Proxy;

import javax.management.MBeanServer;
import javax.management.ObjectName;

import org.apache.geronimo.common.NullArgumentException;

/**
 * Creates a dynamic proxy to an MBean by ObjectName.
 *
 * The interface type and object existance is not enforced during construction.
 * Instead, if a method is invoked on the proxy and there is no object registered
 * with the assigned name, an InvocationTargetException is thrown, which contains
 * an InstanceNotFoundException.
 *
 * If an interface method that is not implemented by
 * the MBean is invoked, an InvocationTargetException is thrown, which contains an
 * NoSuchMethodException.
 *
 * @version $Revision: 1.3 $ $Date: 2003/08/30 20:12:23 $
 */
public class MBeanProxyFactory
{
    /**
     * Creates an MBean proxy using the specified interface to the objectName.
     *
     * @param type          The interface to implement for this proxy
     * @param server        The MBeanServer in which the object is registered
     * @param objectName    The objectName of the MBean to proxy
     * @return              The new MBean proxy, which implemnts the specified interface.
     */
    public static Object create(final Class type,
                                final MBeanServer server,
                                final ObjectName objectName)
    {
        if (type == null) {
            throw new NullArgumentException("type");
        }
        if (!type.isInterface()) {
            throw new IllegalArgumentException("Type is not an interface: " + type);
        }
        if (server == null) {
            throw new NullArgumentException("server");
        }
        
        ClassLoader cl = type.getClassLoader();
        Class[] types = { type, MBeanProxy.class };
        
        MBeanProxyHandler handler =  new MBeanProxyHandler(server, objectName);
        
        return Proxy.newProxyInstance(cl, types, handler);
    }
    
    /**
     * Creates an MBean proxy using the specified interface to the objectName.
     *
     * @param tyep          The interface to implement for this proxy
     * @param objectName    The objectName of the MBean to proxy
     * @return              The new MBean proxy, which implemnts the specified interface.
     */
    public static Object create(final Class type, final ObjectName objectName)
    {
        MBeanServer server = MBeanServerLocator.locate();
        assert server != null;
        
        return create(type, server, objectName);
    }
    
    /**
     * An interface which all proxies created by {@link MBeanProxyFactory}
     * will implement in addition to the requested interface.
     */
    public static interface MBeanProxy
    {
        /**
         * Return the ObjectName for this proxy.
         *
         * @return   The ObjectName for this proxy.
         */
        ObjectName getMBeanProxyObjectName();
        
        /**
         * Return the MBeanServer for this proxy.
         *
         * @return   The ObjectName for this proxy.
         */
        MBeanServer getMBeanProxyMBeanServer();
    }
}
