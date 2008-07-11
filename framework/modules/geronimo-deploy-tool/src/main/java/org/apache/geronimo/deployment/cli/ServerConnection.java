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

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.Serializable;
import java.io.Writer;
import java.util.Properties;
import java.util.jar.JarFile;

import javax.enterprise.deploy.shared.factories.DeploymentFactoryManager;
import javax.enterprise.deploy.spi.DeploymentManager;
import javax.enterprise.deploy.spi.exceptions.DeploymentManagerCreationException;
import javax.enterprise.deploy.spi.factories.DeploymentFactory;

import org.apache.geronimo.cli.deployer.ConnectionParams;
import org.apache.geronimo.common.DeploymentException;
import org.apache.geronimo.deployment.plugin.factories.AuthenticationFailedException;
import org.apache.geronimo.deployment.plugin.jmx.JMXDeploymentManager;
import org.apache.geronimo.deployment.plugin.jmx.LocalDeploymentManager;
import org.apache.geronimo.kernel.Kernel;
import org.apache.geronimo.crypto.EncryptionManager;

/**
 * Supports online connections to the server, via JSR-88, valid only
 * when the server is online.
 *
 * @version $Rev$ $Date$
 */
public class ServerConnection {

    private final static String DEFAULT_URI = "deployer:geronimo:jmx";
    private final static String DEFAULT_SECURE_URI = "deployer:geronimo:jmxs";

    private final DeploymentFactory geronimoDeploymentFactory;

    private DeploymentManager manager;
    private Writer out;
    private InputStream in;
    private SavedAuthentication auth;
    private boolean logToSysErr;
    private boolean verboseMessages;

    public ServerConnection(ConnectionParams params, PrintWriter out, InputStream in, Kernel kernel, DeploymentFactory geronimoDeploymentFactory) throws DeploymentException {
        if (null == kernel) {
            throw new IllegalArgumentException("kernel is required");
        }
        this.geronimoDeploymentFactory = geronimoDeploymentFactory;

        this.out = out;
        this.in = in;        

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
            uri = getDefaultURI(secure) + "://" + (host == null ? "" : host) + (port == null ? "" : ":" + port);
        }
        if (offline) {
            startOfflineDeployer(kernel);
            manager = new LocalDeploymentManager(kernel);
        } else {
            tryToConnect(uri, driver, user, password, secure);
        }
        if (manager == null) {
            throw new DeploymentException("Unexpected error; connection failed.");
        }
    }

    protected void startOfflineDeployer(Kernel kernel) throws DeploymentException {
        OfflineDeployerStarter offlineDeployerStarter = new OfflineDeployerStarter(kernel);
        offlineDeployerStarter.start();
    }

    private static String getDefaultURI(boolean secure) {
        return (secure) ? DEFAULT_SECURE_URI : DEFAULT_URI;
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
        return auth.uri;
    }

    private void tryToConnect(String argURI, String driver, String user, String password, boolean secure) throws DeploymentException {
        DeploymentFactoryManager mgr = DeploymentFactoryManager.getInstance();
        if (driver != null) {
            loadDriver(driver, mgr);
        } else {
            mgr.registerDeploymentFactory(geronimoDeploymentFactory);
        }
        String useURI = argURI == null ? getDefaultURI(secure) : argURI;

        if (user == null && password == null) {
            InputStream in;
            // First check for .geronimo-deployer on class path (e.g. packaged in deployer.jar)
            in = ServerConnection.class.getResourceAsStream("/.geronimo-deployer");
            // If not there, check in home directory
            if (in == null) {
                File authFile = new File(System.getProperty("user.home"), ".geronimo-deployer");
                if (authFile.exists() && authFile.canRead()) {
                    try {
                        in = new BufferedInputStream(new FileInputStream(authFile));
                    } catch (FileNotFoundException e) {
                        // ignore
                    }
                }
            }
            if (in != null) {
                try {
                    Properties props = new Properties();
                    props.load(in);
                    String encrypted = props.getProperty("login." + useURI);
                    if (encrypted != null) {

                        if (encrypted.startsWith("{Plain}")) {
                            int pos = encrypted.indexOf("/");
                            user = encrypted.substring(7, pos);
                            password = encrypted.substring(pos + 1);
                        } else {
                            Object o = EncryptionManager.decrypt(encrypted);
                            if (o == encrypted) {
                                System.out.print(DeployUtils.reformat("Unknown encryption used in saved login file", 4, 72));
                            } else {
                                SavedAuthentication auth = (SavedAuthentication) o;
                                if (auth.uri.equals(useURI)) {
                                    user = auth.user;
                                    password = new String(auth.password);
                                }
                            }
                        }
                    }
                } catch (IOException e) {
                    System.out.print(DeployUtils.reformat("Unable to read authentication from saved login file: " + e.getMessage(), 4, 72));
                } finally {
                    try {
                        in.close();
                    } catch (IOException e) {
                        // ingore
                    }
                }
            }
        }

        if (user == null || password == null) {
            try {
                InputPrompt prompt = new InputPrompt(in, out);
                if (user == null) {
                    user = prompt.getInput("Username: ");
                }
                if (password == null) {
                    password = prompt.getPassword("Password: ");
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
        if (!file.exists() || !file.canRead() || !DeployUtils.isJarFile(file)) {
            throw new DeploymentSyntaxException("Driver '" + file.getAbsolutePath() + "' is not a readable JAR file");
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

    private final static class SavedAuthentication implements Serializable {
        private String uri;
        private String user;
        private char[] password;

        public SavedAuthentication(String uri, String user, char[] password) {
            this.uri = uri;
            this.user = user;
            this.password = password;
        }
    }
}
