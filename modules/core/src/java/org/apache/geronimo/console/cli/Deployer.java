/**
 *
 * Copyright 2004 The Apache Software Foundation
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
import java.util.jar.JarFile;
import javax.enterprise.deploy.shared.factories.DeploymentFactoryManager;
import javax.enterprise.deploy.spi.exceptions.DeploymentManagerCreationException;
import javax.enterprise.deploy.spi.Target;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.geronimo.console.cli.controller.TopLevel;
import org.apache.geronimo.console.cli.module.EJBJARInfo;
import org.apache.geronimo.console.cli.module.WARInfo;

/**
 * Initializes a command-line JSR-88 deployer.
 *
 * @version $Revision: 1.8 $ $Date: 2004/02/25 09:57:25 $
 */
public class Deployer {
    private static final Log log = LogFactory.getLog(Deployer.class);
    private final DeploymentContext context = new DeploymentContext();

    static {
        try {
            Class.forName("org.apache.geronimo.enterprise.deploy.server.GeronimoDeploymentFactory");
        } catch(ClassNotFoundException e) {
            log.error("Unable to load Geronimo JSR-88 implementation");
        }
    }

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
        context.out = out;
        context.in = in instanceof BufferedReader ? (BufferedReader)in : new BufferedReader(in);
        if(!connect()) {
            throw new IllegalStateException("Unable to connect to Deployment service");
        }
    }

    /**
     * Creates a new instance for the provided module file and input/output
     * streams.
     */
    public Deployer(File jarFile, PrintWriter out, Reader in) throws IllegalStateException, IllegalArgumentException {
        context.out = out;
        context.in = in instanceof BufferedReader ? (BufferedReader)in : new BufferedReader(in);
        if(jarFile.getName().endsWith(".jar")) {
            context.moduleInfo = new EJBJARInfo(context);
        } else if(jarFile.getName().endsWith(".war")) {
            context.moduleInfo = new WARInfo(context);
        } else {
            throw new IllegalArgumentException("Expecting file name to end in .jar or .war");
        }
        try {
            context.moduleInfo.file = jarFile;
            context.moduleInfo.jarFile = new JarFile(jarFile);
        } catch(IOException e) {
            throw new IllegalArgumentException(jarFile+" is not a valid JAR file!");
        }
        if(!connect() || !context.moduleInfo.initialize()) {
            throw new IllegalStateException("Unable to connect to Deployment service or prepare deployment information");
        }
    }

    /**
     * Enters the deployment user interface.  When this method returns, the
     * user has finished their deployment activities.
     */
    public void run() {
        new TopLevel(context).execute();
        context.deployer.release();
    }

    /**
     * Prompts the user to enter a Deployer URL and then gets a DeploymentManager
     * for that URL.
     *
     * @return <tt>true</tt> if the connection was successful.
     */
    private boolean connect() {
        context.out.println("\n\nEnter the deployer URL.  Leave blank for the default URL 'deployer:geronimo:'");
        context.out.print("URL: ");
        context.out.flush();
        try {
            String url = context.in.readLine();
            if(url.equals("")) {
                url = "deployer:geronimo:";
                context.connected = false;
            } else {
                context.connected = true; //todo: maybe prompt whether you should connect to the server?
            }
            context.deployer = DeploymentFactoryManager.getInstance().getDeploymentManager(url, null, null);
            Target[] targets = context.deployer.getTargets();
            if(targets.length == 1) {
                context.targets = targets;
            }
        } catch(DeploymentManagerCreationException e) {
            log.error("Can't create deployment manager",e);
            return false;
        } catch(IOException e) {
            log.error("Unable to read user input", e);
            return false;
        }

        return true;
    }

    /** XMLBeans Test
     try {
     // warm up
     InputStream in = new BufferedInputStream(new FileInputStream("modules/core/src/test-data/xml/deployment/simple-alt-ejb-jar.xml"));
     org.apache.geronimo.xbeans.j2ee.EjbJarDocument doc = org.apache.geronimo.xbeans.j2ee.EjbJarDocument.Factory.parse(in);
     in.close();
     //real
     in = new BufferedInputStream(new FileInputStream("modules/core/src/test-data/xml/deployment/simple-ejb-jar.xml"));
     XmlOptions opts = new XmlOptions();
     opts.setEntityResolver(new LocalEntityResolver());
     long before = System.currentTimeMillis();
     doc = org.apache.geronimo.xbeans.j2ee.EjbJarDocument.Factory.parse(in, opts);
     long after = System.currentTimeMillis();
     in.close();
     log.info("Read XML document in "+(after-before)+" ms");
     Writer out = new PrintWriter(new FileWriter("test.out"), true);
     before = System.currentTimeMillis();
     doc.save(out, opts);
     after = System.currentTimeMillis();
     out.flush();
     out.close();
     log.info("Wrote XML document in "+(after-before)+" ms");
     } catch(IOException e) {
     e.printStackTrace();
     } catch(org.apache.xmlbeans.XmlException e) {
     e.printStackTrace();
     }
     */
}
