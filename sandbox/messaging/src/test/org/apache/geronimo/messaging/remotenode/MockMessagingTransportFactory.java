package org.apache.geronimo.messaging.remotenode;

import org.apache.geronimo.messaging.NodeInfo;
import org.apache.geronimo.messaging.io.IOContext;

public class MockMessagingTransportFactory
    implements MessagingTransportFactory
{

    private NodeServer server;
    
    public void setUpFactoryServer(NodeServer aServer) {
        server = aServer;
    }
    
    public NodeServer factoryServer(
        NodeInfo aNodeInfo,
        IOContext anIOContext) {
        return server;
    }

    public RemoteNode factoryNode(NodeInfo aNodeInfo, IOContext anIOContext) {
        return null;
    }

    public RemoteNodeConnection factoryNodeConnection(
        NodeInfo aNodeInfo,
        IOContext anIOContext) {
        return null;
    }

}
