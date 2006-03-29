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

import commonj.timers.Timer;
import commonj.timers.TimerListener;
import commonj.timers.CancelTimerListener;

import edu.emory.mathcs.backport.java.util.concurrent.Future;

public class TimerImpl implements Timer {

    private TimerListener timerListener = null;

    private TimerManagerImpl timerManager = null;

    private Future future = null;

    private long scheduledExecutionTime = 0L;

    private long period = 0L;

    private boolean fixedRate = false;

    private boolean cancelled = false;
    
    private boolean stopped = false;

    public TimerImpl(long scheduledExecutionTime, long period, boolean fixedRate,
                     TimerListener timerListener, TimerManagerImpl manager) {
        this.scheduledExecutionTime = scheduledExecutionTime;
        this.period = period;
        this.fixedRate = fixedRate;
        
        this.timerListener = timerListener;
        this.timerManager = manager;
    }

    synchronized public boolean cancel() {
        if (cancelled)
            return false;
        
        cancelled = true;
        
        if (future != null) {
            future.cancel(false);
        }
        
        timerManager.cancel(this);

        // TODO: probably shouldn't hold lock while calling timerCancel... 
        if (timerListener instanceof CancelTimerListener) {
            try {
                ((CancelTimerListener)timerListener).timerCancel(this);
            } catch (RuntimeException re) { } // ignore
        }
        
        // TODO: not quite according to spec, i think. doesn't handle non-repeating timer properly
        return true;
    }

    public TimerListener getTimerListener() {
        return timerListener;
    }

    public long getScheduledExecutionTime() throws IllegalStateException {
        return scheduledExecutionTime;
    }

    public long getPeriod() {
        return period;
    }

    Future getFuture() {
        return future;
    }

    void setFuture(Future future) {
        this.future = future;
    }

    TimerManagerImpl getTimerManager() {
        return timerManager;
    }

    boolean isFixedRate() {
        return fixedRate;
    }

    void setStopped() {
        stopped = true;
    }

    boolean isStopped() {
        return stopped;
    }

    synchronized void updateScheduledExecutionTime() {
        if (fixedRate) {
            scheduledExecutionTime += period;
        } else {
            scheduledExecutionTime = System.currentTimeMillis() + period;
        }
    }
}
