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

package org.apache.geronimo.connector.mock;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import javax.resource.ResourceException;
import javax.resource.spi.ConnectionEvent;
import javax.resource.spi.ConnectionEventListener;
import javax.resource.spi.ConnectionRequestInfo;
import javax.resource.spi.LocalTransaction;
import javax.resource.spi.ManagedConnection;
import javax.resource.spi.ManagedConnectionMetaData;
import javax.security.auth.Subject;
import javax.transaction.xa.XAResource;

/**
 *
 *
 * @version $Rev$ $Date$
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
        MockConnection mockConnection = new MockConnection(this, subject, (MockConnectionRequestInfo) connectionRequestInfo);
        connections.add(mockConnection);
        return mockConnection;
    }

    private void checkSecurityConsistency(Subject subject, ConnectionRequestInfo connectionRequestInfo) {
        if (!managedConnectionFactory.isReauthentication()) {
            assert subject == null ? this.subject == null : subject.equals(this.subject);
            assert connectionRequestInfo == null ? this.connectionRequestInfo == null : connectionRequestInfo.equals(this.connectionRequestInfo);
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
        MockConnection mockConnection = (MockConnection) connection;
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
