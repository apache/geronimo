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

package org.apache.geronimo.kernel.basic;

import org.apache.geronimo.gbean.AbstractName;
import org.apache.geronimo.gbean.AbstractNameQuery;
import org.apache.geronimo.kernel.DependencyManager;
import org.apache.geronimo.kernel.lifecycle.LifecycleAdapter;
import org.apache.geronimo.kernel.lifecycle.LifecycleListener;
import org.apache.geronimo.kernel.lifecycle.LifecycleMonitor;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 * DependencyManager is the record keeper of the dependencies in Geronimo.  The DependencyManager
 * does not enforce any dependencies, it is simply a place where components can register their intent
 * to be dependent on another component.  Since a JMX Component can pretty much do whatever it wants
 * a component must watch the components it depends on to assure that they are following the
 * J2EE-Management state machine.
 * <p/>
 * The DependencyManager uses the nomenclature of parent-child where a child is dependent on a parent.
 * The names parent and child have no other meaning are just a convience to make the code readable.
 *
 * @version $Rev$ $Date$
 */
public class BasicDependencyManager implements DependencyManager {
    /**
     * The lifecycleMonitor informs us when gbeans go off line,
     * so we can clean up the lingering dependencies.
     */
    private final LifecycleMonitor lifecycleMonitor;

    /**
     * Listenes for GBeans to unregister and removes all dependencies associated with the dependency
     */
    private final LifecycleListener lifecycleListener = new DependencyManagerLifecycleListener();

    /**
     * A map from child names to a list of parents.
     */
    private final Map<AbstractName, Set<AbstractName>> childToParentMap = new HashMap<AbstractName, Set<AbstractName>>();

    /**
     * A map from parent back to a list of its children.
     */
    private final Map<AbstractName, Set<AbstractName>> parentToChildMap = new HashMap<AbstractName, Set<AbstractName>>();

    public BasicDependencyManager(LifecycleMonitor lifecycleMonitor) {
        assert lifecycleMonitor != null;
        this.lifecycleMonitor = lifecycleMonitor;
        lifecycleMonitor.addLifecycleListener(lifecycleListener, new AbstractNameQuery(null, Collections.EMPTY_MAP, Collections.EMPTY_SET));
    }

    public synchronized void close() {
        lifecycleMonitor.removeLifecycleListener(lifecycleListener);
        childToParentMap.clear();
        parentToChildMap.clear();
    }

    /**
     * Declares a dependency from a child to a parent.
     *
     * @param child the dependent component
     * @param parent the component the child is depending on
     */
    public synchronized void addDependency(AbstractName child, AbstractName parent) {
        Set<AbstractName> parents = childToParentMap.get(child);
        if (parents == null) {
            parents = new HashSet<AbstractName>();
            childToParentMap.put(child, parents);
        }
        parents.add(parent);

        Set<AbstractName> children = parentToChildMap.get(parent);
        if (children == null) {
            children = new HashSet<AbstractName>();
            parentToChildMap.put(parent, children);
        }
        children.add(child);
    }

    /**
     * Removes a dependency from a child to a parent
     *
     * @param child the dependnet component
     * @param parent the component that the child wil no longer depend on
     */
    public synchronized void removeDependency(AbstractName child, AbstractName parent) {
        Set<AbstractName> parents = childToParentMap.get(child);
        if (parents != null) {
            parents.remove(parent);
        }

        Set<AbstractName> children = parentToChildMap.get(parent);
        if (children != null) {
            children.remove(child);
        }
    }

    /**
     * Removes all dependencies for a child
     *
     * @param child the component that will no longer depend on anything
     */
    public synchronized void removeAllDependencies(AbstractName child) {
        Set<AbstractName> parents = childToParentMap.remove(child);
        if (parents == null) {
            return;
        }
        for (Iterator<AbstractName> iterator = parents.iterator(); iterator.hasNext();) {
            AbstractName parent = iterator.next();
            Set<AbstractName> children = parentToChildMap.get(parent);
            if (children != null) {
                children.remove(child);
            }

        }
    }

    /**
     * Adds dependencies from the child to every parent in the parents set
     *
     * @param child the dependent component
     * @param parents the set of components the child is depending on
     */
    public synchronized void addDependencies(AbstractName child, Set<AbstractName> parents) {
        Set<AbstractName> existingParents = childToParentMap.get(child);
        if (existingParents == null) {
            existingParents = new HashSet<AbstractName>(parents);
            childToParentMap.put(child, existingParents);
        } else {
            existingParents.addAll(parents);
        }

        for (Iterator<AbstractName> i = parents.iterator(); i.hasNext();) {
            AbstractName startParent = i.next();
            Set<AbstractName> children = parentToChildMap.get(startParent);
            if (children == null) {
                children = new HashSet<AbstractName>();
                parentToChildMap.put(startParent, children);
            }
            children.add(child);
        }
    }

    /**
     * Gets the set of parents that the child is depending on
     *
     * @param child the dependent component
     * @return a collection containing all of the components the child depends on; will never be null
     */
    public synchronized Set<AbstractName> getParents(AbstractName child) {
        Set<AbstractName> parents = childToParentMap.get(child);
        if (parents == null) {
            return Collections.<AbstractName>emptySet();
        }
        return new HashSet<AbstractName>(parents);
    }

    /**
     * Gets all of the MBeans that have a dependency on the specified startParent.
     *
     * @param parent the component the returned childen set depend on
     * @return a collection containing all of the components that depend on the parent; will never be null
     */
    public synchronized Set<AbstractName> getChildren(AbstractName parent) {
        Set<AbstractName> children = parentToChildMap.get(parent);
        if (children == null) {
            return Collections.<AbstractName>emptySet();
        }
        return new HashSet<AbstractName>(children);
    }


    private class DependencyManagerLifecycleListener extends LifecycleAdapter {
        public void unloaded(AbstractName abstractName) {
            synchronized (BasicDependencyManager.this) {
                removeAllDependencies(abstractName);
            }

        }
    }
}
