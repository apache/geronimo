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

package org.apache.geronimo.security.remoting.jmx;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.Collections;
import java.util.Properties;
import javax.management.ObjectName;
import javax.security.auth.Subject;
import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.NameCallback;
import javax.security.auth.callback.PasswordCallback;
import javax.security.auth.callback.UnsupportedCallbackException;
import javax.security.auth.login.LoginContext;

import junit.framework.TestCase;
import org.apache.geronimo.gbean.jmx.GBeanMBean;
import org.apache.geronimo.kernel.Kernel;
import org.apache.geronimo.security.IdentificationPrincipal;
import org.apache.geronimo.security.RealmPrincipal;
import org.apache.geronimo.security.jaas.JaasLoginServiceMBean;
import org.apache.geronimo.system.serverinfo.ServerInfo;


/**
 * @version $Rev$ $Date$
 */
public class RemoteLoginTest extends TestCase {
    Kernel kernel;
    ObjectName serverInfo;
    ObjectName loginService;
    protected ObjectName testCE;
    protected ObjectName testRealm;
    ObjectName subsystemRouter;
    ObjectName secureSubsystemRouter;
    ObjectName asyncTransport;
    ObjectName saslTransport;
    ObjectName gssapiTransport;
    ObjectName jmxRouter;
    ObjectName secureJmxRouter;
    ObjectName serverStub;
    JaasLoginServiceMBean asyncRemoteProxy;
    JaasLoginServiceMBean saslRemoteProxy;
    JaasLoginServiceMBean gssapiRemoteProxy;

    public void testNothing() {
    }

    public void XtestLogin() throws Exception {
        ClassLoader oldCl = Thread.currentThread().getContextClassLoader();
        try {
            Thread.currentThread().setContextClassLoader(getClass().getClassLoader());
            LoginContext context = new LoginContext("FOO", new UsernamePasswordCallback("alan", "starcraft"));

            context.login();
            Subject subject = context.getSubject();

            assertTrue("expected non-null subject", subject != null);
            assertTrue("subject should have one remote principal", subject.getPrincipals(IdentificationPrincipal.class).size() == 1);
            IdentificationPrincipal principal = (IdentificationPrincipal) subject.getPrincipals(IdentificationPrincipal.class).iterator().next();
            assertTrue("id of principal should be non-zero", principal.getId().getSubjectId().longValue() != 0);
            assertTrue("subject should have five principals", subject.getPrincipals().size() == 5);
            assertTrue("subject should have two realm principal", subject.getPrincipals(RealmPrincipal.class).size() == 2);

            context.logout();
        } finally {
            Thread.currentThread().setContextClassLoader(oldCl);
        }
    }

