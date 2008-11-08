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


/**
 *
 * @version $Rev:$ $Date:$
 */
public class ClassLoadingRules implements Serializable {
    private final ClassLoadingRule hiddenRule;
    private final ClassLoadingRule nonOverrideableRule;
    private final ClassLoadingRule privateRule;
    private boolean inverseClassLoading;

    public ClassLoadingRules() {
        hiddenRule = new ClassLoadingRule();
        nonOverrideableRule = new ClassLoadingRule();
        privateRule = new ClassLoadingRule();
    }

    public ClassLoadingRule getHiddenRule() {
        return hiddenRule;
    }

    public ClassLoadingRule getNonOverrideableRule() {
        return nonOverrideableRule;
    }

    public ClassLoadingRule getPrivateRule() {
        return privateRule;
    }

    public boolean isInverseClassLoading() {
        return inverseClassLoading;
    }

    public void setInverseClassLoading(boolean inverseClassLoading) {
        this.inverseClassLoading = inverseClassLoading;
    }

    public void merge(ClassLoadingRules classLoadingRulesToMerge) {
        if (inverseClassLoading) {
            return;
        }
        inverseClassLoading = classLoadingRulesToMerge.inverseClassLoading;
        
        hiddenRule.merge(classLoadingRulesToMerge.hiddenRule);
        nonOverrideableRule.merge(classLoadingRulesToMerge.nonOverrideableRule);
        privateRule.merge(classLoadingRulesToMerge.privateRule);
    }

}
