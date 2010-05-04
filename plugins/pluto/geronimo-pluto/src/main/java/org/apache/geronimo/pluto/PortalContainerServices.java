/**
 *
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
package org.apache.geronimo.pluto;

import org.apache.pluto.driver.config.AdminConfiguration;
import org.apache.pluto.driver.config.DriverConfiguration;
import org.apache.pluto.driver.services.portal.PropertyConfigService;
import org.apache.pluto.driver.services.portal.RenderConfigService;
//import org.apache.pluto.spi.PortalCallbackService;

/**
 * Classes implementing this interface provide access to pluto's
 * container services
 * 
 * @version $Rev$ $Date$
 */
public interface PortalContainerServices {
    
    boolean waitForInitialization(int timeout) throws InterruptedException;
    
    AdminConfiguration getAdminConfiguration();
    void setAdminConfiguration(AdminConfiguration adminConfiguration);
    
    RenderConfigService getRenderConfigService();
    void setRenderConfigService(RenderConfigService renderConfigService);
    
//    PortalCallbackService getPortalCallbackService();
//    void setPortalCallbackService(PortalCallbackService portalCallbackService);

    PropertyConfigService getPropertyConfigService();
    void setPropertyConfigService(PropertyConfigService propertyConfigService);

    DriverConfiguration getDriverConfiguration();
    void setDriverConfiguration(DriverConfiguration driverConfiguration);
    
}