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
package org.apache.geronimo.deployment.service.jsr88;

import org.apache.geronimo.deployment.dconfigbean.XmlBeanSupport;
import org.apache.geronimo.deployment.xbeans.ArtifactType;
import org.apache.xmlbeans.SchemaTypeLoader;
import org.apache.xmlbeans.XmlBeans;

/**
 * Represents an artifactType (e.g. a dependency or configId element) in a
 * Geronimo deployment plan.
 *
 * @version $Rev$ $Date$
 */
public class Artifact extends XmlBeanSupport {
    static final SchemaTypeLoader SCHEMA_TYPE_LOADER = XmlBeans.typeLoaderForClassLoader(ArtifactType.class.getClassLoader());

    public Artifact() {
        super(null);
    }

    public Artifact(ArtifactType dependency) {
        super(null);
        configure(dependency);
    }

    protected ArtifactType getArtifactType() {
        return (ArtifactType) getXmlObject();
    }

    void configure(ArtifactType dependency) {
        setXmlObject(dependency);
    }

    // ----------------------- JavaBean Properties for artifactType ----------------------

    public String getGroupId() {
        return getArtifactType().getGroupId();
    }

    public void setGroupId(String groupId) {
        String old = getGroupId();
        if(groupId == null) {
            getArtifactType().unsetGroupId();
        } else {
            getArtifactType().setGroupId(groupId);
        }
        pcs.firePropertyChange("groupId", old, groupId);
    }

    public String getArtifactId() {
        return getArtifactType().getArtifactId();
    }

    public void setArtifactId(String artifact) {
        String old = getArtifactId();
        getArtifactType().setArtifactId(artifact);
        pcs.firePropertyChange("artifactId", old, artifact);
    }

    public String getType() {
        return getArtifactType().getType();
    }

    public void setType(String type) {
        String old = getArtifactType().getType();
        if(type == null) {
            getArtifactType().unsetType();
        } else {
            getArtifactType().setType(type);
        }
        pcs.firePropertyChange("type", old, type);
    }

    public String getVersion() {
        return getArtifactType().getVersion();
    }

    public void setVersion(String version) {
        String old = getVersion();
        if(version == null) {
            getArtifactType().unsetVersion();
        } else {
            getArtifactType().setVersion(version);
        }
        pcs.firePropertyChange("version", old, version);
    }

    // ----------------------- End of JavaBean Properties ----------------------

    protected SchemaTypeLoader getSchemaTypeLoader() {
        return SCHEMA_TYPE_LOADER;
    }
}
