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
package org.apache.geronimo.kernel.service;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Map;
import java.util.HashMap;
import javax.management.MBeanException;
import javax.management.ReflectionException;
import javax.management.RuntimeOperationsException;
import javax.management.RuntimeMBeanException;
import javax.management.RuntimeErrorException;
import javax.management.MBeanServer;
import javax.management.ObjectName;

/**
 * This handles a connection to another mbean.
 *
 * @version $Revision: 1.1 $ $Date: 2003/11/06 19:52:50 $
 */
class GeronimoMBeanEndpointConnection  {
    /**
     * Map from method proxies to mbeanInvoker.
     */
    private Map methodMap = new HashMap();

    /**
     * The MBean server to Invoke.
     */
    private MBeanServer server;

    /**
     * The object name to which we are connected.
     */
    private ObjectName objectName;

    /**
     * The interface for the proxy
     */
    private Class iface;

    /**
     * Proxy to the to this connection.
     */
    private Object proxy;

    /**
     * The invocation handler for the proxy
     */
    private ConnectionInvocationHandler invocationHandler;

    /**
     * Is this connection open?
     */
    private boolean open = false;

    /**
     * Creates a new connection to the specified component using the specified interface.
     *
     * @param iface the interface for the proxy to the component
     * @param server the mbean server in which the component is registered
     * @param objectName the name of the component
     */
    public GeronimoMBeanEndpointConnection(Class iface, MBeanServer server, ObjectName objectName) {
        assert iface != null: "iface can not be null";
        assert iface.isInterface(): "iface must be an interface";
        assert server != null: "Server can not be null";
        assert objectName != null: "Object name can not be null";
        assert !objectName.isPattern(): "Object name can not be a pattern";

        this.iface = iface;
        this.server = server;
        this.objectName = objectName;

        Method[] methods = iface.getMethods();
        for (int i = 0; i < methods.length; i++) {
            Method method = methods[i];
            methodMap.put(method, new MBeanInvoker(method));
        }
    }

    /**
     * Completely cleans up the internal state of the component.
     */
    public synchronized void invalidate() {
        open = false;
        if(invocationHandler != null) {
            invocationHandler.invalidate();
            invocationHandler = null;
        }
        proxy = null;

        server = null;
        objectName = null;
        methodMap.clear();
        methodMap = null;
        proxy = null;
    }

    /**
     * Gets the proxy to the component.  The proxy will implement the interface set during construction.
     * The proxy is null if the connection is not open.
     * @return a proxy to the componet
     */
    public synchronized Object getProxy() {
        return proxy;
    }

    /**
     * Gets the object name of the component.
     * @return the object name of the component
     */
    public synchronized ObjectName getObjectName() {
        return objectName;
    }

    /**
     * Opens a connection to the component.  This cretes the proxy used to communicate with the component
     */
    public synchronized void open() {
        if(open) {
            throw new IllegalStateException("Connection is already open");
        }
        invocationHandler = new ConnectionInvocationHandler(new HashMap(methodMap), server, objectName);
        proxy = Proxy.newProxyInstance(iface.getClassLoader(), new Class[]{iface}, invocationHandler);
        open = true;
    }

    /**
     * Closes the connection to the component.
     */
    public synchronized void close() {
        if(!open) {
            throw new IllegalStateException("Connection is already closed");
        }
        invocationHandler.invalidate();
        invocationHandler = null;
        proxy = null;
        open = false;
    }

    private static class ConnectionInvocationHandler implements InvocationHandler {
        private boolean valid;
        private Map methodMap;
        private MBeanServer server;
        private ObjectName objectName;

        public ConnectionInvocationHandler(Map methodMap, MBeanServer server, ObjectName objectName) {
            valid = true;
            this.methodMap = methodMap;
            this.server = server;
            this.objectName = objectName;
        }

        public Object invoke(Object proxy, Method method, Object[] arguments) throws Throwable {
            MBeanServer server;
            ObjectName objectName;
            MBeanInvoker mbeanInvoker;

            // grab references to the variables in a static block
            synchronized (this) {
                if (!valid) {
                    throw new IllegalStateException("This proxy has been invalidated");
                }
                server = this.server;
                objectName = this.objectName;
                mbeanInvoker = (MBeanInvoker) methodMap.get(method);
            }
            return mbeanInvoker.invoke(server, objectName, arguments);
        }

        public synchronized void invalidate() {
            valid = false;
            server = null;
            objectName = null;
            methodMap.clear();
            methodMap = null;
        }
    }

    private static class MBeanInvoker {
        private final String methodName;
        private final String[] argumentTypes;
        private final Class[] declaredExceptions;

        public MBeanInvoker(Method method) {
            methodName = method.getName();

            // conver the parameters to a MBeanServer friendly string array
            Class[] parameters = method.getParameterTypes();
            argumentTypes = new String[parameters.length];
            for (int i = 0; i < parameters.length; i++) {
                argumentTypes[i] = parameters[i].getName();
            }

            declaredExceptions = method.getExceptionTypes();
        }

        public Object invoke(MBeanServer server, ObjectName objectName, Object[] arguments) throws Throwable {
            try {
                return server.invoke(objectName, methodName, arguments, argumentTypes);
            } catch (Throwable t) {
                Throwable throwable = t;
                while (true) {
                    for (int i = 0; i < declaredExceptions.length; i++) {
                        Class declaredException = declaredExceptions[i];
                        if (declaredException.isInstance(throwable)) {
                            throw throwable;
                        }
                    }

                    // Unwrap the exceptions we understand
                    if (throwable instanceof MBeanException) {
                        throwable = (((MBeanException) throwable).getTargetException());
                    } else if (throwable instanceof ReflectionException) {
                        throwable = (((ReflectionException) throwable).getTargetException());
                    } else if (throwable instanceof RuntimeOperationsException) {
                        throwable = (((RuntimeOperationsException) throwable).getTargetException());
                    } else if (throwable instanceof RuntimeMBeanException) {
                        throwable = (((RuntimeMBeanException) throwable).getTargetException());
                    } else if (throwable instanceof RuntimeErrorException) {
                        throwable = (((RuntimeErrorException) throwable).getTargetError());
                    } else {
                        // don't know how to unwrap this, just throw it
                        throw throwable;
                    }
                }
            }
        }
    }
}
