/* ====================================================================
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 2003 The Apache Software Foundation.  All rights
 * reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. The end-user documentation included with the redistribution,
 *    if any, must include the following acknowledgment:
 *       "This product includes software developed by the
 *        Apache Software Foundation (http://www.apache.org/)."
 *    Alternately, this acknowledgment may appear in the software itself,
 *    if and wherever such third-party acknowledgments normally appear.
 *
 * 4. The names "Apache" and "Apache Software Foundation" and
 *    "Apache Geronimo" must not be used to endorse or promote products
 *    derived from this software without prior written permission. For
 *    written permission, please contact apache@apache.org.
 *
 * 5. Products derived from this software may not be called "Apache",
 *    "Apache Geronimo", nor may "Apache" appear in their name, without
 *    prior written permission of the Apache Software Foundation.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE APACHE SOFTWARE FOUNDATION OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 *
 * ====================================================================
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
 * @version $Revision: 1.1 $ $Date: 2004/02/17 00:05:40 $
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
