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

import groovy.lang.Binding;
import groovy.lang.GroovyShell;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import org.apache.geronimo.gbean.GBeanData;
import org.apache.geronimo.kernel.config.ConfigurationData;
import org.apache.geronimo.kernel.config.ConfigurationDataTransformer;
import org.apache.geronimo.kernel.config.InvalidConfigException;
import org.apache.geronimo.kernel.repository.Artifact;


/**
 *
 * @version $Rev:$ $Date:$
 */
public class GroovyTransformer implements ConfigurationDataTransformer {
    private static final String BINDING_CONFIGURATION_DATA = "configurationData";
    private static final String BINDING_CONFIGURATION_DATA_BUILDER = "configurationDataBuilder";
    private static final Pattern PATTERN_DEPENDENCIES_FILE = Pattern.compile("Dependencies(.*).groovy"); 

    private static final String BINDING_GBEAN_DATAS = "gbeanDatas";
    private static final String BINDING_GBEAN_DATAS_BUILDER = "gbeanDataBuilder";
    private static final Pattern PATTERN_GBEANS_FILE = Pattern.compile("GBeans(.*).groovy"); 
    
    private final ScriptLocater scriptLocater;

    public GroovyTransformer() {
        scriptLocater = newScriptLocater();
    }

    public void transformDependencies(ConfigurationData configurationData) throws InvalidConfigException {
        configurationData.setConfigurationDataTransformer(this);

        File scriptDir = scriptLocater.locate(configurationData);
        
        Map<String, Object> bindings = new HashMap<String, Object>();
        bindings.put(BINDING_CONFIGURATION_DATA, configurationData);
        bindings.put(BINDING_CONFIGURATION_DATA_BUILDER, new ConfigurationDataBuilder(configurationData));
        Binding binding = new Binding(bindings);
        GroovyShell shell = new GroovyShell(binding);

        executeScripts(shell, scriptDir, PATTERN_DEPENDENCIES_FILE);
    }

    public List<GBeanData> transformGBeans(ClassLoader classLoader,
            ConfigurationData configurationData,
            List<GBeanData> gbeanDatas) throws InvalidConfigException {
        gbeanDatas = new ArrayList<GBeanData>(gbeanDatas);
        
        File scriptDir = scriptLocater.locate(configurationData);

        Map<String, Object> bindings = new HashMap<String, Object>();
        bindings.put(BINDING_CONFIGURATION_DATA, configurationData);
        bindings.put(BINDING_GBEAN_DATAS, gbeanDatas);
        bindings.put(BINDING_GBEAN_DATAS_BUILDER, new GBeanDataBuilder(configurationData, gbeanDatas));
        Binding binding = new Binding(bindings);
        GroovyShell shell = new GroovyShell(classLoader, binding);

        executeScripts(shell, scriptDir, PATTERN_GBEANS_FILE);

        return gbeanDatas;
    }
    
    public void remove(Artifact configId) {
    }
    
    protected ScriptLocater newScriptLocater() {
        return new CollocatedWithConfigInfoLocater();
    }

    protected void executeScripts(GroovyShell shell, File scriptDir, Pattern pattern) throws InvalidConfigException {
        File[] scripts = scriptDir.listFiles(new PatternFilter(pattern));
        executeScripts(shell, scripts);
    }

    protected void executeScripts(GroovyShell shell, File[] scripts) throws InvalidConfigException {
        for (File script : scripts) {
            try {
                shell.evaluate(script);
            } catch (Exception e) {
                if (e instanceof InvalidConfigException) {
                    throw (InvalidConfigException) e;
                }
                throw new GroovyScriptException("Error while evaluating [" + script.getAbsolutePath() + "]", e);
            }
        }
    }

}
