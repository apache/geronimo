/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.apache.geronimo.openejb;

import java.util.Properties;

import org.apache.geronimo.gbean.AbstractName;
import org.apache.geronimo.gbean.GBeanData;
import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.gbean.GBeanInfoBuilder;
import org.apache.geronimo.gbean.GBeanLifecycle;
import org.apache.geronimo.kernel.GBeanNotFoundException;
import org.apache.geronimo.kernel.Kernel;
import org.apache.geronimo.kernel.management.State;
import org.apache.openejb.loader.SystemInstance;
import org.apache.openejb.server.ServerService;
import org.apache.openejb.server.ServiceManager;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTracker;
import org.osgi.util.tracker.ServiceTrackerCustomizer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @version $Rev$ $Date$
 */
public class EjbDaemonGBean implements GBeanLifecycle {
    
    private static final Logger log = LoggerFactory.getLogger(EjbDaemonGBean.class);
    
    private Kernel kernel;
    private AbstractName name;
    private BundleContext bundleContext;
    private ServiceTracker tracker;
    
    private String host;
    private int port;
    private int threads;
    private ServiceManager serviceManager;

    private String multicastHost;
    private String clusterName;
    private int multicastPort;
    private boolean enableMulticast;

    public EjbDaemonGBean(Kernel kernel, AbstractName name, final BundleContext bundleContext) {
        System.setProperty("openejb.nobanner","true");
        this.kernel = kernel;
        this.name = name;
        this.bundleContext = bundleContext;
        
        serviceManager = ServiceManager.getManager();
        
        tracker = new ServiceTracker(bundleContext, ServerService.class.getName(), new ServiceTrackerCustomizer() {

            public Object addingService(ServiceReference reference) {
                ServerService service = (ServerService) bundleContext.getService(reference);
                return addServerService(service);
            }

            public void modifiedService(ServiceReference reference, Object obj) {
            }

            public void removedService(ServiceReference reference, Object obj) {
                removeServerService( (AbstractName) obj);
            }
            
        });
    }

    public String getClusterName() {
        return clusterName;
    }

    public void setClusterName(String clusterName) {
        this.clusterName = clusterName;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public int getThreads() {
        return threads;
    }

    public void setThreads(int threads) {
        this.threads = threads;
    }

    public boolean isEnableMulticast() {
        return enableMulticast;
    }

    public void setEnableMulticast(boolean enableMulticast) {
        this.enableMulticast = enableMulticast;
    }

    public String getMulticastHost() {
        return multicastHost;
    }

    public void setMulticastHost(String multicastHost) {
        this.multicastHost = multicastHost;
    }

    public int getMulticastPort() {
        return multicastPort;
    }

    public void setMulticastPort(int multicastPort) {
        this.multicastPort = multicastPort;
    }

    public void doStart() throws Exception {
        Properties properties = SystemInstance.get().getProperties();
        properties.setProperty("ejbd.bind", host);
        properties.setProperty("ejbd.port", Integer.toString(port));
        properties.setProperty("ejbds.bind", host);
        if (threads > 0) {
            properties.setProperty("ejbd.threads", Integer.toString(threads));
            properties.setProperty("ejbds.threads", Integer.toString(threads));
        }

        properties.setProperty("multicast.bind", multicastHost);
        properties.setProperty("multicast.port", Integer.toString(multicastPort));
        properties.setProperty("multicast.disabled", Boolean.toString(!enableMulticast));
        properties.setProperty("multicast.group", clusterName);
        
        serviceManager.init();
        serviceManager.start(false);
        
        tracker.open();
    }

    public void doStop() throws Exception {
        serviceManager.stop();
        tracker.close();
    }

    public void doFail() {
    }

    private AbstractName addServerService(ServerService service) {
        AbstractName beanName = getUnqiueName(service.getName());
        GBeanData connectorData = new GBeanData(beanName, ServerServiceGBean.getGBeanInfo());

        try {
            kernel.loadGBean(connectorData, bundleContext);
            kernel.startRecursiveGBean(beanName);
        
            ServerServiceGBean connectorGBean = (ServerServiceGBean)kernel.getGBean(beanName);
            connectorGBean.setServerService(service);
            
            return beanName;
        } catch (Exception e) {
            log.warn("Failed to create gbean for ServerService", e);
            return null;
        }
    }
    
    private AbstractName getUnqiueName(String gbeanName) {
        AbstractName beanName = kernel.getNaming().createRootName(name.getArtifact(), gbeanName, "NetworkConnector");
        int i = 1;
        while (kernel.isLoaded(beanName) ) {
            beanName = kernel.getNaming().createRootName(name.getArtifact(), gbeanName + "-" + i, "NetworkConnector");
            i++;
        }
        return beanName;
    }
    
    private void removeServerService(AbstractName gbeanName) {
        try {
            if (kernel.getGBeanState(gbeanName) == State.RUNNING_INDEX) {
                kernel.stopGBean(gbeanName);
            }
            kernel.unloadGBean(gbeanName);
        } catch (GBeanNotFoundException e) {
            // Bean is no longer loaded
        }
    }
    
    public static final GBeanInfo GBEAN_INFO;

    static {
        GBeanInfoBuilder infoBuilder = GBeanInfoBuilder.createStatic("OpenEJB Daemon", EjbDaemonGBean.class);
        infoBuilder.addAttribute("host", String.class, true);
        infoBuilder.addAttribute("port", int.class, true);
        infoBuilder.addAttribute("clusterName", String.class, true);
        infoBuilder.addAttribute("multicastHost", String.class, true);
        infoBuilder.addAttribute("multicastPort", int.class, true);
        infoBuilder.addAttribute("enableMulticast", boolean.class, true);
        infoBuilder.addAttribute("threads", int.class, true);
        
        infoBuilder.addAttribute("kernel", Kernel.class, false);
        infoBuilder.addAttribute("abstractName", AbstractName.class, false, false);
        infoBuilder.addAttribute("bundleContext", BundleContext.class, false);
        
        infoBuilder.setConstructor(new String[]{"kernel", "abstractName", "bundleContext"});

        GBEAN_INFO = infoBuilder.getBeanInfo();
    }

    public static GBeanInfo getGBeanInfo() {
        return GBEAN_INFO;
    }
    
}
