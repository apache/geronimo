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

package org.apache.geronimo.transaction.manager;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Arrays;
import java.util.HashSet;

import javax.transaction.SystemException;
import javax.transaction.xa.XAException;
import javax.transaction.xa.XAResource;
import javax.transaction.xa.Xid;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 *
 *
 * @version $Revision: 1.2 $ $Date: 2004/06/19 17:17:13 $
 *
 * */
public class RecoveryImpl implements Recovery {
    private static final Log log = LogFactory.getLog("Recovery");

    private final TransactionLog txLog;
    private final XidFactory xidFactory;

    private final Map externalXids = new HashMap();
    private final Map ourXids = new HashMap();
    private final Map nameToOurTxMap = new HashMap();
    private final Map externalGlobalIdMap = new HashMap();

    private final List recoveryErrors = new ArrayList();

    public RecoveryImpl(final TransactionLog txLog, final XidFactory xidFactory) {
        this.txLog = txLog;
        this.xidFactory = xidFactory;
    }

    public synchronized void recoverLog() throws XAException {
        Map preparedXids = null;
        try {
            preparedXids = txLog.recover(xidFactory);
        } catch (LogException e) {
            throw (XAException) new XAException(XAException.XAER_RMERR).initCause(e);
        }
        for (Iterator iterator = preparedXids.entrySet().iterator(); iterator.hasNext();) {
            Map.Entry entry = (Map.Entry) iterator.next();
            Xid xid = (Xid) entry.getKey();
            if (xidFactory.matchesGlobalId(xid.getGlobalTransactionId())) {
                Object o = entry.getValue();
                XidBranchesPair xidBranchesPair = new XidBranchesPair(xid, (Set) entry.getValue());
                ourXids.put(new ByteArrayWrapper(xid.getGlobalTransactionId()), xidBranchesPair);
                for (Iterator branches = xidBranchesPair.branches.iterator(); branches.hasNext();) {
                    String name = ((TransactionBranchInfo) branches.next()).getResourceName();
                    Set transactionsForName = (Set)nameToOurTxMap.get(name);
                    if (transactionsForName == null) {
                        transactionsForName = new HashSet();
                    }
                    transactionsForName.add(xidBranchesPair);
                }
            } else {
                Object o = entry.getValue();
                TransactionImpl externalTx = new ExternalTransaction(xid, txLog, (Set) entry.getValue());
                externalXids.put(xid, externalTx);
                externalGlobalIdMap.put(xid.getGlobalTransactionId(), externalTx);
            }
        }
    }


    public synchronized void recoverResourceManager(NamedXAResource xaResource) throws XAException {
        String name = xaResource.getName();
        Xid[] prepared = xaResource.recover(XAResource.TMSTARTRSCAN + XAResource.TMENDRSCAN);
        for (int i = 0; i < prepared.length; i++) {
            Xid xid = prepared[i];
            ByteArrayWrapper globalIdWrapper = new ByteArrayWrapper(xid.getGlobalTransactionId());
            XidBranchesPair xidNamesPair = (XidBranchesPair) ourXids.get(globalIdWrapper);
            if (xidNamesPair != null) {
                try {
                    xaResource.commit(xid, false);
                } catch (XAException e) {
                    recoveryErrors.add(e);
                    log.error(e);
                }
                removeNameFromTransaction(xidNamesPair, name, true);
            } else if (xidFactory.matchesGlobalId(xid.getGlobalTransactionId())) {
                //ours, but prepare not logged
                try {
                    xaResource.rollback(xid);
                } catch (XAException e) {
                    recoveryErrors.add(e);
                    log.error(e);
                }
            } else if (xidFactory.matchesBranchId(xid.getBranchQualifier())) {
                //our branch, but we did not start this tx.
                TransactionImpl externalTx = (TransactionImpl) externalGlobalIdMap.get(xid.getGlobalTransactionId());
                if (externalTx == null) {
                    //we did not prepare this branch, rollback.
                    try {
                        xaResource.rollback(xid);
                    } catch (XAException e) {
                        recoveryErrors.add(e);
                        log.error(e);
                    }
                } else {
                    //we prepared this branch, must wait for commit/rollback command.
                    externalTx.addBranchXid(xaResource, xid);
                }
            }
            //else we had nothing to do with this xid.
        }
        Set transactionsForName = (Set)nameToOurTxMap.get(name);
        if (transactionsForName != null) {
            for (Iterator transactions = transactionsForName.iterator(); transactions.hasNext();) {
                XidBranchesPair xidBranchesPair = (XidBranchesPair) transactions.next();
                removeNameFromTransaction(xidBranchesPair, name, false);
            }
        }
    }

