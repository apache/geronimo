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

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.jar.JarFile;
import javax.enterprise.deploy.shared.factories.DeploymentFactoryManager;
import javax.enterprise.deploy.spi.DeploymentManager;
import javax.enterprise.deploy.spi.exceptions.DeploymentManagerCreationException;
import javax.enterprise.deploy.spi.factories.DeploymentFactory;

import org.apache.geronimo.common.DeploymentException;
import org.apache.geronimo.deployment.plugin.factories.AuthenticationFailedException;
import org.apache.geronimo.deployment.plugin.factories.DeploymentFactoryImpl;
import org.apache.geronimo.deployment.plugin.jmx.JMXDeploymentManager;
import org.apache.geronimo.util.SimpleEncryption;

/**
 * Supports online connections to the server, via JSR-88, valid only
 * when the server is online.
 *
 * @version $Rev$ $Date$
 */
public class ServerConnection {
    private final static Map OPTION_HELP = new LinkedHashMap(9);
    static {
        OPTION_HELP.put("--uri", "A URI to contact the server.  If not specified, the deployer defaults to " +
                "operating on a Geronimo server running on the standard port on localhost.\n" +
                "A URI to connect to Geronimo (including optional host and port parameters) has the form: " +
                "deployer:geronimo:jmx[://host[:port]] (though you could also just use --host and --port instead).");
        OPTION_HELP.put("--host", "The host name of a Geronimo server to deploy to.  This option is " +
                "not compatible with --uri, but is often used with --port.");
        OPTION_HELP.put("--port", "The RMI listen port of a Geronimo server to deploy to.  This option is " +
                "not compatible with --uri, but is often used with --host.  The default port is 1099.");
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
     * argument).  For example, if the arguments were "--user bob foo" then
     * this should return true for "--user" and "bob" and false for "foo".
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
        return last.equals("--uri") || last.equals("--url") || last.equals("--driver") || last.equals("--user") ||
                last.equals("--password") || last.equals("--host") || last.equals("--port");
    }

    private final static String DEFAULT_URI = "deployer:geronimo:jmx";

    private DeploymentManager manager;
    private PrintWriter out;
    private BufferedReader in;
    private SavedAuthentication auth;
    private boolean logToSysErr;
    private boolean verboseMessages;

    public ServerConnection(String[] args, PrintWriter out, BufferedReader in) throws DeploymentException {
        String uri = null, driver = null, user = null, password = null, host = null;
        Integer port = null;
        this.out = out;
        this.in = in;
        for(int i = 0; i < args.length; i++) {
            String arg = args[i];
            if(arg.equals("--uri") || arg.equals("--url")) {
                if(uri != null) {
                    throw new DeploymentSyntaxException("Cannot specify more than one URI");
                } else if(i >= args.length-1) {
                    throw new DeploymentSyntaxException("Must specify a URI (e.g. --uri deployer:...)");
                }
                if(host != null || port != null) {
                    throw new DeploymentSyntaxException("Cannot specify a URI as well as a host/port");
                }
                uri = args[++i];
            } else if(arg.equals("--host")) {
                if(host != null) {
                    throw new DeploymentSyntaxException("Cannot specify more than one host");
                } else if(i >= args.length-1) {
                    throw new DeploymentSyntaxException("Must specify a hostname (e.g. --host localhost)");
                }
                if(uri != null) {
                    throw new DeploymentSyntaxException("Cannot specify a URI as well as a host/port");
                }
                host = args[++i];
            } else if(arg.equals("--port")) {
                if(port != null) {
                    throw new DeploymentSyntaxException("Cannot specify more than one port");
                } else if(i >= args.length-1) {
                    throw new DeploymentSyntaxException("Must specify a port (e.g. --port 1099)");
                }
                if(uri != null) {
                    throw new DeploymentSyntaxException("Cannot specify a URI as well as a host/port");
                }
                try {
                    port = new Integer(args[++i]);
                } catch (NumberFormatException e) {
                    throw new DeploymentSyntaxException("Port must be a number ("+e.getMessage()+")");
                }
            } else if(arg.equals("--driver")) {
                if(driver != null) {
                    throw new DeploymentSyntaxException("Cannot specify more than one driver");
                } else if(i >= args.length-1) {
                    throw new DeploymentSyntaxException("Must specify a driver JAR (--driver jarfile)");
                }
                driver = args[++i];
            } else if(arg.equals("--offline")) {
                throw new DeploymentSyntaxException("This tool no longer handles offline deployment");
            } else if(arg.equals("--user")) {
                if(user != null) {
                    throw new DeploymentSyntaxException("Cannot specify more than one user name");
                } else if(i >= args.length-1) {
                    throw new DeploymentSyntaxException("Must specify a username (--user username)");
                }
                user = args[++i];
            } else if(arg.equals("--password")) {
                if(password != null) {
                    throw new DeploymentSyntaxException("Cannot specify more than one password");
                } else if(i >= args.length-1) {
                    throw new DeploymentSyntaxException("Must specify a password (--password password)");
                }
                password = args[++i];
            } else if (arg.equals("--verbose")) {
                verboseMessages = true;
            } else if (arg.equals("--syserr")) {
                logToSysErr = true;
            } else {
                throw new DeploymentException("Invalid option "+arg);
            }
        }
        if((driver != null) && uri == null) {
            throw new DeploymentSyntaxException("A custom driver requires a custom URI");
        }
        if(host != null || port != null) {
            uri = DEFAULT_URI+"://"+(host == null ? "" : host)+(port == null ? "" : ":"+port);
        }
        tryToConnect(uri, driver, user, password, true);
        if(manager == null) {
            throw new DeploymentException("Unexpected error; connection failed.");
        }
    }

    public void close() throws DeploymentException {
        if(manager != null) {
            manager.release();
        }
    }

    Serializable getAuthentication() {
        return auth;
    }

    String getServerURI() {
        return auth.uri;
    }

    private void tryToConnect(String argURI, String driver, String user, String password, boolean authPrompt) throws DeploymentException {
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
            doAuthPromptAndRetry(useURI, user, password);
            return;
        } else { // Standard URI with no auth, Non-standard URI with auth, or else this is the 2nd try already
            try {
                manager = mgr.getDeploymentManager(useURI, user, password);
                auth = new SavedAuthentication(useURI, user, password == null ? null : password.toCharArray());
            } catch(AuthenticationFailedException e) { // server's there, you just can't talk to it
                if(authPrompt) {
                    doAuthPromptAndRetry(useURI, user, password);
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
            deploymentManager.setLogConfiguration(logToSysErr, verboseMessages);
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
                password = new PasswordPrompt("Password: ", out).getPassword(in);
            }
        } catch(IOException e) {
            throw new DeploymentException("Unable to prompt for login", e);
        }
        tryToConnect(uri, null, user, password, false);
    }

    public DeploymentManager getDeploymentManager() {
        return manager;
    }

    public boolean isGeronimo() {
        return manager.getClass().getName().startsWith("org.apache.geronimo.");
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
