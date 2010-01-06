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

import java.io.File;
import java.io.IOException;

import org.apache.geronimo.gbean.annotation.GBean;
import org.apache.geronimo.gbean.annotation.ParamReference;
import org.apache.geronimo.kernel.config.ConfigurationData;
import org.apache.geronimo.kernel.config.ConfigurationStore;
import org.apache.geronimo.kernel.util.FileUtils;
import org.apache.geronimo.kernel.config.InvalidConfigException;
import org.apache.geronimo.kernel.config.NoSuchConfigException;
import org.apache.geronimo.kernel.repository.Artifact;

/**
 *
 * @version $Rev:$ $Date:$
 */
@GBean(j2eeType=BasicClusterConfigurationStore.GBEAN_J2EE_TYPE)
public class BasicClusterConfigurationStore implements ClusterConfigurationStore {
    private final ConfigurationStore actualConfigurationStore;
    
    public BasicClusterConfigurationStore(@ParamReference(name=GBEAN_REF_CONF_STORE, namingType="ConfigurationStore") ConfigurationStore actualConfigurationStore) {
        if (null == actualConfigurationStore) {
            throw new IllegalArgumentException("actualConfigurationStore is required");
        }
        this.actualConfigurationStore = actualConfigurationStore;
    }

    public void install(ConfigurationData configurationData, File packedConfigurationDir)
            throws IOException, InvalidConfigException {
        try {
            File configurationDir = actualConfigurationStore.createNewConfigurationDir(configurationData.getId());

            DirectoryPackager directoryPackager = newDirectoryPackager();
            directoryPackager.unpack(configurationDir, packedConfigurationDir);
            configurationData.setConfigurationDir(configurationDir);
            
            actualConfigurationStore.install(configurationData);
        } finally {
            deleteDir(packedConfigurationDir);
        }
    }

    public void uninstall(Artifact configId) throws NoSuchConfigException, IOException {
        actualConfigurationStore.uninstall(configId);
    }

    protected void deleteDir(File packedConfigurationDir) {
        FileUtils.recursiveDelete(packedConfigurationDir);
    }

    protected DirectoryPackager newDirectoryPackager() {
        return new ZipDirectoryPackager();
    }

    public static final String GBEAN_J2EE_TYPE = "ClusterConfigurationStore";
    public static final String GBEAN_REF_CONF_STORE = "ConfigurationStore";
}
