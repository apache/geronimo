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

package org.apache.geronimo.deployment;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.jar.Attributes;
import java.util.jar.Manifest;

import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.PosixParser;
import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.gbean.GBeanInfoFactory;
import org.apache.geronimo.kernel.config.ConfigurationStore;
import org.apache.geronimo.kernel.config.InvalidConfigException;
import org.apache.xmlbeans.SchemaTypeLoader;
import org.apache.xmlbeans.XmlBeans;
import org.apache.xmlbeans.XmlObject;
import org.apache.xmlbeans.XmlException;

/**
 * Command line based deployment utility which combines multiple deployable modules
 * into a single configuration.
 *
 * @version $Rev$ $Date$
 */
public class Deployer {
    private final Collection builders;
    private final ConfigurationStore store;

    public Deployer(Collection builders, ConfigurationStore store) {
        this.builders = builders;
        this.store = store;
    }

    public URI deploy(File moduleFile, File deploymentPlan) throws DeploymentException {
        ConfigurationBuilder builder = null;

        XmlObject plan = null;
        if (deploymentPlan != null) {
            try {
                plan = getLoader().parse(deploymentPlan, null, null);
            } catch (XmlException e) {
                throw new DeploymentException(e);
            } catch (IOException e) {
                throw new DeploymentException(e);
            }
            for (Iterator i = builders.iterator(); i.hasNext();) {
                ConfigurationBuilder candidate = (ConfigurationBuilder) i.next();
                if (candidate.canConfigure(plan)) {
                    builder = candidate;
                    break;
                }
            }
            if (builder == null) {
                throw new DeploymentException("No deployer found for this plan type: " + deploymentPlan);
            }
        } else if (moduleFile != null) {
            URL moduleURL;
            try {
                moduleURL = moduleFile.toURL();
            } catch (MalformedURLException e) {
                throw new DeploymentException(e);
            }
            for (Iterator i = builders.iterator(); i.hasNext();) {
                ConfigurationBuilder candidate = (ConfigurationBuilder) i.next();
                try {
                    plan = candidate.getDeploymentPlan(moduleURL);
                } catch (XmlException e) {
                    throw new DeploymentException(e);
                }
                if (plan != null) {
                    builder = candidate;
                    break;
                }
            }
            if (builder == null) {
                throw new DeploymentException("No deployer found for this module type: " + moduleFile);
            }
        }
        try {
            File carfile = File.createTempFile("deployer", ".car");
            try {

                Manifest manifest = new Manifest();
                Attributes mainAttributes = manifest.getMainAttributes();
                mainAttributes.putValue(Attributes.Name.MANIFEST_VERSION.toString(), "1.0");
                builder.buildConfiguration(carfile, manifest, moduleFile, plan);
                return store.install(carfile.toURL());
            } catch (InvalidConfigException e) {
                throw new DeploymentException(e);
            } finally {
                carfile.delete();
            }
        } catch (IOException e) {
            throw new DeploymentException(e);
        }
    }

