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

package org.apache.geronimo.connector.mock;

import java.io.PrintWriter;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import javax.resource.ResourceException;
import javax.resource.spi.ConnectionManager;
import javax.resource.spi.ConnectionRequestInfo;
import javax.resource.spi.ManagedConnection;
import javax.resource.spi.ManagedConnectionFactory;
import javax.resource.spi.ResourceAdapter;
import javax.security.auth.Subject;

/**
 *
 *
 * @version $Revision: 1.2 $ $Date: 2004/02/25 09:57:12 $
 *
 * */
public class MockManagedConnectionFactory implements ManagedConnectionFactory {

    private MockResourceAdapter resourceAdapter;
    private PrintWriter logWriter;

    private final Set managedConnections = new HashSet();

    private boolean reauthentication;

    public void setResourceAdapter(ResourceAdapter resourceAdapter) throws ResourceException {
        assert this.resourceAdapter == null: "Setting ResourceAdapter twice";
        assert resourceAdapter != null: "trying to set resourceAdapter to null";
        this.resourceAdapter = (MockResourceAdapter) resourceAdapter;
    }

    public ResourceAdapter getResourceAdapter() {
        return resourceAdapter;
    }

    public Object createConnectionFactory(ConnectionManager connectionManager) throws ResourceException {
        return new MockConnectionFactory(this, connectionManager);
    }

    public Object createConnectionFactory() throws ResourceException {
        return null;
    }

    public ManagedConnection createManagedConnection(Subject subject, ConnectionRequestInfo connectionRequestInfo) throws ResourceException {
        MockManagedConnection managedConnection = new MockManagedConnection(this, subject, (MockConnectionRequestInfo) connectionRequestInfo);
        managedConnections.add(managedConnection);
        return managedConnection;
    }

    public ManagedConnection matchManagedConnections(Set connectionSet, Subject subject, ConnectionRequestInfo cxRequestInfo) throws ResourceException {
        if (reauthentication) {
            for (Iterator iterator = connectionSet.iterator(); iterator.hasNext();) {
                ManagedConnection managedConnection = (ManagedConnection) iterator.next();
                if (managedConnections.contains(managedConnection)) {
                    return managedConnection;
                }
            }
        } else {
            for (Iterator iterator = connectionSet.iterator(); iterator.hasNext();) {
                ManagedConnection managedConnection = (ManagedConnection) iterator.next();
                if (managedConnections.contains(managedConnection)) {
                    MockManagedConnection mockManagedConnection = (MockManagedConnection) managedConnection;
                    if ((subject == null ? mockManagedConnection.getSubject() == null : subject.equals(mockManagedConnection.getSubject())
                            && (cxRequestInfo == null ? mockManagedConnection.getConnectionRequestInfo() == null : cxRequestInfo.equals(mockManagedConnection.getConnectionRequestInfo())))) {
                        return mockManagedConnection;
                    }
                }
            }
        }
        return null;
    }

    public void setLogWriter(PrintWriter logWriter) throws ResourceException {
        this.logWriter = logWriter;
    }

    public PrintWriter getLogWriter() throws ResourceException {
        return logWriter;
    }

    public boolean isReauthentication() {
        return reauthentication;
    }

    public void setReauthentication(boolean reauthentication) {
        this.reauthentication = reauthentication;
    }

    public Set getManagedConnections() {
        return managedConnections;
    }
}
