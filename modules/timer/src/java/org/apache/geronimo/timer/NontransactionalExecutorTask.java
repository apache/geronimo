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
import org.apache.geronimo.timer.ExecutorTask;

/**
 *
 *
 * @version $Revision: 1.1 $ $Date: 2004/07/18 22:10:56 $
 *
 * */
public class NontransactionalExecutorTask implements ExecutorTask {

    private static final Log log = LogFactory.getLog(NontransactionalExecutorTask.class);

    private final Runnable userTask;
    private final WorkInfo workInfo;
    private final ThreadPooledTimer threadPooledTimer;

    public NontransactionalExecutorTask(Runnable userTask, WorkInfo workInfo, ThreadPooledTimer threadPooledTimer) {
        this.userTask = userTask;
        this.workInfo = workInfo;
        this.threadPooledTimer = threadPooledTimer;
    }

    public void run() {
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
        if (workInfo.isOneTime()) {
            threadPooledTimer.removeWorkInfo(workInfo);
        }
    }

}
