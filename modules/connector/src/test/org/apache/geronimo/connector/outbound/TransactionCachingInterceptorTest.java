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
 * @version $Revision: 1.3 $ $Date: 2004/02/25 09:57:13 $
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
