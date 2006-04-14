/**
 *
 * Copyright 2006 The Apache Software Foundation
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

package org.apache.geronimo.kernel.repository;

import java.io.Serializable;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.List;
import java.util.Collections;
import java.util.ArrayList;
import java.util.Iterator;

/**
 * holds the data from the EnvironmentType xml while it is being resolved, transitively closed, etc.
 *
 * @version $Rev:$ $Date:$
 */
public class Environment implements Serializable {
    private static final long serialVersionUID = 7075760873629376317L;

    private Artifact configId;

    private final LinkedHashSet dependencies = new LinkedHashSet();

    private final Set hiddenClasses = new HashSet();
    private final Set nonOverrideableClasses = new HashSet();

    private boolean inverseClassLoading;
    private boolean suppressDefaultEnvironment;

    public Environment() {
    }

    public Environment(Artifact configId) {
        this.configId = configId;
    }

    public Environment(Environment environment) {
        this.configId = environment.getConfigId();
        this.dependencies.addAll(environment.dependencies);
        this.hiddenClasses.addAll(environment.getHiddenClasses());
        this.nonOverrideableClasses.addAll(environment.getNonOverrideableClasses());
        this.inverseClassLoading = environment.isInverseClassLoading();
        this.suppressDefaultEnvironment = environment.isSuppressDefaultEnvironment();
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
    public List getDependencies() {
        return Collections.unmodifiableList(new ArrayList(dependencies));
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

    /**
     * todo: I should be documented so it's not completely unclear what kind of
     * elements I hold.
     */
    public Set getHiddenClasses() {
        return hiddenClasses;
    }

    public void addHiddenClasses(Collection hiddenClasses) {
        this.hiddenClasses.addAll(hiddenClasses);
    }

    public void setHiddenClasses(Collection hiddenClasses) {
        this.hiddenClasses.clear();
        addHiddenClasses(hiddenClasses);
    }

    /**
     * todo: I should be documented so it's not completely unclear what kind of
     * elements I hold.
     */
    public Set getNonOverrideableClasses() {
        return nonOverrideableClasses;
    }

    public void addNonOverrideableClasses(Collection nonOverrideableClasses) {
        this.nonOverrideableClasses.addAll(nonOverrideableClasses);
    }

    public void setNonOverrideableClasses(Collection nonOverrideableClasses) {
        this.nonOverrideableClasses.clear();
        addNonOverrideableClasses(nonOverrideableClasses);
    }

    public boolean isInverseClassLoading() {
        return inverseClassLoading;
    }

    public void setInverseClassLoading(boolean inverseClassLoading) {
        this.inverseClassLoading = inverseClassLoading;
    }

    public boolean isSuppressDefaultEnvironment() {
        return suppressDefaultEnvironment;
    }

    public void setSuppressDefaultEnvironment(boolean suppressDefaultEnvironment) {
        this.suppressDefaultEnvironment = suppressDefaultEnvironment;
    }

}
