/**
 *
 * Copyright 2003-2004 The Apache Software Foundation
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

import org.apache.geronimo.security.remoting.jmx.JaasLoginServiceRemotingClient;
import org.apache.geronimo.kernel.Kernel;
import org.apache.geronimo.kernel.jmx.MBeanProxyFactory;

import javax.security.auth.spi.LoginModule;
import javax.security.auth.Subject;
import javax.security.auth.login.LoginException;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.Callback;
import java.util.*;
import java.security.Principal;

/**
 * A LoginModule implementation which connects to a Geronimo server under
 * the covers, and uses Geronimo realms to resolve the login.  It handles a
 * mix of client-side and server-side login modules.  It treats any client
 * side module as something it should manage and execute, while a server side
 * login module would be managed and executed by the Geronimo server.
 *
 * Note that this can actually be run from within a Geronimo server, in which
 * case the client/server distinction is somewhat less important, and the
 * communication is optimized by avoiding network traffic.
 *
 * @version $Rev: 46019 $ $Date: 2004-09-14 05:56:06 -0400 (Tue, 14 Sep 2004) $
 */
public class JaasLoginCoordinator implements LoginModule {
    public final static String OPTION_HOST = "host";
    public final static String OPTION_PORT = "port";
    public final static String OPTION_KERNEL = "kernel";
    public final static String OPTION_REALM = "realm";
    private String serverHost;
    private int serverPort;
    private String realmName;
    private String kernelName;
    private JaasLoginServiceMBean service;
    private CallbackHandler handler;
    private Subject subject;
    private Set processedPrincipals = new HashSet();
    private JaasLoginModuleConfiguration[] config;
    private JaasClientId client;
    LoginModuleConfiguration[] workers;

    public void initialize(Subject subject, CallbackHandler callbackHandler,
                           Map sharedState, Map options) {
        serverHost = (String) options.get(OPTION_HOST);
        Object port = options.get(OPTION_PORT);
        if(port != null) {
            serverPort = Integer.parseInt((String)port);
        }
        realmName = (String) options.get(OPTION_REALM);
        kernelName = (String) options.get(OPTION_KERNEL);
        service = connect();
        handler = callbackHandler;
        if(subject == null) {
            this.subject = new Subject();
        } else {
            this.subject = subject;
        }
        //todo: shared state
    }

    public boolean login() throws LoginException {
        client = service.connectToRealm(realmName);
        config = service.getLoginConfiguration(client);
        workers = new LoginModuleConfiguration[config.length];
        for (int i = 0; i < workers.length; i++) {
            LoginModule wrapper;
            if(config[i].isServerSide()) {
                wrapper = new ServerLoginModule(i);
            } else {
                LoginModule source = config[i].getLoginModule(JaasLoginCoordinator.class.getClassLoader());
                wrapper = new ClientLoginModule(source, i);
            }
            workers[i] = new LoginModuleConfiguration(wrapper, config[i].getFlag());
            workers[i].getModule().initialize(subject, handler, new HashMap(), config[i].getOptions());
        }
        return LoginUtils.computeLogin(workers);
    }

    public boolean commit() throws LoginException {
        for (int i = 0; i < workers.length; i++) {
            workers[i].getModule().commit();
        }
        Principal[] principals = service.loginSucceeded(client);
        for (int i = 0; i < principals.length; i++) {
            Principal principal = principals[i];
            subject.getPrincipals().add(principal);
        }
        return true;
    }

    public boolean abort() throws LoginException {
        try {
            for (int i = 0; i < workers.length; i++) {
                workers[i].getModule().abort();
            }
        } finally {
            service.loginFailed(client);
        }
        clear();
        return true;
    }

    public boolean logout() throws LoginException {
        try {
            for (int i = 0; i < workers.length; i++) {
                workers[i].getModule().logout();
            }
        } finally {
            service.logout(client);
        }
        clear();
        return true;
    }

    private void clear() {
        serverHost = null;
        serverPort = 0;
        realmName = null;
        kernelName = null;
        service = null;
        handler = null;
        subject = null;
        processedPrincipals.clear();
        config = null;
        client = null;
        workers = null;
    }

    private JaasLoginServiceMBean connect() {
        if(serverHost != null && serverPort > 0) {
            return JaasLoginServiceRemotingClient.create(serverHost, serverPort);
        } else {
            return (JaasLoginServiceMBean) MBeanProxyFactory.getProxy(JaasLoginServiceMBean.class, Kernel.getKernel(kernelName).getMBeanServer(), JaasLoginService.OBJECT_NAME);
        }
    }

    private class ClientLoginModule implements LoginModule {
        private LoginModule source;
        int index;

        public ClientLoginModule(LoginModule source, int index) {
            this.source = source;
            this.index = index;
        }

        public void initialize(Subject subject, CallbackHandler callbackHandler,
                               Map sharedState, Map options) {
            source.initialize(subject, callbackHandler, sharedState, options);
        }

        public boolean login() throws LoginException {
           return source.login();
        }

        public boolean commit() throws LoginException {
            boolean result = source.commit();
            List list = new ArrayList();
            for (Iterator it = subject.getPrincipals().iterator(); it.hasNext();) {
                Principal p = (Principal) it.next();
                if(!processedPrincipals.contains(p)) {
                    list.add(p);
                    processedPrincipals.add(p);
                }
            }
            service.clientLoginModuleCommit(client, index, (Principal[]) list.toArray(new Principal[list.size()]));
            return result;
        }

        public boolean abort() throws LoginException {
            return source.abort();
        }

        public boolean logout() throws LoginException {
            return source.logout();
        }
    }

    private class ServerLoginModule implements LoginModule {
        int index;
        CallbackHandler handler;
        Callback[] callbacks;

        public ServerLoginModule(int index) {
            this.index = index;
        }

        public void initialize(Subject subject, CallbackHandler handler,
                               Map sharedState, Map options) {
            this.handler = handler;
        }

        public boolean login() throws LoginException {
            try {
                callbacks = service.getServerLoginCallbacks(client, index);
                if(handler != null) {
                    handler.handle(callbacks);
                } else if(callbacks != null && callbacks.length > 0) {
                    System.err.println("No callback handler available for "+callbacks.length+" callbacks!");
                }
                return service.performServerLogin(client, index, callbacks);
            } catch (LoginException e) {
                throw e;
            } catch (Exception e) {
                e.printStackTrace();
                throw new LoginException("Unable to log in: "+e.getMessage());
            }
        }

        public boolean commit() throws LoginException {
            return service.serverLoginModuleCommit(client, index);
        }

        public boolean abort() throws LoginException {
            return false; // taken care of with a single call to the server
        }

        public boolean logout() throws LoginException {
            return false; // taken care of with a single call to the server
        }
    }
}
