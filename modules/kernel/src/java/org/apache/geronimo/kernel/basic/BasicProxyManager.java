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
 * Creates proxies that communicate directly with a Kernel located in the same
 * JVM as the client.
 *
 * @version $Rev$ $Date$
 */
public class BasicProxyManager implements ProxyManager {
    private final String MANAGED_BEAN_NAME = "org.apache.geronimo.kernel.proxy.GeronimoManagedBean";
    private final static Log log = LogFactory.getLog(BasicProxyManager.class);
    private final Kernel kernel;

    private final BasicProxyMap interceptors = new BasicProxyMap();

    public BasicProxyManager(Kernel kernel) {
        this.kernel = kernel;
    }

    public synchronized ProxyFactory createProxyFactory(Class type) {
        assert type != null: "type is null";
        if(type.getClassLoader() == null) {
            // Can't load GeronimoManagedBean if the incoming type doesn't have a ClassLoader set
            log.debug("Unable to add GeronimoManagedBean to proxy for "+type.getName()+" (no CL)");
            return new ManagedProxyFactory(type);
        } else {
            try {
                final Class managedBean = type.getClassLoader().loadClass(MANAGED_BEAN_NAME);
                return new ManagedProxyFactory(new Class[]{type, managedBean});
            } catch (ClassNotFoundException e) {
                log.debug("Unable to add GeronimoManagedBean to proxy for "+type.getName()+" (not in CL)");
                return new ManagedProxyFactory(type);
            }
        }
    }

    public synchronized ProxyFactory createProxyFactory(Class[] type) {
        assert type != null: "type is null";
        assert type.length > 0: "interface list is empty";
        Class managedBean = null;
        for (int i = 0; i < type.length; i++) {
            if(type[i].getClassLoader() != null) {
                try {
                    managedBean = type[i].getClassLoader().loadClass(MANAGED_BEAN_NAME);
                    break;
                } catch (ClassNotFoundException e) {} // OK, we'll try the next one
            }
        }
        if(managedBean != null) {
            Class[] adjusted = new Class[type.length+1];
            System.arraycopy(type, 0, adjusted, 0, type.length);
            adjusted[type.length] = managedBean;
            type = adjusted;
        } else {
            // Can't load GeronimoManagedBean if the incoming type doesn't have a ClassLoader set
            log.debug("Unable to add GeronimoManagedBean to proxy (no proxy classes have ClassLoaders)");
        }
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
            List intfs = new ArrayList();
            for (int i = 0; i < names.length; i++) {
                try {
                    intfs.add(loader.loadClass(names[i]));
                } catch (ClassNotFoundException e) {
                    log.warn("Could not load interface "+names[i]+" in provided ClassLoader for "+target.getKeyProperty("name"));
                }
            }
            return createProxyFactory((Class[]) intfs.toArray(new Class[intfs.size()])).createProxy(target);
        } catch (GBeanNotFoundException e) {
            throw new IllegalArgumentException("Could not get GBeanInfo for target object: " + target);
        }
    }

    public Object createProxy(ObjectName target, Class required, Class[] optional) {
        assert target != null: "target is null";
        if(required == null && (optional == null || optional.length == 0)) {
            throw new IllegalArgumentException("Cannot create proxy for no interfaces");
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
                    if (set.contains(optional[i].getName())) {
                        list.add(optional[i]);
                    }
                }
            } catch (GBeanNotFoundException e) {
                throw new IllegalArgumentException("Could not get GBeanInfo for target object: " + target);
            }
        }
        if(list.size() == 0) {
            return null;
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

        MethodInterceptor methodInterceptor = (MethodInterceptor) interceptors.remove(proxy);
        if (methodInterceptor != null) {
            doDestroy(methodInterceptor);
        }
    }

    public boolean isProxy(Object proxy) {
        return interceptors.containsKey(proxy);
    }

    public synchronized ObjectName getProxyTarget(Object proxy) {
        MethodInterceptor methodInterceptor = (MethodInterceptor) interceptors.get(proxy);
        if (methodInterceptor == null) {
            return null;
        }
        return getObjectName(methodInterceptor);
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
            } else if(type.length == 1) { // Unlikely (as a result of GeronimoManagedBean)
                enhancer.setSuperclass(type[0]);
            } else {
                ClassLoader best = null;
                outer:
                for (int i = 0; i < type.length; i++) {
                    ClassLoader test = type[i].getClassLoader();
                    for (int j = 0; j < type.length; j++) {
                        String className = type[j].getName();
                        try {
                            test.loadClass(className);
                        } catch (ClassNotFoundException e) {
                            continue outer;
                        }
                    }
                    best = test;
                    break;
                }
                if(best != null) {
                    enhancer.setClassLoader(best);
                }
                if(type[0].isInterface()) {
                    enhancer.setSuperclass(Object.class);
                    enhancer.setInterfaces(type);
                } else { // there's a class and reduceInterfaces put the class in the first spot
                    Class[] intfs = new Class[type.length-1];
                    System.arraycopy(type, 1, intfs, 0, intfs.length);
                    enhancer.setSuperclass(type[0]);
                    enhancer.setInterfaces(intfs);
                }
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
         *
         * If one of the entries is a class (not an interface), make sure it's
         * the first one in the array.  If more than one of the entries is a
         * class, throws an IllegalArgumentException
         *
         * @param source the original list of interfaces
         * @return the equal or smaller list of interfaces
         */
        private Class[] reduceInterfaces(Class[] source) {
            boolean changed = false;
            Class cls = null;
            for (int i = 0; i < source.length-1; i++) {
                Class original = source[i];
                if(original == null) {
                    continue;
                }
                if(!original.isInterface()) {
                    if(cls != null) {
                        throw new IllegalArgumentException(original.getName()+" is not an interface (already have "+cls.getName()+"); can only have one non-interface class for proxy");
                    } else {
                        cls = original;
                    }
                }
                for (int j = i+1; j < source.length; j++) {
                    Class other = source[j];
                    if(other == null) {
                        continue;
                    }
                    if(!other.isInterface()) {
                        if(cls != null) {
                            throw new IllegalArgumentException(other.getName()+" is not an interface (already have "+cls.getName()+"); can only have one non-interface class for proxy");
                        } else {
                            cls = other;
                        }
                    }
                    if(other.isAssignableFrom(original)) {
                        source[j] = null; // clear out "other"
                        changed = true;
                    } else if(original.isAssignableFrom(other)) {
                        source[i] = null; // clear out "original"
                        changed = true;
                        break; // the original has been eliminated; move on to the next original
                    }
                }
            }

            if(cls != null) {
                if(cls != source[0]) {
                    for (int i = 0; i < source.length; i++) {
                        if(cls == source[i]) {
                            Class temp = source[0];
                            source[0] = source[i];
                            source[i] = temp;
                            break;
                        }
                    }
                    changed = true;
                }
            }

            if(!changed) {
                return source;
            }
            List list = new ArrayList(source.length);
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

    protected void doDestroy(MethodInterceptor methodInterceptor) {
         ((ProxyMethodInterceptor)methodInterceptor).destroy();
    }

     protected ObjectName getObjectName(MethodInterceptor methodInterceptor) {
        return ((ProxyMethodInterceptor)methodInterceptor).getObjectName();
    }

}
