/* ====================================================================
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 2003 The Apache Software Foundation.  All rights
 * reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. The end-user documentation included with the redistribution,
 *    if any, must include the following acknowledgment:
 *       "This product includes software developed by the
 *        Apache Software Foundation (http://www.apache.org/)."
 *    Alternately, this acknowledgment may appear in the software itself,
 *    if and wherever such third-party acknowledgments normally appear.
 *
 * 4. The names "Apache" and "Apache Software Foundation" and
 *    "Apache Geronimo" must not be used to endorse or promote products
 *    derived from this software without prior written permission. For
 *    written permission, please contact apache@apache.org.
 *
 * 5. Products derived from this software may not be called "Apache",
 *    "Apache Geronimo", nor may "Apache" appear in their name, without
 *    prior written permission of the Apache Software Foundation.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE APACHE SOFTWARE FOUNDATION OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 *
 * ====================================================================
 */
package org.apache.geronimo.transaction.manager;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.transaction.HeuristicMixedException;
import javax.transaction.HeuristicRollbackException;
import javax.transaction.RollbackException;
import javax.transaction.Status;
import javax.transaction.Synchronization;
import javax.transaction.SystemException;
import javax.transaction.Transaction;
import javax.transaction.xa.XAException;
import javax.transaction.xa.XAResource;
import javax.transaction.xa.Xid;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Basic local transaction with support for multiple resources.
 *
 * @version $Revision: 1.1 $ $Date: 2003/09/29 00:32:40 $
 */
public class TransactionImpl implements Transaction {
    private static final Log log = LogFactory.getLog("Transaction");

    private final XidFactory xidFactory;
    private final Xid xid;
    private final TransactionLog txnLog;
    private int status = Status.STATUS_NO_TRANSACTION;
    private List syncList = new ArrayList(5);
    private LinkedList resourceManagers = new LinkedList();
    private Map xaResources = new HashMap(3);

    TransactionImpl(XidFactory xidFactory, TransactionLog txnLog) throws SystemException {
        this.xidFactory = xidFactory;
        this.txnLog = txnLog;
        this.xid = xidFactory.createXid();
        try {
            txnLog.begin(xid);
        } catch (IOException e) {
            status = Status.STATUS_MARKED_ROLLBACK;
            SystemException ex = new SystemException("Error logging begin; transaction marked for roll back)");
            ex.initCause(e);
            throw ex;
        }
        status = Status.STATUS_ACTIVE;
    }

    public synchronized int getStatus() throws SystemException {
        return status;
    }

    public synchronized void setRollbackOnly() throws IllegalStateException, SystemException {
        switch (status) {
        case Status.STATUS_ACTIVE:
        case Status.STATUS_PREPARING:
            status = Status.STATUS_MARKED_ROLLBACK;
            break;
        case Status.STATUS_MARKED_ROLLBACK:
        case Status.STATUS_ROLLING_BACK:
            // nothing to do
            break;
        default:
            throw new IllegalStateException("Cannot set rollback only, status is " + getStateString(status));
        }
    }

    public synchronized void registerSynchronization(Synchronization synch) throws IllegalStateException, RollbackException, SystemException {
        if (synch == null) {
            throw new IllegalArgumentException("Synchronization is null");
        }
        switch (status) {
        case Status.STATUS_ACTIVE:
        case Status.STATUS_PREPARING:
            break;
        case Status.STATUS_MARKED_ROLLBACK:
            throw new RollbackException("Transaction is marked for rollback");
        default:
            throw new IllegalStateException("Status is " + getStateString(status));
        }
        syncList.add(synch);
    }

    public synchronized boolean enlistResource(XAResource xaRes) throws IllegalStateException, RollbackException, SystemException {
        if (xaRes == null) {
            throw new IllegalArgumentException("XAResource is null");
        }
        switch (status) {
        case Status.STATUS_ACTIVE:
            break;
        case Status.STATUS_MARKED_ROLLBACK:
            throw new RollbackException("Transaction is marked for rollback");
        default:
            throw new IllegalStateException("Status is " + getStateString(status));
        }

        try {
            for (Iterator i = resourceManagers.iterator(); i.hasNext();) {
                ResourceManager manager = (ResourceManager) i.next();
                boolean sameRM;
                try {
                    // @todo should we check if xaRes.equals(manager.committer) ?
                    sameRM = xaRes.isSameRM(manager.committer);
                } catch (XAException e) {
                    log.warn("Unexpected error checking for same RM", e);
                    continue;
                }
                if (sameRM) {
                    xaRes.start(manager.branchId, XAResource.TMJOIN);
                    xaResources.put(xaRes, manager);
                    return true;
                }
            }

            Xid branchId = xidFactory.createBranch(xid, resourceManagers.size() + 1);
            xaRes.start(branchId, XAResource.TMNOFLAGS);
            ResourceManager manager = new ResourceManager(xaRes, branchId);
            resourceManagers.add(manager);
            xaResources.put(xaRes, manager);
            return true;
        } catch (XAException e) {
            log.warn("Unable to enlist XAResource " + xaRes, e);
            return false;
        }
    }

