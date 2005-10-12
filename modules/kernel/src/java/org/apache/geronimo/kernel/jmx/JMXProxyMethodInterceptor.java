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
package org.apache.geronimo.kernel.jmx;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import javax.management.ObjectName;

import net.sf.cglib.asm.Type;
import net.sf.cglib.core.Signature;
import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;
import net.sf.cglib.reflect.FastClass;
import org.apache.geronimo.kernel.Kernel;
import org.apache.geronimo.kernel.management.State;
import org.apache.geronimo.kernel.basic.KernelGetAttributeInvoker;
import org.apache.geronimo.kernel.basic.KernelOperationInvoker;
import org.apache.geronimo.kernel.basic.KernelSetAttributeInvoker;
import org.apache.geronimo.kernel.basic.ProxyInvoker;
import org.apache.geronimo.kernel.proxy.DeadProxyException;
import org.apache.geronimo.kernel.proxy.ProxyManager;
import org.apache.geronimo.kernel.proxy.GeronimoManagedBean;

/**
 * @version $Rev: 106345 $ $Date: 2004-11-23 12:37:03 -0800 (Tue, 23 Nov 2004) $
 */
public class JMXProxyMethodInterceptor implements MethodInterceptor {
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

    public JMXProxyMethodInterceptor(Class proxyType, Kernel kernel, ObjectName objectName) {
        assert proxyType != null;
        assert kernel != null;
        assert objectName != null;

        this.proxyType = proxyType;
        this.objectName = objectName;
        gbeanInvokers = createGBeanInvokers(kernel);
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

    private ProxyInvoker[] createGBeanInvokers(Kernel kernel) {
        // build the method lookup table
        FastClass fastClass = FastClass.create(proxyType);
        ProxyInvoker[] invokers = new ProxyInvoker[fastClass.getMaxIndex() + 1];
        Method[] methods = proxyType.getMethods();
        for (int i = 0; i < methods.length; i++) {
            Method method = methods[i];
            int interfaceIndex = getSuperIndex(proxyType, method);
            if (interfaceIndex >= 0) {
                invokers[interfaceIndex] = createProxyInvoker(kernel, method);
            }
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

    private ProxyInvoker createProxyInvoker(Kernel kernel, Method method) {
        String methodName = method.getName();
        if (!Modifier.isPublic(method.getModifiers()) || Modifier.isStatic(method.getModifiers())) {
            return null;
        }

        // is this a getter is "is" method
        if (method.getParameterTypes().length == 0 && method.getReturnType() != Void.TYPE) {
            if (methodName.length() > 3 && methodName.startsWith("get") && !methodName.equals("getClass")) {
                String propertyName = decapitalizePropertyName(methodName.substring(3));
                return new KernelGetAttributeInvoker(kernel, propertyName);
            } else if (methodName.length() > 2 && methodName.startsWith("is")) {
                String propertyName = decapitalizePropertyName(methodName.substring(2));
                return new KernelGetAttributeInvoker(kernel, propertyName);
            }
        }

        // is this a setter method
        if (method.getParameterTypes().length == 1 &&
                method.getReturnType() == Void.TYPE &&
                methodName.length() > 3 &&
                methodName.startsWith("set")) {
            String propertyName = decapitalizePropertyName(methodName.substring(3));
            return new KernelSetAttributeInvoker(kernel, propertyName);
        }

        // it is just a plain old opertaion
        return new KernelOperationInvoker(kernel, method);
    }

    private static int getSuperIndex(Class proxyType, Method method) {
        Signature signature = new Signature(method.getName(), Type.getReturnType(method), Type.getArgumentTypes(method));
        MethodProxy methodProxy = MethodProxy.find(proxyType, signature);
        if (methodProxy != null) {
            return methodProxy.getSuperIndex();
        }
        return -1;
    }

    private static String decapitalizePropertyName(String propertyName) {
        if (Character.isUpperCase(propertyName.charAt(0))) {
            return Character.toLowerCase(propertyName.charAt(0)) + propertyName.substring(1);
        }
        return propertyName;
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
