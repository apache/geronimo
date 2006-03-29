/**
 *
 * Copyright 2006 The Apache Software Foundation
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
package org.apache.geronimo.commonj.timers;

import edu.emory.mathcs.backport.java.util.concurrent.ExecutorService;
import edu.emory.mathcs.backport.java.util.concurrent.RunnableScheduledFuture;

final class ScheduledRunnable implements Runnable {
    private TimerImpl timer;

    private TimerManagerImpl timerManager;

    private ExecutorService workExecutor;

    public ScheduledRunnable(TimerImpl timer, ExecutorService workExecutor) {
        this.timer = timer;
        this.workExecutor = workExecutor;

        this.timerManager = timer.getTimerManager();
    }

    public void run() {
        if (timerManager.preInvoke(timer)) {
            workExecutor.submit(new WorkRunnable(timer));
        }
    }
    
    public void setFuture(RunnableScheduledFuture task){
        timer.setFuture(task);
    }
}
