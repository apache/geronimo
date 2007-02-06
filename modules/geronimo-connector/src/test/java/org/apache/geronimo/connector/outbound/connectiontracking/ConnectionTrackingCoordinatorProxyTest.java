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
package org.apache.geronimo.connector.outbound.connectiontracking;

import junit.framework.TestCase;
import org.apache.geronimo.connector.outbound.ConnectionInterceptor;
import org.apache.geronimo.connector.outbound.ConnectionTrackingInterceptor;
import org.apache.geronimo.connector.outbound.ConnectionInfo;
import org.apache.geronimo.connector.outbound.ConnectionReturnAction;
import org.apache.geronimo.connector.outbound.ManagedConnectionInfo;
import org.apache.geronimo.connector.outbound.GeronimoConnectionEventListener;

import javax.security.auth.Subject;
import javax.resource.ResourceException;
import javax.resource.spi.ManagedConnection;
import javax.resource.spi.ConnectionRequestInfo;
import javax.resource.spi.ConnectionEventListener;
import javax.resource.spi.LocalTransaction;
import javax.resource.spi.ManagedConnectionMetaData;
import javax.transaction.xa.XAResource;
import java.util.Set;
import java.util.HashSet;
import java.util.Map;
import java.lang.reflect.Proxy;
import java.io.PrintWriter;

/**
 * @version $Rev: 476049 $ $Date: 2006-11-16 20:35:17 -0800 (Thu, 16 Nov 2006) $
 */
public class ConnectionTrackingCoordinatorProxyTest extends TestCase implements ConnectionInterceptor {
    private static final String name1 = "foo";
    private ConnectionTrackingCoordinator connectionTrackingCoordinator;
    private ConnectionTrackingInterceptor key1;
    private Subject subject = null;
    private Set unshareableResources;
    private Set applicationManagedSecurityResources;
    private ManagedConnectionInfo mci;
    private ConnectionImpl connection;

    protected void setUp() throws Exception {
        super.setUp();
        connectionTrackingCoordinator = new ConnectionTrackingCoordinator(true);
        key1 = new ConnectionTrackingInterceptor(this, name1, connectionTrackingCoordinator);
        unshareableResources = new HashSet();
        applicationManagedSecurityResources = new HashSet();

        mci = new ManagedConnectionInfo(null, null);
        mci.setManagedConnection(new MockManagedConnection());
        mci.setConnectionEventListener(new GeronimoConnectionEventListener(this, mci));
        connection = new ConnectionImpl("ConnectionString");
    }

    protected void tearDown() throws Exception {
        connectionTrackingCoordinator = null;
        key1 = null;
        super.tearDown();
    }

    public void testSimpleComponentContextLifecyle() throws Exception {
        // enter component context
        ConnectorInstanceContextImpl componentContext = new ConnectorInstanceContextImpl(unshareableResources, applicationManagedSecurityResources);
        ConnectorInstanceContext oldConnectorInstanceContext = connectionTrackingCoordinator.enter(componentContext);
        assertNull("Expected old instance context to be null", oldConnectorInstanceContext);

        // simulate create connection
        ConnectionInfo connectionInfo = createConnectionInfo();
        connectionTrackingCoordinator.handleObtained(key1, connectionInfo, false);

        // connection should be in component instance context
        Map connectionManagerMap = componentContext.getConnectionManagerMap();
        Set infos = (Set) connectionManagerMap.get(key1);
        assertNotNull("Expected one connections for key1", infos);
        assertEquals("Expected one connection for key1", 1, infos.size());
        assertTrue("Expected to get supplied ConnectionInfo from infos", connectionInfo == infos.iterator().next());

        // verify handle
        Object handle = connectionInfo.getConnectionHandle();
        assertNotNull("Expected a handle from ConnectionInfo", handle);
        assertTrue("Expected handle to be an instance of ConnectionImpl", handle instanceof ConnectionImpl);
        ConnectionImpl connection = (ConnectionImpl) handle;
        assertEquals("connection.getString()", "ConnectionString", connection.getString());

        // verify proxy
        Object proxy = connectionInfo.getConnectionProxy();
        assertNotNull("Expected a proxy from ConnectionInfo", proxy);
        assertTrue("Expected proxy to be an instance of Connection", proxy instanceof Connection);
        Connection connectionProxy = (Connection) proxy;
        assertEquals("connection.getString()", "ConnectionString", connectionProxy.getString());
        assertSame("Expected connection.getUnmanaged() to be connectionImpl", connection, connectionProxy.getUnmanaged());
        assertNotSame("Expected connection be proxied", connection, connectionProxy);

        // exit component context
        connectionTrackingCoordinator.exit(oldConnectorInstanceContext);

        // connection should not be in context
        connectionManagerMap = componentContext.getConnectionManagerMap();
        infos = (Set) connectionManagerMap.get(key1);
        assertEquals("Expected no connection set for key1", null, infos);

        // enter again, and close the handle
        oldConnectorInstanceContext = connectionTrackingCoordinator.enter(componentContext);
        assertNull("Expected old instance context to be null", oldConnectorInstanceContext);
        connectionTrackingCoordinator.handleReleased(key1, connectionInfo, ConnectionReturnAction.DESTROY);
        connectionTrackingCoordinator.exit(oldConnectorInstanceContext);

        // use connection which will cause it to get a new handle if it is not closed
        ConnectionTrackingCoordinator.ConnectionInvocationHandler invocationHandler = (ConnectionTrackingCoordinator.ConnectionInvocationHandler) Proxy.getInvocationHandler(connectionProxy);
        assertEquals("connection.getString()", "ConnectionString", connectionProxy.getString());
        assertTrue("Proxy should be connected", invocationHandler.isReleased());
        assertSame("Expected connection.getUnmanaged() to be original connection", connection, connection.getUnmanaged());
    }

