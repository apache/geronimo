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
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import javax.management.ObjectName;

import org.apache.geronimo.kernel.repository.Artifact;
import org.apache.geronimo.kernel.Jsr77Naming;

/**
 * @version $Rev$ $Date$
 */
public class AbstractName implements Serializable {
    private static final long serialVersionUID = 3584199042821734754L;

    private final Artifact artifact;
    private final Map name;
    private final ObjectName objectName;
    private final URI uri;

    public AbstractName(Artifact artifact, Map name) {
        if (artifact == null) throw new NullPointerException("artifact is null");
        if (name == null) throw new NullPointerException("name is null");
        if (name.isEmpty()) throw new IllegalArgumentException("name is empty");

        this.artifact = artifact;
        this.name = name;

        this.objectName = Jsr77Naming.createObjectName(name);

        this.uri = createURI(artifact, name);
    }

    public AbstractName(Artifact artifact, Map name, ObjectName objectName) {
        if (artifact == null) throw new NullPointerException("artifact is null");
        if (name == null) throw new NullPointerException("name is null");
        if (name.isEmpty()) throw new IllegalArgumentException("name is empty");
        if (objectName == null) throw new NullPointerException("objectName is null");

        this.artifact = artifact;
        this.name = name;
        this.objectName = objectName;

        this.uri = createURI(artifact, name);
    }

    /**
     * Contructs an AbstractName object from the given URI.
     * 
     * The artifactId for the AbstractName is constructed from the URI path 
     * (everything up to the ? character) and is composed of four parts delimited by
     * slashes.  The artifactId is the only mandatory part, all slashes are mandatory.
     *
     * The name map for the AbstractName is constructed from key=value pairs.  
     * Each key=value pair is delimited by a ',' character and the key is separated
     * from the value by the '=' character. Each key must be unique. 
     * At least one key=value pair must be specified in the query string.
     * 
     * The URI has the following format:
     *  [vendorId]/artifactId/[version]/[type]?key=value[,key=value][,...]
     * 
     * @param uri The URI to be used to generate an AbstractName.
     */
    public AbstractName(URI uri) {
        if (uri == null) throw new NullPointerException("uri is null");

        //
        // Artifact
        //
        String artifactString = uri.getPath();
        if (artifactString == null) throw new IllegalArgumentException("uri does not contain a path part used for the artifact");

        List artifactParts = split(artifactString, '/');
        if (artifactParts.size() != 4) {
            throw new IllegalArgumentException("uri path must be in the form [vendorId]/artifactId/[version]/[type] : " + artifactString);
        }

        String groupId = (String) artifactParts.get(0);
        if (groupId.length() == 0) groupId = null;

        String artifactId = (String) artifactParts.get(1);
        if (artifactId.length() == 0) artifactId = null;

        String version = (String) artifactParts.get(2);
        if (version.length() == 0) version = null;

        String type = (String) artifactParts.get(3);
        if (type.length() == 0) type = null;

        artifact = new Artifact(groupId, artifactId, version, type);

        //
        // name map
        //
        name = new TreeMap();
        String nameString = uri.getQuery();
        if (nameString == null) {
            throw new IllegalArgumentException("uri does not contain a query part used for the name map; uri: " + uri);
        }
        
        List nameParts = split(nameString, ',');
        for (Iterator iterator = nameParts.iterator(); iterator.hasNext();) {
            String namePart = (String) iterator.next();
            List keyValue = split(namePart, '=');
            if (keyValue.size() != 2) {
                throw new IllegalArgumentException("uri query string must be in the form ?key=value[,key=value]*] : " + nameString);
            }
            String key = (String) keyValue.get(0);
            String value = (String) keyValue.get(1);
            if (name.containsKey(key)) {
                throw new IllegalArgumentException("uri query string contains the key '"+ key + "' twice : " + nameString);
            }
            name.put(key, value);
        }
        if (name.isEmpty()) {
            throw new IllegalArgumentException("name is empty: " + nameString);
        }

        //
        // uri
        //
        this.uri = createURI(artifact, name);

        //
        // object name
        //
        this.objectName = Jsr77Naming.createObjectName(name);
    }

    private static URI createURI(Artifact artifact, Map name) {
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
        try {
            return new URI(null, null, artifact.toString(), queryString.toString(), null);
        } catch (URISyntaxException e) {
            IllegalArgumentException illegalArgumentException = new IllegalArgumentException();
            illegalArgumentException.initCause(e);
            throw illegalArgumentException;
        }
    }

    // why not use String.split? Because String.split works using regular expressions
    // and this should be way faster, but write a benchmark it out if you have time.
    // Also this code is way simpler.
    private static List split(String source, char delim) {
        List parts = new ArrayList();
        for (int index = source.indexOf(delim); index >= 0; index = source.indexOf(delim)) {
            String part = source.substring(0, index);
            source = source.substring(index +  1);
            parts.add(part);
        }
        parts.add(source);
        return parts;
    }

    public Artifact getArtifact() {
        return artifact;
    }

    public Map getName() {
        return Collections.unmodifiableMap(name);
    }

    public String getNameProperty(String key) {
        return (String) name.get(key);
    }

    public ObjectName getObjectName() {
        return objectName;
    }

    public URI toURI() {
        return uri;
    }

    public String toString() {
        return uri.toString();
    }

    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        final AbstractName that = (AbstractName) o;

        if (artifact != null ? !artifact.equals(that.artifact) : that.artifact != null) return false;
        return !(name != null ? !name.equals(that.name) : that.name != null);

    }

    public int hashCode() {
        int result;
        result = (artifact != null ? artifact.hashCode() : 0);
        result = 29 * result + (name != null ? name.hashCode() : 0);
        return result;
    }

}
