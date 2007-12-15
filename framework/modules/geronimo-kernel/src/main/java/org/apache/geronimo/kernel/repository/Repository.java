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

import java.io.File;
import java.util.LinkedHashSet;

/**
 * Provides access to things like JARs via a standard API.  This is
 * a fairly limited read-only type repository.  There are additional
 * interfaces that a Repository may implement to indicate additional
 * capabilities.
 *
 * @version $Rev$ $Date$
 */
public interface Repository {
    /**
     * Checks whether this repository contains an entry for the specified
     * artifact.  The artifact must be fully resolved (isResolved() == true).
     */
    boolean contains(Artifact artifact);

    /**
     * Gets the location on disk where the specified artifact is stored.
     * The artifact must be fully resolved (isResolved() == true).
     *
     * @return The location of the artifact, or null if it is not in this
     *         repository.
     */
    File getLocation(Artifact artifact);

    /**
     * Loads any dependencies for this artifact declared in
     * META-INF/geronimo-dependency.xml within the configuration archive.  This
     * does not do anything special if the artifact is a configuration (which
     * means it doesn't see dependencies in the ConfigurationData, etc.) so
     * it's mainly useful for JAR-type artifacts.
     *
     * @param artifact A fully-resolved artifact representing the repository
     *                 entry you're interested in.
     *
     * @return a LinkedHashSet (with elements of type Artifact) listing any
     *         dependencies declared in META-INF/geronimo-dependency.xml for the
     *         specified artifact.
     */
    LinkedHashSet<Artifact> getDependencies(Artifact artifact);
}
