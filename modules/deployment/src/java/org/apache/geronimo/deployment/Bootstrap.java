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
import java.io.FileOutputStream;
import java.util.jar.Attributes;
import java.util.jar.Manifest;

import org.apache.geronimo.deployment.service.ServiceConfigBuilder;
import org.apache.geronimo.deployment.util.DeploymentUtil;
import org.apache.geronimo.deployment.xbeans.ConfigurationDocument;
import org.apache.geronimo.deployment.xbeans.ConfigurationType;
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
        Thread.currentThread().setContextClassLoader(Bootstrap.class.getClassLoader());
        try {
            // parse the deployment-system and j2ee-deployer plans
            ConfigurationType deployerSystemConfig = ConfigurationDocument.Factory.parse(new File(deployerSystemPlan)).getConfiguration();
            ConfigurationType j2eeDeployerConfig = ConfigurationDocument.Factory.parse(new File(j2eeDeployerPlan)).getConfiguration();

            // create the service builder, repository and config store objects
            LocalConfigStore configStore = new LocalConfigStore(new File(storeDir));
            ReadOnlyRepository repository = new ReadOnlyRepository(new File(repositoryDir));
            ServiceConfigBuilder builder = new ServiceConfigBuilder(repository);

            // create the manifext
            Manifest manifest = new Manifest();
            Attributes mainAttributes = manifest.getMainAttributes();
            mainAttributes.putValue(Attributes.Name.MANIFEST_VERSION.toString(), "1.0");
            mainAttributes.putValue(Attributes.Name.MAIN_CLASS.toString(), "org.apache.geronimo.system.main.CommandLine");
            mainAttributes.putValue(Attributes.Name.CLASS_PATH.toString(), deployerClassPath);
            mainAttributes.putValue(CommandLineManifest.MAIN_GBEAN.toString(), deployerGBean);
            mainAttributes.putValue(CommandLineManifest.MAIN_METHOD.toString(), "deploy");
            mainAttributes.putValue(CommandLineManifest.CONFIGURATIONS.toString(), j2eeDeployerConfig.getConfigId());

            // attribute that indicates to a JSR-88 tool that we have a Deployment factory
            mainAttributes.putValue("J2EE-DeploymentFactory-Implementation-Class", deploymentFactory);

            // write the deployer system out to a jar

            // create a temp directory to build into
            File tempDir = null;
            try {
                tempDir = DeploymentUtil.createTempDir();

                // build the deployer-system configuration into the tempDir
                builder.buildConfiguration(deployerSystemConfig, null, tempDir);

                // Write the manifest
                File metaInf = new File(tempDir, "META-INF");
                metaInf.mkdirs();
                FileOutputStream out = null;
                try {
                    out = new FileOutputStream(new File(metaInf, "MANIFEST.MF"));
                    manifest.write(out);
                } finally {
                    DeploymentUtil.close(out);
                }

                // add the startup file which allows us to locate the startup directory
                File startupJarTag = new File(metaInf, "startup-jar");
                startupJarTag.createNewFile();

                // jar up the directory
                DeploymentUtil.jarDirectory(tempDir,  new File(deployerJar));

                // delete the startup file before moving this to the config store
                startupJarTag.delete();

                // install the configuration
                configStore.install(tempDir);
            } finally {
                DeploymentUtil.recursiveDelete(tempDir);
            }

            // build and install the j2ee-deployer configuration
            try {
                tempDir = DeploymentUtil.createTempDir();

                // build the j2ee-deployer configuration into the tempDir
                builder.buildConfiguration(j2eeDeployerConfig, null, tempDir);

                // install the configuration
                configStore.install(tempDir);
            } finally {
                DeploymentUtil.recursiveDelete(tempDir);
            }
        } finally {
            Thread.currentThread().setContextClassLoader(oldCL);
        }
    }
}
