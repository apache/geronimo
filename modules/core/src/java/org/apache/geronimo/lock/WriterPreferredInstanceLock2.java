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


/**
 * Implementation of InstanceLock using a simple prefer-writer allocation
 * policy.
 * <ul>
 * <li>Contexts requesting exclusive locks will be scheduled before those requesting shared locks</li>
 * <li>A request for an exclusive lock causes requests for shared locks to block
 *     until all exclusive locks have been released.</li>
 * <li>When an exclusive lock is released, any other requests for exclusive locks
 *     will be scheduled before any requests for shared locks</li>
 * </ul>
 * If the workload makes many exclusive requests, this policy may result in
 * starvation of shared requests.
 *
 *
 * @version $Revision: 1.5 $ $Date: 2004/03/10 09:58:43 $
 */
public class WriterPreferredInstanceLock2 implements InstanceLock {
    private Object exclActive;
    private int sharedCount = 0;
    private int exclWaiting = 0;
    private int sharedWaiting = 0;
    private final Object exclLock = new Object();

    public synchronized void sharedLock(Object context) throws InterruptedException {
        assert context != null;
        // we can get the lock immediately if no-one has or is waiting
        // for an exclusive lock
        if (exclActive == null && exclWaiting == 0) {
            sharedCount++;
            return;
        }

        // we will have to wait...
        sharedWaiting++;
        while (true) {
            try {
                wait();
                // can we get the lock now?
                if (exclActive == null && exclWaiting == 0) {
                    sharedWaiting--;
                    sharedCount++;
                    return;
                }
            } catch (InterruptedException e) {
                // we were interrupted whilst waiting for the lock
                // no-one else really cares
                sharedWaiting--;
                throw e;
            }
        }
    }

    public synchronized void exclusiveLock(Object context) throws InterruptedException {
        assert context != null;
        // we can get the lock immediately if no-one has it and
        // no-one has a shared lock
        if (exclActive == null && sharedCount == 0) {
            exclActive = context;
            return;
        }

        // we will have to wait...
        exclWaiting++;
        while (true) {
            try {
                wait();
                // can we get the lock now?
                if (exclActive == null && sharedCount == 0) {
                    exclWaiting--;
                    exclActive = context;
                    return;
                }
            } catch (InterruptedException e) {
                // we were interrupted whilst waiting for the lock
                exclWaiting--;

                // was the fact that we were waiting stopping anyone
                // else from getting it?
                if (exclWaiting > 0) {
                    // trade-off - context switch vs. length of hold
                    // we choose to notify everyone on the assumption
                    // the first holder will release before the
                    // second gets scheduled
                    notifyAll();
                } else if (sharedWaiting > 0) {
                    notifyAll();
                }
                throw e;
            }
        }
    }


    public synchronized void release(Object context) {
        assert context != null;
        if (exclActive == context) {
            exclActive = null;

            // is anyone waiting for me to release?
            // give priority to those requesting exclusive access
            if (exclWaiting > 0) {
                notifyAll();
            } else if (sharedWaiting > 0) {
                notifyAll();
            }
        } else {
            sharedCount--;
            if (sharedCount == 0 && exclWaiting > 0) {
                // I just released the last shared lock and someone is
                // waiting for an exclusive, to its time to notify them
                // again wake everyone - see above
                notifyAll();
            }
        }
    }

    public synchronized int getSharedCount() {
        return sharedCount;
    }

    public synchronized int getSharedWaiting() {
        return sharedWaiting;
    }
}
