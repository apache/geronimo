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
import javax.resource.spi.ConnectionManager;
import javax.resource.spi.ConnectionRequestInfo;
import javax.resource.spi.LazyAssociatableConnectionManager;
import javax.resource.spi.ManagedConnectionFactory;

import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.gbean.GBeanInfoBuilder;
import org.apache.geronimo.transaction.manager.NamedXAResource;
import org.apache.geronimo.connector.outbound.connectionmanagerconfig.PoolingSupport;
import org.apache.geronimo.j2ee.j2eeobjectnames.NameFactory;

/**
 * @version $Rev$ $Date$
 */
public abstract class AbstractConnectionManager implements ConnectionManagerFactory, ConnectionManager, LazyAssociatableConnectionManager, PoolingAttributes {
    protected final Interceptors interceptors;

    //default constructor for use as endpoint
    public AbstractConnectionManager() {
        interceptors = null;
    }

    public AbstractConnectionManager(Interceptors interceptors) {
        this.interceptors = interceptors;
    }

    public Object createConnectionFactory(ManagedConnectionFactory mcf) throws ResourceException {
        return mcf.createConnectionFactory(this);
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
        return ci.getConnectionHandle();
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


    public ConnectionManagerFactory.ReturnableXAResource getRecoveryXAResource(ManagedConnectionFactory managedConnectionFactory) throws ResourceException {
        ManagedConnectionInfo mci = new ManagedConnectionInfo(managedConnectionFactory, null);
        NamedXAResource namedXAResource = (NamedXAResource) mci.getXAResource();
        if (namedXAResource == null) {
            //obviously, we can't do recovery.
            return null;
        }
        ConnectionInfo recoveryConnectionInfo = new ConnectionInfo(mci);
        getRecoveryStack().getConnection(recoveryConnectionInfo);
        return new ConnectionManagerFactory.ReturnableXAResource(namedXAResource, getRecoveryStack(), recoveryConnectionInfo);
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

    //public for persistence of pooling attributes (max, min size, blocking/idle timeouts)
    public PoolingSupport getPooling() {
        return interceptors.getPoolingAttributes();
    }

    public interface Interceptors {
        ConnectionInterceptor getStack();

        ConnectionInterceptor getRecoveryStack();

        PoolingSupport getPoolingAttributes();
    }

    protected static final GBeanInfo GBEAN_INFO;


    static {
        GBeanInfoBuilder infoBuilder = new GBeanInfoBuilder(AbstractConnectionManager.class, NameFactory.JCA_CONNECTION_MANAGER);

        infoBuilder.addInterface(ConnectionManagerFactory.class);
        //these attributes are persisted via the pooling state.
        infoBuilder.addInterface(PoolingAttributes.class);

        GBEAN_INFO = infoBuilder.getBeanInfo();
    }

}
