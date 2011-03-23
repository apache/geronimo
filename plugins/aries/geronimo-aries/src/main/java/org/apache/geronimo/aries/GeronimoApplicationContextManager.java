/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIESOR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.apache.geronimo.aries;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.apache.aries.application.ApplicationMetadata;
import org.apache.aries.application.management.AriesApplication;
import org.apache.aries.application.management.AriesApplicationContext;
import org.apache.aries.application.management.AriesApplicationContextManager;
import org.apache.aries.application.management.ManagementException;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;
import org.osgi.framework.ServiceReference;

public class GeronimoApplicationContextManager implements AriesApplicationContextManager {

    private ConcurrentMap<String, GeronimoApplicationContext> contextMap;
    private BundleContext bundleContext;
    private ServiceReference installerReference;
    private ApplicationInstaller installer;

    public GeronimoApplicationContextManager() {
        this.contextMap = new ConcurrentHashMap<String, GeronimoApplicationContext>();
    }

    public void setBundleContext(BundleContext bundleContext) {
        this.bundleContext = bundleContext;
    }
        
    protected void registerApplicationContext(GeronimoApplicationContext context) { 
        contextMap.put(getApplicationKey(context.getApplication()), context);        
    }
    
    private String getApplicationKey(AriesApplication app) {
        ApplicationMetadata metadata = app.getApplicationMetadata();
        return metadata.getApplicationScope();
    }

    public AriesApplicationContext getApplicationContext(AriesApplication app) throws BundleException, ManagementException {
        String key = getApplicationKey(app);
        GeronimoApplicationContext applicationContext = contextMap.get(key);
        if (applicationContext == null) {
            try {
                getApplicationInstaller().install(app);
            } catch (Exception e) {
                throw new BundleException("Application installation failed", e);
            }
            // if application was successfully installed & started it
            // should have registered its ApplicationContext by now
            applicationContext = contextMap.get(key);
            if (applicationContext == null) {
                throw new ManagementException("No ApplicationContext");
            }
        }

        return applicationContext;
    }

    public Set<AriesApplicationContext> getApplicationContexts() {
        Set<AriesApplicationContext> result = new HashSet<AriesApplicationContext>();
        result.addAll(contextMap.values());
        return result;
    }

    public void remove(AriesApplicationContext app) {     
        String key = getApplicationKey(app.getApplication());
        GeronimoApplicationContext applicationContext = contextMap.remove(key);
        if (applicationContext != null) {
            applicationContext.uninstall();
        }
    }

    private ApplicationInstaller getApplicationInstaller() {
        if (installer == null) {
            installerReference = 
                bundleContext.getServiceReference(ApplicationInstaller.class.getName());
            installer = (ApplicationInstaller) bundleContext.getService(installerReference);
        } 
        return installer;
    }
    
    public void init() {
    }
    
    public void destroy() {
        if (installerReference != null) {
            bundleContext.ungetService(installerReference);
        }
    }
}