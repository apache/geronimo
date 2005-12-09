/**
 *
 * Copyright 2003-2004 The Apache Software Foundation
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
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

import org.apache.geronimo.kernel.repository.Repository;
import org.apache.geronimo.management.AppClientModule;
import org.apache.geronimo.management.EJB;
import org.apache.geronimo.management.EJBModule;
import org.apache.geronimo.management.J2EEDeployedObject;
import org.apache.geronimo.management.J2EEDomain;
import org.apache.geronimo.management.J2EEModule;
import org.apache.geronimo.management.J2EEResource;
import org.apache.geronimo.management.JCAConnectionFactory;
import org.apache.geronimo.management.JCAResource;
import org.apache.geronimo.management.JDBCDataSource;
import org.apache.geronimo.management.JDBCDriver;
import org.apache.geronimo.management.JDBCResource;
import org.apache.geronimo.management.JMSResource;
import org.apache.geronimo.management.ResourceAdapter;
import org.apache.geronimo.management.Servlet;
import org.apache.geronimo.management.WebModule;
import org.apache.geronimo.management.geronimo.EJBConnector;
import org.apache.geronimo.management.geronimo.EJBManager;
import org.apache.geronimo.management.geronimo.J2EEApplication;
import org.apache.geronimo.management.geronimo.J2EEServer;
import org.apache.geronimo.management.geronimo.JCAManagedConnectionFactory;
import org.apache.geronimo.management.geronimo.JMSBroker;
import org.apache.geronimo.management.geronimo.JMSConnector;
import org.apache.geronimo.management.geronimo.JMSManager;
import org.apache.geronimo.management.geronimo.JVM;
import org.apache.geronimo.management.geronimo.ResourceAdapterModule;
import org.apache.geronimo.management.geronimo.WebAccessLog;
import org.apache.geronimo.management.geronimo.WebConnector;
import org.apache.geronimo.management.geronimo.WebContainer;
import org.apache.geronimo.management.geronimo.WebManager;
import org.apache.geronimo.pool.GeronimoExecutor;
import org.apache.geronimo.security.jaas.server.JaasLoginServiceMBean;
import org.apache.geronimo.security.realm.SecurityRealm;
import org.apache.geronimo.system.logging.SystemLog;
import org.apache.geronimo.system.serverinfo.ServerInfo;

import javax.security.auth.Subject;
import javax.security.auth.login.LoginException;
import javax.security.auth.spi.LoginModule;
import java.util.Map;

/**
 * A helper interface to navigate between management objects.  This is not
 * complete; it will be expanded as necessary.
 *
 * @version $Rev: 46019 $ $Date: 2004-09-14 05:56:06 -0400 (Tue, 14 Sep 2004) $
 */
public interface ManagementHelper {
    // root properties
    J2EEDomain[] getDomains();

    // domain properties
    J2EEServer[] getServers(J2EEDomain domain);
    // todo: security realm

