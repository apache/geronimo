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


package org.apache.geronimo.myfaces.config.resource.osgi;

import java.net.URL;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.apache.geronimo.myfaces.config.resource.osgi.api.ConfigRegistry;
import org.osgi.framework.Bundle;
import org.osgi.service.log.LogService;

/**
 * @version $Rev:$ $Date:$
 */
public class ConfigRegistryImpl implements ConfigRegistry {
    private final Activator activator;

    private final Set<URL> urls = new HashSet<URL>();

    public ConfigRegistryImpl(Activator activator) {
        this.activator = activator;
    }

    public Object addBundle(Bundle bundle) {
        log(LogService.LOG_DEBUG, "examining bundle for META-INF/faces-config.xml " + bundle.getSymbolicName());
        URL url = bundle.getEntry("META-INF/faces-config.xml");
        if (url != null) {
            log(LogService.LOG_DEBUG, "found META-INF/faces-config.xml");
            urls.add(url);
        }
        return url;
    }

    public void removeBundle(Bundle bundle, Object object) {
        log(LogService.LOG_DEBUG, "unregistering bundle for META-INF/faces-config.xml " + bundle.getSymbolicName() + " url: " + object);
        if (object != null) {
            urls.remove((URL)object);
        }
    }

    @Override
    public Set<URL> getRegisteredConfigUrls() {
        return Collections.unmodifiableSet(urls);
    }

    private void log(int level, String message) {
        activator.log(level, message);
    }

    private void log(int level, String message, Throwable th) {
        activator.log(level, message, th);
    }


}
