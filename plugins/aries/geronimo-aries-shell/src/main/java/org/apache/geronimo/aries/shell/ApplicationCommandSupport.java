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

package org.apache.geronimo.aries.shell;

import java.util.Set;

import org.apache.aries.application.management.AriesApplicationContext;
import org.apache.aries.application.management.AriesApplicationContextManager;
import org.apache.aries.application.management.AriesApplicationManager;
import org.apache.felix.karaf.shell.console.OsgiCommandSupport;
import org.osgi.framework.ServiceReference;

/**
 * @version $Rev$ $Date$
 */
public abstract class ApplicationCommandSupport extends OsgiCommandSupport {

    protected AriesApplicationContextManager getApplicationContextManager() {
        ServiceReference ref = 
            bundleContext.getServiceReference(AriesApplicationContextManager.class.getName());
        return getService(AriesApplicationContextManager.class, ref);
    }
    
    protected AriesApplicationManager getAriesApplicationManager() {
        ServiceReference ref = 
            bundleContext.getServiceReference(AriesApplicationManager.class.getName());
        return getService(AriesApplicationManager.class, ref);
    }
    
    protected AriesApplicationContext findApplicationContext(AriesApplicationContextManager manager, String appName) {
        Set<AriesApplicationContext> contexts = manager.getApplicationContexts();
        for (AriesApplicationContext context : contexts) {
            String name = context.getApplication().getApplicationMetadata().getApplicationScope();
            if (appName.equals(name)) {
                return context;
            }
        }
        return null;
    }
}
