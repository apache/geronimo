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
import java.lang.*;
import java.lang.reflect.Field;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.ServiceLoader;
import java.util.Set;
import java.util.LinkedHashMap;
import java.util.ArrayList;
import java.util.concurrent.CountDownLatch;

import org.apache.geronimo.system.plugin.model.ArtifactType;
import org.apache.geronimo.system.plugin.model.DependencyType;
import org.apache.geronimo.system.plugin.model.ImportType;
import org.apache.karaf.main.Main;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.factory.ArtifactFactory;
import org.apache.maven.artifact.metadata.ArtifactMetadataSource;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.artifact.resolver.ArtifactCollector;
import org.apache.maven.artifact.resolver.ArtifactResolutionException;
import org.apache.maven.artifact.resolver.ArtifactResolutionResult;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.plugin.Mojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.plugin.logging.SystemStreamLog;
import org.apache.maven.project.MavenProject;
import org.apache.maven.project.ProjectBuildingException;
import org.apache.maven.project.MavenProjectHelper;
import org.apache.maven.project.artifact.InvalidDependencyVersionException;
import org.apache.maven.shared.dependency.tree.DependencyNode;
import org.apache.maven.shared.dependency.tree.DependencyTreeResolutionListener;
import org.apache.maven.shared.filtering.MavenFileFilter;
import org.apache.maven.shared.filtering.MavenFilteringException;
import org.apache.maven.shared.filtering.MavenResourcesFiltering;
import org.codehaus.plexus.logging.AbstractLogEnabled;
import org.codehaus.plexus.util.ReaderFactory;
import org.codehaus.plexus.util.StringUtils;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkEvent;
import org.osgi.framework.FrameworkListener;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.launch.Framework;
import org.osgi.framework.launch.FrameworkFactory;
import org.osgi.framework.BundleException;
import org.osgi.framework.Constants;
import org.osgi.framework.startlevel.FrameworkStartLevel;

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
     * The maven project's helper.
     *
     * @component
     * @required
     * @readonly
     */
    protected MavenProjectHelper projectHelper;

    /**
     * The basedir of the project.
     *
     * @parameter expression="${basedir}"
     * @required
     * @readonly
     */
    protected File basedir;

    protected Set<Artifact> dependencyArtifacts;
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

    //filtering support
    /**
     * The character encoding scheme to be applied when filtering resources.
     *
     * @parameter expression="${encoding}" default-value="${project.build.sourceEncoding}"
     */
    protected String encoding;

    /**
     * @component role="org.apache.maven.shared.filtering.MavenResourcesFiltering" role-hint="default"
     * @required
     */
    protected MavenResourcesFiltering mavenResourcesFiltering;

    /**
     * @parameter expression="${session}"
     * @readonly
     * @required
     */
    protected MavenSession session;

    /**
     * Expression preceded with the String won't be interpolated
     * \${foo} will be replaced with ${foo}
     *
     * @parameter expression="${maven.resources.escapeString}"
     * @since 2.3
     */
    protected String escapeString = "\\";

    /**
     * @plexus.requirement role-hint="default"
     * @component
     * @required
     */
    protected MavenFileFilter mavenFileFilter;
    /**
     * System properties.
     *
     * @parameter
     */
    protected Map<String, String> systemProperties;
    private Map<String,String> previousSystemProperties;

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

            this.dependencyArtifacts = result.getArtifacts();
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
        localDependencies = scanner.localDependencies.keySet();
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
        return new org.apache.geronimo.kernel.repository.Dependency(artifact, org.apache.geronimo.kernel.repository.ImportType.ALL);
    }

    private static org.apache.geronimo.kernel.repository.Artifact toGeronimoArtifact(final Artifact dependency, boolean includeVersion) {
        String groupId = dependency.getGroupId();
        String artifactId = dependency.getArtifactId();
        String version = includeVersion ? dependency.getVersion() : null;
        String type = dependency.getType();

        return new org.apache.geronimo.kernel.repository.Artifact(groupId, artifactId, version, type);
    }

    protected LinkedHashSet<DependencyType> toDependencies(List<Dependency> explicitDependencies, UseMavenDependencies useMavenDependencies, boolean includeImport) throws InvalidDependencyVersionException, ArtifactResolutionException, ProjectBuildingException, MojoExecutionException {
        List<DependencyType> dependencyTypes = new ArrayList<DependencyType>();
        for (Dependency dependency : explicitDependencies) {
            dependencyTypes.add(dependency.toDependencyType());
        }
        LinkedHashSet<DependencyType> dependencies = new LinkedHashSet<DependencyType>();

        if (useMavenDependencies == null || !useMavenDependencies.isValue()) {
            dependencies.addAll(dependencyTypes);
            localDependencies = new HashSet<Artifact>();
            for (DependencyType dependency : dependencies) {
                localDependencies.add(geronimoToMavenArtifact(dependency.toArtifact()));
            }
        } else {
            Map<String, DependencyType> explicitDependencyMap = new HashMap<String, DependencyType>();
            for (DependencyType dependency : dependencyTypes) {
                explicitDependencyMap.put(getKey(dependency), dependency);
            }


            getDependencies(project, useMavenDependencies.isUseTransitiveDependencies());
            for (Artifact entry : localDependencies) {
                dependencies.add(toDependencyType(entry, explicitDependencyMap, useMavenDependencies.isIncludeVersion(), includeImport));
            }
        }

        return dependencies;
    }

    DependencyType toDependencyType(Artifact artifact, Map<String, DependencyType> explicitDependencyMap, boolean includeVersion, boolean includeImport) {
        DependencyType explicitDependency = explicitDependencyMap.get(getKey(artifact));
        DependencyType dependency = toDependencyType(artifact, includeVersion, explicitDependency, includeImport);
        return dependency;
    }

    DependencyType toDependencyType(Artifact artifact, boolean includeVersion, DependencyType explicitDependency, boolean includeImport) {
        DependencyType dependency = new DependencyType();
        dependency.setGroupId(artifact.getGroupId());
        dependency.setArtifactId(artifact.getArtifactId());
        String version = null;
        if (includeVersion) {
            if (artifact.getVersionRange() == null) {
                version = artifact.getVersion();
            } else {
                version = artifact.getVersionRange().getRecommendedVersion().toString();
            }
        }
        dependency.setVersion(version);
        dependency.setType(artifact.getType());
        if (includeImport) {
            ImportType importType = ImportType.ALL;
            if (explicitDependency != null && explicitDependency.getImport() != null) {
                importType = explicitDependency.getImport();
            }
            dependency.setImport(importType);
        }
        if (explicitDependency != null) {
            dependency.setStart(explicitDependency.isStart());
        }
        return dependency;
    }

    private static String getKey(DependencyType dependency) {
        return dependency.getGroupId() + "/" + dependency.getArtifactId() + "/" + dependency.getType();
    }

    private static String getKey(Artifact dependency) {
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

    protected void cleanup() {
        unsetSystemProperties(previousSystemProperties);
    }

    protected Map<String, String> setSystemProperties() {
        if (previousSystemProperties != null) {
            throw new IllegalStateException("setSystemProperties called twice");
        }
        if (systemProperties == null) {
            return Collections.emptyMap();
        } else {
            getLog().debug("Setting system properties: " + systemProperties);
            previousSystemProperties = new HashMap<String, String>();
            for (Map.Entry<String, String> entry : systemProperties.entrySet()) {
                String key = entry.getKey();
                String value = entry.getValue();
                String oldValue = System.setProperty(key, value);
                previousSystemProperties.put(key, oldValue);
            }
            return previousSystemProperties;
        }
    }

    protected void unsetSystemProperties(Map<String, String> previousSystemProperties) {
        if (previousSystemProperties != null) {
            for (Map.Entry<String, String> entry : previousSystemProperties.entrySet()) {
                String key = entry.getKey();
                String value = entry.getValue();
                if (value == null) {
                    System.clearProperty(key);
                } else {
                    System.setProperty(key, value);
                }
            }
        }
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
        private final Map<Artifact, Set<Artifact>> localDependencies = new LinkedHashMap<Artifact, Set<Artifact>>();
        //dependencies from ancestor cars, to be removed from localDependencies.
        private final Set<Artifact> carDependencies = new LinkedHashSet<Artifact>();

        private final StringBuilder log = new StringBuilder();

        public void scan(DependencyNode rootNode, boolean useTransitiveDependencies) {
            Set<Artifact> children = new LinkedHashSet<Artifact>();
            for (DependencyNode child : (List<DependencyNode>) rootNode.getChildren()) {
                scan(child, Accept.ACCEPT, useTransitiveDependencies, false, "", children);
            }
            if (useTransitiveDependencies) {
                localDependencies.keySet().removeAll(carDependencies);
            }
        }

        private void scan(DependencyNode rootNode, Accept parentAccept, boolean useTransitiveDependencies, boolean isFromCar, String indent, Set<Artifact> parentsChildren) {
            Artifact artifact = getArtifact(rootNode);

            Accept accept = accept(artifact, parentAccept);
            if (accept.isContinue()) {
                Set<Artifact> children = localDependencies.get(artifact);
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
                        parentsChildren.add(artifact);
                        return;
                    }
                    parentsChildren.add(artifact);
                    if (children == null) {
                        children = new LinkedHashSet<Artifact>();
                        localDependencies.put(artifact, children);
                    }
                    if (artifact.getType().equals("car") || !useTransitiveDependencies) {
                        isFromCar = true;
                    }
                }
                for (DependencyNode child : (List<DependencyNode>) rootNode.getChildren()) {
                    scan(child, accept, useTransitiveDependencies, isFromCar, indent + "  ", children);
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
            if (dependency.getGroupId().startsWith("org.apache.geronimo.genesis")
                    || dependency.getType().equals("kar")
                    || (dependency.getType().equals("xml") && "features".equals(dependency.getClassifier()))) {
                return Accept.STOP;
            }
            String scope = dependency.getScope();
            if (scope == null || "runtime".equalsIgnoreCase(scope) || "compile".equalsIgnoreCase(scope)) {
                return previous;
            }
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
        for (Artifact artifact : dependencyArtifacts) {
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


    protected void filter(File sourceFile, File targetFile)
            throws MojoExecutionException {
        try {

            if (StringUtils.isEmpty(encoding)) {
                getLog().warn(
                        "File encoding has not been set, using platform encoding " + ReaderFactory.FILE_ENCODING
                                + ", i.e. build is platform dependent!");
            }
            targetFile.getParentFile().mkdirs();
            List filters = mavenFileFilter.getDefaultFilterWrappers(project, null, true, session, null);
            mavenFileFilter.copyFile(sourceFile, targetFile, true, filters, encoding, true);
        }
        catch (MavenFilteringException e) {
            throw new MojoExecutionException(e.getMessage(), e);
        }
    }

    private void setLoggingLevel() {
        if (System.getProperty("org.ops4j.pax.logging.DefaultServiceLog.level") == null) {
            System.setProperty("org.ops4j.pax.logging.DefaultServiceLog.level",
                    log.isDebugEnabled() ? "DEBUG" : "INFO");
        }
    }

    protected Framework getFramework() throws BundleException {
        setLoggingLevel();

        File karafHome = new File(new File(basedir, "target"), "assembly");
        System.setProperty(Main.PROP_KARAF_HOME, karafHome.getAbsolutePath());
        System.setProperty(Main.PROP_KARAF_BASE, karafHome.getAbsolutePath());
        System.setProperty(Main.PROP_KARAF_DATA, new File(karafHome, "data").getAbsolutePath());
        System.setProperty(Main.PROP_KARAF_INSTANCES, new File(karafHome, "instances").getAbsolutePath());

        //enable mvn url handling
//        new org.ops4j.pax.url.mvn.internal.Activator().start(framework.getBundleContext());
        //don't allow mvn urls
        if (systemProperties == null) {
            systemProperties = new HashMap<String, String>();
        }
        systemProperties.put("geronimo.build.car", "true");
        //Fix JIRA GERONIMO-5400
        if (null == System.getProperty("openejb.log.factory")) {
            systemProperties.put("openejb.log.factory", "org.apache.openejb.util.PaxLogStreamFactory");
        }
        systemProperties.put("karaf.startLocalConsole", "false");
        systemProperties.put("openejb.geronimo", "true");
        setSystemProperties();

        Main main = new Main(new String[] {});
        try {
            main.launch();
            Framework framework = main.getFramework();
            FrameworkStartLevel frameworkStartLevel = framework.adapt(FrameworkStartLevel.class);
            while (frameworkStartLevel.getStartLevel() < 100) {
                Thread.sleep(100l);
            }
            return framework;
        } catch (Exception e) {
            if (1 == 1) {
                throw new BundleException("Could not start karaf framwork", e);
            }
        }

        Map<String, String> properties = new HashMap<String, String>();
//        properties.put(FelixConstants.EMBEDDED_EXECUTION_PROP, "true");

        // The following set of packages are the ones that will be exported by the
        // framework bundle from the core system.  We need to explicitly specify
        // these to give explicit versions to many of the javax.* packages so that
        // they'll match any of the versions exported by the Geronimo spec jars.
        // This list and the version numbers needs to be synchronized with the list
        // in the karaf framework config.properties file.
        properties.put(Constants.FRAMEWORK_SYSTEMPACKAGES,
                "org.osgi.framework;version=1.6.0," +
                        "org.osgi.framework.launch;version=1.0.0," +
                        "org.osgi.framework.hooks.service;version=1.1.0," +
                        "org.osgi.service.packageadmin;version=1.2.0," +
                        "org.osgi.service.startlevel;version=1.1.0," +
                        "org.osgi.service.url;version=1.0.0," +
                        "org.osgi.util.tracker;version=1.4.0," +
                        "javax.accessibility," +
                        "javax.annotation.processing," +
                        "javax.activity," +
                        "javax.crypto," +
                        "javax.crypto.interfaces," +
                        "javax.crypto.spec," +
                        "javax.imageio," +
                        "javax.imageio.event," +
                        "javax.imageio.metadata," +
                        "javax.imageio.plugins.bmp," +
                        "javax.imageio.plugins.jpeg," +
                        "javax.imageio.spi," +
                        "javax.imageio.stream," +
                        "javax.jws;version=2.0," +
                        "javax.jws.soap;version=2.0," +
                        "javax.lang.model," +
                        "javax.lang.model.element," +
                        "javax.lang.model.type," +
                        "javax.lang.model.util," +
                        "javax.management," +
                        "javax.management.loading," +
                        "javax.management.modelmbean," +
                        "javax.management.monitor," +
                        "javax.management.openmbean," +
                        "javax.management.relation," +
                        "javax.management.remote," +
                        "javax.management.remote.rmi," +
                        "javax.management.timer," +
                        "javax.naming," +
                        "javax.naming.directory," +
                        "javax.naming.event," +
                        "javax.naming.ldap," +
                        "javax.naming.spi," +
                        "javax.net," +
                        "javax.net.ssl," +
                        "javax.print," +
                        "javax.print.attribute," +
                        "javax.print.attribute.standard," +
                        "javax.print.event," +
                        "javax.rmi," +
                        "javax.rmi.CORBA," +
                        "javax.rmi.ssl," +
                        "javax.script," +
                        "javax.security.auth," +
                        "javax.security.auth.callback," +
                        "javax.security.auth.kerberos," +
                        "javax.security.auth.login," +
                        "javax.security.auth.spi," +
                        "javax.security.auth.x500," +
                        "javax.security.cert," +
                        "javax.security.sasl," +
                        "javax.sound.midi," +
                        "javax.sound.midi.spi," +
                        "javax.sound.sampled," +
                        "javax.sound.sampled.spi," +
                        "javax.sql," +
                        "javax.sql.rowset," +
                        "javax.sql.rowset.serial," +
                        "javax.sql.rowset.spi," +
                        "javax.swing," +
                        "javax.swing.border," +
                        "javax.swing.colorchooser," +
                        "javax.swing.event," +
                        "javax.swing.filechooser," +
                        "javax.swing.plaf," +
                        "javax.swing.plaf.basic," +
                        "javax.swing.plaf.metal," +
                        "javax.swing.plaf.multi," +
                        "javax.swing.plaf.synth," +
                        "javax.swing.table," +
                        "javax.swing.text," +
                        "javax.swing.text.html," +
                        "javax.swing.text.html.parser," +
                        "javax.swing.text.rtf," +
                        "javax.swing.tree," +
                        "javax.swing.undo," +
                        "javax.tools," +
                        "javax.transaction;javax.transaction.xa;version=1.1;partial=true;mandatory:=partial," +
                        "javax.xml," +
                        "javax.xml.namespace;version=1.0," +
                        "javax.xml.crypto," +
                        "javax.xml.crypto.dom," +
                        "javax.xml.crypto.dsig," +
                        "javax.xml.crypto.dsig.dom," +
                        "javax.xml.crypto.dsig.keyinfo," +
                        "javax.xml.crypto.dsig.spec," +
                        "javax.xml.datatype," +
                        "javax.xml.parsers," +
                        "javax.xml.transform," +
                        "javax.xml.transform.dom," +
                        "javax.xml.transform.sax," +
                        "javax.xml.transform.stax," +
                        "javax.xml.transform.stream," +
                        "javax.xml.validation," +
                        "javax.xml.xpath," +
                        "org.ietf.jgss," +
                        "org.omg.CORBA," +
                        "org.omg.CORBA_2_3," +
                        "org.omg.CORBA_2_3.portable," +
                        "org.omg.CORBA.DynAnyPackage," +
                        "org.omg.CORBA.ORBPackage," +
                        "org.omg.CORBA.portable," +
                        "org.omg.CORBA.TypeCodePackage," +
                        "org.omg.CosNaming," +
                        "org.omg.CosNaming.NamingContextExtPackage," +
                        "org.omg.CosNaming.NamingContextPackage," +
                        "org.omg.Dynamic," +
                        "org.omg.DynamicAny," +
                        "org.omg.DynamicAny.DynAnyFactoryPackage," +
                        "org.omg.DynamicAny.DynAnyPackage," +
                        "org.omg.IOP," +
                        "org.omg.IOP.CodecFactoryPackage," +
                        "org.omg.IOP.CodecPackage," +
                        "org.omg.Messaging," +
                        "org.omg.PortableInterceptor," +
                        "org.omg.PortableInterceptor.ORBInitInfoPackage," +
                        "org.omg.PortableServer," +
                        "org.omg.PortableServer.CurrentPackage," +
                        "org.omg.PortableServer.POAManagerPackage," +
                        "org.omg.PortableServer.POAPackage," +
                        "org.omg.PortableServer.portable," +
                        "org.omg.PortableServer.ServantLocatorPackage," +
                        "org.omg.SendingContext," +
                        "org.omg.stub.java.rmi," +
                        "org.omg.stub.javax.management.remote.rmi," +
                        "org.w3c.dom," +
                        "org.w3c.dom.bootstrap," +
                        "org.w3c.dom.css," +
                        "org.w3c.dom.events," +
                        "org.w3c.dom.html," +
                        "org.w3c.dom.ls," +
                        "org.w3c.dom.ranges," +
                        "org.w3c.dom.stylesheets," +
                        "org.w3c.dom.traversal," +
                        "org.w3c.dom.views," +
                        "org.w3c.dom.xpath," +
                        "org.xml.sax," +
                        "org.xml.sax.ext," +
                        "org.xml.sax.helpers");

        properties.put(Constants.FRAMEWORK_SYSTEMPACKAGES_EXTRA,
                "net.sf.cglib.asm," +
                        "net.sf.cglib.core," +
                        "net.sf.cglib.proxy," +
                        "net.sf.cglib.reflect," +
                        "sun.misc," +
                        "sun.reflect," +
                        "org.apache.commons.jexl;version=\"1.1\"," +
                        "org.apache.commons.jexl.context;version=\"1.1\"," +
                        "org.apache.commons.jexl.resolver;version=\"1.1\"," +
                        "org.apache.geronimo.main," +
                        "org.apache.geronimo.cli," +
                        "org.apache.geronimo.cli.client," +
                        "org.apache.geronimo.cli.daemon," +
                        "org.apache.geronimo.common," +
                        "org.apache.geronimo.common.propertyeditor," +
                        "org.apache.geronimo.crypto," +
                        "org.apache.geronimo.gbean," +
                        "org.apache.geronimo.gbean.annotation," +
                        "org.apache.geronimo.gbean.runtime," +

                        "org.apache.geronimo.kernel," +
                        "org.apache.geronimo.kernel.basic," +
                        "org.apache.geronimo.kernel.classloader," +
                        "org.apache.geronimo.kernel.config," +
                        "org.apache.geronimo.kernel.config.xstream," +
                        "org.apache.geronimo.kernel.lifecycle," +
                        "org.apache.geronimo.kernel.management," +
                        "org.apache.geronimo.kernel.osgi," +
                        "org.apache.geronimo.kernel.proxy," +
                        "org.apache.geronimo.kernel.repository," +
                        "org.apache.geronimo.kernel.rmi," +
                        "org.apache.geronimo.kernel.util," +

                        "org.apache.geronimo.system.configuration," +
                        "org.apache.geronimo.system.configuration.cli," +
                        "org.apache.geronimo.system.configuration.condition," +
                        "org.apache.geronimo.system.jmx," +
                        "org.apache.geronimo.system.logging," +
                        "org.apache.geronimo.system.logging.log4j," +
                        "org.apache.geronimo.system.main," +
                        "org.apache.geronimo.system.plugin," +
                        "org.apache.geronimo.system.plugin.model," +
                        "org.apache.geronimo.system.properties," +
                        "org.apache.geronimo.system.repository," +
                        "org.apache.geronimo.system.resolver," +
                        "org.apache.geronimo.system.serverinfo," +
                        "org.apache.geronimo.system.threads," +
                        "org.apache.geronimo.system.util," +
                        "org.apache.geronimo.transformer," +
                        "org.apache.geronimo.hook," +
                        "org.apache.geronimo.mavenplugins.car," +
                        "org.apache.karaf.jaas.boot;version=\"3.0.0.SNAPSHOT\"," +
                        "org.apache.yoko," +
                        "org.apache.yoko.osgi," +
                        "org.apache.yoko.rmispec.util," +
                        " org.apache.geronimo.hook"
        );
        /*

                                "org.apache.log4j;version=\"1.2.12\"," +
                                "org.apache.log4j.helpers;version=\"1.2.12\"," +
                                "org.apache.log4j.spi;version=\"1.2.12\"," +
                                "org.apache.log4j.xml;version=\"1.2.12\"," +

                                "org.codehaus.classworlds," +
                                "org.codehaus.classworlds.realm," +

                                "org.codehaus.plexus," +
                                "org.codehaus.plexus.archiver," +
                                "org.codehaus.plexus.archiver.jar," +
                                "org.codehaus.plexus.archiver.tar," +
                                "org.codehaus.plexus.archiver.util," +
                                "org.codehaus.plexus.archiver.zip," +
                                "org.codehaus.plexus.component," +
                                "org.codehaus.plexus.component.annotations," +
                                "org.codehaus.plexus.component.composition," +
                                "org.codehaus.plexus.component.configurator," +
                                "org.codehaus.plexus.component.configurator.converters," +
                                "org.codehaus.plexus.component.configurator.expression," +
                                "org.codehaus.plexus.component.discovery," +
                                "org.codehaus.plexus.component.factory," +
                                "org.codehaus.plexus.component.manager," +
                                "org.codehaus.plexus.component.repository," +
                                "org.codehaus.plexus.component.repository.exception," +
                                "org.codehaus.plexus.component.repository.io," +
                                "org.codehaus.plexus.configuration," +
                                "org.codehaus.plexus.configuration.processor," +
                                "org.codehaus.plexus.configuration.xml," +
                                "org.codehaus.plexus.context," +
                                "org.codehaus.plexus.embed," +
                                "org.codehaus.plexus.lifecycle," +
                                "org.codehaus.plexus.lifecycle.phase," +
                                "org.codehaus.plexus.logging," +
                                "org.codehaus.plexus.logging.console," +
                                "org.codehaus.plexus.personality," +
                                "org.codehaus.plexus.personality.plexus," +
                                "org.codehaus.plexus.personality.plexus.lifecycle," +
                                "org.codehaus.plexus.personality.plexus.lifecycle.phase," +
                                "org.codehaus.plexus.util," +
                                "org.codehaus.plexus.util.xml," +

                                "org.apache.maven," +
                                "org.apache.maven.plugin," +
                                "org.apache.maven.lifecyle," +
                                "org.apache.maven.shared," +
                                "org.apache.maven.shared.filtering," +

                                "com.thoughtworks.xstream," +
                                "com.thoughtworks.xstream.alias," +
                                "com.thoughtworks.xstream.converters," +
                                "com.thoughtworks.xstream.converters.basic," +
                                "com.thoughtworks.xstream.converters.reflection," +
                                "com.thoughtworks.xstream.core," +
                                "com.thoughtworks.xstream.io," +
                                "com.thoughtworks.xstream.io.xml," +
                                "com.thoughtworks.xstream.mapper," +
                                "javax.management," +
                                "javax.rmi.ssl," +
                                "javax.xml.parsers," +
                                "javax.xml.transform," +
                                "net.sf.cglib.asm," +
                                "net.sf.cglib.core," +
                                "net.sf.cglib.proxy," +
                                "net.sf.cglib.reflect," +
                                "org.apache.xbean.recipe;version=\"3.6\"," +
                                "org.objectweb.asm," +
                                "org.objectweb.asm.commons," +
                                "org.osgi.framework;version=\"1.4\"," +
                                        "org.slf4j;version=\"1.5.6\"," +
                                        "org.slf4j.impl;version=\"1.5.6\"," +
                                        "org.slf4j.bridge;version=\"1.5.6\"," +
                                "org.w3c.dom," +
                                "org.xml.sax," +
                                "org.xml.sax.helpers," +
                                "sun.misc," +
                                "org.apache.xmlbeans," +
                                "org.apache.xml.resolver," +
                                "org.apache.commons.cli," +
                                "javax.enterprise.deploy," +
                                "javax.enterprise.deploy.model," +
                                "javax.enterprise.deploy.shared," +
                                "javax.enterprise.deploy.spi");
        */

        properties.put(Constants.FRAMEWORK_BOOTDELEGATION, "sun.*,com.sun.*");

        File storageDir = new File(basedir, "target/bundle-cache");
        properties.put(Constants.FRAMEWORK_STORAGE, storageDir.getAbsolutePath());

        if (log.isDebugEnabled()) {
            properties.put("felix.log.level", "4");
        }

        /*
         * A hack for Equinox to restore FrameworkProperties to the initial state.
         * If the FrameworkProperties is not restored to the initial state, Equinox
         * will create a separate classloader and load the Geronimo kernel classes
         * from deployed geronimo-kernel bundle instead of the system bundle.
         * That will result in ClassCastException.
         */
        resetFrameworkProperties();

        ServiceLoader<FrameworkFactory> loader = ServiceLoader.load(FrameworkFactory.class);
        Framework framework = loader.iterator().next().newFramework(properties);
        framework.start();
        //enable mvn url handling
//        new org.ops4j.pax.url.mvn.internal.Activator().start(framework.getBundleContext());
        //don't allow mvn urls
        if (systemProperties == null) {
            systemProperties = new HashMap<String, String>();
        }
        systemProperties.put("geronimo.build.car", "true");
        //Fix JIRA GERONIMO-5400
        if (null == System.getProperty("openejb.log.factory")) {
            systemProperties.put("openejb.log.factory", "org.apache.openejb.util.PaxLogStreamFactory");
        }
        systemProperties.put("karaf.startLocalConsole", "false");
        systemProperties.put("openejb.geronimo", "true");
        setSystemProperties();
        return framework;
    }

    private static void resetFrameworkProperties() {
        try {
            Class clazz = Class.forName("org.eclipse.osgi.framework.internal.core.FrameworkProperties");
            Field f = clazz.getDeclaredField("properties");
            f.setAccessible(true);
            f.set(null, null);
        } catch (ClassNotFoundException e) {
            // ignore
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    protected void listBundles(BundleContext ctx) {
        StringBuilder b = new StringBuilder("Bundles:");
        for (Bundle bundle: ctx.getBundles()) {
            b.append("\n   Id:").append(bundle.getBundleId()).append("  status:").append(bundle.getState()).append("  ").append(bundle.getLocation());
        }
        getLog().info(b.toString());
    }

    protected void waitForBundles(BundleContext ctx, long timeout) throws MojoExecutionException {
        long done = System.currentTimeMillis() + timeout;
        while (done > System.currentTimeMillis()) {
            boolean allStarted = true;
            for (Bundle bundle: ctx.getBundles()) {
                if (bundle.getState() != 32  && notFragment(bundle)) {
                    allStarted = false;
                    break;
                }
                if (allStarted) {
                    return;
                }
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {

                }
            }
        }
        listBundles(ctx);
        throw new MojoExecutionException("Cant start all the bundles");
    }

    protected boolean notFragment(Bundle bundle) {
        return bundle.getHeaders().get(Constants.FRAGMENT_HOST) == null;
    }

    protected Object getService(BundleContext ctx, String clazzName, String filter, long initialTimeout) throws MojoExecutionException {
//        String filter = null;
//        if (componentName != null) {
//            filter = "(osgi.service.blueprint.compname=" + componentName + ")";
//        }
        long timeout = initialTimeout;
        while (timeout > 0) {
            ServiceReference sr = null;
            if (filter == null) {
                sr = ctx.getServiceReference(clazzName);
            } else {
                ServiceReference[] refs;
                try {
                    refs = ctx.getServiceReferences(clazzName, filter);
                } catch (InvalidSyntaxException e) {
                    throw new MojoExecutionException("filter syntax problem", e);
                }
                if (refs != null && refs.length == 1) {
                    sr = refs[0];
                }
            }
            if (sr != null) {
//                services.add(sr);
                return ctx.getService(sr);
            }
            try {
                Thread.sleep(100L);
            } catch (InterruptedException e) {
                throw new MojoExecutionException("Interrupted waiting for service " + clazzName + " at " + (initialTimeout - timeout)/1000 + " seconds");
            }
            timeout = timeout - 100;
        }
        throw new MojoExecutionException("Could not get service " + clazzName + " in " + initialTimeout/1000 + " seconds");
    }

}
