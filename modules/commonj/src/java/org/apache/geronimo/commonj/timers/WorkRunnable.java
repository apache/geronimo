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

import commonj.timers.StopTimerListener;
import commonj.timers.TimerListener;

final class WorkRunnable implements Runnable {
    private TimerImpl timer = null;

    private TimerListener timerListener;

    private TimerManagerImpl timerManager = null;

    public WorkRunnable(TimerImpl timer) {
        this.timer = timer;
        this.timerListener = timer.getTimerListener();
        this.timerManager = timer.getTimerManager();
    }

    public void run() {
        RuntimeException runtimeException = null;

        try {
            // here we use the state set during preinvoke, to avoid getting the lock again
            if (!timer.isStopped()) {
                timerListener.timerExpired(timer);
            }
        } catch (RuntimeException re) {
            runtimeException = re;
        }

        try {
            // here we get the lock, and see if things have changed since preinvoke
            State state = timerManager.getState();
            if ((state == State.STOPPING || state == State.STOPPED) && timerListener instanceof StopTimerListener) {
                ((StopTimerListener) timerListener).timerStop(timer);
            }
        } catch (RuntimeException re) {
            if (runtimeException == null) {
                runtimeException = re;
            }
            // TODO log exception
        }

        try {
            timerManager.postInvoke(timer);
        } catch (RuntimeException re) {
            if (runtimeException == null) {
                runtimeException = re;
            }
            // TODO log exception
        }

        if (runtimeException != null) {
            throw runtimeException;
        }
    }
}
