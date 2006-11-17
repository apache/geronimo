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

package org.apache.geronimo.axis.client;

import java.lang.reflect.InvocationTargetException;
import java.util.Iterator;
import java.util.Map;

import javax.naming.NamingException;

import net.sf.cglib.proxy.Callback;
import net.sf.cglib.proxy.Enhancer;
import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.NoOp;
import net.sf.cglib.reflect.FastClass;
import net.sf.cglib.reflect.FastConstructor;
import org.apache.axis.client.Service;
import org.apache.geronimo.naming.reference.ClassLoaderAwareReference;
import org.apache.geronimo.naming.reference.SimpleReference;

/**
 * @version $Rev$ $Date$
 */
public class AxisServiceReference extends SimpleReference implements ClassLoaderAwareReference {
    private static final Class[] SERVICE_CONSTRUCTOR_TYPES = new Class[]{Map.class, Map.class};

    private String serviceInterfaceClassName;
    private Map seiPortNameToFactoryMap;
    private Map seiClassNameToFactoryMap;
    private ClassLoader classLoader;

    public AxisServiceReference(String serviceInterfaceClassName, Map seiPortNameToFactoryMap, Map seiClassNameToFactoryMap) {
        this.serviceInterfaceClassName = serviceInterfaceClassName;
        this.seiPortNameToFactoryMap = seiPortNameToFactoryMap;
        this.seiClassNameToFactoryMap = seiClassNameToFactoryMap;
    }

    public void setClassLoader(ClassLoader classLoader) {
        this.classLoader = classLoader;
    }

    public Object getContent() throws NamingException {
        Class serviceInterface;
        try {
            serviceInterface = classLoader.loadClass(serviceInterfaceClassName);
        } catch (ClassNotFoundException e) {
            throw new NamingException("Could not load service interface class " + serviceInterfaceClassName);
        }
        Object serviceInstance = createServiceInterfaceProxy(serviceInterface, seiPortNameToFactoryMap, seiClassNameToFactoryMap, classLoader);
        for (Iterator iterator = seiPortNameToFactoryMap.values().iterator(); iterator.hasNext();) {
            SEIFactoryImpl seiFactory = (SEIFactoryImpl) iterator.next();
            try {
                seiFactory.initialize(serviceInstance, classLoader);
            } catch (ClassNotFoundException e) {
                throw new NamingException("Could not load service interface class; " + e.getMessage());
            }
        }

        return serviceInstance;
    }

    private Object createServiceInterfaceProxy(Class serviceInterface, Map seiPortNameToFactoryMap, Map seiClassNameToFactoryMap, ClassLoader classLoader) throws NamingException {

        Callback callback = new ServiceMethodInterceptor(seiPortNameToFactoryMap);
        Callback[] methodInterceptors = new Callback[]{SerializableNoOp.INSTANCE, callback};

        Enhancer enhancer = new Enhancer();
        enhancer.setClassLoader(classLoader);
        enhancer.setSuperclass(ServiceImpl.class);
        enhancer.setInterfaces(new Class[]{serviceInterface});
        enhancer.setCallbackFilter(new NoOverrideCallbackFilter(Service.class));
        enhancer.setCallbackTypes(new Class[]{NoOp.class, MethodInterceptor.class});
        enhancer.setUseFactory(false);
        enhancer.setUseCache(false);
        Class serviceClass = enhancer.createClass();

        Enhancer.registerCallbacks(serviceClass, methodInterceptors);
        FastConstructor constructor = FastClass.create(serviceClass).getConstructor(SERVICE_CONSTRUCTOR_TYPES);
        try {
            return constructor.newInstance(new Object[]{seiPortNameToFactoryMap, seiClassNameToFactoryMap});
        } catch (InvocationTargetException e) {
            throw (NamingException)new NamingException("Could not construct service instance").initCause(e.getTargetException());
        }
    }

}
