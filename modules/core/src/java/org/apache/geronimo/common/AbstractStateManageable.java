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
import javax.management.relation.InvalidRelationIdException;
import javax.management.relation.InvalidRoleValueException;
import javax.management.relation.RelationNotFoundException;
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
import org.apache.management.j2ee.State;
import org.apache.management.j2ee.StateManageable;
import org.apache.management.j2ee.NotificationType;

/**
 * Abstract implementation of JSR77 StateManageable.
 * Implementors of StateManageable may use this class and simply provide
 * doStart, doStop and doNotification methods.
 *
 * @version $Revision: 1.4 $ $Date: 2003/08/18 13:30:19 $
 */
public abstract class AbstractStateManageable extends NotificationBroadcasterSupport implements StateManageable, NotificationListener, MBeanRegistration {
    protected Log log = LogFactory.getLog(getClass());
    protected MBeanServer server;
    protected ObjectName objectName;

    private DependencyServiceMBean dependencyService;
    private RelationServiceMBean relationService;
    private long sequenceNumber;
    private State state = State.STOPPED;
    private long startTime;

    /**
     * Do the start tasks for the component.  Called in the STARTING state by
     * the start() and startRecursive() methods to perform the tasks required to
     * start the component.
     * @throws Exception
     */
    protected abstract void doStart() throws Exception;

    /**
     * Do the stop tasks for the component.  Called in the STOPPING state by the stop()
     * method to perform the tasks required to stop the component.
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

        NotificationFilterSupport filter = new NotificationFilterSupport();
        filter.enableType(MBeanServerNotification.REGISTRATION_NOTIFICATION);
        filter.enableType(MBeanServerNotification.UNREGISTRATION_NOTIFICATION);
        server.addNotificationListener(JMXUtil.DELEGATE_NAME, this, filter, null);

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

    public final long getStartTime() {
        return startTime;
    }

    public final void start() throws Exception {
        State state = getStateInstance();
        if (state == State.STARTING || state == State.RUNNING) {
            return;
        }
        try {
            setState(State.STARTING);
            if (dependencyService.canStart(objectName)) {
                enrollInRelationships();
                doStart();
                setState(State.RUNNING);
            }
        } catch (Exception e) {
            setState(State.FAILED);
            throw e;
        } catch (Error e) {
            setState(State.FAILED);
            throw e;
        }
    }

    public final void stop() throws Exception {
        State state = getStateInstance();
        if (state == State.STOPPED || state == State.STOPPING) {
            return;
        } else if (state == State.STARTING) {
            setState(State.STOPPED);
            return;
        }
        try {
            setState(State.STOPPING);

            // stop all of my dependent objects
            Set dependents = dependencyService.getStartChildren(objectName);
            for (Iterator iterator = dependents.iterator(); iterator.hasNext();) {
                ObjectName name = (ObjectName) iterator.next();
                server.invoke(name, "stop", null, null);
            }

            // stop myself
            if (dependencyService.canStop(objectName)) {
                doStop();
                setState(State.STOPPED);
            }
        } catch (Exception e) {
            setState(State.FAILED);
            throw e;
        } catch (Error e) {
            setState(State.FAILED);
            throw e;
        }
    }

    public final void startRecursive() throws Exception {
        State state = getStateInstance();
        if (state == State.STOPPING) {
            throw new IllegalArgumentException("Cannot startRecursive while in the stopping state");
        }

        // get myself starting
        start();

        // startRecursive all of objects that depend on me
        Set dependents = dependencyService.getStartChildren(objectName);
        for (Iterator iterator = dependents.iterator(); iterator.hasNext();) {
            ObjectName dependent = (ObjectName) iterator.next();
            server.invoke(dependent, "startRecursive", null, null);
        }
    }

    private final void doNotification(String s) {
        sendNotification(new Notification(s, this, sequenceNumber++));
    }

    private final void enrollInRelationships() throws StartException {
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
                        }

                        roleList.add(new Role(targetRoleName, Collections.singletonList(target)));
                    }
                    relationService.createRelation(relationshipName, relationshipType, roleList);
                } else {
                    // We have an exiting relationship -- just add to the existing role
                    List members = relationService.getRole(relationshipName, relationshipRole);
                    if(!members.contains(objectName)) {
                        members.add(objectName);
                        relationService.setRole(relationshipName, new Role(relationshipRole, members));
                    }
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

    private void checkState() {
        State state = getStateInstance();
        if (state == State.STARTING) {
            if (dependencyService.canStart(objectName)) {
                try {
                    doStart();
                    setState(State.RUNNING);
                } catch (Exception e) {
                    setState(State.FAILED);
                } catch (Error e) {
                    setState(State.FAILED);
                }
            }
        } else if (state == State.RUNNING) {
            if (dependencyService.shouldStop(objectName)) {
                // we were running and someone stopped or unregisted without informing us
                // try to stop immedately, or just fail
                if (dependencyService.canStop(objectName)) {
                    try {
                        setState(State.STOPPING);
                        doStop();
                        setState(State.STOPPED);
                    } catch (Exception e) {
                        setState(State.FAILED);
                    } catch (Error e) {
                        setState(State.FAILED);
                    }
                } else {
                    setState(State.FAILED);
                }
            }
        } else if (state == State.STOPPING) {
            if (dependencyService.canStop(objectName)) {
                try {
                    doStop();
                    setState(State.STOPPED);
                } catch (Exception e) {
                    setState(State.FAILED);
                } catch (Error e) {
                    setState(State.FAILED);
                }
            }
        }
    }

    /**
     * Set the Component state.
     * @param newState
     * @throws IllegalStateException Thrown if the transition is not supported by the JSR77 lifecycle.
     */
    private void setState(State newState) throws IllegalStateException {
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
        log.debug("State changed from " + state + " to " + newState);
        if (newState == State.RUNNING) {
            startTime = System.currentTimeMillis();
        }
        state = newState;

        doNotification(state.getEventTypeValue());
    }
}
