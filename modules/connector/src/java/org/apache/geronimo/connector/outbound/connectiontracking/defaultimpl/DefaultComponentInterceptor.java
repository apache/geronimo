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

package org.apache.geronimo.connector.outbound.connectiontracking.defaultimpl;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import javax.transaction.Status;
import javax.transaction.Transaction;
import javax.transaction.TransactionManager;

import org.apache.geronimo.connector.outbound.connectiontracking.TrackedConnectionAssociator;
import org.apache.geronimo.transaction.TransactionContext;
import org.apache.geronimo.transaction.InstanceContext;

/**
 *
 *
 * @version $Revision: 1.3 $ $Date: 2004/02/25 09:57:11 $
 *
 * */
public class DefaultComponentInterceptor implements DefaultInterceptor {

    private final DefaultInterceptor next;
    private final TrackedConnectionAssociator cachedConnectionAssociator;
    private final Set unshareableResources;
    private final TransactionManager transactionManager;
    private final Map transactionToTransactionContextMap = new HashMap();

    public DefaultComponentInterceptor(DefaultInterceptor next,
            TrackedConnectionAssociator cachedConnectionManager,
            Set unshareableResources,
            TransactionManager transactionManager) {
        this.next = next;
        this.cachedConnectionAssociator = cachedConnectionManager;
        this.unshareableResources = unshareableResources;
        this.transactionManager = transactionManager;
    }

    public Object invoke(InstanceContext newInstanceContext) throws Throwable {
        Transaction transaction = transactionManager.getTransaction();
        TransactionContext newTransactionContext;
        if (transaction == null || transaction.getStatus() == Status.STATUS_COMMITTED || transaction.getStatus() == Status.STATUS_ROLLEDBACK) {
            newTransactionContext = new DefaultTransactionContext(null);
        } else {
            newTransactionContext = (TransactionContext) transactionToTransactionContextMap.get(transaction);
            if (newTransactionContext == null) {
                newTransactionContext = new DefaultTransactionContext(transaction);
                transactionToTransactionContextMap.put(transaction, newTransactionContext);
            }
        }
        Set oldUnshareableResources = cachedConnectionAssociator.setUnshareableResources(unshareableResources);
        InstanceContext oldInstanceContext = cachedConnectionAssociator.enter(newInstanceContext);
        TransactionContext oldTransactionContext = cachedConnectionAssociator.setTransactionContext(newTransactionContext);
        try {
            return next.invoke(newInstanceContext);
        } finally {
            cachedConnectionAssociator.exit(oldInstanceContext, unshareableResources);
            cachedConnectionAssociator.resetTransactionContext(oldTransactionContext);
            cachedConnectionAssociator.setUnshareableResources(oldUnshareableResources);
        }
    }
}
