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

package org.apache.geronimo.messaging.reference;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

import org.apache.geronimo.messaging.NodeInfo;

/**
 * Wraps meta-data about a Referenceable.
 *
 * @version $Rev$ $Date$
 */
public class ReferenceableInfo implements Externalizable
{

    /**
     * Hosting node.
     */
    private NodeInfo hostingNode;
    
    /**
     * EndPoint identifier containing the Reference.
     */
    private Object id;
    
    /**
     * Reference identifier.
     */
    private int refID;
    
    /**
     * Interfaces of the Referenceable.
     */
    private Class[] clazz;
    
    /**
     * Required for Externalization.
     */
    public ReferenceableInfo() {}

    /**
     * Creates the meta-data of a Referenceable.
     * 
     * @param anHostingNode Hosting node.
     * @param anID EndPoint identifier.
     * @param aClazz Referenceable interfaces.
     * @param aRefId Referenceable identifier contained by the EndPoint.
     */
    public ReferenceableInfo(NodeInfo anHostingNode, Object anID,
        Class[] aClazz, int aRefId) {
        if ( null == anHostingNode ) {
            throw new IllegalArgumentException("Hosting node is required.");
        } else if ( null == anID ) {
            throw new IllegalArgumentException("Connector name is required.");
        } else if ( 0 == aRefId ) {
            throw new IllegalArgumentException("ID is required.");
        } else if ( null == aClazz ) {
            throw new IllegalArgumentException("Class i required.");
        }
        hostingNode = anHostingNode;
        id = anID;
        // Gets rid of the Referenceable interface.
        clazz = new Class[aClazz.length - 1];
        int j = 0;
        for (int i = 0; i < aClazz.length; i++) {
            if ( Referenceable.class.isAssignableFrom(aClazz[i]) ) {
                continue;
            }
            clazz[j++] = aClazz[i];
        }
        refID = aRefId;
    }

    /**
     * Gets the hosting node. 
     * 
     * @return Returns the hostingNode.
     */
    public NodeInfo getHostingNode() {
        return hostingNode;
    }

    /**
     * Gets the Reference identifier.
     * 
     * @return Returns the id.
     */
    public int getRefID() {
        return refID;
    }

    /**
     * Gets the EndPoint identifier containing the Reference.
     * 
     * @return Returns the EndPoint identifier.
     */
    public Object getID() {
        return id;
    }

    /**
     * Gets the Referenceable interfaces.
     * 
     * @return Referenceable interfaces.
     */
    public Class[] getRefClass() {
        return clazz;
    }

    /**
     * @return true if the two references are hosted by the same node,
     * contained by the same EndPoint and have the same id.
     */
    public boolean equals(Object obj) {
        if ( false == obj instanceof ReferenceableInfo ) {
            return false;
        }
        ReferenceableInfo info = (ReferenceableInfo) obj;
        return id.equals(info.id) && hostingNode.equals(info.hostingNode) &&
            refID == info.refID;
    }
    
    public void writeExternal(ObjectOutput out) throws IOException {
        out.writeObject(hostingNode);
        out.writeObject(id);
        out.writeInt(refID);
        out.writeObject(clazz);
    }

    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        hostingNode = (NodeInfo) in.readObject();
        id = in.readObject();
        refID = in.readInt();
        clazz = (Class[]) in.readObject();
    }

}
