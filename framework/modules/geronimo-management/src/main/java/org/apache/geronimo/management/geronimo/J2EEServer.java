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
package org.apache.geronimo.management.geronimo;

import org.apache.geronimo.management.J2EEDeployedObject;
import org.apache.geronimo.management.J2EEResource;
import org.apache.geronimo.management.AppClientModule;
import org.apache.geronimo.management.EJBModule;
import org.apache.geronimo.kernel.repository.ListableRepository;
import org.apache.geronimo.kernel.repository.WritableListableRepository;
import org.apache.geronimo.kernel.config.ConfigurationManager;
import org.apache.geronimo.system.serverinfo.ServerInfo;
import org.apache.geronimo.system.threads.ThreadPool;

/**
 * Geronimo-specific extensions to the standard J2EE server management
 * interface.
 *
 * @version $Rev$ $Date$
 */
public interface J2EEServer extends org.apache.geronimo.management.J2EEServer {
    /**
     * A list of all of the J2EEApplication and J2EEModule types deployed on this J2EEServer.
     * @see "JSR77.3.3.1.1"
     * @return the deployed objects on this server
     */
    J2EEDeployedObject[] getDeployedObjectInstances();

    /**
     * A list of resources available to this server.
     * @see "JSR77.3.3.1.2"
     * @return the resources available to this server
     */
    J2EEResource[] getResourceInstances();

    /**
     * A list of all Java virtual machines on which this J2EEServer has running threads.
     * @see "JSR77.3.3.1.3"
     * @return the JVMs for this server
     */
    JVM[] getJavaVMInstances();

    /**
     * Gets the Web Managers associated with this J2EEServer, or null if
     * there are none in the current server configuration.
     */
    public WebManager[] getWebManagers();

    /**
     * Gets the EJB Managers associated with this J2EEServer, or null if
     * there are none in the current server configuration.
     */
    public EJBManager[] getEJBManagers();

    /**
     * Gets the JMS Managers associated with this J2EEServer, or null if
     * there are none in the current server configuration.
     */
    public JMSManager[] getJMSManagers();

    /**
     * Gets the thread pools associated with this J2EEServer.
     */
    public ThreadPool[] getThreadPools();

    /**
     * Gets the Repositories associated with this J2EEServer.
     */
    public ListableRepository[] getRepositories();

    /**
     * Gets the writable repositories associated with this J2EEServer.
     */
    public WritableListableRepository[] getWritableRepositories();

    /**
     * Gets the SecurityRealms associated with this J2EEServer.
     */
    public SecurityRealm[] getSecurityRealms();

    /**
     * Gets the ServerInfo associated with this J2EEServer.
     */
    public ServerInfo getServerInfo();

    /**
     * Gets the KeystoreManager associated with this J2EEServer.
     */
    public KeystoreManager getKeystoreManager();

    /**
     * Gets the ConfigurationManager associated with this J2EEServer.
     */
    public ConfigurationManager getConfigurationManager();

    /**
     * Gets the applications currently running in this J2EEServer.
     */
    J2EEApplication[] getApplications();

    /**
     * Gets the application clients currently running in this J2EEServer.
     */
    AppClientModule[] getAppClients();

    /**
     * Gets the web modules currently running in this J2EEServer.
     */
    WebModule[] getWebModules();

    /**
     * Gets the EJB modules currently running in this J2EEServer.
     */
    EJBModule[] getEJBModules();

    /**
     * Gets the J2EE Connector modules currently running in this J2EEServer.
     */
    ResourceAdapterModule[] getResourceAdapterModules();
}
