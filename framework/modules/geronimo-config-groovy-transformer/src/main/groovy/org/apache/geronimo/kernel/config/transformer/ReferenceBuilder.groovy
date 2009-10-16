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

import org.apache.geronimo.gbean.AbstractNameQuery
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
class ReferenceBuilder {
    Set references
    
    ReferenceBuilder() {
        references = []
    }
   
    def pattern(String pattern) {
        try {
            references.add(new AbstractNameQuery(new URI(pattern)))
        } catch (Exception e) {
            throw new GroovyScriptException('pattern usage: pattern(patternValue)')
        }
    }
 
}