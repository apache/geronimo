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
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;

import org.apache.geronimo.gbean.GBeanData;
import org.apache.geronimo.gbean.GReferenceInfo;
import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.kernel.Kernel;
import org.apache.geronimo.kernel.KernelFactory;
import org.apache.geronimo.kernel.KernelRegistry;
import org.apache.geronimo.kernel.config.ConfigurationManager;
import org.apache.geronimo.kernel.config.ConfigurationManagerImpl;
import org.apache.geronimo.kernel.config.ConfigurationUtil;
import org.apache.log4j.BasicConfigurator;

/**
 * JellyBean that builds a Geronimo Configuration using the local Mavem
 * infrastructure.
 *
 * @version $Rev$ $Date$
 */
public class PackageBuilder {
    private static final String KERNEL_NAME = "geronimo.maven";
    /**
     * The name of the GBean that will load dependencies from the Maven repository.
     */
    private static final ObjectName REPOSITORY_NAME;

    /**
     * The name of the GBean that will load Configurations from the Maven repository.
     */
    private static final ObjectName CONFIGSTORE_NAME;

    /**
     * The name of the GBean that will manage Configurations.
     */
    private static final ObjectName CONFIGMANAGER_NAME;

    /**
     * The name of the GBean that will provide values for managed attributes.
     */
    private static final ObjectName ATTRIBUTESTORE_NAME;

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

    static {
        try {
            REPOSITORY_NAME = new ObjectName(KERNEL_NAME + ":name=Repository");
            CONFIGSTORE_NAME = new ObjectName(KERNEL_NAME + ":name=MavenConfigStore,j2eeType=ConfigurationStore");
            CONFIGMANAGER_NAME = new ObjectName(KERNEL_NAME + ":name=ConfigurationManager,j2eeType=ConfigurationManager");
            ATTRIBUTESTORE_NAME = new ObjectName(KERNEL_NAME + ":name=ManagedAttributeStore");
        } catch (MalformedObjectNameException e) {
            throw new ExceptionInInitializerError(e.getMessage());
        }
    }

    private String repositoryClass;
    private String configurationStoreClass;

    private File repository;
    private String deploymentConfigString;
    private URI[] deploymentConfig;
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
        deploymentConfig = new URI[configNames.length];
        for (int i = 0; i < configNames.length; i++) {
            String configName = configNames[i];
            deploymentConfig[i] = URI.create(configName);
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
        Kernel kernel = createKernel(repository, repositoryClass, configurationStoreClass);

        // start the Configuration we're going to use for this deployment
        ConfigurationManager configurationManager = ConfigurationUtil.getConfigurationManager(kernel);
        try {
            for (int i = 0; i < deploymentConfig.length; i++) {
                URI configName = deploymentConfig[i];
                if (!configurationManager.isLoaded(configName)) {
                    List configs = configurationManager.loadRecursive(configName);
                    for (Iterator iterator = configs.iterator(); iterator.hasNext(); ) {
                        URI ancestorConfigName = (URI) iterator.next();
                        try {
                            configurationManager.loadGBeans(ancestorConfigName);
                        } catch (Throwable e) {
                            throw new RuntimeException("Could not start configuration: " + configName, e);
                        }
                    }
                    configurationManager.start(configName);
                }
            }
        } finally {
            ConfigurationUtil.releaseConfigurationManager(kernel, configurationManager);
        }

        ObjectName deployer = locateDeployer(kernel);
        invokeDeployer(kernel, deployer);
        System.out.println("Generated package " + packageFile);
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
        ClassLoader cl = PackageBuilder.class.getClassLoader();
        GBeanInfo repoInfo = GBeanInfo.getGBeanInfo(repoClass, cl);
        GBeanData repoGBean = new GBeanData(REPOSITORY_NAME, repoInfo);
        repoGBean.setAttribute("root", repository);
        kernel.loadGBean(repoGBean, cl);
        kernel.startGBean(REPOSITORY_NAME);

        GBeanInfo configStoreInfo = GBeanInfo.getGBeanInfo(configStoreClass, cl);
        GBeanData storeGBean = new GBeanData(CONFIGSTORE_NAME, configStoreInfo);
        Set refs = configStoreInfo.getReferences();
        for (Iterator iterator = refs.iterator(); iterator.hasNext();) {
            GReferenceInfo refInfo = (GReferenceInfo) iterator.next();
            if ("Repository".equals(refInfo.getName())) {
                storeGBean.setReferencePattern("Repository", REPOSITORY_NAME);
                break;
            }
        }
        kernel.loadGBean(storeGBean, cl);
        kernel.startGBean(CONFIGSTORE_NAME);

        GBeanData configManagerGBean = new GBeanData(CONFIGMANAGER_NAME, ConfigurationManagerImpl.GBEAN_INFO);
        configManagerGBean.setReferencePattern("Stores", CONFIGSTORE_NAME);
        configManagerGBean.setReferencePattern("AttributeStore", ATTRIBUTESTORE_NAME);
        kernel.loadGBean(configManagerGBean, cl);
        kernel.startGBean(CONFIGMANAGER_NAME);

        GBeanData attrManagerGBean = new GBeanData(ATTRIBUTESTORE_NAME, MavenAttributeStore.GBEAN_INFO);
        kernel.loadGBean(attrManagerGBean, cl);
        kernel.startGBean(ATTRIBUTESTORE_NAME);
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
        Object[] args = {planFile, moduleFile, packageFile, Boolean.FALSE, mainClass, classPath, endorsedDirs, extensionDirs};
        return (List) kernel.invoke(deployer, "deploy", args, ARG_TYPES);
    }
}
