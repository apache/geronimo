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
import java.util.jar.Attributes;
import java.util.jar.Manifest;

import org.apache.geronimo.deployment.service.ServiceConfigBuilder;
import org.apache.geronimo.deployment.util.DeploymentUtil;
import org.apache.geronimo.deployment.xbeans.ConfigurationDocument;
import org.apache.geronimo.deployment.xbeans.ConfigurationType;
import org.apache.geronimo.kernel.config.ConfigurationData;
import org.apache.geronimo.system.configuration.ExecutableConfigurationUtil;
import org.apache.geronimo.system.configuration.LocalConfigStore;
import org.apache.geronimo.system.main.CommandLineManifest;
import org.apache.geronimo.system.repository.ReadOnlyRepository;

/**
 * Helper class to bootstrap the Geronimo deployer.
 *
 * @version $Rev$ $Date$
 */
public class Bootstrap {
    private String deployerJar;
    private String storeDir;
    private String repositoryDir;
    private String deployerSystemPlan;
    private String j2eeDeployerPlan;
    private String deployerClassPath;
    private String deployerEndorsedDirs;
    private String deployerExtensionDirs;
    private String deployerGBean;
    private String deploymentFactory;

    public String getDeployerJar() {
        return deployerJar;
    }

    public void setDeployerJar(String deployerJar) {
        this.deployerJar = deployerJar;
    }

    public String getStoreDir() {
        return storeDir;
    }

    public void setStoreDir(String storeDir) {
        this.storeDir = storeDir;
    }

    public String getRepositoryDir() {
        return repositoryDir;
    }

    public void setRepositoryDir(String repositoryDir) {
        this.repositoryDir = repositoryDir;
    }

    public String getDeployerSystemPlan() {
        return deployerSystemPlan;
    }

    public void setDeployerSystemPlan(String deployerSystemPlan) {
        this.deployerSystemPlan = deployerSystemPlan;
    }

    public String getJ2eeDeployerPlan() {
        return j2eeDeployerPlan;
    }

    public void setJ2eeDeployerPlan(String j2eeDeployerPlan) {
        this.j2eeDeployerPlan = j2eeDeployerPlan;
    }

    public String getDeployerClassPath() {
        return deployerClassPath;
    }

    public void setDeployerClassPath(String deployerClassPath) {
        this.deployerClassPath = deployerClassPath;
    }

    public String getDeployerEndorsedDirs() {
        return deployerEndorsedDirs;
    }

    public void setDeployerEndorsedDirs(String deployerEndorsedDirs) {
        this.deployerEndorsedDirs = deployerEndorsedDirs;
    }

    public String getDeployerExtensionDirs() {
        return deployerExtensionDirs;
    }

    public void setDeployerExtensionDirs(String deployerExtensionDirs) {
        this.deployerExtensionDirs = deployerExtensionDirs;
    }

    public String getDeployerGBean() {
        return deployerGBean;
    }

    public void setDeployerGBean(String deployerGBean) {
        this.deployerGBean = deployerGBean;
    }

    public String getDeploymentFactory() {
        return deploymentFactory;
    }

    public void setDeploymentFactory(String deploymentFactory) {
        this.deploymentFactory = deploymentFactory;
    }

    public void bootstrap() throws Exception {
        ClassLoader oldCL = Thread.currentThread().getContextClassLoader();
        try {
            Thread.currentThread().setContextClassLoader(Bootstrap.class.getClassLoader());

            // parse the deployment-system and j2ee-deployer plans
            ConfigurationType deployerSystemConfig = ConfigurationDocument.Factory.parse(new File(deployerSystemPlan)).getConfiguration();
            ConfigurationType j2eeDeployerConfig = ConfigurationDocument.Factory.parse(new File(j2eeDeployerPlan)).getConfiguration();

            // create the service builder, repository and config store objects
            LocalConfigStore configStore = new LocalConfigStore(new File(storeDir));
            ReadOnlyRepository repository = new ReadOnlyRepository(new File(repositoryDir));

            //TODO should the defaultParentId be null??
            ServiceConfigBuilder builder = new ServiceConfigBuilder(null, repository);

            // create the manifext
            Manifest manifest = new Manifest();
            Attributes mainAttributes = manifest.getMainAttributes();
            mainAttributes.putValue(Attributes.Name.MANIFEST_VERSION.toString(), "1.0");
            mainAttributes.putValue(Attributes.Name.MAIN_CLASS.toString(), "org.apache.geronimo.deployment.cli.DeployTool");
            mainAttributes.putValue(Attributes.Name.CLASS_PATH.toString(), deployerClassPath);
            mainAttributes.putValue(CommandLineManifest.MAIN_GBEAN.toString(), deployerGBean);
            mainAttributes.putValue(CommandLineManifest.MAIN_METHOD.toString(), "deploy");
            mainAttributes.putValue(CommandLineManifest.CONFIGURATIONS.toString(), j2eeDeployerConfig.getConfigId());
            mainAttributes.putValue(CommandLineManifest.ENDORSED_DIRS.toString(), deployerEndorsedDirs);
            mainAttributes.putValue(CommandLineManifest.EXTENSION_DIRS.toString(), deployerExtensionDirs);

            // attribute that indicates to a JSR-88 tool that we have a Deployment factory
            mainAttributes.putValue("J2EE-DeploymentFactory-Implementation-Class", deploymentFactory);

            // write the deployer system out to a jar

            // create a temp directory to build into
            File configurationDir = null;
            try {
                configurationDir = configStore.createNewConfigurationDir();

                // build the deployer-system configuration into the configurationDir
                ConfigurationData configurationData = builder.buildConfiguration(deployerSystemConfig, null, configurationDir);

                ExecutableConfigurationUtil.createExecutableConfiguration(configurationData, manifest, configurationDir, new File(deployerJar));

                // install the configuration
                configStore.install(configurationData, configurationDir);
            } catch (Throwable e) {
                DeploymentUtil.recursiveDelete(configurationDir);
                if (e instanceof Error) {
                    throw (Error) e;
                } else if (e instanceof Exception) {
                    throw (Exception) e;
                }
                throw new Error(e);
            }

            //get the domain and server from the parent xml config
            String domain = deployerSystemConfig.getDomain();
            String server = deployerSystemConfig.getServer();

            // build and install the j2ee-deployer configuration
            try {
                configurationDir = configStore.createNewConfigurationDir();

                // build the j2ee-deployer configuration into the configurationDir
                ConfigurationData configurationData = builder.buildConfiguration(j2eeDeployerConfig, domain, server, configurationDir);

                // install the configuration
                configStore.install(configurationData, configurationDir);
            } catch (Throwable e) {
                DeploymentUtil.recursiveDelete(configurationDir);
                if (e instanceof Error) {
                    throw (Error) e;
                } else if (e instanceof Exception) {
                    throw (Exception) e;
                }
                throw new Error(e);
            }
        } finally {
            Thread.currentThread().setContextClassLoader(oldCL);
        }
    }
}
