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
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Abstracts the topology of a set of nodes.
 * <BR>
 * Nodes are related by paths, which are physical connections, having a weight.
 * Based on this knowledge, this class is able to derive the shortest path - the
 * one having the lowest weight - between two nodes.
 * <BR>
 * This class is intended to be sent to a multicast group when a topology event
 * happens.
 *
 * @version $Revision: 1.1 $ $Date: 2004/05/11 12:06:41 $
 */
public class NodeTopology
    implements Externalizable
{

    /**
     * NodeInfo to PathWeight map.
     */
    private Map nodeToPaths;

    /**
     * Used to perform atomic operations on nodeToID and idToNode.
     */
    private final Object nodeAndIdLock = new Object();
    
    /**
     * NodeInfo to Integer (node identifier) mapping.
     */
    private final Map nodeToID;
    
    /**
     * Integer (node identifier) to NodeInfo mapping.
     */
    private final Map idToNode;

    /**
     * TwoNodeInfo to NodeInfo[] mapping. It is a cache of the shortest path
     * between two NodeInfos.
     */
    private final Map shortestPathCache;

    /**
     * Used to generate NodeInfo identifiers.
     */
    private static int seqID = 0;
    
    public NodeTopology() {
        nodeToPaths = new HashMap();
        nodeToID = new HashMap();
        idToNode = new HashMap();
        shortestPathCache = new HashMap();
    }

    /**
     * Registers a node.
     * 
     * @param aNodeInfo Node to be registered.
     */
    private void registerNode(NodeInfo aNodeInfo) {
        synchronized(nodeAndIdLock) {
            if ( null == nodeToID.get(aNodeInfo) ) {
                int intID = seqID++;
                Integer id = new Integer(intID);
                nodeToID.put(aNodeInfo, id);
                idToNode.put(new Integer(intID), aNodeInfo);
            }
        }
    }
    
    /**
     * Unregisters a node.
     *
     * @param aNodeInfo Node to be unregistered.
     */
    private void unregisterNode(NodeInfo aNodeInfo) {
        synchronized(nodeAndIdLock) {
            if ( null == nodeToID.remove(aNodeInfo) ) {
                throw new IllegalArgumentException(aNodeInfo +
                    " is not registered by this topology.");
            }
        }
    }
    
    /**
     * Adds a path to this topology. This path must abstracts a physical
     * connections between two nodes.
     * 
     * @param aPath Path to be added to this topology.
     */
    public void addPath(NodePath aPath) {
        if ( null == aPath ) {
            throw new IllegalArgumentException("Path is required.");
        }
        synchronized(nodeToPaths) {
            registerNode(aPath.nodeOne);
            Collection related = (Collection) nodeToPaths.get(aPath.nodeOne);
            if ( null == related ) {
                related = new ArrayList();
                nodeToPaths.put(aPath.nodeOne, related);
            }
            related.add(new NodeWrapper(aPath.nodeTwo, aPath.weigthOneToTwo));
            registerNode(aPath.nodeTwo);
            related = (Collection) nodeToPaths.get(aPath.nodeTwo);
            if ( null == related ) {
                related = new ArrayList();
                nodeToPaths.put(aPath.nodeTwo, related);
            }
            related.add(new NodeWrapper(aPath.nodeOne, aPath.weigthTwoToOne));
        }
        synchronized (shortestPathCache) {
            shortestPathCache.clear();
        }
    }
    
    /**
     * Removes a node from the topology. All the paths leading to this node
     * are also removed.
     * 
     * @param aNode Node to be removed.
     */
    public void removeNode(NodeInfo aNode) {
        if ( null == aNode ) {
            throw new IllegalArgumentException("Node is required.");
        }
        synchronized (nodeToPaths) {
            if ( null == nodeToPaths.remove(aNode) ) {
                throw new IllegalArgumentException(aNode +
                    " is not registered by this topology.");
            }
        }
        unregisterNode(aNode);
        synchronized (shortestPathCache) {
            shortestPathCache.clear();
        }
    }
    
    /**
     * Removes a path. 
     * 
     * @param aPath Path to be removed.
     */
    public void removePath(NodePath aPath) {
        if ( null == aPath ) {
            throw new IllegalArgumentException("Path is required.");
        }
        synchronized (nodeToPaths) {
            Collection related = (Collection) nodeToPaths.get(aPath.nodeOne);
            if ( null == related ) {
                throw new IllegalArgumentException(aPath.nodeOne +
                    " is not registered by this topology.");
            }
            if ( !related.remove(new NodeWrapper(aPath.nodeTwo, null)) ) {
                throw new IllegalArgumentException(aPath +
                    " is not registered by this topology.");
            }
            if ( 0 == related.size() ) {
                unregisterNode(aPath.nodeOne);
                nodeToPaths.remove(aPath.nodeOne);
            }
            related = (Collection) nodeToPaths.get(aPath.nodeTwo);
            if ( null == related ) {
                throw new IllegalArgumentException(aPath.nodeTwo +
                    " is not registered by this topology.");
            }
            if ( !related.remove(new NodeWrapper(aPath.nodeOne, null)) ) {
                throw new IllegalArgumentException(aPath +
                    " is not registered by this topology.");
            }
            if ( 0 == related.size() ) {
                unregisterNode(aPath.nodeTwo);
                nodeToPaths.remove(aPath.nodeTwo);
            }
        }
        synchronized (shortestPathCache) {
            shortestPathCache.clear();
        }
    }

    /**
     * Gets the shortest path, the one having the lowest weight, between aSource
     * and aTarget. 
     * 
     * @param aSource Source node.
     * @param aTarget Target node.
     * @return Nodes to be traversed to reach aTarget from aSource. null is
     * returned if these two nodes are not connected.
     */
    public NodeInfo[] getPath(NodeInfo aSource, NodeInfo aTarget) {
        // First check if the path has already been computed.
        TwoNodeInfo twoNodeInfo = new TwoNodeInfo(aSource, aTarget);
        NodeInfo[] result;
        synchronized(shortestPathCache) {
            result = (NodeInfo[]) shortestPathCache.get(twoNodeInfo);
        }
        if ( null != result ) {
            return result;
        }
        
        // This is the first time that this path needs to be computed.
        Map tmpNodeToRelated;
        synchronized(nodeToPaths) {
            tmpNodeToRelated = new HashMap(nodeToPaths);
        }
        List paths = getPaths(aSource, aTarget, new ArrayList(),
            tmpNodeToRelated);
        if ( 0 == paths.size() ) {
            return null;
        }
        
        int minWeight = -1;
        int minPathIndex = 0;
        int index = 0;
        // Gets the shortest path amongst the available paths.
        for (Iterator iter = paths.iterator(); iter.hasNext();index++) {
            int weight = 0;
            List nodeList = (List) iter.next();
            for (Iterator iter2 = nodeList.iterator(); iter2.hasNext();) {
                NodeWrapper node = (NodeWrapper) iter2.next();
                weight += node.weigth.getWeight();
            }
            if ( -1 == minWeight || weight < minWeight ) {
                minWeight = weight;
                minPathIndex = index;
            }
        }
        List path = (List) paths.get(minPathIndex);
        result = new NodeInfo[path.size()];
        int i = 0;
        for (Iterator iter = path.iterator(); iter.hasNext();) {
            NodeWrapper wrapper = (NodeWrapper) iter.next();
            result[i++] = wrapper.node;
        }
        synchronized(shortestPathCache) {
            shortestPathCache.put(twoNodeInfo, result);
        }
        return result;
    }

    private List getPaths(NodeInfo aSource, NodeInfo aTarget, List aPath,
        Map aNodeToPath) {
        Collection related = (Collection) aNodeToPath.get(aSource);
        if ( null == related ) {
            throw new IllegalArgumentException(aSource +
                " is not registered by this topology.");
        }
        List returned = new ArrayList();
        for (Iterator iter = related.iterator(); iter.hasNext();) {
            NodeWrapper wrapper = (NodeWrapper) iter.next();
            if ( aPath.contains(wrapper) ) {
                continue;
            }
            aPath.add(wrapper);
            if ( wrapper.node.equals(aTarget) ) {
                returned.add(new ArrayList(aPath));
            } else {
                Collection paths =
                    getPaths(wrapper.node, aTarget, aPath, aNodeToPath);
                returned.addAll(paths);
            }
            aPath.remove(wrapper);
        }
        return returned;
    }

    /**
     * Gets the identifier of the provided node.
     * <BR>
     * When a node is added to a topology, this latter assigns it an identifier.
     * 
     * @param aNodeInfo Node whose identifier is to be returned.
     * @return Node identifier.
     */
    public int getIDOfNode(NodeInfo aNodeInfo) {
        Integer id;
        synchronized(nodeAndIdLock) {
            id = (Integer) nodeToID.get(aNodeInfo);
        }
        if ( null == id ) {
            throw new IllegalArgumentException(aNodeInfo +
                " is not registered by this topology.");
        }
        return id.intValue();
    }
    
    /**
     * Gets the NodeInfo having the specified identifier.
     * 
     * @param anId Node identifier.
     * @return NodeInfo having this identifier.
     */
    public NodeInfo getNodeById(int anId) {
        NodeInfo nodeInfo = (NodeInfo) idToNode.get(new Integer(anId));
        if ( null == nodeInfo ) {
            throw new IllegalArgumentException("Identifier " + anId +
                " is not registered by this topology.");
        }
        return nodeInfo;
    }
    
    public void writeExternal(ObjectOutput out) throws IOException {
        out.writeObject(nodeToPaths);
    }

    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        nodeToPaths = (Map) in.readObject();
    }
    
    /**
     * Abstract a weight between two nodes.
     *
     * @version $Revision: 1.1 $ $Date: 2004/05/11 12:06:41 $
     */
    public static class PathWeight implements Externalizable {
        private int weight;
        /**
         * Required for Externalization.
         */
        public PathWeight() {}
        public PathWeight(int aWeigth) {
            weight = aWeigth;
        }
        public int getWeight() {
            return weight;
        }
        public void writeExternal(ObjectOutput out) throws IOException {
            out.writeInt(weight);
        }
        public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
            weight = in.readInt();
        }
    }
    
    /**
     * NodeInfo wrapper. It allows storing a weight with a NodeInfo.
     */
    private static class NodeWrapper {
        private PathWeight weigth;
        private NodeInfo node;
        public NodeWrapper(NodeInfo aNode, PathWeight aWeight) {
            node = aNode;
            weigth = aWeight;
        }
        public boolean equals(Object obj) {
            if ( false == obj instanceof NodeWrapper ) {
                return false;
            }
            NodeWrapper other = (NodeWrapper) obj;
            return node.equals(other.node);
        }
        public int hashCode() {
            return node.hashCode();
        }
    }
    
    /**
     * Abstract a path between two nodes.
     * <BR>
     * A path is a bi-direction relation between two nodes. Two weights are
     * defined by a path: one to traverse from one end to the other and another
     * one to traverse in the other direction.
     */
    public static class NodePath
        implements Externalizable {
        private PathWeight weigthOneToTwo;
        private PathWeight weigthTwoToOne;
        private NodeInfo nodeOne;
        private NodeInfo nodeTwo;
        /**
         * Required for Externalization.
         */
        public NodePath(){}
        public NodePath(NodeInfo aNodeOne, NodeInfo aNodeTwo,
            PathWeight aWeightOneToTwo, PathWeight aWeightTwoToOne) {
            if ( null == aNodeOne ) {
                throw new IllegalArgumentException("Node one is required");
            } else if ( null == aNodeTwo ) {
                throw new IllegalArgumentException("Node two is required");
            }
            nodeOne = aNodeOne;
            nodeTwo = aNodeTwo;
            weigthOneToTwo = aWeightOneToTwo;
            weigthTwoToOne = aWeightTwoToOne;
        }
        public boolean equals(Object obj) {
            if ( false == obj instanceof NodePath ) {
                return false;
            }
            NodePath path = (NodePath) obj;
            return nodeOne.equals(path.nodeOne) &&
                nodeTwo.equals(path.nodeTwo);
        }
        public int hashCode() {
            return nodeOne.hashCode() * nodeTwo.hashCode();
        }
        public void writeExternal(ObjectOutput out) throws IOException {
            out.writeObject(weigthOneToTwo);
            out.writeObject(weigthTwoToOne);
            out.writeObject(nodeOne);
            out.writeObject(nodeTwo);
        }
        public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
            weigthOneToTwo = (PathWeight) in.readObject();
            weigthTwoToOne = (PathWeight) in.readObject();
            nodeOne = (NodeInfo) in.readObject();
            nodeTwo = (NodeInfo) in.readObject();
        }
    }

    private static class TwoNodeInfo {
        private final NodeInfo nodeOne;
        private final NodeInfo nodeTwo;
        private TwoNodeInfo(NodeInfo aNodeOne, NodeInfo aNodeTwo) {
            nodeOne = aNodeOne;
            nodeTwo = aNodeTwo;
        }
        public int hashCode() {
            return nodeOne.hashCode() * nodeTwo.hashCode();
        }
        public boolean equals(Object obj) {
            if ( false == obj instanceof TwoNodeInfo ) {
                return false;
            }
            TwoNodeInfo other = (TwoNodeInfo) obj;
            return nodeOne.equals(other.nodeOne) &&
                nodeTwo.equals(other.nodeTwo);
        }
    }
    
}
