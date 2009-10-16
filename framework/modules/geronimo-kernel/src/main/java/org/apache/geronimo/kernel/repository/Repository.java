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

}
