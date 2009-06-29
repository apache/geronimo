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
 
import org.apache.geronimo.kernel.Jsr77Naming
import org.apache.geronimo.gbean.GBeanInfo
import org.apache.geronimo.gbean.GBeanData
import org.apache.geronimo.gbean.GBeanData
import org.apache.geronimo.kernel.repository.Version
import org.apache.geronimo.kernel.config.ConfigurationData
import org.apache.geronimo.kernel.repository.ImportType
import org.apache.geronimo.kernel.repository.Artifact
import org.apache.geronimo.kernel.repository.Dependency


/**
 *
 * @version $Rev:$ $Date:$
 */
class ConfigurationDataBuilder {
    private final ConfigurationData configurationData
    
    ConfigurationDataBuilder(ConfigurationData configurationData) {
        assert null != configurationData : 'configurationData is required'
        this.configurationData = configurationData
    }
    
    def configure (Closure closure) {
        closure.delegate = this
        closure()
    }

    def addDependency(Map dependencyDeclaration) throws GroovyScriptException {
        def throwUsage = {
            throw new GroovyScriptException('addDependency usage: addDependency(groupId: group, artifactId: artifact, version: X, type: type, importType: ImportType.X)')
        }

        ensureNotNull(dependencyDeclaration.groupId, throwUsage)
        ensureNotNull(dependencyDeclaration.artifactId, throwUsage)

        if (!dependencyDeclaration.type) {
            dependencyDeclaration.type = 'jar'
        }

        def artifact = buildArtifact(dependencyDeclaration)
        
        def importType = dependencyDeclaration.importType
        if (!importType) {
            importType = ImportType.ALL
        }
        
        configurationData.environment.addDependency(artifact, importType)
    }
    
    def removeDependency(Map dependencyFilter) throws GroovyScriptException {
        def throwUsage = {
            throw new GroovyScriptException('removeDependency usage: removeDependency(groupId: group, artifactId: artifact, version: X, type: type)')
        }

        def filteredArtifact = buildArtifact(dependencyFilter)
        def filteredDependencies = configurationData.environment.dependencies.findAll { dependency ->
            !filteredArtifact.matches(dependency.artifact)
        }

        configurationData.environment.dependencies = filteredDependencies
    }

    protected def buildArtifact(dependencyDeclaration) throws GroovyScriptException {
        def groupId = dependencyDeclaration.groupId
        def artifactId = dependencyDeclaration.artifactId
        def version = dependencyDeclaration.version
        def type = dependencyDeclaration.type

        new Artifact(groupId, artifactId, (String) version, type)
    }
    
    protected def ensureNotNull(value, throwUsage) throws GroovyScriptException {
        if (!value) {
            throwUsage()
        }
    }
    
}