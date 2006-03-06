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

package org.apache.geronimo.gbean;

import org.apache.geronimo.kernel.repository.Artifact;

import java.io.Serializable;
import java.util.Map;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.Collections;

/**
 * @version $Rev:$ $Date:$
 */
public class AbstractNameQuery implements Serializable {

    private final List artifacts;
    private final Map name;
    private final Set interfaceTypes;

    public AbstractNameQuery(AbstractName abstractName) {
        this.artifacts = Collections.singletonList(abstractName.getArtifact());
        this.name = abstractName.getName();
        this.interfaceTypes = abstractName.getInterfaceTypes();
    }

    public AbstractNameQuery(Artifact artifact, Map name, String interfaceType) {
        this.artifacts = Collections.singletonList(artifact);
        this.name = name;
        this.interfaceTypes = Collections.singleton(interfaceType);
    }

    public AbstractNameQuery(String interfaceType) {
        this.artifacts = Collections.EMPTY_LIST;
        this.name = Collections.EMPTY_MAP;
        this.interfaceTypes = Collections.singleton(interfaceType);
    }

    public AbstractNameQuery(List artifacts, Map name, Set interfaceTypes) {
        this.artifacts = artifacts;
        this.name = name;
        this.interfaceTypes = interfaceTypes;
    }

    public AbstractNameQuery(Artifact artifact, Map name, Set interfaceTypes) {
        this.artifacts = Collections.singletonList(artifact);
        this.name = name;
        this.interfaceTypes = interfaceTypes;
    }

    public List getArtifacts() {
        return artifacts;
    }

    public Map getName() {
        return name;
    }

    public Set getInterfaceTypes() {
        return interfaceTypes;
    }

    public String toString() {
        StringBuffer buf = new StringBuffer();
        String separator = "";
        for (Iterator iterator = artifacts.iterator(); iterator.hasNext();) {
            Artifact artifact = (Artifact) iterator.next();
            buf.append(separator).append("artifact=").append(artifact);
            separator = ",";
        }
        for (Iterator iterator = interfaceTypes.iterator(); iterator.hasNext();) {
            String interfaceType = (String) iterator.next();
            buf.append(",interface=").append(interfaceType);
        }
        for (Iterator iterator = name.entrySet().iterator(); iterator.hasNext();) {
            Map.Entry entry = (Map.Entry) iterator.next();
            buf.append(",").append(entry.getKey()).append("=").append(entry.getValue());
        }
        return buf.toString();
    }

    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        final AbstractNameQuery that = (AbstractNameQuery) o;

        if (artifacts != null ? !artifacts.equals(that.artifacts) : that.artifacts != null) return false;
        if (interfaceTypes != null ? !interfaceTypes.equals(that.interfaceTypes) : that.interfaceTypes != null)
            return false;
        return !(name != null ? !name.equals(that.name) : that.name != null);

    }

    public int hashCode() {
        int result;
        result = (artifacts != null ? artifacts.hashCode() : 0);
        result = 29 * result + (name != null ? name.hashCode() : 0);
        result = 29 * result + (interfaceTypes != null ? interfaceTypes.hashCode() : 0);
        return result;
    }

    /**
     * determine if the supplied info is more specific and matches our patterns.  In this method
     * "this" is the query, the info parameter is the test specific info.
     * @param info
     * @return if the specific info supplied matches our patterns.
     */
    public boolean matches(AbstractNameQuery info) {
        List artifacts = info.getArtifacts();
        if (artifacts.size() != 1) {
            throw new IllegalArgumentException("source info must have only one artifact");
        }
        if (!info.getName().entrySet().containsAll(name.entrySet())) {
            return false;
        }
        if (!info.getInterfaceTypes().containsAll(interfaceTypes)) {
            return false;
        }
        if (getArtifacts().isEmpty()) {
            return true;
        }
        Artifact otherArtifact = (Artifact) artifacts.iterator().next();
        for (Iterator iterator = getArtifacts().iterator(); iterator.hasNext();) {
            Artifact artifact = (Artifact) iterator.next();
            if (artifact.matches(otherArtifact)) {
                return true;
            }
        }
        return false;
    }

    public boolean matches(AbstractName info) {
        if (!info.getName().entrySet().containsAll(name.entrySet())) {
            return false;
        }
        if (!info.getInterfaceTypes().containsAll(interfaceTypes)) {
            return false;
        }
        if (getArtifacts().isEmpty()) {
            return true;
        }
        Artifact otherArtifact = info.getArtifact();
        for (Iterator iterator = getArtifacts().iterator(); iterator.hasNext();) {
            Artifact artifact = (Artifact) iterator.next();
            if (artifact.matches(otherArtifact)) {
                return true;
            }
        }
        return false;
    }
}
