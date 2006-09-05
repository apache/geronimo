/**
 *
 * Copyright 2005 The Apache Software Foundation
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

package org.apache.geronimo.plugin.car;

import java.io.File;
import java.net.URI;

import java.util.ArrayList;
import java.util.List;
import java.util.Iterator;
import java.util.Set;
import java.util.HashSet;

import org.apache.geronimo.deployment.PluginBootstrap2;
import org.apache.geronimo.system.configuration.RepositoryConfigurationStore;
import org.apache.geronimo.system.repository.Maven2Repository;
import org.apache.geronimo.system.resolver.ExplicitDefaultArtifactResolver;
import org.apache.geronimo.kernel.Kernel;
import org.apache.geronimo.kernel.KernelRegistry;
import org.apache.geronimo.kernel.KernelFactory;
import org.apache.geronimo.kernel.Naming;
import org.apache.geronimo.kernel.repository.DefaultArtifactManager;
import org.apache.geronimo.kernel.log.GeronimoLogging;
import org.apache.geronimo.kernel.management.State;
import org.apache.geronimo.kernel.config.ConfigurationManager;
import org.apache.geronimo.kernel.config.ConfigurationUtil;
import org.apache.geronimo.kernel.config.ConfigurationData;
import org.apache.geronimo.kernel.config.KernelConfigurationManager;
import org.apache.geronimo.gbean.AbstractName;
import org.apache.geronimo.gbean.GBeanData;
import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.gbean.ReferencePatterns;
import org.apache.geronimo.gbean.AbstractNameQuery;

import org.apache.geronimo.genesis.ArtifactItem;

import org.apache.maven.archiver.MavenArchiveConfiguration;
import org.apache.maven.archiver.MavenArchiver;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.artifact.Artifact;

import org.codehaus.plexus.util.FileUtils;
import org.codehaus.plexus.archiver.jar.JarArchiver;

/**
 * Build a Geronimo Configuration using the local Maven infrastructure.
 *
 * <p>
 * <b>NOTE:</b> Calling pom.xml must have defined a ${geronimoVersion} property.
 * </p>
 *
 * @goal package
 * @requiresDependencyResolution runtime
 *
 * @version $Rev$ $Date$
 */
