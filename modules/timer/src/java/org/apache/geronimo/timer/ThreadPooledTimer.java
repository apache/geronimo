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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;

import javax.transaction.RollbackException;
import javax.transaction.Status;
import javax.transaction.Synchronization;
import javax.transaction.SystemException;
import javax.transaction.Transaction;

import EDU.oswego.cs.dl.util.concurrent.Executor;
import org.apache.geronimo.gbean.GBeanLifecycle;
import org.apache.geronimo.gbean.WaitingException;
import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.gbean.GBeanInfoFactory;
import org.apache.geronimo.transaction.context.TransactionContext;
import org.apache.geronimo.timer.ExecutorFeedingTimerTask;
import org.apache.geronimo.timer.ExecutorTask;
import org.apache.geronimo.timer.ExecutorTaskFactory;
import org.apache.geronimo.timer.PersistenceException;
import org.apache.geronimo.timer.PersistentTimer;
import org.apache.geronimo.timer.Playback;

/**
 *
 *
 * @version $Revision: 1.1 $ $Date: 2004/07/18 22:10:56 $
 *
 * */
public class ThreadPooledTimer implements PersistentTimer, GBeanLifecycle {

    private final ExecutorTaskFactory executorTaskFactory;
    private final WorkerPersistence workerPersistence;
    private final Executor executor;

    private Timer delegate;

    private final Map idToWorkInfoMap = Collections.synchronizedMap(new HashMap());

    //default constructor for use as reference endpoint.
    public ThreadPooledTimer() {
        this(null, null, null);
    }

    public ThreadPooledTimer(ExecutorTaskFactory executorTaskFactory, WorkerPersistence workerPersistence, Executor executor) {
        this.executorTaskFactory = executorTaskFactory;
        this.workerPersistence = workerPersistence;
        this.executor = executor;
    }

    public void doStart() throws WaitingException, Exception {
        delegate = new Timer(true);
    }

    public void doStop() {
        if (delegate != null) {
            delegate.cancel();
            delegate = null;
        }
    }

    public void doFail() {
        doStop();
    }

    public WorkInfo schedule(UserTaskFactory userTaskFactory, String key, Object userInfo, long delay) throws PersistenceException, RollbackException, SystemException {
        Date time = new Date(System.currentTimeMillis() + delay);
        return schedule(key, userTaskFactory, userInfo, time);
    }

    public WorkInfo schedule(String key, UserTaskFactory userTaskFactory, Object userInfo, Date time) throws PersistenceException, RollbackException, SystemException {
        WorkInfo worker = createWorker(key, userTaskFactory, executorTaskFactory, false, userInfo, time, null);
        registerSynchronization(new ScheduleSynchronization(worker.getExecutorFeedingTimerTask(), time));
        addWorkInfo(worker);
        return worker;
    }

    public WorkInfo schedule(String key, UserTaskFactory userTaskFactory, Object userInfo, long delay, long period) throws PersistenceException, RollbackException, SystemException {
        Date time = new Date(System.currentTimeMillis() + delay);
        return schedule(key, userTaskFactory, userInfo, time, period);
    }

    public WorkInfo schedule(String key, UserTaskFactory userTaskFactory, Object userInfo, Date firstTime, long period) throws PersistenceException, RollbackException, SystemException {
        WorkInfo worker = createWorker(key, userTaskFactory, executorTaskFactory, false, userInfo, firstTime, new Long(period));
        registerSynchronization(new ScheduleRepeatedSynchronization(worker.getExecutorFeedingTimerTask(), firstTime, period));
        addWorkInfo(worker);
        return worker;
    }

    public WorkInfo scheduleAtFixedRate(String key, UserTaskFactory userTaskFactory, Object userInfo, long delay, long period) throws PersistenceException, RollbackException, SystemException {
        Date time = new Date(System.currentTimeMillis() + delay);
        return scheduleAtFixedRate(key, userTaskFactory, userInfo, time, period);
    }

    public WorkInfo scheduleAtFixedRate(String key, UserTaskFactory userTaskFactory, Object userInfo, Date firstTime, long period) throws PersistenceException, RollbackException, SystemException {
        WorkInfo worker = createWorker(key, userTaskFactory, executorTaskFactory, true, userInfo, firstTime, new Long(period));
        registerSynchronization(new ScheduleAtFixedRateSynchronization(worker.getExecutorFeedingTimerTask(), firstTime, period));
        addWorkInfo(worker);
        return worker;
    }

    public Collection playback(String key, UserTaskFactory userTaskFactory) throws PersistenceException {
        PlaybackImpl playback = new PlaybackImpl(userTaskFactory);
        workerPersistence.playback(key, playback);
        return playback.getWorkInfos();
    }

    public Collection getIdsByKey(String key) throws PersistenceException {
        return workerPersistence.getIdsByKey(key);
    }

    public WorkInfo getWorkInfo(Long id) {
        return (WorkInfo) idToWorkInfoMap.get(id);
    }

    private void addWorkInfo(WorkInfo worker) {
        idToWorkInfoMap.put(new Long(worker.getId()), worker);
    }

