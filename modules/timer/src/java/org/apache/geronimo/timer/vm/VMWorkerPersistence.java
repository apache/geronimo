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

package org.apache.geronimo.timer.vm;

import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Collection;
import java.util.ArrayList;

import EDU.oswego.cs.dl.util.concurrent.SynchronizedLong;
import org.apache.geronimo.timer.PersistenceException;
import org.apache.geronimo.timer.Playback;
import org.apache.geronimo.timer.WorkInfo;
import org.apache.geronimo.timer.WorkerPersistence;

/**
 *
 *
 * @version $Revision: 1.2 $ $Date: 2004/07/20 23:36:53 $
 *
 * */
public class VMWorkerPersistence implements WorkerPersistence {

    private final Map tasks = Collections.synchronizedMap(new LinkedHashMap());

    private final SynchronizedLong counter = new SynchronizedLong(0);

    public void save(WorkInfo workInfo) throws PersistenceException {
        long id = counter.increment();
        workInfo.setId(id);
        tasks.put(new Long(id), workInfo);
    }

    public void cancel(long id) throws PersistenceException {
        tasks.remove(new Long(id));
    }

    public void playback(String key, Playback playback) throws PersistenceException {
        synchronized (tasks) {
            for (Iterator iterator = tasks.entrySet().iterator(); iterator.hasNext();) {
                Map.Entry entry = (Map.Entry) iterator.next();
                WorkInfo workInfo = (WorkInfo) entry.getValue();
                playback.schedule(workInfo);
            }
        }
    }

    public void fixedRateWorkPerformed(long id) throws PersistenceException {
        //don't do anything, we are sharing the object with NonTransactionalWork, which is incrementing the time itself.
//        Long key = new Long(id);
//        synchronized (tasks) {
//            WorkInfo task = (WorkInfo) tasks.get(key);
//            task.nextTime();
//            //see if task was cancelled while we executed.
//            if (task != null) {
//                tasks.put(key, TaskWrapper.nextTask(task));
//            }
//        }
    }

    public void intervalWorkPerformed(long id, long period) throws PersistenceException {
        //dont do anything... sharing data with WorkInfo.
    }

    public Collection getIdsByKey(String key, Object userId) throws PersistenceException {
        Collection ids = new ArrayList();
        for (Iterator iterator = tasks.values().iterator(); iterator.hasNext();) {
            WorkInfo workInfo = (WorkInfo) iterator.next();
            if (key.equals(workInfo.getKey()) && (userId == null || userId.equals(workInfo.getUserId()))) {
                ids.add(new Long(workInfo.getId()));
            }
        }
        return ids;
    }

}
