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
package org.apache.geronimo.security.jacc;

import java.util.Collection;
import java.util.Map;
import javax.security.jacc.PolicyContextException;


/**
 * @version $Revision: $ $Date: $
 */
public class RoleMappingConfigurationImpl implements RoleMappingConfiguration {

    private final GeronimoPolicyConfiguration policyConfiguration;

    RoleMappingConfigurationImpl(GeronimoPolicyConfiguration policyConfiguration) {
        this.policyConfiguration = policyConfiguration;
    }

    public void addRoleMapping(String role, Collection principals) throws PolicyContextException {
        policyConfiguration.addRoleMapping(role, principals);
    }

    public void setPrincipalRoleMapping(Map principalRoleMap) throws PolicyContextException {
        policyConfiguration.setPrincipalRoleMapping(principalRoleMap);
    }
}
