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

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.net.InetAddress;

/**
 * Wraps the properties of a Node.
 * <BR>
 * It identifies a Node uniquely on the network.
 * <BR> 
 * Such an instance could be wrapped in a packet and sent to a multicast group.
 * Other Nodes belonging to this group could then decide to join it or not.
 *
 * @version $Rev$ $Date$
 */
public class NodeInfo implements Externalizable
{

    /**
     * Name.
     */
    private String name;
    
    /**
     * Listening address.
     */
    private InetAddress address;
    
    /**
     * Listening port.
     */
    private int port;

    /**
     * Pops the first element of the array and returns the resulting array.
     * 
     * @param aNodeInfo Array whose first element is to be popped.
     * @return New array. If the size of aNodeInfo is one, then null is
     * returned.
     */
    public static NodeInfo[] pop(NodeInfo[] aNodeInfo) {
        if ( null == aNodeInfo || 0 == aNodeInfo.length) {
            throw new IllegalArgumentException("NodeInfo array is required.");
        }
        if ( 1 == aNodeInfo.length ) {
            return null;
        }
        NodeInfo[] returned = new NodeInfo[aNodeInfo.length-1];
        for (int i = 1; i < aNodeInfo.length; i++) {
            returned[i-1] = aNodeInfo[i];
        }
        return returned;
    }
    
    /**
     * Required for Externalization.
     */
    public NodeInfo() {}
        
    /**
     * Creates a NodeInfo defining uniquely a node on a network.
     * 
     * @param aName Name of the node.
     * @param anAddess Address that the node is listening on.
     * @param aPort Listening port.
     */
    public NodeInfo(String aName, InetAddress anAddess, int aPort) {
        if ( null == aName ) {
            throw new IllegalArgumentException("Name is required.");
        } else if ( null == anAddess ) {
            throw new IllegalArgumentException("Address is required.");
        } else if ( 0 == aPort ) {
            throw new IllegalArgumentException("Port is required.");
        }
        name = aName;
        address = anAddess;
        port = aPort;
    }
    
    /**
     * Gets the listening address of the node providing this instance. 
     * 
     * @return Listening address.
     */
    public InetAddress getAddress() {
        return address;
    }

    /**
     * Gets the name of the node providing this instance.
     * 
     * @return Node name.
     */
    public String getName() {
        return name;
    }

    /**
     * Gets the listening port of the node providing this instance.
     * 
     * @return Listening port.
     */
    public int getPort() {
        return port;
    }
    
    public int hashCode() {
        return name.hashCode() * address.hashCode() *
            (new Integer(port)).hashCode();
    }
    
    public boolean equals(Object obj) {
        if ( false == obj instanceof NodeInfo ) {
            return false;
        }
        NodeInfo other = (NodeInfo) obj;
        return name.equals(other.name) && address.equals(other.address) &&
            port == other.port;
    }

    public void writeExternal(ObjectOutput out) throws IOException {
        out.writeUTF(name);
        out.writeObject(address);
        out.writeInt(port);
    }

    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        name = in.readUTF();
        address = (InetAddress) in.readObject();
        port = in.readInt();
    }

    public String toString() {
        return "NodeInfo: node name = {" + name + "}; address = {" + address +
        "}; port = {" + port + "}";
    }
    
}
