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

package org.apache.geronimo.connector.outbound.connectiontracking;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import javax.security.auth.Subject;
import javax.transaction.Transaction;
import javax.transaction.TransactionManager;

import org.apache.geronimo.connector.outbound.ConnectionInfo;
import org.apache.geronimo.connector.outbound.ConnectionTrackingInterceptor;
import org.apache.geronimo.connector.outbound.ManagedConnectionInfo;
import org.apache.geronimo.connector.outbound.connectiontracking.defaultimpl.DefaultComponentContext;
import org.apache.geronimo.connector.outbound.connectiontracking.defaultimpl.DefaultTransactionContext;
import org.apache.geronimo.security.bridge.RealmBridge;
import org.apache.geronimo.transaction.manager.TransactionManagerImpl;
import org.apache.geronimo.transaction.InstanceContext;
import org.apache.geronimo.transaction.TransactionContext;

import junit.framework.TestCase;

/**
 *
 *
 * @version $Revision: 1.4 $ $Date: 2004/03/10 09:58:34 $
 *
 * */
public class ConnectionTrackingCoordinatorTest extends TestCase
        implements RealmBridge {

    private static final String name1 = "foo";
    private static final String name2 = "bar";
    private ConnectionTrackingCoordinator connectionTrackingCoordinator;
    private ConnectionTrackingInterceptor key1;
    private ConnectionTrackingInterceptor key2;
    private Subject subject = null;
    private Set unshareableResources;
    private TransactionManager transactionManager;


    protected void setUp() throws Exception {
        connectionTrackingCoordinator = new ConnectionTrackingCoordinator();
        key1 = new ConnectionTrackingInterceptor(null, name1, connectionTrackingCoordinator, this);
        key2 = new ConnectionTrackingInterceptor(null, name2, connectionTrackingCoordinator, this);
        unshareableResources = new HashSet();
        connectionTrackingCoordinator.setUnshareableResources(unshareableResources);
        transactionManager = new TransactionManagerImpl();
    }

    protected void tearDown() throws Exception {
        connectionTrackingCoordinator = null;
        key1 = null;
        key2 = null;
        transactionManager = null;
    }

    public void testSimpleComponentContextLifecyle() throws Exception {
        DefaultComponentContext componentContext = new DefaultComponentContext();
        InstanceContext oldComponentContext = connectionTrackingCoordinator.enter(componentContext);
        assertNull("Expected old component context to be null", oldComponentContext);
        //give the context a ConnectionInfo
        ManagedConnectionInfo managedConnectionInfo = new ManagedConnectionInfo(null, null);
        ConnectionInfo connectionInfo = new ConnectionInfo(managedConnectionInfo);
        connectionTrackingCoordinator.handleObtained(key1, connectionInfo);
        connectionTrackingCoordinator.exit(oldComponentContext, unshareableResources);
        Map connectionManagerMap = componentContext.getConnectionManagerMap();
        Set infos = (Set) connectionManagerMap.get(key1);
        assertEquals("Expected one connection for key1", 1, infos.size());
        assertTrue("Expected to get supplied ConnectionInfo from infos", connectionInfo == infos.iterator().next());

        //Enter again, and close the handle
        oldComponentContext = connectionTrackingCoordinator.enter(componentContext);
        assertNull("Expected old component context to be null", oldComponentContext);
        connectionTrackingCoordinator.handleReleased(key1, connectionInfo);
        connectionTrackingCoordinator.exit(oldComponentContext, unshareableResources);
        connectionManagerMap = componentContext.getConnectionManagerMap();
        infos = (Set) connectionManagerMap.get(key1);
        assertEquals("Expected no connection set for key1", null, infos);
    }

    public void testNestedComponentContextLifecyle() throws Exception {
        DefaultComponentContext componentContext1 = new DefaultComponentContext();
        InstanceContext oldComponentContext1 = connectionTrackingCoordinator.enter(componentContext1);
        assertNull("Expected old component context to be null", oldComponentContext1);
        //give the context a ConnectionInfo
        ManagedConnectionInfo managedConnectionInfo1 = new ManagedConnectionInfo(null, null);
        ConnectionInfo connectionInfo1 = new ConnectionInfo(managedConnectionInfo1);
        connectionTrackingCoordinator.handleObtained(key1, connectionInfo1);

        //Simulate calling another component
        DefaultComponentContext componentContext2 = new DefaultComponentContext();
        InstanceContext oldComponentContext2 = connectionTrackingCoordinator.enter(componentContext2);
        assertTrue("Expected returned component context to be componentContext1", oldComponentContext2 == componentContext1);
        //give the context a ConnectionInfo
        ManagedConnectionInfo managedConnectionInfo2 = new ManagedConnectionInfo(null, null);
        ConnectionInfo connectionInfo2 = new ConnectionInfo(managedConnectionInfo2);
        connectionTrackingCoordinator.handleObtained(key2, connectionInfo2);

        connectionTrackingCoordinator.exit(oldComponentContext2, unshareableResources);
        Map connectionManagerMap2 = componentContext2.getConnectionManagerMap();
        Set infos2 = (Set) connectionManagerMap2.get(key2);
        assertEquals("Expected one connection for key2", 1, infos2.size());
        assertTrue("Expected to get supplied ConnectionInfo from infos", connectionInfo2 == infos2.iterator().next());
        assertEquals("Expected no connection for key1", null, connectionManagerMap2.get(key1));


        connectionTrackingCoordinator.exit(oldComponentContext1, unshareableResources);
        Map connectionManagerMap1 = componentContext1.getConnectionManagerMap();
        Set infos1 = (Set) connectionManagerMap1.get(key1);
        assertEquals("Expected one connection for key1", 1, infos1.size());
        assertTrue("Expected to get supplied ConnectionInfo from infos", connectionInfo1 == infos1.iterator().next());
        assertEquals("Expected no connection for key2", null, connectionManagerMap1.get(key2));

        //Enter again, and close the handle
        oldComponentContext1 = connectionTrackingCoordinator.enter(componentContext1);
        assertNull("Expected old component context to be null", oldComponentContext1);
        connectionTrackingCoordinator.handleReleased(key1, connectionInfo1);
        connectionTrackingCoordinator.exit(oldComponentContext1, unshareableResources);
        connectionManagerMap1 = componentContext1.getConnectionManagerMap();
        infos1 = (Set) connectionManagerMap1.get(key1);
        assertEquals("Expected no connection set for key1", null, infos1);
    }

    public void testSimpleTransactionContextLifecycle() throws Exception {
        DefaultComponentContext componentContext = new DefaultComponentContext();
        InstanceContext oldComponentContext = connectionTrackingCoordinator.enter(componentContext);
        transactionManager.begin();
        Transaction transaction = transactionManager.getTransaction();
        DefaultTransactionContext transactionContext = new DefaultTransactionContext(transaction);
        TransactionContext oldTransactionContext = connectionTrackingCoordinator.setTransactionContext(transactionContext);
        assertNull("Expected no old transactionContext", oldTransactionContext);
        TransactionContext availableTransactionContext = connectionTrackingCoordinator.getTransactionContext();
        assertTrue("Expected the same transactionContext as we sent in", transactionContext == availableTransactionContext);

        TransactionContext exitingTransactionContext = connectionTrackingCoordinator.setTransactionContext(null);
        assertTrue("Expected the same transactionContext as we sent in", transactionContext == exitingTransactionContext);
        TransactionContext availableTransactionContext2 = connectionTrackingCoordinator.getTransactionContext();
        assertNull("Expected no transactionContext", availableTransactionContext2);
    }

    public Subject mapSubject(Subject sourceSubject) {
        return subject;
    }
}
