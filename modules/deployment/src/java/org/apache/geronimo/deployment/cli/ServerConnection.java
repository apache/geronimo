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

package org.apache.geronimo.deployment.cli;

import java.util.*;
import java.util.jar.JarFile;
import java.net.URI;
import java.io.BufferedReader;
import java.io.PrintWriter;
import java.io.IOException;
import java.io.File;
import javax.enterprise.deploy.spi.DeploymentManager;
import javax.enterprise.deploy.spi.exceptions.DeploymentManagerCreationException;
import javax.enterprise.deploy.spi.factories.DeploymentFactory;
import javax.enterprise.deploy.shared.factories.DeploymentFactoryManager;
import javax.management.ObjectName;
import javax.management.MalformedObjectNameException;
import org.apache.geronimo.deployment.DeploymentException;
import org.apache.geronimo.deployment.plugin.factories.DeploymentFactoryImpl;
import org.apache.geronimo.deployment.plugin.factories.AuthenticationFailedException;
import org.apache.geronimo.system.main.CommandLine;

/**
 * Supports two types of connections to the server.  One, via JSR-88, is valid
 * whenever the server is online, for any command except "package".  The other,
 * via direct Kernel invocation, is valid when the server is not running for
 * only the commands "distribute" and "package".
 *
 * @version $Rev: 53762 $ $Date: 2004-10-04 18:54:53 -0400 (Mon, 04 Oct 2004) $
 */
public class ServerConnection {
    private final static Map OPTION_HELP = new LinkedHashMap(4);
    static {
        OPTION_HELP.put("--uri", "A URI to contact the server.  The server must be running for this " +
                "to work.  If not specified, the deployer default to operating on a " +
                "Geronimo server running on the standard port on localhost, or if nothing " +
                "is available there, then the Geronimo server installation that the " +
                "deployer JAR is part of.\n" +
                "A URI to connect to Geronimo has the form: " +
                "geronimo:deployer:jmx:rmi://localhost/jndi/rmi:/JMXConnector");
        OPTION_HELP.put("--driver", "If you want to use this tool with a server other than Geronimo, " +
                "then you must provide the path to its driver JAR.  Currently, manifest " +
                "Class-Path entries in that JAR are ignored.");
        OPTION_HELP.put("--user", "If the deployment operation requires authentication, then you can " +
                "specify the username to use to connect.  If no password is specified, the " +
                "deployer will attempt to connect to the server with no password, and if " +
                "that fails, will prompt you for a password.");
        OPTION_HELP.put("--password", "Specifies a password to use to authenticate to the server.");
    }
    public static Map getOptionHelp() {
        return OPTION_HELP;
    }

    private final static String DEPLOYER_OBJECT_NAME="geronimo.deployment:role=Deployer,config=org/apache/geronimo/J2EEDeployer";
    private final static String DEFAULT_URI = "deployer:geronimo:jmx:rmi://localhost/jndi/rmi:/JMXConnector";

    private DeploymentManager manager;
    private KernelWrapper kernel;
    private PrintWriter out;
    private BufferedReader in;

    public ServerConnection(String[] args, boolean forceLocal, PrintWriter out, BufferedReader in) throws DeploymentException {
        String uri = null, driver = null, user = null, password = null;
        this.out = out;
        this.in = in;
        for(int i = 0; i < args.length; i++) {
            String arg = args[i];
            if(arg.equals("--uri")) {
                uri = args[++i];
            } else if(arg.equals("--driver")) {
                driver = args[++i];
            } else if(arg.equals("--user")) {
                user = args[++i];
            } else if(arg.equals("--password")) {
                password = args[++i];
            } else {
                throw new DeploymentException("Invalid option "+arg);
            }
        }
        if((driver != null) && uri == null) {
            throw new DeploymentSyntaxException("A custom driver requires a custom URI");
        }
        if(forceLocal && (uri != null || driver != null || user != null || password != null)) {
            throw new DeploymentSyntaxException("This command does not use normal server connectivity.  No standard options are allowed.");
        }
        if(!forceLocal) {
            tryToConnect(uri, driver, user, password, true);
            if(manager == null) { // uri must be null too or we'd have thrown an exception
                initializeKernel();
            }
        } else {
            initializeKernel();
        }
    }

