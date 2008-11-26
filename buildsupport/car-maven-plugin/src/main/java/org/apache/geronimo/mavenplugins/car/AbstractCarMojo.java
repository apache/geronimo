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
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.LinkedHashSet;
import java.util.HashMap;
import java.util.Collection;

import org.apache.geronimo.kernel.repository.*;
import org.apache.geronimo.system.plugin.model.ArtifactType;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.factory.ArtifactFactory;
import org.apache.maven.artifact.metadata.ArtifactMetadataSource;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.artifact.resolver.ArtifactCollector;
import org.apache.maven.artifact.resolver.ArtifactResolutionException;
import org.apache.maven.artifact.resolver.ArtifactResolutionResult;
import org.apache.maven.plugin.Mojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.plugin.logging.SystemStreamLog;
import org.apache.maven.project.MavenProject;
import org.apache.maven.project.ProjectBuildingException;
import org.apache.maven.project.artifact.InvalidDependencyVersionException;
import org.apache.maven.shared.dependency.tree.DependencyNode;
import org.apache.maven.shared.dependency.tree.DependencyTreeResolutionListener;
import org.codehaus.plexus.logging.AbstractLogEnabled;

/**
 * Support for <em>packaging</em> Mojos.
 *
 * @version $Rev$ $Date$
 */
