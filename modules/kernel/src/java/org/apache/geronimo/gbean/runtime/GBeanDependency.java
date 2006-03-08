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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.geronimo.gbean.AbstractName;
import org.apache.geronimo.gbean.AbstractNameQuery;
import org.apache.geronimo.gbean.InvalidConfigurationException;
import org.apache.geronimo.kernel.GBeanNotFoundException;
import org.apache.geronimo.kernel.Kernel;
import org.apache.geronimo.kernel.lifecycle.LifecycleAdapter;
import org.apache.geronimo.kernel.lifecycle.LifecycleListener;
import org.apache.geronimo.kernel.management.State;

/**
 * @version $Rev$ $Date$
 */
public final class GBeanDependency {


    private static final Log log = LogFactory.getLog(GBeanSingleReference.class);

    /**
     * The GBeanInstance to which this reference belongs.
     */
    private final GBeanInstance gbeanInstance;

    /**
     * The target objectName targetName to watch for a connection.
     */
    private final AbstractName targetName;

    /**
     * Our listener for lifecycle events
     */
    private final LifecycleListener listener;

    /**
     * The kernel to which the reference is bound.
     */
    private final Kernel kernel;

    /**
     * Is the GBean waitng for me to start?
     */
//    private boolean waitingForMe = false;
    private boolean targetRunning = false;
    private boolean dependencyRegistered = false;

    public GBeanDependency(GBeanInstance gbeanInstance, AbstractName targetName, Kernel kernel) throws InvalidConfigurationException {
        this.gbeanInstance = gbeanInstance;
        this.kernel = kernel;
        this.targetName = targetName;
        listener = createLifecycleListener();
    }

    public final synchronized void online() {
        AbstractNameQuery query = new AbstractNameQuery(targetName);
        kernel.getLifecycleMonitor().addLifecycleListener(listener, query);
        targetRunning = isRunning(kernel, targetName);
    }

    public synchronized boolean start() {
        if (targetRunning && !dependencyRegistered) {
            AbstractName abstractName = gbeanInstance.getAbstractName();
            kernel.getDependencyManager().addDependency(abstractName, targetName);
            dependencyRegistered = true;
        }
        // We only need to start if there are targetName and we don't already have a proxy
//        if (waitingForMe) {
//            if (isRunning(kernel, targetName)) {
//                AbstractName abstractName = gbeanInstance.getAbstractName();
//                kernel.getDependencyManager().addDependency(abstractName, targetName);
//                waitingForMe = false;
//            }
//        }

        return targetRunning;
    }


    public synchronized void stop() {
        if (dependencyRegistered) {
            AbstractName abstractName = gbeanInstance.getAbstractName();
            kernel.getDependencyManager().removeDependency(abstractName, targetName);
            dependencyRegistered = false;
        }
//        waitingForMe = true;
//        AbstractName abstractName = gbeanInstance.getAbstractName();
//
//            kernel.getDependencyManager().removeDependency(abstractName, targetName);
    }

    public final synchronized void offline() {
        // make sure we are stopped
        stop();

        kernel.getLifecycleMonitor().removeLifecycleListener(listener);
        targetRunning = false;
//        waitingForMe = true;
    }

    private synchronized void attemptFullStart() {
        try {
            // there could be an issue with really badly written components holding up a stop when the
            // component never reached the starting phase... then a target registers and we automatically
            // attempt to restart
//            waitingForMe = false;
            gbeanInstance.start();
        } catch (Exception e) {
            log.warn("Exception occured while attempting to fully start: objectName=" + gbeanInstance.getObjectName(), e);
        }
    }

    protected LifecycleListener createLifecycleListener() {
        return new LifecycleAdapter() {
            public void running(AbstractName abstractName) {
                addTarget(abstractName);
            }

            public void stopped(AbstractName abstractName) {
                removeTarget(abstractName);
            }

            public void failed(AbstractName abstractName) {
                removeTarget(abstractName);
            }

            public void unloaded(AbstractName abstractName) {
                removeTarget(abstractName);
            }
        };
    }

    protected final void addTarget(AbstractName abstractName) {
        // if we are running, and we now have two valid targets, which is an illegal state so we need to fail
        synchronized (this) {
            targetRunning = true;
            GBeanInstance gbeanInstance1 = gbeanInstance;
            if (gbeanInstance1.getStateInstance() == State.RUNNING) {
                log.error("Illegal state: two or more targets are running for a single valued reference: " + getDescription() +
                        ", currentTarget=" + targetName +
                        ", newTarget=" + abstractName);
//                gbeanInstance1.referenceFailed();
            } //else if (waitingForMe) {
                // the gbean was waiting for me and not there is now just one target
                attemptFullStart();
//            }
        }
    }

    protected final void removeTarget(AbstractName abstractName) {
        synchronized (this) {
            targetRunning = false;
            GBeanInstance gbeanInstance1 = gbeanInstance;
            if (gbeanInstance1.getStateInstance() == State.RUNNING) {
                // we no longer have a valid target, which is an illegal state so we need to fail
                log.error("Illegal state: current target for a single valued reference stopped: " + getDescription() +
                        ", currentTarget=" + abstractName);
                gbeanInstance1.referenceFailed();
            }
        }
    }


    /**
     * Is the component in the Running state
     *
     * @param objectName name of the component to check
     * @return true if the component is running; false otherwise
     */
    private boolean isRunning(Kernel kernel, AbstractName objectName) {
        try {
            final int state = kernel.getGBeanState(objectName);
            return state == State.RUNNING_INDEX;
        } catch (GBeanNotFoundException e) {
            // gbean is no longer registerd
            return false;
        } catch (Exception e) {
            // problem getting the attribute, gbean has most likely failed
            return false;
        }
    }

    protected final String getDescription() {
        return "\n    GBeanInstance: " + gbeanInstance.getName() +
                "\n    Pattern Name: " + targetName;
    }

    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        final GBeanDependency that = (GBeanDependency) o;

        if (gbeanInstance != null ? !gbeanInstance.equals(that.gbeanInstance) : that.gbeanInstance != null)
            return false;
        return !(targetName != null ? !targetName.equals(that.targetName) : that.targetName != null);

    }

    public int hashCode() {
        int result;
        result = (gbeanInstance != null ? gbeanInstance.hashCode() : 0);
        result = 29 * result + (targetName != null ? targetName.hashCode() : 0);
        return result;
    }
}
