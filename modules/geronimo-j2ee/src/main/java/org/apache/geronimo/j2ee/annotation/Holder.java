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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.naming.Context;
import javax.naming.NamingException;

import org.apache.xbean.recipe.ObjectRecipe;
import org.apache.xbean.recipe.Option;
import org.apache.xbean.recipe.StaticRecipe;

/**
 * @version $Rev$ $Date$
 */
public class Holder implements Serializable {

    public static final Holder EMPTY = new Holder() {
    };

    private Map<String, List<Injection>> injectionMap;
    private Map<String, LifecycleMethod> postConstruct;
    private Map<String, LifecycleMethod> preDestroy;


    public Holder() {
    }


    public void addInjection(String className, Injection injection) {
        if (injectionMap == null) {
            injectionMap = new HashMap<String, List<Injection>>();
        }
        List<Injection> injections = injectionMap.get(className);
        if (injections == null) {
            injections = new ArrayList<Injection>();
            injectionMap.put(className, injections);
        }
        injections.add(injection);
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

    public List<Injection> getInjections(String className) {
        if (injectionMap == null) {
            return null;
        }
        return injectionMap.get(className);
    }

    public Map<String, LifecycleMethod> getPostConstruct() {
        return postConstruct;
    }

    public Map<String, LifecycleMethod> getPreDestroy() {
        return preDestroy;
    }

    public boolean isEmpty() {
        return (injectionMap == null || injectionMap.isEmpty())
                && (postConstruct == null || postConstruct.isEmpty())
                && (preDestroy == null || preDestroy.isEmpty());
    }

    public Object newInstance(String className, ClassLoader classLoader, Context context) throws IllegalAccessException, InstantiationException {
        ObjectRecipe objectRecipe = new ObjectRecipe(className);
        objectRecipe.allow(Option.FIELD_INJECTION);
        objectRecipe.allow(Option.PRIVATE_PROPERTIES);
        objectRecipe.allow(Option.IGNORE_MISSING_PROPERTIES);
        List<Injection> callbackHandlerinjections = getInjections(className);
        if (callbackHandlerinjections != null) {
            for (Injection injection : callbackHandlerinjections) {
                try {
                    String jndiName = injection.getJndiName();
                    //our componentContext is attached to jndi at "java:comp" so we remove that when looking stuff up in it
                    Object object = context.lookup("env/" + jndiName);
                    if (object instanceof String) {
                        String string = (String) object;
                        // Pass it in raw so it could be potentially converted to
                        // another data type by an xbean-reflect property editor
                        objectRecipe.setProperty(injection.getTargetName(), string);
                    } else {
                        objectRecipe.setProperty(injection.getTargetName(), new StaticRecipe(object));
                    }
                } catch (NamingException e) {
//                        log.warn("could not look up ", e);
                }
            }
        }
        Object result = objectRecipe.create(classLoader);
        Map unsetProperties = objectRecipe.getUnsetProperties();
        if (unsetProperties.size() > 0) {
            for (Object property : unsetProperties.keySet()) {
//                log.warning("Injection: No such property '"+property+"' in class "+_class.getName());
            }
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

    public void destroyInstance(Object o) throws Exception {
        Class clazz = o.getClass();
        Map<String, LifecycleMethod> preDestroy = getPreDestroy();
        if (preDestroy != null) {
            apply(o, clazz, preDestroy);
        }
    }

    public static void apply(Object o, Class clazz, Map<String, LifecycleMethod> map) throws IllegalAccessException, InvocationTargetException {
        if (clazz == null) {
            clazz = o.getClass();
        }
        ArrayList<Class> classes = new ArrayList<Class>();
        while (clazz != null && clazz != Object.class) {
            classes.add(clazz);
            clazz = clazz.getSuperclass();
        }
        for (int i = classes.size() - 1; i > -1; i--) {
            Class clazz1 = classes.get(i);
            LifecycleMethod m = map.get(clazz1.getName());
            if (m != null) {
                m.call(o, clazz1);
            }
        }
    }

}
