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
import java.util.Set;
import javax.management.InstanceAlreadyExistsException;
import javax.management.InstanceNotFoundException;
import javax.management.JMException;
import javax.management.JMRuntimeException;
import javax.management.MBeanInfo;
import javax.management.MBeanServer;
import javax.management.MBeanServerFactory;
import javax.management.ObjectName;

import org.apache.geronimo.gbean.runtime.GBeanInstance;
import org.apache.geronimo.gbean.runtime.LifecycleBroadcaster;
import org.apache.geronimo.kernel.GBeanAlreadyExistsException;
import org.apache.geronimo.kernel.GBeanNotFoundException;
import org.apache.geronimo.kernel.registry.GBeanRegistry;
import org.apache.geronimo.kernel.InternalKernelException;
import org.apache.geronimo.kernel.Kernel;
import org.apache.geronimo.kernel.lifecycle.LifecycleListener;

/**
 * @version $Rev$ $Date$
 */
public class JMXGBeanRegistry implements GBeanRegistry {
    private final HashMap registry = new HashMap();
    private Kernel kernel;
    private MBeanServer mbServer;

    public void start(Kernel kernel) {
        this.kernel = kernel;
        mbServer = MBeanServerFactory.createMBeanServer(kernel.getKernelName());
    }

    public void stop() {
        MBeanServerFactory.releaseMBeanServer(mbServer);

        // todo destroy instances
        synchronized(this) {
            registry.clear();
        }
    }

    public MBeanServer getMBeanServer() {
        return mbServer;
    }

    public synchronized boolean isRegistered(ObjectName name) {
        return registry.containsKey(name);
    }

    public void register(GBeanInstance gbeanInstance) throws GBeanAlreadyExistsException, InternalKernelException {
        ObjectName name = gbeanInstance.getObjectNameObject();
        MBeanInfo mbeanInfo = JMXUtil.toMBeanInfo(gbeanInstance.getGBeanInfo());
        GBeanMBean gbeanMBean = new GBeanMBean(kernel, name, mbeanInfo);
        try {
            mbServer.registerMBean(gbeanMBean, name);
        } catch (InstanceAlreadyExistsException e) {
            throw new GBeanAlreadyExistsException("A GBean is alreayd registered witht then name " + name);
        } catch (Exception e) {
            throw new InternalKernelException("Error loading GBean " + name.getCanonicalName(), unwrapJMException(e));
        }

        synchronized (this) {
            registry.put(name, gbeanInstance);
        }

        kernel.getLifecycleMonitor().addLifecycleListener(new LifecycleBridge(gbeanMBean), name);

        // fire the loaded event from the gbeanMBean.. it was already fired from
        // the GBeanInstance when it was created
        gbeanMBean.fireLoadedEvent();        
    }

    public void unregister(ObjectName name) throws GBeanNotFoundException, InternalKernelException {
        try {
             mbServer.unregisterMBean(name);
        } catch (InstanceNotFoundException e) {
            throw new GBeanNotFoundException(name.getCanonicalName());
        } catch (Exception e) {
            throw new InternalKernelException("Error unloading GBean " + name, unwrapJMException(e));
        }

        synchronized (this) {
            registry.remove(name);
        }
    }

    public synchronized GBeanInstance getGBeanInstance(ObjectName name) throws GBeanNotFoundException {
        GBeanInstance gbeanInstance = (GBeanInstance) registry.get(name);
        if (gbeanInstance == null) {
            throw new GBeanNotFoundException(name.getCanonicalName());
        }
        return gbeanInstance;
    }

    public Set listGBeans(ObjectName pattern) throws InternalKernelException {
        try {
            return mbServer.queryNames(pattern, null);
        } catch (RuntimeException e) {
            throw new InternalKernelException("Error while applying pattern " + pattern, e);
        }
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