    private void removeNameFromTransaction(XidBranchesPair xidBranchesPair, String name, boolean warn) {
        int removed = 0;
        for (Iterator branches = xidBranchesPair.branches.iterator(); branches.hasNext();) {
            TransactionBranchInfo transactionBranchInfo = (TransactionBranchInfo) branches.next();
            if (name.equals(transactionBranchInfo.getResourceName())) {
                branches.remove();
                removed++;
            }
        }
        if (warn && removed == 0) {
            log.error("XAResource named: " + name + " returned branch xid for xid: " + xidBranchesPair.xid + " but was not registered with that transaction!");
        }
        if (xidBranchesPair.branches.isEmpty()) {
            try {
                ourXids.remove(new ByteArrayWrapper(xidBranchesPair.xid.getGlobalTransactionId()));
                txLog.commit(xidBranchesPair.xid);
            } catch (LogException e) {
                recoveryErrors.add(e);
                log.error(e);
            }
        }
    }

    public synchronized boolean hasRecoveryErrors() {
        return !recoveryErrors.isEmpty();
    }

    public synchronized List getRecoveryErrors() {
        return Collections.unmodifiableList(recoveryErrors);
    }

    public synchronized boolean localRecoveryComplete() {
        return ourXids.isEmpty();
    }

    public synchronized int localUnrecoveredCount() {
        return ourXids.size();
    }

    //hard to implement.. needs ExternalTransaction to have a reference to externalXids.
//    public boolean remoteRecoveryComplete() {
//    }

    public synchronized Map getExternalXids() {
        return new HashMap(externalXids);
    }

    private static class XidBranchesPair {
        private final Xid xid;
        //set of TransactionBranchInfo
        private final Set branches;

        public XidBranchesPair(Xid xid, Set branches) {
            this.xid = xid;
            this.branches = branches;
        }
    }

    private static class ByteArrayWrapper {
        private final byte[] bytes;
        private final int hashCode;

        public ByteArrayWrapper(final byte[] bytes) {
            assert bytes != null;
            this.bytes = bytes;
            int hash = 0;
            for (int i = 0; i < bytes.length; i++) {
                hash += 37 * bytes[i];
            }
            hashCode = hash;
        }

        public boolean equals(Object other) {
            if (other instanceof ByteArrayWrapper) {
                return Arrays.equals(bytes, ((ByteArrayWrapper)other).bytes);
            }
            return false;
        }

        public int hashCode() {
            return hashCode;
        }
    }

    private static class ExternalTransaction extends TransactionImpl {
        private Set resourceNames;

        public ExternalTransaction(Xid xid, TransactionLog txLog, Set resourceNames) {
            super(xid, txLog);
            this.resourceNames = resourceNames;
        }

        public boolean hasName(String name) {
            return resourceNames.contains(name);
        }

        public void removeName(String name) {
            resourceNames.remove(name);
        }

        public void preparedCommit() throws SystemException {
            if (!resourceNames.isEmpty()) {
                throw new SystemException("This tx does not have all resource managers online, commit not allowed yet");
            }
            super.preparedCommit();
        }

        public void rollback() throws SystemException {
            if (!resourceNames.isEmpty()) {
                throw new SystemException("This tx does not have all resource managers online, rollback not allowed yet");
            }
            super.rollback();

        }
    }
}
