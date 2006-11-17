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
import org.apache.geronimo.kernel.repository.Artifact;
import org.apache.geronimo.system.configuration.GBeanOverride;

/**
 * Various metadata on a configuration that's used when listing, importing,
 * and exporting configurations.
 *
 * @version $Rev$ $Date$
 */
public class PluginMetadata implements Serializable, Comparable {
    private final String name;
    private final Artifact moduleId;
    private final String category;
    private final String description;
    private final String pluginURL;
    private final String author;
    private License[] licenses = new License[0];
    private final Hash hash;
    private String[] geronimoVersions = new String[0];
    private String[] jvmVersions = new String[0];
    private Prerequisite[] prerequisites = new Prerequisite[0];
    private String[] dependencies = new String[0];
    private String[] forceStart = new String[0];
    private String[] obsoletes = new String[0];
    private URL[] repositories = new URL[0];
    private CopyFile[] filesToCopy = new CopyFile[0];
    private GBeanOverride[] configXmls = new GBeanOverride[0];

    private final boolean installed;
    private final boolean eligible;

    public PluginMetadata(String name, Artifact moduleId, String category, String description, String pluginURL, String author, Hash hash, boolean installed, boolean eligible) {
        this.name = name;
        this.moduleId = moduleId;
        this.category = category;
        this.description = description;
        this.pluginURL = pluginURL;
        this.author = author;
        this.hash = hash;
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
    public Artifact getModuleId() {
        return moduleId;
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
     * Gets a description of this module in HTML format (with paragraph
     * markers).
     */
    public String getHTMLDescription() {
        String[] paras = splitParas(description);
        StringBuffer buf = new StringBuffer();
        for (int i = 0; i < paras.length; i++) {
            String para = paras[i];
            buf.append("<p>").append(para).append("</p>\n");
        }
        return buf.toString();
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
        return moduleId.getVersion() == null ? "unknown version" : moduleId.getVersion().toString();
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

    public String getAuthor() {
        return author;
    }

    public Hash getHash() {
        return hash;
    }

    public String getPluginURL() {
        return pluginURL;
    }

    public URL[] getRepositories() {
        return repositories;
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

    public void setRepositories(URL[] repositories) {
        this.repositories = repositories;
    }

    public void setPrerequisites(Prerequisite[] prerequisites) {
        this.prerequisites = prerequisites;
    }

    public boolean isEligible() {
        return eligible;
    }

    /**
     * Gets a list of files to copy from the plugin CAR into the server installation.
     */
    public CopyFile[] getFilesToCopy() {
        return filesToCopy;
    }

    public void setFilesToCopy(CopyFile[] filesToCopy) {
        this.filesToCopy = filesToCopy;
    }

    /**
     * Gets a list of settings to populate in config.xml
     */
    public GBeanOverride[] getConfigXmls() {
        return configXmls;
    }

    public void setConfigXmls(GBeanOverride[] configXmls) {
        this.configXmls = configXmls;
    }

    public int compareTo(Object o) {
        PluginMetadata other = (PluginMetadata) o;
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

    public static class Hash implements Serializable {
        private final String type; // MD5 or SHA-1
        private final String value;

        public Hash(String type, String value) {
            this.type = type;
            this.value = value;
        }

        public String getType() {
            return type;
        }

        public String getValue() {
            return value;
        }
    }

    public static class CopyFile implements Serializable {
        private final boolean relativeToVar;  // if not, relative to the Geronimo install directory
        private final String sourceFile;
        private final String destDir;

        public CopyFile(boolean relativeToVar, String sourceFile, String destDir) {
            this.relativeToVar = relativeToVar;
            this.sourceFile = sourceFile;
            this.destDir = destDir;
        }

        public boolean isRelativeToVar() {
            return relativeToVar;
        }

        public String getSourceFile() {
            return sourceFile;
        }

        public String getDestDir() {
            return destDir;
        }
    }

    public static class Prerequisite implements Serializable {
        private final Artifact moduleId;
        private final String resourceType;
        private final String description;
        private final boolean present;

        public Prerequisite(Artifact moduleId, boolean present) {
            this.moduleId = moduleId;
            this.present = present;
            resourceType = null;
            description = null;
        }

        public Prerequisite(Artifact moduleId, boolean present, String resourceType, String description) {
            this.moduleId = moduleId;
            this.present = present;
            this.resourceType = resourceType;
            this.description = description;
        }

        public Artifact getModuleId() {
            return moduleId;
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

        public String getModuleIdWithStars() {
            StringBuffer buf = new StringBuffer();
            if(moduleId.getGroupId() == null) {
                buf.append("*");
            } else {
                buf.append(moduleId.getGroupId());
            }
            buf.append("/");
            if(moduleId.getArtifactId() == null) {
                buf.append("*");
            } else {
                buf.append(moduleId.getArtifactId());
            }
            buf.append("/");
            if(moduleId.getVersion() == null) {
                buf.append("*");
            } else {
                buf.append(moduleId.getVersion());
            }
            buf.append("/");
            if(moduleId.getType() == null) {
                buf.append("*");
            } else {
                buf.append(moduleId.getType());
            }
            return buf.toString();
        }
    }

    private static String[] splitParas(String desc) {
        int start = 0, last=0;
        List list = new ArrayList();
        boolean inSpace = false, multiple = false;
        for(int i=0; i<desc.length(); i++) {
            char c = desc.charAt(i);
            if(inSpace) {
                if(Character.isWhitespace(c)) {
                    if(c == '\r' || c == '\n') {
                        multiple = true;
                        for(int j=i+1; j<desc.length(); j++) {
                            char d = desc.charAt(j);
                            if(d != c && (d == '\r' || d == '\n')) {
                                i = j;
                            } else {
                                break;
                            }
                        }
                    }
                } else {
                    if(multiple) {
                        list.add(desc.substring(last, start).trim());
                        last = i;
                    }
                    inSpace = false;
                }
            } else {
                if(c == '\r' || c == '\n') {
                    inSpace = true;
                    multiple = false;
                    start = i;
                    for(int j=i+1; j<desc.length(); j++) {
                        char d = desc.charAt(j);
                        if(d != c && (d == '\r' || d == '\n')) {
                            i = j;
                        } else {
                            break;
                        }
                    }
                }
            }
        }
        if(last < desc.length()) {
            list.add(desc.substring(last).trim());
        }
        return (String[]) list.toArray(new String[list.size()]);
    }
}
