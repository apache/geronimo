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

package org.apache.geronimo.transaction;

import javax.transaction.HeuristicMixedException;
import javax.transaction.HeuristicRollbackException;
import javax.transaction.InvalidTransactionException;
import javax.transaction.NotSupportedException;
import javax.transaction.RollbackException;
import javax.transaction.Status;
import javax.transaction.SystemException;
import javax.transaction.Transaction;
import javax.transaction.TransactionManager;

import org.apache.geronimo.transaction.InheritableTransactionContext;

/**
 *
 *
 * @version $Revision: 1.1 $ $Date: 2004/01/31 19:27:17 $
 */
public class ContainerTransactionContext extends InheritableTransactionContext {
    private final TransactionManager txnManager;
    private Transaction transaction;

    public ContainerTransactionContext(TransactionManager txnManager) {
        this.txnManager = txnManager;
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

    /**
     * TODO the exceptions thrown here are not all correct.  Don't throw a RollbackException after
     * a successful commit...??
     *
     * @throws javax.transaction.HeuristicMixedException
     * @throws javax.transaction.HeuristicRollbackException
     * @throws javax.transaction.RollbackException
     * @throws javax.transaction.SystemException
     */
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
            try {
                beforeCommit();
            } catch (Exception e) {
                try {
                    txnManager.rollback();
                } catch (Throwable t1) {
                    log.error("Unable to roll back transaction", t1);
                }
                throw (RollbackException) new RollbackException("Could not flush state before commit").initCause(e);
            }
            txnManager.commit();
            try {
                afterCommit(true);
            } catch (Exception e) {
                try {
                    txnManager.rollback();
                } catch (Throwable t1) {
                    log.error("Unable to roll back transaction", t1);
                }
                throw (RollbackException) new RollbackException("Could not flush state before commit").initCause(e);
            }
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
                status == Status.STATUS_ROLLING_BACK );
    }
}
