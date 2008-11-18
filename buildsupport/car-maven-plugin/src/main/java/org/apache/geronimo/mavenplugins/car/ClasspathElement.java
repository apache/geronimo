/**
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

package org.apache.geronimo.mavenplugins.car;

/**
 * Represents a Maven-artifact with additional classpath prefix details to build a
 * jar's Manifest Class-Path.
 *
 * @version $Rev:385659 $ $Date$
 */
public class ClasspathElement
{
    /**
     * Group Id of artifact.
     *
     * @parameter
     * @required
     */
    private String groupId;

    /**
     * Name of artifact.
     *
     * @parameter
     * @required
     */
    private String artifactId;

    /**
     * Version of artifact.
     *
     * @parameter
     */
    private String version = null;

    /**
     * Type of artifact.
     *
     * @parameter
     * @required
     */
    private String type = "jar";

    /**
     * Classifier for artifact.
     *
     * @parameter
     */
    private String classifier;

    /**
     * Prefix to be prepended to the artifact, like <tt>../lib</tt>.
     *
     * @parameter
     */
    private String classpathPrefix;

    /**
     * Entry name used in replacement for ArtifactItem that is not resolved
     *
     * @parameter
     */
    private String entry;

    /**
     * @return Returns the artifactId.
     */
    public String getArtifactId() {
        return artifactId;
    }

    /**
     * @param artifactId The artifactId to set.
     */
    public void setArtifactId(final String artifactId) {
        this.artifactId = artifactId;
    }

    /**
     * @return Returns the groupId.
     */
    public String getGroupId() {
        return groupId;
    }

    /**
     * @param groupId The groupId to set.
     */
    public void setGroupId(final String groupId) {
        this.groupId = groupId;
    }

    /**
     * @return Returns the type.
     */
    public String getType() {
        return type;
    }

    /**
     * @param type The type to set.
     */
    public void setType(final String type) {
        this.type = type;
    }

    /**
     * @return Returns the version.
     */
    public String getVersion() {
        return version;
    }

    /**
     * @param version The version to set.
     */
    public void setVersion(final String version) {
        this.version = version;
    }

    /**
     * @return Classifier.
     */
    public String getClassifier() {
        return classifier;
    }

    /**
     * @param classifier Classifier.
     */
    public void setClassifier(final String classifier) {
        this.classifier = classifier;
    }

    /**
     * @return Returns the classpath prefix.
     */
    public String getClasspathPrefix() {
        return classpathPrefix;
    }

    /**
     * @param classpathPrefix   The classpath prefix
     */
    public void setClasspathPrefix(final String classpathPrefix) {
        this.classpathPrefix = classpathPrefix;
    }

    public String getEntry() {
        return entry;
    }

    public void setEntry(final String entry) {
        this.entry = entry;
    }

    public String toString() {
        return classpathPrefix + "::" + groupId + ":" + artifactId + ":" + classifier + ":" + version + ":" + type + "::" + entry;
    }


}