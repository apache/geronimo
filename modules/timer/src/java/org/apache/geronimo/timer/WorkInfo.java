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

import java.util.Date;

import org.apache.geronimo.timer.ExecutorFeedingTimerTask;
import org.apache.geronimo.timer.ExecutorTask;

/**
 *
 *
 * @version $Rev$ $Date$
 *
 * */
public class WorkInfo {
    //these should be persistent.
    private long id = -1;

    //typically object name of ejb.
    private final String key;
    //typically entity pk
    private final Object userId;
    //typically serializable object ejb timer service supplies to users.
    private final Object userInfo;
    //next firing
    private Date time;
    //time between firings
    private final Long period;

    private final boolean atFixedRate;

    //these should not be persistent.
    private ExecutorFeedingTimerTask worker;
    private ExecutorTask taskWrapper;

    //wrappers to this timer service can store the wrapper here.
    private Object clientHandle;


    public WorkInfo(String key, Object userId, Object userInfo, Date time, Long period, boolean atFixedRate) {
        this.key = key;
        this.userId = userId;
        this.userInfo = userInfo;
        this.time = time;
        this.period = period;
        this.atFixedRate = atFixedRate;
    }

    public String getKey() {
        return key;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        if (this.id != -1) {
            throw new IllegalStateException("Id can be set only once!");
        }
        this.id = id;
    }

    public Object getUserId() {
        return userId;
    }

    public Object getUserInfo() {
        return userInfo;
    }

    public Date getTime() {
        return time;
    }

    public Long getPeriod() {
        return period;
    }

    public boolean getAtFixedRate() {
        return atFixedRate;
    }

    public void initialize(ExecutorFeedingTimerTask worker, ExecutorTask taskWrapper) {
        this.worker = worker;
        this.taskWrapper = taskWrapper;
    }

    public ExecutorFeedingTimerTask getExecutorFeedingTimerTask() {
        return worker;
    }

    public Runnable getExecutorTask() {
        return taskWrapper;
    }

    public Object getClientHandle() {
        return clientHandle;
    }

    public void setClientHandle(Object clientHandle) {
        this.clientHandle = clientHandle;
    }

    public boolean isOneTime() {
        return period == null;
    }

    void nextTime() {
        if (period == null) {
            throw new IllegalStateException("This is a one-time timerTask");
        }
        time = new Date(time.getTime() + period.longValue());
    }

    public void nextInterval() {
        if (period == null) {
            throw new IllegalStateException("This is a one-time timerTask");
        }
        time = new Date(System.currentTimeMillis() + period.longValue());
    }
}
