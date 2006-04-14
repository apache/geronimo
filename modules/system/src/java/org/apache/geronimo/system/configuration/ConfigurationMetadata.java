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
import org.apache.geronimo.kernel.repository.Artifact;
import org.apache.geronimo.kernel.repository.Dependency;

/**
 * Various metadata on a configuration that's used when listing, importing,
 * and exporting configurations.
 *
 * @version $Rev: 46019 $ $Date: 2004-09-14 05:56:06 -0400 (Tue, 14 Sep 2004) $
 */
public class ConfigurationMetadata implements Serializable, Comparable {
    private Artifact configId;
    private String description;
    private String category;
    private boolean installed;
    private Dependency[] dependencies;
    private boolean eligible;
    private String[] geronimoVersions;
    private String[] prerequisites;

    public ConfigurationMetadata(Artifact configId, String description, String category, boolean installed, boolean eligible) {
        this.configId = configId;
        this.description = description;
        this.category = category;
        this.installed = installed;
        this.eligible = eligible;
    }

    public void setDependencies(Dependency[] dependencies) {
        this.dependencies = dependencies;
    }

    public Artifact getConfigId() {
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
    public Dependency[] getDependencies() {
        return dependencies;
    }

    public String[] getGeronimoVersions() {
        return geronimoVersions;
    }

    public void setGeronimoVersions(String[] geronimoVersions) {
        this.geronimoVersions = geronimoVersions;
    }

    public String[] getPrerequisites() {
        return prerequisites;
    }

    public void setPrerequisites(String[] prerequisites) {
        this.prerequisites = prerequisites;
    }

    public boolean isEligible() {
        return eligible;
    }


    public int compareTo(Object o) {
        ConfigurationMetadata other = (ConfigurationMetadata) o;
        int test = category.compareTo(other.category);
        if(test != 0) return test;
        test = description.compareTo(other.description);

        return test;
    }
}
