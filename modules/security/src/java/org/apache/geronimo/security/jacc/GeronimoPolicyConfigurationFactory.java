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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.geronimo.security.GeronimoSecurityPermission;


/**
 * @version $Rev$ $Date$
 */
public class GeronimoPolicyConfigurationFactory extends PolicyConfigurationFactory {

    private final Log log = LogFactory.getLog(GeronimoPolicyConfigurationFactory.class);
    private static GeronimoPolicyConfigurationFactory singleton;
    private Map configurations = new HashMap();

    public GeronimoPolicyConfigurationFactory() {
        synchronized (GeronimoPolicyConfigurationFactory.class) {
            if (singleton != null) {
                log.error("Singleton already assigned.  There may be more than one GeronimoPolicyConfigurationFactory being used.");
                throw new IllegalStateException("Singleton already assigned");
            }
            singleton = this;
        }
    }

    public void setPolicyConfiguration(String contextID, GeronimoPolicyConfiguration configuration) {
        SecurityManager sm = System.getSecurityManager();
        if (sm != null) sm.checkPermission(new GeronimoSecurityPermission("setPolicyConfiguration"));

        configurations.put(contextID, configuration);

        log.trace("Set policy configuration " + contextID);
    }

    public GeronimoPolicyConfiguration getGeronimoPolicyConfiguration(String contextID) throws PolicyContextException {
        GeronimoPolicyConfiguration configuration = (GeronimoPolicyConfiguration) configurations.get(contextID);

        if (configuration == null) {
            throw new PolicyContextException("No policy configuration registered for contextID: " + contextID);
        }

        log.trace("Get policy configuration " + contextID);
        return configuration;
    }

    public PolicyConfiguration getPolicyConfiguration(String contextID, boolean remove) throws PolicyContextException {
        GeronimoPolicyConfiguration configuration = (GeronimoPolicyConfiguration) configurations.get(contextID);

        if (configuration == null) {
            configuration = new PolicyConfigurationGeneric(contextID);
            configurations.put(contextID, configuration);
        } else {
            configuration.open(remove);
        }

        log.trace("Get " + (remove ? "CLEANED" : "") + " policy configuration " + contextID);
        return configuration;
    }

    public boolean inService(String contextID) throws PolicyContextException {
        PolicyConfiguration configuration = getPolicyConfiguration(contextID, false);

        log.trace("Policy configuration " + contextID + " put into service");
        return configuration.inService();
    }

    static GeronimoPolicyConfigurationFactory getSingleton() {
        return singleton;
    }
}
