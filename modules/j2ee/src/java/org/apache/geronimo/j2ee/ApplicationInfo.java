/**
 *
 * Copyright 2004 The Apache Software Foundation
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
package org.apache.geronimo.j2ee.dd;

import java.net.URI;
import java.util.Set;

import org.apache.geronimo.kernel.config.ConfigurationModuleType;

/**
 * @version $Revision$ $Date$
 */
public class ApplicationInfo {
    private ConfigurationModuleType type;
    private URI configId;
    private URI parentId;
    private String applicationName;
    private Set modules;
    private Set moduleLocations;
    private String specDD;

    public ApplicationInfo() {
    }

    public ApplicationInfo(ConfigurationModuleType type, URI configId, URI parentId, String applicationName, Set modules, Set moduleLocations, String specDD) {
        this.type = type;
        this.configId = configId;
        this.parentId = parentId;
        this.applicationName = applicationName;
        this.modules = modules;
        this.moduleLocations = moduleLocations;
        this.specDD = specDD;
    }

    public ConfigurationModuleType getType() {
        return type;
    }

    public void setType(ConfigurationModuleType type) {
        this.type = type;
    }

    public URI getConfigId() {
        return configId;
    }

    public void setConfigId(URI configId) {
        this.configId = configId;
    }

    public URI getParentId() {
        return parentId;
    }

    public void setParentId(URI parentId) {
        this.parentId = parentId;
    }

    public String getApplicationName() {
        return applicationName;
    }

    public void setApplicationName(String applicationName) {
        this.applicationName = applicationName;
    }

    public Set getModules() {
        return modules;
    }

    public void setModules(Set modules) {
        this.modules = modules;
    }

    public Set getModuleLocations() {
        return moduleLocations;
    }

    public void setModuleLocations(Set moduleLocations) {
        this.moduleLocations = moduleLocations;
    }

    public String getSpecDD() {
        return specDD;
    }

    public void setSpecDD(String specDD) {
        this.specDD = specDD;
    }
}
