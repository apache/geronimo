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
package org.apache.geronimo.deployment;

import org.apache.geronimo.kernel.repository.Artifact;
import org.apache.geronimo.kernel.repository.Environment;
import org.apache.geronimo.kernel.repository.Version;

/**
 * A utility class to flesh out any incomplete Module IDs (formerly known as
 * config IDs) encountered during the course of a deployment.  For example,
 * an EAR may have a module ID with only an artifactId, and contain a web
 * app with no Geronimo plan and an EJB JAR with a module ID with no version.
 *
 * @version $Rev$ $Date$
 */
public class ModuleIDBuilder {
    private Version defaultVersion;
    private String defaultGroup;

    public ModuleIDBuilder() {
        defaultVersion = new Version(Long.toString(System.currentTimeMillis()));
        defaultGroup = Artifact.DEFAULT_GROUP_ID;
    }

    /**
     * If an EAR is going to pass this module ID builder to its children, it
     * can use this to set the default groupId to be its own.
     */
    public void setDefaultGroup(String defaultGroup) {
        this.defaultGroup = defaultGroup;
    }


    /**
     * If an EAR is going to pass this module ID builder to its children, it
     * can use this to set the default version to be its own.
     */
    public void setDefaultVersion(Version defaultVersion) {
        this.defaultVersion = defaultVersion;
    }

    /**
     * Translates the argument Artifact to a fully-resolved Artifact, which is
     * returned.  If the argument was fully-resolved to begin with it is
     * returned as is.  Otherwise, a new Artifact is returned with any missing
     * values populated.
     *
     * @param argument     The artifact to review
     * @param defaultType  The type to use if the artifact to review has no
     *                     type specified
     *
     * @return A fully resolved Artifact
     *
     * @throws IllegalArgumentException Occurs when the argument artifact does
     *                                  not have an artifactId
     */
    public Artifact resolve(Artifact argument, String defaultType) {
        if(argument.isResolved()) {
            return argument;
        }
        if(argument.getArtifactId() == null || argument.getArtifactId().equals("")) {
            throw new IllegalArgumentException("Incoming Artifact must have an ArtifactID (not "+argument+")");
        }
        return new Artifact(argument.getGroupId() == null || argument.getGroupId().equals("") ? defaultGroup : argument.getGroupId(),
                argument.getArtifactId(),
                argument.getVersion() == null ? defaultVersion : argument.getVersion(),
                argument.getType() == null || argument.getType().equals("") ? defaultType : argument.getType());
    }

    /**
     * Creates a new artifact using entirely default values.
     *
     * @param defaultArtifact  The artifactId to use for the new Artifact
     * @param defaultType      The type to use for the new Artifact
     */
    public Artifact createDefaultArtifact(String defaultArtifact, String defaultType) {
        return new Artifact(defaultGroup, defaultArtifact, defaultVersion, defaultType);
    }

    /**
     * Guarantees that the argument Environment will have a present and fully
     * qualified module ID when this method returns. If the Environment is
     * missing a module ID, or has a partial module ID (isResolved() == false)
     * then this method will fill in any missing values.  If the module ID is
     * present and resolved, then this method does nothing.
     *
     * @param environment        The Environment to check and populate
     * @param defaultArtifactId  The artifactId to use if the Envrionment does
     *                           not have a module ID at all
     * @param defaultType        The type to use if the Environment is lacking
     *                           a module ID or the module ID is lacking a type
     */
    public void resolve(Environment environment, String defaultArtifactId, String defaultType) {
        if(environment.getConfigId() == null) {
            environment.setConfigId(resolve(new Artifact(null, defaultArtifactId, (Version)null, defaultType), defaultType));
        } else if(!environment.getConfigId().isResolved()) {
            environment.setConfigId(resolve(environment.getConfigId(), defaultType));
        }
    }
}
