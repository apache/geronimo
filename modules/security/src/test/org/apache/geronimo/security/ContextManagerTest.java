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
package org.apache.geronimo.security;

import java.util.Set;
import java.security.Principal;
import javax.security.auth.Subject;

import junit.framework.TestCase;
import org.apache.geronimo.security.realm.providers.GeronimoUserPrincipal;
import org.apache.geronimo.security.realm.providers.GeronimoGroupPrincipal;

/**
 * @version $Rev:  $ $Date:  $
 */
public class ContextManagerTest extends TestCase {

    public void testGetCallerPrincipal() throws Exception {
        Subject subject = new Subject();
        GeronimoUserPrincipal userPrincipal = new GeronimoUserPrincipal("foo");
        RealmPrincipal realmPrincipal = new RealmPrincipal("domain", userPrincipal);
        PrimaryRealmPrincipal primaryRealmPrincipal = new PrimaryRealmPrincipal("domain", userPrincipal);
        GeronimoGroupPrincipal groupPrincipal = new GeronimoGroupPrincipal("bar");
        Set principals = subject.getPrincipals();
        principals.add(userPrincipal);
        principals.add(realmPrincipal);
        principals.add(primaryRealmPrincipal);
        principals.add(groupPrincipal);
        ContextManager.registerSubject(subject);
        Principal principal = ContextManager.getCurrentPrincipal(subject);
        assertSame("Expected GeronimoCallerPrincipal", userPrincipal, principal);
    }
}
