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

package org.apache.geronimo.gbean.jmx;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import javax.management.MBeanAttributeInfo;
import javax.management.MBeanInfo;
import javax.management.MBeanOperationInfo;
import javax.management.MBeanServer;
import javax.management.ObjectName;

import org.apache.geronimo.kernel.jmx.InvokeMBean;
import org.apache.geronimo.kernel.jmx.MBeanOperationSignature;

import net.sf.cglib.core.Signature;
import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;
import net.sf.cglib.reflect.FastClass;
import org.objectweb.asm.Type;

/**
 *
 *
 * @version $Revision: 1.9 $ $Date: 2004/03/10 09:59:01 $
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
            throw new NoSuchOperationError("No implementation method:" +
                    " objectName=" + objectName + ", method=" + method);
        }
        return invoker.invoke(server, objectName, args);
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
            int index = getSuperIndex(proxyType, method);
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
            methodTable[getSuperIndex(proxyType, proxyType.getMethod("equals", new Class[]{Object.class}))] = new EqualsInvoke();
            methodTable[getSuperIndex(proxyType, proxyType.getMethod("hashCode", null))] = new HashCodeInvoke();
            methodTable[getSuperIndex(proxyType, proxyType.getMethod("toString", null))] = new ToStringInvoke(proxyType.getName());
        } catch (Exception e) {
            // this can not happen... all classes must implement equals, hashCode and toString
            throw new AssertionError(e);
        }

        return methodTable;
    }

    private static int getSuperIndex(Class proxyType, Method method) {
        Signature signature = new Signature(method.getName(), Type.getReturnType(method), Type.getArgumentTypes(method));
        MethodProxy methodProxy = MethodProxy.find(proxyType, signature);
        if (methodProxy != null) {
            return methodProxy.getSuperIndex();
        }
        return -1;
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
}
