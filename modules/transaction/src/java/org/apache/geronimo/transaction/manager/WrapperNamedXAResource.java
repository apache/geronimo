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

import javax.transaction.xa.XAResource;
import javax.transaction.xa.Xid;
import javax.transaction.xa.XAException;

import org.apache.geronimo.transaction.manager.NamedXAResource;

/**
 *
 *
 * @version $Revision: 1.1 $ $Date: 2004/06/08 17:33:43 $
 *
 * */
public class WrapperNamedXAResource implements NamedXAResource {

    private final XAResource xaResource;
    private final String name;

    public WrapperNamedXAResource(XAResource xaResource, String name) {
        this.xaResource = xaResource;
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void commit(Xid xid, boolean onePhase) throws XAException {
        xaResource.commit(xid, onePhase);
    }

    public void end(Xid xid, int flags) throws XAException {
        xaResource.end(xid, flags);
    }

    public void forget(Xid xid) throws XAException {
        xaResource.forget(xid);
    }

    public int getTransactionTimeout() throws XAException {
        return xaResource.getTransactionTimeout();
    }

    public boolean isSameRM(XAResource other) throws XAException {
        if (other instanceof WrapperNamedXAResource) {
            return xaResource.isSameRM(((WrapperNamedXAResource)other).xaResource);
        }
        return false;
    }

    public int prepare(Xid xid) throws XAException {
        return xaResource.prepare(xid);
    }

    public Xid[] recover(int flag) throws XAException {
        return xaResource.recover(flag);
    }

    public void rollback(Xid xid) throws XAException {
        xaResource.rollback(xid);
    }

    public boolean setTransactionTimeout(int seconds) throws XAException {
        return xaResource.setTransactionTimeout(seconds);
    }

    public void start(Xid xid, int flags) throws XAException {
        xaResource.start(xid, flags);
    }
}


