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

package org.apache.geronimo.openejb.cdi;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ScheduledExecutorService;

import javax.servlet.ServletContext;

import org.apache.openejb.AppContext;
import org.apache.openejb.BeanContext;
import org.apache.openejb.assembler.classic.AppInfo;
import org.apache.openejb.cdi.CdiAppContextsService;
import org.apache.openejb.cdi.CdiResourceInjectionService;
import org.apache.openejb.cdi.CdiScanner;
import org.apache.openejb.cdi.OpenEJBLifecycle;
import org.apache.openejb.cdi.OpenEJBTransactionService;
import org.apache.openejb.cdi.OptimizedLoaderService;
import org.apache.openejb.cdi.StartupObject;
import org.apache.openejb.loader.SystemInstance;
import org.apache.webbeans.config.OpenWebBeansConfiguration;
import org.apache.webbeans.config.WebBeansContext;
import org.apache.webbeans.el.el22.EL22Adaptor;
import org.apache.webbeans.logger.WebBeansLogger;
import org.apache.webbeans.spi.ContainerLifecycle;
import org.apache.webbeans.spi.ContextsService;
import org.apache.webbeans.spi.ConversationService;
import org.apache.webbeans.spi.JNDIService;
import org.apache.webbeans.spi.LoaderService;
import org.apache.webbeans.spi.ResourceInjectionService;
import org.apache.webbeans.spi.ScannerService;
import org.apache.webbeans.spi.SecurityService;
import org.apache.webbeans.spi.TransactionService;
import org.apache.webbeans.spi.ValidatorService;
import org.apache.webbeans.spi.adaptor.ELAdaptor;
import org.apache.webbeans.util.WebBeansUtil;

/**
 * @version $Rev$ $Date$
 */
public class OpenWebBeansWebInitializer {

    /**Logger instance*/
    private static final WebBeansLogger logger = WebBeansLogger.getLogger(OpenWebBeansWebInitializer.class);

    public static WebBeansContext newWebBeansContext(Object startup) {
        Properties properties = new Properties();
        Map<Class<?>, Object> services = new HashMap<Class<?>, Object>();
        properties.setProperty(OpenWebBeansConfiguration.APPLICATION_IS_JSP, "true");
        properties.setProperty(OpenWebBeansConfiguration.USE_EJB_DISCOVERY, "true");
        //from CDI builder
        properties.setProperty(OpenWebBeansConfiguration.INTERCEPTOR_FORCE_NO_CHECKED_EXCEPTIONS, "false");
        properties.setProperty(SecurityService.class.getName(), ManagedSecurityService.class.getName());
        properties.setProperty(OpenWebBeansConfiguration.CONVERSATION_PERIODIC_DELAY, "1800000");
        properties.setProperty(OpenWebBeansConfiguration.APPLICATION_SUPPORTS_CONVERSATION, "true");
        properties.setProperty(OpenWebBeansConfiguration.IGNORED_INTERFACES, "org.apache.aries.proxy.weaving.WovenProxy");

        services.put(ValidatorService.class, new GeronimoValidatorService());
        services.put(TransactionService.class, new OpenEJBTransactionService());
        services.put(JNDIService.class, new NoopJndiService());
        services.put(ELAdaptor.class, new EL22Adaptor());
        services.put(LoaderService.class, new OptimizedLoaderService());

        if (startup != null && startup instanceof StartupObject) {

            ClassLoader cl = ((StartupObject) startup).getAppContext().getClassLoader();
            try {
                services.put(ConversationService.class, Class.forName("org.apache.webbeans.jsf.DefaultConversationService", true, cl).newInstance());
            } catch (Exception e1) {
                logger.info("openWebbeans-jsf is not in the classpath because the app does not contain webbean, conversationService will not be available.");
            }
        }

        services.put(ContextsService.class, new CdiAppContextsService(true));
        services.put(ResourceInjectionService.class, new CdiResourceInjectionService());
        services.put(ScannerService.class, new CdiScanner());
        WebBeansContext webBeansContext = new WebBeansContext(services, properties);
        //must be last since it copies stuff
        OpenEJBLifecycle lifecycle = new OpenEJBLifecycle(webBeansContext);
        webBeansContext.registerService(ContainerLifecycle.class, lifecycle);

        WebBeansContext oldContext = GeronimoSingletonService.contextEntered(webBeansContext);
        //from OWB's WebBeansConfigurationListener
        try {
            if (startup == null) {
                //this should only be used for servlet tests
                StartupObject startupObject = new StartupObject(new AppContext("none", SystemInstance.get(), Thread.currentThread().getContextClassLoader(), null, null, true), new AppInfo(),
                        Collections.<BeanContext> emptyList());
                lifecycle.startApplication(startupObject);
                //lifecycle.startServletContext((ServletContext)startup);
            } else if (startup instanceof StartupObject) {
                lifecycle.startApplication(startup);
                //((StartupObject)startup).getAppContext().setWebBeansContext(webBeansContext);
            }
        } catch (Exception e) {
            //logger.error(OWBLogConst.ERROR_0018, event.getServletContext().getContextPath());
            WebBeansUtil.throwRuntimeExceptions(e);
        } finally {
            GeronimoSingletonService.contextExited(oldContext);
        }
        return webBeansContext;
    }

