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
package org.apache.geronimo.gbean.jmx;

import java.beans.Introspector;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.Iterator;
import javax.management.MBeanAttributeInfo;
import javax.management.MBeanInfo;
import javax.management.MBeanOperationInfo;
import javax.management.MBeanServerConnection;
import javax.management.ObjectName;

import org.apache.geronimo.gbean.GOperationSignature;

import net.sf.cglib.core.Signature;
import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;
import net.sf.cglib.reflect.FastClass;
import org.objectweb.asm.Type;

/**
 *
 *
 * @version $Rev$ $Date$
 */
public class CGLibMethodInterceptor implements ProxyMethodInterceptor, MethodInterceptor {
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
     * Is this proxy currently stopped.  If it is invocations will not be allowed.
     */
    protected boolean stopped;

    public CGLibMethodInterceptor(Class proxyType) {
        this.proxyType = proxyType;
        stopped = true;
    }

    public synchronized void connect(MBeanServerConnection server, ObjectName objectName) {
        assert server != null && objectName != null;
        this.objectName = objectName;
        stopped = false;
        gbeanInvokers = createGBeanInvokers(server, objectName);
    }

    public synchronized void disconnect() {
        stopped = true;
        objectName = null;
        gbeanInvokers = null;
    }

    public final Object intercept(final Object object, final Method method, final Object[] args, final MethodProxy proxy) throws Throwable {
        GBeanInvoker gbeanInvoker;

        int interfaceIndex = proxy.getSuperIndex();
        synchronized (this) {
            if (stopped) {
                throw new DeadProxyException("Proxy is no longer valid");
            }
            gbeanInvoker = gbeanInvokers[interfaceIndex];
        }

        if (gbeanInvoker == null) {
            throw new NoSuchOperationError("No implementation method: objectName=" + objectName + ", method=" + method);
        }

        return gbeanInvoker.invoke(objectName, args);
    }

    private GBeanInvoker[] createGBeanInvokers(MBeanServerConnection server, ObjectName objectName) {
        GBeanInvoker[] invokers;
        try {
            RawInvoker rawInvoker = (RawInvoker) server.getAttribute(objectName, GBeanMBean.RAW_INVOKER);
            invokers = createRawGBeanInvokers(rawInvoker, proxyType);
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

    private GBeanInvoker[] createRawGBeanInvokers(RawInvoker rawInvoker, Class proxyType) {
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
            return new RawOperationInvoker(rawInvoker, methodIndex);
        }

        if (method.getName().startsWith("get")) {
            String attributeName = method.getName().substring(3);
            Integer methodIndex = ((Integer) attributes.get(attributeName));
            if (methodIndex != null) {
                return new RawGetAttributeInvoker(rawInvoker, methodIndex.intValue());
            }
            methodIndex = getMethodIndex(attributes, attributeName);
            if (methodIndex != null) {
                return new RawGetAttributeInvoker(rawInvoker, methodIndex.intValue());
            }
        }

        if (method.getName().startsWith("is")) {
            String attributeName = method.getName().substring(2);
            Integer methodIndex = ((Integer) attributes.get(attributeName));
            if (methodIndex != null) {
                return new RawGetAttributeInvoker(rawInvoker, methodIndex.intValue());
            }
            methodIndex = getMethodIndex(attributes, attributeName);
            if (methodIndex != null) {
                return new RawGetAttributeInvoker(rawInvoker, methodIndex.intValue());
            }
        }

        if (method.getName().startsWith("set")) {
            String attributeName = method.getName().substring(3);
            Integer methodIndex = ((Integer) attributes.get(attributeName));
            if (methodIndex != null) {
                return new RawSetAttributeInvoker(rawInvoker, methodIndex.intValue());
            }
            methodIndex = getMethodIndex(attributes, attributeName);
            if (methodIndex != null) {
                return new RawSetAttributeInvoker(rawInvoker, methodIndex.intValue());
            }
        }
        return null;
    }

    private GBeanInvoker[] createJMXGBeanInvokers(MBeanServerConnection server, ObjectName objectName, Class proxyType) {
        MBeanInfo info;
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

    private GBeanInvoker createJMXGBeanInvoker(MBeanServerConnection server, Method method, Map operations, Map attributes) {
        if (operations.containsKey(new GOperationSignature(method))) {
            return new JMXOperationInvoker(server, method);
        }

        String name = method.getName();
        if (name.startsWith("get")) {
            String attrName = method.getName().substring(3);
            if (attributes.containsKey(attrName)) {
                return new JMXGetAttributeInvoker(server, method, attrName);
            }
            attrName = Introspector.decapitalize(attrName);
            if (attributes.containsKey(attrName)) {
                return new JMXGetAttributeInvoker(server, method, attrName);
            }
        } else if (name.startsWith("is")) {
            String attrName = method.getName().substring(2);
            if (attributes.containsKey(attrName)) {
                return new JMXGetAttributeInvoker(server, method, attrName);
            }
            attrName = Introspector.decapitalize(attrName);
            if (attributes.containsKey(attrName)) {
                return new JMXGetAttributeInvoker(server, method, attrName);
            }
        } else if (name.startsWith("set")) {
            String attrName = method.getName().substring(3);
            if (attributes.containsKey(attrName)) {
                return new JMXSetAttributeInvoker(server, method, attrName);
            }
            attrName = Introspector.decapitalize(attrName);
            if (attributes.containsKey(attrName)) {
                return new JMXSetAttributeInvoker(server, method, attrName);
            }
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

    private static Integer getMethodIndex(Map attributes, String attributeName) {
        Iterator iter = attributes.keySet().iterator();
        while (iter.hasNext()) {
            String key = (String) iter.next();
            if (key.equalsIgnoreCase(attributeName)) {
                return (Integer) attributes.get(key);
            }
        }
        return null;
    }
}
