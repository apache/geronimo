/**
 *
 * Copyright 2003-2004 The Apache Software Foundation
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

package org.apache.geronimo.security.jacc;

import java.security.SecurityPermission;
import java.util.HashMap;
import java.util.Map;
import javax.security.jacc.PolicyContextException;

import org.apache.geronimo.security.GeronimoSecurityPermission;


public class RoleMappingConfigurationFactory {

    private final static RoleMappingConfigurationFactory factory = new RoleMappingConfigurationFactory();

    private Map configurations = new HashMap();

    private RoleMappingConfigurationFactory() {
    }

    public static RoleMappingConfigurationFactory getRoleMappingFactory() {

        SecurityManager sm = System.getSecurityManager();
        if (sm != null) sm.checkPermission(new SecurityPermission("setPolicy"));

        if (factory != null) return factory;

        return factory;
    }

    public void setRoleMappingConfiguration(String contextID, RoleMappingConfiguration configuration) {
        SecurityManager sm = System.getSecurityManager();
        if (sm != null) sm.checkPermission(new GeronimoSecurityPermission("setPolicyConfiguration"));

        configurations.put(contextID, configuration);
    }

    public RoleMappingConfiguration getRoleMappingConfiguration(String contextID, boolean remove) throws PolicyContextException {

        RoleMappingConfiguration configuration = (RoleMappingConfiguration) configurations.get(contextID);

        if (configuration == null) {

            GeronimoPolicyConfigurationFactory gpcf = GeronimoPolicyConfigurationFactory.getSingleton();
            GeronimoPolicyConfiguration policyConfiguration = gpcf.getGeronimoPolicyConfiguration(contextID);

            configuration = new RoleMappingConfigurationImpl(policyConfiguration);
            configurations.put(contextID, configuration);
        }

        return configuration;
    }
}
