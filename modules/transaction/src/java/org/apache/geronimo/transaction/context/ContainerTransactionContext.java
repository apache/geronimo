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
import javax.transaction.Status;
import javax.transaction.SystemException;
import javax.transaction.Transaction;
import javax.transaction.TransactionManager;

/**
 * @version $Rev$ $Date$
 */
public class ContainerTransactionContext extends InheritableTransactionContext {
    private final TransactionManager txnManager;
    private Transaction transaction;

    private boolean threadAssociated = false;

    public ContainerTransactionContext(TransactionManager txnManager) throws SystemException, NotSupportedException {
        this.txnManager = txnManager;
        txnManager.begin();
        transaction = txnManager.getTransaction();
        threadAssociated = true;
    }

    public ContainerTransactionContext(TransactionManager txnManager, Transaction transaction) {
        this.txnManager = txnManager;
        this.transaction = transaction;
    }

    public void suspend() throws SystemException {
        Transaction suspendedTransaction = txnManager.suspend();
        assert (transaction == suspendedTransaction) : "suspend did not return our transaction. ours: " + transaction + ", suspended returned: " + suspendedTransaction;
        threadAssociated = false;
    }

    public void resume() throws SystemException, InvalidTransactionException {
        txnManager.resume(transaction);
        threadAssociated = true;
    }

    public void commit() throws HeuristicMixedException, HeuristicRollbackException, SystemException {
        boolean wasCommitted = false;
        try {
            if (checkRolledback()) {
                return;
            }

            flushState();

            if (checkRolledback()) {
                return;
            }

            // todo we need to flush anyone enrolled during before and then call before on any flushed...
            beforeCommit();

            if (checkRolledback()) {
                return;
            }

            txnManager.commit();
            wasCommitted = true;
        } catch (Throwable t) {
            rollbackAndThrow("Unable to commit container transaction", t);
        } finally {
            try {
                afterCommit(wasCommitted);
            } catch (Exception e) {
                rollbackAndThrow("After commit of container transaction failed", e);
            } finally {
                connectorAfterCommit();
                transaction = null;
            }
        }
    }

    private boolean checkRolledback() throws SystemException {
        int status;
        try {
            status = transaction.getStatus();
        } catch (SystemException e) {
            txnManager.rollback();
            throw e;
        }

        if (status == Status.STATUS_MARKED_ROLLBACK) {
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

    private void rollbackAndThrow(String message, Throwable throwable) throws HeuristicMixedException, HeuristicRollbackException, SystemException {
        try {
            // just incase there is a junk transaction on the thread
            if (txnManager.getStatus() != Status.STATUS_NO_TRANSACTION) {
                txnManager.rollback();
            }
        } catch (Throwable t) {
            log.error("Unable to roll back transaction", t);
        }

        if (throwable instanceof HeuristicMixedException) {
            throw (HeuristicMixedException) throwable;
        } else if (throwable instanceof HeuristicRollbackException) {
            throw (HeuristicRollbackException) throwable;
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

    public void rollback() throws SystemException {
        try {
            try {
                if (txnManager.getStatus() != Status.STATUS_NO_TRANSACTION) {
                    txnManager.rollback();
                }
            } finally {
                try {
                    afterCommit(false);
                } catch (Throwable e) {
                    try {
                        // just incase there is a junk transaction on the thread
                        if (txnManager.getStatus() != Status.STATUS_NO_TRANSACTION) {
                            txnManager.rollback();
                        }
                    } catch (Throwable t1) {
                        log.error("Unable to roll back transaction", t1);
                    }

                    if (e instanceof SystemException) {
                        throw (SystemException) e;
                    } else if (e instanceof Error) {
                        throw (Error) e;
                    } else if (e instanceof RuntimeException) {
                        throw (RuntimeException) e;
                    }
                    throw (SystemException) new SystemException("After commit of container transaction failed").initCause(e);
                }
            }
        } finally {
            connectorAfterCommit();
            transaction = null;
        }
    }

    public boolean isThreadAssociated() {
        return threadAssociated;
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

    public void setRollbackOnly() throws IllegalStateException, SystemException {
        if (transaction == null) {
            throw new IllegalStateException("There is no transaction in progress.");
        }
        transaction.setRollbackOnly();
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
}
