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

import java.util.ArrayList;
import java.util.List;

import javax.resource.spi.ConnectionEvent;
import javax.resource.spi.ConnectionEventListener;

import org.apache.commons.logging.LogFactory;
import org.apache.commons.logging.Log;

/**
 * ConnectionEventListener.java
 *
 *
 * Created: Thu Oct  2 14:57:43 2003
 *
 * @version 1.0
 */
public class GeronimoConnectionEventListener
        implements ConnectionEventListener {

    private static Log log = LogFactory.getLog(GeronimoConnectionEventListener.class.getName());

    private final ManagedConnectionInfo managedConnectionInfo;
    private final ConnectionInterceptor stack;
    private final List connectionInfos = new ArrayList();

    public GeronimoConnectionEventListener(
            final ConnectionInterceptor stack,
            final ManagedConnectionInfo managedConnectionInfo) {
        this.stack = stack;
        this.managedConnectionInfo = managedConnectionInfo;
    }

    public void connectionClosed(ConnectionEvent connectionEvent) {
        if (connectionEvent.getSource() != managedConnectionInfo.getManagedConnection()) {
            throw new IllegalArgumentException(
                    "ConnectionClosed event received from wrong ManagedConnection. Expected "
                    + managedConnectionInfo.getManagedConnection()
                    + ", actual "
                    + connectionEvent.getSource());
        }
        if (log.isTraceEnabled()) {
            log.trace("connectionClosed called with " + connectionEvent.getConnectionHandle());
        }
        ConnectionInfo ci = new ConnectionInfo(managedConnectionInfo);
        ci.setConnectionHandle(connectionEvent.getConnectionHandle());
        stack.returnConnection(ci, ConnectionReturnAction.RETURN_HANDLE);
    }

    public void connectionErrorOccurred(ConnectionEvent connectionEvent) {
        if (connectionEvent.getSource() != managedConnectionInfo.getManagedConnection()) {
            throw new IllegalArgumentException(
                    "ConnectionError event received from wrong ManagedConnection. Expected "
                    + managedConnectionInfo.getManagedConnection()
                    + ", actual "
                    + connectionEvent.getSource());
        }
        log.info("connectionErrorOccurred called with " + connectionEvent.getConnectionHandle(),connectionEvent.getException());
        ConnectionInfo ci = new ConnectionInfo(managedConnectionInfo);
        ci.setConnectionHandle(connectionEvent.getConnectionHandle());
        stack.returnConnection(ci, ConnectionReturnAction.DESTROY);
    }

    public void localTransactionStarted(ConnectionEvent event) {
        //TODO implement this method
    }

    /**
     * The <code>localTransactionCommitted</code> method
     *
     * @param event a <code>ConnectionEvent</code> value
     * @todo implement this method
     */
    public void localTransactionCommitted(ConnectionEvent event) {
    }

    /**
     * The <code>localTransactionRolledback</code> method
     *
     * @param event a <code>ConnectionEvent</code> value
     * @todo implement this method
     */
    public void localTransactionRolledback(ConnectionEvent event) {
    }

    public void addConnectionInfo(ConnectionInfo connectionInfo) {
        assert connectionInfo.getConnectionHandle() != null;
        connectionInfos.add(connectionInfo);
    }

    public void removeConnectionInfo(ConnectionInfo connectionInfo) {
        assert connectionInfo.getConnectionHandle() != null;
        connectionInfos.remove(connectionInfo);
    }

    public boolean hasConnectionInfos() {
        return !connectionInfos.isEmpty();
    }

    public void clearConnectionInfos() {
        connectionInfos.clear();
    }

    public boolean hasConnectionInfo(ConnectionInfo connectionInfo) {
        return connectionInfos.contains(connectionInfo);
    }

    public boolean isFirstConnectionInfo(ConnectionInfo connectionInfo) {
        return !connectionInfos.isEmpty() && connectionInfos.get(0) == connectionInfo;
    }

}
