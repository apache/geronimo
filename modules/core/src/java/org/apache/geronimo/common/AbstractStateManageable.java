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
package org.apache.geronimo.common;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import javax.management.InstanceNotFoundException;
import javax.management.MBeanNotificationInfo;
import javax.management.MBeanRegistration;
import javax.management.MBeanServer;
import javax.management.MBeanServerNotification;
import javax.management.Notification;
import javax.management.NotificationBroadcasterSupport;
import javax.management.NotificationFilterSupport;
import javax.management.NotificationListener;
import javax.management.ObjectName;
import javax.management.ReflectionException;
import javax.management.relation.InvalidRelationIdException;
import javax.management.relation.InvalidRoleValueException;
import javax.management.relation.RelationNotFoundException;
import javax.management.relation.RelationNotification;
import javax.management.relation.RelationServiceMBean;
import javax.management.relation.RelationServiceNotRegisteredException;
import javax.management.relation.RelationTypeNotFoundException;
import javax.management.relation.Role;
import javax.management.relation.RoleInfo;
import javax.management.relation.RoleList;
import javax.management.relation.RoleNotFoundException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.geronimo.deployment.dependency.DependencyServiceMBean;
import org.apache.geronimo.deployment.service.MBeanRelationship;
import org.apache.geronimo.jmx.JMXUtil;
import org.apache.geronimo.management.NotificationType;
import org.apache.geronimo.management.State;
import org.apache.geronimo.management.StateManageable;

/**
 * Abstract implementation of JSR77 StateManageable.
 * Implementors of StateManageable may use this class and simply provide
 * doStart, doStop and doNotification methods.
 *
 * @version $Revision: 1.6 $ $Date: 2003/08/20 07:13:25 $
 */
public abstract class AbstractStateManageable extends NotificationBroadcasterSupport implements StateManageable, NotificationListener, MBeanRegistration {
    protected Log log = LogFactory.getLog(getClass());
    protected MBeanServer server;
    protected ObjectName objectName;

    private DependencyServiceMBean dependencyService;
    private RelationServiceMBean relationService;
    private long sequenceNumber;
    private long startTime;

    // This must be volatile otherwise getState must be synchronized which will result in deadlock as dependent
    // objects check if each other are in one state or another (i.e., classic A calls B while B calls A)
    private volatile State state = State.STOPPED;

    /**
     * Check if component can start.  Dependencies in the dependency service have already been
     * checked at this point.
     *
     * Note: this method is called from within a synchronized block, so be careful what you call as you
     * may create a deadlock.
     *
     * @return true if the component can start; otherwise false
     */
    protected boolean canStart() {
        return true;
    }

    /**
     * Do the start tasks for the component.  Called in the STARTING state by
     * the start() and startRecursive() methods to perform the tasks required to
     * start the component.
     *
     * Note: this method is called from within a synchronized block, so be careful what you call as you
     * may create a deadlock.
     *
     * @throws Exception
     */
    protected abstract void doStart() throws Exception;

    /**
     * Check if component can stop.  Dependencies in the dependency service have already been
     * checked at this point.
     *
     * Note: this method is called from within a synchronized block, so be careful what you call as you
     * may create a deadlock.
     *
     * @return true if the component can stop; otherwise false
     */
    protected boolean canStop() {
        return true;
    }

    /**
     * Do the stop tasks for the component.  Called in the STOPPING state by the stop()
     * method to perform the tasks required to stop the component.
     *
     * Note: this method is called from within a synchronized block, so be careful what you call as you
     * may create a deadlock.
     *
     * @throws Exception
     */
    protected abstract void doStop() throws Exception;

    public MBeanNotificationInfo[] getNotificationInfo() {
        return new MBeanNotificationInfo[]{
            new MBeanNotificationInfo(NotificationType.TYPES,
                    "javax.management.Notification",
                    "J2EE Notifications")
        };
    }

