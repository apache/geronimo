/**
 *
 * Copyright 2003-2004 The Apache Software Foundation
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

import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.Map;


/**
 *
 *
 *
 * @version $Rev$ $Date$
 */
public class LockDomain {
    private final String name;
    private final boolean reentrant;
    private final Class lockClass;
    private final ReferenceQueue queue = new ReferenceQueue();
    private final Map locks = new HashMap();

    public LockDomain(String name, boolean reentrant, Class lockClass) {
        this.name = name;
        this.reentrant = reentrant;
        this.lockClass = lockClass;
    }

    public boolean isReentrant() {
        return reentrant;
    }

    public InstanceLock getLock(Object key) {
        assert key != null;
        InstanceLock lock;
        synchronized (locks) {
            processQueue();
            LockReference ref = (LockReference) locks.get(key);
            if (ref != null) {
                lock = (InstanceLock) ref.get();
                if (lock != null) {
                    return lock;
                }
            }

            // lock not found create a new one
            try {
                lock = (InstanceLock) lockClass.newInstance();
            } catch (InstantiationException e) {
                throw new IllegalStateException();
            } catch (IllegalAccessException e) {
                throw new IllegalStateException();
            }
            locks.put(key, new LockReference(key, lock));
        }
        return lock;
    }

    private void processQueue() {
        LockReference lockRef;
        while ((lockRef = (LockReference) queue.poll()) != null) {
            synchronized (locks) {
                locks.remove(lockRef.key);
            }
        }
    }

    public String toString() {
        return "LockDomain[" + name + "]";
    }

    private class LockReference extends WeakReference {
        private final Object key;

        public LockReference(Object key, Object lock) {
            super(lock, queue);
            this.key = key;
        }
    }
}