    public void testReassociateConnection() throws Exception {
        // enter component context
        ConnectorInstanceContextImpl componentContext = new ConnectorInstanceContextImpl(unshareableResources, applicationManagedSecurityResources);
        ConnectorInstanceContext oldConnectorInstanceContext1 = connectionTrackingCoordinator.enter(componentContext);
        assertNull("Expected old component context to be null", oldConnectorInstanceContext1);

        // simulate create connection
        ConnectionInfo connectionInfo = createConnectionInfo();
        connectionTrackingCoordinator.handleObtained(key1, connectionInfo, false);

        // connection should be in component instance context
        Map connectionManagerMap = componentContext.getConnectionManagerMap();
        Set infos = (Set) connectionManagerMap.get(key1);
        assertNotNull("Expected one connections for key1", infos);
        assertEquals("Expected one connection for key1", 1, infos.size());
        assertTrue("Expected to get supplied ConnectionInfo from infos", connectionInfo == infos.iterator().next());

        // verify handle
        Object handle = connectionInfo.getConnectionHandle();
        assertNotNull("Expected a handle from ConnectionInfo", handle);
        assertTrue("Expected handle to be an instance of ConnectionImpl", handle instanceof ConnectionImpl);
        ConnectionImpl connection = (ConnectionImpl) handle;
        assertEquals("connection.getString()", "ConnectionString", connection.getString());

        // verify proxy
        Object proxy = connectionInfo.getConnectionProxy();
        assertNotNull("Expected a proxy from ConnectionInfo", proxy);
        assertTrue("Expected proxy to be an instance of Connection", proxy instanceof Connection);
        Connection connectionProxy = (Connection) proxy;
        assertEquals("connection.getString()", "ConnectionString", connectionProxy.getString());
        assertSame("Expected connection.getUnmanaged() to be connectionImpl", connection, connectionProxy.getUnmanaged());
        assertNotSame("Expected connection be proxied", connection, connectionProxy);


        // exit outer component context
        connectionTrackingCoordinator.exit(oldConnectorInstanceContext1);

        // proxy should be disconnected
        ConnectionTrackingCoordinator.ConnectionInvocationHandler invocationHandler = (ConnectionTrackingCoordinator.ConnectionInvocationHandler) Proxy.getInvocationHandler(connectionProxy);
        assertTrue("Proxy should be disconnected", invocationHandler.isReleased());

        // enter again
        oldConnectorInstanceContext1 = connectionTrackingCoordinator.enter(componentContext);
        assertNull("Expected old component context to be null", oldConnectorInstanceContext1);

        // use connection to cause it to get a new handle
        assertEquals("connection.getString()", "ConnectionString", connectionProxy.getString());
        assertSame("Expected connection.getUnmanaged() to be original connection", connection, connection.getUnmanaged());
        assertNotSame("Expected connection to not be original connection", connection, connectionProxy);

        // destroy handle and exit context
        connectionTrackingCoordinator.handleReleased(key1, connectionInfo, ConnectionReturnAction.DESTROY);
        connectionTrackingCoordinator.exit(oldConnectorInstanceContext1);

        // connection should not be in context
        connectionManagerMap = componentContext.getConnectionManagerMap();
        infos = (Set) connectionManagerMap.get(key1);
        assertNull("Expected no connection set for key1", infos);

        // use connection which will cause it to get a new handle if it is not closed
        assertEquals("connection.getString()", "ConnectionString", connectionProxy.getString());
        assertTrue("Proxy should be connected", invocationHandler.isReleased());
        assertSame("Expected connection.getUnmanaged() to be original connection", connection, connection.getUnmanaged());

    }

