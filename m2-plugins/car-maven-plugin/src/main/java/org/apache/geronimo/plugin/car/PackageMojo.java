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
import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Iterator;
import java.util.Properties;

import org.apache.geronimo.deployment.PluginBootstrap2;
import org.apache.geronimo.system.configuration.RepositoryConfigurationStore;
import org.apache.geronimo.system.repository.Maven2Repository;
import org.apache.maven.artifact.Artifact;

import org.codehaus.plexus.util.FileUtils;

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
    private List artifacts;

    /**
     * @parameter expression="${settings.localRepository}"
     * @required
     * @readonly
     */
    private File repository;

    /**
     * @parameter expression="${project.build.directory}/repository"
     * @required
     */
    private File targetRepository;

    /**
     * @parameter expression="org.apache.geronimo.configs/geronimo-gbean-deployer/${geronimoVersion}/car"
     * @required
     * @readonly
     */
    private String deafultDeploymentConfig;

    /**
     * @parameter
     */
    private ArrayList deploymentConfigs;

    /**
     * @parameter expression="org.apache.geronimo.configs/geronimo-gbean-deployer/${geronimoVersion}/car?j2eeType=Deployer,name=Deployer"
     * @required
     */
    private String deployerName;

    /**
     * @parameter expression="${project.build.directory}/plan/plan.xml"
     * @required
     */
    private File planFile;

    /**
     * @parameter
     */
    private File moduleFile;

    /**
     * @parameter expression="${project.build.directory}/${project.artifactId}-${project.version}.car"
     * @required
     */
    private File packageFile;

    /**
     * @parameter expression="${project.build.directory}"
     * @required
     */
    private File buildDir;

    /**
     * @parameter
     */
    private String mainClass;

    /**
     * @parameter
     */
    private String mainMethod;

    /**
     * @parameter
     */
    private String mainGBean;

    /**
     * @parameter
     */
    private String configurations;

    /**
     * The classpath to be added to the generated manifest.
     *
     * @parameter
     */
    private ArrayList classPath;

    /**
     * @parameter default-value="lib/endorsed"
     * @required
     */
    private String endorsedDirs;

    /**
     * @parameter default-value="lib/ext"
     * @required
     */
    private String extensionDirs;

    /**
     * The location where the properties mapping will be generated.
     *
     * @parameter expression="${project.build.directory}/explicit-versions.properties"
     */
    private File explicitResolutionProperties;

    /**
     * @parameter default-value="WARN"
     * @required
     */
    private String logLevel;

    /**
     * @parameter
     */
    private boolean boot = false;

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

        generateExplicitVersionProperties(explicitResolutionProperties);

        if (boot) {
            executeBootShell();
        }
        else {
            executePackageBuilderShell();
        }

        // copy configuration from target/repository to maven repo
        project.getArtifact().setFile(packageFile);
    }

    public void executeBootShell() throws Exception {
        PluginBootstrap2 boot = new PluginBootstrap2();

        boot.setBuildDir(buildDir);
        boot.setCarFile(packageFile);
        boot.setLocalRepo(repository);
        boot.setPlan(planFile);

        boot.bootstrap();
    }

    private String getClassPath() {
        if (classPath == null) {
            return null;
        }

        log.debug("Creating classpath from: " + classPath);

        StringBuffer buff = new StringBuffer();
        Iterator iter = classPath.iterator();
        while (iter.hasNext()) {
            String element = (String)iter.next();
            buff.append(element.trim());

            if (iter.hasNext()) {
                buff.append(" ");
            }
        }
        
        log.debug("Using classpath: " + buff);

        return buff.toString();
    }

    public void executePackageBuilderShell() throws Exception {
        PackageBuilder builder = new PackageBuilder();

        //
        // NOTE: May need to run this in a controlled classloader (w/reflection or a proxy)
        //
        //       http://www.nabble.com/PackageBuilderShellMojo-%28m2%29-and-classloaders-p5271991.html
        //

        builder.setClassPath(getClassPath());
        builder.setDeployerName(deployerName);
        builder.setDeploymentConfig(deploymentConfigs);
        builder.setEndorsedDirs(endorsedDirs);
        builder.setExtensionDirs(extensionDirs);
        builder.setMainClass(mainClass);
        builder.setMainMethod(mainMethod);
        builder.setMainGBean(mainGBean);
        builder.setConfigurations(configurations);
        builder.setModuleFile(moduleFile);
        builder.setPackageFile(packageFile);
        builder.setPlanFile(planFile);
        builder.setRepository(repository);
        builder.setRepositoryClass(Maven2Repository.class.getName());
        builder.setConfigurationStoreClass(MavenConfigStore.class.getName());
        builder.setTargetRepository(targetRepository);
        builder.setTargetRepositoryClass(Maven2Repository.class.getName());
        builder.setTargetConfigurationStoreClass(RepositoryConfigurationStore.class.getName());
        builder.setExplicitResolutionLocation(explicitResolutionProperties.getAbsolutePath());
        builder.setLogLevel(logLevel);

        builder.execute();
    }
}
