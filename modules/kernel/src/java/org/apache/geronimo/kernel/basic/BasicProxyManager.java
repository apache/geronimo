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
import java.util.List;
import java.util.ArrayList;
import java.util.Set;
import javax.management.ObjectName;
import javax.management.MalformedObjectNameException;

import net.sf.cglib.proxy.Callback;
import net.sf.cglib.proxy.Enhancer;
import net.sf.cglib.proxy.MethodInterceptor;
import org.apache.geronimo.kernel.Kernel;
import org.apache.geronimo.kernel.GBeanNotFoundException;
import org.apache.geronimo.kernel.proxy.ProxyFactory;
import org.apache.geronimo.kernel.proxy.ProxyManager;
import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * @version $Rev$ $Date$
 */
public class BasicProxyManager implements ProxyManager {
    private final static Log log = LogFactory.getLog(BasicProxyManager.class);
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

    public synchronized ProxyFactory createProxyFactory(Class[] type) {
        assert type != null: "type is null";
        return new ManagedProxyFactory(type);
    }

    public synchronized Object createProxy(ObjectName target, Class type) {
        assert type != null: "type is null";
        assert target != null: "target is null";

        return createProxyFactory(type).createProxy(target);
    }

    public Object createProxy(ObjectName target, ClassLoader loader) {
        assert target != null: "target is null";
        try {
            GBeanInfo info = kernel.getGBeanInfo(target);
            if(info.getInterfaces().size() == 0) {
                log.warn("No interfaces found for "+target+" ("+info.getClassName()+")");
                return null;
            }
            String[] names = (String[]) info.getInterfaces().toArray(new String[0]);
            Class[] intfs = new Class[names.length];
            for (int i = 0; i < intfs.length; i++) {
                intfs[i] = loader.loadClass(names[i]);
            }
            return createProxyFactory(intfs).createProxy(target);
        } catch (GBeanNotFoundException e) {
            throw new IllegalArgumentException("Could not get GBeanInfo for target object: " + target);
        } catch (ClassNotFoundException e) {
            throw new IllegalArgumentException("Could not load interface in provided ClassLoader: " + e.getMessage());
        }
    }

    public Object createProxy(ObjectName target, Class required, Class[] optional) {
        assert target != null: "target is null";
        if(required == null && (optional == null || optional.length == 0)) {
            throw new IllegalArgumentException("Cannot create proxy for no interfaces");
        }
        if(required != null && !required.isInterface()) {
            throw new IllegalArgumentException("Cannot create a proxy for a class (only interfaces) -- "+required.getName());
        }
        List list = new ArrayList();
        if(required != null) {
            list.add(required);
        }
        if (optional != null) {
            try {
                GBeanInfo info = kernel.getGBeanInfo(target);
                Set set = info.getInterfaces();
                for (int i = 0; i < optional.length; i++) {
                    if (!optional[i].isInterface()) {
                        throw new IllegalArgumentException("Cannot create a proxy for a class (only interfaces) -- " + optional[i].getName());
                    }
                    if (set.contains(optional[i].getName())) {
                        list.add(optional[i]);
                    }
                }
            } catch (GBeanNotFoundException e) {
                throw new IllegalArgumentException("Could not get GBeanInfo for target object: " + target);
            }
        }
        return createProxyFactory((Class[]) list.toArray(new Class[list.size()])).createProxy(target);
    }

    public Object[] createProxies(String[] objectNameStrings, ClassLoader loader) throws MalformedObjectNameException {
        Object[] result = new Object[objectNameStrings.length];
        for (int i = 0; i < result.length; i++) {
            result[i] = createProxy(ObjectName.getInstance(objectNameStrings[i]), loader);
        }
        return result;
    }

    public Object[] createProxies(String[] objectNameStrings, Class required, Class[] optional) throws MalformedObjectNameException {
        Object[] result = new Object[objectNameStrings.length];
        for (int i = 0; i < result.length; i++) {
            result[i] = createProxy(ObjectName.getInstance(objectNameStrings[i]), required, optional);
        }
        return result;
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
        ProxyMethodInterceptor methodInterceptor = (ProxyMethodInterceptor) interceptors.get(proxy);
        if (methodInterceptor == null) {
            return null;
        }
        return methodInterceptor.getObjectName();
    }

    private class ManagedProxyFactory implements ProxyFactory {
        private final Class proxyType;
        private final Enhancer enhancer;

        public ManagedProxyFactory(Class type) {
            this(new Class[]{type});
        }

        public ManagedProxyFactory(Class[] type) {
            enhancer = new Enhancer();
            if(type.length > 1) { // shrink first -- may reduce from many to one
                type = reduceInterfaces(type);
            }
            if(type.length == 0) {
                throw new IllegalArgumentException("Cannot generate proxy for 0 interfaces!");
            } else if(type.length == 1) {
                enhancer.setSuperclass(type[0]);
            } else {
                enhancer.setSuperclass(Object.class);
                enhancer.setInterfaces(type);
            }
            enhancer.setCallbackType(MethodInterceptor.class);
            enhancer.setUseFactory(false);
            proxyType = enhancer.createClass();
        }

        public synchronized Object createProxy(ObjectName target) {
            assert target != null: "target is null";

            Callback callback = getMethodInterceptor(proxyType, kernel, target);

            // @todo trap CodeGenerationException indicating missing no-arg ctr
            enhancer.setCallbacks(new Callback[]{callback});
            Object proxy = enhancer.create();

            interceptors.put(proxy, callback);
            return proxy;
        }

        /**
         * If there are multiple interfaces, and some of them extend each other,
         * eliminate the superclass in favor of the subclasses that extend them.
         * @param source the original list of interfaces
         * @return the equal or smaller list of interfaces
         */
        private Class[] reduceInterfaces(Class[] source) {
            boolean changed = false;
            for (int i = 0; i < source.length-1; i++) {
                Class original = source[i];
                if(original == null) {
                    continue;
                }
                if(!original.isInterface()) {
                    throw new IllegalArgumentException(original.getName()+" is not an interface; cannot generate proxy");
                }
                for (int j = i+1; j < source.length; j++) {
                    Class other = source[j];
                    if(other == null) {
                        continue;
                    }
                    if(!other.isInterface()) {
                        throw new IllegalArgumentException(other.getName()+" is not an interface; cannot generate proxy");
                    }
                    if(other.isAssignableFrom(original)) {
                        other = null;
                        changed = true;
                    } else if(original.isAssignableFrom(other)) {
                        original = null;
                        changed = true;
                        break; // the original has been eliminated; move on to the next original
                    }
                }
            }

            if(!changed) {
                return source;
            }
            List list = new ArrayList(source.length-1);
            for (int i = 0; i < source.length; i++) {
                if(source[i] != null) {
                    list.add(source[i]);
                }
            }
            return (Class[]) list.toArray(new Class[list.size()]);
        }
    }

    protected Callback getMethodInterceptor(Class proxyType, Kernel kernel, ObjectName target) {
        return new ProxyMethodInterceptor(proxyType, kernel, target);
    }
}
