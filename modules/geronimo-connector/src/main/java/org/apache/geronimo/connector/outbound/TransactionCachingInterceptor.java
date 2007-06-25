/**
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
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
import javax.transaction.SystemException;
import javax.transaction.Transaction;
import javax.transaction.TransactionManager;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.geronimo.connector.ConnectionReleaser;
import org.apache.geronimo.connector.ConnectorTransactionContext;

/**
 * TransactionCachingInterceptor.java
 * TODO: This implementation does not take account of unshareable resources
 * TODO: This implementation does not take account of application security
 * where several connections with different security info are obtained.
 * TODO: This implementation does not take account of container managed security where,
 * within one transaction, a security domain boundary is crossed
 * and connections are obtained with two (or more) different subjects.
 * <p/>
 * I suggest a state pattern, with the state set in a threadlocal upon entering a component,
 * will be a usable implementation.
 * <p/>
 * The afterCompletion method will need to move to an interface,  and that interface include the
 * security info to distinguish connections.
 * <p/>
 * <p/>
 * Created: Mon Sep 29 15:07:07 2003
 *
 * @version 1.0
 */
public class TransactionCachingInterceptor implements ConnectionInterceptor, ConnectionReleaser {
    protected static Log log = LogFactory.getLog(TransactionCachingInterceptor.class.getName());

    private final ConnectionInterceptor next;
    private final TransactionManager transactionManager;

    public TransactionCachingInterceptor(ConnectionInterceptor next, TransactionManager transactionManager) {
        this.next = next;
        this.transactionManager = transactionManager;
    }

    public void getConnection(ConnectionInfo connectionInfo) throws ResourceException {
        //There can be an inactive transaction context when a connection is requested in
        //Synchronization.afterCompletion().

        // get the current transation and status... if there is a problem just assume there is no transaction present
        Transaction transaction = TxUtil.getTransactionIfActive(transactionManager);
        if (transaction != null) {
            ManagedConnectionInfos managedConnectionInfos = ConnectorTransactionContext.get(transaction, this);
            if (connectionInfo.isUnshareable()) {
                if (!managedConnectionInfos.containsUnshared(connectionInfo.getManagedConnectionInfo())) {
                    next.getConnection(connectionInfo);
                    managedConnectionInfos.addUnshared(connectionInfo.getManagedConnectionInfo());
                }
            } else {
                ManagedConnectionInfo managedConnectionInfo = managedConnectionInfos.getShared();
                if (managedConnectionInfo != null) {
                    connectionInfo.setManagedConnectionInfo(managedConnectionInfo);
                    //return;
                    if (log.isTraceEnabled()) {
                        log.trace("supplying connection from tx cache " + connectionInfo.getConnectionHandle() + " for managed connection " + connectionInfo.getManagedConnectionInfo().getManagedConnection() + " to tx caching interceptor " + this);
                    }
                } else {
                    next.getConnection(connectionInfo);
                    managedConnectionInfos.setShared(connectionInfo.getManagedConnectionInfo());
                    if (log.isTraceEnabled()) {
                        log.trace("supplying connection from pool " + connectionInfo.getConnectionHandle() + " for managed connection " + connectionInfo.getManagedConnectionInfo().getManagedConnection() + " to tx caching interceptor " + this);
                    }
                }
            }
        } else {
            next.getConnection(connectionInfo);
        }
    }

