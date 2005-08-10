/**
 *
 * Copyright 2004-2005 The Apache Software Foundation
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
import javax.management.ObjectName;
import javax.naming.Reference;

import org.apache.geronimo.common.DeploymentException;
import org.apache.geronimo.gbean.GBeanData;


/**
 * @version $Rev$ $Date$
 */
public interface EJBReferenceBuilder {

    Reference createEJBLocalReference(String objectName, GBeanData gbeanData, boolean isSession, String localHome, String local) throws DeploymentException;

    Reference createEJBRemoteReference(String objectName, GBeanData gbeanData, boolean isSession, String home, String remote) throws DeploymentException;

    Reference createCORBAReference(URI corbaURL, String objectName, ObjectName containerName, String home) throws DeploymentException;

    Object createHandleDelegateReference() throws DeploymentException;

    Reference getImplicitEJBRemoteRef(URI module, String refName, boolean isSession, String home, String remote, NamingContext context) throws DeploymentException;

    Reference getImplicitEJBLocalRef(URI module, String refName, boolean isSession, String localHome, String local, NamingContext context) throws DeploymentException;

}