    public ObjectName preRegister(MBeanServer server, ObjectName objectName) throws Exception {
        this.server = server;
        this.objectName = objectName;
        dependencyService = JMXUtil.getDependencyService(server);
        relationService = JMXUtil.getRelationService(server);

        NotificationFilterSupport mbeanServerFilter = new NotificationFilterSupport();
        mbeanServerFilter.enableType(MBeanServerNotification.REGISTRATION_NOTIFICATION);
        mbeanServerFilter.enableType(MBeanServerNotification.UNREGISTRATION_NOTIFICATION);
        server.addNotificationListener(JMXUtil.DELEGATE_NAME, this, mbeanServerFilter, null);

        NotificationFilterSupport relationServiceFilter = new NotificationFilterSupport();
        relationServiceFilter.enableType(RelationNotification.RELATION_BASIC_REMOVAL);
        relationServiceFilter.enableType(RelationNotification.RELATION_MBEAN_REMOVAL);
        server.addNotificationListener(JMXUtil.RELATION_SERVICE_NAME, this, relationServiceFilter, null);

        return objectName;
    }

    public void postRegister(Boolean aBoolean) {
    }

    public void preDeregister() throws Exception {
    }

    public void postDeregister() {
    }

    public void handleNotification(Notification n, Object o) {
        String type = n.getType();
        ObjectName source = null;
        if (MBeanServerNotification.REGISTRATION_NOTIFICATION.equals(type)) {
            MBeanServerNotification notification = (MBeanServerNotification) n;
            source = notification.getMBeanName();
            try {
                server.addNotificationListener(source, this, NotificationType.NOTIFICATION_FILTER, null);
            } catch (InstanceNotFoundException e) {
                // the instance died before we could get going... not a big deal
                return;
            }
        } else if (MBeanServerNotification.UNREGISTRATION_NOTIFICATION.equals(type)) {
            MBeanServerNotification notification = (MBeanServerNotification) n;
            source = notification.getMBeanName();
        } else if (RelationNotification.RELATION_BASIC_REMOVAL.equals(type) ||
                RelationNotification.RELATION_MBEAN_REMOVAL.equals(type)) {

            if(getStateInstance() == State.RUNNING) {
                RelationNotification notification = (RelationNotification) n;
                String relationType = notification.getRelationTypeName();
                if(relationType != null) {
                    Set relationships = dependencyService.getRelationships(objectName);
                    for (Iterator i = relationships.iterator(); i.hasNext();) {
                        MBeanRelationship relationship = (MBeanRelationship) i.next();
                        if(relationType.equals(relationship.getType())){
                            checkState();
                            return;
                        }
                    }
                }
            }
            return;
        } else {
            source = (ObjectName) n.getSource();
        }
        Set dependencies = dependencyService.getStartParents(objectName);
        if (dependencies.contains(source)) {
            checkState();
        }
    }

    public final State getStateInstance() {
        return state;
    }

    public final int getState() {
        return state.toInt();
    }

    public synchronized final long getStartTime() {
        return startTime;
    }

    /**
     * Moves this MBean to the STARTING state and then attempst to move this MBean immedately to the STARTED
     * state.
     *
     * Note:  This method can not be call while the current thread holds a syncronized lock on this MBean,
     * because this method sends JMX notifications.  Sending a general notification from a synchronized block
     * is a bad idea and therefore not allowed.
     *
     * @throws Exception  If an exception occurs while starting this MBean
     */
    public final void start() throws Exception {
        assert !Thread.holdsLock(this): "This method cannot be called while holding a syncrhonized lock on this";

        // Move to the starting state
        synchronized (this) {
            State state = getStateInstance();
            if (state == State.STARTING || state == State.RUNNING) {
                return;
            }
            setStateInstance(State.STARTING);
        }
        doNotification(State.STARTING.getEventTypeValue());

        State newState = null;
        try {
            synchronized (this) {
                try {
                    // if we are still trying to start and can start now... start
                    if(getStateInstance() == State.STARTING &&
                            dependencyService.canStart(objectName) && canStart()) {
                        enrollInRelationships();
                        doStart();
                        setStateInstance(State.RUNNING);
                        newState = State.RUNNING;
                    }
                } catch (Exception e) {
                    setStateInstance(State.FAILED);
                    newState = State.FAILED;
                    throw e;
                } catch (Error e) {
                    setStateInstance(State.FAILED);
                    newState = State.FAILED;
                    throw e;
                }
            }
        } finally {
            if(newState != null) {
                doNotification(newState.getEventTypeValue());
            }
        }
    }

