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
package org.apache.geronimo.deployment.dependency;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.Collections;
import javax.management.AttributeNotFoundException;
import javax.management.InstanceNotFoundException;
import javax.management.MBeanRegistration;
import javax.management.MBeanServer;
import javax.management.ObjectName;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.geronimo.common.State;
import org.apache.geronimo.jmx.JMXUtil;
import org.apache.geronimo.deployment.service.MBeanRelationship;

/**
 *
 *
 * @version $Revision: 1.1 $ $Date: 2003/08/17 05:40:18 $
 */
public class DependencyService implements MBeanRegistration, DependencyServiceMBean {
    private Log log = LogFactory.getLog(getClass());
    private MBeanServer server;
    private final Map startChildToParentMap = new HashMap();
    private final Map startParentToChildMap = new HashMap();
    private final Map createChildToParentMap = new HashMap();
    private final Map createParentToChildMap = new HashMap();
    private final Map relationshipMap = new HashMap();

    public ObjectName preRegister(MBeanServer server, ObjectName objectName) throws Exception {
        if (objectName == null) {
            objectName = JMXUtil.DEPENDENCY_SERVICE_NAME;
        }
        this.server = server;
        return null;
    }

    public void postRegister(Boolean aBoolean) {
    }

    public void preDeregister() throws Exception {
    }

    public void postDeregister() {
    }

    public synchronized void addStartDependency(ObjectName startChild, ObjectName startParent) {
        Set startParents = (Set) startChildToParentMap.get(startChild);
        if (startParents == null) {
            startParents = new HashSet();
            startChildToParentMap.put(startChild, startParents);
        }
        startParents.add(startParent);

        Set startChildren = (Set) startParentToChildMap.get(startParent);
        if (startChildren == null) {
            startChildren = new HashSet();
            startParentToChildMap.put(startParent, startChildren);
        }
        startChildren.add(startChild);
    }

    public synchronized void removeStartDependency(ObjectName startChild, ObjectName startParent) {
        Set startParents = (Set) startChildToParentMap.get(startChild);
        if (startParents != null) {
            startParents.remove(startParent);
        }

        Set startChildren = (Set) startParentToChildMap.get(startParent);
        if (startChildren != null) {
            startChildren.remove(startChild);
        }
    }

    public synchronized void addStartDependencies(ObjectName startChild, Set startParents) {
        Set existingStartParents = (Set) startChildToParentMap.get(startChild);
        if (existingStartParents == null) {
            existingStartParents = new HashSet(startParents);
            startChildToParentMap.put(startChild, existingStartParents);
        } else {
            existingStartParents.addAll(startParents);
        }

        for (Iterator i = startParents.iterator(); i.hasNext();) {
            Object startParent = i.next();
            Set startChildren = (Set) startParentToChildMap.get(startParent);
            if (startChildren == null) {
                startChildren = new HashSet();
                startParentToChildMap.put(startParent, startChildren);
            }
            startChildren.add(startChild);
        }
    }

    public synchronized Set getStartParents(ObjectName startChild) {
        Set startParents = (Set) startChildToParentMap.get(startChild);
        if (startParents == null) {
            return Collections.EMPTY_SET;
        }
        return startParents;
    }

    /**
     * Gets all of the MBeans that have a dependency on the specificed startParent.
     */
    public synchronized Set getStartChildren(ObjectName startParent) {
        Set startChildren = (Set) startParentToChildMap.get(startParent);
        if (startChildren == null) {
            return Collections.EMPTY_SET;
        }
        return startChildren;
    }


    public synchronized void addRelationships(ObjectName child, Set relationships) {
        Set existingRelationships = (Set) relationshipMap.get(child);
        if (existingRelationships == null) {
            existingRelationships = new HashSet(relationships);
            relationshipMap.put(child, existingRelationships);
        } else {
            existingRelationships.addAll(relationships);
        }

        for (Iterator i = relationships.iterator(); i.hasNext();) {
            MBeanRelationship relationship = (MBeanRelationship) i.next();
            ObjectName parent = relationship.getTarget();
            if (parent != null) {
                Set parents = (Set) createChildToParentMap.get(child);
                if (parents == null) {
                    parents = new HashSet();
                    createChildToParentMap.put(child, parents);
                }
                parents.add(parent);

                Set children = (Set) createParentToChildMap.get(parent);
                if (children == null) {
                    children = new HashSet();
                    createParentToChildMap.put(parent, children);
                }
                children.add(child);
            }
        }
    }

