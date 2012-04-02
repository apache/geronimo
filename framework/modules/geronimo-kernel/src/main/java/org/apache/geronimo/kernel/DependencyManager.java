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

package org.apache.geronimo.kernel;

import org.apache.geronimo.gbean.AbstractName;

import java.util.Set;

/**
 * DependencyManager is the record keeper of the dependencies in Geronimo.  The DependencyManager
 * does not enforce any dependencies, it is simply a place where components can register their intent
 * to be dependent on another component.
 * <p/>
 * The DependencyManager uses the nomenclature of parent-child where a child is dependent on a parent.
 * The names parent and child have no other meaning are just a convience to make the code readable.
 *
 * @version $Rev$ $Date$
 */
public interface DependencyManager {
    /**
     * Closes the dependency manager releasing all resources
     */
    public void close();

    /**
     * Declares a dependency from a child to a parent.
     *
     * @param child the dependent component
     * @param parent the component the child is depending on
     */
    public void addDependency(AbstractName child, AbstractName parent);

    /**
     * Removes a dependency from a child to a parent
     *
     * @param child the dependnet component
     * @param parent the component that the child wil no longer depend on
     */
    public void removeDependency(AbstractName child, AbstractName parent);

    /**
     * Removes all dependencies for a child
     *
     * @param child the component that will no longer depend on anything
     */
    public void removeAllDependencies(AbstractName child);

    /**
     * Adds dependencies from the child to every parent in the parents set
     *
     * @param child the dependent component
     * @param parents the set of components the child is depending on
     */
    public void addDependencies(AbstractName child, Set<AbstractName> parents);

    /**
     * Gets the set of parents that the child is depending on
     *
     * @param child the dependent component
     * @return a collection containing all of the components the child depends on; will never be null
     */
    public Set<AbstractName> getParents(AbstractName child);

    /**
     * Gets all of the MBeans that have a dependency on the specified startParent.
     *
     * @param parent the component the returned childen set depend on
     * @return a collection containing all of the components that depend on the parent; will never be null
     */
    public Set<AbstractName> getChildren(AbstractName parent);

}
