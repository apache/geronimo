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
package org.apache.geronimo.j2ee.deployment;

import java.io.File;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.geronimo.common.DeploymentException;
import org.apache.geronimo.deployment.DeploymentContext;
import org.apache.geronimo.deployment.ResourceContext;
import org.apache.geronimo.gbean.AbstractName;
import org.apache.geronimo.gbean.AbstractNameQuery;
import org.apache.geronimo.kernel.Naming;
import org.apache.geronimo.kernel.config.ConfigurationManager;
import org.apache.geronimo.kernel.config.ConfigurationModuleType;
import org.apache.geronimo.kernel.repository.Environment;
import org.osgi.framework.BundleContext;

/**
 * @version $Rev:386276 $ $Date$
 */
public class EARContext extends DeploymentContext {

    private final AbstractNameQuery serverName;
    private final AbstractNameQuery transactionManagerObjectName;
    private final AbstractNameQuery connectionTrackerObjectName;
    private final AbstractNameQuery corbaGBeanObjectName;

    private final Map<String, Object> contextIDToPermissionsMap = new HashMap<String, Object>();
    private final Set<String> subModuleNames = new HashSet<String>();
    private Object securityConfiguration;
    private boolean hasSecurity;

    //TODO what is the map?
    private final Map<String, Map>  messageDestinations;

    private final Map<Key,Object> generalData = new HashMap<Key,Object>();

    public EARContext(File baseDir,
                      File inPlaceConfigurationDir,
                      Environment environment,
                      ConfigurationModuleType moduleType,
                      Naming naming,
                      ConfigurationManager configurationManager,
                      BundleContext bundleContext,
                      AbstractNameQuery serverName,
                      AbstractName baseName,
                      AbstractNameQuery transactionManagerObjectName,
                      AbstractNameQuery connectionTrackerObjectName,
                      AbstractNameQuery corbaGBeanObjectName
    ) throws DeploymentException {
        this(baseDir,
             inPlaceConfigurationDir,
             environment,
             moduleType,
             naming,
             configurationManager,
             bundleContext,
             serverName,
             baseName,
             transactionManagerObjectName,
             connectionTrackerObjectName,
             corbaGBeanObjectName,
             new HashMap());
    }

    public EARContext(File baseDir,
                      File inPlaceConfigurationDir,
                      Environment environment,
                      ConfigurationModuleType moduleType,
                      Naming naming,
                      ConfigurationManager configurationManager,
                      BundleContext bundleContext,
                      AbstractNameQuery serverName,
                      AbstractName baseName,
                      AbstractNameQuery transactionManagerObjectName,
                      AbstractNameQuery connectionTrackerObjectName,
                      AbstractNameQuery corbaGBeanObjectName,
                      Map messageDestinations) throws DeploymentException {
        super(baseDir, inPlaceConfigurationDir, environment, baseName, moduleType, naming, configurationManager, bundleContext);

        this.serverName = serverName;
        this.transactionManagerObjectName = transactionManagerObjectName;
        this.connectionTrackerObjectName = connectionTrackerObjectName;
        this.corbaGBeanObjectName = corbaGBeanObjectName;
        this.messageDestinations = messageDestinations;
    }

    public EARContext(File baseDir,
                      File inPlaceConfigurationDir,
                      Environment environment,
                      ConfigurationModuleType moduleType,
                      AbstractName baseName,
                      EARContext parent) throws DeploymentException {
        this(baseDir, inPlaceConfigurationDir, environment, moduleType, baseName, parent.getTransactionManagerName(), parent.getConnectionTrackerName(), parent.getCORBAGBeanName(), parent);
    }

    public EARContext(File baseDir,
                      File inPlaceConfigurationDir,
                      Environment environment,
                      ConfigurationModuleType moduleType,
                      AbstractName baseName,
                      AbstractNameQuery transactionManagerName,
                      AbstractNameQuery connectionTrackerName,
                      AbstractNameQuery corbaGBeanName,
                      EARContext parent) throws DeploymentException {
        super(baseDir, inPlaceConfigurationDir, environment, baseName, moduleType, parent.getNaming(), parent.getConfigurationManager(), parent.getBundleContext());
        this.serverName = parent.getServerName();

        this.transactionManagerObjectName = transactionManagerName;
        this.connectionTrackerObjectName = connectionTrackerName;
        this.corbaGBeanObjectName = corbaGBeanName;
        this.messageDestinations  = new HashMap();
    }

