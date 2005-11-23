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
import org.apache.geronimo.kernel.GBeanNotFoundException;
import org.apache.geronimo.kernel.Kernel;
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

/**
 * Creates proxies that communicate directly with a Kernel located in the same
 * JVM as the client.
 *
 * @version $Rev$ $Date$
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

        ClassLoader classLoader = type.getClassLoader();
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
        Object proxy = proxyFactory.createProxy(target);
        return proxy;
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
            List intfs = new ArrayList();
            for (int i = 0; i < names.length; i++) {
                try {
                    intfs.add(classLoader.loadClass(names[i]));
                } catch (ClassNotFoundException e) {
                    log.warn("Could not load interface "+names[i]+" in provided ClassLoader for "+target.getKeyProperty("name"));
                }
            }
            return createProxyFactory((Class[]) intfs.toArray(new Class[intfs.size()]), classLoader).createProxy(target);
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

    public ObjectName getProxyTarget(Object proxy) {
        MethodInterceptor methodInterceptor = (MethodInterceptor) interceptors.get(proxy);
        if (methodInterceptor == null) {
            return null;
        }
        return getObjectName(methodInterceptor);
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
                type = reduceInterfaces(type);
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
                  throw (RuntimeException) cause;
                } else if (cause != null) {
                  throw new ProxyCreationException(cause);
                } else {
                  throw new ProxyCreationException(e);
                }
            }
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
