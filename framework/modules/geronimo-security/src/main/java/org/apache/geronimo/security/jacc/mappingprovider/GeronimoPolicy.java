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

import java.security.CodeSource;
import java.security.Permission;
import java.security.PermissionCollection;
import java.security.Policy;
import java.security.ProtectionDomain;
import javax.security.jacc.PolicyContext;
import javax.security.jacc.PolicyContextException;


/**
 * @version $Rev$ $Date$
 */
public class GeronimoPolicy extends Policy {
    private final Policy root;
    private GeronimoPolicyConfigurationFactory factory;
    private boolean loaded;

    public GeronimoPolicy() {
        String provider = System.getProperty("org.apache.geronimo.jacc.policy.provider");

        if (provider == null) {
            root = Policy.getPolicy();
        } else {
            try {
                Object obj = Class.forName(provider).newInstance();
                if (obj instanceof Policy) {
                    root = (Policy) obj;
                } else {
                    throw new RuntimeException(provider + "is not a type of java.security.Policy");
                }
            } catch (InstantiationException e) {
                throw new RuntimeException("Unable to create an instance of " + provider, e);
            } catch (IllegalAccessException e) {
                throw new RuntimeException("Unable to create an instance of " + provider, e);
            } catch (ClassNotFoundException e) {
                throw new RuntimeException("Unable to create an instance of " + provider, e);
            }
        }
        root.refresh();

        // Force GeronimoPolicyConfigurationFactory class to be loaded
        // before the new Policy is in the place. See GERONIMO-4037.
        GeronimoPolicyConfigurationFactory.getSingleton();
    }

    public PermissionCollection getPermissions(CodeSource codesource) {

        if (root != null) return root.getPermissions(codesource);

        return null;
    }

    public void refresh() {
    }

    public boolean implies(ProtectionDomain domain, Permission permission) {

        if (!loaded) {
            factory = GeronimoPolicyConfigurationFactory.getSingleton();
            loaded = true;
        }

        if (factory != null) {
            String contextID = PolicyContext.getContextID();
            if (contextID != null) {
                try {
                    GeronimoPolicyConfiguration configuration = factory.getGeronimoPolicyConfiguration(contextID);

                    if (configuration.inService()) {
                        if (configuration.implies(domain, permission)) return true;
                    } else {
                        return false;
                    }
                } catch (PolicyContextException e) {
                }
            }
        }
        if (root != null) return root.implies(domain, permission);

        return false;
    }
}
