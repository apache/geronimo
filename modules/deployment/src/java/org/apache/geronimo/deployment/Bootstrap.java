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

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.jar.Attributes;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;
import java.util.zip.ZipEntry;

import org.apache.geronimo.deployment.service.ServiceConfigBuilder;
import org.apache.geronimo.deployment.xbeans.ConfigurationDocument;
import org.apache.geronimo.deployment.xbeans.ConfigurationType;
import org.apache.geronimo.system.configuration.LocalConfigStore;
import org.apache.geronimo.system.main.CommandLine;
import org.apache.geronimo.system.repository.ReadOnlyRepository;
import org.apache.xmlbeans.XmlBeans;
import org.apache.xmlbeans.XmlObject;

/**
 * Helper class to bootstrap the Geronimo deployer.
 *
 * @version $Revision: 1.13 $ $Date: 2004/04/03 22:37:57 $
 */
public class Bootstrap {
    private String deployerJar;
    private String storeDir;
    private String repositoryDir;
    private String deployerSystemPlan;
    private String j2eeDeployerPlan;
    private String deployerClassPath;
    private String deployerGBean;

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

    public void bootstrap() throws Exception {
        ClassLoader oldCL = Thread.currentThread().getContextClassLoader();
        Thread.currentThread().setContextClassLoader(Bootstrap.class.getClassLoader());
        try {
            // parse the deployment-system and j2ee-deployer plans
            XmlObject deployerSystemXML = XmlBeans.getContextTypeLoader().parse(new File(deployerSystemPlan), null, null);
            XmlObject j2eeDeployerXML = XmlBeans.getContextTypeLoader().parse(new File(j2eeDeployerPlan), null, null);
            ConfigurationType j2eeDeployerConfig = ((ConfigurationDocument) j2eeDeployerXML).getConfiguration();

            // create the service builder, repository and config store objects
            LocalConfigStore configStore = new LocalConfigStore(new File(storeDir));
            ReadOnlyRepository repository = new ReadOnlyRepository(new File(repositoryDir));
            ServiceConfigBuilder builder = new ServiceConfigBuilder(repository);

            // create the manifext
            Manifest manifest = new Manifest();
            Attributes mainAttributes = manifest.getMainAttributes();
            mainAttributes.putValue(Attributes.Name.MANIFEST_VERSION.toString(), "1.0");
            mainAttributes.putValue(Attributes.Name.MAIN_CLASS.toString(), CommandLine.class.getName());
            mainAttributes.putValue(Attributes.Name.CLASS_PATH.toString(), deployerClassPath);
            mainAttributes.putValue(CommandLine.MAIN_GBEAN.toString(), deployerGBean);
            mainAttributes.putValue(CommandLine.MAIN_METHOD.toString(), "deploy");
            mainAttributes.putValue(CommandLine.CONFIGURATIONS.toString(), j2eeDeployerConfig.getConfigId());

            // build and install the deployer-system configuration
            // write the deployer system out to a jar
            File outputFile = new File(deployerJar);
            JarOutputStream jos = new JarOutputStream(new BufferedOutputStream(new FileOutputStream(outputFile)), manifest);
            try {
                // add the startup jar entry which allows us to locat the startup directory
                jos.putNextEntry(new ZipEntry("META-INF/startup-jar"));
                jos.closeEntry();

                // add the deployment system configuration to the jar
                builder.buildConfiguration(jos, deployerSystemXML);
            } finally {
                jos.close();
            }
            configStore.install(outputFile.toURL());

            // build and install the j2ee-deployer configuration
            File tempFile = File.createTempFile("j2ee-deployer", ".car");
            try {
                builder.buildConfiguration(tempFile, (InputStream)null, j2eeDeployerXML);
                configStore.install(tempFile.toURL());
            } finally {
                tempFile.delete();
            }
        } finally {
            Thread.currentThread().setContextClassLoader(oldCL);
        }
    }
}
