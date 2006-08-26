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
 * @version $Rev$ $Date$
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

        // not strictly synchronized with destroy(), but pooled operations in internalReturn() are...
        if (destroyed) {
            try {
                connectionInfo.getManagedConnectionInfo().getManagedConnection().destroy();
            } catch (ResourceException re) {
            }
            return;
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
            resizeLock.writeLock().acquire();
            try {
                ResizeInfo resizeInfo = new ResizeInfo(this.minSize, (int)permits.permits(), connectionCount, newMaxSize);
                this.shrinkLater = resizeInfo.getShrinkLater();

                permits = new FIFOSemaphore(newMaxSize);
                //pre-acquire permits for the existing checked out connections that will not be closed when they are returned.
                for (int i = 0; i < resizeInfo.getTransferCheckedOut(); i++) {
                    permits.acquire();
                }
                //transfer connections we are going to keep
                transferConnections(newMaxSize, resizeInfo.getShrinkNow());
                this.minSize = resizeInfo.getNewMinSize();
            } finally {
                resizeLock.writeLock().release();
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
            try {
                interceptor.resizeLock.readLock().acquire();
            } catch (InterruptedException e) {
                return;
            }
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
            } finally {
                interceptor.resizeLock.readLock().release();
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
