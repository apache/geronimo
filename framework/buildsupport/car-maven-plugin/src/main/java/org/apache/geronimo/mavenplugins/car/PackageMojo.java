/**
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

package org.apache.geronimo.mavenplugins.car;

import java.io.File;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;

import org.apache.felix.utils.manifest.Attribute;
import org.apache.felix.utils.manifest.Clause;
import org.apache.felix.utils.manifest.Directive;
import org.apache.felix.utils.manifest.Parser;
import org.apache.felix.utils.version.VersionRange;
import org.apache.geronimo.deployment.ConfigurationBuilder;
import org.apache.geronimo.deployment.Deployer;
import org.apache.geronimo.gbean.AbstractName;
import org.apache.geronimo.kernel.config.ConfigurationManager;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;
import org.osgi.service.packageadmin.ExportedPackage;
import org.osgi.service.packageadmin.PackageAdmin;

/**
 * Build a Geronimo Configuration using the local Maven infrastructure.
 *
 * @version $Rev$ $Date$
 * @goal package
 * @requiresDependencyResolution compile
 */
public class PackageMojo extends AbstractCarMojo {


    /**
     * Directory containing the generated archive.
     *
     * @parameter expression="${project.build.directory}"
     * @required
     */
    private File outputDirectory = null;

    /**
     * The local Maven repository which will be used to pull artifacts into the Geronimo repository when packaging.
     *
     * @parameter expression="${settings.localRepository}"
     * @required
     * @readonly
     */
    private File repository = null;

    /**
     * The karaf repository where modules will be packaged up from.
     *
     * @parameter expression="${project.build.directory}/assembly/system"
     * @required
     */
    private File targetRepository = null;

    /**
     * The default deployer module to be used when no other deployer modules are configured.
     *
     * @parameter expression="org.apache.geronimo.framework/j2ee-system/${geronimoVersion}/car"
     * @required
     * @readonly
     */
    private String defaultDeploymentConfig = null;

    /**
     * Ther deployer modules to be used when packaging.
     *
     * @parameter
     */
    private String[] deploymentConfigs;

    /**
     * The name of the deployer which will be used to deploy the CAR.
     *
     * @parameter expression="org.apache.geronimo.framework/geronimo-gbean-deployer/${geronimoVersion}/car?j2eeType=Deployer,name=Deployer"
     * @required
     */
    private String deployerName = null;

    /**
     * The plan file for the CAR.
     *
     * @parameter expression="${project.build.directory}/work/plan.xml"
     * @required
     */
    private File planFile = null;

    /**
     * The file to include as a module of the CAR.
     *
     * @parameter
     */
    private File moduleFile = null;

    /**
     * An {@link Dependency} to include as a module of the CAR.
     *
     * @parameter
     */
    private Dependency module = null;

    /**
     * The location where the properties mapping will be generated.
     * <p/>
     * <p>
     * Probably don't want to change this.
     * </p>
     *
     * @parameter expression="${project.build.directory}/explicit-versions.properties"
     */
    private File explicitResolutionProperties = null;

    /**
     * True to enable the bootshell when packaging.
     *
     * @parameter
     */
    private boolean bootstrap = false;

    /**
     * Holds a local repo lookup instance so that we can use the current project to resolve.
     * This is required since the Kernel used to deploy is cached.
     */
    private static ThreadLocal<Maven2RepositoryAdapter.ArtifactLookup> lookupHolder = new ThreadLocal<Maven2RepositoryAdapter.ArtifactLookup>();

    /**
     * Directory for generated plugin metadata file.
     *
     * @parameter expression="${project.build.directory}/resources/"
     * @required
     */
    protected File targetDir = null;

    /**
     * Name of generated plugin metadata file.
     *
     * @parameter default-value="META-INF/geronimo-plugin.xml"
     * @required
     */
    protected String pluginMetadataFileName = null;
    private BundleContext bundleContext;

    //
    // Mojo
    //

    public void execute() throws MojoExecutionException, MojoFailureException {
        try {
            // We need to make sure to clean up any previous work first or this operation will fail
//            FileUtils.forceDelete(targetRepository);
//            FileUtils.forceMkdir(targetRepository);

            if (!planFile.exists()) {
                return;
            }

            // Use the default configs if none specified
            if (deploymentConfigs == null) {
//                if (bootstrap) {
                    deploymentConfigs = new String[]{};
//                } else {
//                    deploymentConfigs = new String[]{defaultDeploymentConfig};
//                }
            }
            getLog().debug("Deployment configs: " + Arrays.asList(deploymentConfigs));

            getDependencies(project, false);
            // If module is set, then resolve the artifact and set moduleFile
            if (module != null) {
                Artifact artifact = resolveArtifact(module.getGroupId(), module.getArtifactId(), module.getType());
                if (artifact == null) {
                    throw new MojoExecutionException("Could not resolve module " + module.getGroupId() + ":" + module.getArtifactId() + ":" + module.getType() + ". Perhaps it is not listed as a dependency");
                }
                moduleFile = artifact.getFile();
                getLog().debug("Using module file: " + moduleFile);
            }


            generateExplicitVersionProperties(explicitResolutionProperties, dependencyArtifacts);

            //
            // NOTE: Install a local lookup, so that the cached kernel can resolve based on the current project
            //       and not the project where the kernel was first initialized.
            //
            lookupHolder.set(new ArtifactLookupImpl());

            buildPackage();
        } catch (Exception e) {
            throw new MojoExecutionException("could not package plugin", e);
        } finally {
            cleanup();
        }
    }

