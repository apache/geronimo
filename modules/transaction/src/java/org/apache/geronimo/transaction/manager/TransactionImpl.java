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

package org.apache.geronimo.transaction.manager;

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
 * @version $Rev$ $Date$
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
    private long logMark;

    TransactionImpl(XidFactory xidFactory, TransactionLog txnLog) throws SystemException {
        this(xidFactory.createXid(), xidFactory, txnLog);
    }

    TransactionImpl(Xid xid, XidFactory xidFactory, TransactionLog txnLog) throws SystemException {
        this.xidFactory = xidFactory;
        this.txnLog = txnLog;
        this.xid = xid;
        try {
            txnLog.begin(xid);
        } catch (LogException e) {
            status = Status.STATUS_MARKED_ROLLBACK;
            SystemException ex = new SystemException("Error logging begin; transaction marked for roll back)");
            ex.initCause(e);
            throw ex;
        }
        status = Status.STATUS_ACTIVE;
    }

    //reconstruct a tx for an external tx found in recovery
    public TransactionImpl(Xid xid, TransactionLog txLog) {
        this.xidFactory = null;
        this.txnLog = txLog;
        this.xid = xid;
        status = Status.STATUS_PREPARED;
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
                TransactionBranch manager = (TransactionBranch) i.next();
                boolean sameRM;
                //if the xares is already known, we must be resuming after a suspend.
                if (xaRes == manager.getCommitter()) {
                    xaRes.start(manager.getBranchId(), XAResource.TMRESUME);
                    return true;
                }
                //Otherwise, see if this is a new xares for the same resource manager
                try {
                    sameRM = xaRes.isSameRM(manager.getCommitter());
                } catch (XAException e) {
                    log.warn("Unexpected error checking for same RM", e);
                    continue;
                }
                if (sameRM) {
                    xaRes.start(manager.getBranchId(), XAResource.TMJOIN);
                    xaResources.put(xaRes, manager);
                    return true;
                }
            }

            Xid branchId = xidFactory.createBranch(xid, resourceManagers.size() + 1);
            xaRes.start(branchId, XAResource.TMNOFLAGS);
            addBranchXid(xaRes,  branchId);
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
        TransactionBranch manager = (TransactionBranch) xaResources.remove(xaRes);
        if (manager == null) {
            throw new IllegalStateException("Resource not enlisted");
        }
        try {
            xaRes.end(manager.getBranchId(), flag);
            return true;
        } catch (XAException e) {
            log.warn("Unable to delist XAResource " + xaRes, e);
            return false;
        }
    }

    //Transaction method, does 2pc
    public void commit() throws HeuristicMixedException, HeuristicRollbackException, RollbackException, SecurityException, SystemException {
        beforePrepare();

        try {
            if (status == Status.STATUS_MARKED_ROLLBACK) {
                rollbackResources(resourceManagers);
                throw new RollbackException("Unable to commit");
            }
            synchronized (this) {
                if (status == Status.STATUS_ACTIVE) {
                    if (this.resourceManagers.size() == 0) {
                        // nothing to commit
                        status = Status.STATUS_COMMITTED;
                    } else if (this.resourceManagers.size() == 1) {
                        // one-phase commit decision
                        status = Status.STATUS_COMMITTING;
                    } else {
                        // start prepare part of two-phase
                        status = Status.STATUS_PREPARING;
                    }
                }
                // resourceManagers is now immutable
            }


            // no-phase
            if (resourceManagers.size() == 0) {
                synchronized (this) {
                    status = Status.STATUS_COMMITTED;
                }
                return;
            }

            // one-phase
            if (resourceManagers.size() == 1) {
                TransactionBranch manager = (TransactionBranch) resourceManagers.getFirst();
                try {
                    manager.getCommitter().commit(manager.getBranchId(), true);
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
            boolean willCommit = internalPrepare();

            // notify the RMs
            if (willCommit) {
                commitResources(resourceManagers);
            } else {
                rollbackResources(resourceManagers);
                throw new RollbackException("Unable to commit");
            }
        } finally {
            afterCompletion();
            synchronized (this) {
                status = Status.STATUS_NO_TRANSACTION;
            }
        }
    }

    //Used from XATerminator for first phase in a remotely controlled tx.
    int prepare() throws SystemException, RollbackException {
        beforePrepare();
        int result = XAResource.XA_RDONLY;
        try {
            LinkedList rms;
            synchronized (this) {
                if (status == Status.STATUS_ACTIVE) {
                    if (resourceManagers.size() == 0) {
                        // nothing to commit
                        status = Status.STATUS_COMMITTED;
                        return result;
                    } else {
                        // start prepare part of two-phase
                        status = Status.STATUS_PREPARING;
                    }
                }
                // resourceManagers is now immutable
                rms = resourceManagers;
            }

            boolean willCommit = internalPrepare();

            // notify the RMs
            if (willCommit) {
                if (!rms.isEmpty()) {
                    result = XAResource.XA_OK;
                }
            } else {
                rollbackResources(rms);
                throw new RollbackException("Unable to commit");
            }
        } finally {
            if (result == XAResource.XA_RDONLY) {
                afterCompletion();
                synchronized (this) {
                    status = Status.STATUS_NO_TRANSACTION;
                }
            }
        }
        return result;
    }

    //used from XATerminator for commit phase of non-readonly remotely controlled tx.
    void preparedCommit() throws SystemException {
        try {
            commitResources(resourceManagers);
        } finally {
            afterCompletion();
            synchronized (this) {
                status = Status.STATUS_NO_TRANSACTION;
            }
        }
    }

    //helper method used by Transaction.commit and XATerminator prepare.
    private void beforePrepare() {
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
    }


    //helper method used by Transaction.commit and XATerminator prepare.
    private boolean internalPrepare() throws SystemException {
    	
        for (Iterator rms = resourceManagers.iterator(); rms.hasNext();) {
            synchronized (this) {
                if (status != Status.STATUS_PREPARING) {
                    // we were marked for rollback
                    break;
                }
            }
            TransactionBranch manager = (TransactionBranch) rms.next();
            try {
                int vote = manager.getCommitter().prepare(manager.getBranchId());
                if (vote == XAResource.XA_RDONLY) {
                    // we don't need to consider this RM any more
                    rms.remove();
                }
            } catch (XAException e) {
                synchronized (this) {
                    status = Status.STATUS_MARKED_ROLLBACK;
                    //TODO document why this is true from the spec.
                    //XAException during prepare means we can assume resource is rolled back.
                    rms.remove();
                    break;
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
        if (willCommit && !resourceManagers.isEmpty()) {
            try {
                logMark = txnLog.prepare(xid, resourceManagers);
            } catch (LogException e) {
                try {
                    rollbackResources(resourceManagers);
                } catch (Exception se) {
                    log.error("Unable to rollback after failure to log prepare", se.getCause());
                }
                throw (SystemException) new SystemException("Error logging prepare; transaction was rolled back)").initCause(e);
            }
        }
        return willCommit;
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
            rollbackResources(rms);
            try {
                txnLog.rollback(xid, logMark);
            } catch (LogException e) {
                try {
                    rollbackResources(rms);
                } catch (Exception se) {
                    log.error("Unable to rollback after failure to log decision", se.getCause());
                }
                throw (SystemException) new SystemException("Error logging rollback").initCause(e);
            }
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
            TransactionBranch manager;
            int flags;
            synchronized (this) {
                Set entrySet = xaResources.entrySet();
                if (entrySet.isEmpty()) {
                    return;
                }
                Map.Entry entry = (Map.Entry) entrySet.iterator().next();
                xaRes = (XAResource) entry.getKey();
                manager = (TransactionBranch) entry.getValue();
                flags = (status == Status.STATUS_MARKED_ROLLBACK) ? XAResource.TMFAIL : XAResource.TMSUCCESS;
                xaResources.remove(xaRes);
            }
            try {
                xaRes.end(manager.getBranchId(), flags);
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
            TransactionBranch manager = (TransactionBranch) i.next();
            try {
                manager.getCommitter().rollback(manager.getBranchId());
            } catch (XAException e) {
                log.error("Unexpected exception rolling back " + manager.getCommitter() + "; continuing with rollback", e);
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
            TransactionBranch manager = (TransactionBranch) i.next();
            try {
                manager.getCommitter().commit(manager.getBranchId(), false);
            } catch (XAException e) {
                log.error("Unexpected exception committing" + manager.getCommitter() + "; continuing to commit other RMs", e);
                if (cause == null) {
                    cause = new SystemException(e.errorCode);
                }
                continue;
            }
        }
        try {
            txnLog.commit(xid, logMark);
        } catch (LogException e) {
            log.error("Unexpected exception logging commit completion for xid " + xid, e);
            throw (SystemException)new SystemException("Unexpected error logging commit completion for xid " + xid).initCause(e);
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

    public void addBranchXid(XAResource xaRes, Xid branchId) {
        TransactionBranch manager = new TransactionBranch(xaRes, branchId);
        resourceManagers.add(manager);
        xaResources.put(xaRes, manager);
    }

    private static class TransactionBranch implements TransactionBranchInfo {
        private final NamedXAResource committer;
        private final Xid branchId;

        public TransactionBranch(XAResource xaRes, Xid branchId) {
            committer = (NamedXAResource)xaRes;
            this.branchId = branchId;
        }

        public NamedXAResource getCommitter() {
            return committer;
        }

        public Xid getBranchId() {
            return branchId;
        }

        public String getResourceName() {
            return committer.getName();
        }

        public Xid getBranchXid() {
            return branchId;
        }
    }


}
