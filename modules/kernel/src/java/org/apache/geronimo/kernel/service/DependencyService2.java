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
package org.apache.geronimo.kernel.service;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.management.JMException;
import javax.management.MBeanRegistration;
import javax.management.MBeanServer;
import javax.management.MBeanServerNotification;
import javax.management.Notification;
import javax.management.NotificationFilterSupport;
import javax.management.NotificationListener;
import javax.management.ObjectName;

import org.apache.geronimo.kernel.jmx.JMXUtil;

/**
 * DependencyService is the record keeper of the dependencies in Geronimo.  The DependencyService
 * does not enforce any dependencies, it is simply a place where components can register their intent
 * to be dependent on another component.  Since a JMX Component can pretty much do whatever it wants
 * a component must watch the components it depends on to assure that they are following the
 * J2EE-Management state machine.
 *
 * The DependencyService uses the nomenclature of parent-child where a child is dependent on a parent.
 * The names parent and child have no other meaning are just a convience to make the code readable.
 *
 * @jmx:mbean
 *
 * @version $Revision: 1.3 $ $Date: 2004/01/15 05:33:09 $
 */
public class DependencyService2 implements MBeanRegistration, NotificationListener, DependencyService2MBean {
    /**
     * The mbean server we are registered with.
     */
    private MBeanServer server;

    /**
     * A map from child names to a list of parents.
     */
    private final Map childToParentMap = new HashMap();

    /**
     * A map from parent back to a list of its children.
     */
    private final Map parentToChildMap = new HashMap();

    /**
     * A map from a component's ObjectName to the list of ObjectPatterns that the component is blocking
     * from starting.
     */
    private final Map startHoldsMap = new HashMap();

    public ObjectName preRegister(MBeanServer server, ObjectName objectName) throws Exception {
        if (objectName == null) {
            objectName = JMXUtil.DEPENDENCY_SERVICE_NAME;
        }
        this.server = server;

        NotificationFilterSupport mbeanServerFilter = new NotificationFilterSupport();
        mbeanServerFilter.enableType(MBeanServerNotification.UNREGISTRATION_NOTIFICATION);
        server.addNotificationListener(JMXUtil.DELEGATE_NAME, this, mbeanServerFilter, null);

        return objectName;
    }

    public void postRegister(Boolean aBoolean) {
    }

    public void preDeregister() throws Exception {
    }

    public void postDeregister() {
        try {
            server.removeNotificationListener(JMXUtil.DELEGATE_NAME, this);
        } catch (JMException ignored) {
            // no big deal... just good citizen clean up code
        }
        synchronized(this) {
            server = null;
            childToParentMap.clear();
            parentToChildMap.clear();
            startHoldsMap.clear();
        }
    }

    /**
     * Declares a dependency from a child to a parent.
     * @param child the dependent component
     * @param parent the component the child is depending on
     *
     * @jmx:managed-operation
     */
    public synchronized void addDependency(ObjectName child, ObjectName parent) {
        Set parents = (Set) childToParentMap.get(child);
        if (parents == null) {
            parents = new HashSet();
            childToParentMap.put(child, parents);
        }
        parents.add(parent);

        Set children = (Set) parentToChildMap.get(parent);
        if (children == null) {
            children = new HashSet();
            parentToChildMap.put(parent, children);
        }
        children.add(child);
    }

    /**
     * Removes a dependency from a child to a parent
     * @param child the dependnet component
     * @param parent the component that the child wil no longer depend on
     *
     * @jmx:managed-operation
     */
    public synchronized void removeDependency(ObjectName child, ObjectName parent) {
        Set parents = (Set) childToParentMap.get(child);
        if (parents != null) {
            parents.remove(parent);
        }

        Set children = (Set) parentToChildMap.get(parent);
        if (children != null) {
            children.remove(child);
        }
    }

