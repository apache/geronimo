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

package org.apache.geronimo.kernel.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @version $Rev$ $Date$
 */
public class SortUtils {

    public static interface Visitor<T> {

        String getName(T t);

        List<String> getAfterNames(T t);

        List<String> getBeforeNames(T t);

        boolean afterOthers(T t);

        boolean beforeOthers(T t);
    }

    public static <T> List<T> sort(Collection<T> objects, Visitor<T> visitor) throws IllegalNodeConfigException, CircularReferencesException {

        if (objects.size() == 0) {
            return Collections.emptyList();
        }

        final Map<String, Node<T>> nodes = new LinkedHashMap<String, Node<T>>();

        // Create nodes
        for (T obj : objects) {
            String name = visitor.getName(obj);
            Node<T> node = new Node<T>(name, obj, visitor.afterOthers(obj), visitor.beforeOthers(obj), visitor.getAfterNames(obj), visitor.getBeforeNames(obj));
            if (node.beforeOthers && node.afterOthers) {
                throw new IllegalNodeConfigException(name, "Before others and after others could not be configured at the sametime ");
            }
            nodes.put(name, node);
        }

        // Link nodes
        for (Node<T> node : nodes.values()) {
            //Convert all the before and after configurations to dependOns
            if (node.after.contains(node.name)) {
                throw new IllegalNodeConfigException(node.name, "The fragment could not be configured to after itself");
            }
            for (String afterNodeName : node.after) {
                Node<T> afterNode = nodes.get(afterNodeName);
                if (afterNode != null) {
                    node.dependOns.add(afterNode);
                }
            }
            if (node.before.contains(node.name)) {
                throw new IllegalNodeConfigException(node.name, "The fragment could not be configured to before itself");
            }
            for (String beforeNodeName : node.before) {
                Node<T> beforeNode = nodes.get(beforeNodeName);
                if (beforeNode != null) {
                    beforeNode.dependOns.add(node);
                }
            }
        }

        boolean circuitFounded = false;
        for (Node<T> node : nodes.values()) {
            Set<Node<T>> visitedNodes = new HashSet<Node<T>>();
            if (!normalizeNodeReferences(node, node, visitedNodes)) {
                circuitFounded = true;
                break;
            }
            node.dependOns.addAll(visitedNodes);
        }

        //detect circus
        if (circuitFounded) {
            Set<Circuit<T>> circuits = new LinkedHashSet<Circuit<T>>();

            for (Node<T> node : nodes.values()) {
                findCircuits(circuits, node, new java.util.Stack<Node<T>>());
            }

            ArrayList<Circuit<T>> list = new ArrayList<Circuit<T>>(circuits);
            Collections.sort(list);

            List<List> all = new ArrayList<List>();
            for (Circuit circuit : list) {
                all.add(unwrap(circuit.nodes));
            }

            throw new CircularReferencesException(all);
        }

        //Build Double Link Node List
        Node<T> rootNode = new Node<T>();
        rootNode.previous = rootNode;
        rootNode.next = nodes.values().iterator().next();

        for (Node<T> node : nodes.values()) {
            node.previous = rootNode.previous;
            rootNode.previous.next = node;
            node.next = rootNode;
            rootNode.previous = node;
        }

        //Sort by  Before/After
        Node<T> lastBeforeNode = rootNode;
        for (Node<T> node : nodes.values()) {
            if (node.beforeOthers) {
                moveAfter(node, lastBeforeNode);
                lastBeforeNode = node;
            } else if (node.afterOthers) {
                moveBefore(node, rootNode);
            }
        }

        //Sort by dependOns
        for (Node<T> node : nodes.values()) {
            for (Node<T> reference : node.dependOns) {
                swap(node, reference, rootNode);
            }
        }

        List<T> sortedList = new ArrayList<T>(nodes.size());
        Node<T> currentNode = rootNode.next;
        while (currentNode != rootNode) {
            sortedList.add(currentNode.object);
            currentNode = currentNode.next;
        }
        return sortedList;
    }

    private static <T> boolean normalizeNodeReferences(Node<T> rootNode, Node<T> node, Set<Node<T>> dependOns) {
        if (node.dependOns.contains(rootNode)) {
            return false;
        }
        for (Node<T> reference : node.dependOns) {
            if (!dependOns.add(reference)) {
                //this reference node has been visited in the past
                continue;
            }
            if (!normalizeNodeReferences(rootNode, reference, dependOns)) {
                return false;
            }
        }
        return true;
    }

    private static <T> void moveAfter(Node<T> insertNode, Node<T> targetNode) {
        //Remove the insertNode
        insertNode.previous.next = insertNode.next;
        insertNode.next.previous = insertNode.previous;
        //Insert the node after the targetNode
        targetNode.next.previous = insertNode;
        insertNode.next = targetNode.next;
        targetNode.next = insertNode;
        insertNode.previous = targetNode;
    }

