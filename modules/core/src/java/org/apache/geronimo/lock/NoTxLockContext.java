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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 *
 *
 *
 * @version $Revision: 1.4 $ $Date: 2004/03/10 09:58:43 $
 */
public class NoTxLockContext extends LockContext {
    private final static Log log = LogFactory.getLog(LockContext.class);
    private boolean isTrace = log.isTraceEnabled();

    public void sharedLock(LockDomain domain, Object key) throws LockReentranceException, InterruptedException {
        if (isTrace) {
            log.trace("NoTx Shared Lock domain=" + domain + ", id=" + key);
        }
        InstanceLock lock = domain.getLock(key);
        sharedLock(lock);
    }

    public void exclusiveLock(LockDomain domain, Object key) throws LockReentranceException, InterruptedException {
        if (isTrace) {
            log.trace("NoTx Exclusive Lock domain=" + domain + ", id=" + key);
        }
        InstanceLock lock = domain.getLock(key);
        exclusiveLock(lock);
    }

    public void release(LockDomain domain, Object key) {
        if (isTrace) {
            log.trace("NoTx Release Lock domain=" + domain + ", id=" + key);
        }
        InstanceLock lock = domain.getLock(key);
        releaseLock(lock);
    }
}
