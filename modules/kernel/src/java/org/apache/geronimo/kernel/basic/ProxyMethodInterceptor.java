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
package org.apache.geronimo.kernel.basic;

import java.lang.reflect.Method;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.HashSet;
import java.beans.Introspector;
import javax.management.ObjectName;

import net.sf.cglib.asm.Type;
import net.sf.cglib.core.Signature;
import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;
import net.sf.cglib.reflect.FastClass;
import org.apache.geronimo.gbean.GOperationSignature;
import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.gbean.GAttributeInfo;
import org.apache.geronimo.gbean.GOperationInfo;
import org.apache.geronimo.gbean.runtime.GBeanInstance;
import org.apache.geronimo.gbean.runtime.RawInvoker;
import org.apache.geronimo.kernel.Kernel;
import org.apache.geronimo.kernel.management.State;
import org.apache.geronimo.kernel.proxy.DeadProxyException;
import org.apache.geronimo.kernel.proxy.ProxyManager;
import org.apache.geronimo.kernel.proxy.GeronimoManagedBean;

/**
 * @version $Rev: 106345 $ $Date: 2004-11-23 12:37:03 -0800 (Tue, 23 Nov 2004) $
 */
public class ProxyMethodInterceptor implements MethodInterceptor {
    /**
     * Type of the proxy interface
     */
    private final Class proxyType;

    /**
     * The object name to which we are connected.
     */
    private final ObjectName objectName;

    /**
     * GBeanInvokers keyed on the proxy interface method index
     */
    private ProxyInvoker[] gbeanInvokers;

    public ProxyMethodInterceptor(Class proxyType, Kernel kernel, ObjectName objectName) {
        assert proxyType != null;
        assert kernel != null;
        assert objectName != null;

        this.proxyType = proxyType;
        this.objectName = objectName;
        gbeanInvokers = createGBeanInvokers(kernel, objectName);
    }

    public synchronized void destroy() {
        gbeanInvokers = null;
    }

    public ObjectName getObjectName() {
        return objectName;
    }

    public final Object intercept(final Object object, final Method method, final Object[] args, final MethodProxy proxy) throws Throwable {
        ProxyInvoker gbeanInvoker;

        int interfaceIndex = proxy.getSuperIndex();
        synchronized (this) {
            if (gbeanInvokers == null) {
                throw new DeadProxyException("Proxy is no longer valid");
            }
            gbeanInvoker = gbeanInvokers[interfaceIndex];
        }

        if (gbeanInvoker == null) {
            throw new UnsupportedOperationException("No implementation method: objectName=" + objectName + ", method=" + method);
        }

        return gbeanInvoker.invoke(objectName, args);
    }

    private ProxyInvoker[] createGBeanInvokers(Kernel kernel, ObjectName objectName) {
        ProxyInvoker[] invokers;
        try {
            RawInvoker rawInvoker = (RawInvoker) kernel.getAttribute(objectName, GBeanInstance.RAW_INVOKER);
            invokers = createRawGBeanInvokers(rawInvoker, proxyType);
        } catch (Exception e) {
            invokers = createKernelGBeanInvokers(kernel, objectName, proxyType);
        }

        // handle equals, hashCode and toString directly here
        try {
            invokers[getSuperIndex(proxyType, proxyType.getMethod("equals", new Class[]{Object.class}))] = new EqualsInvoke(kernel.getProxyManager());
            invokers[getSuperIndex(proxyType, proxyType.getMethod("hashCode", null))] = new HashCodeInvoke();
            invokers[getSuperIndex(proxyType, proxyType.getMethod("toString", null))] = new ToStringInvoke(proxyType.getName());
            if(GeronimoManagedBean.class.isAssignableFrom(proxyType)) {
                invokers[getSuperIndex(proxyType, proxyType.getMethod("getState", null))] = new GetStateInvoke(kernel);
                invokers[getSuperIndex(proxyType, proxyType.getMethod("getStateInstance", null))] = new GetStateInstanceInvoke(kernel);
                invokers[getSuperIndex(proxyType, proxyType.getMethod("start", null))] = new StartInvoke(kernel);
                invokers[getSuperIndex(proxyType, proxyType.getMethod("startRecursive", null))] = new StartRecursiveInvoke(kernel);
                invokers[getSuperIndex(proxyType, proxyType.getMethod("stop", null))] = new StopInvoke(kernel);
                invokers[getSuperIndex(proxyType, proxyType.getMethod("getStartTime", null))] = new GetStartTimeInvoke(kernel);
                invokers[getSuperIndex(proxyType, proxyType.getMethod("getObjectName", null))] = new GetObjectNameInvoke();
            }
        } catch (Exception e) {
            // this can not happen... all classes must implement equals, hashCode and toString
            throw new AssertionError(e);
        }

        return invokers;
    }

