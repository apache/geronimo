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

import java.io.Serializable;
import java.util.Collections;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.apache.geronimo.kernel.repository.Artifact;

/**
 * @version $Rev:$ $Date:$
 */
public class AbstractNameQuery implements Serializable {
    private static final long serialVersionUID = 7444620122607155678L;

    private final Artifact artifact;
    private final Map name;
    private final Set interfaceTypes;

    public AbstractNameQuery(AbstractName abstractName) {
        this.artifact = abstractName.getArtifact();
        this.name = abstractName.getName();
        this.interfaceTypes = abstractName.getInterfaceTypes();
    }

    public AbstractNameQuery(Artifact artifact, Map name, String interfaceType) {
        this.artifact = artifact;
        this.name = name;
        if (interfaceType != null) {
            this.interfaceTypes = Collections.singleton(interfaceType);
        } else {
            this.interfaceTypes = Collections.EMPTY_SET;
        }
    }

    public AbstractNameQuery(String interfaceType) {
        this.artifact = null;
        this.name = Collections.EMPTY_MAP;
        this.interfaceTypes = Collections.singleton(interfaceType);
    }

    public AbstractNameQuery(Artifact artifact, Map name, Set interfaceTypes) {
        this.artifact = artifact;
        this.name = name;
        if (interfaceTypes == null) interfaceTypes = Collections.EMPTY_SET;
        this.interfaceTypes = interfaceTypes;
    }

    public Artifact getArtifact() {
        return artifact;
    }

    public Map getName() {
        return name;
    }

    public Set getInterfaceTypes() {
        return interfaceTypes;
    }

    public String toString() {
        StringBuffer buf = new StringBuffer("artifact=");
        buf.append(artifact);
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

        if (artifact != null ? !artifact.equals(that.artifact) : that.artifact != null) return false;
        if (interfaceTypes != null ? !interfaceTypes.equals(that.interfaceTypes) : that.interfaceTypes != null)
            return false;
        return !(name != null ? !name.equals(that.name) : that.name != null);

    }

    public int hashCode() {
        int result;
        result = (artifact != null ? artifact.hashCode() : 0);
        result = 29 * result + (name != null ? name.hashCode() : 0);
        result = 29 * result + (interfaceTypes != null ? interfaceTypes.hashCode() : 0);
        return result;
    }


    public boolean matches(AbstractName info) {
        if (!info.getName().entrySet().containsAll(name.entrySet())) {
            return false;
        }
        if (!info.getInterfaceTypes().containsAll(interfaceTypes)) {
            return false;
        }
        if (artifact == null) {
            return true;
        }
        Artifact otherArtifact = info.getArtifact();
        return artifact.matches(otherArtifact);
    }
}
