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
import javax.resource.ResourceException;

import junit.framework.TestCase;
import org.apache.geronimo.connector.outbound.ConnectionInfo;
import org.apache.geronimo.connector.outbound.ConnectionTrackingInterceptor;
import org.apache.geronimo.connector.outbound.ManagedConnectionInfo;
import org.apache.geronimo.connector.outbound.ConnectionInterceptor;
import org.apache.geronimo.connector.outbound.ConnectionReturnAction;
import org.apache.geronimo.connector.outbound.connectiontracking.defaultimpl.DefaultComponentContext;
import org.apache.geronimo.transaction.TrackedConnectionAssociator;

/**
 *
 *
 * @version $Revision: 1.6 $ $Date: 2004/05/24 19:10:35 $
 *
 * */
public class ConnectionTrackingCoordinatorTest extends TestCase
        implements ConnectionInterceptor {

    private static final String name1 = "foo";
    private static final String name2 = "bar";
    private ConnectionTrackingCoordinator connectionTrackingCoordinator;
    private ConnectionTrackingInterceptor key1;
    private ConnectionTrackingInterceptor key2;
    private Subject subject = null;
    private Set unshareableResources;
    private Set applicationManagedSecurityResources;

    protected void setUp() throws Exception {
        connectionTrackingCoordinator = new ConnectionTrackingCoordinator();
        key1 = new ConnectionTrackingInterceptor(this, name1, connectionTrackingCoordinator);
        key2 = new ConnectionTrackingInterceptor(this, name2, connectionTrackingCoordinator);
        unshareableResources = new HashSet();
        applicationManagedSecurityResources = new HashSet();
    }

    protected void tearDown() throws Exception {
        connectionTrackingCoordinator = null;
        key1 = null;
        key2 = null;
    }

    public void testSimpleComponentContextLifecyle() throws Exception {
        DefaultComponentContext componentContext = new DefaultComponentContext();
        TrackedConnectionAssociator.ConnectorContextInfo connectorContext = connectionTrackingCoordinator.enter(componentContext, unshareableResources, applicationManagedSecurityResources);
        assertNull("Expected old component context to be null", connectorContext.getInstanceContext());
        //give the context a ConnectionInfo
        ManagedConnectionInfo managedConnectionInfo = new ManagedConnectionInfo(null, null);
        ConnectionInfo connectionInfo = new ConnectionInfo(managedConnectionInfo);
        connectionTrackingCoordinator.handleObtained(key1, connectionInfo);
        connectionTrackingCoordinator.exit(connectorContext);
        Map connectionManagerMap = componentContext.getConnectionManagerMap();
        Set infos = (Set) connectionManagerMap.get(key1);
        assertEquals("Expected one connection for key1", 1, infos.size());
        assertTrue("Expected to get supplied ConnectionInfo from infos", connectionInfo == infos.iterator().next());

        //Enter again, and close the handle
        connectorContext = connectionTrackingCoordinator.enter(componentContext, unshareableResources, applicationManagedSecurityResources);
        assertNull("Expected old component context to be null", connectorContext.getInstanceContext());
        connectionTrackingCoordinator.handleReleased(key1, connectionInfo);
        connectionTrackingCoordinator.exit(connectorContext);
        connectionManagerMap = componentContext.getConnectionManagerMap();
        infos = (Set) connectionManagerMap.get(key1);
        assertEquals("Expected no connection set for key1", null, infos);
    }

    public void testNestedComponentContextLifecyle() throws Exception {
        DefaultComponentContext componentContext1 = new DefaultComponentContext();
        TrackedConnectionAssociator.ConnectorContextInfo oldConnectorContext1 = connectionTrackingCoordinator.enter(componentContext1, unshareableResources, applicationManagedSecurityResources);
        assertNull("Expected old component context to be null", oldConnectorContext1.getInstanceContext());
        //give the context a ConnectionInfo
        ManagedConnectionInfo managedConnectionInfo1 = new ManagedConnectionInfo(null, null);
        ConnectionInfo connectionInfo1 = new ConnectionInfo(managedConnectionInfo1);
        connectionTrackingCoordinator.handleObtained(key1, connectionInfo1);

        //Simulate calling another component
        DefaultComponentContext componentContext2 = new DefaultComponentContext();
        TrackedConnectionAssociator.ConnectorContextInfo oldConnectorContext2 = connectionTrackingCoordinator.enter(componentContext2, unshareableResources, applicationManagedSecurityResources);
        assertTrue("Expected returned component context to be componentContext1", oldConnectorContext2.getInstanceContext() == componentContext1);
        //give the context a ConnectionInfo
        ManagedConnectionInfo managedConnectionInfo2 = new ManagedConnectionInfo(null, null);
        ConnectionInfo connectionInfo2 = new ConnectionInfo(managedConnectionInfo2);
        connectionTrackingCoordinator.handleObtained(key2, connectionInfo2);

        connectionTrackingCoordinator.exit(oldConnectorContext2);
        Map connectionManagerMap2 = componentContext2.getConnectionManagerMap();
        Set infos2 = (Set) connectionManagerMap2.get(key2);
        assertEquals("Expected one connection for key2", 1, infos2.size());
        assertTrue("Expected to get supplied ConnectionInfo from infos", connectionInfo2 == infos2.iterator().next());
        assertEquals("Expected no connection for key1", null, connectionManagerMap2.get(key1));


        connectionTrackingCoordinator.exit(oldConnectorContext1);
        Map connectionManagerMap1 = componentContext1.getConnectionManagerMap();
        Set infos1 = (Set) connectionManagerMap1.get(key1);
        assertEquals("Expected one connection for key1", 1, infos1.size());
        assertTrue("Expected to get supplied ConnectionInfo from infos", connectionInfo1 == infos1.iterator().next());
        assertEquals("Expected no connection for key2", null, connectionManagerMap1.get(key2));

        //Enter again, and close the handle
        oldConnectorContext1 = connectionTrackingCoordinator.enter(componentContext1, unshareableResources, applicationManagedSecurityResources);
        assertNull("Expected old component context to be null", oldConnectorContext1.getInstanceContext());
        connectionTrackingCoordinator.handleReleased(key1, connectionInfo1);
        connectionTrackingCoordinator.exit(oldConnectorContext1);
        connectionManagerMap1 = componentContext1.getConnectionManagerMap();
        infos1 = (Set) connectionManagerMap1.get(key1);
        assertEquals("Expected no connection set for key1", null, infos1);
    }

    public Subject mapSubject(Subject sourceSubject) {
        return subject;
    }

    public void getConnection(ConnectionInfo connectionInfo) throws ResourceException {
    }

    public void returnConnection(ConnectionInfo connectionInfo, ConnectionReturnAction connectionReturnAction) {
    }
}
