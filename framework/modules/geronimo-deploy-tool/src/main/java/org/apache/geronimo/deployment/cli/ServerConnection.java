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

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.Serializable;
import java.util.jar.JarFile;

import javax.enterprise.deploy.shared.factories.DeploymentFactoryManager;
import javax.enterprise.deploy.spi.DeploymentManager;
import javax.enterprise.deploy.spi.exceptions.DeploymentManagerCreationException;
import javax.enterprise.deploy.spi.factories.DeploymentFactory;

import org.apache.geronimo.cli.deployer.ConnectionParams;
import org.apache.geronimo.common.DeploymentException;
import org.apache.geronimo.crypto.EncryptionManager;
import org.apache.geronimo.deployment.cli.DeployUtils.SavedAuthentication;
import org.apache.geronimo.deployment.plugin.factories.AuthenticationFailedException;
import org.apache.geronimo.deployment.plugin.jmx.JMXDeploymentManager;
import org.apache.geronimo.deployment.plugin.jmx.LocalDeploymentManager;
import org.apache.geronimo.kernel.GBeanNotFoundException;
import org.apache.geronimo.kernel.Kernel;
import org.apache.geronimo.kernel.util.JarUtils;

/**
 * Supports online connections to the server, via JSR-88, valid only
 * when the server is online.
 *
 * @version $Rev$ $Date$
 */
public class ServerConnection {

    private final DeploymentFactory geronimoDeploymentFactory;

    private DeploymentManager manager;
    private UsernamePasswordHandler handler;
    private SavedAuthentication auth;
    private boolean logToSysErr;
    private boolean verboseMessages;
    String KEYSTORE_TRUSTSTORE_PASSWORD_FILE="org.apache.geronimo.keyStoreTrustStorePasswordFile";
    String DEFAULT_TRUSTSTORE_KEYSTORE_LOCATION="/var/security/keystores/geronimo-default";
    String GERONIMO_HOME="org.apache.geronimo.home.dir";

    public ServerConnection(ConnectionParams params, PrintWriter out, InputStream in, Kernel kernel, DeploymentFactory geronimoDeploymentFactory) throws DeploymentException {
        this(params, new DefaultUserPasswordHandler(in, out), kernel, geronimoDeploymentFactory);
    }

    public ServerConnection(ConnectionParams params, UsernamePasswordHandler handler, Kernel kernel, DeploymentFactory geronimoDeploymentFactory) throws DeploymentException {
        if (null == kernel) {
            throw new IllegalArgumentException("kernel is required");
        }
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
        boolean offline = params.isOffline();
        boolean secure = params.isSecure();

        if ((driver != null) && uri == null) {
            throw new DeploymentSyntaxException("A custom driver requires a custom URI");
        }
        if (host != null || port != null) {
            uri = DeployUtils.getConnectionURI(host, port, secure);
        }
        if (offline) {
            startOfflineDeployer(kernel);
            try {
                manager = new LocalDeploymentManager(kernel);
            } catch (IOException e) {
                throw new DeploymentException(e);
            }
        } else {

            ClassLoader OldCL = Thread.currentThread().getContextClassLoader();
            Thread.currentThread().setContextClassLoader(DeployUtils.class.getClassLoader());

            tryToConnect(uri, driver, user, password, secure);

            Thread.currentThread().setContextClassLoader(OldCL);

        }
        if (manager == null) {
            throw new DeploymentException("Unexpected error; connection failed.");
        }
    }

    protected void startOfflineDeployer(Kernel kernel) throws DeploymentException {
        try {
            OfflineDeployerStarter offlineDeployerStarter = new OfflineDeployerStarter(kernel);
            offlineDeployerStarter.start();
        } catch (GBeanNotFoundException e) {
            throw new DeploymentException(e);
        }
    }

    public void close() throws DeploymentException {
        if (manager != null) {
            manager.release();
        }
    }

    Serializable getAuthentication() {
        return auth;
    }

    String getServerURI() {
        return (auth == null) ? null : auth.getURI();
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

        if(secure)
        {
            try {
            FileInputStream fstream= new FileInputStream(System.getProperty(KEYSTORE_TRUSTSTORE_PASSWORD_FILE));
            DataInputStream in = new DataInputStream(fstream);
            BufferedReader br = new BufferedReader(new InputStreamReader(in));
            String strLine;
            String keyStorePassword=null;
            String trustStorePassword=null;
            while ((strLine = br.readLine()) != null)   {
                if(strLine.startsWith("keyStorePassword"))
                {
                    keyStorePassword=(String)EncryptionManager.decrypt(strLine.substring(17));                    
                }
                if(strLine.startsWith("trustStorePassword"))
                {
                    trustStorePassword=(String)EncryptionManager.decrypt(strLine.substring(19));;
                }
            }
             
            String value=System.getProperty("javax.net.ssl.keyStore",System.getProperty(GERONIMO_HOME)+DEFAULT_TRUSTSTORE_KEYSTORE_LOCATION);
            String value1=System.getProperty("javax.net.ssl.trustStore",System.getProperty(GERONIMO_HOME)+DEFAULT_TRUSTSTORE_KEYSTORE_LOCATION);
            System.setProperty("javax.net.ssl.keyStore", value);
            System.setProperty("javax.net.ssl.trustStore", value1);
            System.setProperty("javax.net.ssl.keyStorePassword",keyStorePassword);
            System.setProperty("javax.net.ssl.trustStorePassword",trustStorePassword);
            }
            catch(NullPointerException e)
            {
                throw new NullPointerException("Null value specified for trustStore keyStore location property org.apache.geronimo.keyStoreTrustStorePasswordFile");
            }
            catch(IOException e)
            {
                throw new DeploymentException("Unable to set KeyStorePassword and TrustStorePassword", e);
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

    public DeploymentManager getDeploymentManager() {
        return manager;
    }

    public boolean isGeronimo() {
        return manager.getClass().getName().startsWith("org.apache.geronimo.");
    }

    public static interface UsernamePasswordHandler {
        String getUsername() throws IOException;
        String getPassword() throws IOException;
    }

    private static class DefaultUserPasswordHandler implements UsernamePasswordHandler {

        private PrintWriter out;
        private InputStream in;
        private InputPrompt prompt;

        public DefaultUserPasswordHandler(InputStream in, PrintWriter out) {
            this.out = out;
            this.in = in;
        }

        private void initPrompt() throws IOException {
            this.prompt = new InputPrompt(this.in, this.out);
        }

        public String getPassword() throws IOException {
            initPrompt();
            return this.prompt.getPassword("Password: ");
        }

        public String getUsername() throws IOException {
            initPrompt();
            return this.prompt.getInput("Username: ");
        }

    }
}
