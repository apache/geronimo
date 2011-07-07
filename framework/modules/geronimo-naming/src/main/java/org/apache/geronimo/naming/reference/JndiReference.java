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
package org.apache.geronimo.naming.reference;

import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.apache.geronimo.gbean.AbstractName;
import org.apache.geronimo.kernel.GBeanNotFoundException;
import org.apache.geronimo.kernel.Kernel;
import org.apache.geronimo.kernel.config.ConfigurationManager;
import org.apache.geronimo.kernel.config.ConfigurationUtil;
import org.apache.geronimo.kernel.repository.MissingDependencyException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @version $Rev$ $Date$
 */
public class JndiReference extends SimpleReference implements KernelAwareReference{

    private static final Logger logger = LoggerFactory.getLogger(JndiReference.class);
    private String jndiName;
    private AbstractName gbeanName;
    private String prefix;

    public JndiReference(String jndiName) {
        this.jndiName = jndiName;
    }

    public JndiReference(String prefix, AbstractName gbeanName) {
        this.gbeanName = gbeanName;
        this.prefix = prefix;
    }

    @Override
    public Object getContent() throws NamingException {
        InitialContext ctx = new InitialContext();
        return ctx.lookup(jndiName);
    }

    @Override
    public void setKernel(Kernel kernel) {
        if(jndiName == null) {
            try {
                ConfigurationManager configurationManager = ConfigurationUtil.getConfigurationManager(kernel);
                AbstractName mappedAbstractName = new AbstractName(configurationManager.getArtifactResolver().resolveInClassLoader(gbeanName.getArtifact()), gbeanName.getName());
                String osgiJndiName = kernel.getNaming().toOsgiJndiName(mappedAbstractName);
                jndiName = prefix + osgiJndiName;
            } catch (GBeanNotFoundException e) {
                logger.error("Fail to build the jndi name for " + gbeanName, e);
            } catch (MissingDependencyException e) {
                logger.error("Fail to build the jndi name for " + gbeanName, e);
            }
        }
    }
}
