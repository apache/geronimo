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
package org.apache.geronimo.network.protocol.control;

import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.Map;

import org.apache.geronimo.network.SelectorManager;
import org.apache.geronimo.system.ClockPool;
import org.apache.geronimo.system.ThreadPool;


/**
 * @version $Revision: 1.1 $ $Date: 2004/03/10 02:14:28 $
 */
public class ControlContext {

    private ThreadPool threadPool;
    private ClockPool clockPool;
    private SelectorManager selectorManager;
    private ClassLoader classLoader;
    private Map assignments = new IdentityHashMap();
    private Map registrations = new HashMap();
    private long nextId = 3;

    public ThreadPool getThreadPool() {
        return threadPool;
    }

    public void setThreadPool(ThreadPool threadPool) {
        this.threadPool = threadPool;
        registrations.put(new Long(0), threadPool);
        assignments.put(threadPool, new Long(0));
    }

    public ClockPool getClockPool() {
        return clockPool;
    }

    public void setClockPool(ClockPool clockPool) {
        this.clockPool = clockPool;
        registrations.put(new Long(1), clockPool);
        assignments.put(clockPool, new Long(1));
    }

    public SelectorManager getSelectorManager() {
        return selectorManager;
    }

    public void setSelectorManager(SelectorManager selectorManager) {
        this.selectorManager = selectorManager;
        registrations.put(new Long(2), selectorManager);
        assignments.put(selectorManager, new Long(2));
    }

    public ClassLoader getClassLoader() {
        return classLoader;
    }

    public void setClassLoader(ClassLoader classLoader) {
        this.classLoader = classLoader;
    }

    public Long assignId(Object object) {
        if (threadPool == null) throw new IllegalStateException("Thread pool not assigned");
        if (clockPool == null) throw new IllegalStateException("Clock pool not assigned");
        if (selectorManager == null) throw new IllegalStateException("Selector manager not assigned");

        Long value = (Long) assignments.get(object);
        if (value == null) {
            value = new Long(nextId++);
            assignments.put(object, value);
        }
        return value;
    }

    public void register(Long id, Object object) {
        registrations.put(id, object);
    }

    public Object retrieve(Long id) {
        if (threadPool == null) throw new IllegalStateException("Thread pool not assigned");
        if (clockPool == null) throw new IllegalStateException("Clock pool not assigned");
        if (selectorManager == null) throw new IllegalStateException("Selector manager not assigned");

        return registrations.get(id);
    }

}
