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

package org.apache.geronimo.kernel.repository;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;

/**
 * holds the data from the EnvironmentType xml while it is being resolved, transitively closed, etc.
 *
 * @version $Rev$ $Date$
 */
public class Environment implements Serializable {
    private static final long serialVersionUID = 7075760873629376317L;

    private Artifact configId;
    private final LinkedHashSet dependencies = new LinkedHashSet();
    private final ClassLoadingRules classLoadingRules;
    private boolean suppressDefaultEnvironment;

    public Environment() {
        classLoadingRules = new ClassLoadingRules();
    }

    public Environment(Artifact configId) {
        this.configId = configId;

        classLoadingRules = new ClassLoadingRules();
    }

    public Environment(Environment environment) {
        configId = environment.getConfigId();
        dependencies.addAll(environment.dependencies);
        suppressDefaultEnvironment = environment.isSuppressDefaultEnvironment();
        classLoadingRules = environment.classLoadingRules;
    }

    public Artifact getConfigId() {
        return configId;
    }

    public void setConfigId(Artifact configId) {
        this.configId = configId;
    }

    /**
     * Gets a List (with elements of type Dependency) of the configuration and
     * JAR dependencies of this configuration.
     *
     * @see Dependency
     */
    public List<Dependency> getDependencies() {
        return Collections.unmodifiableList(new ArrayList<Dependency>(dependencies));
    }

    public void addDependency(Artifact artifact, ImportType importType) {
        this.dependencies.add(new Dependency(artifact, importType));
    }

    public void addDependency(Dependency dependency) {
        this.dependencies.add(dependency);
    }

    public void addDependencies(Collection dependencies) {
        for (Iterator iterator = dependencies.iterator(); iterator.hasNext();) {
            // make sure they are all dependency objects... generics would be sooooo nice
            Dependency dependency = (Dependency) iterator.next();
            addDependency(dependency);
        }
    }

    public void setDependencies(Collection dependencies) {
        this.dependencies.clear();
        addDependencies(dependencies);
    }

    public ClassLoadingRules getClassLoadingRules() {
        return classLoadingRules;
    }

    public boolean isSuppressDefaultEnvironment() {
        return suppressDefaultEnvironment;
    }

    public void setSuppressDefaultEnvironment(boolean suppressDefaultEnvironment) {
        this.suppressDefaultEnvironment = suppressDefaultEnvironment;
    }

}
