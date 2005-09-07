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

import java.util.Iterator;
import java.util.Map;
import java.util.ArrayList;
import javax.transaction.SystemException;
import javax.transaction.Status;
import javax.transaction.Transaction;
import javax.transaction.InvalidTransactionException;
import javax.transaction.NotSupportedException;
import javax.transaction.HeuristicMixedException;
import javax.transaction.HeuristicRollbackException;
import javax.transaction.RollbackException;
import javax.transaction.Synchronization;
import javax.transaction.xa.XAResource;

import org.apache.geronimo.transaction.ExtendedTransactionManager;
import org.apache.geronimo.transaction.ConnectionReleaser;
import org.apache.geronimo.transaction.InstanceContext;

/**
 * @version $Rev$ $Date$
 */
abstract class InheritableTransactionContext extends AbstractTransactionContext {
    private final ExtendedTransactionManager txnManager;
    private Transaction transaction;
    private boolean threadAssociated = false;

    protected InheritableTransactionContext(ExtendedTransactionManager txnManager) {
        this.txnManager = txnManager;
    }

    protected InheritableTransactionContext(ExtendedTransactionManager txnManager, Transaction transaction) {
        this.txnManager = txnManager;
        this.transaction = transaction;
    }

    void begin(long transactionTimeoutMilliseconds) throws SystemException, NotSupportedException {
        if (transaction != null) {
            throw new SystemException("Context is already associated with a transaction");
        }
        transaction = txnManager.begin(transactionTimeoutMilliseconds);
        threadAssociated = true;
    }

    boolean isThreadAssociated() {
        return threadAssociated;
    }

    public Transaction getTransaction() {
        return transaction;
    }

    public boolean isInheritable() {
        return true;
    }

    public boolean isActive() {
        if (transaction == null) {
            return false;
        }
        try {
            int status = transaction.getStatus();
            return status == Status.STATUS_ACTIVE || status == Status.STATUS_MARKED_ROLLBACK;
        } catch (SystemException e) {
            return false;
        }
    }

    public boolean enlistResource(XAResource xaResource) throws RollbackException, SystemException {
        if (transaction == null) {
            throw new IllegalStateException("There is no transaction in progress.");
        }

        return transaction.enlistResource(xaResource);
    }

    public boolean delistResource(XAResource xaResource, int flag) throws SystemException {
        if (transaction == null) {
            throw new IllegalStateException("There is no transaction in progress.");
        }
        boolean success = transaction.delistResource(xaResource, flag);
        if (!success) {
            transaction.setRollbackOnly();
        }
        return success;
    }

    public void registerSynchronization(Synchronization synchronization) throws RollbackException, SystemException {
        if (transaction == null) {
            throw new IllegalStateException("There is no transaction in progress.");
        }

        transaction.registerSynchronization(synchronization);
    }

    public boolean getRollbackOnly() throws SystemException {
        if (transaction == null) {
            throw new IllegalStateException("There is no transaction in progress.");
        }

        int status = transaction.getStatus();
        return (status == Status.STATUS_MARKED_ROLLBACK ||
                status == Status.STATUS_ROLLEDBACK ||
                status == Status.STATUS_ROLLING_BACK);
    }

    public void setRollbackOnly() throws IllegalStateException, SystemException {
        if (transaction == null) {
            throw new IllegalStateException("There is no transaction in progress.");
        }
        transaction.setRollbackOnly();
    }

    public void suspend() throws SystemException {
        Transaction suspendedTransaction = txnManager.suspend();
        if (transaction != suspendedTransaction) {
            throw new SystemException("Suspend did not return our transaction: expectedTx=" + transaction + ", suspendedTx=" + suspendedTransaction);
        }
        threadAssociated = false;
    }

    public void resume() throws SystemException, InvalidTransactionException {
        txnManager.resume(transaction);
        threadAssociated = true;
    }

    public boolean commit() throws HeuristicMixedException, HeuristicRollbackException, SystemException, RollbackException {
        return complete();
    }

    public void rollback() throws SystemException {
        setRollbackOnly();
        try {
            complete();
        } catch (SystemException e) {
            throw e;
        } catch (RuntimeException e) {
            throw e;
        } catch (Error e) {
            throw e;
        } catch (Exception e) {
            throw (SystemException) new SystemException("After commit of container transaction failed").initCause(e);
        }
    }