    public synchronized Set getRelationships(ObjectName child) {
        Set relationships = (Set) relationshipMap.get(child);
        if (relationships == null) {
            return Collections.EMPTY_SET;
        }
        return relationships;
    }

    public synchronized Set getCreateParents(ObjectName createChild) {
        Set createParents = (Set) createChildToParentMap.get(createChild);
        if (createParents == null) {
            return Collections.EMPTY_SET;
        }
        return createParents;
    }

    public synchronized Set getCreateChildren(ObjectName createParent) {
        Set createChildren = (Set) createParentToChildMap.get(createParent);
        if (createChildren == null) {
            return Collections.EMPTY_SET;
        }
        return createChildren;
    }

    public synchronized boolean canStart(ObjectName child) {
        Set createParents = new HashSet((Set) startChildToParentMap.get(child));
        for (Iterator i = createParents.iterator(); i.hasNext();) {
            ObjectName createParent = (ObjectName) i.next();
            if (!server.isRegistered(createParent)) {
                log.trace("Cannot run because parent is not registered: parent=" + createParent);
                return false;
            }
        }

        Set startParents = new HashSet((Set) startChildToParentMap.get(child));
        for (Iterator i = startParents.iterator(); i.hasNext();) {
            ObjectName startParent = (ObjectName) i.next();
            if (!server.isRegistered(startParent)) {
                log.trace("Cannot run because parent is not registered: parent=" + startParent);
                return false;
            }
            try {
                log.trace("Checking if parent is running: parent=" + startParent);
                if (((Integer) server.getAttribute(startParent, "State")).intValue() != State.RUNNING_INDEX) {
                    log.trace("Cannot run because parent is not running: parent=" + startParent);
                    return false;
                }
                log.trace("Parent is running: parent=" + startParent);
            } catch (AttributeNotFoundException e) {
                // ok -- parent is not a startable
                log.trace("Parent does not have a State attibute");
            } catch (InstanceNotFoundException e) {
                // depended on instance was removed bewteen the register check and the invoke
                log.trace("Cannot run because parent is not parent: parent=" + startParent);
                return false;
            } catch (Exception e) {
                // problem getting the attribute, parent has most likely failed
                log.trace("Cannot run because an error occurred while checking if parent is running: parent=" + startParent);
                return false;
            }
        }
        return true;
    }

    public synchronized boolean canStop(ObjectName dependency) {
        Set set = (Set) startParentToChildMap.get(dependency);
        if (set == null) {
            return true;
        }
        // make a copy before exiting the synchronized block
        Set dependents = new HashSet(set);

        for (Iterator u = dependents.iterator(); u.hasNext();) {
            ObjectName dependent = (ObjectName) u.next();
            if (server.isRegistered(dependent)) {
                try {
                    log.trace("Checking if dependent is stopped: dependent=" + dependent);
                    int state = ((Integer) server.getAttribute(dependent, "State")).intValue();
                    if (state != State.STOPPED_INDEX && state != State.FAILED_INDEX) {
                        log.trace("Cannot run because dependent is not stopped: dependent=" + dependent);
                        return false;
                    }
                } catch (AttributeNotFoundException e) {
                    // ok -- dependect bean is not state manageable
                    log.trace("Dependent does not have a State attibute");
                } catch (InstanceNotFoundException e) {
                    // depended on instance was removed between the register check and the invoke
                } catch (Exception e) {
                    // problem getting the attribute, depended on bean has most likely failed
                    log.trace("Cannot run because an error occurred while checking if dependent is stopped: dependent=" + dependent);
                    return false;
                }
            }
        }
        return true;
    }

    public synchronized boolean shouldStop(ObjectName dependent) {
        return !canStart(dependent);
    }
}
