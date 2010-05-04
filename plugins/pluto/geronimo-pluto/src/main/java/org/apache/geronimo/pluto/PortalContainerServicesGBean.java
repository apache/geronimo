/**
 *
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

package org.apache.geronimo.pluto;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.gbean.GBeanInfoBuilder;
import org.apache.geronimo.gbean.GBeanLifecycle;
import org.apache.geronimo.kernel.Kernel;
import org.apache.geronimo.kernel.KernelRegistry;
import org.apache.pluto.driver.config.AdminConfiguration;
import org.apache.pluto.driver.config.DriverConfiguration;
import org.apache.pluto.driver.services.portal.PropertyConfigService;
import org.apache.pluto.driver.services.portal.RenderConfigService;
//import org.apache.pluto.spi.PortalCallbackService;

/*
 * A GBean that provides access to pluto's container services.  The pluto
 * services are typically configured by spring.  The spring config should
 * create this GBean using the getSingleton() method provided here and
 * then provide it with references to the pluto services.
 */
public class PortalContainerServicesGBean implements PortalContainerServices, GBeanLifecycle {
    private static final Logger log = LoggerFactory.getLogger(PortalContainerServicesGBean.class);
    
    private RenderConfigService renderConfigService;
//    private PortalCallbackService portalCallbackService;
    private PropertyConfigService propertyConfigService;
    private DriverConfiguration driverConfiguration;
    private AdminConfiguration adminConfiguration;

    private CountDownLatch latch = new CountDownLatch(1);
    
    public void doStart() throws Exception {
        log.debug("Started PortalContainerServicesGBean");
    }
    
    public void doStop() throws Exception {
        log.debug("Stopped PortalContainerServicesGBean");
    }
    
    public void doFail() {
        log.warn("PortalContainerServicesGBean Failed");
    }
    
    public AdminConfiguration getAdminConfiguration() {
        return adminConfiguration;
    }

    public void setAdminConfiguration(AdminConfiguration adminConfiguration) {
        this.adminConfiguration = adminConfiguration;
    }
    
    public RenderConfigService getRenderConfigService() {
        return renderConfigService;
    }

    public void setRenderConfigService(RenderConfigService renderConfigService) {
        this.renderConfigService = renderConfigService;
    }

    public DriverConfiguration getDriverConfiguration() {
        return driverConfiguration;
    }

    public void setDriverConfiguration(DriverConfiguration driverConfigurion) {
        this.driverConfiguration = driverConfigurion;
    }

//    public PortalCallbackService getPortalCallbackService() {
//        return portalCallbackService;
//    }
//
//    public void setPortalCallbackService(
//            PortalCallbackService portalCallbackService) {
//        this.portalCallbackService = portalCallbackService;
//    }

    public PropertyConfigService getPropertyConfigService() {
        return propertyConfigService;
    }

    public void setPropertyConfigService(
            PropertyConfigService propertyConfigService) {
        this.propertyConfigService = propertyConfigService;
    }

    public static PortalContainerServices getSingleton() {
        Kernel kernel = KernelRegistry.getSingleKernel();
        PortalContainerServices portalServices = null;
        try {
            portalServices = (PortalContainerServices) kernel.getGBean(PortalContainerServices.class);
        } catch (Exception e) {
            log.error("Failed to get PortalContainerServices GBean from kernel", e);
        }
        return portalServices;
    }
    
    public void init() {
        latch.countDown();
    }
    
    public boolean waitForInitialization(int timeout) throws InterruptedException {
        return latch.await(timeout, TimeUnit.MILLISECONDS);
    }

    public static final GBeanInfo GBEAN_INFO;

    static {
        GBeanInfoBuilder infoFactory = GBeanInfoBuilder.createStatic("PortalContainerServicesGBean", PortalContainerServicesGBean.class);
        infoFactory.addInterface(PortalContainerServices.class);
        infoFactory.setConstructor(new String[0]);
        GBEAN_INFO = infoFactory.getBeanInfo();
    }

    public static GBeanInfo getGBeanInfo() {
        return GBEAN_INFO;
    }

}