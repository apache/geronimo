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

package org.apache.geronimo.connector.outbound;

import javax.resource.ResourceException;
import javax.resource.spi.LocalTransaction;
import javax.transaction.xa.XAException;
import javax.transaction.xa.XAResource;
import javax.transaction.xa.Xid;

import org.apache.geronimo.transaction.manager.NamedXAResource;

/**
 * LocalXAResource adapts a local transaction to be controlled by a
 * JTA transaction manager.  Of course, it cannot provide xa
 * semantics.
 *
 *
 * @version $Revision: 1.5 $ $Date: 2004/06/08 17:38:00 $
 */
public class LocalXAResource implements NamedXAResource {

    //accessible in package for testing
    final LocalTransaction localTransaction;
    private final String name;
    private Xid xid;
    private int transactionTimeout;

    public LocalXAResource(LocalTransaction localTransaction, String name) {
        this.localTransaction = localTransaction;
        this.name = name;
    }

    // Implementation of javax.transaction.xa.XAResource

    public void commit(Xid xid, boolean flag) throws XAException {
        if (this.xid == null || !this.xid.equals(xid)) {
            throw new XAException();
        }
        try {
            localTransaction.commit();
        } catch (ResourceException e) {
            throw (XAException)new XAException().initCause(e);
         } finally {
            this.xid = null;
        }

    }

    public void forget(Xid xid) throws XAException {
        this.xid = null;
    }

    public int getTransactionTimeout() throws XAException {
        return transactionTimeout;
    }

    public boolean isSameRM(XAResource xares) throws XAException {
        return this == xares;
    }

    public Xid[] recover(int n) throws XAException {
        return null;
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

    public boolean setTransactionTimeout(int txTimeout) throws XAException {
        this.transactionTimeout = txTimeout;
        return true;
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

    public void end(Xid xid, int flag) throws XAException {
        if (xid != this.xid) {
            throw new XAException();
        }
        //we could keep track of if the flag is TMSUCCESS...
    }

    public int prepare(Xid xid) throws XAException {
        //log warning that semantics are incorrect...
        return XAResource.XA_OK;
    }

    public String getName() {
        return name;
    }
}
