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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.geronimo.transaction.context.TransactionContextManager;
import org.apache.geronimo.transaction.context.TransactionContext;

/**
 * @version $Rev$ $Date$
 */
public class TransactionalExecutorTask implements ExecutorTask {
    private static final Log log = LogFactory.getLog(TransactionalExecutorTask.class);

    private final Runnable userTask;
    private final WorkInfo workInfo;
    private final ThreadPooledTimer threadPooledTimer;

    private final TransactionContextManager transactionContextManager;
    private final int repeatCount;

    public TransactionalExecutorTask(Runnable userTask, WorkInfo workInfo, ThreadPooledTimer threadPooledTimer, TransactionContextManager transactionContextManager, int repeatCount) {
        this.userTask = userTask;
        this.workInfo = workInfo;
        this.threadPooledTimer = threadPooledTimer;
        this.transactionContextManager = transactionContextManager;
        this.repeatCount = repeatCount;
    }
    
    public void run() {
        TransactionContext transactionContext = null;
        for (int tries = 0; tries < repeatCount; tries++) {
            try {
                transactionContext = transactionContextManager.newContainerTransactionContext();
            } catch (Exception e) {
                log.info("Exception occured while starting container transaction", e);
                break;
            }
            try {
                try {
                    userTask.run();
                } catch (Exception e) {
                    log.info("Exception occured while running user task", e);
                }
                try {
                    threadPooledTimer.workPerformed(workInfo);
                } catch (PersistenceException e) {
                    log.info("Exception occured while updating timer persistent state", e);
                }
            } finally {
                try {
                    if (transactionContext.commit()) {
                        if (workInfo.isOneTime()) {
                            threadPooledTimer.removeWorkInfo(workInfo);
                        }
                        // todo this is a very weird code structure.... returning from a finally is very confusing
                        return;
                    }
                } catch (Exception e) {
                    log.info("Exception occured while completing container transaction", e);
                }
            }
        }
        if (workInfo.isOneTime()) {
            threadPooledTimer.removeWorkInfo(workInfo);
        }
        log.warn("Failed to execute work successfully");
    }

}
