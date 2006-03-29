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

import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import commonj.timers.StopTimerListener;
import commonj.timers.Timer;
import commonj.timers.TimerListener;
import commonj.timers.TimerManager;

import edu.emory.mathcs.backport.java.util.Queue;
import edu.emory.mathcs.backport.java.util.concurrent.ConcurrentHashMap;
import edu.emory.mathcs.backport.java.util.concurrent.ConcurrentLinkedQueue;
import edu.emory.mathcs.backport.java.util.concurrent.ExecutorService;
import edu.emory.mathcs.backport.java.util.concurrent.Executors;
import edu.emory.mathcs.backport.java.util.concurrent.ScheduledExecutorService;
import edu.emory.mathcs.backport.java.util.concurrent.TimeUnit;
import edu.emory.mathcs.backport.java.util.concurrent.locks.Condition;
import edu.emory.mathcs.backport.java.util.concurrent.locks.ReentrantLock;

public class TimerManagerImpl implements TimerManager {

    private static final ScheduledExecutorService scheduledExecutor = new ScheduledTimerExecutor(1);

    private static final ExecutorService workExecutor = Executors.newCachedThreadPool();

    private State state = State.RUNNING;

    private ReentrantLock stateLock = new ReentrantLock();

    private Condition stateChange = stateLock.newCondition();

    private Map managedTimers = new ConcurrentHashMap();

    private Map inflight = new ConcurrentHashMap();

    // doesn't need synchronization, update when move to 5.0
    private Queue suspendedWork = new ConcurrentLinkedQueue();

    public TimerManagerImpl() {
    }

    public void suspend() {
        doSuspend();
    }

    public boolean isSuspending() throws IllegalStateException {
        State currentState = getState();
        return (currentState == State.SUSPENDED || currentState == State.SUSPENDING);
    }

    public boolean isSuspended() throws IllegalStateException {
        return getState() == State.SUSPENDED;
    }

    public boolean waitForSuspend(long timeout) 
    throws InterruptedException, IllegalStateException, IllegalArgumentException {
        if (timeout < 0) {
            throw new IllegalArgumentException("Negative timeout is not permitted");
        }

        doSuspend();

        stateLock.lock();
        try {

            long startTime = System.currentTimeMillis();
            long currentTimeout = timeout;

            while (currentTimeout > 0 && state == State.SUSPENDING) {
                stateChange.await(currentTimeout, TimeUnit.MILLISECONDS);

                long currentTime = System.currentTimeMillis();  
                long duration = currentTime - startTime;
                if (duration > currentTimeout) {
                    currentTimeout -= duration;
                    startTime = currentTime;
                }
            }
            
            if (state == State.SUSPENDED) {
                return true;
            }
            return false;
        } finally {
            stateLock.unlock();
        }
    }

    void doSuspend() {
        stateLock.lock();
        try {
            if (state == State.SUSPENDING || state == State.SUSPENDED ) {
                return; // nothing to do
            }
            
            if (inflightWork()) {
                setState(State.SUSPENDING);
            } else { 
                setState(State.SUSPENDED);
            }

        } finally {
            stateLock.unlock();
        }
    }
    
    public void resume() throws IllegalStateException {
        Queue resumeWork = null;

        stateLock.lock();
        try {
            setState(State.RUNNING);
            resumeWork = suspendedWork;
            suspendedWork = new ConcurrentLinkedQueue();
        } finally {
            stateLock.unlock();
        }

        while (!resumeWork.isEmpty()) {
            TimerImpl timer = (TimerImpl) resumeWork.remove();
            Runnable runnable = new ScheduledRunnable(timer, workExecutor);

            if (timer.getPeriod() == 0) {
                scheduledExecutor.schedule(runnable, timer.getScheduledExecutionTime() - System.currentTimeMillis(), TimeUnit.MILLISECONDS);
            } else if (timer.isFixedRate()) {
                scheduledExecutor.scheduleAtFixedRate(runnable, timer.getScheduledExecutionTime() - System.currentTimeMillis(), timer.getPeriod(),
                        TimeUnit.MILLISECONDS);
            } else {
                scheduledExecutor.scheduleWithFixedDelay(runnable, timer.getScheduledExecutionTime() - System.currentTimeMillis(), timer.getPeriod(),
                        TimeUnit.MILLISECONDS);
            }
        }
    }