    public synchronized boolean delistResource(XAResource xaRes, int flag) throws IllegalStateException, SystemException {
        if (xaRes == null) {
            throw new IllegalArgumentException("XAResource is null");
        }
        switch (status) {
        case Status.STATUS_ACTIVE:
        case Status.STATUS_MARKED_ROLLBACK:
            break;
        default:
            throw new IllegalStateException("Status is " + getStateString(status));
        }
        ResourceManager manager = (ResourceManager) xaResources.remove(xaRes);
        if (manager == null) {
            throw new IllegalStateException("Resource not enlisted");
        }
        try {
            xaRes.end(manager.branchId, flag);
            return true;
        } catch (XAException e) {
            log.warn("Unable to delist XAResource " + xaRes, e);
            return false;
        }
    }

    public void commit() throws HeuristicMixedException, HeuristicRollbackException, RollbackException, SecurityException, SystemException {
        synchronized (this) {
            switch (status) {
            case Status.STATUS_ACTIVE:
            case Status.STATUS_MARKED_ROLLBACK:
                break;
            default:
                throw new IllegalStateException("Status is " + getStateString(status));
            }
        }

        beforeCompletion();
        endResources();
        try {
            LinkedList rms;
            synchronized (this) {
                if (status == Status.STATUS_ACTIVE) {
                    if (resourceManagers.size() == 0) {
                        // nothing to commit
                        status = Status.STATUS_COMMITTED;
                    } else if (resourceManagers.size() == 1) {
                        // one-phase commit decision
                        status = Status.STATUS_COMMITTING;
                    } else {
                        // start prepare part of two-phase
                        status = Status.STATUS_PREPARING;
                    }
                }
                // resourceManagers is now immutable
                rms = resourceManagers;
            }

            // one-phase
            if (rms.size() == 1) {
                ResourceManager manager = (ResourceManager) rms.getFirst();
                try {
                    manager.committer.commit(manager.branchId, true);
                    synchronized (this) {
                        status = Status.STATUS_COMMITTED;
                    }
                    return;
                } catch (XAException e) {
                    synchronized (this) {
                        status = Status.STATUS_ROLLEDBACK;
                    }
                    RollbackException ex = new RollbackException("Error during one-phase commit");
                    ex.initCause(e);
                    throw ex;
                }
            }

            // two-phase
            try {
                txnLog.prepare(xid);
            } catch (IOException e) {
                try {
                    rollbackResources(rms);
                } catch (Exception se) {
                    log.error("Unable to rollback after failure to log prepare", se.getCause());
                }
                SystemException ex = new SystemException("Error logging prepare; transaction was rolled back)");
                ex.initCause(e);
                throw ex;
            }
            for (Iterator i = rms.iterator(); i.hasNext();) {
                synchronized (this) {
                    if (status != Status.STATUS_PREPARING) {
                        // we were marked for rollback
                        break;
                    }
                }
                ResourceManager manager = (ResourceManager) i.next();
                try {
                    int vote = manager.committer.prepare(manager.branchId);
                    if (vote == XAResource.XA_RDONLY) {
                        // we don't need to consider this RM any more
                        i.remove();
                    }
                } catch (XAException e) {
                    synchronized (this) {
                        status = Status.STATUS_MARKED_ROLLBACK;
                    }
                }
            }

            // decision time...
            boolean willCommit;
            synchronized (this) {
                willCommit = (status != Status.STATUS_MARKED_ROLLBACK);
                if (willCommit) {
                    status = Status.STATUS_PREPARED;
                }
            }

            // log our decision
            try {
                if (willCommit) {
                    txnLog.commit(xid);
                } else {
                    txnLog.rollback(xid);
                }
            } catch (IOException e) {
                try {
                    rollbackResources(rms);
                } catch (Exception se) {
                    log.error("Unable to rollback after failure to log decision", se.getCause());
                }
                SystemException ex = new SystemException("Error logging decision (outcome is unknown)");
                ex.initCause(e);
                throw ex;
            }

            // notify the RMs
            if (willCommit) {
                commitResources(rms);
            } else {
                rollbackResources(rms);
                throw new RollbackException("Unable to commit");
            }
        } finally {
            afterCompletion();
            synchronized (this) {
                status = Status.STATUS_NO_TRANSACTION;
            }
        }
    }

    public void rollback() throws IllegalStateException, SystemException {
        List rms;
        synchronized (this) {
            switch (status) {
            case Status.STATUS_ACTIVE:
                status = Status.STATUS_MARKED_ROLLBACK;
                break;
            case Status.STATUS_MARKED_ROLLBACK:
                break;
            default:
                throw new IllegalStateException("Status is " + getStateString(status));
            }
            rms = resourceManagers;
        }

        beforeCompletion();
        endResources();
        try {
            try {
                txnLog.rollback(xid);
            } catch (IOException e) {
                try {
                    rollbackResources(rms);
                } catch (Exception se) {
                    log.error("Unable to rollback after failure to log decision", se.getCause());
                }
                SystemException ex = new SystemException("Error logging rollback");
                ex.initCause(e);
                throw ex;
            }
            rollbackResources(rms);
        } finally {
            afterCompletion();
            synchronized (this) {
                status = Status.STATUS_NO_TRANSACTION;
            }
        }
    }

