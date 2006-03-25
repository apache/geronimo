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
import java.net.URI;

/**
 * Various metadata on a configuration that's used when listing, importing,
 * and exporting configurations.
 *
 * @version $Rev: 46019 $ $Date: 2004-09-14 05:56:06 -0400 (Tue, 14 Sep 2004) $
 */
public class ConfigurationMetadata implements Serializable {
    private URI configId;
    private String description;
    private String category;
    private boolean installed;
    private URI[] parents;
    private URI[] dependencies;

    public ConfigurationMetadata(URI configId, String description, String category, boolean installed) {
        this.configId = configId;
        this.description = description;
        this.category = category;
        this.installed = installed;
    }

    public void setParents(URI[] parents) {
        this.parents = parents;
    }

    public void setDependencies(URI[] dependencies) {
        this.dependencies = dependencies;
    }

    public URI getConfigId() {
        return configId;
    }

    public String getDescription() {
        return description;
    }

    public String getCategory() {
        return category;
    }

    public boolean isInstalled() {
        return installed;
    }

    public String getVersion() {
        String[] parts = configId.toString().split("/");
        if(parts.length == 4) {
            return parts[2];
        }
        return "unknown version";
    }

    /**
     * Note: if null, this information has not yet been loaded from the repository
     */
    public URI[] getParents() {
        return parents;
    }

    /**
     * Note: if null, this information has not yet been loaded from the repository
     */
    public URI[] getDependencies() {
        return dependencies;
    }
}
