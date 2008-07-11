/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.geronimo.security.jaspi;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.security.auth.AuthPermission;
import javax.security.auth.message.AuthException;
import javax.security.auth.message.config.AuthConfigFactory;
import javax.security.auth.message.config.AuthConfigProvider;
import javax.security.auth.message.config.RegistrationListener;

/**
 * Implementation of the AuthConfigFactory. 
 */
public class AuthConfigFactoryImpl extends AuthConfigFactory {

    private static ClassLoader contextClassLoader;
    private Map<String, Context> registrations = new HashMap<String, Context>();
    
    static {
        contextClassLoader = java.security.AccessController
                        .doPrivileged(new java.security.PrivilegedAction<ClassLoader>() {
                            public ClassLoader run() {
                                return Thread.currentThread().getContextClassLoader();
                            }
                        });
    }
    
    public AuthConfigFactoryImpl() throws AuthException {
        loadConfig();
    }
    
    public synchronized String[] detachListener(RegistrationListener listener, String layer, String appContext) throws SecurityException {
        SecurityManager sm = System.getSecurityManager();
        if (sm != null) {
            sm.checkPermission(new AuthPermission("detachAuthListener"));
        }
        List<String> ids = new ArrayList<String>();
        for (Map.Entry<String, Context> entry : registrations.entrySet()) {
            Context ctx = entry.getValue();
            if ((layer == null || layer.equals(ctx.getMessageLayer())) &&
                    (appContext == null || appContext.equals(ctx.getAppContext()))) {
                if (ctx.getListeners().remove(listener)) {
                    ids.add(entry.getKey());
                }
            }
        }
        return ids.toArray(new String[ids.size()]);
    }

    public synchronized AuthConfigProvider getConfigProvider(String layer, String appContext, RegistrationListener listener) {
        Context ctx = registrations.get(getRegistrationKey(layer, appContext));
        if (ctx == null) {
            ctx = registrations.get(getRegistrationKey(null, appContext));
        }
        if (ctx == null) {
            ctx = registrations.get(getRegistrationKey(layer, null));
        }
        if (ctx == null) {
            ctx = registrations.get(getRegistrationKey(null, null));
        }
        if (ctx != null) {
            if (listener != null) {
                ctx.getListeners().add(listener);
            }
            return ctx.getProvider();
        }
        return null;
    }

    public synchronized RegistrationContext getRegistrationContext(String registrationID) {
        return registrations.get(registrationID);
    }

    public synchronized String[] getRegistrationIDs(AuthConfigProvider provider) {
        List<String> ids = new ArrayList<String>();
        for (Map.Entry<String, Context> entry : registrations.entrySet()) {
            Context ctx = entry.getValue();
            if (provider == null ||
                    provider.getClass().getName().equals(ctx.getProvider().getClass().getName())) {
                ids.add(entry.getKey());
            }
        }
        return ids.toArray(new String[ids.size()]);
    }

    public synchronized void refresh() throws AuthException, SecurityException {
        SecurityManager sm = System.getSecurityManager();
        if (sm != null) {
            sm.checkPermission(new AuthPermission("refreshAuth"));
        }
        loadConfig();
    }

    public String registerConfigProvider(AuthConfigProvider authConfigProvider, String layer, String appContext, String description) throws AuthException, SecurityException {
        SecurityManager sm = System.getSecurityManager();
        if (sm != null) {
            sm.checkPermission(new AuthPermission("registerAuthConfigProvider"));
        }
        return registerConfigProvider(authConfigProvider, layer, appContext, description, false, null);
    }

