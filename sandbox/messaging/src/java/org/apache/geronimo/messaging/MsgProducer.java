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

package org.apache.geronimo.messaging;

import org.apache.geronimo.messaging.interceptors.MsgOutInterceptor;

/**
 * Components producing Msgs implement this interface. 
 *
 * @version $Rev$ $Date$
 */
public interface MsgProducer
{
    
    /**
     * Sets a mean to this instance to push Msgs to the outside world.
     *  
     * @param aMsgOut Used by this instance to push Msgs to other components.
     */
    public void setMsgProducerOut(MsgOutInterceptor aMsgOut);
    
}
