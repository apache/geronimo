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
package org.apache.geronimo.management.geronimo;

import org.apache.geronimo.management.J2EEDeployedObject;
import org.apache.geronimo.management.J2EEResource;
import org.apache.geronimo.kernel.repository.Repository;
import org.apache.geronimo.kernel.config.ConfigurationManager;
import org.apache.geronimo.system.serverinfo.ServerInfo;
import org.apache.geronimo.system.configuration.ConfigurationInstaller;
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
     * Gets the ObjectNames of the thread pools associated with this
     * J2EEServer.
     *
     * @return The ObjectNames of the thread pools, in String form.
     */
    public ThreadPool[] getThreadPools();

    /**
     * Gets the Repositories associated with this J2EEServer.
     */
    public Repository[] getRepositories();

    /**
     * Gets the SecurityRealms associated with this J2EEServer.
     *
     * @see org.apache.geronimo.security.realm.SecurityRealm
     *
     * @return The ObjectNames of the realms, in String form.
     */
    public SecurityRealm[] getSecurityRealms();

    /**
     * Gets the ServerInfo associated with this J2EEServer.
     */
    public ServerInfo getServerInfo();

    /**
     * Gets the ObjectName of the LoginService associated with this
     * J2EEServer.
     *
     * @see org.apache.geronimo.security.jaas.server.JaasLoginServiceMBean
     *
     * @return The ObjectName of the LoginService, in String form.
     */
    public LoginService getLoginService();

    /**
     * Gets the ObjectName of the KeystoreManager associated with this
     * J2EEServer.
     *
     * @see org.apache.geronimo.security.keystore.FileKeystoreManager
     *
     * @return The ObjectName of the KeystoreManager, in String form.
     */
    public KeystoreManager getKeystoreManager();

    /**
     * Gets the ConfigurationInstaller associated with this J2EEServer.
     */
    public ConfigurationInstaller getConfigurationInstaller();

    /**
     * Gets the ConfigurationManager associated with this J2EEServer.
     */
    public ConfigurationManager getConfigurationManager();
}
