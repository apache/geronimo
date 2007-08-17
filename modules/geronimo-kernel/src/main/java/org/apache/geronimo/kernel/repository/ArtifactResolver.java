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

import java.util.Collection;
import java.util.LinkedHashSet;

import org.apache.geronimo.kernel.config.Configuration;

/**
 * @version $Rev$ $Date$
 */
public interface ArtifactResolver {

    /**
     * Used to generate a fully-populated Artifact from a partially-populated Artifact
     * when you're about to deploy/save a new artifact.  That is, this method comes up
     * with reasonable default values that hopefully do not conflict with anything
     * that's already deployed.
     *
     * @param source      The artifact to complete (normally partially-resolved)
     * @param defaultType The type to use for the resulting artifact if the source
     *                    artifact doesn't have a type set
     * @return If the source artifact is fully resolved (e.g. artifact.isResolved()
     *         == true) then it will be returned.  Otherwise a new fully-resolved
     *         artifact is returned.
     */
    Artifact generateArtifact(Artifact source, String defaultType);

    /**
     * Used to search for existing artifacts that match the supplied artifact (which
     * may be partially-populated).  Preference is given to artifacts that are already
     * loaded, to reduce duplication.  If nothing can be found that's an error,
     * because something depends on this.
     *
     * @param source incompletely resolved Artifact
     * @return completely resolved Artifact matching the source
     * @throws MissingDependencyException if no matching Artifact can be found.
     */
    Artifact resolveInClassLoader(Artifact source) throws MissingDependencyException;

    /**
     * Used to search for existing artifacts that match the supplied artifact (which
     * may be partially-populated).  Preference is given to artifacts that are already
     * loaded, or that exist in the parent configurations, to reduce duplication.  If
     * nothing can be found that's an error, because something depends on this.
     *
     * @param source               incompletely resolved Artifact
     * @param parentConfigurations A Collection with entries of type Configuration
     * @return completely resolved Artifact matching the source
     * @throws MissingDependencyException if no matching Artifact can be found.
     */
    Artifact resolveInClassLoader(Artifact source, Collection<Configuration> parentConfigurations) throws MissingDependencyException;

    /**
     * Used to search for existing artifacts that match the supplied artifact (which
     * may be partially-populated).  Preference is given to artifacts that are already
     * loaded, to reduce duplication.  If nothing can be found that's an error,
     * because something depends on this.
     *
     * @param sources incompletely resolved Artifact
     * @return A sorted set ordered in the same way the input was ordered, with
     *         entries of type Artifact
     * @throws MissingDependencyException if no matching Artifact can be found.
     */
    LinkedHashSet<Artifact> resolveInClassLoader(Collection<Artifact> sources) throws MissingDependencyException;

    /**
     * Used to search for existing artifacts that match the supplied artifact (which
     * may be partially-populated).  Preference is given to artifacts that are already
     * loaded, or that exist in the parent configurations, to reduce duplication.  If
     * nothing can be found that's an error, because something depends on this.
     *
     * @param sources incompletely resolved Artifacts to match
     * @param parentConfigurations Configurations to search in
     * @return A sorted set ordered in the same way the input was ordered, with
     *         entries of type Artifact
     * @throws MissingDependencyException if no matching Artifact can be found.
     */
    LinkedHashSet<Artifact> resolveInClassLoader(Collection<Artifact> sources, Collection<Configuration> parentConfigurations) throws MissingDependencyException;

    /**
     * Used to search for existing artifacts in the server that match the supplied
     * artifact (which may be partially-populated).  This method expects either no
     * results or one result (multiple matches is an error).
     *
     * @param artifact incompletely resolved artifact to match
     * @return A matching artifact, or null of there were no matches
     * @throws MultipleMatchesException if there is more than one match
     */
    Artifact queryArtifact(Artifact artifact) throws MultipleMatchesException;

    /**
     * Used to search for existing artifacts in the server that match the supplied
     * artifact (which may be partially-populated).
     * <p/>
     * TODO: The artifacts should be sorted ascending by type then group then artifact then version
     *
     * @param artifact the Artifact to match.
     * @return The matching artifacts, which may be 0, 1, or many
     */
    Artifact[] queryArtifacts(Artifact artifact);
}
