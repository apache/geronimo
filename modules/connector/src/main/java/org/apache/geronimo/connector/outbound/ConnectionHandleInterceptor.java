/**
 *
 * Copyright 2003-2005 The Apache Software Foundation
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

/**
 * ConnectionHandleInterceptor.java
 *
 *
 * @version $Rev$ $Date$
 */
public class ConnectionHandleInterceptor implements ConnectionInterceptor {

    private final ConnectionInterceptor next;

    public ConnectionHandleInterceptor(ConnectionInterceptor next) {
        this.next = next;
    }

    /**
     * in: connectionInfo not null, managedConnectionInfo not null. ManagedConnection may or may not be null.  ConnectionHandle may or may not be null
     * out: managedConnection not null. connection handle not null. managedConnectionInfo has connection handle registered.  Connection handle is associated with ManagedConnection.
     * @param connectionInfo
     * @throws ResourceException
     */
    public void getConnection(ConnectionInfo connectionInfo) throws ResourceException {
        next.getConnection(connectionInfo);
        ManagedConnectionInfo mci = connectionInfo.getManagedConnectionInfo();
        if (connectionInfo.getConnectionHandle() == null) {
            connectionInfo.setConnectionHandle(
                    mci.getManagedConnection().getConnection(
                            mci.getSubject(),
                            mci.getConnectionRequestInfo()));
            mci.addConnectionHandle(connectionInfo);

        } else if (!mci.hasConnectionInfo(connectionInfo)) {
            mci.getManagedConnection().associateConnection(
                    connectionInfo.getConnectionHandle());
            mci.addConnectionHandle(connectionInfo);
        }
        connectionInfo.setTrace();
    }

    /**
     *  in: connectionInfo not null, managedConnectionInfo not null, managedConnection not null.  Handle can be null if mc is being destroyed from pool.
     * out: managedCOnnectionInfo null, handle not in mci.handles.
     * @param connectionInfo
     * @param connectionReturnAction
     */
    public void returnConnection(ConnectionInfo connectionInfo, ConnectionReturnAction connectionReturnAction) {
        if (connectionInfo.getConnectionHandle() != null) {
            connectionInfo.getManagedConnectionInfo().removeConnectionHandle(
                    connectionInfo);
        }
        next.returnConnection(connectionInfo, connectionReturnAction);
    }

    public void destroy() {
        next.destroy();
    }
}
