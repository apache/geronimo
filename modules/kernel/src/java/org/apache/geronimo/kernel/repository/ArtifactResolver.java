/**
 *
 * Copyright 2005 The Apache Software Foundation
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
package org.apache.geronimo.kernel.repository;

import java.util.Collection;
import java.util.LinkedHashSet;

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
     * @param source       The artifact to complete (normally partially-populated)
     * @param defaultType  The type to use for the resulting artifact if the source
     *                     artifact doesn't have a type set
     *
     * @return If the source artifact is fully populated (e.g. artifact.isResolved()
     *         == true) then it will be returned.  Otherwise a new fully-populated
     *         artifact is returned.
     */
    Artifact generateArtifact(Artifact source, String defaultType);

    /**
     * Used to search for existing artifacts that match the supplied artifact (which
     * may be partially-populated).  Preference is given to artifacts that are already
     * loaded, to reduce duplication.  If nothing can be found that's an error,
     * because something depends on this.
     */
    Artifact resolveInClassLoader(Artifact source) throws MissingDependencyException;
    /**
     * Used to search for existing artifacts that match the supplied artifact (which
     * may be partially-populated).  Preference is given to artifacts that are already
     * loaded, or that exist in the parent configurations, to reduce duplication.  If
     * nothing can be found that's an error, because something depends on this.
     */
    Artifact resolveInClassLoader(Artifact source, Collection parentConfigurations) throws MissingDependencyException;
    /**
     * Used to search for existing artifacts that match the supplied artifact (which
     * may be partially-populated).  Preference is given to artifacts that are already
     * loaded, to reduce duplication.  If nothing can be found that's an error,
     * because something depends on this.
     *
     * @return A sorted set ordered in the same way the input was ordered, with
     *         entries of type Artifact
     */
    LinkedHashSet resolveInClassLoader(Collection artifacts) throws MissingDependencyException;
    /**
     * Used to search for existing artifacts that match the supplied artifact (which
     * may be partially-populated).  Preference is given to artifacts that are already
     * loaded, or that exist in the parent configurations, to reduce duplication.  If
     * nothing can be found that's an error, because something depends on this.
     *
     * @return A sorted set ordered in the same way the input was ordered, with
     *         entries of type Artifact
     */
    LinkedHashSet resolveInClassLoader(Collection artifacts, Collection parentConfigurations) throws MissingDependencyException;

    /**
     * Used to search for existing artifacts in the server that match the supplied
     * artifact (which may be partially-populated).  This method expects either no
     * results or one result (multiple matches is an error).
     *
     * @return A matching artifact, or null of there were no matches
     */
    Artifact queryArtifact(Artifact artifact) throws MultipleMatchesException;

    /**
     * Used to search for existing artifacts in the server that match the supplied
     * artifact (which may be partially-populated).
     *
     * TODO: The artifacts should be sorted ascending by type then group then artifact then version
     *
     * @return The matching artifacts, which may be 0, 1, or many
     */
    Artifact[] queryArtifacts(Artifact artifact);
}
