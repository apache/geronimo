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

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.Collections;
import java.util.HashMap;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.handler.DefaultArtifactHandler;
import org.apache.maven.artifact.factory.ArtifactFactory;
import org.apache.maven.artifact.resolver.ArtifactResolutionException;
import org.apache.maven.artifact.resolver.ArtifactResolutionResult;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.project.MavenProject;
import org.apache.maven.project.MavenProjectHelper;
import org.apache.maven.project.ProjectBuildingException;
import org.apache.maven.project.artifact.InvalidDependencyVersionException;
import org.codehaus.mojo.pluginsupport.MojoSupport;
import org.codehaus.mojo.pluginsupport.dependency.DependencyHelper;
import org.codehaus.mojo.pluginsupport.util.ArtifactItem;

/**
 * Support for <em>packaging</em> Mojos.
 *
 * @version $Rev$ $Date$
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
    
    /**
     * dependency resolution for the maven repository
     *
     * @component
     */
    protected DependencyHelper dependencyHelper = null;
    /**
     * @component
     * @required
     * @readonly
     */
    protected ArtifactFactory artifactFactory;
    protected Set<Artifact> dependencies;
    protected Map<Artifact, Artifact> dependencyMap = new HashMap<Artifact, Artifact>();
    protected ProjectNode projectNode;

    //
    // MojoSupport Hooks
    //

    protected MavenProject getProject() {
        return project;
    }

    /**
     * @parameter expression="${localRepository}"
     * @readonly
     * @required
     */
    protected ArtifactRepository artifactRepository = null;

    protected ArtifactRepository getArtifactRepository() {
        return artifactRepository;
    }
    
    protected void init() throws MojoExecutionException, MojoFailureException {
        super.init();
        
        dependencyHelper.setArtifactRepository(artifactRepository);
    }
    
    /**
     * Generates a properties file with explicit versions of artifacts of the current project transitivly.
     */
    protected void generateExplicitVersionProperties(final File outputFile, Set<org.apache.maven.artifact.Artifact> dependencies) throws MojoExecutionException, IOException {
        log.debug("Generating explicit version properties: " + outputFile);

        // Generate explicit_versions for all our dependencies...
        Properties props = new Properties();

        for (org.apache.maven.artifact.Artifact artifact: dependencies) {
            String name = artifact.getGroupId() + "/" + artifact.getArtifactId() + "//" + artifact.getType();
            String value = artifact.getGroupId() + "/" + artifact.getArtifactId() + "/" + artifact.getVersion() + "/" + artifact.getType();

            if (log.isDebugEnabled()) {
                log.debug("Setting " + name + "=" + value);
            }
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
    // Geronimo/Maven Artifact Interop
    //

    protected org.apache.geronimo.kernel.repository.Artifact mavenToGeronimoArtifact(final org.apache.maven.artifact.Artifact artifact) {
        assert artifact != null;

        return new org.apache.geronimo.kernel.repository.Artifact(artifact.getGroupId(), artifact.getArtifactId(), artifact.getVersion(), artifact.getType());
    }
    
    protected org.apache.geronimo.kernel.repository.Artifact mavenToGeronimoArtifact(final org.apache.maven.model.Dependency artifact) {
        assert artifact != null;

        return new org.apache.geronimo.kernel.repository.Artifact(artifact.getGroupId(), artifact.getArtifactId(), artifact.getVersion(), artifact.getType());
    }

    protected org.apache.maven.artifact.Artifact geronimoToMavenArtifact(final org.apache.geronimo.kernel.repository.Artifact artifact) throws MojoExecutionException {
        assert artifact != null;

        ArtifactItem item = new ArtifactItem();
        item.setGroupId(artifact.getGroupId());
        item.setArtifactId(artifact.getArtifactId());
        item.setVersion(artifact.getVersion().toString());
        item.setType(artifact.getType());

        return createArtifact(item);
    }

    /**
     * Determine if the given artifact is a Geronimo module.
     *
     * @param artifact  The artifact to check; must not be null.
     * @return          True if the artifact is a Geronimo module.
     */
    protected boolean isModuleArtifact(final org.apache.geronimo.kernel.repository.Artifact artifact) {
        assert artifact != null;

        return "car".equals(artifact.getType());
    }

    protected boolean includeDependency(org.apache.maven.model.Dependency dependency) {
        if (dependency.getGroupId().startsWith("org.apache.geronimo.genesis")) {
            return false;
        }
        String scope = dependency.getScope();
        return scope == null || "runtime".equalsIgnoreCase(scope) || "compile".equalsIgnoreCase(scope);
    }

    protected org.apache.maven.model.Dependency resolveDependency(org.apache.maven.model.Dependency dependency, List<org.apache.maven.model.Dependency> artifacts) {
        for (org.apache.maven.model.Dependency match: artifacts) {
            if (matches(dependency, match)) {
                return match;
            }
        }
        throw new IllegalStateException("Dependency " + dependency + " is not resolved in project");
    }

    private boolean matches(org.apache.maven.model.Dependency dependency, org.apache.maven.model.Dependency match) {
        if (dependency.getGroupId() != null && !dependency.getGroupId().equals(match.getGroupId())) {
            return false;
        }
        if (dependency.getArtifactId() != null && !dependency.getArtifactId().equals(match.getArtifactId())) {
            return false;
        }
        if (dependency.getType() != null && !dependency.getType().equals(match.getType())) {
            return false;
        }
        return true;
    }

    protected void getDependencies(MavenProject project) throws ProjectBuildingException, InvalidDependencyVersionException, ArtifactResolutionException {
        Map managedVersions = DependencyHelper.getManagedVersionMap(project, artifactFactory);

        if (project.getDependencyArtifacts() == null) {
            project.setDependencyArtifacts(project.createArtifacts(artifactFactory, null, null));
        }

        DependencyListener listener = new DependencyListener();

        ArtifactResolutionResult artifactResolutionResult = dependencyHelper.getArtifactCollector().collect(
                project.getDependencyArtifacts(),
                project.getArtifact(),
                managedVersions,
                getArtifactRepository(),
                project.getRemoteArtifactRepositories(),
                dependencyHelper.getArtifactMetadataSource(),
                null,
                Collections.singletonList(listener));

        dependencies = artifactResolutionResult.getArtifacts();
        projectNode = listener.getTop();
        for (Artifact artifact: dependencies) {
            dependencyMap.put(DependencyListener.shrink(artifact), artifact);
        }
    }

    protected class ArtifactLookupImpl
        implements Maven2RepositoryAdapter.ArtifactLookup
    {

        private final Map<org.apache.geronimo.kernel.repository.Artifact, Artifact> resolvedArtifacts;

        public ArtifactLookupImpl(Map<org.apache.geronimo.kernel.repository.Artifact, Artifact> resolvedArtifacts) {
            this.resolvedArtifacts = resolvedArtifacts;
        }

        public File getBasedir() {
            String path = getArtifactRepository().getBasedir();
            return new File(path);
        }

        private boolean isProjectArtifact(final org.apache.geronimo.kernel.repository.Artifact artifact) {
            MavenProject project = getProject();

            return artifact.getGroupId().equals(project.getGroupId()) &&
                   artifact.getArtifactId().equals(project.getArtifactId());
        }

        public File getLocation(final org.apache.geronimo.kernel.repository.Artifact artifact) {
            assert artifact != null;

            boolean debug = log.isDebugEnabled();

            Artifact mavenArtifact = resolvedArtifacts.get(artifact);

            // If not cached, then make a new artifact
            if (mavenArtifact == null) {
                mavenArtifact = getArtifactFactory().createArtifact(
                        artifact.getGroupId(),
                        artifact.getArtifactId(),
                        artifact.getVersion().toString(),
                        null,
                        artifact.getType()
                );
            }

            // Do not attempt to resolve an artifact that is the same as the project
            if (isProjectArtifact(artifact)) {
                if (debug) {
                    log.debug("Skipping resolution of project artifact: " + artifact);
                }

                //
                // HACK: Still have to return something, otherwise some CAR packaging will fail...
                //       no idea what is using this file, or if the files does exist if that will be
                //       used instead of any details we are currently building
                //
                return new File(getBasedir(), getArtifactRepository().pathOf(mavenArtifact));
            }

            File file;
            try {
                if (!mavenArtifact.isResolved()) {
                    if (debug) {
                        log.debug("Resolving artifact: " + mavenArtifact);
                    }
                    mavenArtifact = resolveArtifact(mavenArtifact);

                    // Cache the resolved artifact
                    resolvedArtifacts.put(artifact, mavenArtifact);
                }

                //
                // HACK: Construct the real local filename from the path and resolved artifact file.
                //       Probably a better way to do this with the Maven API directly, but this is the
                //       best I can do for now.
                //
                String path = getArtifactRepository().pathOf(mavenArtifact);
                file = new File(getBasedir(), path);
                file = new File(mavenArtifact.getFile().getParentFile(), file.getName());
            }
            catch (MojoExecutionException e) {
                throw new RuntimeException("Failed to resolve: " + mavenArtifact, e);
            }

            return file;
        }
    }
    
}
