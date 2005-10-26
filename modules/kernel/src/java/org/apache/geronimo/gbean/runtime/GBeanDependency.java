/**
 *
 * Copyright 2004 The Apache Software Foundation
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
package org.apache.geronimo.gbean.runtime;

import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import javax.management.ObjectName;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.geronimo.gbean.InvalidConfigurationException;
import org.apache.geronimo.kernel.DependencyManager;
import org.apache.geronimo.kernel.GBeanNotFoundException;
import org.apache.geronimo.kernel.Kernel;
import org.apache.geronimo.kernel.lifecycle.LifecycleAdapter;
import org.apache.geronimo.kernel.lifecycle.LifecycleListener;
import org.apache.geronimo.kernel.management.State;

/**
 * @version $Rev$ $Date$
 */
public class GBeanDependency {
    /**
     * The GBeanInstance to which this reference belongs.
     */
    private final GBeanInstance gbeanInstance;

    /**
     * The target objectName patterns to watch for a connection.
     */
    private Set patterns = Collections.EMPTY_SET;

    /**
     * Our listener for lifecycle events
     */
    private final LifecycleListener listener;

    /**
     * Current set of targets
     */
    private final Set targets = new HashSet();

    /**
     * The kernel to which the reference is bound.
     */
    private final Kernel kernel;

    /**
     * The dependency manager of the kernel.
     */
    private final DependencyManager dependencyManager;

    /**
     * is this reference online
     */
    private boolean isOnline = false;

    public GBeanDependency(GBeanInstance gbeanInstance, ObjectName pattern, Kernel kernel, DependencyManager dependencyManager) throws InvalidConfigurationException {
        this.gbeanInstance = gbeanInstance;
        this.kernel = kernel;
        this.dependencyManager = dependencyManager;
        patterns = Collections.singleton(pattern);
        listener = createLifecycleListener();
    }

    private static final Log log = LogFactory.getLog(GBeanSingleReference.class);

    /**
     * Is the GBeanMBean waitng for me to start?
     */
    private boolean waitingForMe = false;

    /**
     * The object to which the proxy is bound
     */
    private ObjectName proxyTarget;


    public synchronized boolean start() {
        // We only need to start if there are patterns and we don't already have a proxy
        if (proxyTarget == null) {
            //
            // We must have exactally one running target
            //
            ObjectName objectName = getGBeanInstance().getObjectNameObject();
            Set targets = getTargets();
            if (targets.size() == 0) {
                waitingForMe = true;
                log.debug("Waiting to start " + objectName + " because no targets are running for the dependency matching the patternspatterns " + getPatternsText());
                return false;
            } else if (targets.size() > 1) {
                waitingForMe = true;
                log.debug("Waiting to start " + objectName + " because more then one targets are running for the dependency matching the patternspatterns " + getPatternsText());
                return false;
            }
            waitingForMe = false;

            // stop all gbeans that would match our patterns from starting
            DependencyManager dependencyManager = getDependencyManager();
            dependencyManager.addStartHolds(objectName, getPatterns());

            // add a dependency on our target and create the proxy
            ObjectName target = (ObjectName) targets.iterator().next();
            proxyTarget = target;
            dependencyManager.addDependency(objectName, target);
        }

        return true;
    }

    private String getPatternsText() {
        StringBuffer buf = new StringBuffer();
        Set patterns = getPatterns();
        for (Iterator iterator = patterns.iterator(); iterator.hasNext();) {
            ObjectName objectName = (ObjectName) iterator.next();
            buf.append(objectName.getCanonicalName()).append(" ");
        }
        return buf.toString();
    }

    public synchronized void stop() {
        waitingForMe = false;
        ObjectName objectName = getGBeanInstance().getObjectNameObject();
        Set patterns = getPatterns();
        DependencyManager dependencyManager = getDependencyManager();
        if (!patterns.isEmpty()) {
            dependencyManager.removeStartHolds(objectName, patterns);
        }

        if (proxyTarget != null) {
            dependencyManager.removeDependency(objectName, proxyTarget);
            proxyTarget = null;
        }
    }

    protected synchronized void targetAdded(ObjectName target) {
        // if we are running, and we now have two valid targets, which is an illegal state so we need to fail
        GBeanInstance gbeanInstance = getGBeanInstance();
        if (gbeanInstance.getStateInstance() == State.RUNNING) {
            log.error("Illegal state: two or more targets are not running for a signle valued reference: " + getDescription() +
                    ", currentTarget=" + proxyTarget +
                    ", newTarget=" + target);
            gbeanInstance.referenceFailed();
        } else if (waitingForMe) {
            Set targets = getTargets();
            if (targets.size() == 1) {
                // the gbean was waiting for me and not there is now just one target
                attemptFullStart();
            }
        }
    }

