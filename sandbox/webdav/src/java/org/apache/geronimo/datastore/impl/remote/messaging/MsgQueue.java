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

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * A named queue. It is a staging repository for Msgs.
 *
 * @version $Revision: 1.1 $ $Date: 2004/02/25 13:36:15 $
 */
public class MsgQueue
{

    private static final Log log = LogFactory.getLog(MsgQueue.class);
    
    /**
     * Actual queue.
     */
    private final List queue;
    
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
        queue = new ArrayList();
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
        synchronized(queue) {
            queue.add(aMessage);
            queue.notify();
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
        synchronized (queue) {
            while ( queue.isEmpty() ) {
                try {
                    queue.wait();
                } catch (InterruptedException e) {
                    log.error(e);
                }
            }
            message = (Msg) queue.remove(0);
        }
        log.trace("Message removed from queue {" + name + "}");
        return message;
    }
    
}
