/**
 *
 * Copyright 2005 The Apache Software Foundation
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

import java.net.URI;
import javax.naming.Reference;
import javax.management.ObjectName;

import org.apache.geronimo.common.DeploymentException;
import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.gbean.GBeanInfoBuilder;
import org.apache.geronimo.gbean.GBeanData;
import org.apache.geronimo.j2ee.j2eeobjectnames.NameFactory;

/**
 * @version $Rev:  $ $Date:  $
 */
public class UnavailableEJBReferenceBuilder implements EJBReferenceBuilder {

    public Reference createEJBLocalReference(String objectName, GBeanData gbeanData, boolean isSession, String localHome, String local) throws DeploymentException {
        throw new DeploymentException("EJB references are unavailable in this configuration");
    }

    public Reference createEJBRemoteReference(String objectName, GBeanData gbeanData, boolean isSession, String home, String remote) throws DeploymentException {
        throw new DeploymentException("EJB references are unavailable in this configuration");
    }

    public Reference createCORBAReference(URI corbaURL, String objectName, ObjectName containerName, String home) throws DeploymentException {
        throw new DeploymentException("EJB references are unavailable in this configuration");
    }

    public Object createHandleDelegateReference() throws DeploymentException {
        throw new DeploymentException("EJB references are unavailable in this configuration");
    }

    public Reference getImplicitEJBRemoteRef(URI module, String refName, boolean isSession, String home, String remote, NamingContext context) throws DeploymentException {
        throw new DeploymentException("EJB references are unavailable in this configuration");
    }

    public Reference getImplicitEJBLocalRef(URI module, String refName, boolean isSession, String localHome, String local, NamingContext context) throws DeploymentException {
        throw new DeploymentException("EJB references are unavailable in this configuration");
    }

    public static final GBeanInfo GBEAN_INFO;

    static {
        GBeanInfoBuilder infoBuilder = GBeanInfoBuilder.createStatic(UnavailableEJBReferenceBuilder.class, NameFactory.MODULE_BUILDER);
        infoBuilder.addInterface(EJBReferenceBuilder.class);
        GBEAN_INFO = infoBuilder.getBeanInfo();
    }

    public static GBeanInfo getGBeanInfo() {
        return GBEAN_INFO;
    }
}
