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

/*
 * This class is based on and borrows code from java.util.concurrent.ScheduledThreadPoolExecutor
 * class in Apache Harmony.
 */
package org.apache.geronimo.concurrent.executor;

import java.util.AbstractCollection;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.DelayQueue;
import java.util.concurrent.Delayed;
import java.util.concurrent.Future;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

import javax.util.concurrent.ManagedScheduledExecutorService;
import javax.util.concurrent.ManagedTaskListener;
import javax.util.concurrent.Trigger;

import org.apache.geronimo.concurrent.ManagedContext;
import org.apache.geronimo.concurrent.ManagedTaskListenerSupport;

public abstract class AbstractManagedScheduledExecutorService 
    extends AbstractManagedExecutorService 
    implements ManagedScheduledExecutorService {

    /**
     * False if should cancel/suppress periodic tasks on shutdown.
     */
    private volatile boolean continueExistingPeriodicTasksAfterShutdown;

    /**
     * False if should cancel non-periodic tasks on shutdown.
     */
    private volatile boolean executeExistingDelayedTasksAfterShutdown = true;

    /**
     * Sequence number to break scheduling ties, and in turn to
     * guarantee FIFO order among tied entries.
     */
    private static final AtomicLong sequencer = new AtomicLong(0);

    private class ScheduledFutureTask<V> 
            extends ManagedFutureTask<V> implements ScheduledFuture<V> {
                        
        /** Sequence number to break ties FIFO */
        private final long sequenceNumber;
        
        private final Trigger trigger;
        
        private Date lastActualRunTime;
        private Date lastScheduledRunTime;
        private Date lastCompleteTime;
        private Date submitTime;
        
        ScheduledFutureTask(Runnable r, 
                            V result, 
                            ManagedContext managedContext,
                            ManagedTaskListenerSupport listener,
                            Date triggerTime) {
            super(r, result, managedContext, listener);
            this.lastScheduledRunTime = triggerTime;
            this.trigger = null;
            this.submitTime = new Date();
            this.sequenceNumber = sequencer.getAndIncrement();
        }
        
        ScheduledFutureTask(Runnable r, 
                            V result,
                            ManagedContext managedContext,
                            ManagedTaskListenerSupport listener,
                            Trigger trigger) {
            super(r, result, managedContext, listener);
            this.trigger = trigger;
            this.submitTime = new Date();
            this.sequenceNumber = sequencer.getAndIncrement();
            getNextRunTime();
        }

        ScheduledFutureTask(Callable<V> callable,
                            ManagedContext managedContext,
                            ManagedTaskListenerSupport listener,
                            Date triggerTime) { 
            super(callable, managedContext, listener);
            this.lastScheduledRunTime = triggerTime;
            this.trigger = null;
            this.submitTime = new Date();
            this.sequenceNumber = sequencer.getAndIncrement();
        }
        
        ScheduledFutureTask(Callable<V> callable,
                            ManagedContext managedContext,
                            ManagedTaskListenerSupport listener,
                            Trigger trigger) {
            super(callable, managedContext, listener);
            this.trigger = trigger;
            this.submitTime = new Date();
            this.sequenceNumber = sequencer.getAndIncrement();
            getNextRunTime();
        }
        
        private void getNextRunTime() {   
            /* XXX: The spec does not define (yet) what should happen if trigger.getNextRunTime()
             * returns null on the verify first time. We throw RejectedExecutionException
             * in such case since the task will not run.              
             */
            this.lastScheduledRunTime = 
                trigger.getNextRunTime(this, this.submitTime, null, null, null);  
            if (this.lastScheduledRunTime == null) {
                throw new RejectedExecutionException("Null nextRunTime returned by Trigger");
            }            
        }
        
        public long getDelay(TimeUnit unit) {
            long d = unit.convert(this.lastScheduledRunTime.getTime() - System.currentTimeMillis(), TimeUnit.MILLISECONDS);         
            return d;
        }

        public int compareTo(Delayed other) {
            if (other == this) // compare zero ONLY if same object
                return 0;
            ScheduledFutureTask<?> x = (ScheduledFutureTask<?>)other;
            long diff = this.lastScheduledRunTime.getTime() - x.lastScheduledRunTime.getTime();
            if (diff < 0)
                return -1;
            else if (diff > 0)
                return 1;
            else if (sequenceNumber < x.sequenceNumber)
                return -1;
            else
                return 1;
        }

        /**
         * Returns true if this is a periodic (not a one-shot) action.
         * @return true if periodic
         */
        boolean isPeriodic() {
            return this.trigger != null;
        }

        /**
         * Run a periodic task
         */
        private void runPeriodicSub() {
            boolean ok = false;
            if (isCancelled()) {
                ok = false;
                if (this.listenerSupport != null) {
                    this.listenerSupport.taskDone(this, null);
                }
            } else {
                if (this.trigger.skipRun(this, this.lastScheduledRunTime)) {
                    // skip run, task is rescheduled only if not cancelled
                    ok = super.setSkipped();
                    if (this.listenerSupport != null) {
                        this.listenerSupport.taskDone(this, null);
                    }
                } else {
                    // run now
                    this.lastActualRunTime = new Date();
                    ok = super.runAndReset();
                    this.lastCompleteTime = new Date();
                }
            }
            boolean down = isShutdown();
            // Reschedule if not cancelled and not shutdown or policy allows
            if (ok && (!down ||
                       (getContinueExistingPeriodicTasksAfterShutdownPolicy() && 
                        !isTerminating()))) {
                this.lastScheduledRunTime = 
                    this.trigger.getNextRunTime(this, 
                                                this.submitTime,
                                                this.lastActualRunTime, 
                                                this.lastScheduledRunTime, 
                                                this.lastCompleteTime); 
                if (this.lastScheduledRunTime != null) {
                    // reschedule the task
                    if (this.listenerSupport != null) {
                        this.listenerSupport.taskSubmitted(this);
                    }
                    AbstractManagedScheduledExecutorService.super.getQueue().add(this);
                }
            } else if (down) {
                // This might have been the final executed delayed
                // task.  Wake up threads to check.           
                interruptIdleWorkers();
            }
        }
        
        private void runPeriodic() {
            if (this.setContextOnRun && this.managedContext != null) {
                Map<String, Object> threadContext = null;
                try {
                    threadContext = this.managedContext.set();
                } catch (RuntimeException e) {
                    LOG.warn("Failed to apply context to thread for task " + this + ": " + 
                             e.getMessage() + ". Cancelling task");        
                    cancelFutureTask(false);
                    return;
                }
                try {
                    runPeriodicSub();
                } finally {
                    this.managedContext.unset(threadContext);
                }
            } else {
                runPeriodicSub();
            }
        }

        /**
         * Overrides FutureTask version so as to reset/requeue if periodic.
         */ 
        public void run() {
            if (isPeriodic()) {
                runPeriodic();
            } else { 
                super.run();
            }
        }
    }

    public AbstractManagedScheduledExecutorService(int corePoolSize,
                                                   ThreadFactory threadFactory) {
        super(corePoolSize, Integer.MAX_VALUE, 0, TimeUnit.NANOSECONDS,
              new DelayedWorkQueue(), threadFactory);
    }
        
    /**
     * Specialized variant of ThreadPoolExecutor.execute for delayed tasks.
     */
    protected void delayedExecute(ScheduledFutureTask<?> task) {
        preExecute(task);
        super.getQueue().add(task);
    }
    
    protected void delayedExecute(ManagedFutureTask<?> task) {
        preExecute(task);
        // wrap ManagedFutureTask into ScheduledFutureTask
        Date triggerTime = new Date();
        ScheduledFutureTask<?> t = 
            new ScheduledFutureTask<Object>(task, null, null, null, triggerTime);    
        super.getQueue().add(t);
    }
    
    protected void preExecute(ManagedFutureTask<?> task) {
        ManagedTaskListenerSupport listenerSupport = task.getManagedTaskListenerSupport();
        
        if (listenerSupport != null) {
            listenerSupport.taskSubmitted(task);
        }
        
        if (isShutdown()) {
            try {
                reject(task);
            } catch (RejectedExecutionException exception) {
                if (listenerSupport != null) {
                    listenerSupport.taskDone(task, exception);
                }
                throw exception;
            }
            return;
        }
        
        // Prestart a thread if necessary. We cannot prestart it
        // running the task because the task (probably) shouldn't be
        // run yet, so thread will just idle until delay elapses.
        if (getPoolSize() < getCorePoolSize()) {
            prestartCoreThread();
        }
    }    

    /**
     * Cancel and clear the queue of all tasks that should not be run
     * due to shutdown policy.
     */
    private void cancelUnwantedTasks() {
        boolean keepDelayed = getExecuteExistingDelayedTasksAfterShutdownPolicy();
        boolean keepPeriodic = getContinueExistingPeriodicTasksAfterShutdownPolicy();
        if (!keepDelayed && !keepPeriodic) 
            super.getQueue().clear();
        else if (keepDelayed || keepPeriodic) {
            Object[] entries = super.getQueue().toArray();
            for (int i = 0; i < entries.length; ++i) {
                Object e = entries[i];
                if (e instanceof ScheduledFutureTask) {
                    ScheduledFutureTask<?> t = (ScheduledFutureTask<?>)e;
                    if (t.isPeriodic()? !keepPeriodic : !keepDelayed)
                        t.cancel(false);
                }
            }
            entries = null;
            purge();
        }
    }

    public boolean remove(Runnable task) {
        if (!(task instanceof ScheduledFutureTask))
            return false;
        return getQueue().remove(task);
    }
  
    // *** schedule commands ***
    
    public ScheduledFuture<?> schedule(Runnable command, 
                                       long delay, 
                                       TimeUnit unit) {
        return schedule(command, null, delay, unit, null);
    }

    public ScheduledFuture<?> schedule(Runnable command,
                                       long delay,
                                       TimeUnit unit,
                                       ManagedTaskListener listener) {
        return schedule(command, null, delay, unit, listener);
    }
    
    protected <T> ScheduledFuture<T> schedule(Runnable command,
                                              T result, 
                                              long delay,
                                              TimeUnit unit,
                                              ManagedTaskListener listener) {
        if (command == null || unit == null) {
            throw new NullPointerException();
        }
        Date triggerTime = new Date(System.currentTimeMillis() + unit.toMillis(delay));
        ManagedTaskListenerSupport listenerSupport = getManagedTaskListenerSupport(listener);
        ManagedContext managedContext = getManagedContext();
        ScheduledFutureTask<T> t = new ScheduledFutureTask<T>(command, 
                                                              result, 
                                                              managedContext, 
                                                              listenerSupport, 
                                                              triggerTime);
        delayedExecute(t);
        return t;
    }
    
    public ScheduledFuture<?> schedule(Runnable command,
                                       Trigger trigger,
                                       ManagedTaskListener listener) {
        if (command == null || trigger == null) {
            throw new NullPointerException();
        }
        ManagedTaskListenerSupport listenerSupport = getManagedTaskListenerSupport(listener);
        ManagedContext managedContext = getManagedContext();
        ScheduledFutureTask<?> t = new ScheduledFutureTask<Boolean>(command, 
                                                                    null, 
                                                                    managedContext, 
                                                                    listenerSupport, 
                                                                    trigger);
        delayedExecute(t);
        return t;
    }
    
    public <V> ScheduledFuture<V> schedule(Callable<V> callable, 
                                           long delay, 
                                           TimeUnit unit) {
        return schedule(callable, delay, unit, null);
    }
    
    public <V> ScheduledFuture<V> schedule(Callable<V> callable,
                                           long delay,
                                           TimeUnit unit,
                                           ManagedTaskListener listener) {
        if (callable == null || unit == null) {
            throw new NullPointerException();
        }
        if (delay < 0) delay = 0;
        Date triggerTime = new Date(System.currentTimeMillis() + unit.toMillis(delay));
        ManagedTaskListenerSupport listenerSupport = getManagedTaskListenerSupport(listener);
        ManagedContext managedContext = getManagedContext();
        ScheduledFutureTask<V> t = new ScheduledFutureTask<V>(callable, 
                                                              managedContext, 
                                                              listenerSupport,
                                                              triggerTime);
        delayedExecute(t);
        return t;
    }
    
    public <V> ScheduledFuture<V> schedule(Callable<V> callable,
                                           Trigger trigger,
                                           ManagedTaskListener listener) {
        if (callable == null || trigger == null) {
            throw new NullPointerException();
        }
        ManagedTaskListenerSupport listenerSupport = getManagedTaskListenerSupport(listener);
        ManagedContext managedContext = getManagedContext();
        ScheduledFutureTask<V> t = new ScheduledFutureTask<V>(callable, 
                                                              managedContext, 
                                                              listenerSupport,
                                                              trigger);
        delayedExecute(t);
        return t;
    }
    
    // *** scheduleAt* commands *****
         
    private static class PeriodicTrigger implements Trigger {

        private long period;
        private long initialDelay;
        private boolean rate;

        public PeriodicTrigger(long initialDelay, long period, boolean rate) {
            this.initialDelay = initialDelay;
            this.period = period;
            this.rate = rate;
        }
        
        public Date getNextRunTime(Future<?> future,
                                   Date submitTime,
                                   Date lastActualRunTime,
                                   Date lastScheduledRunTime,
                                   Date lastCompleteTime) {
            long nextRunTime;
            if (lastScheduledRunTime == null) {
                nextRunTime = System.currentTimeMillis() + this.initialDelay;
            } else {
                if (this.rate) {
                    nextRunTime = lastScheduledRunTime.getTime() + this.period;
                } else {
                    nextRunTime = System.currentTimeMillis() + this.period;
                }
            }
            return new Date(nextRunTime);
        }

        public boolean skipRun(Future<?> arg0, Date arg1) {
            return false;
        }

    }
    
    public ScheduledFuture<?> scheduleAtFixedRate(Runnable command, 
                                                  long initialDelay,  
                                                  long period, 
                                                  TimeUnit unit) {
        return scheduleAtFixedRate(command, initialDelay, period, unit, null);
    }
    
    public ScheduledFuture<?> scheduleAtFixedRate(Runnable command,
                                                  long initialDelay,
                                                  long period,
                                                  TimeUnit unit,
                                                  ManagedTaskListener listener) {
        if (command == null || unit == null) {
            throw new NullPointerException();
        }
        if (period <= 0) {
            throw new IllegalArgumentException();
        }
        if (initialDelay < 0) initialDelay = 0;
        PeriodicTrigger trigger = 
            new PeriodicTrigger(unit.toMillis(initialDelay), unit.toMillis(period), true);
        return schedule(command, trigger, listener);
    }
    
    public ScheduledFuture<?> scheduleWithFixedDelay(Runnable command, 
                                                     long initialDelay,  
                                                     long delay, 
                                                     TimeUnit unit) {
        return scheduleWithFixedDelay(command, initialDelay, delay, unit, null);
    }
    
    public ScheduledFuture<?> scheduleWithFixedDelay(Runnable command,
                                                     long initialDelay,
                                                     long delay,
                                                     TimeUnit unit,
                                                     ManagedTaskListener listener) {
        if (command == null || unit == null) {
            throw new NullPointerException();
        }
        if (delay <= 0) {
            throw new IllegalArgumentException();
        }
        if (initialDelay < 0) initialDelay = 0;
        PeriodicTrigger trigger = 
            new PeriodicTrigger(unit.toMillis(initialDelay), unit.toMillis(delay), false);
        return schedule(command, trigger, listener);
    }


    // Override ExecutorService methods

    public void execute(Runnable command) {
        schedule(command, null, 0, TimeUnit.NANOSECONDS, null);
    }
   
    public Future<?> submit(Runnable task) {
        return schedule(task, null, 0, TimeUnit.NANOSECONDS, null);
    }

    public <T> Future<T> submit(Runnable task, T result) {
        return schedule(task, result, 0, TimeUnit.NANOSECONDS, null);
    }

    public <T> Future<T> submit(Callable<T> task) {
        return schedule(task, 0, TimeUnit.NANOSECONDS, null);
    }
      
    public Future<?> submit(Runnable task, ManagedTaskListener listener) {
        return schedule(task, null, 0, TimeUnit.NANOSECONDS, listener);
    }

    public <T> Future<T> submit(Runnable task, T result, ManagedTaskListener listener) {
        return schedule(task, result, 0, TimeUnit.NANOSECONDS, listener);
    }
        
    public <T> Future<T> submit(Callable<T> task, ManagedTaskListener listener) {
        return schedule(task, 0, TimeUnit.NANOSECONDS, listener);
    }
    
    /* 
     * This is called by invokeAll/invokeAny functions.     
     */
    protected void executeTask(ManagedFutureTask<?> task) {  
        // ManagedFutureTask will get wrapped in ScheduledFutureTask
        delayedExecute(task);
    }
    
    // Policy methods
    
    /**
     * Set policy on whether to continue executing existing periodic
     * tasks even when this executor has been <tt>shutdown</tt>. In
     * this case, these tasks will only terminate upon
     * <tt>shutdownNow</tt>, or after setting the policy to
     * <tt>false</tt> when already shutdown. This value is by default
     * false.
     * @param value if true, continue after shutdown, else don't.
     * @see #getExecuteExistingDelayedTasksAfterShutdownPolicy
     */
    public void setContinueExistingPeriodicTasksAfterShutdownPolicy(boolean value) {
        continueExistingPeriodicTasksAfterShutdown = value;
        if (!value && isShutdown())
            cancelUnwantedTasks();
    }

    /**
     * Get the policy on whether to continue executing existing
     * periodic tasks even when this executor has been
     * <tt>shutdown</tt>. In this case, these tasks will only
     * terminate upon <tt>shutdownNow</tt> or after setting the policy
     * to <tt>false</tt> when already shutdown. This value is by
     * default false.
     * @return true if will continue after shutdown.
     * @see #setContinueExistingPeriodicTasksAfterShutdownPolicy
     */
    public boolean getContinueExistingPeriodicTasksAfterShutdownPolicy() {
        return continueExistingPeriodicTasksAfterShutdown;
    }

    /**
     * Set policy on whether to execute existing delayed
     * tasks even when this executor has been <tt>shutdown</tt>. In
     * this case, these tasks will only terminate upon
     * <tt>shutdownNow</tt>, or after setting the policy to
     * <tt>false</tt> when already shutdown. This value is by default
     * true.
     * @param value if true, execute after shutdown, else don't.
     * @see #getExecuteExistingDelayedTasksAfterShutdownPolicy
     */
    public void setExecuteExistingDelayedTasksAfterShutdownPolicy(boolean value) {
        executeExistingDelayedTasksAfterShutdown = value;
        if (!value && isShutdown())
            cancelUnwantedTasks();
    }

    /**
     * Get policy on whether to execute existing delayed
     * tasks even when this executor has been <tt>shutdown</tt>. In
     * this case, these tasks will only terminate upon
     * <tt>shutdownNow</tt>, or after setting the policy to
     * <tt>false</tt> when already shutdown. This value is by default
     * true.
     * @return true if will execute after shutdown.
     * @see #setExecuteExistingDelayedTasksAfterShutdownPolicy
     */
    public boolean getExecuteExistingDelayedTasksAfterShutdownPolicy() {
        return executeExistingDelayedTasksAfterShutdown;
    }


    /**
     * Initiates an orderly shutdown in which previously submitted
     * tasks are executed, but no new tasks will be accepted. If the
     * <tt>ExecuteExistingDelayedTasksAfterShutdownPolicy</tt> has
     * been set <tt>false</tt>, existing delayed tasks whose delays
     * have not yet elapsed are cancelled. And unless the
     * <tt>ContinueExistingPeriodicTasksAfterShutdownPolicy</tt> has
     * been set <tt>true</tt>, future executions of existing periodic
     * tasks will be cancelled.
     */
    public void shutdown() {
        cancelUnwantedTasks();
        super.shutdown();
    }

    /**
     * Attempts to stop all actively executing tasks, halts the
     * processing of waiting tasks, and returns a list of the tasks that were
     * awaiting execution. 
     *  
     * <p>There are no guarantees beyond best-effort attempts to stop
     * processing actively executing tasks.  This implementation
     * cancels tasks via {@link Thread#interrupt}, so if any tasks mask or
     * fail to respond to interrupts, they may never terminate.
     *
     * @return list of tasks that never commenced execution.  Each
     * element of this list is a {@link ScheduledFuture},
     * including those tasks submitted using <tt>execute</tt>, which
     * are for scheduling purposes used as the basis of a zero-delay
     * <tt>ScheduledFuture</tt>.
     */
    public List<Runnable> shutdownNow() {
        return super.shutdownNow();
    }

    /**
     * Returns the task queue used by this executor.  Each element of
     * this queue is a {@link ScheduledFuture}, including those
     * tasks submitted using <tt>execute</tt> which are for scheduling
     * purposes used as the basis of a zero-delay
     * <tt>ScheduledFuture</tt>. Iteration over this queue is
     * <em>not</em> guaranteed to traverse tasks in the order in
     * which they will execute.
     *
     * @return the task queue
     */
    public BlockingQueue<Runnable> getQueue() {
        return super.getQueue();
    }

    /**
     * An annoying wrapper class to convince generics compiler to
     * use a DelayQueue<ScheduledFutureTask> as a BlockingQueue<Runnable>
     */ 
    private static class DelayedWorkQueue 
        extends AbstractCollection<Runnable> 
        implements BlockingQueue<Runnable> {
        
        private final DelayQueue<ScheduledFutureTask> dq = new DelayQueue<ScheduledFutureTask>();
        public Runnable poll() { return dq.poll(); }
        public Runnable peek() { return dq.peek(); }
        public Runnable take() throws InterruptedException { return dq.take(); }
        public Runnable poll(long timeout, TimeUnit unit) throws InterruptedException {
            return dq.poll(timeout, unit);
        }

        public boolean add(Runnable x) { return dq.add((ScheduledFutureTask)x); }
        public boolean offer(Runnable x) { return dq.offer((ScheduledFutureTask)x); }
        public void put(Runnable x)  {
            dq.put((ScheduledFutureTask)x); 
        }
        public boolean offer(Runnable x, long timeout, TimeUnit unit) {
            return dq.offer((ScheduledFutureTask)x, timeout, unit);
        }

        public Runnable remove() { return dq.remove(); }
        public Runnable element() { return dq.element(); }
        public void clear() { dq.clear(); }
        public int drainTo(Collection<? super Runnable> c) { return dq.drainTo(c); }
        public int drainTo(Collection<? super Runnable> c, int maxElements) { 
            return dq.drainTo(c, maxElements); 
        }

        public int remainingCapacity() { return dq.remainingCapacity(); }
        public boolean remove(Object x) { return dq.remove(x); }
        public boolean contains(Object x) { return dq.contains(x); }
        public int size() { return dq.size(); }
        public boolean isEmpty() { return dq.isEmpty(); }
        public Object[] toArray() { return dq.toArray(); }
        public <T> T[] toArray(T[] array) { return dq.toArray(array); }
        public Iterator<Runnable> iterator() { 
            return new Iterator<Runnable>() {
                private Iterator<ScheduledFutureTask> it = dq.iterator();
                public boolean hasNext() { return it.hasNext(); }
                public Runnable next() { return it.next(); }
                public void remove() {  it.remove(); }
            };
        }
    }
   
}
