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
package org.apache.geronimo.naming.deployment.jsr88;

import java.io.Serializable;

/**
 * Holds the elements that make up an ObjectName.  This class exists
 * so that the bundle of elements can be get, set, and edited together
 * separate from any other elements that may be on the parent.
 *
 * @version $Rev$ $Date$
 */
public class Pattern implements Serializable {
    private String groupId;
    private String artifactId;
    private String version;
    private String module;
    private String name;
    private String type;

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

    public String getModule() {
        return module;
    }

    public void setModule(String module) {
        this.module = module;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }


    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public boolean empty() {
        return (groupId == null || groupId.trim().equals("")) &&
                (artifactId == null || artifactId.trim().equals("")) &&
                (version == null || version.trim().equals("")) &&
                (module == null || module.trim().equals("")) &&
                (name == null || name.trim().equals("")) &&
                (type == null || type.trim().equals(""));
    }

    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        final Pattern pattern = (Pattern) o;

        if (artifactId != null ? !artifactId.equals(pattern.artifactId) : pattern.artifactId != null) return false;
        if (groupId != null ? !groupId.equals(pattern.groupId) : pattern.groupId != null) return false;
        if (module != null ? !module.equals(pattern.module) : pattern.module != null) return false;
        if (name != null ? !name.equals(pattern.name) : pattern.name != null) return false;
        if (type != null ? !type.equals(pattern.type) : pattern.type != null) return false;
        if (version != null ? !version.equals(pattern.version) : pattern.version != null) return false;

        return true;
    }

    public int hashCode() {
        int result;
        result = (groupId != null ? groupId.hashCode() : 0);
        result = 29 * result + (artifactId != null ? artifactId.hashCode() : 0);
        result = 29 * result + (version != null ? version.hashCode() : 0);
        result = 29 * result + (module != null ? module.hashCode() : 0);
        result = 29 * result + (name != null ? name.hashCode() : 0);
        result = 29 * result + (type != null ? type.hashCode() : 0);
        return result;
    }
}
