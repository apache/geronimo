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

import javax.enterprise.deploy.spi.Target;

import org.apache.geronimo.deployment.plugin.TargetImpl;

/**
 * A serializable Target. Should replace TargetImpl.
 *
 * @version $Revision: 1.1 $ $Date: 2004/05/27 14:45:59 $
 */
public class TargetImpl2
    implements Target, Serializable
{

    private final String name;
    private final String description;
    
    public TargetImpl2(TargetImpl aTarget) {
        name = aTarget.getName();
        description = aTarget.getDescription();
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

}
