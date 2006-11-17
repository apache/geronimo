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

package org.apache.geronimo.connector.mock;

import java.util.HashSet;
import java.util.Set;
import javax.transaction.xa.XAException;
import javax.transaction.xa.XAResource;
import javax.transaction.xa.Xid;

/**
 *
 *
 * @version $Rev$ $Date$
 *
 * */
public class MockXAResource implements XAResource {

    private final MockManagedConnection mockManagedConnection;
    private int prepareResult = XAResource.XA_OK;
    private Xid currentXid;
    private int transactionTimeoutSeconds;
    private final Set knownXids = new HashSet();
    private final Set successfulXids = new HashSet();
    private Xid prepared;
    private Xid committed;
    private Xid rolledback;

    public MockXAResource(MockManagedConnection mockManagedConnection) {
        this.mockManagedConnection = mockManagedConnection;
    }

    public void commit(Xid xid, boolean onePhase) throws XAException {
        assert xid != null;
        assert onePhase || prepared == xid;
        committed = xid;
    }

    //TODO TMFAIL? TMENDRSCAN?
    public void end(Xid xid, int flags) throws XAException {
        assert xid != null;
        assert knownXids.contains(xid);
        assert flags == XAResource.TMSUSPEND || flags == XAResource.TMSUCCESS;
        if (flags == XAResource.TMSUSPEND) {
            assert currentXid == xid;
            currentXid = null;
        }
        if (flags == XAResource.TMSUCCESS) {
            successfulXids.add(xid);
            if (xid.equals(currentXid)) {
                currentXid = null;
            }
        }
    }

    public void forget(Xid xid) throws XAException {
        //todo
    }

    public int getTransactionTimeout() throws XAException {
        return transactionTimeoutSeconds;
    }

    public boolean isSameRM(XAResource xaResource) throws XAException {
        if (!(xaResource instanceof MockXAResource)) {
            return false;
        }
        MockXAResource other = (MockXAResource) xaResource;
        return other.mockManagedConnection.getManagedConnectionFactory() == mockManagedConnection.getManagedConnectionFactory();
    }

    public int prepare(Xid xid) throws XAException {
        assert xid != null;
        prepared = xid;
        return prepareResult;
    }

    public Xid[] recover(int flag) throws XAException {
        //todo
        return new Xid[0];
    }

    public void rollback(Xid xid) throws XAException {
        assert xid != null;
        rolledback = xid;
    }

    public boolean setTransactionTimeout(int seconds) throws XAException {
        transactionTimeoutSeconds = seconds;
        return true;
    }

    //TODO TMSTARTRSCAN?
    public void start(Xid xid, int flags) throws XAException {
        assert currentXid == null :"Expected no xid when start called";
        assert xid != null: "Expected xid supplied to start";
        assert flags == XAResource.TMNOFLAGS || flags == XAResource.TMJOIN || flags == XAResource.TMRESUME;
        if (flags == XAResource.TMNOFLAGS || flags == XAResource.TMJOIN) {
            assert !knownXids.contains(xid);
            knownXids.add(xid);
        }
        if (flags == XAResource.TMRESUME) {
            assert knownXids.contains(xid);
        }
        currentXid = xid;
    }

    public void setPrepareResult(int prepareResult) {
        this.prepareResult = prepareResult;
    }

    public Xid getCurrentXid() {
        return currentXid;
    }

    public Set getKnownXids() {
        return knownXids;
    }

    public Set getSuccessfulXids() {
        return successfulXids;
    }

    public Xid getPrepared() {
        return prepared;
    }

    public Xid getCommitted() {
        return committed;
    }

    public Xid getRolledback() {
        return rolledback;
    }

    public void clear() {
        currentXid = null;
        prepared = null;
        rolledback = null;
        committed = null;
        knownXids.clear();
        successfulXids.clear();
        prepareResult = XAResource.XA_OK;
    }
}
