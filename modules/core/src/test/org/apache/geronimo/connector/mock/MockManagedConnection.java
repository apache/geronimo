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

package org.apache.geronimo.connector.mock;

import java.io.PrintWriter;
import java.util.Set;
import java.util.HashSet;
import java.util.List;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Collections;

import javax.resource.spi.ManagedConnection;
import javax.resource.spi.ConnectionRequestInfo;
import javax.resource.spi.ConnectionEventListener;
import javax.resource.spi.LocalTransaction;
import javax.resource.spi.ManagedConnectionMetaData;
import javax.resource.spi.ConnectionEvent;
import javax.resource.ResourceException;
import javax.security.auth.Subject;
import javax.transaction.xa.XAResource;

/**
 *
 *
 * @version $Revision: 1.1 $ $Date: 2003/12/23 17:34:34 $
 *
 * */
public class MockManagedConnection implements ManagedConnection {

    private final MockManagedConnectionFactory managedConnectionFactory;
    private final MockXAResource mockXAResource;
    private Subject subject;
    private MockConnectionRequestInfo connectionRequestInfo;

    private final Set connections = new HashSet();
    private final List connectionEventListeners = Collections.synchronizedList(new ArrayList());

    private boolean destroyed;
    private PrintWriter logWriter;

    public MockManagedConnection(MockManagedConnectionFactory managedConnectionFactory, Subject subject, MockConnectionRequestInfo connectionRequestInfo) {
        this.managedConnectionFactory = managedConnectionFactory;
        mockXAResource = new MockXAResource(this);
        this.subject = subject;
        this.connectionRequestInfo = connectionRequestInfo;
    }

    public Object getConnection(Subject subject, ConnectionRequestInfo connectionRequestInfo) throws ResourceException {
        checkSecurityConsistency(subject, connectionRequestInfo);
        MockConnection mockConnection = new MockConnection(this, subject, (MockConnectionRequestInfo)connectionRequestInfo);
        connections.add(mockConnection);
        return mockConnection;
    }

    private void checkSecurityConsistency(Subject subject, ConnectionRequestInfo connectionRequestInfo) {
        if (!managedConnectionFactory.isReauthentication()) {
            assert subject == null? this.subject == null: subject.equals(this.subject);
            assert connectionRequestInfo == null? this.connectionRequestInfo == null: connectionRequestInfo.equals(this.connectionRequestInfo);
        }
    }

    public void destroy() throws ResourceException {
        destroyed = true;
        cleanup();
    }

    public void cleanup() throws ResourceException {
        for (Iterator iterator = new HashSet(connections).iterator(); iterator.hasNext();) {
            MockConnection mockConnection = (MockConnection) iterator.next();
            mockConnection.close();
        }
        assert connections.isEmpty();
    }

    public void associateConnection(Object connection) throws ResourceException {
        assert connection != null;
        assert connection.getClass() == MockConnection.class;
        MockConnection mockConnection = (MockConnection)connection;
        checkSecurityConsistency(mockConnection.getSubject(), mockConnection.getConnectionRequestInfo());
        mockConnection.reassociate(this);
        connections.add(mockConnection);
    }

    public void addConnectionEventListener(ConnectionEventListener listener) {
        connectionEventListeners.add(listener);
    }

    public void removeConnectionEventListener(ConnectionEventListener listener) {
        connectionEventListeners.remove(listener);
    }

    public XAResource getXAResource() throws ResourceException {
        return mockXAResource;
    }

    public LocalTransaction getLocalTransaction() throws ResourceException {
        return new MockSPILocalTransaction();
    }

    public ManagedConnectionMetaData getMetaData() throws ResourceException {
        return null;
    }

    public void setLogWriter(PrintWriter logWriter) throws ResourceException {
        this.logWriter = logWriter;
    }

    public PrintWriter getLogWriter() throws ResourceException {
        return logWriter;
    }

    public Subject getSubject() {
        return subject;
    }

    public MockConnectionRequestInfo getConnectionRequestInfo() {
        return connectionRequestInfo;
    }

    public void removeHandle(MockConnection mockConnection) {
        connections.remove(mockConnection);
    }

    public MockManagedConnectionFactory getManagedConnectionFactory() {
        return managedConnectionFactory;
    }

    public Set getConnections() {
        return connections;
    }

    public List getConnectionEventListeners() {
        return connectionEventListeners;
    }

    public boolean isDestroyed() {
        return destroyed;
    }

    public void closedEvent(MockConnection mockConnection) {
        ConnectionEvent connectionEvent = new ConnectionEvent(this, ConnectionEvent.CONNECTION_CLOSED);
        connectionEvent.setConnectionHandle(mockConnection);
        for (Iterator iterator = new ArrayList(connectionEventListeners).iterator(); iterator.hasNext();) {
            ConnectionEventListener connectionEventListener = (ConnectionEventListener) iterator.next();
            connectionEventListener.connectionClosed(connectionEvent);
        }
    }

    public void errorEvent(MockConnection mockConnection) {
        ConnectionEvent connectionEvent = new ConnectionEvent(this, ConnectionEvent.CONNECTION_ERROR_OCCURRED);
        connectionEvent.setConnectionHandle(mockConnection);
        for (Iterator iterator = new ArrayList(connectionEventListeners).iterator(); iterator.hasNext();) {
            ConnectionEventListener connectionEventListener = (ConnectionEventListener) iterator.next();
            connectionEventListener.connectionErrorOccurred(connectionEvent);
        }
    }

    public void localTransactionStartedEvent(MockConnection mockConnection) {
        ConnectionEvent connectionEvent = new ConnectionEvent(this, ConnectionEvent.LOCAL_TRANSACTION_STARTED);
        connectionEvent.setConnectionHandle(mockConnection);
        for (Iterator iterator = new ArrayList(connectionEventListeners).iterator(); iterator.hasNext();) {
            ConnectionEventListener connectionEventListener = (ConnectionEventListener) iterator.next();
            connectionEventListener.localTransactionStarted(connectionEvent);
        }
    }

    public void localTransactionCommittedEvent(MockConnection mockConnection) {
        ConnectionEvent connectionEvent = new ConnectionEvent(this, ConnectionEvent.LOCAL_TRANSACTION_COMMITTED);
        connectionEvent.setConnectionHandle(mockConnection);
        for (Iterator iterator = new ArrayList(connectionEventListeners).iterator(); iterator.hasNext();) {
            ConnectionEventListener connectionEventListener = (ConnectionEventListener) iterator.next();
            connectionEventListener.localTransactionCommitted(connectionEvent);
        }
    }

    public void localTransactionRolledBackEvent(MockConnection mockConnection) {
        ConnectionEvent connectionEvent = new ConnectionEvent(this, ConnectionEvent.LOCAL_TRANSACTION_ROLLEDBACK);
        connectionEvent.setConnectionHandle(mockConnection);
        for (Iterator iterator = new ArrayList(connectionEventListeners).iterator(); iterator.hasNext();) {
            ConnectionEventListener connectionEventListener = (ConnectionEventListener) iterator.next();
            connectionEventListener.localTransactionRolledback(connectionEvent);
        }
    }
}
