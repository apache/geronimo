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

import java.util.Set;
import java.util.HashSet;

import javax.security.auth.Subject;
import javax.transaction.TransactionManager;

import junit.framework.TestCase;
import org.apache.geronimo.connector.outbound.connectiontracking.DefaultInterceptor;
import org.apache.geronimo.connector.outbound.connectiontracking.ConnectionTrackingCoordinator;
import org.apache.geronimo.connector.outbound.connectiontracking.DefaultComponentInterceptor;
import org.apache.geronimo.connector.outbound.connectiontracking.defaultimpl.DefaultComponentContext;
import org.apache.geronimo.connector.mock.MockManagedConnectionFactory;
import org.apache.geronimo.connector.mock.MockConnectionFactory;
import org.apache.geronimo.connector.mock.MockConnection;
import org.apache.geronimo.connector.mock.MockXAResource;
import org.apache.geronimo.connector.mock.MockManagedConnection;
import org.apache.geronimo.security.bridge.RealmBridge;
import org.apache.geronimo.security.ContextManager;
import org.apache.geronimo.transaction.manager.TransactionManagerImpl;
import org.apache.geronimo.transaction.InstanceContext;
import org.apache.geronimo.transaction.UserTransactionImpl;

/**
 *
 *
 * @version $Revision: 1.7 $ $Date: 2004/04/22 17:06:30 $
 *
 * */
public class ConnectionManagerTestUtils extends TestCase implements DefaultInterceptor, RealmBridge {
    protected boolean useConnectionRequestInfo = false;
    protected boolean useSubject = true;
    protected boolean useTransactionCaching = true;
    protected boolean useLocalTransactions = false;
    protected boolean useTransactions = true;
    protected int maxSize = 50;
    protected int blockingTimeout = 100;
    protected String name = "testCF";
    //dependencies
    protected RealmBridge realmBridge = this;
    protected ConnectionTrackingCoordinator connectionTrackingCoordinator;
    protected TransactionManager transactionManager;
    protected ConnectionManagerDeployment connectionManagerDeployment;
    protected MockConnectionFactory connectionFactory;
    protected MockManagedConnectionFactory mockManagedConnectionFactory;
    protected DefaultComponentContext defaultComponentContext;
    protected DefaultComponentInterceptor defaultComponentInterceptor;
    protected Set unshareableResources = new HashSet();
    protected MockManagedConnection mockManagedConnection;
    protected Subject subject;
    protected UserTransactionImpl userTransaction;

    protected void setUp() throws Exception {
        connectionTrackingCoordinator = new ConnectionTrackingCoordinator();
        transactionManager = new TransactionManagerImpl();
        mockManagedConnectionFactory = new MockManagedConnectionFactory();
        subject = new Subject();
        ContextManager.setCurrentCaller(subject);
        connectionManagerDeployment = new ConnectionManagerDeployment(useConnectionRequestInfo,
                useSubject,
                useTransactionCaching,
                useLocalTransactions,
                useTransactions,
                maxSize,
                blockingTimeout,
                //name,
                realmBridge,
                connectionTrackingCoordinator);
        connectionManagerDeployment.doStart();
        connectionFactory = (MockConnectionFactory) connectionManagerDeployment.createConnectionFactory(mockManagedConnectionFactory);
        defaultComponentContext = new DefaultComponentContext();
        defaultComponentInterceptor = new DefaultComponentInterceptor(this, connectionTrackingCoordinator, unshareableResources);
    }

    protected void tearDown() throws Exception {
        connectionTrackingCoordinator = null;
        transactionManager = null;
        mockManagedConnectionFactory = null;
        connectionManagerDeployment = null;
        connectionFactory = null;
        defaultComponentContext = null;
    }

    public Object invoke(InstanceContext newInstanceContext) throws Throwable {
        MockConnection mockConnection = (MockConnection) connectionFactory.getConnection();
        mockManagedConnection = mockConnection.getManagedConnection();
        if (userTransaction != null) {
            userTransaction.begin();
            MockXAResource mockXAResource = (MockXAResource) mockManagedConnection.getXAResource();
            assertEquals("XAResource should know one xid", 1, mockXAResource.getKnownXids().size());
            assertNull("Should not be committed", mockXAResource.getCommitted());
            userTransaction.commit();
            assertNotNull("Should be committed", mockXAResource.getCommitted());
        }
        mockConnection.close();
        return null;
    }

    public Subject mapSubject(Subject sourceSubject) {
        return subject;
    }
}
