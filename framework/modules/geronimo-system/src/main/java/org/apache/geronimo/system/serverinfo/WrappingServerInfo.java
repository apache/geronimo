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


package org.apache.geronimo.system.serverinfo;

import java.io.File;
import java.net.URI;

import org.apache.geronimo.gbean.annotation.GBean;
import org.apache.geronimo.gbean.annotation.ParamSpecial;
import org.apache.geronimo.gbean.annotation.SpecialAttributeType;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTracker;
import org.osgi.util.tracker.ServiceTrackerCustomizer;

/**
 * @version $Rev:$ $Date:$
 */
@GBean
public class WrappingServerInfo implements ServerInfo {

    private ServerInfo serverInfo;

    public WrappingServerInfo(@ParamSpecial(type = SpecialAttributeType.bundle)final Bundle bundle) {
        final BundleContext bundleContext = bundle.getBundleContext();
        ServiceTracker t = new ServiceTracker(bundleContext, ServerInfo.class.getName(), new ServiceTrackerCustomizer() {

            @Override
            public Object addingService(ServiceReference serviceReference) {
                serverInfo = (ServerInfo) bundleContext.getService(serviceReference);
                return null;
            }

            @Override
            public void modifiedService(ServiceReference serviceReference, Object o) {
            }

            @Override
            public void removedService(ServiceReference serviceReference, Object o) {
                serverInfo = null;
            }
        });
        t.open();
    }


    @Override
    public String getBaseDirectory() {
        return serverInfo.getBaseDirectory();
    }

    @Override
    public String getBuildDate() {
        return serverInfo.getBuildDate();
    }

    @Override
    public String getBuildTime() {
        return serverInfo.getBuildTime();
    }

    @Override
    public String getCopyright() {
        return serverInfo.getCopyright();
    }

    @Override
    public String getCurrentBaseDirectory() {
        return serverInfo.getCurrentBaseDirectory();
    }

    @Override
    public String getVersion() {
        return serverInfo.getVersion();
    }

    @Override
    public File resolve(String filename) {
        return serverInfo.resolve(filename);
    }

    @Override
    public URI resolve(URI uri) {
        return serverInfo.resolve(uri);
    }

    @Override
    public String resolvePath(String filename) {
        return serverInfo.resolvePath(filename);
    }

    @Override
    public File resolveServer(String filename) {
        return serverInfo.resolveServer(filename);
    }

    @Override
    public URI resolveServer(URI uri) {
        return serverInfo.resolveServer(uri);
    }

    @Override
    public String resolveServerPath(String filename) {
        return serverInfo.resolveServerPath(filename);
    }
}
