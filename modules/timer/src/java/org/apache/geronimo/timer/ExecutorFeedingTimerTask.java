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
 * @version $Revision: 1.1 $ $Date: 2004/07/18 22:10:56 $
 *
 * */
public class ExecutorFeedingTimerTask extends TimerTask {

    private static final Log log = LogFactory.getLog(ExecutorFeedingTimerTask.class);

    private final WorkInfo workInfo;
    private final ThreadPooledTimer threadPooledTimer;

    public ExecutorFeedingTimerTask(WorkInfo workInfo, ThreadPooledTimer threadPooledTimer) {
        this.workInfo = workInfo;
        this.threadPooledTimer = threadPooledTimer;
    }

    public void run() {
        try {
            threadPooledTimer.getExecutor().execute(workInfo.getExecutorTask());
        } catch (InterruptedException e) {
            log.warn(e);
        }
    }

    public boolean cancel() {
        try {
            threadPooledTimer.getWorkerPersistence().cancel(workInfo.getId());
        } catch (PersistenceException e) {
            log.warn(e);
        }
        try {
            threadPooledTimer.registerSynchronization(new CancelSynchronization(this));
        } catch (RollbackException e) {
            log.info(e);
            throw (IllegalStateException) new IllegalStateException("RollbackException when trying to register cacel synchronization").initCause(e);
        } catch (SystemException e) {
            log.info(e);
            throw (IllegalStateException) new IllegalStateException("SystemException when trying to register cacel synchronization").initCause(e);
        }
        //return value is meaningless.
        return true;
    }

    private void doCancel() {
        super.cancel();
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
            }
        }

    }

}
