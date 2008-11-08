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

import java.util.HashSet;
import java.util.Set;

import org.apache.geronimo.deployment.xbeans.ClassFilterType;
import org.apache.geronimo.deployment.xbeans.EnvironmentType;
import org.apache.geronimo.kernel.repository.ClassLoadingRule;
import org.apache.geronimo.kernel.repository.ClassLoadingRules;

/**
 *
 * @version $Rev:$ $Date:$
 */
public final class ClassLoadingRulesUtil {

    private ClassLoadingRulesUtil() {
    }

    public static void configureRules(ClassLoadingRules classLoadingRules, EnvironmentType environmentType) {
        classLoadingRules.setInverseClassLoading(environmentType.isSetInverseClassloading());
        
        if (null != environmentType.getHiddenClasses()) {
            ClassLoadingRule hiddenRule = classLoadingRules.getHiddenRule();
            hiddenRule.setClassPrefixes(toFilters(environmentType.getHiddenClasses()));
        }
        
        if (null != environmentType.getNonOverridableClasses()) {
            ClassLoadingRule nonOverrideableRule = classLoadingRules.getNonOverrideableRule();
            nonOverrideableRule.setClassPrefixes(toFilters(environmentType.getNonOverridableClasses()));
        }
        
        if (null != environmentType.getPrivateClasses()) {
            ClassLoadingRule privateRule = classLoadingRules.getPrivateRule();
            privateRule.setClassPrefixes(toFilters(environmentType.getPrivateClasses()));
        }
    }
   
    private static Set<String> toFilters(ClassFilterType filterType) {
        Set<String> filters = new HashSet<String>();
        if (null != filterType) {
            String[] filterArray = filterType.getFilterArray();
            for (String filter : filterArray) {
                filter = filter.trim();
                filters.add(filter);
            }
        }
        return filters;
    }
    
}
