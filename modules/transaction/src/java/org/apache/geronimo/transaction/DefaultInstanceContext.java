/**
 *
 * Copyright 2003-2004 The Apache Software Foundation
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

package org.apache.geronimo.transaction;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;


/**
 * Simple implementation of ComponentContext satisfying invariant.
 *
 * @version $Rev$ $Date$
 *
 * */
public class DefaultInstanceContext implements InstanceContext {

    private final Map connectionManagerMap = new HashMap();
    private final Set unshareableResources;
    private final Set applicationManagedSecurityResources;

    public DefaultInstanceContext(Set unshareableResources, Set applicationManagedSecurityResources) {
        this.unshareableResources = unshareableResources;
        this.applicationManagedSecurityResources = applicationManagedSecurityResources;
    }

    public Object getId() {
        return null;
    }

    public void setId(Object id) {
    }

    public Object getContainerId() {
        return null;
    }

    public void associate() throws Exception {
    }

    public void flush() throws Exception {
    }

    public void beforeCommit() throws Exception {
    }

    public void afterCommit(boolean status) throws Exception {
    }

    public Map getConnectionManagerMap() {
        return connectionManagerMap;
    }

    public Set getUnshareableResources() {
        return unshareableResources;
    }

    public Set getApplicationManagedSecurityResources() {
        return applicationManagedSecurityResources;
    }

}
