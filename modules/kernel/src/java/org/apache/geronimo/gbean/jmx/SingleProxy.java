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
import java.util.Set;
import javax.management.ObjectName;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.geronimo.gbean.WaitingException;
import org.apache.geronimo.kernel.management.State;

/**
 *
 *
 * @version $Revision: 1.10 $ $Date: 2004/03/10 09:59:01 $
 */
public class SingleProxy implements Proxy {
    private static final Log log = LogFactory.getLog(SingleProxy.class);

    /**
     * The GBeanMBean to which this proxy belongs.
     */
    private GBeanMBean gmbean;

    /**
     * Name of this proxy.
     */
    private String name;

    /**
     * The ObjectName patterns to which this proxy could be connected.
     * This is used to block mbeans from starting that would match a
     * pattern while we are running.
     */
    private Set patterns;

    /**
     * A set of all targets matching the
     */
    private Set targets = new HashSet();

    /**
     * Proxy implementation held by the component
     */
    private Object proxy;

    /**
     * Is the GBeanMBean waitng for me to start?
     */
    private boolean waitingForMe = false;

    /**
     * The interceptor for the proxy instance
     */
    private ProxyMethodInterceptor methodInterceptor;

    public SingleProxy(GBeanMBean gmbean, String name, Class type, Set patterns) throws Exception {
        this.gmbean = gmbean;
        this.name = name;
        this.patterns = patterns;
        ProxyFactory factory = new ProxyFactory(type);
        methodInterceptor = new ProxyMethodInterceptor(factory.getType());
        proxy = factory.create(methodInterceptor);
    }

    public synchronized void destroy() {
        methodInterceptor.disconnect();

        gmbean = null;
        name = null;
        patterns = null;
        targets = null;
        proxy = null;
        waitingForMe = false;
        methodInterceptor = null;
    }

    public synchronized Object getProxy() {
        return proxy;
    }

    public synchronized Set getTargets() {
        return targets;
    }

    public synchronized void addTarget(ObjectName target) {
        // if this is a new target...
        if (!targets.contains(target)) {
            if (targets.size() == 1) {
                // will be more then one target... remove the dependency
                ObjectName currentTarget = (ObjectName) targets.iterator().next();
                gmbean.getDependencyService().removeDependency(gmbean.getObjectNameObject(), currentTarget);
            }

            targets.add(target);

            // if we are running, we now have two valid targets, which is an illegal state so we need to fail
            if (gmbean.getStateInstance() == State.RUNNING) {
                gmbean.fail();
            } else if (targets.size() == 1) {
                // there is now just one target... add a dependency
                gmbean.getDependencyService().addDependency(gmbean.getObjectNameObject(), target);
                if (waitingForMe) {
                    attemptFullStart();
                }
            }

        }
    }

    public synchronized void removeTarget(ObjectName target) {
        boolean wasTarget = targets.remove(target);
        if (wasTarget) {
            if (gmbean.getStateInstance() == State.RUNNING) {
                // we no longer have a valid target, which is an illegal state so we need to fail
                gmbean.fail();
            } else if (targets.size() == 1) {
                // we only have one target remaining... add a dependency
                ObjectName remainingTarget = (ObjectName) targets.iterator().next();
                gmbean.getDependencyService().addDependency(gmbean.getObjectNameObject(), remainingTarget);

                if (waitingForMe) {
                    attemptFullStart();
                }
            } else if (targets.isEmpty()) {
                // that was our last target... remove the dependency
                gmbean.getDependencyService().removeDependency(gmbean.getObjectNameObject(), target);
            }

        }
    }

    private synchronized void attemptFullStart() {
        try {
            // there could be an issue with really badly written components holding up a stop when the
            // component never reached the starting phase... then a target registers and we automatically
            // attempt to restart
            waitingForMe = false;
            gmbean.attemptFullStart();
        } catch (Exception e) {
            log.warn("Exception occured while attempting to fully start: objectName=" + gmbean.getObjectName(), e);
        }
    }

    public synchronized void start() throws WaitingException {
        //
        // We must have exactally one running target
        //
        if (targets.size() == 0) {
            waitingForMe = true;
            throw new WaitingException("No targets are running for " + name + " reference");
        } else if (targets.size() > 1) {
            waitingForMe = true;
            throw new WaitingException("More then one targets are running for " + name + " reference");
        }
        waitingForMe = false;
        gmbean.getDependencyService().addStartHolds(gmbean.getObjectNameObject(), patterns);
        methodInterceptor.connect(gmbean.getServer(), (ObjectName) targets.iterator().next());
    }

    public synchronized void stop() {
        waitingForMe = false;
        methodInterceptor.disconnect();
        gmbean.getDependencyService().removeStartHolds(gmbean.getObjectNameObject(), patterns);
    }
}
