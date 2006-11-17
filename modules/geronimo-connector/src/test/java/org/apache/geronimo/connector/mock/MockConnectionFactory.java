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

import javax.naming.NamingException;
import javax.naming.Reference;
import javax.resource.ResourceException;
import javax.resource.cci.Connection;
import javax.resource.cci.ConnectionFactory;
import javax.resource.cci.ConnectionSpec;
import javax.resource.cci.RecordFactory;
import javax.resource.cci.ResourceAdapterMetaData;
import javax.resource.spi.ConnectionManager;

/**
 *
 *
 * @version $Rev$ $Date$
 *
 * */
public class MockConnectionFactory implements ConnectionFactoryExtension {

    private ConnectionManager connectionManager;
    private MockManagedConnectionFactory managedConnectionFactory;
    private Reference reference;

    public MockConnectionFactory(MockManagedConnectionFactory managedConnectionFactory, ConnectionManager connectionManager) {
        this.managedConnectionFactory = managedConnectionFactory;
        this.connectionManager = connectionManager;
    }

    public Connection getConnection() throws ResourceException {
        return getConnection(null);
    }

    public Connection getConnection(ConnectionSpec properties) throws ResourceException {
        return (MockConnection) connectionManager.allocateConnection(managedConnectionFactory, (MockConnectionRequestInfo) properties);
    }

    public RecordFactory getRecordFactory() throws ResourceException {
        return null;
    }

    public ResourceAdapterMetaData getMetaData() throws ResourceException {
        return null;
    }

    public void setReference(Reference reference) {
        this.reference = reference;
    }

    public Reference getReference() throws NamingException {
        return reference;
    }

    public String doSomethingElse() {
        return "SomethingElse";
    }
}
