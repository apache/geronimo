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

import javax.resource.spi.ConnectionRequestInfo;
import javax.resource.spi.ManagedConnection;
import javax.resource.spi.ManagedConnectionFactory;
import javax.security.auth.Subject;
import javax.transaction.xa.XAResource;

import org.apache.geronimo.transaction.TransactionContext;

/**
 * ConnectionRequest.java
 *
 *
 * Created: Thu Sep 25 14:29:07 2003
 *
 * @version 1.0
 */
public class ManagedConnectionInfo {

    private ManagedConnectionFactory managedConnectionFactory;
    private ConnectionRequestInfo connectionRequestInfo;
    private Subject subject;
    private TransactionContext transactionContext;
    private ManagedConnection managedConnection;
    private XAResource xares;
    private long lastUsed;
    private ConnectionInterceptor poolInterceptor;

    private GeronimoConnectionEventListener listener;

    public ManagedConnectionInfo(
            ManagedConnectionFactory managedConnectionFactory,
            ConnectionRequestInfo connectionRequestInfo) {
        this.managedConnectionFactory = managedConnectionFactory;
        this.connectionRequestInfo = connectionRequestInfo;
    }

    public ManagedConnectionFactory getManagedConnectionFactory() {
        return managedConnectionFactory;
    }

    public void setManagedConnectionFactory(ManagedConnectionFactory managedConnectionFactory) {
        this.managedConnectionFactory = managedConnectionFactory;
    }

    public ConnectionRequestInfo getConnectionRequestInfo() {
        return connectionRequestInfo;
    }

    public void setConnectionRequestInfo(ConnectionRequestInfo cri) {
        this.connectionRequestInfo = cri;
    }

    public Subject getSubject() {
        return subject;
    }

    public void setSubject(Subject subject) {
        this.subject = subject;
    }

    public TransactionContext getTransactionContext() {
        return transactionContext;
    }

    public void setTransactionContext(TransactionContext transactionContext) {
        this.transactionContext = transactionContext;
    }

    public ManagedConnection getManagedConnection() {
        return managedConnection;
    }

    public void setManagedConnection(ManagedConnection managedConnection) {
        this.managedConnection = managedConnection;
    }

    public XAResource getXAResource() {
        return xares;
    }

    public void setXAResource(XAResource xares) {
        this.xares = xares;
    }

    public long getLastUsed() {
        return lastUsed;
    }

    public void setLastUsed(long lastUsed) {
        this.lastUsed = lastUsed;
    }

    public void setPoolInterceptor(ConnectionInterceptor poolInterceptor) {
        this.poolInterceptor = poolInterceptor;
    }

    public ConnectionInterceptor getPoolInterceptor() {
        return poolInterceptor;
    }

    public void setConnectionEventListener(GeronimoConnectionEventListener listener) {
        this.listener = listener;
    }

    public void addConnectionHandle(ConnectionInfo connectionInfo) {
        listener.addConnectionInfo(connectionInfo);
    }

    public void removeConnectionHandle(ConnectionInfo connectionInfo) {
        listener.removeConnectionInfo(connectionInfo);
    }

    public boolean hasConnectionHandles() {
        return listener.hasConnectionInfos();
    }

    public void clearConnectionHandles() {
        listener.clearConnectionInfos();
    }

    public boolean securityMatches(ManagedConnectionInfo other) {
        return (
                subject == null
                ? other.getSubject() == null
                : subject.equals(other.getSubject()))
                && (connectionRequestInfo == null
                ? other.getConnectionRequestInfo() == null
                : connectionRequestInfo.equals(other.getConnectionRequestInfo()));
    }

    public boolean hasConnectionInfo(ConnectionInfo connectionInfo) {
        return listener.hasConnectionInfo(connectionInfo);
    }

    public boolean isFirstConnectionInfo(ConnectionInfo connectionInfo) {
        return listener.isFirstConnectionInfo(connectionInfo);
    }

}
