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

import java.io.PrintWriter;

import javax.resource.ResourceException;
import javax.resource.spi.LocalTransaction;
import javax.resource.spi.ManagedConnection;
import javax.resource.spi.ConnectionRequestInfo;
import javax.resource.spi.ConnectionEventListener;
import javax.resource.spi.ManagedConnectionMetaData;
import javax.security.auth.Subject;
import javax.transaction.xa.XAResource;

import junit.framework.TestCase;

/**
 *
 *
 * @version $Revision: 1.1 $ $Date: 2003/12/09 04:17:39 $
 *
 * */
public class LocalXAResourceInsertionInterceptorTest extends TestCase
    implements ConnectionInterceptor {

    private LocalXAResourceInsertionInterceptor localXAResourceInsertionInterceptor;
    private LocalTransaction localTransaction;

    protected void setUp() throws Exception {
        localXAResourceInsertionInterceptor = new LocalXAResourceInsertionInterceptor(this);
    }

    protected void tearDown() throws Exception {
        localXAResourceInsertionInterceptor = null;
    }

    public void testInsertLocalXAResource() throws Exception {
        ManagedConnectionInfo managedConnectionInfo = new ManagedConnectionInfo(null, null);
        ConnectionInfo connectionInfo = new ConnectionInfo(managedConnectionInfo);
        localXAResourceInsertionInterceptor.getConnection(connectionInfo);
        LocalXAResource returnedLocalXAResource = (LocalXAResource)managedConnectionInfo.getXAResource();
        assertTrue("Expected the same LocalTransaction", localTransaction == returnedLocalXAResource.localTransaction);
    }

    public void getConnection(ConnectionInfo connectionInfo) throws ResourceException {
        localTransaction = new TestLocalTransaction();
        TestManagedConnection managedConnection = new TestManagedConnection(localTransaction);
        ManagedConnectionInfo managedConnectionInfo = connectionInfo.getManagedConnectionInfo();
        managedConnectionInfo.setManagedConnection(managedConnection);
    }

    public void returnConnection(ConnectionInfo connectionInfo, ConnectionReturnAction connectionReturnAction) {
    }

    private static class TestLocalTransaction implements LocalTransaction {
        public void begin() throws ResourceException {
        }

        public void commit() throws ResourceException {
        }

        public void rollback() throws ResourceException {
        }

    }

    private static class TestManagedConnection implements ManagedConnection {

        private final LocalTransaction localTransaction;

        public TestManagedConnection(LocalTransaction localTransaction) {
            this.localTransaction = localTransaction;
        }

        public Object getConnection(Subject subject, ConnectionRequestInfo cxRequestInfo) throws ResourceException {
            return null;
        }

        public void destroy() throws ResourceException {
        }

        public void cleanup() throws ResourceException {
        }

        public void associateConnection(Object connection) throws ResourceException {
        }

        public void addConnectionEventListener(ConnectionEventListener listener) {
        }

        public void removeConnectionEventListener(ConnectionEventListener listener) {
        }

        public XAResource getXAResource() throws ResourceException {
            return null;
        }

        public LocalTransaction getLocalTransaction() throws ResourceException {
            return localTransaction;
        }

        public ManagedConnectionMetaData getMetaData() throws ResourceException {
            return null;
        }

        public void setLogWriter(PrintWriter out) throws ResourceException {
        }

        public PrintWriter getLogWriter() throws ResourceException {
            return null;
        }

    }
}
