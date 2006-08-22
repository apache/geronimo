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
import java.util.ArrayList;
import java.util.List;

import org.apache.geronimo.deployment.PluginBootstrap2;
import org.apache.geronimo.system.configuration.RepositoryConfigurationStore;
import org.apache.geronimo.system.repository.Maven2Repository;

import org.apache.maven.archiver.MavenArchiveConfiguration;
import org.apache.maven.archiver.MavenArchiver;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.artifact.Artifact;

import org.codehaus.plexus.util.FileUtils;
import org.codehaus.plexus.archiver.jar.JarArchiver;

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
    /**
     * The maven archive configuration to use.
     *
     * See <a href="http://maven.apache.org/ref/current/maven-archiver/apidocs/org/apache/maven/archiver/MavenArchiveConfiguration.html">the Javadocs for MavenArchiveConfiguration</a>.
     *
     * @parameter
     */
    private MavenArchiveConfiguration archive = new MavenArchiveConfiguration();

    /**
     * The Jar archiver.
     *
     * @parameter expression="${component.org.codehaus.plexus.archiver.Archiver#jar}"
     * @required
     * @readonly
     */
    private JarArchiver jarArchiver = null;

    /**
     * Directory containing the generated archive.
     *
     * @parameter expression="${project.build.directory}"
     * @required
     */
    private File outputDirectory = null;

    /**
     * Directory containing the classes/resources.
     *
     * @parameter expression="${project.build.outputDirectory}"
     * @required
     */
    private File classesDirectory = null;

    /**
     * Name of the generated archive.
     *
     * @parameter expression="${project.build.finalName}"
     * @required
     */
    private String finalName = null;

    /**
     * ???
     *
     * @parameter expression="${settings.localRepository}"
     * @required
     * @readonly
     */
    private File repository = null;

    /**
     * ???
     *
     * @parameter expression="${project.build.directory}/repository"
     * @required
     */
    private File targetRepository = null;

    /**
     * ???
     *
     * @parameter expression="org.apache.geronimo.configs/geronimo-gbean-deployer/${geronimoVersion}/car"
     * @required
     * @readonly
     */
    private String deafultDeploymentConfig = null;

    /**
     * ???
     *
     * @parameter
     */
    private ArrayList deploymentConfigs;

    /**
     * ???
     *
     * @parameter expression="org.apache.geronimo.configs/geronimo-gbean-deployer/${geronimoVersion}/car?j2eeType=Deployer,name=Deployer"
     * @required
     */
    private String deployerName = null;

    /**
     * ???
     *
     * @parameter expression="${project.build.directory}/plan/plan.xml"
     * @required
     */
    private File planFile = null;

    /**
     * ???
     *
     * @parameter
     */
    private File moduleFile = null;

    /**
     * ???
     *
     * @parameter
     */
    private ArtifactItem module = null;
    
    /**
     * The location where the properties mapping will be generated.
     *
     * @parameter expression="${project.build.directory}/explicit-versions.properties"
     */
    private File explicitResolutionProperties = null;

    /**
     * A list of {@link ClasspathElement} objects which will be used to construct the
     * Class-Path entry of the manifest.
     *
     * This is needed to allow per-element prefixes to be added, which the standard Maven archiver
     * does not provide.
     *
     * @parameter
     */
    private List classpath = null;

    /**
     * The default prefix to be applied to all elements of the <tt>classpath</tt> which
     * do not provide a prefix.
     *
     * @parameter
     */
    private String classpathPrefix = null;

    /**
     * True to enable the bootshell when packaging.
     *
     * @parameter
     */
    private boolean bootstrap = false;

    //
    // Mojo
    //

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

        // If module is set, then resolve the artifact and set moduleFile
        if (module != null) {
            Artifact artifact = getArtifact(module);
            moduleFile = artifact.getFile();
            log.debug("Using module file: " + moduleFile);
        }

        generateExplicitVersionProperties(explicitResolutionProperties);
        
        if (bootstrap) {
            executeBootShell();
        }
        else {
            executePackageBuilderShell();
        }

        // Build the archive
        File archive = createArchive();

        // Attach the generated archive for install/deploy
        project.getArtifact().setFile(archive);
    }

    private File getArtifactInRepositoryDir() {
        //
        // HACK: Generate the filename in the repo... really should delegate this to the
        //       repo impl, but need to condense PackageMojo and PackageBuilder first
        //

        File dir = new File(targetRepository, project.getGroupId().replace('.', '/'));
            dir = new File(dir, project.getArtifactId());
            dir = new File(dir, project.getVersion());
            dir = new File(dir, project.getArtifactId() + "-" + project.getVersion() + ".car");

        return dir;
    }

    public void executeBootShell() throws Exception {
        log.debug("Starting bootstrap shell...");

        PluginBootstrap2 boot = new PluginBootstrap2();

        boot.setBuildDir(outputDirectory);
        boot.setCarFile(getArtifactInRepositoryDir());
        boot.setLocalRepo(repository);
        boot.setPlan(planFile);

        // Generate expanded so we can use Maven to generate the archive
        boot.setExpanded(true);

        boot.bootstrap();
    }

    public void executePackageBuilderShell() throws Exception {
        log.debug("Starting builder shell...");

        PackageBuilder builder = new PackageBuilder();

        //
        // NOTE: May need to run this in a controlled classloader (w/reflection or a proxy)
        //
        //       http://www.nabble.com/PackageBuilderShellMojo-%28m2%29-and-classloaders-p5271991.html
        //

        builder.setDeployerName(deployerName);
        builder.setDeploymentConfig(deploymentConfigs);
        builder.setModuleFile(moduleFile);
        builder.setPlanFile(planFile);
        builder.setRepository(repository);
        builder.setRepositoryClass(Maven2Repository.class.getName());
        builder.setConfigurationStoreClass(MavenConfigStore.class.getName());
        builder.setTargetRepository(targetRepository);
        builder.setTargetRepositoryClass(Maven2Repository.class.getName());
        builder.setTargetConfigurationStoreClass(RepositoryConfigurationStore.class.getName());
        builder.setExplicitResolutionLocation(explicitResolutionProperties.getAbsolutePath());

        builder.execute();
    }

    /**
     * Generates the configuration archive.
     */
    private File createArchive() throws MojoExecutionException {
        File archiveFile = getArchiveFile(outputDirectory, finalName, null);

        MavenArchiver archiver = new MavenArchiver();
        archiver.setArchiver(jarArchiver);
        archiver.setOutputFile(archiveFile);

        try {
            // Incldue the generated artifact contents
            archiver.getArchiver().addDirectory(getArtifactInRepositoryDir());

            // Include the optional classes.resources
            if (classesDirectory.isDirectory()) {
                archiver.getArchiver().addDirectory(classesDirectory);
            }
            
            if (classpath != null) {
                archive.addManifestEntry("Class-Path", getClassPath());
            }

            archiver.createArchive(project, archive);

            return archiveFile;
        }
        catch (Exception e) {
            throw new MojoExecutionException("Failed to create archive", e);
        }
    }

    private String getClassPath() throws MojoExecutionException {
        StringBuffer buff = new StringBuffer();

        ClasspathElement[] elements = (ClasspathElement[]) classpath.toArray(new ClasspathElement[classpath.size()]);
        for (int i=0; i < elements.length; i++) {
            Artifact artifact = getArtifact(elements[i]);

            //
            // TODO: Need to optionally get all transitive dependencies... but dunno how to get that intel from m2
            //

            String prefix = elements[i].getClasspathPrefix();
            if (prefix == null) {
                prefix = classpathPrefix;
            }

            if (prefix != null) {
                buff.append(prefix);

                if (!prefix.endsWith("/")) {
                    buff.append("/");
                }
            }

            File file = artifact.getFile();
            buff.append(file.getName());

            if (i + 1< elements.length) {
                buff.append(" ");
            }
        }

        log.debug("Using classpath: " + buff);

        return buff.toString();
    }
}
