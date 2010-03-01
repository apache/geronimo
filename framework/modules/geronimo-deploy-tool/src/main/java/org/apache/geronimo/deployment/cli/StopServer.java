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
import java.net.MalformedURLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import javax.management.MBeanServerConnection;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;
import javax.management.remote.rmi.RMIConnectorServer;
import javax.rmi.ssl.SslRMIClientSocketFactory;

import org.apache.geronimo.crypto.EncryptionManager;
import org.apache.geronimo.deployment.cli.DeployUtils.SavedAuthentication;
import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.gbean.GBeanInfoBuilder;
import org.apache.geronimo.kernel.Kernel;
import org.apache.geronimo.kernel.util.Main;
import org.apache.geronimo.system.jmx.KernelDelegate;

/**
 * @version $Rev$ $Date$
 */
public class StopServer implements Main {

    public static final String RMI_NAMING_CONFG_ID = "org/apache/geronimo/RMINaming";

    public static final String DEFAULT_PORT = "1099"; // 1099 is used by
                                                      // java.rmi.registry.Registry

    String host;

    String port;

    String user;

    String password;

    boolean secure = false;

    private String[] args;
    String KEYSTORE_TRUSTSTORE_PASSWORD_FILE = "org.apache.geronimo.keyStoreTrustStorePasswordFile";
    String DEFAULT_TRUSTSTORE_KEYSTORE_LOCATION = "/var/security/keystores/geronimo-default";
    String GERONIMO_HOME = "org.apache.geronimo.home.dir";
    String DEFAULT_KEYSTORE_TRUSTSTORE_PASSWORD_FILE = System.getProperty(GERONIMO_HOME)
            + "/var/config/config-substitutions.properties";

    public static void main(String[] args) throws Exception {
        StopServer cmd = new StopServer();
        cmd.execute(args);
    }

    public int execute(Object opaque) {
        if (!(opaque instanceof String[])) {
            throw new IllegalArgumentException("Argument type is [" + opaque.getClass() + "]; expected ["
                    + String[].class + "]");
        }
        this.args = (String[]) opaque;

        int i = 0;
        while (i < args.length && args[i].startsWith("--")) {
            if (setParam(i++)) {
                i++;
            }
        }

        if (i < args.length) {
            // There was an argument error somewhere.
            printUsage();
        }

        Integer portI = null;
        if (port != null) {
            try {
                portI = new Integer(port);
            } catch (NumberFormatException e) {
                System.out.println("Invalid port number specified.");
                return 1;
            }
        }

        if (user == null && password == null) {
            String uri = DeployUtils.getConnectionURI(host, portI, secure);
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
                InputPrompt prompt = new InputPrompt(System.in, System.out);
                if (user == null) {
                    user = prompt.getInput("Username: ");
                }
                if (password == null) {
                    password = prompt.getPassword("Password: ");
                }
            } catch (IOException e) {
                System.out.println("Unable to prompt for login.");
                return 1;
            }
        }

        try {
            if (port == null) {
                port = DEFAULT_PORT;
            }
            if (host == null) {
                host = "localhost";
            }
            System.out.print("Locating server on " + host + ":" + port + "... ");
            Kernel kernel = null;
            try {
                kernel = getRunningKernel();
            } catch (IOException e) {
                System.out.println();
                System.out
                        .println("Could not communicate with the server.  The server may not be running or the port number may be incorrect ("
                                + e.getMessage() + ")");
            }
            if (kernel != null) {
                System.out.println("Server found.");
                System.out.println("Server shutdown started");
                kernel.shutdown();
                System.out.println("Server shutdown completed");
            }
        } catch (Exception e) {
            e.printStackTrace();
            return 1;
        }
        return 0;
    }

    private boolean argumentHasValue(int i) {
        return i + 1 < args.length && !args[i + 1].startsWith("--");
    }

    private boolean setParam(int i) {
        if (argumentHasValue(i)) {
            if (args[i].equals("--user")) {
                user = args[++i];
            } else if (args[i].equals("--password")) {
                password = args[++i];
            } else if (args[i].equals("--port")) {
                port = args[++i];
            } else if (args[i].equals("--host")) {
                host = args[++i];
            } else {
                printUsage();
            }
            return true;
        } else if (args[i].equals("--secure")) {
            secure = true;
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
        } else {
            printUsage();
        }
        return false;
    }

    public Kernel getRunningKernel() throws IOException {
        Map map = new HashMap();
        map.put(JMXConnector.CREDENTIALS, new String[] { user, password });
        String connectorName = "/JMXConnector";
        if (secure) {
            connectorName = "/JMXSecureConnector";
            SslRMIClientSocketFactory csf = new SslRMIClientSocketFactory();
            map.put(RMIConnectorServer.RMI_CLIENT_SOCKET_FACTORY_ATTRIBUTE, csf);
        }
        Kernel kernel = null;
        try {
            JMXServiceURL address = new JMXServiceURL("service:jmx:rmi:///jndi/rmi://" + host + ":" + port
                    + connectorName);
            JMXConnector jmxConnector = JMXConnectorFactory.connect(address, map);
            MBeanServerConnection mbServerConnection = jmxConnector.getMBeanServerConnection();
            kernel = new KernelDelegate(mbServerConnection);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        return kernel;
    }

    public void printUsage() {
        System.out.println();
        System.out.println("Command-line shutdown syntax:");
        System.out.println("    shutdown [options]");
        System.out.println();
        System.out.println("The available options are:");
        System.out.println("    --user <username>");
        System.out.println("    --password <password>");
        System.out.println("    --host <hostname>");
        System.out.println("    --port <port>");
        System.out.println("    --secure");
        System.exit(1);
    }

    public static final GBeanInfo GBEAN_INFO;

    static {
        GBeanInfoBuilder infoBuilder = GBeanInfoBuilder.createStatic(StopServer.class, "StopServer");

        infoBuilder.addInterface(Main.class);

        infoBuilder.setConstructor(new String[0]);

        GBEAN_INFO = infoBuilder.getBeanInfo();
    }

    public static GBeanInfo getGBeanInfo() {
        return GBEAN_INFO;
    }

}
