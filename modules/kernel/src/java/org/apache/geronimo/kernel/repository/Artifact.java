/**
 *
 * Copyright 2006 The Apache Software Foundation
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

package org.apache.geronimo.kernel.repository;

import java.net.URI;
import java.net.URISyntaxException;

/**
 * @version $Rev:$ $Date:$
 */
public class Artifact implements Comparable {
    private String groupId;
    private String artifactId;
    private Version version;
    private String type;
    private boolean resolved;

    public Artifact() {
    }

    public Artifact(String groupId, String artifactId, String version, String type, boolean resolved) {
        this.groupId = groupId;
        this.artifactId = artifactId;
        this.version = new Version(version);
        this.type = type;
        this.resolved = resolved;
    }

    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    public String getArtifactId() {
        return artifactId;
    }

    public void setArtifactId(String artifactId) {
        this.artifactId = artifactId;
    }

    public Version getVersion() {
        return version;
    }

    public void setVersion(Version version) {
        this.version = version;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public boolean isResolved() {
        return resolved;
    }

    public void setResolved(boolean resolved) {
        this.resolved = resolved;
    }

    public int compareTo(Object object) {
        Artifact artifact = (Artifact) object;

        int i = safeCompare(groupId, artifact.groupId);
        if (i != 0) return i;

        i = safeCompare(artifactId, artifact.artifactId);
        if (i != 0) return i;

        i = safeCompare(version, artifact.version);
        if (i != 0) return i;

        i = safeCompare(type, artifact.type);
        return i;
    }

    private static int GREATER = 1;
    private static int LESS = -1;
    private static int safeCompare(Comparable left, Comparable right) {
        if (left == null) {
            if (right != null) return LESS;
            return 0;
        }
        if (right == null) return GREATER;
        return left.compareTo(right);
    }

    /**
     * @deprecated for use during conversion only!
     * @return
     * @throws URISyntaxException
     */
    public URI toURI() throws URISyntaxException {
        String id = groupId + "/" + artifactId + "/" + version + "/" + type;
        return new URI(id);
    }

    public String toString() {
        String id = groupId + "/" + artifactId + "/" + version + "/" + type;
        return id;
    }
}
