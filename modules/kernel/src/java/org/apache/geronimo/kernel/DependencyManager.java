/**
 *
 * Copyright 2003-2004 The Apache Software Foundation
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

package org.apache.geronimo.kernel;

import org.apache.geronimo.gbean.AbstractName;

import java.util.Collection;
import java.util.Set;
import javax.management.ObjectName;

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
    public void addDependencies(AbstractName child, Set parents);

    /**
     * Gets the set of parents that the child is depending on
     *
     * @param child the dependent component
     * @return a collection containing all of the components the child depends on; will never be null
     */
    public Set getParents(AbstractName child);

    /**
     * Gets all of the MBeans that have a dependency on the specified startParent.
     *
     * @param parent the component the returned childen set depend on
     * @return a collection containing all of the components that depend on the parent; will never be null
     */
    public Set getChildren(AbstractName parent);

    /**
     * Adds a hold on a collection of object name patterns.  If the name of a component matches an object name
     * pattern in the collection, the component should not start.
     *
     * @param abstractName
     * @param holds a collection of object name patterns which should not start
     */
    public void addStartHolds(AbstractName abstractName, Collection holds);

    /**
     * Removes a collection of holds.
     *
     * @param abstractName
     * @param holds a collection of the holds to remove
     */
    public void removeStartHolds(AbstractName abstractName, Collection holds);

    /**
     * Removes all of the holds owned by a component.
     *
     * @param abstractName
     */
    public void removeAllStartHolds(AbstractName abstractName);

    /**
     * Gets the object name of the bean blocking the start specified bean.
     *
     * @param abstractName
     * @return the bean blocking the specified bean, or null if there are no blockers
     */
    public AbstractName checkBlocker(AbstractName abstractName);
}
