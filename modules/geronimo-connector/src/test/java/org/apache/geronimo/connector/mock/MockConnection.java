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

import javax.resource.ResourceException;
import javax.resource.cci.Connection;
import javax.resource.cci.ConnectionMetaData;
import javax.resource.cci.Interaction;
import javax.resource.cci.LocalTransaction;
import javax.resource.cci.ResultSetInfo;
import javax.security.auth.Subject;

/**
 *
 *
 * @version $Rev$ $Date$
 *
 * */
public class MockConnection implements Connection {

    private MockManagedConnection managedConnection;
    private Subject subject;
    private MockConnectionRequestInfo connectionRequestInfo;

    private boolean closed;


    public MockConnection(MockManagedConnection managedConnection, Subject subject, MockConnectionRequestInfo connectionRequestInfo) {
        this.managedConnection = managedConnection;
        this.subject = subject;
        this.connectionRequestInfo = connectionRequestInfo;
    }

    public Interaction createInteraction() throws ResourceException {
        return null;
    }

    public LocalTransaction getLocalTransaction() throws ResourceException {
        return new MockCCILocalTransaction(this);
    }

    public ConnectionMetaData getMetaData() throws ResourceException {
        return null;
    }

    public ResultSetInfo getResultSetInfo() throws ResourceException {
        return null;
    }

    public void close() throws ResourceException {
        closed = true;
        managedConnection.removeHandle(this);
        managedConnection.closedEvent(this);
    }

    public void error() {
        managedConnection.errorEvent(this);
    }

    public MockManagedConnection getManagedConnection() {
        return managedConnection;
    }

    public Subject getSubject() {
        return subject;
    }

    public MockConnectionRequestInfo getConnectionRequestInfo() {
        return connectionRequestInfo;
    }

    public boolean isClosed() {
        return closed;
    }

    public void reassociate(MockManagedConnection mockManagedConnection) {
        assert managedConnection != null;
        managedConnection.removeHandle(this);
        managedConnection = mockManagedConnection;
        subject = mockManagedConnection.getSubject();
        connectionRequestInfo = mockManagedConnection.getConnectionRequestInfo();
    }
}
