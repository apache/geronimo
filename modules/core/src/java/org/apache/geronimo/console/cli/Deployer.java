/* ====================================================================
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 2003 The Apache Software Foundation.  All rights
 * reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. The end-user documentation included with the redistribution,
 *    if any, must include the following acknowledgment:
 *       "This product includes software developed by the
 *        Apache Software Foundation (http://www.apache.org/)."
 *    Alternately, this acknowledgment may appear in the software itself,
 *    if and wherever such third-party acknowledgments normally appear.
 *
 * 4. The names "Apache" and "Apache Software Foundation" and
 *    "Apache Geronimo" must not be used to endorse or promote products
 *    derived from this software without prior written permission. For
 *    written permission, please contact apache@apache.org.
 *
 * 5. Products derived from this software may not be called "Apache",
 *    "Apache Geronimo", nor may "Apache" appear in their name, without
 *    prior written permission of the Apache Software Foundation.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE APACHE SOFTWARE FOUNDATION OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 *
 * ====================================================================
 */
package org.apache.geronimo.console.cli;

import java.util.jar.JarFile;
import java.io.*;
import java.net.URLClassLoader;
import java.net.URL;
import java.net.MalformedURLException;
import javax.enterprise.deploy.spi.*;
import javax.enterprise.deploy.spi.exceptions.DeploymentManagerCreationException;
import javax.enterprise.deploy.spi.exceptions.InvalidModuleException;
import javax.enterprise.deploy.spi.exceptions.ConfigurationException;
import javax.enterprise.deploy.shared.factories.DeploymentFactoryManager;
import javax.enterprise.deploy.model.*;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.geronimo.enterprise.deploy.tool.EjbDeployableObject;
import org.apache.geronimo.enterprise.deploy.tool.WebDeployableObject;

/**
 * Initializes a command-line JSR-88 deployer.
 *
 * @version $Revision: 1.6 $ $Date: 2003/10/07 17:16:35 $
 */
public class Deployer {
    private static final Log log = LogFactory.getLog(Deployer.class);
    static {
        try {
            Class.forName("org.apache.geronimo.enterprise.deploy.server.GeronimoDeploymentFactory");
        } catch(ClassNotFoundException e) {
            log.error("Unable to load Geronimo JSR-88 implementation");
        }
    }

    private DeploymentManager deployer;
    private DeployableObject standardModule;
    private DeploymentConfiguration serverModule;
    private PrintWriter out;
    private BufferedReader in;
    private ModuleInfo moduleInfo;
    private File saveDir = new File(System.getProperty("user.dir"));

    /**
     * Creates a new instance using System.out and System.in to interact with
     * the user.  The user will need ot begin by selecting an EJB JAR file
     * to work with.
     */
    public Deployer() throws IllegalStateException, IllegalArgumentException {
        this(new PrintWriter(new OutputStreamWriter(System.out), true), new BufferedReader(new InputStreamReader(System.in)));
    }

    /**
     * Creates a new instance for the provided EJB JAR file using System.out
     * and System.in to interact with the user.
     */
    public Deployer(File jarFile) throws IllegalStateException, IllegalArgumentException {
        this(jarFile, new PrintWriter(new OutputStreamWriter(System.out), true), new BufferedReader(new InputStreamReader(System.in)));
    }

    /**
     * Creates a new instance using the provided input/output streams to
     * interact with the user.  The user will need ot begin by selecting an EJB
     * JAR file to work with.
     */
    public Deployer(PrintWriter out, Reader in) throws IllegalStateException, IllegalArgumentException {
        this.out = out;
        this.in = in instanceof BufferedReader ? (BufferedReader)in : new BufferedReader(in);
        if(!connect()) {
            throw new IllegalStateException("Unable to connect to Deployment service");
        }
    }

    /**
     * Creates a new instance for the provided module file and input/output
     * streams.
     */
    public Deployer(File jarFile, PrintWriter out, Reader in) throws IllegalStateException, IllegalArgumentException {
        this.out = out;
        this.in = in instanceof BufferedReader ? (BufferedReader)in : new BufferedReader(in);
        if(jarFile.getName().endsWith(".jar")) {
            moduleInfo = new EjbJarInfo();
        } else if(jarFile.getName().endsWith(".war")) {
            moduleInfo = new WarInfo();
        } else {
            throw new IllegalArgumentException("Expecting file name to end in .jar or .war");
        }
        try {
            moduleInfo.file = jarFile;
            moduleInfo.jarFile = new JarFile(jarFile);
        } catch(IOException e) {
            throw new IllegalArgumentException(jarFile+" is not a valid JAR file!");
        }
        if(!connect() || !moduleInfo.initialize()) {
            throw new IllegalStateException("Unable to connect to Deployment service or prepare deployment information");
        }
    }

