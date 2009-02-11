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
package org.apache.geronimo.jmxremoting;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.UnknownHostException;
import java.rmi.server.RMIClientSocketFactory;
import java.rmi.server.RMIServerSocketFactory;
import java.util.HashMap;
import java.util.Map;

import javax.management.MBeanServer;
import javax.management.NotificationFilterSupport;
import javax.management.remote.JMXConnectionNotification;
import javax.management.remote.JMXConnectorServer;
import javax.management.remote.JMXConnectorServerFactory;
import javax.management.remote.JMXServiceURL;
import javax.management.remote.rmi.RMIConnectorServer;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLServerSocketFactory;
import javax.rmi.ssl.SslRMIClientSocketFactory;

import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.gbean.GBeanInfoBuilder;
import org.apache.geronimo.management.geronimo.KeystoreManager;
import org.apache.geronimo.system.jmx.MBeanServerReference;

/**
 * A secure (SSL/TLS) connector that supports the server side of JSR 160 JMX Remoting.
 *
 * @version $Rev: 651684 $ $Date: 2008-04-25 15:11:52 -0400 (Fri, 25 Apr 2008) $
 */
public class JMXSecureConnector extends JMXConnector {
    
    private KeystoreManager keystoreManager;
    private String algorithm;
    private String secureProtocol;
    private String keyStore;
    private String trustStore;
    private String keyAlias;
    private boolean clientAuth;
    
    public JMXSecureConnector(MBeanServerReference mbeanServerReference, String objectName, ClassLoader classLoader) {
        this(mbeanServerReference.getMBeanServer(), objectName, classLoader);
    }

    public JMXSecureConnector(MBeanServer mbeanServer, String objectName, ClassLoader classLoader) {
        super(mbeanServer, objectName, classLoader);
    }

    public void setKeystoreManager(KeystoreManager keystoreManager) {
        this.keystoreManager = keystoreManager;
    }
            
    public KeystoreManager getKeystoreManager() {
        return this.keystoreManager;
    }
            
    public String getKeyStore() {
        return this.keyStore;
    }
    
    public void setKeyStore(String keyStore) {
        this.keyStore = keyStore;
    }
        
    public String getTrustStore() {
        return this.trustStore;
    }
        
    public void setTrustStore(String trustStore) {
        this.trustStore = trustStore;
    }
        
    public String getKeyAlias() {
        return this.keyAlias;
    }
        
    public void setKeyAlias(String keyAlias) {
        this.keyAlias = keyAlias;
    }
        
    public String getAlgorithm() {
        return this.algorithm;
    }
        
    /**
     * Algorithm to use.
     * As different JVMs have different implementations available, the default algorithm can be used by supplying the value "Default".
     *
     * @param algorithm the algorithm to use, or "Default" to use the default from {@link javax.net.ssl.KeyManagerFactory#getDefaultAlgorithm()}
     */
    public void setAlgorithm(String algorithm) {                
        if ("default".equalsIgnoreCase(algorithm)) {
            this.algorithm = KeyManagerFactory.getDefaultAlgorithm();
        } else {
            this.algorithm = algorithm;
        }
    }
            
    public String getSecureProtocol() {
        return this.secureProtocol;
    }
        
    public void setSecureProtocol(String secureProtocol) {
        this.secureProtocol = secureProtocol;
    }
        
    public void setClientAuth(boolean clientAuth) {
        this.clientAuth = clientAuth;
    }
        
    public boolean isClientAuth() {
        return this.clientAuth;
    }
               
    public void doStart() throws Exception {
        jmxServiceURL = new JMXServiceURL(protocol, host, port, urlPath);
        Map env = new HashMap();
        Authenticator authenticator = null;
        if (applicationConfigName != null) {
            authenticator = new Authenticator(applicationConfigName, classLoader);
            env.put(JMXConnectorServer.AUTHENTICATOR, authenticator);
        } else {
            log.warn("Starting unauthenticating JMXConnector for " + jmxServiceURL);
        }
        
        SSLServerSocketFactory sssf = keystoreManager.createSSLServerFactory(null, secureProtocol, algorithm, keyStore, keyAlias, trustStore, classLoader);
        RMIServerSocketFactory rssf = new GeronimoSslRMIServerSocketFactory(sssf, host, clientAuth);
        RMIClientSocketFactory rcsf = new SslRMIClientSocketFactory();
        env.put(RMIConnectorServer.RMI_SERVER_SOCKET_FACTORY_ATTRIBUTE, rssf);
        env.put(RMIConnectorServer.RMI_CLIENT_SOCKET_FACTORY_ATTRIBUTE, rcsf);
        
        server = JMXConnectorServerFactory.newJMXConnectorServer(jmxServiceURL, env, mbeanServer);
        NotificationFilterSupport filter = new NotificationFilterSupport();
        filter.enableType(JMXConnectionNotification.OPENED);
        filter.enableType(JMXConnectionNotification.CLOSED);
        filter.enableType(JMXConnectionNotification.FAILED);
        server.addNotificationListener(authenticator, filter, null);
        server.start();
        log.debug("Started JMXConnector " + server.getAddress());
    }

    private static class GeronimoSslRMIServerSocketFactory implements RMIServerSocketFactory {
        private SSLServerSocketFactory sssf;
        private boolean clientAuth;
        private InetAddress bindAddress;
        
        public GeronimoSslRMIServerSocketFactory(SSLServerSocketFactory sssf, String bindHost, boolean clientAuth) throws UnknownHostException {
            this.sssf = sssf;
            this.bindAddress = InetAddress.getByName(bindHost);
            this.clientAuth = clientAuth;
        }
        
        public ServerSocket createServerSocket(int port) throws IOException {
            SSLServerSocket ss = (SSLServerSocket) sssf.createServerSocket(port, 0, this.bindAddress);
            ss.setNeedClientAuth(clientAuth);
            return ss;
        }
    }
    
    public static final GBeanInfo GBEAN_INFO;

    static {
        GBeanInfoBuilder infoFactory = GBeanInfoBuilder.createStatic("JMX Secure Remoting Connector", JMXSecureConnector.class);
        infoFactory.addReference("MBeanServerReference", MBeanServerReference.class);
        infoFactory.addAttribute("objectName", String.class, false);
        infoFactory.addAttribute("classLoader", ClassLoader.class, false);

        infoFactory.addAttribute("protocol", String.class, true, true);
        infoFactory.addAttribute("host", String.class, true, true);
        infoFactory.addAttribute("port", int.class, true, true);
        infoFactory.addAttribute("urlPath", String.class, true, true);
        infoFactory.addAttribute("applicationConfigName", String.class, true, true);

        infoFactory.addInterface(JMXConnectorInfo.class);
        
        infoFactory.addReference("KeystoreManager", KeystoreManager.class);
        infoFactory.addAttribute("algorithm", String.class, true, true);
        infoFactory.addAttribute("secureProtocol", String.class, true, true);
        infoFactory.addAttribute("keyStore", String.class, true, true);
        infoFactory.addAttribute("keyAlias", String.class, true, true);
        infoFactory.addAttribute("trustStore", String.class, true, true);
        infoFactory.addAttribute("clientAuth", boolean.class, true, true);
        
        infoFactory.setConstructor(new String[]{"MBeanServerReference", "objectName", "classLoader"});
        GBEAN_INFO = infoFactory.getBeanInfo();
    }

    public static GBeanInfo getGBeanInfo() {
        return GBEAN_INFO;
    }
}
