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
import javax.security.auth.login.LoginException;

import org.apache.geronimo.security.realm.providers.GeronimoPasswordCredential;
import org.apache.geronimo.security.IdentificationPrincipal;
import org.apache.geronimo.security.ContextManager;

import java.util.Set;


/**
 * @version $Rev$ $Date$
 */
public class CallerIdentityUserPasswordBridgeTest extends AbstractBridgeTest {

    private CallerIdentityUserPasswordRealmBridge bridge;

    protected void setUp() throws Exception {
        super.setUp();
        bridge = new CallerIdentityUserPasswordRealmBridge();
        bridge.setTargetRealm(TestRealm.JAAS_NAME);
    }

    public void testCallerIdentityBridge() throws Exception {
        Subject sourceSubject = new Subject();
        sourceSubject.getPrivateCredentials().add(new GeronimoPasswordCredential(AbstractBridgeTest.USER, AbstractBridgeTest.PASSWORD.toCharArray()));
        Subject targetSubject = bridge.mapSubject(sourceSubject);
        assertTrue("expected non-null client subject", targetSubject != null);
        Set set = targetSubject.getPrincipals(IdentificationPrincipal.class);
        assertEquals("client subject should have one ID principal", set.size(), 1);
        IdentificationPrincipal idp = (IdentificationPrincipal)set.iterator().next();
        targetSubject = ContextManager.getRegisteredSubject(idp.getId());
        checkValidSubject(targetSubject);
    }

    public void testNoCredentials() throws Exception {
        Subject sourceSubject = new Subject();
        try {
            bridge.mapSubject(sourceSubject);
            fail();
        } catch (LoginException e) {
        }

    }
}
