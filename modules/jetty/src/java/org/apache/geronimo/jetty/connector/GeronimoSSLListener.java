/**
 *
 * Copyright 2005 The Apache Software Foundation
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
package org.apache.geronimo.jetty.connector;

import org.mortbay.http.SslListener;
import org.apache.geronimo.management.geronimo.KeystoreManager;

import javax.net.ssl.SSLServerSocketFactory;

/**
 * SSL listener that hooks into the Geronimo keystore infrastructure.
 *
 * @version $Rev: 46019 $ $Date: 2004-09-14 05:56:06 -0400 (Tue, 14 Sep 2004) $
 */
public class GeronimoSSLListener extends SslListener {
    private KeystoreManager manager;
    private String keyStore;
    private String trustStore;
    private String keyAlias;

    public GeronimoSSLListener(KeystoreManager manager) {
        this.manager = manager;
    }

    protected SSLServerSocketFactory createFactory() throws Exception {
        // we need the server factory version.
        return manager.createSSLServerFactory(null, getProtocol(), getAlgorithm(), keyStore, keyAlias, trustStore, SslListener.class.getClassLoader());
    }

    public String getKeyStore() {
        return keyStore;
    }

    public void setKeyStore(String keyStore) {
        this.keyStore = keyStore;
    }

    public String getTrustStore() {
        return trustStore;
    }

    public void setTrustStore(String trustStore) {
        this.trustStore = trustStore;
    }

    public String getKeyAlias() {
        return keyAlias;
    }

    public void setKeyAlias(String keyAlias) {
        this.keyAlias = keyAlias;
    }
}
