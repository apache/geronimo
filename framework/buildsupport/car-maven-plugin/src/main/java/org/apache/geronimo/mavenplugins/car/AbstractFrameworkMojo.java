/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */


package org.apache.geronimo.mavenplugins.car;

import java.io.File;
import java.lang.Override;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.geronimo.deployment.ConfigurationBuilder;
import org.apache.geronimo.deployment.Deployer;
import org.apache.karaf.features.FeaturesService;
import org.apache.karaf.tooling.features.DependencyHelper;
import org.apache.maven.artifact.handler.manager.ArtifactHandlerManager;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.project.MavenProject;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleException;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.launch.Framework;
import org.sonatype.aether.RepositorySystem;
import org.sonatype.aether.RepositorySystemSession;
import org.sonatype.aether.artifact.Artifact;
import org.sonatype.aether.repository.RemoteRepository;
import org.sonatype.aether.resolution.ArtifactRequest;
import org.sonatype.aether.resolution.ArtifactResolutionException;
import org.sonatype.aether.resolution.ArtifactResult;
import org.sonatype.aether.util.artifact.DefaultArtifact;

/**
 * Base for karaf-aware mojos
 *
 * @version $Rev$ $Date$
 */
public class AbstractFrameworkMojo extends AbstractMojo {
    /**
     * The entry point to Aether, i.e. the component doing all the work.
     *
     * @component
     */
    private RepositorySystem repoSystem;

    /**
     * The current repository/network configuration of Maven.
     *
     * @parameter default-value="${repositorySystemSession}"
     * @readonly
     */
    private RepositorySystemSession repoSession;

    /**
     * The project's remote repositories to use for the resolution of plugins and their dependencies.
     *
     * @parameter default-value="${project.remoteProjectRepositories}"
     * @readonly
     */
    private List<RemoteRepository> remoteRepos;

    /**
     * where we set up karaf
     *
     * @parameter default-value="${project.build.directory}/karaf"
     */
    private String karafHome;

    /**
     * how long to wait for a service
     *
     * @parameter default-value="20000"
     */
    private long timeout = 20000L;

    private Framework framework;
    private List<ServiceReference> services = new ArrayList<ServiceReference>();


    @java.lang.Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        initializeFramework();
        try {
            doExecute();
        } finally {
            done();
        }
    }

    protected void doExecute() throws MojoExecutionException {
        getService(FeaturesService.class);
        getService(Deployer.class);
        getService(ConfigurationBuilder.class);
//        listBundles();
    }

    protected void done() throws MojoExecutionException {
        for (ServiceReference sr: services) {
            framework.getBundleContext().ungetService(sr);
        }
        try {
            framework.stop();
        } catch (BundleException e) {
            throw new MojoExecutionException("Can't stop framework: ", e );
        }
    }


    public Framework getFramework() {
        return framework;
    }

    protected <T> T getService(Class<T> clazz, String componentName) throws MojoExecutionException {
        String filter = null;
        if (componentName != null) {
            filter = "(osgi.service.blueprint.compname=" + componentName + ")";
        }
        long timeout = this.timeout;
        while (timeout > 0) {
            ServiceReference sr = null;
            if (filter == null) {
                sr = framework.getBundleContext().getServiceReference(clazz.getName());
            } else {
                ServiceReference[] refs = new ServiceReference[0];
                try {
                    refs = framework.getBundleContext().getServiceReferences(clazz.getName(), filter);
                } catch (InvalidSyntaxException e) {
                    throw new MojoExecutionException("filter syntax problem", e);
                }
                if (refs != null && refs.length == 1) {
                    sr = refs[0];
                }
            }
            if (sr != null) {
                services.add(sr);
                return (T)framework.getBundleContext().getService(sr);
            }
            try {
                Thread.sleep(100L);
            } catch (InterruptedException e) {
                throw new MojoExecutionException("Interrupted waiting for service " + clazz.getName() + " at " + (this.timeout - timeout)/1000 + " seconds");
            }
            timeout = timeout - 100;
        }
        throw new MojoExecutionException("Could not get service " + clazz.getName() + " in " + this.timeout/1000 + " seconds");
    }

    protected <T> T getService(Class<T> clazz) throws MojoExecutionException {
        return getService(clazz, null);
    }

    void initializeFramework() throws MojoFailureException {
        FrameworkHelper helper = new FrameworkHelper(karafHome, new AetherResolver());
        try {
            framework = helper.start();
        } catch (Exception e) {
            throw new MojoFailureException("Could not start karaf framework", e);
        }
    }

    protected Map<Artifact, String> getTransitiveDependencies(MavenProject project) throws MojoExecutionException {
        DependencyHelper dependencyHelper = new DependencyHelper(remoteRepos, remoteRepos, repoSession, repoSystem, true);
        dependencyHelper.getDependencies(project, true);
        return dependencyHelper.getLocalDependencies();
    }

    protected void listBundles() {
        StringBuilder b = new StringBuilder("Bundles:");
        for (Bundle bundle: framework.getBundleContext().getBundles()) {
            b.append("\n   Id:").append(bundle.getBundleId()).append("  status:").append(bundle.getState()).append("  ").append(bundle.getLocation());
        }
        getLog().info(b.toString());
    }

    protected File resolve(Artifact artifact) {
        ArtifactRequest request = new ArtifactRequest();
        request.setArtifact(artifact);
        request.setRepositories(remoteRepos);

        getLog().debug("Resolving artifact " + artifact +
                " from " + remoteRepos);

        ArtifactResult result;
        try {
            result = repoSystem.resolveArtifact(repoSession, request);
        } catch (ArtifactResolutionException e) {
            getLog().warn("could not resolve " + artifact, e);
            return null;
        }

        getLog().debug("Resolved artifact " + artifact + " to " +
                result.getArtifact().getFile() + " from "
                + result.getRepository());
        return result.getArtifact().getFile();

    }

    class AetherResolver implements FrameworkHelper.Resolver {

        @Override
        public File resolve(String id) {
            if (id.startsWith("mvn:")) {
                id = id.substring("mvn:".length()).replaceAll("/", ":");
            }
            ArtifactRequest request = new ArtifactRequest();
            request.setArtifact(
                    new DefaultArtifact(id));
            request.setRepositories(remoteRepos);

            getLog().debug("Resolving artifact " + id +
                    " from " + remoteRepos);

            ArtifactResult result;
            try {
                result = repoSystem.resolveArtifact(repoSession, request);
            } catch (ArtifactResolutionException e) {
                getLog().warn("could not resolve " + id, e);
                return null;
            }

            getLog().debug("Resolved artifact " + id + " to " +
                    result.getArtifact().getFile() + " from "
                    + result.getRepository());
            return result.getArtifact().getFile();
        }
    }


}
