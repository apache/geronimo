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

package org.apache.geronimo.messaging.remotenode.network;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.geronimo.network.protocol.Protocol;
import org.apache.geronimo.network.protocol.SocketProtocol;

/**
 * SocketProtocol providing asynchronous callbacks upon closure.
 *
 * @version $Revision: 1.1 $ $Date: 2004/07/20 00:15:05 $
 */
public class CallbackSocketProtocol
    extends SocketProtocol
{
    
    private Log log = LogFactory.getLog(CallbackSocketProtocol.class);

    private static int nextConnectionId = 0;

    /**
     * To be notified when the socket is closed.
     */
    private SocketProtocolListener listener;

    private synchronized static int getNextConnectionId() {
        return nextConnectionId++;
    }

    public void close() {
        super.close();
        if ( null != listener ) {
            listener.onClose();
        }
    }

    /**
     * Gets the listener to be notified upon closure of the underlying socket.
     * 
     * @return Listener.
     */
    public SocketProtocolListener getListener() {
        return listener;
    }
    
    /**
     * Sets the listener.
     * 
     * @param aListener Listener.
     */
    public void setListener(SocketProtocolListener aListener) {
        listener = aListener;
    }

    public Protocol cloneProtocol() throws CloneNotSupportedException {
        CallbackSocketProtocol p = (CallbackSocketProtocol) super.clone();
        p.log = LogFactory.getLog(CallbackSocketProtocol.class.getName() + ":" + getNextConnectionId());
        return p;
    }


    /**
     * When the underlying socket is closed, this callback is called.
     *
     * @version $Revision: 1.1 $ $Date: 2004/07/20 00:15:05 $
     */
    public interface SocketProtocolListener {
        
        public void onClose();
        
    }
    
}