    /**
     * Moves this MBean to the STOPPING state, calls stop on all start dependent children, and then attempt
     * to move this MBean to the STOPPED state.
     *
     * Note:  This method can not be call while the current thread holds a syncronized lock on this MBean,
     * because this method sends JMX notifications.  Sending a general notification from a synchronized block
     * is a bad idea and therefore not allowed.
     *
     * @throws Exception  If an exception occurs while stoping this MBean or any of the childern
     */
    public final void stop() throws Exception {
        assert !Thread.holdsLock(this): "This method cannot be called while holding a syncrhonized lock on this";

        // move to the stopping state
        synchronized (this) {
            State state = getStateInstance();
            if (state == State.STOPPED || state == State.STOPPING) {
                return;
            }
            setStateInstance(State.STOPPING);
        }
        doNotification(State.STOPPING.getEventTypeValue());

        // Don't try to stop dependents from within a synchronized block... this should reduce deadlocks

        // stop all of my dependent objects
        Set dependents = dependencyService.getStartChildren(objectName);
        for (Iterator iterator = dependents.iterator(); iterator.hasNext();) {
            ObjectName name = (ObjectName) iterator.next();
            try {
                server.invoke(name, "stop", null, null);
            } catch (ReflectionException e) {
                if (e.getTargetException() instanceof NoSuchMethodException) {
                    // did not have a stop method - ok
                } else {
                    throw e;
                }
            }
        }

        // Try to fully stop
        State newState = null;
        try {
            synchronized (this) {
                // if we are still trying to stop...
                if (getStateInstance() == State.STOPPING) {
                    try {
                        // if we can stop, stop
                        if (dependencyService.canStop(objectName) && canStop()) {
                            unenrollInRelationships();
                            doStop();
                            setStateInstance(State.STOPPED);
                            newState = State.STOPPED;
                        }
                    } catch (Exception e) {
                        setStateInstance(State.FAILED);
                        newState = State.FAILED;
                        throw e;
                    } catch (Error e) {
                        setStateInstance(State.FAILED);
                        newState = State.FAILED;
                        throw e;
                    }
                }
            }
        } finally {
            if(newState != null) {
                doNotification(newState.getEventTypeValue());
            }
        }
    }

    /**
     * Starts this MBean and then attempts to start all of the start dependent children of this MBean.
     *
     * Note:  This method can not be call while the current thread holds a syncronized lock on this MBean,
     * because this method sends JMX notifications.  Sending a general notification from a synchronized block
     * is a bad idea and therefore not allowed.
     *
     * @throws Exception  if a problem occurs will starting this MBean or any child MBean
     */
    public final void startRecursive() throws Exception {
        assert !Thread.holdsLock(this): "This method cannot be called while holding a syncrhonized lock on this";

        State state = getStateInstance();
        if (state != State.STOPPED && state != State.FAILED) {
            // Cannot startRecursive while in the stopping state
            // Dain: I don't think we can throw an exception here because there is no way for the caller
            // to lock the instance and check the state before calling
            return;
        }

        // get myself starting
        start();

        // startRecursive all of objects that depend on me
        Set dependents = dependencyService.getStartChildren(objectName);
        for (Iterator iterator = dependents.iterator(); iterator.hasNext();) {
            ObjectName dependent = (ObjectName) iterator.next();
            try {
                server.invoke(dependent, "startRecursive", null, null);
            } catch (ReflectionException e) {
                if (e.getTargetException() instanceof NoSuchMethodException) {
                    // did not have a startRecursive method - ok
                } else {
                    throw e;
                }
            }
        }
    }

    /**
     * Sends the specified MBean notification.
     *
     * Note:  This method can not be call while the current thread holds a syncronized lock on this MBean,
     * because this method sends JMX notifications.  Sending a general notification from a synchronized block
     * is a bad idea and therefore not allowed.
     *
     * @param type the notification type to send
     */
    private final void doNotification(String type) {
        assert !Thread.holdsLock(this): "This method cannot be called while holding a syncrhonized lock on this";
        sendNotification(new Notification(type, this, sequenceNumber++));
    }

