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

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import javax.management.AttributeNotFoundException;
import javax.management.InstanceNotFoundException;
import javax.management.JMException;
import javax.management.ListenerNotFoundException;
import javax.management.MBeanNotificationInfo;
import javax.management.MBeanRegistration;
import javax.management.MBeanServer;
import javax.management.MBeanServerNotification;
import javax.management.Notification;
import javax.management.NotificationBroadcasterSupport;
import javax.management.NotificationEmitter;
import javax.management.NotificationFilter;
import javax.management.NotificationFilterSupport;
import javax.management.NotificationListener;
import javax.management.ObjectName;
import javax.management.ReflectionException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.geronimo.gbean.WaitingException;
import org.apache.geronimo.kernel.jmx.JMXUtil;
import org.apache.geronimo.kernel.management.EventProvider;
import org.apache.geronimo.kernel.management.ManagedObject;
import org.apache.geronimo.kernel.management.NotificationType;
import org.apache.geronimo.kernel.management.State;
import org.apache.geronimo.kernel.management.StateManageable;

/**
 * Abstract implementation of JSR77 StateManageable.
 * Implementors of StateManageable may use this class and simply provide
 * {@link #doStart()}, {@link #doStop()} and {@link #sendNotification(String)} methods.
 *
 * @version $Revision: 1.10 $ $Date: 2004/05/27 01:05:59 $
 */
public abstract class AbstractManagedObject implements ManagedObject, StateManageable, EventProvider, NotificationListener, MBeanRegistration, NotificationEmitter {
    protected final Log log = LogFactory.getLog(getClass());

    /**
     * The mbean server in which this server is registered.
     */
    protected MBeanServer server;

    /**
     * The unique name of this service.
     */
    protected ObjectName objectName;

    /**
     * The definitive list of notifications types supported by this service.
     */
    private final Set notificationTypes = new HashSet();

    /**
     * A dynamic proxy to the dependency service.
     */
    private DependencyServiceMBean dependencyService;

    /**
     * The sequence number of the events.
     */
    private long sequenceNumber;

    /**
     * The time this application started.
     */
    private long startTime;

    /**
     * The name of the object blocking the start of this mbean.
     */
    private ObjectName blocker;

    // This must be volatile otherwise getState must be synchronized which will result in deadlock as dependent
    // objects check if each other are in one state or another (i.e., classic A calls B while B calls A)
    private volatile State state = State.STOPPED;

    public AbstractManagedObject() {
        for (int i = 0; i < NotificationType.TYPES.length; i++) {
            notificationTypes.add(NotificationType.TYPES[i]);
        }
    }

    /**
     * The broadcaster for notifications
     */
    protected final NotificationBroadcasterSupport notificationBroadcaster = new NotificationBroadcasterSupport();

    /**
     * Do the start tasks for the component.  Called in the {@link State#STARTING} state by
     * the {@link #start()} and {@link #startRecursive()} methods to perform the tasks required to
     * start the component.
     * <p/>
     * Note: this method is called from within a synchronized block, so be careful what you call as you
     * may create a deadlock.
     */
    protected void doStart() throws Exception {
    }

    /**
     * Do the stop tasks for the component.  Called in the {@link State#STOPPING} state by
     * the {@link #stop()} method to perform the tasks required to stop the component.
     * <p/>
     * Note: this method is called from within a synchronized block, so be careful what you call as you
     * may create a deadlock.
     */
    protected void doStop() throws Exception {
    }

    /**
     * Do the failure tasks for the component.  Called in the {@link State#FAILED} state by
     * the {@link #fail()} method to perform the tasks required to cleanup a failed component.
     * <p/>
     * Note: this method is called from within a synchronized block, so be careful what you call as you
     * may create a deadlock.
     */
    protected void doFail() {
    }

