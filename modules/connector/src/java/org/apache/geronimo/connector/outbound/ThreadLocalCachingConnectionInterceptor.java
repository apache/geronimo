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

import java.util.Collections;

import javax.resource.ResourceException;

/**
 *
 *
 * @version $Rev$ $Date$
 *
 * */
public class ThreadLocalCachingConnectionInterceptor implements ConnectionInterceptor {

    private final ConnectionInterceptor next;

    private final ThreadLocal connections = new ThreadLocal();
    private final boolean matchConnections;

    public ThreadLocalCachingConnectionInterceptor(final ConnectionInterceptor next, final boolean matchConnections) {
        this.next = next;
        this.matchConnections = matchConnections;
    }


    public void getConnection(ConnectionInfo connectionInfo) throws ResourceException {
        if (connectionInfo.isUnshareable()) {
            next.getConnection(connectionInfo);
            return;
        }
        ManagedConnectionInfo managedConnectionInfo = (ManagedConnectionInfo) connections.get();
        if (managedConnectionInfo != null) {
            if (matchConnections) {
                ManagedConnectionInfo mciRequest = connectionInfo.getManagedConnectionInfo();
                if (null != managedConnectionInfo.getManagedConnectionFactory().matchManagedConnections(
                        Collections.singleton(managedConnectionInfo.getManagedConnection()),
                        mciRequest.getSubject(),
                        mciRequest.getConnectionRequestInfo()
                )) {
                    connectionInfo.setManagedConnectionInfo(managedConnectionInfo);
                    return;
                } else {
                    //match failed, get a new cx after returning this one
                    connections.set(null);
                    next.returnConnection(connectionInfo, ConnectionReturnAction.RETURN_HANDLE);
                }
            } else {
                connectionInfo.setManagedConnectionInfo(managedConnectionInfo);
                return;
            }
        }
        //nothing for this thread or match failed
        next.getConnection(connectionInfo);
        connections.set(connectionInfo.getManagedConnectionInfo());
    }

    public void returnConnection(ConnectionInfo connectionInfo, ConnectionReturnAction connectionReturnAction) {
        if (connectionReturnAction == ConnectionReturnAction.DESTROY || connectionInfo.isUnshareable()) {
            next.returnConnection(connectionInfo, connectionReturnAction);
        }
    }
}
