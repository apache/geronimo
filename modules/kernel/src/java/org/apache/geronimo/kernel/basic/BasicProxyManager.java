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

import net.sf.cglib.proxy.Callback;
import net.sf.cglib.proxy.Enhancer;
import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.reflect.FastClass;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.gbean.AbstractName;
import org.apache.geronimo.gbean.GBeanData;
import org.apache.geronimo.kernel.GBeanNotFoundException;
import org.apache.geronimo.kernel.Kernel;
import org.apache.geronimo.kernel.ClassLoading;
import org.apache.geronimo.kernel.proxy.ProxyFactory;
import org.apache.geronimo.kernel.proxy.ProxyManager;
import org.apache.geronimo.kernel.proxy.ProxyCreationException;

import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.Map;
import java.util.Collections;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;

/**
 * Creates proxies that communicate directly with a Kernel located in the same
 * JVM as the client.
 *
 * @version $Rev:386515 $ $Date$
 */
public class BasicProxyManager implements ProxyManager {
    private final static String MANAGED_BEAN_NAME = "org.apache.geronimo.kernel.proxy.GeronimoManagedBean";
    private final static Log log = LogFactory.getLog(BasicProxyManager.class);
    private final Kernel kernel;

    private final Map interceptors = Collections.synchronizedMap(new BasicProxyMap());

    public BasicProxyManager(Kernel kernel) {
        this.kernel = kernel;
    }

    /**
     * Creates a proxy factory for GBeans of the specified type.  The proxy class will be created within the class
     * loader from which the specified type was loaded, or from the system class loader if the specified type has
     * a null class loader.
     *
     * @param type the type of the proxies this factory should create
     * @return the proxy factory
     */
    public ProxyFactory createProxyFactory(Class type) {
        if (type == null) throw new NullPointerException("type is null");
        if (!type.isInterface() && !hasDefaultConstructor(type)) {
            throw new IllegalArgumentException("Type class does not have a default constructor " + type.getName());
        }

        ClassLoader classLoader = type.getClassLoader();
        if(classLoader == null) {
            classLoader = getClass().getClassLoader();
        }
        if(classLoader == null) {
            classLoader = ClassLoader.getSystemClassLoader();
        }

        return createProxyFactory(new Class[] {type}, classLoader);
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

    public Object createProxy(ObjectName target, Class type) {
        if (target == null) throw new NullPointerException("target is null");
        if (type == null) throw new NullPointerException("type is null");

        ProxyFactory proxyFactory = createProxyFactory(type);
        return proxyFactory.createProxy(target);
    }

    public Object createProxy(AbstractName target, Class type) {
        if (target == null) throw new NullPointerException("target is null");
        if (type == null) throw new NullPointerException("type is null");

        ProxyFactory proxyFactory = createProxyFactory(type);
        return proxyFactory.createProxy(target);
    }

    public Object createProxy(ObjectName target, ClassLoader classLoader) {
        if (target == null) throw new NullPointerException("target is null");
        if (classLoader == null) throw new NullPointerException("classLoader is null");

        try {
            GBeanInfo info = kernel.getGBeanInfo(target);
            Set interfaces = info.getInterfaces();
            if(interfaces.size() == 0) {
                log.warn("No interfaces found for " + target + " ("+info.getClassName()+")");
                return null;
            }
            String[] names = (String[]) interfaces.toArray(new String[0]);
            List types = new ArrayList();
            for (int i = 0; i < names.length; i++) {
                try {
                    Class type = classLoader.loadClass(names[i]);
                    if (type.isInterface() || hasDefaultConstructor(type)) {
                        types.add(type);
                    }
                } catch (ClassNotFoundException e) {
                    log.warn("Could not load interface "+names[i]+" in provided ClassLoader for "+target.getKeyProperty("name"));
                }
            }
            return createProxyFactory((Class[]) types.toArray(new Class[types.size()]), classLoader).createProxy(target);
        } catch (GBeanNotFoundException e) {
            throw new IllegalArgumentException("Could not get GBeanInfo for target object: " + target);
        }
    }

    public Object createProxy(AbstractName target, ClassLoader classLoader) {
        if (target == null) throw new NullPointerException("target is null");
        if (classLoader == null) throw new NullPointerException("classLoader is null");

        try {
            GBeanInfo info = kernel.getGBeanInfo(target);
            Set interfaces = info.getInterfaces();
            if(interfaces.size() == 0) {
                log.warn("No interfaces found for " + target + " ("+target+")");
                return null;
            }
            String[] names = (String[]) interfaces.toArray(new String[0]);
            List types = new ArrayList();
            for (int i = 0; i < names.length; i++) {
                try {
                    Class type = classLoader.loadClass(names[i]);
                    if (type.isInterface() || hasDefaultConstructor(type)) {
                        types.add(type);
                    }
                } catch (ClassNotFoundException e) {
                    log.warn("Could not load interface "+names[i]+" in provided ClassLoader for "+target);
                }
            }
            return createProxyFactory((Class[]) types.toArray(new Class[types.size()]), classLoader).createProxy(target);
        } catch (GBeanNotFoundException e) {
            throw new IllegalArgumentException("Could not get GBeanInfo for target object: " + target);
        }
    }

    public Object[] createProxies(String[] objectNameStrings, ClassLoader classLoader) throws MalformedObjectNameException {
        Object[] result = new Object[objectNameStrings.length];
        for (int i = 0; i < result.length; i++) {
            result[i] = createProxy(ObjectName.getInstance(objectNameStrings[i]), classLoader);
        }
        return result;
    }
    public Object[] createProxies(AbstractName[] names, ClassLoader classLoader) {
        Object[] result = new Object[names.length];
        for (int i = 0; i < result.length; i++) {
            result[i] = createProxy(names[i], classLoader);
        }
        return result;
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

        public Object createProxy(ObjectName target) {
            assert target != null: "target is null";

            Callback callback;
            try {
                AbstractName targetName = getAbstractName(target, kernel);
                callback = getMethodInterceptor(proxyType, kernel, targetName);
            } catch (GBeanNotFoundException e) {
                throw new ProxyCreationException(e);
            }

            Enhancer.registerCallbacks(proxyType, new Callback[]{callback});
            try {
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
            }
        }
        public Object createProxy(AbstractName target) {
            assert target != null: "target is null";

            Callback callback = getMethodInterceptor(proxyType, kernel, target);

            Enhancer.registerCallbacks(proxyType, new Callback[]{callback});
            try {
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
            }
        }
    }

    protected Callback getMethodInterceptor(Class proxyType, Kernel kernel, ObjectName target) throws GBeanNotFoundException {
        AbstractName targetName = getAbstractName(target, kernel);
        return new ProxyMethodInterceptor(proxyType, kernel, targetName);
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

    private AbstractName getAbstractName(ObjectName objectName, Kernel kernel) throws GBeanNotFoundException {
        GBeanData gBeanData = kernel.getGBeanData(objectName);
        return gBeanData.getAbstractName();
    }

    public static boolean hasDefaultConstructor(Class type) {
        if (!Modifier.isPublic(type.getModifiers())) {
            return false;
        }
        Constructor[] constructors = type.getDeclaredConstructors();
        for (int i = 0; i < constructors.length; i++) {
            Constructor constructor = constructors[i];
            boolean accessible = Modifier.isPublic(constructor.getModifiers()) ||
                    Modifier.isProtected(constructor.getModifiers());
            if (accessible && constructor.getParameterTypes().length == 0) {
                return true;
            }
        }
        return false;
    }
}
