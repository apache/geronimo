/**
 *
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.apache.geronimo.security;

import org.apache.openejb.client.IdentityResolver;

import javax.security.auth.Subject;
import java.security.AccessController;
import java.util.Set;

/**
 * @version $Rev$ $Date$
 */
public class GeronimoIdentityResolver implements IdentityResolver {
    public Object getIdentity() {
        Subject subject = ContextManager.getCurrentCaller();
        if (subject == null) {
            return null;
        }

        Set<IdentificationPrincipal> identificationPrincipals = subject.getPrincipals(IdentificationPrincipal.class);
        if (identificationPrincipals.isEmpty()) {
            return null;
        }

        IdentificationPrincipal principal = identificationPrincipals.iterator().next();
        return principal.getId();
    }
}
