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

package org.apache.geronimo.messaging.remotenode.admin;

import org.apache.geronimo.messaging.Msg;
import org.apache.geronimo.messaging.remotenode.RemoteNodeConnection;

/**
 * Join command reply.
 * 
 * @version $Revision: 1.1 $ $Date: 2004/06/03 14:39:44 $
 */
public class JoinReply
    implements RemoteNodeConnectionCommand
{

    /**
     * Join request.
     */
    private final Msg request;
    
    /**
     * @param aRequest Join request.
     */
    public JoinReply(Msg aRequest) {
        request = aRequest;
    }
    
	public void execute(RemoteNodeConnection aConnection) {
        Msg msg = request.reply();
        msg.getBody().setContent(Boolean.TRUE);
        aConnection.getMsgConsumerOut().push(msg);
	}

}
