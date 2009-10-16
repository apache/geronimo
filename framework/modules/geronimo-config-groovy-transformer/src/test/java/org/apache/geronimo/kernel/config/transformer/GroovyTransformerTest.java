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

package org.apache.geronimo.kernel.config.transformer;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.apache.geronimo.gbean.AbstractNameQuery;
import org.apache.geronimo.gbean.GBeanData;
import org.apache.geronimo.gbean.ReferencePatterns;
import org.apache.geronimo.kernel.Jsr77Naming;
import org.apache.geronimo.kernel.config.ConfigurationData;
import org.apache.geronimo.kernel.repository.Artifact;
import org.apache.geronimo.kernel.repository.Dependency;
import org.apache.geronimo.kernel.repository.Environment;
import org.apache.geronimo.kernel.repository.ImportType;
import org.apache.geronimo.testsupport.RMockTestSupport;

/**
 *
 * @version $Rev:$ $Date:$
 */
public class GroovyTransformerTest extends RMockTestSupport {

    private GroovyTransformer transformer;
    private ConfigurationData configurationData;

    @Override
    protected void setUp() throws Exception {
        final ScriptLocater scriptLocater = (ScriptLocater) mock(ScriptLocater.class);
        transformer = new GroovyTransformer() {
            @Override
            protected ScriptLocater newScriptLocater() {
                return scriptLocater;
            }
        };

        Artifact configArtifact = new Artifact("group", "configArtifact", "1.0", "car");
        configurationData = new ConfigurationData(configArtifact , new Jsr77Naming());
        
        scriptLocater.locate(configurationData);
        modify().returnValue(resolveFile("src/test/resources/configurationDir"));
    }
    
    public void testTransformDependencies() throws Exception {
        startVerification();
        
        Environment environment = configurationData.getEnvironment();
        environment.addDependency(new Dependency(new Artifact("group", "artifactToRemove", "1.0", "jar"), ImportType.ALL));

        transformer.transformDependencies(configurationData);
        
        List<Dependency> newDependencies = environment.getDependencies();
        assertEquals(1, newDependencies.size());
        Dependency newDependency = newDependencies.get(0);
        assertEquals(new Artifact("group", "artifactToAdd", "1.0", "jar"), newDependency.getArtifact());
        assertSame(ImportType.SERVICES, newDependency.getImportType());
        
        assertSame(transformer, configurationData.getConfigurationDataTransformer());
    }
    
    public void testTransformGBeans() throws Exception {
        startVerification();
        
        GBeanData existingGBean = new GBeanData(DummyGBean.class);
        List gbeans = Collections.singletonList(existingGBean);
        List<GBeanData> newGBeans = transformer.transformGBeans(getClass().getClassLoader(), configurationData, gbeans);
        assertEquals(2, newGBeans.size());
        assertSame(existingGBean, newGBeans.get(0));
        
        GBeanData addedGBean = newGBeans.get(1);
        assertEquals(DummyGBean.class.getName(), addedGBean.getGBeanInfo().getClassName());
        assertEquals("value", addedGBean.getAttribute("attributeName"));
        ReferencePatterns patterns = addedGBean.getReferencePatterns("referenceName");
        Set<AbstractNameQuery> nameQueries = patterns.getPatterns();
        assertEquals(2, nameQueries.size());
    }
    
}
