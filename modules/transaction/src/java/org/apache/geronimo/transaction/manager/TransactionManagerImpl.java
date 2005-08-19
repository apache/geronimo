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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.geronimo.transaction.ExtendedTransactionManager;
import org.apache.geronimo.transaction.log.UnrecoverableLog;

import javax.transaction.*;
import javax.transaction.xa.XAException;
import javax.transaction.xa.Xid;
import java.util.*;

/**
 * Simple implementation of a transaction manager.
 *
 * @version $Rev$ $Date$
 */
public class TransactionManagerImpl implements ExtendedTransactionManager, XidImporter {
    final TransactionLog transactionLog;
    final XidFactory xidFactory;
    private final int defaultTransactionTimeoutMilliseconds;
    private final ThreadLocal transactionTimeoutMilliseconds = new ThreadLocal();
    private final ThreadLocal threadTx = new ThreadLocal();
    private static final Log recoveryLog = LogFactory.getLog("RecoveryController");
    final Recovery recovery;
    final Collection resourceManagers;
    private List recoveryErrors = new ArrayList();

    /**
     * TODO NOTE!!! this should be called in an unspecified transaction context, but we cannot enforce this restriction!
     */
    public TransactionManagerImpl(int defaultTransactionTimeoutSeconds, TransactionLog transactionLog, Collection resourceManagers) throws XAException {
        if (defaultTransactionTimeoutSeconds <= 0) {
            throw new IllegalArgumentException("defaultTransactionTimeoutSeconds must be positive: attempted value: " + defaultTransactionTimeoutSeconds);
        }

        this.defaultTransactionTimeoutMilliseconds = defaultTransactionTimeoutSeconds * 1000;
        this.transactionLog = transactionLog == null ? new UnrecoverableLog() : transactionLog;
        this.xidFactory = new XidFactoryImpl("WHAT DO WE CALL IT?".getBytes());
        this.resourceManagers = resourceManagers;
        recovery = new RecoveryImpl(this.transactionLog, this.xidFactory);

        if (resourceManagers != null) {
            recovery.recoverLog();
            List copy = watchResourceManagers(resourceManagers);
            for (Iterator iterator = copy.iterator(); iterator.hasNext();) {
                ResourceManager resourceManager = (ResourceManager) iterator.next();
                recoverResourceManager(resourceManager);
            }
        }
    }

    protected List watchResourceManagers(Collection resourceManagers) {
        return new ArrayList(resourceManagers);
    }


    public Transaction getTransaction() throws SystemException {
        return (Transaction) threadTx.get();
    }

    public void setTransactionTimeout(int seconds) throws SystemException {
        if (seconds < 0) {
            throw new SystemException("transaction timeout must be positive or 0 to reset to default");
        }
        if (seconds == 0) {
            transactionTimeoutMilliseconds.set(null);
        } else {
            transactionTimeoutMilliseconds.set(new Long(seconds * 1000));
        }
    }

    public int getStatus() throws SystemException {
        Transaction tx = getTransaction();
        return (tx != null) ? tx.getStatus() : Status.STATUS_NO_TRANSACTION;
    }

    public void begin() throws NotSupportedException, SystemException {
        begin(getTransactionTimeoutMilliseconds(0L));
    }

    public Transaction begin(long transactionTimeoutMilliseconds) throws NotSupportedException, SystemException {
        if (getStatus() != Status.STATUS_NO_TRANSACTION) {
            throw new NotSupportedException("Nested Transactions are not supported");
        }
        TransactionImpl tx = new TransactionImpl(xidFactory, transactionLog, getTransactionTimeoutMilliseconds(transactionTimeoutMilliseconds));
//        timeoutTimer.schedule(tx, getTransactionTimeoutMilliseconds(transactionTimeoutMilliseconds));
        threadTx.set(tx);
                // Todo: Verify if this is correct thing to do. Use default timeout for next transaction.
        this.transactionTimeoutMilliseconds.set(null);
        return tx;
    }

    public Transaction suspend() throws SystemException {
        Transaction tx = getTransaction();
        if (tx != null) {
        }
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
    public Transaction importXid(Xid xid, long transactionTimeoutMilliseconds) throws XAException, SystemException {
        if (transactionTimeoutMilliseconds < 0) {
            throw new SystemException("transaction timeout must be positive or 0 to reset to default");
        }
        TransactionImpl tx = new TransactionImpl(xid, xidFactory, transactionLog, getTransactionTimeoutMilliseconds(transactionTimeoutMilliseconds));
        return tx;
    }

    public void commit(Transaction tx, boolean onePhase) throws XAException {
        if (onePhase) {
            try {
                tx.commit();
            } catch (HeuristicMixedException e) {
                throw (XAException) new XAException().initCause(e);
            } catch (HeuristicRollbackException e) {
                throw (XAException) new XAException().initCause(e);
            } catch (RollbackException e) {
                throw (XAException) new XAException().initCause(e);
            } catch (SecurityException e) {
                throw (XAException) new XAException().initCause(e);
            } catch (SystemException e) {
                throw (XAException) new XAException().initCause(e);
            }
        } else {
            try {
                ((TransactionImpl) tx).preparedCommit();
            } catch (SystemException e) {
                throw (XAException) new XAException().initCause(e);
            }
        }
    }

    public void forget(Transaction tx) throws XAException {
        //TODO implement this!
    }

    public int prepare(Transaction tx) throws XAException {
        try {
            return ((TransactionImpl) tx).prepare();
        } catch (SystemException e) {
            throw (XAException) new XAException().initCause(e);
        } catch (RollbackException e) {
            throw (XAException) new XAException().initCause(e);
        }
    }

    public void rollback(Transaction tx) throws XAException {
        try {
            tx.rollback();
        } catch (IllegalStateException e) {
            throw (XAException) new XAException().initCause(e);
        } catch (SystemException e) {
            throw (XAException) new XAException().initCause(e);
        }
    }

    long getTransactionTimeoutMilliseconds(long transactionTimeoutMilliseconds) {
        if (transactionTimeoutMilliseconds != 0) {
            return transactionTimeoutMilliseconds;
        }
        Long timeout = (Long) this.transactionTimeoutMilliseconds.get();
        if (timeout != null) {
            return timeout.longValue();
        }
        return defaultTransactionTimeoutMilliseconds;
    }

    protected void recoverResourceManager(ResourceManager resourceManager) {
        NamedXAResource namedXAResource = null;
        try {
            namedXAResource = resourceManager.getRecoveryXAResources();
        } catch (SystemException e) {
            recoveryLog.error(e);
            recoveryErrors.add(e);
            return;
        }
        if (namedXAResource != null) {
            try {
                recovery.recoverResourceManager(namedXAResource);
            } catch (XAException e) {
                recoveryLog.error(e);
                recoveryErrors.add(e);
            } finally {
                resourceManager.returnResource(namedXAResource);
            }
        }
    }


    public Map getExternalXids() {
        return new HashMap(recovery.getExternalXids());
    }

}
