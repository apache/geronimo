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
import java.util.Map;
import java.util.HashMap;
import javax.management.MBeanServer;
import javax.management.ObjectName;
import javax.management.MBeanInfo;
import javax.management.MBeanAttributeInfo;
import javax.management.MBeanOperationInfo;

import net.sf.cglib.core.Signature;
import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;
import net.sf.cglib.reflect.FastClass;
import org.apache.geronimo.gbean.GOperationSignature;
import org.apache.geronimo.gbean.GOperationSignature;
import org.objectweb.asm.Type;

/**
 * @version $Revision: 1.11 $ $Date: 2004/05/26 22:58:30 $
 */
public final class ProxyMethodInterceptor implements MethodInterceptor {
    /**
     * Type of the proxy interface
     */
    private final Class proxyType;

    /**
     * The object name to which we are connected.
     */
    private ObjectName objectName;

    /**
     * GBeanInvokers keyed on the proxy interface method index
     */
    private GBeanInvoker[] gbeanInvokers;

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
        this.objectName = objectName;
        this.stopped = stopped;
        gbeanInvokers = createGBeanInvokers(server, objectName, proxyType);
    }

    public synchronized void disconnect() {
        stopped = true;
        objectName = null;
        gbeanInvokers = null;
    }

    public synchronized void start() {
        if (gbeanInvokers == null) {
            throw new IllegalStateException("Proxy is not connected");
        }
        this.stopped = false;
    }

    public synchronized void stop() {
        this.stopped = true;
    }

    public Object intercept(Object object, Method method, Object[] args, MethodProxy proxy) throws Throwable {
        GBeanInvoker gbeanInvoker;

        int interfaceIndex = proxy.getSuperIndex();
        synchronized (this) {
            if (stopped) {
                throw new IllegalStateException("Proxy is stopped");
            }
            gbeanInvoker = gbeanInvokers[interfaceIndex];
        }

        if (gbeanInvoker == null) {
            throw new NoSuchOperationError("No implementation method: objectName=" + objectName + ", method=" + method);
        }

        return gbeanInvoker.invoke(objectName, args);
    }

    public GBeanInvoker[] createGBeanInvokers(MBeanServer server, ObjectName objectName, Class proxyType) {
        GBeanInvoker[] invokers;
        try {
            RawInvoker rawInvoker = (RawInvoker) server.getAttribute(objectName, GBeanMBean.RAW_INVOKER);
            invokers = createRawGBeanInvokers(rawInvoker, objectName, proxyType);
        } catch (Exception e) {
            invokers = createJMXGBeanInvokers(server, objectName, proxyType);
        }

        // handle equals, hashCode and toString directly here
        try {
            invokers[getSuperIndex(proxyType, proxyType.getMethod("equals", new Class[]{Object.class}))] = new EqualsInvoke();
            invokers[getSuperIndex(proxyType, proxyType.getMethod("hashCode", null))] = new HashCodeInvoke();
            invokers[getSuperIndex(proxyType, proxyType.getMethod("toString", null))] = new ToStringInvoke(proxyType.getName());
        } catch (Exception e) {
            // this can not happen... all classes must implement equals, hashCode and toString
            throw new AssertionError(e);
        }

        return invokers;
    }

    public GBeanInvoker[] createRawGBeanInvokers(RawInvoker rawInvoker, ObjectName objectName, Class proxyType) {
        Map operations = rawInvoker.getOperationIndex();
        Map attributes = rawInvoker.getAttributeIndex();

        // build the method lookup table
        FastClass fastClass = FastClass.create(proxyType);
        GBeanInvoker[] invokers = new GBeanInvoker[fastClass.getMaxIndex() + 1];
        Method[] methods = proxyType.getMethods();
        for (int i = 0; i < methods.length; i++) {
            Method method = methods[i];
            int interfaceIndex = getSuperIndex(proxyType, method);
            if (interfaceIndex >= 0) {
                invokers[interfaceIndex] = createRawGBeanInvoker(rawInvoker, method, operations, attributes);
            }
        }

        return invokers;
    }

    private GBeanInvoker createRawGBeanInvoker(RawInvoker rawInvoker, Method method, Map operations, Map attributes) {
        if (operations.containsKey(new GOperationSignature(method))) {
            int methodIndex = ((Integer) operations.get(new GOperationSignature(method))).intValue();
            return new RawGBeanInvoker(rawInvoker, methodIndex, GBeanInvoker.OPERATION);
        }

        if (method.getName().startsWith("get")) {
            Integer methodIndex = ((Integer) attributes.get(method.getName().substring(3)));
            if (methodIndex != null) {
                return new RawGBeanInvoker(rawInvoker, methodIndex.intValue(), GBeanInvoker.GETTER);
            }
        }

        if (method.getName().startsWith("is")) {
            Integer methodIndex = ((Integer) attributes.get(method.getName().substring(2)));
            if (methodIndex != null) {
                return new RawGBeanInvoker(rawInvoker, methodIndex.intValue(), GBeanInvoker.GETTER);
            }
        }

        if (method.getName().startsWith("set")) {
            Integer methodIndex = ((Integer) attributes.get(method.getName().substring(3)));
            if (methodIndex != null) {
                return new RawGBeanInvoker(rawInvoker, methodIndex.intValue(), GBeanInvoker.SETTER);
            }
        }
        return null;
    }

    public GBeanInvoker[] createJMXGBeanInvokers(MBeanServer server, ObjectName objectName, Class proxyType) {
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
            operations.put(new GOperationSignature(operationInfo), operationInfo);
        }

        // build the method lookup table
        FastClass fastClass = FastClass.create(proxyType);
        GBeanInvoker[] invokers = new GBeanInvoker[fastClass.getMaxIndex() + 1];
        Method[] methods = proxyType.getMethods();
        for (int i = 0; i < methods.length; i++) {
            Method method = methods[i];
            int interfaceIndex = getSuperIndex(proxyType, method);
            if (interfaceIndex >= 0) {
                invokers[interfaceIndex] = createJMXGBeanInvoker(server, method, operations, attributes);
            }
        }

        return invokers;
    }

    private GBeanInvoker createJMXGBeanInvoker(MBeanServer server, Method method, Map operations, Map attributes) {
        if (operations.containsKey(new GOperationSignature(method))) {
            return new JMXGBeanInvoker(server, method, GBeanInvoker.OPERATION);
        }

        if (method.getName().startsWith("get") && attributes.containsKey(method.getName().substring(3))) {
            return new JMXGBeanInvoker(server, method, GBeanInvoker.GETTER);
        }

        if (method.getName().startsWith("is") && attributes.containsKey(method.getName().substring(2))) {
            return new JMXGBeanInvoker(server, method, GBeanInvoker.GETTER);
        }

        if (method.getName().startsWith("set") && attributes.containsKey(method.getName().substring(3))) {
            return new JMXGBeanInvoker(server, method, GBeanInvoker.SETTER);
        }
        return null;
    }

    private static int getSuperIndex(Class proxyType, Method method) {
        Signature signature = new Signature(method.getName(), Type.getReturnType(method), Type.getArgumentTypes(method));
        MethodProxy methodProxy = MethodProxy.find(proxyType, signature);
        if (methodProxy != null) {
            return methodProxy.getSuperIndex();
        }
        return -1;
    }

    private static final class HashCodeInvoke implements GBeanInvoker {
        public Object invoke(ObjectName objectName, Object[] arguments) throws Throwable {
            return new Integer(objectName.hashCode());
        }
    }

    private static final class EqualsInvoke implements GBeanInvoker {
        public Object invoke(ObjectName objectName, Object[] arguments) throws Throwable {
            // todo this is broken.. we need a way to extract the object name from the other proxy
            return new Boolean(objectName.equals(arguments[0]));
        }
    }

    private static final class ToStringInvoke implements GBeanInvoker {
        private final String interfaceName;

        public ToStringInvoke(String interfaceName) {
            this.interfaceName = "[" + interfaceName + ": ";
        }

        public Object invoke(ObjectName objectName, Object[] arguments) throws Throwable {
            return interfaceName + objectName + "]";
        }
    }
}
