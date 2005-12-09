/**
 *
 * Copyright 2005 The Apache Software Foundation
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
package org.apache.geronimo.web.deployment;

import java.util.Set;
import java.util.HashSet;

import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;

import org.apache.geronimo.j2ee.deployment.ModuleBuilder;
import org.apache.geronimo.j2ee.deployment.EARContext;
import org.apache.geronimo.j2ee.j2eeobjectnames.NameFactory;

/**
 * @version $Rev$ $Date$
 */
public abstract class AbstractWebModuleBuilder implements ModuleBuilder {
    protected static final ObjectName MANAGED_CONNECTION_FACTORY_PATTERN;
    private static final ObjectName ADMIN_OBJECT_PATTERN;
    protected static final ObjectName STATELESS_SESSION_BEAN_PATTERN;
    protected static final ObjectName STATEFUL_SESSION_BEAN_PATTERN;
    protected static final ObjectName ENTITY_BEAN_PATTERN;


    static {
        try {
            MANAGED_CONNECTION_FACTORY_PATTERN = ObjectName.getInstance("*:j2eeType=" + NameFactory.JCA_MANAGED_CONNECTION_FACTORY +  ",*");
            ADMIN_OBJECT_PATTERN = ObjectName.getInstance("*:j2eeType=" + NameFactory.JCA_ADMIN_OBJECT +  ",*");
            STATELESS_SESSION_BEAN_PATTERN = ObjectName.getInstance("*:j2eeType=" + NameFactory.STATELESS_SESSION_BEAN +  ",*");
            STATEFUL_SESSION_BEAN_PATTERN = ObjectName.getInstance("*:j2eeType=" + NameFactory.STATEFUL_SESSION_BEAN +  ",*");
            ENTITY_BEAN_PATTERN = ObjectName.getInstance("*:j2eeType=" + NameFactory.ENTITY_BEAN +  ",*");
        } catch (MalformedObjectNameException e) {
            throw new RuntimeException(e);
        }

    }

    protected Set findGBeanDependencies(EARContext earContext) {
        Set dependencies = new HashSet();
        dependencies.addAll(earContext.listGBeans(MANAGED_CONNECTION_FACTORY_PATTERN));
        dependencies.addAll(earContext.listGBeans(ADMIN_OBJECT_PATTERN));
        dependencies.addAll(earContext.listGBeans(STATELESS_SESSION_BEAN_PATTERN));
        dependencies.addAll(earContext.listGBeans(STATEFUL_SESSION_BEAN_PATTERN));
        dependencies.addAll(earContext.listGBeans(ENTITY_BEAN_PATTERN));
        return dependencies;
    }
}
