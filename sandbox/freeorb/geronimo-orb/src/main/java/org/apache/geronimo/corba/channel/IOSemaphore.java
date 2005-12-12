/**
 *
 * Copyright 2005 The Apache Software Foundation or its licensors, as applicable.
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
package org.apache.geronimo.corba.channel;

import org.apache.geronimo.corba.concurrency.IOSemaphoreClosedException;


/**
 * An IOSemaphore is used for buffered IO.
 * <p/>
 * The special feature of the IOSemaphore is that it can be "closed,"
 * after which no more permits can be released.  When an IOSemaphore is
 * closed, existing permits can be acquired.  Successive attempts
 * to acquire more permits yields an IOSemaphoreClosedException.
 */

public class IOSemaphore {

    private int permits;

    private boolean isClosed;

    public IOSemaphore(int permits) {
        super();
        this.permits = permits;
    }

    public synchronized boolean isClosed() {
        return isClosed;
    }

    public void acquire() throws IOSemaphoreClosedException {
        acquire(1);
    }

    /**
     * acquire at least one, and maximum <code>max</code> permits.
     *
     * @param max
     * @return
     * @throws IOSemaphoreClosedException
     * @throws IOSemaphoreClosedException
     */

    public int acquireSome(int max) throws IOSemaphoreClosedException {
        return acquireSome(1, max, 0);
    }

    public synchronized int acquireSome(int min, int max, long time) throws IOSemaphoreClosedException {

        // the fast track //
        if (permits >= 1) {
            int howmany = Math.min(permits, max);
            permits -= howmany;
            return howmany;
        }

        long now = System.currentTimeMillis();

        long end;
        if (time == 0) {
            end = Long.MAX_VALUE;
        } else {
            end = now + Math.min(time, Long.MAX_VALUE - now);
        }

        while (permits < min) {
            if (isClosed) {
                throw new IOSemaphoreClosedException(permits);
            }

            now = System.currentTimeMillis();
            long rest = end - now;
            try {
                wait(rest);
            }
            catch (InterruptedException e) {
                // clear interrupted flag
                Thread.interrupted();

            }

        }

        int howmany = Math.min(permits, max);
        permits -= howmany;
        return howmany;
    }

    public void acquire(int i) throws IOSemaphoreClosedException {
        attempt(i, 0L);
    }

    public synchronized boolean attempt(int i, long time)
            throws IOSemaphoreClosedException
    {

        // the fast track //
        if (permits >= i) {
            permits -= i;
            return true;
        }

        long now = System.currentTimeMillis();

        long end;
        if (time == 0) {
            end = Long.MAX_VALUE;
        } else {
            end = now + Math.min(time, Long.MAX_VALUE - now);
        }

        while (permits < i) {
            if (isClosed) {
                throw new IOSemaphoreClosedException(permits);
            }

            now = System.currentTimeMillis();
            long rest = end - now;
            try {
                wait(rest);
            }
            catch (InterruptedException e) {
                // clear interrupted flag
                Thread.interrupted();

            }

        }
        permits -= i;

        return true;
    }

    public void release() throws IOSemaphoreClosedException {
        release(1);
    }

    public synchronized void release(int i) throws IOSemaphoreClosedException {
        if (isClosed) {
            throw new IOSemaphoreClosedException(permits);
        }

        permits += i;
        notifyAll();
    }

    public synchronized void close() {
        isClosed = true;
        notifyAll();
    }

    public synchronized int availablePermits() {
        return permits;
    }

    public synchronized void releaseIfNotClosed(int amount) {
        if (!isClosed) {
            permits += amount;
            notifyAll();
        }
    }
}
