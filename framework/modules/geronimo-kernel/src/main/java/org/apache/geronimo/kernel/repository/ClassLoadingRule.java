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

package org.apache.geronimo.kernel.repository;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

/**
 *
 * @version $Rev:$ $Date:$
 */
public class ClassLoadingRule implements Serializable {
    private final Set<String> classPrefixes;
    private final Set<String> resourcePrefixes;
    
    public ClassLoadingRule() {
        classPrefixes = new HashSet<String>();
        resourcePrefixes = new HashSet<String>();
    }

    public Set<String> getClassPrefixes() {
        return classPrefixes;
    }

    public boolean isFilteredClass(String name) {
        return isMatching(classPrefixes, name);
    }
    
    public boolean isFilteredResource(String name) {
        return isMatching(resourcePrefixes, name);
    }
    
    public void addClassPrefixes(Set<String> classPrefixes) {
        this.classPrefixes.addAll(classPrefixes);

        Set<String> resources = toResources(classPrefixes);
        resourcePrefixes.addAll(resources);
    }

    public void setClassPrefixes(Set<String> classPrefixes) {
        this.classPrefixes.clear();
        resourcePrefixes.clear();
        addClassPrefixes(classPrefixes);
    }
    
    public void merge(ClassLoadingRule classLoadingRuleToMerge) {
        addClassPrefixes(classLoadingRuleToMerge.classPrefixes);
    }

    protected Set<String> toResources(Set<String> classPrefixes) {
        Set<String> resources = new HashSet<String>();
        for (String className : classPrefixes) {
            resources.add(className.replace('.', '/'));
        }
        return resources;
    }
    
    protected boolean isMatching(Set<String> prefixes, String name) {
        for (String prefix : prefixes) {
            if (name.startsWith(prefix)) {
                return true;
            }
        }
        return false;
    }

}