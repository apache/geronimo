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

package org.apache.geronimo.security.bridge;

import javax.security.auth.Subject;

import org.apache.geronimo.security.AbstractTest;
import org.apache.geronimo.security.realm.providers.GeronimoPasswordCredential;


/**
 * @version $Rev$ $Date$
 */
public abstract class AbstractBridgeTest extends AbstractTest {
    protected final static String USER = "testuser";
    protected final static String PASSWORD = "testpassword";

    protected void checkValidSubject(Subject targetSubject) {
        assertEquals("Expected one  TestPrincipal", 1, targetSubject.getPrincipals(TestPrincipal.class).size());
        Object p = targetSubject.getPrincipals(TestPrincipal.class).iterator().next();
        assertSame("Expected ResourcePrincipal", TestPrincipal.class, p.getClass());
        assertEquals("Expected name of TestPrincipal to be " + ConfiguredIdentityUserPasswordBridgeTest.USER, ConfiguredIdentityUserPasswordBridgeTest.USER, ((TestPrincipal) p).getName());
        assertEquals("Expected no public credential", 0, targetSubject.getPublicCredentials().size());
        assertEquals("Expected one private credential", 1, targetSubject.getPrivateCredentials().size());
        Object cred = targetSubject.getPrivateCredentials().iterator().next();
        assertSame("Expected GeronimoPasswordCredential", GeronimoPasswordCredential.class, cred.getClass());
        assertEquals("Expected user", ConfiguredIdentityUserPasswordBridgeTest.USER, ((GeronimoPasswordCredential) cred).getUserName());
        assertEquals("Expected password", ConfiguredIdentityUserPasswordBridgeTest.PASSWORD, new String(((GeronimoPasswordCredential) cred).getPassword()));
    }
}
