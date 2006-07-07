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
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.geronimo.deployment.PluginBootstrap2;
import org.apache.geronimo.system.configuration.RepositoryConfigurationStore;
import org.apache.geronimo.system.repository.Maven2Repository;

import org.apache.maven.plugin.logging.Log;
import org.apache.maven.project.MavenProject;

//
// TODO: Rename to PackageMojo
//

/**
 * Build a Geronimo Configuration using the local Maven infrastructure.
 *
 * @goal package
 * @phase package
 *
 * @version $Rev$ $Date$
 */
public class PackageBuilderShellMojo
    extends AbstractPackagingMojo
{
    private List artifacts;

    private static ClassLoader classLoader;

    /**
     * @parameter expression="${project}"
     */
    private MavenProject project;

    /**
     * @parameter expression="${settings.localRepository}"
     */
    private File repository;

    /**
     * @parameter expression="${project.build.directory}/repository"
     */
    private File targetRepository;

    /**
     * @parameter expression="org.apache.geronimo.configs/geronimo-gbean-deployer/${geronimoVersion}/car"
     */
    private String deploymentConfig;

    private Collection deploymentConfigList;

    /**
     * @parameter expression="org.apache.geronimo.configs/geronimo-gbean-deployer/${geronimoVersion}/car?j2eeType=Deployer,name=Deployer"
     */
    private String deployerName;

    /**
     * @parameter expression="${project.build.directory}/plan/plan.xml"
     */
    private File planFile;

    /**
     * @parameter
     */
    private File moduleFile;

    /**
     * @parameter expression="${project.build.directory}/${project.artifactId}-${project.version}.car"
     */
    private File packageFile;

    /**
     * @parameter expression="${project.build.directory}"
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
     * @parameter
     */
    private String classPath;

    /**
     * @parameter expression="lib/endorsed"
     */
    private String endorsedDirs;

    /**
     * @parameter expression="lib/ext"
     */
    private String extensionDirs;

    /**
     * @parameter expression="${basedir}/../../etc/explicit_versions.properties"
     */
    private String explicitResolutionLocation;

    /**
     * @parameter expression="WARN"
     */
    private String logLevel;

    /**
     * @parameter
     */
    private boolean boot = false;

    protected void doExecute() throws Exception {
        targetRepository.mkdir();

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
        if (log.isDebugEnabled()) {
            log.debug("Build dir: " + buildDir);
            log.debug("Package file: " + packageFile);
            log.debug("Repository: " + repository);
            log.debug("Plan file: " + planFile);
        }

        PluginBootstrap2 boot = new PluginBootstrap2();

        boot.setBuildDir(buildDir);
        boot.setCarFile(packageFile);
        boot.setLocalRepo(repository);
        boot.setPlan(planFile);

        boot.bootstrap();
    }

    public void setDeploymentConfigList(String deploymentConfigs) {
        Collection values = new ArrayList();
        String[] configList = deploymentConfigs.split(",");

        for (int i = 0; i < configList.length; i++) {
            values.add(configList[i]);
        }

        deploymentConfigList = values;
    }

    public void executePackageBuilderShell() throws Exception {
        setDeploymentConfigList(deploymentConfig);
        Object packageBuilder = getPackageBuilder();

        set("setClassPath", classPath, String.class, packageBuilder);
        set("setDeployerName", deployerName, String.class, packageBuilder);
        set("setDeploymentConfig", deploymentConfigList, Collection.class, packageBuilder);
        set("setEndorsedDirs", endorsedDirs, String.class, packageBuilder);
        set("setExtensionDirs", extensionDirs, String.class, packageBuilder);
        set("setMainClass", mainClass, String.class, packageBuilder);
        set("setMainMethod", mainMethod, String.class, packageBuilder);
        set("setMainGBean", mainGBean, String.class, packageBuilder);
        set("setConfigurations", configurations, String.class, packageBuilder);
        set("setModuleFile", moduleFile, File.class, packageBuilder);
        set("setPackageFile", packageFile, File.class, packageBuilder);
        set("setPlanFile", planFile, File.class, packageBuilder);
        set("setRepository", repository, File.class, packageBuilder);
        set("setRepositoryClass", Maven2Repository.class.getName(), String.class, packageBuilder);
        set("setConfigurationStoreClass", MavenConfigStore.class.getName(), String.class, packageBuilder);
        set("setTargetRepository", targetRepository, File.class, packageBuilder);
        set("setTargetRepositoryClass", Maven2Repository.class.getName(), String.class, packageBuilder);
        set("setTargetConfigurationStoreClass", RepositoryConfigurationStore.class.getName(), String.class, packageBuilder);
        set("setExplicitResolutionLocation", explicitResolutionLocation, String.class, packageBuilder);
        set("setLogLevel", logLevel, String.class, packageBuilder);
        
        try {
            Method m = packageBuilder.getClass().getMethod("execute", new Class[0]);
            m.invoke(packageBuilder, new Object[0]);
        }
        catch (InvocationTargetException e) {
            log.debug("Decoding ITE", e);
            
            Throwable t = e.getTargetException();
            
            if (t instanceof Exception) {
                throw (Exception)t;
            }
            if (t instanceof Error) {
                throw (Error)t;
            }
            
            throw new Error(t);
        }
    }

    private void set(String methodName, Object value, Class type, Object packageBuilder) throws Exception {
        Log log = getLog();
        
        log.debug("Setting (" + methodName + "): " + value);

        try {
            Method m = packageBuilder.getClass().getMethod(methodName, new Class[]{type});
            m.invoke(packageBuilder, new Object[]{value});
        }
        catch (InvocationTargetException e) {
            log.debug("Decoding ITE", e);
            
            // Decode ITE
            Throwable t = e.getTargetException();
            
            if (t instanceof Exception) {
                throw (Exception)t;
            }
            if (t instanceof Error) {
                throw (Error)t;
            }
            
            throw new Error(t);
        }
    }

    private Object getPackageBuilder() {
        return new PackageBuilder();
    }
}