    public void setUp() throws Exception {
        kernel = new Kernel("test.kernel", "simple.geronimo.test");
        kernel.boot();

        GBeanMBean gbean;

        // Create all the parts

        gbean = new GBeanMBean(ServerInfo.GBEAN_INFO);
        serverInfo = new ObjectName("geronimo.system:role=ServerInfo");
        gbean.setAttribute("baseDirectory", ".");
        kernel.loadGBean(serverInfo, gbean);
        kernel.startGBean(serverInfo);

        gbean = new GBeanMBean("org.apache.geronimo.security.jaas.JaasLoginService");
        loginService = new ObjectName("geronimo.security:type=JaasLoginService");
        gbean.setReferencePatterns("Realms", Collections.singleton(new ObjectName("geronimo.security:type=SecurityRealm,*")));
//        gbean.setAttribute("reclaimPeriod", new Long(100));
        gbean.setAttribute("algorithm", "HmacSHA1");
        gbean.setAttribute("password", "secret");
        kernel.loadGBean(loginService, gbean);

        gbean = new GBeanMBean("org.apache.geronimo.security.jaas.LoginModuleGBean");
        testCE = new ObjectName("geronimo.security:type=LoginModule,name=properties");
        gbean.setAttribute("loginModuleClass", "org.apache.geronimo.security.realm.providers.PropertiesFileLoginModule");
        gbean.setAttribute("serverSide", new Boolean(true));
        Properties props = new Properties();
        props.put("usersURI", new File(new File("."), "src/test-data/data/users.properties").toString());
        props.put("groupsURI", new File(new File("."), "src/test-data/data/groups.properties").toString());
        gbean.setAttribute("options", props);
        kernel.loadGBean(testCE, gbean);

        gbean = new GBeanMBean("org.apache.geronimo.security.realm.GenericSecurityRealm");
        testRealm = new ObjectName("geronimo.security:type=SecurityRealm,realm=properties-realm");
        gbean.setAttribute("realmName", "properties-realm");
        props = new Properties();
        props.setProperty("LoginModule.1.REQUIRED","geronimo.security:type=LoginModule,name=properties");
        gbean.setAttribute("loginModuleConfiguration", props);
        gbean.setReferencePatterns("ServerInfo", Collections.singleton(serverInfo));
        kernel.loadGBean(testRealm, gbean);

        gbean = new GBeanMBean("org.apache.geronimo.remoting.router.SubsystemRouter");
        subsystemRouter = new ObjectName("geronimo.remoting:router=SubsystemRouter");
        kernel.loadGBean(subsystemRouter, gbean);

        gbean = new GBeanMBean("org.apache.geronimo.remoting.transport.TransportLoader");
        gbean.setAttribute("bindURI", new URI("async://0.0.0.0:0"));
        gbean.setReferencePatterns("Router", Collections.singleton(subsystemRouter));
        asyncTransport = new ObjectName("geronimo.remoting:transport=async");
        kernel.loadGBean(asyncTransport, gbean);

        gbean = new GBeanMBean("org.apache.geronimo.remoting.router.JMXRouter");
        gbean.setReferencePatterns("SubsystemRouter", Collections.singleton(subsystemRouter));
        jmxRouter = new ObjectName("geronimo.remoting:router=JMXRouter");
        kernel.loadGBean(jmxRouter, gbean);

        gbean = new GBeanMBean("org.apache.geronimo.remoting.router.SubsystemRouter");
        secureSubsystemRouter = new ObjectName("geronimo.remoting:router=SubsystemRouter,type=secure");
        kernel.loadGBean(secureSubsystemRouter, gbean);

        gbean = new GBeanMBean("org.apache.geronimo.remoting.transport.TransportLoader");
        gbean.setAttribute("bindURI", new URI("async://0.0.0.0:4242"));
        gbean.setReferencePatterns("Router", Collections.singleton(secureSubsystemRouter));
        saslTransport = new ObjectName("geronimo.remoting:transport=async,subprotocol=sasl");
        kernel.loadGBean(saslTransport, gbean);

        gbean = new GBeanMBean("org.apache.geronimo.remoting.transport.TransportLoader");
        gbean.setAttribute("bindURI", new URI("async://0.0.0.0:4243"));
        gbean.setReferencePatterns("Router", Collections.singleton(secureSubsystemRouter));
        gssapiTransport = new ObjectName("geronimo.remoting:transport=async,subprotocol=gssapi");
        kernel.loadGBean(gssapiTransport, gbean);

        gbean = new GBeanMBean("org.apache.geronimo.remoting.router.JMXRouter");
        gbean.setReferencePatterns("SubsystemRouter", Collections.singleton(secureSubsystemRouter));
        secureJmxRouter = new ObjectName("geronimo.remoting:router=JMXRouter,type=secure");
        kernel.loadGBean(secureJmxRouter, gbean);

        gbean = new GBeanMBean("org.apache.geronimo.security.remoting.jmx.JaasLoginServiceRemotingServer");
        gbean.setReferencePatterns("Router", Collections.singleton(secureJmxRouter));
        serverStub = new ObjectName("geronimo.remoting:target=JaasLoginServiceRemotingServer");
        kernel.loadGBean(serverStub, gbean);

        kernel.startGBean(loginService);
        kernel.startGBean(testCE);
        kernel.startGBean(testRealm);
        kernel.startGBean(subsystemRouter);
        kernel.startGBean(secureSubsystemRouter);
        kernel.startGBean(asyncTransport);
        kernel.startGBean(saslTransport);
        kernel.startGBean(gssapiTransport);
        kernel.startGBean(jmxRouter);
        kernel.startGBean(secureJmxRouter);
        kernel.startGBean(serverStub);

        URI connectURI = (URI) kernel.getAttribute(asyncTransport, "clientConnectURI");
        asyncRemoteProxy = JaasLoginServiceRemotingClient.create(connectURI.getHost(), connectURI.getPort());

        connectURI = (URI) kernel.getAttribute(saslTransport, "clientConnectURI");
        saslRemoteProxy = JaasLoginServiceRemotingClient.create(connectURI.getHost(), connectURI.getPort());

        connectURI = (URI) kernel.getAttribute(gssapiTransport, "clientConnectURI");
        gssapiRemoteProxy = JaasLoginServiceRemotingClient.create(connectURI.getHost(), connectURI.getPort());
    }

    protected void tearDown() throws Exception {
        kernel.stopGBean(serverStub);
        kernel.stopGBean(secureJmxRouter);
        kernel.stopGBean(jmxRouter);
        kernel.stopGBean(gssapiTransport);
        kernel.stopGBean(saslTransport);
        kernel.stopGBean(asyncTransport);
        kernel.stopGBean(secureSubsystemRouter);
        kernel.stopGBean(subsystemRouter);
        kernel.stopGBean(testRealm);
        kernel.stopGBean(testCE);
        kernel.stopGBean(loginService);
        kernel.stopGBean(serverInfo);

        kernel.unloadGBean(loginService);
        kernel.unloadGBean(testCE);
        kernel.unloadGBean(testRealm);
        kernel.unloadGBean(subsystemRouter);
        kernel.unloadGBean(secureSubsystemRouter);
        kernel.unloadGBean(asyncTransport);
        kernel.unloadGBean(saslTransport);
        kernel.unloadGBean(gssapiTransport);
        kernel.unloadGBean(jmxRouter);
        kernel.unloadGBean(secureJmxRouter);
        kernel.unloadGBean(serverStub);
        kernel.unloadGBean(serverInfo);

        kernel.shutdown();
    }

    class UsernamePasswordCallback implements CallbackHandler {
        private final String username;
        private final String password;

        UsernamePasswordCallback(String username, String password) {
            this.username = username;
            this.password = password;
        }

        public void handle(Callback[] callbacks) throws IOException, UnsupportedCallbackException {
            for (int i = 0; i < callbacks.length; i++) {
                if (callbacks[i] instanceof PasswordCallback) {
                    ((PasswordCallback) callbacks[i]).setPassword(password.toCharArray());
                } else if (callbacks[i] instanceof NameCallback) {
                    ((NameCallback) callbacks[i]).setName(username);
                }
            }
        }
    }
}
