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
package org.apache.geronimo.connector.outbound.connectiontracking;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * @version $Rev$ $Date$
 */
public class SharedConnectorInstanceContext implements ConnectorInstanceContext {

    private Map connectionManagerMap = new HashMap();

    private final Set unshareableResources;
    private final Set applicationManagedSecurityResources;

    private boolean hide = false;

    public SharedConnectorInstanceContext(Set unshareableResources, Set applicationManagedSecurityResources, boolean share) {
        this.unshareableResources = unshareableResources;
        this.applicationManagedSecurityResources = applicationManagedSecurityResources;
        if (!share) {
            connectionManagerMap = new HashMap();
        }
    }

    public void share(SharedConnectorInstanceContext context) {
        connectionManagerMap = context.connectionManagerMap;
    }

    public void hide() {
        this.hide = true;
    }

    public Map getConnectionManagerMap() {
        if (hide) {
            return Collections.EMPTY_MAP;
        }
        return connectionManagerMap;
    }

    public Set getUnshareableResources() {
        return unshareableResources;
    }

    public Set getApplicationManagedSecurityResources() {
        return applicationManagedSecurityResources;
    }
}
