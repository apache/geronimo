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
package org.apache.geronimo.security.jacc;

import java.security.Permission;
import java.security.PermissionCollection;
import java.security.Policy;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import javax.security.auth.Subject;
import javax.security.jacc.PolicyConfiguration;
import javax.security.jacc.PolicyConfigurationFactory;
import javax.security.jacc.PolicyContextException;

import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.gbean.GBeanInfoBuilder;
import org.apache.geronimo.gbean.GBeanLifecycle;
import org.apache.geronimo.j2ee.j2eeobjectnames.NameFactory;
import org.apache.geronimo.security.ContextManager;
import org.apache.geronimo.security.IdentificationPrincipal;
import org.apache.geronimo.security.SubjectId;

/**
 * @version $Rev$ $Date$
 */
public class ApplicationPrincipalRoleConfigurationManager implements PrincipalRoleMapper {

    private final Map principalRoleMap;

    public ApplicationPrincipalRoleConfigurationManager(Map principalRoleMap) throws PolicyContextException, ClassNotFoundException {
        this.principalRoleMap = principalRoleMap;
    }


    public void install(Set contextIds) throws PolicyContextException {
        GeronimoPolicyConfigurationFactory roleMapperFactory = GeronimoPolicyConfigurationFactory.getSingleton();
        if (roleMapperFactory == null) {
            throw new IllegalStateException("Inconsistent security setup.  GeronimoPolicyConfigurationFactory is not being used");
        }

        for (Iterator iterator = contextIds.iterator(); iterator.hasNext();) {
            String contextID = (String) iterator.next();

            GeronimoPolicyConfiguration geronimoPolicyConfiguration = roleMapperFactory.getGeronimoPolicyConfiguration(contextID);
            geronimoPolicyConfiguration.setPrincipalRoleMapping(principalRoleMap);
        }

    }


    public void uninstall() throws PolicyContextException {
    }


    public static final GBeanInfo GBEAN_INFO;

    static {
        GBeanInfoBuilder infoBuilder = GBeanInfoBuilder.createStatic(ApplicationPrincipalRoleConfigurationManager.class, NameFactory.JACC_MANAGER);
        infoBuilder.addAttribute("principalRoleMap", Map.class, true);
        infoBuilder.addInterface(PrincipalRoleMapper.class);
        infoBuilder.setConstructor(new String[] {"principalRoleMap"});
        GBEAN_INFO = infoBuilder.getBeanInfo();
    }

    public GBeanInfo getGBeanInfo() {
        return GBEAN_INFO;
    }
}
