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

import javax.transaction.xa.XAException;
import javax.transaction.xa.XAResource;
import javax.transaction.xa.Xid;

/**
 *
 *
 * @version $Revision: 1.2 $ $Date: 2004/02/25 09:58:19 $
 */
public class MockResource implements XAResource {
    private Xid xid;
    private MockResourceManager manager;
    private int timeout = 0;

    public MockResource(MockResourceManager manager) {
        this.manager = manager;
    }

    public int getTransactionTimeout() throws XAException {
        return timeout;
    }

    public boolean setTransactionTimeout(int seconds) throws XAException {
        return false;
    }

    public Xid getXid() {
        return xid;
    }

    public void start(Xid xid, int flags) throws XAException {
        if (this.xid != null) {
            throw new XAException(XAException.XAER_PROTO);
        }
        if ((flags & XAResource.TMJOIN) != 0) {
            manager.join(xid, this);
        } else {
            manager.newTx(xid, this);
        }
        this.xid = xid;
    }

    public void end(Xid xid, int flags) throws XAException {
        if (this.xid != xid) {
            throw new XAException(XAException.XAER_INVAL);
        }
        this.xid = null;
    }

    public int prepare(Xid xid) throws XAException {
        return 0;
    }

    public void commit(Xid xid, boolean onePhase) throws XAException {
    }

    public void rollback(Xid xid) throws XAException {
        manager.forget(xid, this);
    }

    public boolean isSameRM(XAResource xaResource) throws XAException {
        if (xaResource instanceof MockResource) {
            return manager == ((MockResource) xaResource).manager;
        }
        return false;
    }

    public void forget(Xid xid) throws XAException {
        throw new UnsupportedOperationException();
    }

    public Xid[] recover(int flag) throws XAException {
        throw new UnsupportedOperationException();
    }

}
