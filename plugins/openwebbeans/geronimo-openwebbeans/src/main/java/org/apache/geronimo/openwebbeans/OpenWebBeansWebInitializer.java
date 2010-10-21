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

import java.util.Map;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import org.apache.webbeans.config.OpenWebBeansConfiguration;
import org.apache.webbeans.el.el22.EL22Adaptor;
import org.apache.webbeans.lifecycle.LifecycleFactory;
import org.apache.webbeans.spi.ContainerLifecycle;
import org.apache.webbeans.spi.JNDIService;
import org.apache.webbeans.spi.adaptor.ELAdaptor;
import org.apache.webbeans.util.WebBeansUtil;
import org.apache.webbeans.web.context.WebContextsService;
import org.apache.webbeans.web.lifecycle.WebContainerLifecycle;

/**
 *  
 * @version $Rev: 698441 $ $Date: 2008-09-24 00:10:08 -0700 (Wed, 24 Sep 2008) $
 */
public class OpenWebBeansWebInitializer {
    
    public OpenWebBeansWebInitializer(Map<String, Object> owbContext, ServletContext servletContext) {
        GeronimoSingletonService.contextEntered(owbContext);

        try {
            setConfiguration(OpenWebBeansConfiguration.getInstance());
            //from OWB's WebBeansConfigurationListener
            if (servletContext != null) {
                ContainerLifecycle lifeCycle = LifecycleFactory.getInstance().getLifecycle();

                try
                {
                        lifeCycle.startApplication(new ServletContextEvent(servletContext));
                        servletContext.setAttribute(OpenWebBeansConfiguration.PROPERTY_OWB_APPLICATION, "true");
                }
                catch (Exception e)
                {
    //             logger.error(OWBLogConst.ERROR_0018, event.getServletContext().getContextPath());
                     WebBeansUtil.throwRuntimeExceptions(e);
                }
            }
        } finally {
            GeronimoSingletonService.contextExited(null);
        }
    }

    private void setConfiguration(OpenWebBeansConfiguration configuration) {
        configuration.setProperty(OpenWebBeansConfiguration.APPLICATION_IS_JSP, "true");
        
        configuration.setProperty(OpenWebBeansConfiguration.CONTAINER_LIFECYCLE, WebContainerLifecycle.class.getName());
        configuration.setProperty(OpenWebBeansConfiguration.JNDI_SERVICE, NoopJndiService.class.getName());
        configuration.setProperty(OpenWebBeansConfiguration.SCANNER_SERVICE, OsgiMetaDataScannerService.class.getName());
        configuration.setProperty(OpenWebBeansConfiguration.CONTEXTS_SERVICE, WebContextsService.class.getName());
        configuration.setProperty(ELAdaptor.class.getName(), EL22Adaptor.class.getName());
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
