/* ====================================================================
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 2003 The Apache Software Foundation.  All rights
 * reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. The end-user documentation included with the redistribution,
 *    if any, must include the following acknowledgment:
 *       "This product includes software developed by the
 *        Apache Software Foundation (http://www.apache.org/)."
 *    Alternately, this acknowledgment may appear in the software itself,
 *    if and wherever such third-party acknowledgments normally appear.
 *
 * 4. The names "Apache" and "Apache Software Foundation" and
 *    "Apache Geronimo" must not be used to endorse or promote products
 *    derived from this software without prior written permission. For
 *    written permission, please contact apache@apache.org.
 *
 * 5. Products derived from this software may not be called "Apache",
 *    "Apache Geronimo", nor may "Apache" appear in their name, without
 *    prior written permission of the Apache Software Foundation.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE APACHE SOFTWARE FOUNDATION OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 *
 * ====================================================================
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
import javax.transaction.xa.Xid;
import javax.transaction.xa.XAException;

import org.apache.geronimo.transaction.log.UnrecoverableLog;

/**
 * Simple implementation of a local transaction manager.
 *
 * @version $Revision: 1.2 $ $Date: 2004/02/23 20:28:43 $
 */
public class TransactionManagerImpl implements TransactionManager, XidImporter {
    private final TransactionLog txnLog;
    private final XidFactory xidFactory = new XidFactory();
    private volatile int timeout;
    private final ThreadLocal threadTx = new ThreadLocal();

    public TransactionManagerImpl() {
        txnLog = new UnrecoverableLog();
    }

    public TransactionManagerImpl(TransactionLog txnLog) {
        this.txnLog = txnLog;
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
