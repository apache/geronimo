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

import java.util.IdentityHashMap;
import javax.management.ObjectName;

import net.sf.cglib.proxy.Callback;
import net.sf.cglib.proxy.Enhancer;
import net.sf.cglib.proxy.MethodInterceptor;
import org.apache.geronimo.kernel.Kernel;
import org.apache.geronimo.kernel.proxy.ProxyFactory;
import org.apache.geronimo.kernel.proxy.ProxyManager;

/**
 * @version $Rev$ $Date$
 */
public class BasicProxyManager implements ProxyManager {
    private final Kernel kernel;

    // todo use weak keys for this
    private final IdentityHashMap interceptors = new IdentityHashMap();

    public BasicProxyManager(Kernel kernel) {
        this.kernel = kernel;
    }

    public synchronized ProxyFactory createProxyFactory(Class type) {
        assert type != null: "type is null";
        return new ManagedProxyFactory(type);
    }

    public synchronized Object createProxy(ObjectName target, Class type) {
        assert type != null: "type is null";
        assert target != null: "target is null";

        return createProxyFactory(type).createProxy(target);
    }

    public synchronized void destroyProxy(Object proxy) {
        if (proxy == null) {
            return;
        }

        ProxyMethodInterceptor methodInterceptor = (ProxyMethodInterceptor) interceptors.remove(proxy);
        if (methodInterceptor != null) {
            methodInterceptor.destroy();
        }
    }

    public boolean isProxy(Object proxy) {
        return interceptors.containsKey(proxy);
    }

    public synchronized ObjectName getProxyTarget(Object proxy) {
        ProxyMethodInterceptor methodInterceptor = (ProxyMethodInterceptor) interceptors.remove(proxy);
        if (methodInterceptor == null) {
            return null;
        }
        return methodInterceptor.getObjectName();
    }

    private class ManagedProxyFactory implements ProxyFactory {
        private final Class type;
        private final Enhancer enhancer;

        public ManagedProxyFactory(Class type) {
            enhancer = new Enhancer();
            enhancer.setSuperclass(type);
            enhancer.setCallbackType(MethodInterceptor.class);
            enhancer.setUseFactory(false);
            this.type = enhancer.createClass();
        }

        public synchronized Object createProxy(ObjectName target) {
            assert target != null: "target is null";

            ProxyMethodInterceptor interceptor = new ProxyMethodInterceptor(type, kernel, target);

            // @todo trap CodeGenerationException indicating missing no-arg ctr
            enhancer.setCallbacks(new Callback[]{interceptor});
            Object proxy = enhancer.create();

            interceptors.put(proxy, interceptor);
            return proxy;
        }
    }
}