    void removeWorkInfo(WorkInfo workInfo) {
        idToWorkInfoMap.remove(new Long(workInfo.getId()));
    }

    void workPerformed(WorkInfo workInfo) throws PersistenceException {
        if (workInfo.isOneTime()) {
            workerPersistence.cancel(workInfo.getId());
        } else if (workInfo.getAtFixedRate()) {
            workerPersistence.fixedRateWorkPerformed(workInfo.getId());
            workInfo.nextTime();
        } else {
            workInfo.nextInterval();
            //TODO this is wrong, need different update.
            workerPersistence.fixedRateWorkPerformed(workInfo.getId());
        }
    }

    private Timer getTimer() {
        if (delegate == null) {
            throw new IllegalStateException("Timer is stopped");
        }
        return delegate;
    }

    WorkerPersistence getWorkerPersistence() {
        return workerPersistence;
    }

    Executor getExecutor() {
        return executor;
    }

    private WorkInfo createWorker(String key, UserTaskFactory userTaskFactory, ExecutorTaskFactory executorTaskFactory, boolean atFixedRate, Object userInfo, Date time, Long period) throws PersistenceException {
        WorkInfo workInfo = new WorkInfo(key, userInfo, time, period, atFixedRate);
        //save and assign id
        workerPersistence.save(workInfo);

        Runnable userTask = userTaskFactory.newTask(workInfo.getId());
        ExecutorTask executorTask = executorTaskFactory.createExecutorTask(userTask, workInfo, this);
        ExecutorFeedingTimerTask worker = new ExecutorFeedingTimerTask(workInfo, this);
        workInfo.initialize(worker, executorTask);
        return workInfo;
    }

    void registerSynchronization(Synchronization sync) throws RollbackException, SystemException {
        TransactionContext transactionContext = TransactionContext.getContext();
        Transaction transaction = transactionContext == null ? null : transactionContext.getTransaction();
        if (transaction == null) {
            sync.beforeCompletion();
            sync.afterCompletion(Status.STATUS_COMMITTED);
        } else {
            assert transactionContext.isActive(): "Trying to register a sync on an inactive transaction context";
            transaction.registerSynchronization(sync);
        }
    }

    private class ScheduleSynchronization implements Synchronization {

        private final ExecutorFeedingTimerTask worker;
        private final Date time;

        public ScheduleSynchronization(ExecutorFeedingTimerTask worker, Date time) {
            this.worker = worker;
            this.time = time;
        }

        public void beforeCompletion() {
        }

        public void afterCompletion(int status) {
            if (status == Status.STATUS_COMMITTED) {
                getTimer().schedule(worker, time);
            }
        }
    }

    private class ScheduleRepeatedSynchronization implements Synchronization {

        private final ExecutorFeedingTimerTask worker;
        private final Date time;
        private final long period;

        public ScheduleRepeatedSynchronization(ExecutorFeedingTimerTask worker, Date time, long period) {
            this.worker = worker;
            this.time = time;
            this.period = period;
        }

        public void beforeCompletion() {
        }

        public void afterCompletion(int status) {
            if (status == Status.STATUS_COMMITTED) {
                getTimer().schedule(worker, time, period);
            }
        }
    }

    private class ScheduleAtFixedRateSynchronization implements Synchronization {

        private final ExecutorFeedingTimerTask worker;
        private final Date time;
        private final long period;

        public ScheduleAtFixedRateSynchronization(ExecutorFeedingTimerTask worker, Date time, long period) {
            this.worker = worker;
            this.time = time;
            this.period = period;
        }

        public void beforeCompletion() {
        }

        public void afterCompletion(int status) {
            if (status == Status.STATUS_COMMITTED) {
                getTimer().scheduleAtFixedRate(worker, time, period);
            }
        }
    }

    private class PlaybackImpl implements Playback {

        private final UserTaskFactory userTaskFactory;

        private final Collection workInfos = new ArrayList();

        public PlaybackImpl(UserTaskFactory userTaskFactory) {
            this.userTaskFactory = userTaskFactory;
        }

        public void schedule(WorkInfo workInfo) {
            Runnable userTask = userTaskFactory.newTask(workInfo.getId());
            ExecutorTask executorTask = executorTaskFactory.createExecutorTask(userTask, workInfo, ThreadPooledTimer.this);
            ExecutorFeedingTimerTask worker = new ExecutorFeedingTimerTask(workInfo, ThreadPooledTimer.this);
            workInfo.initialize(worker, executorTask);
            if (workInfo.getPeriod() == null) {
                getTimer().schedule(worker, workInfo.getTime());
            } else if (!workInfo.getAtFixedRate()) {
                getTimer().schedule(worker, workInfo.getTime(), workInfo.getPeriod().longValue());
            } else {
                getTimer().scheduleAtFixedRate(worker, workInfo.getTime(), workInfo.getPeriod().longValue());
            }
            addWorkInfo(workInfo);
            workInfos.add(workInfo);
        }

        public Collection getWorkInfos() {
            return workInfos;
        }

    }

}
