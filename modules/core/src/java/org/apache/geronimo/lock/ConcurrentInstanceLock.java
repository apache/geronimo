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

package org.apache.geronimo.lock;

import java.util.HashSet;

import EDU.oswego.cs.dl.util.concurrent.ReadWriteLock;
import EDU.oswego.cs.dl.util.concurrent.WriterPreferenceReadWriteLock;

/**
 * Implementation based on the use of the util.concurrent package from Doug Lea.
 *
 * @version $Revision: 1.4 $ $Date: 2004/02/25 09:57:28 $
 */
public class ConcurrentInstanceLock implements InstanceLock {
    private final ReadWriteLock lock;
    private Object writer;
    private final HashSet readers = new HashSet();

    public ConcurrentInstanceLock() {
        this.lock = new WriterPreferenceReadWriteLock();
    }

    public void sharedLock(Object context) throws InterruptedException {
        lock.readLock().acquire();
        synchronized (this) {
            readers.add(context);
        }
    }

    public void exclusiveLock(Object context) throws InterruptedException {
        lock.writeLock().acquire();
        synchronized (this) {
            writer = context;
        }
    }

    public synchronized void release(Object context) {
        if (writer == context) {
            writer = null;
            lock.writeLock().release();
        } else {
            readers.remove(context);
            lock.readLock().release();
        }
    }

    public synchronized int getSharedCount() {
        return readers.size();
    }
}
