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
package org.apache.geronimo.system.plugin;

import java.io.Serializable;
import java.net.URL;
import java.util.List;
import java.util.ArrayList;
import java.util.Set;
import java.util.HashSet;
import org.apache.geronimo.kernel.repository.Artifact;

/**
 * Metadata on a list of configurations available in a remote server.
 *
 * @version $Rev$ $Date$
 */
public class PluginList implements Serializable {
    private final URL[] repositories;
    private final PluginMetadata[] plugins;

    public PluginList(URL[] repositories, PluginMetadata[] plugins) {
        this.repositories = repositories;
        this.plugins = plugins;
    }

    public static PluginList createInstallList(PluginList all, Artifact[] selectedConfigIDs) {
        URL[] repositories = all.repositories;
        List list = new ArrayList();
        Set set = new HashSet();
        for (int i = 0; i < selectedConfigIDs.length; i++) {
            Artifact artifact = selectedConfigIDs[i];
            set.add(artifact);
        }
        for (int i = 0; i < all.getPlugins().length; i++) {
            PluginMetadata metadata = all.getPlugins()[i];
            if(set.contains(metadata.getModuleId())) {
                if(metadata.isInstalled() || !metadata.isEligible()) {
                    throw new IllegalArgumentException("Cannot install "+metadata.getModuleId());
                }
                list.add(metadata);
            }
        }
        if(list.size() == 0) {
            return null;
        }
        PluginMetadata[] configurations = (PluginMetadata[]) list.toArray(new PluginMetadata[list.size()]);
        return new PluginList(repositories, configurations);
    }

    public static PluginList createInstallList(PluginList all, Artifact selectedConfigID) {
        URL[] repositories = all.repositories;
        PluginMetadata target = null;
        for (int i = 0; i < all.getPlugins().length; i++) {
            PluginMetadata metadata = all.getPlugins()[i];
            if(selectedConfigID.equals(metadata.getModuleId())) {
                if(metadata.isInstalled() || !metadata.isEligible()) {
                    throw new IllegalArgumentException("Cannot install "+metadata.getModuleId());
                }
                target = metadata;
                break;
            }
        }
        if(target == null) {
            return null;
        }
        return new PluginList(repositories, new PluginMetadata[]{target});
    }

    public URL[] getRepositories() {
        return repositories;
    }

    public PluginMetadata[] getPlugins() {
        return plugins;
    }
}
