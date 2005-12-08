/**
 *
 * Copyright 2004, 2005 The Apache Software Foundation or its licensors, as applicable.
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

package org.apache.geronimo.console.util;

import org.apache.geronimo.kernel.Kernel;
import org.apache.geronimo.kernel.KernelRegistry;

import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import java.util.Set;

public final class ObjectNameConstants {

    // Security object names
    public static final ObjectName SE_REALM_MBEAN_NAME;
    public static final ObjectName DEPLOYER_OBJECT_NAME;
    public static final ObjectName KEYSTORE_OBJ_NAME;

    static {
        Kernel kernel = KernelRegistry.getSingleKernel();
        try {
            SE_REALM_MBEAN_NAME = getUniquename("*:J2EEModule=null,j2eeType=GBean,name=PropertiesLoginManager,*", kernel);
            DEPLOYER_OBJECT_NAME = getUniquename("*:J2EEApplication=null,j2eeType=Deployer,name=Deployer,*", kernel);
            KEYSTORE_OBJ_NAME = getUniquename("*:J2EEModule=null,j2eeType=GBean,name=KeyStore,*", kernel);
        } catch (MalformedObjectNameException e) {
            throw new RuntimeException(e);
        }
    }

    private static ObjectName getUniquename(String queryPattern, Kernel kernel) throws MalformedObjectNameException {
        ObjectName query = ObjectName.getInstance(queryPattern);
        Set results = kernel.listGBeans(query);
        if (results.size() != 1) {
            throw new RuntimeException("Invalid query: " + queryPattern + ", returns: " + results);
        }
        return (ObjectName) results.iterator().next();
    }

}
