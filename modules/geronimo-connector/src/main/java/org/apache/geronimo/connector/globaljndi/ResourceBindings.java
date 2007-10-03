/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */


package org.apache.geronimo.connector.globaljndi;

import org.apache.geronimo.connector.outbound.ConnectionFactorySource;
import org.apache.geronimo.gbean.AbstractName;
import org.apache.geronimo.gbean.AbstractNameQuery;
import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.gbean.GBeanInfoBuilder;
import org.apache.geronimo.gbean.GBeanLifecycle;
import org.apache.geronimo.gjndi.KernelContextGBean;
import org.apache.geronimo.kernel.Kernel;

import javax.naming.Name;
import javax.naming.NameParser;
import javax.naming.NamingException;
import javax.resource.ResourceException;

import java.util.Collections;
import java.util.Map;

/**
 * @version $Rev$ $Date$
 */
public class ResourceBindings extends KernelContextGBean implements GBeanLifecycle {
    private final Class type;

    public ResourceBindings(Kernel kernel, String nameInNamespace, Class type) throws NamingException {
        super(nameInNamespace, new AbstractNameQuery(null, Collections.EMPTY_MAP, ConnectionFactorySource.class.getName()), kernel);
        this.type = type;
    }

    public ResourceBindings(Kernel kernel, String nameInNamespace, ClassLoader classLoader, String type) throws NamingException, ClassNotFoundException {
        super(nameInNamespace, new AbstractNameQuery(null, Collections.EMPTY_MAP, ConnectionFactorySource.class.getName()), kernel);
        this.type = classLoader.loadClass(type);
    }

    protected Map createBindings(AbstractName abstractName, Object value) throws NamingException {
        if (value instanceof ConnectionFactorySource) {
            ConnectionFactorySource connectionFactorySource = (ConnectionFactorySource) value;

            String name = (String) abstractName.getName().get("name");
            if (name == null) return null;

            Object resource = null;
            try {
                resource = connectionFactorySource.$getResource();
            } catch (ResourceException e) {
                throw (NamingException)new NamingException("Could not obtain connection factory from gbean").initCause(e);
            }
            if (!type.isInstance(resource)) return null;

            NameParser parser = getNameParser();
            Name jndiName = parser.parse(name);

            return Collections.singletonMap(jndiName, resource);
        }
        throw new NamingException("value is not a ConnectionFactorySource: abstractName=" + abstractName + " valueType=" + value.getClass().getName());
    }

    public static final GBeanInfo GBEAN_INFO;

    public static GBeanInfo getGBeanInfo() {
        return ResourceBindings.GBEAN_INFO;
    }

    static {
        GBeanInfoBuilder builder = GBeanInfoBuilder.createStatic(ResourceBindings.class, "JdbcBindings");
        builder.addAttribute("type", String.class, true);
        builder.setConstructor(new String[]{"kernel", "nameInNamespace", "classLoader", "type"});
        GBEAN_INFO = builder.getBeanInfo();
    }
}
