/**
 *
 * Copyright 2004 The Apache Software Foundation
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

package org.apache.geronimo.messaging.interceptors;

import org.apache.geronimo.messaging.Msg;

/**
 * Throwables trap.
 * <BR>
 * It pushes the received Msgs to its delegate. If its delegate throws a
 * Throwable, it traps it and notifies the associated ThrowableTrapHandler.
 *
 * @version $Rev$ $Date$
 */
public class ThrowableTrapOutInterceptor implements MsgOutInterceptor
{

    /**
     * Delegate.
     */
    private final MsgOutInterceptor out;
    
    /**
     * Throwable handler.
     */
    private final ThrowableTrapHandler handler;

    /**
     * Creates an exception trap.
     * 
     * @param anOut Output to be filtered..
     * @param anHandler Exception handler to be notified if received Msgs are
     * not successfully pushed to anOut.
     */
    public ThrowableTrapOutInterceptor(MsgOutInterceptor anOut,
        ThrowableTrapHandler anHandler) {
        if ( null == anOut ) {
            throw new IllegalArgumentException("MsgOut is required.");
        } else if ( null == anHandler ) {
            throw new IllegalArgumentException("Handler is required.");
        }
        out = anOut;
        handler = anHandler;
    }
    
    public void push(Msg aMsg) {
        try {
            out.push(aMsg);
        } catch (Throwable e) {
            handler.push(aMsg, e);
        }
    }

    /**
     * When a Msg is not successfully (a Throwable is thrown) pushed to the
     * filtered Msg output, ThrowableTrapOutInterceptor notifies this instance.
     */
    public interface ThrowableTrapHandler {

        /**
         * To receive failure notification.
         * 
         * @param aMsg Msg which has not been successfully pushed.
         * @param aThrowable Throwable raised during the processing of the
         * associated Msg output.
         */
        public void push(Msg aMsg, Throwable aThrowable);

    }
    
}
