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

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.faces.context.ExternalContext;

import org.apache.geronimo.kernel.osgi.BundleUtils;
import org.apache.myfaces.config.annotation.LifecycleProvider;
import org.apache.myfaces.config.annotation.LifecycleProviderFactory;
import org.osgi.framework.Bundle;

/**
 * @version $Rev$ $Date$
 */
public class ApplicationIndexedLifecycleProviderFactory extends LifecycleProviderFactory {

    private final Map<Bundle, LifecycleProvider> providers = new ConcurrentHashMap<Bundle, LifecycleProvider>();

    public LifecycleProvider getLifecycleProvider(ExternalContext externalContext) {
        Bundle bundle = getBundle();
        LifecycleProvider provider = providers.get(bundle);
        if (provider == null) {
            throw new IllegalStateException("No LifecycleProvider registered for application bundle: " + bundle);
        }
        return provider;
    }

    private Bundle getBundle() {
        Bundle bundle = BundleUtils.getContextBundle(false);
        if (bundle == null) {
            throw new IllegalStateException("Unable to get Bundle object associated with the context classloader");
        }
        return bundle;
    }
    
    /**
     * Register a lifecycle provider for an application classloader.  This method is intended to be called
     * by the container in which MyFaces is running, once for each application, during application startup before
     * any other myfaces initialization has taken place.
     *
     * @param cl       application classloader, used to index LifecycleProviders
     * @param provider LifecycleProvider for the application.
     */
    public void registerLifecycleProvider(Bundle bundle, LifecycleProvider provider) {
        providers.put(bundle, provider);
    }

    public void unregisterLifecycleProvider(Bundle bundle) {
        providers.remove(bundle);
    }

    public void release() {
        providers.clear();
    }

}
