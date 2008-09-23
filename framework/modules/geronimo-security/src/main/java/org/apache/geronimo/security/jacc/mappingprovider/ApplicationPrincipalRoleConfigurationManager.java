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
package org.apache.geronimo.security.jacc.mappingprovider;

import java.util.Map;
import java.util.Set;
import java.security.Principal;

import javax.security.jacc.PolicyContextException;

import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.gbean.GBeanInfoBuilder;
import org.apache.geronimo.security.jacc.PrincipalRoleMapper;
import org.apache.geronimo.security.SecurityNames;

/**
 * @version $Rev$ $Date$
 */
public class ApplicationPrincipalRoleConfigurationManager implements PrincipalRoleMapper {

    private static PrincipalRoleConfigurationFactory principalRoleConfigurationFactory;
    private final Map<Principal, Set<String>> principalRoleMap;

    public ApplicationPrincipalRoleConfigurationManager(Map<Principal, Set<String>> principalRoleMap) throws PolicyContextException, ClassNotFoundException {
        this.principalRoleMap = principalRoleMap;
    }

    public static void setPrincipalRoleConfigurationFactory(PrincipalRoleConfigurationFactory principalRoleConfigurationFactory) {
        if (ApplicationPrincipalRoleConfigurationManager.principalRoleConfigurationFactory != null) {
            throw new IllegalStateException("ApplicationPrincipalRoleConfigurationManager.principalRoleConfigurationFactory already set");
        }
        ApplicationPrincipalRoleConfigurationManager.principalRoleConfigurationFactory = principalRoleConfigurationFactory;
    }

    public void install(Set<String> contextIds) throws PolicyContextException {
        if (principalRoleConfigurationFactory == null) {
            throw new IllegalStateException("Inconsistent security setup.  PrincipalRoleConfigurationFactory is not set");
        }

        for (String contextID : contextIds) {
            PrincipalRoleConfiguration principalRoleConfiguration = principalRoleConfigurationFactory.getPrincipalRoleConfiguration(contextID);
            principalRoleConfiguration.setPrincipalRoleMapping(principalRoleMap);
        }

    }


    public void uninstall(Set<String> contextIds) throws PolicyContextException {
    }


    public static final GBeanInfo GBEAN_INFO;

    static {
        GBeanInfoBuilder infoBuilder = GBeanInfoBuilder.createStatic(ApplicationPrincipalRoleConfigurationManager.class, SecurityNames.JACC_MANAGER);
        infoBuilder.addAttribute("principalRoleMap", Map.class, true);
        infoBuilder.addInterface(PrincipalRoleMapper.class);
        infoBuilder.setConstructor(new String[] {"principalRoleMap"});
        GBEAN_INFO = infoBuilder.getBeanInfo();
    }

    public GBeanInfo getGBeanInfo() {
        return GBEAN_INFO;
    }
}
