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

public interface ObjectNameConstants {

    // Security object names
    public static final String SE_REALM_MBEAN_NAME = "geronimo.server:J2EEApplication=org/apache/geronimo/Console,J2EEModule=null,J2EEServer=geronimo,j2eeType=GBean,name=PropertiesLoginManager";

    public static final String SE_REALM_IMMUTABLE_MBEAN_NAME = "geronimo.server:name=PropertiesLoginManager,J2EEServer=geronimo,J2EEApplication=null,j2eeType=GBean,J2EEModule=null";

    public static final String SECURITY_REALM = "geronimo.security:type=SecurityRealm,*";

    public static final String ROOT_LOGGER_OBJECT_NAME = "geronimo.server:name=Logger,J2EEServer=geronimo,J2EEApplication=null,j2eeType=GBean,J2EEModule=org/apache/geronimo/System";

    public static final String REQUEST_LOGGER_OBJECT_NAME = "geronimo.server:J2EEApplication=null,J2EEModule=org/apache/geronimo/Jetty,J2EEServer=geronimo,j2eeType=GBean,name=JettyRequestLog";

    public static final String DERBY_OBJECT_NAME = "geronimo.server:name=DerbySystem,J2EEServer=geronimo,J2EEApplication=null,j2eeType=GBean,J2EEModule=null";

    public static final String REPO_OBJECT_NAME = "geronimo.server:name=Repository,J2EEServer=geronimo,J2EEApplication=null,j2eeType=GBean,J2EEModule=org/apache/geronimo/System";

    public static final String SERVER_INFO_OBJECT_NAME = "geronimo.server:name=ServerInfo,J2EEServer=geronimo,J2EEApplication=null,j2eeType=GBean,J2EEModule=org/apache/geronimo/System";

    public static final String JVM_IMPL_NAME = "geronimo.server:j2eeType=JVM,J2EEServer=geronimo,name=JVM";

    public static final String DEPLOYER_OBJECT_NAME = "geronimo.server:J2EEApplication=null,J2EEModule=org/apache/geronimo/RuntimeDeployer,J2EEServer=geronimo,j2eeType=Deployer,name=Deployer";

    public static final String JCA_MANAGED_CF_QUERY = "*:j2eeType=JCAManagedConnectionFactory,*";

    public static final String CONFIG_GBEAN_PREFIX = "geronimo.config:name=";

    public static final String KEYSTORE_OBJ_NAME = "geronimo.security:type=KeyStore";

}
