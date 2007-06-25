/**
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
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
import java.util.Iterator;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import javax.resource.ResourceException;
import javax.resource.spi.ConnectionRequestInfo;
import javax.resource.spi.ManagedConnectionFactory;
import javax.security.auth.Subject;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * @version $Rev$ $Date$
 */
public abstract class AbstractSinglePoolConnectionInterceptor implements ConnectionInterceptor, PoolingAttributes {
    protected static Log log = LogFactory.getLog(AbstractSinglePoolConnectionInterceptor.class.getName());
    protected final ConnectionInterceptor next;
    private final ReadWriteLock resizeLock = new ReentrantReadWriteLock();
    protected Semaphore permits;
    protected int blockingTimeoutMilliseconds;
    protected int connectionCount = 0;
    private long idleTimeoutMilliseconds;
    private IdleReleaser idleReleaser;
    protected Timer timer = PoolIdleReleaserTimer.getTimer();
    protected int maxSize = 0;
    protected int minSize = 0;
    protected int shrinkLater = 0;
    protected volatile boolean destroyed = false;

    public AbstractSinglePoolConnectionInterceptor(final ConnectionInterceptor next,
                                                   int maxSize,
                                                   int minSize,
                                                   int blockingTimeoutMilliseconds,
                                                   int idleTimeoutMinutes) {
        this.next = next;
        this.maxSize = maxSize;
        this.minSize = minSize;
        this.blockingTimeoutMilliseconds = blockingTimeoutMilliseconds;
        setIdleTimeoutMinutes(idleTimeoutMinutes);
        permits = new Semaphore(maxSize, true);
    }

    public void getConnection(ConnectionInfo connectionInfo) throws ResourceException {
        if (connectionInfo.getManagedConnectionInfo().getManagedConnection() != null) {
            if (log.isTraceEnabled()) {
                log.trace("using already assigned connection " + connectionInfo.getConnectionHandle() + " for managed connection " + connectionInfo.getManagedConnectionInfo().getManagedConnection() + " to pool " + this);
            }
            return;
        }
        try {
            resizeLock.readLock().lock();
            try {
                if (permits.tryAcquire(blockingTimeoutMilliseconds, TimeUnit.MILLISECONDS)) {
                    internalGetConnection(connectionInfo);
                } else {
                    throw new ResourceException("No ManagedConnections available "
                            + "within configured blocking timeout ( "
                            + blockingTimeoutMilliseconds
                            + " [ms] ) for pool " + this);

                }
            } finally {
                resizeLock.readLock().unlock();
            }

        } catch (InterruptedException ie) {
            throw new ResourceException("Interrupted while requesting permit.", ie);
        } // end of try-catch
    }

    protected abstract void internalGetConnection(ConnectionInfo connectionInfo) throws ResourceException;

    public void returnConnection(ConnectionInfo connectionInfo,
                                 ConnectionReturnAction connectionReturnAction) {
        if (log.isTraceEnabled()) {
            log.trace("returning connection " + connectionInfo.getConnectionHandle() + " for MCI " + connectionInfo.getManagedConnectionInfo() + " and MC " + connectionInfo.getManagedConnectionInfo().getManagedConnection() + " to pool " + this);
        }

        // not strictly synchronized with destroy(), but pooled operations in internalReturn() are...
        if (destroyed) {
            try {
                connectionInfo.getManagedConnectionInfo().getManagedConnection().destroy();
            } catch (ResourceException re) {
                // empty
            }
            return;
        }

        resizeLock.readLock().lock();
        try {
            ManagedConnectionInfo mci = connectionInfo.getManagedConnectionInfo();
            if (connectionReturnAction == ConnectionReturnAction.RETURN_HANDLE && mci.hasConnectionHandles()) {
                if (log.isTraceEnabled()) {
                    log.trace("Return request at pool with connection handles! " + connectionInfo.getConnectionHandle() + " for MCI " + connectionInfo.getManagedConnectionInfo() + " and MC " + connectionInfo.getManagedConnectionInfo().getManagedConnection() + " to pool " + this, new Exception("Stack trace"));
                }
                return;
            }

            boolean wasInPool = internalReturn(connectionInfo, connectionReturnAction);

            if (!wasInPool) {
                permits.release();
            }
        } finally {
            resizeLock.readLock().unlock();
        }
    }

    protected abstract boolean internalReturn(ConnectionInfo connectionInfo, ConnectionReturnAction connectionReturnAction);

    protected abstract void internalDestroy();

    // Cancel the IdleReleaser TimerTask (fixes memory leak) and clean up the pool
    public void destroy() {
        destroyed = true;
        if (idleReleaser != null)
            idleReleaser.cancel();
        internalDestroy();
        next.destroy();
    }

