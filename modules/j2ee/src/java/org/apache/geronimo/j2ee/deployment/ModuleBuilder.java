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

import java.net.URI;
import java.net.URL;
import java.util.jar.JarFile;

import org.apache.geronimo.deployment.DeploymentException;
import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlObject;
import org.apache.xmlbeans.SchemaTypeLoader;

/**
 * @version $Revision: 1.2 $ $Date: 2004/07/18 22:04:27 $
 */
public interface ModuleBuilder {
    XmlObject getDeploymentPlan(URL module) throws XmlException;

    boolean canHandlePlan(XmlObject plan);

    Module createModule(String name, XmlObject plan) throws DeploymentException;

    URI getParentId(XmlObject plan) throws DeploymentException;

    URI getConfigId(XmlObject plan) throws DeploymentException;

    void installModule(JarFile earFile, EARContext earContext, Module module) throws DeploymentException;

    void initContext(EARContext earContext, Module module, ClassLoader cl) throws DeploymentException;

    void addGBeans(EARContext earContext, Module module, ClassLoader cl) throws DeploymentException;

    SchemaTypeLoader getSchemaTypeLoader();
}
