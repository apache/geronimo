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

import javax.management.ObjectName;
import javax.security.auth.Subject;
import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.NameCallback;
import javax.security.auth.callback.PasswordCallback;
import javax.security.auth.callback.UnsupportedCallbackException;
import javax.security.auth.login.LoginContext;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.Collections;

import junit.framework.TestCase;

import org.apache.geronimo.gbean.jmx.GBeanMBean;
import org.apache.geronimo.kernel.Kernel;
import org.apache.geronimo.kernel.jmx.MBeanProxyFactory;
import org.apache.geronimo.remoting.transport.TransportLoader;
import org.apache.geronimo.security.IdentificationPrincipal;
import org.apache.geronimo.security.RealmPrincipal;
import org.apache.geronimo.security.jaas.LoginServiceMBean;
import org.apache.geronimo.system.serverinfo.ServerInfo;


/**
 * @version $Revision: 1.8 $ $Date: 2004/07/12 06:07:52 $
 */
public class RemoteLoginTest extends TestCase {
    Kernel kernel;
    ObjectName serverInfo;
    ObjectName loginService;
    ObjectName kerberosRealm;
    ObjectName subsystemRouter;
    ObjectName secureSubsystemRouter;
    ObjectName asyncTransport;
    ObjectName saslTransport;
    ObjectName gssapiTransport;
    ObjectName jmxRouter;
    ObjectName secureJmxRouter;
    ObjectName serverStub;
    LoginServiceMBean asyncRemoteProxy;
    LoginServiceMBean saslRemoteProxy;
    LoginServiceMBean gssapiRemoteProxy;


    public void testLogin() throws Exception {
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

        gbean = new GBeanMBean("org.apache.geronimo.security.jaas.LoginService");
        loginService = new ObjectName("geronimo.security:type=LoginService");
        gbean.setReferencePatterns("Realms", Collections.singleton(new ObjectName("geronimo.security:type=SecurityRealm,*")));
        gbean.setAttribute("reclaimPeriod", new Long(100));
        gbean.setAttribute("algorithm", "HmacSHA1");
        gbean.setAttribute("password", "secret");
        kernel.loadGBean(loginService, gbean);

        gbean = new GBeanMBean("org.apache.geronimo.security.realm.providers.PropertiesFileSecurityRealm");
        kerberosRealm = new ObjectName("geronimo.security:type=SecurityRealm,realm=properties-realm");
        gbean.setAttribute("realmName", "properties-realm");
        gbean.setAttribute("maxLoginModuleAge", new Long(1 * 1000));
        gbean.setAttribute("usersURI", (new File(new File("."), "src/test-data/data/users.properties")).toURI());
        gbean.setAttribute("groupsURI", (new File(new File("."), "src/test-data/data/groups.properties")).toURI());
        gbean.setReferencePatterns("ServerInfo", Collections.singleton(serverInfo));
        kernel.loadGBean(kerberosRealm, gbean);

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

        gbean = new GBeanMBean("org.apache.geronimo.security.remoting.jmx.LoginServiceStub");
        gbean.setReferencePatterns("Router", Collections.singleton(secureJmxRouter));
        serverStub = new ObjectName("geronimo.remoting:target=LoginServiceStub");
        kernel.loadGBean(serverStub, gbean);

        kernel.startGBean(loginService);
        kernel.startGBean(kerberosRealm);
        kernel.startGBean(subsystemRouter);
        kernel.startGBean(secureSubsystemRouter);
        kernel.startGBean(asyncTransport);
        kernel.startGBean(saslTransport);
        kernel.startGBean(gssapiTransport);
        kernel.startGBean(jmxRouter);
        kernel.startGBean(secureJmxRouter);
        kernel.startGBean(serverStub);

        TransportLoader bean = (TransportLoader) MBeanProxyFactory.getProxy(TransportLoader.class, kernel.getMBeanServer(), asyncTransport);
        URI connectURI = bean.getClientConnectURI();
        asyncRemoteProxy = RemoteLoginServiceFactory.create(connectURI.getHost(), connectURI.getPort());

        bean = (TransportLoader) MBeanProxyFactory.getProxy(TransportLoader.class, kernel.getMBeanServer(), saslTransport);
        connectURI = bean.getClientConnectURI();
        saslRemoteProxy = RemoteLoginServiceFactory.create(connectURI.getHost(), connectURI.getPort());

        bean = (TransportLoader) MBeanProxyFactory.getProxy(TransportLoader.class, kernel.getMBeanServer(), gssapiTransport);
        connectURI = bean.getClientConnectURI();
        gssapiRemoteProxy = RemoteLoginServiceFactory.create(connectURI.getHost(), connectURI.getPort());
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
        kernel.stopGBean(kerberosRealm);
        kernel.stopGBean(loginService);
        kernel.stopGBean(serverInfo);

        kernel.unloadGBean(loginService);
        kernel.unloadGBean(kerberosRealm);
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
