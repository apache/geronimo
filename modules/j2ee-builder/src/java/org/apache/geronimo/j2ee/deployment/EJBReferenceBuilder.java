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
import java.util.Set;
import javax.naming.Reference;

import org.apache.geronimo.common.DeploymentException;
import org.apache.geronimo.gbean.GBeanData;
import org.apache.geronimo.gbean.AbstractName;
import org.apache.geronimo.gbean.AbstractNameQuery;
import org.apache.geronimo.kernel.repository.Artifact;
import org.apache.geronimo.kernel.config.Configuration;
import org.apache.geronimo.kernel.GBeanNotFoundException;


/**
 * @version $Rev: 386000 $ $Date$
 */
public interface EJBReferenceBuilder {

    Reference createCORBAReference(Configuration configuration, AbstractNameQuery containerNameQuery, URI nsCorbaloc, String objectName, String home) throws DeploymentException;

    Object createHandleDelegateReference() throws DeploymentException;

    Reference createEJBRemoteRef(String refName, Configuration configuration, String name, String requiredModule, String optionalModule, Artifact targetConfigId, AbstractNameQuery query, boolean isSession, String home, String remote) throws DeploymentException;

    Reference createEJBLocalRef(String refName, Configuration configuration, String name, String requiredModule, String optionalModule, Artifact targetConfigId, AbstractNameQuery query, boolean isSession, String localHome, String local) throws DeploymentException;
}
