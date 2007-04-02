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
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.Iterator;

import org.apache.geronimo.system.repository.Maven2Repository;
import org.apache.geronimo.kernel.repository.Artifact;
import org.apache.geronimo.kernel.repository.Version;
import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.gbean.GBeanInfoBuilder;

import org.codehaus.mojo.pluginsupport.dependency.DependencyTree;

/**
 * Helps adapt Geronimo repositories to Maven repositories for packaging building.
 *
 * @version $Rev$ $Date$
 */
public class Maven2RepositoryAdapter
    extends Maven2Repository
{
    private ArtifactLookup lookup;

    private DependencyTree dependencyTree;

    public Maven2RepositoryAdapter(DependencyTree dependencyTree, final ArtifactLookup lookup) {
        super(lookup.getBasedir());
        this.dependencyTree = dependencyTree;
        this.lookup = lookup;
    }

    public File getLocation(final Artifact artifact) {
        assert artifact != null;

        return lookup.getLocation(artifact);
    }

    public SortedSet list() {
        TreeSet list = new TreeSet();
        listInternal(list, dependencyTree.getRootNode(), null, null, null, null);
        return list;
    }

    public SortedSet list(Artifact query) {
        TreeSet list = new TreeSet();
        listInternal(list, dependencyTree.getRootNode(), query.getGroupId(), query.getArtifactId(), query.getVersion(), query.getType());
        return list;
    }

    private void listInternal(TreeSet list, DependencyTree.Node node, String groupId, String artifactId, Version version, String type) {
        if (matches(node.getArtifact(), groupId, artifactId, version, type)) {
            list.add(mavenToGeronimoArtifact(node.getArtifact()));
        }
        for (Iterator iterator = node.getChildren().iterator(); iterator.hasNext();) {
            DependencyTree.Node childNode = (DependencyTree.Node) iterator.next();
            listInternal(list, childNode, groupId, artifactId, version, type);
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

    //
    // ArtifactLookup
    //

    public static interface ArtifactLookup
    {
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
        infoFactory.addAttribute("dependencies", DependencyTree.class, true);
        infoFactory.setConstructor(new String[]{"dependencies", "lookup" });
        GBEAN_INFO = infoFactory.getBeanInfo();
    }

    public static GBeanInfo getGBeanInfo() {
        return GBEAN_INFO;
    }
}
