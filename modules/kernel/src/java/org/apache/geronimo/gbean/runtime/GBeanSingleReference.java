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

package org.apache.geronimo.gbean.runtime;

import java.util.Set;
import java.util.Iterator;
import javax.management.ObjectName;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.geronimo.gbean.GReferenceInfo;
import org.apache.geronimo.gbean.InvalidConfigurationException;
import org.apache.geronimo.kernel.DependencyManager;
import org.apache.geronimo.kernel.Kernel;
import org.apache.geronimo.kernel.lifecycle.LifecycleAdapter;
import org.apache.geronimo.kernel.lifecycle.LifecycleListener;
import org.apache.geronimo.kernel.management.State;

/**
 * @version $Rev$ $Date$
 */
public class GBeanSingleReference extends AbstractGBeanReference {
    private static final Log log = LogFactory.getLog(GBeanSingleReference.class);

    /**
     * Is the GBeanMBean waitng for me to start?
     */
    private boolean waitingForMe = false;

    /**
     * The object to which the proxy is bound
     */
    private ObjectName proxyTarget;

    public GBeanSingleReference(GBeanInstance gbeanInstance, GReferenceInfo referenceInfo, Kernel kernel, DependencyManager dependencyManager) throws InvalidConfigurationException {
        super(gbeanInstance, referenceInfo, kernel, dependencyManager);
    }

    public synchronized boolean start() {
        // We only need to start if there are patterns and we don't already have a proxy
        if (!getPatterns().isEmpty() && getProxy() == null) {
            //
            // We must have exactly one running target
            //
            ObjectName objectName = getGBeanInstance().getObjectNameObject();
            Set targets = getTargets();
            if (targets.size() == 0) {
                waitingForMe = true;
                log.debug("Waiting to start " + objectName + " because no targets are running for reference " + getName() +" matching the patterns " + getPatternsText());
                return false;
            } else if (targets.size() > 1) {
                waitingForMe = true;
                log.debug("Waiting to start " + objectName + " because more then one targets are running for the single valued reference " + getName() +" matching the patterns " + getPatternsText());
                return false;
            }
            waitingForMe = false;

            // stop all gbeans that would match our patterns from starting
            DependencyManager dependencyManager = getDependencyManager();
            dependencyManager.addStartHolds(objectName, getPatterns());

            // add a dependency on our target and create the proxy
            ObjectName target = (ObjectName) targets.iterator().next();
            setProxy(getKernel().getProxyManager().createProxy(target, getReferenceType()));
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

        Object proxy = getProxy();
        if (proxy != null) {
            dependencyManager.removeDependency(objectName, proxyTarget);
            getKernel().getProxyManager().destroyProxy(proxy);
            setProxy(null);
            proxyTarget = null;
        }
    }

    protected synchronized void targetAdded(ObjectName target) {
        // if we are running, and we now have two valid targets, which is an illegal state so we need to fail
        GBeanInstance gbeanInstance = getGBeanInstance();
        if (gbeanInstance.getStateInstance() == State.RUNNING) {
            log.error("Illegal state: two or more targets are not running for a single valued reference: " + getDescription() +
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
            log.error("Illegal state: current target for a single valued reference stopped: " + getDescription() +
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
}
