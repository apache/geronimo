/**
 *
 * Copyright 2004 The Apache Software Foundation
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

package org.apache.geronimo.timer;

import java.util.TimerTask;

import javax.transaction.RollbackException;
import javax.transaction.SystemException;
import javax.transaction.Synchronization;
import javax.transaction.Status;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 *
 *
 * @version $Rev$ $Date$
 *
 * */
public class ExecutorFeedingTimerTask extends TimerTask {

    private static final Log log = LogFactory.getLog(ExecutorFeedingTimerTask.class);

    private final WorkInfo workInfo;
    private final ThreadPooledTimer threadPooledTimer;
    boolean cancelled = false;

    public ExecutorFeedingTimerTask(WorkInfo workInfo, ThreadPooledTimer threadPooledTimer) {
        this.workInfo = workInfo;
        this.threadPooledTimer = threadPooledTimer;
    }

    public void run() {
        try {
            threadPooledTimer.getExecutor().execute(workInfo.getExecutorTask());
        } catch (InterruptedException e) {
            log.warn("Exception running task", e);
        }
    }

    public boolean cancel() {
        threadPooledTimer.removeWorkInfo(workInfo);
        try {
            threadPooledTimer.registerSynchronization(new CancelSynchronization(this));
        } catch (RollbackException e) {
            log.warn("Exception canceling task", e);
            throw (IllegalStateException) new IllegalStateException("RollbackException when trying to register Cancel Synchronization").initCause(e);
        } catch (SystemException e) {
            log.warn("Exception canceling task", e);
            throw (IllegalStateException) new IllegalStateException("SystemException when trying to register Cancel Synchronization").initCause(e);
        }
        // One cancels the task at this specific time. If the transaction is
        // rolled-back, one will recreate it.
        cancelled = true;
        return super.cancel();
    }

    public boolean isCancelled() {
        return cancelled;
    }

    private void doCancel() {
        try {
            // Impacts the timer storage only if the timer is cancelled
            // in the scope of a committed transactions.
            threadPooledTimer.getWorkerPersistence().cancel(workInfo.getId());
        } catch (PersistenceException e) {
            log.warn("Exception canceling task", e);
        }
    }

    private void rollbackCancel() {
        threadPooledTimer.addWorkInfo(workInfo);
        
        // The transaction has been rolled-back, we need to restore the
        // task as if cancel has been called.
        if ( workInfo.isOneTime() ) {
            threadPooledTimer.getTimer().schedule(
                new ExecutorFeedingTimerTask(workInfo, threadPooledTimer), 
                workInfo.getTime());
        } else if ( workInfo.getAtFixedRate() ) {
            threadPooledTimer.getTimer().scheduleAtFixedRate(
                new ExecutorFeedingTimerTask(workInfo, threadPooledTimer), 
                workInfo.getTime(), workInfo.getPeriod().longValue());
        } else {
            threadPooledTimer.getTimer().schedule(
                new ExecutorFeedingTimerTask(workInfo, threadPooledTimer),
                workInfo.getTime(), workInfo.getPeriod().longValue());
        }
    }
    
    private static class CancelSynchronization implements Synchronization {

        private final ExecutorFeedingTimerTask worker;

        public CancelSynchronization(ExecutorFeedingTimerTask worker) {
            this.worker = worker;
        }

        public void beforeCompletion() {
        }

        public void afterCompletion(int status) {
            if (status == Status.STATUS_COMMITTED) {
                worker.doCancel();
            } else if (status == Status.STATUS_ROLLEDBACK) {
                worker.rollbackCancel();
            }
        }

    }

}
