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

package org.apache.geronimo.console.cli;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.Reader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.jar.Attributes;
import java.util.jar.JarFile;
import java.util.jar.Manifest;

import javax.enterprise.deploy.shared.factories.DeploymentFactoryManager;
import javax.enterprise.deploy.spi.factories.DeploymentFactory;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.geronimo.console.cli.controller.TopLevel;

/**
 * Initializes a command-line JSR-88 deployer.
 *
 * @version $Rev$ $Date$
 */
public class Deployer {

    private static final Log log = LogFactory.getLog(Deployer.class);

    private final DeploymentContext context = new DeploymentContext();

    /**
     * Entry point of a potential executable jar.
     * 
     * @param args Array of file names. These files must be .jar files defining
     * DeploymentFactories as per the JSR 88 requirements.
     */
    public static void main(String[] args) {
       if ( 0 == args.length ) {
           System.err.println("DeploymentFactory archives not specified.");
           System.exit(1);
       }
       
       for (int i = 0; i < args.length; i++) {
            File file = new File(args[i]);
            if (!file.isFile()) {
                System.err.println(args[i] + " does not exist.");
                System.exit(2);
            }
            registerDeploymentFactory(file);
        }
       
       Deployer deployer = new Deployer();
       deployer.run();
    }
    
    private static void registerDeploymentFactory(File anArchive) {
        String clazzName = null;
        try {
            JarFile jarFile = new JarFile(anArchive);
            Manifest manifest = jarFile.getManifest();
            Attributes attributes = manifest.getMainAttributes();
            clazzName = attributes.getValue(
                "J2EE-DeploymentFactory-Implementation-Class");
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Can not retrieve DeploymentManagerFactory");
            System.exit(3);
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
            System.err.println("No J2EE-DeploymentFactory-Implementation-Class.");
            System.exit(4);
        }
        
        try {
            URLClassLoader cl = new URLClassLoader(new URL[]{anArchive.toURL()});
            Class clazz = cl.loadClass(clazzName);
            DeploymentFactoryManager.getInstance().
                registerDeploymentFactory(
                    (DeploymentFactory) clazz.newInstance());
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            System.err.println("Can not load " + clazzName);
            System.exit(5);
        } catch (InstantiationException e) {
            e.printStackTrace();
            System.err.println("Can not create " + clazzName);
            System.exit(5);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
            System.err.println("Can not create " + clazzName);
            System.exit(5);
        } catch (MalformedURLException e) {
            throw new AssertionError();
        }
    }
    
    /**
     * Creates a new instance using System.out and System.in to interact with
     * the user.  The user will need to begin by selecting an EJB JAR file
     * to work with.
     */
    public Deployer() throws IllegalStateException, IllegalArgumentException {
        this(new PrintWriter(new OutputStreamWriter(System.out), true), new BufferedReader(new InputStreamReader(System.in)));
    }

    /**
     * Creates a new instance using the provided input/output streams to
     * interact with the user.  The user will need to begin by selecting an EJB
     * JAR file to work with.
     */
    public Deployer(PrintWriter out, Reader in) throws IllegalStateException, IllegalArgumentException {
        context.out = out;
        context.in = in instanceof BufferedReader ? (BufferedReader)in : new BufferedReader(in);
    }

    /**
     * Enters the deployment user interface.  When this method returns, the
     * user has finished their deployment activities.
     */
    public void run() {
        new TopLevel(context).execute();
        context.deployer.release();
    }

}