    /**
     * Enters the deployment user interface.  When this method returns, the
     * user has finished their deployment activities.
     */
    public void run() {
        workWithoutModule();
        deployer.release();
    }

    /**
     * Prompts the user to enter a Deployer URL and then gets a DeploymentManager
     * for that URL.
     *
     * @return <tt>true</tt> if the connection was successful.
     */
    private boolean connect() {
        out.println("\n\nEnter the deployer URL.  Leave blank for the default URL 'deployer:geronimo:'");
        out.print("URL: ");
        out.flush();
        try {
            String url = in.readLine();
            if(url.equals("")) {
                url = "deployer:geronimo:";
            }
            deployer = DeploymentFactoryManager.getInstance().getDeploymentManager(url, null, null);
        } catch(DeploymentManagerCreationException e) {
            log.error("Can't create deployment manager",e);
            return false;
        } catch(IOException e) {
            log.error("Unable to read user input", e);
            return false;
        }
        return true;
    }

    /**
     * Loads the deployment descriptor information from the specific EJB JAR
     * file.
     *
     * @return <tt>true</tt> if the deployment information was loaded
     *         successfully.
     */
    private boolean initializeEjbJar() {
        if(!(moduleInfo instanceof EjbJarInfo)) {
            throw new IllegalStateException("Tried to load an EJB JAR but the current module is "+moduleInfo.getClass().getName());
        }
        EjbJarInfo jarInfo = (EjbJarInfo)moduleInfo;
        try {
            ClassLoader loader = new URLClassLoader(new URL[]{jarInfo.file.toURL()}, ClassLoader.getSystemClassLoader());
            standardModule = new EjbDeployableObject(jarInfo.jarFile, loader);
        } catch(MalformedURLException e) {
            out.println("ERROR: "+jarInfo.file+" is not a valid JAR file!");
            return false;
        }
        try {
            serverModule = deployer.createConfiguration(standardModule);
        } catch(InvalidModuleException e) {
            out.println("ERROR: Unable to initialize a Geronimo DD for EJB JAR "+jarInfo.file);
            return false;
        }
        jarInfo.ejbJar = standardModule.getDDBeanRoot();
        jarInfo.editingEjbJar = true;
        try {
            jarInfo.ejbJarConfig = serverModule.getDConfigBeanRoot(jarInfo.ejbJar);
            initializeDConfigBean(jarInfo.ejbJarConfig);
        } catch(ConfigurationException e) {
            log.error("Unable to initialize server-specific deployment information", e);
            return false;
        }
        return true;
    }

    /**
     * Loads the deployment descriptor information from the specific WAR
     * file.
     *
     * @return <tt>true</tt> if the deployment information was loaded
     *         successfully.
     */
    private boolean initializeWar() {
        if(!(moduleInfo instanceof WarInfo)) {
            throw new IllegalStateException("Tried to load a WAR but the current module is "+moduleInfo.getClass().getName());
        }
        WarInfo warInfo = (WarInfo)moduleInfo;
        try {
            ClassLoader loader = new URLClassLoader(new URL[]{warInfo.file.toURL()}, ClassLoader.getSystemClassLoader());
            standardModule = new WebDeployableObject(warInfo.jarFile, loader);
        } catch(MalformedURLException e) {
            out.println("ERROR: "+warInfo.file+" is not a valid JAR file!");
            return false;
        }
        try {
            serverModule = deployer.createConfiguration(standardModule);
        } catch(InvalidModuleException e) {
            out.println("ERROR: Unable to initialize a Geronimo DD for WAR "+warInfo.file);
            return false;
        }
        warInfo.war = standardModule.getDDBeanRoot();
        try {
            warInfo.warConfig = serverModule.getDConfigBeanRoot(warInfo.war);
            initializeDConfigBean(warInfo.warConfig);
        } catch(ConfigurationException e) {
            log.error("Unable to initialize server-specific deployment information", e);
            return false;
        }
        return true;
    }

