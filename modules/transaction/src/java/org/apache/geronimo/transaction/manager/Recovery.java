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

import javax.transaction.SystemException;
import javax.transaction.xa.XAException;
import javax.transaction.xa.XAResource;
import javax.transaction.xa.Xid;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 *
 *
 * @version $Revision: 1.2 $ $Date: 2004/06/08 17:33:42 $
 *
 * */
public class Recovery {
    private static final Log log = LogFactory.getLog("Recovery");

    private final List xaResources;
    private final TransactionLog txLog;
    private final XidFactory xidFactory;

    private final Map externalXids = new HashMap();
    private final Map ourXids = new HashMap();
    private final Map externalGlobalIdMap = new HashMap();

    private final List recoveryErrors = new ArrayList();

    public Recovery(final List xaResources, final TransactionLog txLog, final XidFactory xidFactory) {
        this.xaResources = xaResources;
        this.txLog = txLog;
        this.xidFactory = xidFactory;
    }

    public synchronized void recover() throws XAException {
        recoverLog();

        for (Iterator iterator = xaResources.iterator(); iterator.hasNext();) {
            NamedXAResource xaResource = (NamedXAResource) iterator.next();
            recoverResourceManager(xaResource);
        }
    }

    private void recoverLog() throws XAException {
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
                XidNamesPair xidNamesPair = new XidNamesPair(xid, (Set) entry.getValue());
                ourXids.put(xid.getGlobalTransactionId(), xidNamesPair);
            } else {
                TransactionImpl externalTx = new ExternalTransaction(xid, txLog, (Set) entry.getValue());
                externalXids.put(xid, externalTx);
                externalGlobalIdMap.put(xid.getGlobalTransactionId(), externalTx);
            }
        }
    }


    public synchronized void recoverResourceManager(NamedXAResource xaResource) throws XAException {
        Xid[] prepared = xaResource.recover(XAResource.TMSTARTRSCAN + XAResource.TMENDRSCAN);
        for (int i = 0; i < prepared.length; i++) {
            Xid xid = prepared[i];
            XidNamesPair xidNamesPair = (XidNamesPair) ourXids.get(xid.getGlobalTransactionId());
            if (xidNamesPair != null) {
                try {
                    xaResource.commit(xid, false);
                } catch (XAException e) {
                    recoveryErrors.add(e);
                    log.error(e);
                }
                if (!xidNamesPair.resourceNames.remove(xaResource.getName())) {
                    log.error("XAResource named: " + xaResource.getName() + " returned branch xid for xid: " + xid + " but was not registered with that transaction!");
                }
                if (xidNamesPair.resourceNames.isEmpty()) {
                    try {
                        txLog.commit(xidNamesPair.xid);
                    } catch (LogException e) {
                        recoveryErrors.add(e);
                        log.error(e);
                    }
                }
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

    //hard to implement.. needs ExternalTransaction to have a reference to externalXids.
//    public boolean remoteRecoveryComplete() {
//    }

    public synchronized Map getExternalXids() {
        return new HashMap(externalXids);
    }

    private static class XidNamesPair {
        private final Xid xid;
        private final Set resourceNames;

        public XidNamesPair(Xid xid, Set resourceNames) {
            this.xid = xid;
            this.resourceNames = resourceNames;
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
