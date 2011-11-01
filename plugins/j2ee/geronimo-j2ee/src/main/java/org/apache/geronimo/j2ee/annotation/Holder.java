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

package org.apache.geronimo.j2ee.annotation;

import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;

import javax.naming.Context;
import javax.naming.NamingException;

import org.apache.xbean.recipe.ConstructionException;
import org.apache.xbean.recipe.ObjectRecipe;
import org.apache.xbean.recipe.Option;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @version $Rev$ $Date$
 */
public class Holder implements Serializable {

    private static final Logger log = LoggerFactory.getLogger(Holder.class);

    public static final Holder EMPTY = new Holder() {
    };

    private Map<String, Set<Injection>> injectionMap;

    private Map<String, LifecycleMethod> postConstruct;

    private Map<String, LifecycleMethod> preDestroy;

    private List<Interceptor> interceptors = new CopyOnWriteArrayList<Interceptor>();

    public Holder() {
    }

    public Holder(Holder source) {
        if (source.getInjectionMap() != null) {
            this.injectionMap = new HashMap<String, Set<Injection>>();
            addInjectionMap(source.getInjectionMap());
        }

        if (source.getPostConstruct() != null) {
            this.postConstruct = new HashMap<String, LifecycleMethod>();
            addPostConstructs(source.getPostConstruct());
        }

        if (source.getPreDestroy() != null) {
            this.preDestroy = new HashMap<String, LifecycleMethod>();
            addPreDestroys(source.getPreDestroy());
        }
    }

    private Set<Injection> getInjectionList(String className) {
        if (injectionMap == null) {
            injectionMap = new HashMap<String, Set<Injection>>();
        }
        Set<Injection> injections = injectionMap.get(className);
        if (injections == null) {
            injections = new HashSet<Injection>();
            injectionMap.put(className, injections);
        }
        return injections;
    }

    public void addInjection(String className, Injection newInjection) {
        Set<Injection> injections = getInjectionList(className);
        injections.add(newInjection);
    }

    public void addInjections(String className, Collection<Injection> newInjections) {
        Set<Injection> injections = getInjectionList(className);
        for (Injection injection : newInjections) {
            injections.add(injection);
        }
    }

    public void addPostConstructs(Map<String, LifecycleMethod> newPostConstructs) {
        this.postConstruct = merge(postConstruct, newPostConstructs);
    }

    public void addPreDestroys(Map<String, LifecycleMethod> newPreDestroys) {
        this.preDestroy = merge(preDestroy, newPreDestroys);
    }

    private Map<String, LifecycleMethod> merge(Map<String, LifecycleMethod> old, Map<String, LifecycleMethod> additional) {
        if (old == null) {
            return additional;
        }
        if (additional == null) {
            return old;
        }
        old.putAll(additional);
        return old;
    }

    public void addInjectionMap(Map<String, Set<Injection>> injectionMap) {
        if (injectionMap == null) {
            return;
        }
        for (Map.Entry<String, Set<Injection>> entry : injectionMap.entrySet()) {
            String className = entry.getKey();
            Set<Injection> injections = entry.getValue();
            addInjections(className, injections);
        }
    }

    public List<Injection> getInjections(String className) {
        if (injectionMap != null) {
            Set<Injection> injections = injectionMap.get(className);
            if (injections != null) {
                return new ArrayList<Injection>(injections);
            }
        }
        return null;
    }

    public Map<String, Set<Injection>> getInjectionMap() {
        return injectionMap;
    }

    public Map<String, LifecycleMethod> getPostConstruct() {
        return postConstruct;
    }

    public Map<String, LifecycleMethod> getPreDestroy() {
        return preDestroy;
    }

    public boolean isEmpty() {
        return (injectionMap == null || injectionMap.isEmpty()) && (postConstruct == null || postConstruct.isEmpty()) && (preDestroy == null || preDestroy.isEmpty());
    }