    public void returnConnection(ConnectionInfo connectionInfo, ConnectionReturnAction connectionReturnAction) {

        if (connectionReturnAction == ConnectionReturnAction.DESTROY) {
            if (log.isTraceEnabled()) {
                log.trace("destroying connection" + connectionInfo.getConnectionHandle() + " for managed connection " + connectionInfo.getManagedConnectionInfo().getManagedConnection() + " to tx caching interceptor " + this);
            }
            next.returnConnection(connectionInfo, connectionReturnAction);
            return;
        }
        Transaction transaction;
        try {
            transaction = transactionManager.getTransaction();
            if (transaction != null) {
                if (TxUtil.isActive(transaction)) {
                    if (log.isTraceEnabled()) {
                        log.trace("tx active, not returning connection" + connectionInfo.getConnectionHandle() + " for managed connection " + connectionInfo.getManagedConnectionInfo().getManagedConnection() + " to tx caching interceptor " + this);
                    }
                    return;
                }
                //We are called from an afterCompletion synchronization.  Remove the MCI from the ManagedConnectionInfos
                //so we don't close it twice
                ManagedConnectionInfos managedConnectionInfos = ConnectorTransactionContext.get(transaction, this);
                managedConnectionInfos.remove(connectionInfo.getManagedConnectionInfo());
                if (log.isTraceEnabled()) {
                    log.trace("tx ended, but not removed");
                }
            }
        } catch (SystemException e) {
            //ignore
        }
        if (log.isTraceEnabled()) {
            log.trace("tx ended, returning connection" + connectionInfo.getConnectionHandle() + " for managed connection " + connectionInfo.getManagedConnectionInfo().getManagedConnection() + " to tx caching interceptor " + this);
        }
        internalReturn(connectionInfo, connectionReturnAction);
    }

    private void internalReturn(ConnectionInfo connectionInfo, ConnectionReturnAction connectionReturnAction) {
        if (connectionInfo.getManagedConnectionInfo().hasConnectionHandles()) {
            if (log.isTraceEnabled()) {
                log.trace("not returning connection from tx cache (has handles) " + connectionInfo.getConnectionHandle() + " for managed connection " + connectionInfo.getManagedConnectionInfo().getManagedConnection() + " to tx caching interceptor " + this);
            }
            return;
        }
        //No transaction, no handles, we return it.
        next.returnConnection(connectionInfo, connectionReturnAction);
        if (log.isTraceEnabled()) {
            log.trace("completed return of connection through tx cache " + connectionInfo.getConnectionHandle() + " for MCI: " + connectionInfo.getManagedConnectionInfo() + " and MC " + connectionInfo.getManagedConnectionInfo().getManagedConnection() + " to tx caching interceptor " + this);
        }
    }

    public void destroy() {
        next.destroy();
    }

    public void afterCompletion(Object stuff) {
        ManagedConnectionInfos managedConnectionInfos = (ManagedConnectionInfos) stuff;
        ManagedConnectionInfo sharedMCI = managedConnectionInfos.getShared();
        if (sharedMCI != null) {
            if (log.isTraceEnabled()) {
                log.trace("Transaction completed, attempting to return shared connection MCI: " + sharedMCI + " for managed connection " + sharedMCI.getManagedConnection() + " to tx caching interceptor " + this);
            }
            returnHandle(sharedMCI);
        }
        for (Iterator iterator = managedConnectionInfos.getUnshared().iterator(); iterator.hasNext();) {
            ManagedConnectionInfo managedConnectionInfo = (ManagedConnectionInfo) iterator.next();
            if (log.isTraceEnabled()) {
                log.trace("Transaction completed, attempting to return unshared connection MCI: " + managedConnectionInfo + " for managed connection " + managedConnectionInfo.getManagedConnection() + " to tx caching interceptor " + this);
            }
            returnHandle(managedConnectionInfo);
        }
    }

    private void returnHandle(ManagedConnectionInfo managedConnectionInfo) {
        ConnectionInfo connectionInfo = new ConnectionInfo();
        connectionInfo.setManagedConnectionInfo(managedConnectionInfo);
        internalReturn(connectionInfo, ConnectionReturnAction.RETURN_HANDLE);
    }

    public static class ManagedConnectionInfos {
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

        public void remove(ManagedConnectionInfo managedConnectionInfo) {
            if (shared == managedConnectionInfo) {
                shared = null;
            } else {
                unshared.remove(managedConnectionInfo);
            }
        }
    }

}
