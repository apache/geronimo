/**
 *
 * Copyright 2004 The Apache Software Foundation
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
package org.apache.geronimo.j2ee.deployment;

import java.io.File;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import javax.management.MalformedObjectNameException;

import org.apache.geronimo.common.DeploymentException;
import org.apache.geronimo.deployment.DeploymentContext;
import org.apache.geronimo.gbean.AbstractName;
import org.apache.geronimo.gbean.AbstractNameQuery;
import org.apache.geronimo.j2ee.j2eeobjectnames.NameFactory;
import org.apache.geronimo.kernel.Kernel;
import org.apache.geronimo.kernel.config.ConfigurationModuleType;
import org.apache.geronimo.kernel.repository.Environment;
import org.apache.geronimo.security.deployment.SecurityConfiguration;

/**
 * @version $Rev$ $Date$
 */
public class EARContext extends DeploymentContext implements NamingContext {
    private final AbstractName domainObjectName;
    private final AbstractName serverObjectName;
    private final AbstractName applicationName;

    private final AbstractNameQuery transactionContextManagerObjectName;
    private final AbstractNameQuery connectionTrackerObjectName;

    private final AbstractNameQuery transactedTimerName;
    private final AbstractNameQuery nonTransactedTimerName;

    private final AbstractNameQuery corbaGBeanObjectName;

    private final RefContext refContext;
    private final AbstractName moduleName;

    private final Map contextIDToPermissionsMap = new HashMap();
    private AbstractName jaccManagerName;
    private SecurityConfiguration securityConfiguration;

    public EARContext(File baseDir, Environment environment, ConfigurationModuleType moduleType, Kernel kernel, AbstractName baseName, AbstractNameQuery transactionContextManagerObjectName, AbstractNameQuery connectionTrackerObjectName, AbstractNameQuery transactedTimerName, AbstractNameQuery nonTransactedTimerName, AbstractNameQuery corbaGBeanObjectName, RefContext refContext) throws MalformedObjectNameException, DeploymentException {
        super(baseDir, environment, moduleType, kernel);
            moduleName = baseName;
            applicationName = moduleName;
        //TODO configId FIXME
        domainObjectName = null;//NameFactory.getDomainName(null, moduleName);
        serverObjectName = null;//NameFactory.getServerName(null, null, moduleName);

        this.transactionContextManagerObjectName = transactionContextManagerObjectName;
        this.connectionTrackerObjectName = connectionTrackerObjectName;
        this.transactedTimerName = transactedTimerName;
        this.nonTransactedTimerName = nonTransactedTimerName;
        this.corbaGBeanObjectName = corbaGBeanObjectName;
        this.refContext = refContext;
    }

    public String getJ2EEDomainName() {
        return moduleName.getObjectName().getKeyProperty(NameFactory.J2EE_DOMAIN);
    }

    public String getJ2EEServerName() {
        return moduleName.getObjectName().getKeyProperty(NameFactory.J2EE_SERVER);
    }

    public String getJ2EEApplicationName() {
        return moduleName.getObjectName().getKeyProperty(NameFactory.J2EE_APPLICATION);
    }

    public AbstractName getDomainObjectName() {
        return domainObjectName;
    }

    public AbstractName getServerObjectName() {
        return serverObjectName;
    }

    public AbstractName getApplicationName() {
        return applicationName;
    }

    public AbstractNameQuery getTransactionContextManagerObjectName() {
        return transactionContextManagerObjectName;
    }

    public AbstractNameQuery getConnectionTrackerObjectName() {
        return connectionTrackerObjectName;
    }

    public AbstractNameQuery getTransactedTimerName() {
        return transactedTimerName;
    }

    public AbstractNameQuery getNonTransactedTimerName() {
        return nonTransactedTimerName;
    }

    public AbstractNameQuery getCORBAGBeanObjectName() {
        return corbaGBeanObjectName;
    }

    public RefContext getRefContext() {
        return refContext;
    }

    public AbstractName getModuleName() {
        return moduleName;
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

    public void setJaccManagerName(AbstractName jaccManagerName) {
        this.jaccManagerName = jaccManagerName;
    }

    public AbstractName getJaccManagerName() {
        return jaccManagerName;
    }

    public void setSecurityConfiguration(SecurityConfiguration securityConfiguration) throws DeploymentException {
        if (this.securityConfiguration != null) {
            throw new DeploymentException("Only one security configuration allowed per application");
        }
        this.securityConfiguration = securityConfiguration;
    }

    public SecurityConfiguration getSecurityConfiguration() {
        return securityConfiguration;
    }
}
