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


package org.apache.geronimo.client.builder;

import java.io.IOException;
import java.util.Collection;
import java.util.LinkedHashSet;

import org.apache.geronimo.kernel.repository.ArtifactManager;
import org.apache.geronimo.kernel.repository.Repository;
import org.apache.geronimo.kernel.repository.ListableRepository;
import org.apache.geronimo.kernel.repository.ArtifactResolver;
import org.apache.geronimo.kernel.repository.Artifact;
import org.apache.geronimo.kernel.repository.MissingDependencyException;
import org.apache.geronimo.kernel.repository.MultipleMatchesException;
import org.apache.geronimo.system.resolver.ExplicitDefaultArtifactResolver;
import org.apache.geronimo.system.serverinfo.ServerInfo;
import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.gbean.GBeanInfoBuilder;

/**
 * This class is intended to get around some problems using the normal ExplicitDefaultArtifactResolver for client building.
 * We really want it to refer to the server's client_artifact_aliases.properties file, but that isn't available when run
 * from the car-maven-plugin.  Also the ServerInfo is missing and the ArtifactManager is not in a parent configuration
 * (the car-maven-plugin starts a configuration that isn't a parent of anything.  We might be able to fix that by a
 * use of artifact_aliases.properties itself, but that might be for later).
 *
 * @version $Rev$ $Date$
 */
public class OptionalExplicitDefaultArtifactResolver implements ArtifactResolver {

    private final ArtifactResolver delegate;

    public OptionalExplicitDefaultArtifactResolver(String versionMapLocation, Collection<ArtifactManager> artifactManagers, Collection<ListableRepository> repositories, Collection<ServerInfo> serverInfos, Collection<ArtifactResolver> fallbackResolver) throws IOException {
        ServerInfo serverInfo = getServerInfo(serverInfos);
        if (serverInfo != null) {
            delegate = new ExplicitDefaultArtifactResolver(versionMapLocation, getArtifactManager(artifactManagers), repositories, serverInfo);
        } else {
            if (fallbackResolver == null || fallbackResolver.isEmpty()) {
                throw new IllegalStateException("No ServerInfo and no delegate ArtifactResolver supplied");
            }
            delegate = fallbackResolver.iterator().next();
        }
    }

    private static ServerInfo getServerInfo(Collection<ServerInfo> serverInfo) {
        if (serverInfo == null || serverInfo.isEmpty()) {
            return null;
        } else {
            return serverInfo.iterator().next();
        }
    }

    private static ArtifactManager getArtifactManager(Collection<ArtifactManager> artifactManagers) {
        if (artifactManagers == null || artifactManagers.isEmpty()) {
            throw new IllegalStateException("No ArtifactManager found");
        }
        return artifactManagers.iterator().next();
    }

    public Artifact generateArtifact(Artifact source, String defaultType) {
        return delegate.generateArtifact(source, defaultType);
    }

    public Artifact resolveInClassLoader(Artifact source) throws MissingDependencyException {
        return delegate.resolveInClassLoader(source);
    }

    public Artifact resolveInClassLoader(Artifact source, Collection parentConfigurations) throws MissingDependencyException {
        return delegate.resolveInClassLoader(source, parentConfigurations);
    }

    public LinkedHashSet resolveInClassLoader(Collection artifacts) throws MissingDependencyException {
        return delegate.resolveInClassLoader(artifacts);
    }

    public LinkedHashSet resolveInClassLoader(Collection artifacts, Collection parentConfigurations) throws MissingDependencyException {
        return delegate.resolveInClassLoader(artifacts, parentConfigurations);
    }

    public Artifact queryArtifact(Artifact artifact) throws MultipleMatchesException {
        return delegate.queryArtifact(artifact);
    }

    public Artifact[] queryArtifacts(Artifact artifact) {
        return delegate.queryArtifacts(artifact);
    }

    public static final GBeanInfo GBEAN_INFO;

    static {
        GBeanInfoBuilder infoFactory = GBeanInfoBuilder.createStatic(OptionalExplicitDefaultArtifactResolver.class, "ArtifactResolver");
        infoFactory.addAttribute("versionMapLocation", String.class, true, true);
        infoFactory.addReference("ArtifactManager", ArtifactManager.class, "ArtifactManager");
        infoFactory.addReference("Repositories", ListableRepository.class, "Repository");
        infoFactory.addReference("ServerInfo", ServerInfo.class, "GBean");
        infoFactory.addReference("FallbackArtifactResolver", ArtifactResolver.class, "ArtifactResolver");
        infoFactory.addInterface(ArtifactResolver.class);

        infoFactory.setConstructor(new String[]{
                "versionMapLocation",
                "ArtifactManager",
                "Repositories",
                "ServerInfo",
                "FallbackArtifactResolver"
        });


        GBEAN_INFO = infoFactory.getBeanInfo();

    }

    public static GBeanInfo getGBeanInfo() {
        return GBEAN_INFO;
    }

}
