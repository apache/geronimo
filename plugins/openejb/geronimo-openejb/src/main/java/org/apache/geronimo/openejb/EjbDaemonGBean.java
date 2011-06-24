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
import org.apache.geronimo.gbean.GBeanLifecycle;
import org.apache.geronimo.gbean.annotation.GBean;
import org.apache.geronimo.gbean.annotation.ParamAttribute;
import org.apache.geronimo.gbean.annotation.ParamSpecial;
import org.apache.geronimo.gbean.annotation.SpecialAttributeType;
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
@GBean
public class EjbDaemonGBean implements GBeanLifecycle {

    private static final Logger log = LoggerFactory.getLogger(EjbDaemonGBean.class);

    private final Kernel kernel;
    private final AbstractName name;
    private final BundleContext bundleContext;
    private final ClassLoader classLoader;
    private ServiceTracker tracker;
    private ServiceManager serviceManager;

    public EjbDaemonGBean(@ParamAttribute(name = "host") String host,
                          @ParamAttribute(name = "port") int port,
                          @ParamAttribute(name = "threads") int threads,
                          @ParamAttribute(name = "clusterName") String clusterName,
                          @ParamAttribute(name = "multicastHost") String multicastHost,
                          @ParamAttribute(name = "multicastPort") int multicastPort,
                          @ParamAttribute(name = "multicastEnabled") boolean multicastEnabled,
                          @ParamAttribute(name = "multipointEnabled") boolean multipointEnabled,
                          @ParamAttribute(name = "multipointHost") String multipointHost,
                          @ParamAttribute(name = "multipointPort") int multipointPort,
                          @ParamAttribute(name = "multipointServers") String multipointServers,

                          @ParamSpecial(type = SpecialAttributeType.kernel) Kernel kernel,
                          @ParamSpecial(type = SpecialAttributeType.abstractName) AbstractName name,
                          @ParamSpecial(type = SpecialAttributeType.bundleContext) final BundleContext bundleContext,
                          @ParamSpecial(type = SpecialAttributeType.classLoader) ClassLoader classLoader) throws Exception {
        System.setProperty("openejb.nobanner", "true");
        this.kernel = kernel;
        this.name = name;
        this.bundleContext = bundleContext;
        this.classLoader = classLoader;

        serviceManager = ServiceManager.getManager();

        tracker = new ServiceTracker(bundleContext, ServerService.class.getName(), new ServiceTrackerCustomizer() {

            public Object addingService(ServiceReference reference) {
                ServerService service = (ServerService) bundleContext.getService(reference);
                return addServerService(service);
            }

            public void modifiedService(ServiceReference reference, Object obj) {
            }

            public void removedService(ServiceReference reference, Object obj) {
                removeServerService((AbstractName) obj);
            }

        });
        Properties properties = SystemInstance.get().getProperties();
        properties.setProperty("ejbd.bind", host);
        properties.setProperty("ejbd.port", Integer.toString(port));
        properties.setProperty("ejbds.bind", host);
        properties.setProperty("ejbds.disabled", "true");
        if (threads > 0) {
            properties.setProperty("ejbd.threads", Integer.toString(threads));
            properties.setProperty("ejbds.threads", Integer.toString(threads));
        }

        properties.setProperty("multicast.bind", multicastHost);
        properties.setProperty("multicast.port", Integer.toString(multicastPort));
        properties.setProperty("multicast.disabled", Boolean.toString(!multicastEnabled));
        properties.setProperty("multicast.group", clusterName);
        
        properties.setProperty("multipoint.bind", multipointHost);
        properties.setProperty("multipoint.port", Integer.toString(multipointPort));
        properties.setProperty("multipoint.initialServers", multipointServers);
        properties.setProperty("multipoint.disabled", Boolean.toString(!multipointEnabled));
        properties.setProperty("multipoint.group", clusterName);

        ClassLoader oldCl = Thread.currentThread().getContextClassLoader();
        Thread.currentThread().setContextClassLoader(classLoader);
        try {
            serviceManager.init();
            serviceManager.start(false);
        } finally {
            Thread.currentThread().setContextClassLoader(oldCl);
        }

        tracker.open();
    }

    public void doStart() throws Exception {
    }

    public void doStop() throws Exception {
        serviceManager.stop();
        tracker.close();
    }

    public void doFail() {
        try {
            doStop();
        } catch (Exception e) {
            log.info("exception failing", e);
        }
    }

    private AbstractName addServerService(ServerService service) {
        AbstractName beanName = getUnqiueName(service.getName());
        GBeanData connectorData = new GBeanData(beanName, ServerServiceGBean.getGBeanInfo());

        try {
            kernel.loadGBean(connectorData, bundleContext);
            kernel.startRecursiveGBean(beanName);

            ServerServiceGBean connectorGBean = (ServerServiceGBean) kernel.getGBean(beanName);
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
        while (kernel.isLoaded(beanName)) {
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


}
