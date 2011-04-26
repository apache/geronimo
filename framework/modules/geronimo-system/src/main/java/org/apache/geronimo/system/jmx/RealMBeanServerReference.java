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
package org.apache.geronimo.system.jmx;

import java.lang.management.ManagementFactory;
import java.util.ArrayList;

import javax.management.MBeanServer;
import javax.management.MBeanServerFactory;

import org.apache.geronimo.gbean.GBeanLifecycle;
import org.apache.geronimo.gbean.annotation.GBean;
import org.apache.geronimo.gbean.annotation.ParamAttribute;
import org.apache.geronimo.gbean.annotation.ParamSpecial;
import org.apache.geronimo.gbean.annotation.SpecialAttributeType;
import org.osgi.framework.BundleContext;

/**
 * Creates a real mbean server of finds an existing one with the specified mbeanServerId
 *
 * @version $Rev$ $Date$
 */
@GBean
public class RealMBeanServerReference implements MBeanServerReference, GBeanLifecycle {
    private static final String GERONIMO_DEFAULT_DOMAIN = "geronimo";

    private BundleContext bundleContext;
    private MBeanServer mbeanServer;

    public RealMBeanServerReference(@ParamSpecial(type = SpecialAttributeType.bundleContext) BundleContext bundleContext,
                                    @ParamAttribute(name="usePlatformMBeanServer") boolean usePlatformMBeanServer,
                                    @ParamAttribute(name="mbeanServerId")String mbeanServerId) throws MBeanServerNotFound {
        this.bundleContext = bundleContext;
        if (usePlatformMBeanServer) {
            mbeanServer = ManagementFactory.getPlatformMBeanServer();
        } else {
            ArrayList servers = MBeanServerFactory.findMBeanServer(mbeanServerId);
            if (servers.size() == 0) {
                mbeanServer = MBeanServerFactory.createMBeanServer(GERONIMO_DEFAULT_DOMAIN);
            } else if (servers.size() > 1) {
                throw new MBeanServerNotFound(servers.size() + " MBeanServers were found with the agent id " + mbeanServerId);
            } else {
                mbeanServer = (MBeanServer) servers.get(0);
            }
        }
    }

    /**
     * Finds an existing MBeanServer with default domain GERONIMO_DEFAULT_DOMAIN
     * or creates a new one if there isn't any.
     */
    public RealMBeanServerReference() {
        // Find all MBeanServers
        ArrayList<MBeanServer> servers = MBeanServerFactory.findMBeanServer(null);
        for (MBeanServer server : servers) {
            // Look for one with default domain GERONIMO_DEFAULT_DOMAIN
            if (GERONIMO_DEFAULT_DOMAIN.equals(server.getDefaultDomain())) {
                mbeanServer = server;
                break;
            }
        }
        if (mbeanServer == null) {
            // No MBeanServer with default domain GERONIMO_DEFAULT_DOMAIN exists. Create one.
            mbeanServer = MBeanServerFactory.createMBeanServer(GERONIMO_DEFAULT_DOMAIN);
        }
    }

    public MBeanServer getMBeanServer() {
        return mbeanServer;
    }

    public void doFail() {
    }

    public void doStart() throws Exception {
        if (mbeanServer != null) {
            bundleContext.registerService(MBeanServer.class.getName(), mbeanServer, null);
        }       
    }

    public void doStop() throws Exception {
        // TODO: unregister MBeanServer service?
    }

}