    /**
     * Presents a user interface to let the user take high-level deployment
     * actions.  This lets them do the things you do without reference to a
     * particular J2EE module.
     */
    private void workWithoutModule() {
        while(true) {
            if(moduleInfo instanceof EjbJarInfo) {
                workWithEjbJar();
                continue;
            } else if(moduleInfo instanceof WarInfo) {
                workWithWar();
                continue;
            }
            out.println("\n\nNo J2EE module is currently selected.");
            out.println("  -- Select one or more servers or clusters to work with"); // DM.getTargets()
            out.println("  -- Start non-running modules on the selected servers/clusters");
            out.println("  -- Stop running modules on the selected servers/clusters");
            out.println("  -- Undeploy modules from the selected servers/clusters");
            out.println("  -- View modules on the selected servers/clusters");
            out.println("  6) Select an EJB JAR or WAR to configure, deploy, or redeploy"); //todo: change text when other modules are supported
            out.println("  7) Disconnect from any servers.");
            String choice;
            while(true) {
                out.print("Action ([6-7] or [Q]uit): ");
                out.flush();
                try {
                    choice = in.readLine().trim().toLowerCase();
                } catch(IOException e) {
                    log.error("Unable to read user input", e);
                    return;
                }
                if(choice.equals("6")) {
                    selectModule();
                    break;
                } else if(choice.equals("7")) {
                    deployer.release();
                    out.println("Released any server resources and disconnected.");
                    break;
                } else if(choice.equals("q")) {
                    return;
                }
            }
        }
    }

    /**
     * Prompts the user to select a J2EE module to work with.
     *
     * Currently handles EJB JAR modules and WAR modules only.
     */
    private void selectModule() {
        out.println("\nCurrent directory is "+saveDir);
        out.println("Select an EJB JAR or WAR file to load.");
        String choice;
        File file;
        while(true) {
            out.print("File Name: ");
            out.flush();
            try {
                choice = in.readLine().trim();
            } catch(IOException e) {
                log.error("Unable to read user input", e);
                return;
            }
            file = new File(saveDir, choice);
            if(!file.canRead() || file.isDirectory()) {
                out.println("ERROR: cannot read from this file.  Please try again.");
                continue;
            }
            saveDir = file.getParentFile();
            break;
        }

        if(file.getName().endsWith(".jar")) {
            moduleInfo = new EjbJarInfo();
        } else if(file.getName().endsWith(".war")) {
            moduleInfo = new WarInfo();
        } else {
            out.println("ERROR: Expecting file name to end in .jar or .war");
        }
        try {
            moduleInfo.file = file;
            moduleInfo.jarFile = new JarFile(file);
        } catch(IOException e) {
            out.println("ERROR: "+file+" is not a valid JAR file!");
            moduleInfo = null;
            return;
        }
        if(!moduleInfo.initialize()) {
            moduleInfo = null;
            return;
        }
    }

    /**
     * Presents a user interface for a user to work with an EJB JAR.
     */
    private void workWithEjbJar() {
        while(true) {
            out.println("\n\nLoaded an EJB JAR.  Working with the "+moduleInfo.getFileName()+" deployment descriptor.");
            out.println("  -- Edit the standard EJB deployment descriptor ("+moduleInfo.getFileName()+")");
            out.println("  2) Edit the corresponding server-specific deployment information");
            out.println("  3) Load a saved set of server-specific deployment information");
            out.println("  4) Save the current set of server-specific deployment information");
            out.println("  -- Edit web services deployment information");
            out.println("  -- Deploy or redeploy the JAR into the application server");
            out.println("  7) Select a new EJB JAR or WAR to work with"); //todo: adjust text when other modules are accepted
            out.println("  8) Manage existing deployments in the server");
            String choice;
            while(true) {
                out.print("Action ([2-4,7,8] or [Q]uit): ");
                out.flush();
                try {
                    choice = in.readLine().trim().toLowerCase();
                } catch(IOException e) {
                    log.error("Unable to read user input", e);
                    return;
                }
                if(choice.equals("2")) {
                    editServerSpecificDD();
                    break;
                } else if(choice.equals("3")) {
                    loadServerSpecificDD();
                    break;
                } else if(choice.equals("4")) {
                    saveServerSpecificDD();
                    break;
                } else if(choice.equals("7")) {
                    selectModule();
                    return;
                } else if(choice.equals("8")) { //todo: prompt to save if modifications were made
                    moduleInfo = null;
                    return;
                } else if(choice.equals("q")) {
                    moduleInfo = null;
                    return;
                }
            }
        }
    }

