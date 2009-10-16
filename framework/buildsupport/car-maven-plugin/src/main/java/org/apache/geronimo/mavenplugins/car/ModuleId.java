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

import org.apache.geronimo.system.plugin.model.ArtifactType;
import org.apache.geronimo.kernel.repository.Artifact;

/**
 * @version $Rev$ $Date$
 */
public class ModuleId {
    /**
     * @parameter
     */
    protected String groupId;
    /**
     * @parameter
     */
    protected String artifactId;
    /**
     * @parameter
     */
    protected String version;
    /**
     * @parameter
     */
    protected String type;

    private String importType;

    public String getGroupId() {
        return groupId;
    }

    public String getArtifactId() {
        return artifactId;
    }

    public String getVersion() {
        return version;
    }

    public String getType() {
        return type;
    }


    /**
     * @parameter
     */
    public void setImport(String importType) {
        this.importType = importType;
    }

    public String getImport() {
        return importType;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    public void setArtifactId(String artifactId) {
        this.artifactId = artifactId;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public void setType(String type) {
        this.type = type;
    }

    public ArtifactType toArtifactType() {
        ArtifactType artifact = new ArtifactType();
        artifact.setGroupId(groupId);
        artifact.setArtifactId(artifactId);
        artifact.setVersion(version);
        artifact.setType(type);
        return artifact;
    }

    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ModuleId)) return false;

        ModuleId moduleId = (ModuleId) o;

        if (artifactId != null ? !artifactId.equals(moduleId.artifactId) : moduleId.artifactId != null) return false;
        if (groupId != null ? !groupId.equals(moduleId.groupId) : moduleId.groupId != null) return false;
        if (importType != null ? !importType.equals(moduleId.importType) : moduleId.importType != null) return false;
        if (type != null ? !type.equals(moduleId.type) : moduleId.type != null) return false;
        if (version != null ? !version.equals(moduleId.version) : moduleId.version != null) return false;

        return true;
    }

    public int hashCode() {
        int result;
        result = (groupId != null ? groupId.hashCode() : 0);
        result = 31 * result + (artifactId != null ? artifactId.hashCode() : 0);
        result = 31 * result + (version != null ? version.hashCode() : 0);
        result = 31 * result + (type != null ? type.hashCode() : 0);
        result = 31 * result + (importType != null ? importType.hashCode() : 0);
        return result;
    }
}
