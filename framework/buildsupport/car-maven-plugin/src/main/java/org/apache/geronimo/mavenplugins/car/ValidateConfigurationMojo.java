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


package org.apache.geronimo.mavenplugins.car;

import java.util.Collections;
import java.util.List;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;

/**
 * Check that all dependencies mentioned explicitly in the car-maven-plugin configuration are present as maven dependencies.
 *
 * @goal validate-configuration
 * @requiresDependencyResolution compile
 *
 * @version $Rev$ $Date$
 */
public class ValidateConfigurationMojo extends AbstractCarMojo {

    /**
     * Dependencies explicitly listed in the car-maven-plugin configuration
     *
     * @parameter
     */
    private List<Dependency> dependencies = Collections.emptyList();

    public void execute() throws MojoExecutionException, MojoFailureException {
        for (Dependency dependency: dependencies) {
            checkForMatch(dependency);
        }
    }

    private void checkForMatch(Dependency dependency) {
        for (Object o: getProject().getDependencies())  {
            org.apache.maven.model.Dependency test = (org.apache.maven.model.Dependency) o;
            if (matches(test, dependency)) {
                return;
            }
        }
        throw new IllegalStateException("No match for dependency: " + dependency);
    }

    private boolean matches(org.apache.maven.model.Dependency test, Dependency dependency) {
        if (dependency.getGroupId() != null && !dependency.getGroupId().equals(test.getGroupId())) {
            return false;
        }
        if (dependency.getArtifactId() != null && !dependency.getArtifactId().equals(test.getArtifactId())) {
            return false;
        }
        if (dependency.getVersion() != null && !dependency.getVersion().equals(test.getVersion())) {
            return false;
        }
        if (dependency.getType() != null && !dependency.getType().equals(test.getType())) {
            return false;
        }
        return true;
    }
}
