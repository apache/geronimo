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

package org.apache.geronimo.deployment.plugin.factories;

import java.io.InputStream;
import java.net.URI;

import javax.enterprise.deploy.model.DeployableObject;
import javax.enterprise.deploy.spi.DeploymentConfiguration;
import javax.enterprise.deploy.spi.exceptions.InvalidModuleException;

import org.apache.geronimo.deployment.DeploymentException;
import org.apache.geronimo.deployment.DeploymentModule;
import org.apache.xmlbeans.XmlObject;
import org.apache.xmlbeans.SchemaType;
import org.apache.xmlbeans.SchemaTypeLoader;

/**
 *
 *
 * @version $Revision: 1.7 $ $Date: 2004/02/25 09:57:38 $
 */
public interface DeploymentConfigurationFactory {

    public DeploymentModule createModule(InputStream moduleArchive, XmlObject deploymentPlan, URI configID, boolean isLocal) throws DeploymentException;

    //these might be temporary
    public SchemaType getSchemaType();
    public SchemaTypeLoader getSchemaTypeLoader();

}
