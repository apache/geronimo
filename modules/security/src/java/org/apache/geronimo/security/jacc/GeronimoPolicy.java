/**
 *
 * Copyright 2003-2005 The Apache Software Foundation
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

import java.security.CodeSource;
import java.security.Permission;
import java.security.PermissionCollection;
import java.security.Policy;
import java.security.ProtectionDomain;
import javax.security.jacc.PolicyConfigurationFactory;
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
        this(null);
    }

    public GeronimoPolicy(Policy root) {
        this.root = root;
    }

    public PermissionCollection getPermissions(CodeSource codesource) {
        return null;
    }

    public void refresh() {
    }

    public boolean implies(ProtectionDomain domain, Permission permission) {

        if (!loaded) {
            factory = obtainFactory();
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

    private GeronimoPolicyConfigurationFactory obtainFactory() {
        GeronimoPolicyConfigurationFactory result = null;
        try {
            result = (GeronimoPolicyConfigurationFactory) PolicyConfigurationFactory.getPolicyConfigurationFactory();
        } catch (ClassNotFoundException e) {
        } catch (PolicyContextException e) {
        }
        return result;
    }
}
