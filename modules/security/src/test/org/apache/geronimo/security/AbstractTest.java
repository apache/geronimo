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

package org.apache.geronimo.security;

import javax.management.ObjectName;
import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.NameCallback;
import javax.security.auth.callback.PasswordCallback;
import javax.security.auth.callback.UnsupportedCallbackException;

import java.io.IOException;
import java.net.URI;
import java.util.Collections;

import junit.framework.TestCase;

import org.apache.geronimo.gbean.jmx.GBeanMBean;
import org.apache.geronimo.kernel.Kernel;
import org.apache.geronimo.security.bridge.TestRealm;


/**
 * @version $Revision: 1.2 $ $Date: 2004/02/25 09:58:10 $
 */
public abstract class AbstractTest extends TestCase {
    protected Kernel kernel;
    protected ObjectName loginService;
    protected ObjectName testRealm;
    protected ObjectName subsystemRouter;
    protected ObjectName asyncTransport;
    protected ObjectName jmxRouter;
    protected ObjectName serverStub;

    protected void setUp() throws Exception {
        kernel = new Kernel("test.kernel", "simple.geronimo.test");
        kernel.boot();

        GBeanMBean gbean;

        // Create all the parts

        gbean = new GBeanMBean("org.apache.geronimo.security.jaas.LoginService");
        loginService = new ObjectName("geronimo.security:type=LoginService");
        gbean.setReferencePatterns("Realms", Collections.singleton(new ObjectName("geronimo.security:type=SecurityRealm,*")));
        gbean.setAttribute("Kernel", kernel);
        gbean.setAttribute("ReclaimPeriod", new Long(100));
        gbean.setAttribute("Algorithm", "HmacSHA1");
        gbean.setAttribute("Password", "secret");
        kernel.loadGBean(loginService, gbean);

        gbean = new GBeanMBean("org.apache.geronimo.security.bridge.TestRealm");
        testRealm = new ObjectName("geronimo.security:type=SecurityRealm,realm=testrealm");
        gbean.setAttribute("RealmName", TestRealm.REALM_NAME);
        gbean.setAttribute("MaxLoginModuleAge", new Long(1 * 1000));
        gbean.setAttribute("debug", new Boolean(true));
        kernel.loadGBean(testRealm, gbean);

        gbean = new GBeanMBean("org.apache.geronimo.remoting.router.SubsystemRouter");
        subsystemRouter = new ObjectName("geronimo.remoting:router=SubsystemRouter");
        kernel.loadGBean(subsystemRouter, gbean);

        gbean = new GBeanMBean("org.apache.geronimo.remoting.transport.TransportLoader");
        gbean.setAttribute("BindURI", new URI("async://0.0.0.0:4242"));
        gbean.setReferencePatterns("Router", Collections.singleton(subsystemRouter));
        asyncTransport = new ObjectName("geronimo.remoting:transport=async");
        kernel.loadGBean(asyncTransport, gbean);

        gbean = new GBeanMBean("org.apache.geronimo.remoting.router.JMXRouter");
        gbean.setReferencePatterns("SubsystemRouter", Collections.singleton(subsystemRouter));
        jmxRouter = new ObjectName("geronimo.remoting:router=JMXRouter");
        kernel.loadGBean(jmxRouter, gbean);

        gbean = new GBeanMBean("org.apache.geronimo.security.remoting.jmx.LoginServiceStub");
        gbean.setReferencePatterns("Router", Collections.singleton(jmxRouter));
        serverStub = new ObjectName("geronimo.remoting:target=LoginServiceStub");
        kernel.loadGBean(serverStub, gbean);

        kernel.startGBean(loginService);
        kernel.startGBean(testRealm);
        kernel.startGBean(subsystemRouter);
        kernel.startGBean(asyncTransport);
        kernel.startGBean(jmxRouter);
        kernel.startGBean(serverStub);
    }

    protected void tearDown() throws Exception {
        kernel.stopGBean(serverStub);
        kernel.stopGBean(jmxRouter);
        kernel.stopGBean(asyncTransport);
        kernel.stopGBean(subsystemRouter);
        kernel.stopGBean(testRealm);
        kernel.stopGBean(loginService);

        kernel.unloadGBean(loginService);
        kernel.unloadGBean(testRealm);
        kernel.unloadGBean(subsystemRouter);
        kernel.unloadGBean(asyncTransport);
        kernel.unloadGBean(jmxRouter);
        kernel.unloadGBean(serverStub);

        kernel.shutdown();
    }

    public static class UsernamePasswordCallback implements CallbackHandler {
        private final String username;
        private final String password;

        public UsernamePasswordCallback(String username, String password) {
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
