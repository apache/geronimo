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
import java.net.URI;
import java.util.Collection;
import java.util.Iterator;
import java.util.jar.Attributes;
import java.util.jar.JarFile;
import java.util.jar.Manifest;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.PosixParser;
import org.apache.geronimo.deployment.util.FileUtil;
import org.apache.geronimo.deployment.util.JarUtil;
import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.gbean.GBeanInfoFactory;
import org.apache.geronimo.kernel.config.ConfigurationStore;
import org.apache.geronimo.kernel.config.InvalidConfigException;

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

    public URI deploy(File moduleFile, File planFile) throws DeploymentException {
        return deploy(planFile, moduleFile, null, true, null, null);
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

        File planFile = cmd.planFile;
        File module = cmd.module;
        File carfile = cmd.carfile;
        boolean install = cmd.install;
        String mainClass = cmd.mainClass;
        String classPath = cmd.classPath;


        URI uri = deploy(planFile, module, carfile, install, mainClass, classPath);
        System.out.println("Deployment uri is " + uri);
    }

    public URI deploy(File planFile, File moduleFile, File carfile, boolean install, String mainClass, String classPath) throws DeploymentException {
        if (planFile == null && moduleFile == null) {
            throw new DeploymentException("No plan or module specified");
        }

        JarFile module = null;
        if (moduleFile != null) {
            try {
                module = JarUtil.createJarFile(moduleFile);
            } catch (IOException e) {
                throw new DeploymentException("Cound not open module file: " + moduleFile.getAbsolutePath(), e);
            }
        }

        Object plan = null;
        ConfigurationBuilder builder = null;
        for (Iterator i = builders.iterator(); i.hasNext();) {
            ConfigurationBuilder candidate = (ConfigurationBuilder) i.next();
            plan = candidate.getDeploymentPlan(planFile, module);
            if (plan != null) {
                builder = candidate;
                break;
            }
        }
        if (builder == null) {
            throw new DeploymentException("No deployer found for this plan type: " + planFile);
        }

        boolean saveOutput;
        if (carfile == null) {
            saveOutput = false;
            try {
                carfile = FileUtil.createTempFile();
            } catch (IOException e) {
                throw new DeploymentException("Unable to create temp file for deployment", e);
            }
        } else {
            saveOutput = true;
        }

        Manifest manifest = new Manifest();
        Attributes mainAttributes = manifest.getMainAttributes();
        mainAttributes.putValue(Attributes.Name.MANIFEST_VERSION.toString(), "1.0");
        if (mainClass != null) {
            mainAttributes.putValue(Attributes.Name.MAIN_CLASS.toString(), mainClass);
        }
        if (classPath != null) {
            mainAttributes.putValue(Attributes.Name.CLASS_PATH.toString(), classPath);
        }


        try {
            builder.buildConfiguration(carfile, manifest, plan, module);

            try {
                if (install) {
                    return store.install(carfile.toURL());
                }
                return null;
            } catch (InvalidConfigException e) {
                // unlikely as we just built this
                throw new DeploymentException(e);
            }
        } catch (DeploymentException e) {
            saveOutput = false;
            throw e;
        } catch (Exception e) {
            saveOutput = false;
            throw new DeploymentException(e);
        } finally {
            if (!saveOutput) {
                carfile.delete();
            }
        }
    }

    private static Command parseArgs(String[] args) throws ParseException, DeploymentException {
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
        if (cmd.hasOption('o')) {
            command.carfile = new File(cmd.getOptionValue('o'));
        }
        if (cmd.hasOption('p')) {
            command.planFile = getFile(cmd.getOptionValue('p'));
        }
        if (cmd.hasOption('m')) {
             command.module = getFile(cmd.getOptionValue('m'));
        }

        if (command.module == null && command.planFile == null) {
            System.err.println("No plan or module specified");
            return null;
        }
        if (cmd.hasOption("mainClass")) {
            command.mainClass = cmd.getOptionValue("mainClass");
        }
        if (cmd.hasOption("classPath")) {
            command.classPath = cmd.getOptionValue("classPath");
        }
        return command;
    }

    private static File getFile(String location) throws DeploymentException {
        File f = new File(location);
        if (f.exists() && f.canRead()) {
            return f;
        }
            URI uri = new File(".").toURI().resolve(location);
            if ("file".equals(uri.getScheme())) {
                return new File(uri);
            } else if (uri.getPath().endsWith("/")) {
                throw new DeploymentException("Unpacked modules can only be loaded from the local file system");
            } else {
                try {
                    return FileUtil.toTempFile(uri.toURL());
                } catch (IOException e) {
                    throw new DeploymentException("Could not open url: " + uri);
                }
            }
    }

    private static class Command {
        private boolean install;
        private File carfile;
        private File module;
        private File planFile;
        private String mainClass;
        private String classPath;
    }

    public static final GBeanInfo GBEAN_INFO;

    static {
        GBeanInfoFactory infoFactory = new GBeanInfoFactory(Deployer.class);

        infoFactory.addOperation("deploy", new Class[]{String[].class});
        infoFactory.addOperation("deploy", new Class[]{File.class, File.class});
        infoFactory.addOperation("deploy", new Class[]{File.class, File.class, File.class, boolean.class, String.class, String.class});

        infoFactory.addReference("Builders", ConfigurationBuilder.class);
        infoFactory.addReference("Store", ConfigurationStore.class);

        infoFactory.setConstructor(new String[]{"Builders", "Store"});

        GBEAN_INFO = infoFactory.getBeanInfo();
    }

    public static GBeanInfo getGBeanInfo() {
        return GBEAN_INFO;
    }
}
