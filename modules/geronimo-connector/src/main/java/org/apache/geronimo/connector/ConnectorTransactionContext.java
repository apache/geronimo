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
package org.apache.geronimo.connector;

import java.util.Map;
import java.util.HashMap;
import java.util.Iterator;

import javax.transaction.Transaction;
import javax.transaction.Synchronization;
import javax.transaction.RollbackException;
import javax.transaction.SystemException;
import javax.transaction.Status;

import edu.emory.mathcs.backport.java.util.concurrent.ConcurrentHashMap;
import org.apache.geronimo.connector.outbound.TransactionCachingInterceptor;

/**
 * @version $Rev$ $Date$
 */
public class ConnectorTransactionContext {
    private static final ConcurrentHashMap DATA_INDEX = new ConcurrentHashMap();

    public static ConnectorTransactionContext get(Transaction transaction) {
        if (transaction == null) {
            throw new NullPointerException("transaction is null");
        }

        ConnectorTransactionContext ctx = (ConnectorTransactionContext) DATA_INDEX.get(transaction);
        if (ctx == null) {
            ctx = new ConnectorTransactionContext();

            try {
                if (transaction.getStatus() == Status.STATUS_ACTIVE) {
                    transaction.registerSynchronization(new ConnectorSynchronization(ctx, transaction));
                    // Note: no synchronization is necessary here.  Since a transaction can only be associated with a single
                    // thread at a time, it should not be possible for someone else to have snuck in and created a
                    // ConnectorTransactionContext for this transaction.  We still protect against that with the putIfAbsent
                    // call below, and we simply have an extra transaction synchronization registered that won't do anything
                    DATA_INDEX.putIfAbsent(transaction, ctx);
                }
            } catch (RollbackException e) {
                throw (IllegalStateException) new IllegalStateException("Transaction is already rolled back").initCause(e);
            } catch (SystemException e) {
                throw new RuntimeException("Unable to register ejb transaction synchronization callback", e);
            }

        }
        return ctx;
    }

    public static TransactionCachingInterceptor.ManagedConnectionInfos get(Transaction transaction, ConnectionReleaser key) {
        ConnectorTransactionContext ctx = get(transaction);
        TransactionCachingInterceptor.ManagedConnectionInfos infos = ctx.getManagedConnectionInfo(key);
        if (infos == null) {
            infos = new TransactionCachingInterceptor.ManagedConnectionInfos();
            ctx.setManagedConnectionInfo(key, infos);
        }
        return infos;
    }

    private static void remove(Transaction transaction) {
        DATA_INDEX.remove(transaction);
    }

    private Map managedConnections;

    private synchronized TransactionCachingInterceptor.ManagedConnectionInfos getManagedConnectionInfo(ConnectionReleaser key) {
        if (managedConnections == null) {
            return null;
        }
        return (TransactionCachingInterceptor.ManagedConnectionInfos) managedConnections.get(key);
    }

    private synchronized void setManagedConnectionInfo(ConnectionReleaser key, TransactionCachingInterceptor.ManagedConnectionInfos info) {
        if (managedConnections == null) {
            managedConnections = new HashMap();
        }
        managedConnections.put(key, info);
    }

    private static class ConnectorSynchronization implements Synchronization {
        private final ConnectorTransactionContext ctx;
        private final Transaction transaction;

        public ConnectorSynchronization(ConnectorTransactionContext ctx, Transaction transaction) {
            this.ctx = ctx;
            this.transaction = transaction;
        }

        public void beforeCompletion() {
        }

        public void afterCompletion(int status) {
            try {
                synchronized (ctx) {
                    if (ctx.managedConnections != null) {
                        for (Iterator entries = ctx.managedConnections.entrySet().iterator(); entries.hasNext();) {
                            Map.Entry entry = (Map.Entry) entries.next();
                            ConnectionReleaser key = (ConnectionReleaser) entry.getKey();
                            key.afterCompletion(entry.getValue());
                        }
                        //If BeanTransactionContext never reuses the same instance for sequential BMT, this
                        //clearing is unnecessary.
                        ctx.managedConnections.clear();
                    }
                }
            } finally {
                remove(transaction);
            }
        }
    }
}
