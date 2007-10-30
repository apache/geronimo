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

import java.util.Collection;
import java.util.Map;

import javax.xml.namespace.QName;

import org.apache.geronimo.common.DeploymentException;
import org.apache.geronimo.deployment.AbstractBuilderCollection;
import org.apache.geronimo.kernel.repository.Environment;
import org.apache.xmlbeans.XmlObject;

/**
 * @version $Rev$ $Date$
 */
public class NamingBuilderCollection extends AbstractBuilderCollection<NamingBuilder> implements NamingBuilder {

    public NamingBuilderCollection(Collection<NamingBuilder> builders, final QName basePlanElementName) {
        super(builders, basePlanElementName);
    }

    public void buildEnvironment(XmlObject specDD, XmlObject plan, Environment environment) throws DeploymentException {
        for (NamingBuilder namingBuilder : builders) {
            namingBuilder.buildEnvironment(specDD, plan, environment);
        }
    }

    public void initContext(XmlObject specDD, XmlObject plan, Module module) throws DeploymentException {
        for (NamingBuilder namingBuilder : builders) {
            namingBuilder.initContext(specDD, plan, module);
        }
    }

    public void buildNaming(XmlObject specDD, XmlObject plan, Module module, Map componentContext) throws DeploymentException {
        for (NamingBuilder namingBuilder : builders) {
            namingBuilder.buildNaming(specDD, plan, module, componentContext);
        }
    }

}
