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

package org.apache.geronimo.kernel.config;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.SecureClassLoader;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;

import org.apache.geronimo.kernel.repository.ClassLoadingRule;
import org.apache.geronimo.kernel.repository.ClassLoadingRules;

import sun.misc.CompoundEnumeration;

/**
 *
 * @version $Rev:$ $Date:$
 */
public class ChildrenConfigurationClassLoader extends SecureClassLoader {

    private final ClassLoadingRules rules;

    public ChildrenConfigurationClassLoader(ClassLoader parent, ClassLoadingRules rules) {
        super(parent);
        if (null == rules) {
            throw new IllegalArgumentException("rules is required");
        }
        this.rules = rules;
    }

    public Class<?> loadClass(String name, List<ClassLoader> visitedClassLoaders) throws ClassNotFoundException {
        return loadClass(name, false, visitedClassLoaders);
    }
    
    protected synchronized Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
        return loadClass(name, resolve, Collections.EMPTY_LIST);
    }

    protected synchronized Class<?> loadClass(String name, boolean resolve, List<ClassLoader> visitedClassLoaders)
            throws ClassNotFoundException {
        ClassLoadingRule privateRule = rules.getPrivateRule();
        ClassLoader parent = getParent();
        if (privateRule.isFilteredClass(name)) {
            throw new ClassNotFoundException(name + " is hidden by classloader " + parent);
        }
        
        if (parent instanceof MultiParentClassLoader) {
            try {
                return ((MultiParentClassLoader) parent).loadClassInternal(name, resolve, visitedClassLoaders);
            } catch (MalformedURLException e) {
            }
        }
        return super.loadClass(name, resolve);
    }
    
    public URL getResource(String name) {
        ClassLoadingRule privateRule = rules.getPrivateRule();
        if (privateRule.isFilteredResource(name)) {
            return null;
        }
        return super.getResource(name);
    }

    public Enumeration<URL> getResources(String name) throws IOException {
        ClassLoadingRule privateRule = rules.getPrivateRule();
        if (privateRule.isFilteredResource(name)) {
            return new CompoundEnumeration(new Enumeration[0]);
        }
        return super.getResources(name);
    }

}
