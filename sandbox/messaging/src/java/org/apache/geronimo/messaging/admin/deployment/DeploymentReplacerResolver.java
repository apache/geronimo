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

import java.io.IOException;

import javax.enterprise.deploy.shared.ModuleType;

import org.apache.geronimo.deployment.plugin.TargetImpl;
import org.apache.geronimo.messaging.io.AbstractReplacerResolver;

/**
 *
 * @version $Revision: 1.1 $ $Date: 2004/05/27 14:45:59 $
 */
public class DeploymentReplacerResolver
    extends AbstractReplacerResolver
{

    protected Object customReplaceObject(Object obj) throws IOException {
        if ( obj instanceof TargetImpl ) {
            return new TargetImpl2((TargetImpl) obj);
        } else if ( obj instanceof ModuleType ) {
            return new ModuleTypeWrapper((ModuleType) obj);
        }
        return null;
    }

    protected Object customResolveObject(Object obj) throws IOException {
        if ( obj instanceof TargetImpl2 ) {
            TargetImpl2 target = (TargetImpl2) obj; 
            return new TargetImpl(target.getName(), target.getDescription());
        } else if ( obj instanceof ModuleTypeWrapper ) {
            return ((ModuleTypeWrapper)obj).getModuleType();
        }
        return null;
    }

}
