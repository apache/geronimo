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

import javax.transaction.HeuristicMixedException;
import javax.transaction.HeuristicRollbackException;
import javax.transaction.NotSupportedException;
import javax.transaction.RollbackException;
import javax.transaction.SystemException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.geronimo.transaction.context.ContainerTransactionContext;
import org.apache.geronimo.transaction.context.TransactionContextManager;
import org.apache.geronimo.timer.ExecutorTask;
import org.apache.geronimo.timer.PersistenceException;
import org.apache.geronimo.timer.ThreadPooledTimer;

/**
 *
 *
 * @version $Rev$ $Date$
 *
 * */
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
        ContainerTransactionContext transactionContext = null;
        for (int tries = 0; tries < repeatCount; tries++) {
            try {
                transactionContext = transactionContextManager.newContainerTransactionContext();
            } catch (NotSupportedException e) {
                log.info(e);
                break;
            } catch (SystemException e) {
                log.info(e);
                break;
            }
            try {
                try {
                    userTask.run();
                } catch (Exception e) {
                    log.info(e);
                }
                try {
                    threadPooledTimer.workPerformed(workInfo);
                } catch (PersistenceException e) {
                    log.info(e);
                }
            } finally {
                try {
                    if (transactionContext.getRollbackOnly()) {
                        transactionContext.rollback();
                    } else {
                        transactionContext.commit();
                        if (workInfo.isOneTime()) {
                            threadPooledTimer.removeWorkInfo(workInfo);
                        }
                        return;
                    }
                } catch (SystemException e) {
                    log.info(e);
                } catch (HeuristicMixedException e) {
                    log.info(e);
                } catch (HeuristicRollbackException e) {
                    log.info(e);
                } catch (RollbackException e) {
                    log.info(e);
                }
            }
        }
        if (workInfo.isOneTime()) {
            threadPooledTimer.removeWorkInfo(workInfo);
        }
        log.warn("Failed to execute work successfully");
    }

}
