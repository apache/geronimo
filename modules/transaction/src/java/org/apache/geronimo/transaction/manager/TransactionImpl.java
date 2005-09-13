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

package org.apache.geronimo.transaction.manager;

import java.util.ArrayList;
import java.util.IdentityHashMap;
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
    private final long timeout;
    private final List syncList = new ArrayList(5);
    private final LinkedList resourceManagers = new LinkedList();
    private final IdentityHashMap activeXaResources = new IdentityHashMap(3);
    private final IdentityHashMap suspendedXaResources = new IdentityHashMap(3);
    private int status = Status.STATUS_NO_TRANSACTION;
    private Object logMark;

    TransactionImpl(XidFactory xidFactory, TransactionLog txnLog, long transactionTimeoutMilliseconds) throws SystemException {
        this(xidFactory.createXid(), xidFactory, txnLog, transactionTimeoutMilliseconds);
    }

    TransactionImpl(Xid xid, XidFactory xidFactory, TransactionLog txnLog, long transactionTimeoutMilliseconds) throws SystemException {
        this.xidFactory = xidFactory;
        this.txnLog = txnLog;
        this.xid = xid;
        this.timeout = transactionTimeoutMilliseconds + TransactionTimer.getCurrentTime();
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
        //TODO is this a good idea?
        this.timeout = Long.MAX_VALUE;
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

        if (activeXaResources.containsKey(xaRes)) {
            throw new IllegalStateException("xaresource: " + xaRes + " is already enlisted!");
        }

        try {
            TransactionBranch manager = (TransactionBranch) suspendedXaResources.remove(xaRes);
            if (manager != null) {
                //we know about this one, it was suspended
                xaRes.start(manager.getBranchId(), XAResource.TMRESUME);
                activeXaResources.put(xaRes, manager);
                return true;
            }
            //it is not suspended.
            for (Iterator i = resourceManagers.iterator(); i.hasNext();) {
                manager = (TransactionBranch) i.next();
                boolean sameRM;
                //if the xares is already known, we must be resuming after a suspend.
                if (xaRes == manager.getCommitter()) {
                    throw new IllegalStateException("xaRes " + xaRes + " is a committer but is not active or suspended");
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
                    activeXaResources.put(xaRes, manager);
                    return true;
                }
            }
            //we know nothing about this XAResource or resource manager
            Xid branchId = xidFactory.createBranch(xid, resourceManagers.size() + 1);
            xaRes.start(branchId, XAResource.TMNOFLAGS);
            activeXaResources.put(xaRes, addBranchXid(xaRes,  branchId));
            return true;
        } catch (XAException e) {
            log.warn("Unable to enlist XAResource " + xaRes, e);
            return false;
        }
    }

    public synchronized boolean delistResource(XAResource xaRes, int flag) throws IllegalStateException, SystemException {
        if (!(flag == XAResource.TMFAIL || flag == XAResource.TMSUCCESS || flag == XAResource.TMSUSPEND)) {
            throw new IllegalStateException("invalid flag for delistResource: " + flag);
        }
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
        TransactionBranch manager = (TransactionBranch) activeXaResources.remove(xaRes);
        if (manager == null) {
            if (flag == XAResource.TMSUSPEND) {
                throw new IllegalStateException("trying to suspend an inactive xaresource: " + xaRes);
            }
            //not active, and we are not trying to suspend.  We must be ending tx.
            manager = (TransactionBranch) suspendedXaResources.remove(xaRes);
            if (manager == null) {
                throw new IllegalStateException("Resource not known to transaction: " + xaRes);
            }
        }

        try {
            xaRes.end(manager.getBranchId(), flag);
            if (flag == XAResource.TMSUSPEND) {
                suspendedXaResources.put(xaRes, manager);
            }
            return true;
        } catch (XAException e) {
            log.warn("Unable to delist XAResource " + xaRes + ", error code: " + e.errorCode, e);
            return false;
        }
    }

    //Transaction method, does 2pc
    public void commit() throws HeuristicMixedException, HeuristicRollbackException, RollbackException, SecurityException, SystemException {
        beforePrepare();

        try {
                      boolean timedout = false;
                      if (TransactionTimer.getCurrentTime() > timeout)
                      {
                          status = Status.STATUS_MARKED_ROLLBACK;
                          timedout = true;
                      }

            if (status == Status.STATUS_MARKED_ROLLBACK) {
                rollbackResources(resourceManagers);
                              if(timedout)
                              {
                                  throw new RollbackException("Transaction timout");
                              }
                              else
                              {
                                  throw new RollbackException("Unable to commit");
                              }
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
                    throw (RollbackException) new RollbackException("Error during one-phase commit").initCause(e);
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
            //only write rollback record if we have already written prepare record.
            if (logMark != null) {
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
        endResources(activeXaResources);
        endResources(suspendedXaResources);
    }

    private void endResources(IdentityHashMap resourceMap) {
        while (true) {
            XAResource xaRes;
            TransactionBranch manager;
            int flags;
            synchronized (this) {
                Set entrySet = resourceMap.entrySet();
                if (entrySet.isEmpty()) {
                    return;
                }
                Map.Entry entry = (Map.Entry) entrySet.iterator().next();
                xaRes = (XAResource) entry.getKey();
                manager = (TransactionBranch) entry.getValue();
                flags = (status == Status.STATUS_MARKED_ROLLBACK) ? XAResource.TMFAIL : XAResource.TMSUCCESS;
                resourceMap.remove(xaRes);
            }
            try {
                xaRes.end(manager.getBranchId(), flags);
            } catch (XAException e) {
                log.warn("Error ending association for XAResource " + xaRes + "; transaction will roll back. XA error code: " + e.errorCode, e);
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

    //when used from recovery, do not add manager to active or suspended resource maps.
    // The xaresources have already been ended with TMSUCCESS.
    public TransactionBranch addBranchXid(XAResource xaRes, Xid branchId) {
        TransactionBranch manager = new TransactionBranch(xaRes, branchId);
        resourceManagers.add(manager);
        return manager;
    }

    private static class TransactionBranch implements TransactionBranchInfo {
        private final XAResource committer;
        private final Xid branchId;

        public TransactionBranch(XAResource xaRes, Xid branchId) {
            committer = xaRes;
            this.branchId = branchId;
        }

        public XAResource getCommitter() {
            return committer;
        }

        public Xid getBranchId() {
            return branchId;
        }

        public String getResourceName() {
            if (committer instanceof NamedXAResource) {
            return ((NamedXAResource)committer).getName();
            } else {
                throw new IllegalStateException("Cannot log transactions unles XAResources are named! " + committer);
            }
        }

        public Xid getBranchXid() {
            return branchId;
        }
    }


}
