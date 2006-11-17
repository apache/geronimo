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

import org.apache.geronimo.gbean.AbstractName;
import org.apache.geronimo.gbean.GReferenceInfo;
import org.apache.geronimo.gbean.InvalidConfigurationException;
import org.apache.geronimo.gbean.ReferencePatterns;
import org.apache.geronimo.gbean.AbstractNameQuery;
import org.apache.geronimo.kernel.Kernel;
import org.apache.geronimo.kernel.lifecycle.LifecycleAdapter;
import org.apache.geronimo.kernel.lifecycle.LifecycleListener;

import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

/**
 * @version $Rev:386515 $ $Date$
 */
public class GBeanCollectionReference extends AbstractGBeanReference {
    /**
     * is this reference online
     */
    private boolean isOnline = false;

    /**
     * The target objectName patterns to watch for a connection.
     */
    private Set patterns = Collections.EMPTY_SET;

    /**
     * Current set of targets
     */
    private final Set targets = new HashSet();

    /**
     * Our listener for lifecycle events
     */
    private final LifecycleListener listener;

    public GBeanCollectionReference(GBeanInstance gbeanInstance, GReferenceInfo referenceInfo, Kernel kernel, ReferencePatterns referencePatterns) throws InvalidConfigurationException {
        super(gbeanInstance, referenceInfo, kernel, hasTargets(referencePatterns));
        listener = createLifecycleListener();
        if (referencePatterns != null) {
            setReferencePatterns(referencePatterns);
        }
    }

    private static boolean hasTargets(ReferencePatterns referencePatterns) {
        if (referencePatterns == null) {
            return false;
        }
        if (referencePatterns.isResolved()) {
            return true;
        }
        return !referencePatterns.getPatterns().isEmpty();
    }

    public synchronized boolean start() {
        // We only need to start if there are patterns and we don't already have a proxy
        if (!patterns.isEmpty() && getProxy() == null) {
            // add a dependency on our target and create the proxy
            setProxy(new ProxyCollection(getName(), getReferenceType(), getTargets(), getKernel()));
        }
        return true;
    }

    public synchronized void stop() {
        ProxyCollection proxy = (ProxyCollection) getProxy();
        if (proxy != null) {
            proxy.destroy();
            setProxy(null);
        }
    }

    protected synchronized void targetAdded(AbstractName target) {
        ProxyCollection proxy = (ProxyCollection) getProxy();
        if (proxy != null) {
            proxy.addTarget(target);
        }
    }

    protected synchronized void targetRemoved(AbstractName target) {
        ProxyCollection proxy = (ProxyCollection) getProxy();
        if (proxy != null) {
            proxy.removeTarget(target);
        }
    }

    protected LifecycleListener createLifecycleListener() {
        return new LifecycleAdapter() {
            public void running(AbstractName abstractName) {
                addTarget(abstractName);
            }

            public void stopping(AbstractName abstractName) {
                removeTarget(abstractName);
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

    public final synchronized Set getPatterns() {
        return patterns;
    }

    public final synchronized void setReferencePatterns(ReferencePatterns referencePatterns) {
        if (isOnline) {
            throw new IllegalStateException("Pattern set can not be modified while online");
        }
        if (referencePatterns.isResolved()) {
            this.patterns = Collections.unmodifiableSet(Collections.singleton(new AbstractNameQuery(referencePatterns.getAbstractName())));
        } else {
            this.patterns = Collections.unmodifiableSet(referencePatterns.getPatterns());
        }
    }

    public final synchronized void online() {
        Set gbeans = getKernel().listGBeans(patterns);
        for (Iterator objectNameIterator = gbeans.iterator(); objectNameIterator.hasNext();) {
            AbstractName target = (AbstractName) objectNameIterator.next();
            if (!targets.contains(target)) {

                // if the bean is running add it to the runningTargets list
                if (isRunning(getKernel(), target)) {
                    targets.add(target);
                }
            }
        }

        getKernel().getLifecycleMonitor().addLifecycleListener(listener, patterns);
        isOnline = true;
    }

    public final synchronized void offline() {
        // make sure we are stoped
        stop();

        getKernel().getLifecycleMonitor().removeLifecycleListener(listener);

        targets.clear();
        isOnline = false;
    }

    protected final Set getTargets() {
        return targets;
    }

    protected final void addTarget(AbstractName abstractName) {
        if (!targets.contains(abstractName)) {
            targets.add(abstractName);
            targetAdded(abstractName);
        }
    }

    protected final void removeTarget(AbstractName abstractName) {
        boolean wasTarget = targets.remove(abstractName);
        if (wasTarget) {
            targetRemoved(abstractName);
        }
    }

}
