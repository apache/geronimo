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

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import javax.management.MBeanServer;
import javax.management.ObjectName;

import org.apache.geronimo.kernel.jmx.InvokeMBean;

import net.sf.cglib.proxy.CallbackFilter;
import net.sf.cglib.proxy.Callbacks;
import net.sf.cglib.proxy.Enhancer;
import net.sf.cglib.proxy.Factory;
import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;
import net.sf.cglib.proxy.SimpleCallbacks;
import net.sf.cglib.reflect.FastClass;

/**
 * This handles a connection to another mbean.
 *
 * @version $Revision: 1.5 $ $Date: 2003/12/30 08:25:32 $
 */
class GeronimoMBeanEndpointConnection {
    /**
     * The MBean server to Invoke.
     */
    private MBeanServer server;

    /**
     * The object name to which we are connected.
     */
    private ObjectName objectName;

    private final GeronimoMBeanEndpointListener endpointListener;

    /**
     * A factory to create instances
     */
    private Factory factory;

    /**
     * Map from interface method ids to InvokeMBean objects.
     */
    private InvokeMBean[] methodTable;

    /**
     * Is this connection open?
     */
    private boolean open = false;

    /**
     * Proxy to the to this connection.
     */
    private Object proxy;

    /**
     * The invocation handler for the proxy
     */
    private ConnectionMethodInterceptor methodInterceptor;

    /**
     * Creates a new connection to the specified component using the specified interface.
     *
     * @param iface the interface for the proxy to the component
     * @param server the mbean server in which the component is registered
     * @param objectName the name of the component
     */
    public GeronimoMBeanEndpointConnection(Class iface, MBeanServer server, ObjectName objectName, GeronimoMBeanEndpointListener endpointListener) {
        assert iface != null: "iface can not be null";
        assert server != null: "Server can not be null";
        assert objectName != null: "Object name can not be null";

        if(Modifier.isFinal(iface.getModifiers())) {
            throw new IllegalArgumentException("Proxy interface cannot be a final class: " + iface.getName());
        }
        if(objectName.isPattern()) {
            throw new IllegalArgumentException("Object name can not be a pattern");
        }

        this.server = server;
        this.objectName = objectName;
        this.endpointListener = endpointListener;

        MethodInterceptor dummyInterceptor = new MethodInterceptor() {
            public Object intercept(Object obj, Method method, Object[] args, MethodProxy proxy) throws Throwable {
                return null;
            }
        };

        // get the factory
        Enhancer enhancer = new Enhancer();
        enhancer.setSuperclass(iface);
        enhancer.setCallbackFilter(new CallbackFilter() {
            public int accept(Method method) {
                if(Modifier.isStatic(method.getModifiers())) {
                    return Callbacks.NO_OP;
                }
                if(Modifier.isFinal(method.getModifiers())) {
                    return Callbacks.NO_OP;
                }
                return Callbacks.INTERCEPT;
            }
        });
        enhancer.setCallbacks(new SimpleCallbacks());
        factory = enhancer.create();

        final Class javaClass = factory.newInstance(dummyInterceptor).getClass();
        FastClass fastClass = FastClass.create(javaClass);
        methodTable = new InvokeMBean[fastClass.getMaxIndex() + 1];
        Method[] methods = iface.getMethods();
        for (int i = 0; i < methods.length; i++) {
            Method method = methods[i];
            if((method.getModifiers() & (Modifier.FINAL | Modifier.PUBLIC | Modifier.STATIC)) == Modifier.PUBLIC) {
                int index = getSuperIndex(fastClass, method);
                methodTable[index] = new InvokeMBean(method, false, false);
            }
        }

        try {
            methodTable[getSuperIndex(fastClass, javaClass.getMethod("equals", new Class[]{Object.class}))] = new EqualsInvoke();
            methodTable[getSuperIndex(fastClass, javaClass.getMethod("hashCode", null))] = new HashCodeInvoke();
            methodTable[getSuperIndex(fastClass, javaClass.getMethod("toString", null))] = new ToStringInvoke(iface.getName());
        } catch (Exception e) {
            // this can not happen... all classes must implement equals, hashCode and toString
            throw new AssertionError(e);
        }
    }