    public synchronized ObjectName preRegister(MBeanServer server, ObjectName objectName) throws Exception {
        this.server = server;
        this.objectName = objectName;
        dependencyService = new DependencyServiceProxy(server);
        return objectName;
    }

    public void postRegister(Boolean registrationDone) {
        if (registrationDone.booleanValue()) {
            sendNotification(NotificationType.OBJECT_CREATED);
        }
    }

    public void preDeregister() throws Exception {
    }

    public void postDeregister() {
        sendNotification(NotificationType.OBJECT_DELETED);
        synchronized (this) {
            server = null;
            objectName = null;
            dependencyService = null;
        }
    }

    public MBeanServer getServer() {
        return server;
    }

    public final String getObjectName() {
        return objectName.toString();
    }

    public final ObjectName getObjectNameObject() {
        return objectName;
    }

    public DependencyServiceMBean getDependencyService() {
        return dependencyService;
    }

    public final boolean isStateManageable() {
        return true;
    }

    public boolean isStatisticsProvider() {
        return false;
    }

    public final boolean isEventProvider() {
        return true;
    }

    public final String[] getEventTypes() {
        return (String[]) notificationTypes.toArray(new String[notificationTypes.size()]);
    }

    public MBeanNotificationInfo[] getNotificationInfo() {
        return new MBeanNotificationInfo[]{
            new MBeanNotificationInfo(getEventTypes(), "javax.management.Notification", "J2EE Notifications")
        };
    }

    protected void addEventType(String eventType) {
        notificationTypes.add(eventType);
    }

    public void addNotificationListener(NotificationListener listener, NotificationFilter filter, Object handback) {
        notificationBroadcaster.addNotificationListener(listener, filter, handback);
    }

    public void removeNotificationListener(NotificationListener listener) throws ListenerNotFoundException {
        notificationBroadcaster.removeNotificationListener(listener);
    }

    public void removeNotificationListener(NotificationListener listener, NotificationFilter filter, Object handback) throws ListenerNotFoundException {
        notificationBroadcaster.removeNotificationListener(listener, filter, handback);
    }

    /**
     * Sends the specified MBean notification.
     * <p/>
     * Note:  This method can not be call while the current thread holds a syncronized lock on this MBean,
     * because this method sends JMX notifications.  Sending a general notification from a synchronized block
     * is a bad idea and therefore not allowed.
     *
     * @param type the notification type to send
     */
    public final void sendNotification(String type) {
        assert !Thread.holdsLock(this): "This method cannot be called while holding a synchronized lock on this";
        long seq;
        synchronized (this) {
            seq = sequenceNumber++;
        }
        notificationBroadcaster.sendNotification(new Notification(type, objectName, seq));
    }

    public void sendNotification(Notification notification) {
        assert !Thread.holdsLock(this): "This method cannot be called while holding a synchronized lock on this";
        notificationBroadcaster.sendNotification(notification);
    }

    public synchronized final long getStartTime() {
        return startTime;
    }

    /**
     * Moves this MBean to the {@link State#STARTING} state and then attempts to move this MBean immediately
     * to the {@link State#RUNNING} state.
     * <p/>
     * Note:  This method cannot be called while the current thread holds a synchronized lock on this MBean,
     * because this method sends JMX notifications. Sending a general notification from a synchronized block
     * is a bad idea and therefore not allowed.
     *
     * @throws java.lang.Exception If an exception occurs while starting this MBean
     */
    public final void start() throws Exception {
        assert !Thread.holdsLock(this): "This method cannot be called while holding a synchronized lock on this";

        // Move to the starting state
        synchronized (this) {
            State state = getStateInstance();
            if (state == State.STARTING || state == State.RUNNING) {
                return;
            }
            setStateInstance(State.STARTING);
        }
        sendNotification(State.STARTING.getEventTypeValue());

        attemptFullStart();
    }

