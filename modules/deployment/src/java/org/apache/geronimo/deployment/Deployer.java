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
import java.io.FileOutputStream;
import java.net.URI;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Collections;
import java.util.ArrayList;
import java.util.jar.Attributes;
import java.util.jar.JarFile;
import java.util.jar.Manifest;

//import org.apache.commons.cli.CommandLine;
//import org.apache.commons.cli.HelpFormatter;
//import org.apache.commons.cli.Options;
//import org.apache.commons.cli.ParseException;
//import org.apache.commons.cli.PosixParser;
import org.apache.geronimo.deployment.util.DeploymentUtil;
import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.gbean.GBeanInfoBuilder;
import org.apache.geronimo.kernel.config.ConfigurationStore;
import org.apache.geronimo.kernel.config.InvalidConfigException;
import org.apache.geronimo.common.DeploymentException;

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

    public List deploy(File moduleFile, File planFile) throws DeploymentException {
        File tmpDir = null;
        if (moduleFile != null && !moduleFile.isDirectory()) {
            // todo jar url handling with Sun's VM on Windows leaves a lock on the module file preventing rebuilds
            // to address this we use a gross hack and copy the file to a temporary directory
            // unfortunately the lock on the file will prevent that being deleted properly
            // we need to rewrite deployment so that it does not use jar: urls
            try {
                tmpDir = File.createTempFile("deployer", ".tmpdir");
                tmpDir.delete();
                tmpDir.mkdir();
                File tmpFile = new File(tmpDir, moduleFile.getName());
                DeploymentUtil.copyFile(moduleFile, tmpFile);
                moduleFile = tmpFile;
            } catch (IOException e) {
                throw new DeploymentException(e);
            }
        }

        try {
            return deploy(planFile, moduleFile, null, true, null, null);
        } finally {
            if (tmpDir != null) {
                DeploymentUtil.recursiveDelete(tmpDir);
            }
        }
    }

    /*
     * GBean entry point invoked from an executable CAR.
     *
     * @param args command line args
     *
    public void deploy(String[] args) throws Exception {
        Command cmd = parseArgs(args);
        try {
            if (cmd == null) {
                return;
            }

            File planFile = cmd.planFile;
            File module = cmd.moduleFile;
            File carfile = cmd.carFile;
            boolean install = cmd.install;
            String mainClass = cmd.mainClass;
            String classPath = cmd.classPath;


            List objectNames = deploy(planFile, module, carfile, install, mainClass, classPath);
            if (!objectNames.isEmpty()) {
                Iterator iterator = objectNames.iterator();
                System.out.println("Server URI: " + iterator.next());
                while (iterator.hasNext()) {
                    System.out.println("Client URI: " + iterator.next());
                }
            }
        } finally {
            if (cmd.isPlanFileTemp) {
                cmd.planFile.delete();
            }
            if (cmd.isModuleFileTemp) {
                cmd.moduleFile.delete();
            }
        }
    }
    */

    public List deploy(File planFile, File moduleFile, File targetFile, boolean install, String mainClass, String classPath) throws DeploymentException {
        if (planFile == null && moduleFile == null) {
            throw new DeploymentException("No plan or module specified");
        }

        if (planFile != null) {
            if (!planFile.exists()) {
                throw new DeploymentException("Plan file does not exist: " + planFile.getAbsolutePath());
            }
            if (!planFile.isFile()) {
                throw new DeploymentException("Plan file is not a regular file: " + planFile.getAbsolutePath());
            }
        }

        JarFile module = null;
        if (moduleFile != null) {
            if (!moduleFile.exists()) {
                throw new DeploymentException("Module file does not exist: " + moduleFile.getAbsolutePath());
            }
            try {
                module = DeploymentUtil.createJarFile(moduleFile);
            } catch (IOException e) {
                throw new DeploymentException("Cound not open module file: " + moduleFile.getAbsolutePath(), e);
            }
        }

        File configurationDir = null;
        try {
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
                throw new DeploymentException("Syntax error in deployment plan or no deployer service available (currently I can't tell the difference):" +
                        (planFile == null ? "" : " planFile=" + planFile.getAbsolutePath()) +
                        (moduleFile == null ? "" : ", moduleFile" + moduleFile.getAbsolutePath()));
            }

            // create a configuration dir to write the configuration during the building proces
            configurationDir = store.createNewConfigurationDir();

            // create te meta-inf dir
            File metaInf = new File(configurationDir, "META-INF");
            metaInf.mkdirs();

            // create the manifest
            Manifest manifest = new Manifest();
            Attributes mainAttributes = manifest.getMainAttributes();
            mainAttributes.putValue(Attributes.Name.MANIFEST_VERSION.toString(), "1.0");
            if (mainClass != null) {
                mainAttributes.putValue(Attributes.Name.MAIN_CLASS.toString(), mainClass);
            }
            if (classPath != null) {
                mainAttributes.putValue(Attributes.Name.CLASS_PATH.toString(), classPath);
            }

            // Write the manifest
            FileOutputStream out = null;
            try {
                out = new FileOutputStream(new File(metaInf, "MANIFEST.MF"));
                manifest.write(out);
            } finally {
                DeploymentUtil.close(out);
            }


            // this is a bit weird and should be rethougth but it works
            List childURIs = builder.buildConfiguration(plan, module, configurationDir);

            try {
                if (targetFile != null) {
                    // add the startup tag file which allows us to locate the startup directory
                    File startupJarTag = new File(metaInf, "startup-jar");
                    if (mainClass != null) {
                        startupJarTag.createNewFile();
                    }

                    // jar up the directory
                    DeploymentUtil.jarDirectory(configurationDir,  targetFile);

                    // remove the startup tag file so it doesn't accidently leak into a normal classloader
                    startupJarTag.delete();
                }
                if (install) {
                    URI uri = store.install(configurationDir);
                    List deployedURIs = new ArrayList(childURIs.size() + 1);
                    deployedURIs.add(uri.toString());
                    deployedURIs.addAll(childURIs);
                    return deployedURIs;
                }
                return Collections.EMPTY_LIST;
            } catch (InvalidConfigException e) {
                // unlikely as we just built this
                throw new DeploymentException(e);
            }
        } catch(Throwable e) {
            DeploymentUtil.recursiveDelete(configurationDir);
            if (targetFile != null) {
                targetFile.delete();
            }

            if (e instanceof Error) {
                throw (Error)e;
            } else if (e instanceof DeploymentException) {
                throw (DeploymentException)e;
            } else if (e instanceof Exception) {
                throw new DeploymentException(e);
            }
            throw new Error(e);
        } finally {
            DeploymentUtil.close(module);
        }
    }
    /*
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
        try {
            command.install = cmd.hasOption('I');
            if (cmd.hasOption('o')) {
                command.carFile = new File(cmd.getOptionValue('o'));
            }
            if (cmd.hasOption('p')) {
                URI uri = getURI(cmd.getOptionValue('p'));
                if ("file".equals(uri.getScheme())) {
                    command.planFile = new File(uri);
                    command.isPlanFileTemp = false;
                } else {
                    try {
                        command.planFile = DeploymentUtil.toTempFile(uri.toURL());
                    } catch (IOException e) {
                        throw new DeploymentException("Invalid plan file location: " + uri, e);
                    }
                    command.isPlanFileTemp = true;
                }
            }
            if (cmd.hasOption('m')) {
                URI uri = getURI(cmd.getOptionValue('m'));
                if ("file".equals(uri.getScheme())) {
                    command.moduleFile = new File(uri);
                    command.isModuleFileTemp = false;
                } else {
                    try {
                        command.moduleFile = DeploymentUtil.toTempFile(uri.toURL());
                    } catch (IOException e) {
                        throw new DeploymentException("Invalid module file location: " + uri, e);
                    }
                    command.isModuleFileTemp = true;
                }
            }

            if (command.moduleFile == null && command.planFile == null) {
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
        } catch (Throwable e) {
            if (command.isPlanFileTemp) {
                command.planFile.delete();
            }
            if (command.isModuleFileTemp) {
                command.moduleFile.delete();
            }

            if (e instanceof DeploymentException) {
                throw (DeploymentException) e;
            } else if (e instanceof RuntimeException) {
                throw (RuntimeException) e;
            } else if (e instanceof Error) {
                throw (Error) e;
            }
            throw new DeploymentException(e);
        }
    }

    private static URI getURI(String location) throws DeploymentException {
		// on windows the location may be an absolute path including a drive letter
		// this will cause uri.resolve to fail because of the presence of a ':' character
		// to stop this we first try locating the uri using a File object
		File file = new File(location);
		if (file.exists() && file.canRead()) {
			return file.toURI();
		}

        URI uri = new File(".").toURI().resolve(location);
        if (!"file".equals(uri.getScheme()) && uri.getPath().endsWith("/")) {
            throw new DeploymentException("Unpacked modules can only be loaded from the local file system");
        }
        return uri;
    }

    private static class Command {
        private boolean install;
        private File carFile;
        private File moduleFile;
        private boolean isModuleFileTemp = false;
        private File planFile;
        private boolean isPlanFileTemp = false;
        private String mainClass;
        private String classPath;
    }
*/
    public static final GBeanInfo GBEAN_INFO;

    static {
        GBeanInfoBuilder infoFactory = new GBeanInfoBuilder(Deployer.class);

//        infoFactory.addOperation("deploy", new Class[]{String[].class});
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
