/**
 *
 * Copyright 2004 The Apache Software Foundation
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.apache.geronimo.security.jaas;

import javax.security.auth.Subject;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.login.LoginException;
import javax.security.auth.spi.LoginModule;

import java.net.URI;
import java.net.URISyntaxException;
import java.security.AccessController;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;
import java.util.HashMap;
import java.util.Map;

import org.apache.geronimo.security.GeronimoSecurityException;
import org.apache.geronimo.security.remoting.jmx.RemoteLoginServiceFactory;


/**
 * @version $Rev$ $Date$
 */
public class RemoteLoginModule implements javax.security.auth.spi.LoginModule {

    public final static String MODULE_IS_LOCAL = "org.apache.geronimo.security.jaas.RemoteLoginModule.MODULE_IS_LOCAL";
    public final static String LOGIN_URI = "org.apache.geronimo.security.jaas.RemoteLoginModule.LOGIN_URI";
    public final static String LOGIN_SERVICE = "org.apache.geronimo.security.jaas.RemoteLoginModule.LOGIN_SERVICE";
    private boolean debug;
    private URI connectURI;
    private LoginServiceMBean remoteLoginService;
    private LoginModuleConfiguration[] modules;
    private static ClassLoader classLoader;

    static {
        classLoader = (ClassLoader) java.security.AccessController.doPrivileged(new java.security.PrivilegedAction() {
            public Object run() {
                return Thread.currentThread().getContextClassLoader();
            }
        });
    }

    public void initialize(Subject subject, CallbackHandler callbackHandler, Map sharedState, Map options) {
        debug = "true".equalsIgnoreCase((String) options.get("debug"));
        String uri = (String) options.get("uri");
        String realm = (String) options.get("realm");

        if (uri == null) throw new GeronimoSecurityException("Initialize error: uri to security service is not set");
        if (realm == null) throw new GeronimoSecurityException("Initialize error: realm name not specified");

        try {
            connectURI = new URI(uri);
            remoteLoginService = RemoteLoginServiceFactory.create(connectURI.getHost(), connectURI.getPort());

            SerializableACE[] entries = remoteLoginService.getAppConfigurationEntries(realm);
            modules = new LoginModuleConfiguration[entries.length];
            for(int i = 0; i < entries.length; i++) {
                SerializableACE entry = entries[i];

                final String finalClass = entry.getLoginModuleName();
                LoginModule wrapper;
                wrapper = (LoginModule) AccessController.doPrivileged(new PrivilegedExceptionAction() {
                    public Object run() throws ClassNotFoundException, InstantiationException, IllegalAccessException {
                        return Class.forName(finalClass, true, classLoader).newInstance();
                    }
                });

                HashMap map = new HashMap(entry.getOptions());
                map.put(LOGIN_SERVICE, remoteLoginService);

                wrapper.initialize(subject, callbackHandler, sharedState, map);
                modules[i] = new LoginModuleConfiguration(wrapper, entry.getControlFlag());
            }

            if (debug) {
                System.out.print("[GeronimoLoginModule] Debug is  " + debug + " uri " + uri + " realm " + realm + "\n");
            }
        } catch (PrivilegedActionException pae) {
            Exception e = pae.getException();
            if (e instanceof InstantiationException) {
                throw (GeronimoSecurityException) new GeronimoSecurityException("Initialize error:" + e.getCause().getMessage()).initCause(e.getCause());
            } else {
                throw (GeronimoSecurityException) new GeronimoSecurityException("Initialize error: " + e.toString()).initCause(e);
            }
        } catch (URISyntaxException e) {
            throw (GeronimoSecurityException) new GeronimoSecurityException("Initialize error: " + e.toString()).initCause(e);
        }
    }

    public boolean login() throws LoginException {
        if (modules == null || modules.length == 0) throw new LoginException("RemoteLoginModule not properly initialzied");
        return LoginUtils.computeLogin(modules);
    }

    public boolean commit() throws LoginException {
        if (modules == null || modules.length == 0) throw new LoginException("RemoteLoginModule not properly initialzied");
        for(int i = 0; i < modules.length; i++) {
            LoginModuleConfiguration configuration = modules[i];
            configuration.getModule().commit();
        }
        return true;
    }

    public boolean abort() throws LoginException {
        if (modules == null || modules.length == 0) throw new LoginException("RemoteLoginModule not properly initialzied");
        for(int i = 0; i < modules.length; i++) {
            LoginModuleConfiguration configuration = modules[i];
            configuration.getModule().abort();
        }
        return true;
    }

    public boolean logout() throws LoginException {
        if (modules == null || modules.length == 0) throw new LoginException("RemoteLoginModule not properly initialzied");
        for(int i = 0; i < modules.length; i++) {
            LoginModuleConfiguration configuration = modules[i];
            configuration.getModule().logout();
        }
        return true;
    }
}
