/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */


package org.apache.geronimo.openejb.cdi;

import java.net.URL;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import javassist.util.proxy.ProxyFactory;
import javassist.util.proxy.ProxyObject;
import org.apache.geronimo.gbean.annotation.GBean;
import org.apache.geronimo.gbean.annotation.ParamSpecial;
import org.apache.geronimo.gbean.annotation.SpecialAttributeType;
import org.osgi.framework.Bundle;

/**
 * @version $Rev$ $Date$
 */

@GBean
public class SingletonServiceInitializerGBean {

    public SingletonServiceInitializerGBean(@ParamSpecial(type = SpecialAttributeType.bundle) Bundle bundle) {
        GeronimoSingletonService.init(bundle);
        ProxyFactory.classLoaderProvider = new GeronimoClassLoaderProvider();
    }

    private static class GeronimoClassLoaderProvider implements ProxyFactory.ClassLoaderProvider {

        private final ConcurrentMap<Bundle, ClassLoader> proxyClassLoaders = new ConcurrentHashMap<Bundle, ClassLoader>();

        @Override
        public ClassLoader get(ProxyFactory proxyFactory) {
            Collection<ClassLoader> baseCl = getBaseCl(proxyFactory);
            if (baseCl.size() == 1) {
                return baseCl.iterator().next();
            }
//            Bundle bundle = null;
//            if (baseCl instanceof BundleReference) {
//                bundle = ((BundleReference)baseCl).getBundle();
//                ClassLoader proxyCl = proxyClassLoaders.get(bundle);
//                if (proxyCl != null) {
//                    return proxyCl;
//                }
//            }
//            ClassLoader extensionCl = baseProvider.getClass().getClassLoader();
            ClassLoader proxyCl = new MultiParentClassLoader(baseCl);
//            if (bundle != null) {
//                ClassLoader oldCl = proxyClassLoaders.putIfAbsent(bundle, proxyCl);
//                if (oldCl != null) {
//                    return oldCl;
//                }
//            }
            return proxyCl;
        }

        private Collection<ClassLoader> getBaseCl(ProxyFactory proxyFactory) {
            Set<Class<?>> classes = new HashSet<Class<?>>();
            classes.add(ProxyObject.class);
            Class<?> superClass = proxyFactory.getSuperclass();
            if (superClass != null && superClass != Object.class) {
                classes.add(superClass);
            }
            for (Class<?> clazz: proxyFactory.getInterfaces()) {
                classes.add(clazz);
            }
            Map<Class<?>, ClassLoader> classLoaderMap = new HashMap<Class<?>, ClassLoader>(3);
            for (Class<?> clazz: classes) {
                ClassLoader newCl = clazz.getClassLoader();
                if (newCl != null) {
                    boolean loadable = false;
                    for (Map.Entry<Class<?>, ClassLoader> entry: classLoaderMap.entrySet()) {
                        try {
                            entry.getValue().loadClass(clazz.getName());
                            loadable = true;
                            break;
                        } catch (ClassNotFoundException e) {
                            //continue looking
                        }
                    }
                    if (!loadable) {
                        for (Iterator<Map.Entry<Class<?>, ClassLoader>> it = classLoaderMap.entrySet().iterator(); it.hasNext(); ) {
                            Map.Entry<Class<?>, ClassLoader> entry = it.next();
                            try {
                                newCl.loadClass(entry.getKey().getName());
                                it.remove();
                            } catch (ClassNotFoundException e) {
                                //continue
                            }
                        }
                        classLoaderMap.put(clazz, newCl);
                    }
                }
            }
            //check for default access superclass and more than one or wrong classloader


            return classLoaderMap.values();
        }

        private static class MultiParentClassLoader extends ClassLoader {
            private final Collection<ClassLoader> classLoaders;
            public MultiParentClassLoader(Collection<ClassLoader> classLoaders) {
                this.classLoaders = classLoaders;
            }

            @Override
            public Class<?> loadClass(String name) throws ClassNotFoundException {
                for (ClassLoader cl: classLoaders) {
                    try {
                        return cl.loadClass(name);
                    } catch (ClassNotFoundException e) {
                        //ignore
                    }

                }
                return super.loadClass(name);
            }

            @Override
            public URL getResource(String name) {
                for (ClassLoader cl: classLoaders) {
                    URL url = cl.getResource(name);
                    if (url != null) {
                        return url;
                    }
                }
                return null;
            }
        }
    }
}