    private File getArtifactInRepositoryDir() {

        org.apache.geronimo.kernel.repository.Artifact geronimoArtifact = mavenToGeronimoArtifact(project.getArtifact());
        org.apache.geronimo.kernel.repository.Maven2Repository repo = new org.apache.geronimo.kernel.repository.Maven2Repository(targetRepository);
        return repo.getLocation(geronimoArtifact);
    }


    //
    // Deployment
    //

    private static final String KERNEL_NAME = "geronimo.maven";

    /**
     * Reference to the kernel that will last the lifetime of this classloader.
     * The KernelRegistry keeps soft references that may be garbage collected.
     */
//    private Kernel kernel;

    private AbstractName targetConfigStoreAName;

    private AbstractName targetRepositoryAName;

    private boolean targetSet;

    public void buildPackage() throws Exception {
        getLog().info("Packaging module configuration: " + planFile);

        try {
            getLog().debug("Creating kernel...");
            bundleContext = getFramework().getBundleContext();
            waitForBundles(bundleContext, 20000l);
            listBundles(bundleContext);

            getLog().debug("Starting configurations..." + Arrays.asList(deploymentConfigs));

            // start the Configuration we're going to use for this deployment
            Object configurationManager = getService(bundleContext, ConfigurationManager.class.getName(), null, 10000l);
            ClassLoader cl = configurationManager.getClass().getClassLoader();
            Class artifactClass = cl.loadClass("org.apache.geronimo.kernel.repository.Artifact");
            Method create = artifactClass.getMethod("create", String.class);
            Method isLoaded = configurationManager.getClass().getMethod("isLoaded", artifactClass);
            Class monitorClass = cl.loadClass("org.apache.geronimo.kernel.config.LifecycleMonitor");
            Class recordingLifecycleMonitorClass = cl.loadClass("org.apache.geronimo.kernel.config.RecordingLifecycleMonitor");
            Method loadConfiguration = configurationManager.getClass().getMethod("loadConfiguration", artifactClass, monitorClass);
            Method startConfiguration = configurationManager.getClass().getMethod("startConfiguration", artifactClass, monitorClass);

            //add a repo pointing to maven local repo
            Class m2repoClass = cl.loadClass("org.apache.geronimo.kernel.repository.Maven2Repository");
            Constructor m2repoConstructor = m2repoClass.getConstructor(File.class);
            Object m2repo = m2repoConstructor.newInstance(repository);
            Class repoClass = cl.loadClass("org.apache.geronimo.kernel.repository.Repository");
            bundleContext.registerService(repoClass, m2repo, null);
//            Method bindMethod = configurationManager.getClass().getMethod("bindRepository", repoClass);
//            bindMethod.invoke(configurationManager, m2repo);

            for (String artifactName : deploymentConfigs) {
                Object configName = create.invoke(null, artifactName);// org.apache.geronimo.kernel.repository.Artifact.create(artifactName);
                if (!(Boolean) isLoaded.invoke(configurationManager, configName)) { //.isLoaded(configName)) {
                    Object monitor = recordingLifecycleMonitorClass.newInstance();//new RecordingLifecycleMonitor();
                    try {
                        loadConfiguration.invoke(configurationManager, configName, monitor);
//                        configurationManager.loadConfiguration(configName, monitor);
                    } catch (Exception e) {
                        getLog().error("Could not load deployer configuration: " + configName + "\n" + monitor.toString(), e);
                    }
                    monitor = recordingLifecycleMonitorClass.newInstance();//new RecordingLifecycleMonitor();
                    try {
                        startConfiguration.invoke(configurationManager, configName, monitor);
//                        configurationManager.startConfiguration(configName, monitor);
                        getLog().info("Started deployer: " + configName);
                    } catch (Exception e) {
                        getLog().error("Could not start deployer configuration: " + configName + "\n" + monitor.toString(), e);
                    }
                }
            }

            Object kernel = getService(bundleContext, "org.apache.geronimo.kernel.Kernel", null, 10000l);
            Method list = kernel.getClass().getMethod("listGBeans", cl.loadClass("org.apache.geronimo.gbean.AbstractNameQuery"));
            Collection results = (Collection) list.invoke(kernel, (Object)null);
            for (Object o: results) {
                getLog().info("  " + o);
            }

            getLog().debug("Deploying...");

            getService(bundleContext, ConfigurationBuilder.class.getName(), null, 10000l);
            Object deployer = getService(bundleContext, Deployer.class.getName(), null, 10000l);
            invokeDeployer(deployer, null);
        } catch (Exception e) {
            getLog().info("Exception, use console to investigate ", e);
            listBundles(bundleContext);
            PackageAdmin admin = (PackageAdmin) getService(bundleContext, PackageAdmin.class.getName(), null, 1000l);
//            for (Bundle b : bundleContext.getBundles()) {
//                int state = b.getState();
//                if (state != 32) {
//                    try {
//                        b.start();
//                        getLog().info("trying to start " + b + " from state " + state + ", got to state " + b.getState());
////                        getLog().info(headers(admin, b));
//                    } catch (BundleException e1) {
//                        getLog().info("Could not start " + b + e.getMessage());
//                    }
//                }
//            }
            listBundles(bundleContext);
            while (1 == 1) {
                try {
                    Thread.sleep(1000L);
                    if (bundleContext.getBundle().getState() != 32) {
                        break;
                    }
                } catch (InterruptedException e1) {
                    //exit
                    break;
                }
            }
            throw e;

        }
        //use a fresh kernel for each module
//        kernel.shutdown();
//        kernel = null;
        bundleContext.getBundle().stop();
        bundleContext = null;
    }

