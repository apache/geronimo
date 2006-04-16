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

/**
 * Indicates that you tried to resolve a partially-populated artifact to
 * a real artifact expecting one match but you got multiple matches.
 *
 * Note there is a separate method to call if you expect multiple matches.
 *
 * @version $Rev: 46019 $ $Date: 2004-09-14 05:56:06 -0400 (Tue, 14 Sep 2004) $
 */
public class MultipleMatchesException extends Exception {
    private final Artifact artifact;

    public MultipleMatchesException(Artifact query) {
        super();
        artifact = query;
    }

    public MultipleMatchesException(Artifact query, String message) {
        super(message);
        artifact = query;
    }

    /**
     * Gets the artifact used as a query argument that matched multiple real
     * artifacts available in the server.
     */
    public Artifact getArtifact() {
        return artifact;
    }
}