    /**
     * Presents a user interface for a user to work with an EJB JAR.
     */
    private void workWithWar() {
        while(true) {
            out.println("\n\nLoaded a WAR.  Working with the "+moduleInfo.getFileName()+" deployment descriptor.");
            out.println("  -- Edit the standard Web App deployment descriptor ("+moduleInfo.getFileName()+")");
            out.println("  2) Edit the corresponding server-specific deployment information");
            out.println("  3) Load a saved set of server-specific deployment information");
            out.println("  4) Save the current set of server-specific deployment information");
            out.println("  -- Deploy or redeploy the JAR into the application server");
            out.println("  6) Select a new EJB JAR or WAR to work with"); //todo: adjust text when other modules are accepted
            out.println("  7) Manage existing deployments in the server");
            String choice;
            while(true) {
                out.print("Action ([2-4,7,8] or [Q]uit): ");
                out.flush();
                try {
                    choice = in.readLine().trim().toLowerCase();
                } catch(IOException e) {
                    log.error("Unable to read user input", e);
                    return;
                }
                if(choice.equals("2")) {
                    editServerSpecificDD();
                    break;
                } else if(choice.equals("3")) {
                    loadServerSpecificDD();
                    break;
                } else if(choice.equals("4")) {
                    saveServerSpecificDD();
                    break;
                } else if(choice.equals("6")) {
                    selectModule();
                    return;
                } else if(choice.equals("7")) { //todo: prompt to save if modifications were made
                    moduleInfo = null;
                    return;
                } else if(choice.equals("q")) {
                    moduleInfo = null;
                    return;
                }
            }
        }
    }

    /**
     * Loads the server-specific deployment information from a file on disk.
     * Note that in JSR-88, server-specific DDs are not saved in the
     * JAR/EAR/whatever.
     */
    private void loadServerSpecificDD() {
        out.println("\nCurrent directory is "+saveDir);
        out.println("Select a file name.  The server-specific deployment information for the ");
        out.println(moduleInfo.getFileName()+" will be loaded from the file you specify.");
        String choice;
        while(true) {
            out.print("File Name: ");
            out.flush();
            try {
                choice = in.readLine().trim();
            } catch(IOException e) {
                log.error("Unable to read user input", e);
                return;
            }
            File file = new File(saveDir, choice);
            if(!file.canRead() || file.isDirectory()) {
                out.println("ERROR: cannot read from this file.  Please try again.");
                continue;
            }
            saveDir = file.getParentFile();
            try {
                moduleInfo.loadDConfigBean(file);
                out.println("Deployment information loaded from "+file.getName());
                return;
            } catch(IOException e) {
                log.error("Unable to read from file", e);
                return;
            } catch(ConfigurationException e) {
                out.println("ERROR: "+e.getMessage());
                if(e.getCause() != null) {
                    e.printStackTrace(out);
                }
                return;
            }
        }
    }

    /**
     * Saves the server-specific deployment information to a file on disk.
     * Note that in JSR-88, server-specific DDs are not saved in the
     * JAR/EAR/whatever.
     */
    private void saveServerSpecificDD() {
        out.println("\nCurrent directory is "+saveDir);
        out.println("Select a file name.  The server-specific deployment information for the ");
        out.println(moduleInfo.getFileName()+" will be saved to the file you specify.");
        String choice;
        try {
            while(true) {
                out.print("File Name: ");
                out.flush();
                    choice = in.readLine().trim();
                File file = new File(saveDir, choice);
                if((file.exists() && !file.canWrite()) || (!file.exists() && !file.getParentFile().canWrite()) || file.isDirectory()) {
                    out.println("ERROR: cannot write to this file.  Please try again.");
                    continue;
                }
                if(file.exists()) {
                    out.print("File already exists.  Overwrite (Y/N)? ");
                    out.flush();
                    choice = in.readLine().trim().toLowerCase();
                    if(choice.equals("n")) { // todo: make sure they entered y or n
                        continue;
                    }
                }
                saveDir = file.getParentFile();
                try {
                    moduleInfo.saveDConfigBean(file);
                    out.println("Deployment information saved to "+file.getName());
                    return;
                } catch(IOException e) {
                    log.error("Unable to write to file", e);
                    return;
                } catch(ConfigurationException e) {
                    out.println("ERROR: "+e.getMessage());
                    return;
                }
            }
        } catch(IOException e) {
            log.error("Unable to read user input", e);
            return;
        }
    }

