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
package org.apache.geronimo.deployment;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.Collection;
import java.util.Iterator;
import java.util.Properties;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.PosixParser;
import org.apache.commons.logging.LogFactory;
import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.gbean.GBeanInfoFactory;
import org.apache.geronimo.gbean.GConstructorInfo;
import org.apache.geronimo.gbean.GOperationInfo;
import org.apache.geronimo.gbean.GReferenceInfo;
import org.apache.geronimo.kernel.Kernel;
import org.apache.geronimo.kernel.log.GeronimoLogFactory;
import org.apache.geronimo.kernel.config.LocalConfigStore;
import org.apache.xmlbeans.SchemaTypeLoader;
import org.apache.xmlbeans.XmlBeans;
import org.apache.xmlbeans.XmlObject;

/**
 * Command line based deployment utility which combines multiple deployable modules
 * into a single configuration.
 *
 * @version $Revision: 1.1 $ $Date: 2004/02/12 18:27:39 $
 */
public class Deployer {
    static {
        // This MUST be done before the first log is acquired
        System.setProperty(LogFactory.FACTORY_PROPERTY, GeronimoLogFactory.class.getName());
    }

    public static final URI DEFAULT_CONFIG = URI.create("org/apache/geronimo/J2EEDeployer");

    private final Collection builders;

    public Deployer(Collection builders) {
        this.builders = builders;
    }

    public void deploy(CommandLine cmd) throws Exception {
        URL planURL = getURL(cmd.getOptionValue('p'));
        XmlObject plan = getLoader().parse(planURL, null, null);
        ConfigurationBuilder builder = null;
        for (Iterator i = builders.iterator(); i.hasNext();) {
            builder = (ConfigurationBuilder) i.next();
            if (builder.canConfigure(plan)) {
                break;
            }
            builder = null;
        }
        if (builder == null) {
            throw new DeploymentException("No deployer found for this plan type");
        }

        boolean saveOutput;
        File outfile;
        if (cmd.hasOption('o')) {
            saveOutput = true;
            outfile = new File(cmd.getOptionValue('o'));
        } else {
            saveOutput = false;
            outfile = File.createTempFile("deployer", ".car");
        }

        builder.buildConfiguration(outfile, plan, cmd.hasOption('I'));

        if (!saveOutput) {
            outfile.delete();
        }
    }

    private URL getURL(String location) throws MalformedURLException {
        File f = new File(location);
        if (f.exists() && f.canRead()) {
            return f.toURL();
        }
        return new URL(new File(".").toURL(), location);
    }

    private SchemaTypeLoader getLoader() {
        // @todo this should also set up the entity resolver and error handlers
        return XmlBeans.getContextTypeLoader();
    }

    /**
     * Command line entry point called by executable jar
     * @param args command line args
     */
    public static void main(String[] args) {
        try {
            CommandLine cmd = parseArgs(args);
            if (cmd == null) {
                return;
            }
            URI deployerID = cmd.hasOption('d') ? new URI(cmd.getOptionValue('d')) : DEFAULT_CONFIG;
            File configStore = cmd.hasOption('s') ? new File(cmd.getOptionValue('s')) : new File("../config-store");
            if (!configStore.isDirectory()) {
                System.err.println("Store does not exist or is not a directory: " + configStore);
                System.exit(2);
                throw new AssertionError();
            }

            Kernel kernel = new Kernel("geronimo.deployment", LocalConfigStore.GBEAN_INFO, configStore);
            kernel.boot();

            ObjectName configName = kernel.load(deployerID);
            kernel.startRecursiveGBean(configName);

            ObjectName deployerName = getDeployerName(deployerID);
            kernel.getMBeanServer().invoke(deployerName, "deploy", new Object[]{cmd}, new String[]{CommandLine.class.getName()});

            kernel.stopGBean(configName);
            kernel.shutdown();
        } catch (ParseException e) {
            e.printStackTrace();
            System.exit(2);
            throw new AssertionError();
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(3);
            throw new AssertionError();
        }
    }

    public static ObjectName getDeployerName(URI configID) throws MalformedObjectNameException {
        Properties props = new Properties();
        props.put("role", "Deployer");
        props.put("config", configID.toString());
        return new ObjectName("geronimo.deployment", props);
    }

    public static CommandLine parseArgs(String[] args) throws ParseException {
        Options options = new Options();
        options.addOption("h", "help", false, "print this message");
        options.addOption("I", "install", false, "install configuration in store");
        options.addOption("o", "outfile", true, "output file to generate");
        options.addOption("m", "module", true, "module to deploy");
        options.addOption("p", "plan", true, "deployment plan");
        options.addOption("d", "deployer", true, "id of the config used to perform deployment");
        options.addOption("s", "store", true, "location of the config store");
        CommandLine cmd = new PosixParser().parse(options, args);
        if (cmd.hasOption("h")) {
            new HelpFormatter().printHelp("deploy.jar [OPTIONS] <module>...", options);
            return null;
        }
        return cmd;
    }

    /**
     * GBean entry point invoked from an executable CAR.
     * @param args command line args
     */
    public void deploy(String[] args) throws Exception {
        CommandLine cmd = parseArgs(args);
        if (cmd == null) {
            return;
        }
        deploy(cmd);
    }

    public static final GBeanInfo GBEAN_INFO;

    static {
        GBeanInfoFactory infoFactory = new GBeanInfoFactory(Deployer.class);
        infoFactory.addOperation(new GOperationInfo("deploy", new Class[]{String[].class}));
        infoFactory.addOperation(new GOperationInfo("deploy", new Class[]{CommandLine.class}));
        infoFactory.addReference(new GReferenceInfo("Builders", ConfigurationBuilder.class));
        infoFactory.setConstructor(new GConstructorInfo(
                new String[]{"Builders"},
                new Class[]{Collection.class}
        ));
        GBEAN_INFO = infoFactory.getBeanInfo();
    }
}
