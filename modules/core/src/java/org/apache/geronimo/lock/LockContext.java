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

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import javax.transaction.Synchronization;

/**
 *
 *
 *
 * @version $Revision: 1.5 $ $Date: 2004/03/10 09:58:43 $
 */
public abstract class LockContext implements Synchronization {
    protected final Set locks = new HashSet();

    public abstract void sharedLock(LockDomain domain, Object key) throws LockReentranceException, InterruptedException;

    public abstract void exclusiveLock(LockDomain domain, Object key) throws LockReentranceException, InterruptedException;

    public abstract void release(LockDomain domain, Object key);

    protected synchronized void sharedLock(InstanceLock lock) throws InterruptedException {
        lock.sharedLock(this);
        locks.add(lock);
    }

    protected synchronized void exclusiveLock(InstanceLock lock) throws InterruptedException {
        lock.exclusiveLock(this);
        locks.add(lock);
    }

    protected synchronized void releaseLock(InstanceLock lock) {
        lock.release(this);
        locks.remove(lock);
    }

    public void beforeCompletion() {
    }

    public void afterCompletion(int status) {
        synchronized (this) {
            for (Iterator i = locks.iterator(); i.hasNext();) {
                InstanceLock lock = (InstanceLock) i.next();
                lock.release(this);
            }
            locks.clear();
        }
    }
}
