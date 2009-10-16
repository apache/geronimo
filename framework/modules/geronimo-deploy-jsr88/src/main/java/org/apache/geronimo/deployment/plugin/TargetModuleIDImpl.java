/**
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
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
import javax.enterprise.deploy.shared.ModuleType;
import java.io.Serializable;

/**
 * 
 * 
 * @version $Rev$ $Date$
 */
public class TargetModuleIDImpl implements TargetModuleID, Serializable {
    private String webURL;
    private ModuleType type;
    private final Target target;
    private final String moduleID;
    private final TargetModuleID parentTargetModuleID;
    private final TargetModuleID[] childTargetModuleID;

    public TargetModuleIDImpl(Target target, String moduleID) {
        this.target = target;
        this.moduleID = moduleID;
        parentTargetModuleID = null;
        childTargetModuleID = null;
    }

    public TargetModuleIDImpl(Target target, String moduleID, String[] childIDs) {
        this.target = target;
        this.moduleID = moduleID;
        parentTargetModuleID = null;
        if(childIDs == null || childIDs.length == 0) {
            childTargetModuleID = null;
        } else {
            childTargetModuleID = new TargetModuleID[childIDs.length];
            for (int i = 0; i < childIDs.length; i++) {
                String childID = childIDs[i];
                childTargetModuleID[i] = new TargetModuleIDImpl(target, childID, this);
            }
        }
    }

    private TargetModuleIDImpl(Target target, String moduleID, TargetModuleID parent) {
        this.target = target;
        this.moduleID = moduleID;
        this.parentTargetModuleID = parent;
        childTargetModuleID = null;
    }

    public Target getTarget() {
        return target;
    }

    public String getModuleID() {
        return moduleID;
    }

    public TargetModuleID getParentTargetModuleID() {
        return parentTargetModuleID;
    }

    public TargetModuleID[] getChildTargetModuleID() {
        return childTargetModuleID;
    }

    public String getWebURL() {
        return webURL;
    }

    public void setWebURL(String webURL) {
        this.webURL = webURL;
    }

    public ModuleType getType() {
        return type;
    }

    public void setType(ModuleType type) {
        this.type = type;
    }

    public String toString() {
        return "[" + target + ", " + moduleID + "]";
    }

    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof TargetModuleIDImpl)) return false;

        final TargetModuleIDImpl targetModuleID = (TargetModuleIDImpl) o;

        if (!moduleID.equals(targetModuleID.moduleID)) return false;
        if (!target.equals(targetModuleID.target)) return false;

        return true;
    }

    public int hashCode() {
        int result;
        result = target.hashCode();
        result = 29 * result + moduleID.hashCode();
        return result;
    }
}
