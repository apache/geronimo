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

package org.apache.geronimo.transaction;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

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
import javax.transaction.xa.XAResource;
import javax.transaction.xa.Xid;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.gbean.GBeanInfoFactory;
import org.apache.geronimo.gbean.GBeanLifecycle;
import org.apache.geronimo.gbean.ReferenceCollection;
import org.apache.geronimo.gbean.ReferenceCollectionEvent;
import org.apache.geronimo.gbean.ReferenceCollectionListener;
import org.apache.geronimo.gbean.WaitingException;
import org.apache.geronimo.transaction.manager.NamedXAResource;
import org.apache.geronimo.transaction.manager.Recovery;
import org.apache.geronimo.transaction.manager.ResourceManager;
import org.apache.geronimo.transaction.manager.XidImporter;
import org.apache.geronimo.transaction.context.TransactionContext;
import org.apache.geronimo.transaction.context.UnspecifiedTransactionContext;

/**
 * A wrapper for a TransactionManager that wraps all Transactions in a TransactionProxy
 * so that we can add addition metadata to the Transaction. Only begin (and setTransactionTimeout)
 * are delegated to the wrapped TransactionManager; all other operations are delegated to the
 * wrapped Transaction.
 *
 * @version $Rev$ $Date$
 */
public class TransactionManagerProxy implements TransactionManager, XATerminator, XAWork, GBeanLifecycle {
    private static final boolean NOT_IN_RECOVERY = false;
    private static final boolean IN_RECOVERY = true;

    private static final Log recoveryLog = LogFactory.getLog("RecoveryController");

    private final TransactionManager delegate;
    private final XidImporter importer;
    private final ThreadLocal threadTx = new ThreadLocal();
    private final Map importedTransactions = new HashMap();
    private boolean recoveryState = NOT_IN_RECOVERY;
    private final Recovery recovery;
    private final ReferenceCollection resourceManagers;
    private List recoveryErrors = new ArrayList();

    /**
     * Constructor taking the TransactionManager to wrap.
     * @param delegate the TransactionManager that should be wrapped
     */
    public TransactionManagerProxy(TransactionManager delegate, XidImporter importer, Recovery recovery, Collection resourceManagers) {
        assert delegate != null;
        assert importer != null;
        assert recovery != null;
        assert resourceManagers != null;
        this.delegate = delegate;
        this.importer = importer;
        this.recovery = recovery;
        this.resourceManagers = (ReferenceCollection) resourceManagers;
    }

    public static class ConstructorParams {
        TransactionManager delegate;
        XidImporter xidImporter;
        Recovery recovery;
        ReferenceCollection resourceManagers;
    }

    public TransactionManagerProxy(ConstructorParams params) {
        this(params.delegate, params.xidImporter, params.recovery, params.resourceManagers);
    }

    public void doStart() throws WaitingException, Exception {
        recovery.recoverLog();
        List copy = null;
        synchronized (resourceManagers) {
            copy = new ArrayList(resourceManagers);
            resourceManagers.addReferenceCollectionListener(new ReferenceCollectionListener() {
                public void memberAdded(ReferenceCollectionEvent event) {
                    ResourceManager resourceManager = (ResourceManager) event.getMember();
                    recoverResourceManager(resourceManager);
                }

                public void memberRemoved(ReferenceCollectionEvent event) {
                }

            });
        }
        for (Iterator iterator = copy.iterator(); iterator.hasNext();) {
            ResourceManager resourceManager = (ResourceManager) iterator.next();
            recoverResourceManager(resourceManager);
        }
        //what to do if there are recovery errors? or not all resource managers are online?
    }

    private void recoverResourceManager(ResourceManager resourceManager) {
        TransactionContext oldTransactionContext = TransactionContext.getContext();
        try {
            TransactionContext.setContext(new UnspecifiedTransactionContext());
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
        } finally {
            TransactionContext.setContext(oldTransactionContext);
        }
    }

    public void doStop() throws WaitingException, Exception {
    }

    public void doFail() {
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
        ImportedTransactionInfo txInfo;
        synchronized (importedTransactions) {
            txInfo = (ImportedTransactionInfo) importedTransactions.remove(xid);
        }
        if (txInfo == null) {
            throw new XAException("No imported transaction for xid: " + xid);
        }
        TransactionProxy tx = txInfo.getTransactionProxy();

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
        ImportedTransactionInfo txInfo;
        synchronized (importedTransactions) {
            txInfo = (ImportedTransactionInfo) importedTransactions.remove(xid);
        }
        if (txInfo == null) {
            throw new XAException("No imported transaction for xid: " + xid);
        }
        TransactionProxy tx = txInfo.getTransactionProxy();
        //todo is there a correct status test here?
//        try {
//            int status = tx.getStatus();
//            assert status == Status.STATUS_ACTIVE || status == Status.STATUS_PREPARED;
//        } catch (SystemException e) {
//            throw new XAException();
//        }
        importer.forget(tx.getDelegate());
    }

