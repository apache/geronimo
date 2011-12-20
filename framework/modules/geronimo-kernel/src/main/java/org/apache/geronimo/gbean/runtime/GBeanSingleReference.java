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
import org.apache.geronimo.gbean.GReferenceInfo;
import org.apache.geronimo.gbean.InvalidConfigurationException;
import org.apache.geronimo.gbean.ReferencePatterns;
import org.apache.geronimo.kernel.Kernel;
import org.apache.geronimo.kernel.GBeanNotFoundException;

/**
 * @version $Rev$ $Date$
 */
public class GBeanSingleReference extends AbstractGBeanReference
{
    private static final Logger log = LoggerFactory.getLogger(GBeanSingleReference.class);

    /**
     * The object to which the proxy is bound
     */
    private final AbstractName targetName;

    public GBeanSingleReference(GBeanInstance gbeanInstance, GReferenceInfo referenceInfo, Kernel kernel, ReferencePatterns referencePatterns) throws InvalidConfigurationException {
        super(gbeanInstance, referenceInfo, kernel, referencePatterns != null && referencePatterns.getAbstractName() != null);
        targetName = referencePatterns != null? referencePatterns.getAbstractName(): null;
    }

    public AbstractName getTargetName() {
        return targetName;
    }

    public final synchronized void online() {
    }

    public final synchronized void offline() {
        stop();
    }


    public synchronized boolean start() {
        // We only need to start if there are patterns and we don't already have a proxy
        if (targetName == null) {
            return true;
        }

        // assure the gbean is running
        AbstractName abstractName = getGBeanInstance().getAbstractName();
        if (!isRunning(getKernel(), targetName)) {
            log.debug("Waiting to start " + abstractName + " because no targets are running for reference " + getName() +" matching the patterns " + targetName);
            return false;
        }

        if (getProxy() != null) {
            return true;
        }

        if (NO_PROXY) {
            try {
                setProxy(getKernel().getGBean(targetName));
            } catch (GBeanNotFoundException e) {
                // gbean disappeard on us
                log.debug("Waiting to start " + abstractName + " because no targets are running for reference " + getName() +" matching the patterns " + targetName);
                return false;
            }
        } else {
            setProxy(getKernel().getProxyManager().createProxy(targetName, getReferenceType()));
        }
        log.debug("Started {}", abstractName);
        return true;
    }

    public synchronized void stop() {
        Object proxy = getProxy();
        if (proxy != null) {
            if (getKernel().getProxyManager() != null) {
                getKernel().getProxyManager().destroyProxy(proxy);
            }
            setProxy(null);
        }
    }


}
