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

package org.apache.geronimo.transaction.manager;

import java.util.Timer;
import java.util.TimerTask;
import javax.transaction.HeuristicMixedException;
import javax.transaction.HeuristicRollbackException;
import javax.transaction.InvalidTransactionException;
import javax.transaction.NotSupportedException;
import javax.transaction.RollbackException;
import javax.transaction.Status;
import javax.transaction.SystemException;
import javax.transaction.Transaction;
import javax.transaction.TransactionManager;
import javax.transaction.xa.XAException;
import javax.transaction.xa.Xid;

import org.apache.geronimo.transaction.log.UnrecoverableLog;

/**
 * Simple implementation of a transaction manager.
 * TODO transactionTimeoutMilliseconds functionality
 * TODO shut down timer gracefully
 *
 * @version $Rev$ $Date$
 */
public class TransactionManagerImpl implements TransactionManager, XidImporter {
    private final TransactionLog txnLog;
    private final XidFactory xidFactory;
    private final int defaultTransactionTimeoutMilliseconds;
    private volatile int transactionTimeoutMilliseconds;
    private final ThreadLocal threadTx = new ThreadLocal();
    private final Timer timeoutTimer = new Timer(true);

    public TransactionManagerImpl() {
        defaultTransactionTimeoutMilliseconds = 10 * 1000;
        transactionTimeoutMilliseconds = defaultTransactionTimeoutMilliseconds;
        txnLog = new UnrecoverableLog();
        xidFactory = new XidFactoryImpl();
    }

    public TransactionManagerImpl(int defaultTransactionTimeoutSeconds, TransactionLog txnLog, XidFactory xidFactory) throws SystemException {
        if (defaultTransactionTimeoutSeconds <= 0) {
            throw new IllegalArgumentException("defaultTransactionTimeoutSeconds must be positive: attempted value: " + defaultTransactionTimeoutSeconds);
        }
        this.defaultTransactionTimeoutMilliseconds = defaultTransactionTimeoutSeconds * 1000;
        setTransactionTimeout(defaultTransactionTimeoutSeconds);
        this.txnLog = txnLog;
        this.xidFactory = xidFactory;
    }

    public Transaction getTransaction() throws SystemException {
        return (Transaction) threadTx.get();
    }

    public void setTransactionTimeout(int seconds) throws SystemException {
        if (seconds < 0) {
            throw new SystemException("transaction timeout must be positive or 0 to reset to default");
        }
        if (seconds == 0) {
            transactionTimeoutMilliseconds = defaultTransactionTimeoutMilliseconds;
        } else {
            transactionTimeoutMilliseconds = seconds * 1000;
        }
    }

    public int getStatus() throws SystemException {
        Transaction tx = getTransaction();
        return (tx != null) ? tx.getStatus() : Status.STATUS_NO_TRANSACTION;
    }

    public void begin() throws NotSupportedException, SystemException {
        if (getStatus() != Status.STATUS_NO_TRANSACTION) {
            throw new NotSupportedException("Nested Transactions are not supported");
        }
        TransactionImpl tx = new TransactionImpl(xidFactory, txnLog);
        TimerTask timeout = new TransactionTimeout(tx);
        timeoutTimer.schedule(timeout, transactionTimeoutMilliseconds);
        threadTx.set(tx);
    }

    public Transaction suspend() throws SystemException {
        Transaction tx = getTransaction();
        threadTx.set(null);
        return tx;
    }

    public void resume(Transaction tx) throws IllegalStateException, InvalidTransactionException, SystemException {
        if (threadTx.get() != null) {
            throw new IllegalStateException("Transaction already associated with current thread");
        }
        if (tx instanceof TransactionImpl == false) {
            throw new InvalidTransactionException("Cannot resume foreign transaction: " + tx);
        }
        threadTx.set(tx);
    }

    public void setRollbackOnly() throws IllegalStateException, SystemException {
        Transaction tx = getTransaction();
        if (tx == null) {
            throw new IllegalStateException("No transaction associated with current thread");
        }
        tx.setRollbackOnly();
    }

    public void commit() throws HeuristicMixedException, HeuristicRollbackException, IllegalStateException, RollbackException, SecurityException, SystemException {
        Transaction tx = getTransaction();
        if (tx == null) {
            throw new IllegalStateException("No transaction associated with current thread");
        }
        try {
            tx.commit();
        } finally {
            threadTx.set(null);
        }
    }

    public void rollback() throws IllegalStateException, SecurityException, SystemException {
        Transaction tx = getTransaction();
        if (tx == null) {
            throw new IllegalStateException("No transaction associated with current thread");
        }
        try {
            tx.rollback();
        } finally {
            threadTx.set(null);
        }
    }

    //XidImporter implementation
    public Transaction importXid(Xid xid) throws XAException, SystemException {
        TransactionImpl tx = new TransactionImpl(xid, xidFactory, txnLog);
        return tx;
    }

    public void commit(Transaction tx, boolean onePhase) throws XAException {
        if (onePhase) {
            try {
                tx.commit();
            } catch (HeuristicMixedException e) {
                throw new XAException();
            } catch (HeuristicRollbackException e) {
                throw new XAException();
            } catch (RollbackException e) {
                throw new XAException();
            } catch (SecurityException e) {
                throw new XAException();
            } catch (SystemException e) {
                throw new XAException();
            }
        } else {
            try {
                ((TransactionImpl) tx).preparedCommit();
            } catch (SystemException e) {
                throw new XAException();
            }
        }
    }

    public void forget(Transaction tx) throws XAException {
    }

    public int prepare(Transaction tx) throws XAException {
        try {
            return ((TransactionImpl) tx).prepare();
        } catch (SystemException e) {
            throw new XAException();
        } catch (RollbackException e) {
            throw new XAException();
        }
    }

    public void rollback(Transaction tx) throws XAException {
        try {
            tx.rollback();
        } catch (IllegalStateException e) {
            throw new XAException();
        } catch (SystemException e) {
            throw new XAException();
        }
    }

    public void setTransactionTimeout(long milliseconds) {
    }

    private static class TransactionTimeout extends TimerTask {

        private final TransactionImpl tx;

        public TransactionTimeout(TransactionImpl tx) {
            this.tx = tx;
        }

        public void run() {
            try {
                tx.setRollbackOnly();
            } catch (SystemException e) {
                //??
            } catch (IllegalStateException ise) {
                //transaction was committed
            }
        }
    }

}
