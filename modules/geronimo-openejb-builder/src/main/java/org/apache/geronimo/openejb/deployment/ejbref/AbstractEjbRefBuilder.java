/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.geronimo.openejb.deployment.ejbref;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import javax.naming.Reference;

import org.apache.geronimo.common.DeploymentException;
import org.apache.geronimo.common.UnresolvedEJBRefException;
import org.apache.geronimo.gbean.AbstractName;
import org.apache.geronimo.gbean.AbstractNameQuery;
import org.apache.geronimo.gbean.GBeanData;
import org.apache.geronimo.j2ee.j2eeobjectnames.NameFactory;
import org.apache.geronimo.j2ee.deployment.Module;
import org.apache.geronimo.kernel.GBeanNotFoundException;
import org.apache.geronimo.kernel.config.Configuration;
import org.apache.geronimo.kernel.repository.Artifact;
import org.apache.geronimo.kernel.repository.Environment;
import org.apache.geronimo.naming.deployment.AbstractNamingBuilder;
import org.apache.geronimo.openejb.EjbReference;
import org.apache.geronimo.openejb.EjbDeployment;
import org.apache.geronimo.openejb.deployment.EjbInterface;
import org.apache.geronimo.openejb.deployment.EjbModuleBuilder;
import org.apache.openejb.jee.JndiConsumer;
import org.apache.openejb.alt.config.JndiEncInfoBuilder;
import org.apache.openejb.assembler.classic.JndiEncInfo;
import org.apache.openejb.assembler.classic.JndiEncBuilder;
import org.apache.openejb.OpenEJBException;

/**
 * @version $Rev: 451417 $ $Date: 2006-09-29 13:13:22 -0700 (Fri, 29 Sep 2006) $
 */
public abstract class AbstractEjbRefBuilder extends AbstractNamingBuilder {

    protected AbstractEjbRefBuilder(Environment defaultEnvironment) {
        super(defaultEnvironment);
    }

    protected void bindContext(Module module, JndiConsumer jndiConsumer, Map componentContext) throws DeploymentException {
        Map<String, Object> map = null;
        try {
            EjbModuleBuilder.EarData earData = (EjbModuleBuilder.EarData) module.getEarContext().getGeneralData().get(EjbModuleBuilder.EarData.class);
            JndiEncInfoBuilder jndiEncInfoBuilder = new JndiEncInfoBuilder(earData.getEjbJars());
            JndiEncInfo jndiEncInfo = jndiEncInfoBuilder.build(jndiConsumer, "GeronimoEnc");
            JndiEncBuilder jndiEncBuilder = new JndiEncBuilder(jndiEncInfo);
            map = jndiEncBuilder.buildMap();
        } catch (OpenEJBException e) {
            throw new DeploymentException(e);
        }

        for (Map.Entry<String, Object> entry : map.entrySet()) {
            String name = entry.getKey();
            Object value = entry.getValue();

            // work with names prefixed with java:comp/
            if (name.startsWith("java:comp/")) {
                name = name.substring("java:comp/".length());
            }

            // if this is a ref it will be prefixed with env/
            if (name.startsWith("env/")) {
                getJndiContextMap(componentContext).put(name, wrapReference(value));
            }
        }
    }

    // this method exists so client refs can be made remote
    protected Object wrapReference(Object value) {
        return value;
    }
}
