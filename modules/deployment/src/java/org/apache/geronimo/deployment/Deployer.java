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
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Collection;
import java.util.Iterator;
import java.util.Properties;
import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.PosixParser;
import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.gbean.GBeanInfoFactory;
import org.apache.geronimo.gbean.GConstructorInfo;
import org.apache.geronimo.gbean.GOperationInfo;
import org.apache.geronimo.gbean.GReferenceInfo;
import org.apache.geronimo.kernel.Kernel;
import org.apache.geronimo.kernel.config.LocalConfigStore;
import org.apache.geronimo.kernel.config.InvalidConfigException;
import org.apache.geronimo.kernel.log.GeronimoLogging;
import org.apache.xmlbeans.SchemaTypeLoader;
import org.apache.xmlbeans.XmlBeans;
import org.apache.xmlbeans.XmlObject;

/**
 * Command line based deployment utility which combines multiple deployable modules
 * into a single configuration.
 *
 * @version $Revision: 1.6 $ $Date: 2004/02/20 07:40:53 $
 */
public class Deployer {
    static {
        // This MUST be done before the first log is acquired
        GeronimoLogging.initialize(GeronimoLogging.ERROR);
    }

    public static final URI DEFAULT_CONFIG = URI.create("org/apache/geronimo/J2EEDeployer");

    private final Collection builders;
    private final Kernel kernel;

    public Deployer(Kernel kernel, Collection builders) {
        this.kernel = kernel;
        this.builders = builders;
    }

    public void deploy(Command cmd) throws Exception {
        URL planURL = cmd.plan;
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
        if (cmd.carfile == null) {
            saveOutput = false;
            cmd.carfile = File.createTempFile("deployer", ".car");
        } else {
            saveOutput = true;
        }
        try {
            builder.buildConfiguration(cmd.carfile, null, plan);

            try {
                if (cmd.install) {
                    kernel.install(cmd.carfile.toURL());
                }
            } catch (InvalidConfigException e) {
                // unlikely as we just built this
                throw new DeploymentException(e);
            }
        } finally {
            if (!saveOutput) {
                cmd.carfile.delete();
            }
        }
    }

    private SchemaTypeLoader getLoader() {
        List types = new ArrayList(builders.size());
        for (Iterator i = builders.iterator(); i.hasNext();) {
            ConfigurationBuilder builder = (ConfigurationBuilder) i.next();
            types.addAll(Arrays.asList(builder.getTypeLoaders()));
        }
        // @todo this should also set up the entity resolver and error handlers
        SchemaTypeLoader[] loaders = (SchemaTypeLoader[]) types.toArray(new SchemaTypeLoader[types.size()]);
        return XmlBeans.typeLoaderUnion(loaders);
    }

    /**
     * Command line entry point called by executable jar
     * @param args command line args
     */
    public static void main(String[] args) {
        try {
            Command cmd = parseArgs(args);
            if (cmd == null) {
                return;
            }

            Kernel kernel = new Kernel("geronimo.deployment", LocalConfigStore.GBEAN_INFO, cmd.store);
            kernel.boot();

            ObjectName configName = kernel.load(cmd.deployer);
            kernel.startRecursiveGBean(configName);

            ObjectName deployerName = getDeployerName(cmd.deployer);
            kernel.getMBeanServer().invoke(deployerName, "deploy", new Object[]{cmd}, new String[]{Command.class.getName()});

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

    /**
     * GBean entry point invoked from an executable CAR.
     * @param args command line args
     */
    public void deploy(String[] args) throws Exception {
        Command cmd = parseArgs(args);
        if (cmd == null) {
            return;
        }
        deploy(cmd);
    }

    public static ObjectName getDeployerName(URI configID) throws MalformedObjectNameException {
        Properties props = new Properties();
        props.put("role", "Deployer");
        props.put("config", configID.toString());
        return new ObjectName("geronimo.deployment", props);
    }

    public static Command parseArgs(String[] args) throws ParseException {
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

        Command command = new Command();
        command.install = cmd.hasOption('I');
        command.carfile = cmd.hasOption('o') ? new File(cmd.getOptionValue('o')) : null;
        try {
            command.plan = cmd.hasOption('p') ? getURL(cmd.getOptionValue('p')) : null;
        } catch (MalformedURLException e) {
            System.err.println("Invalid URL for plan: "+cmd.getOptionValue('p'));
            return null;
        }
        try {
            command.module = cmd.hasOption('m') ? getURL(cmd.getOptionValue('m')) : null;
        } catch (MalformedURLException e) {
            System.err.println("Invalid URL for module: "+cmd.getOptionValue('m'));
            return null;
        }
        if (command.module == null && command.plan == null) {
            System.err.println("No plan or module specified");
            return null;
        }
        try {
            command.deployer = cmd.hasOption('d') ? new URI(cmd.getOptionValue('d')) : DEFAULT_CONFIG;
        } catch (URISyntaxException e) {
            System.err.println("Invalid URI for deployer: "+cmd.getOptionValue('d'));
            return null;
        }
        command.store = cmd.hasOption('s') ? new File(cmd.getOptionValue('s')) : new File("../config-store");
        return command;
    }

    private static URL getURL(String location) throws MalformedURLException {
        File f = new File(location);
        if (f.exists() && f.canRead()) {
            return f.toURL();
        }
        return new URL(new File(".").toURL(), location);
    }

    private static class Command {
        private boolean install;
        private File carfile;
        private URL module;
        private URL plan;
        private URI deployer;
        private File store;
    }

    public static final GBeanInfo GBEAN_INFO;

    static {
        GBeanInfoFactory infoFactory = new GBeanInfoFactory(Deployer.class);
        infoFactory.addOperation(new GOperationInfo("deploy", new Class[]{String[].class}));
        infoFactory.addOperation(new GOperationInfo("deploy", new Class[]{Command.class}));
        infoFactory.addReference(new GReferenceInfo("Kernel", Kernel.class));
        infoFactory.addReference(new GReferenceInfo("Builders", ConfigurationBuilder.class));
        infoFactory.setConstructor(new GConstructorInfo(
                new String[]{"Kernel", "Builders"},
                new Class[]{Kernel.class, Collection.class}
        ));
        GBEAN_INFO = infoFactory.getBeanInfo();
    }

    public static GBeanInfo getGBeanInfo() {
        return GBEAN_INFO;
    }
}