    /**
     * GBean entry point invoked from an executable CAR.
     *
     * @param args command line args
     */
    public void deploy(String[] args) throws Exception {
        Command cmd = parseArgs(args);
        if (cmd == null) {
            return;
        }

        ConfigurationBuilder builder = null;

        // parse the plan
        XmlObject plan = null;
        if (cmd.plan != null) {
            plan = getLoader().parse(cmd.plan, null, null);
            for (Iterator i = builders.iterator(); i.hasNext();) {
                ConfigurationBuilder candidate = (ConfigurationBuilder) i.next();
                if (candidate.canConfigure(plan)) {
                    builder = candidate;
                    break;
                }
            }
            if (builder == null) {
                throw new DeploymentException("No deployer found for this plan type: " + cmd.plan);
            }
        } else if (cmd.module != null) {
            for (Iterator i = builders.iterator(); i.hasNext();) {
                ConfigurationBuilder candidate = (ConfigurationBuilder) i.next();
                plan = candidate.getDeploymentPlan(cmd.module);
                if (plan != null) {
                    builder = candidate;
                    break;
                }
            }
            if (builder == null) {
                throw new DeploymentException("No deployer found for this module type: " + cmd.module);
            }
        } else {
            throw new DeploymentException("No plan or module supplied");
        }

        boolean saveOutput;
        if (cmd.carfile == null) {
            saveOutput = false;
            cmd.carfile = File.createTempFile("deployer", ".car");
        } else {
            saveOutput = true;
        }

        Manifest manifest = new Manifest();
        Attributes mainAttributes = manifest.getMainAttributes();
        mainAttributes.putValue(Attributes.Name.MANIFEST_VERSION.toString(), "1.0");
        if (cmd.mainClass != null) {
            mainAttributes.putValue(Attributes.Name.MAIN_CLASS.toString(), cmd.mainClass);
        }
        if (cmd.classPath != null) {
            mainAttributes.putValue(Attributes.Name.CLASS_PATH.toString(), cmd.classPath);
        }


        try {
            if (cmd.module == null) {
                builder.buildConfiguration(cmd.carfile, manifest, (InputStream) null, plan);
            } else if ("file".equals(cmd.module.getProtocol())) {
                File module = new File(new URI(cmd.module.toString()));
                builder.buildConfiguration(cmd.carfile, manifest, module, plan);
            } else if (cmd.module.toString().endsWith("/")) {
                throw new DeploymentException("Unpacked modules must be files");
            } else {
                InputStream moduleStream = cmd.module.openStream();
                try {
                    builder.buildConfiguration(cmd.carfile, manifest, moduleStream, plan);
                } finally {
                    try {
                        moduleStream.close();
                    } catch (IOException e) {
                        // ignore
                    }
                }
            }

            try {
                if (cmd.install) {
                    store.install(cmd.carfile.toURL());
                }
            } catch (InvalidConfigException e) {
                // unlikely as we just built this
                throw new DeploymentException(e);
            }
        } catch (Exception e) {
            saveOutput = false;
            throw e;
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
        options.addOption(null, "mainClass", true, "deployment plan");
        options.addOption(null, "classPath", true, "deployment plan");

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
            System.err.println("Invalid URL for plan: " + cmd.getOptionValue('p'));
            return null;
        }
        try {
            command.module = cmd.hasOption('m') ? getURL(cmd.getOptionValue('m')) : null;
        } catch (MalformedURLException e) {
            System.err.println("Invalid URL for module: " + cmd.getOptionValue('m'));
            return null;
        }
        if (command.module == null && command.plan == null) {
            System.err.println("No plan or module specified");
            return null;
        }
        command.mainClass = cmd.hasOption("mainClass") ? cmd.getOptionValue("mainClass") : null;
        command.classPath = cmd.hasOption("classPath") ? cmd.getOptionValue("classPath") : null;
        return command;
    }

    private static URL getURL(String location) throws MalformedURLException {
        File f = new File(location);
        if (f.exists() && f.canRead()) {
            return f.toURI().toURL();
        }
        try {
            return new File(".").toURI().resolve(location).toURL();
        } catch (IllegalArgumentException e) {
            // thrown by URI.resolve if the location is not valid
            throw (MalformedURLException) new MalformedURLException("Invalid location: " + location).initCause(e);
        }
    }

    private static class Command {
        private boolean install;
        private File carfile;
        private URL module;
        private URL plan;
        private String mainClass;
        private String classPath;
    }

    public static final GBeanInfo GBEAN_INFO;

    static {
        GBeanInfoFactory infoFactory = new GBeanInfoFactory(Deployer.class);

        infoFactory.addOperation("deploy", new Class[]{String[].class});
        infoFactory.addOperation("deploy", new Class[]{File.class, File.class});

        infoFactory.addReference("Builders", ConfigurationBuilder.class);
        infoFactory.addReference("Store", ConfigurationStore.class);

        infoFactory.setConstructor(new String[]{"Builders", "Store"});

        GBEAN_INFO = infoFactory.getBeanInfo();
    }

    public static GBeanInfo getGBeanInfo() {
        return GBEAN_INFO;
    }
}
