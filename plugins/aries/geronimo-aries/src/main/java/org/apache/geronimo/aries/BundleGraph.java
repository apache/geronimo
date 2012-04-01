/**
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.apache.geronimo.aries;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.osgi.framework.Bundle;

/**
 * The bundle graph, using the class to get the ordered bundles by the dependency relationship among them
 *
 * @version $Rev$ $Date$
 */
public class BundleGraph {
    private final Collection<BundleNode> nodes;
    
    public BundleGraph(Collection<BundleNode> nodes) {
        this.nodes = nodes;
        
    }
    
    /**
     * Traverse the graph in DFS way
     * 
     * @param action
     */
    public void traverseDFS(TraverseVisitor action) {
        clearState();
        for(BundleNode node : nodes) {
            DFS(node, action);
        }
        
    }
    
    protected void DFS(BundleNode startNode, TraverseVisitor action) {
        if(startNode.isVisited) return;
        
        // do action before
        if(action.isVisitBefore()) {
            action.doVisit(startNode);
        }
        
        startNode.isVisited = true;
        
        Collection<BundleNode> requiredBundles = startNode.requiredBundles;
        if(! requiredBundles.isEmpty()) {
            for(BundleNode rqBundle : requiredBundles) {
                DFS(rqBundle, action);
            }
        }
        
        // do action after
        if(! action.isVisitBefore()) {
            action.doVisit(startNode);
        }
    }
    
    /**
     * Get the ordered bundles according to the bundle dependencies
     * 
     * @return
     */
    public List<Bundle> getOrderedBundles() {
        final List<Bundle> result = new ArrayList<Bundle>();
        
        traverseDFS(new TraverseVisitor() {
            @Override
            public void doVisit(BundleNode node) {
                if(! result.contains(node.value)) {
                    result.add(node.value);
                }
            }
            @Override
            public boolean isVisitBefore() {
                return false;
            }
        });
        
        return result;
    }
    
    protected void clearState() {
        for(BundleNode node : nodes) {
            node.isVisited = false;
        }
    }
    /**
     * The bundle node in graph
     *
     * @version $Rev$ $Date$
     */
    public static class BundleNode {
        private final Bundle value;
        private final Set<BundleNode> requiredBundles;
        private boolean isVisited;
        
        public BundleNode(Bundle value) {
            this.value = value;
            requiredBundles = new HashSet<BundleNode>();
        }
        
        /**
         * @return the value
         */
        public Bundle getValue() {
            return value;
        }

        /**
         * @return the isVisited
         */
        public boolean isVisited() {
            return isVisited;
        }

        /**
         * @return the requiredBundles
         */
        public Set<BundleNode> getRequiredBundles() {
            return requiredBundles;
        }

        /* (non-Javadoc)
         * @see java.lang.Object#hashCode()
         */
        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + ((value == null) ? 0 : value.hashCode());
            return result;
        }

        /* (non-Javadoc)
         * @see java.lang.Object#equals(java.lang.Object)
         */
        @Override
        public boolean equals(Object obj) {
            if (this == obj)
                return true;
            if (obj == null)
                return false;
            if (getClass() != obj.getClass())
                return false;
            BundleNode other = (BundleNode) obj;
            if (value == null) {
                if (other.value != null)
                    return false;
            } else if (!value.equals(other.value))
                return false;
            return true;
        }
        
        
    }
    
    /**
     * Do some action when traverse the graph
     *
     * @version $Rev$ $Date$
     */
    public static interface TraverseVisitor {
        public void doVisit(BundleNode node);
        public boolean isVisitBefore();
    }
}

