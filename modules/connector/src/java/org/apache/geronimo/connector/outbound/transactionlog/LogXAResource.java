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

package org.apache.geronimo.connector.outbound.transactionlog;

import javax.resource.ResourceException;
import javax.resource.spi.LocalTransaction;
import javax.transaction.xa.XAException;
import javax.transaction.xa.XAResource;
import javax.transaction.xa.Xid;

import org.apache.geronimo.transaction.manager.NamedXAResource;

/**
 * Works with JDBCLog to provide last resource optimization for a single 1-pc database.
 * The database work is committed when the log writes its prepare record, not here.
 *
 * @version $Revision: 1.3 $ $Date: 2004/06/08 17:38:01 $
 *
 * */
public class LogXAResource implements NamedXAResource {

    final String name;
    final LocalTransaction localTransaction;
    private Xid xid;

    public LogXAResource(LocalTransaction localTransaction, String name) {
        this.localTransaction = localTransaction;
        this.name = name;
    }
    public void commit(Xid xid, boolean onePhase) throws XAException {
    }

    public void end(Xid xid, int flags) throws XAException {
    }

    public void forget(Xid xid) throws XAException {
    }

    public int getTransactionTimeout() throws XAException {
        return 0;
    }

    public boolean isSameRM(XAResource xaResource) throws XAException {
        return this == xaResource;
    }

    public int prepare(Xid xid) throws XAException {
        return 0;
    }

    public Xid[] recover(int flag) throws XAException {
        return new Xid[0];
    }

    public void rollback(Xid xid) throws XAException {
        if (this.xid == null || !this.xid.equals(xid)) {
            throw new XAException();
        }
        try {
            localTransaction.rollback();
        } catch (ResourceException e) {
            throw (XAException)new XAException().initCause(e);
        } finally {
            this.xid = null;
        }
    }

    public boolean setTransactionTimeout(int seconds) throws XAException {
        return false;
    }

    public void start(Xid xid, int flag) throws XAException {
        if (flag == XAResource.TMNOFLAGS) {
            // first time in this transaction
            if (this.xid != null) {
                throw new XAException("already enlisted");
            }
            this.xid = xid;
            try {
                localTransaction.begin();
            } catch (ResourceException e) {
                throw (XAException) new XAException("could not start local tx").initCause(e);
            }
        } else if (flag == XAResource.TMRESUME) {
            if (xid != this.xid) {
                throw new XAException("attempting to resume in different transaction");
            }
        } else {
            throw new XAException("unknown state");
        }
     }

    public String getName() {
        return name;
    }
}