    // some code calls the release method using a freshly constructed ContainerInfo without a proxy
    // the ConnectionTrackingCoordinator must find the correct proxy and call releaseHandle or destroy
    public void testReleaseNoProxy() throws Exception {
        // enter component context
        ConnectorInstanceContextImpl componentContext = new ConnectorInstanceContextImpl(unshareableResources, applicationManagedSecurityResources);
        ConnectorInstanceContext oldConnectorInstanceContext1 = connectionTrackingCoordinator.enter(componentContext);
        assertNull("Expected old component context to be null", oldConnectorInstanceContext1);

        // simulate create connection
        ConnectionInfo connectionInfo = createConnectionInfo();
        connectionTrackingCoordinator.handleObtained(key1, connectionInfo, false);

        // connection should be in component instance context
        Map connectionManagerMap = componentContext.getConnectionManagerMap();
        Set infos = (Set) connectionManagerMap.get(key1);
        assertNotNull("Expected one connections for key1", infos);
        assertEquals("Expected one connection for key1", 1, infos.size());
        assertTrue("Expected to get supplied ConnectionInfo from infos", connectionInfo == infos.iterator().next());

        // verify handle
        Object handle = connectionInfo.getConnectionHandle();
        assertNotNull("Expected a handle from ConnectionInfo", handle);
        assertTrue("Expected handle to be an instance of ConnectionImpl", handle instanceof ConnectionImpl);
        ConnectionImpl connection = (ConnectionImpl) handle;
        assertEquals("connection.getString()", "ConnectionString", connection.getString());

        // verify proxy
        Object proxy = connectionInfo.getConnectionProxy();
        assertNotNull("Expected a proxy from ConnectionInfo", proxy);
        assertTrue("Expected proxy to be an instance of Connection", proxy instanceof Connection);
        Connection connectionProxy = (Connection) proxy;
        assertEquals("connection.getString()", "ConnectionString", connectionProxy.getString());
        assertSame("Expected connection.getUnmanaged() to be connectionImpl", connection, connectionProxy.getUnmanaged());
        assertNotSame("Expected connection be proxied", connection, connectionProxy);


        // simulate handle release due to a event listener, which won't hav the proxy
        connectionTrackingCoordinator.handleReleased(key1, createConnectionInfo(), ConnectionReturnAction.RETURN_HANDLE);

        // proxy should be disconnected
        ConnectionTrackingCoordinator.ConnectionInvocationHandler invocationHandler = (ConnectionTrackingCoordinator.ConnectionInvocationHandler) Proxy.getInvocationHandler(connectionProxy);
        assertTrue("Proxy should be disconnected", invocationHandler.isReleased());

        // use connection which will cause it to get a new handle if it is not closed
        assertEquals("connection.getString()", "ConnectionString", connectionProxy.getString());
        assertTrue("Proxy should be connected", invocationHandler.isReleased());
        assertSame("Expected connection.getUnmanaged() to be original connection", connection, connection.getUnmanaged());

        // exit outer component context
        connectionTrackingCoordinator.exit(oldConnectorInstanceContext1);

        // proxy should be disconnected
        assertTrue("Proxy should be disconnected", invocationHandler.isReleased());

        // connection should not be in context
        connectionManagerMap = componentContext.getConnectionManagerMap();
        infos = (Set) connectionManagerMap.get(key1);
        assertNull("Expected no connection set for key1", infos);

        // enter again
        oldConnectorInstanceContext1 = connectionTrackingCoordinator.enter(componentContext);
        assertNull("Expected old component context to be null", oldConnectorInstanceContext1);

        // exit context
        connectionTrackingCoordinator.exit(oldConnectorInstanceContext1);

        // use connection which will cause it to get a new handle if it is not closed
        assertEquals("connection.getString()", "ConnectionString", connectionProxy.getString());
        assertTrue("Proxy should be connected", invocationHandler.isReleased());
        assertSame("Expected connection.getUnmanaged() to be original connection", connection, connection.getUnmanaged());
    }


    public Subject mapSubject(Subject sourceSubject) {
        return subject;
    }

    public void getConnection(ConnectionInfo connectionInfo) throws ResourceException {
    }

    public void returnConnection(ConnectionInfo connectionInfo, ConnectionReturnAction connectionReturnAction) {
    }

    public void destroy() {
    }

    private ConnectionInfo createConnectionInfo() {
        ConnectionInfo ci = new ConnectionInfo(mci);
        ci.setConnectionHandle(connection);
        mci.addConnectionHandle(ci);
        return ci;
    }


    public static interface Connection {
        String getString();
        Connection getUnmanaged();
    }

    public static class ConnectionImpl implements Connection {
        private final String string;

        public ConnectionImpl(String string) {
            this.string = string;
        }

        public String getString() {
            return string;
        }

        public Connection getUnmanaged() {
            return this;
        }
    }

    public static class MockManagedConnection implements ManagedConnection {
           public Object getConnection(Subject defaultSubject, ConnectionRequestInfo connectionRequestInfo) throws ResourceException {
               return null;
           }

           public void destroy() throws ResourceException {
           }

           public void cleanup() throws ResourceException {
           }

           public void associateConnection(Object object) throws ResourceException {
           }

           public void addConnectionEventListener(ConnectionEventListener connectionEventListener) {
           }

           public void removeConnectionEventListener(ConnectionEventListener connectionEventListener) {
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

           public void setLogWriter(PrintWriter printWriter) throws ResourceException {
           }

           public PrintWriter getLogWriter() throws ResourceException {
               return null;
           }

           public void dissociateConnections() throws ResourceException {
           }
       }
}
