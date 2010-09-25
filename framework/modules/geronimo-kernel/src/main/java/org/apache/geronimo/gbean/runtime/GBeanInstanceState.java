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
package org.apache.geronimo.gbean.runtime;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.geronimo.gbean.AbstractName;
import org.apache.geronimo.kernel.DependencyManager;
import org.apache.geronimo.kernel.GBeanNotFoundException;
import org.apache.geronimo.kernel.Kernel;
import org.apache.geronimo.kernel.management.State;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Iterator;
import java.util.Set;

/**
 * @version $Rev$ $Date$
 */
public class GBeanInstanceState
{
    private static final Logger log = LoggerFactory.getLogger(GBeanInstanceState.class);

    /**
     * The GBeanInstance in which this server is registered.
     */
    private final GBeanInstance gbeanInstance;

    /**
     * The kernel in which this server is registered.
     */
    private final Kernel kernel;

    /**
     * The unique name of this service.
     */
    private final AbstractName abstractName;

    /**
     * The dependency manager
     */
    private final DependencyManager dependencyManager;

    /**
     * The broadcaster of lifecycle events
     */
    private final LifecycleBroadcaster lifecycleBroadcaster;

    // This must be volatile otherwise getState must be synchronized which will result in deadlock as dependent
    // objects check if each other are in one state or another (i.e., classic A calls B while B calls A)
    private volatile State state = State.STOPPED;

    GBeanInstanceState(AbstractName abstractName, Kernel kernel, DependencyManager dependencyManager, GBeanInstance gbeanInstance, LifecycleBroadcaster lifecycleBroadcaster) {
        this.abstractName = abstractName;
        this.kernel = kernel;
        this.dependencyManager = dependencyManager;
        this.gbeanInstance = gbeanInstance;
        this.lifecycleBroadcaster = lifecycleBroadcaster;
    }

    /**
     * Moves this MBean to the {@link org.apache.geronimo.kernel.management.State#STARTING} state and then attempts to move this MBean immediately
     * to the {@link org.apache.geronimo.kernel.management.State#RUNNING} state.
     * <p/>
     * Note:  This method cannot be called while the current thread holds a synchronized lock on this MBean,
     * because this method sends JMX notifications. Sending a general notification from a synchronized block
     * is a bad idea and therefore not allowed.
     */
    public final void start() {
        assert !Thread.holdsLock(this): "This method cannot be called while holding a synchronized lock on this";

        // Move to the starting state
        State originalState;
        synchronized (this) {
            originalState = getStateInstance();
            if (originalState == State.RUNNING) {
                return;
            }
            // only try to change states if we are not already starting
            if (originalState != State.STARTING) {
                setStateInstance(State.STARTING);
            }
        }

        // only fire a notification if we are not already starting
        if (originalState != State.STARTING) {
            lifecycleBroadcaster.fireStartingEvent();
        }

        attemptFullStart();
    }