    private void beforeCompletion() {
        int i = 0;
        while (true) {
            Synchronization synch;
            synchronized (this) {
                if (i == syncList.size()) {
                    return;
                }
                synch = (Synchronization) syncList.get(i++);
            }
            try {
                synch.beforeCompletion();
            } catch (Exception e) {
                log.warn("Unexpected exception from beforeCompletion; transaction will roll back", e);
                synchronized (this) {
                    status = Status.STATUS_MARKED_ROLLBACK;
                }
            }
        }
    }

    private void afterCompletion() {
        // this does not synchronize because nothing can modify our state at this time
        for (Iterator i = syncList.iterator(); i.hasNext();) {
            Synchronization synch = (Synchronization) i.next();
            try {
                synch.afterCompletion(status);
            } catch (Exception e) {
                log.warn("Unexpected exception from afterCompletion; continuing", e);
                continue;
            }
        }
    }

    private void endResources() {
        while (true) {
            XAResource xaRes;
            ResourceManager manager;
            int flags;
            synchronized (this) {
                Set entrySet = xaResources.entrySet();
                if (entrySet.isEmpty()) {
                    return;
                }
                Map.Entry entry = (Map.Entry) entrySet.iterator().next();
                xaRes = (XAResource) entry.getKey();
                manager = (ResourceManager) entry.getValue();
                flags = (status == Status.STATUS_MARKED_ROLLBACK) ? XAResource.TMFAIL : XAResource.TMSUCCESS;
                xaResources.remove(xaRes);
            }
            try {
                xaRes.end(manager.branchId, flags);
            } catch (XAException e) {
                log.warn("Error ending association for XAResource " + xaRes + "; transaction will roll back", e);
                synchronized (this) {
                    status = Status.STATUS_MARKED_ROLLBACK;
                }
            }
        }
    }

    private void rollbackResources(List rms) throws SystemException {
        SystemException cause = null;
        synchronized (this) {
            status = Status.STATUS_ROLLING_BACK;
        }
        for (Iterator i = rms.iterator(); i.hasNext();) {
            ResourceManager manager = (ResourceManager) i.next();
            try {
                manager.committer.rollback(manager.branchId);
            } catch (XAException e) {
                log.error("Unexpected exception rolling back " + manager.committer + "; continuing with rollback", e);
                if (cause == null) {
                    cause = new SystemException(e.errorCode);
                }
                continue;
            }
        }
        synchronized (this) {
            status = Status.STATUS_ROLLEDBACK;
        }
        if (cause != null) {
            throw cause;
        }
    }

    private void commitResources(List rms) throws SystemException {
        SystemException cause = null;
        synchronized (this) {
            status = Status.STATUS_COMMITTING;
        }
        for (Iterator i = rms.iterator(); i.hasNext();) {
            ResourceManager manager = (ResourceManager) i.next();
            try {
                manager.committer.commit(manager.branchId, false);
            } catch (XAException e) {
                log.error("Unexpected exception committing" + manager.committer + "; continuing to commit other RMs", e);
                if (cause == null) {
                    cause = new SystemException(e.errorCode);
                }
                continue;
            }
        }
        synchronized (this) {
            status = Status.STATUS_COMMITTED;
        }
        if (cause != null) {
            throw cause;
        }
    }

    private static String getStateString(int status) {
        switch (status) {
        case Status.STATUS_ACTIVE:
            return "STATUS_ACTIVE";
        case Status.STATUS_PREPARING:
            return "STATUS_PREPARING";
        case Status.STATUS_PREPARED:
            return "STATUS_PREPARED";
        case Status.STATUS_MARKED_ROLLBACK:
            return "STATUS_MARKED_ROLLBACK";
        case Status.STATUS_ROLLING_BACK:
            return "STATUS_ROLLING_BACK";
        case Status.STATUS_COMMITTING:
            return "STATUS_COMMITTING";
        case Status.STATUS_COMMITTED:
            return "STATUS_COMMITTED";
        case Status.STATUS_ROLLEDBACK:
            return "STATUS_ROLLEDBACK";
        case Status.STATUS_NO_TRANSACTION:
            return "STATUS_NO_TRANSACTION";
        case Status.STATUS_UNKNOWN:
            return "STATUS_UNKNOWN";
        default:
            throw new AssertionError();
        }
    }

    public boolean equals(Object obj) {
        if (obj instanceof TransactionImpl) {
            TransactionImpl other = (TransactionImpl) obj;
            return xid.equals(other.xid);
        } else {
            return false;
        }
    }

    private static class ResourceManager {
        private final XAResource committer;
        private final Xid branchId;

        public ResourceManager(XAResource xaRes, Xid branchId) {
            committer = xaRes;
            this.branchId = branchId;
        }
    }
}
