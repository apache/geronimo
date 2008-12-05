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

package org.apache.geronimo.deployment.service;

import java.util.Set;

import junit.framework.TestCase;

import org.apache.geronimo.deployment.xbeans.ClassFilterType;
import org.apache.geronimo.deployment.xbeans.EmptyType;
import org.apache.geronimo.deployment.xbeans.EnvironmentType;
import org.apache.geronimo.kernel.repository.ClassLoadingRule;
import org.apache.geronimo.kernel.repository.ClassLoadingRules;

/**
 *
 * @version $Rev:$ $Date:$
 */
public class ClassLoadingRulesUtilTest extends TestCase {

    public void testConfiguration() throws Exception {
        EnvironmentType environmentType = EnvironmentType.Factory.newInstance();
        environmentType.setInverseClassloading(EmptyType.Factory.newInstance());
        environmentType.setHiddenClasses(newFilter("hidden"));
        environmentType.setNonOverridableClasses(newFilter("nonOverrideable"));
        environmentType.setPrivateClasses(newFilter("private"));
        
        ClassLoadingRules classLoadingRules = new ClassLoadingRules();
        ClassLoadingRulesUtil.configureRules(classLoadingRules, environmentType);
        
        assertTrue(classLoadingRules.isInverseClassLoading());
        assertPrefix(classLoadingRules.getHiddenRule(), "hidden");
        assertPrefix(classLoadingRules.getNonOverrideableRule(), "nonOverrideable");
        assertPrefix(classLoadingRules.getPrivateRule(), "private");
    }

    private void assertPrefix(ClassLoadingRule classLoadingRule, String filter) {
        Set<String> classPrefixes = classLoadingRule.getClassPrefixes();
        assertEquals(1, classPrefixes.size());
        assertTrue(classPrefixes.contains(filter));
    }

    private ClassFilterType newFilter(String filter) {
        ClassFilterType hiddenClasses = ClassFilterType.Factory.newInstance();
        hiddenClasses.addFilter(filter);
        return hiddenClasses;
    }
    
}
