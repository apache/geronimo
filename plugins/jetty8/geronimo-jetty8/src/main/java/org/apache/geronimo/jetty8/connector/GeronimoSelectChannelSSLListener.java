/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */


package org.apache.geronimo.jetty8.connector;

import javax.net.ssl.SSLContext;

import org.eclipse.jetty.server.ssl.SslSelectChannelConnector;
import org.eclipse.jetty.server.ssl.SslSocketConnector;
import org.apache.geronimo.management.geronimo.KeystoreManager;

/**
 * SSL listener that hooks into the Geronimo keystore infrastructure.
 *
 * @version $Rev$ $Date$
 */
public class GeronimoSelectChannelSSLListener extends SslSelectChannelConnector {
    private KeystoreManager manager;
    private String keyStore;
    private String trustStore;
    private String keyAlias;

    public GeronimoSelectChannelSSLListener(KeystoreManager manager) {
        this.manager = manager;
    }

    protected SSLContext createSSLContext() throws Exception {
        return manager.createSSLContext(null, getProtocol(), getSslKeyManagerFactoryAlgorithm(), keyStore, keyAlias, trustStore, SslSocketConnector.class.getClassLoader());
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
