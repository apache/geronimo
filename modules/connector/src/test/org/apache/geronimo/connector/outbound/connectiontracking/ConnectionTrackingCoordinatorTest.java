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

import javax.resource.ResourceException;
import javax.security.auth.Subject;

import junit.framework.TestCase;
import org.apache.geronimo.connector.outbound.ConnectionInfo;
import org.apache.geronimo.connector.outbound.ConnectionInterceptor;
import org.apache.geronimo.connector.outbound.ConnectionReturnAction;
import org.apache.geronimo.connector.outbound.ConnectionTrackingInterceptor;
import org.apache.geronimo.connector.outbound.ManagedConnectionInfo;
import org.apache.geronimo.connector.outbound.GeronimoConnectionEventListener;
import org.apache.geronimo.transaction.DefaultInstanceContext;
import org.apache.geronimo.transaction.InstanceContext;

/**
 *
 *
 * @version $Rev$ $Date$
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
        DefaultInstanceContext componentContext = new DefaultInstanceContext(unshareableResources, applicationManagedSecurityResources);
        InstanceContext oldInstanceContext = connectionTrackingCoordinator.enter(componentContext);
        assertNull("Expected old instance context to be null", oldInstanceContext);
        //give the context a ConnectionInfo
        ConnectionInfo connectionInfo = newConnectionInfo();
        connectionTrackingCoordinator.handleObtained(key1, connectionInfo);
        connectionTrackingCoordinator.exit(oldInstanceContext);
        Map connectionManagerMap = componentContext.getConnectionManagerMap();
        Set infos = (Set) connectionManagerMap.get(key1);
        assertEquals("Expected one connection for key1", 1, infos.size());
        assertTrue("Expected to get supplied ConnectionInfo from infos", connectionInfo == infos.iterator().next());

        //Enter again, and close the handle
        oldInstanceContext = connectionTrackingCoordinator.enter(componentContext);
        assertNull("Expected old instance context to be null", oldInstanceContext);
        connectionTrackingCoordinator.handleReleased(key1, connectionInfo);
        connectionTrackingCoordinator.exit(oldInstanceContext);
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
        DefaultInstanceContext componentContext1 = new DefaultInstanceContext(unshareableResources, applicationManagedSecurityResources);
        InstanceContext oldInstanceContext1 = connectionTrackingCoordinator.enter(componentContext1);
        assertNull("Expected old component context to be null", oldInstanceContext1);
        //give the context a ConnectionInfo
        ConnectionInfo connectionInfo1 = newConnectionInfo();
        connectionTrackingCoordinator.handleObtained(key1, connectionInfo1);

        //Simulate calling another component
        DefaultInstanceContext componentContext2 = new DefaultInstanceContext(unshareableResources, applicationManagedSecurityResources);
        InstanceContext oldInstanceContext2 = connectionTrackingCoordinator.enter(componentContext2);
        assertTrue("Expected returned component context to be componentContext1", oldInstanceContext2 == componentContext1);
        //give the context a ConnectionInfo
        ConnectionInfo connectionInfo2 = newConnectionInfo();
        connectionTrackingCoordinator.handleObtained(key2, connectionInfo2);

        connectionTrackingCoordinator.exit(oldInstanceContext2);
        Map connectionManagerMap2 = componentContext2.getConnectionManagerMap();
        Set infos2 = (Set) connectionManagerMap2.get(key2);
        assertEquals("Expected one connection for key2", 1, infos2.size());
        assertTrue("Expected to get supplied ConnectionInfo from infos", connectionInfo2 == infos2.iterator().next());
        assertEquals("Expected no connection for key1", null, connectionManagerMap2.get(key1));


        connectionTrackingCoordinator.exit(oldInstanceContext1);
        Map connectionManagerMap1 = componentContext1.getConnectionManagerMap();
        Set infos1 = (Set) connectionManagerMap1.get(key1);
        assertEquals("Expected one connection for key1", 1, infos1.size());
        assertTrue("Expected to get supplied ConnectionInfo from infos", connectionInfo1 == infos1.iterator().next());
        assertEquals("Expected no connection for key2", null, connectionManagerMap1.get(key2));

        //Enter again, and close the handle
        oldInstanceContext1 = connectionTrackingCoordinator.enter(componentContext1);
        assertNull("Expected old component context to be null", oldInstanceContext1);
        connectionTrackingCoordinator.handleReleased(key1, connectionInfo1);
        connectionTrackingCoordinator.exit(oldInstanceContext1);
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