    // For sub-classes only
    protected EARContext(File baseDir,
                         File inPlaceConfigurationDir,
                         Environment environment,
                         ConfigurationModuleType moduleType,
                         Naming naming,
                         ConfigurationManager configurationManager,
                         ResourceContext resourceContext,
                         BundleContext bundleContext,
                         AbstractNameQuery serverName,
                         AbstractName baseName,
                         AbstractNameQuery transactionManagerObjectName,
                         AbstractNameQuery connectionTrackerObjectName,
                         AbstractNameQuery corbaGBeanObjectName,
                         Map messageDestinations) throws DeploymentException {
        super(baseDir, inPlaceConfigurationDir, environment, baseName, moduleType, naming,
              configurationManager, resourceContext, bundleContext);

        this.serverName = serverName;
        this.transactionManagerObjectName = transactionManagerObjectName;
        this.connectionTrackerObjectName = connectionTrackerObjectName;
        this.corbaGBeanObjectName = corbaGBeanObjectName;
        this.messageDestinations = messageDestinations;
    }

    public AbstractNameQuery getServerName() {
        return serverName;
    }

    public AbstractNameQuery getTransactionManagerName() {
        return transactionManagerObjectName;
    }

    public AbstractNameQuery getConnectionTrackerName() {
        return connectionTrackerObjectName;
    }

    public AbstractNameQuery getCORBAGBeanName() {
        return corbaGBeanObjectName;
    }

    public Map getContextIDToPermissionsMap() {
        return contextIDToPermissionsMap;
    }

    public void addSecurityContext(String contextID, Object componentPermissions) throws DeploymentException {
        Object old = contextIDToPermissionsMap.put(contextID, componentPermissions);
        if (old != null) {
            throw new DeploymentException("Duplicate contextID registered! " + contextID);
        }
    }

    public void setSecurityConfiguration(Object securityConfiguration) throws DeploymentException {
        if (this.securityConfiguration != null) {
            throw new DeploymentException("Only one security configuration allowed per application");
        }
        this.securityConfiguration = securityConfiguration;
    }

    public Object getSecurityConfiguration() {
        return securityConfiguration;
    }

    public void registerMessageDestionations(String moduleName, Map nameMap) throws DeploymentException {
        messageDestinations.put(moduleName, nameMap);
    }

    public Map getMessageDestinations() {
        return messageDestinations;
    }

    public Map<Key,Object> getGeneralData() {
        return generalData;
    }

    public boolean isHasSecurity() {
        return hasSecurity;
    }

    public void setHasSecurity(boolean hasSecurity) {
        this.hasSecurity = hasSecurity;
    }

    public Set<String> getSubModuleNames() {
        return subModuleNames;
    }

    public static interface Key<T> {
        T get(Map<Key, Object> context);
    }

    public static final Key<Collection<String>> MODULE_LIST_KEY = new Key<Collection<String>>() {

        @Override
        public Collection<String> get(Map<Key, Object> context) {
            return (Collection<String>) context.get(this);
        }
    };

    public static final Key<AbstractName> APPLICATION_JNDI_NAME_KEY = new Key<AbstractName>() {

        @Override
        public AbstractName get(Map<Key, Object> context) {
            return (AbstractName) context.get(this);
        }
    };

    public static final Key<AbstractName> JACC_MANAGER_NAME_KEY = new Key<AbstractName>() {

        @Override
        public AbstractName get(Map<Key, Object> context) {
            return (AbstractName) context.get(this);
        }
    };

    public static final EARContext.Key<AbstractName> APPINFO_GBEAN_NAME_KEY = new Key<AbstractName>() {

        @Override
        public AbstractName get(Map<Key, Object> context) {
            return (AbstractName) context.get(this);
        }
    };

}
