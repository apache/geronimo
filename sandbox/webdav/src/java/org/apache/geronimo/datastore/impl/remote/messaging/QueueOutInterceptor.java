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

/**
 * Used to push Msg to a MsgQueue.
 * 
 * @version $Revision: 1.1 $ $Date: 2004/02/25 13:36:15 $
 */
public class QueueOutInterceptor
    implements MsgOutInterceptor
{

    /**
     * Target MsgQueue.
     */
    private final MsgQueue queue;
    
    /**
     * Pushes Msg to the specified queue.
     * 
     * @param aQueue Target queue.
     */
    public QueueOutInterceptor(MsgQueue aQueue) {
        if ( null == aQueue ) {
            throw new IllegalArgumentException("Queue is required.");
        }
        queue = aQueue;
    }
    
    public void push(Msg aMessage) {
        queue.add(aMessage);
    }

}
