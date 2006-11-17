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

/**
 *
 *
 * @version $Rev$ $Date$
 *
 * */
public class TransactionalExecutorTaskFactory implements ExecutorTaskFactory {

    private final TransactionManager transactionManager;
    private int repeatCount;

    public TransactionalExecutorTaskFactory(TransactionManager transactionManager, int repeatCount) {
        this.transactionManager = transactionManager;
        this.repeatCount = repeatCount;
    }

    public int getRepeatCount() {
        return repeatCount;
    }

    public ExecutorTask createExecutorTask(Runnable userTask, WorkInfo workInfo, ThreadPooledTimer threadPooledTimer) {
        return new TransactionalExecutorTask(userTask, workInfo, threadPooledTimer, transactionManager, repeatCount);
    }

}
