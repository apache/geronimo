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

package org.apache.geronimo.messaging.proxy;

import org.apache.geronimo.messaging.NodeInfo;

/**
 * EndPoint proxy meta-data.
 *
 * @version $Revision: 1.1 $ $Date: 2004/05/20 13:37:11 $
 */
public class EndPointProxyInfo
{
    
    /**
     * EndPoint identifier.
     */
    private final Object endPointID;
    
    /**
     * Interfaces implemented by the EndPoint.
     */
    private final Class[] interfaces;
    
    /**
     * Nodes hosting the EndPoint.
     */
    private final NodeInfo[] targets;
    
    /**
     * Creates the meta-data of an EndPoint proxy.  
     * 
     * @param anEndPointID EndPoint identifier.
     * @param anInterfaces Interfaces of the EndPoint proxy.
     * @param aTargets Nodes hosting the EndPoint.
     */
    public EndPointProxyInfo(Object anEndPointID, Class[] anInterfaces,
        NodeInfo[] aTargets) {
        if ( null == anEndPointID ) {
            throw new IllegalArgumentException("EndPointID is required.");
        } else if ( null == anInterfaces || 0 == anInterfaces.length ) {
            throw new IllegalArgumentException("Interfaces is required");
        } else if ( null == aTargets || 0 == aTargets.length ) {
            throw new IllegalArgumentException("Targets is required");
        }
        endPointID = anEndPointID;
        interfaces = anInterfaces;
        targets = aTargets;
    }
    
    /**
     * Gets the EndPoint identifier.
     * 
     * @return EndPoint id.
     */
    public Object getEndPointID() {
        return endPointID;
    }
    
    /**
     * Gets the interfaces of the EndPoint.
     * 
     * @return EndPoint interfaces.
     */
    public Class[] getInterfaces() {
        return interfaces;
    }
    
    /**
     * Gets the Nodes hosting the EndPoint.
     * 
     * @return Hosting nodes.
     */
    public NodeInfo[] getTargets() {
        return targets;
    }
    
}