public class PackageMojo
    extends AbstractCarMojo
{
    /**
     * The maven archive configuration to use.
     *
     * See <a href="http://maven.apache.org/ref/current/maven-archiver/apidocs/org/apache/maven/archiver/MavenArchiveConfiguration.html">the Javadocs for MavenArchiveConfiguration</a>.
     *
     * @parameter
     */
    private MavenArchiveConfiguration archive = new MavenArchiveConfiguration();

    /**
     * The Jar archiver.
     *
     * @parameter expression="${component.org.codehaus.plexus.archiver.Archiver#jar}"
     * @required
     * @readonly
     */
    private JarArchiver jarArchiver = null;

    /**
     * Directory containing the generated archive.
     *
     * @parameter expression="${project.build.directory}"
     * @required
     */
    private File outputDirectory = null;

    /**
     * Directory containing the classes/resources.
     *
     * @parameter expression="${project.build.outputDirectory}"
     * @required
     */
    private File classesDirectory = null;

    /**
     * Name of the generated archive.
     *
     * @parameter expression="${project.build.finalName}"
     * @required
     */
    private String finalName = null;

    /**
     * ???
     *
     * @parameter expression="${settings.localRepository}"
     * @required
     * @readonly
     */
    private File repository = null;

    /**
     * ???
     *
     * @parameter expression="${project.build.directory}/repository"
     * @required
     */
    private File targetRepository = null;

    /**
     * ???
     *
     * @parameter expression="org.apache.geronimo.configs/geronimo-gbean-deployer/${geronimoVersion}/car"
     * @required
     * @readonly
     */
    private String deafultDeploymentConfig = null;

    /**
     * ???
     *
     * @parameter
     */
    private List deploymentConfigs;

    /**
     * The name of the deployer which will be used to deploy the CAR.
     *
     * @parameter expression="org.apache.geronimo.configs/geronimo-gbean-deployer/${geronimoVersion}/car?j2eeType=Deployer,name=Deployer"
     * @required
     */
    private String deployerName = null;

    /**
     * The plan file for the CAR.
     *
     * @parameter expression="${project.build.directory}/plan/plan.xml"
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
     * An {@link ArtifactItem} to include as a module of the CAR.
     *
     * @parameter
     */
    private ArtifactItem module = null;

    /**
     * The location where the properties mapping will be generated.
     *
     * <p>
     * Probably don't wanto to change this.
     *
     * @parameter expression="${project.build.directory}/explicit-versions.properties"
     */
    private File explicitResolutionProperties = null;

    /**
     * An array of {@link ClasspathElement} objects which will be used to construct the
     * Class-Path entry of the manifest.
     *
     * This is needed to allow per-element prefixes to be added, which the standard Maven archiver
     * does not provide.
     *
     * @parameter
     */
    private ClasspathElement[] classpath = null;

    /**
     * The default prefix to be applied to all elements of the <tt>classpath</tt> which
     * do not provide a prefix.
     *
     * @parameter
     */
    private String classpathPrefix = null;

    /**
     * True to enable the bootshell when packaging.
     *
     * @parameter
     */
    private boolean bootstrap = false;

    //
    // Mojo
    //

    protected void doExecute() throws Exception {
        // We need to make sure to clean up any previous work first or this operation will fail
        FileUtils.forceDelete(targetRepository);
        FileUtils.forceMkdir(targetRepository);

        // Use the default configs if none specified
        if (deploymentConfigs == null) {
            deploymentConfigs = new ArrayList();
            deploymentConfigs.add(deafultDeploymentConfig);
        }
        log.debug("Deployment configs: " + deploymentConfigs);

        // If module is set, then resolve the artifact and set moduleFile
        if (module != null) {
            Artifact artifact = getArtifact(module);
            moduleFile = artifact.getFile();
            log.debug("Using module file: " + moduleFile);
        }

        generateExplicitVersionProperties(explicitResolutionProperties);

        if (bootstrap) {
            executeBootShell();
        }
        else {
            buildPackage();
        }

        // Build the archive
        File archive = createArchive();

        // Attach the generated archive for install/deploy
        project.getArtifact().setFile(archive);
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
        log.debug("Starting bootstrap shell...");

        PluginBootstrap2 boot = new PluginBootstrap2();

        boot.setBuildDir(outputDirectory);
        boot.setCarFile(getArtifactInRepositoryDir());
        boot.setLocalRepo(repository);
        boot.setPlan(planFile);

        // Generate expanded so we can use Maven to generate the archive
        boot.setExpanded(true);

        boot.bootstrap();
    }

    /**
     * Generates the configuration archive.
     */
    private File createArchive() throws MojoExecutionException {
        File archiveFile = getArchiveFile(outputDirectory, finalName, null);

        MavenArchiver archiver = new MavenArchiver();
        archiver.setArchiver(jarArchiver);
        archiver.setOutputFile(archiveFile);

        try {
            // Incldue the generated artifact contents
            archiver.getArchiver().addDirectory(getArtifactInRepositoryDir());

            // Include the optional classes.resources
            if (classesDirectory.isDirectory()) {
                archiver.getArchiver().addDirectory(classesDirectory);
            }

            if (classpath != null) {
                archive.addManifestEntry("Class-Path", getClassPath());
            }

            archiver.createArchive(project, archive);

            return archiveFile;
        }
        catch (Exception e) {
            throw new MojoExecutionException("Failed to create archive", e);
        }
    }

    private String getClassPath() throws MojoExecutionException {
        StringBuffer buff = new StringBuffer();

        for (int i=0; i < classpath.length; i++) {
            Artifact artifact = getArtifact(classpath[i]);

            //
            // TODO: Need to optionally get all transitive dependencies... but dunno how to get that intel from m2
            //

            String prefix = classpath[i].getClasspathPrefix();
            if (prefix == null) {
                prefix = classpathPrefix;
            }

            if (prefix != null) {
                buff.append(prefix);

                if (!prefix.endsWith("/")) {
                    buff.append("/");
                }
            }

            File file = artifact.getFile();
            buff.append(file.getName());

            if (i + 1< classpath.length) {
                buff.append(" ");
            }
        }

        log.debug("Using classpath: " + buff);

        return buff.toString();
    }

    //
    // Deployment
    //

    private static final String KERNEL_NAME = "geronimo.maven";

    /**
     * Reference to the kernel that will last the lifetime of this classloader.
     * The KernelRegistry keeps soft references that may be garbage collected.
     */
    private static Kernel kernel;

    private static AbstractName targetConfigStoreAName;

    private static AbstractName targetRepositoryAName;

    private boolean targetSet;

    public void buildPackage() throws Exception {
        log.info("Packaging module configuration: " + planFile);


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

        log.debug("Starting configuration...");

        // start the Configuration we're going to use for this deployment
        ConfigurationManager configurationManager = ConfigurationUtil.getConfigurationManager(kernel);
        try {
            for (Iterator iterator = deploymentConfigs.iterator(); iterator.hasNext();) {
                String artifactName = (String) iterator.next();
                org.apache.geronimo.kernel.repository.Artifact configName =
                        org.apache.geronimo.kernel.repository.Artifact.create(artifactName);
                if (!configurationManager.isLoaded(configName)) {
                    configurationManager.loadConfiguration(configName);
                    configurationManager.startConfiguration(configName);
                }
            }
        } finally {
            ConfigurationUtil.releaseConfigurationManager(kernel, configurationManager);
        }

        log.debug("Deploying...");

        AbstractName deployer = locateDeployer(kernel);
        invokeDeployer(kernel, deployer, targetConfigStoreAName.toString());
    }

    /**
     * Create a Geronimo Kernel to contain the deployment configurations.
     */
    private synchronized Kernel createKernel() throws Exception {
        // first return our cached version
        if (kernel != null) {
            return kernel;
        }

        log.debug("Creating kernel...");

        // check the registry in case someone else created one
        kernel = KernelRegistry.getKernel(KERNEL_NAME);
        if (kernel != null) {
            return kernel;
        }

        GeronimoLogging geronimoLogging = GeronimoLogging.getGeronimoLogging("WARN");
        if (geronimoLogging == null) {
            geronimoLogging = GeronimoLogging.DEBUG;
        }
        GeronimoLogging.initialize(geronimoLogging);

        // boot one ourselves
        kernel = KernelFactory.newInstance().createKernel(KERNEL_NAME);
        kernel.boot();

        bootDeployerSystem();

        return kernel;
    }

    /**
     * Boot the in-Maven deployment system.
     *
     * <p>
     * This contains Repository and ConfigurationStore GBeans that map to
     * the local maven installation.
     */
    private void bootDeployerSystem() throws Exception {
        log.debug("Booting deployer system...");

        org.apache.geronimo.kernel.repository.Artifact baseId =
                new org.apache.geronimo.kernel.repository.Artifact("geronimo", "packaging", "fixed", "car");
        Naming naming = kernel.getNaming();
        ConfigurationData bootstrap = new ConfigurationData(baseId, naming);
        ClassLoader cl = getClass().getClassLoader();
        Set repoNames = new HashSet();

        // Source repo
        GBeanData repoGBean = bootstrap.addGBean("SourceRepository", GBeanInfo.getGBeanInfo(Maven2Repository.class.getName(), cl));
        URI repositoryURI = repository.toURI();
        repoGBean.setAttribute("root", repositoryURI);
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
     *
     * @throws IllegalStateException if there is not exactly one GBean matching the deployerName pattern
     */
    private AbstractName locateDeployer(final Kernel kernel) {
        AbstractName name = new AbstractName(URI.create(deployerName));

        Iterator i = kernel.listGBeans(new AbstractNameQuery(name)).iterator();
        if (!i.hasNext()) {
            throw new IllegalStateException("No deployer found matching deployerName: " + name);
        }

        AbstractName deployer = (AbstractName)i.next();
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
            planFile,
            moduleFile,
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
