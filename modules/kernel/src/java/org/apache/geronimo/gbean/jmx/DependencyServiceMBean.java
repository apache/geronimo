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

package org.apache.geronimo.gbean.jmx;

import java.util.Collection;
import java.util.Set;
import javax.management.ObjectName;

import org.apache.geronimo.kernel.jmx.JMXUtil;

/**
 * JMX MBean interface for {@link org.apache.geronimo.gbean.jmx.DependencyService}.
 *
 * @version $Revision: 1.4 $ $Date: 2004/05/26 03:22:21 $
 */
public interface DependencyServiceMBean {
    /**
     * Declares a dependency from a child to a parent.
     *
     * @param child  the dependent component
     * @param parent the component the child is depending on
     */
    void addDependency(ObjectName child, ObjectName parent);

    /**
     * Removes a dependency from a child to a parent
     *
     * @param child  the dependnet component
     * @param parent the component that the child wil no longer depend on
     */
    void removeDependency(ObjectName child, ObjectName parent);

    /**
     * Removes all dependencies for a child
     *
     * @param child the component that will no longer depend on anything
     */
    void removeAllDependencies(ObjectName child);

    /**
     * Adds dependencies from the child to every parent in the parents set
     *
     * @param child   the dependent component
     * @param parents the set of components the child is depending on
     */
    void addDependencies(ObjectName child, Set parents);

    /**
     * Gets the set of parents that the child is depending on
     *
     * @param child the dependent component
     * @return a collection containing all of the components the child depends on; will never be null
     */
     Set getParents(ObjectName child);

    /**
     * Gets all of the MBeans that have a dependency on the specified startParent.
     *
     * @param parent the component the returned childen set depend on
     * @return a collection containing all of the components that depend on the parent; will never be null
     */
    Set getChildren(ObjectName parent);

    /**
     * Adds a hold on a collection of object name patterns. If the name of a component matches an object name pattern in the collection, the component should not start.
     *
     * @param objectName the name of the component placing the holds
     * @param holds      a collection of object name patterns which should not start
     */
    void addStartHolds(ObjectName objectName, Collection holds);

    /**
     * Removes a collection of holds.
     *
     * @param objectName the object name of the components owning the holds
     * @param holds      a collection of the holds to remove
     */
    void removeStartHolds(ObjectName objectName, Collection holds);

    /**
     * Removes all of the holds owned by a component.
     *
     * @param objectName the object name of the component that will no longer have any holds
     */
    void removeAllStartHolds(ObjectName objectName);

    /**
     * Gets the object name of the mbean blocking the start specified mbean.
     *
     * @param objectName the mbean to check for blockers
     * @return the mbean blocking the specified mbean, or null if there are no blockers
     */
    ObjectName checkBlocker(ObjectName objectName);
}
