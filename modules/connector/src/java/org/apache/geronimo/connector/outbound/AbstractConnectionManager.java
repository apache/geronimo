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
import org.apache.geronimo.gbean.GBeanInfoFactory;
import org.apache.geronimo.gbean.GBeanLifecycle;
import org.apache.geronimo.gbean.WaitingException;
import org.apache.geronimo.transaction.manager.NamedXAResource;

/**
 * @version $Rev$ $Date$
 */
public abstract class AbstractConnectionManager implements ConnectionManagerFactory, GBeanLifecycle, ConnectionManager, LazyAssociatableConnectionManager {

    private ConnectionInterceptor stack;

    private ConnectionInterceptor recoveryStack;


    public AbstractConnectionManager() {
    }

    public void doStart() throws WaitingException, Exception {
        ConnectionInterceptor[] stacks = setUpConnectionManager();
        assert stacks.length == 2;
        stack = stacks[0];
        recoveryStack = stacks[1];
    }

    protected abstract ConnectionInterceptor[] setUpConnectionManager() throws IllegalStateException;

    public void doStop() {
        stack = null;
    }

    public void doFail() {
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
        stack.getConnection(ci);
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
        stack.getConnection(ci);
    }

    ConnectionInterceptor getConnectionInterceptor() {
        return stack;
    }


    public ConnectionManagerFactory.ReturnableXAResource getRecoveryXAResource(ManagedConnectionFactory managedConnectionFactory) throws ResourceException {
        ManagedConnectionInfo mci = new ManagedConnectionInfo(managedConnectionFactory, null);
        NamedXAResource namedXAResource = (NamedXAResource) mci.getXAResource();
        if (namedXAResource == null) {
            //obviously, we can't do recovery.
            return null;
        }
        ConnectionInfo recoveryConnectionInfo = new ConnectionInfo(mci);
        recoveryStack.getConnection(recoveryConnectionInfo);
        return new ConnectionManagerFactory.ReturnableXAResource(namedXAResource, recoveryStack, recoveryConnectionInfo);
    }


    protected static final GBeanInfo GBEAN_INFO;


    static {
        GBeanInfoFactory infoFactory = new GBeanInfoFactory(AbstractConnectionManager.class);

        infoFactory.addInterface(ConnectionManagerFactory.class);

        GBEAN_INFO = infoFactory.getBeanInfo();
    }

}
