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
 * Interface providing a pessimistic lock mechanism to an instance of a
 * resource. Access is controlled by shared and exclusive locks; multiple
 * contexts can hold a shared lock at the same time, but only one may hold
 * an exclusive lock at any time. Lock allocation policy is determined by the
 * implementation and may or may not be fair.
 *
 *
 * @version $Revision: 1.4 $ $Date: 2004/03/10 09:58:43 $
 */
public interface InstanceLock {
    /**
     * Provide context with shared access to this resource.
     * @param context the context requesting access
     * @throws java.lang.InterruptedException if the thread is interrupted before receiving the lock
     */
    public void sharedLock(Object context) throws InterruptedException;

    /**
     * Provide context with exclusive access to this resource.
     * @param context the context requesting access
     * @throws java.lang.InterruptedException if the thread is interrupted before receiving the lock
     */
    public void exclusiveLock(Object context) throws InterruptedException;

    /**
     * Notification from context that it no longer requires access to this resource
     * @param context the context relinguishing access
     */
    public void release(Object context);
}
