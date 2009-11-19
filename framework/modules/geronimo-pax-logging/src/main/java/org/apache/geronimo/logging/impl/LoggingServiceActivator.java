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

package org.apache.geronimo.logging.impl;

import org.apache.geronimo.logging.SystemLog;
import org.apache.geronimo.main.ServerInfo;
import org.ops4j.pax.logging.service.internal.Activator;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.service.cm.Configuration;
import org.osgi.service.cm.ConfigurationAdmin;

public class LoggingServiceActivator implements BundleActivator {

    private Activator activator;
    private Log4jService service;
    
    public LoggingServiceActivator() {
        activator = new Activator();       
    }
    
    public void start(BundleContext context) throws Exception {
        String config = System.getProperty(Log4jService.LOG4JSERVICE_CONFIG_PROPERTY);
        if (config != null) {
            ConfigurationAdmin configAdmin = getService(context, ConfigurationAdmin.class);
            if (configAdmin != null) {
                ServerInfo serverInfo = getService(context, ServerInfo.class);
                Configuration configuration = configAdmin.getConfiguration("org.ops4j.pax.logging");                
                service = new OSGiLog4jService(config, 60, serverInfo, configuration);
                service.start();
                
                context.registerService(SystemLog.class.getName(), service, null);
            }
        }
        
        activator.start(context);
    }

    private <T> T getService(BundleContext context, Class<T> name) {
        ServiceReference ref = context.getServiceReference(name.getName());
        return (ref == null) ? null : (T) context.getService(ref);
    }
    
    public void stop(BundleContext context) throws Exception {
        if (service != null) {
            service.stop();
        }    
        activator.stop(context);       
    }
   
}
