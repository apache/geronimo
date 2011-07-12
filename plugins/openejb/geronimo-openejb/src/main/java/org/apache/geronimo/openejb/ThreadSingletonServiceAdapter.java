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


package org.apache.geronimo.openejb;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.apache.geronimo.openwebbeans.GeronimoSingletonService;
import org.apache.geronimo.openwebbeans.GeronimoValidatorService;
import org.apache.geronimo.openwebbeans.OpenWebBeansWebInitializer;
import org.apache.geronimo.openwebbeans.OsgiMetaDataScannerService;
import org.apache.openejb.cdi.CdiAppContextsService;
import org.apache.openejb.cdi.CdiResourceInjectionService;
import org.apache.openejb.cdi.OpenEJBLifecycle;
import org.apache.openejb.cdi.OpenEJBTransactionService;
import org.apache.openejb.cdi.StartupObject;
import org.apache.openejb.cdi.ThreadSingletonService;
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

/**
 * @version $Rev$ $Date$
 */
public class ThreadSingletonServiceAdapter implements ThreadSingletonService {

    private final GeronimoSingletonService geronimoSingletonService = new GeronimoSingletonService();

    public ThreadSingletonServiceAdapter() {
        super();
    }

    @Override
    public void initialize(StartupObject startupObject) {
        //share owb singletons
        WebBeansContext webBeansContext = startupObject.getAppContext().get(WebBeansContext.class);
        if (webBeansContext != null) {
            return;
        }
        Object old = contextEntered(null);
        try {
            if (old == null) {
                Properties properties = new Properties();
                Map<Class<?>, Object> services = new HashMap<Class<?>, Object>();
//                properties.setProperty(OpenWebBeansConfiguration.APPLICATION_IS_JSP, "true");
                properties.setProperty(OpenWebBeansConfiguration.USE_EJB_DISCOVERY, "true");
                //from CDI builder
                properties.setProperty(OpenWebBeansConfiguration.INTERCEPTOR_FORCE_NO_CHECKED_EXCEPTIONS, "false");

                properties.setProperty(SecurityService.class.getName(), org.apache.geronimo.openwebbeans.ManagedSecurityService.class.getName());
                properties.setProperty(OpenWebBeansConfiguration.CONVERSATION_PERIODIC_DELAY, "1800000");
                properties.setProperty(OpenWebBeansConfiguration.APPLICATION_SUPPORTS_CONVERSATION, "true");
                properties.setProperty(OpenWebBeansConfiguration.IGNORED_INTERFACES, "org.apache.aries.proxy.weaving.WovenProxy");

                services.put(ValidatorService.class, new GeronimoValidatorService());
                services.put(TransactionService.class, new OpenEJBTransactionService());
                services.put(JNDIService.class, new OpenWebBeansWebInitializer.NoopJndiService());
                services.put(ELAdaptor.class, new EL22Adaptor());
                services.put(ConversationService.class, new DefaultConversationService());
                services.put(ContextsService.class, new CdiAppContextsService());
                services.put(ResourceInjectionService.class, new CdiResourceInjectionService());
                webBeansContext = new WebBeansContext(services, properties);
                webBeansContext.registerService(ScannerService.class, new OsgiMetaDataScannerService(webBeansContext));
                contextEntered(webBeansContext);
                ContainerLifecycle lifecycle = new OpenEJBLifecycle();
                webBeansContext.registerService(ContainerLifecycle.class, lifecycle);
                try {
                    //not embedded. Are we the first ejb module to try this?
                    startupObject.getAppContext().set(WebBeansContext.class, webBeansContext);
//                    setConfiguration(webBeansContext.getOpenWebBeansConfiguration());
                    lifecycle.startApplication(startupObject);
                } catch (Exception e) {
                    throw new RuntimeException("couldn't start owb context", e);
                } finally {
                    contextExited(null);
                }
                startupObject.getAppContext().set(WebBeansContext.class, webBeansContext);
            } else {
                // an existing OWBConfiguration will have already been initialized
                startupObject.getAppContext().set(WebBeansContext.class, (WebBeansContext) old);
            }
        } finally {
            contextExited(old);
        }
    }

//    private void setConfiguration(OpenWebBeansConfiguration configuration) {
//        configuration.setProperty(OpenWebBeansConfiguration.USE_EJB_DISCOVERY, "true");
//        //from CDI builder
//        configuration.setProperty(OpenWebBeansConfiguration.INTERCEPTOR_FORCE_NO_CHECKED_EXCEPTIONS, "false");
//
//        configuration.setProperty(OpenWebBeansConfiguration.CONTAINER_LIFECYCLE, OpenEJBLifecycle.class.getName());
//        configuration.setProperty(OpenWebBeansConfiguration.JNDI_SERVICE, OpenWebBeansWebInitializer.NoopJndiService.class.getName());
//        configuration.setProperty(OpenWebBeansConfiguration.SCANNER_SERVICE, OsgiMetaDataScannerService.class.getName());
//        configuration.setProperty(OpenWebBeansConfiguration.CONTEXTS_SERVICE, CdiAppContextsService.class.getName());
//        configuration.setProperty(ResourceInjectionService.class.getName(), CdiResourceInjectionService.class.getName());
////        configuration.setProperty(ELAdaptor.class.getName(), EL22Adaptor.class.getName());
//    }

    @Override
    public Object contextEntered(WebBeansContext owbContext) {
        return GeronimoSingletonService.contextEntered(owbContext);
    }

    @Override
    public void contextExited(Object oldContext) {
        if (oldContext != null && !(oldContext instanceof WebBeansContext)) throw new IllegalArgumentException("Expecting a WebBeansContext not " + oldContext.getClass().getName());
        GeronimoSingletonService.contextExited((WebBeansContext) oldContext);
    }

    @Override
    public WebBeansContext get(Object key) {
        return geronimoSingletonService.get(key);
    }

    @Override
    public void clear(Object key) {
        geronimoSingletonService.clear(key);
    }
}
