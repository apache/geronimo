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

package org.apache.geronimo.messaging.cluster;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.net.InetAddress;

/**
 * Cluster meta-data.
 *
 * @version $Revision: 1.1 $ $Date: 2004/07/17 03:38:42 $
 */
public class ClusterInfo
    implements Externalizable
{

    /**
     * Multicast address used by this cluster for cluster-wide communications.
     */
    private InetAddress address;
    
    /**
     * Multicast port.
     */
    private int port;
    
    /**
     * Required for externalization
     */
    public ClusterInfo() {}
    
    public ClusterInfo(InetAddress anAddress, int aPort) {
        if ( null == anAddress ) {
            throw new IllegalArgumentException("Address is required");
        }
        address = anAddress;
        port = aPort;
    }
    
    public InetAddress getAddress() {
        return address;
    }
    
    public int getPort() {
        return port;
    }

    public void readExternal(ObjectInput in)
        throws IOException, ClassNotFoundException {
        address = (InetAddress) in.readObject();
        port = in.readInt();
    }

    public void writeExternal(ObjectOutput out) throws IOException {
        out.writeObject(address);
        out.writeInt(port);
    }
    
    public String toString() {
        return " Cluster: address = {" + address + "}; port = {" + port + "}";
    }
    
}
