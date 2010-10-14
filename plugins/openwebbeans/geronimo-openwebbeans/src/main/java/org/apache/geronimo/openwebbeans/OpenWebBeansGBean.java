/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.apache.geronimo.openwebbeans;

import org.apache.geronimo.gbean.GBeanLifecycle;
import org.apache.geronimo.gbean.annotation.GBean;
import org.apache.geronimo.gbean.annotation.ParamSpecial;
import org.apache.geronimo.gbean.annotation.SpecialAttributeType;
import org.apache.webbeans.config.OpenWebBeansConfiguration;
import org.apache.webbeans.spi.JNDIService;
import org.apache.webbeans.web.context.WebContextsService;
import org.apache.webbeans.web.lifecycle.WebContainerLifecycle;
import org.osgi.framework.BundleContext;

/**
 *  
 * @version $Rev: 698441 $ $Date: 2008-09-24 00:10:08 -0700 (Wed, 24 Sep 2008) $
 */
@GBean
public class OpenWebBeansGBean implements GBeanLifecycle {
    
    public OpenWebBeansGBean(@ParamSpecial(type = SpecialAttributeType.bundleContext) final BundleContext bundleContext,
                             @ParamSpecial(type = SpecialAttributeType.classLoader) ClassLoader classLoader) {
        setConfiguration(OpenWebBeansConfiguration.getInstance());
    }

    private void setConfiguration(OpenWebBeansConfiguration configuration) {
        configuration.setProperty(OpenWebBeansConfiguration.APPLICATION_IS_JSP, "true");
        
        configuration.setProperty(OpenWebBeansConfiguration.CONTAINER_LIFECYCLE, WebContainerLifecycle.class.getName());
        configuration.setProperty(OpenWebBeansConfiguration.JNDI_SERVICE, NoopJndiService.class.getName());
        configuration.setProperty(OpenWebBeansConfiguration.SCANNER_SERVICE, OsgiMetaDataScannerService.class.getName());
        configuration.setProperty(OpenWebBeansConfiguration.CONTEXTS_SERVICE, WebContextsService.class.getName());
    }

    public void doStart() {
        System.out.println("Start OpenWebBeansGBean");
    }

    public void doStop() {
        System.out.println("Stop OpenWebBeansGBean");
    }

    public void doFail() {
        doStop();
    }
    
    public static class NoopJndiService implements JNDIService {

        public void bind(String name, Object object) {
            System.out.println("Bind");
        }

        public <T> T lookup(String name, Class<? extends T> expectedClass) {
            System.out.println("Lookup");
            return null;
        }

        public void unbind(String name) {
            System.out.println("Unbind");
        }
        
    }

}
