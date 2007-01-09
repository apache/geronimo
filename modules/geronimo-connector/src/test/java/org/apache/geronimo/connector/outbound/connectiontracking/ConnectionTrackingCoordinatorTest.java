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

package org.apache.geronimo.connector.outbound.connectiontracking;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.resource.ResourceException;
import javax.security.auth.Subject;

import junit.framework.TestCase;
import org.apache.geronimo.connector.outbound.ConnectionInfo;
import org.apache.geronimo.connector.outbound.ConnectionInterceptor;
import org.apache.geronimo.connector.outbound.ConnectionReturnAction;
import org.apache.geronimo.connector.outbound.ConnectionTrackingInterceptor;
import org.apache.geronimo.connector.outbound.ManagedConnectionInfo;
import org.apache.geronimo.connector.outbound.GeronimoConnectionEventListener;

/**
 * @version $Rev$ $Date$
 */
public class ConnectionTrackingCoordinatorTest extends TestCase
        implements ConnectionInterceptor {

    private static final String name1 = "foo";
    private static final String name2 = "bar";
    private ConnectionTrackingCoordinator connectionTrackingCoordinator;
    private ConnectionTrackingInterceptor key1;
    private ConnectionTrackingInterceptor nestedKey;
    private Subject subject = null;
    private Set unshareableResources;
    private Set applicationManagedSecurityResources;

    protected void setUp() throws Exception {
        super.setUp();
        connectionTrackingCoordinator = new ConnectionTrackingCoordinator(false);
        key1 = new ConnectionTrackingInterceptor(this, name1, connectionTrackingCoordinator);
        nestedKey = new ConnectionTrackingInterceptor(this, name2, connectionTrackingCoordinator);
        unshareableResources = new HashSet();
        applicationManagedSecurityResources = new HashSet();
    }

    protected void tearDown() throws Exception {
        connectionTrackingCoordinator = null;
        key1 = null;
        nestedKey = null;
        super.tearDown();
    }

    public void testSimpleComponentContextLifecyle() throws Exception {
        // enter component context
        ConnectorInstanceContextImpl componentContext = new ConnectorInstanceContextImpl(unshareableResources, applicationManagedSecurityResources);
        ConnectorInstanceContext oldConnectorInstanceContext = connectionTrackingCoordinator.enter(componentContext);
        assertNull("Expected old instance context to be null", oldConnectorInstanceContext);

        // simulate create connection
        ConnectionInfo connectionInfo = newConnectionInfo();
        connectionTrackingCoordinator.handleObtained(key1, connectionInfo, false);

        // exit component context
        connectionTrackingCoordinator.exit(oldConnectorInstanceContext);

        // connection should be in component instance context
        Map connectionManagerMap = componentContext.getConnectionManagerMap();
        Set infos = (Set) connectionManagerMap.get(key1);
        assertNotNull("Expected one connections for key1", infos);
        assertEquals("Expected one connection for key1", 1, infos.size());
        assertTrue("Expected to get supplied ConnectionInfo from infos", connectionInfo == infos.iterator().next());

        // enter again, and close the handle
        oldConnectorInstanceContext = connectionTrackingCoordinator.enter(componentContext);
        assertNull("Expected old instance context to be null", oldConnectorInstanceContext);
        connectionTrackingCoordinator.handleReleased(key1, connectionInfo, ConnectionReturnAction.DESTROY);
        connectionTrackingCoordinator.exit(oldConnectorInstanceContext);

        // connection should not be in context
        connectionManagerMap = componentContext.getConnectionManagerMap();
        infos = (Set) connectionManagerMap.get(key1);
        assertEquals("Expected no connection set for key1", null, infos);
    }

    private ConnectionInfo newConnectionInfo() {
        ManagedConnectionInfo mci = new ManagedConnectionInfo(null, null);
        mci.setConnectionEventListener(new GeronimoConnectionEventListener(this, mci));
        ConnectionInfo ci = new ConnectionInfo(mci);
        ci.setConnectionHandle(new Object());
        mci.addConnectionHandle(ci);
        return ci;
    }

    public void testNestedComponentContextLifecyle() throws Exception {
        // enter component context
        ConnectorInstanceContextImpl componentContext1 = new ConnectorInstanceContextImpl(unshareableResources, applicationManagedSecurityResources);
        ConnectorInstanceContext oldConnectorInstanceContext1 = connectionTrackingCoordinator.enter(componentContext1);
        assertNull("Expected old component context to be null", oldConnectorInstanceContext1);

        // simulate create connection
        ConnectionInfo connectionInfo1 = newConnectionInfo();
        connectionTrackingCoordinator.handleObtained(key1, connectionInfo1, false);

        // enter another (nested) component context
        ConnectorInstanceContextImpl nextedComponentContext = new ConnectorInstanceContextImpl(unshareableResources, applicationManagedSecurityResources);
        ConnectorInstanceContext oldConnectorInstanceContext2 = connectionTrackingCoordinator.enter(nextedComponentContext);
        assertTrue("Expected returned component context to be componentContext1", oldConnectorInstanceContext2 == componentContext1);

        // simulate create connection in nested context
        ConnectionInfo nestedConnectionInfo = newConnectionInfo();
        connectionTrackingCoordinator.handleObtained(nestedKey, nestedConnectionInfo, false);

        // exit nested component context
        connectionTrackingCoordinator.exit(oldConnectorInstanceContext2);
        Map nestedConnectionManagerMap = nextedComponentContext.getConnectionManagerMap();
        Set nestedInfos = (Set) nestedConnectionManagerMap.get(nestedKey);
        assertNotNull("Expected one connections for key2", nestedInfos);
        assertEquals("Expected one connection for key2", 1, nestedInfos.size());
        assertSame("Expected to get supplied ConnectionInfo from infos", nestedConnectionInfo, nestedInfos.iterator().next());
        assertNull("Expected no connection for key1", nestedConnectionManagerMap.get(key1));


        // exit outer component context
        connectionTrackingCoordinator.exit(oldConnectorInstanceContext1);
        Map connectionManagerMap = componentContext1.getConnectionManagerMap();
        Set infos1 = (Set) connectionManagerMap.get(key1);
        assertNotNull("Expected one connections for key1", infos1);
        assertEquals("Expected one connection for key1", 1, infos1.size());
        assertSame("Expected to get supplied ConnectionInfo from infos", connectionInfo1, infos1.iterator().next());
        assertNull("Expected no connection for key2", connectionManagerMap.get(nestedKey));

        // enter again, and close the handle
        oldConnectorInstanceContext1 = connectionTrackingCoordinator.enter(componentContext1);
        assertNull("Expected old component context to be null", oldConnectorInstanceContext1);
        connectionTrackingCoordinator.handleReleased(key1, connectionInfo1, ConnectionReturnAction.DESTROY);
        connectionTrackingCoordinator.exit(oldConnectorInstanceContext1);

        // connection should not be in context
        connectionManagerMap = componentContext1.getConnectionManagerMap();
        infos1 = (Set) connectionManagerMap.get(key1);
        assertNull("Expected no connection set for key1", infos1);
    }

    public Subject mapSubject(Subject sourceSubject) {
        return subject;
    }

    public void getConnection(ConnectionInfo connectionInfo) throws ResourceException {
    }

    public void returnConnection(ConnectionInfo connectionInfo, ConnectionReturnAction connectionReturnAction) {
    }

    public void destroy() {        
    }
}
