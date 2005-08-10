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
import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;

import org.apache.geronimo.common.DeploymentException;
import org.apache.geronimo.deployment.DeploymentContext;
import org.apache.geronimo.j2ee.j2eeobjectnames.J2eeContext;
import org.apache.geronimo.j2ee.j2eeobjectnames.J2eeContextImpl;
import org.apache.geronimo.j2ee.j2eeobjectnames.NameFactory;
import org.apache.geronimo.kernel.Kernel;
import org.apache.geronimo.kernel.config.ConfigurationModuleType;
import org.apache.geronimo.security.deployment.SecurityConfiguration;

/**
 * @version $Rev$ $Date$
 */
public class EARContext extends DeploymentContext implements NamingContext {
    private final ObjectName domainObjectName;
    private final ObjectName serverObjectName;
    private final ObjectName applicationObjectName;

    private final ObjectName transactionContextManagerObjectName;
    private final ObjectName connectionTrackerObjectName;

    private final ObjectName transactedTimerName;
    private final ObjectName nonTransactedTimerName;

    private final ObjectName corbaGBeanObjectName;

    private final RefContext refContext;
    private final J2eeContext j2eeContext;

    private final Map contextIDToPermissionsMap = new HashMap();
    private ObjectName jaccManagerName;
    private SecurityConfiguration securityConfiguration;

    public EARContext(File baseDir, URI id, ConfigurationModuleType moduleType, URI parentID, Kernel kernel, String j2eeApplicationName, ObjectName transactionContextManagerObjectName, ObjectName connectionTrackerObjectName, ObjectName transactedTimerName, ObjectName nonTransactedTimerName, ObjectName corbaGBeanObjectName, RefContext refContext) throws MalformedObjectNameException, DeploymentException {
        super(baseDir, id, moduleType, parentID, kernel);
        j2eeContext = new J2eeContextImpl(getDomain(), getServer(), j2eeApplicationName == null ? NameFactory.NULL : j2eeApplicationName, NameFactory.J2EE_MODULE, NameFactory.NULL, null, null);
        domainObjectName = NameFactory.getDomainName(null, j2eeContext);
        serverObjectName = NameFactory.getServerName(null, null, j2eeContext);

        if (j2eeApplicationName != null) {
            applicationObjectName = NameFactory.getApplicationName(null, null, null, j2eeContext);
        } else {
            applicationObjectName = null;
        }

        this.transactionContextManagerObjectName = transactionContextManagerObjectName;
        this.connectionTrackerObjectName = connectionTrackerObjectName;
        this.transactedTimerName = transactedTimerName;
        this.nonTransactedTimerName = nonTransactedTimerName;
        this.corbaGBeanObjectName = corbaGBeanObjectName;
        this.refContext = refContext;
    }

    public String getJ2EEDomainName() {
        return j2eeContext.getJ2eeDomainName();
    }

    public String getJ2EEServerName() {
        return j2eeContext.getJ2eeServerName();
    }

    public String getJ2EEApplicationName() {
        return j2eeContext.getJ2eeApplicationName();
    }

    public ObjectName getDomainObjectName() {
        return domainObjectName;
    }

    public ObjectName getServerObjectName() {
        return serverObjectName;
    }

    public ObjectName getApplicationObjectName() {
        return applicationObjectName;
    }

    public ObjectName getTransactionContextManagerObjectName() {
        return transactionContextManagerObjectName;
    }

    public ObjectName getConnectionTrackerObjectName() {
        return connectionTrackerObjectName;
    }

    public ObjectName getTransactedTimerName() {
        return transactedTimerName;
    }

    public ObjectName getNonTransactedTimerName() {
        return nonTransactedTimerName;
    }

    public ObjectName getCORBAGBeanObjectName() {
        return corbaGBeanObjectName;
    }

    public RefContext getRefContext() {
        return refContext;
    }

    public J2eeContext getJ2eeContext() {
        return j2eeContext;
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

    public void setJaccManagerName(ObjectName jaccManagerName) {
        this.jaccManagerName = jaccManagerName;
    }

    public ObjectName getJaccManagerName() {
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
