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

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.resource.ResourceException;
import javax.resource.spi.ManagedConnection;
import javax.resource.spi.ManagedConnectionFactory;

import EDU.oswego.cs.dl.util.concurrent.FIFOSemaphore;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * This pool is the most spec-compliant pool.  It can be used by itself with no partitioning.
 * It is apt to be the slowest pool.
 * For each connection request, it synchronizes access to the pool and asks the
 * ManagedConnectionFactory for a match from among all managed connections.  If none is found,
 * it may discard a random existing connection, and creates a new connection.
 *
 * @version $Rev$ $Date$
 */
public class SinglePoolMatchAllConnectionInterceptor implements ConnectionInterceptor {

    private static Log log = LogFactory.getLog(SinglePoolMatchAllConnectionInterceptor.class.getName());


    private final ConnectionInterceptor next;

    private FIFOSemaphore permits;

    private HashMap pool;

    private int maxSize;

    private int blockingTimeout;
    private int actualConnections = 0;

    public SinglePoolMatchAllConnectionInterceptor(
            final ConnectionInterceptor next,
            int maxSize,
            int blockingTimeout) {
        this.next = next;
        this.maxSize = maxSize;
        this.blockingTimeout = blockingTimeout;
        permits = new FIFOSemaphore(maxSize);
        pool = new HashMap(maxSize);
    }

    public void getConnection(ConnectionInfo connectionInfo) throws ResourceException {
        ManagedConnectionInfo mci = connectionInfo.getManagedConnectionInfo();
        if (mci.getManagedConnection() != null) {
            return;
        }
        ManagedConnectionFactory managedConnectionFactory = mci.getManagedConnectionFactory();
        try {
            if (permits.attempt(blockingTimeout)) {
                synchronized (pool) {
                    try {
                        if (!pool.isEmpty()) {
                            ManagedConnection matchedMC =
                                    managedConnectionFactory
                                    .matchManagedConnections(
                                            pool.keySet(),
                                            mci.getSubject(),
                                            mci.getConnectionRequestInfo());
                            if (matchedMC != null) {
                                connectionInfo.setManagedConnectionInfo((ManagedConnectionInfo) pool.get(matchedMC));
                                if (log.isTraceEnabled()) {
                                    log.trace("Returning pooled connection " + connectionInfo.getManagedConnectionInfo());
                                }
                                return;
                            }
                            //matching failed or pool is empty
                            //if pool is at maximum size, pick a cx to kill
                            if (actualConnections == maxSize) {
                                Iterator iterator = pool.entrySet().iterator();
                                ManagedConnectionInfo kill = (ManagedConnectionInfo) ((Map.Entry) iterator.next()).getValue();
                                iterator.remove();
                                ConnectionInfo killInfo = new ConnectionInfo(kill);
                                returnConnection(killInfo, ConnectionReturnAction.DESTROY);
                            }
                            next.getConnection(connectionInfo);
                            actualConnections++;
                            if (log.isTraceEnabled()) {
                                log.trace("Returning new connection " + connectionInfo.getManagedConnectionInfo());
                            }
                            return;
                        }
                    } catch (ResourceException e) {
                        //something is wrong: rethrow, release permit
                        permits.release();
                        throw e;
                    }
                }
            } else {
                throw new ResourceException(
                        "No ManagedConnections available "
                        + "within configured blocking timeout ( "
                        + blockingTimeout
                        + " [ms] )");

            } // end of else

        } catch (InterruptedException ie) {
            throw new ResourceException("Interrupted while requesting permit!");
        } // end of try-catch
    }

    public void returnConnection(
            ConnectionInfo connectionInfo,
            ConnectionReturnAction connectionReturnAction) {
        if (log.isTraceEnabled()) {
            log.trace("returning connection" + connectionInfo.getConnectionHandle());
        }
        boolean wasInPool = false;
        ManagedConnectionInfo mci = connectionInfo.getManagedConnectionInfo();
        if (connectionReturnAction == ConnectionReturnAction.DESTROY) {
            synchronized (pool) {
                wasInPool = (pool.remove(mci.getManagedConnection()) != null);
            }
        } else {
            if (mci.hasConnectionHandles()) {
                return;
            }
        } // end of else

        ManagedConnection mc = mci.getManagedConnection();
        try {
            mc.cleanup();
        } catch (ResourceException e) {
            connectionReturnAction = ConnectionReturnAction.DESTROY;
        }

        if (connectionReturnAction == ConnectionReturnAction.DESTROY) {
            actualConnections--;
            next.returnConnection(connectionInfo, connectionReturnAction);
        } else {
            synchronized (pool) {
                mci.setLastUsed(System.currentTimeMillis());
                pool.put(mci.getManagedConnection(), mci);
            }
        }
        if (!wasInPool) {
            permits.release();
        }
    }

}