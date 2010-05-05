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
package org.apache.geronimo.kernel.basic;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.sf.cglib.proxy.Callback;
import net.sf.cglib.proxy.Enhancer;
import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.reflect.FastClass;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.geronimo.gbean.AbstractName;
import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.kernel.ClassLoading;
import org.apache.geronimo.kernel.GBeanNotFoundException;
import org.apache.geronimo.kernel.Kernel;
import org.apache.geronimo.kernel.classloader.DelegatingClassLoader;
import org.apache.geronimo.kernel.proxy.ProxyCreationException;
import org.apache.geronimo.kernel.proxy.ProxyFactory;
import org.apache.geronimo.kernel.proxy.ProxyManager;

/**
 * Creates proxies that communicate directly with a Kernel located in the same
 * JVM as the client.
 *
 * @version $Rev:386515 $ $Date$
 */
public class BasicProxyManager implements ProxyManager {
    private final static String MANAGED_BEAN_NAME = "org.apache.geronimo.kernel.proxy.GeronimoManagedBean";
    private final static Logger log = LoggerFactory.getLogger(BasicProxyManager.class);
    private final Kernel kernel;

    private final Map interceptors = Collections.synchronizedMap(new BasicProxyMap());

    public BasicProxyManager(Kernel kernel) {
        this.kernel = kernel;
    }

    public ProxyFactory createProxyFactory(Class[] types, ClassLoader classLoader) {
        if (types == null) throw new NullPointerException("type is null");
        if (types.length == 0) throw new IllegalArgumentException("interface list is empty");
        if (classLoader == null) throw new NullPointerException("classLoader is null");

        Class managedBean = null;
        try {
            managedBean = classLoader.loadClass(MANAGED_BEAN_NAME);
        } catch (ClassNotFoundException e) {
            // Can't load GeronimoManagedBean if the incoming type doesn't have a ClassLoader set
            log.debug("Unable to add GeronimoManagedBean to proxy (specified class loader does not have class)");
        }

        if(managedBean != null) {
            Class[] adjusted = new Class[types.length+1];
            System.arraycopy(types, 0, adjusted, 0, types.length);
            adjusted[types.length] = managedBean;
            types = adjusted;
        }

        return new ManagedProxyFactory(types, classLoader);
    }

    public <T> T createProxy(AbstractName target, Class<T> type) {
        if (target == null) throw new NullPointerException("target is null");
        if (type == null) throw new NullPointerException("type is null");

        try {
            // if the type is visible from the target's classloader use it
            // otherwise use the type's classloader
            ClassLoader classLoader = type.getClassLoader();
            
            // add any interface exposed by the gbean that is visible from the selected class loader
            List<Class> types = getVisibleInterfaces(target, classLoader, true);
            if (types == null) {
                types = new ArrayList<Class>();
            }
            types.add(type);

            DelegatingClassLoader proxyClassLoader = new DelegatingClassLoader();
            proxyClassLoader.addLoader(classLoader);
            proxyClassLoader.addLoader(getClass()); // to be able to load GeronimoManagedBean
            
            return (T) createProxyFactory((Class[]) types.toArray(new Class[types.size()]), proxyClassLoader).createProxy(target);
        } catch (GBeanNotFoundException e) {
            throw new IllegalArgumentException("Could not get GBeanInfo for target object: " + target, e);
        }
    }
    
    public Object createProxy(AbstractName target, ClassLoader classLoader) {
        if (target == null) throw new NullPointerException("target is null");
        if (classLoader == null) throw new NullPointerException("classLoader is null");

        try {
            List<Class> types = getVisibleInterfaces(target, classLoader, true);
            if (types == null) {
                return null;
            }
            
            DelegatingClassLoader proxyClassLoader = new DelegatingClassLoader();
            proxyClassLoader.addLoader(classLoader);
            proxyClassLoader.addLoader(getClass()); // to be able to load GeronimoManagedBean
            
            return createProxyFactory((Class[]) types.toArray(new Class[types.size()]), proxyClassLoader).createProxy(target);
        } catch (GBeanNotFoundException e) {
            throw new IllegalArgumentException("Could not get GBeanInfo for target object: " + target, e);
        }
    }

