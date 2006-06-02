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
package org.apache.geronimo.plugin.packaging;

import java.io.File;
import java.net.URI;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
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
import org.apache.geronimo.kernel.log.GeronimoLogging;
import org.apache.geronimo.kernel.management.State;
import org.apache.geronimo.kernel.repository.Artifact;
import org.apache.geronimo.kernel.repository.DefaultArtifactManager;
import org.apache.geronimo.system.resolver.ExplicitDefaultArtifactResolver;

/**
 * JellyBean that builds a Geronimo Configuration using the local Mavem
 * infrastructure.
 *
 * @version $Rev:385659 $ $Date$
 */
public class PackageBuilder {

    private static Log log = LogFactory.getLog(PackageBuilder.class);

    private static final String KERNEL_NAME = "geronimo.maven";

    /**
     * Reference to the kernel that will last the lifetime of this classloader.
     * The KernelRegistry keeps soft references that may be garbage collected.
     */
    private static Kernel kernel;
    private static AbstractName targetConfigStoreAName;
    private static AbstractName targetRepositoryAName;

    private static final String[] ARG_TYPES = {
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

    private String repositoryClass;
    private String configurationStoreClass;
    private String targetRepositoryClass;
    private String targetConfigurationStoreClass;

    private File repository;
    private File targetRepository;
    private Collection deploymentConfigs;
//    private Artifact[] deploymentConfig;
    private AbstractName deployerName;

    private File planFile;
    private File moduleFile;
    private File packageFile;
    private String mainClass;
    private String mainGBean;
    private String mainMethod;
    private String configurations;
    private String classPath;
    private String endorsedDirs;
    private String extensionDirs;
    private String explicitResolutionLocation;
    private String logLevel;

    private boolean targetSet;
    private boolean singleArtifact = true;

    public String getRepositoryClass() {
        return repositoryClass;
    }

    public void setRepositoryClass(String repositoryClass) {
        this.repositoryClass = repositoryClass;
    }

    public String getConfigurationStoreClass() {
        return configurationStoreClass;
    }

    public void setConfigurationStoreClass(String configurationStoreClass) {
        this.configurationStoreClass = configurationStoreClass;
    }


    public File getRepository() {
        return repository;
    }

    /**
     * Set the location of the Maven repository; typically ${maven.repo.local}
     *
     * @param repository the location of the Maven repository
     */
    public void setRepository(File repository) {
        this.repository = repository;
    }

    public String getTargetRepositoryClass() {
        return targetRepositoryClass;
    }

    public void setTargetRepositoryClass(String targetRepositoryClass) {
        this.targetRepositoryClass = targetRepositoryClass;
    }

    public String getTargetConfigurationStoreClass() {
        return targetConfigurationStoreClass;
    }

    public void setTargetConfigurationStoreClass(String targetConfigurationStoreClass) {
        this.targetConfigurationStoreClass = targetConfigurationStoreClass;
    }

    public File getTargetRepository() {
        return targetRepository;
    }

    public void setTargetRepository(File targetRepository) {
        this.targetRepository = targetRepository;
    }

    public Collection getDeploymentConfig() {
        return deploymentConfigs;
    }

    /**
     * Set the id of the Configuration to use to perform the packaging.
     *
     * @param deploymentConfigString comma-separated list of the ids of the Configurations performing the deployment
     */
    public void setDeploymentConfig(Collection deploymentConfigString) {
        this.deploymentConfigs = deploymentConfigString;
    }

    public String getDeployerName() {
        return deployerName.toString();
    }

    /**
     * Set the name of the GBean that is the Deployer.
     *
     * @param deployerName the name of the Deployer GBean
     */
    public void setDeployerName(String deployerName) {
        this.deployerName = new AbstractName(URI.create(deployerName));
    }

    public File getPlanFile() {
        return planFile;
    }

    /**
     * Set the File that is the deployment plan.
     *
     * @param planFile the deployment plan
     */
    public void setPlanFile(File planFile) {
        this.planFile = planFile;
    }

    public File getModuleFile() {
        return moduleFile;
    }

    /**
     * Set the File that is the module being deployed.
     *
     * @param moduleFile the module to deploy
     */
    public void setModuleFile(File moduleFile) {
        this.moduleFile = moduleFile;
    }

    public File getPackageFile() {
        return packageFile;
    }

    /**
     * Set the File where the Configuration will be stored; normally the artifact being produced.
     *
     * @param packageFile the package file to produce
     */
    public void setPackageFile(File packageFile) {
        this.packageFile = packageFile;
    }

    public String getMainClass() {
        return mainClass;
    }

    /**
     * Set the name of the class containing the main method for a executable configuration.
     *
     * @param mainClass
     */
    public void setMainClass(String mainClass) {
        this.mainClass = mainClass;
    }

    public String getMainGBean() {
        return mainGBean;
    }

    public void setMainGBean(String mainGBean) {
        this.mainGBean = mainGBean;
    }

    public String getMainMethod() {
        return mainMethod;
    }

    public void setMainMethod(String mainMethod) {
        this.mainMethod = mainMethod;
    }

    public String getConfigurations() {
        return configurations;
    }

    public void setConfigurations(String configurations) {
        this.configurations = configurations;
    }

    public String getClassPath() {
        return classPath;
    }

    public void setClassPath(String classPath) {
        this.classPath = classPath;
    }

    public String getEndorsedDirs() {
        return endorsedDirs;
    }

    public void setEndorsedDirs(String endorsedDirs) {
        this.endorsedDirs = endorsedDirs;
    }

    public String getExtensionDirs() {
        return extensionDirs;
    }

    public void setExtensionDirs(String extensionDirs) {
        this.extensionDirs = extensionDirs;
    }

    public String getExplicitResolutionLocation() {
        return explicitResolutionLocation;
    }

    public void setExplicitResolutionLocation(String explicitResolutionLocation) {
        this.explicitResolutionLocation = explicitResolutionLocation;
    }

    public String getLogLevel() {
        return logLevel;
    }

    public void setLogLevel(String logLevel) {
        this.logLevel = logLevel;
    }

    public void execute() throws Exception {
        System.out.println();
        System.out.println("    Packaging configuration " + planFile);
        System.out.println();
        try {
            Kernel kernel = createKernel();
            if(!targetSet) {
                setTargetConfigStore();
            }

            // start the Configuration we're going to use for this deployment
            ConfigurationManager configurationManager = ConfigurationUtil.getConfigurationManager(kernel);
            try {
                for (Iterator iterator = deploymentConfigs.iterator(); iterator.hasNext();) {
                    String artifactName = (String) iterator.next();
                    Artifact configName = Artifact.create(artifactName);
                    if (!configurationManager.isLoaded(configName)) {
                        configurationManager.loadConfiguration(configName);
                        configurationManager.startConfiguration(configName);
                    }
                }
            } finally {
                ConfigurationUtil.releaseConfigurationManager(kernel, configurationManager);
            }

            AbstractName deployer = locateDeployer(kernel);
            invokeDeployer(kernel, deployer, targetConfigStoreAName.toString());
            System.out.println("Generated package " + packageFile);
        } catch (Exception e) {
            log.error(e.getClass().getName() + ": " + e.getMessage(), e);
            throw e;
        }
    }

    private void setTargetConfigStore() throws Exception {
        try {
            kernel.stopGBean(targetRepositoryAName);
            kernel.setAttribute(targetRepositoryAName, "root", targetRepository.toURI());
            kernel.startGBean(targetRepositoryAName);
            if(kernel.getGBeanState(targetConfigStoreAName) != State.RUNNING_INDEX) {
                throw new IllegalStateException("After restarted repository then config store is not running");
            }
            targetSet = true;
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * Create a Geronimo Kernel to contain the deployment configurations.
     */
    private synchronized Kernel createKernel() throws Exception {
        // first return our cached version
        if (kernel != null) {
            return kernel;
        }

        // check the registry in case someone else created one
        kernel = KernelRegistry.getKernel(KERNEL_NAME);
        if (kernel != null) {
            return kernel;
        }

        GeronimoLogging geronimoLogging = GeronimoLogging.getGeronimoLogging(logLevel);
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
     * This contains Repository and ConfigurationStore GBeans that map to
     * the local maven installation.
     */
    private void bootDeployerSystem() throws Exception {
        Artifact baseId = new Artifact("geronimo", "packaging", "fixed", "car");
        Naming naming = kernel.getNaming();
        ConfigurationData bootstrap = new ConfigurationData(baseId, naming);
        ClassLoader cl = PackageBuilder.class.getClassLoader();
        Set repoNames = new HashSet();

        //Source repo
        GBeanData repoGBean = bootstrap.addGBean("SourceRepository", GBeanInfo.getGBeanInfo(repositoryClass, cl));
        URI repositoryURI = repository.toURI();
        repoGBean.setAttribute("root", repositoryURI);
        repoNames.add(repoGBean.getAbstractName());

        //Target repo
        GBeanData targetRepoGBean = bootstrap.addGBean("TargetRepository", GBeanInfo.getGBeanInfo(targetRepositoryClass, cl));
        URI targetRepositoryURI = targetRepository.toURI();
        targetRepoGBean.setAttribute("root", targetRepositoryURI);
        repoNames.add(targetRepoGBean.getAbstractName());
        targetRepositoryAName = targetRepoGBean.getAbstractName();

        GBeanData artifactManagerGBean = bootstrap.addGBean("ArtifactManager", DefaultArtifactManager.GBEAN_INFO);

        GBeanData artifactResolverGBean = bootstrap.addGBean("ArtifactResolver", ExplicitDefaultArtifactResolver.GBEAN_INFO);
        artifactResolverGBean.setAttribute("versionMapLocation", explicitResolutionLocation);
        ReferencePatterns repoPatterns = new ReferencePatterns(repoNames);
        artifactResolverGBean.setReferencePatterns("Repositories", repoPatterns);
        artifactResolverGBean.setReferencePattern("ArtifactManager", artifactManagerGBean.getAbstractName());

        Set storeNames = new HashSet();
        //source config store
        GBeanInfo configStoreInfo = GBeanInfo.getGBeanInfo(configurationStoreClass, cl);
        GBeanData storeGBean = bootstrap.addGBean("ConfigStore", configStoreInfo);
        if (configStoreInfo.getReference("Repository") != null) {
            storeGBean.setReferencePattern("Repository", repoGBean.getAbstractName());
        }
        storeNames.add(storeGBean.getAbstractName());

        //target config store
        GBeanInfo targetConfigStoreInfo = GBeanInfo.getGBeanInfo(targetConfigurationStoreClass, cl);
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
    private AbstractName locateDeployer(Kernel kernel) {
        Iterator i = kernel.listGBeans(new AbstractNameQuery(deployerName)).iterator();
        if (!i.hasNext()) {
            throw new IllegalStateException("No deployer found matching deployerName: " + deployerName);
        }
        AbstractName deployer = (AbstractName) i.next();
        if (i.hasNext()) {
            throw new IllegalStateException("Multiple deployers found matching deployerName: " + deployerName);
        }
        return deployer;
    }

    private List invokeDeployer(Kernel kernel, AbstractName deployer, String targetConfigStore) throws Exception {
        boolean isExecutable = mainClass != null;
        Object[] args = {Boolean.FALSE, planFile, moduleFile, singleArtifact ? packageFile : null, Boolean.valueOf(!isExecutable), mainClass, mainGBean, mainMethod, configurations, classPath, endorsedDirs, extensionDirs, targetConfigStore};
        return (List) kernel.invoke(deployer, "deploy", args, ARG_TYPES);
    }
}
