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
package org.apache.geronimo.console.util;

import java.util.Map;
import javax.security.auth.Subject;
import javax.security.auth.login.LoginException;
import javax.security.auth.spi.LoginModule;

import org.apache.geronimo.gbean.AbstractName;
import org.apache.geronimo.gbean.GBeanData;
import org.apache.geronimo.kernel.Naming;
import org.apache.geronimo.kernel.config.ConfigurationModuleType;
import org.apache.geronimo.kernel.repository.Artifact;
import org.apache.geronimo.management.AppClientModule;
import org.apache.geronimo.management.EJB;
import org.apache.geronimo.management.EJBModule;
import org.apache.geronimo.management.J2EEDeployedObject;
import org.apache.geronimo.management.J2EEModule;
import org.apache.geronimo.management.J2EEResource;
import org.apache.geronimo.management.JDBCDataSource;
import org.apache.geronimo.management.JDBCDriver;
import org.apache.geronimo.management.JDBCResource;
import org.apache.geronimo.management.JMSResource;
import org.apache.geronimo.management.Servlet;
import org.apache.geronimo.management.geronimo.J2EEApplication;
import org.apache.geronimo.management.geronimo.J2EEDomain;
import org.apache.geronimo.management.geronimo.J2EEServer;
import org.apache.geronimo.management.geronimo.JCAAdminObject;
import org.apache.geronimo.management.geronimo.JCAConnectionFactory;
import org.apache.geronimo.management.geronimo.JCAManagedConnectionFactory;
import org.apache.geronimo.management.geronimo.JCAResource;
import org.apache.geronimo.management.geronimo.JVM;
import org.apache.geronimo.management.geronimo.ResourceAdapter;
import org.apache.geronimo.management.geronimo.ResourceAdapterModule;
import org.apache.geronimo.management.geronimo.WebModule;
import org.apache.geronimo.logging.SystemLog;

/**
 * A helper interface to navigate between management objects.  This is not
 * complete; it will be expanded as necessary.
 *
 * @version $Rev$ $Date$
 */
public interface ManagementHelper {
    // root properties
    J2EEDomain[] getDomains();

    // server properties
    J2EEApplication[] getApplications(J2EEServer server);
    AppClientModule[] getAppClients(J2EEServer server);
    WebModule[] getWebModules(J2EEServer server);
    EJBModule[] getEJBModules(J2EEServer server);
    ResourceAdapterModule[] getRAModules(J2EEServer server);
    ResourceAdapterModule[] getOutboundRAModules(J2EEServer server, String connectionFactoryInterface);
    ResourceAdapterModule[] getOutboundRAModules(J2EEServer server, String[] connectionFactoryInterfaces);
    ResourceAdapterModule[] getAdminObjectModules(J2EEServer server, String[] adminObjectInterfaces);
    JCAManagedConnectionFactory[] getOutboundFactories(J2EEServer server, String connectionFactoryInterface);
    JCAResource[] getJCAResources(J2EEServer server);
    JDBCResource[] getJDBCResources(J2EEServer server);
    JMSResource[] getJMSResources(J2EEServer server);
    JVM[] getJavaVMs(J2EEServer server);
    J2EEResource[] getResources(J2EEServer server);

    // JVM properties
    SystemLog getSystemLog(JVM jvm);

    // application properties
    J2EEModule[] getModules(J2EEApplication application);
    AppClientModule[] getAppClients(J2EEApplication application);
    WebModule[] getWebModules(J2EEApplication application);
    EJBModule[] getEJBModules(J2EEApplication application);
    ResourceAdapterModule[] getRAModules(J2EEApplication application);
    JCAResource[] getJCAResources(J2EEApplication application);
    JDBCResource[] getJDBCResources(J2EEApplication application);
    JMSResource[] getJMSResources(J2EEApplication application);

    // module properties
    EJB[] getEJBs(EJBModule module);
    Servlet[] getServlets(WebModule module);
    ResourceAdapter[] getResourceAdapters(ResourceAdapterModule module);
    JCAManagedConnectionFactory[] getOutboundFactories(ResourceAdapterModule module);
    JCAManagedConnectionFactory[] getOutboundFactories(ResourceAdapterModule module, String connectionFactoryInterface);
    JCAManagedConnectionFactory[] getOutboundFactories(ResourceAdapterModule module, String[] connectionFactoryInterfaces);
    //todo: create an interface for admin objects
    JCAAdminObject[] getAdminObjects(ResourceAdapterModule module, String[] adminObjectInterfaces);

    // resource adapter properties
    JCAResource[] getRAResources(ResourceAdapter adapter);

    // resource properties
    JDBCDataSource[] getDataSource(JDBCResource resource);
    JDBCDriver[] getDriver(JDBCDataSource dataSource);
    JCAConnectionFactory[] getConnectionFactories(JCAResource resource);
    JCAManagedConnectionFactory getManagedConnectionFactory(JCAConnectionFactory factory);

    // Generic utility methods
    Object getObject(AbstractName abstractName);
    Artifact getConfigurationNameFor(AbstractName abstractName);
    String getGBeanDescription(AbstractName abstractName);

    // Misc
    void testLoginModule(J2EEServer server, LoginModule module, Map options);
    Subject testLoginModule(J2EEServer server, LoginModule module, Map options, String username, String password) throws LoginException;
    Object[] findByInterface(Class iface);
    AbstractName getNameFor(Object component);
    ConfigurationData[] getConfigurations(ConfigurationModuleType type, boolean includeChildModules);
    /**
     * Gets a JSR-77 Module (WebModule, EJBModule, etc.) for the specified configuration.
     * Note: this only works if the configuration is running at the time you ask.
     *
     * @return The Module, or null if the configuration is not running.
     */
    J2EEDeployedObject getModuleForConfiguration(Artifact configuration);

    /**
     * Adds a new GBean to an existing Configuration.
     * @param configID  The configuration to add the GBean to.
     * @param gbean     The data representing the GBean to add.
     * @param start     If true, the GBean should be started as part of this call.
     */
    public void addGBeanToConfiguration(Artifact configID, GBeanData gbean, boolean start);

    /**
     * This method returns the Naming object of the kernel.
     */
    public Naming getNaming();

    Object[] getGBeansImplementing(Class iface);
}
