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
import javax.transaction.Transaction;
import javax.transaction.xa.XAResource;

import org.apache.geronimo.connector.outbound.connectiontracking.defaultimpl.DefaultTransactionContext;
import org.apache.geronimo.security.bridge.RealmBridge;

import junit.framework.TestCase;

/**
 *
 *
 * @version $Revision: 1.1 $ $Date: 2004/01/23 05:56:11 $
 *
 * */
public class ConnectionManagerTestUtils extends TestCase implements RealmBridge, ConnectionInterceptor {
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

    //RealmBridge implementation
    public Subject mapSubject(Subject sourceSubject) {
        return subject;
    }

    //ConnectorInterceptor implementation
    public void getConnection(ConnectionInfo connectionInfo) throws ResourceException {
        ManagedConnectionInfo managedConnectionInfo = connectionInfo.getManagedConnectionInfo();
        managedConnectionInfo.setManagedConnection(managedConnection);
        obtainedConnectionInfo = connectionInfo;
    }

    public void returnConnection(ConnectionInfo connectionInfo, ConnectionReturnAction connectionReturnAction) {
        returnedConnectionInfo = connectionInfo;
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

    protected ConnectionInfo makeConnectionInfo(Transaction transaction) throws Exception {
        DefaultTransactionContext transactionContext = new DefaultTransactionContext(transaction);
        ManagedConnectionInfo managedConnectionInfo = new ManagedConnectionInfo(null, null);
        managedConnectionInfo.setTransactionContext(transactionContext);
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
