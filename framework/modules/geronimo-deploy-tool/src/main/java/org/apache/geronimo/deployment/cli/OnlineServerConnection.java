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

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;
import java.util.jar.JarFile;

import javax.enterprise.deploy.shared.factories.DeploymentFactoryManager;
import javax.enterprise.deploy.spi.exceptions.DeploymentManagerCreationException;
import javax.enterprise.deploy.spi.factories.DeploymentFactory;

import org.apache.geronimo.cli.deployer.ConnectionParams;
import org.apache.geronimo.common.DeploymentException;
import org.apache.geronimo.crypto.EncryptionManager;
import org.apache.geronimo.deployment.cli.DeployUtils.SavedAuthentication;
import org.apache.geronimo.deployment.plugin.factories.AuthenticationFailedException;
import org.apache.geronimo.deployment.plugin.jmx.JMXDeploymentManager;
import org.apache.geronimo.kernel.util.JarUtils;

/**
 * Supports online connections to the server, via JSR-88, valid only when the
 * server is online.
 *
 * @version $Rev$ $Date$
 */
public class OnlineServerConnection extends ServerConnection {

    private final DeploymentFactory geronimoDeploymentFactory;

    private final ServerConnection.UsernamePasswordHandler handler;
    
    private boolean logToSysErr;

    private boolean verboseMessages;

    String KEYSTORE_TRUSTSTORE_PASSWORD_FILE = "org.apache.geronimo.keyStoreTrustStorePasswordFile";

    String DEFAULT_TRUSTSTORE_KEYSTORE_LOCATION = "/var/security/keystores/geronimo-default";

    String GERONIMO_HOME = "org.apache.geronimo.home.dir";

    String DEFAULT_KEYSTORE_TRUSTSTORE_PASSWORD_FILE = System.getProperty(GERONIMO_HOME) + "/var/config/config-substitutions.properties";

    public OnlineServerConnection(ConnectionParams params, ConsoleReader consoleReader, DeploymentFactory geronimoDeploymentFactory) throws DeploymentException {
        this(params, new DefaultUserPasswordHandler(consoleReader), geronimoDeploymentFactory);
    }

    public OnlineServerConnection(ConnectionParams params, ServerConnection.UsernamePasswordHandler handler, DeploymentFactory geronimoDeploymentFactory) throws DeploymentException {
        this.geronimoDeploymentFactory = geronimoDeploymentFactory;
        this.handler = handler;
        String uri = params.getURI();
        String driver = params.getDriver();
        String user = params.getUser();
        String password = params.getPassword();
        String host = params.getHost();
        Integer port = params.getPort();
        verboseMessages = params.isVerbose();
        logToSysErr = params.isSyserr();
        boolean secure = params.isSecure();
        
        if ((driver != null) && uri == null) {
            throw new DeploymentSyntaxException("A custom driver requires a custom URI");
        }
        if (params.isOffline()) {
            throw new DeploymentException("Offline connection is not supported");
        }
        if (host != null || port != null) {
            uri = DeployUtils.getConnectionURI(host, port, secure);
        }
        
        ClassLoader oldCL = Thread.currentThread().getContextClassLoader();
        Thread.currentThread().setContextClassLoader(DeployUtils.class.getClassLoader());
        try {
            tryToConnect(uri, driver, user, password, secure);
        } finally  {
            Thread.currentThread().setContextClassLoader(oldCL);
        }
        if (manager == null) {
            throw new DeploymentException("Unexpected error; connection failed.");
        }
    }

