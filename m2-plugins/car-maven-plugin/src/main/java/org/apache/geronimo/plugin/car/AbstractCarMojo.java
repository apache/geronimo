/*
 *  Copyright 2006 The Apache Software Foundation
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

import org.apache.geronimo.plugin.MojoSupport;

import org.apache.maven.project.MavenProject;
import org.apache.maven.project.MavenProjectHelper;
import org.apache.maven.artifact.resolver.filter.ArtifactFilter;
import org.apache.maven.artifact.resolver.filter.ExcludesArtifactFilter;
import org.apache.maven.artifact.resolver.ArtifactResolutionException;
import org.apache.maven.artifact.resolver.ArtifactNotFoundException;
import org.apache.maven.artifact.resolver.ArtifactResolver;
import org.apache.maven.artifact.versioning.VersionRange;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.artifact.factory.ArtifactFactory;
import org.apache.maven.model.Dependency;
import org.apache.maven.model.Exclusion;
import org.apache.maven.plugin.MojoExecutionException;

import java.io.File;
import java.io.IOException;
import java.io.FileOutputStream;
import java.io.BufferedOutputStream;

import java.util.Set;
import java.util.List;
import java.util.Iterator;
import java.util.HashSet;
import java.util.ArrayList;
import java.util.Properties;

/**
 * Support for <em>packaging</em> Mojos.
 *
 * @version $Id$
 */
