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
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.Collections;

import javax.resource.ResourceException;
import javax.resource.spi.ConnectionManager;
import javax.resource.spi.ConnectionRequestInfo;
import javax.resource.spi.ManagedConnection;
import javax.resource.spi.ManagedConnectionFactory;
import javax.resource.spi.ResourceAdapter;
import javax.security.auth.Subject;
import javax.validation.constraints.Pattern;

/**
 *
 *
 * @version $Rev$ $Date$
 *
 * */
public class MockManagedConnectionFactory implements ManagedConnectionFactory {

    private MockResourceAdapter resourceAdapter;
    private PrintWriter logWriter;

    private final Set managedConnections = Collections.synchronizedSet(new HashSet());

    private boolean reauthentication;

    public String getOutboundStringProperty1() {
        return outboundStringProperty1;
    }

    public void setOutboundStringProperty1(String outboundStringProperty1) {
        this.outboundStringProperty1 = outboundStringProperty1;
    }

    public String getOutboundStringProperty2() {
        return outboundStringProperty2;
    }

    public void setOutboundStringProperty2(String outboundStringProperty2) {
        this.outboundStringProperty2 = outboundStringProperty2;
    }

    public String getOutboundStringProperty3() {
        return outboundStringProperty3;
    }

    public void setOutboundStringProperty3(String outboundStringProperty3) {
        this.outboundStringProperty3 = outboundStringProperty3;
    }

    public String getOutboundStringProperty4() {
        return outboundStringProperty4;
    }

    public void setOutboundStringProperty4(String outboundStringProperty4) {
        this.outboundStringProperty4 = outboundStringProperty4;
    }

    // add a simple validation pattern to these fields to allow validation tests.  This 
    // pattern will allow any word characters as a value, but not the null string. 
    @Pattern(regexp="[\\w]+" )
    private String outboundStringProperty1;
    @Pattern(regexp="[\\w]+" )
    private String outboundStringProperty2;
    @Pattern(regexp="[\\w]+" )
    private String outboundStringProperty3;
    @Pattern(regexp="[\\w]+" )
    private String outboundStringProperty4;

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
//                return managedConnection;
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
