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
 * Used to pop Msgs from a MsgQueue.
 *
 * @version $Revision: 1.1 $ $Date: 2004/05/11 12:06:40 $
 */
public class QueueInInterceptor
    implements MsgInInterceptor
{

    /**
     * Source MsgQueue.
     */
    private final MsgQueue queue;
    
    /**
     * Pop Msg from a Queue. 
     *  
     * @param aQueue Queue from which Msg are popped.
     */
    public QueueInInterceptor(MsgQueue aQueue) {
        if ( null == aQueue ) {
            throw new IllegalArgumentException("Queue is required");
        }
        queue = aQueue;
    }
    
    public Msg pop() {
        return queue.remove();
    }

}
