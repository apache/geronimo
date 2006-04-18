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

/**
 * Various metadata on a configuration that's used when listing, importing,
 * and exporting configurations.
 *
 * @version $Rev: 46019 $ $Date: 2004-09-14 05:56:06 -0400 (Tue, 14 Sep 2004) $
 */
public class ConfigurationMetadata implements Serializable, Comparable {
    private final Artifact configId;
    private final String name;
    private final String description;
    private final String category;
    private final boolean installed;
    private final boolean eligible;
    private String[] dependencies;
    private String[] obsoletes;
    private License[] licenses;
    private String[] geronimoVersions;
    private String[] jvmVersions;
    private String[] forceStart;
    private Prerequisite[] prerequisites;

    public ConfigurationMetadata(Artifact configId, String name, String description, String category, boolean installed, boolean eligible) {
        this.configId = configId;
        this.name = name;
        this.description = description;
        this.category = category;
        this.installed = installed;
        this.eligible = eligible;
    }

    public void setDependencies(String[] dependencies) {
        this.dependencies = dependencies;
    }

    public void setObsoletes(String[] obsoletes) {
        this.obsoletes = obsoletes;
    }

    public void setForceStart(String[] forceStart) {
        this.forceStart = forceStart;
    }

    /**
     * Gets the Config ID for this configuration, which is a globally unique
     * identifier.
     */
    public Artifact getConfigId() {
        return configId;
    }

    /**
     * Gets a human-readable name for this configuration.
     */
    public String getName() {
        return name;
    }

    /**
     * Gets a description of this configuration and why it is interesting
     */
    public String getDescription() {
        return description;
    }

    /**
     * Gets a category name for this configuration.  In a list, configurations
     * in the same category will be listed together.  There are no specific
     * allowed values, though each repository may have standards for that.
     */
    public String getCategory() {
        return category;
    }

    public boolean isInstalled() {
        return installed;
    }

    public String getVersion() {
        return configId.getVersion() == null ? "unknown version" : configId.getVersion().toString();
    }

    /**
     * Gets the JAR or configuration dependencies for this configuration,  Each
     * String in the result is an Artifact (or Config ID) in String form.
     * Generally speaking, the dependency names may be partial artifact names
     * (but not, for example, if this whole thing is a plugin list).
     */
    public String[] getDependencies() {
        return dependencies;
    }

    /**
     * Gets the configurations obsoleted by this configuration.  Each
     * String in the result is an Artifact (or Config ID) in String form.
     */
    public String[] getObsoletes() {
        return obsoletes;
    }

    /**
     * Gets the configurations that should definitely be started when the
     * install process completes.
     */
    public String[] getForceStart() {
        return forceStart;
    }

    public String[] getGeronimoVersions() {
        return geronimoVersions;
    }

    public void setGeronimoVersions(String[] geronimoVersions) {
        this.geronimoVersions = geronimoVersions;
    }

    public License[] getLicenses() {
        return licenses;
    }

    public void setLicenses(License[] licenses) {
        this.licenses = licenses;
    }

    public String[] getJvmVersions() {
        return jvmVersions;
    }

    public void setJvmVersions(String[] jdkVersions) {
        this.jvmVersions = jdkVersions;
    }

    public Prerequisite[] getPrerequisites() {
        return prerequisites;
    }

    public void setPrerequisites(Prerequisite[] prerequisites) {
        this.prerequisites = prerequisites;
    }

    public boolean isEligible() {
        return eligible;
    }


    public int compareTo(Object o) {
        ConfigurationMetadata other = (ConfigurationMetadata) o;
        int test = category.compareTo(other.category);
        if(test != 0) return test;
        test = name.compareTo(other.name);

        return test;
    }

    public static class License implements Serializable {
        private final String name;
        private final boolean osiApproved;

        public License(String name, boolean osiApproved) {
            this.name = name;
            this.osiApproved = osiApproved;
        }

        public String getName() {
            return name;
        }

        public boolean isOsiApproved() {
            return osiApproved;
        }
    }

    public static class Prerequisite implements Serializable {
        private final Artifact configId;
        private final String resourceType;
        private final String description;
        private final boolean present;

        public Prerequisite(Artifact configId, boolean present) {
            this.configId = configId;
            this.present = present;
            resourceType = null;
            description = null;
        }

        public Prerequisite(Artifact configId, boolean present, String resourceType, String description) {
            this.configId = configId;
            this.present = present;
            this.resourceType = resourceType;
            this.description = description;
        }

        public Artifact getConfigId() {
            return configId;
        }

        public String getResourceType() {
            return resourceType;
        }

        public String getDescription() {
            return description;
        }

        public boolean isPresent() {
            return present;
        }

        public String getConfigIdWithStars() {
            StringBuffer buf = new StringBuffer();
            if(configId.getGroupId() == null) {
                buf.append("*");
            } else {
                buf.append(configId.getGroupId());
            }
            buf.append("/");
            if(configId.getArtifactId() == null) {
                buf.append("*");
            } else {
                buf.append(configId.getArtifactId());
            }
            buf.append("/");
            if(configId.getVersion() == null) {
                buf.append("*");
            } else {
                buf.append(configId.getVersion());
            }
            buf.append("/");
            if(configId.getType() == null) {
                buf.append("*");
            } else {
                buf.append(configId.getType());
            }
            return buf.toString();
        }
    }
}
