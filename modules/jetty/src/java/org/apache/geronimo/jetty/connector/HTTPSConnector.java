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

package org.apache.geronimo.jetty.connector;

import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.gbean.GBeanInfoBuilder;
import org.apache.geronimo.gbean.WaitingException;
import org.apache.geronimo.jetty.JettyContainer;
import org.apache.geronimo.system.serverinfo.ServerInfo;
import org.mortbay.http.SunJsseListener;

/**
 * @version $Rev$ $Date$
 */
public class HTTPSConnector extends JettyConnector {
    private final SunJsseListener https;
    private final ServerInfo serverInfo;
    private String keystore;

    public HTTPSConnector(JettyContainer container, ServerInfo serverInfo) {
        super(container, new SunJsseListener());
        this.serverInfo = serverInfo;
        https = (SunJsseListener)listener;
    }

    public String getKeystore() {
        return keystore;
    }

    public void setKeystore(String keystore) {
        this.keystore = keystore;
    }

    public void setKeyPassword(String password) {
        https.setKeyPassword(password);
    }

    public void setKeystoreProviderClass(String cls) {
        https.setKeystoreProviderClass(cls);
    }

    public String getKeystoreProviderClass() {
        return https.getKeystoreProviderClass();
    }

    public void setKeystoreProviderName(String cls) {
        https.setKeystoreProviderName(cls);
    }

    public String getKeystoreProviderName() {
        return https.getKeystoreProviderName();
    }

    public void setKeystoreType(String cls) {
        https.setKeystoreType(cls);
    }

    public String getKeystoreType() {
        return https.getKeystoreType();
    }

    public void setPassword(String password) {
        https.setPassword(password);
    }

    public void setUseDefaultTrustStore(boolean use) {
        https.setUseDefaultTrustStore(use);
    }

    public boolean getUseDefaultTrustStore() {
        return https.getUseDefaultTrustStore();
    }

    public void setNeedClientAuth(boolean auth) {
        https.setNeedClientAuth(auth);
    }

    public boolean getNeedClientAuth() {
        return https.getNeedClientAuth();
    }

    public void doStart() throws WaitingException, Exception {
        https.setKeystore(serverInfo.resolvePath(keystore));
        super.doStart();
    }

    public static final GBeanInfo GBEAN_INFO;

    static {
        GBeanInfoBuilder infoFactory = new GBeanInfoBuilder("Jetty HTTPS Connector", HTTPSConnector.class, JettyConnector.GBEAN_INFO);
        infoFactory.addAttribute("keystore", String.class, true);
        infoFactory.addAttribute("keyPassword", String.class, true);
        infoFactory.addAttribute("keystoreProviderClass", String.class, true);
        infoFactory.addAttribute("keystoreProviderName", String.class, true);
        infoFactory.addAttribute("keystoreType", String.class, true);
        infoFactory.addAttribute("password", String.class, true);
        infoFactory.addAttribute("useDefaultTrustStore", boolean.class, true);
        infoFactory.addAttribute("needClientAuth", boolean.class, true);
        infoFactory.addReference("ServerInfo", ServerInfo.class);
        infoFactory.setConstructor(new String[]{"JettyContainer", "ServerInfo"});
        GBEAN_INFO = infoFactory.getBeanInfo();
    }

    public static GBeanInfo getGBeanInfo() {
        return GBEAN_INFO;
    }
}
