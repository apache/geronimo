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

package org.apache.geronimo.console.util;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.apache.geronimo.gbean.AbstractName;
import org.apache.geronimo.gbean.AbstractNameQuery;
import org.apache.geronimo.j2ee.j2eeobjectnames.NameFactory;
import org.apache.geronimo.kernel.Kernel;
import org.apache.geronimo.kernel.KernelRegistry;

/**
 * @version $Rev$ $Date$
 */
public final class ObjectNameConstants {

    // Security object names
    public static final AbstractName SE_REALM_MBEAN_NAME;
    public static final AbstractName DEPLOYER_OBJECT_NAME;

    static {
        Kernel kernel = KernelRegistry.getSingleKernel();
        SE_REALM_MBEAN_NAME = getUniquename("PropertiesLoginManager", "GBean", kernel);
        DEPLOYER_OBJECT_NAME = getUniquename("Deployer", "Deployer", kernel);
    }

    private static AbstractName getUniquename(String name, String type, Kernel kernel) {
        Map properties = new HashMap(2);
        properties.put(NameFactory.J2EE_NAME, name);
        properties.put(NameFactory.J2EE_TYPE, type);
        AbstractNameQuery query = new AbstractNameQuery(null, properties);
        Set results = kernel.listGBeans(query);
        if (results.isEmpty()) {
            throw new RuntimeException("No services found with name " + name + " and type " + type);
        }
        if (results.size() != 1) {
            throw new RuntimeException("More than one service was found with name " + name + " and type " + type + ", returns: " + results);
        }
        return (AbstractName) results.iterator().next();
    }

}
