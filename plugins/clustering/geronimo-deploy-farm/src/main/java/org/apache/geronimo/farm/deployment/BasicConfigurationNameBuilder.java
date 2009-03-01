/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.apache.geronimo.farm.deployment;

import org.apache.geronimo.kernel.repository.Artifact;

/**
 *
 * @version $Rev:$ $Date:$
 */
public class BasicConfigurationNameBuilder implements ConfigurationNameBuilder {
    private static final String ARTIFACT_SUFFIX = "_G_MASTER";

    public Artifact buildMasterConfigurationName(Artifact configId) {
        return newArtifact(configId, configId.getArtifactId() + ARTIFACT_SUFFIX);
    }

    public Artifact buildSlaveConfigurationName(Artifact configId) {
        if (!isMasterConfigurationName(configId)) {
            throw new IllegalArgumentException("[" + configId + "] is not a master configuration name");
        }
        String artifactId = configId.getArtifactId();
        return newArtifact(configId, artifactId.substring(0, artifactId.length() - ARTIFACT_SUFFIX.length()));
    }
    
    public boolean isMasterConfigurationName(Artifact configId) {
        return configId.getArtifactId().endsWith(ARTIFACT_SUFFIX);
    }
    
    protected Artifact newArtifact(Artifact configId, String artifactId) {
        return new Artifact(configId.getGroupId(), artifactId, configId.getVersion(), configId.getType());
    }
    
}
