/**
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

package org.apache.geronimo.mavenplugins.car;

import java.io.File;
import java.lang.reflect.Method;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.felix.fileinstall.ArtifactInstaller;
import org.apache.geronimo.deployment.Deployer;
import org.apache.karaf.features.FeaturesService;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.util.FileUtils;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleException;

/**
 * Build a Geronimo Configuration using the local Maven infrastructure.
 *
 * @goal compile-gbean-plan
 * @requiresDependencyResolution compile
 *
 * @version $Rev$ $Date$
 */
public class PackageMojo extends AbstractFrameworkMojo {


    /**
     * Directory containing the generated archive.
     *
     * @parameter expression="${project.build.directory}"
     * @required
     */
    private File outputDirectory = null;

    /**
     * The local Maven repository which will be used to pull artifacts into the Geronimo repository when packaging.
     *
     * @parameter expression="${settings.localRepository}"
     * @required
     * @readonly
     */
    private File repository = null;

    /**
     * The Geronimo repository where modules will be packaged up from.
     *
     * @parameter expression="${project.build.directory}/repository"
     * @required
     */
    private File targetRepository = null;

    /**
     * The default deployer module to be used when no other deployer modules are configured.
     *
     * @parameter expression="org.apache.geronimo.framework/geronimo-gbean-deployer/${geronimoVersion}/car"
     * @required
     * @readonly
     */
    private String defaultDeploymentConfig = null;

    /**
     * Ther deployer modules to be used when packaging.
     *
     * @parameter
     */
    private String[] deploymentConfigs;

    /**
     * The plan file for the CAR.
     *
     * @parameter expression="${project.build.directory}/work/plan.xml"
     * @required
     */
    private File planFile = null;

    /**
     * The file to include as a module of the CAR.
     *
     * @parameter
     */
    private File moduleFile = null;

    /**
     * An {@link Dependency} to include as a module of the CAR.
     *
     * @parameter
     */
    private Dependency module = null;

    /**
     * Directory for generated plugin metadata file.
     *
     * @parameter expression="${project.build.directory}/resources/"
     * @required
     */
    protected File targetDir = null;

    /**
     * Name of generated plugin metadata file.
     *
     * @parameter default-value="META-INF/geronimo-plugin.xml"
     * @required
     */
    protected String pluginMetadataFileName = null;

    /**
     * The maven project.
     *
     * @parameter expression="${project}"
     * @required
     * @readonly
     */
    protected MavenProject project;

    /**
     * features to start
     *
     * @parameter
     */
    protected List<String> features;
    //
    // Mojo
    //

    public void doExecute() throws MojoExecutionException {
        try {
            try {
                super.doExecute();
                // We need to make sure to clean up any previous work first or this operation will fail
                FileUtils.forceDelete(targetRepository);
                FileUtils.forceMkdir(targetRepository);

                if (!planFile.exists()) {
                    return;
                }
                Object featuresService = getService(FeaturesService.class);
                Object karService = getService(ArtifactInstaller.class, "karArtifactInstaller");
                Method karInstallMethod;
                Method featuresAddRepoMethod;
                Method featuresInstallFeatureMethod;

                try {
                    karInstallMethod = karService.getClass().getMethod("install", new Class[]{File.class});
                    featuresAddRepoMethod = featuresService.getClass().getMethod("addRepository", new Class[]{URI.class});
                    featuresInstallFeatureMethod = featuresService.getClass().getMethod("installFeature", new Class[]{String.class});
                } catch (NoSuchMethodException e) {
                    throw new MojoFailureException("What class??", e);
                }

                List<Long> ids = new ArrayList<Long>();
                Map<org.sonatype.aether.artifact.Artifact, String> artifacts = getTransitiveDependencies(project);
                for (org.sonatype.aether.artifact.Artifact dependency : artifacts.keySet()) {
                    if ("kar".equals(dependency.getExtension())) {
                        try {
                            File file = resolve(dependency);
                            karInstallMethod.invoke(karService, file);
                        } catch (Exception e) {
                            throw new MojoExecutionException("Cannot install kar " + dependency, e);
                        }
                    } else if ("xml".equals(dependency.getExtension()) && "features".equals(dependency.getClassifier())) {
                        try {
                            File file = resolve(dependency);
                            featuresAddRepoMethod.invoke(featuresService, file.toURI());
                        } catch (Exception e) {
                            throw new MojoExecutionException("Could not install feature " + dependency, e);
                        }
                    } else if ("jar".equals(dependency.getExtension()) || "car".equals(dependency.getExtension())) {
                        getLog().info("starting dependency: " + dependency);
                        File file = resolve(dependency);
                        try {
                            ids.add(getFramework().getBundleContext().installBundle("reference:" + file.toURI().toURL()).getBundleId());
                        } catch (BundleException e) {
                            getLog().info("Can't install " + dependency + " due to " + e.getMessage());
                        }
                    }
                }
                if (features != null) {
                    for (String featureName: features) {
                        featuresInstallFeatureMethod.invoke(featuresService, featureName);
                    }
                }
                for (Long id : ids) {
                    try {
                        getFramework().getBundleContext().getBundle(id).start();
                    } catch (BundleException e) {
                        getLog().info("Can't start " + id + " due to " + e.getMessage());
                    }
                }
                listBundles();
                Object deployer = getService(Deployer.class);
                invokeDeployer(deployer, null);
            } catch (Exception e) {
                getLog().info("Exception, use console to investigate ", e);
                listBundles();
                for (Bundle b : getFramework().getBundleContext().getBundles()) {
                    if (b.getState() != 32) {
                        try {
                            b.start();
                        } catch (BundleException e1) {
                            getLog().info("Could not start " + b + e.getMessage());
                        }
                    }
                }
                while (1 == 1) {
                    try {
                        Thread.sleep(1000L);
                    } catch (InterruptedException e1) {
                        //exit
                        break;
                    }
                }
                throw e;
            }
        } catch (Exception e) {
            throw new MojoExecutionException("could not package plugin", e);
        }
    }


    private static final Class[] DEPLOY_SIGNATURE = {
            boolean.class,
            File.class,
            File.class
    };

    private List<String> invokeDeployer(Object deployer, final String targetConfigStore) throws Exception {
        Method m = deployer.getClass().getMethod("deploy", DEPLOY_SIGNATURE);
        return (List<String>) m.invoke(deployer, new Object[] {Boolean.FALSE, moduleFile, planFile});
    }

}
