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
package org.apache.geronimo.system.jmx;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.Collections;
import javax.management.InstanceAlreadyExistsException;
import javax.management.InstanceNotFoundException;
import javax.management.JMException;
import javax.management.JMRuntimeException;
import javax.management.MBeanInfo;
import javax.management.MBeanServer;
import javax.management.NotificationBroadcasterSupport;
import javax.management.ObjectName;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.geronimo.gbean.GBeanLifecycle;
import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.gbean.GBeanInfoBuilder;
import org.apache.geronimo.gbean.AbstractName;
import org.apache.geronimo.gbean.AbstractNameQuery;
import org.apache.geronimo.kernel.GBeanNotFoundException;
import org.apache.geronimo.kernel.Kernel;
import org.apache.geronimo.kernel.lifecycle.LifecycleAdapter;

/**
 * @version $Rev$ $Date$
 */
public class MBeanServerKernelBridge implements GBeanLifecycle {
    private static final Logger log = LoggerFactory.getLogger(MBeanServerKernelBridge.class);
    private static final AbstractNameQuery ALL = new AbstractNameQuery(null, Collections.EMPTY_MAP, Collections.EMPTY_SET);

    private final HashMap registry = new HashMap();
    private final Kernel kernel;
    private final MBeanServer mbeanServer;

    // todo remove this as soon as Geronimo supports factory beans    
    public MBeanServerKernelBridge(Kernel kernel, MBeanServerReference mbeanServerReference) {
        this(kernel, mbeanServerReference.getMBeanServer());
    }

    public MBeanServerKernelBridge(Kernel kernel, MBeanServer mbeanServer) {
        this.kernel = kernel;
        this.mbeanServer = mbeanServer;
    }

    public void doStart() {
        kernel.getLifecycleMonitor().addLifecycleListener(new GBeanRegistrationListener(), ALL);

        Set allNames = kernel.listGBeans(ALL);
        for (Iterator iterator = allNames.iterator(); iterator.hasNext();) {
            AbstractName abstractName = (AbstractName) iterator.next();
            register(abstractName);
        }
    }

    public void doFail() {
        doStop();
    }

    public void doStop() {
        // unregister all of our GBeans from the MBeanServer
        Map beans;
        synchronized (this) {
            beans = new HashMap(registry);
            registry.clear();
        }
        for (Iterator i = beans.values().iterator(); i.hasNext();) {
            MBeanGBeanBridge mbeanGBeanBridge = (MBeanGBeanBridge) i.next();
            ObjectName objectName = mbeanGBeanBridge.getObjectName();
            try {
                mbeanServer.unregisterMBean(objectName);
            } catch (Exception e) {
                // ignore
            }
        }
    }

    public NotificationBroadcasterSupport getNotificationBroadcasterSupport(AbstractName abstractName) {
        MBeanGBeanBridge bridge = (MBeanGBeanBridge)registry.get(abstractName);
        return (bridge == null) ? null : bridge.getNotificationBroadcasterSupport();
    }
    
    private void register(AbstractName abstractName) {
        try {
            MBeanGBeanBridge mbeanGBeanBridge;
            synchronized (this) {
                if (registry.containsKey(abstractName)) {
                    return;
                }
                MBeanInfo mbeanInfo = JMXUtil.toMBeanInfo(kernel.getGBeanInfo(abstractName));
                mbeanGBeanBridge = new MBeanGBeanBridge(kernel, abstractName, abstractName.getObjectName(), mbeanInfo);
                registry.put(abstractName, mbeanGBeanBridge);
            }
            mbeanServer.registerMBean(mbeanGBeanBridge, mbeanGBeanBridge.getObjectName());
        } catch (GBeanNotFoundException e) {
            // ignore - gbean already unregistered
        } catch (InstanceAlreadyExistsException e) {
            // ignore - gbean already has an mbean shadow object
        } catch (Exception e) {
            log.warn("Unable to register MBean shadow object for GBean", unwrapJMException(e));
        }
    }

    private void unregister(AbstractName abstractName) {
        MBeanGBeanBridge mbeanGBeanBridge;
        synchronized (this) {
            mbeanGBeanBridge = (MBeanGBeanBridge) registry.remove(abstractName);
        }

        if (mbeanGBeanBridge != null) {
            try {
                mbeanServer.unregisterMBean(mbeanGBeanBridge.getObjectName());
            } catch (InstanceNotFoundException e) {
                // ignore - something else may have unregistered us
                // if there truely is no GBean then we will catch it below whwn we call the superclass
            } catch (Exception e) {
                log.warn("Unable to unregister MBean shadow object for GBean", unwrapJMException(e));
            }
        }
    }

    private Throwable unwrapJMException(Throwable cause) {
        while ((cause instanceof JMException || cause instanceof JMRuntimeException) && cause.getCause() != null) {
            cause = cause.getCause();
        }
        return cause;
    }

    private class GBeanRegistrationListener extends LifecycleAdapter {
        public void loaded(AbstractName abstractName) {
            register(abstractName);
        }

        public void unloaded(AbstractName abstractName) {
            unregister(abstractName);
        }
    }

    public static final GBeanInfo GBEAN_INFO;

    static {
        GBeanInfoBuilder infoFactory = GBeanInfoBuilder.createStatic(MBeanServerKernelBridge.class);
        infoFactory.addAttribute("kernel", Kernel.class, false);
        infoFactory.addReference("MBeanServerReference", MBeanServerReference.class);
        infoFactory.setConstructor(new String[]{"kernel", "MBeanServerReference"});
        GBEAN_INFO = infoFactory.getBeanInfo();
    }

    public static GBeanInfo getGBeanInfo() {
        return GBEAN_INFO;
    }
}
