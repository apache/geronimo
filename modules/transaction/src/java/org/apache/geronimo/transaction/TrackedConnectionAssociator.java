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

package org.apache.geronimo.transaction;

import java.util.Set;

import javax.resource.ResourceException;

/**
 *
 *
 * @version $Revision: 1.1 $ $Date: 2004/04/06 00:21:20 $
 *
 * */
public interface TrackedConnectionAssociator {

    ConnectorContextInfo enter(InstanceContext newInstanceContext,
                               Set unshareableResources)
            throws ResourceException;

    void newTransaction() throws ResourceException;

    void exit(ConnectorContextInfo connectorContext)
            throws ResourceException;

    class ConnectorContextInfo {
        private final InstanceContext instanceContext;
        private final Set unshareableResources;

        public ConnectorContextInfo(InstanceContext instanceContext, Set unshareableResources) {
            this.instanceContext = instanceContext;
            this.unshareableResources = unshareableResources;
        }

        public InstanceContext getInstanceContext() {
            return instanceContext;
        }

        public Set getUnshareableResources() {
            return unshareableResources;
        }
    }
}
