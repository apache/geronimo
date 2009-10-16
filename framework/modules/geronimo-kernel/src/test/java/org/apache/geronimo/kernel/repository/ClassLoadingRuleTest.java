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

import java.util.Collections;
import java.util.Set;

import junit.framework.TestCase;

/**
 *
 * @version $Rev:$ $Date:$
 */
public class ClassLoadingRuleTest extends TestCase {
    private static final String FILTERED_PREFIX = "org.apache.geronimo";
    private static final String FILTERED_RESOURCE_PREFIX = "org/apache/geronimo";

    private ClassLoadingRule rule;

    @Override
    protected void setUp() throws Exception {
        rule = new ClassLoadingRule();
        Set<String> filter = Collections.singleton(FILTERED_PREFIX);
        rule.addClassPrefixes(filter);
    }
    
    public void testIsFilteredClass() throws Exception {
        assertTrue(rule.isFilteredClass(FILTERED_PREFIX + ".mock"));
    }
    
    public void testIsNotFilteredClass() throws Exception {
        assertFalse(rule.isFilteredClass("mock"));
    }
    
    public void testIsFilteredResource() throws Exception {
        assertTrue(rule.isFilteredResource(FILTERED_RESOURCE_PREFIX + "/mock"));
    }
    
    public void testIsNotFilteredResource() throws Exception {
        assertFalse(rule.isFilteredResource("mock"));
    }
    
    public void testMerge() throws Exception {
        ClassLoadingRule ruleToMerge = new ClassLoadingRule();
        String mergedFilteredPrefix = "geronimo";
        Set<String> filter = Collections.singleton(mergedFilteredPrefix);
        ruleToMerge.addClassPrefixes(filter);

        rule.merge(ruleToMerge);
        
        assertTrue(rule.isFilteredClass(mergedFilteredPrefix + ".mock"));
        assertTrue(rule.isFilteredResource(mergedFilteredPrefix + "/mock"));
    }
    
    public void testSetClassPrefixResetState() throws Exception {
        String newFilteredPrefix = "geronimo";
        Set<String> filter = Collections.singleton(newFilteredPrefix);
        rule.setClassPrefixes(filter);
        
        assertTrue(rule.isFilteredClass(newFilteredPrefix + ".mock"));
        assertTrue(rule.isFilteredResource(newFilteredPrefix + "/mock"));

        assertFalse(rule.isFilteredClass(FILTERED_PREFIX + ".mock"));
        assertFalse(rule.isFilteredResource(FILTERED_PREFIX + "/mock"));
    }
    
}
