/**
 *
 * Copyright 2004 The Apache Software Foundation
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
 * @version $Revision: 1.4 $ $Date: 2004/02/25 09:57:10 $
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
        managedConnectionInfo.setTransactionContext(connectionTracker.getTransactionContext());
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
