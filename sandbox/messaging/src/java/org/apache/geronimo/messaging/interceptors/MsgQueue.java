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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.geronimo.messaging.Msg;

import EDU.oswego.cs.dl.util.concurrent.LinkedQueue;

/**
 * A named queue. It is a staging repository for Msgs.
 *
 * @version $Revision: 1.1 $ $Date: 2004/05/11 12:06:40 $
 */
public class MsgQueue
{

    private static final Log log = LogFactory.getLog(MsgQueue.class);
    
    /**
     * Actual queue.
     */
    private final LinkedQueue queue;
    
    /**
     * Name of this queue.
     */
    private final String name;
    
    /**
     * Creates a queue having the specified name.
     * 
     * @param aName Name of the queue.
     */
    public MsgQueue(String aName) {
        if ( null == aName ) {
            throw new IllegalArgumentException("Name is required.");
        }
        name = aName;
        queue = new LinkedQueue();
    }
    
    /**
     * Adds a Message to this queue.
     * 
     * @param aMessage Message to be queued.
     */
    public void add(Msg aMessage) {
        if ( null == aMessage ) {
            throw new IllegalArgumentException("Message must be defined.");
        }
        try {
            queue.put(aMessage);
        } catch (InterruptedException e) {
            log.error(e);
            throw new MsgInterceptorStoppedException(e);
        }
        log.trace("Message added to queue {" + name + "}");
    }
    
    /**
     * Removes a Message from this queue.
     * 
     * @return Message.
     */
    public Msg remove() {
        Msg message;
        try {
            message = (Msg) queue.take();
        } catch (InterruptedException e) {
            log.error(e);
            throw new MsgInterceptorStoppedException(e);
        }
        log.trace("Message removed from queue {" + name + "}");
        return message;
    }
    
    public String toString() {
        return "MsgQueue {" + name + "}";
    }
    
}
