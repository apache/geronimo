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


package org.apache.geronimo.openwebbeans;

import java.util.Map;

import org.apache.webbeans.config.WebBeansFinder;
import org.apache.webbeans.exception.WebBeansException;
import org.apache.webbeans.spi.SingletonService;
import org.osgi.framework.Bundle;

/**
 * @version $Rev$ $Date$
 */
public class GeronimoSingletonService implements SingletonService {

    private static final ThreadLocal<Map<String, Object>> contexts = new ThreadLocal<Map<String, Object>>();
    private static Bundle bundle;

    public static void init(Bundle owbBundle) {
        bundle = owbBundle;
        WebBeansFinder.setSingletonService(new GeronimoSingletonService());
    }

    public GeronimoSingletonService() {
    }

    public static Map<String, Object> contextEntered(Map<String, Object> newContext) {
        Map<String, Object> oldContext = contexts.get();
        contexts.set(newContext);
        return oldContext;
    }

    public static void contextExited(Map<String, Object> oldContext) {
        contexts.set(oldContext);
    }

    @Override
    public Object get(Object key, String singletonClassName) {
        Map<String, Object> context = getContext();
        Object service = context.get(singletonClassName);
        if (service == null) {
            try {
                Class clazz = bundle.loadClass(singletonClassName);
                service = clazz.newInstance();
            } catch (ClassNotFoundException e) {
                throw new WebBeansException("Could not locate requested class " + singletonClassName + " in bundle " + bundle, e);
            } catch (InstantiationException e) {
                throw new WebBeansException("Could not create instance of class " + singletonClassName, e);
            } catch (IllegalAccessException e) {
                throw new WebBeansException("Could not create instance of class " + singletonClassName, e);
            } catch (NoClassDefFoundError e) {
                throw new WebBeansException("Could not locate requested class " + singletonClassName + " in bundle " + bundle, e);
            }
            context.put(singletonClassName, service);
        }
        return service;
    }

    private Map<String, Object> getContext() {
        Map<String, Object> context = contexts.get();
        if (context == null) {
            throw new IllegalStateException("On a thread without an initialized context");
        }
        return context;
    }

    @Override
    public void clear(Object key) {
        getContext().clear();
    }

    @Override
    public boolean isExist(Object key, String singletonClassName) {
        return getContext().containsKey(singletonClassName);
    }

    @Override
    public Object getExist(Object key, String singletonClassName) {
        return getContext().get(singletonClassName);
    }

    @Override
    public Object getKey(Object singleton) {
        return null;
    }
}
