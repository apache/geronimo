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

import java.io.Serializable;

import javax.enterprise.deploy.shared.ModuleType;

/**
 * ModuleType wrapper which is serializable.
 *
 * @version $Rev$ $Date$
 */
public class ModuleTypeWrapper
    implements Serializable
{

    /**
     * ModuleType ordinal 
     */
    private final int code;
    
    public ModuleTypeWrapper(ModuleType aType) {
        if ( null == aType ) {
            throw new IllegalArgumentException("Type is required");
        }
        code = aType.getValue();
    }
    
    public ModuleType getModuleType() {
        return ModuleType.getModuleType(code);
    }

}
