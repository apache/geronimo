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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Processors associated to a server.
 *
 * @version $Revision: 1.5 $ $Date: 2004/03/11 15:36:14 $
 */
class ServerProcessors
{

    private static final Log log = LogFactory.getLog(ServerProcessors.class);

    /**
     * Server owning these processors.
     */
    private final ServerNode server;

    /**
     * StreamManager used by the server to resolve InputStreams.
     */
    private final StreamManager streamManager;
    
    /**
     * Processor pool.
     */
    private final Processors processors;
    
    /**
     * Creates processors for the provided server.
     * 
     * @param aServer Server owning these processors. 
     */
    public ServerProcessors(ServerNode aServer) {
        server = aServer;
        processors = new Processors(aServer.getNodeInfo().getName(), 2, 10);
        streamManager = aServer.getStreamManager();
    }
    
    /**
     * Execute a Processor in a separate Thread.
     *  
     * @param aProcessor Processor to be executed.
     */
    public void execute(Processor aProcessor) {
        processors.execute(aProcessor);
    }
    
    public Processors getProcessors() {
        return processors;
    }
    
    /**
     * Dispatches the Msgs seating in the inbound queue. Pushes the Msg seating
     * in the outbound queue to the relevant node.
     */
    public void start() {
        processors.execute(server.inReactor);
        processors.execute(new OutputQueueDispatcher());
    }
    
    public void stop() {
    }
    
    /**
     * Runnable in charge of dispatching the Msgs seating in the outbound
     * queue to the relevant node. 
     */
    private class OutputQueueDispatcher implements Processor {
        
        /**
         * Is this Processor started.
         */
        private volatile boolean isStarted = true;
        
        public void run() {
            HeaderInInterceptor in =
                new HeaderInInterceptor(
                    new QueueInInterceptor(server.queueOut),
                    MsgHeaderConstants.DEST_NODES);
            while ( isStarted ) {
                Msg msg = in.pop();
                Object destNode = in.getHeader();
                MsgOutInterceptor out;
                if ( destNode instanceof NodeInfo ) {
                    destNode = new NodeInfo[] {(NodeInfo) destNode};
                }
                NodeInfo[] dests = (NodeInfo[]) destNode;
                for (int i = 0; i < dests.length; i++) {
                    NodeInfo target = dests[i];
                    Msg msg2 = new Msg(msg);
                    MsgHeader header = msg2.getHeader();
                    // A path is defined if this Msg is routed via the node 
                    // owning this instance.
                    NodeInfo[] path = (NodeInfo[])
                        header.getOptionalHeader(MsgHeaderConstants.DEST_NODE_PATH);
                    try {
                        if ( null != path ) {
                            target = path[0];
                            header.addHeader(MsgHeaderConstants.DEST_NODE_PATH,
                                NodeInfo.pop(path));
                            out = server.getRawOutForNode(target);
                        } else {
                            out = server.getOutForNode(target);
                        }
                    } catch (CommunicationException e) {
                        log.error(e);
                        continue;
                    }
                    out.push(msg2);
                }
            }
        }

        public void release() {
            isStarted = false;
        }
        
    }
    
}
