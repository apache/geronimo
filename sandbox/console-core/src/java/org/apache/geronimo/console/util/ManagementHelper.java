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

import org.apache.geronimo.j2ee.management.J2EEDomain;
import org.apache.geronimo.j2ee.management.J2EEServer;
import org.apache.geronimo.j2ee.management.J2EEDeployedObject;
import org.apache.geronimo.j2ee.management.J2EEResource;
import org.apache.geronimo.j2ee.management.AppClientModule;
import org.apache.geronimo.j2ee.management.WebModule;
import org.apache.geronimo.j2ee.management.EJBModule;
import org.apache.geronimo.j2ee.management.ResourceAdapterModule;
import org.apache.geronimo.j2ee.management.J2EEModule;
import org.apache.geronimo.j2ee.management.JCAResource;
import org.apache.geronimo.j2ee.management.JDBCResource;
import org.apache.geronimo.j2ee.management.JMSResource;
import org.apache.geronimo.j2ee.management.JDBCDataSource;
import org.apache.geronimo.j2ee.management.JDBCDriver;
import org.apache.geronimo.j2ee.management.JCAConnectionFactory;
import org.apache.geronimo.j2ee.management.JCAManagedConnectionFactory;
import org.apache.geronimo.j2ee.management.EJB;
import org.apache.geronimo.j2ee.management.Servlet;
import org.apache.geronimo.j2ee.management.ResourceAdapter;
import org.apache.geronimo.j2ee.management.geronimo.JVM;
import org.apache.geronimo.j2ee.management.geronimo.J2EEApplication;
import org.apache.geronimo.system.logging.SystemLog;

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
    J2EEResource[] getResources(J2EEServer server);
    JCAResource[] getJCAResources(J2EEServer server);
    JDBCResource[] getJDBCResources(J2EEServer server);
    JMSResource[] getJMSResources(J2EEServer server);
    JVM[] getJavaVMs(J2EEServer server);
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
    ResourceAdapter getResourceAdapters(ResourceAdapterModule module);

    // resource adapter properties
    JCAResource[] getRAResources(ResourceAdapter adapter);

    // resource properties
    JDBCDataSource[] getDataSource(JDBCResource resource);
    JDBCDriver[] getDriver(JDBCDataSource dataSource);
    JCAConnectionFactory[] getConnectionFactories(JCAResource resource);
    JCAManagedConnectionFactory getManagedConnectionFactory(JCAConnectionFactory factory);
}