    /**
     * Enrolls this MBean is all relationships specified in the dependency service.
     *
     * @throws StartException is this MBean can not be enrolled in a relationship
     */
    private synchronized final void enrollInRelationships() throws StartException {
        String relationshipType = null;
        String relationshipRole = null;
        String targetRoleName = null;
        try {
            Set relationships = dependencyService.getRelationships(objectName);
            for (Iterator i = relationships.iterator(); i.hasNext();) {
                MBeanRelationship relationship = (MBeanRelationship) i.next();

                // if we don't have a relationship instance create one
                String relationshipName = relationship.getName();
                relationshipRole = relationship.getRole();
                if (!relationService.hasRelation(relationshipName).booleanValue()) {
                    relationshipType = relationship.getType();
                    RoleList roleList = new RoleList();
                    roleList.add(new Role(relationshipRole, Collections.singletonList(objectName)));

                    // if we have a target we need to add it to the role list
                    ObjectName target = relationship.getTarget();
                    if (target != null) {
                        targetRoleName = relationship.getTargetRole();
                        if (targetRoleName == null || targetRoleName.length() == 0) {
                            List roles = relationService.getRoleInfos(relationshipType);
                            if (roles.size() < 2) {
                                throw new StartException("Relationship has less than two roles. You cannot specify a target");
                            }
                            if (roles.size() > 2) {
                                throw new StartException("Relationship has more than two roles. You must use targetRoleName");
                            }
                            if (((RoleInfo) roles.get(0)).getName().equals(relationshipRole)) {
                                targetRoleName = ((RoleInfo) roles.get(1)).getName();
                            } else {
                                targetRoleName = ((RoleInfo) roles.get(0)).getName();
                            }
                            relationship.setTargetRole(targetRoleName);
                        }

                        roleList.add(new Role(targetRoleName, Collections.singletonList(target)));
                    }
                    relationService.createRelation(relationshipName, relationshipType, roleList);
                    relationship.setCreatedRelationship(true);
                } else {
                    // We have an exiting relationship -- just add to the existing role
                    List members = relationService.getRole(relationshipName, relationshipRole);
                    if (!members.contains(objectName)) {
                        members.add(objectName);
                        relationService.setRole(relationshipName, new Role(relationshipRole, members));
                    }
                    relationship.setCreatedRelationship(false);
                }
            }
        } catch (RelationTypeNotFoundException e) {
            throw new StartException("Relationship type is not registered: relationType=" + relationshipType);
        } catch (RelationServiceNotRegisteredException e) {
            throw new StartException("RelationshipService is not registered", e);
        } catch (RoleNotFoundException e) {
            throw new StartException("RelationshipService is not registered", e);
        } catch (InvalidRelationIdException e) {
            throw new StartException("Relationship type does not contain role:" +
                    " relationType=" + relationshipType +
                    " sourceRole=" + relationshipRole +
                    " targetRole=" + targetRoleName, e);
        } catch (InvalidRoleValueException e) {
            throw new StartException("Relationship role state is invalid", e);
        } catch (RelationNotFoundException e) {
            throw new StartException("Relation was unregistered while executing", e);
        }
    }

    /**
     * Removes this MBean from all relationships specified in the dependency service.
     *
     * @throws StopException is this MBean can not be removed in a relationship
     */
    private synchronized final void unenrollInRelationships() throws StopException {
        String relationshipType = null;
        String relationshipRole = null;
        try {
            Set relationships = dependencyService.getRelationships(objectName);
            for (Iterator i = relationships.iterator(); i.hasNext();) {
                MBeanRelationship relationship = (MBeanRelationship) i.next();
                String relationshipName = relationship.getName();
                if(relationship.getCreatedRelationship()) {
                    // drop the entire relationship
                    relationService.removeRelation(relationshipName);
                } else {
                    // just remove myself from the relationship
                    relationshipRole = relationship.getRole();
                    List members = relationService.getRole(relationshipName, relationshipRole);
                    if (members.contains(objectName)) {
                        members.remove(objectName);
                        relationService.setRole(relationshipName, new Role(relationshipRole, members));
                    }
                }
            }
        } catch (RelationTypeNotFoundException e) {
            throw new StopException("Relationship type is not registered: relationType=" + relationshipType);
        } catch (RelationServiceNotRegisteredException e) {
            throw new StopException("RelationshipService is not registered", e);
        } catch (RoleNotFoundException e) {
            throw new StopException("RelationshipService is not registered", e);
        } catch (InvalidRoleValueException e) {
            throw new StopException("Relationship role state is invalid", e);
        } catch (RelationNotFoundException e) {
            throw new StopException("Relation was unregistered while executing", e);
        }
    }

