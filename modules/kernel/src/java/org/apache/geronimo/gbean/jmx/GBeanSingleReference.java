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

import java.util.Set;
import javax.management.ObjectName;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.geronimo.gbean.GReferenceInfo;
import org.apache.geronimo.gbean.InvalidConfigurationException;
import org.apache.geronimo.gbean.WaitingException;
import org.apache.geronimo.kernel.Kernel;
import org.apache.geronimo.kernel.management.State;

/**
 * @version $Rev: 71492 $ $Date: 2004-11-14 21:31:50 -0800 (Sun, 14 Nov 2004) $
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

    public GBeanSingleReference(GBeanMBean gmbean, GReferenceInfo referenceInfo, Class constructorType) throws InvalidConfigurationException {
        super(gmbean, referenceInfo, constructorType);
    }

    public synchronized void start() throws Exception {
        // if there are no patterns then there is nothing to start
        if (getPatterns().isEmpty()) {
            return;
        }

        // if we already have a proxy then we have already been started
        if (getProxy() != null) {
            return;
        }

        //
        // We must have exactally one running target
        //
        Set targets = getTargets();
        if (targets.size() == 0) {
            waitingForMe = true;
            throw new WaitingException("No targets are running for " + getName() + " reference");
        } else if (targets.size() > 1) {
            waitingForMe = true;
            throw new WaitingException("More then one targets are running for " + getName() + " reference");
        }
        waitingForMe = false;

        // stop all gbeans that would match our patterns from starting
        Kernel kernel = getKernel();
        ObjectName objectName = getGBeanMBean().getObjectNameObject();
        kernel.getDependencyManager().addStartHolds(objectName, getPatterns());

        // add a dependency on our target and create the proxy
        ObjectName target = (ObjectName) targets.iterator().next();
        setProxy(kernel.getProxyManager().createProxy(target, getType()));
        proxyTarget = target;
        kernel.getDependencyManager().addDependency(objectName, target);
    }

    public synchronized void stop() {
        waitingForMe = false;
        Kernel kernel = getKernel();
        ObjectName objectName = getGBeanMBean().getObjectNameObject();
        Set patterns = getPatterns();
        if (!patterns.isEmpty()) {
            kernel.getDependencyManager().removeStartHolds(objectName, patterns);
        }

        Object proxy = getProxy();
        if (proxy != null) {
            kernel.getDependencyManager().removeDependency(objectName, proxyTarget);
            kernel.getProxyManager().destroyProxy(proxy);
            setProxy(null);
            proxyTarget = null;
        }
    }

    public synchronized void targetAdded(ObjectName target) {
        // if we are running, and we now have two valid targets, which is an illegal state so we need to fail
        GBeanMBean gbeanMBean = getGBeanMBean();
        if (gbeanMBean.getStateInstance() == State.RUNNING) {
            gbeanMBean.fail();
        } else if (waitingForMe) {
            Set targets = getTargets();
            if (targets.size() == 1) {
                // the gbean was waiting for me and not there is now just one target
                attemptFullStart();
            }
        }
    }

    public synchronized void targetRemoved(ObjectName target) {
        GBeanMBean gbeanMBean = getGBeanMBean();
        if (gbeanMBean.getStateInstance() == State.RUNNING) {
            // we no longer have a valid target, which is an illegal state so we need to fail
            gbeanMBean.fail();
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
            getGBeanMBean().attemptFullStart();
        } catch (Exception e) {
            log.warn("Exception occured while attempting to fully start: objectName=" + getGBeanMBean().getObjectName(), e);
        }
    }

}
