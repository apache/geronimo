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

import java.util.Collections;

import org.apache.geronimo.kernel.repository.ClassLoadingRule;
import org.apache.geronimo.kernel.repository.ClassLoadingRules;

import junit.framework.TestCase;

/**
 *
 * @version $Rev:$ $Date:$
 */
public class ChildrenConfigurationClassLoaderTest extends TestCase {

    private String privateResourceName;
    private String privateResourceClass;
    private ChildrenConfigurationClassLoader classLoader;
    private ClassLoadingRules rules;

    @Override
    protected void setUp() throws Exception {
        rules = new ClassLoadingRules();
        privateResourceClass = ChildrenConfigurationClassLoaderTest.class.getName();
        privateResourceName = privateResourceClass.replace(".", "/") + ".class";

        classLoader = new ChildrenConfigurationClassLoader(ChildrenConfigurationClassLoaderTest.class.getClassLoader(), rules);
    }

    public void testLoadClassThrowsCNFEForHiddenClass() throws Exception {
        classLoader.loadClass(privateResourceClass);

        addPrivateConfiguration();

        try {
            classLoader.loadClass(privateResourceClass);
            fail();
        } catch (ClassNotFoundException e) {
        }
    }
    
    public void testGetResourceReturnsNullForHiddenClass() throws Exception {
        assertNotNull(classLoader.getResource(privateResourceName));
        addPrivateConfiguration();
        assertNull(classLoader.getResource(privateResourceName));
    }
    
    public void testGetResourcesReturnsEmptyEnumForHiddenClass() throws Exception {
        assertTrue(classLoader.getResources(privateResourceName).hasMoreElements());
        addPrivateConfiguration();
        assertFalse(classLoader.getResources(privateResourceName).hasMoreElements());
    }

    private void addPrivateConfiguration() {
        ClassLoadingRule rule = rules.getPrivateRule();
        rule.addClassPrefixes(Collections.singleton(privateResourceClass));
    }

}
