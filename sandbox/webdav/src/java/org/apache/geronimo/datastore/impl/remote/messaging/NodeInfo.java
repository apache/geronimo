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

import java.io.Serializable;
import java.net.InetAddress;

/**
 * Wraps the properties of a node, which identify it uniquely on the network.
 * <BR>
 * This class could be wrapped in a packet and send to a multicast group in
 * order to notify the availability of a new node to other nodes. These other
 * nodes could then decide to join it or not.
 *
 * @version $Revision: 1.1 $ $Date: 2004/03/01 13:16:35 $
 */
public class NodeInfo implements Serializable
{

    /**
     * Name.
     */
    private final String name;
    /**
     * Listening address.
     */
    private final InetAddress address;
    /**
     * Listening port.
     */
    private final int port;
    
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
    
    public boolean equals(Object obj) {
        if ( false == obj instanceof NodeInfo ) {
            return false;
        }
        NodeInfo other = (NodeInfo) obj;
        return name.equals(other.name) && address.equals(other.address) &&
            port == other.port;
    }
    
    public String toString() {
        return "NodeInfo: node name = {" + name + "}; address = {" + address +
            "}; port = {" + port + "}";
    }

}
