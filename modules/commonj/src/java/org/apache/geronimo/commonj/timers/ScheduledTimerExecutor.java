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

import edu.emory.mathcs.backport.java.util.concurrent.Callable;
import edu.emory.mathcs.backport.java.util.concurrent.RunnableScheduledFuture;
import edu.emory.mathcs.backport.java.util.concurrent.ScheduledThreadPoolExecutor;
import edu.emory.mathcs.backport.java.util.concurrent.ThreadFactory;

final class ScheduledTimerExecutor extends ScheduledThreadPoolExecutor {

    public ScheduledTimerExecutor(int corePoolSize) {
        super(corePoolSize);
    }
    
    public ScheduledTimerExecutor(int corePoolSize, ThreadFactory threadFactory) {
        super(corePoolSize, threadFactory);
    }

    protected RunnableScheduledFuture decorateTask(java.lang.Runnable runnable, RunnableScheduledFuture task) {
        
        // This is the timing thing.  Need to have this set before the timer can expire, so do it here.
        ScheduledRunnable scheduledRunnable = (ScheduledRunnable) runnable;
        scheduledRunnable.setFuture(task);
        return task;
    }

    protected RunnableScheduledFuture decorateTask(Callable callable, RunnableScheduledFuture task) {
        throw new UnsupportedOperationException();
    }
}
