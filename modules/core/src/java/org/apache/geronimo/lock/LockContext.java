/* ====================================================================
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 2003 The Apache Software Foundation.  All rights
 * reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. The end-user documentation included with the redistribution,
 *    if any, must include the following acknowledgment:
 *       "This product includes software developed by the
 *        Apache Software Foundation (http://www.apache.org/)."
 *    Alternately, this acknowledgment may appear in the software itself,
 *    if and wherever such third-party acknowledgments normally appear.
 *
 * 4. The names "Apache" and "Apache Software Foundation" and
 *    "Apache Geronimo" must not be used to endorse or promote products
 *    derived from this software without prior written permission. For
 *    written permission, please contact apache@apache.org.
 *
 * 5. Products derived from this software may not be called "Apache",
 *    "Apache Geronimo", nor may "Apache" appear in their name, without
 *    prior written permission of the Apache Software Foundation.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE APACHE SOFTWARE FOUNDATION OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 *
 * ====================================================================
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
 * @version $Revision: 1.3 $ $Date: 2003/08/11 17:59:12 $
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
