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

import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.gbean.GBeanInfoFactory;
import org.apache.geronimo.messaging.NodeInfo;
import org.apache.geronimo.messaging.io.IOContext;
import org.apache.geronimo.messaging.remotenode.MessagingTransportFactory;
import org.apache.geronimo.messaging.remotenode.NodeServer;
import org.apache.geronimo.messaging.remotenode.RemoteNode;
import org.apache.geronimo.network.SelectorManager;
import org.apache.geronimo.pool.ClockPool;

/**
 * MessagingTransportFactory using Geronimo network as the transport layer.
 * 
 * @version $Revision: 1.5 $ $Date: 2004/07/20 00:15:05 $
 */
public class NetworkTransportFactory
    implements MessagingTransportFactory
{
    
    private final SelectorManager sm;
    private final ClockPool cp;
    
    public NetworkTransportFactory(SelectorManager aSelectorManager,
        ClockPool aClockPool) {
        if ( null == aSelectorManager ) {
            throw new IllegalArgumentException("SelectorManager is required.");
        }
        sm = aSelectorManager;
        cp = aClockPool;
    }
    
    public NodeServer factoryServer(NodeInfo aNodeInfo, IOContext anIOContext) {
        return new NodeServerImpl(aNodeInfo, anIOContext, sm, cp);
    }
    
    public RemoteNode factoryRemoteNode(NodeInfo aLocalNodeInfo,
        NodeInfo aRemoteNodeInfo, IOContext anIOContext) {
        return new RemoteNodeJoiner(aLocalNodeInfo, aRemoteNodeInfo, 
            anIOContext, sm);
    }

    public static final GBeanInfo GBEAN_INFO;

    static {
        GBeanInfoFactory factory = new GBeanInfoFactory(NetworkTransportFactory.class);
        factory.setConstructor(new String[] {"SelectorManager", "ClockPool"});
        factory.addInterface(MessagingTransportFactory.class);
        factory.addReference("SelectorManager", SelectorManager.class);
        factory.addReference("ClockPool", ClockPool.class);
        GBEAN_INFO = factory.getBeanInfo();
    }

    public static GBeanInfo getGBeanInfo() {
        return GBEAN_INFO;
    }
    
    
}
