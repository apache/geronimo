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

package org.apache.geronimo.connector.outbound;

import javax.resource.ResourceException;
import javax.transaction.Transaction;
import javax.transaction.TransactionManager;
import javax.transaction.xa.XAException;
import javax.transaction.xa.XAResource;
import javax.transaction.xa.Xid;

import org.apache.geronimo.transaction.manager.TransactionManagerImpl;

/**
 *
 *
 * @version $Revision: 1.1 $ $Date: 2004/01/23 05:56:11 $
 *
 * */
public class TransactionEnlistingInterceptorTest extends ConnectionManagerTestUtils
        implements XAResource {

    private TransactionEnlistingInterceptor transactionEnlistingInterceptor;
    private boolean started;
    private boolean ended;
    private boolean returned;
    private boolean committed;

    protected void setUp() throws Exception {
        super.setUp();
        transactionEnlistingInterceptor = new TransactionEnlistingInterceptor(this);
    }

    protected void tearDown() throws Exception {
        super.tearDown();
        transactionEnlistingInterceptor = null;
        started = false;
        ended = false;
        returned = false;
        committed = false;
    }

    public void testNoTransaction() throws Exception {
        ConnectionInfo connectionInfo = makeConnectionInfo(null);
        transactionEnlistingInterceptor.getConnection(connectionInfo);
        assertTrue("Expected not started", !started);
        assertTrue("Expected not ended", !ended);
        transactionEnlistingInterceptor.returnConnection(connectionInfo, ConnectionReturnAction.RETURN_HANDLE);
        assertTrue("Expected returned", returned);
        assertTrue("Expected not committed", !committed);
    }

    public void testTransaction() throws Exception {
        TransactionManager transactionManager = new TransactionManagerImpl();
        transactionManager.begin();
        Transaction transaction = transactionManager.getTransaction();
        ConnectionInfo connectionInfo = makeConnectionInfo(transaction);
        transactionEnlistingInterceptor.getConnection(connectionInfo);
        assertTrue("Expected started", started);
        assertTrue("Expected not ended", !ended);
        started = false;
        transactionEnlistingInterceptor.returnConnection(connectionInfo, ConnectionReturnAction.RETURN_HANDLE);
        assertTrue("Expected not started", !started);
        assertTrue("Expected ended", ended);
        assertTrue("Expected returned", returned);
        transactionManager.commit();
        assertTrue("Expected committed", committed);
    }

    //ConnectionInterceptor
    public void getConnection(ConnectionInfo connectionInfo) throws ResourceException {
        ManagedConnectionInfo managedConnectionInfo = connectionInfo.getManagedConnectionInfo();
        managedConnectionInfo.setXAResource(this);
    }

    public void returnConnection(ConnectionInfo connectionInfo, ConnectionReturnAction connectionReturnAction) {
        returned = true;
    }

    //XAResource
    public void commit(Xid xid, boolean onePhase) throws XAException {
        committed = true;
    }

    public void end(Xid xid, int flags) throws XAException {
        ended = true;
    }

    public void forget(Xid xid) throws XAException {
    }

    public int getTransactionTimeout() throws XAException {
        return 0;
    }

    public boolean isSameRM(XAResource xaResource) throws XAException {
        return false;
    }

    public int prepare(Xid xid) throws XAException {
        return 0;
    }

    public Xid[] recover(int flag) throws XAException {
        return new Xid[0];
    }

    public void rollback(Xid xid) throws XAException {
    }

    public boolean setTransactionTimeout(int seconds) throws XAException {
        return false;
    }

    public void start(Xid xid, int flags) throws XAException {
        started = true;
    }


}
