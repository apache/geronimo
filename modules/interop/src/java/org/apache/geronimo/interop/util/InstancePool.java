/**
 *
 *  Copyright 2004-2005 The Apache Software Foundation
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
 *
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.apache.geronimo.interop.util;




public class InstancePool {
    // -----------------------------------------------------------------------
    // inner classes
    // -----------------------------------------------------------------------

    private static class Entry {
        Object object;
        long timeout;
        Entry next;
    }

    // -----------------------------------------------------------------------
    // private data
    // -----------------------------------------------------------------------

    private Entry _stack = null;

    private Entry _freeList = null;

    private long _idleTimeout;

    // -----------------------------------------------------------------------
    // public methods
    // -----------------------------------------------------------------------

    public InstancePool(String name) {
        init(name, 0); //, null);
    }

    public InstancePool(String name, long idleTimeout) // , TimeoutObject timeoutObject)
    {
        init(name, idleTimeout); //, timeoutObject);
    }

    public Object get() {
        synchronized (this) {
            Entry top = _stack;
            if (top != null) {
                _stack = top.next;
                //_size.decrement();
                Object object = top.object;
                top.object = null;
                top.next = _freeList;
                _freeList = top;
                return object;
            } else {
                return null;
            }
        }
    }

    public void put(Object object) {
        synchronized (this) {
            //_size.increment();
            Entry top = _freeList;
            if (top != null) {
                _freeList = top.next;
            } else {
                top = new Entry();
            }
            top.object = object;
            if (_idleTimeout > 0) {
                top.timeout = System.currentTimeMillis() + _idleTimeout;
            }
            top.next = _stack;
            _stack = top;
        }
    }

    // -----------------------------------------------------------------------
    // private methods
    // -----------------------------------------------------------------------

    private void init(final String name, long idleTimeout) //, final TimeoutObject timeoutObject)
    {
        //_size = sizeStatistic.getInstance(name);
        _idleTimeout = idleTimeout;

        if (_idleTimeout > 0) {
            /*
            long now = SystemClock.getLastSampleTime();
            final long checkInterval = _idleTimeout > 10 ? (_idleTimeout / 10) : _idleTimeout;
            Task timeoutTask = new Task()
            {
                public long run(long time)
                {
                    Entry restoreStack = null;
                    Entry timeoutChain = null;
                    synchronized (InstancePool.this)
                    {
                        while (_stack != null)
                        {
                            Entry entry = _stack;
                            _stack = entry.next;
                            if (entry.timeout > time)
                            {
                                entry.next = restoreStack;
                                restoreStack = entry;
                            }
                            else
                            {
                                entry.next = timeoutChain;
                                timeoutChain = entry;
                                _size.decrement();
                            }
                        }

                        // Restore still-active entries to the stack in
                        // their original order. This ensures that less
                        // frequently used entries stay at the bottom of
                        // the stack, becoming elegible for timeout.
                        while (restoreStack != null)
                        {
                            Entry entry = restoreStack;
                            restoreStack = entry.next;
                            entry.next = _stack;
                            _stack = entry;
                        }
                    }
                    while (timeoutChain != null)
                    {
                        Entry entry = timeoutChain;
                        timeoutChain = entry.next;
                        try
                        {
                            timeoutObject.onTimeout(entry.object);
                        }
                        catch (Throwable ex)
                        {
                            ExceptionLog.getInstance().log(ex, InstancePool.class.getName() + ".onTimeout(" + name + ")");
                        }
                        entry.object = null;
                        entry.next = null;
                    }
                    return time + checkInterval;
                }
            }
            ;
            TaskScheduler.getInstance().start(timeoutTask, now);
            */
        }
    }
}