    protected synchronized void targetRemoved(ObjectName target) {
        GBeanInstance gbeanInstance = getGBeanInstance();
        if (gbeanInstance.getStateInstance() == State.RUNNING) {
            // we no longer have a valid target, which is an illegal state so we need to fail
            log.error("Illegal state: current target for a signle valued reference stopped: " + getDescription() +
                    ", currentTarget=" + target);
            gbeanInstance.referenceFailed();
        } else if (waitingForMe) {
            Set targets = getTargets();
            if (targets.size() == 1) {
                // the gbean was waiting for me and not there is now just one target
                attemptFullStart();
            }
        }
    }

    private synchronized void attemptFullStart() {
        try {
            // there could be an issue with really badly written components holding up a stop when the
            // component never reached the starting phase... then a target registers and we automatically
            // attempt to restart
            waitingForMe = false;
            getGBeanInstance().start();
        } catch (Exception e) {
            log.warn("Exception occured while attempting to fully start: objectName=" + getGBeanInstance().getObjectName(), e);
        }
    }

    protected LifecycleListener createLifecycleListener() {
        return new LifecycleAdapter() {
                    public void running(ObjectName objectName) {
                        addTarget(objectName);
                    }

                    public void stopped(ObjectName objectName) {
                        removeTarget(objectName);
                    }

                    public void failed(ObjectName objectName) {
                        removeTarget(objectName);
                    }

                    public void unloaded(ObjectName objectName) {
                        removeTarget(objectName);
                    }
                };
    }

    protected final Kernel getKernel() {
        return kernel;
    }

    protected final DependencyManager getDependencyManager() {
        return dependencyManager;
    }

    public final GBeanInstance getGBeanInstance() {
        return gbeanInstance;
    }

    public final Set getPatterns() {
        return patterns;
    }

    public final void setPatterns(Set patterns) {
        if (isOnline) {
            throw new IllegalStateException("Pattern set can not be modified while online");
        }

        if (patterns == null || patterns.isEmpty() || (patterns.size() == 1 && patterns.iterator().next() == null)) {
            this.patterns = Collections.EMPTY_SET;
        } else {
            patterns = new HashSet(patterns);
            for (Iterator iterator = this.patterns.iterator(); iterator.hasNext();) {
                if (iterator.next() == null) {
                    iterator.remove();
                    //there can be at most one null value in a set.
                    break;
                }
            }
            this.patterns = Collections.unmodifiableSet(patterns);
        }
    }

    public final synchronized void online() {
        Set gbeans = kernel.listGBeans(patterns);
        for (Iterator objectNameIterator = gbeans.iterator(); objectNameIterator.hasNext();) {
            ObjectName target = (ObjectName) objectNameIterator.next();
            if (!targets.contains(target)) {

                // if the bean is running add it to the runningTargets list
                if (isRunning(kernel, target)) {
                    targets.add(target);
                }
            }
        }

        kernel.getLifecycleMonitor().addLifecycleListener(listener, patterns);
        isOnline = true;
    }

    public final synchronized void offline() {
        // make sure we are stoped
        stop();

        kernel.getLifecycleMonitor().removeLifecycleListener(listener);

        targets.clear();
        isOnline = false;
    }

    protected final Set getTargets() {
        return targets;
    }

    protected final void addTarget(ObjectName objectName) {
        if (!targets.contains(objectName)) {
            targets.add(objectName);
            targetAdded(objectName);
        }
    }

    protected final void removeTarget(ObjectName objectName) {
        boolean wasTarget = targets.remove(objectName);
        if (wasTarget) {
            targetRemoved(objectName);
        }
    }


    /**
     * Is the component in the Running state
     *
     * @param objectName name of the component to check
     * @return true if the component is running; false otherwise
     */
    private boolean isRunning(Kernel kernel, ObjectName objectName) {
        try {
            final int state = kernel.getGBeanState(objectName);
            return state == State.RUNNING_INDEX;
        } catch (GBeanNotFoundException e) {
            // mbean is no longer registerd
            return false;
        } catch (Exception e) {
            // problem getting the attribute, mbean has most likely failed
            return false;
        }
    }

    protected final String getDescription() {
        return "\n    GBeanInstance: " + gbeanInstance.getName() +
                "\n    Pattern Name: " + getPatterns();
    }
}
