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
class GBeanDataBuilder {
    private final def configurationData
    private final def gbeanDatas
    
    GBeanDataBuilder(configurationData, gbeanDatas) {
        assert null != configurationData : 'configurationData is required'
        assert null != gbeanDatas : 'gbeanDatas is required'
        this.configurationData = configurationData
        this.gbeanDatas = gbeanDatas
    }
    
    def configure (Closure closure) {
        closure.delegate = this
        closure()
    }

    def addGBean(Map gbeanDeclaration, Closure gbeanClosure) {
        def throwUsage = {
            throw new GroovyScriptException('addGBean usage: addGBean(name: gbeanName, gbean: gbeanClass, type: gbeanType)')
        }

        if (!gbeanDeclaration.name) {
            throwUsage()
        } else if (!gbeanDeclaration.gbean) {
            throwUsage()
        }

        def gbeanName = buildGBeanName(gbeanDeclaration)
        def gbean = gbeanDeclaration.gbean 
        def gbeanData = new GBeanData(gbeanName, gbean)

        gbeanClosure.delegate = new GBeanAttributeAndReferenceBuilder(configurationData, gbeanData)
        gbeanClosure()

        gbeanDatas.push(gbeanData)
    }
    
    protected def buildGBeanName(gbeanDeclaration) throws GroovyScriptException {
        def name = gbeanDeclaration.name

        def type = gbeanDeclaration.type
        if (!type) {
            type = 'GBean'
        }
        
        def naming = configurationData.naming
        naming.createRootName(configurationData.environment.configId, name, type)
    }
    
}