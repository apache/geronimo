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

import java.io.Serializable;
import javax.management.InstanceNotFoundException;
import javax.management.MBeanException;
import javax.management.MBeanServer;
import javax.management.MBeanServerFactory;
import javax.management.ObjectName;
import javax.management.ReflectionException;
import javax.resource.ResourceException;
import javax.resource.spi.ConnectionManager;
import javax.resource.spi.ConnectionRequestInfo;
import javax.resource.spi.LazyAssociatableConnectionManager;
import javax.resource.spi.ManagedConnectionFactory;

/**
 * ProxyConnectionManager.java
 *
 *
 * Created: Tue Sep 23 21:35:32 2003
 *
 * @version 1.0
 *
 */
public class ProxyConnectionManager
        implements Serializable, ConnectionManager, LazyAssociatableConnectionManager {

    /**
     * The field agentID holds the agentID of the mbean server
     * we use to lookup the stack if we are deserialized.
     */
    private final String agentID;

    /**
     * The field <code>ConnectionManagerName</code> holds the object name of
     * the ConnectionManagerDeployment that sets us up.
     *
     */
    private final ObjectName CMName;

    private transient ConnectionInterceptor stack;

    public ProxyConnectionManager(
            String agentID,
            ObjectName ConnectionManagerName,
            ConnectionInterceptor stack) {
        this.agentID = agentID;
        this.CMName = ConnectionManagerName;
        this.stack = stack;
    }

    /**
     * in: mcf != null, is a deployed mcf
     * out: useable connection object.
     * @param managedConnectionFactory
     * @param connectionRequestInfo
     * @return
     * @throws ResourceException
     */
    public Object allocateConnection(
            ManagedConnectionFactory managedConnectionFactory,
            ConnectionRequestInfo connectionRequestInfo)
            throws ResourceException {
        internalGetStack();
        ManagedConnectionInfo mci = new ManagedConnectionInfo(managedConnectionFactory, connectionRequestInfo);
        ConnectionInfo ci = new ConnectionInfo(mci);
        stack.getConnection(ci);
        return ci.getConnectionHandle();
    }

    /**
     * in: non-null connection object, from non-null mcf.
     * connection object is not associated with a managed connection
     * out: supplied connection object is assiciated with a non-null ManagedConnection from mcf.
     * @param connection
     * @param managedConnectionFactory
     * @param connectionRequestInfo
     * @throws ResourceException
     */
    public void associateConnection(
            Object connection,
            ManagedConnectionFactory managedConnectionFactory,
            ConnectionRequestInfo connectionRequestInfo)
            throws ResourceException {
        internalGetStack();
        ManagedConnectionInfo mci = new ManagedConnectionInfo(managedConnectionFactory, connectionRequestInfo);
        ConnectionInfo ci = new ConnectionInfo(mci);
        ci.setConnectionHandle(connection);
        stack.getConnection(ci);
    }

    private void internalGetStack() throws ResourceException {
        if (stack == null) {
            MBeanServer server =
                    (MBeanServer) MBeanServerFactory.findMBeanServer(agentID).get(
                            0);
            try {
                this.stack =
                        (ConnectionInterceptor) server.invoke(CMName, "getStack", null, null);
            } catch (InstanceNotFoundException e) {
                throw new ResourceException("Could not get stack from jmx", e);
            } catch (MBeanException e) {
                throw new ResourceException("Could not get stack from jmx", e);
            } catch (ReflectionException e) {
                throw new ResourceException("Could not get stack from jmx", e);
            }
        }
    }

    /**
     * The <code>getStack</code> method is called through jmx to get
     * the actual ConnectionInterceptor stack for deserialized copies
     * of this object.
     *
     * @return a <code>ConnectionInterceptor</code> value
     */
    public ConnectionInterceptor getStack() {
        return stack;
    }

}
