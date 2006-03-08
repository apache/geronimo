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
package org.apache.geronimo.system.jmx;

import java.util.ArrayList;
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
import javax.management.MBeanServerFactory;
import javax.management.ObjectName;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.geronimo.gbean.GBeanLifecycle;
import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.gbean.GBeanInfoBuilder;
import org.apache.geronimo.gbean.AbstractName;
import org.apache.geronimo.gbean.AbstractNameQuery;
import org.apache.geronimo.kernel.GBeanNotFoundException;
import org.apache.geronimo.kernel.Kernel;
import org.apache.geronimo.kernel.jmx.JMXUtil;
import org.apache.geronimo.kernel.lifecycle.LifecycleAdapter;

/**
 * @version $Rev$ $Date$
 */
public class MBeanServerKernelBridge implements GBeanLifecycle {
    private static final Log log = LogFactory.getLog(MBeanServerKernelBridge.class);
    private final HashMap registry = new HashMap();
    private final Kernel kernel;
    private final MBeanServer mbeanServer;
    private static final AbstractNameQuery ALL = new AbstractNameQuery(null, Collections.EMPTY_MAP, Collections.EMPTY_SET);

    public MBeanServerKernelBridge(Kernel kernel, String mbeanServerId) throws MBeanServerNotFound {
        this.kernel = kernel;
        ArrayList servers = MBeanServerFactory.findMBeanServer(mbeanServerId);
        if (servers.size() == 0) {
            mbeanServer = MBeanServerFactory.createMBeanServer("geronimo");
        } else if (servers.size() > 1) {
            throw new MBeanServerNotFound(servers.size() + " MBeanServers were found with the agent id " + mbeanServerId);
        } else {
            mbeanServer = (MBeanServer) servers.get(0);
        }

    }

    public void doStart() {
        kernel.getLifecycleMonitor().addLifecycleListener(new GBeanRegistrationListener(), ALL);

        HashMap beans = new HashMap();
        synchronized (this) {
            Set allNames = kernel.listGBeans(ALL);
            for (Iterator iterator = allNames.iterator(); iterator.hasNext();) {
                AbstractName abstractName = (AbstractName) iterator.next();
                if (registry.containsKey(abstractName.getObjectName())) {
                    // instance already registered
                    continue;
                }
                MBeanInfo mbeanInfo;
                try {
                    mbeanInfo = JMXUtil.toMBeanInfo(kernel.getGBeanInfo(abstractName));
                } catch (GBeanNotFoundException e) {
                    // ignore - gbean already unregistered
                    continue;
                }
                MBeanGBeanBridge mbeanGBeanBridge = new MBeanGBeanBridge(kernel, abstractName, mbeanInfo);
                registry.put(abstractName.getObjectName(), mbeanGBeanBridge);
                beans.put(abstractName.getObjectName(), mbeanGBeanBridge);
            }
        }
        for (Iterator iterator = beans.entrySet().iterator(); iterator.hasNext();) {
            Map.Entry entry = (Map.Entry) iterator.next();
            ObjectName objectName = (ObjectName) entry.getKey();
            MBeanGBeanBridge bridge = (MBeanGBeanBridge) entry.getValue();
            try {
                mbeanServer.registerMBean(bridge, objectName);
            } catch (InstanceAlreadyExistsException e) {
                // ignore - gbean already has an mbean shadow object
            } catch (Exception e) {
                log.warn("Unable to register MBean shadow object for GBean", unwrapJMException(e));
            }
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
        for (Iterator i = beans.keySet().iterator(); i.hasNext();) {
            ObjectName objectName = (ObjectName) i.next();
            try {
                mbeanServer.unregisterMBean(objectName);
            } catch (Exception e) {
                // ignore
            }
        }
    }

    private void register(AbstractName abstractName) {
        try {
            MBeanGBeanBridge mbeanGBeanBridge;
            synchronized (this) {
                if (registry.containsKey(abstractName.getObjectName())) {
                    return;
                }
                MBeanInfo mbeanInfo = JMXUtil.toMBeanInfo(kernel.getGBeanInfo(abstractName));
                mbeanGBeanBridge = new MBeanGBeanBridge(kernel, abstractName, mbeanInfo);
                registry.put(abstractName.getObjectName(), mbeanGBeanBridge);
            }
            mbeanServer.registerMBean(mbeanGBeanBridge, abstractName.getObjectName());
        } catch (GBeanNotFoundException e) {
            // ignore - gbean already unregistered
        } catch (InstanceAlreadyExistsException e) {
            // ignore - gbean already has an mbean shadow object
        } catch (Exception e) {
            log.warn("Unable to register MBean shadow object for GBean", unwrapJMException(e));
        }
    }

    private void unregister(AbstractName abstractName) {
        synchronized (this) {
            if (registry.remove(abstractName.getObjectName()) == null) {
                return;
            }
        }

        try {
            mbeanServer.unregisterMBean(abstractName.getObjectName());
        } catch (InstanceNotFoundException e) {
            // ignore - something else may have unregistered us
            // if there truely is no GBean then we will catch it below whwn we call the superclass
        } catch (Exception e) {
            log.warn("Unable to unregister MBean shadow object for GBean", unwrapJMException(e));
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
        infoFactory.addAttribute("mbeanServerId", String.class, true);
        infoFactory.setConstructor(new String[]{"kernel", "mbeanServerId"});
        GBEAN_INFO = infoFactory.getBeanInfo();
    }

    public static GBeanInfo getGBeanInfo() {
        return GBEAN_INFO;
    }
}
