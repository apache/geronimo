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
 * Based on this knowledge, Topology is able to derive the shortest path - the
 * one having the lowest weight - between two nodes.
 * <BR>
 * This class is intended to be send to a multicast group when a topology event
 * happens.
 *
 * @version $Revision: 1.1 $ $Date: 2004/03/11 15:36:14 $
 */
public class Topology
    implements Externalizable
{

    /**
     * NodeInfo to PathWeight map.
     */
    private Map nodeToPaths;
    
    public Topology() {
        nodeToPaths = new HashMap();
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
            Collection related = (Collection) nodeToPaths.get(aPath.nodeOne);
            if ( null == related ) {
                related = new ArrayList();
                nodeToPaths.put(aPath.nodeOne, related);
            }
            related.add(new Node(aPath.nodeTwo, aPath.weigthOneToTwo));
            related = (Collection) nodeToPaths.get(aPath.nodeTwo);
            if ( null == related ) {
                related = new ArrayList();
                nodeToPaths.put(aPath.nodeTwo, related);
            }
            related.add(new Node(aPath.nodeOne, aPath.weigthTwoToOne));
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
            if ( !related.remove(new Node(aPath.nodeTwo, null)) ) {
                throw new IllegalArgumentException(aPath +
                    " is not registered by this topology.");
            }
            related = (Collection) nodeToPaths.get(aPath.nodeTwo);
            if ( null == related ) {
                throw new IllegalArgumentException(aPath.nodeTwo +
                    " is not registered by this topology.");
            }
            if ( !related.remove(new Node(aPath.nodeOne, null)) ) {
                throw new IllegalArgumentException(aPath +
                    " is not registered by this topology.");
            }
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
        for (Iterator iter = paths.iterator(); iter.hasNext();index++) {
            int weight = 0;
            List nodeList = (List) iter.next();
            for (Iterator iterator = nodeList.iterator();
                iterator.hasNext();
                ) {
                Node node = (Node) iterator.next();
                weight += node.weigth.getWeight();
            }
            if ( -1 == minWeight || weight < minWeight ) {
                minWeight = weight;
                minPathIndex = index;
            }
        }
        List path = (List) paths.get(minPathIndex);
        return (NodeInfo[]) path.toArray(new NodeInfo[0]);
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
            Node node = (Node) iter.next();
            if ( aPath.contains(node) ) {
                continue;
            }
            aPath.add(node);
            if ( node.equals(aTarget) ) {
                returned.add(new ArrayList(aPath));
            } else {
                Collection paths = getPaths(node, aTarget, aPath, aNodeToPath);
                returned.addAll(paths);
            }
            aPath.remove(node);
        }
        return returned;
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
     * @version $Revision: 1.1 $ $Date: 2004/03/11 15:36:14 $
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
    
    private static class Node
        extends NodeInfo
        implements Externalizable {
        private PathWeight weigth;
        /**
         * Required for Externalization.
         */
        public Node() {}
        public Node(NodeInfo aNode, PathWeight aWeight) {
            super(aNode.getName(), aNode.getAddress(), aNode.getPort());
            if ( null == aNode ) {
                throw new IllegalArgumentException("Node is required");
            }
            weigth = aWeight;
        }
        public boolean equals(Object obj) {
            return super.equals(obj);
        }
        public int hashCode() {
            return super.hashCode();
        }
        public void writeExternal(ObjectOutput out) throws IOException {
            super.writeExternal(out);
            out.writeObject(weigth);
        }
        public void readExternal(ObjectInput in)
            throws IOException, ClassNotFoundException {
            super.readExternal(in);
            weigth = (PathWeight) in.readObject();
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
    
}
