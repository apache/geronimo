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

import java.util.TimerTask;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Timer;
import javax.resource.spi.ManagedConnectionFactory;
import javax.resource.spi.ConnectionRequestInfo;
import javax.resource.ResourceException;
import javax.security.auth.Subject;

import EDU.oswego.cs.dl.util.concurrent.FIFOSemaphore;
import EDU.oswego.cs.dl.util.concurrent.ReadWriteLock;
import EDU.oswego.cs.dl.util.concurrent.WriterPreferenceReadWriteLock;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * @version $Rev:  $ $Date:  $
 */
public abstract class AbstractSinglePoolConnectionInterceptor implements ConnectionInterceptor, PoolingAttributes {
    protected static Log log = LogFactory.getLog(SinglePoolConnectionInterceptor.class.getName());
    protected final ConnectionInterceptor next;
    private final ReadWriteLock resizeLock = new WriterPreferenceReadWriteLock();
    protected FIFOSemaphore permits;
    protected int blockingTimeoutMilliseconds;
    protected int connectionCount = 0;
    private long idleTimeoutMilliseconds;
    private IdleReleaser idleReleaser;
    protected Timer timer = PoolIdleReleaserTimer.getTimer();
    protected int minSize = 0;
    protected int shrinkLater = 0;

    public AbstractSinglePoolConnectionInterceptor(final ConnectionInterceptor next,
                                                   int maxSize,
                                                   int minSize,
                                                   int blockingTimeoutMilliseconds,
                                                   int idleTimeoutMinutes) {
        this.next = next;
        this.minSize = minSize;
        this.blockingTimeoutMilliseconds = blockingTimeoutMilliseconds;
        setIdleTimeoutMinutes(idleTimeoutMinutes);
        permits = new FIFOSemaphore(maxSize);
    }

    public void getConnection(ConnectionInfo connectionInfo) throws ResourceException {
        if (connectionInfo.getManagedConnectionInfo().getManagedConnection() != null) {
            return;
        }
        try {
            resizeLock.readLock().acquire();
            try {
                if (permits.attempt(blockingTimeoutMilliseconds)) {
                    internalGetConnection(connectionInfo);
                } else {
                    throw new ResourceException("No ManagedConnections available "
                            + "within configured blocking timeout ( "
                            + blockingTimeoutMilliseconds
                            + " [ms] )");

                }
            } finally {
                resizeLock.readLock().release();
            }

        } catch (InterruptedException ie) {
            throw new ResourceException("Interrupted while requesting permit!");
        } // end of try-catch
    }

    protected abstract void internalGetConnection(ConnectionInfo connectionInfo) throws ResourceException;

    public void returnConnection(ConnectionInfo connectionInfo,
                                 ConnectionReturnAction connectionReturnAction) {
        if (log.isTraceEnabled()) {
            log.trace("returning connection" + connectionInfo.getConnectionHandle());
        }
        try {
            resizeLock.readLock().acquire();
        } catch (InterruptedException e) {
            //TODO figure out something better to do here!!!
            throw new RuntimeException("Interrupted before returning connection! Pool is now in an invalid state!");
        }
        try {
            ManagedConnectionInfo mci = connectionInfo.getManagedConnectionInfo();
            if (connectionReturnAction == ConnectionReturnAction.RETURN_HANDLE && mci.hasConnectionHandles()) {
                return;
            }

            boolean wasInPool = internalReturn(connectionInfo, connectionReturnAction);

            if (!wasInPool) {
                permits.release();
            }
        } finally {
            resizeLock.readLock().release();
        }
    }

    protected abstract boolean internalReturn(ConnectionInfo connectionInfo, ConnectionReturnAction connectionReturnAction);

    public int getPartitionCount() {
        return 1;
    }

    public abstract int getPartitionMaxSize();

    public void setPartitionMaxSize(int maxSize) throws InterruptedException {
        if (maxSize <= 0) {
            throw new IllegalArgumentException("Max size must be positive, not " + maxSize);
        }
        if (maxSize != getPartitionMaxSize()) {
            resizeLock.writeLock().acquire();
            try {
                //example: old maxsize 40, permits 20, connection count 20
                //new maxSize 10
                //shrinkLater is 10
                //shrinkNow is 20
                //2nd example: old maxsize 30, permits 10, connection count 10
                //new maxSize 40
                //shrinkLater and shrinkNow are 0.
                int checkedOut = (int) permits.permits();
                shrinkLater = checkedOut - maxSize;
                if (shrinkLater < 0) {
                    shrinkLater = 0;
                }
                int shrinkNow = checkedOut + connectionCount - maxSize - shrinkLater;
                if (shrinkNow < 0) {
                    shrinkNow = 0;
                }

                permits = new FIFOSemaphore(maxSize);
                //1st example: acquire 10 (all)
                //2nd example: acquire 10 (same as in old semaphore)
                for (int i = 0; i < checkedOut - shrinkLater; i++) {
                    permits.acquire();
                }
                //1st example: copy 0 (none)
                //2nd example: copy 10 (all)
                transferConnections(maxSize, shrinkNow);
            } finally {
                resizeLock.writeLock().release();
            }
        }
    }

