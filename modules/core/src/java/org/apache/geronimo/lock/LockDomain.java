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

import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.Map;


/**
 *
 *
 *
 * @version $Revision: 1.1 $ $Date: 2003/08/10 20:55:14 $
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
        assert (key != null);
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
