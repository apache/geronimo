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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.geronimo.gbean.GBeanData;
import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.gbean.GReferenceInfo;
import org.apache.geronimo.gbean.AbstractName;
import org.apache.geronimo.kernel.Kernel;
import org.apache.geronimo.kernel.KernelFactory;
import org.apache.geronimo.kernel.KernelRegistry;
import org.apache.geronimo.kernel.config.ConfigurationManager;
import org.apache.geronimo.kernel.config.KernelConfigurationManager;
import org.apache.geronimo.kernel.config.ConfigurationUtil;
import org.apache.geronimo.kernel.repository.Artifact;
import org.apache.geronimo.kernel.repository.DefaultArtifactManager;
import org.apache.geronimo.kernel.repository.DefaultArtifactResolver;
import org.apache.log4j.BasicConfigurator;

import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import java.io.File;
import java.net.URI;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.Map;
import java.util.HashMap;

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

    private static final String[] ARG_TYPES = {
            File.class.getName(),
            File.class.getName(),
            File.class.getName(),
            Boolean.TYPE.getName(),
            String.class.getName(),
            String.class.getName(),
            String.class.getName(),
            String.class.getName(),
    };

    private String repositoryClass;
    private String configurationStoreClass;

    private File repository;
    private String deploymentConfigString;
    private Artifact[] deploymentConfig;
    private ObjectName deployerName;

    private File planFile;
    private File moduleFile;
    private File packageFile;
    private String mainClass;
    private String classPath;
    private String endorsedDirs;
    private String extensionDirs;

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

    public String getDeploymentConfig() {
        return deploymentConfigString;
    }

    /**
     * Set the id of the Configuration to use to perform the packaging.
     *
     * @param deploymentConfigString comma-separated list of the ids of the Configurations performing the deployment
     */
    public void setDeploymentConfig(String deploymentConfigString) {
        this.deploymentConfigString = deploymentConfigString;
        String[] configNames = deploymentConfigString.split(",");
        deploymentConfig = new Artifact[configNames.length];
        for (int i = 0; i < configNames.length; i++) {
            String configName = configNames[i];
            deploymentConfig[i] = Artifact.create(configName);
        }
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
        try {
            this.deployerName = new ObjectName(deployerName);
        } catch (MalformedObjectNameException e) {
            throw new IllegalArgumentException("deployerName is not a valid ObjectName: " + deployerName);
        }
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

    public void execute() throws Exception {
        System.out.println();
        System.out.println("    Packaging configuration " + planFile);
        System.out.println();
        try {
            Kernel kernel = createKernel(repository, repositoryClass, configurationStoreClass);

            // start the Configuration we're going to use for this deployment
            ConfigurationManager configurationManager = ConfigurationUtil.getConfigurationManager(kernel);
            try {
                for (int i = 0; i < deploymentConfig.length; i++) {
                    Artifact configName = deploymentConfig[i];
                    if (!configurationManager.isLoaded(configName)) {
                        configurationManager.loadConfiguration(configName);
                        configurationManager.startConfiguration(configName);
                    }
                }
            } finally {
                ConfigurationUtil.releaseConfigurationManager(kernel, configurationManager);
            }

            ObjectName deployer = locateDeployer(kernel);
            invokeDeployer(kernel, deployer);
            System.out.println("Generated package " + packageFile);
        } catch (Exception e) {
            log.error(e.getClass().getName() + ": " + e.getMessage(), e);
            throw e;
        }
    }

    /**
     * Create a Geronimo Kernel to contain the deployment configurations.
     */
    private static synchronized Kernel createKernel(File repository, String repoClass, String configStoreClass) throws Exception {
        // first return our cached version
        if (kernel != null) {
            return kernel;
        }

        // check the registry in case someone else created one
        kernel = KernelRegistry.getKernel(KERNEL_NAME);
        if (kernel != null) {
            return kernel;
        }

        BasicConfigurator.configure();
        // boot one ourselves
        kernel = KernelFactory.newInstance().createKernel(KERNEL_NAME);
        kernel.boot();

        bootDeployerSystem(kernel, repository, repoClass, configStoreClass);

        return kernel;
    }

    /**
     * Boot the in-Maven deployment system.
     * This contains Repository and ConfigurationStore GBeans that map to
     * the local maven installation.
     */
    private static void bootDeployerSystem(Kernel kernel, File repository, String repoClass, String configStoreClass) throws Exception {
        Artifact artifact = new Artifact("geronimo", "packaging", "fixed", "car");
        Map nameMap = new HashMap();
        nameMap.put("type", "plugin");
        nameMap.put("name", "packaging");
        ObjectName objectName;
        try {
            objectName = ObjectName.getInstance(KERNEL_NAME + "j2eeType=plugin,name=packaging");
        } catch (MalformedObjectNameException e) {
            throw (IllegalArgumentException)new IllegalArgumentException("Could not construct a fixed object name").initCause(e);
        }
        AbstractName rootName = new AbstractName(artifact, nameMap, objectName);
        AbstractName repositoryName = kernel.getNaming().createChildName(rootName, "Repository", "Repository");
        AbstractName artifactManagerName = kernel.getNaming().createChildName(rootName, "ArtifactManager", "ArtifactManager");
        AbstractName artifactResolverName = kernel.getNaming().createChildName(rootName, "ArtifactResolver", "ArtifactResolver");
        AbstractName configStoreName = kernel.getNaming().createChildName(rootName, "PackageBuilderConfigStore", "ConfigurationStore");
        AbstractName configManagerName = kernel.getNaming().createChildName(rootName, "ConfigurationManager", "ConfigurationManager");
        AbstractName attributeStoreName = kernel.getNaming().createChildName(rootName, "ManagedAttributeStore", "ManagedAttributeStore");

        ClassLoader cl = PackageBuilder.class.getClassLoader();
        GBeanInfo repoInfo = GBeanInfo.getGBeanInfo(repoClass, cl);
        GBeanData repoGBean = new GBeanData(repositoryName, repoInfo);
        URI repositoryURI = repository.toURI();
        repoGBean.setAttribute("root", repositoryURI);
        kernel.loadGBean(repoGBean, cl);
        kernel.startGBean(repositoryName);

        //TODO parameterize these?
        GBeanData artifactManagerGBean = new GBeanData(artifactManagerName, DefaultArtifactManager.GBEAN_INFO);
        kernel.loadGBean(artifactManagerGBean, cl);
        kernel.startGBean(artifactManagerName);

        GBeanData artifactResolverGBean = new GBeanData(artifactResolverName, DefaultArtifactResolver.GBEAN_INFO);
        artifactResolverGBean.setReferencePattern("Repositories", repositoryName);
        artifactResolverGBean.setReferencePattern("ArtifactManager", artifactManagerName);
        kernel.loadGBean(artifactResolverGBean, cl);
        kernel.startGBean(artifactResolverName);

        GBeanInfo configStoreInfo = GBeanInfo.getGBeanInfo(configStoreClass, cl);
        GBeanData storeGBean = new GBeanData(configStoreName, configStoreInfo);
        Set refs = configStoreInfo.getReferences();
        for (Iterator iterator = refs.iterator(); iterator.hasNext();) {
            GReferenceInfo refInfo = (GReferenceInfo) iterator.next();
            if ("Repository".equals(refInfo.getName())) {
                storeGBean.setReferencePattern("Repository", repositoryName);
                break;
            }
        }
        kernel.loadGBean(storeGBean, cl);
        kernel.startGBean(configStoreName);

        GBeanData configManagerGBean = new GBeanData(configManagerName, KernelConfigurationManager.GBEAN_INFO);
        configManagerGBean.setReferencePattern("Stores", configStoreName);
        configManagerGBean.setReferencePattern("AttributeStore", attributeStoreName);
        configManagerGBean.setReferencePattern("ArtifactManager", artifactManagerName);
        configManagerGBean.setReferencePattern("ArtifactResolver", artifactResolverName);
        kernel.loadGBean(configManagerGBean, cl);
        kernel.startGBean(configManagerName);

        GBeanData attrManagerGBean = new GBeanData(attributeStoreName, MavenAttributeStore.GBEAN_INFO);
        kernel.loadGBean(attrManagerGBean, cl);
        kernel.startGBean(attributeStoreName);
    }

    /**
     * Locate a Deployer GBean matching the deployerName pattern.
     *
     * @param kernel the kernel to search.
     * @return the ObjectName of the Deployer GBean
     * @throws IllegalStateException if there is not exactly one GBean matching the deployerName pattern
     */
    private ObjectName locateDeployer(Kernel kernel) {
        Iterator i = kernel.listGBeans(deployerName).iterator();
        if (!i.hasNext()) {
            throw new IllegalStateException("No deployer found matching deployerName: " + deployerName);
        }
        ObjectName deployer = (ObjectName) i.next();
        if (i.hasNext()) {
            throw new IllegalStateException("Multiple deployers found matching deployerName: " + deployerName);
        }
        return deployer;
    }

    private List invokeDeployer(Kernel kernel, ObjectName deployer) throws Exception {
        boolean isExecutable = mainClass != null;
        Object[] args = {planFile, moduleFile, isExecutable ? packageFile : null, Boolean.valueOf(!isExecutable), mainClass, classPath, endorsedDirs, extensionDirs};
        return (List) kernel.invoke(deployer, "deploy", args, ARG_TYPES);
    }
}
