/**
 *
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
import java.util.Iterator;
import java.util.Map;

import javax.xml.namespace.QName;

import org.apache.geronimo.common.DeploymentException;
import org.apache.geronimo.deployment.AbstractBuilderCollection;
import org.apache.geronimo.kernel.config.Configuration;
import org.apache.geronimo.kernel.repository.Environment;
import org.apache.xmlbeans.XmlObject;

/**
 * @version $Rev$ $Date$
 */
public class NamingBuilderCollection extends AbstractBuilderCollection implements NamingBuilder {

    public NamingBuilderCollection(Collection builders, final QName basePlanElementName) {
        super(builders, basePlanElementName);
    }

    public void buildEnvironment(XmlObject specDD, XmlObject plan, Environment environment) throws DeploymentException {
        for (Iterator iterator = builders.iterator(); iterator.hasNext();) {
            NamingBuilder namingBuilder = (NamingBuilder) iterator.next();
            namingBuilder.buildEnvironment(specDD, plan, environment);
        }
    }

    public void initContext(XmlObject specDD, XmlObject plan, Configuration localConfiguration, Configuration remoteConfiguration, Module module) throws DeploymentException {
        for (Iterator iterator = builders.iterator(); iterator.hasNext();) {
            NamingBuilder namingBuilder = (NamingBuilder) iterator.next();
            namingBuilder.initContext(specDD, plan, localConfiguration, remoteConfiguration, module);
        }
    }

    public void buildNaming(XmlObject specDD, XmlObject plan, Configuration localContext, Configuration remoteContext, Module module, Map componentContext) throws DeploymentException {
        for (Iterator iterator = builders.iterator(); iterator.hasNext();) {
            NamingBuilder namingBuilder = (NamingBuilder) iterator.next();
            namingBuilder.buildNaming(specDD, plan, localContext, remoteContext, module, componentContext);
        }
    }

}