    public static ScheduledExecutorService initializeServletContext(WebBeansContext webBeansContext, ServletContext servletContext) {
        WebBeansContext oldContext = GeronimoSingletonService.contextEntered(webBeansContext);
        try {
            OpenEJBLifecycle lifecycle = (OpenEJBLifecycle) webBeansContext.getService(ContainerLifecycle.class);
            //lifecycle.startServletContext(servletContext);
            //startServletContext will eventually call the static method  initializeServletContext, which will return a ThreadPool reference
            //We do need to keep that reference to prevent the thread leak
            return OpenEJBLifecycle.initializeServletContext(servletContext, webBeansContext);
        } finally {
            GeronimoSingletonService.contextExited(oldContext);
        }
    }

    //    public OpenWebBeansWebInitializer(WebBeansContext webBeansContext, ServletContext servletContext) {
    //        GeronimoSingletonService.contextEntered(webBeansContext);
    //
    //        try {
    //            setConfiguration(webBeansContext);
    //            //from OWB's WebBeansConfigurationListener
    //            if (servletContext != null) {
    //                ContainerLifecycle lifeCycle = webBeansContext.getService(ContainerLifecycle.class);
    //
    //                try {
    //                    lifeCycle.startApplication(new ServletContextEvent(servletContext));
    //                }
    //                catch (Exception e) {
    //                    //             logger.error(OWBLogConst.ERROR_0018, event.getServletContext().getContextPath());
    //                    WebBeansUtil.throwRuntimeExceptions(e);
    //                }
    //            }
    //
    //        } finally {
    //            GeronimoSingletonService.contextExited(null);
    //        }
    //    }
    //
    //    private void setConfiguration(WebBeansContext webBeansContext) {
    //        OpenWebBeansConfiguration configuration = webBeansContext.getOpenWebBeansConfiguration();
    //        configuration.setProperty(OpenWebBeansConfiguration.APPLICATION_IS_JSP, "true");
    //
    //        webBeansContext.registerService(JNDIService.class, new NoopJndiService());
    //        webBeansContext.registerService(ScannerService.class, new OsgiMetaDataScannerService(webBeansContext));
    //        webBeansContext.registerService(ContextsService.class, new WebContextsService(webBeansContext));
    //        webBeansContext.registerService(ResourceInjectionService.class, new GeronimoResourceInjectionService(webBeansContext));
    //        webBeansContext.registerService(ELAdaptor.class, new EL22Adaptor());
    //        //must be last since it idiotically copies stuff
    //        webBeansContext.registerService(ContainerLifecycle.class, new WebContainerLifecycle());
    ////        configuration.setProperty(OpenWebBeansConfiguration.CONTAINER_LIFECYCLE, WebContainerLifecycle.class.getName());
    ////        configuration.setProperty(OpenWebBeansConfiguration.JNDI_SERVICE, NoopJndiService.class.getName());
    ////        configuration.setProperty(OpenWebBeansConfiguration.SCANNER_SERVICE, OsgiMetaDataScannerService.class.getName());
    ////        configuration.setProperty(OpenWebBeansConfiguration.CONTEXTS_SERVICE, WebContextsService.class.getName());
    ////        configuration.setProperty(OpenWebBeansConfiguration.RESOURCE_INJECTION_SERVICE, GeronimoResourceInjectionService.class.getName());
    ////        configuration.setProperty(OpenWebBeansConfiguration.EL_ADAPTOR_CLASS, EL22Adaptor.class.getName());
    //    }

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
