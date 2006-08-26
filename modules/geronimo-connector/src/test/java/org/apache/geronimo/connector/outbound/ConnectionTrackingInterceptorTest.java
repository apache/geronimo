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

import java.util.Collection;
import java.util.HashSet;

import javax.resource.ResourceException;

import org.apache.geronimo.connector.outbound.connectiontracking.ConnectionTracker;

/**
 * TODO test unshareable resources.
 * TODO test repeat calls with null/non-null Subject
 *
 * @version $Rev$ $Date$
 *
 * */
public class ConnectionTrackingInterceptorTest extends ConnectionInterceptorTestUtils
        implements ConnectionTracker {

    private final static String key = "test-name";
    private ConnectionTrackingInterceptor connectionTrackingInterceptor;


    private ConnectionTrackingInterceptor obtainedConnectionTrackingInterceptor;
    private ConnectionInfo obtainedTrackedConnectionInfo;

    private ConnectionTrackingInterceptor releasedConnectionTrackingInterceptor;
    private ConnectionInfo releasedTrackedConnectionInfo;

    private Collection connectionInfos;

    protected void setUp() throws Exception {
        super.setUp();
        connectionTrackingInterceptor = new ConnectionTrackingInterceptor(this, key, this);
    }

    protected void tearDown() throws Exception {
        super.tearDown();
        connectionTrackingInterceptor = null;
        managedConnection = null;
        obtainedConnectionTrackingInterceptor = null;
        obtainedTrackedConnectionInfo = null;
        releasedConnectionTrackingInterceptor = null;
        releasedTrackedConnectionInfo = null;
    }

    public void testConnectionRegistration() throws Exception {
        ConnectionInfo connectionInfo = makeConnectionInfo();
        connectionTrackingInterceptor.getConnection(connectionInfo);
        assertTrue("Expected handleObtained call with our connectionTrackingInterceptor",
                connectionTrackingInterceptor == obtainedConnectionTrackingInterceptor);
        assertTrue("Expected handleObtained call with our connectionInfo",
                connectionInfo == obtainedTrackedConnectionInfo);
        //release connection handle
        connectionTrackingInterceptor.returnConnection(connectionInfo, ConnectionReturnAction.RETURN_HANDLE);
        assertTrue("Expected handleReleased call with our connectionTrackingInterceptor",
                connectionTrackingInterceptor == releasedConnectionTrackingInterceptor);
        assertTrue("Expected handleReleased call with our connectionInfo",
                connectionInfo == releasedTrackedConnectionInfo);

    }

    private void getConnectionAndReenter() throws ResourceException {
        ConnectionInfo connectionInfo = makeConnectionInfo();
        connectionTrackingInterceptor.getConnection(connectionInfo);
        //reset our test indicator
        obtainedConnectionInfo = null;
        connectionInfos = new HashSet();
        connectionInfos.add(connectionInfo);
        connectionTrackingInterceptor.enter(connectionInfos);
    }

    public void testEnterWithSameSubject() throws Exception {
        makeSubject("foo");
        getConnectionAndReenter();
        //decision on re-association happens in subject interceptor
        assertTrue("Expected connection asked for", obtainedConnectionInfo != null);
        assertTrue("Expected no connection returned", returnedConnectionInfo == null);
    }

    public void testEnterWithChangedSubject() throws Exception {
        testEnterWithSameSubject();
        makeSubject("bar");
        connectionTrackingInterceptor.enter(connectionInfos);
        //expect re-association
        assertTrue("Expected connection asked for", obtainedConnectionInfo != null);
        //connection is returned by SubjectInterceptor
        assertTrue("Expected no connection returned", returnedConnectionInfo == null);
    }

    public void testExitWithNonDissociatableConnection() throws Exception {
        managedConnection = new TestPlainManagedConnection();
        testEnterWithSameSubject();
        connectionTrackingInterceptor.exit(connectionInfos);
        assertTrue("Expected no connection returned", returnedConnectionInfo == null);
        assertEquals("Expected one info in connectionInfos", connectionInfos.size(), 1);
    }

    public void testExitWithDissociatableConnection() throws Exception {
        managedConnection = new TestDissociatableManagedConnection();
        testEnterWithSameSubject();
        assertEquals("Expected one info in connectionInfos", 1, connectionInfos.size());
        connectionTrackingInterceptor.exit(connectionInfos);
        assertTrue("Expected connection returned", returnedConnectionInfo != null);
        assertEquals("Expected no infos in connectionInfos", 0, connectionInfos.size());
    }

    //ConnectionTracker interface
    public void handleObtained(
            ConnectionTrackingInterceptor connectionTrackingInterceptor,
            ConnectionInfo connectionInfo) {
        obtainedConnectionTrackingInterceptor = connectionTrackingInterceptor;
        obtainedTrackedConnectionInfo = connectionInfo;
    }

    public void handleReleased(
            ConnectionTrackingInterceptor connectionTrackingInterceptor,
            ConnectionInfo connectionInfo) {
        releasedConnectionTrackingInterceptor = connectionTrackingInterceptor;
        releasedTrackedConnectionInfo = connectionInfo;
    }

    public void setEnvironment(ConnectionInfo connectionInfo, String key) {
        //unsharable = false, app security = false;
    }

    //ConnectionInterceptor interface
    public void getConnection(ConnectionInfo connectionInfo) throws ResourceException {
        super.getConnection(connectionInfo);
        ManagedConnectionInfo managedConnectionInfo = connectionInfo.getManagedConnectionInfo();
        managedConnectionInfo.setConnectionEventListener(new GeronimoConnectionEventListener(null, managedConnectionInfo));
        managedConnectionInfo.setSubject(subject);
        if (managedConnectionInfo.getManagedConnection() == null) {
            managedConnectionInfo.setManagedConnection(managedConnection);
        }
        if (connectionInfo.getConnectionHandle() == null) {
            connectionInfo.setConnectionHandle(new Object());
        }
        managedConnectionInfo.addConnectionHandle(connectionInfo);
    }

}