    public void stop() throws IllegalStateException {
        doStop();
    }

    public boolean isStopped() {
        return getState() == State.STOPPED;
    }

    public boolean isStopping() {
        State currentState = getState();
        return (currentState == State.STOPPED || currentState == State.STOPPING);
    }

    public boolean waitForStop(long timeout) throws InterruptedException, IllegalArgumentException {
        if (timeout < 0) {
            throw new IllegalArgumentException("Negative timeout is not permitted");
        }

        doStop();

        stateLock.lock();
        try {
            long startTime = System.currentTimeMillis();
            long currentTimeout = timeout;

            while (currentTimeout > 0 && state == State.STOPPING) {
                stateChange.await(currentTimeout, TimeUnit.MILLISECONDS);

                long currentTime = System.currentTimeMillis();  
                long duration = currentTime - startTime;
                if (duration > currentTimeout) {
                    currentTimeout -= duration;
                    startTime = currentTime;
                }
            }
            
            if (state == State.STOPPED) {
                return true;
            }
            return false;
        } finally {
            stateLock.unlock();
        }
    }

    private void doStop() {
        Set stopTimers = null;

        stateLock.lock();
        try {
            if (getState() == State.STOPPING || getState() == State.STOPPED) {
                return; // nothing to do
            }
            
            if (inflightWork()) {
                setState(State.STOPPING);
            } else {
                setState(State.STOPPED);
            }

            stopTimers = new HashSet(managedTimers.keySet());
            stopTimers.removeAll(inflight.keySet());
        } finally {
            stateLock.unlock();
        }

        Iterator iterator = stopTimers.iterator();
        while (iterator.hasNext()) {
            TimerImpl timer = (TimerImpl) iterator.next();

            // it's OK if the timer fires before we call cancel, pre-invoke will
            // catch that case
            timer.getFuture().cancel(false);
            timer.setStopped();

            TimerListener listener = timer.getTimerListener();
            if (listener instanceof StopTimerListener) {
                ((StopTimerListener) timer.getTimerListener()).timerStop(timer);
            }
            
            managedTimers.remove (timer); // prevent garbage

            workExecutor.submit(new WorkRunnable(timer));
        }
    }

    public void checkState(String method) {
        State currentState = getState();
        if (currentState == State.STOPPING || currentState == State.STOPPED) {
            throw new IllegalStateException("Cannot call method " + method + " when TimerManager is in state " + currentState + ".");
        }
    }

    public Timer schedule(TimerListener listener, Date firstTime) throws IllegalArgumentException, IllegalStateException {
        stateLock.lock();
        try {
            checkState("schedule()");

            TimerImpl timer = new TimerImpl(firstTime.getTime(), 0L, false, listener, this);
            managedTimers.put(timer, timer);
            
            Runnable runnable = new ScheduledRunnable(timer, workExecutor);
            scheduledExecutor.schedule(runnable, firstTime.getTime() - System.currentTimeMillis(), TimeUnit.MILLISECONDS);
            
            return timer;
        } finally {
            stateLock.unlock();
        }
    }

    public Timer schedule(TimerListener listener, long delay) throws IllegalArgumentException, IllegalStateException {
        stateLock.lock();
        try {
            checkState("schedule()");
            
            TimerImpl timer = new TimerImpl(System.currentTimeMillis() + delay, 0L, false, listener, this);
            managedTimers.put(timer, timer);
            
            Runnable runnable = new ScheduledRunnable(timer, workExecutor);
            scheduledExecutor.schedule(runnable, delay, TimeUnit.MILLISECONDS);
            
            return timer;
        } finally {
            stateLock.unlock();
        }
    }

    public Timer schedule(TimerListener listener, Date firstTime, long period) throws IllegalArgumentException, IllegalStateException {
        stateLock.lock();
        try {
            checkState("schedule()");
            
            TimerImpl timer = new TimerImpl(firstTime.getTime(), period, false, listener, this);
            managedTimers.put(timer, timer);
            
            Runnable runnable = new ScheduledRunnable(timer, workExecutor);
            scheduledExecutor.scheduleWithFixedDelay(runnable, firstTime.getTime() - System.currentTimeMillis(), period, TimeUnit.MILLISECONDS);
            
            return timer;
        } finally {
            stateLock.unlock();
        }
    }

