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
//        implements ConnectionInterceptor, PoolingAttributes {

//    private static Log log = LogFactory.getLog(SinglePoolMatchAllConnectionInterceptor.class.getName());


//    private final ConnectionInterceptor next;

//    private Timer timer = PoolIdleReleaserTimer.getTimer();

//    private FIFOSemaphore permits;

    private HashMap pool;

    private int maxSize;

//    private int blockingTimeoutMilliseconds;

//    private long idleTimeoutMilliseconds;

//    private int connectionCount = 0;

//    private int minSize = 0;

//    private IdleReleaser idleReleaser;
//    private int shrinkLater = 0;

    public SinglePoolMatchAllConnectionInterceptor(final ConnectionInterceptor next,
                                                   int maxSize,
                                                   int minSize,
                                                   int blockingTimeoutMilliseconds,
                                                   int idleTimeoutMinutes) {

        super(next, maxSize, minSize, blockingTimeoutMilliseconds, idleTimeoutMinutes);
//        this.next = next;
        this.maxSize = maxSize;
//        this.blockingTimeoutMilliseconds = blockingTimeoutMilliseconds;
//        permits = new FIFOSemaphore(maxSize);
        pool = new HashMap(maxSize);
//        setIdleTimeoutMinutes(idleTimeoutMinutes);
    }

//    public void getConnection(ConnectionInfo connectionInfo) throws ResourceException {
//        if (connectionInfo.getManagedConnectionInfo().getManagedConnection() != null) {
//            return;
//        }
//        try {
//            if (permits.attempt(blockingTimeoutMilliseconds)) {
//                internalGetConnection(connectionInfo);
//            } else {
//                throw new ResourceException("No ManagedConnections available "
//                        + "within configured blocking timeout ( "
//                        + blockingTimeoutMilliseconds
//                        + " [ms] )");
//
//            } // end of else
//
//        } catch (InterruptedException ie) {
//            throw new ResourceException("Interrupted while requesting permit!");
//        } // end of try-catch
//    }

    protected void internalGetConnection(ConnectionInfo connectionInfo) throws ResourceException {
        synchronized (pool) {
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

//    public void returnConnection(ConnectionInfo connectionInfo,
//                                 ConnectionReturnAction connectionReturnAction) {
//        if (log.isTraceEnabled()) {
//            log.trace("returning connection" + connectionInfo.getConnectionHandle());
//        }
//        ManagedConnectionInfo mci = connectionInfo.getManagedConnectionInfo();
//        if (connectionReturnAction == ConnectionReturnAction.RETURN_HANDLE && mci.hasConnectionHandles()) {
//            return;
//        }
//
//        boolean wasInPool = internalReturn(connectionInfo, connectionReturnAction);
//
//        if (!wasInPool) {
//            permits.release();
//        }
//    }

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

    //PoolingAttributes implementation
//    public int getPartitionCount() {
//        return 1;
//    }

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

//    public int getConnectionCount() {
//        return connectionCount;
//    }

//    public int getBlockingTimeoutMilliseconds() {
//        return blockingTimeoutMilliseconds;
//    }
//
//    public void setBlockingTimeoutMilliseconds(int timeoutMilliseconds) {
//        this.blockingTimeoutMilliseconds = timeoutMilliseconds;
//    }

//    public int getIdleTimeoutMinutes() {
//        return (int) idleTimeoutMilliseconds / (1000 * 60);
//    }

//    public void setIdleTimeoutMinutes(int idleTimeoutMinutes) {
//        this.idleTimeoutMilliseconds = idleTimeoutMinutes * 60 * 1000;
//        if (idleReleaser != null) {
//            idleReleaser.cancel();
//        }
//        idleReleaser = new IdleReleaser();
//        timer.schedule(idleReleaser, this.idleTimeoutMilliseconds, this.idleTimeoutMilliseconds);
//    }


//    private class IdleReleaser extends TimerTask {
//
//        public void run() {
//            long threshold = System.currentTimeMillis() - idleTimeoutMilliseconds;
//            ManagedConnectionInfo[] killList = new ManagedConnectionInfo[pool.size()];
//            int j = 0;
//            synchronized (pool) {
//                for (Iterator iterator = pool.entrySet().iterator(); iterator.hasNext();) {
//                    Map.Entry entry = (Map.Entry) iterator.next();
//                    ManagedConnectionInfo mci = (ManagedConnectionInfo) entry.getValue();
//                    if (mci.getLastUsed() < threshold) {
//                        killList[j] = mci;
//                        j++;
//                    }
//                }
//            }
//            for (int i = 0; i < j; i++) {
//                ManagedConnectionInfo managedConnectionInfo = killList[i];
//                ConnectionInfo killInfo = new ConnectionInfo(managedConnectionInfo);
//                internalReturn(killInfo, ConnectionReturnAction.DESTROY);
//            }
//            permits.release(j);
//        }
//    }
//
//    private class FillTask extends TimerTask {
//        private final ManagedConnectionFactory managedConnectionFactory;
//        private final Subject subject;
//        private final ConnectionRequestInfo cri;
//
//        public FillTask(ConnectionInfo connectionInfo) {
//            managedConnectionFactory = connectionInfo.getManagedConnectionInfo().getManagedConnectionFactory();
//            subject = connectionInfo.getManagedConnectionInfo().getSubject();
//            cri = connectionInfo.getManagedConnectionInfo().getConnectionRequestInfo();
//        }
//
//        public void run() {
//            while (connectionCount < minSize) {
//                ManagedConnectionInfo mci = new ManagedConnectionInfo(managedConnectionFactory, cri);
//                mci.setSubject(subject);
//                ConnectionInfo ci = new ConnectionInfo(mci);
//                try {
//                    next.getConnection(ci);
//                } catch (ResourceException e) {
//                    return;
//                }
//                boolean added = false;
//                synchronized (pool) {
//                    connectionCount++;
//                    added = maxSize > pool.size();
//                    if (added) {
//                        pool.put(mci.getManagedConnection(), mci);
//                    }
//                }
//                if (!added) {
//                    internalReturn(ci, ConnectionReturnAction.DESTROY);
//                    return;
//                }
//            }
//        }
//    }

}