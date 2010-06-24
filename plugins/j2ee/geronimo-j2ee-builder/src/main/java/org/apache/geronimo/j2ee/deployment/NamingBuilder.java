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

import java.util.HashMap;
import java.util.Map;

import org.apache.geronimo.common.DeploymentException;
import org.apache.geronimo.deployment.AbstractNamespaceBuilder;
import org.apache.geronimo.gbean.AbstractName;
import org.apache.geronimo.j2ee.annotation.Holder;
import org.apache.geronimo.j2ee.jndi.JndiKey;
import org.apache.geronimo.kernel.repository.Environment;
import org.apache.openejb.jee.JndiConsumer;
import org.apache.xmlbeans.XmlObject;

/**
 * @version $Rev$ $Date$
 */
public interface NamingBuilder extends AbstractNamespaceBuilder {

    int NORMAL_PRIORITY = 50;
    
    XmlObject[] NO_REFS = new XmlObject[] {};

    EARContext.Key<Holder> INJECTION_KEY = new EARContext.Key<Holder>() {

        public Holder get(Map<EARContext.Key, Object> context) {
            Holder result = (Holder) context.get(this);
            if (result == null) {
                result = new Holder();
                context.put(this, result);
            }
            return result;
        }
    };
    EARContext.Key<AbstractName> GBEAN_NAME_KEY = new EARContext.Key<AbstractName>() {

        public AbstractName get(Map<EARContext.Key, Object> context) {
            return (AbstractName) context.get(this);
        }
    };

    void buildEnvironment(JndiConsumer specDD, XmlObject plan, Environment environment) throws DeploymentException;

    void initContext(JndiConsumer specDD, XmlObject plan, Module module) throws DeploymentException;
    
    void buildNaming(JndiConsumer specDD, XmlObject plan, Module module, Map<EARContext.Key, Object> sharedContext) throws DeploymentException;

    /**
     * Returns sort order priority.  Lower numbers indicate higher priority.
     */
    int getPriority();

}
