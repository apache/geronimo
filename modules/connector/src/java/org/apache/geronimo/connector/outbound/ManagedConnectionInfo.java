/**
 *
 * Copyright 2003-2004 The Apache Software Foundation
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

package org.apache.geronimo.connector.outbound;

import java.util.Collection;

import javax.resource.spi.ConnectionRequestInfo;
import javax.resource.spi.ManagedConnection;
import javax.resource.spi.ManagedConnectionFactory;
import javax.security.auth.Subject;
import javax.transaction.xa.XAResource;

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

    public ManagedConnection getManagedConnection() {
        return managedConnection;
    }

    public void setManagedConnection(ManagedConnection managedConnection) {
        assert this.managedConnection == null;
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

    public Collection getConnectionInfos() {
        return listener.getConnectionInfos();
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
