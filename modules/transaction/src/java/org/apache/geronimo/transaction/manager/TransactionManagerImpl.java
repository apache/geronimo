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

import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.gbean.GBeanInfoFactory;
import org.apache.geronimo.transaction.log.UnrecoverableLog;

/**
 * Simple implementation of a transaction manager.
 * TODO timeout functionality
 *
 * @version $Revision: 1.6 $ $Date: 2004/06/11 19:20:55 $
 */
public class TransactionManagerImpl implements TransactionManager, XidImporter {
    private final TransactionLog txnLog;
    private final XidFactory xidFactory;
    private volatile int timeout;
    private final ThreadLocal threadTx = new ThreadLocal();

    public TransactionManagerImpl() {
        txnLog = new UnrecoverableLog();
        xidFactory = new XidFactoryImpl();
    }

    public TransactionManagerImpl(TransactionLog txnLog, XidFactory xidFactory) {
        this.txnLog = txnLog;
        this.xidFactory = xidFactory;
    }

    public Transaction getTransaction() throws SystemException {
        return (Transaction) threadTx.get();
    }

    public void setTransactionTimeout(int seconds) throws SystemException {
        timeout = seconds;
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

    public Transaction importXid(Xid xid) throws XAException, SystemException {
        if (getStatus() != Status.STATUS_NO_TRANSACTION) {
            throw new XAException("Transaction already active in this thread");
        }
        TransactionImpl tx = new TransactionImpl(xid, xidFactory, txnLog);
        threadTx.set(tx);
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
                ((TransactionImpl)tx).preparedCommit();
            } catch (SystemException e) {
                throw new XAException();
            }
        }
    }

    public void forget(Transaction tx) throws XAException {
    }

    public int prepare(Transaction tx) throws XAException {
        try {
            return ((TransactionImpl)tx).prepare();
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

}
