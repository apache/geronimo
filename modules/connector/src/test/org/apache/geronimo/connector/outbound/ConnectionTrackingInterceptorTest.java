/* ====================================================================
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 2003 The Apache Software Foundation.  All rights
 * reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. The end-user documentation included with the redistribution,
 *    if any, must include the following acknowledgment:
 *       "This product includes software developed by the
 *        Apache Software Foundation (http://www.apache.org/)."
 *    Alternately, this acknowledgment may appear in the software itself,
 *    if and wherever such third-party acknowledgments normally appear.
 *
 * 4. The names "Apache" and "Apache Software Foundation" and
 *    "Apache Geronimo" must not be used to endorse or promote products
 *    derived from this software without prior written permission. For
 *    written permission, please contact apache@apache.org.
 *
 * 5. Products derived from this software may not be called "Apache",
 *    "Apache Geronimo", nor may "Apache" appear in their name, without
 *    prior written permission of the Apache Software Foundation.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE APACHE SOFTWARE FOUNDATION OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 *
 * ====================================================================
 */

package org.apache.geronimo.connector.outbound;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import javax.resource.ResourceException;

import org.apache.geronimo.connector.outbound.connectiontracking.ConnectionTracker;

/**
 * TODO test unshareable resources.
 * TODO test repeat calls with null/non-null Subject
 *
 * @version $Revision: 1.1 $ $Date: 2004/01/23 05:56:11 $
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

    public ConnectorTransactionContext getConnectorTransactionContext() {
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
