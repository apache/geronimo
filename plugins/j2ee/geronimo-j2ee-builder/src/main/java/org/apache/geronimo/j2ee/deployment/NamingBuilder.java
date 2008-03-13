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
import org.apache.geronimo.kernel.repository.Environment;
import org.apache.xmlbeans.XmlObject;

/**
 * @version $Rev$ $Date$
 */
public interface NamingBuilder extends AbstractNamespaceBuilder {

    int NORMAL_PRIORITY = 50;
    
    XmlObject[] NO_REFS = new XmlObject[] {};
    String ENV = "env/";

    Key<Map<String, Object>> JNDI_KEY = new Key<Map<String, Object>>() {

        public Map<String, Object> get(Map context) {
            Map<String, Object> result = (Map<String, Object>) context.get(this);
            if (result == null) {
                result = new HashMap<String, Object>();
                context.put(this, result);
            }
            return result;
        }
    };
    Key<Holder> INJECTION_KEY = new Key<Holder>() {

        public Holder get(Map context) {
            Holder result = (Holder) context.get(this);
            if (result == null) {
                result = new Holder();
                context.put(this, result);
            }
            return result;
        }
    };
    Key<AbstractName> GBEAN_NAME_KEY = new Key<AbstractName>() {

        public AbstractName get(Map context) {
            return (AbstractName) context.get(this);
        }
    };

    void buildEnvironment(XmlObject specDD, XmlObject plan, Environment environment) throws DeploymentException;

    void initContext(XmlObject specDD, XmlObject plan, Module module) throws DeploymentException;
    
    void buildNaming(XmlObject specDD, XmlObject plan, Module module, Map componentContext) throws DeploymentException;

    /**
     * Returns sort order priority.  Lower numbers indicate higher priority.
     */
    int getPriority();
    
    public interface Key<T> {
        T get(Map context);
    }



}
