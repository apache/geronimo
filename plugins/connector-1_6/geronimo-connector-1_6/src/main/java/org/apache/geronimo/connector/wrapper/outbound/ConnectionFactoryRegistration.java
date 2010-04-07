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
package org.apache.geronimo.connector.wrapper.outbound;

import java.util.Hashtable;

import javax.resource.ResourceException;

import org.apache.geronimo.connector.outbound.GenericConnectionManager;
import org.apache.geronimo.gbean.AbstractName;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceException;
import org.osgi.framework.ServiceFactory;
import org.osgi.framework.ServiceRegistration;

/**
 * @version $Revision$
 */
public class ConnectionFactoryRegistration {
    
    public static final String OSGI_JNDI_SERVICE_NAME = "osgi.jndi.service.name";
    
    private final GenericConnectionManager connectionManager;
    private final BundleContext bundleContext;
    private final String jndiName;
    private final String[] connectionInterfaces;
    private ServiceRegistration serviceRegistration;

    public ConnectionFactoryRegistration(GenericConnectionManager connectionManager,
                                         BundleContext bundleContext,
                                         AbstractName abstractName,                                         
                                         String jndiName,
                                         String [] connectionInterfaces) {    
        this.connectionManager = connectionManager;
        this.bundleContext = bundleContext;      
        this.connectionInterfaces = connectionInterfaces;
        
        if (jndiName == null && abstractName != null) {
            this.jndiName = abstractName.getArtifact().getGroupId() + "/" + 
                            abstractName.getArtifact().getArtifactId() + "/" + 
                            abstractName.getNameProperty("j2eeType") + "/" + 
                            abstractName.getNameProperty("name");
        } else {
            this.jndiName = jndiName;
        }
    }    

    public void register() {                
        Hashtable properties = new Hashtable();
        if (jndiName != null) {
            properties.put(OSGI_JNDI_SERVICE_NAME, jndiName);
        }
        // register ServiceFactory so that each bundle gets its own instance of the connection factory
        serviceRegistration = bundleContext.registerService(connectionInterfaces, new ConnectionFactoryService(), properties);
    }

    private class ConnectionFactoryService implements ServiceFactory {

        public Object getService(Bundle bundle, ServiceRegistration registration) {
            try {
                return connectionManager.createConnectionFactory();
            } catch (ResourceException e) {
                throw new ServiceException("Error creating connection factory", e);
            }
        }

        public void ungetService(Bundle bundle, ServiceRegistration registration, Object service) {
        }
        
    }
    
    public void unregister() throws Exception {
        if (serviceRegistration != null) {
            serviceRegistration.unregister();
        }
    }

}
