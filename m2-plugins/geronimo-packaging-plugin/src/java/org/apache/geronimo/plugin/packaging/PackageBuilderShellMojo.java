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
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.geronimo.deployment.PluginBootstrap;
import org.apache.geronimo.system.configuration.RepositoryConfigurationStore;
import org.apache.geronimo.system.repository.Maven2Repository;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.project.MavenProject;


/**
 * Build a Geronimo Configuration using the local Maven infrastructure.
 * 
 * @goal package
 * @phase package
 * @version $Rev$ $Date$
 */

public class PackageBuilderShellMojo extends AbstractPackagingMojo {
	private static Log log = LogFactory.getLog(PlanProcessorMojo.class);
	private List artifacts;

	private static ClassLoader classLoader;

	/**
	 * @parameter expression="${project}"
	 */
	private MavenProject project;

	/**
	 * @parameter expression="${plugin.artifacts}"
	 */
	private List pluginArtifacts;

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

	//private static final String PACKAGING_CLASSPATH_PROPERTY = "packaging.classpath";
	
	/**
	 * @parameter
	 */
	private boolean boot = false;
	

	public void execute() throws MojoExecutionException {
		try {
			// create target/repository, delete old contents 
            //targetRepository.delete();
			targetRepository.mkdir();
			if (boot)
				executeBootShell();
			else
				executePackageBuilderShell();
		} catch (Exception e) {
			handleError(e);
		}
		// copy configuration from target/repository to maven repo
		project.getArtifact().setFile(packageFile);
	}

	public void executeBootShell() throws Exception {
		PluginBootstrap boot = new PluginBootstrap();
		boot.setBuildDir(buildDir);
		boot.setCarFile(packageFile);
		boot.setLocalRepo(repository);
		boot.setPlan(planFile);
		System.out.println("******build dir = " + buildDir);
		System.out.println("******car file = " + packageFile);
		System.out.println("******repo = " + repository);
		System.out.println("******plan file = " + planFile);
		boot.bootstrap();
	}
	public void setDeploymentConfigList(String deploymentConfigs) {
		Collection values = new ArrayList();
		String[] configList = deploymentConfigs.split(",");
		for (int i = 0; i < configList.length; i++) {
			values.add(configList[i]);
		}
/*
	      this.artifacts = artifacts; for (Iterator iterator = artifacts.iterator();
		  iterator.hasNext();) { Artifact artifact = (Artifact) iterator.next();
		  //Dependency dependency = artifact.getDependency(); //if
		  (dependency.getProperty(PACKAGING_CONFIG_PROPERTY) != null) { if
		  ("car".equals(artifact.getType()) && "provided".equals(artifact.getScope())) {
		  //String orderString = dependency.getProperty(PACKAGING_CONFIG_PROPERTY);
		  String orderString = getOrderString(artifact); try { Integer order =
		  Integer.decode(orderString); String artifactString = artifact.getGroupId() +
		  "/" + artifact.getArtifactId() + "/" + dependency.getVersion() + "/" +
		  dependency.getType(); tree.put(order, artifactString); }
		  catch(NumberFormatException e) { System.out.println("Could not interpret
		  order for " + dependency); } } }
 */

        deploymentConfigList = values;
    }
	
	public void executePackageBuilderShell() throws Exception {
		try {
			setDeploymentConfigList(deploymentConfig);			
			Object packageBuilder = getPackageBuilder();
			set("setClassPath", classPath, String.class, packageBuilder);
			set("setDeployerName", deployerName, String.class, packageBuilder);
			set("setDeploymentConfig", deploymentConfigList, Collection.class,
					packageBuilder);
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
			set("setRepositoryClass", Maven2Repository.class.getName(), //was maven1
					String.class, packageBuilder);
			set("setConfigurationStoreClass", MavenConfigStore.class.getName(),
					String.class, packageBuilder);
			set("setTargetRepository", targetRepository, File.class,
					packageBuilder);
			set("setTargetRepositoryClass", Maven2Repository.class.getName(),
					String.class, packageBuilder);
			set("setTargetConfigurationStoreClass",
					RepositoryConfigurationStore.class.getName(), String.class,
					packageBuilder);
			set("setExplicitResolutionLocation", explicitResolutionLocation,
					String.class, packageBuilder);
            set("setLogLevel", logLevel, String.class, packageBuilder);

			Method m = packageBuilder.getClass().getMethod("execute",
					new Class[] {});
			m.invoke(packageBuilder, new Object[] {});
		} catch (Exception e) {
			log.error(e.getClass().getName() + ": " + e.getMessage(), e);
			throw e;
		}
	}

	private void set(String methodName, Object value, Class type,
			Object packageBuilder) throws NoSuchMethodException,
			IllegalAccessException, InvocationTargetException {
		Method m = packageBuilder.getClass().getMethod(methodName,
				new Class[] { type });
		System.out.println("-----------" + value);
		m.invoke(packageBuilder, new Object[] { value });
	}

	private Object getPackageBuilder() throws ClassNotFoundException,
			IllegalAccessException, InstantiationException,
			MalformedURLException {
		//System.out.println("plugin artifacts = " + pluginArtifacts);
		return new PackageBuilder();
		}
	}