    // server properties
    J2EEDeployedObject[] getDeployedObjects(J2EEServer server);
    J2EEApplication[] getApplications(J2EEServer server);
    AppClientModule[] getAppClients(J2EEServer server);
    WebModule[] getWebModules(J2EEServer server);
    EJBModule[] getEJBModules(J2EEServer server);
    ResourceAdapterModule[] getRAModules(J2EEServer server);
    ResourceAdapterModule[] getOutboundRAModules(J2EEServer server, String connectionFactoryInterface);
    JCAManagedConnectionFactory[] getOutboundFactories(J2EEServer server, String connectionFactoryInterface);
    J2EEResource[] getResources(J2EEServer server);
    JCAResource[] getJCAResources(J2EEServer server);
    JDBCResource[] getJDBCResources(J2EEServer server);
    JMSResource[] getJMSResources(J2EEServer server);
    JVM[] getJavaVMs(J2EEServer server);
    Repository[] getRepositories(J2EEServer server);
    SecurityRealm[] getSecurityRealms(J2EEServer server);
    ServerInfo getServerInfo(J2EEServer server);
    JaasLoginServiceMBean getLoginService(J2EEServer server);
    WebManager[] getWebManagers(J2EEServer server);
    WebAccessLog getWebAccessLog(WebManager manager, WebContainer container);
    WebAccessLog getWebAccessLog(WebManager manager, String containerObjectName);
    WebContainer[] getWebContainers(WebManager manager);
    WebConnector[] getWebConnectorsForContainer(WebManager manager, WebContainer container, String protocol);
    WebConnector[] getWebConnectorsForContainer(WebManager manager, WebContainer container);
    WebConnector[] getWebConnectorsForContainer(WebManager manager, String containerObjectName, String protocol);
    WebConnector[] getWebConnectorsForContainer(WebManager manager, String containerObjectName);
    WebConnector[] getWebConnectors(WebManager manager, String protocol);
    WebConnector[] getWebConnectors(WebManager manager);
    EJBManager[] getEJBManagers(J2EEServer server);
//todo    EJBContainer[] getEJBContainers(EJBManager manager);
//todo    EJBConnector[] getEJBConnectors(EJBManager manager, EJBContainer container, String protocol);
//todo    EJBConnector[] getEJBConnectors(EJBManager manager, EJBContainer container);
    EJBConnector[] getEJBConnectors(EJBManager container, String protocol);
    EJBConnector[] getEJBConnectors(EJBManager container);
    JMSManager[] getJMSManagers(J2EEServer server);
    JMSBroker[] getJMSBrokers(JMSManager manager);
    JMSConnector[] getJMSConnectors(JMSManager manager, String protocol);
    JMSConnector[] getJMSConnectors(JMSManager manager);
    JMSConnector[] getJMSConnectorsForContainer(JMSManager manager, JMSBroker broker, String protocol);
    JMSConnector[] getJMSConnectorsForContainer(JMSManager manager, JMSBroker broker);
    JMSConnector[] getJMSConnectorsForContainer(JMSManager manager, String brokerObjectName, String protocol);
    JMSConnector[] getJMSConnectorsForContainer(JMSManager manager, String brokerObjectName);
    GeronimoExecutor[] getThreadPools(J2EEServer server);
    //todo: repository, embedded database

    // JVM properties
    SystemLog getSystemLog(JVM jvm);

    // application properties
    J2EEModule[] getModules(J2EEApplication application);
    AppClientModule[] getAppClients(J2EEApplication application);
    WebModule[] getWebModules(J2EEApplication application);
    EJBModule[] getEJBModules(J2EEApplication application);
    ResourceAdapterModule[] getRAModules(J2EEApplication application);
    J2EEResource[] getResources(J2EEApplication application);
    JCAResource[] getJCAResources(J2EEApplication application);
    JDBCResource[] getJDBCResources(J2EEApplication application);
    JMSResource[] getJMSResources(J2EEApplication application);

    // module properties
    EJB[] getEJBs(EJBModule module);
    Servlet[] getServlets(WebModule module);
    ResourceAdapter[] getResourceAdapters(ResourceAdapterModule module);
    JCAManagedConnectionFactory[] getOutboundFactories(ResourceAdapterModule module);
    JCAManagedConnectionFactory[] getOutboundFactories(ResourceAdapterModule module, String connectionFactoryInterface);

    // resource adapter properties
    JCAResource[] getRAResources(ResourceAdapter adapter);

    // resource properties
    JDBCDataSource[] getDataSource(JDBCResource resource);
    JDBCDriver[] getDriver(JDBCDataSource dataSource);
    JCAConnectionFactory[] getConnectionFactories(JCAResource resource);
    JCAManagedConnectionFactory getManagedConnectionFactory(JCAConnectionFactory factory);

    // Generic utility methods
    Object getObject(String objectName);
    String getGBeanDescription(String objectName);

    // Misc
    void testLoginModule(J2EEServer server, LoginModule module, Map options);
    Subject testLoginModule(J2EEServer server, LoginModule module, Map options, String username, String password) throws LoginException;
    Object[] findByInterface(Class iface);
}
