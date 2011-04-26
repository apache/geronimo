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
import java.util.Collections;
import java.util.List;

import org.apache.karaf.features.FeaturesService;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleException;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.launch.Framework;
import org.sonatype.aether.RepositorySystem;
import org.sonatype.aether.RepositorySystemSession;
import org.sonatype.aether.repository.RemoteRepository;
import org.sonatype.aether.resolution.ArtifactRequest;
import org.sonatype.aether.resolution.ArtifactResolutionException;
import org.sonatype.aether.resolution.ArtifactResult;
import org.sonatype.aether.util.artifact.DefaultArtifact;

/**
 * Base for karaf-aware mojos
 *
 * @goal karaf-framework
 * @requiresDependencyResolution runtime
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

    private Framework framework;


    @java.lang.Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        initializeFramework();
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {

        }
        ServiceReference sr = framework.getBundleContext().getServiceReference(FeaturesService.class.getName());
        Object o = framework.getBundleContext().getService(sr);
        if (o == null) {
            throw new RuntimeException("no feature service found");
        }
        try {
            framework.stop();
        } catch (BundleException e) {
            throw new MojoExecutionException("Can't stop framework: ", e );
        }
    }

    void initializeFramework() throws MojoFailureException {
        FrameworkHelper helper = new FrameworkHelper(karafHome, new AetherResolver(), Collections.<Artifact>emptyList());
        try {
            framework = helper.start();
        } catch (Exception e) {
            throw new MojoFailureException("Could not start karaf framework", e);
        }
    }

    protected void listBundles() {
        StringBuilder b = new StringBuilder("Bundles:");
        for (Bundle bundle: framework.getBundleContext().getBundles()) {
            b.append("\n   Id:").append(bundle.getBundleId()).append("  status:").append(bundle.getState()).append("  ").append(bundle.getLocation());
        }
        getLog().info(b.toString());
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
