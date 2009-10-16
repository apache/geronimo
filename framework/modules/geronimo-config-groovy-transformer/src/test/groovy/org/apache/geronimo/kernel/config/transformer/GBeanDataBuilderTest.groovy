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

import org.apache.geronimo.kernel.config.transformer.GBeanDataBuilderimport org.apache.geronimo.kernel.config.transformer.GroovyScriptExceptionimport org.apache.geronimo.kernel.config.ConfigurationDataimport org.apache.geronimo.kernel.repository.Artifactimport org.apache.geronimo.kernel.Jsr77Naming
/**
 *
 * @version $Rev:$ $Date:$
 */
class GBeanDataBuilderTest extends GroovyTestCase {
     def builder
     def gbeanDatas
     
     protected void setUp() {
         gbeanDatas = []

         def artifact = new Artifact('groupId', 'artifactId', '1,0', 'car')
         def configurationData = new ConfigurationData(artifact, new Jsr77Naming())
         builder = new GBeanDataBuilder(configurationData, gbeanDatas)
     }
     
     void testAddGBean() {
         builder.addGBean(name: 'name', gbean: DummyGBean, type: 'DummyGBeanType') {}
         
         assertEquals(1, gbeanDatas.size())
         def gbeanData = gbeanDatas[0]
         
         assertEquals(DummyGBean.class.name, gbeanData.gbeanInfo.className)

         def abstractName = gbeanData.abstractName
         assertEquals('name', abstractName.getNameProperty(Jsr77Naming.J2EE_NAME))
         assertEquals('DummyGBeanType', abstractName.getNameProperty(Jsr77Naming.J2EE_TYPE))
	}

     void testAddGBeanWithoutTypeDefaultsToGBeanType() {
         builder.addGBean(name: 'name', gbean: DummyGBean) {}
         
         def gbeanData = gbeanDatas[0]
         def abstractName = gbeanData.abstractName
         assertEquals('GBean', abstractName.getNameProperty(Jsr77Naming.J2EE_TYPE))
	}

     void testAddGBeanWihoutNameThrowsGSE() {
         shouldFailWithCause(GroovyScriptException) {
             builder.addGBean(gbean: DummyGBean, type: 'DummyGBeanType') {}
         }
	}

     void testAddGBeanWihoutClassThrowsGSE() {
         shouldFailWithCause(GroovyScriptException) {
             builder.addGBean(name: 'name', type: 'DummyGBeanType') {}
         }
	}

 }