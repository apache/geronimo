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
package org.apache.geronimo.gbean.jmx;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import javax.management.MBeanAttributeInfo;
import javax.management.MBeanInfo;
import javax.management.MBeanOperationInfo;
import javax.management.MBeanServer;
import javax.management.ObjectName;

import org.apache.geronimo.kernel.jmx.InvokeMBean;
import org.apache.geronimo.kernel.jmx.MBeanOperationSignature;

import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;
import net.sf.cglib.reflect.FastClass;

/**
 *
 *
 * @version $Revision: 1.3 $ $Date: 2004/01/19 06:33:24 $
 */
public final class ProxyMethodInterceptor implements MethodInterceptor {
    /**
     * Type of the proxy interface
     */
    private final Class proxyType;

    /**
     * The MBeanServer we are using.
     */
    private MBeanServer server;

    /**
     * The object name to which we are connected.
     */
    private ObjectName objectName;

    /**
     * Map from interface method ids to InvokeMBean objects.
     */
    private InvokeMBean[] methodTable;

    /**
     * Is this proxy currently stoped.  If it is invocations will not be allowed.
     */
    private boolean stopped;

    public ProxyMethodInterceptor(Class proxyType) {
        assert proxyType != null;
        this.proxyType = proxyType;
        stopped = true;
    }

    public synchronized void connect(MBeanServer server, ObjectName objectName) {
        this.connect(server, objectName, false);
    }

    public synchronized void connect(MBeanServer server, ObjectName objectName, boolean stopped) {
        assert server != null && objectName != null;
        this.server = server;
        this.objectName = objectName;
        this.stopped = stopped;
        this.methodTable = ProxyMethodInterceptor.createMethodTable(server, objectName, proxyType);
    }

    public synchronized void disconnect() {
        stopped = true;
        this.server = null;
        this.objectName = null;
        this.methodTable = null;
    }

    public synchronized void start() {
        if (server == null || objectName == null) {
            throw new IllegalStateException("Server or objectName is null");
        }
        this.stopped = false;
    }

    public synchronized void stop() {
        this.stopped = true;
    }

    /**
     * Handles an invocation on a proxy
     * @param object the proxy instance
     * @param method java method that was invoked
     * @param args arguments to the mentod
     * @param proxy a CGLib method proxy of the method invoked
     * @return the result of the invocation
     * @throws java.lang.Throwable if any exceptions are thrown by the implementation method
     */
    public Object intercept(Object object, Method method, Object[] args, MethodProxy proxy) throws Throwable {
        synchronized (this) {
            if (stopped) {
                throw new IllegalStateException("Proxy is stopped");
            }
        }
        InvokeMBean invoker = methodTable[proxy.getSuperIndex()];
        if (invoker == null) {
            System.out.println("NO INVOKER FOR ");
        }
        return methodTable[proxy.getSuperIndex()].invoke(server, objectName, args);
    }

    public static InvokeMBean[] createMethodTable(MBeanServer server, ObjectName objectName, Class proxyType) {
        MBeanInfo info = null;
        try {
            info = server.getMBeanInfo(objectName);
        } catch (Exception e) {
            throw new IllegalArgumentException("Could not get MBeanInfo for target object: " + objectName);
        }

        // build attributeName->attributeInfo map
        MBeanAttributeInfo[] attributeInfos = info.getAttributes();
        Map attributes = new HashMap(attributeInfos.length);
        for (int i = 0; i < attributeInfos.length; i++) {
            MBeanAttributeInfo attributeInfo = attributeInfos[i];
            attributes.put(attributeInfo.getName(), attributeInfo);
        }

        // build operationName->operationInfo map
        MBeanOperationInfo[] operationInfos = info.getOperations();
        Map operations = new HashMap(operationInfos.length);
        for (int i = 0; i < operationInfos.length; i++) {
            MBeanOperationInfo operationInfo = operationInfos[i];
            operations.put(new MBeanOperationSignature(operationInfo), operationInfo);
        }

        // build the method lookup table
        FastClass fastClass = FastClass.create(proxyType);
        InvokeMBean[] methodTable = new InvokeMBean[fastClass.getMaxIndex() + 1];
        Method[] methods = proxyType.getMethods();
        for (int i = 0; i < methods.length; i++) {
            Method method = methods[i];
            int index = getSuperIndex(fastClass, method);
            if (index >= 0) {
                if (operations.containsKey(new MBeanOperationSignature(method))) {
                    methodTable[index] = new InvokeMBean(method, false, false);
                } else if (method.getName().startsWith("get") && attributes.containsKey(method.getName().substring(3))) {
                    methodTable[index] = new InvokeMBean(method, true, true);
                } else if (method.getName().startsWith("is") && attributes.containsKey(method.getName().substring(2))) {
                    methodTable[index] = new InvokeMBean(method, true, true);
                } else if (method.getName().startsWith("set") && attributes.containsKey(method.getName().substring(3))) {
                    methodTable[index] = new InvokeMBean(method, true, false);
                }
            }
        }

        // handle equals, hashCode and toString directly here
        try {
            methodTable[getSuperIndex(fastClass, proxyType.getMethod("equals", new Class[]{Object.class}))] = new EqualsInvoke();
            methodTable[getSuperIndex(fastClass, proxyType.getMethod("hashCode", null))] = new HashCodeInvoke();
            methodTable[getSuperIndex(fastClass, proxyType.getMethod("toString", null))] = new ToStringInvoke(proxyType.getName());
        } catch (Exception e) {
            System.out.println("Missing method for " + proxyType + " object name " + objectName);
            // this can not happen... all classes must implement equals, hashCode and toString
            throw new AssertionError(e);
        }

        return methodTable;
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
    private static int getSuperIndex(FastClass enhancedClass, Method method) {
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
        return -1;
    }
}
