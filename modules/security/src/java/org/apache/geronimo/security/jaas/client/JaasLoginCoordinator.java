/**
 *
 * Copyright 2003-2005 The Apache Software Foundation
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
package org.apache.geronimo.security.jaas.client;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import javax.security.auth.Subject;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.login.LoginException;
import javax.security.auth.spi.LoginModule;

import org.apache.geronimo.kernel.Kernel;
import org.apache.geronimo.kernel.KernelRegistry;
import org.apache.geronimo.security.jaas.server.JaasSessionId;
import org.apache.geronimo.security.jaas.server.JaasLoginModuleConfiguration;
import org.apache.geronimo.security.jaas.LoginModuleControlFlag;
import org.apache.geronimo.security.jaas.LoginUtils;
import org.apache.geronimo.security.jaas.server.JaasLoginServiceMBean;
import org.apache.geronimo.security.remoting.jmx.JaasLoginServiceRemotingClient;


/**
 * A LoginModule implementation which connects to a Geronimo server under
 * the covers, and uses Geronimo realms to resolve the login.  It handles a
 * mix of client-side and server-side login modules.  It treats any client
 * side module as something it should manage and execute, while a server side
 * login module would be managed and executed by the Geronimo server.
 * <p/>
 * Note that this can actually be run from within a Geronimo server, in which
 * case the client/server distinction is somewhat less important, and the
 * communication is optimized by avoiding network traffic.
 *
 * @version $Rev$ $Date$
 */
public class JaasLoginCoordinator implements LoginModule {
    public final static String OPTION_HOST = "host";
    public final static String OPTION_PORT = "port";
    public final static String OPTION_KERNEL = "kernel";
    public final static String OPTION_REALM = "realm";
    public final static String OPTION_SERVICENAME = "serviceName";
    private String serverHost;
    private int serverPort;
    private String realmName;
    private String kernelName;
    private ObjectName serviceName;
    private JaasLoginServiceMBean service;
    private CallbackHandler handler;
    private Subject subject;
    private JaasSessionId sessionHandle;
    private LoginModuleProxy[] proxies;
    private final Map sharedState = new HashMap();


    public void initialize(Subject subject, CallbackHandler callbackHandler, Map sharedState, Map options) {
        serverHost = (String) options.get(OPTION_HOST);
        Object port = options.get(OPTION_PORT);
        if (port != null) {
            serverPort = Integer.parseInt((String) port);
        }
        realmName = (String) options.get(OPTION_REALM);
        kernelName = (String) options.get(OPTION_KERNEL);
        try {
            String s = (String) options.get(OPTION_SERVICENAME);
            serviceName = s != null ? new ObjectName(s) : null;
        } catch (MalformedObjectNameException e) {
            throw new IllegalArgumentException("option " + OPTION_SERVICENAME + "is not a valid ObjectName: " + options.get(OPTION_SERVICENAME));
        }
        service = connect();
        handler = callbackHandler;
        if (subject == null) {
            this.subject = new Subject();
        } else {
            this.subject = subject;
        }
    }

    public boolean login() throws LoginException {
        sessionHandle = service.connectToRealm(realmName);
        JaasLoginModuleConfiguration[] config = service.getLoginConfiguration(sessionHandle);
        proxies = new LoginModuleProxy[config.length];

        for (int i = 0; i < proxies.length; i++) {
            if (config[i].isServerSide()) {
                proxies[i] = new ServerLoginProxy(config[i].getFlag(), subject, i, service, sessionHandle);
            } else {
                LoginModule source = config[i].getLoginModule(JaasLoginCoordinator.class.getClassLoader());
                if (config[i].isWrapPrincipals()) {
                    proxies[i] = new WrappingClientLoginModuleProxy(config[i].getFlag(), subject, source, config[i].getLoginDomainName(), realmName);
                } else {
                    proxies[i] = new ClientLoginModuleProxy(config[i].getFlag(), subject, source);
                }
            }
            proxies[i].initialize(subject, handler, sharedState, config[i].getOptions());
            syncSharedState();
        }
        return performLogin();
    }

    public boolean commit() throws LoginException {
        for (int i = 0; i < proxies.length; i++) {
            proxies[i].commit();
            syncSharedState();
            syncPrincipals();
        }
        subject.getPrincipals().add(service.loginSucceeded(sessionHandle));
        return true;
    }

    public boolean abort() throws LoginException {
        try {
            for (int i = 0; i < proxies.length; i++) {
                proxies[i].abort();
                syncSharedState();
            }
        } finally {
            service.loginFailed(sessionHandle);
        }
        clear();
        return true;
    }

    public boolean logout() throws LoginException {
        try {
            for (int i = 0; i < proxies.length; i++) {
                proxies[i].logout();
                syncSharedState();
            }
        } finally {
            service.logout(sessionHandle);
        }
        clear();
        return true;
    }

    private void clear() {
        Kernel kernel = KernelRegistry.getKernel(kernelName);
        if (kernel != null) {
            kernel.getProxyManager().destroyProxy(service);
        }
        serverHost = null;
        serverPort = 0;
        realmName = null;
        kernelName = null;
        service = null;
        handler = null;
        subject = null;
        sessionHandle = null;
        proxies = null;
    }

    private JaasLoginServiceMBean connect() {
        if (serverHost != null && serverPort > 0) {
            return JaasLoginServiceRemotingClient.create(serverHost, serverPort);
        } else {
            Kernel kernel = KernelRegistry.getKernel(kernelName);
            return (JaasLoginServiceMBean) kernel.getProxyManager().createProxy(serviceName, JaasLoginServiceMBean.class);
        }
    }

    /**
     * See http://java.sun.com/j2se/1.4.2/docs/api/javax/security/auth/login/Configuration.html
     *
     * @return
     * @throws LoginException
     */
    private boolean performLogin() throws LoginException {
        Boolean success = null;
        Boolean backup = null;

        for (int i = 0; i < proxies.length; i++) {
            LoginModuleProxy proxy = proxies[i];
            boolean result = proxy.login();
            syncSharedState();

            if (proxy.getControlFlag() == LoginModuleControlFlag.REQUIRED) {
                if (success == null || success.booleanValue()) {
                    success = result ? Boolean.TRUE : Boolean.FALSE;
                }
            } else if (proxy.getControlFlag() == LoginModuleControlFlag.REQUISITE) {
                if (!result) {
                    return false;
                } else if (success == null) {
                    success = Boolean.TRUE;
                }
            } else if (proxy.getControlFlag() == LoginModuleControlFlag.SUFFICIENT) {
                if (result && (success == null || success.booleanValue())) {
                    return true;
                }
            } else if (proxy.getControlFlag() == LoginModuleControlFlag.OPTIONAL) {
                if (backup == null || backup.booleanValue()) {
                    backup = result ? Boolean.TRUE : Boolean.FALSE;
                }
            }
        }
        // all required and requisite modules succeeded, or at least one required module failed
        if (success != null) {
            return success.booleanValue();
        }
        // no required or requisite modules, no sufficient modules succeeded, fall back to optional modules
        if (backup != null) {
            return backup.booleanValue();
        }
        // perhaps only a sufficient module, and it failed
        return false;
    }

    private void syncSharedState() throws LoginException {
        Map map = service.syncShareState(sessionHandle, LoginUtils.getSerializableCopy(sharedState));
        sharedState.putAll(map);
    }

    private void syncPrincipals() throws LoginException {
        Set principals = service.syncPrincipals(sessionHandle, subject.getPrincipals());
        subject.getPrincipals().addAll(principals);
    }
}
