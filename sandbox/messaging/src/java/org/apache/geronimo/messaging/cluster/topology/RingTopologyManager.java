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

package org.apache.geronimo.messaging.cluster.topology;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.geronimo.messaging.NodeInfo;
import org.apache.geronimo.messaging.NodeTopology;

/**
 * A simple ring topology manager.
 *
 * @version $Revision: 1.2 $ $Date: 2004/07/05 07:03:50 $
 */
public class RingTopologyManager
    implements TopologyManager
{

    /**
     * List<NodeInfo> nodes registered by this manager.
     */
    private final List nodes;
    
    public RingTopologyManager() {
        nodes = new ArrayList();
    }

    public Set getNodes() {
        synchronized (nodes) {
            return new HashSet(nodes);
        }
    }

    public void addNode(NodeInfo aNode) {
        synchronized(nodes) {
            nodes.add(aNode);
        }
    }

    public void removeNode(NodeInfo aNode) {
        synchronized(nodes) {
            nodes.remove(aNode);
        }
    }

    public NodeTopology factoryTopology() {
        List tmpNodes;
        synchronized(nodes) {
            tmpNodes = new ArrayList(nodes);
        }
        return new RingTopology(tmpNodes);
    }

    private static class RingTopology implements NodeTopology {

        private static int versionSeq = 0;
        
        private final List nodes;
        private final Set nodeSet;
        private final NodeInfo[] nodeArray;
        private final NodeInfo[] nodeArrayInv;
        private final int version;
        
        
        private RingTopology(List aNodes) {
            nodes = aNodes;
            nodeSet = new HashSet(nodes);
            nodeArray = (NodeInfo[]) aNodes.toArray(new NodeInfo[0]);
            int length = nodeArray.length;
            nodeArrayInv = new NodeInfo[length];
            for (int i = 0; i < length; i++) {
                nodeArrayInv[i] = nodeArray[length - i - 1];
            }
            version = versionSeq++;
        }
        
        public Set getNeighbours(NodeInfo aRoot) {
            if ( 1 == nodeArray.length ) {
                return Collections.EMPTY_SET;
            } else if ( 2 == nodeArray.length ) {
                Set result = new HashSet();
                int index = getIDOfNode(aRoot);
                result.add(nodeArray[0 == index ? 1 : 0]);
                return result;
            }
            
            Set result = new HashSet();
            int index = getIDOfNode(aRoot);
            if ( 0 == index ) {
                result.add(nodeArray[nodeArray.length - 1]);
                result.add(nodeArray[1]);
            } else if ( nodeArray.length - 1 == index ) {
                result.add(nodeArray[0]);
                result.add(nodeArray[nodeArray.length - 2]);
            } else {
                result.add(nodeArray[index - 1]);
                result.add(nodeArray[index + 1]);
            }
            return result;
        }

        public NodeInfo[] getPath(NodeInfo aSource, NodeInfo aTarget) {
            int srcIndex = getIDOfNode(aSource);
            int destIndex = getIDOfNode(aTarget);
            NodeInfo[] path = new NodeInfo[Math.abs(destIndex - srcIndex)];
            if ( srcIndex < destIndex ) {
                System.arraycopy(nodeArray, srcIndex + 1, path, 0, path.length);
            } else {
                System.arraycopy(nodeArrayInv,
                    nodeArrayInv.length - srcIndex, path, 0, path.length);
            }
            return path;
        }

        public int getIDOfNode(NodeInfo aNodeInfo) {
            for (int i = 0; i < nodeArray.length; i++) {
                if ( nodeArray[i].equals(aNodeInfo) ) {
                    return i;
                }
            }
            throw new IllegalArgumentException(aNodeInfo +
                " is not registered by the topology.");
        }

        public NodeInfo getNodeById(int anId) {
            if ( 0 > anId || nodeArray.length <= anId ) {
                throw new IllegalArgumentException("Wrong identifier.");
            }
            return nodeArray[anId];
        }

        public Set getNodes() {
            return nodeSet;
        }

        public int getVersion() {
            return version;
        }

    }
    
}
