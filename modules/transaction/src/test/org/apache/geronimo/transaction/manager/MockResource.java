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

package org.apache.geronimo.transaction.manager;

import java.util.Set;
import java.util.HashSet;

import javax.transaction.xa.XAException;
import javax.transaction.xa.XAResource;
import javax.transaction.xa.Xid;

/**
 *
 *
 * @version $Rev$ $Date$
 */
public class MockResource implements NamedXAResource {
    private String xaResourceName = "mockResource";
    private Xid xid;
    private MockResourceManager manager;
    private int timeout = 0;
    private boolean prepared;
    private boolean committed;
    private boolean rolledback;
    private Set preparedXids = new HashSet();

    public MockResource(MockResourceManager manager, String xaResourceName) {
        this.manager = manager;
        this.xaResourceName = xaResourceName;
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
        prepared = true;
        preparedXids.add(xid);
        return XAResource.XA_OK;
    }

    public void commit(Xid xid, boolean onePhase) throws XAException {
        preparedXids.remove(xid);
        committed = true;
    }

    public void rollback(Xid xid) throws XAException {
        rolledback = true;
        preparedXids.remove(xid);
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
        return (Xid[]) preparedXids.toArray(new Xid[preparedXids.size()]);
    }

    public boolean isPrepared() {
        return prepared;
    }

    public boolean isCommitted() {
        return committed;
    }

    public boolean isRolledback() {
        return rolledback;
    }

    public String getName() {
        return xaResourceName;
    }

}