public abstract class AbstractCarMojo
        extends AbstractLogEnabled implements Mojo {

    private Log log;

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

    protected Set<Artifact> dependencies;
    protected Set<Artifact> localDependencies;


    /**
     * The artifact repository to use.
     *
     * @parameter expression="${localRepository}"
     * @required
     * @readonly
     */
    private ArtifactRepository localRepository;

    /**
     * The artifact factory to use.
     *
     * @component
     * @required
     * @readonly
     */
    protected ArtifactFactory artifactFactory;

    /**
     * The artifact metadata source to use.
     *
     * @component
     * @required
     * @readonly
     */
    private ArtifactMetadataSource artifactMetadataSource;

    /**
     * The artifact collector to use.
     *
     * @component
     * @required
     * @readonly
     */
    private ArtifactCollector artifactCollector;

    protected String treeListing;

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
//        super.init();
    }

    /**
     * Generates a properties file with explicit versions of artifacts of the current project transitivly.
     */
    protected void generateExplicitVersionProperties(final File outputFile, Set<org.apache.maven.artifact.Artifact> dependencies) throws MojoExecutionException, IOException {
        getLog().debug("Generating explicit version properties: " + outputFile);

        // Generate explicit_versions for all our dependencies...
        Properties props = new Properties();

        for (org.apache.maven.artifact.Artifact artifact : dependencies) {
            String name = artifact.getGroupId() + "/" + artifact.getArtifactId() + "//" + artifact.getType();
            String value = artifact.getGroupId() + "/" + artifact.getArtifactId() + "/" + artifact.getVersion() + "/" + artifact.getType();

            if (getLog().isDebugEnabled()) {
                getLog().debug("Setting " + name + "=" + value);
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
        } else if (classifier.trim().length() > 0 && !classifier.startsWith("-")) {
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
        return artifactFactory.createArtifactWithClassifier(artifact.getGroupId(), artifact.getArtifactId(), artifact.getVersion().toString(), artifact.getType(), null);
    }

    /**
     * Determine if the given artifact is a Geronimo module.
     *
     * @param artifact The artifact to check; must not be null.
     * @return True if the artifact is a Geronimo module.
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
        for (org.apache.maven.model.Dependency match : artifacts) {
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

    protected void getDependencies(MavenProject project, boolean useTransitiveDependencies) throws MojoExecutionException {

        DependencyTreeResolutionListener listener = new DependencyTreeResolutionListener(getLogger());

        DependencyNode rootNode;
        try {
            Map managedVersions = project.getManagedVersionMap();

            Set dependencyArtifacts = project.getDependencyArtifacts();

            if (dependencyArtifacts == null) {
                dependencyArtifacts = project.createArtifacts(artifactFactory, null, null);
            }
            ArtifactResolutionResult result = artifactCollector.collect(dependencyArtifacts, project.getArtifact(), managedVersions, localRepository,
                    project.getRemoteArtifactRepositories(), artifactMetadataSource, null,
                    Collections.singletonList(listener));

            dependencies = result.getArtifacts();
            rootNode = listener.getRootNode();
        }
        catch (ArtifactResolutionException exception) {
            throw new MojoExecutionException("Cannot build project dependency tree", exception);
        }
        catch (InvalidDependencyVersionException e) {
            throw new MojoExecutionException("Invalid dependency version for artifact "
                    + project.getArtifact());
        }

        Scanner scanner = new Scanner();
        scanner.scan(rootNode, useTransitiveDependencies);
        localDependencies = scanner.localDependencies;
        treeListing = scanner.getLog();
    }

    public void setLog(Log log) {
        this.log = log;
    }

    public Log getLog() {
        if (log == null) {
            setLog(new SystemStreamLog());
        }
        return log;
    }

    protected static org.apache.geronimo.kernel.repository.Dependency toGeronimoDependency(final Artifact dependency, boolean includeVersion) {
        org.apache.geronimo.kernel.repository.Artifact artifact = toGeronimoArtifact(dependency, includeVersion);
        return new org.apache.geronimo.kernel.repository.Dependency(artifact, ImportType.ALL);
    }

    private static org.apache.geronimo.kernel.repository.Artifact toGeronimoArtifact(final Artifact dependency, boolean includeVersion) {
        String groupId = dependency.getGroupId();
        String artifactId = dependency.getArtifactId();
        String version = includeVersion ? dependency.getVersion() : null;
        String type = dependency.getType();

        return new org.apache.geronimo.kernel.repository.Artifact(groupId, artifactId, version, type);
    }

    protected LinkedHashSet<Dependency> toDependencies(Collection<Dependency> listedDependencies, UseMavenDependencies useMavenDependencies, boolean includeImport) throws InvalidDependencyVersionException, ArtifactResolutionException, ProjectBuildingException, MojoExecutionException {
        LinkedHashSet<Dependency> dependencies = new LinkedHashSet<Dependency>();

        if (useMavenDependencies == null || !useMavenDependencies.isValue()) {
            dependencies.addAll(listedDependencies);
        } else {
            Map<String, Dependency> explicitDependencyMap = new HashMap<String, Dependency>();
            for (Dependency dependency : listedDependencies) {
                explicitDependencyMap.put(getKey(dependency), dependency);
            }


            getDependencies(project, useMavenDependencies.isUseTransitiveDependencies());
            for (org.apache.maven.artifact.Artifact artifact: localDependencies) {
                Dependency explicitDependency = explicitDependencyMap.get(getKey(artifact));
                dependencies.add(toDependency(artifact, useMavenDependencies.isIncludeVersion(), explicitDependency, includeImport));
            }
        }

        return dependencies;
    }

    private Dependency toDependency(Artifact artifact, boolean includeVersion, Dependency explicitDependency, boolean includeImport) {
        Dependency dependency = new Dependency();
        dependency.setGroupId(artifact.getGroupId());
        dependency.setArtifactId(artifact.getArtifactId());
        dependency.setVersion(includeVersion ? artifact.getVersion() : null);
        dependency.setType(artifact.getType());
        String importType = ImportType.ALL.getName();
        if (includeImport && explicitDependency != null && explicitDependency.getImport() != null) {
            importType = explicitDependency.getImport();
        }
        dependency.setImport(importType);
        if (explicitDependency != null) {
            dependency.setStart(explicitDependency.isStart());
        }
        return dependency;
    }

    private String getKey(Dependency dependency) {
        return dependency.getGroupId() + "/" + dependency.getArtifactId() + "/" + dependency.getType();
    }
    private String getKey(Artifact dependency) {
        return dependency.getGroupId() + "/" + dependency.getArtifactId() + "/" + dependency.getType();
    }

    protected ArtifactType getModuleId() {
        ArtifactType artifactType = new ArtifactType();
        artifactType.setGroupId(project.getGroupId());
        artifactType.setArtifactId(project.getArtifactId());
        artifactType.setVersion(project.getVersion());
        artifactType.setType(project.getArtifact().getType());
        return artifactType;
    }


    private static class Scanner {
        private static enum Accept {
            ACCEPT(true, true),
            PROVIDED(true, false),
            STOP(false, false);

            private final boolean more;
            private final boolean local;

            private Accept(boolean more, boolean local) {
                this.more = more;
                this.local = local;
            }

            public boolean isContinue() {
                return more;
            }

            public boolean isLocal() {
                return local;
            }
        }

        //all the dependencies needed for this car, with provided dependencies removed
        private final Set<Artifact> localDependencies = new LinkedHashSet<Artifact>();
        //dependencies from ancestor cars, to be removed from localDependencies.
        private final Set<Artifact> carDependencies = new LinkedHashSet<Artifact>();

        private final StringBuilder log = new StringBuilder();

        public void scan(DependencyNode rootNode, boolean useTransitiveDependencies) {
            for (DependencyNode child : (List<DependencyNode>) rootNode.getChildren()) {
                scan(child, Accept.ACCEPT, useTransitiveDependencies, false, "");
            }
            if (useTransitiveDependencies) {
                localDependencies.removeAll(carDependencies);
            }
        }

        private void scan(DependencyNode rootNode, Accept parentAccept, boolean useTransitiveDependencies, boolean isFromCar, String indent) {
            Artifact artifact = getArtifact(rootNode);

            Accept accept = accept(artifact, parentAccept);
            if (accept.isContinue()) {
//                if (accept.isLocal()) {
                if (isFromCar) {
                    if (!artifact.getType().equals("car")) {
                        log.append(indent).append("from car:").append(artifact).append("\n");
                        carDependencies.add(artifact);
                    } else {
                        log.append(indent).append("is car:").append(artifact).append("\n");
                    }
                } else {
                    log.append(indent).append("local:").append(artifact).append("\n");
                    if (carDependencies.contains(artifact)) {
                        log.append(indent).append("already in car, returning:").append(artifact).append("\n");
                        return;
                    }
                    localDependencies.add(artifact);
                    if (artifact.getType().equals("car") || !useTransitiveDependencies) {
                        isFromCar = true;
//                        localDependencies = carDependencies;
                    }
                }
                for (DependencyNode child : (List<DependencyNode>) rootNode.getChildren()) {
                    scan(child, accept, useTransitiveDependencies, isFromCar, indent + "  ");
                }
            }
        }

        public String getLog() {
            return log.toString();
        }

        private Artifact getArtifact(DependencyNode rootNode) {
            Artifact artifact = rootNode.getArtifact();
            if (rootNode.getRelatedArtifact() != null) {
                artifact = rootNode.getRelatedArtifact();
            }
            return artifact;
        }

        private Accept accept(Artifact dependency, Accept previous) {
            if (dependency.getGroupId().startsWith("org.apache.geronimo.genesis")) {
                return Accept.STOP;
            }
            String scope = dependency.getScope();
            if (scope == null || "runtime".equalsIgnoreCase(scope) || "compile".equalsIgnoreCase(scope)) {
                return previous;
            }
//            if ("provided".equalsIgnoreCase(scope)) {
//                return Accept.PROVIDED;
//            }
            return Accept.STOP;
        }

    }

    protected class ArtifactLookupImpl
            implements Maven2RepositoryAdapter.ArtifactLookup {

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
            Artifact mavenArtifact;

            // Do not attempt to resolve an artifact that is the same as the project
            if (isProjectArtifact(artifact) && artifact.getVersion() == null) {
                throw new IllegalStateException("WTF? project has no version??");
            }

            if (artifact.getVersion() == null) {
                if (log.isDebugEnabled()) {
                    getLog().debug("Resolving artifact: " + artifact);
                }
                mavenArtifact = resolveArtifact(artifact.getGroupId(), artifact.getArtifactId(), artifact.getType());

            } else {
                mavenArtifact = artifactFactory.createArtifactWithClassifier(
                        artifact.getGroupId(),
                        artifact.getArtifactId(),
                        artifact.getVersion().toString(),
                        artifact.getType(),
                        null);
            }

            String path = getArtifactRepository().pathOf(mavenArtifact);
            return new File(getBasedir(), path);
        }
    }

    protected Artifact resolveArtifact(String groupId, String artifactId, String type) {
        for (Artifact artifact : dependencies) {
            if (matches(groupId, artifactId, type, artifact)) {
                return artifact;
            }
        }
        return null;
    }

    private boolean matches(String groupId, String artifactId, String type, Artifact artifact) {
        if (!groupId.equals(artifact.getGroupId())) return false;
        if (!artifactId.equals(artifact.getArtifactId())) return false;
        if (!type.equals(artifact.getType())) return false;

        return true;
    }

}
