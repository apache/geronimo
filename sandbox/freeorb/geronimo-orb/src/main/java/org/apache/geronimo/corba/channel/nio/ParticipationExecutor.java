/**
 *
 * Copyright 2005 The Apache Software Foundation or its licensors, as applicable.
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
package org.apache.geronimo.corba.channel.nio;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import EDU.oswego.cs.dl.util.concurrent.Executor;
import EDU.oswego.cs.dl.util.concurrent.Mutex;
import EDU.oswego.cs.dl.util.concurrent.ReentrantLock;
import EDU.oswego.cs.dl.util.concurrent.Semaphore;
import EDU.oswego.cs.dl.util.concurrent.Sync;
import EDU.oswego.cs.dl.util.concurrent.SyncMap;


public class ParticipationExecutor implements Executor {

    private SyncMap map = new SyncMap(new HashMap(), new Mutex(),
                                      new ReentrantLock());

    private final Executor backing;

    public ParticipationExecutor(Executor backing) {
        this.backing = backing;
    }

    public void execute(Runnable arg0) throws InterruptedException {

        Participation p = null;
        if (!map.isEmpty()) {
            Sync lock = map.writerSync();
            lock.acquire();
            try {
                Set set = map.entrySet();
                Iterator iter = set.iterator();
                if (iter.hasNext()) {
                    Map.Entry ent = (Map.Entry) iter.next();
                    p = (Participation) ent.getValue();
                    iter.remove();
                }
            }
            finally {
                lock.release();
            }
        }

        if (p == null) {
            backing.execute(arg0);
        } else {
            p.task = arg0;
            p.release();
        }

    }

    static class Participation extends Semaphore {

        Thread participant = Thread.currentThread();

        public Participation() {
            super(0);
        }

        Runnable task;
        public Object value;
    }

    public Object participate(Object key) {

        Participation p = new Participation();
        map.put(key, p);

        while (true) {
            try {
                p.acquire();
            }
            catch (InterruptedException e) {
                continue;
            }

            if (p.task == null) {
                return p.value;
            } else {
                try {
                    p.task.run();
                }
                catch (RuntimeException ex) {
                    ex.printStackTrace();
                }
                catch (Error ex) {
                    ex.printStackTrace();
                }
                finally {
                    p.task = null;
                    map.put(key, p);
                }
            }
        }
    }

    public void release(Object key, Object value) {
        Participation p = (Participation) map.remove(key);
        if (p != null) {
            p.value = value;
            p.release();
        }
    }

}
