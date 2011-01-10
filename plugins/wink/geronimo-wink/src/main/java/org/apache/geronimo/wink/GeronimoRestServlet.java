/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.    
 */

package org.apache.geronimo.wink;

import java.io.IOException;

import javax.ws.rs.core.Application;

import org.apache.wink.common.internal.i18n.Messages;
import org.apache.wink.common.internal.lifecycle.ObjectFactory;
import org.apache.wink.common.internal.utils.ClassUtils;
import org.apache.wink.server.internal.DeploymentConfiguration;
import org.apache.wink.server.internal.RequestProcessor;
import org.apache.wink.server.internal.servlet.RestServlet;
import org.apache.xbean.osgi.bundle.util.BundleClassLoader;
import org.osgi.framework.BundleContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GeronimoRestServlet extends RestServlet {
    

    private static final String DEPLOYMENT_CONF_PARAM = "deploymentConfiguration";

    private static final Logger logger               =
                                                         LoggerFactory
                                                             .getLogger(GeronimoRestServlet.class);

    private static final long   serialVersionUID     = -1920970727031271538L;

    @Override
    protected DeploymentConfiguration createDeploymentConfiguration()
        throws ClassNotFoundException, InstantiationException, IllegalAccessException {
       
        DeploymentConfiguration deploymentConfiguration=null;
        BundleContext bundleContext = (BundleContext) this.getServletContext().getAttribute("osgi-bundlecontext");
        ClassLoader deploymentClassLoader = new BundleClassLoader(bundleContext.getBundle());
        ClassLoader oldContextClassLoader = Thread.currentThread().getContextClassLoader();
        try {
            Thread.currentThread().setContextClassLoader(deploymentClassLoader);
            deploymentConfiguration=super.createDeploymentConfiguration();
        } finally {
            Thread.currentThread().setContextClassLoader(oldContextClassLoader);
        }

        return deploymentConfiguration;
        
    }
    
    @Override
    protected RequestProcessor createRequestProcessor() throws ClassNotFoundException,
    InstantiationException, IllegalAccessException, IOException {
    
        RequestProcessor requestProcessor=null;
        BundleContext bundleContext = (BundleContext) this.getServletContext().getAttribute("osgi-bundlecontext");
        ClassLoader deploymentClassLoader = new BundleClassLoader(bundleContext.getBundle());
        ClassLoader oldContextClassLoader = Thread.currentThread().getContextClassLoader();
        try {
            Thread.currentThread().setContextClassLoader(deploymentClassLoader);
            requestProcessor=super.createRequestProcessor();
        } finally {
            Thread.currentThread().setContextClassLoader(oldContextClassLoader);
        }

        return requestProcessor;
    }
    
    
    
    @SuppressWarnings("unchecked")
    @Override
    protected Application getApplication(DeploymentConfiguration configuration) throws ClassNotFoundException,
            InstantiationException, IllegalAccessException {
        
        Class<? extends Application> appClass = null;
        String initParameter = getInitParameter(APPLICATION_INIT_PARAM);
        if (initParameter != null) {
            if (logger.isInfoEnabled()) {
                logger.info(Messages.getMessage("restServletJAXRSApplicationInitParam", //$NON-NLS-1$
                        initParameter, APPLICATION_INIT_PARAM));
            }
            
            BundleContext bundleContext = (BundleContext) this.getServletContext().getAttribute("osgi-bundlecontext");
            ClassLoader deploymentClassLoader = new BundleClassLoader(bundleContext.getBundle());
            ClassLoader oldContextClassLoader = Thread.currentThread().getContextClassLoader();
            try {
                Thread.currentThread().setContextClassLoader(deploymentClassLoader);
                appClass = (Class<? extends Application>) ClassUtils.loadClass(initParameter);
            } finally {
                Thread.currentThread().setContextClassLoader(oldContextClassLoader);
            }

            // let the lifecycle manager create the instance and process fields
            // for injection
            ObjectFactory<? extends Application> of = configuration.getOfFactoryRegistry().getObjectFactory(appClass);
            configuration.addApplicationObjectFactory(of);

            return of.getInstance(null);
            
        } else {

            throw new IllegalAccessException("Can find init parameter: " + APPLICATION_INIT_PARAM + " for rest servelt");

        }

        
    }

}
