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
import java.util.LinkedHashSet;
import java.util.List;

import org.apache.maven.archiver.MavenArchiveConfiguration;
import org.apache.maven.archiver.MavenArchiver;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.resolver.ArtifactResolutionException;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.project.artifact.InvalidDependencyVersionException;
import org.apache.maven.project.ProjectBuildingException;
import org.codehaus.plexus.archiver.jar.JarArchiver;

/**
 * Jar up a packaged plugin
 *
 * @goal archive-car
 * @requiresDependencyResolution runtime
 *
 * @version $Rev$ $Date$
 */
public class ArchiveCarMojo
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
     * The module base directory.
     *
     * @parameter expression="${project.basedir}"
     * @required
     * @readonly
     */
    private File baseDirectory = null;

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
     * The Geronimo repository where modules will be packaged up from.
     *
     * @parameter expression="${project.build.directory}/repository"
     * @required
     */
    private File targetRepository = null;

    /**
     * The location where the properties mapping will be generated.
     *
     * <p>
     * Probably don't want to change this.
     * </p>
     *
     * @parameter expression="${project.build.directory}/explicit-versions.properties"
     */
    private File explicitResolutionProperties = null;

    /**
     * An array of {@link org.apache.geronimo.mavenplugins.car.ClasspathElement} objects which will be used to construct the
     * Class-Path entry of the manifest.
     *
     * This is needed to allow per-element prefixes to be added, which the standard Maven archiver
     * does not provide.
     *
     * @parameter
     */
    private ClasspathElement[] classpath = null;

    /**
     * The default prefix to be applied to all elements of the <tt>classpath</tt> which
     * do not provide a prefix.
     *
     * @parameter
     */
    private String classpathPrefix = null;

    /**
     * Location of resources directory for additional content to include in the car.
     *
     * @parameter expression="${project.build.directory}/resources"
     */
    private File resourcesDir;

    //
    // Mojo
    //

    public void execute() throws MojoExecutionException, MojoFailureException {
        getDependencies(project, false);
        // Build the archive
        File archive = createArchive();

        // Attach the generated archive for install/deploy
        project.getArtifact().setFile(archive);
    }

    private File getArtifactInRepositoryDir() {
        //
        // HACK: Generate the filename in the repo... really should delegate this to the repo impl
        //

        File dir = new File(targetRepository, project.getGroupId().replace('.', '/'));
        dir = new File(dir, project.getArtifactId());
        dir = new File(dir, project.getVersion());
        dir = new File(dir, project.getArtifactId() + "-" + project.getVersion() + ".car");

        return dir;
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
            File artifactDirectory = getArtifactInRepositoryDir();
            if (artifactDirectory.exists()) {
                archiver.getArchiver().addDirectory(artifactDirectory);
            }

            // Include the optional classes.resources
            if (classesDirectory.isDirectory()) {
                archiver.getArchiver().addDirectory(classesDirectory);
            }

            if (resourcesDir.isDirectory()) {
                archiver.getArchiver().addDirectory(resourcesDir);
            }

            //
            // HACK: Include legal files here for sanity
            //

            //
            // NOTE: Would be nice to share this with the copy-legal-files mojo
            //
            String[] includes = {
                "LICENSE.txt",
                "LICENSE",
                "NOTICE.txt",
                "NOTICE",
                "DISCLAIMER.txt",
                "DISCLAIMER"
            };

            archiver.getArchiver().addDirectory(baseDirectory, "META-INF/", includes, new String[0]);

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

        for (int i=0; i < classpath.length; i++) {
            String entry = classpath[i].getEntry();
            if (entry != null) {
                buff.append(entry);
            } else {
                Artifact artifact = resolveArtifact(classpath[i].getGroupId(), classpath[i].getArtifactId(), classpath[i].getType());
                if (artifact == null) {
                    throw new MojoExecutionException("Could not resolve classpath item: " + classpath[i]);
                }
                //
                // TODO: Need to optionally get all transitive dependencies... but dunno how to get that intel from m2
                //

                String prefix = classpath[i].getClasspathPrefix();
                if (prefix == null) {
                    prefix = classpathPrefix;
                }

                if (prefix != null) {
                    buff.append(prefix);

                    if (!prefix.endsWith("/")) {
                        buff.append("/");
                    }
                }

                String path = getArtifactRepository().pathOf(artifact);
                File file = new File(path);
                buff.append(file.getName());
            }

            if (i + 1< classpath.length) {
                buff.append(" ");
            }
        }

        getLog().debug("Using classpath: " + buff);

        return buff.toString();
    }

}