    /**
     * Marches recursively through the DConfigBean tree to initialize
     * DConfigBeans for all the interesting DDBeans.  Once this is done, and
     * DDBean changes need to be relayed to the DConfigBeans that listn on them.
     */
    private void initializeDConfigBean(DConfigBean dcb) throws ConfigurationException {
        String[] xpaths = dcb.getXpaths();
        for(int i=0; i<xpaths.length; i++) {
            DDBean[] ddbs = dcb.getDDBean().getChildBean(xpaths[i]);
            for(int j = 0; j < ddbs.length; j++) {
                initializeDConfigBean(dcb.getDConfigBean(ddbs[j]));
            }
        }
    }

    /**
     * Hands over control to {@link DConfigBeanConfigurator} to let the user edit
     * the server-specific deployment information.
     */
    private void editServerSpecificDD() {
        new DConfigBeanConfigurator(moduleInfo.getConfigRoot(), out, in).configure();
    }

    private abstract class ModuleInfo {
        public File file;
        public JarFile jarFile;
        public abstract boolean initialize();
        public abstract String getFileName();
        public abstract DConfigBeanRoot getConfigRoot();
        public abstract void loadDConfigBean(File source) throws IOException, ConfigurationException;
        public abstract void saveDConfigBean(File dest) throws IOException, ConfigurationException;
    }

    /**
     * Holds all the relevent data for an EJB JAR.
     */
    private class EjbJarInfo extends ModuleInfo {
        public DDBeanRoot ejbJar;
        public DConfigBeanRoot ejbJarConfig;
        public DDBeanRoot webServices;
        public DConfigBeanRoot webServicesConfig;
        public boolean editingEjbJar;

        public boolean initialize() {
            return initializeEjbJar();
        }

        public String getFileName() {
            return editingEjbJar ? "ejb-jar.xml" : "Web Services DD";
        }

        public DConfigBeanRoot getConfigRoot() {
            return editingEjbJar ? ejbJarConfig : webServicesConfig;
        }

        public void loadDConfigBean(File source) throws IOException, ConfigurationException {
            BufferedInputStream fin = new BufferedInputStream(new FileInputStream(source));
            DConfigBeanRoot root = serverModule.restoreDConfigBean(fin, editingEjbJar ? ejbJar : webServices);
            fin.close();
            if(editingEjbJar) {
                ejbJarConfig = root;
            } else {
                webServicesConfig = root;
            }
        }

        public void saveDConfigBean(File dest) throws IOException, ConfigurationException {
            BufferedOutputStream fout = new BufferedOutputStream(new FileOutputStream(dest));
            serverModule.saveDConfigBean(fout, editingEjbJar ? ejbJarConfig : webServicesConfig);
            fout.close();
        }
    }
    /**
     * Holds all the relevent data for a WAR.
     */
    private class WarInfo extends ModuleInfo {
        public DDBeanRoot war;
        public DConfigBeanRoot warConfig;

        public boolean initialize() {
            return initializeWar();
        }

        public String getFileName() {
            return "web.xml";
        }

        public DConfigBeanRoot getConfigRoot() {
            return warConfig;
        }

        public void loadDConfigBean(File source) throws IOException, ConfigurationException {
            BufferedInputStream fin = new BufferedInputStream(new FileInputStream(source));
            DConfigBeanRoot root = serverModule.restoreDConfigBean(fin, war);
            fin.close();
            warConfig = root;
        }

        public void saveDConfigBean(File dest) throws IOException, ConfigurationException {
            BufferedOutputStream fout = new BufferedOutputStream(new FileOutputStream(dest));
            serverModule.saveDConfigBean(fout, warConfig);
            fout.close();
        }
    }
}