    public Object newInstance(String className, ClassLoader classLoader, Context context) throws IllegalAccessException, InstantiationException {
        ObjectRecipe objectRecipe = new ObjectRecipe(className);
        objectRecipe.allow(Option.FIELD_INJECTION);
        objectRecipe.allow(Option.PRIVATE_PROPERTIES);
        Class<?> clazz;
        try {
            clazz = classLoader.loadClass(className);
        } catch (ClassNotFoundException e) {
            throw (InstantiationException) new InstantiationException("Can't load class " + className + " in classloader: " + classLoader).initCause(e);
        }
        List<NamingException> problems = new ArrayList<NamingException>();
        while (clazz != Object.class) {
            addInjections(clazz.getName(), context, objectRecipe, problems);
            clazz = clazz.getSuperclass();
        }
        if (!problems.isEmpty()) {
            throw new InstantiationException("Some objects to be injected were not found in jndi: " + problems);
        }
        Object result;
        try {
            result = objectRecipe.create(classLoader);
        } catch (ConstructionException e) {
            throw (InstantiationException) new InstantiationException("Could not construct object").initCause(e);
        }
        InvocationContext invocationContext = new InvocationContext(context, result);
        try {
            for (Interceptor interceptor : interceptors) {
                interceptor.instancerCreated(invocationContext);
            }
        } catch (InterceptorException e) {
            throw (InstantiationException) new InstantiationException("Interceptor invocation failed").initCause(e);
        }

        if (getPostConstruct() != null) {
            try {
                apply(result, null, postConstruct);
            } catch (InvocationTargetException e) {
                Throwable cause = e.getCause();
                throw (InstantiationException) new InstantiationException("Could not call postConstruct method").initCause(cause);
            }
        }
        return result;
    }

    private void addInjections(String className, Context context, ObjectRecipe objectRecipe, List<NamingException> problems) {
        List<Injection> callbackHandlerinjections = getInjections(className);
        if (callbackHandlerinjections != null) {
            for (Injection injection : callbackHandlerinjections) {
                try {
                    String jndiName = injection.getJndiName();
                    Object object = context.lookup(jndiName);
                    objectRecipe.setProperty(injection.getTargetName(), object);
                } catch (NamingException e) {
                    //Per EE 5.4.1.3, if no value is configured for environment entry, it will be ignored
                    //In the past, we skip this in the EnvironmentEntryBuilder, while in Java EE 6, there are some sharable JNDI name spaces are added
                    //So, in some scenarios, it is impossible to know whether the environment entry is available in the deploying process.
                    if (injection.getType() != ReferenceType.ENV_ENTRY) {
                        problems.add(e);
                    }
                    log.info("Could not look up " + injection.getJndiName(), e);
                }
            }
        }
    }

    public void destroyInstance(Object o) throws Exception {
        Class<?> clazz = o.getClass();
        Map<String, LifecycleMethod> preDestroy = getPreDestroy();
        if (preDestroy != null) {
            apply(o, clazz, preDestroy);
        }
    }

    public static void apply(Object o, Class<?> clazz, Map<String, LifecycleMethod> map) throws IllegalAccessException, InvocationTargetException {
        if (clazz == null) {
            clazz = o.getClass();
        }
        ArrayList<Class<?>> classes = new ArrayList<Class<?>>();
        while (clazz != null && clazz != Object.class) {
            classes.add(clazz);
            clazz = clazz.getSuperclass();
        }
        for (int i = classes.size() - 1; i > -1; i--) {
            Class<?> clazz1 = classes.get(i);
            LifecycleMethod m = map.get(clazz1.getName());
            if (m != null) {
                m.call(o, clazz1);
            }
        }
    }

    public boolean addInterceptor(Interceptor interceptor) {
        return interceptors.add(interceptor);
    }

    public boolean removeInterceptor(Interceptor interceptor) {
        return interceptors.remove(interceptor);
    }

    public static interface Interceptor {

        public void instancerCreated(InvocationContext context) throws InterceptorException;

        public void instanceDestoryed(InvocationContext context) throws InterceptorException;

        public void postConstructInvoked(InvocationContext context) throws InterceptorException;
    }

    public static class InterceptorException extends Exception {

        public InterceptorException(Throwable cause) {
            super(cause);
        }

        public InterceptorException(String message) {
            super(message);
        }

        public InterceptorException(String message, Throwable cause) {
            super(message, cause);
        }

    }

    public static class InvocationContext {

        private final Context context;

        private final Object instance;

        public InvocationContext(Context context, Object instance) {
            this.context = context;
            this.instance = instance;
        }

        public Context getContext() {
            return context;

        }

        public Object getInstance() {
            return instance;
        }
    }
}
