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

import org.apache.geronimo.transaction.context.ContainerTransactionContext;
import org.apache.geronimo.transaction.context.TransactionContext;
import org.apache.geronimo.transaction.context.TransactionContextManager;
import org.apache.geronimo.transaction.context.UnspecifiedTransactionContext;
import org.apache.geronimo.transaction.manager.TransactionManagerImpl;

/**
 *
 *
 * @version $Rev$ $Date$
 *
 * */
public class TransactionCachingInterceptorTest extends ConnectionInterceptorTestUtils {

    private TransactionManagerImpl transactionManager;
    private TransactionContextManager transactionContextManager;
    private TransactionCachingInterceptor transactionCachingInterceptor;

    protected void setUp() throws Exception {
        super.setUp();
        transactionManager = new TransactionManagerImpl(10 * 1000, null, null);
        transactionContextManager = new TransactionContextManager(transactionManager, transactionManager);
        transactionCachingInterceptor = new TransactionCachingInterceptor(this, transactionContextManager);
    }

    protected void tearDown() throws Exception {
        super.tearDown();
        transactionManager = null;
        transactionContextManager = null;
        transactionCachingInterceptor = null;
    }

    public void testGetConnectionInTransaction() throws Exception {
        ContainerTransactionContext transactionContext = transactionContextManager.newContainerTransactionContext();
        ConnectionInfo connectionInfo1 = makeConnectionInfo();
        transactionCachingInterceptor.getConnection(connectionInfo1);
        assertTrue("Expected to get an initial connection", obtainedConnectionInfo != null);
        assertTrue("Expected nothing returned yet", returnedConnectionInfo == null);
        assertTrue("Expected the same ManagedConnectionInfo in the TransactionContext as was returned",
                connectionInfo1.getManagedConnectionInfo()
                == getSharedManagedConnectionInfo(transactionContext));
        obtainedConnectionInfo = null;
        ConnectionInfo connectionInfo2 = new ConnectionInfo();
        transactionCachingInterceptor.getConnection(connectionInfo2);
        assertTrue("Expected to not get a second connection", obtainedConnectionInfo == null);
        assertTrue("Expected nothing returned yet", returnedConnectionInfo == null);
        assertTrue("Expected the same ManagedConnectionInfo in both ConnectionInfos",
                connectionInfo1.getManagedConnectionInfo() == connectionInfo2.getManagedConnectionInfo());
        assertTrue("Expected the same ManagedConnectionInfo in the TransactionContext as was returned",
                connectionInfo1.getManagedConnectionInfo() == getSharedManagedConnectionInfo(transactionContext));
        //commit, see if connection returned.
        //we didn't create any handles, so the "ManagedConnection" should be returned.
        assertTrue("Expected TransactionContext to report active", transactionContext.isActive());
        transactionContext.commit();
        assertTrue("Expected connection to be returned", returnedConnectionInfo != null);
        assertTrue("Expected TransactionContext to report inactive", !transactionContext.isActive());

    }

    public void testGetUnshareableConnectionsInTransaction() throws Exception {
        ContainerTransactionContext transactionContext = transactionContextManager.newContainerTransactionContext();
        ConnectionInfo connectionInfo1 = makeConnectionInfo();
        connectionInfo1.setUnshareable(true);
        transactionCachingInterceptor.getConnection(connectionInfo1);
        assertTrue("Expected to get an initial connection", obtainedConnectionInfo != null);
        assertTrue("Expected nothing returned yet", returnedConnectionInfo == null);
        assertTrue("Expected different ManagedConnectionInfo in the TransactionContext as was returned",
                connectionInfo1.getManagedConnectionInfo()
                != getSharedManagedConnectionInfo(transactionContext));
        //2nd is shared, modelling a call into another ejb
        obtainedConnectionInfo = null;
        ConnectionInfo connectionInfo2 = makeConnectionInfo();
        transactionCachingInterceptor.getConnection(connectionInfo2);
        assertTrue("Expected to get a second connection", obtainedConnectionInfo != null);
        assertTrue("Expected nothing returned yet", returnedConnectionInfo == null);
        assertTrue("Expected the same ManagedConnectionInfo in both ConnectionInfos",
                connectionInfo1.getManagedConnectionInfo() != connectionInfo2.getManagedConnectionInfo());
        assertTrue("Expected the same ManagedConnectionInfo in the TransactionContext as was returned",
                connectionInfo2.getManagedConnectionInfo() == getSharedManagedConnectionInfo(transactionContext));
        //3rd is unshared, modelling a call into a third ejb
        obtainedConnectionInfo = null;
        ConnectionInfo connectionInfo3 = makeConnectionInfo();
        connectionInfo3.setUnshareable(true);
        transactionCachingInterceptor.getConnection(connectionInfo3);
        assertTrue("Expected to get a third connection", obtainedConnectionInfo != null);
        assertTrue("Expected nothing returned yet", returnedConnectionInfo == null);
        assertTrue("Expected different ManagedConnectionInfo in both unshared ConnectionInfos",
                connectionInfo1.getManagedConnectionInfo() != connectionInfo3.getManagedConnectionInfo());
        assertTrue("Expected different ManagedConnectionInfo in the TransactionContext as was returned",
                connectionInfo3.getManagedConnectionInfo() != getSharedManagedConnectionInfo(transactionContext));
        //commit, see if connection returned.
        //we didn't create any handles, so the "ManagedConnection" should be returned.
        assertTrue("Expected TransactionContext to report active", transactionContext.isActive());
        transactionContext.commit();
        assertTrue("Expected connection to be returned", returnedConnectionInfo != null);
        assertTrue("Expected TransactionContext to report inactive", !transactionContext.isActive());
    }

