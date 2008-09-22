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


package org.apache.geronimo.farm.plugin;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.GeneratedValue;

import org.apache.geronimo.system.plugin.model.PluginType;
import org.apache.geronimo.system.plugin.model.PluginArtifactType;
import org.apache.geronimo.system.plugin.model.ArtifactType;
import org.apache.geronimo.kernel.repository.Artifact;

/**
 * @version $Rev$ $Date$
 */
@Entity(name="plugin")
public class JpaPluginInstance {

    @Id
    @GeneratedValue
    private int id;
    private String groupId;
    private String artifactId;
    private String version;
    private String type;

    public JpaPluginInstance() {
    }

    public JpaPluginInstance(String groupId, String artifactId, String version, String type) {
        this.groupId = groupId;
        this.artifactId = artifactId;
        this.version = version;
        this.type = type;
    }

    public JpaPluginInstance(String artifactUri) {
        Artifact artifact = Artifact.create(artifactUri);
        this.groupId = artifact.getGroupId();
        this.artifactId = artifact.getArtifactId();
        this.version = artifact.getVersion().toString();
        this.type = artifact.getType();
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

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public PluginType getPlugin() {
        PluginType plugin = new PluginType();
        PluginArtifactType pluginArtifact = new PluginArtifactType();
        ArtifactType artifact = new ArtifactType();
        artifact.setGroupId(groupId);
        artifact.setArtifactId(artifactId);
        artifact.setVersion(version);
        artifact.setType(type);
        pluginArtifact.setModuleId(artifact);
        plugin.getPluginArtifact().add(pluginArtifact);
        return plugin;
    }

    public String toString() {
        return groupId + "/" + artifactId + "/" + version + "/" + type;
    }

    public Artifact toArtifact() {
        return new Artifact(groupId, artifactId, version, type);
    }
}
