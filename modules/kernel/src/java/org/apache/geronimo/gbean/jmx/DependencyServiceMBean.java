/* ====================================================================
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 2003 The Apache Software Foundation.  All rights
 * reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. The end-user documentation included with the redistribution,
 *    if any, must include the following acknowledgment:
 *       "This product includes software developed by the
 *        Apache Software Foundation (http://www.apache.org/)."
 *    Alternately, this acknowledgment may appear in the software itself,
 *    if and wherever such third-party acknowledgments normally appear.
 *
 * 4. The names "Apache" and "Apache Software Foundation" and
 *    "Apache Geronimo" must not be used to endorse or promote products
 *    derived from this software without prior written permission. For
 *    written permission, please contact apache@apache.org.
 *
 * 5. Products derived from this software may not be called "Apache",
 *    "Apache Geronimo", nor may "Apache" appear in their name, without
 *    prior written permission of the Apache Software Foundation.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE APACHE SOFTWARE FOUNDATION OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 *
 * ====================================================================
 */

package org.apache.geronimo.gbean.jmx;

/**
 * JMX MBean interface for {@link org.apache.geronimo.gbean.jmx.DependencyService}.
 *
 * @version $Revision: 1.1 $ $Date: 2004/01/22 20:58:40 $
 */
public interface DependencyServiceMBean
{

    /**
     * Declares a dependency from a child to a parent.
     * @param child the dependent component
     * @param parent the component the child is depending on
     */
    void addDependency(javax.management.ObjectName child,javax.management.ObjectName parent) ;

    /**
     * Removes a dependency from a child to a parent
     * @param child the dependnet component
     * @param parent the component that the child wil no longer depend on
     */
    void removeDependency(javax.management.ObjectName child,javax.management.ObjectName parent) ;

    /**
     * Removes all dependencies for a child
     * @param child the component that will no longer depend on anything
     */
    void removeAllDependencies(javax.management.ObjectName child) ;

    /**
     * Adds dependencies from the child to every parent in the parents set
     * @param child the dependent component
     * @param parents the set of components the child is depending on
     */
    void addDependencies(javax.management.ObjectName child,java.util.Set parents) ;

    /**
     * Gets the set of parents that the child is depending on
     * @param child the dependent component
     * @return a collection containing all of the components the child depends on; will never be null
     */
    java.util.Set getParents(javax.management.ObjectName child) ;

    /**
     * Gets all of the MBeans that have a dependency on the specified startParent.
     * @param parent the component the returned childen set depend on
     * @return a collection containing all of the components that depend on the parent; will never be null
     */
    java.util.Set getChildren(javax.management.ObjectName parent) ;

    /**
     * Adds a hold on a collection of object name patterns. If the name of a component matches an object name pattern in the collection, the component should not start.
     * @param objectName the name of the component placing the holds
     * @param holds a collection of object name patterns which should not start
     */
    void addStartHolds(javax.management.ObjectName objectName,java.util.Collection holds) ;

    /**
     * Removes a collection of holds.
     * @param objectName the object name of the components owning the holds
     * @param holds a collection of the holds to remove
     */
    void removeStartHolds(javax.management.ObjectName objectName,java.util.Collection holds) ;

    /**
     * Removes all of the holds owned by a component.
     * @param objectName the object name of the component that will no longer have any holds
     */
    void removeAllStartHolds(javax.management.ObjectName objectName) ;

    /**
     * Gets the object name of the mbean blocking the start specified mbean.
     * @param objectName the mbean to check for blockers
     * @return the mbean blocking the specified mbean, or null if there are no blockers
     */
    javax.management.ObjectName checkBlocker(javax.management.ObjectName objectName) ;

}
