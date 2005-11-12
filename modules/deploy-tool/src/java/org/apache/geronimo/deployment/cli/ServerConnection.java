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

import java.io.*;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.jar.JarFile;
import javax.enterprise.deploy.shared.factories.DeploymentFactoryManager;
import javax.enterprise.deploy.spi.DeploymentManager;
import javax.enterprise.deploy.spi.exceptions.DeploymentManagerCreationException;
import javax.enterprise.deploy.spi.factories.DeploymentFactory;
import javax.management.ObjectName;

import org.apache.geronimo.common.DeploymentException;
import org.apache.geronimo.deployment.plugin.factories.AuthenticationFailedException;
import org.apache.geronimo.deployment.plugin.factories.DeploymentFactoryImpl;
import org.apache.geronimo.deployment.plugin.jmx.JMXDeploymentManager;
import org.apache.geronimo.system.main.CommandLine;
import org.apache.geronimo.system.main.CommandLineManifest;
import org.apache.geronimo.util.SimpleEncryption;

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
                "to work.  If not specified, the deployer defaults to operating on a " +
                "Geronimo server running on the standard port on localhost.\n" +
                "A URI to connect to Geronimo (including optional host and port parameters) has the form: " +
                "deployer:geronimo:jmx:rmi:///jndi/rmi:[//host[:port]]/JMXConnector");
        OPTION_HELP.put("--offline", "Indicates that you don't want the deployer to try to connect to " +
                "a Geronimo server over the network.  If you're running on the same machine as the " +
                "Geronimo installation, using this option means that you're asserting that the" +
                "Geronimo server is not running.  WARNING: do not use this option if there's a Geronimo " +
                "server running out of the same Geronimo installation as the deploy tool is from --" +
                "the results may be unexpected.  Further, only a small number of commands may be" +
                "run in offline mode.");
        OPTION_HELP.put("--driver", "If you want to use this tool with a server other than Geronimo, " +
                "then you must provide the path to its driver JAR.  Currently, manifest " +
                "Class-Path entries in that JAR are ignored.");
        OPTION_HELP.put("--user", "If the deployment operation requires authentication, then you can " +
                "specify the username to use to connect.  If no password is specified, the " +
                "deployer will attempt to connect to the server with no password, and if " +
                "that fails, will prompt you for a password.");
        OPTION_HELP.put("--password", "Specifies a password to use to authenticate to the server.");
        OPTION_HELP.put("--syserr", "Enables error logging to syserr.  Disabled by default.");
        OPTION_HELP.put("--verbose", "Enables verbose execution mode.  Disabled by default.");
    }
    public static Map getOptionHelp() {
        return OPTION_HELP;
    }

    /**
     * Checks whether the stated command-line argument is a general argument (which
     * may be the general argument itself, or a required parameter after the general
     * argument).  For example, if the arguments were "--user bob --offline foo" then
     * this should return true for "--user" "bob" and "--offline" and false for "foo"
     * (since --offline does not expect a parameter).
     *
     * @param args The previous arguments on the command line
     * @param option The argument we're checking at the moment
     *
     * @return True if the argument we're checking is part of a general argument
     */
    public static boolean isGeneralOption(List args, String option) {
        if(OPTION_HELP.containsKey(option) || option.equals("--url")) {
            return true;
        }
        if(args.size() == 0) {
            return false;
        }
        String last = (String) args.get(args.size()-1);
        if(last.equals("--uri") || last.equals("--url") || last.equals("--driver") || last.equals("--user") ||
                last.equals("--password")) {
            return true;
        }
        return false;
    }

    private final static String DEFAULT_URI = "deployer:geronimo:jmx:rmi:///jndi/rmi://localhost:1099/JMXConnector";
    
    private DeploymentManager manager;
    private KernelWrapper kernel;
    private PrintWriter out;
    private BufferedReader in;
    private SavedAuthentication auth;

    public ServerConnection(String[] args, boolean forceLocal, PrintWriter out, BufferedReader in) throws DeploymentException {
        String uri = null, driver = null, user = null, password = null;
        boolean offline = false;
        JMXDeploymentManager.CommandContext commandContext = new JMXDeploymentManager.CommandContext();
        this.out = out;
        this.in = in;
        for(int i = 0; i < args.length; i++) {
            String arg = args[i];
            if(arg.equals("--uri") || arg.equals("--url")) {
                if(uri != null) {
                    throw new DeploymentSyntaxException("Cannot specify more than one URI");
                } else if(i >= args.length-1) {
                    throw new DeploymentSyntaxException("Must specify a URI (--uri deployer:...)");
                }
                if(offline) {
                    throw new DeploymentSyntaxException("Cannot specify a URI in offline mode");
                }
                uri = args[++i];
            } else if(arg.equals("--driver")) {
                if(driver != null) {
                    throw new DeploymentSyntaxException("Cannot specify more than one driver");
                } else if(i >= args.length-1) {
                    throw new DeploymentSyntaxException("Must specify a driver JAR (--driver jarfile)");
                }
                if(offline) {
                    throw new DeploymentSyntaxException("Cannot specify a driver in offline mode");
                }
                driver = args[++i];
            } else if(arg.equals("--offline")) {
                if(uri != null) {
                    throw new DeploymentSyntaxException("Cannot specify a URI in offline mode");
                }
                if(driver != null) {
                    throw new DeploymentSyntaxException("Cannot specify a driver in offline mode");
                }
                if(user != null) {
                    throw new DeploymentSyntaxException("Cannot specify a username in offline mode");
                }
                if(password != null) {
                    throw new DeploymentSyntaxException("Cannot specify a password in offline mode");
                }
                offline = true;
            } else if(arg.equals("--user")) {
                if(user != null) {
                    throw new DeploymentSyntaxException("Cannot specify more than one user name");
                } else if(i >= args.length-1) {
                    throw new DeploymentSyntaxException("Must specify a username (--user username)");
                }
                if(offline) {
                    throw new DeploymentSyntaxException("Cannot specify a username in offline mode");
                }
                user = args[++i];
            } else if(arg.equals("--password")) {
                if(password != null) {
                    throw new DeploymentSyntaxException("Cannot specify more than one password");
                } else if(i >= args.length-1) {
                    throw new DeploymentSyntaxException("Must specify a password (--password password)");
                }
                if(offline) {
                    throw new DeploymentSyntaxException("Cannot specify a password in offline mode");
                }
                password = args[++i];
            } else if (arg.equals("--verbose")) {
                commandContext.setVerbose(true);
            } else if (arg.equals("--syserr")) {
                commandContext.setLogErrors(true);
            } else {
                throw new DeploymentException("Invalid option "+arg);
            }
        }
        if((driver != null) && uri == null) {
            throw new DeploymentSyntaxException("A custom driver requires a custom URI");
        }
        if(forceLocal && !offline) {
            throw new DeploymentSyntaxException("This command may only be run offline.  Make sure the server is not running and use the --offline option.");
        }
        if(forceLocal && (uri != null || driver != null || user != null || password != null)) {
            throw new DeploymentSyntaxException("This command does not use normal server connectivity.  No standard options are allowed.");
        }
        if(forceLocal || offline) {
            initializeKernel();
        } else {
            tryToConnect(uri, commandContext, driver, user, password, true);
            if(manager == null) {
                throw new DeploymentException("Unexpected error; connection failed.");
            }
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

    Serializable getAuthentication() {
        return auth;
    }

    String getServerURI() {
        return auth.uri;
    }

    private void tryToConnect(String argURI, JMXDeploymentManager.CommandContext commandContext, String driver, String user, String password, boolean authPrompt) throws DeploymentException {
        DeploymentFactoryManager mgr = DeploymentFactoryManager.getInstance();
        if(driver != null) {
            loadDriver(driver, mgr);
        } else {
            mgr.registerDeploymentFactory(new DeploymentFactoryImpl());
        }
        String useURI = argURI == null ? DEFAULT_URI : argURI;

        if(authPrompt && user == null && password == null) {
            File authFile = new File(System.getProperty("user.home"), ".geronimo-deployer");
            if(authFile.exists() && authFile.canRead()) {
                try {
                    Properties props = new Properties();
                    InputStream in = new BufferedInputStream(new FileInputStream(authFile));
                    props.load(in);
                    in.close();
                    String encryped = props.getProperty("login."+useURI);
                    if(encryped != null) {
                        if(encryped.startsWith("{Standard}")) {
                            SavedAuthentication auth = (SavedAuthentication) SimpleEncryption.decrypt(encryped.substring(10));
                            if(auth.uri.equals(useURI)) {
                                user = auth.user;
                                password = new String(auth.password);
                            }
                        } else if(encryped.startsWith("{Plain}")) {
                            int pos = encryped.indexOf("/");
                            user = encryped.substring(7, pos);
                            password = encryped.substring(pos+1);
                        } else {
                            System.out.println(DeployUtils.reformat("Unknown encryption used in saved login file", 4, 72));
                        }
                    }
                } catch (IOException e) {
                    System.out.println(DeployUtils.reformat("Unable to read authentication from saved login file: "+e.getMessage(), 4, 72));
                }
            }
        }

        if(authPrompt && !useURI.equals(DEFAULT_URI) && user == null && password == null) {
            // Non-standard URI, but no authentication information
            doAuthPromptAndRetry(useURI, commandContext, user, password);
            return;
        } else { // Standard URI with no auth, Non-standard URI with auth, or else this is the 2nd try already
            try {
                manager = mgr.getDeploymentManager(useURI, user, password);
                auth = new SavedAuthentication(useURI, user, password.toCharArray());
            } catch(AuthenticationFailedException e) { // server's there, you just can't talk to it
                if(authPrompt) {
                    doAuthPromptAndRetry(useURI, commandContext, user, password);
                    return;
                } else {
                    throw new DeploymentException("Login Failed");
                }
            } catch(DeploymentManagerCreationException e) {
                throw new DeploymentException("Unable to connect to server at "+useURI+" -- "+e.getMessage());
            }
        }

        if (manager instanceof JMXDeploymentManager) {
            JMXDeploymentManager deploymentManager = (JMXDeploymentManager) manager;
            deploymentManager.setCommandContext(commandContext);
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

    private void doAuthPromptAndRetry(String uri, JMXDeploymentManager.CommandContext commandContext, String user, String password) throws DeploymentException {
        try {
            if(user == null) {
                out.print("Username: ");
                out.flush();
                user = in.readLine();
            }
            if(password == null) {
                password = new PasswordPrompt("Password: ", out).getPassword(in);
            }
        } catch(IOException e) {
            throw new DeploymentException("Unable to prompt for login", e);
        }
        tryToConnect(uri, commandContext, null, user, password, false);
    }

    public DeploymentManager getDeploymentManager() {
        return manager;
    }

    public boolean isOnline() {
        return manager != null;
    }

    public boolean isGeronimo() {
        return isOnline() && manager.getClass().getName().startsWith("org.apache.geronimo.");
    }

    public Object invokeOfflineDeployer(Object[] args, String[] argTypes) throws DeploymentException {
        if(kernel == null) {
            throw new IllegalStateException("Cannot attempt to package when no local kernel is available");
        }
        return kernel.invoke(args, argTypes);
    }

    private static class KernelWrapper extends CommandLine {
        private ObjectName mainGbean;
        private String mainMethod;
        private List configurations;

        public KernelWrapper() {
            CommandLineManifest entries = CommandLineManifest.getManifestEntries();
            configurations = entries.getConfigurations();
            mainGbean = entries.getMainGBean();
            mainMethod = entries.getMainMethod();
        }

        public Object invoke(Object[] args, String[] argTypes) throws DeploymentException {
            try {
                return getKernel().invoke(mainGbean, mainMethod, args, argTypes);
            } catch(Exception e) {
                throw new DeploymentException("Unable to connect to local deployer service", e);
            }
        }

        public void start() throws DeploymentException {
            try {
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

    /**
     * Prompts for and grabs a password, trying to suppress any console output
     * along the way.  Kind of heavy-handed, but we don't have access to any
     * platform-specific APIs that might make this nicer.
     */
    public static class PasswordPrompt implements Runnable {
        private volatile boolean done = false;
        private String prompt;
        private PrintWriter out;

        public PasswordPrompt(String prompt, PrintWriter out) {
            this.prompt = prompt;
            this.out = out;
        }

        /**
         * Don't call this directly.
         */
        public void run() {
            int priority = Thread.currentThread().getPriority();
            try {
                Thread.currentThread().setPriority(Thread.MAX_PRIORITY);
                String fullPrompt = "\r"+prompt+"          "+"\r"+prompt;
                StringBuffer clearline = new StringBuffer();
                clearline.append('\r');
                for(int i=prompt.length()+10; i>=0; i--) {
                    clearline.append(' ');
                }
                while(!done) {
                    out.print(fullPrompt);
                    out.flush();
                    Thread.sleep(1);
                }
                out.print(clearline.toString());
                out.flush();
                out.println();
                out.flush();
            } catch (InterruptedException e) {
            } finally {
                Thread.currentThread().setPriority(priority);
            }
            prompt = null;
            out = null;
        }

        /**
         * Displays the prompt, grabs the password, cleans up, and returns
         * the entered password.  For this to make sense, the input reader
         * here must be part of the same console as the output writer passed
         * to the constructor.
         *
         * For higher security, should return a char[], but that will just
         * be defeated by the JSR-88 call that takes a String anyway, so
         * why bother?
         */
        public String getPassword(BufferedReader in) throws IOException {
            Thread t = new Thread(this, "Password hiding thread");
            t.start();
            String password = in.readLine();
            done = true;
            try {
                t.join();
            } catch (InterruptedException e) {
            }
            return password;
        }
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
