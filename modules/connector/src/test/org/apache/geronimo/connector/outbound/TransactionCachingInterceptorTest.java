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

import org.apache.geronimo.connector.outbound.connectiontracking.ConnectionTracker;
import org.apache.geronimo.connector.outbound.connectiontracking.defaultimpl.DefaultTransactionContext;
import org.apache.geronimo.transaction.manager.TransactionManagerImpl;
import org.apache.geronimo.transaction.TransactionContext;

/**
 *
 *
 * @version $Revision: 1.2 $ $Date: 2004/01/31 19:27:16 $
 *
 * */
public class TransactionCachingInterceptorTest extends ConnectionManagerTestUtils
        implements ConnectionTracker {

    private TransactionManager transactionManager;
    private TransactionCachingInterceptor transactionCachingInterceptor;
    private DefaultTransactionContext defaultTransactionContext;

    protected void setUp() throws Exception {
        super.setUp();
        transactionManager = new TransactionManagerImpl();
        transactionCachingInterceptor = new TransactionCachingInterceptor(this, this);
    }

    protected void tearDown() throws Exception {
        super.tearDown();
        transactionManager = null;
        transactionCachingInterceptor = null;
    }

    public void testGetConnectionInTransaction() throws Exception {
        transactionManager.begin();
        Transaction transaction = transactionManager.getTransaction();
        defaultTransactionContext = new DefaultTransactionContext(transaction);
        ConnectionInfo connectionInfo1 = makeConnectionInfo();
        transactionCachingInterceptor.getConnection(connectionInfo1);
        assertTrue("Expected to get an initial connection", obtainedConnectionInfo != null);
        assertTrue("Expected nothing returned yet", returnedConnectionInfo == null);
        assertTrue("Expected the same ManagedConnectionInfo in the TransactionContext as was returned",
                connectionInfo1.getManagedConnectionInfo()
                == defaultTransactionContext.getManagedConnectionInfo(transactionCachingInterceptor));
        obtainedConnectionInfo = null;
        ConnectionInfo connectionInfo2 = new ConnectionInfo();
        transactionCachingInterceptor.getConnection(connectionInfo2);
        assertTrue("Expected to not get a second connection", obtainedConnectionInfo == null);
        assertTrue("Expected nothing returned yet", returnedConnectionInfo == null);
        assertTrue("Expected the same ManagedConnectionInfo in both ConnectionInfos",
                connectionInfo1.getManagedConnectionInfo() == connectionInfo2.getManagedConnectionInfo());
        assertTrue("Expected the same ManagedConnectionInfo in the TransactionContext as was returned",
                connectionInfo1.getManagedConnectionInfo() == defaultTransactionContext.getManagedConnectionInfo(transactionCachingInterceptor));
        //commit, see if connection returned.
        //we didn't create any handles, so the "ManagedConnection" should be returned.
        assertTrue("Expected TransactionContext to report active", defaultTransactionContext.isActive());
        transactionManager.commit();
        assertTrue("Expected connection to be returned", returnedConnectionInfo != null);
        assertTrue("Expected TransactionContext to report inactive", !defaultTransactionContext.isActive());

    }

    public void testGetConnectionOutsideTransaction() throws Exception {
        defaultTransactionContext = new DefaultTransactionContext(null);
        ConnectionInfo connectionInfo1 = makeConnectionInfo();
        transactionCachingInterceptor.getConnection(connectionInfo1);
        assertTrue("Expected to get an initial connection", obtainedConnectionInfo != null);
        assertTrue("Expected nothing returned yet", returnedConnectionInfo == null);
        assertTrue("Expected no ManagedConnectionInfo in the TransactionContext",
                null == defaultTransactionContext.getManagedConnectionInfo(transactionCachingInterceptor));
        obtainedConnectionInfo = null;
        ConnectionInfo connectionInfo2 = makeConnectionInfo();
        transactionCachingInterceptor.getConnection(connectionInfo2);
        assertTrue("Expected to get a second connection", obtainedConnectionInfo != null);
        assertTrue("Expected nothing returned yet", returnedConnectionInfo == null);
        assertTrue("Expected different ManagedConnectionInfo in both ConnectionInfos",
                connectionInfo1.getManagedConnectionInfo() != connectionInfo2.getManagedConnectionInfo());
        assertTrue("Expected no ManagedConnectionInfo in the TransactionContext",
                null == defaultTransactionContext.getManagedConnectionInfo(transactionCachingInterceptor));
        //we didn't create any handles, so the "ManagedConnection" should be returned.
        assertTrue("Expected TransactionContext to report inactive", !defaultTransactionContext.isActive());
        transactionCachingInterceptor.returnConnection(connectionInfo1, ConnectionReturnAction.RETURN_HANDLE);
        assertTrue("Expected connection to be returned", returnedConnectionInfo != null);
        returnedConnectionInfo = null;
        transactionCachingInterceptor.returnConnection(connectionInfo2, ConnectionReturnAction.RETURN_HANDLE);
        assertTrue("Expected connection to be returned", returnedConnectionInfo != null);

        assertTrue("Expected TransactionContext to report inactive", !defaultTransactionContext.isActive());

    }

    public void testTransactionIndependence() throws Exception {
        transactionManager.begin();
        Transaction transaction1 = transactionManager.getTransaction();
        defaultTransactionContext = new DefaultTransactionContext(transaction1);
        DefaultTransactionContext defaultTransactionContext1 = defaultTransactionContext;
        ConnectionInfo connectionInfo1 = makeConnectionInfo();
        transactionCachingInterceptor.getConnection(connectionInfo1);
        obtainedConnectionInfo = null;

        //start a second transaction
        transactionManager.suspend();
        transactionManager.begin();
        Transaction transaction2 = transactionManager.getTransaction();
        defaultTransactionContext = new DefaultTransactionContext(transaction2);
        ConnectionInfo connectionInfo2 = makeConnectionInfo();
        transactionCachingInterceptor.getConnection(connectionInfo2);
        assertTrue("Expected to get a second connection", obtainedConnectionInfo != null);
        assertTrue("Expected nothing returned yet", returnedConnectionInfo == null);
        assertTrue("Expected different ManagedConnectionInfo in each ConnectionInfos",
                connectionInfo1.getManagedConnectionInfo() != connectionInfo2.getManagedConnectionInfo());
        assertTrue("Expected the same ManagedConnectionInfo in the TransactionContext as was returned",
                connectionInfo2.getManagedConnectionInfo() == defaultTransactionContext.getManagedConnectionInfo(transactionCachingInterceptor));
        //commit 2nd transaction, see if connection returned.
        //we didn't create any handles, so the "ManagedConnection" should be returned.
        assertTrue("Expected TransactionContext to report active", defaultTransactionContext.isActive());
        transactionManager.commit();
        assertTrue("Expected connection to be returned", returnedConnectionInfo != null);
        assertTrue("Expected TransactionContext to report inactive", !defaultTransactionContext.isActive());
        returnedConnectionInfo = null;
        //resume first transaction
        transactionManager.resume(transaction1);
        defaultTransactionContext = defaultTransactionContext1;
        transactionManager.commit();
        assertTrue("Expected connection to be returned", returnedConnectionInfo != null);
        assertTrue("Expected TransactionContext to report inactive", !defaultTransactionContext.isActive());
    }

//interface implementations
    public void getConnection(ConnectionInfo connectionInfo) throws ResourceException {
        super.getConnection(connectionInfo);
        ManagedConnectionInfo managedConnectionInfo = connectionInfo.getManagedConnectionInfo();
        managedConnectionInfo.setConnectionEventListener(new GeronimoConnectionEventListener(null, managedConnectionInfo));
    }


    public void handleObtained(
            ConnectionTrackingInterceptor connectionTrackingInterceptor,
            ConnectionInfo connectionInfo) {
    }

    public void handleReleased(
            ConnectionTrackingInterceptor connectionTrackingInterceptor,
            ConnectionInfo connectionInfo) {
    }

    public TransactionContext getTransactionContext() {
        return defaultTransactionContext;
    }


}
