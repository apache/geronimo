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

package org.apache.geronimo.myfaces;

import javax.faces.context.ExternalContext;

import org.apache.geronimo.myfaces.webapp.MyFacesWebAppContext;
import org.apache.geronimo.web.WebApplicationConstants;
import org.apache.myfaces.config.annotation.LifecycleProvider;
import org.apache.myfaces.config.annotation.LifecycleProviderFactory;
import org.apache.xbean.osgi.bundle.util.BundleUtils;
import org.osgi.framework.Bundle;

/**
 * @version $Rev$ $Date$
 */
public class GeronimoLifecycleProviderFactory extends LifecycleProviderFactory {

    private LifecycleProvider lifecycleProvider;

    public LifecycleProvider getLifecycleProvider(ExternalContext externalContext) {
        if (lifecycleProvider == null) {
            String webModuleName = (String) externalContext.getApplicationMap().get(WebApplicationConstants.WEB_APP_NAME);
            MyFacesWebAppContext myFacesWebAppContext = MyFacesWebAppContext.getMyFacesWebAppContext(webModuleName);
            lifecycleProvider = myFacesWebAppContext.getLifecycleProvider();
            if (lifecycleProvider == null) {
                throw new IllegalStateException("No LifecycleProvider registered for application " + webModuleName + " in the bundle: " + getBundle());
            }
        }
        return lifecycleProvider;
    }

    private Bundle getBundle() {
        Bundle bundle = BundleUtils.getContextBundle(false);
        if (bundle == null) {
            throw new IllegalStateException("Unable to get Bundle object associated with the context classloader");
        }
        return bundle;
    }

    @Override
    public void release() {
        lifecycleProvider = null;
    }
}
