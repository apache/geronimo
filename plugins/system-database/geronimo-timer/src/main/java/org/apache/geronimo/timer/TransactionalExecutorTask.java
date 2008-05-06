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

package org.apache.geronimo.timer;

import javax.transaction.TransactionManager;
import javax.transaction.Status;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @version $Rev$ $Date$
 */
public class TransactionalExecutorTask implements ExecutorTask {
    private static final Logger log = LoggerFactory.getLogger(TransactionalExecutorTask.class);

    private final Runnable userTask;
    private final WorkInfo workInfo;
    private final ThreadPooledTimer threadPooledTimer;

    private final TransactionManager transactionManager;
    private final int repeatCount;

    public TransactionalExecutorTask(Runnable userTask, WorkInfo workInfo, ThreadPooledTimer threadPooledTimer, TransactionManager transactionManager, int repeatCount) {
        this.userTask = userTask;
        this.workInfo = workInfo;
        this.threadPooledTimer = threadPooledTimer;
        this.transactionManager = transactionManager;
        this.repeatCount = repeatCount;
    }

    public void run() {
        try {
            // try to do the work until it succeeded or we reach the repeat count
            boolean succeeded = false;
            for (int tries = 0; !succeeded && tries < repeatCount; tries++) {
                try {
                    if (!beginWork()) {
                        break;
                    }

                    work();
                } finally {
                    succeeded = completeWork();
                }
            }

            // if this was a one time thing, remove the job
            if (workInfo.isOneTime()) {
                threadPooledTimer.removeWorkInfo(workInfo);
            }

            // if we didn't succeed, log it
            if (!succeeded) {
                log.warn("Failed to execute work successfully");
            }
        } catch (RuntimeException e) {
            log.warn("RuntimeException occured while running user task", e);
            throw e;
        } catch (Error e) {
            log.warn("Error occured while running user task", e);
            throw e;
        }
    }

    private boolean beginWork() {
        try {
            transactionManager.begin();
        } catch (Exception e) {
            log.warn("Exception occured while starting container transaction", e);
            return false;
        }
        return true;
    }

    private void work() {
        try {
            userTask.run();
        } catch (Exception e) {
            log.warn("Exception occured while running user task", e);
        }
    }

    private boolean completeWork() {
        try {
            if (transactionManager.getStatus() == Status.STATUS_ACTIVE) {
                // clean up the work persistent data
                try {
                    threadPooledTimer.workPerformed(workInfo);
                } catch (PersistenceException e) {
                    log.warn("Exception occured while updating timer persistent state", e);
                }

                // commit the tx
                transactionManager.commit();

                // all is cool
                return true;
            } else {
                // tx was marked rollback, so roll it back
                transactionManager.rollback();
            }
        } catch (Exception e) {
            log.warn("Exception occured while completing container transaction", e);
        }
        // something bad happened
        return false;
    }

}
