/**
 *
 * Copyright 2004 The Apache Software Foundation
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

import javax.security.jacc.PolicyConfiguration;
import javax.security.jacc.PolicyContextException;


/**
 * <p>The methods of this interface are used by containers to create role mappings in a <code>Policy</code> provider.
 * An object that implements the <code>RoleMappingConfiguration</code> interface provides the role mapping configuration
 * interface for a corresponding policy context within the corresponding Policy provider.</p>
 *
 * <p>Geronimo will obtain an instance of this class by calling
 * <code>PolicyConfigurationFactory.getPolicyConfiguration</code>.  If the object that is returned <i>also</i>
 * implements <code>RoleMappingConfiguration</code>, Geronimo will call the methods of that interface to provide role
 * mappings to the <code>Policy</code> provider</p>
 * @version $Revision: 1.2 $ $Date: 2004/02/25 09:58:09 $
 * @see        javax.security.jacc.PolicyConfiguration
 * @see        javax.security.jacc.PolicyConfigurationFactory#getPolicyConfiguration
 */
public interface RoleMappingConfiguration extends PolicyConfiguration {

    /**
     * Add a mapping from a module's security roles to physical principals.  Mapping principals to the same role twice
     * will cause a <code>PolicyContextException</code> to be thrown.
     * @param role The role that is to be mapped to a set of principals.
     * @param principals The set of principals that are to be mapped to to role.
     * @throws javax.security.jacc.PolicyContextException if the mapping principals to the same role twice occurs.
     */
    public void addRoleMapping(String role, Collection principals) throws PolicyContextException;
}
