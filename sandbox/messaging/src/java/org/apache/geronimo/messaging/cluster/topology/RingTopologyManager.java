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

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.geronimo.messaging.NodeInfo;
import org.apache.geronimo.messaging.NodeTopology;

/**
 * A simple ring topology manager.
 *
 * @version $Rev$ $Date$
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
        return new HashSet(nodes);
    }

    public void addNode(NodeInfo aNode) {
        nodes.add(aNode);
    }

    public void removeNode(NodeInfo aNode) {
        nodes.remove(aNode);
    }

    public NodeTopology factoryTopology() {
        return new RingTopology(new ArrayList(nodes));
    }

    /**
     * Implementation note: it is an Externalizable as one does not want to
     * serialize the Map of paths.
     */
    private static class RingTopology implements NodeTopology, Externalizable {

        private static int versionSeq = 0;

        private List nodes;
        private Set nodeSet;
        private int version;
        private Map paths;

        /**
         * Requires for externalization.
         */
        public RingTopology() {}
        
        private RingTopology(List aNodes) {
            nodes = aNodes;
            version = versionSeq++;
            initialize();
        }
        
        public Set getNeighbours(NodeInfo aRoot) {
            if ( 1 == nodes.size() ) {
                return Collections.EMPTY_SET;
            }
            int index;
            try {
                index = getIDOfNode(aRoot);
            } catch (IllegalArgumentException e) {
                return Collections.EMPTY_SET;
            }
            if ( 2 == nodes.size() ) {
                Set result = new HashSet();
                result.add(nodes.get(0 == index ? 1 : 0));
                return result;
            }
            
            Set result = new HashSet();
            if ( 0 == index ) {
                result.add(nodes.get(nodes.size() - 1));
                result.add(nodes.get(1));
            } else if ( nodes.size() - 1 == index ) {
                result.add(nodes.get(0));
                result.add(nodes.get(nodes.size() - 2));
            } else {
                result.add(nodes.get(index - 1));
                result.add(nodes.get(index + 1));
            }
            return result;
        }

        public NodeInfo[] getPath(NodeInfo aSource, NodeInfo aTarget) {
            PathInfo pathInfo = new PathInfo(aSource, aTarget);
            return (NodeInfo[]) paths.get(pathInfo);
        }

        public int getIDOfNode(NodeInfo aNodeInfo) {
            int index = nodes.indexOf(aNodeInfo);
            if ( -1 == index ) {
                throw new IllegalArgumentException(aNodeInfo +
                    " is not registered by the topology.");
            }
            return index;
        }

        public NodeInfo getNodeById(int anId) {
            try {
                return (NodeInfo) nodes.get(anId);
            } catch (IndexOutOfBoundsException e) {
                throw new IllegalArgumentException("Wrong identifier.");
            }
        }

        public Set getNodes() {
            return nodeSet;
        }

        public int getVersion() {
            return version;
        }

        public void readExternal(ObjectInput in)
            throws IOException, ClassNotFoundException {
            nodes = (List) in.readObject();
            version = in.readInt();
            initialize();
        }
    
        public void writeExternal(ObjectOutput out) throws IOException {
            out.writeObject(nodes);
            out.writeInt(version);
        }

        public String toString() {
            StringBuffer buffer = new StringBuffer();
            buffer.append("Topology type: Ring \nMembers: ");
            for (Iterator iter = nodeSet.iterator(); iter.hasNext();) {
                NodeInfo nodeInfo = (NodeInfo) iter.next();
                buffer.append("\n  " + nodeInfo + "");
            }
            return buffer.toString();
        }
        
        private void initialize() {
            nodeSet = new HashSet(nodes);

            paths = new HashMap();
            List cycledNodes = new ArrayList(nodes);
            cycledNodes.addAll(nodes);
            for(int i = 0; i < nodes.size(); i++) {
                for(int j = i + 1; j < i + nodes.size(); j++) {
                    PathInfo pathInfo =
                        new PathInfo(
                            (NodeInfo)cycledNodes.get(i), 
                            (NodeInfo)cycledNodes.get(j));
                    List result = cycledNodes.subList(i + 1, j + 1);
                    paths.put(pathInfo, result.toArray(new NodeInfo[0]));
                }
            }
            Collections.reverse(cycledNodes);
            for(int i = 0; i < nodes.size(); i++) {
                for(int j = i + 1; j < i + nodes.size(); j++) {
                    PathInfo pathInfo =
                        new PathInfo(
                            (NodeInfo)cycledNodes.get(i), 
                            (NodeInfo)cycledNodes.get(j));
                    List result = cycledNodes.subList(i + 1, j + 1);
                    NodeInfo[] path = (NodeInfo[]) paths.get(pathInfo);
                    if ( result.size() < path.length ) {
                        paths.put(pathInfo, result.toArray(new NodeInfo[0]));
                    }
                }
            }
        }        
    }
    
    private static class PathInfo {
        private final NodeInfo src;
        private final NodeInfo dest;
        private PathInfo(NodeInfo aSrc, NodeInfo aDest) {
            src = aSrc;
            dest = aDest;
        }
        public int hashCode() {
            return src.hashCode() ^ dest.hashCode();
        }
        public boolean equals(Object obj) {
            if ( false == obj instanceof PathInfo ) {
                return false;
            }
            PathInfo other = (PathInfo) obj;
            return src.equals(other.src) && dest.equals(other.dest);
        }
    }
    
}
