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

package org.apache.geronimo.connector.outbound.connectiontracking;

import java.util.Set;

import org.apache.geronimo.transaction.InstanceContext;
import org.apache.geronimo.transaction.TrackedConnectionAssociator;
import org.apache.geronimo.transaction.TransactionContext;
import org.apache.geronimo.transaction.UnspecifiedTransactionContext;

/**
 *
 *
 * @version $Revision: 1.2 $ $Date: 2004/05/24 19:10:35 $
 *
 * */
public class DefaultComponentInterceptor implements DefaultInterceptor {

    private final DefaultInterceptor next;
    private final TrackedConnectionAssociator cachedConnectionAssociator;
    private final Set unshareableResources;
    private final Set applicationManagedSecurityResources;

    public DefaultComponentInterceptor(DefaultInterceptor next,
            TrackedConnectionAssociator cachedConnectionManager,
            Set unshareableResources, Set applicationManagedSecurityResources) {
        this.next = next;
        this.cachedConnectionAssociator = cachedConnectionManager;
        this.unshareableResources = unshareableResources;
        this.applicationManagedSecurityResources = applicationManagedSecurityResources;
    }

    public Object invoke(InstanceContext newInstanceContext) throws Throwable {
        if (TransactionContext.getContext() == null) {
            TransactionContext.setContext(new UnspecifiedTransactionContext());
        }
        TrackedConnectionAssociator.ConnectorContextInfo oldConnectorContext = cachedConnectionAssociator.enter(newInstanceContext, unshareableResources, applicationManagedSecurityResources);
        try {
            return next.invoke(newInstanceContext);
        } finally {
            cachedConnectionAssociator.exit(oldConnectorContext);
        }
    }
}
