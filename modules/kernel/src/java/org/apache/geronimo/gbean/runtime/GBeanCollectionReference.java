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

import javax.management.ObjectName;

import org.apache.geronimo.gbean.GReferenceInfo;
import org.apache.geronimo.gbean.InvalidConfigurationException;
import org.apache.geronimo.kernel.Kernel;
import org.apache.geronimo.kernel.DependencyManager;
import org.apache.geronimo.kernel.lifecycle.LifecycleAdapter;
import org.apache.geronimo.kernel.lifecycle.LifecycleListener;

/**
 * @version $Rev: 71492 $ $Date: 2004-11-14 21:31:50 -0800 (Sun, 14 Nov 2004) $
 */
public class GBeanCollectionReference extends AbstractGBeanReference {
    public GBeanCollectionReference(GBeanInstance gbeanInstance, GReferenceInfo referenceInfo, Kernel kernel, DependencyManager dependencyManager) throws InvalidConfigurationException {
        super(gbeanInstance, referenceInfo, kernel, dependencyManager);
    }

    public synchronized boolean start() {
        // We only need to start if there are patterns and we don't already have a proxy
        if (!getPatterns().isEmpty() && getProxy() == null) {
            // add a dependency on our target and create the proxy
            setProxy(new ProxyCollection(getName(), getReferenceType(), getKernel().getProxyManager(), getTargets()));
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

    protected synchronized void targetAdded(ObjectName target) {
        ProxyCollection proxy = (ProxyCollection) getProxy();
        if (proxy != null) {
            proxy.addTarget(target);
        }
    }

    protected synchronized void targetRemoved(ObjectName target) {
        ProxyCollection proxy = (ProxyCollection) getProxy();
        if (proxy != null) {
            proxy.removeTarget(target);
        }
    }

    protected LifecycleListener createLifecycleListener() {
        return new LifecycleAdapter() {
                    public void running(ObjectName objectName) {
                        addTarget(objectName);
                    }

                    public void stopping(ObjectName objectName) {
                        removeTarget(objectName);
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
