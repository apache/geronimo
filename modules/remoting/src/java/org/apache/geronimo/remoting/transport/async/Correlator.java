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

package org.apache.geronimo.remoting.transport.async;

import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.Map;

import EDU.oswego.cs.dl.util.concurrent.Slot;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


/**
 * Allows you to create a request to which
 * you can at a later time wait for the response to
 * arrive asynchrously.
 *
 * @version $Revision: 1.4 $ $Date: 2004/03/10 09:59:20 $
 */
public class Correlator {
    private final ReferenceQueue queue = new ReferenceQueue();

    static public class FutureResult extends Slot {
        private final HashKey key;

        FutureResult(HashKey key) {
            this.key = key;
        }

        public int getID() {
            return key.value;
        }
    }

    Log log = LogFactory.getLog(Correlator.class);

    /**
     * Since a incrementing int is used as a key to a HashMap,
     * this class is used to calculate a hashCode that is more
     * spred to get better hashing.
     */
    private static class HashKey {
        final int value;
        final int hashCode;

        public HashKey(int value) {
            this.value = value;
            long rc = value;
            if ((value % 2) == 1)
                rc *= -1;
            rc *= Integer.MAX_VALUE / 7;
            hashCode = (int) (rc % Integer.MAX_VALUE);
        }

        /**
         * Not a very proper equals since it takes
         * shortcuts, but since this class is not used
         * in a general case, it's ok.
         */
        public boolean equals(Object obj) {
            return ((HashKey) obj).value == value;
        }

        public int hashCode() {
            return hashCode;
        }
    }

    private class SlotReference extends WeakReference {
        private final Object key;

        public SlotReference(Object key, Object lock) {
            super(lock, queue);
            this.key = key;
        }
    }

    private Map slots = new HashMap(100);
    private int nextFutureResultID = 0;
    private Object nextFutureResultIDLock = new Object();

    private int getNextFutureResultID() {
        synchronized (nextFutureResultIDLock) {
            return nextFutureResultID++;
        }
    }

    public FutureResult getNextFutureResult() {
        HashKey key = new HashKey(getNextFutureResultID());
        FutureResult s = new FutureResult(key);
        synchronized (slots) {
            slots.put(key, new SlotReference(key, s));
        }
        if (log.isTraceEnabled())
            log.trace("Created Request: " + key.value);
        return s;
    }

    public void dispatchResponse(int id, Object data) {
        if (log.isTraceEnabled())
            log.trace("Received resposne for request: " + id);

        FutureResult s = null;
        synchronized (slots) {
            processQueue();
            SlotReference ref = (SlotReference) slots.get(new HashKey(id));
            if (ref != null) {
                s = (FutureResult) ref.get();
            }
        }
        if (s != null) {
            try {
                s.put(data);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        } else {
            log.trace("The request may have timed out.  Request slot was not found");
        }

    }

    private void processQueue() {
        SlotReference slotRef;
        while ((slotRef = (SlotReference) queue.poll()) != null) {
            synchronized (slots) {
                slots.remove(slotRef.key);
            }
        }
    }
}
