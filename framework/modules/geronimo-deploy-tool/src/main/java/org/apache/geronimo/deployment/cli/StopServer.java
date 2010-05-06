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

package org.apache.geronimo.deployment.cli;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import javax.management.MBeanServerConnection;
import javax.management.ObjectName;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;
import javax.management.remote.rmi.RMIConnectorServer;
import javax.rmi.ssl.SslRMIClientSocketFactory;

import org.apache.geronimo.cli.shutdown.ShutdownCLParser;
import org.apache.geronimo.crypto.EncryptionManager;
import org.apache.geronimo.deployment.cli.DeployUtils.SavedAuthentication;
import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.gbean.GBeanInfoBuilder;
import org.apache.geronimo.kernel.util.Main;
import org.osgi.framework.Bundle;

/**
 * @version $Rev$ $Date$
 */
public class StopServer implements Main {

	public static final String DEFAULT_PORT = "1099"; // 1099 is used by java.rmi.registry.Registry

	private String host;
	private Integer port;
	private String user;
	private String password;
	private boolean secure;

    private final Bundle bundle;

    String KEYSTORE_TRUSTSTORE_PASSWORD_FILE = "org.apache.geronimo.keyStoreTrustStorePasswordFile";
    String DEFAULT_TRUSTSTORE_KEYSTORE_LOCATION = "/var/security/keystores/geronimo-default";
    String GERONIMO_HOME = "org.apache.geronimo.home.dir";
    String DEFAULT_KEYSTORE_TRUSTSTORE_PASSWORD_FILE = System.getProperty(GERONIMO_HOME)
            + "/var/config/config-substitutions.properties";

    public StopServer(Bundle bundle) {
        this.bundle = bundle;
    }

    public int execute(Object opaque) {
        if (! (opaque instanceof ShutdownCLParser)) {
            throw new IllegalArgumentException("Argument type is [" + opaque.getClass() + "]; expected [" + ShutdownCLParser.class + "]");
        }
        ShutdownCLParser parser = (ShutdownCLParser) opaque;

        port = parser.getPort();
        if (port == null) {
            port = new Integer(DEFAULT_PORT);
        }

        host = parser.getHost();
        if (host == null) {
            host = "localhost";
        }

        secure = parser.isSecure();

        if(secure){

          try {
                Properties props = new Properties();

                String keyStorePassword = null;
                String trustStorePassword = null;

                FileInputStream fstream = new FileInputStream(System.getProperty(KEYSTORE_TRUSTSTORE_PASSWORD_FILE,
                        DEFAULT_KEYSTORE_TRUSTSTORE_PASSWORD_FILE));
                props.load(fstream);

                keyStorePassword = (String) EncryptionManager.decrypt(props.getProperty("keyStorePassword"));
                trustStorePassword = (String) EncryptionManager.decrypt(props.getProperty("trustStorePassword"));

                fstream.close();

                String value = System.getProperty("javax.net.ssl.keyStore", System.getProperty(GERONIMO_HOME)
                        + DEFAULT_TRUSTSTORE_KEYSTORE_LOCATION);
                String value1 = System.getProperty("javax.net.ssl.trustStore", System.getProperty(GERONIMO_HOME)
                        + DEFAULT_TRUSTSTORE_KEYSTORE_LOCATION);
                System.setProperty("javax.net.ssl.keyStore", value);
                System.setProperty("javax.net.ssl.trustStore", value1);
                System.setProperty("javax.net.ssl.keyStorePassword", keyStorePassword);
                System.setProperty("javax.net.ssl.trustStorePassword", trustStorePassword);
            }

            catch (IOException e) {
                System.out.println("Unable to set KeyStorePassword and TrustStorePassword");
                e.printStackTrace();
            }

        }

        user = parser.getUser();

        password = parser.getPassword();

        if (user == null && password == null) {
            String uri = DeployUtils.getConnectionURI(host, port, secure);
            try {
                SavedAuthentication savedAuthentication = DeployUtils.readSavedCredentials(uri);
                if (savedAuthentication != null) {
                    user = savedAuthentication.getUser();
                    password = new String(savedAuthentication.getPassword());
                }
            } catch (IOException e) {
                System.out.println("Warning: " + e.getMessage());
            }
        }

        if (user == null || password == null) {
            try {
                ConsoleReader consoleReader = new StreamConsoleReader(System.in, System.out);
                if (user == null) {
                    user = consoleReader.readLine("Username: ");
                }
                if (password == null) {
                    password = new String(consoleReader.readPassword("Password: "));
                }
            } catch (Exception e) {
                System.out.println("Unable to prompt for login.");
                return 1;
            }
        }

        System.out.print("Locating server on " + host + ":" + port + "... ");
        MBeanServerConnection conn = null;
        try {
            conn = getMBeanServerConnection();
        } catch (Exception e) {
            System.err.println("Could not communicate with the server.  The server may not be running or the port number may be incorrect (" + e.getMessage() + ")");
            return 1;
        }
        if (conn != null) {
            System.out.println("Server found.");
            try {
                shutdown(conn);
            } catch (Exception e) {
                System.err.println("Error shutting down the server");
                e.printStackTrace();
                return 2;
            }
        }
        return 0;
    }

    public MBeanServerConnection getMBeanServerConnection() throws Exception {
        Map<String, Object> map = new HashMap<String, Object>();
        map.put(JMXConnector.CREDENTIALS, new String[] { user, password });
        String connectorName = "/JMXConnector";
        if (secure) {
            connectorName = "/JMXSecureConnector";
            SslRMIClientSocketFactory csf = new SslRMIClientSocketFactory();
            map.put(RMIConnectorServer.RMI_CLIENT_SOCKET_FACTORY_ATTRIBUTE, csf);
        }
        JMXServiceURL address = new JMXServiceURL(
                "service:jmx:rmi:///jndi/rmi://" + host + ":" + port + connectorName);
        JMXConnector jmxConnector = JMXConnectorFactory.connect(address, map);
        return jmxConnector.getMBeanServerConnection();
    }

    public void shutdown(MBeanServerConnection mbServerConnection) throws Exception {
        Set<ObjectName> objectNameSet =
            mbServerConnection.queryNames(new ObjectName("osgi.core:type=framework,*"), null);
        if (objectNameSet.isEmpty()) {
            throw new Exception("Framework mbean not found");
        } else if (objectNameSet.size() == 1) {
            System.out.println("Server shutdown started");
            mbServerConnection.invoke(objectNameSet.iterator().next(), "stopBundle",
                                      new Object[] { 0 }, new String[] { long.class.getName() });
            System.out.println("Server shutdown completed");
        } else {
            throw new Exception("Found multiple framework mbeans");
        }
    }

    public static final GBeanInfo GBEAN_INFO;

    static {
        GBeanInfoBuilder infoBuilder = GBeanInfoBuilder.createStatic(StopServer.class, "StopServer");
        infoBuilder.addAttribute("bundle", Bundle.class, false);
        infoBuilder.setConstructor(new String[]{"bundle"});
        infoBuilder.addInterface(Main.class);

        GBEAN_INFO = infoBuilder.getBeanInfo();
    }

    public static GBeanInfo getGBeanInfo() {
        return GBEAN_INFO;
    }

}
