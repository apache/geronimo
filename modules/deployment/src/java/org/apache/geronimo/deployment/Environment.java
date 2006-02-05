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

package org.apache.geronimo.deployment;

import org.apache.geronimo.kernel.repository.Artifact;

import java.util.Map;
import java.util.HashMap;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.HashSet;

/**
 * holds the data from the EnvironmentType xml while it is being resolved, transitively closed, etc.
 * @version $Rev:$ $Date:$
 */
public class Environment {

    private Artifact configId;

    private Map nameKeys = new HashMap();

    private  LinkedHashSet imports;
    private  LinkedHashSet references;
    private  LinkedHashSet dependencies;
    private  LinkedHashSet includes;

    private  Set hiddenClasses;
    private  Set nonOverrideableClasses;

    private boolean inverseClassloading;

    public Environment(Artifact configId, Map nameKeys, LinkedHashSet imports, LinkedHashSet references, LinkedHashSet dependencies, LinkedHashSet includes, Set hiddenClasses, Set nonOverrideableClasses, boolean inverseClassloading) {
        this.configId = configId;
        this.nameKeys = nameKeys;
        this.imports = imports;
        this.references = references;
        this.dependencies = dependencies;
        this.includes = includes;
        this.hiddenClasses = hiddenClasses;
        this.nonOverrideableClasses = nonOverrideableClasses;
        this.inverseClassloading = inverseClassloading;
    }

    public Artifact getConfigId() {
        return configId;
    }

    public void setConfigId(Artifact configId) {
        this.configId = configId;
    }

    public Map getNameKeys() {
        return nameKeys;
    }

    public void setNameKeys(Map nameKeys) {
        this.nameKeys = nameKeys;
    }

    public LinkedHashSet getImports() {
        return imports;
    }

    public void setImports(LinkedHashSet imports) {
        this.imports = imports;
    }

    public LinkedHashSet getReferences() {
        return references;
    }

    public void setReferences(LinkedHashSet references) {
        this.references = references;
    }

    public LinkedHashSet getDependencies() {
        return dependencies;
    }

    public void setDependencies(LinkedHashSet dependencies) {
        this.dependencies = dependencies;
    }

    public LinkedHashSet getIncludes() {
        return includes;
    }

    public void setIncludes(LinkedHashSet includes) {
        this.includes = includes;
    }

    public Set getHiddenClasses() {
        return hiddenClasses;
    }

    public void setHiddenClasses(Set hiddenClasses) {
        this.hiddenClasses = hiddenClasses;
    }

    public Set getNonOverrideableClasses() {
        return nonOverrideableClasses;
    }

    public void setNonOverrideableClasses(Set nonOverrideableClasses) {
        this.nonOverrideableClasses = nonOverrideableClasses;
    }

    public boolean isInverseClassloading() {
        return inverseClassloading;
    }

    public void setInverseClassloading(boolean inverseClassloading) {
        this.inverseClassloading = inverseClassloading;
    }
}
