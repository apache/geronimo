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

import javax.management.ObjectName;
import java.io.Serializable;
import java.util.Collections;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 * @version $Rev:$ $Date:$
 */
public class AbstractName implements Serializable {

    private final Artifact artifact;
    private final Map name;
    private final Set interfaceTypes;

    private final ObjectName objectName;

    public AbstractName(Artifact artifact, Map name, String interfaceType, ObjectName objectName) {
        this.artifact = artifact;
        this.name = name;
        this.interfaceTypes = Collections.singleton(interfaceType);
        this.objectName = objectName;
    }

    public AbstractName(Artifact artifact, Map name, Set interfaceTypes, ObjectName objectName) {
        this.artifact = artifact;
        this.name = name;
        this.interfaceTypes = interfaceTypes;
        this.objectName = objectName;
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

    public ObjectName getObjectName() {
        return objectName;
    }

    public String toString() {
        StringBuffer buf = new StringBuffer("AbstractName");
        for (Iterator iterator = name.entrySet().iterator(); iterator.hasNext();) {
            Map.Entry entry = (Map.Entry) iterator.next();
            buf.append(",").append(entry.getKey()).append("=").append(entry.getValue());
        }
        buf.append(",artifact=").append(artifact);
        for (Iterator iterator = interfaceTypes.iterator(); iterator.hasNext();) {
            String interfaceType = (String) iterator.next();
            buf.append(",interface=").append(interfaceType);
        }
        return buf.toString();
    }

    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        final AbstractName that = (AbstractName) o;

        if (artifact != null ? !artifact.equals(that.artifact) : that.artifact != null) return false;
        if (name != null ? !name.equals(that.name) : that.name != null) return false;

        return true;
    }

    public int hashCode() {
        int result;
        result = (artifact != null ? artifact.hashCode() : 0);
        result = 29 * result + (name != null ? name.hashCode() : 0);
        return result;
    }

}