    public synchronized String registerConfigProvider(final String className, final Map constructorParam, String layer, String appContext, String description) throws AuthException, SecurityException {
        SecurityManager sm = System.getSecurityManager();
        if (sm != null) {
            sm.checkPermission(new AuthPermission("registerAuthConfigProvider"));
        }
        AuthConfigProvider authConfigProvider;
        try {
            authConfigProvider = java.security.AccessController
            .doPrivileged(new PrivilegedExceptionAction<AuthConfigProvider>() {
                public AuthConfigProvider run() throws ClassNotFoundException, SecurityException, NoSuchMethodException, IllegalArgumentException, InstantiationException, IllegalAccessException, InvocationTargetException  {
                    Class<? extends AuthConfigProvider> cl = (Class<? extends AuthConfigProvider>) Class.forName(className, true, contextClassLoader);
                    Constructor<? extends AuthConfigProvider> cnst = cl.getConstructor(Map.class);
                    return cnst.newInstance(constructorParam);
                }
            });
        } catch (PrivilegedActionException e) {
            Exception inner = e.getException();
            if (inner instanceof InstantiationException) {
                throw (AuthException) new AuthException("AuthConfigFactory error:"
                                + inner.getCause().getMessage()).initCause(inner.getCause());
            } else {
                throw (AuthException) new AuthException("AuthConfigFactory error: " + inner).initCause(inner);
            }
        } catch (Exception e) {
            throw (AuthException) new AuthException("AuthConfigFactory error: " + e).initCause(e);
        }
        String key = registerConfigProvider(authConfigProvider, layer, appContext, description, true, constructorParam);
        saveConfig();
        return key;
    }

    private String registerConfigProvider(AuthConfigProvider provider, String layer, String appContext, String description, boolean persistent, Map<String, String> constructorParam) throws AuthException {
        String key = getRegistrationKey(layer, appContext);
        // Get or create context
        Context ctx = registrations.get(key);
        if (ctx == null) {
            ctx = new Context(layer, appContext, persistent);
        } else {
            if (persistent != ctx.isPersistent()) {
                throw new IllegalArgumentException("Cannot change the persistence state");
            }
        }
        // Create provider
        ctx.setProvider(provider);
        ctx.setDescription(description);
        registrations.put(key, ctx);
        // Notify listeners
        List<RegistrationListener> listeners = ctx.getListeners();
        for (RegistrationListener listener : listeners) {
            listener.notify(ctx.getMessageLayer(), ctx.getAppContext());
        }
        // Return registration Id
        return key;
    }

    public synchronized boolean removeRegistration(String registrationID) throws SecurityException {
        SecurityManager sm = System.getSecurityManager();
        if (sm != null) {
            sm.checkPermission(new AuthPermission("removeAuthRegistration"));
        }
        Context ctx = registrations.remove(registrationID);
        if (ctx != null) {
            List<RegistrationListener> listeners = ctx.getListeners();
            for (RegistrationListener listener : listeners) {
                listener.notify(ctx.getMessageLayer(), ctx.getAppContext());
            }
            return true;
        }
        return false;
    }
    
    private String getRegistrationKey(String layer, String appContext) {
        return layer + "/" + appContext;
    }
    
    private void loadConfig() throws AuthException {
        // TODO: load configuration file
    }
    
    private void saveConfig() throws AuthException {
        // TODO: save configuration file
    }
    
    private static class Context implements RegistrationContext {

        private final String layer;
        private final String appContext;
        private final List<RegistrationListener> listeners = new ArrayList<RegistrationListener>();
        private String description;
        private AuthConfigProvider provider;
        private final boolean persistent;
        private Map<String, String> constructorParam;
        
        public Context(String layer, String appContext, boolean persistent) {
            this.layer = layer;
            this.appContext = appContext;
            this.persistent = persistent;
        }
        
        public String getAppContext() {
            return appContext;
        }

        public String getMessageLayer() {
            return layer;
        }

        public boolean isPersistent() {
            return persistent;
        }

        public String getDescription() {
            return description;
        }

        public List<RegistrationListener> getListeners() {
            return listeners;
        }
        
        public AuthConfigProvider getProvider() {
            return provider;
        }

        public void setProvider(AuthConfigProvider provider) {
            this.provider = provider;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        public Map<String, String> getConstructorParam() {
            return constructorParam;
        }

        public void setConstructorParam(Map<String, String> constructorParam) {
            this.constructorParam = constructorParam;
        }
    }
    
}
