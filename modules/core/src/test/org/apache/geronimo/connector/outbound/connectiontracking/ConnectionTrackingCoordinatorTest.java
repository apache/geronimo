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

package org.apache.geronimo.connector.outbound.connectiontracking;

import java.util.Set;
import java.util.HashSet;
import java.util.Map;

import javax.security.auth.Subject;
import javax.transaction.TransactionManager;
import javax.transaction.Transaction;

import junit.framework.TestCase;
import org.apache.geronimo.connector.outbound.ConnectionTrackingInterceptor;
import org.apache.geronimo.connector.outbound.SecurityDomain;
import org.apache.geronimo.connector.outbound.ConnectorComponentContext;
import org.apache.geronimo.connector.outbound.ConnectionInfo;
import org.apache.geronimo.connector.outbound.ManagedConnectionInfo;
import org.apache.geronimo.connector.outbound.ConnectorTransactionContext;
import org.apache.geronimo.connector.outbound.connectiontracking.defaultimpl.DefaultComponentContext;
import org.apache.geronimo.connector.outbound.connectiontracking.defaultimpl.DefaultTransactionContext;
import org.apache.geronimo.transaction.manager.TransactionManagerImpl;

/**
 *
 *
 * @version $Revision: 1.2 $ $Date: 2003/12/10 09:39:46 $
 *
 * */
