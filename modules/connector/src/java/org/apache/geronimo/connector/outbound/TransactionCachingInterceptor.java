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

import javax.resource.ResourceException;

import org.apache.geronimo.connector.outbound.connectiontracking.ConnectionTracker;
import org.apache.geronimo.transaction.ConnectionReleaser;
import org.apache.geronimo.transaction.TransactionContext;

/**
 * TransactionCachingInterceptor.java
 * TODO: This implementation does not take account of unshareable resources
 * TODO: This implementation does not take account of application security
 *  where several connections with different security info are obtained.
 * TODO: This implementation does not take account of container managed security where,
 *  within one transaction, a security domain boundary is crossed
 * and connections are obtained with two (or more) different subjects.
 *
 * I suggest a state pattern, with the state set in a threadlocal upon entering a component,
 * will be a usable implementation.
 *
 * The afterCompletion method will need to move to an interface,  and that interface include the
 * security info to distinguish connections.
 *
 *
 * Created: Mon Sep 29 15:07:07 2003
 *
 * @version 1.0
 */
public class TransactionCachingInterceptor implements ConnectionInterceptor, ConnectionReleaser {

    private final ConnectionInterceptor next;
    private final ConnectionTracker connectionTracker;

    public TransactionCachingInterceptor(final ConnectionInterceptor next, final ConnectionTracker connectionTracker) {
        this.next = next;
        this.connectionTracker = connectionTracker;
    }

    public void getConnection(ConnectionInfo connectionInfo) throws ResourceException {
        TransactionContext transactionContext = connectionTracker.getTransactionContext();
        ManagedConnectionInfo managedConnectionInfo = (ManagedConnectionInfo)transactionContext.getManagedConnectionInfo(this);
        if (managedConnectionInfo != null) {
            connectionInfo.setManagedConnectionInfo(managedConnectionInfo);
            return;
        } else {
            next.getConnection(connectionInfo);
            transactionContext.setManagedConnectionInfo(this, connectionInfo.getManagedConnectionInfo());
        }
    }

    public void returnConnection(ConnectionInfo connectionInfo, ConnectionReturnAction connectionReturnAction) {

        if (connectionReturnAction == ConnectionReturnAction.DESTROY) {
            next.returnConnection(connectionInfo, connectionReturnAction);
        }

        TransactionContext transactionContext = connectionTracker.getTransactionContext();
        if (transactionContext.isActive()) {
            return;
        }
        if (connectionInfo.getManagedConnectionInfo().hasConnectionHandles()) {
            return;
        }
        //No transaction, no handles, we return it.
        next.returnConnection(connectionInfo, connectionReturnAction);
    }

    public void afterCompletion(Object managedConnectionInfo) {
        ConnectionInfo connectionInfo = new ConnectionInfo();
        connectionInfo.setManagedConnectionInfo((ManagedConnectionInfo)managedConnectionInfo);
        returnConnection(connectionInfo, ConnectionReturnAction.RETURN_HANDLE);
    }

}