    /**
     * Starts this MBean and then attempts to start all of its start dependent children.
     * <p/>
     * Note:  This method cannot be call while the current thread holds a synchronized lock on this MBean,
     * because this method sends JMX notifications.  Sending a general notification from a synchronized block
     * is a bad idea and therefore not allowed.
     */
    public final void startRecursive() {
        assert !Thread.holdsLock(this): "This method cannot be called while holding a synchronized lock on this";

        State state = getStateInstance();
        if (state != State.STOPPED && state != State.FAILED && state != State.RUNNING) {
            // Cannot startRecursive while in the stopping state
            // Dain: I don't think we can throw an exception here because there is no way for the caller
            // to lock the instance and check the state before calling
            return;
        }

        // get myself starting
        start();

        // startRecursive all of objects that depend on me
        Set dependents = dependencyManager.getChildren(abstractName);
        for (Iterator iterator = dependents.iterator(); iterator.hasNext();) {
            AbstractName dependent = (AbstractName) iterator.next();
            try {
                kernel.startRecursiveGBean(dependent);
            } catch (GBeanNotFoundException e) {
                // this is ok the gbean died before we could start it
            } catch (Exception e) {
                // there is something wrong with this gbean... skip it
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
     */
    public final void stop() {
        assert !Thread.holdsLock(this): "This method cannot be called while holding a synchronized lock on this";

        // move to the stopping state
        State originalState;
        synchronized (this) {
            originalState = getStateInstance();
            if (originalState == State.STOPPED || originalState == State.FAILED) {
                return;
            }

            // only try to change states if we are not already stopping
            if (originalState != State.STOPPING) {
                setStateInstance(State.STOPPING);
            }
        }

        // only fire a notification if we are not already stopping
        if (originalState != State.STOPPING) {
            lifecycleBroadcaster.fireStoppingEvent();
        }

        // Don't try to stop dependents from within a synchronized block... this should reduce deadlocks

        // stop all of my dependent objects
        Set dependents = dependencyManager.getChildren(abstractName);
        for (Iterator iterator = dependents.iterator(); iterator.hasNext();) {
            AbstractName child = (AbstractName) iterator.next();
            try {
                log.trace("Checking if child is running: child={}", child);
                if (kernel.getGBeanState(child) == State.RUNNING_INDEX) {
                    log.trace("Stopping child: child={}", child);
                    kernel.stopGBean(child);
                    log.trace("Stopped child: child={}", child);
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
        }

        try {
            if (gbeanInstance.destroyInstance(false)) {
                // instance is not ready to destroyed... this is because another thread has
                // already killed the gbean.
                return;
            }
        } catch (Throwable e) {
            gbeanInstance.setStateReason(e.getMessage());
            log.warn("Problem in doFail", e);
        }
        setStateInstance(State.FAILED);
        lifecycleBroadcaster.fireFailedEvent();
    }

    /**
     * Attempts to bring the component into {@link org.apache.geronimo.kernel.management.State#RUNNING} state. If an Exception occurs while
     * starting the component, the component will be failed.
     * <p/>
     * <p/>
     * Note: Do not call this from within a synchronized block as it makes may send a JMX notification
     */
    void attemptFullStart() {
        assert !Thread.holdsLock(this): "This method cannot be called while holding a synchronized lock on this";

        synchronized (this) {
            // if we are still trying to start and can start now... start
            if (getStateInstance() != State.STARTING) {
                return;
            }

            // check if all of the gbeans we depend on are running
            Set parents = dependencyManager.getParents(abstractName);
            for (Iterator i = parents.iterator(); i.hasNext();) {
                AbstractName parent = (AbstractName) i.next();
                if (!kernel.isLoaded(parent)) {
                    log.trace("Cannot run because parent is not registered: parent={}", parent);
                    return;
                }
                try {
                    log.trace("Checking if parent is running: parent={}", parent);
                    if (kernel.getGBeanState(parent) != State.RUNNING_INDEX) {
                        log.trace("Cannot run because parent is not running: parent={}", parent);
                        return;
                    }
                    log.trace("Parent is running: parent={}", parent);
                } catch (GBeanNotFoundException e) {
                    // depended on instance was removed bewteen the register check and the invoke
                    log.trace("Cannot run because parent is not registered: parent={}", parent);
                    return;
                } catch (Exception e) {
                    // problem getting the attribute, parent has most likely failed
                    log.trace("Cannot run because an error occurred while checking if parent is running: parent={}", parent);
                    return;
                }
            }
        }

        try {
            // try to create the instance
            if (!gbeanInstance.createInstance()) {
                // instance is not ready to start... this is normally caused by references
                // not being available, but could be because someone already started the gbean.
                // in another thread.  The reference will log a debug message about why
                // it could not startf
                return;
            }
        } catch (Throwable t) {
            // oops there was a problem and the gbean failed
            log.error("Error while starting; GBean is now in the FAILED state: abstractName=\"" + abstractName + "\"", t);
            setStateInstance(State.FAILED);
            lifecycleBroadcaster.fireFailedEvent();

            if (t instanceof Exception) {
                // ignore - we only rethrow errors
                StringWriter sw = new StringWriter();
                PrintWriter pw = new PrintWriter(sw);
                pw.append(t.getMessage()).append("\n");
                t.printStackTrace(pw);
                gbeanInstance.setStateReason(sw.toString());
                return;
            } else if (t instanceof Error) {
                throw (Error) t;
            } else {
                throw new Error(t);
            }
        }

        // started successfully... notify everyone else
        setStateInstance(State.RUNNING);
        lifecycleBroadcaster.fireRunningEvent();
    }

    /**
     * Attempt to bring the component into the fully stopped state.
     * If an exception occurs while stopping the component, the component will be failed.
     * <p/>
     * <p/>
     * Note: Do not call this from within a synchronized block as it may send a JMX notification
     */
    void attemptFullStop() {
        assert !Thread.holdsLock(this): "This method cannot be called while holding a synchronized lock on this";

        // check if we are able to stop
        synchronized (this) {
            // if we are still trying to stop...
            if (getStateInstance() != State.STOPPING) {
                return;
            }

            // check if all of the mbeans depending on us are stopped
            Set children = dependencyManager.getChildren(abstractName);
            for (Iterator i = children.iterator(); i.hasNext();) {
                AbstractName child = (AbstractName) i.next();
                if (kernel.isLoaded(child)) {
                    try {
                        log.trace("Checking if child is stopped: child={}", child);
                        int state = kernel.getGBeanState(child);
                        if (state == State.RUNNING_INDEX) {
                            log.trace("Cannot stop because child is still running: child={}", child);
                            return;
                        }
                    } catch (GBeanNotFoundException e) {
                        // depended on instance was removed between the register check and the invoke
                    } catch (Exception e) {
                        // problem getting the attribute, depended on bean has most likely failed
                        log.trace("Cannot run because an error occurred while checking if child is stopped: child={}", child);
                        return;
                    }
                }
            }
        }

        // all is clear to stop... try to stop
        try {
            if (!gbeanInstance.destroyInstance(true)) {
                // instance is not ready to stop... this is because another thread has
                // already stopped the gbean.
                return;
            }
        } catch (Throwable t) {
            log.error("Error while stopping; GBean is now in the FAILED state: abstractName=\"" + abstractName + "\"", t);
            setStateInstance(State.FAILED);
            lifecycleBroadcaster.fireFailedEvent();

            if (t instanceof Exception) {
                // ignore - we only rethrow errors
                gbeanInstance.setStateReason(t.getMessage());
                return;
            } else if (t instanceof Error) {
                throw (Error) t;
            } else {
                throw new Error(t);
            }
        }

        // we successfully stopped, notify everyone else
        setStateInstance(State.STOPPED);
        lifecycleBroadcaster.fireStoppedEvent();
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

        log.debug("{} State changed from {} to {}", new Object[] { toString(), state, newState });

        state = newState;
    }

    public String toString() {
        return "GBeanInstanceState for: " + abstractName;
    }

}
