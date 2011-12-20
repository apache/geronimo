/**
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
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

import java.io.IOException;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.util.HashMap;
import java.util.Map;

import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.NameCallback;
import javax.security.auth.callback.PasswordCallback;
import javax.security.auth.callback.UnsupportedCallbackException;

import org.apache.geronimo.gbean.AbstractName;
import org.apache.geronimo.gbean.AbstractNameQuery;
import org.apache.geronimo.gbean.GBeanData;
import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.gbean.MultiGBeanInfoFactory;
import org.apache.geronimo.kernel.Kernel;
import org.apache.geronimo.kernel.KernelFactory;
import org.apache.geronimo.kernel.osgi.MockBundleContext;
import org.apache.geronimo.kernel.repository.Artifact;
import org.apache.geronimo.security.jaas.ConfigurationEntryFactory;
import org.apache.geronimo.security.jaas.GeronimoLoginConfiguration;
import org.apache.geronimo.security.realm.providers.CertificateCallback;
import org.apache.geronimo.security.realm.providers.CertificateChainCallback;
import org.apache.geronimo.system.serverinfo.BasicServerInfo;
import org.apache.geronimo.testsupport.TestSupport;


/**
 * @version $Rev$ $Date$
 */
public abstract class AbstractTest extends TestSupport {
    protected Kernel kernel;
    protected AbstractName serverInfo;
    protected AbstractName testLoginModule;
    protected AbstractName testRealm;
    private static final String REALM_NAME = "test-realm";
    protected boolean timeoutTest = false;
    protected boolean needServerInfo = true;
    protected AbstractName loginConfiguration;
    protected boolean needLoginConfiguration = true;

    protected void setUp() throws Exception {
        bundleContext = new MockBundleContext(getClass().getClassLoader(), BASEDIR.getAbsolutePath(), null, null);

        kernel = KernelFactory.newInstance(bundleContext).createKernel("test.kernel");
        kernel.boot(bundleContext);

        GBeanData gbean;

        // Create all the parts
        if (needServerInfo) {
            gbean = buildGBeanData("name", "ServerInfo", BasicServerInfo.class);
            serverInfo = gbean.getAbstractName();
            gbean.setAttribute("baseDirectory", BASEDIR.getAbsolutePath());
            kernel.loadGBean(gbean, bundleContext);
            kernel.startGBean(serverInfo);
        }
        if (needLoginConfiguration) {
            gbean = buildGBeanData("name", "LoginConfiguration", GeronimoLoginConfiguration.class);
            loginConfiguration = gbean.getAbstractName();
            gbean.setReferencePattern("Configurations", new AbstractNameQuery(ConfigurationEntryFactory.class.getName()));
            kernel.loadGBean(gbean, bundleContext);
            kernel.startGBean(loginConfiguration);
        }

    }

    protected void tearDown() throws Exception {
        kernel.shutdown();
    }


    protected GBeanData buildGBeanData(String key, String value, Class clazz) throws MalformedObjectNameException {
        AbstractName abstractName = buildAbstractName(key, value);
        GBeanInfo info = new MultiGBeanInfoFactory().getGBeanInfo(clazz);
        return new GBeanData(abstractName, info);
    }

    private AbstractName buildAbstractName(String key, String value) throws MalformedObjectNameException {
        Map names = new HashMap();
        names.put(key, value);
        return new AbstractName(new Artifact("test", "foo", "1", "car"), names, new ObjectName("test:" + key + "=" + value));
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

    public static class CertCallback implements CallbackHandler {
        private final X509Certificate cert;

        public CertCallback(X509Certificate cert) {
            this.cert = cert;
        }

        public void handle(Callback[] callbacks) throws IOException, UnsupportedCallbackException {
            for (int i = 0; i < callbacks.length; i++) {
                if (callbacks[i] instanceof CertificateCallback) {
                    ((CertificateCallback) callbacks[i]).setCertificate(cert);
                }
            }
        }
    }

    public static class CertChainCallback implements CallbackHandler {
        private final Certificate[] certChain;

        public CertChainCallback(Certificate[] certChain) {
            this.certChain = certChain;
        }

        public void handle(Callback[] callbacks) throws IOException, UnsupportedCallbackException {
            for (int i = 0; i < callbacks.length; i++) {
                if (callbacks[i] instanceof CertificateChainCallback) {
                    ((CertificateChainCallback) callbacks[i]).setCertificateChain(certChain);
                }
            }
        }
    }
}
