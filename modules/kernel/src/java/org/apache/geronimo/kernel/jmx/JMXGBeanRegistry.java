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
package org.apache.geronimo.kernel.jmx;

import java.util.HashMap;
import java.util.Iterator;
import javax.management.InstanceAlreadyExistsException;
import javax.management.InstanceNotFoundException;
import javax.management.JMException;
import javax.management.JMRuntimeException;
import javax.management.MBeanInfo;
import javax.management.MBeanServer;
import javax.management.ObjectName;

import org.apache.geronimo.gbean.GBeanName;
import org.apache.geronimo.gbean.runtime.GBeanInstance;
import org.apache.geronimo.gbean.runtime.LifecycleBroadcaster;
import org.apache.geronimo.kernel.GBeanAlreadyExistsException;
import org.apache.geronimo.kernel.GBeanNotFoundException;
import org.apache.geronimo.kernel.InternalKernelException;
import org.apache.geronimo.kernel.Kernel;
import org.apache.geronimo.kernel.lifecycle.LifecycleListener;
import org.apache.geronimo.kernel.registry.AbstractGBeanRegistry;

/**
 * An implementation of GBeanRegistry that also registers the GBeans with a JMX MBeanServer.
 *
 * @version $Rev$ $Date$
 */
public class JMXGBeanRegistry extends AbstractGBeanRegistry {
    private final HashMap registry = new HashMap();
    private Kernel kernel;
    private final MBeanServer mbServer;

    public JMXGBeanRegistry(MBeanServer mbServer) {
        this.mbServer = mbServer;
    }

    public void start(Kernel kernel) {
        super.start(kernel);
        this.kernel = kernel;
    }

    public synchronized void stop() {
        this.kernel = null;

        // unregister all our GBean from the MBeanServer
        for (Iterator i = registry.keySet().iterator(); i.hasNext();) {
            GBeanName name = (GBeanName) i.next();
            try {
                mbServer.unregisterMBean(name.getObjectName());
            } catch (Exception e) {
                // ignore
            }
        }
        super.stop();
    }

    public void register(GBeanInstance gbeanInstance) throws GBeanAlreadyExistsException {
        // create an MBean to wrap the plain GBean
        ObjectName name = gbeanInstance.getObjectNameObject();
        MBeanInfo mbeanInfo = JMXUtil.toMBeanInfo(gbeanInstance.getGBeanInfo());
        GBeanMBean gbeanMBean = new GBeanMBean(kernel, name, mbeanInfo);

        // register the MBean with the JMX MBeanServer
        try {
            mbServer.registerMBean(gbeanMBean, name);
        } catch (InstanceAlreadyExistsException e) {
            throw new GBeanAlreadyExistsException("An MBean is already registered under the name " + name);
        } catch (Exception e) {
            throw new InternalKernelException("Error loading GBean " + name.getCanonicalName(), unwrapJMException(e));
        }

        super.register(gbeanInstance);

        // todo when can we get rid if this?
        // fire the loaded event from the gbeanMBean.. it was already fired from the GBeanInstance when it was created
        kernel.getLifecycleMonitor().addLifecycleListener(new LifecycleBridge(gbeanMBean), name);
        gbeanMBean.fireLoadedEvent();
    }

    public void unregister(GBeanName name) throws GBeanNotFoundException, InternalKernelException {
        try {
            ObjectName objectName = name.getObjectName();
            mbServer.unregisterMBean(objectName);
        } catch (InstanceNotFoundException e) {
            // ignore - something else may have unregistered us
            // if there truely is no GBean then we will catch it below whwn we call the superclass
        } catch (Exception e) {
            throw new InternalKernelException("Error unloading GBean " + name, unwrapJMException(e));
        }

        super.unregister(name);
    }

    private Throwable unwrapJMException(Throwable cause) {
        while ((cause instanceof JMException || cause instanceof JMRuntimeException) && cause.getCause() != null) {
            cause = cause.getCause();
        }
        return cause;
    }

    private static class LifecycleBridge implements LifecycleListener {
        private final LifecycleBroadcaster broadcaster;

        public LifecycleBridge(LifecycleBroadcaster broadcaster) {
            this.broadcaster = broadcaster;
        }

        public void loaded(ObjectName objectName) {
            broadcaster.fireLoadedEvent();
        }

        public void starting(ObjectName objectName) {
            broadcaster.fireStartingEvent();
        }

        public void running(ObjectName objectName) {
            broadcaster.fireRunningEvent();
        }

        public void stopping(ObjectName objectName) {
            broadcaster.fireStoppingEvent();
        }

        public void stopped(ObjectName objectName) {
            broadcaster.fireStoppedEvent();
        }

        public void failed(ObjectName objectName) {
            broadcaster.fireFailedEvent();
        }

        public void unloaded(ObjectName objectName) {
            broadcaster.fireUnloadedEvent();
        }
    }
}
