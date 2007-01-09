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

package org.apache.geronimo.connector.outbound;

import org.apache.geronimo.connector.mock.MockXAResource;
import org.apache.geronimo.connector.mock.ConnectionExtension;
import org.apache.geronimo.connector.outbound.connectiontracking.DefaultInterceptor;
import org.apache.geronimo.connector.outbound.connectiontracking.ConnectorInstanceContext;
import org.apache.geronimo.transaction.GeronimoUserTransaction;

/**
 *
 *
 * @version $Rev$ $Date$
 *
 * */
public class ConnectionManagerTest extends ConnectionManagerTestUtils {


    public void testSingleTransactionCall() throws Throwable {
        transactionManager.begin();
        defaultComponentInterceptor.invoke(connectorInstanceContext);
        MockXAResource mockXAResource = (MockXAResource) mockManagedConnection.getXAResource();
        assertEquals("XAResource should know one xid", 1, mockXAResource.getKnownXids().size());
        assertNull("Should not be committed", mockXAResource.getCommitted());
        transactionManager.commit();
        assertNotNull("Should be committed", mockXAResource.getCommitted());
    }

    public void testNoTransactionCall() throws Throwable {
        defaultComponentInterceptor.invoke(connectorInstanceContext);
        MockXAResource mockXAResource = (MockXAResource) mockManagedConnection.getXAResource();
        assertEquals("XAResource should know 0 xid", 0, mockXAResource.getKnownXids().size());
        assertNull("Should not be committed", mockXAResource.getCommitted());
    }

    public void testOneTransactionTwoCalls() throws Throwable {
        transactionManager.begin();
        defaultComponentInterceptor.invoke(connectorInstanceContext);
        MockXAResource mockXAResource = (MockXAResource) mockManagedConnection.getXAResource();
        assertEquals("XAResource should know one xid", 1, mockXAResource.getKnownXids().size());
        assertNull("Should not be committed", mockXAResource.getCommitted());
        defaultComponentInterceptor.invoke(connectorInstanceContext);
        assertEquals("Expected same XAResource", mockXAResource, mockManagedConnection.getXAResource());
        assertEquals("XAResource should know one xid", 1, mockXAResource.getKnownXids().size());
        assertNull("Should not be committed", mockXAResource.getCommitted());
        transactionManager.commit();
        assertNotNull("Should be committed", mockXAResource.getCommitted());
    }

    public void testUserTransactionEnlistingExistingConnections() throws Throwable {
        mockComponent = new DefaultInterceptor() {
            public Object invoke(ConnectorInstanceContext newConnectorInstanceContext) throws Throwable {
                ConnectionExtension mockConnection = (ConnectionExtension) connectionFactory.getConnection();
                mockManagedConnection = mockConnection.getManagedConnection();
                userTransaction.begin();
                MockXAResource mockXAResource = (MockXAResource) mockManagedConnection.getXAResource();
                assertEquals("XAResource should know one xid", 1, mockXAResource.getKnownXids().size());
                assertNull("Should not be committed", mockXAResource.getCommitted());
                userTransaction.commit();
                assertNotNull("Should be committed", mockXAResource.getCommitted());
                mockConnection.close();
                return null;
            }
        };
        userTransaction = new GeronimoUserTransaction(transactionManager);
        defaultComponentInterceptor.invoke(connectorInstanceContext);
        MockXAResource mockXAResource = (MockXAResource) mockManagedConnection.getXAResource();
        assertEquals("XAResource should know 1 xid", 1, mockXAResource.getKnownXids().size());
        assertNotNull("Should be committed", mockXAResource.getCommitted());
        mockXAResource.clear();
    }

    public void testConnectionCloseReturnsCxAfterUserTransaction() throws Throwable {
        for (int i = 0; i < maxSize + 1; i++) {
            testUserTransactionEnlistingExistingConnections();
        }
    }

    public void testUnshareableConnections() throws Throwable {
        unshareableResources.add(name);
        mockComponent = new DefaultInterceptor() {
            public Object invoke(ConnectorInstanceContext newConnectorInstanceContext) throws Throwable {
                ConnectionExtension mockConnection1 = (ConnectionExtension) connectionFactory.getConnection();
                mockManagedConnection = mockConnection1.getManagedConnection();
                ConnectionExtension mockConnection2 = (ConnectionExtension) connectionFactory.getConnection();
                //the 2 cx are for the same RM, so tm will call commit only one one (the first)
                //mockManagedConnection = mockConnection2.getManagedConnection();
                assertNotNull("Expected non-null managedconnection 1", mockConnection1.getManagedConnection());
                assertNotNull("Expected non-null managedconnection 2", mockConnection2.getManagedConnection());
                assertTrue("Expected different managed connections for each unshared handle", mockConnection1.getManagedConnection() != mockConnection2.getManagedConnection());

                mockConnection1.close();
                mockConnection2.close();
                return null;
            }

        };
        transactionManager.begin();
        defaultComponentInterceptor.invoke(connectorInstanceContext);
        MockXAResource mockXAResource = (MockXAResource) mockManagedConnection.getXAResource();
        assertEquals("XAResource should know one xid", 1, mockXAResource.getKnownXids().size());
        assertNull("Should not be committed", mockXAResource.getCommitted());
        transactionManager.commit();
        assertNotNull("Should be committed", mockXAResource.getCommitted());
    }
}
