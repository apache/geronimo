/**
 *
 * Copyright 2004 The Apache Software Foundation
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

package org.apache.geronimo.messaging.admin.deployment;

import javax.enterprise.deploy.shared.ModuleType;

import org.apache.geronimo.deployment.plugin.TargetImpl;
import org.apache.geronimo.messaging.io.ReplacerResolver;

import junit.framework.TestCase;

/**
 *
 * @version $Rev$ $Date$
 */
public class DeploymentReplacerResolverTest extends TestCase
{

    public void testCustomReplaceObject1() throws Exception {
        ModuleType type = ModuleType.CAR;
        ReplacerResolver replacerResolver = new DeploymentReplacerResolver();
        ModuleTypeWrapper result =
            (ModuleTypeWrapper) replacerResolver.replaceObject(type);
        assertEquals(type, result.getModuleType());
    }
    
    public void testCustomResolveObject1() throws Exception {
        ModuleType type = ModuleType.CAR;
        ReplacerResolver replacerResolver = new DeploymentReplacerResolver();
        ModuleTypeWrapper tmp =
            (ModuleTypeWrapper) replacerResolver.replaceObject(type);
        ModuleType result = (ModuleType) replacerResolver.resolveObject(tmp);
        assertEquals(type, result);
    }
    
}
