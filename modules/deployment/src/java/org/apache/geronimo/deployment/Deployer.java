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
import org.apache.geronimo.system.main.CommandLineManifest;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Command line based deployment utility which combines multiple deployable modules
 * into a single configuration.
 *
 * @version $Rev$ $Date$
 */
public class Deployer {
    private static final Log log = LogFactory.getLog(Deployer.class);
    private final Collection builders;
    private final ConfigurationStore store;

    public Deployer(Collection builders, ConfigurationStore store) {
        this.builders = builders;
        this.store = store;
    }

    public List deploy(File moduleFile, File planFile) throws DeploymentException {
        File originalModuleFile = moduleFile;
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
            return deploy(planFile, moduleFile, null, true, null, null, null);
        } catch (DeploymentException e) {
            log.debug("Deployment failed: plan=" + planFile +", module=" + originalModuleFile, e);
            throw e;
        } finally {
            if (tmpDir != null) {
                DeploymentUtil.recursiveDelete(tmpDir);
            }
        }
    }

    public List deploy(File planFile, File moduleFile, File targetFile, boolean install, String mainClass, String classPath, String endorsedDirs) throws DeploymentException {
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
            if (endorsedDirs != null) {
                mainAttributes.putValue(CommandLineManifest.ENDORSED_DIRS.toString(), endorsedDirs);
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

    public static final GBeanInfo GBEAN_INFO;

    private static final String DEPLOYER = "Deployer";

    static {
        GBeanInfoBuilder infoFactory = new GBeanInfoBuilder(Deployer.class, DEPLOYER);

        infoFactory.addOperation("deploy", new Class[]{File.class, File.class});
        infoFactory.addOperation("deploy", new Class[]{File.class, File.class, File.class, boolean.class, String.class, String.class, String.class});

        infoFactory.addReference("Builders", ConfigurationBuilder.class);
        infoFactory.addReference("Store", ConfigurationStore.class);

        infoFactory.setConstructor(new String[]{"Builders", "Store"});

        GBEAN_INFO = infoFactory.getBeanInfo();
    }

    public static GBeanInfo getGBeanInfo() {
        return GBEAN_INFO;
    }
}
