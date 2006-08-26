/**
 *
 * Copyright 2003-2005 The Apache Software Foundation
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

import java.io.PrintWriter;
import java.security.Principal;
import java.util.Set;
import javax.resource.ResourceException;
import javax.resource.spi.ConnectionEventListener;
import javax.resource.spi.ConnectionRequestInfo;
import javax.resource.spi.DissociatableManagedConnection;
import javax.resource.spi.LocalTransaction;
import javax.resource.spi.ManagedConnection;
import javax.resource.spi.ManagedConnectionMetaData;
import javax.security.auth.Subject;
import javax.transaction.xa.XAResource;

import junit.framework.TestCase;

/**
 *
 *
 * @version $Rev$ $Date$
 *
 * */
public class ConnectionInterceptorTestUtils extends TestCase implements ConnectionInterceptor {
    protected Subject subject;
    protected ConnectionInfo obtainedConnectionInfo;
    protected ConnectionInfo returnedConnectionInfo;
    protected ManagedConnection managedConnection;

    protected void setUp() throws Exception {
    }

    protected void tearDown() throws Exception {
        subject = null;
        obtainedConnectionInfo = null;
        returnedConnectionInfo = null;
        managedConnection = null;
    }

    public void testNothing() throws Exception {
    }

    //ConnectorInterceptor implementation
    public void getConnection(ConnectionInfo connectionInfo) throws ResourceException {
        ManagedConnectionInfo managedConnectionInfo = connectionInfo.getManagedConnectionInfo();
        if (managedConnectionInfo.getManagedConnection() == null) {
            managedConnectionInfo.setManagedConnection(managedConnection);
        }
        obtainedConnectionInfo = connectionInfo;
    }

    public void returnConnection(ConnectionInfo connectionInfo, ConnectionReturnAction connectionReturnAction) {
        returnedConnectionInfo = connectionInfo;
    }
    
    public void destroy() {
        
    }

    protected void makeSubject(String principalName) {
        subject = new Subject();
        Set principals = subject.getPrincipals();
        principals.add(new TestPrincipal(principalName));
    }

    protected ConnectionInfo makeConnectionInfo() {
        ManagedConnectionInfo managedConnectionInfo = new ManagedConnectionInfo(null, null);
        return new ConnectionInfo(managedConnectionInfo);
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

    protected static class TestPlainManagedConnection implements ManagedConnection {
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

    protected static class TestDissociatableManagedConnection implements ManagedConnection, DissociatableManagedConnection {
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