public class ConnectionTrackingCoordinatorTest extends TestCase
        implements SecurityDomain {

    private static final String name1 = "foo";
    private static final String name2 = "bar";
    private ConnectionTrackingCoordinator connectionTrackingCoordinator;
    private ConnectionTrackingInterceptor key1;
    private ConnectionTrackingInterceptor key2;
    private Subject subject = null;
    private Set unshareableResources;
    private TransactionManager transactionManager;


    protected void setUp() throws Exception {
        connectionTrackingCoordinator = new ConnectionTrackingCoordinator();
        key1 = new ConnectionTrackingInterceptor(null, name1, connectionTrackingCoordinator, this);
        key2 = new ConnectionTrackingInterceptor(null, name2, connectionTrackingCoordinator, this);
        unshareableResources = new HashSet();
        connectionTrackingCoordinator.setUnshareableResources(unshareableResources);
        transactionManager = new TransactionManagerImpl();
    }

    protected void tearDown() throws Exception {
        connectionTrackingCoordinator = null;
        key1 = null;
        key2 = null;
        transactionManager = null;
    }

    public void testSimpleComponentContextLifecyle() throws Exception {
        DefaultComponentContext componentContext = new DefaultComponentContext();
        ConnectorComponentContext oldComponentContext = connectionTrackingCoordinator.enter(componentContext);
        assertNull("Expected old component context to be null", oldComponentContext);
        //give the context a ConnectionInfo
        ManagedConnectionInfo managedConnectionInfo = new ManagedConnectionInfo(null, null);
        ConnectionInfo connectionInfo = new ConnectionInfo(managedConnectionInfo);
        connectionTrackingCoordinator.handleObtained(key1, connectionInfo);
        connectionTrackingCoordinator.exit(oldComponentContext, unshareableResources);
        Map connectionManagerMap = componentContext.getConnectionManagerMap();
        Set infos = (Set) connectionManagerMap.get(key1);
        assertEquals("Expected one connection for key1", 1, infos.size());
        assertTrue("Expected to get supplied ConnectionInfo from infos", connectionInfo == infos.iterator().next());

        //Enter again, and close the handle
        oldComponentContext = connectionTrackingCoordinator.enter(componentContext);
        assertNull("Expected old component context to be null", oldComponentContext);
        connectionTrackingCoordinator.handleReleased(key1, connectionInfo);
        connectionTrackingCoordinator.exit(oldComponentContext, unshareableResources);
        connectionManagerMap = componentContext.getConnectionManagerMap();
        infos = (Set) connectionManagerMap.get(key1);
        assertEquals("Expected no connection set for key1", null, infos);
    }

    public void testNestedComponentContextLifecyle() throws Exception {
        DefaultComponentContext componentContext1 = new DefaultComponentContext();
        ConnectorComponentContext oldComponentContext1 = connectionTrackingCoordinator.enter(componentContext1);
        assertNull("Expected old component context to be null", oldComponentContext1);
        //give the context a ConnectionInfo
        ManagedConnectionInfo managedConnectionInfo1 = new ManagedConnectionInfo(null, null);
        ConnectionInfo connectionInfo1 = new ConnectionInfo(managedConnectionInfo1);
        connectionTrackingCoordinator.handleObtained(key1, connectionInfo1);

        //Simulate calling another component
        DefaultComponentContext componentContext2 = new DefaultComponentContext();
        ConnectorComponentContext oldComponentContext2 = connectionTrackingCoordinator.enter(componentContext2);
        assertTrue("Expected returned component context to be componentContext1", oldComponentContext2 == componentContext1);
        //give the context a ConnectionInfo
        ManagedConnectionInfo managedConnectionInfo2 = new ManagedConnectionInfo(null, null);
        ConnectionInfo connectionInfo2 = new ConnectionInfo(managedConnectionInfo2);
        connectionTrackingCoordinator.handleObtained(key2, connectionInfo2);

        connectionTrackingCoordinator.exit(oldComponentContext2, unshareableResources);
        Map connectionManagerMap2 = componentContext2.getConnectionManagerMap();
        Set infos2 = (Set) connectionManagerMap2.get(key2);
        assertEquals("Expected one connection for key2", 1, infos2.size());
        assertTrue("Expected to get supplied ConnectionInfo from infos", connectionInfo2 == infos2.iterator().next());
        assertEquals("Expected no connection for key1", null, connectionManagerMap2.get(key1));


        connectionTrackingCoordinator.exit(oldComponentContext1, unshareableResources);
        Map connectionManagerMap1 = componentContext1.getConnectionManagerMap();
        Set infos1 = (Set) connectionManagerMap1.get(key1);
        assertEquals("Expected one connection for key1", 1, infos1.size());
        assertTrue("Expected to get supplied ConnectionInfo from infos", connectionInfo1 == infos1.iterator().next());
        assertEquals("Expected no connection for key2", null, connectionManagerMap1.get(key2));

        //Enter again, and close the handle
        oldComponentContext1 = connectionTrackingCoordinator.enter(componentContext1);
        assertNull("Expected old component context to be null", oldComponentContext1);
        connectionTrackingCoordinator.handleReleased(key1, connectionInfo1);
        connectionTrackingCoordinator.exit(oldComponentContext1, unshareableResources);
        connectionManagerMap1 = componentContext1.getConnectionManagerMap();
        infos1 = (Set) connectionManagerMap1.get(key1);
        assertEquals("Expected no connection set for key1", null, infos1);
    }

    public void testSimpleTransactionContextLifecycle() throws Exception {
        DefaultComponentContext componentContext = new DefaultComponentContext();
        ConnectorComponentContext oldComponentContext = connectionTrackingCoordinator.enter(componentContext);
        transactionManager.begin();
        Transaction transaction = transactionManager.getTransaction();
        DefaultTransactionContext transactionContext = new DefaultTransactionContext(transaction);
        ConnectorTransactionContext oldTransactionContext = connectionTrackingCoordinator.setConnectorTransactionContext(transactionContext);
        assertNull("Expected no old transactionContext", oldTransactionContext);
        ConnectorTransactionContext availableTransactionContext = connectionTrackingCoordinator.getConnectorTransactionContext();
        assertTrue("Expected the same transactionContext as we sent in", transactionContext == availableTransactionContext);

        ConnectorTransactionContext exitingTransactionContext = connectionTrackingCoordinator.setConnectorTransactionContext(null);
        assertTrue("Expected the same transactionContext as we sent in", transactionContext == exitingTransactionContext);
        ConnectorTransactionContext availableTransactionContext2 = connectionTrackingCoordinator.getConnectorTransactionContext();
        assertNull("Expected no transactionContext", availableTransactionContext2);
    }

    public Subject getSubject() {
        return subject;
    }
}
