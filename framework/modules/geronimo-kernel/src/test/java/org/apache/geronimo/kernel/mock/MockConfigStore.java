/**
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.apache.geronimo.kernel.mock;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.List; 

import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.gbean.GBeanInfoBuilder;
import org.apache.geronimo.kernel.Jsr77Naming;
import org.apache.geronimo.kernel.Naming;
import org.apache.geronimo.kernel.config.ConfigurationData;
import org.apache.geronimo.kernel.config.ConfigurationStore;
import org.apache.geronimo.kernel.config.InvalidConfigException;
import org.apache.geronimo.kernel.config.NoSuchConfigException;
import org.apache.geronimo.kernel.config.NullConfigurationStore;
import org.apache.geronimo.kernel.repository.Artifact;
import org.apache.geronimo.kernel.util.FileUtils;

/**
 * ???
 *
 * @version $Rev$ $Date$
 */
public class MockConfigStore
    extends NullConfigurationStore
{
    protected static final Naming naming = new Jsr77Naming();

    protected final Map<Artifact, File> locations = new HashMap<Artifact, File>();
    private Map<Artifact, ConfigurationData> configs = new HashMap<Artifact, ConfigurationData>();
    private List<File> createdLocations = new ArrayList<File>(); 
    

    public void install(ConfigurationData configurationData) throws IOException, InvalidConfigException {
        configs.put(configurationData.getId(), configurationData);
        configurationData.setConfigurationStore(this);
    }

    public void uninstall(Artifact configID) throws NoSuchConfigException, IOException {
        configs.remove(configID);
    }

    public ConfigurationData loadConfiguration(Artifact configId) throws NoSuchConfigException, IOException, InvalidConfigException {
        if (configs.containsKey(configId)) {
            return configs.get(configId);
        } else {
            ConfigurationData configurationData = new ConfigurationData(configId, naming);
            install(configurationData);
            return configurationData;
        }
    }

    public boolean containsConfiguration(Artifact configID) {
        return configs.containsKey(configID);
    }

    public File createNewConfigurationDir(Artifact configId) {
        try {
            File file = createTempDir();
            locations.put(configId, file);
            createdLocations.add(file); 
            return file;
        } catch (IOException e) {
            return null;
        }
    }

    public Set<URL> resolve(Artifact configId, String moduleName, String pattern) throws NoSuchConfigException, MalformedURLException {
        File file = locations.get(configId);
        if (file == null) {
            throw new NoSuchConfigException(configId);
        }
        return FileUtils.search(file, pattern);
    }

    public void installFake(Artifact configId, File file) {
        if (!configs.containsKey(configId)) {
            configs.put(configId, null);
        }
        locations.put(configId, file);
    }
    
    /**
     * Attempt to cleanup and temp directories associated with 
     * this Mock config store. 
     */
    public void cleanup() {
        for (File file: createdLocations) {
            FileUtils.recursiveDelete(file);
        }
        createdLocations.clear(); 
    }

    private static File createTempDir() throws IOException {
        File tempDir = File.createTempFile("mock-geronimo-deploymentUtil", ".tmpdir");
        tempDir.delete();
        tempDir.mkdirs();
        return tempDir;
    }

    public final static GBeanInfo GBEAN_INFO;

    static {
        GBeanInfoBuilder infoBuilder = GBeanInfoBuilder.createStatic(MockConfigStore.class, "ConfigurationStore");
        infoBuilder.addInterface(ConfigurationStore.class);
        GBEAN_INFO = infoBuilder.getBeanInfo();
    }
}
