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

package org.apache.geronimo.messaging.io;

import java.io.IOException;

import org.apache.geronimo.messaging.Msg;


/**
 * Allows an implementation to be notified when a Msg is about to be
 * pushed.
 * 
 * @version $Rev$ $Date$
 */
public interface PushSynchronization
{
    
    /**
     * Notifies the implementation that a Msg is being pushed.
     * <BR>
     * This method is called before the actual push of the Msg.
     * 
     * @param anOut Used to write information before the Msg itself.
     * @param aMsg Msg being pushed. 
     * @return Opaque object which is passed by to this instance via
     * afterPush. It can be used to pass information between a beforePush
     * and a afterPush call.
     * @throws IOException Indicates that an I/O error has occured.
     */
    public Object beforePush(StreamOutputStream anOut, Msg aMsg)
        throws IOException;
    
    /**
     * Notifies the implementation that a Msg has been pushed.
     * 
     * @param anOut Used to write information after the Msg itself.
     * @param aMsg Msg which has just been pushed.
     * @param anOpaque Value returned by beforePush.
     * @throws IOException Indicates that an I/O error has occured.
     */
    public void afterPush(StreamOutputStream anOut, Msg aMsg,
        Object anOpaque)
        throws IOException;
    
}