    private void initializeKernel() throws DeploymentException {
        if(kernel != null) {
            throw new IllegalStateException("Kernel is already running!");
        }
        kernel = new KernelWrapper();
        kernel.start();
    }

    public void close() throws DeploymentException {
        if(manager != null) {
            manager.release();
        }
        if(kernel != null) {
            kernel.stop();
        }
    }

    private void tryToConnect(String uri, String driver, String user, String password, boolean authPrompt) throws DeploymentException {
        DeploymentFactoryManager mgr = DeploymentFactoryManager.getInstance();
        if(driver != null) {
            loadDriver(driver, mgr);
        } else {
            mgr.registerDeploymentFactory(new DeploymentFactoryImpl());
        }
        try {
            manager = mgr.getDeploymentManager(uri == null ? DEFAULT_URI : uri, user, password);
        } catch(AuthenticationFailedException e) { // server's there, you just can't talk to it
            if(authPrompt && (user == null || password == null)) {
                doAuthPromptAndRetry(uri, user, password);
            } else {
                throw new DeploymentException("Unable to connect to server: "+e.getMessage());
            }
        } catch(DeploymentManagerCreationException e) {
            if(uri != null) {
                throw new DeploymentException("Unable to connect to server at "+uri+" -- "+e.getMessage());
            } //else, fall through and try local access
        }
    }

    private void loadDriver(String driver, DeploymentFactoryManager mgr) throws DeploymentException {
        File file = new File(driver);
        if(!file.exists() || !file.canRead() || !DeployUtils.isJarFile(file)) {
            throw new DeploymentSyntaxException("Driver '"+file.getAbsolutePath()+"' is not a readable JAR file");
        }
        String className = null;
        try {
            JarFile jar = new JarFile(file);
            className = jar.getManifest().getMainAttributes().getValue("J2EE-DeploymentFactory-Implementation-Class");
            if(className == null) {
                throw new DeploymentException("The driver JAR "+file.getAbsolutePath()+" does not specify a J2EE-DeploymentFactory-Implementation-Class; cannot load driver.");
            }
            jar.close();
            DeploymentFactory factory = (DeploymentFactory) Class.forName(className).newInstance();
            mgr.registerDeploymentFactory(factory);
        } catch(DeploymentException e) {
            throw e;
        } catch(Exception e) {
            throw new DeploymentSyntaxException("Unable to load driver class "+className+" from JAR "+file.getAbsolutePath(), e);
        }
    }

    private void doAuthPromptAndRetry(String uri, String user, String password) throws DeploymentException {
        try {
            if(user == null) {
                out.print("Username: ");
                out.flush();
                user = in.readLine();
            }
            if(password == null) {
                out.print("Password: ");
                out.flush();
                password = in.readLine();
            }
        } catch(IOException e) {
            throw new DeploymentException("Unable to prompt for login", e);
        }
        tryToConnect(uri, null, user, password, false);
    }

    public DeploymentManager getDeploymentManager() {
        return manager;
    }

    public boolean isOnline() {
        return manager != null;
    }

    public Object invokeOfflineDeployer(String method, Object[] args, String[] argTypes) throws DeploymentException {
        if(kernel == null) {
            throw new IllegalStateException("Cannot attempt to package when no local kernel is available");
        }
        try {
            return kernel.invoke(new ObjectName(DEPLOYER_OBJECT_NAME), method, args, argTypes);
        } catch(MalformedObjectNameException e) {
            throw new DeploymentException("This should never happen", e);
        }
    }

    private static class KernelWrapper extends CommandLine {
        public Object invoke(ObjectName target, String method, Object[] args, String[] argTypes) throws DeploymentException {
            try {
                return getKernel().invoke(target, method, args, argTypes);
            } catch(Exception e) {
                throw new DeploymentException("Unable to connect to local deployer service", e);
            }
        }

        public void start() throws DeploymentException {
            //todo: load configuration list dynamically once the bootstrapper builds this deploy tool
            List configurations = new ArrayList();
            try {
                configurations.add(new URI("org/apache/geronimo/J2EEDeployer"));
                super.startKernel(configurations);
            } catch(Exception e) {
                throw new DeploymentException("Unable to start local kernel", e);
            }
        }

        public void stop() throws DeploymentException {
            try {
                super.stopKernel();
            } catch(Exception e) {
                throw new DeploymentException("Unable to stop local kernel", e);
            }
        }
    }
}