    /**
     * Starts this MBean and then attempts to start all of its start dependent children.
     * <p/>
     * Note:  This method cannot be call while the current thread holds a synchronized lock on this MBean,
     * because this method sends JMX notifications.  Sending a general notification from a synchronized block
     * is a bad idea and therefore not allowed.
     *
     * @throws java.lang.Exception if a problem occurs will starting this MBean or any child MBean
     */
    public final void startRecursive() throws Exception {
        assert !Thread.holdsLock(this): "This method cannot be called while holding a synchronized lock on this";

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
        Set dependents = dependencyService.getChildren(objectName);
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
     * Moves this MBean to the STOPPING state, calls stop on all start dependent children, and then attempt
     * to move this MBean to the STOPPED state.
     * <p/>
     * Note:  This method can not be call while the current thread holds a syncronized lock on this MBean,
     * because this method sends JMX notifications.  Sending a general notification from a synchronized block
     * is a bad idea and therefore not allowed.
     *
     * @throws java.lang.Exception If an exception occurs while stoping this MBean or any of the childern
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
        sendNotification(State.STOPPING.getEventTypeValue());

        // Don't try to stop dependents from within a synchronized block... this should reduce deadlocks

        // stop all of my dependent objects
        Set dependents = dependencyService.getChildren(objectName);
        for (Iterator iterator = dependents.iterator(); iterator.hasNext();) {
            ObjectName child = (ObjectName) iterator.next();
            try {
                log.trace("Checking if child is running: child=" + child);
                if (((Integer) server.getAttribute(child, "state")).intValue() == State.RUNNING_INDEX) {
                    log.trace("Stopping child: child=" + child);
                    server.invoke(child, "stop", null, null);
                    log.trace("Stopped child: child=" + child);
                }
            } catch (Exception ignore) {
                // not a big deal... did my best
            }
        }

        attemptFullStop();
    }

    /**
     * Moves this MBean to the FAILED state.  There are no calls to dependent children, but they will be notified
     * using standard J2EE management notification.
     * <p/>
     * Note:  This method can not be call while the current thread holds a syncronized lock on this MBean,
     * because this method sends JMX notifications.  Sending a general notification from a synchronized block
     * is a bad idea and therefore not allowed.
     */
    final void fail() {
        assert !Thread.holdsLock(this): "This method cannot be called while holding a synchronized lock on this";

        synchronized (this) {
            State state = getStateInstance();
            if (state == State.STOPPED || state == State.FAILED) {
                return;
            }
            doSafeFail();
            setStateInstance(State.FAILED);
        }
        sendNotification(State.FAILED.getEventTypeValue());
    }

