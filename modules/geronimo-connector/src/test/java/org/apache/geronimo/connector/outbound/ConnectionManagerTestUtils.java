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

package org.apache.geronimo.connector.outbound;

import java.util.HashSet;
import java.util.Set;
import javax.security.auth.Subject;
import javax.transaction.TransactionManager;
import javax.transaction.UserTransaction;

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
import org.apache.geronimo.security.ContextManager;
import org.apache.geronimo.connector.outbound.connectiontracking.ConnectorInstanceContextImpl;
import org.apache.geronimo.connector.outbound.connectiontracking.ConnectorInstanceContext;
import org.apache.geronimo.connector.outbound.connectiontracking.GeronimoTransactionListener;
import org.apache.geronimo.transaction.manager.TransactionManagerImpl;
import org.apache.geronimo.testsupport.TestSupport;

/**
 * ???
 *
 * @version $Rev$ $Date$
 */
public class ConnectionManagerTestUtils extends TestSupport implements DefaultInterceptor {
    protected boolean useTransactionCaching = true;
    protected boolean useLocalTransactions = false;
    protected boolean useThreadCaching = false;
    protected boolean useTransactions = true;
    protected int maxSize = 10;
    protected int minSize = 0;
    protected int blockingTimeout = 100;
    protected int idleTimeoutMinutes = 15;
    protected boolean useConnectionRequestInfo = false;
    protected boolean useSubject = true;
    private boolean matchOne = true;
    private boolean matchAll = false;
    private boolean selectOneNoMatch = false;
    protected String name = "testCF";
    //dependencies
    protected ConnectionTrackingCoordinator connectionTrackingCoordinator;
    protected TransactionManager transactionManager;
    protected AbstractConnectionManager connectionManagerDeployment;
    protected MockConnectionFactory connectionFactory;
    protected MockManagedConnectionFactory mockManagedConnectionFactory;
    protected ConnectorInstanceContextImpl connectorInstanceContext;
    protected DefaultComponentInterceptor defaultComponentInterceptor;
    protected Set unshareableResources = new HashSet();
    protected Set applicationManagedSecurityResources = new HashSet();
    protected MockManagedConnection mockManagedConnection;
    protected Subject subject;
    protected UserTransaction userTransaction;
    protected TransactionSupport transactionSupport = new XATransactions(useTransactionCaching, useThreadCaching);
    protected PoolingSupport poolingSupport = new PartitionedPool(maxSize, minSize, blockingTimeout, idleTimeoutMinutes, matchOne, matchAll, selectOneNoMatch, useConnectionRequestInfo, useSubject);
    protected boolean containerManagedSecurity = true;

    protected DefaultInterceptor mockComponent = new DefaultInterceptor() {
        public Object invoke(ConnectorInstanceContext newConnectorInstanceContext) throws Throwable {
            MockConnection mockConnection = (MockConnection) connectionFactory.getConnection();
            mockManagedConnection = mockConnection.getManagedConnection();
            mockConnection.close();
            return null;
        }
    };
    private ClassLoader classLoader = this.getClass().getClassLoader();

    protected void setUp() throws Exception {
        TransactionManagerImpl transactionManager = new TransactionManagerImpl();
        this.transactionManager = transactionManager;

        connectionTrackingCoordinator = new ConnectionTrackingCoordinator();
        transactionManager.addTransactionAssociationListener(new GeronimoTransactionListener(connectionTrackingCoordinator));

        mockManagedConnectionFactory = new MockManagedConnectionFactory();
        subject = new Subject();
        ContextManager.setCallers(subject, subject);
        connectionManagerDeployment = new GenericConnectionManager(
                transactionSupport,
                poolingSupport,
                containerManagedSecurity,
                connectionTrackingCoordinator,
                this.transactionManager,
                name,
                classLoader);
        connectionFactory = (MockConnectionFactory) connectionManagerDeployment.createConnectionFactory(mockManagedConnectionFactory);
        connectorInstanceContext = new ConnectorInstanceContextImpl(unshareableResources, applicationManagedSecurityResources);
        defaultComponentInterceptor = new DefaultComponentInterceptor(this, connectionTrackingCoordinator);
    }

    protected void tearDown() throws Exception {
        connectionTrackingCoordinator = null;
        transactionManager = null;
        mockManagedConnectionFactory = null;
        connectionManagerDeployment = null;
        connectionFactory = null;
        connectorInstanceContext = null;
    }

    public Object invoke(ConnectorInstanceContext newConnectorInstanceContext) throws Throwable {
        return mockComponent.invoke(newConnectorInstanceContext);
    }
}
