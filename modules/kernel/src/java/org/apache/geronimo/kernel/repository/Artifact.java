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
import java.io.Serializable;

/**
 * @version $Rev:$ $Date:$
 */
public class Artifact implements Comparable, Serializable {
    private static final long serialVersionUID = -3459638899709893444L;
    public static final String DEFAULT_GROUP_ID = "Unspecified";

    private final String groupId;
    private final String artifactId;
    private final Version version;
    private final String type;
    private final boolean resolved;

    public Artifact(String groupId, String artifactId, String version, String type) {
        this(groupId, artifactId, new Version(version), type);
    }

    public Artifact(String groupId, String artifactId, Version version, String type) {
        if (artifactId == null) throw new NullPointerException("artifactId is null");
        this.groupId = groupId;
        this.artifactId = artifactId;
        this.version = version;
        this.type = type;
        this.resolved = groupId != null && artifactId != null && version != null && type != null;
    }

    public static Artifact create(String id) {
        String[] parts = id.split("/");
         if (parts.length != 4) {
             throw new IllegalArgumentException("Invalid id: " + id);
         }
         return new Artifact(parts[0], parts[1], parts[2], parts[3]);
    }

    public String getGroupId() {
        return groupId;
    }

    public String getArtifactId() {
        return artifactId;
    }

    public Version getVersion() {
        return version;
    }

    public String getType() {
        return type;
    }

    public boolean isResolved() {
        return resolved;
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

    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        final Artifact artifact = (Artifact) o;

        if (!artifactId.equals(artifact.artifactId)) {
            return false;
        }

        if (groupId != null ? !groupId.equals(artifact.groupId) : artifact.groupId != null) {
            return false;
        }

        if (type != null ? !type.equals(artifact.type) : artifact.type != null) {
            return false;
        }

        if (version != null ? !version.equals(artifact.version) : artifact.version != null) {
            return false;
        }

        return true;
    }

    public int hashCode() {
        int result;
        result = (groupId != null ? groupId.hashCode() : 0);
        result = 29 * result + artifactId.hashCode();
        result = 29 * result + (version != null ? version.hashCode() : 0);
        result = 29 * result + (type != null ? type.hashCode() : 0);
        return result;
    }

    /**
     * @deprecated for use during conversion only!
     * @return
     * @throws URISyntaxException
     */
    public URI toURI() throws URISyntaxException {
        return new URI(toString());
    }

    public String toString() {
        return groupId + "/" + artifactId + "/" + version + "/" + type;
    }

}
