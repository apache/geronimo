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
import java.net.URI;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.geronimo.deployment.PluginBootstrap2;
import org.apache.geronimo.gbean.AbstractName;
import org.apache.geronimo.gbean.AbstractNameQuery;
import org.apache.geronimo.gbean.GBeanData;
import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.gbean.ReferencePatterns;
import org.apache.geronimo.kernel.Kernel;
import org.apache.geronimo.kernel.KernelFactory;
import org.apache.geronimo.kernel.KernelRegistry;
import org.apache.geronimo.kernel.Naming;
import org.apache.geronimo.kernel.config.ConfigurationData;
import org.apache.geronimo.kernel.config.ConfigurationManager;
import org.apache.geronimo.kernel.config.ConfigurationUtil;
import org.apache.geronimo.kernel.config.KernelConfigurationManager;
import org.apache.geronimo.kernel.config.LifecycleException;
import org.apache.geronimo.kernel.config.RecordingLifecycleMonitor;
import org.apache.geronimo.kernel.management.State;
import org.apache.geronimo.kernel.repository.DefaultArtifactManager;
import org.apache.geronimo.system.configuration.RepositoryConfigurationStore;
import org.apache.geronimo.system.repository.Maven2Repository;
import org.apache.geronimo.system.resolver.ExplicitDefaultArtifactResolver;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.codehaus.plexus.util.FileUtils;