    private boolean complete() throws HeuristicMixedException, HeuristicRollbackException, SystemException, RollbackException {
        if (transaction == null) {
            throw new IllegalStateException("There is no transaction in progress.");
        }

        boolean wasCommitted = false;
        try {
            if (isRolledback()) {
                return false;
            }

            flushState();

            if (isRolledback()) {
                return false;
            }

            // todo we need to flush anyone enrolled during before and then call before on any flushed...
            beforeCommit();

            if (isRolledback()) {
                return false;
            }

            // verify our tx is the current tx associated with the thread
            // this is really only an error case and should never happen, but just to be sure double check
            // immedately before committing using the transaction manager
            Transaction currentTransaction = txnManager.getTransaction();
            if (currentTransaction != transaction) {
                throw new SystemException("An unknown transaction is currently associated with the thread: expectedTx=" + transaction + ", currentTx=" + currentTransaction);
            }

            txnManager.commit();
            wasCommitted = true;
        } catch (Throwable t) {
            rollbackAndThrow("Unable to commit container transaction", t);
        } finally {
            transaction = null;
            try {
                afterCommit(wasCommitted);
            } catch (Throwable e) {
                rollbackAndThrow("After commit of container transaction failed", e);
            } finally {
                unassociateAll();
                connectorAfterCommit();
                threadAssociated = false;
            }
        }
        return wasCommitted;
    }

    private void beforeCommit() throws Throwable {
        // @todo allow for enrollment during pre-commit
        ArrayList toFlush = getAssociatedContexts();
        for (Iterator i = toFlush.iterator(); i.hasNext();) {
            InstanceContext context = (InstanceContext) i.next();
            if (!context.isDead()) {
                context.beforeCommit();
            }
        }
    }

    private void afterCommit(boolean status) throws Throwable {
        Throwable firstThrowable = null;
        ArrayList toFlush = getAssociatedContexts();
        for (Iterator i = toFlush.iterator(); i.hasNext();) {
            InstanceContext context = (InstanceContext) i.next();
            if (!context.isDead()) {
                try {
                    context.afterCommit(status);
                } catch (Throwable e) {
                    if (firstThrowable == null) {
                        firstThrowable = e;
                    }
                }
            }
        }

        if (firstThrowable instanceof Error) {
            throw (Error) firstThrowable;
        } else if (firstThrowable instanceof Exception) {
            throw (Exception) firstThrowable;
        } else if (firstThrowable != null) {
            throw (SystemException) new SystemException().initCause(firstThrowable);
        }
    }

    private void connectorAfterCommit() {
        if (managedConnections != null) {
            for (Iterator entries = managedConnections.entrySet().iterator(); entries.hasNext();) {
                Map.Entry entry = (Map.Entry) entries.next();
                ConnectionReleaser key = (ConnectionReleaser) entry.getKey();
                key.afterCompletion(entry.getValue());
            }
            //If BeanTransactionContext never reuses the same instance for sequential BMT, this
            //clearing is unnecessary.
            managedConnections.clear();
        }
    }

    private boolean isRolledback() throws SystemException {
        int status;
        try {
            status = transaction.getStatus();
        } catch (SystemException e) {
            transaction.rollback();
            throw e;
        }

        if (status == Status.STATUS_MARKED_ROLLBACK) {
            // verify our tx is the current tx associated with the thread
            // this is really only an error case and should never happen, but just to be sure double check
            // immedately before committing using the transaction manager
            Transaction currentTransaction = txnManager.getTransaction();
            if (currentTransaction != transaction) {
                throw new SystemException("An unknown transaction is currently associated with the thread: expectedTx=" + transaction + ", currentTx=" + currentTransaction);
            }

            // we need to rollback
            txnManager.rollback();
            return true;
        } else if (status == Status.STATUS_ROLLEDBACK ||
                status == Status.STATUS_ROLLING_BACK) {
            // already rolled back
            return true;
        }
        return false;
    }

    private void rollbackAndThrow(String message, Throwable throwable) throws HeuristicMixedException, HeuristicRollbackException, SystemException, RollbackException {
        try {
            if (txnManager.getStatus() != Status.STATUS_NO_TRANSACTION) {
                txnManager.rollback();
            }
        } catch (Throwable t) {
            log.error("Unable to roll back transaction", t);
        }

        try {
            // make doubly sure our transaction was rolled back
            // this can happen when there was a junk transaction on the thread
            int status = transaction.getStatus();
            if (status != Status.STATUS_ROLLEDBACK &&
                    status != Status.STATUS_ROLLING_BACK) {
                transaction.rollback();
            }
        } catch (Throwable t) {
            log.error("Unable to roll back transaction", t);
        }

        if (throwable instanceof HeuristicMixedException) {
            throw (HeuristicMixedException) throwable;
        } else if (throwable instanceof HeuristicRollbackException) {
            throw (HeuristicRollbackException) throwable;
        } else if (throwable instanceof RollbackException) {
            throw (RollbackException) throwable;
        } else if (throwable instanceof SystemException) {
            throw (SystemException) throwable;
        } else if (throwable instanceof Error) {
            throw (Error) throwable;
        } else if (throwable instanceof RuntimeException) {
            throw (RuntimeException) throwable;
        } else {
            throw (SystemException) new SystemException(message).initCause(throwable);
        }
    }
}
