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

package org.apache.geronimo.transaction;

import java.util.Map;
import java.util.HashMap;
import java.util.Set;
import java.util.HashSet;

import javax.resource.spi.XATerminator;
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
import org.apache.geronimo.gbean.GOperationInfo;
import org.apache.geronimo.gbean.GConstructorInfo;
import org.apache.geronimo.gbean.GAttributeInfo;
import org.apache.geronimo.transaction.manager.TransactionManagerImpl;
import org.apache.geronimo.transaction.manager.XidImporter;

/**
 * A wrapper for a TransactionManager that wraps all Transactions in a TransactionProxy
 * so that we can add addition metadata to the Transaction. Only begin (and setTransactionTimeout)
 * are delegated to the wrapped TransactionManager; all other operations are delegated to the
 * wrapped Transaction.
 *
 * @version $Revision: 1.4 $ $Date: 2004/02/25 09:58:19 $
 */
public class TransactionManagerProxy implements TransactionManager, XATerminator, XAWork {

    public static final GBeanInfo GBEAN_INFO;

    private final TransactionManager delegate;
    private final XidImporter importer;
    private final ThreadLocal threadTx = new ThreadLocal();
    private final Map importedTransactions = new HashMap();
    private Set activeTransactions = new HashSet();

    /**
     * Constructor taking the TransactionManager to wrap.
     * @param delegate the TransactionManager that should be wrapped
     */
    public TransactionManagerProxy(TransactionManager delegate, XidImporter importer) {
        this.delegate = delegate;
        this.importer = importer;
    }

    /**
     * Possibly temporary constructor to enable deploying this as a geronimo mbean w/o a managed constructor.
     */
    public TransactionManagerProxy() {
        this.delegate = new TransactionManagerImpl();
        this.importer = null;
    }

    public void setTransactionTimeout(int timeout) throws SystemException {
        delegate.setTransactionTimeout(timeout);
    }

    public void begin() throws NotSupportedException, SystemException {
        delegate.begin();
        threadTx.set(new TransactionProxy(delegate.getTransaction()));
    }

    public int getStatus() throws SystemException {
        Transaction tx = getTransaction();
        return (tx != null) ? tx.getStatus() : Status.STATUS_NO_TRANSACTION;
    }

    public Transaction getTransaction() throws SystemException {
        return (Transaction) threadTx.get();
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
        if (tx instanceof TransactionProxy == false) {
            throw new InvalidTransactionException("Cannot resume foreign transaction: " + tx);
        }
        threadTx.set(tx);
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

    public void setRollbackOnly() throws IllegalStateException, SystemException {
        Transaction tx = getTransaction();
        if (tx == null) {
            throw new IllegalStateException("No transaction associated with current thread");
        }
        tx.setRollbackOnly();
    }

    /**
     * @see javax.resource.spi.XATerminator#commit(javax.transaction.xa.Xid, boolean)
     */
    public void commit(Xid xid, boolean onePhase) throws XAException {
        TransactionProxy tx = (TransactionProxy) importedTransactions.remove(xid);
        if (tx == null) {
            throw new XAException("No imported transaction for xid: " + xid);
        }

        try {
            int status = tx.getStatus();
            assert status == Status.STATUS_ACTIVE || status == Status.STATUS_PREPARED;
        } catch (SystemException e) {
            throw new XAException();
        }
        importer.commit(tx.getDelegate(), onePhase);
    }

    /**
     * @see javax.resource.spi.XATerminator#forget(javax.transaction.xa.Xid)
     */
    public void forget(Xid xid) throws XAException {
        TransactionProxy tx = (TransactionProxy) importedTransactions.remove(xid);
        if (tx == null) {
            throw new XAException("No imported transaction for xid: " + xid);
        }

        try {
            int status = tx.getStatus();
            //assert status == Status.STATUS_ACTIVE || status == Status.STATUS_PREPARED;
        } catch (SystemException e) {
            throw new XAException();
        }
        importer.forget(tx.getDelegate());
    }

    /**
     * @see javax.resource.spi.XATerminator#prepare(javax.transaction.xa.Xid)
     */
    public int prepare(Xid xid) throws XAException {
        TransactionProxy tx = (TransactionProxy) importedTransactions.get(xid);
        if (tx == null) {
            throw new XAException("No imported transaction for xid: " + xid);
        }

        try {
            int status = tx.getStatus();
            assert status == Status.STATUS_ACTIVE;
        } catch (SystemException e) {
            throw new XAException();
        }
        return importer.prepare(tx.getDelegate());
    }

    /**
     * @see javax.resource.spi.XATerminator#recover(int)
     */
    public Xid[] recover(int arg0) throws XAException {
        throw new XAException("Not implemented.");
    }

    /**
     * @see javax.resource.spi.XATerminator#rollback(javax.transaction.xa.Xid)
     */
    public void rollback(Xid xid) throws XAException {
        TransactionProxy tx = (TransactionProxy) importedTransactions.remove(xid);
        if (tx == null) {
            throw new XAException("No imported transaction for xid: " + xid);
        }

        try {
            int status = tx.getStatus();
            assert status == Status.STATUS_ACTIVE || status == Status.STATUS_PREPARED;
        } catch (SystemException e) {
            throw new XAException();
        }
        importer.rollback(tx.getDelegate());
    }

    public void begin(Xid xid, long txTimeoutMillis) throws XAException {
        TransactionProxy tx = (TransactionProxy) importedTransactions.get(xid);
        if (tx == null) {
            try {
                tx = new TransactionProxy(importer.importXid(xid));
            } catch (SystemException e) {
                throw (XAException)new XAException("Could not import xid").initCause(e);
            }
            importedTransactions.put(xid, tx);
        }
        if (activeTransactions.contains(tx)) {
            throw new XAException("Xid already active");
        }
        activeTransactions.add(tx);
        threadTx.set(tx);
        importer.setTransactionTimeout(txTimeoutMillis);
    }

    public void end(Xid xid) throws XAException {
        TransactionProxy tx = (TransactionProxy) importedTransactions.get(xid);
        if (tx == null) {
            throw new XAException("No imported transaction for xid: " + xid);
        }
        if (!activeTransactions.remove(tx)) {
            throw new XAException("tx not active for xid: " + xid);
        }
    }

    //for now we use the default constructor.
    static {
        GBeanInfoFactory infoFactory = new GBeanInfoFactory(TransactionManagerProxy.class.getName());

        infoFactory.setConstructor(new GConstructorInfo(
                new String[]{"Delegate"},
                new Class[]{TransactionManager.class}));

        infoFactory.addAttribute(new GAttributeInfo("Delegate", true));

        infoFactory.addOperation(new GOperationInfo("setTransactionTimeout", new String[]{Integer.TYPE.getName()}));
        infoFactory.addOperation(new GOperationInfo("begin"));
        infoFactory.addOperation(new GOperationInfo("getStatus"));
        infoFactory.addOperation(new GOperationInfo("getTransaction"));
        infoFactory.addOperation(new GOperationInfo("suspend"));
        infoFactory.addOperation(new GOperationInfo("resume", new String[]{Transaction.class.getName()}));
        infoFactory.addOperation(new GOperationInfo("commit"));
        infoFactory.addOperation(new GOperationInfo("rollback"));
        infoFactory.addOperation(new GOperationInfo("setRollbackOnly"));
        GBEAN_INFO = infoFactory.getBeanInfo();
    }

    public static GBeanInfo getGBeanInfo() {
        return GBEAN_INFO;
    }

}
