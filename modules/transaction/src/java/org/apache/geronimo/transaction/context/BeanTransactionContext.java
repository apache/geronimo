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

package org.apache.geronimo.transaction.context;

import javax.transaction.HeuristicMixedException;
import javax.transaction.HeuristicRollbackException;
import javax.transaction.InvalidTransactionException;
import javax.transaction.NotSupportedException;
import javax.transaction.RollbackException;
import javax.transaction.Status;
import javax.transaction.SystemException;
import javax.transaction.Transaction;
import javax.transaction.TransactionManager;

/**
 *
 *
 * @version $Rev$ $Date$
 */
public class BeanTransactionContext extends InheritableTransactionContext {
    private final TransactionManager txnManager;
    private final UnspecifiedTransactionContext oldContext;
    private Transaction transaction;


    public BeanTransactionContext(TransactionManager txnManager, UnspecifiedTransactionContext oldContext) {
        this.txnManager = txnManager;
        this.oldContext = oldContext;
    }

    public UnspecifiedTransactionContext getOldContext() {
        return oldContext;
    }

    public void begin() throws SystemException, NotSupportedException {
        txnManager.begin();
        transaction = txnManager.getTransaction();
    }

    public void suspend() throws SystemException {
        Transaction suspendedTransaction = txnManager.suspend();
        assert (transaction == suspendedTransaction) : "suspend did not return our transaction";
    }

    public void resume() throws SystemException, InvalidTransactionException {
        txnManager.resume(transaction);
    }

    public void commit() throws HeuristicMixedException, HeuristicRollbackException, RollbackException, SystemException {
        try {
            try {
                flushState();
            } catch (Throwable t) {
                try {
                    txnManager.rollback();
                } catch (Throwable t1) {
                    log.error("Unable to roll back transaction", t1);
                }
                throw (RollbackException) new RollbackException("Could not flush state before commit").initCause(t);
            }
            txnManager.commit();
        } finally {
            connectorAfterCommit();
            transaction = null;
        }
    }

    public void rollback() throws SystemException {
        try {
            txnManager.rollback();
        } finally {
            connectorAfterCommit();
            transaction = null;
        }
    }

    //Geronimo connector framework support

    public boolean isActive() {
        try {
            return txnManager.getStatus() == Status.STATUS_ACTIVE;
        } catch (SystemException e) {
            return false;
        }
    }

    public Transaction getTransaction() {
        return transaction;
    }
}
