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

package org.apache.geronimo.gbean;

import java.io.Serializable;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import org.apache.geronimo.kernel.repository.Artifact;

/**
 * @version $Rev$ $Date$
 */
public class AbstractNameQuery implements Serializable {
    private static final long serialVersionUID = 7444620122607155678L;

    private final Artifact artifact;
    private final Map name;
    private final Set interfaceTypes;

    private final URI uri;

    public AbstractNameQuery(AbstractName abstractName) {
        this(abstractName, null);
    }

    public AbstractNameQuery(AbstractName abstractName, Set interfaceTypes) {
        this.artifact = abstractName.getArtifact();
        this.name = abstractName.getName();
        this.interfaceTypes = interfaceTypes == null ? Collections.EMPTY_SET : interfaceTypes;
        this.uri = createURI(artifact, name, this.interfaceTypes);
    }

    public AbstractNameQuery(Artifact artifact, Map name) {
        this.artifact = artifact;
        this.name = name;
        this.interfaceTypes = Collections.EMPTY_SET;
        this.uri = createURI(artifact, name, interfaceTypes);
    }

    public AbstractNameQuery(Artifact artifact, Map name, String interfaceType) {
        this.artifact = artifact;
        this.name = name;
        if (interfaceType != null) {
            this.interfaceTypes = Collections.singleton(interfaceType);
        } else {
            this.interfaceTypes = Collections.EMPTY_SET;
        }
        this.uri = createURI(artifact, name, interfaceTypes);
    }

    public AbstractNameQuery(String interfaceType) {
        this.artifact = null;
        this.name = Collections.EMPTY_MAP;
        this.interfaceTypes = Collections.singleton(interfaceType);
        this.uri = createURI(artifact, name, interfaceTypes);
    }

    public AbstractNameQuery(Artifact artifact, Map name, Set interfaceTypes) {
        this.artifact = artifact;
        this.name = name;
        if (interfaceTypes == null) interfaceTypes = Collections.EMPTY_SET;
        this.interfaceTypes = interfaceTypes;
        this.uri = createURI(artifact, name, this.interfaceTypes);
    }

    public AbstractNameQuery(URI uri) {
        if (uri == null) throw new NullPointerException("uri is null");

        //
        // Artifact
        //
        String artifactString = uri.getPath();
        //this doesn't seem to happen
//        if (artifactString == null) throw new IllegalArgumentException("uri does not contain a path part used for the artifact");

        if (artifactString != null && artifactString.length() > 0) {
            artifact = Artifact.createPartial(artifactString);
        } else {
            artifact = null;
        }

        //
        // name map
        //
        name = new TreeMap();
        String nameString = uri.getQuery();
        List nameParts = split(nameString, ',');
        for (Iterator iterator = nameParts.iterator(); iterator.hasNext();) {
            String namePart = (String) iterator.next();
            List keyValue = split(namePart, '=');
            if (keyValue.size() != 2) {
                throw new IllegalArgumentException("uri query string must be in the form [vendorId]/artifactId/[version]/[type] : " + nameString);
            }
            String key = (String) keyValue.get(0);
            String value = (String) keyValue.get(1);
            if (name.containsKey(key)) {
                throw new IllegalArgumentException("uri query string contains the key '" + key + "' twice : " + nameString);
            }
            name.put(key, value);
        }
//        if (name.isEmpty()) {
//            throw new IllegalArgumentException("name is empty: " + nameString);
//        }

        String interfaceString = uri.getFragment();
        List interfaces = split(interfaceString, ',');
        interfaceTypes = new HashSet(interfaces);

        //
        // uri
        //
        this.uri = createURI(artifact, name, interfaceTypes);
    }

    private static List split(String source, char delim) {
        List parts = new ArrayList();
        if (source != null && source.length() > 0) {
            for (int index = source.indexOf(delim); index >= 0; index = source.indexOf(delim)) {
                String part = source.substring(0, index);
                source = source.substring(index + 1);
                parts.add(part);
            }
            parts.add(source);
        }
        return parts;
    }

    private static URI createURI(Artifact artifact, Map name, Set interfaceTypes) {
        StringBuilder queryString = new StringBuilder();
        TreeMap treeMap = new TreeMap(name);
        for (Iterator iterator = treeMap.entrySet().iterator(); iterator.hasNext();) {
            Map.Entry entry = (Map.Entry) iterator.next();
            String key = (String) entry.getKey();
            String value = (String) entry.getValue();
            queryString.append(key).append('=').append(value);
            if (iterator.hasNext()) {
                queryString.append(',');
            }
        }
        StringBuilder fragmentString = new StringBuilder();
        TreeSet treeSet = new TreeSet(interfaceTypes);
        for (Iterator iterator = treeSet.iterator(); iterator.hasNext();) {
            String interfaceType = (String) iterator.next();
            fragmentString.append(interfaceType);
            if (iterator.hasNext()) {
                fragmentString.append(',');
            }
        }
        try {
            return new URI(null, null, artifact == null? null: artifact.toString(), queryString.toString(), fragmentString.toString());
        } catch (URISyntaxException e) {
            IllegalArgumentException illegalArgumentException = new IllegalArgumentException();
            illegalArgumentException.initCause(e);
            throw illegalArgumentException;
        }
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
        return uri.toString();
    }

    public URI toURI() {
        return uri;
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


    public boolean matches(AbstractName info, Set targetInterfaceTypes) {
        try {
            if (!info.getName().entrySet().containsAll(name.entrySet())) {
                return false;
            }
            if (!targetInterfaceTypes.containsAll(interfaceTypes)) {
                return false;
            }
            if (artifact == null) {
                return true;
            }
            Artifact otherArtifact = info.getArtifact();
            return artifact.matches(otherArtifact);
        } catch (NullPointerException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * N.B. parameter info is supposed to be more specific than this.
     * This is the opposite of the meaning of Artifact.matches.
     *
     * @param info
     * @return if info is a more specific version of this name query.
     */
    public boolean matches(AbstractNameQuery info) {
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