    /**
     * @see javax.resource.spi.XATerminator#prepare(javax.transaction.xa.Xid)
     */
    public int prepare(Xid xid) throws XAException {
        ImportedTransactionInfo txInfo;
        synchronized (importedTransactions) {
            txInfo = (ImportedTransactionInfo) importedTransactions.get(xid);
        }
        if (txInfo == null) {
            throw new XAException("No imported transaction for xid: " + xid);
        }
        TransactionProxy tx = txInfo.getTransactionProxy();
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
    public Xid[] recover(int flag) throws XAException {
        if (recoveryState == NOT_IN_RECOVERY) {
            if ((flag & XAResource.TMSTARTRSCAN) == 0) {
                throw new XAException(XAException.XAER_PROTO);
            }
            recoveryState = IN_RECOVERY;
        }
        if ((flag & XAResource.TMENDRSCAN) != 0) {
            recoveryState = NOT_IN_RECOVERY;
        }
        //we always return all xids in first call.
        //calling "startrscan" repeatedly starts at beginning of list again.
        if ((flag & XAResource.TMSTARTRSCAN) != 0) {
            Map recoveredXidMap = recovery.getExternalXids();
            Xid[] recoveredXids = new Xid[recoveredXidMap.size()];
            int i = 0;
            synchronized (importedTransactions) {
                for (Iterator iterator = recoveredXidMap.entrySet().iterator(); iterator.hasNext();) {
                    Map.Entry entry = (Map.Entry) iterator.next();
                    Xid xid = (Xid) entry.getKey();
                    recoveredXids[i++] = xid;
                    ImportedTransactionInfo txInfo = new ImportedTransactionInfo(new TransactionProxy((Transaction)entry.getValue()));
                    importedTransactions.put(xid, txInfo);
                }
            }
            return recoveredXids;
        } else {
            return new Xid[0];
        }
    }

    /**
     * @see javax.resource.spi.XATerminator#rollback(javax.transaction.xa.Xid)
     */
    public void rollback(Xid xid) throws XAException {
        ImportedTransactionInfo txInfo;
        synchronized (importedTransactions) {
            txInfo = (ImportedTransactionInfo) importedTransactions.remove(xid);
        }
        if (txInfo == null) {
            throw new XAException("No imported transaction for xid: " + xid);
        }
        TransactionProxy tx = txInfo.getTransactionProxy();

        try {
            int status = tx.getStatus();
            assert status == Status.STATUS_ACTIVE || status == Status.STATUS_PREPARED;
        } catch (SystemException e) {
            throw new XAException();
        }
        importer.rollback(tx.getDelegate());
    }

    public void begin(Xid xid, long txTimeoutMillis) throws XAException, InvalidTransactionException, SystemException {
        ImportedTransactionInfo txInfo;
        boolean old = true;
        synchronized (importedTransactions) {
             txInfo = (ImportedTransactionInfo) importedTransactions.get(xid);
            if (txInfo == null) {
                try {
                    txInfo = new ImportedTransactionInfo(new TransactionProxy(importer.importXid(xid)));
                    old = false;
                } catch (SystemException e) {
                    throw (XAException) new XAException("Could not import xid").initCause(e);
                }
                importedTransactions.put(xid, txInfo);
            }
            if (txInfo.isActive()) {
                throw new XAException("Xid already active");
            }
            txInfo.setActive(true);
        }
        threadTx.set(txInfo.getTransactionProxy());
        if (old) {
            delegate.resume(txInfo.getTransactionProxy().getDelegate());
        }
        importer.setTransactionTimeout(txTimeoutMillis);
    }

    public void end(Xid xid) throws XAException, SystemException {
        synchronized (importedTransactions) {
            ImportedTransactionInfo txInfo = (ImportedTransactionInfo) importedTransactions.get(xid);
            if (txInfo == null) {
                throw new XAException("No imported transaction for xid: " + xid);
            }
            if (!txInfo.isActive()) {
                throw new XAException("tx not active for xid: " + xid);
            }
            txInfo.setActive(false);
        }
        threadTx.set(null);
        delegate.suspend();
    }

    private static class ImportedTransactionInfo {
        private final TransactionProxy transactionProxy;
        private boolean active;

        public ImportedTransactionInfo(TransactionProxy transactionProxy) {
            this.transactionProxy = transactionProxy;
        }

        public TransactionProxy getTransactionProxy() {
            return transactionProxy;
        }

        public boolean isActive() {
            return active;
        }

        public void setActive(boolean active) {
            this.active = active;
        }
    }

    public static final GBeanInfo GBEAN_INFO;

    static {
        GBeanInfoFactory infoFactory = new GBeanInfoFactory(TransactionManagerProxy.class);

        infoFactory.addReference("delegate", TransactionManager.class);
        infoFactory.addReference("xidImporter", XidImporter.class);
        infoFactory.addReference("recovery", Recovery.class);
        infoFactory.addReference("resourceManagers", ResourceManager.class);

        infoFactory.addOperation("setTransactionTimeout", new Class[]{int.class});
        infoFactory.addOperation("begin");
        infoFactory.addOperation("getStatus");
        infoFactory.addOperation("getTransaction");
        infoFactory.addOperation("suspend");
        infoFactory.addOperation("resume", new Class[]{Transaction.class});
        infoFactory.addOperation("commit");
        infoFactory.addOperation("rollback");
        infoFactory.addOperation("setRollbackOnly");

        infoFactory.setConstructor(new String[]{"delegate", "xidImporter", "recovery", "resourceManagers"});

        GBEAN_INFO = infoFactory.getBeanInfo();
    }

    public static GBeanInfo getGBeanInfo() {
        return GBEAN_INFO;
    }

}