    /**
     * Removes all dependencies for a child
     * @param child the component that will no longer depend on anything
     *
     * @jmx:managed-operation
     */
    public synchronized void removeAllDependencies(ObjectName child) {
        Set parents = (Set) childToParentMap.remove(child);
        if(parents == null) {
            return;
        }
        for (Iterator iterator = parents.iterator(); iterator.hasNext();) {
            ObjectName parent = (ObjectName) iterator.next();
            Set children = (Set) parentToChildMap.get(parent);
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
     *
     * @jmx:managed-operation
     */
    public synchronized void addDependencies(ObjectName child, Set parents) {
        Set existingParents = (Set) childToParentMap.get(child);
        if (existingParents == null) {
            existingParents = new HashSet(parents);
            childToParentMap.put(child, existingParents);
        } else {
            existingParents.addAll(parents);
        }

        for (Iterator i = parents.iterator(); i.hasNext();) {
            Object startParent = i.next();
            Set children = (Set) parentToChildMap.get(startParent);
            if (children == null) {
                children = new HashSet();
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
     *
     * @jmx:managed-operation
     */
    public synchronized Set getParents(ObjectName child) {
        Set parents = (Set) childToParentMap.get(child);
        if (parents == null) {
            return Collections.EMPTY_SET;
        }
        return parents;
    }

    /**
     * Gets all of the MBeans that have a dependency on the specified startParent.
     * 
     * @param parent the component the returned childen set depend on
     * @return a collection containing all of the components that depend on the parent; will never be null
     *
     * @jmx:managed-operation
     */
    public synchronized Set getChildren(ObjectName parent) {
        Set children = (Set) parentToChildMap.get(parent);
        if (children == null) {
            return Collections.EMPTY_SET;
        }
        return children;
    }

    /**
     * Adds a hold on a collection of object name patterns.  If the name of a component matches an object name
     * pattern in the collection, the component should not start.
     * @param objectName the name of the component placing the holds
     * @param holds a collection of object name patterns which should not start
     *
     * @jmx:managed-operation
     */
    public synchronized void addStartHolds(ObjectName objectName, java.util.Collection holds) {
        Collection currentHolds = (Collection)startHoldsMap.get(objectName);
        if(currentHolds == null) {
            currentHolds = new LinkedList(holds);
            startHoldsMap.put(objectName, currentHolds);
        } else {
            currentHolds.addAll(holds);
        }
    }

    /**
     * Removes a collection of holds.
     * @param objectName the object name of the components owning the holds
     * @param holds a collection of the holds to remove
     *
     * @jmx:managed-operation
     */
    public synchronized void removeStartHolds(ObjectName objectName, java.util.Collection holds) {
        Collection currentHolds = (Collection)startHoldsMap.get(objectName);
        if(currentHolds != null) {
            currentHolds.removeAll(holds);
        }
    }

    /**
     * Removes all of the holds owned by a component.
     * @param objectName the object name of the component that will no longer have any holds
     *
     * @jmx:managed-operation
     */
    public synchronized void removeAllStartHolds(ObjectName objectName) {
        startHoldsMap.remove(objectName);
    }

    /**
     * Gets the object name of the mbean blocking the start specified mbean.
     * @param objectName the mbean to check for blockers
     * @return the mbean blocking the specified mbean, or null if there are no blockers
     *
     * @jmx:managed-operation
     */
    public synchronized ObjectName checkBlocker(ObjectName objectName) {
        // check if objectName name is on one of the hold lists
        for (Iterator iterator = startHoldsMap.keySet().iterator(); iterator.hasNext();) {
            ObjectName blocker = (ObjectName) iterator.next();
            List holds = (List) startHoldsMap.get(blocker);
            for (Iterator holdsIterator = holds.iterator(); holdsIterator.hasNext();) {
                ObjectName pattern = (ObjectName) holdsIterator.next();
                if(pattern.apply(objectName)) {
                    return blocker;
                }
            }
        }
        return null;
    }

    public void handleNotification(Notification n, Object handback) {
        String type = n.getType();
        if (MBeanServerNotification.UNREGISTRATION_NOTIFICATION.equals(type)) {
            MBeanServerNotification notification = (MBeanServerNotification) n;
            ObjectName source = notification.getMBeanName();
            synchronized(this) {
                removeAllDependencies(source);
                removeAllStartHolds(source);
            }
        }
    }
}
