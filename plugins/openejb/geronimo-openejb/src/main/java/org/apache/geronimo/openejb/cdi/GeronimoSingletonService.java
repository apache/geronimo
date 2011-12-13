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

import org.apache.webbeans.config.WebBeansContext;
import org.apache.webbeans.config.WebBeansFinder;
import org.apache.webbeans.spi.SingletonService;
import org.osgi.framework.Bundle;

/**
 * @version $Rev$ $Date$
 */
public class GeronimoSingletonService implements SingletonService<WebBeansContext> {

    private static final GeronimoSingletonService INSTANCE = new GeronimoSingletonService();

    private static final ThreadLocal<WebBeansContext> contexts = new ThreadLocal<WebBeansContext>();
    private static Bundle bundle;

    private static final WebBeansContext noContext = new WebBeansContext();

    private SingletonService<WebBeansContext> webApplicationSingletonService;

    public static void init(Bundle owbBundle) {
        bundle = owbBundle;
        WebBeansFinder.setSingletonService(INSTANCE);
    }

    private GeronimoSingletonService() {
    }

    public static GeronimoSingletonService getInstance() {
        return INSTANCE;
    }

    public static WebBeansContext contextEntered(WebBeansContext newContext) {
        final WebBeansContext oldContext = contexts.get();
        contexts.set(newContext);
        return oldContext;
    }

    public static void contextExited(WebBeansContext oldContext) {
        contexts.set(oldContext);
    }

    @Override
    public WebBeansContext get(Object key) {
        WebBeansContext context = contexts.get();
        if (context == null && webApplicationSingletonService != null) {
            context = webApplicationSingletonService.get(key);
            if (context != null) {
                return context;
            }
        }
        if (context == null) {
            contexts.set(noContext);
        }
        return context;
    }

    public SingletonService<WebBeansContext> getWebApplicationSingletonService() {
        return webApplicationSingletonService;
    }

    public void setWebApplicationSingletonService(SingletonService<WebBeansContext> webApplicationSingletonService) {
        this.webApplicationSingletonService = webApplicationSingletonService;
    }

    @Override
    public void clear(Object key) {
        get(key).clear();
    }
}
