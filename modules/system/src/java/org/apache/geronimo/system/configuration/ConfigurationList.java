/**
 *
 * Copyright 2005 The Apache Software Foundation
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.apache.geronimo.system.configuration;

import java.io.Serializable;
import java.net.URL;
import java.util.List;
import java.util.ArrayList;
import java.util.Set;
import java.util.HashSet;
import org.apache.geronimo.kernel.repository.Artifact;

/**
 * ReplaceMe
 *
 * @version $Rev: 46019 $ $Date: 2004-09-14 05:56:06 -0400 (Tue, 14 Sep 2004) $
 */
public class ConfigurationList implements Serializable {
    private final URL mainRepository;
    private final URL[] backupRepositories;
    private final ConfigurationMetadata[] configurations;

    public ConfigurationList(URL mainRepository, URL[] backupRepositories, ConfigurationMetadata[] configurations) {
        this.mainRepository = mainRepository;
        this.backupRepositories = backupRepositories;
        this.configurations = configurations;
    }

    public static ConfigurationList createInstallList(ConfigurationList all, Artifact[] selectedConfigIDs) {
        URL mainRepository = all.mainRepository;
        URL[] backupRepositories = all.backupRepositories;
        List list = new ArrayList();
        Set set = new HashSet();
        for (int i = 0; i < selectedConfigIDs.length; i++) {
            Artifact artifact = selectedConfigIDs[i];
            set.add(artifact);
        }
        for (int i = 0; i < all.getConfigurations().length; i++) {
            ConfigurationMetadata metadata = all.getConfigurations()[i];
            if(set.contains(metadata.getConfigId())) {
                if(metadata.isInstalled() || !metadata.isEligible()) {
                    throw new IllegalArgumentException("Cannot install "+metadata.getConfigId());
                }
                list.add(metadata);
            }
        }
        if(list.size() == 0) {
            return null;
        }
        ConfigurationMetadata[] configurations = (ConfigurationMetadata[]) list.toArray(new ConfigurationMetadata[list.size()]);
        return new ConfigurationList(mainRepository, backupRepositories, configurations);
    }

    public static ConfigurationList createInstallList(ConfigurationList all, Artifact selectedConfigID) {
        URL mainRepository = all.mainRepository;
        URL[] backupRepositories = all.backupRepositories;
        ConfigurationMetadata target = null;
        for (int i = 0; i < all.getConfigurations().length; i++) {
            ConfigurationMetadata metadata = all.getConfigurations()[i];
            if(selectedConfigID.equals(metadata.getConfigId())) {
                if(metadata.isInstalled() || !metadata.isEligible()) {
                    throw new IllegalArgumentException("Cannot install "+metadata.getConfigId());
                }
                target = metadata;
                break;
            }
        }
        if(target == null) {
            return null;
        }
        return new ConfigurationList(mainRepository, backupRepositories, new ConfigurationMetadata[]{target});
    }

    public URL getMainRepository() {
        return mainRepository;
    }

    public URL[] getBackupRepositories() {
        return backupRepositories;
    }

    public ConfigurationMetadata[] getConfigurations() {
        return configurations;
    }
}