    protected abstract void transferConnections(int maxSize, int shrinkNow);

    public abstract int getIdleConnectionCount();

    public int getConnectionCount() {
        return connectionCount;
    }

    public int getPartitionMinSize() {
        return minSize;
    }

    public void setPartitionMinSize(int minSize) {
        this.minSize = minSize;
    }

    public int getBlockingTimeoutMilliseconds() {
        return blockingTimeoutMilliseconds;
    }

    public void setBlockingTimeoutMilliseconds(int blockingTimeoutMilliseconds) {
        if (blockingTimeoutMilliseconds < 0) {
            throw new IllegalArgumentException("blockingTimeoutMilliseconds must be positive or 0, not " + blockingTimeoutMilliseconds);
        }
        if (blockingTimeoutMilliseconds == 0) {
            this.blockingTimeoutMilliseconds = Integer.MAX_VALUE;
        } else {
            this.blockingTimeoutMilliseconds = blockingTimeoutMilliseconds;
        }
    }

    public int getIdleTimeoutMinutes() {
        return (int) idleTimeoutMilliseconds / (1000 * 60);
    }

    public void setIdleTimeoutMinutes(int idleTimeoutMinutes) {
        if (idleTimeoutMinutes < 0) {
            throw new IllegalArgumentException("idleTimeoutMinutes must be positive or 0, not " + idleTimeoutMinutes);
        }
        if (idleReleaser != null) {
            idleReleaser.cancel();
        }
        if (idleTimeoutMinutes > 0) {
            this.idleTimeoutMilliseconds = idleTimeoutMinutes * 60 * 1000;
            idleReleaser = new IdleReleaser();
            timer.schedule(idleReleaser, this.idleTimeoutMilliseconds, this.idleTimeoutMilliseconds);
        }
    }

    protected abstract void getExpiredManagedConnectionInfos(long threshold, ArrayList killList);

    protected abstract boolean addToPool(ManagedConnectionInfo mci);

    private class IdleReleaser extends TimerTask {

        public void run() {
            try {
                resizeLock.readLock().acquire();
            } catch (InterruptedException e) {
                return;
            }
            try {
                long threshold = System.currentTimeMillis() - idleTimeoutMilliseconds;
                ArrayList killList = new ArrayList(getPartitionMaxSize());
                getExpiredManagedConnectionInfos(threshold, killList);
                for (Iterator i = killList.iterator(); i.hasNext();) {
                    ManagedConnectionInfo managedConnectionInfo = (ManagedConnectionInfo) i.next();
                    ConnectionInfo killInfo = new ConnectionInfo(managedConnectionInfo);
                    internalReturn(killInfo, ConnectionReturnAction.DESTROY);
                }
                permits.release(killList.size());
            } finally {
                resizeLock.readLock().release();
            }
        }

    }

    protected class FillTask extends TimerTask {
        private final ManagedConnectionFactory managedConnectionFactory;
        private final Subject subject;
        private final ConnectionRequestInfo cri;

        public FillTask(ConnectionInfo connectionInfo) {
            managedConnectionFactory = connectionInfo.getManagedConnectionInfo().getManagedConnectionFactory();
            subject = connectionInfo.getManagedConnectionInfo().getSubject();
            cri = connectionInfo.getManagedConnectionInfo().getConnectionRequestInfo();
        }

        public void run() {
            try {
                resizeLock.readLock().acquire();
            } catch (InterruptedException e) {
                return;
            }
            try {
                while (connectionCount < minSize) {
                    ManagedConnectionInfo mci = new ManagedConnectionInfo(managedConnectionFactory, cri);
                    mci.setSubject(subject);
                    ConnectionInfo ci = new ConnectionInfo(mci);
                    try {
                        next.getConnection(ci);
                    } catch (ResourceException e) {
                        return;
                    }
                    boolean added = false;
                    added = addToPool(mci);
                    if (!added) {
                        internalReturn(ci, ConnectionReturnAction.DESTROY);
                        return;
                    }
                }
            } finally {
                resizeLock.readLock().release();
            }
        }

    }
}
