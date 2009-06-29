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
class GBeanAttributeAndReferenceBuilder {
    private final def configurationData
    private final def gbeanData
    
    GBeanAttributeAndReferenceBuilder(configurationData, gbeanData) {
        assert null != configurationData : 'configurationData is required'
        assert null != gbeanData : 'gbeanData is required'
        this.configurationData = configurationData
        this.gbeanData = gbeanData
    }
   
    def attribute(Map attributeInfo) {
        def throwUsage = {
            throw new GroovyScriptException('attribute usage: attribute(attributeName: attributeValue)')
        }

        def mapIter = attributeInfo.entrySet().iterator()
        if (!mapIter.hasNext()) {
            throwUsage()
        }
        
        def entry = mapIter.next()
        def key = entry.key
        def value = entry.value
        gbeanData.setAttribute(key, value)
    }
 
    def reference(Object[] referenceInfo) {
        def throwUsage = {
            throw new GroovyScriptException('reference usage: reference(referenceName) {pattern(patternValue)}')
        }
        
        if (2 != referenceInfo.length) {
            throwUsage()
        }
        
        def name = referenceInfo[0]
        if (!(name instanceof String)) {
            throwUsage()
        }
        
        def referenceDeclarationClosure = referenceInfo[1]
        if (!(referenceDeclarationClosure instanceof Closure)) {
            throwUsage()
        }

        def referenceBuilder = new ReferenceBuilder()
        referenceDeclarationClosure.delegate = referenceBuilder
        referenceDeclarationClosure()
        
        gbeanData.setReferencePatterns(name, referenceBuilder.references)
    }

}