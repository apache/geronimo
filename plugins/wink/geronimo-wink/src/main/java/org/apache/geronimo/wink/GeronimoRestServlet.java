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

import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletException;
import javax.ws.rs.core.Application;

import org.apache.wink.common.internal.i18n.Messages;
import org.apache.wink.common.internal.lifecycle.ObjectFactory;
import org.apache.wink.common.internal.utils.ClassUtils;
import org.apache.wink.server.internal.DeploymentConfiguration;
import org.apache.wink.server.internal.servlet.RestServlet;
import org.apache.xbean.osgi.bundle.util.BundleClassLoader;
import org.apache.xbean.osgi.bundle.util.BundleUtils;
import org.apache.xbean.osgi.bundle.util.DelegatingBundle;
import org.osgi.framework.Bundle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GeronimoRestServlet extends RestServlet {

    private static final Logger logger = LoggerFactory.getLogger(GeronimoRestServlet.class);

    private static final long serialVersionUID = -1920970727031271538L;

    @Override
    public void init() throws ServletException {
        ClassLoader oldClassLoader = Thread.currentThread().getContextClassLoader();
        try {
            Thread.currentThread().setContextClassLoader(getContextClassLoader());
            super.init();
        } finally {
            Thread.currentThread().setContextClassLoader(oldClassLoader);
        }
    }

    /**
     * The new delegating bundle classloader will make sure the resources in the wink-server bundle could be found even if
     * wired bundles are not searched
     * @return
     */
    protected ClassLoader getContextClassLoader() {
        ClassLoader contextCL = Thread.currentThread().getContextClassLoader();
        List<Bundle> bundles = new ArrayList<Bundle>(2);
        bundles.add(BundleUtils.getBundle(contextCL, false));
        bundles.add(BundleUtils.getBundle(RestServlet.class.getClassLoader(), true));
        return new BundleClassLoader(new DelegatingBundle(bundles));
    }

    @SuppressWarnings("unchecked")
    @Override
    protected Application getApplication(DeploymentConfiguration configuration) throws ClassNotFoundException, InstantiationException, IllegalAccessException {
        Class<? extends Application> appClass = null;
        String initParameter = getInitParameter(APPLICATION_INIT_PARAM);
        if (initParameter != null) {
            if (logger.isInfoEnabled()) {
                logger.info(Messages.getMessage("restServletJAXRSApplicationInitParam", //$NON-NLS-1$
                        initParameter, APPLICATION_INIT_PARAM));
            }
            appClass = ClassUtils.loadClass(initParameter);
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
