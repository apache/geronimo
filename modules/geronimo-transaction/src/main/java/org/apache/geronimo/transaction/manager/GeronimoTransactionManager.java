/**
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
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

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import javax.resource.spi.XATerminator;
import javax.transaction.InvalidTransactionException;
import javax.transaction.Status;
import javax.transaction.SystemException;
import javax.transaction.Transaction;
import javax.transaction.xa.XAException;
import javax.transaction.xa.XAResource;
import javax.transaction.xa.Xid;

/**
 * @version $Rev$ $Date$
 */
public class GeronimoTransactionManager extends TransactionManagerImpl implements XATerminator, XAWork {
    private final Map importedTransactions = new HashMap();
    private boolean isInRecovery = false;

    public GeronimoTransactionManager() throws XAException {
    }

    public GeronimoTransactionManager(int defaultTransactionTimeoutSeconds) throws XAException {
        super(defaultTransactionTimeoutSeconds);
    }

    public GeronimoTransactionManager(int defaultTransactionTimeoutSeconds, TransactionLog transactionLog) throws XAException {
        super(defaultTransactionTimeoutSeconds, transactionLog);
    }

    public GeronimoTransactionManager(int defaultTransactionTimeoutSeconds, XidFactory xidFactory, TransactionLog transactionLog) throws XAException {
        super(defaultTransactionTimeoutSeconds, xidFactory, transactionLog);
    }

    /**
     * @see javax.resource.spi.XATerminator#commit(javax.transaction.xa.Xid, boolean)
     */
    public void commit(Xid xid, boolean onePhase) throws XAException {
        Transaction importedTransaction;
        synchronized (importedTransactions) {
            importedTransaction = (Transaction) importedTransactions.remove(xid);
        }
        if (importedTransaction == null) {
            throw new XAException("No imported transaction for xid: " + xid);
        }

        try {
            int status = importedTransaction.getStatus();
            assert status == Status.STATUS_ACTIVE || status == Status.STATUS_PREPARED: "invalid status: " + status;
        } catch (SystemException e) {
            throw (XAException)new XAException().initCause(e);
        }
        commit(importedTransaction, onePhase);
    }

    /**
     * @see javax.resource.spi.XATerminator#forget(javax.transaction.xa.Xid)
     */
    public void forget(Xid xid) throws XAException {
        Transaction importedTransaction;
        synchronized (importedTransactions) {
            importedTransaction = (Transaction) importedTransactions.remove(xid);
        }
        if (importedTransaction == null) {
            throw new XAException("No imported transaction for xid: " + xid);
        }
        //todo is there a correct status test here?
//        try {
//            int status = tx.getStatus();
//            assert status == Status.STATUS_ACTIVE || status == Status.STATUS_PREPARED;
//        } catch (SystemException e) {
//            throw new XAException();
//        }
        forget(importedTransaction);
    }

    /**
     * @see javax.resource.spi.XATerminator#prepare(javax.transaction.xa.Xid)
     */
    public int prepare(Xid xid) throws XAException {
        Transaction importedTransaction;
        synchronized (importedTransactions) {
            importedTransaction = (Transaction) importedTransactions.get(xid);
        }
        if (importedTransaction == null) {
            throw new XAException("No imported transaction for xid: " + xid);
        }
        try {
            int status = importedTransaction.getStatus();
            assert status == Status.STATUS_ACTIVE;
        } catch (SystemException e) {
            throw (XAException)new XAException().initCause(e);
        }
        return prepare(importedTransaction);
    }

    /**
     * @see javax.resource.spi.XATerminator#recover(int)
     */
    public Xid[] recover(int flag) throws XAException {
        if (!isInRecovery) {
            if ((flag & XAResource.TMSTARTRSCAN) == 0) {
                throw new XAException(XAException.XAER_PROTO);
            }
            isInRecovery = true;
        }
        if ((flag & XAResource.TMENDRSCAN) != 0) {
            isInRecovery = false;
        }
        //we always return all xids in first call.
        //calling "startrscan" repeatedly starts at beginning of list again.
        if ((flag & XAResource.TMSTARTRSCAN) != 0) {
            Map recoveredXidMap = getExternalXids();
            Xid[] recoveredXids = new Xid[recoveredXidMap.size()];
            int i = 0;
            synchronized (importedTransactions) {
                for (Iterator iterator = recoveredXidMap.entrySet().iterator(); iterator.hasNext();) {
                    Map.Entry entry = (Map.Entry) iterator.next();
                    Xid xid = (Xid) entry.getKey();
                    recoveredXids[i++] = xid;
                    Transaction transaction = (Transaction) entry.getValue();
                    importedTransactions.put(xid, transaction);
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
        Transaction importedTransaction;
        synchronized (importedTransactions) {
            importedTransaction = (Transaction) importedTransactions.remove(xid);
        }
        if (importedTransaction == null) {
            throw new XAException("No imported transaction for xid: " + xid);
        }
        try {
            int status = importedTransaction.getStatus();
            assert status == Status.STATUS_ACTIVE || status == Status.STATUS_PREPARED;
        } catch (SystemException e) {
            throw (XAException)new XAException().initCause(e);
        }
        rollback(importedTransaction);
    }


    //XAWork implementation
    public void begin(Xid xid, long txTimeoutMillis) throws XAException, InvalidTransactionException, SystemException, ImportedTransactionActiveException {
        Transaction importedTransaction;
        synchronized (importedTransactions) {
            importedTransaction = (Transaction) importedTransactions.get(xid);
            if (importedTransaction == null) {
                // this does not associate tx with current thread.
                importedTransaction = importXid(xid, txTimeoutMillis);
                importedTransactions.put(xid, importedTransaction);
            }
            // associate the the imported transaction with the current thread
            try {
                resume(importedTransaction);
            } catch (InvalidTransactionException e) {
                // this occures if our transaciton is associated with another thread
                throw (ImportedTransactionActiveException)new ImportedTransactionActiveException(xid).initCause(e);
            }
        }
    }

    public void end(Xid xid) throws XAException, SystemException {
        synchronized (importedTransactions) {
            Transaction importedTransaction = (Transaction) importedTransactions.get(xid);
            if (importedTransaction == null) {
                throw new XAException("No imported transaction for xid: " + xid);
            }
            if (importedTransaction != getTransaction()) {
                throw new XAException("Imported transaction is not associated with the curren thread xid: " + xid);
            }
            suspend();
        }
    }
}
