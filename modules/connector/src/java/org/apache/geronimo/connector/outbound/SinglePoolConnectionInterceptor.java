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

import java.util.Collections;

import javax.resource.ResourceException;
import javax.resource.spi.ManagedConnection;

import EDU.oswego.cs.dl.util.concurrent.FIFOSemaphore;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

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
 *
 */
public class SinglePoolConnectionInterceptor implements ConnectionInterceptor, PoolingAttributes {

    private static Log log = LogFactory.getLog(SinglePoolConnectionInterceptor.class.getName());


    private final ConnectionInterceptor next;

    private FIFOSemaphore permits;

    private PoolDeque pool;

    private int blockingTimeout;
    private boolean selectOneAssumeMatch;

    private int connectionCount = 0;

    public SinglePoolConnectionInterceptor(
            final ConnectionInterceptor next,
            int maxSize,
            int blockingTimeout,
            boolean selectOneAssumeMatch) {
        this.next = next;
        this.blockingTimeout = blockingTimeout;
        permits = new FIFOSemaphore(maxSize);
        pool = new PoolDeque(maxSize);
        this.selectOneAssumeMatch = selectOneAssumeMatch;
    }

    public void getConnection(ConnectionInfo connectionInfo) throws ResourceException {
        ManagedConnectionInfo mci = connectionInfo.getManagedConnectionInfo();
        if (mci.getManagedConnection() != null) {
            return;
        }
        try {
            if (permits.attempt(blockingTimeout)) {
                ManagedConnectionInfo newMCI = null;
                synchronized (pool) {
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
                    if (selectOneAssumeMatch) {
                        connectionInfo.setManagedConnectionInfo(newMCI);
                        if (log.isTraceEnabled()) {
                            log.trace("Returning pooled connection without checking matching " + connectionInfo.getManagedConnectionInfo());
                        }
                        return;
                    }
                    try {
                        ManagedConnection matchedMC =
//                                newMCI.getManagedConnection();
                                newMCI
                                .getManagedConnectionFactory()
                                .matchManagedConnections(
                                        Collections.singleton(
                                                newMCI.getManagedConnection()),
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
                            returnConnection(
                                    returnCI,
                                    ConnectionReturnAction.RETURN_HANDLE);
                            throw new ResourceException("The pooling strategy does not match the MatchManagedConnections implementation.  Please investigate and reconfigure this pool");
                        }
                    } catch (ResourceException e) {
                        //something is wrong: destroy connection, rethrow, release permit
                        ConnectionInfo returnCI = new ConnectionInfo();
                        returnCI.setManagedConnectionInfo(newMCI);
                        returnConnection(
                                returnCI,
                                ConnectionReturnAction.DESTROY);
                        throw e;
                    } // end of try-catch
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
                wasInPool = pool.remove(mci);
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
            next.returnConnection(connectionInfo, connectionReturnAction);
            connectionCount--;
        } else {
            synchronized (pool) {
                mci.setLastUsed(System.currentTimeMillis());
                pool.addLast(mci);
            }

        } // end of else

        if (!wasInPool) {
            permits.release();
        }
    }

    public int getPartitionCount() {
        return 1;
    }

    public int getPartitionMaxSize() {
        return pool.capacity();
    }

    public int getIdleConnectionCount() {
        return pool.currentSize();
    }

    public int getConnectionCount() {
        return connectionCount;
    }

    static class PoolDeque {

        private final ManagedConnectionInfo[] deque;
        private int first = 0;
        private int last = -1;

        public PoolDeque(int size) {
            deque = new ManagedConnectionInfo[size];
        }

        public boolean isEmpty() {
            return first > last;
        }


        public void addLast(ManagedConnectionInfo mci) {
            if (last == deque.length - 1) {
                throw new IllegalStateException("deque is full");
            }

            deque[++last] = mci;
        }

        public ManagedConnectionInfo peekLast() {
            if (isEmpty()) {
                throw new IllegalStateException("deque is empty");
            }

            return deque[last];
        }

        public ManagedConnectionInfo removeLast() {
            if (isEmpty()) {
                throw new IllegalStateException("deque is empty");
            }

            return deque[last--];
        }

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

        public int capacity() {
            return deque.length;
        }

        public int currentSize() {
            return last - first + 1;
        }
    }

} // SinglePoolConnectionInterceptor
