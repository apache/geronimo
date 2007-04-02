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

import org.codehaus.mojo.pluginsupport.util.ArtifactItem;

/**
 * Represents a Maven-artifact with additional classpath prefix details to build a
 * jar's Manifest Class-Path.
 *
 * @version $Rev:385659 $ $Date$
 */
public class ClasspathElement
    extends ArtifactItem
{
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
}