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

package org.apache.geronimo.deployment.plugin;

import javax.enterprise.deploy.spi.TargetModuleID;
import javax.enterprise.deploy.spi.Target;

/**
 * 
 * 
 * @version $Revision: 1.2 $ $Date: 2004/02/25 09:57:36 $
 */
public class TargetModuleIDImpl implements TargetModuleID {
    private final Target target;
    private final String moduleID;

    public TargetModuleIDImpl(Target target, String moduleID) {
        this.target = target;
        this.moduleID = moduleID;
    }

    public Target getTarget() {
        return target;
    }

    public String getModuleID() {
        return moduleID;
    }

    public TargetModuleID getParentTargetModuleID() {
        return null;
    }

    public TargetModuleID[] getChildTargetModuleID() {
        return null;
    }

    public String getWebURL() {
        return null;
    }
}
