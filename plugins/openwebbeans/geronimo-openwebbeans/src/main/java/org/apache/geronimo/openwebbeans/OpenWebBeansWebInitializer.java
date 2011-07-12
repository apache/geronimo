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

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import org.apache.webbeans.config.OpenWebBeansConfiguration;
import org.apache.webbeans.config.WebBeansContext;
import org.apache.webbeans.el.el22.EL22Adaptor;
import org.apache.webbeans.jsf.DefaultConversationService;
import org.apache.webbeans.spi.ContainerLifecycle;
import org.apache.webbeans.spi.ContextsService;
import org.apache.webbeans.spi.ConversationService;
import org.apache.webbeans.spi.JNDIService;
import org.apache.webbeans.spi.ResourceInjectionService;
import org.apache.webbeans.spi.ScannerService;
import org.apache.webbeans.spi.SecurityService;
import org.apache.webbeans.spi.TransactionService;
import org.apache.webbeans.spi.ValidatorService;
import org.apache.webbeans.spi.adaptor.ELAdaptor;
import org.apache.webbeans.util.WebBeansUtil;
import org.apache.webbeans.web.context.WebContextsService;
import org.apache.webbeans.web.lifecycle.WebContainerLifecycle;

/**
 * @version $Rev: 698441 $ $Date: 2008-09-24 00:10:08 -0700 (Wed, 24 Sep 2008) $
 */
public class OpenWebBeansWebInitializer {

    public static WebBeansContext newWebBeansContext(ServletContext servletContext) {
        Properties properties = new Properties();
        Map<Class<?>, Object> services = new HashMap<Class<?>, Object>();
        properties.setProperty(OpenWebBeansConfiguration.APPLICATION_IS_JSP, "true");
        properties.setProperty(SecurityService.class.getName(), org.apache.geronimo.openwebbeans.ManagedSecurityService.class.getName());
        properties.setProperty(OpenWebBeansConfiguration.CONVERSATION_PERIODIC_DELAY, "1800000");
        properties.setProperty(OpenWebBeansConfiguration.APPLICATION_SUPPORTS_CONVERSATION, "true");
        properties.setProperty(OpenWebBeansConfiguration.IGNORED_INTERFACES, "org.apache.aries.proxy.weaving.WovenProxy");

        services.put(ValidatorService.class, new GeronimoValidatorService());
        services.put(TransactionService.class, new GeronimoTransactionService());
        services.put(JNDIService.class, new NoopJndiService());
        services.put(ELAdaptor.class, new EL22Adaptor());
        services.put(ConversationService.class, new DefaultConversationService());
        WebBeansContext webBeansContext = new WebBeansContext(services, properties);
        webBeansContext.registerService(ScannerService.class, new OsgiMetaDataScannerService(webBeansContext));
        webBeansContext.registerService(ContextsService.class, new WebContextsService(webBeansContext));
        webBeansContext.registerService(ResourceInjectionService.class, new GeronimoResourceInjectionService(webBeansContext));
        //must be last since it idiotically copies stuff
        WebContainerLifecycle lifecycle = new WebContainerLifecycle(webBeansContext);
        webBeansContext.registerService(ContainerLifecycle.class, lifecycle);
        if (servletContext != null) {
            GeronimoSingletonService.contextEntered(webBeansContext);
            try {
                //from OWB's WebBeansConfigurationListener

                try {
                    lifecycle.startApplication(new ServletContextEvent(servletContext));
                } catch (Exception e) {
                    //             logger.error(OWBLogConst.ERROR_0018, event.getServletContext().getContextPath());
                    WebBeansUtil.throwRuntimeExceptions(e);
                }

            } finally {
                GeronimoSingletonService.contextExited(null);
            }
        }
        return webBeansContext;
    }

    public OpenWebBeansWebInitializer(WebBeansContext webBeansContext, ServletContext servletContext) {
        GeronimoSingletonService.contextEntered(webBeansContext);

        try {
            setConfiguration(webBeansContext);
            //from OWB's WebBeansConfigurationListener
            if (servletContext != null) {
                ContainerLifecycle lifeCycle = webBeansContext.getService(ContainerLifecycle.class);

                try {
                    lifeCycle.startApplication(new ServletContextEvent(servletContext));
                }
                catch (Exception e) {
                    //             logger.error(OWBLogConst.ERROR_0018, event.getServletContext().getContextPath());
                    WebBeansUtil.throwRuntimeExceptions(e);
                }
            }

        } finally {
            GeronimoSingletonService.contextExited(null);
        }
    }

    private void setConfiguration(WebBeansContext webBeansContext) {
        OpenWebBeansConfiguration configuration = webBeansContext.getOpenWebBeansConfiguration();
        configuration.setProperty(OpenWebBeansConfiguration.APPLICATION_IS_JSP, "true");

        webBeansContext.registerService(JNDIService.class, new NoopJndiService());
        webBeansContext.registerService(ScannerService.class, new OsgiMetaDataScannerService(webBeansContext));
        webBeansContext.registerService(ContextsService.class, new WebContextsService(webBeansContext));
        webBeansContext.registerService(ResourceInjectionService.class, new GeronimoResourceInjectionService(webBeansContext));
        webBeansContext.registerService(ELAdaptor.class, new EL22Adaptor());
        //must be last since it idiotically copies stuff
        webBeansContext.registerService(ContainerLifecycle.class, new WebContainerLifecycle());
//        configuration.setProperty(OpenWebBeansConfiguration.CONTAINER_LIFECYCLE, WebContainerLifecycle.class.getName());
//        configuration.setProperty(OpenWebBeansConfiguration.JNDI_SERVICE, NoopJndiService.class.getName());
//        configuration.setProperty(OpenWebBeansConfiguration.SCANNER_SERVICE, OsgiMetaDataScannerService.class.getName());
//        configuration.setProperty(OpenWebBeansConfiguration.CONTEXTS_SERVICE, WebContextsService.class.getName());
//        configuration.setProperty(OpenWebBeansConfiguration.RESOURCE_INJECTION_SERVICE, GeronimoResourceInjectionService.class.getName());
//        configuration.setProperty(OpenWebBeansConfiguration.EL_ADAPTOR_CLASS, EL22Adaptor.class.getName());
    }

    public static class NoopJndiService implements JNDIService {

        public void bind(String name, Object object) {
        }

        public <T> T lookup(String name, Class<? extends T> expectedClass) {
            return null;
        }

        public void unbind(String name) {
        }

    }

}