    private ProxyInvoker[] createRawGBeanInvokers(RawInvoker rawInvoker, Class proxyType) {
        Map operations = rawInvoker.getOperationIndex();
        Map attributes = rawInvoker.getAttributeIndex();

        // build the method lookup table
        FastClass fastClass = FastClass.create(proxyType);
        ProxyInvoker[] invokers = new ProxyInvoker[fastClass.getMaxIndex() + 1];
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

    private ProxyInvoker createRawGBeanInvoker(RawInvoker rawInvoker, Method method, Map operations, Map attributes) {
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

    private ProxyInvoker[] createKernelGBeanInvokers(Kernel kernel, ObjectName objectName, Class proxyType) {
        GBeanInfo info;
        try {
            info = kernel.getGBeanInfo(objectName);
        } catch (Exception e) {
            throw new IllegalArgumentException("Could not get GBeanInfo for target object: " + objectName);
        }

        // build attributeName->attributeInfo map
        Set attributeInfos = info.getAttributes();
        Set attributeNames = new HashSet(attributeInfos.size());
        for (Iterator iterator = attributeInfos.iterator(); iterator.hasNext();) {
            GAttributeInfo attributeInfo = (GAttributeInfo) iterator.next();
            attributeNames.add(attributeInfo.getName());
        }

        // build operationSignature->operationInfo map
        Set operationInfos = info.getOperations();
        Set operationSignatures = new HashSet(operationInfos.size());
        for (Iterator iterator = operationInfos.iterator(); iterator.hasNext();) {
            GOperationInfo operationInfo = (GOperationInfo) iterator.next();
            operationSignatures.add(new GOperationSignature(operationInfo.getName(), operationInfo.getParameterList()));
        }

        // build the method lookup table
        FastClass fastClass = FastClass.create(proxyType);
        ProxyInvoker[] invokers = new ProxyInvoker[fastClass.getMaxIndex() + 1];
        Method[] methods = proxyType.getMethods();
        for (int i = 0; i < methods.length; i++) {
            Method method = methods[i];
            int interfaceIndex = getSuperIndex(proxyType, method);
            if (interfaceIndex >= 0) {
                invokers[interfaceIndex] = createJMXGBeanInvoker(kernel, method, operationSignatures, attributeNames);
            }
        }

        return invokers;
    }

    private ProxyInvoker createJMXGBeanInvoker(Kernel kernel, Method method, Set operationSignatures, Set attributeNames) {
        if (operationSignatures.contains(new GOperationSignature(method))) {
            return new KernelOperationInvoker(kernel, method);
        }

        String name = method.getName();
        if (name.startsWith("get")) {
            String attributeName = method.getName().substring(3);
            if (attributeNames.contains(attributeName)) {
                return new KernelGetAttributeInvoker(kernel, attributeName);
            }
            attributeName = Introspector.decapitalize(attributeName);
            if (attributeNames.contains(attributeName)) {
                return new KernelGetAttributeInvoker(kernel, attributeName);
            }
        } else if (name.startsWith("is")) {
            String attrName = method.getName().substring(2);
            if (attributeNames.contains(attrName)) {
                return new KernelGetAttributeInvoker(kernel, attrName);
            }
            attrName = Introspector.decapitalize(attrName);
            if (attributeNames.contains(attrName)) {
                return new KernelGetAttributeInvoker(kernel, attrName);
            }
        } else if (name.startsWith("set")) {
            String attrName = method.getName().substring(3);
            if (attributeNames.contains(attrName)) {
                return new KernelSetAttributeInvoker(kernel, attrName);
            }
            attrName = Introspector.decapitalize(attrName);
            if (attributeNames.contains(attrName)) {
                return new KernelSetAttributeInvoker(kernel, attrName);
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

    static final class HashCodeInvoke implements ProxyInvoker {
        public Object invoke(ObjectName objectName, Object[] arguments) throws Throwable {
            return new Integer(objectName.hashCode());
        }
    }

    static final class EqualsInvoke implements ProxyInvoker {
        private final ProxyManager proxyManager;

        public EqualsInvoke(ProxyManager proxyManager) {
            this.proxyManager = proxyManager;
        }

        public Object invoke(ObjectName objectName, Object[] arguments) throws Throwable {
            ObjectName proxyTarget = proxyManager.getProxyTarget(arguments[0]);
            return Boolean.valueOf(objectName.equals(proxyTarget));
        }
    }

    static final class ToStringInvoke implements ProxyInvoker {
        private final String interfaceName;

        public ToStringInvoke(String interfaceName) {
            this.interfaceName = "[" + interfaceName + ": ";
        }

        public Object invoke(ObjectName objectName, Object[] arguments) throws Throwable {
            return interfaceName + objectName + "]";
        }
    }

    static final class GetStateInvoke implements ProxyInvoker {
        private Kernel kernel;

        public GetStateInvoke(Kernel kernel) {
            this.kernel = kernel;
        }

        public Object invoke(ObjectName objectName, Object[] arguments) throws Throwable {
            return new Integer(kernel.getGBeanState(objectName));
        }
    }

    static final class GetStateInstanceInvoke implements ProxyInvoker {
        private Kernel kernel;

        public GetStateInstanceInvoke(Kernel kernel) {
            this.kernel = kernel;
        }

        public Object invoke(ObjectName objectName, Object[] arguments) throws Throwable {
            return State.fromInt(kernel.getGBeanState(objectName));
        }
    }

    static final class StartInvoke implements ProxyInvoker {
        private Kernel kernel;

        public StartInvoke(Kernel kernel) {
            this.kernel = kernel;
        }

        public Object invoke(ObjectName objectName, Object[] arguments) throws Throwable {
            kernel.startGBean(objectName);
            return null;
        }
    }

    static final class StartRecursiveInvoke implements ProxyInvoker {
        private Kernel kernel;

        public StartRecursiveInvoke(Kernel kernel) {
            this.kernel = kernel;
        }

        public Object invoke(ObjectName objectName, Object[] arguments) throws Throwable {
            kernel.startRecursiveGBean(objectName);
            return null;
        }
    }

    static final class GetStartTimeInvoke implements ProxyInvoker {
        private Kernel kernel;

        public GetStartTimeInvoke(Kernel kernel) {
            this.kernel = kernel;
        }

        public Object invoke(ObjectName objectName, Object[] arguments) throws Throwable {
            return new Long(kernel.getGBeanStartTime(objectName));
        }
    }

    static final class StopInvoke implements ProxyInvoker {
        private Kernel kernel;

        public StopInvoke(Kernel kernel) {
            this.kernel = kernel;
        }

        public Object invoke(ObjectName objectName, Object[] arguments) throws Throwable {
            kernel.stopGBean(objectName);
            return null;
        }
    }

    static final class GetObjectNameInvoke implements ProxyInvoker {
        public Object invoke(ObjectName objectName, Object[] arguments) throws Throwable {
            return objectName.getCanonicalName();
        }
    }
}
