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

package org.apache.geronimo.remoting.transport.async;


/**
 * This interface should be implemented by objects that wants to 
 * receive Channel events.
 * 
 * @version $Revision: 1.2 $ $Date: 2004/02/25 09:58:04 $
 */
public interface ChannelListner {
    
    /**
     * Sends an asynch packet of data down the channel.  It does not 
     * wait wait for a response if possible.
     */
    public void receiveEvent(AsyncMsg data);

    /**
    	* The remote end closed the connection.  The receiver of this event
    	* should close() the channel.
    	*/
    public void closeEvent();

}
