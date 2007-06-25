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

import javax.resource.ResourceException;
import javax.resource.spi.ConnectionManager;
import javax.resource.spi.ConnectionRequestInfo;
import javax.resource.spi.LazyAssociatableConnectionManager;
import javax.resource.spi.ManagedConnectionFactory;
import javax.transaction.SystemException;

import org.apache.geronimo.connector.outbound.connectionmanagerconfig.PoolingSupport;
import org.apache.geronimo.transaction.manager.NamedXAResource;
import org.apache.geronimo.transaction.manager.RecoverableTransactionManager;

/**
 * @version $Rev$ $Date$
 */
public abstract class AbstractConnectionManager implements ConnectionManagerContainer, ConnectionManager, LazyAssociatableConnectionManager, PoolingAttributes {
    protected final Interceptors interceptors;
    private final RecoverableTransactionManager transactionManager;

    //default constructor for use as endpoint
    public AbstractConnectionManager() {
        interceptors = null;
        transactionManager = null;
    }

    public AbstractConnectionManager(Interceptors interceptors, RecoverableTransactionManager transactionManager) {
        this.interceptors = interceptors;
        this.transactionManager = transactionManager;
    }

    public Object createConnectionFactory(ManagedConnectionFactory mcf) throws ResourceException {
        return mcf.createConnectionFactory(this);
    }

    protected ConnectionManager getConnectionManager() {
        return this;
    }
    
    public void doRecovery(ManagedConnectionFactory managedConnectionFactory) {
        try {
            if (!getIsRecoverable()) {
                return;
            }
            ManagedConnectionInfo mci = new ManagedConnectionInfo(managedConnectionFactory, null);

            ConnectionInfo recoveryConnectionInfo = new ConnectionInfo(mci);
            getRecoveryStack().getConnection(recoveryConnectionInfo);

            // For pooled resources, we may now have a new MCI (not the one constructed above). Make sure we use the correct MCI
            NamedXAResource xaResource = (NamedXAResource) recoveryConnectionInfo.getManagedConnectionInfo().getXAResource();
            if (xaResource != null) {
                transactionManager.recoverResourceManager(xaResource);
                getRecoveryStack().returnConnection(recoveryConnectionInfo, ConnectionReturnAction.DESTROY);
            }
        } catch (ResourceException e) {
            transactionManager.recoveryError((SystemException)new SystemException("Could not obtain recovery XAResource for managedConnectionFactory " + managedConnectionFactory).initCause(e));
        }
    }

    /**
     * in: mcf != null, is a deployed mcf
     * out: useable connection object.
     */
    public Object allocateConnection(ManagedConnectionFactory managedConnectionFactory,
                                     ConnectionRequestInfo connectionRequestInfo)
            throws ResourceException {
        ManagedConnectionInfo mci = new ManagedConnectionInfo(managedConnectionFactory, connectionRequestInfo);
        ConnectionInfo ci = new ConnectionInfo(mci);
        getStack().getConnection(ci);
        Object connection = ci.getConnectionProxy();
        if (connection == null) {
            connection = ci.getConnectionHandle();
        }
        return connection;
    }

    /**
     * in: non-null connection object, from non-null mcf.
     * connection object is not associated with a managed connection
     * out: supplied connection object is assiciated with a non-null ManagedConnection from mcf.
     */
    public void associateConnection(Object connection,
                                    ManagedConnectionFactory managedConnectionFactory,
                                    ConnectionRequestInfo connectionRequestInfo)
            throws ResourceException {
        ManagedConnectionInfo mci = new ManagedConnectionInfo(managedConnectionFactory, connectionRequestInfo);
        ConnectionInfo ci = new ConnectionInfo(mci);
        ci.setConnectionHandle(connection);
        getStack().getConnection(ci);
    }

    ConnectionInterceptor getConnectionInterceptor() {
        return getStack();
    }

    //statistics

    public int getPartitionCount() {
        return getPooling().getPartitionCount();
    }

    public int getPartitionMaxSize() {
        return getPooling().getPartitionMaxSize();
    }

    public void setPartitionMaxSize(int maxSize) throws InterruptedException {
        getPooling().setPartitionMaxSize(maxSize);
    }

    public int getPartitionMinSize() {
        return getPooling().getPartitionMinSize();
    }

    public void setPartitionMinSize(int minSize) {
        getPooling().setPartitionMinSize(minSize);
    }

    public int getIdleConnectionCount() {
        return getPooling().getIdleConnectionCount();
    }

    public int getConnectionCount() {
        return getPooling().getConnectionCount();
    }

    public int getBlockingTimeoutMilliseconds() {
        return getPooling().getBlockingTimeoutMilliseconds();
    }

    public void setBlockingTimeoutMilliseconds(int timeoutMilliseconds) {
        getPooling().setBlockingTimeoutMilliseconds(timeoutMilliseconds);
    }

    public int getIdleTimeoutMinutes() {
        return getPooling().getIdleTimeoutMinutes();
    }

    public void setIdleTimeoutMinutes(int idleTimeoutMinutes) {
        getPooling().setIdleTimeoutMinutes(idleTimeoutMinutes);
    }

    private ConnectionInterceptor getStack() {
        return interceptors.getStack();
    }

    private ConnectionInterceptor getRecoveryStack() {
        return interceptors.getRecoveryStack();
    }

    private boolean getIsRecoverable() {
        return interceptors.getRecoveryStack() != null;
    }

    //public for persistence of pooling attributes (max, min size, blocking/idle timeouts)
    public PoolingSupport getPooling() {
        return interceptors.getPoolingAttributes();
    }

    public interface Interceptors {
        ConnectionInterceptor getStack();

        ConnectionInterceptor getRecoveryStack();
        
        PoolingSupport getPoolingAttributes();
    }

    public void doStart() throws Exception {

    }

    public void doStop() throws Exception {
        interceptors.getStack().destroy();
    }

    public void doFail() {
        interceptors.getStack().destroy();
    }
}
