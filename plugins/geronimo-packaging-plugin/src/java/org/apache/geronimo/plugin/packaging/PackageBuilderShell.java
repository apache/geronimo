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
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;

import org.apache.commons.jelly.JellyContext;
import org.apache.maven.jelly.MavenJellyContext;
import org.apache.maven.project.Dependency;
import org.apache.maven.repository.Artifact;

/**
 * JellyBean that builds a Geronimo Configuration using the local Mavem
 * infrastructure.
 *
 * @version $Rev: 330620 $ $Date: 2005-11-03 12:07:18 -0800 (Thu, 03 Nov 2005) $
 */
public class PackageBuilderShell {

    private List artifacts;
    private List pluginArtifacts;
    private MavenJellyContext context;

    private static ClassLoader classLoader;

    private File repository;
    private String deploymentConfigString;
    private String deployerName;

    private File planFile;
    private File moduleFile;
    private File packageFile;
    private String mainClass;
    private String classPath;
    private String endorsedDirs;
    private String extensionDirs;
    private static final String PACKAGING_CLASSPATH_PROPERTY = "packaging.classpath";

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
    }

    public String getDeployerName() {
        return deployerName;
    }

    /**
     * Set the name of the GBean that is the Deployer.
     *
     * @param deployerName the name of the Deployer GBean
     */
    public void setDeployerName(String deployerName) {
        this.deployerName = deployerName;
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

    public List getArtifacts() {
        return artifacts;
    }

    public void setArtifacts(List artifacts) {
        this.artifacts = artifacts;
    }

    public List getPluginArtifacts() {
        return pluginArtifacts;
    }

    public void setPluginArtifacts(List pluginArtifacts) {
        this.pluginArtifacts = pluginArtifacts;
    }

    public MavenJellyContext getContext() {
        return context;
    }

    public void setContext(MavenJellyContext context) {
        this.context = context;
    }

    public void execute() throws Exception {
        Object packageBuilder = getPackageBuilder();
        set("setClassPath", classPath, String.class, packageBuilder);
        set("setDeployerName", deployerName, String.class, packageBuilder);
        set("setDeploymentConfig", deploymentConfigString, String.class, packageBuilder);
        set("setEndorsedDirs", endorsedDirs, String.class, packageBuilder);
        set("setExtensionDirs", extensionDirs, String.class, packageBuilder);
        set("setMainClass", mainClass, String.class, packageBuilder);
        set("setModuleFile", moduleFile, File.class, packageBuilder);
        set("setPackageFile", packageFile, File.class, packageBuilder);
        set("setPlanFile", planFile, File.class, packageBuilder);
        set("setRepository", repository, File.class, packageBuilder);

        Method m = packageBuilder.getClass().getMethod("execute", new Class[]{});
        m.invoke(packageBuilder, new Object[]{});
    }

    private void set(String methodName, Object value, Class type, Object packageBuilder) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        Method m = packageBuilder.getClass().getMethod(methodName, new Class[]{type});
        m.invoke(packageBuilder, new Object[]{value});
    }


    private Object getPackageBuilder() throws ClassNotFoundException, IllegalAccessException, InstantiationException, MalformedURLException {
        if (classLoader == null) {
            String repo = context.getMavenRepoLocal();
            List urls = new ArrayList();
            for (Iterator iterator = pluginArtifacts.iterator(); iterator.hasNext();) {
                Artifact artifact = (Artifact) iterator.next();
                Dependency dependency = (Dependency) artifact.getDependency();
                if ("true".equals(dependency.getProperty(PACKAGING_CLASSPATH_PROPERTY))) {
                    String urlString = artifact.getUrlPath();
                    URL url = new File(repo + urlString).toURL();
                    urls.add(url);
                }
            }
            boolean found = false;
            for (Iterator iterator = artifacts.iterator(); iterator.hasNext();) {
                Artifact artifact = (Artifact) iterator.next();
                Dependency dependency = (Dependency) artifact.getDependency();
                if ("geronimo".equals(dependency.getGroupId())
                && "geronimo-packaging-plugin".equals(dependency.getArtifactId())
                && "plugin".equals(dependency.getType())) {
                    String urlString = artifact.getUrlPath();
                    URL url = new File(repo + urlString).toURL();
                    urls.add(url);
                    found = true;
                }
            }
            if (!found) {
                System.err.println("You must include the geronimo packaging plugin as a dependency in your project.xml");
                throw new RuntimeException("You must include the geronimo packaging plugin as a dependency in your project.xml");
            }
            URL[] builderClassPath = (URL[]) urls.toArray(new URL[urls.size()]);
            classLoader = new URLClassLoader(builderClassPath, ClassLoader.getSystemClassLoader());
        }
        return classLoader.loadClass(PackageBuilder.class.getName()).newInstance();
    }


}
