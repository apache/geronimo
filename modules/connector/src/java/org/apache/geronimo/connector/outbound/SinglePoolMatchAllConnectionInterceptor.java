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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import javax.resource.ResourceException;
import javax.resource.spi.ManagedConnection;
import javax.resource.spi.ManagedConnectionFactory;

/**
 * This pool is the most spec-compliant pool.  It can be used by itself with no partitioning.
 * It is apt to be the slowest pool.
 * For each connection request, it synchronizes access to the pool and asks the
 * ManagedConnectionFactory for a match from among all managed connections.  If none is found,
 * it may discard a random existing connection, and creates a new connection.
 *
 * @version $Rev$ $Date$
 */
public class SinglePoolMatchAllConnectionInterceptor extends AbstractSinglePoolConnectionInterceptor {

    private HashMap pool;

    private int maxSize;

    public SinglePoolMatchAllConnectionInterceptor(final ConnectionInterceptor next,
                                                   int maxSize,
                                                   int minSize,
                                                   int blockingTimeoutMilliseconds,
                                                   int idleTimeoutMinutes) {

        super(next, maxSize, minSize, blockingTimeoutMilliseconds, idleTimeoutMinutes);
        this.maxSize = maxSize;
        pool = new HashMap(maxSize);
    }

    protected void internalGetConnection(ConnectionInfo connectionInfo) throws ResourceException {
        synchronized (pool) {
            if (destroyed) {
                throw new ResourceException("ManagedConnection pool has been destroyed");
            }
            try {
                if (!pool.isEmpty()) {
                    ManagedConnectionInfo mci = connectionInfo.getManagedConnectionInfo();
                    ManagedConnectionFactory managedConnectionFactory = mci.getManagedConnectionFactory();
                    ManagedConnection matchedMC =
                            managedConnectionFactory
                            .matchManagedConnections(pool.keySet(),
                                    mci.getSubject(),
                                    mci.getConnectionRequestInfo());
                    if (matchedMC != null) {
                        connectionInfo.setManagedConnectionInfo((ManagedConnectionInfo) pool.get(matchedMC));
                        pool.remove(matchedMC);
                        if (log.isTraceEnabled()) {
                            log.trace("Returning pooled connection " + connectionInfo.getManagedConnectionInfo());
                        }
                        if (connectionCount < minSize) {
                            timer.schedule(new FillTask(connectionInfo), 10);
                        }
                        return;
                    }
                }
                //matching failed or pool is empty
                //if pool is at maximum size, pick a cx to kill
                if (connectionCount == maxSize) {
                    Iterator iterator = pool.entrySet().iterator();
                    ManagedConnectionInfo kill = (ManagedConnectionInfo) ((Map.Entry) iterator.next()).getValue();
                    iterator.remove();
                    ConnectionInfo killInfo = new ConnectionInfo(kill);
                    internalReturn(killInfo, ConnectionReturnAction.DESTROY);
                }
                next.getConnection(connectionInfo);
                connectionCount++;
                if (log.isTraceEnabled()) {
                    log.trace("Returning new connection " + connectionInfo.getManagedConnectionInfo());
                }
                if (connectionCount < minSize) {
                    timer.schedule(new FillTask(connectionInfo), 10);
                }

            } catch (ResourceException e) {
                //something is wrong: rethrow, release permit
                permits.release();
                throw e;
            }
        }
    }

    protected boolean internalReturn(ConnectionInfo connectionInfo, ConnectionReturnAction connectionReturnAction) {
        ManagedConnectionInfo mci = connectionInfo.getManagedConnectionInfo();
        ManagedConnection mc = mci.getManagedConnection();
        try {
            mc.cleanup();
        } catch (ResourceException e) {
            connectionReturnAction = ConnectionReturnAction.DESTROY;
        }

        boolean wasInPool = false;
        synchronized (pool) {
            // a bit redundant, but this closes a small timing hole...
            if (destroyed) {
                try {
                    mc.destroy();
                }
                catch (ResourceException re) { } // ignore
                return pool.remove(mci.getManagedConnection()) != null;
            }
            if (shrinkLater > 0) {
                //nothing can get in the pool while shrinkLater > 0, so wasInPool is false here.
                connectionReturnAction = ConnectionReturnAction.DESTROY;
                shrinkLater--;
            } else if (connectionReturnAction == ConnectionReturnAction.RETURN_HANDLE) {
                mci.setLastUsed(System.currentTimeMillis());
                pool.put(mci.getManagedConnection(), mci);
                return wasInPool;
            } else {
                wasInPool = pool.remove(mci.getManagedConnection()) != null;
            }
        }
        //we must destroy connection.
        next.returnConnection(connectionInfo, connectionReturnAction);
        connectionCount--;
        return wasInPool;
    }

    protected void internalDestroy() {
        synchronized (pool) {
            Iterator it = pool.keySet().iterator();
            for (; it.hasNext(); ) {
                try {
                    ((ManagedConnection)it.next()).destroy();
                }
                catch (ResourceException re) { } // ignore
                it.remove();
            }
        }
    }

    public int getPartitionMaxSize() {
        return maxSize;
    }

    public int getIdleConnectionCount() {
        return pool.size();
    }

    protected void transferConnections(int maxSize, int shrinkNow) {
        //1st example: copy 0 (none)
        //2nd example: copy 10 (all)
        HashMap oldPool = pool;
        pool = new HashMap(maxSize);
        //since we have replaced pool already, pool.remove will be very fast:-)
        assert oldPool.size() == connectionCount;
        Iterator it = oldPool.entrySet().iterator();
        for (int i = 0; i < shrinkNow; i++) {
            ConnectionInfo killInfo = new ConnectionInfo((ManagedConnectionInfo) ((Map.Entry)it.next()).getValue());
            internalReturn(killInfo, ConnectionReturnAction.DESTROY);
        }
        for (; it.hasNext(); ) {
            Map.Entry entry = (Map.Entry) it.next();
            pool.put(entry.getKey(), entry.getValue());
        }

    }

    protected void getExpiredManagedConnectionInfos(long threshold, ArrayList killList) {
        synchronized (pool) {
            for (Iterator iterator = pool.entrySet().iterator(); iterator.hasNext();) {
                Map.Entry entry = (Map.Entry) iterator.next();
                ManagedConnectionInfo mci = (ManagedConnectionInfo) entry.getValue();
                if (mci.getLastUsed() < threshold) {
                    killList.add(mci);
                }
            }
        }

    }

    protected boolean addToPool(ManagedConnectionInfo mci) {
        boolean added;
        synchronized (pool) {
            connectionCount++;
            added = getPartitionMaxSize() > getIdleConnectionCount();
            if (added) {
                pool.put(mci.getManagedConnection(), mci);
            }
        }
        return added;
    }

}
