/**
 *
 * Copyright 2006 The Apache Software Foundation
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

package org.apache.geronimo.deployment;

import java.util.List;

import org.apache.xmlbeans.XmlObject;
import org.apache.geronimo.common.DeploymentException;
import org.apache.geronimo.gbean.AbstractName;
import org.apache.geronimo.kernel.repository.Environment;

/**
 * @version $Rev$ $Date$
 */
public interface NamespaceDrivenBuilder {

    void buildEnvironment(XmlObject container, Environment environment) throws DeploymentException;
 
    void build(XmlObject container, DeploymentContext applicationContext, DeploymentContext moduleContext) throws DeploymentException;

    String getNamespace();

}
