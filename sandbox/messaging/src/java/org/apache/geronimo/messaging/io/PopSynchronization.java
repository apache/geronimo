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
 * popped from an InputStream.
 *
 * @version $Rev$ $Date$
 */
public interface PopSynchronization
{
    
    /**
     * Notifies the implementation that a Msg is being popped.
     * <BR>
     * This method is called before the actual pop of the Msg.
     * 
     * @param anIn Used to read information from the input stream before
     * the Msg itself. 
     * @return Opaque object which is passed back to this instance via
     * afterPop. It can be used to pass information between a beforePop
     * and an afterPop call.
     * @throws IOException Indicates that an I/O error has occured.
     */
    public Object beforePop(StreamInputStream anIn)
        throws IOException ;
    
    /**
     * Notifies the implementation that a Msg has been popped.
     * 
     * @param anIn Used to read information from the input stream after
     * the Msg itself. 
     * @param aMsg Msg which has just been popped.
     * @param anOpaque Value returned by beforePop.
     * @throws IOException Indicates that an I/O error has occured.
     */
    public void afterPop(StreamInputStream anIn, Msg aMsg, Object anOpaque)
        throws IOException;
    
}