    /**
     * Completely cleans up the internal state of the component.
     */
    public synchronized void invalidate() {
        open = false;
        if (methodInterceptor != null) {
            methodInterceptor.invalidate();
            methodInterceptor = null;
        }
        proxy = null;

        server = null;
        objectName = null;
        methodTable = null;
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
        if (open) {
            throw new IllegalStateException("Connection is already open");
        }

        methodInterceptor = new ConnectionMethodInterceptor(methodTable, server, objectName);
        proxy = factory.newInstance(methodInterceptor);
        open = true;
        if (endpointListener != null) {
            endpointListener.endpointAdded(proxy);
        }
    }

    /**
     * Closes the connection to the component.
     */
    public synchronized void close() {
        if (!open) {
            throw new IllegalStateException("Connection is already closed");
        }
        if (endpointListener != null) {
            endpointListener.endpointRemoved(proxy);
        }
        methodInterceptor.invalidate();
        methodInterceptor = null;
        proxy = null;
        open = false;
    }

    private static class ConnectionMethodInterceptor implements MethodInterceptor {
        private boolean valid;
        private InvokeMBean[] methodTable;
        private MBeanServer server;
        private ObjectName objectName;

        public ConnectionMethodInterceptor(InvokeMBean[] methodTable, MBeanServer server, ObjectName objectName) {
            valid = true;
            this.methodTable = methodTable;
            this.server = server;
            this.objectName = objectName;
        }

        public Object intercept(Object obj, Method method, Object[] arguments, MethodProxy proxy) throws Throwable {
            MBeanServer server;
            ObjectName objectName;
            InvokeMBean mbeanInvoker;

            // grab references to the variables in a static block
            synchronized (this) {
                if (!valid) {
                    throw new IllegalStateException("The connection in this proxy has been closed");
                }
                server = this.server;
                objectName = this.objectName;
                mbeanInvoker = methodTable[proxy.getSuperIndex()];
            }
            if (mbeanInvoker == null) {
                throw new AssertionError("Unknown operation " + method);
            }
            return mbeanInvoker.invoke(server, objectName, arguments);
        }

        public synchronized void invalidate() {
            valid = false;
            server = null;
            objectName = null;
            methodTable = null;
        }
    }

    private static final class HashCodeInvoke extends InvokeMBean {
        public HashCodeInvoke() {
            super("hashCode", new String[0], new Class[0], false, false, 0);
        }

        public Object invoke(MBeanServer server, ObjectName objectName, Object[] arguments) throws Throwable {
            return new Integer(objectName.hashCode());
        }
    }

    private static final class EqualsInvoke extends InvokeMBean {
        public EqualsInvoke() {
            super("hashCode", new String[]{"java.lang.Object"}, new Class[]{Object.class}, false, false, 1);
        }

        public Object invoke(MBeanServer server, ObjectName objectName, Object[] arguments) throws Throwable {
            return new Boolean(objectName.equals(arguments[0]));
        }
    }


    private static final class ToStringInvoke extends InvokeMBean {
        private final String interfaceName;

        public ToStringInvoke(String interfaceName) {
            super("toString", new String[0], new Class[0], false, false, 0);
            this.interfaceName = "[" + interfaceName + ": ";
        }

        public Object invoke(MBeanServer server, ObjectName objectName, Object[] arguments) throws Throwable {
            return interfaceName + objectName + "]";
        }
    }


    private static String ACCESS_PREFIX = "CGLIB$$ACCESS_";

    /**
     * Returns the name of the synthetic method created by CGLIB which is
     * used by invokesuper to invoke the superclass
     * (non-intercepted) method implementation. The parameter types are
     * guaranteed to be the same.
     * @param enhancedClass the class generated by Enhancer
     * @param method the original method; only the name and parameter types are used.
     * @return the name of the synthetic proxy method, or null if no matching method can be found
     */
    public static int getSuperIndex(FastClass enhancedClass, Method method) {
        String prefix = ACCESS_PREFIX + method.getName() + "_";
        int lastUnderscore = prefix.length() - 1;
        Class[] params = method.getParameterTypes();

        Method[] methods = enhancedClass.getJavaClass().getDeclaredMethods();
        for (int i = 0; i < methods.length; i++) {
            String name = methods[i].getName();
            Class[] parameterTypes = methods[i].getParameterTypes();
            if (name.startsWith(prefix) &&
                    name.lastIndexOf('_') == lastUnderscore &&
                    Arrays.equals(parameterTypes, params)) {
                return enhancedClass.getIndex(name, parameterTypes);
            }
        }
        throw new IllegalArgumentException("Method not found on enhancedClass:" +
                " enhancedClass=" + enhancedClass.getJavaClass() + " method=" + method);
    }
}