    /**
     * Attempts to bring the component into {@link State#RUNNING} state. If an Exception occurs while
     * starting the component, the component will be failed.
     *
     * @throws java.lang.Exception if a problem occurs while starting the component
     * <p/>
     * Note: Do not call this from within a synchronized block as it makes may send a JMX notification
     */
    void attemptFullStart() throws Exception {
        assert !Thread.holdsLock(this): "This method cannot be called while holding a synchronized lock on this";

        State newState = null;
        try {
            synchronized (this) {
                try {
                    // if we are still trying to start and can start now... start
                    if (getStateInstance() != State.STARTING) {
                        return;
                    }

                    // check if an mbean is blocking us from starting
                    blocker = dependencyService.checkBlocker(objectName);
                    if (blocker != null) {
                        try {
                            // register for state change with the blocker
                            NotificationFilterSupport stoppedFilter = new NotificationFilterSupport();
                            stoppedFilter.enableType(NotificationType.STATE_STOPPED);
                            stoppedFilter.enableType(NotificationType.STATE_FAILED);
                            stoppedFilter.enableType(NotificationType.OBJECT_DELETED);
                            server.addNotificationListener(blocker, this, stoppedFilter, null);

                            // watch for the blocker to unregister
                            NotificationFilterSupport mbeanServerFilter = new NotificationFilterSupport();
                            mbeanServerFilter.enableType(MBeanServerNotification.UNREGISTRATION_NOTIFICATION);
                            server.addNotificationListener(JMXUtil.DELEGATE_NAME, this, mbeanServerFilter, null);

                            // done for now... wait for the blocker to die
                            return;
                        } catch (InstanceNotFoundException e) {
                            // blocker died before we could get going... not a big deal
                        }
                    }

                    // check if all of the mbeans we depend on are running
                    Set parents = dependencyService.getParents(objectName);
                    for (Iterator i = parents.iterator(); i.hasNext();) {
                        ObjectName parent = (ObjectName) i.next();
                        if (!server.isRegistered(parent)) {
                            log.trace("Cannot run because parent is not registered: parent=" + parent);
                            return;
                        }
                        try {
                            log.trace("Checking if parent is running: parent=" + parent);
                            if (((Integer) server.getAttribute(parent, "state")).intValue() != State.RUNNING_INDEX) {
                                log.trace("Cannot run because parent is not running: parent=" + parent);
                                return;
                            }
                            log.trace("Parent is running: parent=" + parent);
                        } catch (AttributeNotFoundException e) {
                            // ok -- parent is not a startable
                            log.trace("Parent does not have a State attibute");
                        } catch (InstanceNotFoundException e) {
                            // depended on instance was removed bewteen the register check and the invoke
                            log.trace("Cannot run because parent is not registered: parent=" + parent);
                            return;
                        } catch (Exception e) {
                            // problem getting the attribute, parent has most likely failed
                            log.trace("Cannot run because an error occurred while checking if parent is running: parent=" + parent);
                            return;
                        }
                    }

                    // remove any open watches on a blocker
                    // todo is this correct if we are returning to a waiting state?
                    if (blocker != null) {
                        // remove any open watches on a blocker
                        try {
                            server.removeNotificationListener(blocker, this);
                        } catch (JMException ignore) {
                            // don't care, just cleaning up... blocker is most likely dead
                        }
                        try {
                            server.removeNotificationListener(JMXUtil.DELEGATE_NAME, this);
                        } catch (JMException ignore) {
                            // this should never happen... maybe server is dead
                        }
                        blocker = null;
                    }

                    try {
                        doStart();
                    } catch (WaitingException e) {
                        log.debug("Waiting to start: objectName=\"" + objectName + "\" reason=\"" + e.getMessage() + "\"");
                        return;
                    }
                    setStateInstance(State.RUNNING);
                    newState = State.RUNNING;
                } catch (Exception e) {
                    doSafeFail();
                    setStateInstance(State.FAILED);
                    newState = State.FAILED;
                    throw e;
                } catch (Error e) {
                    doSafeFail();
                    setStateInstance(State.FAILED);
                    newState = State.FAILED;
                    throw e;
                }
            }
        } finally {
            if (newState != null) {
                sendNotification(newState.getEventTypeValue());
            }
        }
    }

