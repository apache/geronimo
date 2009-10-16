/**
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.apache.geronimo.system.jmx;

import net.sf.cglib.asm.Type;
import net.sf.cglib.core.Signature;
import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;
import net.sf.cglib.reflect.FastClass;
import org.apache.geronimo.gbean.AbstractName;
import org.apache.geronimo.kernel.Kernel;
import org.apache.geronimo.kernel.basic.KernelGetAttributeInvoker;
import org.apache.geronimo.kernel.basic.KernelOperationInvoker;
import org.apache.geronimo.kernel.basic.KernelSetAttributeInvoker;
import org.apache.geronimo.kernel.basic.ProxyInvoker;
import org.apache.geronimo.kernel.management.State;
import org.apache.geronimo.kernel.proxy.DeadProxyException;
import org.apache.geronimo.kernel.proxy.GeronimoManagedBean;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

/**
 * @version $Rev$ $Date$
 */
public class JMXProxyMethodInterceptor implements MethodInterceptor {
    /**
     * Type of the proxy interface
     */
    private final Class proxyType;

    /**
     * The object name to which we are connected.
     */
    private final AbstractName objectName;

    /**
     * GBeanInvokers keyed on the proxy interface method index
     */
    private ProxyInvoker[] gbeanInvokers;

    public JMXProxyMethodInterceptor(Class proxyType, Kernel kernel, AbstractName targetName) {
        assert proxyType != null;
        assert kernel != null;
        assert targetName != null;

        this.proxyType = proxyType;
        this.objectName = targetName;
        gbeanInvokers = createGBeanInvokers(kernel);
    }

    public synchronized void destroy() {
        gbeanInvokers = null;
    }

    public AbstractName getAbstractName() {
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
        for (Method method : methods) {
            int interfaceIndex = getSuperIndex(proxyType, method);
            if (interfaceIndex >= 0) {
                invokers[interfaceIndex] = createProxyInvoker(kernel, method);
            }
        }

        // handle equals, hashCode and toString directly here
        try {
            invokers[getSuperIndex(proxyType, proxyType.getMethod("equals", Object.class))] = new EqualsInvoke(kernel);
            invokers[getSuperIndex(proxyType, proxyType.getMethod("hashCode", (Class[])null))] = new HashCodeInvoke();
            invokers[getSuperIndex(proxyType, proxyType.getMethod("toString", (Class[])null))] = new ToStringInvoke(proxyType.getName());
            if(GeronimoManagedBean.class.isAssignableFrom(proxyType)) {
                invokers[getSuperIndex(proxyType, proxyType.getMethod("getState", (Class[])null))] = new GetStateInvoke(kernel);
                invokers[getSuperIndex(proxyType, proxyType.getMethod("getStateInstance", (Class[])null))] = new GetStateInstanceInvoke(kernel);
                invokers[getSuperIndex(proxyType, proxyType.getMethod("start", (Class[])null))] = new StartInvoke(kernel);
                invokers[getSuperIndex(proxyType, proxyType.getMethod("startRecursive", (Class[])null))] = new StartRecursiveInvoke(kernel);
                invokers[getSuperIndex(proxyType, proxyType.getMethod("stop", (Class[])null))] = new StopInvoke(kernel);
                invokers[getSuperIndex(proxyType, proxyType.getMethod("getStartTime", (Class[])null))] = new GetStartTimeInvoke(kernel);
                invokers[getSuperIndex(proxyType, proxyType.getMethod("getObjectName", (Class[])null))] = new GetObjectNameInvoke();
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
        public Object invoke(AbstractName abstractName, Object[] arguments) throws Throwable {
            return abstractName.hashCode();
        }
    }

    static final class EqualsInvoke implements ProxyInvoker {
        private final Kernel kernel;

        public EqualsInvoke(Kernel kernel) {
            this.kernel = kernel;
        }

        public Object invoke(AbstractName abstractName, Object[] arguments) throws Throwable {
            AbstractName proxyTarget = kernel.getAbstractNameFor(arguments[0]);
            return abstractName.equals(proxyTarget);
        }
    }

    static final class ToStringInvoke implements ProxyInvoker {
        private final String interfaceName;

        public ToStringInvoke(String interfaceName) {
            this.interfaceName = "[" + interfaceName + ": ";
        }

        public Object invoke(AbstractName abstractName, Object[] arguments) throws Throwable {
            return interfaceName + abstractName + "]";
        }
    }

    static final class GetStateInvoke implements ProxyInvoker {
        private Kernel kernel;

        public GetStateInvoke(Kernel kernel) {
            this.kernel = kernel;
        }

        public Object invoke(AbstractName abstractName, Object[] arguments) throws Throwable {
            return kernel.getGBeanState(abstractName);
        }
    }

    static final class GetStateInstanceInvoke implements ProxyInvoker {
        private Kernel kernel;

        public GetStateInstanceInvoke(Kernel kernel) {
            this.kernel = kernel;
        }

        public Object invoke(AbstractName abstractName, Object[] arguments) throws Throwable {
            return State.fromInt(kernel.getGBeanState(abstractName));
        }
    }

    static final class StartInvoke implements ProxyInvoker {
        private Kernel kernel;

        public StartInvoke(Kernel kernel) {
            this.kernel = kernel;
        }

        public Object invoke(AbstractName abstractName, Object[] arguments) throws Throwable {
            kernel.startGBean(abstractName);
            return null;
        }
    }

    static final class StartRecursiveInvoke implements ProxyInvoker {
        private Kernel kernel;

        public StartRecursiveInvoke(Kernel kernel) {
            this.kernel = kernel;
        }

        public Object invoke(AbstractName abstractName, Object[] arguments) throws Throwable {
            kernel.startRecursiveGBean(abstractName);
            return null;
        }
    }

    static final class GetStartTimeInvoke implements ProxyInvoker {
        private Kernel kernel;

        public GetStartTimeInvoke(Kernel kernel) {
            this.kernel = kernel;
        }

        public Object invoke(AbstractName abstractName, Object[] arguments) throws Throwable {
            return kernel.getGBeanStartTime(abstractName);
        }
    }

    static final class StopInvoke implements ProxyInvoker {
        private Kernel kernel;

        public StopInvoke(Kernel kernel) {
            this.kernel = kernel;
        }

        public Object invoke(AbstractName abstractName, Object[] arguments) throws Throwable {
            kernel.stopGBean(abstractName);
            return null;
        }
    }

    static final class GetObjectNameInvoke implements ProxyInvoker {
        public Object invoke(AbstractName abstractName, Object[] arguments) throws Throwable {
            return abstractName.getObjectName().getCanonicalName();
        }
    }
}
