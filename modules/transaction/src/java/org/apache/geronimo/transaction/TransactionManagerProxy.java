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
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.gbean.GBeanInfoFactory;
import org.apache.geronimo.gbean.GBeanLifecycle;
import org.apache.geronimo.gbean.ReferenceCollection;
import org.apache.geronimo.gbean.ReferenceCollectionEvent;
import org.apache.geronimo.gbean.ReferenceCollectionListener;
import org.apache.geronimo.gbean.WaitingException;
import org.apache.geronimo.transaction.context.TransactionContext;
import org.apache.geronimo.transaction.context.UnspecifiedTransactionContext;
import org.apache.geronimo.transaction.manager.NamedXAResource;
import org.apache.geronimo.transaction.manager.Recovery;
import org.apache.geronimo.transaction.manager.ResourceManager;
import org.apache.geronimo.transaction.manager.XidImporter;

/**
 * A wrapper for a TransactionManager that wraps all Transactions in a TransactionProxy
 * so that we can add addition metadata to the Transaction. Only begin (and setTransactionTimeout)
 * are delegated to the wrapped TransactionManager; all other operations are delegated to the
 * wrapped Transaction.
 *
 * @version $Rev$ $Date$
 */
public class TransactionManagerProxy implements TransactionManager, XidImporter, Recovery, GBeanLifecycle {
    private static final Log recoveryLog = LogFactory.getLog("RecoveryController");

    private final TransactionManager delegate;
    private final XidImporter importer;
    private final ThreadLocal threadTx = new ThreadLocal();
    private final Recovery recovery;
    private final ReferenceCollection resourceManagers;
    private List recoveryErrors = new ArrayList();

    /**
     * Constructor taking the TransactionManager to wrap.
     *
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

    //TODO NOTE!!! this should be called in an unspecified transaction context, but we cannot enforce this restriction!
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


    //XidImporter implementation. Wrap and unwrap TransactionProxy.
    //the importer functions should not affect the thread context.
    public Transaction importXid(Xid xid) throws XAException, SystemException {
        if (threadTx.get() != null) {
            throw new IllegalStateException("Transaction already associated with current thread");
        }
        TransactionProxy transactionProxy = new TransactionProxy(importer.importXid(xid));
//        threadTx.set(transactionProxy);
        return transactionProxy;
    }

    //TODO how do these relate to threadTx???? probably not at all...
    public void commit(Transaction tx, boolean onePhase) throws XAException {
        importer.commit(((TransactionProxy) tx).getDelegate(), onePhase);
    }

    public void forget(Transaction tx) throws XAException {
        importer.forget(((TransactionProxy) tx).getDelegate());
    }

    public int prepare(Transaction tx) throws XAException {
        return importer.prepare(((TransactionProxy) tx).getDelegate());
    }

    public void rollback(Transaction tx) throws XAException {
        importer.rollback(((TransactionProxy) tx).getDelegate());
    }

    public void setTransactionTimeout(long milliseconds) {
        importer.setTransactionTimeout(milliseconds);
    }

    //Recovery implementation
    //TODO make an interface of only getExternalIds since other methods don't work.
    //Or, decide to expose the other methods.
    public void recoverLog() throws XAException {
        throw new IllegalStateException("Don't call this");
    }

    public void recoverResourceManager(NamedXAResource xaResource) throws XAException {
        throw new IllegalStateException("Don't call this");
    }

    public boolean hasRecoveryErrors() {
        throw new IllegalStateException("Don't call this");
    }

    public List getRecoveryErrors() {
        throw new IllegalStateException("Don't call this");
    }

    public boolean localRecoveryComplete() {
        throw new IllegalStateException("Don't call this");
    }

    public int localUnrecoveredCount() {
        throw new IllegalStateException("Don't call this");
    }

    public Map getExternalXids() {
        Map internal = recovery.getExternalXids();
        Map external = new HashMap(internal.size());
        for (Iterator iterator = internal.entrySet().iterator(); iterator.hasNext();) {
            Map.Entry entry = (Map.Entry) iterator.next();
            Transaction tx = (Transaction) entry.getValue();
            external.put(entry.getKey(), new TransactionProxy(tx));
        }
        return external;
    }

    public static final GBeanInfo GBEAN_INFO;

    static {
        GBeanInfoFactory infoFactory = new GBeanInfoFactory(TransactionManagerProxy.class);

        infoFactory.addReference("delegate", TransactionManager.class);
        infoFactory.addReference("xidImporter", XidImporter.class);
        infoFactory.addReference("recovery", Recovery.class);
        infoFactory.addReference("resourceManagers", ResourceManager.class);

        infoFactory.addInterface(TransactionManager.class);
        infoFactory.addInterface(XidImporter.class);

        infoFactory.setConstructor(new String[]{"delegate", "xidImporter", "recovery", "resourceManagers"});

        GBEAN_INFO = infoFactory.getBeanInfo();
    }

    public static GBeanInfo getGBeanInfo() {
        return GBEAN_INFO;
    }

}