    private static <T> void moveBefore(Node<T> insertNode, Node<T> targetNode) {
        //Remove the insertNode
        insertNode.previous.next = insertNode.next;
        insertNode.next.previous = insertNode.previous;
        //Insert the node before the targetNode
        targetNode.previous.next = insertNode;
        insertNode.previous = targetNode.previous;
        insertNode.next = targetNode;
        targetNode.previous = insertNode;
    }

    private static <T> void swap(Node<T> shouldAfterNode, Node<T> shouldBeforeNode, Node<T> rootNode) {
        Node<T> currentNode = shouldBeforeNode;
        while (currentNode.next != rootNode) {
            if (currentNode.next == shouldAfterNode) {
                return;
            }
            currentNode = currentNode.next;
        }
        //Remove the shouldAfterNode from list
        shouldAfterNode.previous.next = shouldAfterNode.next;
        shouldAfterNode.next.previous = shouldAfterNode.previous;
        //Insert the node immediately after the shouldBeforeNode
        shouldAfterNode.previous = shouldBeforeNode;
        shouldAfterNode.next = shouldBeforeNode.next;
        shouldBeforeNode.next = shouldAfterNode;
        shouldAfterNode.next.previous = shouldAfterNode;
    }

    private static <T> List<T> unwrap(List<Node<T>> nodes) {
        List<T> referees = new ArrayList<T>(nodes.size());
        for (Node<T> node : nodes) {
            referees.add(node.object);
        }
        return referees;
    }

    private static <T> void findCircuits(Set<Circuit<T>> circuits, Node<T> node, java.util.Stack<Node<T>> stack) {
        if (stack.contains(node)) {
            int fromIndex = stack.indexOf(node);
            int toIndex = stack.size();
            ArrayList<Node<T>> circularity = new ArrayList<Node<T>>(stack.subList(fromIndex, toIndex));
            // add ending node to list so a full circuit is shown
            circularity.add(node);
            Circuit circuit = new Circuit(circularity);
            circuits.add(circuit);
            return;
        }

        stack.push(node);

        for (Node<T> reference : node.dependOns) {
            findCircuits(circuits, reference, stack);
        }

        stack.pop();
    }

    private static class Node<T> implements Comparable<Node<T>> {

        public String name;

        public T object;

        public List<String> after = new ArrayList<String>();

        public List<String> before = new ArrayList<String>();

        public boolean afterOthers;

        public boolean beforeOthers;

        public final Set<Node<T>> dependOns = new HashSet<Node<T>>();

        public Node<T> next;

        public Node<T> previous;

        public Node() {
        }

        public Node(String name, T object, boolean afterOthers, boolean beforeOthers, List<String> after, List<String> before) {
            this.name = name;
            this.object = object;
            this.afterOthers = afterOthers;
            this.beforeOthers = beforeOthers;
            this.before = before;
            this.after = after;
        }

        public boolean equals(Object o) {
            if (this == o)
                return true;
            if (o == null || getClass() != o.getClass())
                return false;

            final Node<T> node = (Node<T>) o;

            return name.equals(node.name);
        }

        public int hashCode() {
            return name.hashCode();
        }

        public int compareTo(Node<T> o) {
            return this.name.compareTo(o.name);
        }

        public String toString() {
            return name;
        }
    }

    private static class Circuit<T> implements Comparable<Circuit<T>> {

        private final List<Node<T>> nodes;

        private final List<Node<T>> atomic;

        public Circuit(List<Node<T>> nodes) {
            this.nodes = nodes;
            atomic = new ArrayList<Node<T>>(nodes);
            atomic.remove(atomic.size() - 1);
            Collections.sort(atomic);
        }

        public List<Node<T>> getNodes() {
            return nodes;
        }

        public boolean equals(Object o) {
            if (this == o)
                return true;
            if (o == null || getClass() != o.getClass())
                return false;

            final Circuit<T> circuit = (Circuit<T>) o;

            if (!atomic.equals(circuit.atomic))
                return false;

            return true;
        }

        public int hashCode() {
            return atomic.hashCode();
        }

        public int compareTo(Circuit<T> o) {
            int i = atomic.size() - o.atomic.size();
            if (i != 0)
                return i;

            Iterator<Node<T>> iterA = atomic.listIterator();
            Iterator<Node<T>> iterB = o.atomic.listIterator();
            while (iterA.hasNext() && iterB.hasNext()) {
                Node<T> a = iterA.next();
                Node<T> b = iterB.next();
                i = a.compareTo(b);
                if (i != 0)
                    return i;
            }

            return 0;
        }

        public String toString() {
            return "Circuit(" + JoinUtils.join(",", unwrap(nodes)) + ")";
        }
    }
}