    private void tryToConnect(String argURI, String driver, String user, String password, boolean secure) throws DeploymentException {
        DeploymentFactoryManager mgr = DeploymentFactoryManager.getInstance();
        if (driver != null) {
            loadDriver(driver, mgr);
        } else {
            mgr.registerDeploymentFactory(geronimoDeploymentFactory);
        }
        String useURI = argURI == null ? DeployUtils.getConnectionURI(null, null, secure) : argURI;
        if (user == null && password == null) {
            try {
                SavedAuthentication savedAuthentication = DeployUtils.readSavedCredentials(useURI);
                if (savedAuthentication != null) {
                    user = savedAuthentication.getUser();
                    password = new String(savedAuthentication.getPassword());
                }
            } catch (IOException e) {
                System.out.println("Warning: " + e.getMessage());
            }
        }
        if (secure) {
            try {
                Properties props = new Properties();
                String keyStorePassword = null;
                String trustStorePassword = null;
                FileInputStream fstream = new FileInputStream(System.getProperty(KEYSTORE_TRUSTSTORE_PASSWORD_FILE, DEFAULT_KEYSTORE_TRUSTSTORE_PASSWORD_FILE));
                props.load(fstream);
                keyStorePassword = (String) EncryptionManager.decrypt(props.getProperty("keyStorePassword"));
                trustStorePassword = (String) EncryptionManager.decrypt(props.getProperty("trustStorePassword"));
                fstream.close();
                String value = System.getProperty("javax.net.ssl.keyStore", System.getProperty(GERONIMO_HOME) + DEFAULT_TRUSTSTORE_KEYSTORE_LOCATION);
                String value1 = System.getProperty("javax.net.ssl.trustStore", System.getProperty(GERONIMO_HOME) + DEFAULT_TRUSTSTORE_KEYSTORE_LOCATION);
                System.setProperty("javax.net.ssl.keyStore", value);
                System.setProperty("javax.net.ssl.trustStore", value1);
                System.setProperty("javax.net.ssl.keyStorePassword", keyStorePassword);
                System.setProperty("javax.net.ssl.trustStorePassword", trustStorePassword);
            } catch (IOException e) {
                throw new DeploymentException("Unable to set KeyStorePassword and TrustStorePassword.", e);
            }
        }
        if (user == null || password == null) {
            try {
                if (user == null) {
                    user = handler.getUsername();
                }
                if (password == null) {
                    password = handler.getPassword();
                }
            } catch (IOException e) {
                throw new DeploymentException("Unable to prompt for login", e);
            }
        }
        try {
            manager = mgr.getDeploymentManager(useURI, user, password);
            auth = new SavedAuthentication(useURI, user, password == null ? null : password.toCharArray());
        } catch (AuthenticationFailedException e) {
            // server's there, you just can't talk to it
            throw new DeploymentException("Login Failed");
        } catch (DeploymentManagerCreationException e) {
            throw new DeploymentException("Unable to connect to server at " + useURI + " -- " + e.getMessage(), e);
        }
        if (manager instanceof JMXDeploymentManager) {
            JMXDeploymentManager deploymentManager = (JMXDeploymentManager) manager;
            deploymentManager.setLogConfiguration(logToSysErr, verboseMessages);
        }
    }

    private void loadDriver(String driver, DeploymentFactoryManager mgr) throws DeploymentException {
        File file = new File(driver);
        try {
            if (!file.exists() || !file.canRead() || !JarUtils.isJarFile(file)) {
                throw new DeploymentSyntaxException("Driver '" + file.getAbsolutePath() + "' is not a readable JAR file");
            }
        } catch (IOException e) {
            throw new DeploymentException("Driver '" + file.getAbsolutePath() + "' is not a readable JAR file");
        }
        String className = null;
        try {
            JarFile jar = new JarFile(file);
            className = jar.getManifest().getMainAttributes().getValue("J2EE-DeploymentFactory-Implementation-Class");
            if (className == null) {
                throw new DeploymentException("The driver JAR " + file.getAbsolutePath() + " does not specify a J2EE-DeploymentFactory-Implementation-Class; cannot load driver.");
            }
            jar.close();
            DeploymentFactory factory = (DeploymentFactory) Class.forName(className).newInstance();
            mgr.registerDeploymentFactory(factory);
        } catch (DeploymentException e) {
            throw e;
        } catch (Exception e) {
            throw new DeploymentSyntaxException("Unable to load driver class " + className + " from JAR " + file.getAbsolutePath(), e);
        }
    }

    private static class DefaultUserPasswordHandler implements ServerConnection.UsernamePasswordHandler {

        private ConsoleReader consoleReader;

        public DefaultUserPasswordHandler(ConsoleReader consoleReader) {
            this.consoleReader = consoleReader;
        }

        public String getPassword() throws IOException {
            return new String(consoleReader.readPassword("Password: "));
        }

        public String getUsername() throws IOException {
            return consoleReader.readLine("Username: ");
        }
    }
}
