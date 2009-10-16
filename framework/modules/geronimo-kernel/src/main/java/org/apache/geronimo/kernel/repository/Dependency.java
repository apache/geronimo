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
package org.apache.geronimo.kernel.repository;

import java.io.Serializable;

/**
 * A dependency of a Geronimo configuration.  This may be another
 * configuration, or it may be a third-party JAR that just needs to go on the
 * configuration Class Path.
 *
 * @version $Rev$ $Date$
 */
public class Dependency implements Serializable {
    private static final long serialVersionUID = -1940822102064150145L;
    private final Artifact artifact;
    private final ImportType importType;

    public Dependency(Artifact artifact, ImportType importType) {
        this.artifact = artifact;
        this.importType = importType;
    }

    public Artifact getArtifact() {
        return artifact;
    }

    public ImportType getImportType() {
        return importType;
    }

    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        final Dependency that = (Dependency) o;

        if (artifact != null ? !artifact.equals(that.artifact) : that.artifact != null) return false;

        return true;
    }
    
    public int hashCode() {
        return (artifact != null ? artifact.hashCode() : 0);
    }

    public String toString() {
        return "[" + importType  + ": " + artifact + "]";
    }
}
