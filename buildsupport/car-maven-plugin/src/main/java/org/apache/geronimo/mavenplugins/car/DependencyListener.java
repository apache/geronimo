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

import java.util.Stack;

import org.apache.maven.artifact.resolver.ResolutionListener;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.DefaultArtifact;
import org.apache.maven.artifact.handler.ArtifactHandler;
import org.apache.maven.artifact.handler.DefaultArtifactHandler;
import org.apache.maven.artifact.versioning.VersionRange;

/**
 * @version $Rev:$ $Date:$
 */
public class DependencyListener implements ResolutionListener {
    private static final VersionRange RANGE = VersionRange.createFromVersion("");
    private static final ArtifactHandler artifactHandler = new DefaultArtifactHandler();
    private ProjectNode top;
    private final Stack<ProjectNode> parents = new Stack<ProjectNode>();

    public void testArtifact(Artifact artifact) {
        if (!parents.isEmpty()) {
            ProjectNode parent = parents.peek();
            Artifact shrunk = shrink(artifact);
            ProjectNode child = new ProjectNode(shrunk);
            parent.addChild(child);
        }
        //else??
    }

    public void startProcessChildren(Artifact artifact) {
        Artifact shrunk = shrink(artifact);
        if (top == null) {
            top = new ProjectNode(shrunk);
            parents.push(top);
        } else {
            ProjectNode node = parents.peek();
            for (ProjectNode child: node.getChildNodes()) {
                if (shrunk.equals(child.getArtifact())) {
                    parents.push(child);
                    break;
                }
            }
        }
    }

    public void endProcessChildren(Artifact artifact) {
        ProjectNode node = parents.pop();
        if (!shrink(artifact).equals(node.getArtifact())) {
            throw new IllegalStateException("Unexpected parent, expected: " + artifact + " got " + node.getArtifact());
        }
    }

    public void includeArtifact(Artifact artifact) {
    }

    public void omitForNearer(Artifact artifact, Artifact artifact1) {
    }

    public void updateScope(Artifact artifact, String s) {
    }

    public void manageArtifact(Artifact artifact, Artifact artifact1) {
    }

    public void omitForCycle(Artifact artifact) {
    }

    public void updateScopeCurrentPom(Artifact artifact, String s) {
    }

    public void selectVersionFromRange(Artifact artifact) {
    }

    public void restrictRange(Artifact artifact, Artifact artifact1, VersionRange versionRange) {
    }

    static Artifact shrink(Artifact artifact) {
        Artifact shrunk = new DefaultArtifact(artifact.getGroupId(), artifact.getArtifactId(), RANGE, artifact.getScope(), artifact.getType(), artifact.getClassifier(), artifactHandler);
        return shrunk;
    }

    public ProjectNode getTop() {
        return top;
    }
}
