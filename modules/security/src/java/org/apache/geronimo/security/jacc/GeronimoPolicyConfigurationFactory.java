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

import java.util.HashMap;
import java.util.Map;

import javax.security.jacc.PolicyConfiguration;
import javax.security.jacc.PolicyConfigurationFactory;
import javax.security.jacc.PolicyContextException;

import org.apache.geronimo.security.jacc.GeronimoPolicyConfiguration;
import org.apache.geronimo.security.GeronimoSecurityPermission;


/**
 *
 * @version $Rev$ $Date$
 */
public class GeronimoPolicyConfigurationFactory extends PolicyConfigurationFactory {
    private Map configurations = new HashMap();

    public void setPolicyConfiguration(String contextID, GeronimoPolicyConfiguration configuration) {
        SecurityManager sm = System.getSecurityManager();
        if (sm != null) sm.checkPermission(new GeronimoSecurityPermission("setPolicyConfiguration"));

        configurations.put(contextID, configuration);
    }

    public PolicyConfiguration getPolicyConfiguration(String contextID, boolean remove) throws PolicyContextException {
        PolicyConfiguration configuration = (PolicyConfiguration) configurations.get(contextID);

        if (configuration == null || remove) {
            configuration = new PolicyConfigurationGeneric(contextID);
            configurations.put(contextID, configuration);
        }

        return configuration;
    }

    public boolean inService(String contextID) throws PolicyContextException {
        PolicyConfiguration configuration = getPolicyConfiguration(contextID, false);

        return configuration.inService();
    }

}
