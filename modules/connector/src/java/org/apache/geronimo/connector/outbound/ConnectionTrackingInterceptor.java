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

import java.util.Collection;
import java.util.Iterator;
import java.util.Set;
import javax.resource.ResourceException;
import javax.resource.spi.DissociatableManagedConnection;
import javax.resource.spi.ManagedConnection;
import javax.security.auth.Subject;
import javax.security.auth.login.LoginException;

import org.apache.geronimo.connector.outbound.connectiontracking.ConnectionTracker;
import org.apache.geronimo.security.bridge.RealmBridge;
import org.apache.geronimo.security.ContextManager;

/**
 * ConnectionTrackingInterceptor.java handles communication with the
 * CachedConnectionManager.  On method call entry, cached handles are
 * checked for the correct Subject.  On method call exit, cached
 * handles are disassociated if possible. On getting or releasing
 * a connection the CachedConnectionManager is notified.
 *
 *
 * @version $Revision: 1.2 $ $Date: 2004/01/23 06:47:05 $
 */
public class ConnectionTrackingInterceptor implements ConnectionInterceptor {

    private final ConnectionInterceptor next;
    private final String key;
    private final ConnectionTracker connectionTracker;
    private final RealmBridge realmBridge;

    public ConnectionTrackingInterceptor(
            final ConnectionInterceptor next,
            final String key,
            final ConnectionTracker connectionTracker,
            final RealmBridge realmBridge) {
        this.next = next;
        this.key = key;
        this.connectionTracker = connectionTracker;
        this.realmBridge = realmBridge;
    }

    /**
     * called by: ProxyConnectionManager.allocateConnection, ProxyConnectionManager.associateConnection, and enter.
     * in: connectionInfo is non-null, and has non-null ManagedConnectionInfo with non-null managedConnectionfactory.
     * connection handle may or may not be null.
     * out: connectionInfo has non-null connection handle, non null ManagedConnectionInfo with non-null ManagedConnection and GeronimoConnectionEventListener.
     * connection tracker has been notified of handle-managed connection association.
     * @param connectionInfo
     * @throws ResourceException
     */
    public void getConnection(ConnectionInfo connectionInfo) throws ResourceException {
        ManagedConnectionInfo managedConnectionInfo = connectionInfo.getManagedConnectionInfo();
        managedConnectionInfo.setTransactionContext(connectionTracker.getConnectorTransactionContext());
        next.getConnection(connectionInfo);
        connectionTracker.handleObtained(this, connectionInfo);
    }

    /**
     * called by: GeronimoConnectionEventListener.connectionClosed, GeronimoConnectionEventListener.connectionErrorOccurred, exit
     * in: handle has already been dissociated from ManagedConnection. connectionInfo not null, has non-null ManagedConnectionInfo, ManagedConnectionInfo has non-null ManagedConnection
     * handle can be null if called from error in ManagedConnection in pool.
     * out: connectionTracker has been notified, ManagedConnectionInfo null.
     * @param connectionInfo
     * @param connectionReturnAction
     */
    public void returnConnection(
            ConnectionInfo connectionInfo,
            ConnectionReturnAction connectionReturnAction) {
        connectionTracker.handleReleased(this, connectionInfo);
        next.returnConnection(connectionInfo, connectionReturnAction);
    }

    public void enter(Collection connectionInfos, Set unshareable)
            throws ResourceException {
        if (unshareable.contains(key)) {
            //should probably check to see if subjects are consistent,
            //and if not raise an exception.  Also need to check if
            //the spec says anything about this.
            //this is wrong
        }
        if (realmBridge == null) {
            return;    //this is wrong: need a "bouncing" subjectInterceptor
        }

        Subject currentSubject = null;
        try {
            currentSubject = realmBridge.mapSubject(ContextManager.getCurrentCaller());
        } catch (SecurityException e) {
            throw new ResourceException("Can not obtain Subject for login", e);
        } catch (LoginException e) {
            throw new ResourceException("Can not obtain Subject for login", e);
        }
        //TODO figure out which is right here
        assert currentSubject != null;
        if (currentSubject == null) {
            //check to see if mci.getSubject() is null?
            return;
        }
        for (Iterator i = connectionInfos.iterator(); i.hasNext();) {
            ConnectionInfo connectionInfo = (ConnectionInfo) i.next();
            getConnection(connectionInfo);
        }

    }

    public void exit(Collection connectionInfos, Set unshareableResources)
            throws ResourceException {
        if (unshareableResources.contains(key)) {
            return;
        }
        for (Iterator i = connectionInfos.iterator(); i.hasNext();) {
            ConnectionInfo connectionInfo = (ConnectionInfo) i.next();
            ManagedConnectionInfo managedConnectionInfo = connectionInfo.getManagedConnectionInfo();
            ManagedConnection managedConnection = managedConnectionInfo.getManagedConnection();
            if (managedConnection instanceof DissociatableManagedConnection
                    && managedConnectionInfo.isFirstConnectionInfo(connectionInfo)) {
                i.remove();
                ((DissociatableManagedConnection) managedConnection).dissociateConnections();
                managedConnectionInfo.clearConnectionHandles();
                returnConnection(connectionInfo, ConnectionReturnAction.RETURN_HANDLE);
            }
        }
    }
}
