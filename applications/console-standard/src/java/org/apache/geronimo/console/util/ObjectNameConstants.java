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

import java.util.Set;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;

import org.apache.geronimo.kernel.Kernel;
import org.apache.geronimo.kernel.KernelRegistry;

public final class ObjectNameConstants {

    // Security object names
    public static final ObjectName SE_REALM_MBEAN_NAME;
    //= "geronimo.server:J2EEApplication=org/apache/geronimo/Console,J2EEModule=null,J2EEServer=geronimo,j2eeType=GBean,name=PropertiesLoginManager";

//    public static final ObjectName SE_REALM_IMMUTABLE_MBEAN_NAME;
    //= "geronimo.server:name=PropertiesLoginManager,J2EEServer=geronimo,J2EEApplication=null,j2eeType=GBean,J2EEModule=null";

    public static final ObjectName REQUEST_LOGGER_OBJECT_NAME;
    //= "geronimo.server:J2EEApplication=null,J2EEModule=org/apache/geronimo/Jetty,J2EEServer=geronimo,j2eeType=GBean,name=JettyRequestLog";

    public static final ObjectName REPO_OBJECT_NAME;
    //= "geronimo.server:name=Repository,J2EEServer=geronimo,J2EEApplication=null,j2eeType=GBean,J2EEModule=org/apache/geronimo/System";

    public static final ObjectName SERVER_INFO_OBJECT_NAME;
    //= "geronimo.server:name=ServerInfo,J2EEServer=geronimo,J2EEApplication=null,j2eeType=GBean,J2EEModule=org/apache/geronimo/System";

    public static final ObjectName DEPLOYER_OBJECT_NAME;
    //= "geronimo.server:J2EEApplication=null,J2EEModule=org/apache/geronimo/RuntimeDeployer,J2EEServer=geronimo,j2eeType=Deployer,name=Deployer";

    public static final ObjectName KEYSTORE_OBJ_NAME;
    //= "geronimo.security:type=KeyStore";

    static {
        Kernel kernel = KernelRegistry.getSingleKernel();
        try {
            SE_REALM_MBEAN_NAME = getUniquename("*:J2EEModule=null,j2eeType=GBean,name=PropertiesLoginManager,*", kernel);
//            SE_REALM_IMMUTABLE_MBEAN_NAME = getUniquename("geronimo.server:name=PropertiesLoginManager,J2EEServer=geronimo,J2EEApplication=null,j2eeType=GBean,J2EEModule=null", kernel);
            REQUEST_LOGGER_OBJECT_NAME = getUniquename("*:J2EEApplication=null,j2eeType=GBean,name=JettyRequestLog,*", kernel);
            REPO_OBJECT_NAME = getUniquename("*:J2EEApplication=null,j2eeType=GBean,name=Repository,*", kernel);
            SERVER_INFO_OBJECT_NAME = getUniquename("*:J2EEApplication=null,j2eeType=GBean,name=ServerInfo,*", kernel);
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
