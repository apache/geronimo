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
import java.io.IOException;
import java.util.jar.JarFile;

import javax.enterprise.deploy.shared.factories.DeploymentFactoryManager;
import javax.enterprise.deploy.spi.exceptions.DeploymentManagerCreationException;
import javax.enterprise.deploy.spi.factories.DeploymentFactory;

import org.apache.geronimo.cli.deployer.ConnectionParams;
import org.apache.geronimo.common.DeploymentException;
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

    private final ServerConnection.UsernamePasswordHandler handler;

    private boolean logToSysErr;

    private boolean verboseMessages;
    
    public OnlineServerConnection(ConnectionParams params, ConsoleReader consoleReader) throws DeploymentException {
        this(params, new DefaultUserPasswordHandler(consoleReader));
    }

    public OnlineServerConnection(ConnectionParams params, ServerConnection.UsernamePasswordHandler handler) throws DeploymentException {
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

        if (driver != null && uri == null) {
            throw new DeploymentSyntaxException("A custom driver requires a custom URI");
        }
        if (params.isOffline()) {
            throw new DeploymentException("Offline connection is not supported");
        }
        
        ClassLoader oldCL = Thread.currentThread().getContextClassLoader();
        Thread.currentThread().setContextClassLoader(DeployUtils.class.getClassLoader());
        try {
            tryToConnect(uri, driver, host, port, user, password, secure);
        } finally  {
            Thread.currentThread().setContextClassLoader(oldCL);
        }
        if (manager == null) {
            throw new DeploymentException("Unexpected error; connection failed.");
        }
        
    }

    private void tryToConnect(String uri, String driver, String host, Integer port, String user, String password, boolean secure) throws DeploymentException {
        DeploymentFactoryManager mgr = DeploymentFactoryManager.getInstance();
        if (driver != null) {
            loadDriver(driver, mgr);
        }
        
        if (host != null || port != null || uri == null) {
            uri = DeployUtils.getConnectionURI(host, port, secure);
        }
        
        if (user == null && password == null) {
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
        if (secure) {
            DeployUtils.setSecurityProperties();
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
            manager = mgr.getDeploymentManager(uri, user, password);
            auth = new SavedAuthentication(uri, user, password == null ? null : password.toCharArray());
        } catch (AuthenticationFailedException e) {
            // server's there, you just can't talk to it
            throw new DeploymentException("Login Failed");
        } catch (DeploymentManagerCreationException e) {
            throw new DeploymentException("Unable to connect to server at " + uri + " -- " + e.getMessage(), e);
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