    private String headers(PackageAdmin admin, Bundle b) {
        StringBuilder buf = new StringBuilder();
        String header = b.getHeaders().get("Import-Package");
        formatHeader(header, buf, 3);
        return buf.toString();
    }

    protected void formatHeader(String header, StringBuilder builder, int indent) {
        Clause[] clauses = Parser.parseHeader(header);
        formatClauses(clauses, builder, indent);
    }

    protected void formatClauses(Clause[] clauses, StringBuilder builder, int indent) {
        boolean first = true;
        for (Clause clause : clauses) {
            if (first) {
                first = false;
            } else {
                builder.append(",\n");
            }
            formatClause(clause, builder, indent);
        }
    }

    protected void formatClause(Clause clause, StringBuilder builder, int indent) {
        if (indent < 0) {
            indent = 3;
        }
        String name = clause.getName();
        Directive[] directives = clause.getDirectives();
        Attribute[] attributes = clause.getAttributes();
        Arrays.sort(directives, new Comparator<Directive>() {
            public int compare(Directive o1, Directive o2) {
                return o1.getName().compareTo(o2.getName());
            }
        });
        Arrays.sort(attributes, new Comparator<Attribute>() {
            public int compare(Attribute o1, Attribute o2) {
                return o1.getName().compareTo(o2.getName());
            }
        });
        builder.append(name);
        for (int i = 0; directives != null && i < directives.length; i++) {
            builder.append(";");
            if (indent > 1) {
                builder.append("\n\t\t");
            }
            builder.append(directives[i].getName()).append(":=");
            String v = directives[i].getValue();
            if (v.contains(",")) {
                if (indent > 2 && v.length() > 20) {
                    v = v.replace(",", ",\n\t\t\t");
                }
                builder.append("\"").append(v).append("\"");
            } else {
                builder.append(v);
            }
        }
        for (int i = 0; attributes != null && i < attributes.length; i++) {
            builder.append(";");
            if (indent > 1) {
                builder.append("\n\t\t");
            }
            builder.append(attributes[i].getName()).append("=");
            String v = attributes[i].getValue();
            if (v.contains(",")) {
                if (indent > 2 && v.length() > 20) {
                    v = v.replace(",", ",\n\t\t\t");
                }
                builder.append("\"").append(v).append("\"");
            } else {
                builder.append(v);
            }
        }
    }

    private boolean checkPackage(PackageAdmin admin, String packageName, String version) {
        VersionRange range = VersionRange.parseVersionRange(version);
        if (admin != null) {
            ExportedPackage[] packages = admin.getExportedPackages(packageName);
            if (packages != null) {
                for (ExportedPackage export : packages) {
                    if (range.contains(export.getVersion())) {
                        return true;
                    }
                }
            }
        }
        return false;
    }


    private static final Class[] DEPLOY_SIGNATURE = {
            boolean.class,
            File.class,
            File.class,
    };

    private List invokeDeployer(final Object deployer, final String targetConfigStore) throws Exception {
        Object[] args = {
                Boolean.FALSE, // Not in-place
                moduleFile,
                planFile,
        };

        Method m = deployer.getClass().getMethod("deploy", DEPLOY_SIGNATURE);
        return (List) m.invoke(deployer, args);
    }

}
