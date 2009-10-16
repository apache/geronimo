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

package org.apache.geronimo.kernel.config.transformer

import org.apache.geronimo.kernel.config.transformer.ConfigurationDataBuilder
import org.apache.geronimo.kernel.repository.ImportType
import org.apache.geronimo.kernel.config.ConfigurationData
import org.apache.geronimo.kernel.Jsr77Naming
import org.apache.geronimo.kernel.repository.Artifact
import org.apache.geronimo.kernel.config.transformer.GroovyScriptException

/**
 *
 * @version $Rev:$ $Date:$
 */
class ConfigurationDataBuilderTest extends GroovyTestCase {
     def builder
     def configurationData
     
     protected void setUp() {
         def artifact = new Artifact('groupId', 'artifactId', '1,0', 'car')
         configurationData = new ConfigurationData(artifact, new Jsr77Naming())
         builder = new ConfigurationDataBuilder(configurationData)
     }
     
     void testAddDependency() {
         def addedArtifact = new Artifact('group', 'artifact', '1,0', 'tar')
         
         builder.addDependency(groupId: addedArtifact.groupId, 
                 artifactId: addedArtifact.artifactId, 
                 version: addedArtifact.version, 
                 type: addedArtifact.type, 
                 importType: ImportType.SERVICES)

         def dependencies = configurationData.environment.dependencies
         assertEquals(1, dependencies.size())
         def dependency = dependencies[0]
         assertEquals(addedArtifact, dependency.artifact)
         assertEquals(ImportType.SERVICES, dependency.importType)
     }

     void testAddDependencyOnlyWithGroupAndArtifact() {
         def group = 'group'
         def artifact = 'artifact'
         
         builder.addDependency(groupId: group, artifactId: artifact)

         def dependencies = configurationData.environment.dependencies
         assertEquals(1, dependencies.size())
         def dependency = dependencies[0]
         def depArtifact = dependency.artifact
         assertEquals(group, depArtifact.groupId)
         assertEquals(artifact, depArtifact.artifactId)
         assertEquals('jar', depArtifact.type)
         assertEquals(ImportType.ALL, dependency.importType)
     }

     void testAddDependencyThrowsGSEIfGroupIsMissing() {
         def addedArtifact = new Artifact(null, 'artifact', '1.0', 'jar')

         shouldFail(GroovyScriptException) {
             builder.addDependency(groupId: addedArtifact.groupId, 
                     artifactId: addedArtifact.artifactId, 
                     version: addedArtifact.version, 
                     type: addedArtifact.type,
                     importType: ImportType.SERVICES)
         }
     }

     void testRemoveDependency() {
         def addedArtifact = new Artifact('group', 'artifact', '1,0', 'tar')
         
         builder.addDependency(groupId: addedArtifact.groupId, 
                 artifactId: addedArtifact.artifactId, 
                 version: addedArtifact.version, 
                 type: addedArtifact.type, 
                 importType: ImportType.SERVICES)

         builder.removeDependency(artifactId: addedArtifact.artifactId)
                 
         def dependencies = configurationData.environment.dependencies
         assertTrue(dependencies.empty)
     }

 }