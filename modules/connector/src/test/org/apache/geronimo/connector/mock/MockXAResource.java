/* ====================================================================
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 2003 The Apache Software Foundation.  All rights
 * reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. The end-user documentation included with the redistribution,
 *    if any, must include the following acknowledgment:
 *       "This product includes software developed by the
 *        Apache Software Foundation (http://www.apache.org/)."
 *    Alternately, this acknowledgment may appear in the software itself,
 *    if and wherever such third-party acknowledgments normally appear.
 *
 * 4. The names "Apache" and "Apache Software Foundation" and
 *    "Apache Geronimo" must not be used to endorse or promote products
 *    derived from this software without prior written permission. For
 *    written permission, please contact apache@apache.org.
 *
 * 5. Products derived from this software may not be called "Apache",
 *    "Apache Geronimo", nor may "Apache" appear in their name, without
 *    prior written permission of the Apache Software Foundation.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE APACHE SOFTWARE FOUNDATION OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 *
 * ====================================================================
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
 * @version $Revision: 1.1 $ $Date: 2004/01/23 05:56:11 $
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
