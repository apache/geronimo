/**
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

package org.apache.geronimo.openwebbeans;

import java.util.concurrent.ScheduledExecutorService;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;

import org.apache.geronimo.openejb.cdi.OpenWebBeansWebInitializer;
import org.apache.geronimo.web.WebApplicationConstants;
import org.apache.geronimo.web.WebModuleListener;
import org.apache.webbeans.config.OWBLogConst;
import org.apache.webbeans.config.WebBeansContext;
import org.apache.webbeans.logger.WebBeansLogger;
import org.apache.webbeans.spi.ContainerLifecycle;
import org.apache.webbeans.spi.FailOverService;
import org.apache.webbeans.util.WebBeansUtil;

/**
 * @version $Rev$ $Date$
 */
public class OpenWebBeansWebModuleListener implements WebModuleListener {

    private static final WebBeansLogger logger = WebBeansLogger.getLogger(WebBeansConfigurationListener.class);

    protected FailOverService failoverService;

    /**Manages the container lifecycle*/
    protected ContainerLifecycle lifeCycle = null;

    protected WebBeansContext webBeansContext;

    private ScheduledExecutorService service;

    @Override
    public void moduleInitialized(ServletContext servletContext) {
        String webModuleName = (String) servletContext.getAttribute(WebApplicationConstants.WEB_APP_NAME);
        OpenWebBeansWebAppContext webAppContext = OpenWebBeansWebAppContext.getOpenWebBeansWebAppContext(webModuleName);
        // Initialize WebBeansContext in current servlet context
        this.webBeansContext = webAppContext.getWebBeansContext();
        //TODO Seems that only one cleanUp thread is required for the shareable WebBeansContext
        service = OpenWebBeansWebInitializer.initializeServletContext(webBeansContext, servletContext);
        //Register the interceptor to Holder instance
        webAppContext.getHolder().addInterceptor(new OpenWebBeansHolderInterceptor(this.webBeansContext));
        this.failoverService = webBeansContext.getService(FailOverService.class);
        try {
            this.lifeCycle = webBeansContext.getService(ContainerLifecycle.class);
            //OpenWebBeansWebModuleListener will only manage the lifecycle if the WebBeansContext is standalone for the current web application
            //Or the AppInfoGBean will maintain the sharelable WebBeansContext
            if (lifeCycle instanceof org.apache.webbeans.web.lifecycle.WebContainerLifecycle || !webAppContext.isShareableWebBeansContext()) {
                this.lifeCycle.startApplication(new ServletContextEvent(servletContext));
            } else {
                this.lifeCycle = null;
            }
        } catch (Exception e) {
            logger.error(OWBLogConst.ERROR_0018, servletContext.getContextPath());
            WebBeansUtil.throwRuntimeExceptions(e);
        }
    }

    @Override
    public void moduleDestoryed(ServletContext servletContext) {
        if (this.lifeCycle != null) {
            this.lifeCycle.stopApplication(new ServletContextEvent(servletContext));
            this.lifeCycle = null;
        }
        if (service != null) {
            service.shutdownNow();
        }
        this.webBeansContext = null;
    }

}
