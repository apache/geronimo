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

import java.util.HashSet;
import java.util.Set;

import javax.security.auth.Subject;
import javax.transaction.TransactionManager;

import junit.framework.TestCase;
import org.apache.geronimo.connector.mock.MockConnection;
import org.apache.geronimo.connector.mock.MockConnectionFactory;
import org.apache.geronimo.connector.mock.MockManagedConnection;
import org.apache.geronimo.connector.mock.MockManagedConnectionFactory;
import org.apache.geronimo.connector.outbound.connectionmanagerconfig.PartitionedPool;
import org.apache.geronimo.connector.outbound.connectionmanagerconfig.PoolingSupport;
import org.apache.geronimo.connector.outbound.connectionmanagerconfig.TransactionSupport;
import org.apache.geronimo.connector.outbound.connectionmanagerconfig.XATransactions;
import org.apache.geronimo.connector.outbound.connectiontracking.ConnectionTrackingCoordinator;
import org.apache.geronimo.connector.outbound.connectiontracking.DefaultComponentInterceptor;
import org.apache.geronimo.connector.outbound.connectiontracking.DefaultInterceptor;
import org.apache.geronimo.transaction.DefaultInstanceContext;
import org.apache.geronimo.security.ContextManager;
import org.apache.geronimo.security.bridge.RealmBridge;
import org.apache.geronimo.transaction.InstanceContext;
import org.apache.geronimo.transaction.UserTransactionImpl;
import org.apache.geronimo.transaction.GeronimoTransactionManager;
import org.apache.geronimo.transaction.context.TransactionContextManager;
import org.apache.geronimo.transaction.manager.TransactionManagerImpl;

/**
 *
 *
 * @version $Revision: 1.13 $ $Date: 2004/07/18 22:08:59 $
 *
 * */
public class ConnectionManagerTestUtils extends TestCase implements DefaultInterceptor, RealmBridge {
    protected boolean useTransactionCaching = true;
    protected boolean useLocalTransactions = false;
    protected boolean useThreadCaching = false;
    protected boolean useTransactions = true;
    protected int maxSize = 10;
    protected int blockingTimeout = 100;
    protected boolean useConnectionRequestInfo = false;
    protected boolean useSubject = true;
    private boolean matchOne = true;
    private boolean matchAll = false;
    private boolean selectOneNoMatch = false;
    protected String name = "testCF";
    //dependencies
    protected RealmBridge realmBridge = this;
    protected ConnectionTrackingCoordinator connectionTrackingCoordinator;
    protected TransactionContextManager transactionContextManager;
    protected AbstractConnectionManager connectionManagerDeployment;
    protected MockConnectionFactory connectionFactory;
    protected MockManagedConnectionFactory mockManagedConnectionFactory;
    protected DefaultInstanceContext defaultComponentContext;
    protected DefaultComponentInterceptor defaultComponentInterceptor;
    protected Set unshareableResources = new HashSet();
    protected Set applicationManagedSecurityResources = new HashSet();
    protected MockManagedConnection mockManagedConnection;
    protected Subject subject;
    protected UserTransactionImpl userTransaction;
    protected TransactionSupport transactionSupport = new XATransactions(useTransactionCaching, useThreadCaching);
    protected PoolingSupport poolingSupport = new PartitionedPool(useConnectionRequestInfo, useSubject, maxSize, blockingTimeout, matchOne, matchAll, selectOneNoMatch);

    protected DefaultInterceptor mockComponent = new DefaultInterceptor() {
        public Object invoke(InstanceContext newInstanceContext) throws Throwable {
            MockConnection mockConnection = (MockConnection) connectionFactory.getConnection();
            mockManagedConnection = mockConnection.getManagedConnection();
            mockConnection.close();
            return null;
        }
    };

    protected void setUp() throws Exception {
        connectionTrackingCoordinator = new ConnectionTrackingCoordinator();
        TransactionManager transactionManager = new TransactionManagerImpl();
        transactionContextManager = new TransactionContextManager(transactionManager);
        mockManagedConnectionFactory = new MockManagedConnectionFactory();
        subject = new Subject();
        ContextManager.setCurrentCaller(subject);
        connectionManagerDeployment = new GenericConnectionManager(
                transactionSupport,
                poolingSupport,
                name,
                realmBridge,
                connectionTrackingCoordinator);
        connectionManagerDeployment.doStart();
        connectionFactory = (MockConnectionFactory) connectionManagerDeployment.createConnectionFactory(mockManagedConnectionFactory);
        defaultComponentContext = new DefaultInstanceContext(unshareableResources, applicationManagedSecurityResources);
        defaultComponentInterceptor = new DefaultComponentInterceptor(this, connectionTrackingCoordinator);
    }

    protected void tearDown() throws Exception {
        connectionTrackingCoordinator = null;
        transactionContextManager = null;
        mockManagedConnectionFactory = null;
        connectionManagerDeployment = null;
        connectionFactory = null;
        defaultComponentContext = null;
    }


    public Subject mapSubject(Subject sourceSubject) {
        return subject;
    }

    public Object invoke(InstanceContext newInstanceContext) throws Throwable {
        return mockComponent.invoke(newInstanceContext);
    }
}
