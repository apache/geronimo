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
import javax.enterprise.deploy.spi.status.DeploymentStatus;

import org.apache.geronimo.messaging.io.AbstractReplacerResolver;

/**
 *
 * @version $Rev$ $Date$
 */
public class DeploymentReplacerResolver
    extends AbstractReplacerResolver
{

    protected Object customReplaceObject(Object obj) throws IOException {
    	if ( obj instanceof ModuleType ) {
            return new ModuleTypeWrapper((ModuleType) obj);
        } else if ( obj instanceof DeploymentStatus ) {
            DeploymentStatus status = (DeploymentStatus) obj;
            return new DeploymentStatusImpl(status.getCommand(),
                status.getAction(), status.getState(), status.getMessage());
        }
        return null;
    }

    protected Object customResolveObject(Object obj) throws IOException {
        if ( obj instanceof ModuleTypeWrapper ) {
            return ((ModuleTypeWrapper)obj).getModuleType();
        }
        return null;
    }

}
