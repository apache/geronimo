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

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import javax.resource.ResourceException;

import org.apache.geronimo.connector.outbound.connectiontracking.ConnectionTracker;
import org.apache.geronimo.transaction.TransactionContext;

/**
 * TODO test unshareable resources.
 * TODO test repeat calls with null/non-null Subject
 *
 * @version $Revision: 1.3 $ $Date: 2004/02/25 09:57:13 $
 *
 * */
public class ConnectionTrackingInterceptorTest extends ConnectionManagerTestUtils
        implements ConnectionTracker {

    private final static String key = "test-name";
    private ConnectionTrackingInterceptor connectionTrackingInterceptor;


    private ConnectionTrackingInterceptor obtainedConnectionTrackingInterceptor;
    private ConnectionInfo obtainedTrackedConnectionInfo;

    private ConnectionTrackingInterceptor releasedConnectionTrackingInterceptor;
    private ConnectionInfo releasedTrackedConnectionInfo;

    private Collection connectionInfos;
    private Set unshareable;

    protected void setUp() throws Exception {
        super.setUp();
        connectionTrackingInterceptor = new ConnectionTrackingInterceptor(this, key, this, this);
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


    public void testEnterWithNullSubject() throws Exception {
        getConnectionAndReenter();
        //expect no re-association
        assertTrue("Expected no connection asked for", obtainedConnectionInfo == null);
        assertTrue("Expected no connection returned", returnedConnectionInfo == null);
    }

    private void getConnectionAndReenter() throws ResourceException {
        ConnectionInfo connectionInfo = makeConnectionInfo();
        connectionTrackingInterceptor.getConnection(connectionInfo);
        //reset our test indicator
        obtainedConnectionInfo = null;
        connectionInfos = new HashSet();
        connectionInfos.add(connectionInfo);
        unshareable = new HashSet();
        connectionTrackingInterceptor.enter(connectionInfos, unshareable);
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
        connectionTrackingInterceptor.enter(connectionInfos, unshareable);
        //expect re-association
        assertTrue("Expected connection asked for", obtainedConnectionInfo != null);
        //connection is returned by SubjectInterceptor
        assertTrue("Expected no connection returned", returnedConnectionInfo == null);
    }

    public void testExitWithNonDissociatableConnection() throws Exception {
        managedConnection = new TestPlainManagedConnection();
        testEnterWithSameSubject();
        connectionTrackingInterceptor.exit(connectionInfos, unshareable);
        assertTrue("Expected no connection returned", returnedConnectionInfo == null);
        assertEquals("Expected one info in connectionInfos", connectionInfos.size(), 1);
    }

    public void testExitWithDissociatableConnection() throws Exception {
        managedConnection = new TestDissociatableManagedConnection();
        testEnterWithSameSubject();
        connectionTrackingInterceptor.exit(connectionInfos, unshareable);
        assertTrue("Expected connection returned", returnedConnectionInfo != null);
        assertEquals("Expected no infos in connectionInfos", connectionInfos.size(), 0);
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

    public TransactionContext getTransactionContext() {
        return null;
    }

    //ConnectionInterceptor interface
    public void getConnection(ConnectionInfo connectionInfo) throws ResourceException {
        super.getConnection(connectionInfo);
        ManagedConnectionInfo managedConnectionInfo = connectionInfo.getManagedConnectionInfo();
        managedConnectionInfo.setConnectionEventListener(new GeronimoConnectionEventListener(null, managedConnectionInfo));
        managedConnectionInfo.setSubject(subject);
        managedConnectionInfo.setManagedConnection(managedConnection);
        connectionInfo.setConnectionHandle(new Object());
        managedConnectionInfo.addConnectionHandle(connectionInfo);
    }

}
