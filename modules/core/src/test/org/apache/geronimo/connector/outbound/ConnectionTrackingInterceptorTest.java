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

import java.util.HashSet;
import java.util.Collection;
import java.util.Set;
import java.security.Principal;
import java.io.PrintWriter;

import javax.resource.ResourceException;
import javax.resource.spi.ManagedConnection;
import javax.resource.spi.ConnectionRequestInfo;
import javax.resource.spi.ConnectionEventListener;
import javax.resource.spi.LocalTransaction;
import javax.resource.spi.ManagedConnectionMetaData;
import javax.resource.spi.DissociatableManagedConnection;
import javax.security.auth.Subject;
import javax.transaction.xa.XAResource;

import junit.framework.TestCase;
import org.apache.geronimo.connector.outbound.connectiontracking.ConnectionTracker;

/**
 * TODO test unshareable resources.
 * TODO test repeat calls with null/non-null Subject
 *
 * @version $Revision: 1.1 $ $Date: 2003/12/09 04:17:39 $
 *
 * */
public class ConnectionTrackingInterceptorTest extends TestCase
    implements ConnectionTracker, ConnectionInterceptor, SecurityDomain {

    private final static String key = "test-name";
    private ConnectionTrackingInterceptor connectionTrackingInterceptor;

    private Subject subject;

    private ConnectionTrackingInterceptor obtainedConnectionTrackingInterceptor;
    private ConnectionInfo obtainedConnectionInfo;

    private ConnectionTrackingInterceptor releasedConnectionTrackingInterceptor;
    private ConnectionInfo releasedConnectionInfo;
    private boolean gotConnection;
    private boolean returnedConnection;

    private Collection connectionInfos;
    private Set unshareable;

    private ManagedConnection managedConnection;

    protected void setUp() throws Exception {
        connectionTrackingInterceptor = new ConnectionTrackingInterceptor(this, key, this, this);
    }

    protected void tearDown() throws Exception {
        connectionTrackingInterceptor = null;
        subject = null;
        managedConnection = null;
        obtainedConnectionTrackingInterceptor = null;
        obtainedConnectionInfo = null;
        releasedConnectionTrackingInterceptor = null;
        releasedConnectionInfo = null;
        gotConnection = false;
        returnedConnection = false;
    }

    public void testConnectionRegistration() throws Exception {
        ConnectionInfo connectionInfo = new ConnectionInfo();
        connectionTrackingInterceptor.getConnection(connectionInfo);
        assertTrue("Expected handleObtained call with our connectionTrackingInterceptor",
                connectionTrackingInterceptor == obtainedConnectionTrackingInterceptor);
        assertTrue("Expected handleObtained call with our connectionInfo",
                connectionInfo == obtainedConnectionInfo);
        //release connection handle
        connectionTrackingInterceptor.returnConnection(connectionInfo, ConnectionReturnAction.RETURN_HANDLE);
        assertTrue("Expected handleReleased call with our connectionTrackingInterceptor",
                    connectionTrackingInterceptor == releasedConnectionTrackingInterceptor);
            assertTrue("Expected handleReleased call with our connectionInfo",
                    connectionInfo == releasedConnectionInfo);

    }

    //Well, subject is null if this is called directly
    public void testEnterWithNullSubject() throws Exception {
        ConnectionInfo connectionInfo = new ConnectionInfo();
        //easy way to get ManagedConnectionInfo set up
        connectionTrackingInterceptor.getConnection(connectionInfo);
        //reset our test indicator
        gotConnection = false;
        connectionInfos = new HashSet();
        connectionInfos.add(connectionInfo);
        unshareable = new HashSet();
        connectionTrackingInterceptor.enter(connectionInfos, unshareable);
        //expect no re-association
        assertTrue("Expected no connection asked for", !gotConnection);
        assertTrue("Expected no connection returned", !returnedConnection);
    }

    public void testEnterWithSameSubject() throws Exception {
        makeSubject("foo");
        testEnterWithNullSubject();
    }

    public void testEnterWithChangedSubject() throws Exception {
        testEnterWithSameSubject();
        makeSubject("bar");
        connectionTrackingInterceptor.enter(connectionInfos, unshareable);
        //expect re-association
        assertTrue("Expected connection asked for", gotConnection);
        assertTrue("Expected connection returned", returnedConnection);
    }

    public void testExitWithNonDissociatableConnection() throws Exception {
        managedConnection = new TestPlainManagedConnection();
        testEnterWithSameSubject();
        connectionTrackingInterceptor.exit(connectionInfos, unshareable);
        assertTrue("Expected no connection returned", !returnedConnection);
        assertEquals("Expected one info in connectionInfos", connectionInfos.size(), 1);
    }

    public void testExitWithDissociatableConnection() throws Exception {
        managedConnection = new TestDissociatableManagedConnection();
        testEnterWithSameSubject();
        connectionTrackingInterceptor.exit(connectionInfos, unshareable);
        assertTrue("Expected connection returned", returnedConnection);
        assertEquals("Expected no infos in connectionInfos", connectionInfos.size(), 0);
    }

    private void makeSubject(String principalName) {
        subject = new Subject();
        Set principals = subject.getPrincipals();
        principals.add(new TestPrincipal(principalName));
    }

    //ConnectionTracker interface
    public void handleObtained(
            ConnectionTrackingInterceptor connectionTrackingInterceptor,
            ConnectionInfo connectionInfo) {
        obtainedConnectionTrackingInterceptor = connectionTrackingInterceptor;
        obtainedConnectionInfo = connectionInfo;
    }

    public void handleReleased(
            ConnectionTrackingInterceptor connectionTrackingInterceptor,
            ConnectionInfo connectionInfo) {
        releasedConnectionTrackingInterceptor = connectionTrackingInterceptor;
        releasedConnectionInfo = connectionInfo;
    }

    public ConnectorTransactionContext getConnectorTransactionContext() {
        return null;
    }

    //ConnectionInterceptor interface
    public void getConnection(ConnectionInfo connectionInfo) throws ResourceException {
        ManagedConnectionInfo managedConnectionInfo = new ManagedConnectionInfo(null, null);
        managedConnectionInfo.setConnectionEventListener(new GeronimoConnectionEventListener(null, managedConnectionInfo));
        managedConnectionInfo.setSubject(subject);
        managedConnectionInfo.setManagedConnection(managedConnection);
        connectionInfo.setManagedConnectionInfo(managedConnectionInfo);
        gotConnection = true;
    }

    public void returnConnection(ConnectionInfo connectionInfo, ConnectionReturnAction connectionReturnAction) {
        returnedConnection = true;
    }

    //SecurityDomain interface
    public Subject getSubject() {
        return subject;
    }

    private static class TestPrincipal implements Principal {

        private final String name;

        public TestPrincipal(String name) {
            this.name = name;
        }
        public String getName() {
            return name;
        }

    }

    private static class TestPlainManagedConnection implements ManagedConnection {
        public Object getConnection(Subject subject, ConnectionRequestInfo cxRequestInfo) throws ResourceException {
            return null;
        }

        public void destroy() throws ResourceException {
        }

        public void cleanup() throws ResourceException {
        }

        public void associateConnection(Object connection) throws ResourceException {
        }

        public void addConnectionEventListener(ConnectionEventListener listener) {
        }

        public void removeConnectionEventListener(ConnectionEventListener listener) {
        }

        public XAResource getXAResource() throws ResourceException {
            return null;
        }

        public LocalTransaction getLocalTransaction() throws ResourceException {
            return null;
        }

        public ManagedConnectionMetaData getMetaData() throws ResourceException {
            return null;
        }

        public void setLogWriter(PrintWriter out) throws ResourceException {
        }

        public PrintWriter getLogWriter() throws ResourceException {
            return null;
        }

    }

    private static class TestDissociatableManagedConnection implements ManagedConnection, DissociatableManagedConnection {
        public void dissociateConnections() throws ResourceException {
        }

        public Object getConnection(Subject subject, ConnectionRequestInfo cxRequestInfo) throws ResourceException {
            return null;
        }

        public void destroy() throws ResourceException {
        }

        public void cleanup() throws ResourceException {
        }

        public void associateConnection(Object connection) throws ResourceException {
        }

        public void addConnectionEventListener(ConnectionEventListener listener) {
        }

        public void removeConnectionEventListener(ConnectionEventListener listener) {
        }

        public XAResource getXAResource() throws ResourceException {
            return null;
        }

        public LocalTransaction getLocalTransaction() throws ResourceException {
            return null;
        }

        public ManagedConnectionMetaData getMetaData() throws ResourceException {
            return null;
        }

        public void setLogWriter(PrintWriter out) throws ResourceException {
        }

        public PrintWriter getLogWriter() throws ResourceException {
            return null;
        }

    }
}
