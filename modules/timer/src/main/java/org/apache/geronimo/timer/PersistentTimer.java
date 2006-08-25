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
import java.util.Collection;

import javax.transaction.RollbackException;
import javax.transaction.SystemException;

import org.apache.geronimo.timer.PersistenceException;

/**
 *
 *
 * @version $Rev$ $Date$
 *
 * */
public interface PersistentTimer {
    WorkInfo schedule(UserTaskFactory userTaskFactory, String key, Object userId, Object userInfo, long delay) throws PersistenceException, RollbackException, SystemException;

    WorkInfo schedule(String key, UserTaskFactory userTaskFactory, Object userId, Object userInfo, Date time) throws PersistenceException, RollbackException, SystemException;

    WorkInfo schedule(String key, UserTaskFactory userTaskFactory, Object userInfo, long delay, long period, Object userId) throws PersistenceException, RollbackException, SystemException;

    WorkInfo schedule(String key, UserTaskFactory userTaskFactory, Object userId, Object userInfo, Date firstTime, long period) throws PersistenceException, RollbackException, SystemException;

    WorkInfo scheduleAtFixedRate(String key, UserTaskFactory userTaskFactory, Object userId, Object userInfo, long delay, long period) throws PersistenceException, RollbackException, SystemException;

    WorkInfo scheduleAtFixedRate(String key, UserTaskFactory userTaskFactory, Object userId, Object userInfo, Date firstTime, long period) throws PersistenceException, RollbackException, SystemException;

    Collection playback(String key, UserTaskFactory userTaskFactory) throws PersistenceException;

    Collection getIdsByKey(String key, Object userId) throws PersistenceException;

    WorkInfo getWorkInfo(Long id);

    void cancelTimerTasks(Collection ids);
}
