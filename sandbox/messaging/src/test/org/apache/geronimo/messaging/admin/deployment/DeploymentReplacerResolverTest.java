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
 * @version $Revision: 1.1 $ $Date: 2004/05/27 14:45:58 $
 */
public class DeploymentReplacerResolverTest extends TestCase
{

    public void testCustomReplaceObject1() throws Exception {
        TargetImpl target = new TargetImpl("test", "description");
        ReplacerResolver replacerResolver = new DeploymentReplacerResolver();
        TargetImpl2 result = (TargetImpl2) replacerResolver.replaceObject(target);
        assertEquals(target.getName(), result.getName());
        assertEquals(target.getDescription(), result.getDescription());
    }
    
    public void testCustomReplaceObject2() throws Exception {
        ModuleType type = ModuleType.CAR;
        ReplacerResolver replacerResolver = new DeploymentReplacerResolver();
        ModuleTypeWrapper result =
            (ModuleTypeWrapper) replacerResolver.replaceObject(type);
        assertEquals(type, result.getModuleType());
    }
    
    public void testCustomResolveObject1() throws Exception {
        TargetImpl target = new TargetImpl("test", "description");
        ReplacerResolver replacerResolver = new DeploymentReplacerResolver();
        TargetImpl2 tmp = (TargetImpl2) replacerResolver.replaceObject(target);
        
        TargetImpl result = (TargetImpl) replacerResolver.resolveObject(tmp);
        assertEquals(target.getName(), result.getName());
        assertEquals(target.getDescription(), result.getDescription());
    }
    
    public void testCustomResolveObject2() throws Exception {
        ModuleType type = ModuleType.CAR;
        ReplacerResolver replacerResolver = new DeploymentReplacerResolver();
        ModuleTypeWrapper tmp =
            (ModuleTypeWrapper) replacerResolver.replaceObject(type);
        ModuleType result = (ModuleType) replacerResolver.resolveObject(tmp);
        assertEquals(type, result);
    }
    
}
