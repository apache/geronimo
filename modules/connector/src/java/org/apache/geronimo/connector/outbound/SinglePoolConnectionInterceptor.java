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
import java.util.Collections;
import javax.resource.ResourceException;
import javax.resource.spi.ManagedConnection;

/**
 * SinglePoolConnectionInterceptor chooses a single connection from the pool.  If selectOneAssumeMatch
 * is true, it simply returns the selected connection.
 * THIS SHOULD BE USED ONLY IF MAXIMUM SPEED IS ESSENTIAL AND YOU HAVE THOROUGLY CHECKED THAT
 * MATCHING WOULD SUCCEED ON THE SELECTED CONNECTION. (i.e., read the docs on your connector
 * to find out how matching works)
 * If selectOneAssumeMatch is false, it checks with the ManagedConnectionFactory that the
 * selected connection does match before returning it: if not it throws an exception.
 *
 * @version $Rev$ $Date$
 */
public class SinglePoolConnectionInterceptor extends AbstractSinglePoolConnectionInterceptor {


    private boolean selectOneAssumeMatch;

    private PoolDeque pool;


    public SinglePoolConnectionInterceptor(final ConnectionInterceptor next,
                                           int maxSize,
                                           int minSize,
                                           int blockingTimeoutMilliseconds,
                                           int idleTimeoutMinutes,
                                           boolean selectOneAssumeMatch) {
        super(next, maxSize, minSize, blockingTimeoutMilliseconds, idleTimeoutMinutes);
        pool = new PoolDeque(maxSize);
        this.selectOneAssumeMatch = selectOneAssumeMatch;
    }

    protected void internalGetConnection(ConnectionInfo connectionInfo) throws ResourceException {
        synchronized (pool) {
            ManagedConnectionInfo newMCI = null;
            if (pool.isEmpty()) {
                next.getConnection(connectionInfo);
                connectionCount++;
                if (log.isTraceEnabled()) {
                    log.trace("Returning new connection " + connectionInfo.getManagedConnectionInfo());
                }
                return;
            } else {
                newMCI = pool.removeLast();
            }
            if (connectionCount < minSize) {
                timer.schedule(new FillTask(connectionInfo), 10);
            }
            if (selectOneAssumeMatch) {
                connectionInfo.setManagedConnectionInfo(newMCI);
                if (log.isTraceEnabled()) {
                    log.trace("Returning pooled connection without checking matching " + connectionInfo.getManagedConnectionInfo());
                }
                return;
            }
            try {
                ManagedConnectionInfo mci = connectionInfo.getManagedConnectionInfo();
                ManagedConnection matchedMC =
                        newMCI
                        .getManagedConnectionFactory()
                        .matchManagedConnections(Collections.singleton(newMCI.getManagedConnection()),
                                mci.getSubject(),
                                mci.getConnectionRequestInfo());
                if (matchedMC != null) {
                    connectionInfo.setManagedConnectionInfo(newMCI);
                    if (log.isTraceEnabled()) {
                        log.trace("Returning pooled connection " + connectionInfo.getManagedConnectionInfo());
                    }
                    return;
                } else {
                    //matching failed.
                    ConnectionInfo returnCI = new ConnectionInfo();
                    returnCI.setManagedConnectionInfo(newMCI);
                    returnConnection(returnCI,
                            ConnectionReturnAction.RETURN_HANDLE);
                    throw new ResourceException("The pooling strategy does not match the MatchManagedConnections implementation.  Please investigate and reconfigure this pool");
                }
            } catch (ResourceException e) {
                //something is wrong: destroy connection, rethrow, release permit
                ConnectionInfo returnCI = new ConnectionInfo();
                returnCI.setManagedConnectionInfo(newMCI);
                returnConnection(returnCI,
                        ConnectionReturnAction.DESTROY);
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
            if (shrinkLater > 0) {
                //nothing can get in the pool while shrinkLater > 0, so wasInPool is false here.
                connectionReturnAction = ConnectionReturnAction.DESTROY;
                shrinkLater--;
            } else if (connectionReturnAction == ConnectionReturnAction.RETURN_HANDLE) {
                mci.setLastUsed(System.currentTimeMillis());
                pool.add(mci);
                return wasInPool;
            } else {
                wasInPool = pool.remove(mci);
            }
        }
        //we must destroy connection.
        next.returnConnection(connectionInfo, connectionReturnAction);
        connectionCount--;
        return wasInPool;
    }

    public int getPartitionMaxSize() {
        return pool.capacity();
    }

    protected void transferConnections(int maxSize, int shrinkNow) {
        //1st example: copy 0 (none)
        //2nd example: copy 10 (all)
        PoolDeque oldPool = pool;
        pool = new PoolDeque(maxSize);
        //since we have replaced pool already, pool.remove will be very fast:-)
        for (int i = 0; i < shrinkNow; i++) {
            ConnectionInfo killInfo = new ConnectionInfo(oldPool.peek(i));
            internalReturn(killInfo, ConnectionReturnAction.DESTROY);
        }
        for (int i = shrinkNow; i < connectionCount; i++) {
            pool.add(oldPool.peek(i));
        }
    }

    public int getIdleConnectionCount() {
        return pool.currentSize();
    }


    protected void getExpiredManagedConnectionInfos(long threshold, ArrayList killList) {
        synchronized (pool) {
            for (int i = 0; i < pool.currentSize(); i++) {
                ManagedConnectionInfo mci = pool.peek(i);
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
                pool.add(mci);
            }
        }
        return added;
    }


    static class PoolDeque {

        private final ManagedConnectionInfo[] deque;
        private final int first = 0;
        private int last = -1;

        public PoolDeque(int size) {
            deque = new ManagedConnectionInfo[size];
        }

        //internal
        public boolean isEmpty() {
            return first > last;
        }

        //internal
        public void add(ManagedConnectionInfo mci) {
            if (last == deque.length - 1) {
                throw new IllegalStateException("deque is full");
            }

            deque[++last] = mci;
        }

        //internal
        public ManagedConnectionInfo peek(int i) {
            if (i < first || i > last) {
                throw new IllegalStateException("index is out of current range");
            }
            return deque[i];
        }

        //internal
        public ManagedConnectionInfo removeLast() {
            if (isEmpty()) {
                throw new IllegalStateException("deque is empty");
            }

            return deque[last--];
        }

        //internal
        public boolean remove(ManagedConnectionInfo mci) {
            for (int i = first; i <= last; i++) {
                if (deque[i] == mci) {
                    for (int j = i + 1; j <= last; j++) {
                        deque[j - 1] = deque[j];
                    }
                    last--;
                    return true;
                }

            }
            return false;
        }

        //internal
        public int capacity() {
            return deque.length;
        }

        //internal
        public int currentSize() {
            return last - first + 1;
        }
    }

}
