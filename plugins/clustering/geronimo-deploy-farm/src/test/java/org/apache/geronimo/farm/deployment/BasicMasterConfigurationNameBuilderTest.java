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

import junit.framework.TestCase;

/**
 *
 * @version $Rev:$ $Date:$
 */
public class BasicMasterConfigurationNameBuilderTest extends TestCase {

    private BasicSlaveConfigurationNameBuilder builder;

    @Override
    protected void setUp() throws Exception {
        builder = new BasicSlaveConfigurationNameBuilder();
    }
    
    public void testIsSlaveConfigurationName() throws Exception {
        Artifact artifact = new Artifact("groupId", "artifactId", "2.0", "car");
        assertFalse(builder.isSlaveConfigurationName(artifact));
        Artifact masterConfiguration = builder.buildSlaveConfigurationName(artifact);
        assertTrue(builder.isSlaveConfigurationName(masterConfiguration));
    }
    
    public void testBuildSlaveConfigurationName() throws Exception {
        Artifact artifact = new Artifact("groupId", "artifactId", "2.0", "car");
        Artifact masterConfiguration = builder.buildSlaveConfigurationName(artifact);
        assertEquals(artifact.getGroupId(), masterConfiguration.getGroupId());
        assertEquals(artifact.getArtifactId() + "_G_SLAVE", masterConfiguration.getArtifactId());
        assertEquals(artifact.getVersion(), masterConfiguration.getVersion());
        assertEquals(artifact.getType(), masterConfiguration.getType());
    }
    
}
