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

import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import javax.resource.ResourceException;

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

    public TransactionCachingInterceptor(final ConnectionInterceptor next) {
        this.next = next;
    }

    public void getConnection(ConnectionInfo connectionInfo) throws ResourceException {
        TransactionContext transactionContext = TransactionContext.getContext();
        ManagedConnectionInfos managedConnectionInfos = (ManagedConnectionInfos) transactionContext.getManagedConnectionInfo(this);
        if (managedConnectionInfos == null) {
            managedConnectionInfos = new ManagedConnectionInfos();
            transactionContext.setManagedConnectionInfo(this, managedConnectionInfos);
        }
        if (connectionInfo.isUnshareable()) {
            if (!managedConnectionInfos.containsUnshared(connectionInfo.getManagedConnectionInfo())) {
                next.getConnection(connectionInfo);
                managedConnectionInfos.addUnshared(connectionInfo.getManagedConnectionInfo());
            }
        } else {
            ManagedConnectionInfo managedConnectionInfo = managedConnectionInfos.getShared();
            if (managedConnectionInfo != null) {
                connectionInfo.setManagedConnectionInfo(managedConnectionInfo);
                return;
            } else {
                next.getConnection(connectionInfo);
                managedConnectionInfos.setShared(connectionInfo.getManagedConnectionInfo());
            }
        }
    }

    public void returnConnection(ConnectionInfo connectionInfo, ConnectionReturnAction connectionReturnAction) {

        if (connectionReturnAction == ConnectionReturnAction.DESTROY) {
            next.returnConnection(connectionInfo, connectionReturnAction);
        }

        TransactionContext transactionContext = TransactionContext.getContext();
        if (transactionContext.isActive()) {
            return;
        }
        if (connectionInfo.getManagedConnectionInfo().hasConnectionHandles()) {
            return;
        }
        //No transaction, no handles, we return it.
        next.returnConnection(connectionInfo, connectionReturnAction);
    }

    public void afterCompletion(Object stuff) {
        ManagedConnectionInfos managedConnectionInfos = (ManagedConnectionInfos)stuff;
        ManagedConnectionInfo sharedMCI = managedConnectionInfos.getShared();
        if (sharedMCI != null) {
            returnHandle(sharedMCI);
        }
        for (Iterator iterator = managedConnectionInfos.getUnshared().iterator(); iterator.hasNext();) {
            ManagedConnectionInfo managedConnectionInfo = (ManagedConnectionInfo) iterator.next();
            returnHandle(managedConnectionInfo);
        }
    }

    private void returnHandle(ManagedConnectionInfo managedConnectionInfo) {
        ConnectionInfo connectionInfo = new ConnectionInfo();
        connectionInfo.setManagedConnectionInfo(managedConnectionInfo);
        returnConnection(connectionInfo, ConnectionReturnAction.RETURN_HANDLE);
    }

    static class ManagedConnectionInfos {
        private ManagedConnectionInfo shared;
        private Set unshared = Collections.EMPTY_SET;

        public ManagedConnectionInfo getShared() {
            return shared;
        }

        public void setShared(ManagedConnectionInfo shared) {
            this.shared = shared;
        }

        public Set getUnshared() {
            return unshared;
        }

        public void addUnshared(ManagedConnectionInfo unsharedMCI) {
            if (this.unshared == Collections.EMPTY_SET) {
                this.unshared = new HashSet();
            }
            this.unshared.add(unsharedMCI);
        }

        public boolean containsUnshared(ManagedConnectionInfo unsharedMCI) {
            return this.unshared.contains(unsharedMCI);
        }
    }

}
