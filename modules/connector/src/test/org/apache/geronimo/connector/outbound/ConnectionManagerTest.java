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

import java.util.HashSet;
import java.util.Set;
import javax.security.auth.Subject;
import javax.transaction.TransactionManager;

import org.apache.geronimo.connector.mock.MockConnection;
import org.apache.geronimo.connector.mock.MockConnectionFactory;
import org.apache.geronimo.connector.mock.MockManagedConnection;
import org.apache.geronimo.connector.mock.MockManagedConnectionFactory;
import org.apache.geronimo.connector.mock.MockXAResource;
import org.apache.geronimo.connector.outbound.connectiontracking.ConnectionTrackingCoordinator;
import org.apache.geronimo.connector.outbound.connectiontracking.defaultimpl.DefaultComponentContext;
import org.apache.geronimo.connector.outbound.connectiontracking.defaultimpl.DefaultComponentInterceptor;
import org.apache.geronimo.connector.outbound.connectiontracking.defaultimpl.DefaultInterceptor;
import org.apache.geronimo.kernel.Kernel;
import org.apache.geronimo.security.bridge.RealmBridge;
import org.apache.geronimo.transaction.manager.TransactionManagerImpl;

import junit.framework.TestCase;

/**
 *
 *
 * @version $Revision: 1.1 $ $Date: 2004/01/23 05:56:11 $
 *
 * */
public class ConnectionManagerTest extends TestCase implements DefaultInterceptor, RealmBridge {

    protected boolean useConnectionRequestInfo = false;
    protected boolean useSubject = true;
    protected boolean useTransactionCaching = true;
    protected boolean useLocalTransactions = false;
    protected boolean useTransactions = true;
    protected int maxSize = 5;
    protected int blockingTimeout = 100;
    protected String name = "testCF";
    //dependencies
    protected RealmBridge realmBridge = this;
    protected ConnectionTrackingCoordinator connectionTrackingCoordinator;
    protected Kernel kernel;

    protected TransactionManager transactionManager;
    protected ConnectionManagerDeployment connectionManagerDeployment;
    protected MockConnectionFactory connectionFactory;
    protected MockManagedConnectionFactory mockManagedConnectionFactory;
    protected DefaultComponentContext defaultComponentContext;
    protected DefaultComponentInterceptor defaultComponentInterceptor;
    protected Set unshareableResources = new HashSet();
    protected MockManagedConnection mockManagedConnection;
    protected Subject subject;

    protected void setUp() throws Exception {
        connectionTrackingCoordinator = new ConnectionTrackingCoordinator();
        kernel = new Kernel("testdomain");
        kernel.boot();
        transactionManager = new TransactionManagerImpl();
        mockManagedConnectionFactory = new MockManagedConnectionFactory();
        subject = new Subject();
        connectionManagerDeployment = new ConnectionManagerDeployment(useConnectionRequestInfo,
                useSubject,
                useTransactionCaching,
                useLocalTransactions,
                useTransactions,
                maxSize,
                blockingTimeout,
                name,
                realmBridge,
                connectionTrackingCoordinator,
                kernel);
        connectionManagerDeployment.doStart();
        connectionFactory = (MockConnectionFactory) connectionManagerDeployment.createConnectionFactory(mockManagedConnectionFactory);
        defaultComponentContext = new DefaultComponentContext();
        defaultComponentInterceptor = new DefaultComponentInterceptor(this, connectionTrackingCoordinator, unshareableResources, transactionManager);
    }

    protected void tearDown() throws Exception {
        connectionTrackingCoordinator = null;
        kernel.shutdown();
        kernel = null;
        transactionManager = null;
        mockManagedConnectionFactory = null;
        connectionManagerDeployment = null;
        connectionFactory = null;
        defaultComponentContext = null;
    }


    public void testSingleTransactionCall() throws Throwable {
        transactionManager.begin();
        defaultComponentInterceptor.invoke(defaultComponentContext);
        MockXAResource mockXAResource = (MockXAResource) mockManagedConnection.getXAResource();
        assertTrue("XAResource should know one xid", mockXAResource.getKnownXids().size() == 1);
        assertTrue("Should not be committed", mockXAResource.getCommitted() == null);
        transactionManager.commit();
        assertTrue("Should be committed", mockXAResource.getCommitted() != null);
    }

    public void testNoTransactionCall() throws Throwable {
        defaultComponentInterceptor.invoke(defaultComponentContext);
        MockXAResource mockXAResource = (MockXAResource) mockManagedConnection.getXAResource();
        assertTrue("XAResource should know 0 xid", mockXAResource.getKnownXids().size() == 0);
        assertTrue("Should not be committed", mockXAResource.getCommitted() == null);
    }

    public void testOneTransactionTwoCalls() throws Throwable {
        transactionManager.begin();
        defaultComponentInterceptor.invoke(defaultComponentContext);
        MockXAResource mockXAResource = (MockXAResource) mockManagedConnection.getXAResource();
        assertTrue("XAResource should know one xid", mockXAResource.getKnownXids().size() == 1);
        assertTrue("Should not be committed", mockXAResource.getCommitted() == null);
        defaultComponentInterceptor.invoke(defaultComponentContext);
        assertTrue("Expected same XAResource", mockXAResource == mockManagedConnection.getXAResource());
        assertTrue("XAResource should know one xid", mockXAResource.getKnownXids().size() == 1);
        assertTrue("Should not be committed", mockXAResource.getCommitted() == null);
        transactionManager.commit();
        assertTrue("Should be committed", mockXAResource.getCommitted() != null);
    }

    public Object invoke(ConnectorComponentContext newConnectorComponentContext) throws Throwable {
        MockConnection mockConnection = (MockConnection) connectionFactory.getConnection();
        mockManagedConnection = mockConnection.getManagedConnection();
        mockConnection.close();
        return null;
    }

    public Subject mapSubject(Subject sourceSubject) {
        return subject;
    }
}
