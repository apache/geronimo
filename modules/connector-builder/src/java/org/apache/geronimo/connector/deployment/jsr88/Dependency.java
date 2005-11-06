/**
 *
 * Copyright 2003-2004 The Apache Software Foundation
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
package org.apache.geronimo.connector.deployment.jsr88;

import org.apache.geronimo.deployment.plugin.XmlBeanSupport;
import org.apache.geronimo.deployment.xbeans.DependencyType;
import org.apache.xmlbeans.SchemaTypeLoader;
import org.apache.xmlbeans.XmlBeans;

/**
 * Represents /connector/dependency in the Geronimo Connector deployment plan
 *
 * //todo: move to service-builder module
 *
 * @version $Rev: 46019 $ $Date: 2004-09-14 05:56:06 -0400 (Tue, 14 Sep 2004) $
 */
public class Dependency extends XmlBeanSupport {
    static final SchemaTypeLoader SCHEMA_TYPE_LOADER = XmlBeans.typeLoaderForClassLoader(DependencyType.class.getClassLoader());

    public Dependency() {
        super(null);
    }

    public Dependency(DependencyType dependency) {
        super(null);
        configure(dependency);
    }

    protected DependencyType getDependency() {
        return (DependencyType) getXmlObject();
    }

    void configure(DependencyType dependency) {
        setXmlObject(dependency);
        //todo: read in data from dependency object?
    }

    // ----------------------- JavaBean Properties for /connector/dependency ----------------------

    public String getGroupId() {
        return getDependency().getGroupId();
    }

    public void setGroupId(String groupId) {
        String old = getGroupId();
        if(groupId == null) {
            getDependency().unsetGroupId();
        } else {
            getDependency().setGroupId(groupId);
        }
        pcs.firePropertyChange("groupId", old, groupId);
    }

    public String getArtifactId() {
        return getDependency().getArtifactId();
    }

    public void setArtifactId(String artifact) {
        String old = getArtifactId();
        if(artifact == null) {
            getDependency().unsetArtifactId();
        } else {
            getDependency().setArtifactId(artifact);
        }
        pcs.firePropertyChange("artifactId", old, artifact);
    }

    public String getType() {
        return getDependency().getType();
    }

    public void setType(String type) {
        String old = type;
        if(type == null) {
            getDependency().unsetType();
        } else {
            getDependency().setType(type);
        }
        pcs.firePropertyChange("type", old, type);
    }

    public String getVersion() {
        return getDependency().getVersion();
    }

    public void setVersion(String version) {
        String old = getVersion();
        if(version == null) {
            getDependency().unsetVersion();
        } else {
            getDependency().setVersion(version);
        }
        pcs.firePropertyChange("version", old, version);
    }

    public String getURI() {
        return getDependency().getUri();
    }

    public void setURI(String uri) {
        String old = getURI();
        if(uri == null) {
            getDependency().unsetUri();
        } else {
            getDependency().setUri(uri);
        }
        pcs.firePropertyChange("URI", old, uri);
    }

    // ----------------------- End of JavaBean Properties ----------------------

    protected SchemaTypeLoader getSchemaTypeLoader() {
        return SCHEMA_TYPE_LOADER;
    }
}