    /**
     * Checks if we need to make a state transition based on our dependencies registered with the dependency service.
     *
     * Note: Do not call this from within a synchronized block as it makes may send a JMX notification
     */
    protected void checkState() {
        assert !Thread.holdsLock(this): "This method cannot be called while holding a syncrhonized lock on this";

        State newState = null;
        try {
            synchronized (this) {
                State state = getStateInstance();
                if (state == State.STARTING) {
                    if (dependencyService.canStart(objectName) && canStart()) {
                        try {
                            doStart();
                            setStateInstance(State.RUNNING);
                            newState = State.RUNNING;
                        } catch (Exception e) {
                            setStateInstance(State.FAILED);
                            newState = State.FAILED;
                        } catch (Error e) {
                            setStateInstance(State.FAILED);
                            newState = State.FAILED;
                        }
                    }
                } else if (state == State.RUNNING) {
                    // Someone stopping, stopped, failed or unregistered we need to change state
                    State recommendState = dependencyService.shouldChangeState(objectName);
                    if (recommendState != null) {
                        setStateInstance(recommendState);
                        newState = recommendState;
                    }
                } else if (state == State.STOPPING) {
                    if (dependencyService.canStop(objectName) && canStop()) {
                        try {
                            unenrollInRelationships();
                            doStop();
                            setStateInstance(State.STOPPED);
                            newState = State.STOPPED;
                        } catch (Exception e) {
                            setStateInstance(State.FAILED);
                            newState = State.FAILED;
                        } catch (Error e) {
                            setStateInstance(State.FAILED);
                            newState = State.FAILED;
                        }
                    }
                }
            }
        } finally {
            if (newState != null) {
                doNotification(newState.getEventTypeValue());
            }
        }
    }

    /**
     * Set the Component state.
     * @param newState the target state to transition
     * @throws IllegalStateException Thrown if the transition is not supported by the J2EE Management lifecycle.
     */
    private synchronized void setStateInstance(State newState) throws IllegalStateException {
        switch (state.toInt()) {
        case State.STOPPED_INDEX:
            switch (newState.toInt()) {
            case State.STARTING_INDEX:
                break;
            case State.STOPPED_INDEX:
            case State.RUNNING_INDEX:
            case State.STOPPING_INDEX:
            case State.FAILED_INDEX:
                throw new IllegalStateException(
                        "Can not transition to " + newState + " state from " + state);
            }
            break;

        case State.STARTING_INDEX:
            switch (newState.toInt()) {
            case State.RUNNING_INDEX:
            case State.FAILED_INDEX:
            case State.STOPPING_INDEX:
                break;
            case State.STOPPED_INDEX:
            case State.STARTING_INDEX:
                throw new IllegalStateException(
                        "Can not transition to " + newState + " state from " + state);
            }
            break;

        case State.RUNNING_INDEX:
            switch (newState.toInt()) {
            case State.STOPPING_INDEX:
            case State.FAILED_INDEX:
                break;
            case State.STOPPED_INDEX:
            case State.STARTING_INDEX:
            case State.RUNNING_INDEX:
                throw new IllegalStateException(
                        "Can not transition to " + newState + " state from " + state);

            }
            break;

        case State.STOPPING_INDEX:
            switch (newState.toInt()) {
            case State.STOPPED_INDEX:
            case State.FAILED_INDEX:
                break;
            case State.STARTING_INDEX:
            case State.RUNNING_INDEX:
            case State.STOPPING_INDEX:
                throw new IllegalStateException(
                        "Can not transition to " + newState + " state from " + state);
            }
            break;

        case State.FAILED_INDEX:
            switch (newState.toInt()) {
            case State.STARTING_INDEX:
            case State.STOPPING_INDEX:
                break;
            case State.RUNNING_INDEX:
            case State.STOPPED_INDEX:
            case State.FAILED_INDEX:
                throw new IllegalStateException(
                        "Can not transition to " + newState + " state from " + state);
            }
            break;
        }
        log.debug(objectName.toString() + " State changed from " + state + " to " + newState);
        if (newState == State.RUNNING) {
            startTime = System.currentTimeMillis();
        }
        state = newState;
    }

    public String toString() {
        return objectName.toString();
    }
}
