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

import org.apache.geronimo.connector.outbound.connectiontracking.ConnectorInstanceContext;
import org.apache.geronimo.connector.outbound.connectiontracking.TrackedConnectionAssociator;

/**
 * Sample functionality for an interceptor that enables connection caching and obtaining
 * connections outside a UserTransaction.
 *
 * @version $Rev$ $Date$
 */
public class DefaultComponentInterceptor implements DefaultInterceptor {

    private final DefaultInterceptor next;
    private final TrackedConnectionAssociator trackedConnectionAssociator;

    public DefaultComponentInterceptor(DefaultInterceptor next, TrackedConnectionAssociator trackedConnectionAssociator) {
        this.next = next;
        this.trackedConnectionAssociator = trackedConnectionAssociator;
    }

    public Object invoke(ConnectorInstanceContext newConnectorInstanceContext) throws Throwable {
        ConnectorInstanceContext oldConnectorInstanceContext = trackedConnectionAssociator.enter(newConnectorInstanceContext);
        try {
            return next.invoke(newConnectorInstanceContext);
        } finally {
            trackedConnectionAssociator.exit(oldConnectorInstanceContext);
        }
    }
}