    /**
     * Attempt to bring the component into the fully stopped state.
     * If an exception occurs while stopping the component, the component will be failed.
     *
     * @throws java.lang.Exception if a problem occurs while stopping the component
     * <p/>
     * Note: Do not call this from within a synchronized block as it may send a JMX notification
     */
    void attemptFullStop() throws Exception {
        assert !Thread.holdsLock(this): "This method cannot be called while holding a synchronized lock on this";

        State newState = null;
        try {
            synchronized (this) {
                // if we are still trying to stop...
                if (getStateInstance() != State.STOPPING) {
                    return;
                }
                try {
                    // check if all of the mbeans depending on us are stopped
                    Set children = dependencyService.getChildren(objectName);
                    for (Iterator i = children.iterator(); i.hasNext();) {
                        ObjectName child = (ObjectName) i.next();
                        if (server.isRegistered(child)) {
                            try {
                                log.trace("Checking if child is stopped: child=" + child);
                                int state = ((Integer) server.getAttribute(child, "State")).intValue();
                                if (state == State.RUNNING_INDEX) {
                                    log.trace("Cannot stop because child is still running: child=" + child);
                                    return;
                                }
                            } catch (AttributeNotFoundException e) {
                                // ok -- dependect bean is not state manageable
                                log.trace("Child does not have a State attibute");
                            } catch (InstanceNotFoundException e) {
                                // depended on instance was removed between the register check and the invoke
                            } catch (Exception e) {
                                // problem getting the attribute, depended on bean has most likely failed
                                log.trace("Cannot run because an error occurred while checking if child is stopped: child=" + child);
                                return;
                            }
                        }
                    }

                    // if we can stop, stop
                    try {
                        doStop();
                    } catch (WaitingException e) {
                        log.debug("Waiting to stop: objectName=\"" + objectName + "\" reason=\"" + e.getMessage() + "\"");
                        return;
                    }
                    setStateInstance(State.STOPPED);
                    newState = State.STOPPED;
                } catch (Exception e) {
                    doSafeFail();
                    setStateInstance(State.FAILED);
                    newState = State.FAILED;
                    throw e;
                } catch (Error e) {
                    doSafeFail();
                    setStateInstance(State.FAILED);
                    newState = State.FAILED;
                    throw e;
                }
            }
        } finally {
            if (newState != null) {
                sendNotification(newState.getEventTypeValue());
            }
        }
    }

    /**
     * Calls {@link #doFail}, but catches all RutimeExceptions and Errors.
     * These problems are logged but ignored.
     * <p/>
     * Note: This must be called while holding a lock on this
     */
    private void doSafeFail() {
        assert Thread.holdsLock(this): "This method can only called while holding a synchronized lock on this";

        try {
            doFail();
        } catch (RuntimeException e) {
            log.warn("RuntimeError thrown from doFail", e);
        } catch (Error e) {
            log.warn("RuntimeError thrown from doFail", e);
        }
    }

    public void handleNotification(Notification n, Object o) {
        String type = n.getType();
        if (MBeanServerNotification.UNREGISTRATION_NOTIFICATION.equals(type)) {
            MBeanServerNotification notification = (MBeanServerNotification) n;
            ObjectName source = notification.getMBeanName();
            if (source.equals(blocker)) {
                try {
                    attemptFullStart();
                } catch (Exception e) {
                    log.warn("A problem occured while attempting to start", e);
                }
            }
        } else if (type.equals(NotificationType.STATE_STOPPED) ||
                type.equals(NotificationType.STATE_FAILED) ||
                type.equals(NotificationType.OBJECT_DELETED)) {

            ObjectName source = (ObjectName) n.getSource();
            if (source.equals(blocker)) {
                try {
                    attemptFullStart();
                } catch (Exception e) {
                    log.warn("A problem occured while attempting to start", e);
                }

            }
        }
    }

    public int getState() {
        return state.toInt();
    }

    public final State getStateInstance() {
        return state;
    }

    /**
     * Set the Component state.
     *
     * @param newState the target state to transition
     * @throws java.lang.IllegalStateException Thrown if the transition is not supported by the J2EE Management lifecycle.
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
                        throw new IllegalStateException("Cannot transition to " + newState + " state from " + state);
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
                        throw new IllegalStateException("Cannot transition to " + newState + " state from " + state);
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
                        throw new IllegalStateException("Cannot transition to " + newState + " state from " + state);
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
                        throw new IllegalStateException("Cannot transition to " + newState + " state from " + state);
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
                        throw new IllegalStateException("Cannot transition to " + newState + " state from " + state);
                }
                break;
        }
        log.debug(toString() + " State changed from " + state + " to " + newState);
        if (newState == State.RUNNING) {
            startTime = System.currentTimeMillis();
        }
        state = newState;
    }

    public String toString() {
        if (objectName == null) {
            return super.toString();
        }
        return objectName.toString();
    }
}
