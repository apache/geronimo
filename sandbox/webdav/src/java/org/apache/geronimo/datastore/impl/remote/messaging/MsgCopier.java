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

package org.apache.geronimo.datastore.impl.remote.messaging;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Processor in charge of copying Msgs coming from inbound Msg interceptor
 * to outbound Msg interceptors.
 *
 * @version $Revision: 1.1 $ $Date: 2004/02/25 13:36:15 $
 */
public class MsgCopier
    implements Processor
{

    private static final Log log = LogFactory.getLog(MsgCopier.class);
    
    /**
     * Null listener.
     */
    private static final NullCopierListener NULL_LISTENER =
        new NullCopierListener(); 
    
    /**
     * Inbound interceptor from which Msgs are popped.
     */
    private final MsgInInterceptor in;
    
    /**
     * Outbound interceptor to which Msgs are pushed.
     */
    private final MsgOutInterceptor[] outs;
    
    /**
     * Client callback.
     */
    private final CopierListener listener;
    
    /**
     * Indicates if this Processor is started. 
     */
    private volatile boolean isStarted;
    
    /**
     * Creates a copier which copies anIn to anOut and notifies aListener.
     * 
     * @param anIn Source.
     * @param anOut Destination.
     * @param aListener Callback.
     */
    public MsgCopier(MsgInInterceptor anIn, MsgOutInterceptor anOut,
        CopierListener aListener) {
        this(anIn, new MsgOutInterceptor[] {anOut}, aListener);
    }
    
    /**
     * Creates a copier, which copies anIn to anOuts.
     * 
     * @param anIn Msgs to be copied.
     * @param anOuts Targets of the copy.
     * @param aListener Client call-back.
     */
    public MsgCopier(MsgInInterceptor anIn, MsgOutInterceptor[] anOuts,
        CopierListener aListener) {
        if ( null == anIn ) {
            throw new IllegalArgumentException("BlockInput is required.");
        } else if ( null == anOuts || 0 == anOuts.length ) {
            throw new IllegalArgumentException("BlockOutputs is required.");
        }
        in = anIn;
        outs = anOuts;
        if ( null == aListener ) {
            listener = NULL_LISTENER;
        } else {
            listener = aListener;
        }
        isStarted = true;
    }

    public void run() {
        listener.onStart();
        try {
            while ( isStarted ) {
                copy();
            }
        } catch (Throwable e) {
            log.error(e);
            listener.onFailure();
        } finally {
            listener.onStop();
        }
    }

    public void release() {
        isStarted = false;
    }
    
    /**
     * Actual copy.
     */
    private void copy() {
        Msg msg;
        synchronized (in) {
            msg = in.pop();
        }
        for (int i = 0; i < outs.length; i++) {
            MsgOutInterceptor out = outs[i];
            synchronized (out) {
                out.push(new Msg(msg));
            }
        }
        listener.onCopy();
    }
    
    /**
     * Life-cycle listener.
     */
    public interface CopierListener {
        // Called when the copier is started.
        public void onStart();
        // Called when the copier is stopped.
        public void onStop();
        // Called when the copier has performed a copy. May be used to monitor
        public void onCopy();
        // Called when the copier fails. 
        public void onFailure();
    }

    /**
     * Null CopierListener.
     */
    public static class NullCopierListener implements CopierListener {
        public void onStart() {}
        public void onStop() {}
        public void onCopy() {}
        public void onFailure() {}
    }
    
}