    private ManagedConnectionInfo getSharedManagedConnectionInfo(ContainerTransactionContext transactionContext) {
        return ((TransactionCachingInterceptor.ManagedConnectionInfos)transactionContext.getManagedConnectionInfo(transactionCachingInterceptor)).getShared();
    }

    public void testGetConnectionOutsideTransaction() throws Exception {
        TransactionContext.setContext(new UnspecifiedTransactionContext());
        ConnectionInfo connectionInfo1 = makeConnectionInfo();
        transactionCachingInterceptor.getConnection(connectionInfo1);
        assertTrue("Expected to get an initial connection", obtainedConnectionInfo != null);
        assertTrue("Expected nothing returned yet", returnedConnectionInfo == null);
        assertTrue("Expected no ManagedConnectionInfo in the TransactionContext",
                null == transactionContextManager.getContext().getManagedConnectionInfo(transactionCachingInterceptor));
        obtainedConnectionInfo = null;
        ConnectionInfo connectionInfo2 = makeConnectionInfo();
        transactionCachingInterceptor.getConnection(connectionInfo2);
        assertTrue("Expected to get a second connection", obtainedConnectionInfo != null);
        assertTrue("Expected nothing returned yet", returnedConnectionInfo == null);
        assertTrue("Expected different ManagedConnectionInfo in both ConnectionInfos",
                connectionInfo1.getManagedConnectionInfo() != connectionInfo2.getManagedConnectionInfo());
        assertTrue("Expected no ManagedConnectionInfo in the TransactionContext",
                null == transactionContextManager.getContext().getManagedConnectionInfo(transactionCachingInterceptor));
        //we didn't create any handles, so the "ManagedConnection" should be returned.
        assertTrue("Expected TransactionContext to report inactive", !transactionContextManager.getContext().isActive());
        transactionCachingInterceptor.returnConnection(connectionInfo1, ConnectionReturnAction.RETURN_HANDLE);
        assertTrue("Expected connection to be returned", returnedConnectionInfo != null);
        returnedConnectionInfo = null;
        transactionCachingInterceptor.returnConnection(connectionInfo2, ConnectionReturnAction.RETURN_HANDLE);
        assertTrue("Expected connection to be returned", returnedConnectionInfo != null);

        assertTrue("Expected TransactionContext to report inactive", !transactionContextManager.getContext().isActive());

    }

    public void testTransactionIndependence() throws Exception {
        ContainerTransactionContext transactionContext1 = transactionContextManager.newContainerTransactionContext();
        ConnectionInfo connectionInfo1 = makeConnectionInfo();
        transactionCachingInterceptor.getConnection(connectionInfo1);
        obtainedConnectionInfo = null;

        //start a second transaction
        transactionContext1.suspend();
        ContainerTransactionContext transactionContext2 = transactionContextManager.newContainerTransactionContext();
        ConnectionInfo connectionInfo2 = makeConnectionInfo();
        transactionCachingInterceptor.getConnection(connectionInfo2);
        assertTrue("Expected to get a second connection", obtainedConnectionInfo != null);
        assertTrue("Expected nothing returned yet", returnedConnectionInfo == null);
        assertTrue("Expected different ManagedConnectionInfo in each ConnectionInfos",
                connectionInfo1.getManagedConnectionInfo() != connectionInfo2.getManagedConnectionInfo());
        assertTrue("Expected the same ManagedConnectionInfo in the TransactionContext as was returned",
                connectionInfo2.getManagedConnectionInfo() == getSharedManagedConnectionInfo(transactionContext2));
        //commit 2nd transaction, see if connection returned.
        //we didn't create any handles, so the "ManagedConnection" should be returned.
        assertTrue("Expected TransactionContext to report active", transactionContext2.isActive());
        transactionContext2.commit();
        assertTrue("Expected connection to be returned", returnedConnectionInfo != null);
        assertTrue("Expected TransactionContext to report inactive", !transactionContext2.isActive());
        returnedConnectionInfo = null;
        //resume first transaction
        transactionContext1.resume();
        transactionContext1.commit();
        assertTrue("Expected connection to be returned", returnedConnectionInfo != null);
        assertTrue("Expected TransactionContext to report inactive", !transactionContext1.isActive());
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

}
