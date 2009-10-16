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
import java.io.IOException;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import javax.security.auth.login.FailedLoginException;

import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.gbean.GBeanInfoBuilder;
import org.apache.geronimo.kernel.repository.Artifact;
import org.apache.geronimo.kernel.repository.FileWriteMonitor;
import org.apache.geronimo.kernel.repository.Version;
import org.apache.geronimo.system.plugin.LocalOpenResult;
import org.apache.geronimo.system.plugin.OpenResult;
import org.apache.geronimo.system.plugin.SourceRepository;
import org.apache.geronimo.system.plugin.model.PluginListType;
import org.apache.geronimo.system.repository.Maven2Repository;

/**
 * Helps adapt Geronimo repositories to Maven repositories for packaging building.
 *
 * @version $Rev$ $Date$
 */
public class Maven2RepositoryAdapter extends Maven2Repository implements SourceRepository {
    private ArtifactLookup lookup;

    private Set<org.apache.maven.artifact.Artifact> dependencyTree;

    public Maven2RepositoryAdapter(Set<org.apache.maven.artifact.Artifact> dependencyTree, final ArtifactLookup lookup) {
        super(lookup.getBasedir());
        this.dependencyTree = dependencyTree;
        this.lookup = lookup;
    }

    public File getLocation(final Artifact artifact) {
        assert artifact != null;

        return lookup.getLocation(artifact);
    }

    public SortedSet list() {
        TreeSet<Artifact> list = new TreeSet<Artifact>();
        listInternal(list, null, null, null, null);
        return list;
    }

    public SortedSet list(Artifact query) {
        TreeSet<Artifact> list = new TreeSet<Artifact>();
        listInternal(list, query.getGroupId(), query.getArtifactId(), query.getVersion(), query.getType());
        return list;
    }

    private void listInternal(TreeSet<Artifact> list, String groupId, String artifactId, Version version, String type) {
        for (org.apache.maven.artifact.Artifact artifact: dependencyTree) {
            if (matches(artifact, groupId, artifactId, version, type)) {
                list.add(mavenToGeronimoArtifact(artifact));
            }
        }
    }

    private boolean matches(org.apache.maven.artifact.Artifact artifact, String groupId, String artifactId, Version version, String type) {
        return (groupId == null || artifact.getGroupId().equals(groupId))
                && (artifactId == null || artifact.getArtifactId().equals(artifactId))
                && (version == null || artifact.getVersion().equals(version.toString()))
                && (type == null || artifact.getType().equals(type));
    }

    protected org.apache.geronimo.kernel.repository.Artifact mavenToGeronimoArtifact(final org.apache.maven.artifact.Artifact artifact) {
        assert artifact != null;

        return new org.apache.geronimo.kernel.repository.Artifact(artifact.getGroupId(), artifact.getArtifactId(), artifact.getVersion(), artifact.getType());
    }

    public PluginListType getPluginList() {
        throw new RuntimeException("Not implemented");
    }

    public OpenResult open(Artifact artifact, FileWriteMonitor fileWriteMonitor) throws IOException, FailedLoginException {
        if (!artifact.isResolved()) {
            SortedSet<Artifact> list = list(artifact);
            if (list.isEmpty()) {
                throw new IOException("Could not resolve artifact " + artifact + " in repo " + rootFile);
            }
            artifact = list.first();
        }
        File location = getLocation(artifact);
        return new LocalOpenResult(artifact, location);

    }

    //
    // ArtifactLookup
    //

    public static interface ArtifactLookup {
        File getLocation(Artifact artifact);

        File getBasedir();
    }

    //
    // GBean
    //

    public static final GBeanInfo GBEAN_INFO;

    static {
        GBeanInfoBuilder infoFactory = GBeanInfoBuilder.createStatic(Maven2RepositoryAdapter.class, "Repository");
        infoFactory.addAttribute("lookup", ArtifactLookup.class, true);
        infoFactory.addAttribute("dependencies", Set.class, true);
        infoFactory.setConstructor(new String[]{"dependencies", "lookup"});
        GBEAN_INFO = infoFactory.getBeanInfo();
    }

    public static GBeanInfo getGBeanInfo() {
        return GBEAN_INFO;
    }
}
