/**
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
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

import java.io.Serializable;

/**
 * @version $Rev$ $Date$
 */
public class Artifact implements Comparable, Serializable {
    private static final long serialVersionUID = -3459638899709893444L;
    public static final String DEFAULT_GROUP_ID = "default";

    private final String groupId;
    private final String artifactId;
    private final Version version;
    private final String type;

    public Artifact(String groupId, String artifactId, String version, String type) {
        this(groupId, artifactId, version == null ? null : new Version(version), type, true);
    }

    public Artifact(String groupId, String artifactId, Version version, String type) {
        this(groupId, artifactId, version, type, true);
    }

    private Artifact(String groupId, String artifactId, Version version, String type, boolean requireArtifactId) {
        if (requireArtifactId && artifactId == null) throw new NullPointerException("artifactId is null: groupId: " + groupId + ", version: " + version + ", type: " + type);
        this.groupId = groupId;
        this.artifactId = artifactId;
        this.version = version;
        this.type = type;
    }

    public static Artifact create(String id) {
        return create(id, true);
    }

    public static Artifact createPartial(String id) {
        return create(id, false);
    }

    private static Artifact create(String id, boolean requireArtifactId) {
        String[] parts = id.split("/", -1);
        if (parts.length != 4) {
            throw new IllegalArgumentException("id must be in the form [groupId]/[artifactId]/[version]/[type] : " + id);
        }
        for (int i = 0; i < parts.length; i++) {
            if (parts[i].equals("")) {
                parts[i] = null;
            }
        }
        return new Artifact(parts[0], parts[1], parts[2] == null ? null : new Version(parts[2]), parts[3], requireArtifactId);
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
        return groupId != null && artifactId != null && version != null && type != null;
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

    private static <T extends Comparable<T>> int safeCompare(T left, T right) {
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

        if (artifactId != null? !artifactId.equals(artifact.artifactId) : artifact.artifactId != null) {
            return false;
        }

        if (groupId != null ? !groupId.equals(artifact.groupId) : artifact.groupId != null) {
            return false;
        }

        if (type != null ? !type.equals(artifact.type) : artifact.type != null) {
            return false;
        }

        return !(version != null ? !version.equals(artifact.version) : artifact.version != null);

    }

    public int hashCode() {
        int result;
        result = (groupId != null ? groupId.hashCode() : 0);
        result = 29 * result + (artifactId != null? artifactId.hashCode() : 0);
        result = 29 * result + (version != null ? version.hashCode() : 0);
        result = 29 * result + (type != null ? type.hashCode() : 0);
        return result;
    }

    public String toString() {
        StringBuilder buffer = new StringBuilder();

        if (groupId != null) {
            buffer.append(groupId);
        }
        buffer.append("/");

        if (artifactId != null) {
            buffer.append(artifactId);
        }
        buffer.append("/");

        if (version != null) {
            buffer.append(version);
        }
        buffer.append("/");

        if (type != null) {
            buffer.append(type);
        }
        return buffer.toString();
    }

    /**
     * see if this artifact matches the other artifact (which is more specific than this one)
     *
     * @param otherArtifact the more specific artifact we are comparing with
     * @return whether the other artifact is consistent with everything specified in this artifact.
     */
    public boolean matches(Artifact otherArtifact) {
        if (groupId != null && !groupId.equals(otherArtifact.groupId)) {
            return false;
        }
        if (artifactId != null && !artifactId.equals(otherArtifact.artifactId)) {
            return false;
        }
        if (version != null && !version.equals(otherArtifact.version)) {
            return false;
        }
        return (type == null || type.equals(otherArtifact.type));
    }
}
