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
import org.apache.openejb.cdi.StartupObject;
import org.apache.openejb.cdi.ThreadSingletonService;
import org.apache.webbeans.config.WebBeansContext;


/**
 * @version $Rev$ $Date$
 */
public class ThreadSingletonServiceAdapter implements ThreadSingletonService {

    private final GeronimoSingletonService geronimoSingletonService = GeronimoSingletonService.getInstance();

    public ThreadSingletonServiceAdapter() {
        super();
    }

    @Override
    public void initialize(StartupObject startupObject) {
        //share owb singletons
        WebBeansContext webBeansContext = startupObject.getAppContext().get(WebBeansContext.class);
        if (webBeansContext != null) {
            return;
        }
        Object old = contextEntered(null);
        try {
            if (old == null) {
                OpenWebBeansWebInitializer.newWebBeansContext(startupObject);
            } else {
                // an existing OWBConfiguration will have already been initialized
                startupObject.getAppContext().set(WebBeansContext.class, (WebBeansContext) old);
            }
        } finally {
            contextExited(old);
        }
    }

    @Override
    public Object contextEntered(WebBeansContext owbContext) {
        return GeronimoSingletonService.contextEntered(owbContext);
    }

    @Override
    public void contextExited(Object oldContext) {
        if (oldContext != null && !(oldContext instanceof WebBeansContext)) throw new IllegalArgumentException("Expecting a WebBeansContext not " + oldContext.getClass().getName());
        GeronimoSingletonService.contextExited((WebBeansContext) oldContext);
    }

    @Override
    public WebBeansContext get(Object key) {
        return geronimoSingletonService.get(key);
    }

    @Override
    public void clear(Object key) {
        geronimoSingletonService.clear(key);
    }

    public GeronimoSingletonService getGeronimoSingletonService() {
        return geronimoSingletonService;
    }
}