    private List<Class> getVisibleInterfaces(AbstractName target, ClassLoader classLoader, boolean shouldLog) throws GBeanNotFoundException {
        GBeanInfo info = kernel.getGBeanInfo(target);
        Set<String> interfaces = info.getInterfaces();
        if(interfaces.size() == 0) {
            if (shouldLog) {
                log.warn("No interfaces found for " + target + " ("+target+")");
            }
            return null;
        }
        String[] names = (String[]) interfaces.toArray(new String[0]);
        List<Class> types = new ArrayList<Class>();
        for (int i = 0; i < names.length; i++) {
            try {
                Class<?> type = classLoader.loadClass(names[i]);
                if (type.isInterface()) {
                    types.add(type);
                }
            } catch (ClassNotFoundException e) {
                if (shouldLog) {
                    log.warn("Could not load interface "+names[i]+" in provided ClassLoader for "+target);
                }
            }
        }
        return types;
    }

    public void destroyProxy(Object proxy) {
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

    public AbstractName getProxyTarget(Object proxy) {
        MethodInterceptor methodInterceptor = (MethodInterceptor) interceptors.get(proxy);
        if (methodInterceptor == null) {
            return null;
        }
        return getAbstractName(methodInterceptor);
    }

    private class ManagedProxyFactory implements ProxyFactory {
        private final Class proxyType;
        private final FastClass fastClass;

        public ManagedProxyFactory(Class type, ClassLoader classLoader) {
            this(new Class[]{type}, classLoader);
        }

        public ManagedProxyFactory(Class[] type, ClassLoader classLoader) {
            Enhancer enhancer = new Enhancer();
            if(type.length > 1) { // shrink first -- may reduce from many to one
                type = ClassLoading.reduceInterfaces(type);
            }
            if(type.length == 0) {
                throw new IllegalArgumentException("Cannot generate proxy for 0 interfaces!");
            } else if(type.length == 1) { // Unlikely (as a result of GeronimoManagedBean)
                enhancer.setSuperclass(type[0]);
            } else {
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
            enhancer.setClassLoader(classLoader);
            enhancer.setCallbackType(MethodInterceptor.class);
            enhancer.setUseFactory(false);
            proxyType = enhancer.createClass();
            fastClass = FastClass.create(proxyType);
        }

        public Object createProxy(AbstractName target) {
            assert target != null: "target is null";

            Callback callback = getMethodInterceptor(proxyType, kernel, target);

            try {
                Enhancer.registerCallbacks(proxyType, new Callback[]{callback});
                Object proxy = fastClass.newInstance();
                interceptors.put(proxy, callback);
                return proxy;
            } catch (InvocationTargetException e) {
                Throwable cause = e.getCause();
                if (cause instanceof RuntimeException) {
                    throw (RuntimeException) cause;
                } else  if (cause instanceof Error) {
                    throw (Error) cause;
                } else if (cause != null) {
                    throw new ProxyCreationException(cause);
                } else {
                    throw new ProxyCreationException(e);
                }
            } finally {
                Enhancer.registerCallbacks(proxyType, null);
            }
        }
    }

    protected Callback getMethodInterceptor(Class proxyType, Kernel kernel, AbstractName target) {
        return new ProxyMethodInterceptor(proxyType, kernel, target);
    }

    protected void doDestroy(MethodInterceptor methodInterceptor) {
         ((ProxyMethodInterceptor)methodInterceptor).destroy();
    }

    protected AbstractName getAbstractName(MethodInterceptor methodInterceptor) {
        return ((ProxyMethodInterceptor)methodInterceptor).getAbstractName();
    }
}
