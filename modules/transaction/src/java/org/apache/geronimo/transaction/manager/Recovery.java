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
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.transaction.xa.XAException;
import javax.transaction.xa.XAResource;
import javax.transaction.xa.Xid;

/**
 *
 *
 * @version $Revision: 1.1 $ $Date: 2004/05/06 04:00:51 $
 *
 * */
public class Recovery {

    private final List xaResources;
    private final TransactionLog txLog;
    private XidFactory xidFactory;

    private Map externalXids;

    public Recovery(List xaResources, TransactionLog txLog, XidFactory xidFactory) {
        this.xaResources = xaResources;
        this.txLog = txLog;
        this.xidFactory = xidFactory;
    }

    public synchronized void recover() throws XAException {
        Set ourXids = new HashSet();
        externalXids = new HashMap();
        Map externalGlobalIdMap = new HashMap();
        List preparedXids = null;
        try {
            preparedXids = txLog.recover();
        } catch (LogException e) {
            throw (XAException)new XAException(XAException.XAER_RMERR).initCause(e);
        }
        for (Iterator iterator = preparedXids.iterator(); iterator.hasNext();) {
            Xid xid = (Xid) iterator.next();
            if (xidFactory.matchesGlobalId(xid.getGlobalTransactionId())) {
                ourXids.add(xid);
            } else {
                TransactionImpl externalTx = new TransactionImpl(xid, txLog);
                externalXids.put(xid, externalTx);
                externalGlobalIdMap.put(xid.getGlobalTransactionId(), externalTx);
            }
        }

        for (Iterator iterator = xaResources.iterator(); iterator.hasNext();) {
            XAResource xaResource = (XAResource) iterator.next();
            Xid[] prepared = xaResource.recover(XAResource.TMSTARTRSCAN + XAResource.TMENDRSCAN);
            for (int i = 0; i < prepared.length; i++) {
                Xid xid = prepared[i];
                if (ourXids.contains(xid)) {
                    xaResource.commit(xid, false);
                } else if (xidFactory.matchesGlobalId(xid.getGlobalTransactionId())) {
                    //ours, but prepare not logged
                    xaResource.rollback(xid);
                } else if (xidFactory.matchesBranchId(xid.getBranchQualifier())) {
                    //our branch, but we did not start this tx.
                    TransactionImpl externalTx = (TransactionImpl)externalGlobalIdMap.get(xid.getGlobalTransactionId());
                    if (externalTx == null) {
                        //we did not prepare this branch, rollback.
                        xaResource.rollback(xid);
                    } else {
                        //we prepared this branch, must wait for commit/rollback command.
                        externalTx.addBranchXid(xaResource, xid);
                    }
                }
                //else we had nothing to do with this xid.
            }
        }


    }

    public Map getExternalXids() {
        return new HashMap(externalXids);
    }
}