public abstract class AbstractCarMojo
    extends MojoSupport
{
    /**
     * The maven project.
     *
     * @parameter expression="${project}"
     * @required
     * @readonly
     */
    protected MavenProject project;

    /**
     * Used to look up Artifacts in the remote repository.
     *
     * @component
     * @required
     * @readonly
     */
    protected ArtifactFactory factory;

    /**
     * Used to look up Artifacts in the remote repository.
     *
     * @component
     * @required
     * @readonly
     */
    protected ArtifactResolver resolver;

    /**
     * Location of the local repository.
     *
     * @parameter expression="${localRepository}"
     * @readonly
     * @required
     */
    protected ArtifactRepository local;

    /**
     * List of Remote Repositories used by the resolver.
     *
     * @parameter expression="${project.remoteArtifactRepositories}"
     * @readonly
     * @required
     */
    protected java.util.List remoteRepos;

    /**
     * The basedir of the project.
     *
     * @parameter expression="${basedir}"
     * @required
     * @readonly
     */
    protected File basedir;

    /**
     * The maven project's helper.
     *
     * @component
     * @required
     * @readonly
     */
    protected MavenProjectHelper projectHelper;

    protected Set getProjectArtifacts(final MavenProject project) {
        Set artifacts = new HashSet();

        Iterator dependencies = project.getDependencies().iterator();
        while (dependencies.hasNext()) {
            Dependency dep = (Dependency) dependencies.next();

            String groupId = dep.getGroupId();
            String artifactId = dep.getArtifactId();
            VersionRange versionRange = VersionRange.createFromVersion(dep.getVersion());
            String type = dep.getType();
            if (type == null) {
                type = "jar";
            }

            String classifier = dep.getClassifier();
            boolean optional = dep.isOptional();
            String scope = dep.getScope();
            if (scope == null) {
                scope = Artifact.SCOPE_COMPILE;
            }

            Artifact artifact = factory.createDependencyArtifact(
                groupId,
                artifactId,
                versionRange,
                type,
                classifier,
                scope,
                optional);

            if (scope.equalsIgnoreCase(Artifact.SCOPE_SYSTEM)) {
                artifact.setFile(new File(dep.getSystemPath()));
            }

            List exclusions = new ArrayList();
            for (Iterator j = dep.getExclusions().iterator(); j.hasNext();) {
                Exclusion e = (Exclusion) j.next();
                exclusions.add(e.getGroupId() + ":" + e.getArtifactId());
            }

            ArtifactFilter newFilter = new ExcludesArtifactFilter(exclusions);
            artifact.setDependencyFilter(newFilter);
            artifacts.add(artifact);
        }

        return artifacts;
    }

    protected Set getProjectArtifacts() {
        return getProjectArtifacts(project);
    }
    
    protected void generateExplicitVersionProperties(final File outputFile) throws IOException {
        log.debug("Generating explicit version properties: " + outputFile);

        // Generate explicit_versions for all our dependencies...
        Properties props = new Properties();
        Iterator iter = getProjectArtifacts().iterator();
        while (iter.hasNext()) {
            Artifact artifact = (Artifact)iter.next();
            String name = artifact.getGroupId() + "/" + artifact.getArtifactId() + "//" + artifact.getType();
            String value = artifact.getVersion();

            log.debug("Setting " + name + "=" + value);
            props.setProperty(name, value);
        }

        BufferedOutputStream output = new BufferedOutputStream(new FileOutputStream(outputFile));
        props.store(output, null);
        output.flush();
        output.close();
    }

    protected static File getArchiveFile(final File basedir, final String finalName, String classifier) {
        if (classifier == null) {
            classifier = "";
        }
        else if (classifier.trim().length() > 0 && !classifier.startsWith("-")) {
            classifier = "-" + classifier;
        }

        return new File(basedir, finalName + classifier + ".car");
    }

    //
    // NOTE: Bits below lifed from the maven-depndency-plugin
    //

    //
    // TODO: Replace with ArtifactItem and move to base-class
    //

    /**
     * Resolves the Artifact from the remote repository if nessessary. If no version is specified, it will
     * be retrieved from the dependency list or from the DependencyManagement section of the pom.
     */
    protected Artifact getArtifact(final ArtifactItem item) throws MojoExecutionException {
        Artifact artifact;

        if (item.getVersion() == null) {
            fillMissingArtifactVersion(item);

            if (item.getVersion() == null) {
                throw new MojoExecutionException("Unable to find artifact version of " + item.getGroupId()
                    + ":" + item.getArtifactId() + " in either dependency list or in project's dependency management.");
            }

        }

        String classifier = item.getClassifier();
        if (classifier == null || classifier.equals("")) {
            artifact = factory.createArtifact(
                    item.getGroupId(),
                    item.getArtifactId(),
                    item.getVersion(),
                    Artifact.SCOPE_PROVIDED,
                    item.getType());
        }
        else {
            artifact = factory.createArtifactWithClassifier(
                    item.getGroupId(),
                    item.getArtifactId(),
                    item.getVersion(),
                    item.getType(),
                    item.getClassifier());
        }

        try {
            resolver.resolve(artifact, remoteRepos, local);
        }
        catch (ArtifactResolutionException e) {
            throw new MojoExecutionException("Unable to resolve artifact.", e);
        }
        catch (ArtifactNotFoundException e) {
            throw new MojoExecutionException("Unable to find artifact.", e);
        }

        return artifact;
    }

    /**
     * Tries to find missing version from dependancy list and dependency management.
     * If found, the artifact is updated with the correct version.
     */
    private void fillMissingArtifactVersion(final ArtifactItem item) {
        log.debug("Attempting to find missing version in " + item.getGroupId() + ":" + item.getArtifactId());

        List list = this.project.getDependencies();

        for (int i = 0; i < list.size(); ++i) {
            Dependency dependency = (Dependency) list.get(i);

            if (dependency.getGroupId().equals(item.getGroupId())
                && dependency.getArtifactId().equals(item.getArtifactId())
                && dependency.getType().equals(item.getType()))
            {
                log.debug("Found missing version: " + dependency.getVersion() + " in dependency list.");

                item.setVersion(dependency.getVersion());

                return;
            }
        }

        list = this.project.getDependencyManagement().getDependencies();

        for (int i = 0; i < list.size(); i++) {
            Dependency dependency = (Dependency) list.get(i);

            if (dependency.getGroupId().equals(item.getGroupId())
                && dependency.getArtifactId().equals(item.getArtifactId())
                && dependency.getType().equals(item.getType()))
            {
                log.debug("Found missing version: " + dependency.getVersion() + " in dependency management list");

                item.setVersion(dependency.getVersion());
            }
        }
    }
}