    public int getPartitionCount() {
        return 1;
    }

    public abstract int getPartitionMaxSize();

    public void setPartitionMaxSize(int newMaxSize) throws InterruptedException {
        if (newMaxSize <= 0) {
            throw new IllegalArgumentException("Max size must be positive, not " + newMaxSize);
        }
        if (newMaxSize != getPartitionMaxSize()) {
            resizeLock.writeLock().lock();
            try {
                ResizeInfo resizeInfo = new ResizeInfo(this.minSize, permits.availablePermits(), connectionCount, newMaxSize);
                this.shrinkLater = resizeInfo.getShrinkLater();

                permits = new Semaphore(newMaxSize, true);
                //pre-acquire permits for the existing checked out connections that will not be closed when they are returned.
                for (int i = 0; i < resizeInfo.getTransferCheckedOut(); i++) {
                    permits.acquire();
                }
                //transfer connections we are going to keep
                transferConnections(newMaxSize, resizeInfo.getShrinkNow());
                this.minSize = resizeInfo.getNewMinSize();
            } finally {
                resizeLock.writeLock().unlock();
            }
        }
    }


    static final class ResizeInfo {

        private final int newMinSize;
        private final int shrinkNow;
        private final int shrinkLater;
        private final int transferCheckedOut;

        ResizeInfo(final int oldMinSize, final int oldPermitsAvailable, final int oldConnectionCount, final int newMaxSize) {
            final int checkedOut = oldConnectionCount - oldPermitsAvailable;
            int shrinkLater = checkedOut - newMaxSize;
            if (shrinkLater < 0) {
                shrinkLater = 0;
            }
            this.shrinkLater = shrinkLater;
            int shrinkNow = oldConnectionCount - newMaxSize - shrinkLater;
            if (shrinkNow < 0) {
                shrinkNow = 0;
            }
            this.shrinkNow = shrinkNow;
            if (newMaxSize >= oldMinSize) {
                newMinSize = oldMinSize;
            } else {
                newMinSize = newMaxSize;
            }
            this.transferCheckedOut = checkedOut - shrinkLater;
        }

        public int getNewMinSize() {
            return newMinSize;
        }

        public int getShrinkNow() {
            return shrinkNow;
        }

        public int getShrinkLater() {
            return shrinkLater;
        }

        public int getTransferCheckedOut() {
            return transferCheckedOut;
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
            idleReleaser = new IdleReleaser(this);
            timer.schedule(idleReleaser, this.idleTimeoutMilliseconds, this.idleTimeoutMilliseconds);
        }
    }

    protected abstract void getExpiredManagedConnectionInfos(long threshold, ArrayList killList);

    protected abstract boolean addToPool(ManagedConnectionInfo mci);

    // static class to permit chain of strong references from preventing ClassLoaders
    // from being GC'ed.
    private static class IdleReleaser extends TimerTask {
        private AbstractSinglePoolConnectionInterceptor parent;

        private IdleReleaser(AbstractSinglePoolConnectionInterceptor parent) {
            this.parent = parent;
        }

        public boolean cancel() {
            this.parent = null;
            return super.cancel();
        }

        public void run() {
            // protect against interceptor being set to null mid-execution
            AbstractSinglePoolConnectionInterceptor interceptor = parent;
            if (interceptor == null)
                return;

            interceptor.resizeLock.readLock().lock();
            try {
                long threshold = System.currentTimeMillis() - interceptor.idleTimeoutMilliseconds;
                ArrayList killList = new ArrayList(interceptor.getPartitionMaxSize());
                interceptor.getExpiredManagedConnectionInfos(threshold, killList);
                for (Iterator i = killList.iterator(); i.hasNext();) {
                    ManagedConnectionInfo managedConnectionInfo = (ManagedConnectionInfo) i.next();
                    ConnectionInfo killInfo = new ConnectionInfo(managedConnectionInfo);
                    interceptor.internalReturn(killInfo, ConnectionReturnAction.DESTROY);
                }
                interceptor.permits.release(killList.size());
            } catch (Throwable t) {
                log.error("Error occurred during execution of ExpirationMonitor TimerTask", t);
            } finally {
                interceptor.resizeLock.readLock().unlock();
            }
        }

    }

    // Currently only a short-lived (10 millisecond) task.
    // So, FillTask, unlike IdleReleaser, shouldn't cause GC problems.
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
            resizeLock.readLock().lock();
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
                    boolean added = addToPool(mci);
                    if (!added) {
                        internalReturn(ci, ConnectionReturnAction.DESTROY);
                        return;
                    }
                }
            } catch (Throwable t) {
                log.error("FillTask encountered error in run method", t);
            } finally {
                resizeLock.readLock().unlock();
            }
        }

    }
}