/**
 * Build a Geronimo Configuration using the local Maven infrastructure.
 *
 * @goal package
 * @requiresDependencyResolution runtime
 *
 * @version $Rev$ $Date$
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
     * The Geronimo repository where modules will be packaged up from.
     *
     * @parameter expression="${project.build.directory}/repository"
     * @required
     */
    private File targetRepository = null;

    /**
     * The default deployer module to be used when no other deployer modules are configured.
     *
     * @parameter expression="org.apache.geronimo.framework/geronimo-gbean-deployer/${geronimoVersion}/car"
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
     * @parameter expression="${project.build.directory}/resources/META-INF/plan.xml"
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

    //
    // Mojo
    //

    public void execute() throws MojoExecutionException, MojoFailureException {
        try {
// We need to make sure to clean up any previous work first or this operation will fail
            FileUtils.forceDelete(targetRepository);
            FileUtils.forceMkdir(targetRepository);

            // Use the default configs if none specified
            if (deploymentConfigs == null) {
                if (!bootstrap) {
                    deploymentConfigs = new String[]{defaultDeploymentConfig};
                } else {
                    deploymentConfigs = new String[]{};
                }
            }
            getLog().debug("Deployment configs: " + Arrays.asList(deploymentConfigs));

            getDependencies(project, false);
            // If module is set, then resolve the artifact and set moduleFile
            if (module != null) {
                Artifact artifact = resolveArtifact(module.getGroupId(), module.getArtifactId(), module.getType());
                moduleFile = artifact.getFile();
                getLog().debug("Using module file: " + moduleFile);
            }


            generateExplicitVersionProperties(explicitResolutionProperties, dependencies);

            //
            // NOTE: Install a local lookup, so that the cached kernel can resolve based on the current project
            //       and not the project where the kernel was first initialized.
            //
            lookupHolder.set(new ArtifactLookupImpl(new HashMap()));

            if (bootstrap) {
                executeBootShell();
            } else {
                buildPackage();
            }
        } catch (Exception e) {
            throw new MojoExecutionException("could not package plugin", e);
        }
    }

    private File getArtifactInRepositoryDir() {
        //
        // HACK: Generate the filename in the repo... really should delegate this to the repo impl
        //

        File dir = new File(targetRepository, project.getGroupId().replace('.', '/'));
        dir = new File(dir, project.getArtifactId());
        dir = new File(dir, project.getVersion());
        dir = new File(dir, project.getArtifactId() + "-" + project.getVersion() + ".car");

        return dir;
    }

    public void executeBootShell() throws Exception {
        getLog().debug("Starting bootstrap shell...");

        PluginBootstrap2 boot = new PluginBootstrap2();

        boot.setBuildDir(outputDirectory);
        boot.setCarFile(getArtifactInRepositoryDir());
        boot.setLocalRepo(repository);
        boot.setPlan(planFile);

        // Generate expanded so we can use Maven to generate the archive
        boot.setExpanded(true);

        boot.bootstrap();
    }

    //
    // Deployment
    //

    private static final String KERNEL_NAME = "geronimo.maven";

    /**
     * Reference to the kernel that will last the lifetime of this classloader.
     * The KernelRegistry keeps soft references that may be garbage collected.
     */
    private Kernel kernel;

    private AbstractName targetConfigStoreAName;

    private AbstractName targetRepositoryAName;

    private boolean targetSet;

    public void buildPackage() throws Exception {
        getLog().info("Packaging module configuration: " + planFile);

        Kernel kernel = createKernel();
        if (!targetSet) {
            kernel.stopGBean(targetRepositoryAName);
            kernel.setAttribute(targetRepositoryAName, "root", targetRepository.toURI());
            kernel.startGBean(targetRepositoryAName);

            if (kernel.getGBeanState(targetConfigStoreAName) != State.RUNNING_INDEX) {
                throw new IllegalStateException("After restarted repository then config store is not running");
            }

            targetSet = true;
        }

        getLog().debug("Starting configurations..." + Arrays.asList(deploymentConfigs));

        // start the Configuration we're going to use for this deployment
        ConfigurationManager configurationManager = ConfigurationUtil.getConfigurationManager(kernel);
        try {
            for (String artifactName : deploymentConfigs) {
                org.apache.geronimo.kernel.repository.Artifact configName = org.apache.geronimo.kernel.repository.Artifact.create(artifactName);
                if (!configurationManager.isLoaded(configName)) {
                    RecordingLifecycleMonitor monitor = new RecordingLifecycleMonitor();
                    try {
                        configurationManager.loadConfiguration(configName, monitor);
                    } catch (LifecycleException e) {
                        getLog().error("Could not load deployer configuration: " + configName + "\n" + monitor.toString(), e);
                    }
                    monitor = new RecordingLifecycleMonitor();
                    try {
                        configurationManager.startConfiguration(configName, monitor);
                        getLog().info("Started deployer: " + configName);
                    } catch (LifecycleException e) {
                        getLog().error("Could not start deployer configuration: " + configName + "\n" + monitor.toString(), e);
                    }
                }
            }
        } finally {
            ConfigurationUtil.releaseConfigurationManager(kernel, configurationManager);
        }

        getLog().debug("Deploying...");

        AbstractName deployer = locateDeployer(kernel);
        invokeDeployer(kernel, deployer, targetConfigStoreAName.toString());
        //use a fresh kernel for each module
        kernel.shutdown();
        kernel = null;
    }

    /**
     * Create a Geronimo Kernel to contain the deployment configurations.
     */
    private synchronized Kernel createKernel() throws Exception {
        // first return our cached version
        if (kernel != null) {
            return kernel;
        }

        getLog().debug("Creating kernel...");

        // check the registry in case someone else created one
        kernel = KernelRegistry.getKernel(KERNEL_NAME);
        if (kernel != null) {
            return kernel;
        }

        // boot one ourselves
        kernel = KernelFactory.newInstance().createKernel(KERNEL_NAME);
        kernel.boot();

        bootDeployerSystem();

        return kernel;
    }

    /**
     * Boot the in-Maven deployment system.
     * <p/>
     * <p>
     * This contains Repository and ConfigurationStore GBeans that map to
     * the local maven installation.
     * </p>
     */
    private void bootDeployerSystem() throws Exception {
        getLog().debug("Booting deployer system...");

        org.apache.geronimo.kernel.repository.Artifact baseId =
                new org.apache.geronimo.kernel.repository.Artifact("geronimo", "packaging", "fixed", "car");
        Naming naming = kernel.getNaming();
        ConfigurationData bootstrap = new ConfigurationData(baseId, naming);
        ClassLoader cl = getClass().getClassLoader();
        Set<AbstractName> repoNames = new HashSet<AbstractName>();

        //
        // NOTE: Install an adapter for the source repository that will leverage the Maven2 repository subsystem
        //       to allow for better handling of SNAPSHOT values.
        //
        GBeanData repoGBean = bootstrap.addGBean("SourceRepository", GBeanInfo.getGBeanInfo(Maven2RepositoryAdapter.class.getName(), cl));
        Maven2RepositoryAdapter.ArtifactLookup lookup = new Maven2RepositoryAdapter.ArtifactLookup() {
            private Maven2RepositoryAdapter.ArtifactLookup getDelegate() {
                return lookupHolder.get();
            }

            public File getBasedir() {
                return getDelegate().getBasedir();
            }

            public File getLocation(final org.apache.geronimo.kernel.repository.Artifact artifact) {
                return getDelegate().getLocation(artifact);
            }
        };
        repoGBean.setAttribute("lookup", lookup);
        repoGBean.setAttribute("dependencies", dependencies);
        repoNames.add(repoGBean.getAbstractName());

        // Target repo
        GBeanData targetRepoGBean = bootstrap.addGBean("TargetRepository", GBeanInfo.getGBeanInfo(Maven2Repository.class.getName(), cl));
        URI targetRepositoryURI = targetRepository.toURI();
        targetRepoGBean.setAttribute("root", targetRepositoryURI);
        repoNames.add(targetRepoGBean.getAbstractName());
        targetRepositoryAName = targetRepoGBean.getAbstractName();

        GBeanData artifactManagerGBean = bootstrap.addGBean("ArtifactManager", DefaultArtifactManager.GBEAN_INFO);
        GBeanData artifactResolverGBean = bootstrap.addGBean("ArtifactResolver", ExplicitDefaultArtifactResolver.GBEAN_INFO);
        artifactResolverGBean.setAttribute("versionMapLocation", explicitResolutionProperties.getAbsolutePath());
        ReferencePatterns repoPatterns = new ReferencePatterns(repoNames);
        artifactResolverGBean.setReferencePatterns("Repositories", repoPatterns);
        artifactResolverGBean.setReferencePattern("ArtifactManager", artifactManagerGBean.getAbstractName());

        Set storeNames = new HashSet();

        // Source config store
        GBeanInfo configStoreInfo = GBeanInfo.getGBeanInfo(MavenConfigStore.class.getName(), cl);
        GBeanData storeGBean = bootstrap.addGBean("ConfigStore", configStoreInfo);
        if (configStoreInfo.getReference("Repository") != null) {
            storeGBean.setReferencePattern("Repository", repoGBean.getAbstractName());
        }
        storeNames.add(storeGBean.getAbstractName());

        // Target config store
        GBeanInfo targetConfigStoreInfo = GBeanInfo.getGBeanInfo(RepositoryConfigurationStore.class.getName(), cl);
        GBeanData targetStoreGBean = bootstrap.addGBean("TargetConfigStore", targetConfigStoreInfo);
        if (targetConfigStoreInfo.getReference("Repository") != null) {
            targetStoreGBean.setReferencePattern("Repository", targetRepoGBean.getAbstractName());
        }
        storeNames.add(targetStoreGBean.getAbstractName());

        targetConfigStoreAName = targetStoreGBean.getAbstractName();
        targetSet = true;

        GBeanData attrManagerGBean = bootstrap.addGBean("AttributeStore", MavenAttributeStore.GBEAN_INFO);
        GBeanData configManagerGBean = bootstrap.addGBean("ConfigManager", KernelConfigurationManager.GBEAN_INFO);
        configManagerGBean.setReferencePatterns("Stores", new ReferencePatterns(storeNames));
        configManagerGBean.setReferencePattern("AttributeStore", attrManagerGBean.getAbstractName());
        configManagerGBean.setReferencePattern("ArtifactManager", artifactManagerGBean.getAbstractName());
        configManagerGBean.setReferencePattern("ArtifactResolver", artifactResolverGBean.getAbstractName());
        configManagerGBean.setReferencePatterns("Repositories", repoPatterns);

        ConfigurationUtil.loadBootstrapConfiguration(kernel, bootstrap, cl);
    }

    /**
     * Locate a Deployer GBean matching the deployerName pattern.
     *
     * @param kernel the kernel to search.
     * @return the ObjectName of the Deployer GBean
     * @throws IllegalStateException if there is not exactly one GBean matching the deployerName pattern
     */
    private AbstractName locateDeployer(final Kernel kernel) {
        AbstractName name = new AbstractName(URI.create(deployerName));

        Iterator i = kernel.listGBeans(new AbstractNameQuery(name)).iterator();
        if (!i.hasNext()) {
            throw new IllegalStateException("No deployer found matching deployerName: " + name);
        }

        AbstractName deployer = (AbstractName) i.next();
        if (i.hasNext()) {
            throw new IllegalStateException("Multiple deployers found matching deployerName: " + name);
        }

        return deployer;
    }

    private static final String[] DEPLOY_SIGNATURE = {
            boolean.class.getName(),
            File.class.getName(),
            File.class.getName(),
            File.class.getName(),
            Boolean.TYPE.getName(),
            String.class.getName(),
            String.class.getName(),
            String.class.getName(),
            String.class.getName(),
            String.class.getName(),
            String.class.getName(),
            String.class.getName(),
            String.class.getName(),
    };

    private List invokeDeployer(final Kernel kernel, final AbstractName deployer, final String targetConfigStore) throws Exception {
        Object[] args = {
                Boolean.FALSE, // Not in-place
                moduleFile,
                planFile,
                null, // Target file
                Boolean.TRUE, // Install
                null, // main-class
                null, // main-gbean
                null, // main-method
                null, // Manifest configurations
                null, // class-path
                null, // endorsed-dirs
                null, // extension-dirs
                targetConfigStore
        };

        return (List) kernel.invoke(deployer, "deploy", args, DEPLOY_SIGNATURE);
    }

}
