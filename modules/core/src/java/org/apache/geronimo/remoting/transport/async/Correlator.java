/*
 * The Apache Software License, Version 1.1
 *
 *
 * Copyright (c) 1999 The Apache Software Foundation.  All rights
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
 * 4. The names "Xalan" and "Apache Software Foundation" must
 *    not be used to endorse or promote products derived from this
 *    software without prior written permission. For written
 *    permission, please contact apache@apache.org.
 *
 * 5. Products derived from this software may not be called "Apache",
 *    nor may "Apache" appear in their name, without prior written
 *    permission of the Apache Software Foundation.
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
 * individuals on behalf of the Apache Software Foundation and was
 * originally based on software copyright (c) 1999, Lotus
 * Development Corporation., http://www.lotus.com.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 */
package org.apache.geronimo.remoting.transport.async;

import java.util.Map;
import java.util.WeakHashMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import EDU.oswego.cs.dl.util.concurrent.Slot;

/**
 * Allows you to create a request to which
 * you can at a later time wait for the response to 
 * arrive asynchrously.
 * 
 * @version $Revision: 1.1 $ $Date: 2003/08/22 02:23:26 $
 */
public class Correlator {

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

    private Map slots = new WeakHashMap(100);
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
            slots.put(key, s);
        }
        if (log.isTraceEnabled())
            log.trace("Created Request: " + key.value);
        return s;
    }

    public void dispatchResponse(int id, Object data) {
        if (log.isTraceEnabled())
            log.trace("Received resposne for request: " + id);

        FutureResult s;
        synchronized (slots) {
            s = (FutureResult) slots.get(new HashKey(id));
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
}
