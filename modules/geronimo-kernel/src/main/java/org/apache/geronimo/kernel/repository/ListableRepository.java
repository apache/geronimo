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

import java.util.SortedSet;

/**
 * For repositories that can provide a list of their contents.
 * Normally local ones can handle it, but remote ones may or may
 * not implement this.
 *
 * @version $Rev$ $Date$
 */
public interface ListableRepository extends Repository {
    /**
     * Gets a set (with entries of type Artifact) of all the items available
     * in the repository.
     * @return sorted list of artifacts in the repository
     */
    public SortedSet<Artifact> list();

    /**
     * Gets a set (with entries of type Artifact) of all the available items
     * matching the specified artifact, which is normally not fully resolved
     * (so the results all match whatever fields are specified on the argument
     * Artifact).
     * @param query match for repository
     * @return sorted list of artifacts in the repository that match the query.
     */
    public SortedSet<Artifact> list(Artifact query);
}