    public Timer schedule(TimerListener listener, long delay, long period) throws IllegalArgumentException, IllegalStateException {
        stateLock.lock();
        try {
            checkState("schedule()");

            TimerImpl timer = new TimerImpl(System.currentTimeMillis() + delay, period, false, listener, this);
            managedTimers.put(timer, timer);

            Runnable runnable = new ScheduledRunnable(timer, workExecutor);
            scheduledExecutor.scheduleWithFixedDelay(runnable, delay, period, TimeUnit.MILLISECONDS);
            return timer;
        } finally {
            stateLock.unlock();
        }
    }

    public Timer scheduleAtFixedRate(TimerListener listener, Date firstTime, long period) throws IllegalArgumentException, IllegalStateException {
        stateLock.lock();
        try {
            checkState("schedule()");
            
            TimerImpl timer = new TimerImpl(firstTime.getTime(), period, true, listener, this);
            managedTimers.put(timer, timer);

            Runnable runnable = new ScheduledRunnable(timer, workExecutor);
            scheduledExecutor.scheduleAtFixedRate(runnable, firstTime.getTime() - System.currentTimeMillis(), period, TimeUnit.MILLISECONDS);

            return timer;
        } finally {
            stateLock.unlock();
        }
    }

    public Timer scheduleAtFixedRate(TimerListener listener, long delay, long period) throws IllegalArgumentException, IllegalStateException {
        stateLock.lock();
        try {
            checkState("schedule()");

            TimerImpl timer = new TimerImpl(System.currentTimeMillis() + delay, period, false, listener, this);
            managedTimers.put(timer, timer);

            Runnable runnable = new ScheduledRunnable(timer, workExecutor);
            scheduledExecutor.scheduleAtFixedRate(runnable, delay, period, TimeUnit.MILLISECONDS);

            return timer;
        } finally {
            stateLock.unlock();
        }
    }

    State getState() {
        stateLock.lock();
        try {
            return state;
        } finally {
            stateLock.unlock();
        }
    }

    boolean preInvoke(TimerImpl timer) {
        boolean continueInvoke = true;

        stateLock.lock();
        try {
            if (getState() == State.RUNNING) {
                inflight.put(timer, timer);
            } else {
                continueInvoke = false;
                timer.getFuture().cancel(false);
                if (state == State.SUSPENDING || state == State.SUSPENDED) {
                    suspendedWork.add(timer);
                }
            }
        } finally {
            stateLock.unlock();
        }

        return continueInvoke;
    }

    void postInvoke(TimerImpl timer) {
        stateLock.lock();
        try {
            inflight.remove(timer);
            if (state == State.SUSPENDING && !inflightWork()) {
                setState(State.SUSPENDED);
            } else if (state == State.STOPPING && !inflightWork()) {
                setState(State.STOPPED);
            }

            if (timer.getPeriod() == 0 || (state == State.STOPPING || state == State.STOPPED)) {
                managedTimers.remove(timer);
                if (state == State.STOPPING || state == State.STOPPED) {
                    timer.getFuture().cancel(false);
                }
            } else {
                timer.updateScheduledExecutionTime();
            }

        } finally {
            stateLock.unlock();
        }
    }

    private boolean inflightWork() {
        return inflight.size() > 0;
    }

    private void setState(State newState) {
        stateLock.lock();
        try {
            if (newState == State.SUSPENDING || newState == State.SUSPENDED) {
                if (state == State.STOPPED || state == State.STOPPING) {
                    throw new IllegalStateException("Cannot suspend a TimerManager that is in the " + state + " state.");
                }
            } else if (newState == State.RUNNING) {
                if (state == State.STOPPED || state == State.STOPPING) {
                    throw new IllegalStateException("Cannot transition a TimerManager that is in the " + state + " to state " + newState + ".");
                }
            }

            state = newState;
            stateChange.signalAll();
        } finally {
            stateLock.unlock();
        }
    }

    void cancel(TimerImpl timer) {
        managedTimers.remove(timer);
        inflight.remove(timer);
    }
}
