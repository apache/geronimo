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

import org.apache.geronimo.transaction.InstanceContext;
import org.apache.geronimo.transaction.TrackedConnectionAssociator;
import org.apache.geronimo.transaction.context.TransactionContext;
import org.apache.geronimo.transaction.context.UnspecifiedTransactionContext;

/**
 * Sample functionality for an interceptor that enables connection caching and obtaining
 * connections outside a UserTransaction.
 *
 * @version $Revision: 1.4 $ $Date: 2004/07/18 22:08:59 $
 *
 * */
public class DefaultComponentInterceptor implements DefaultInterceptor {

    private final DefaultInterceptor next;
    private final TrackedConnectionAssociator trackedConnectionAssociator;

    public DefaultComponentInterceptor(DefaultInterceptor next,
            TrackedConnectionAssociator trackedConnectionAssociator) {
        this.next = next;
        this.trackedConnectionAssociator = trackedConnectionAssociator;
    }

    public Object invoke(InstanceContext newInstanceContext) throws Throwable {
        if (TransactionContext.getContext() == null) {
            TransactionContext.setContext(new UnspecifiedTransactionContext());
        }
        InstanceContext oldInstanceContext = trackedConnectionAssociator.enter(newInstanceContext);
        try {
            return next.invoke(newInstanceContext);
        } finally {
            trackedConnectionAssociator.exit(oldInstanceContext);
        }
    }
}
