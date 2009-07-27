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
package org.apache.geronimo.security;

import java.util.Set;
import java.security.Principal;
import javax.security.auth.Subject;

import junit.framework.TestCase;
import org.apache.geronimo.security.realm.providers.GeronimoUserPrincipal;
import org.apache.geronimo.security.realm.providers.GeronimoGroupPrincipal;

/**
 * @version $Rev$ $Date$
 */
public class ContextManagerTest extends TestCase {

    public void testGetCallerPrincipal() throws Exception {
        Subject subject = new Subject();
        GeronimoUserPrincipal userPrincipal = new GeronimoUserPrincipal("foo");
        RealmPrincipal realmPrincipal = new RealmPrincipal("realm", "domain", userPrincipal);
        PrimaryRealmPrincipal primaryRealmPrincipal = new PrimaryRealmPrincipal("realm", "domain", userPrincipal);
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

    private final Subject s1 = new Subject();
    private final Subject s2 = new Subject();
    private final Subject s3 = new Subject();

    public void testPushNextCallerWithSubjectPresent() throws Exception {
        try {
            ContextManager.setCallers(s1, s1);
            Callers c1 = ContextManager.pushNextCaller(s2);
            assertSame("Callers should have s1 in current position", s1, c1.getCurrentCaller());
            assertSame("Callers should have s1 in next position", s1, c1.getNextCaller());
            assertSame("CurrentCaller should be s1", s1, ContextManager.getCurrentCaller());
            Callers c2 = ContextManager.pushNextCaller(s3);
            assertSame("Callers should have s1 in current position", s1, c2.getCurrentCaller());
            assertSame("Callers should have s2 in next position", s2, c2.getNextCaller());
            assertSame("CurrentCaller should be s2", s2, ContextManager.getCurrentCaller());
            Callers c3 = ContextManager.pushNextCaller(null);
            assertSame("Callers should have s2 in current position", s2, c3.getCurrentCaller());
            assertSame("Callers should have s3 in next position", s3, c3.getNextCaller());
            assertSame("CurrentCaller should be s3", s3, ContextManager.getCurrentCaller());
            Callers c4 = ContextManager.pushNextCaller(null);
            assertSame("Callers should have s3 in current position", s3, c4.getCurrentCaller());
            assertSame("Callers should have s3 in next position", s3, c4.getNextCaller());
            assertSame("CurrentCaller should be s3", s3, ContextManager.getCurrentCaller());
            ContextManager.popCallers(c4);
            assertSame("CurrentCaller should be s3", s3, ContextManager.getCurrentCaller());
            ContextManager.popCallers(c3);
            assertSame("CurrentCaller should be s2", s2, ContextManager.getCurrentCaller());
            ContextManager.popCallers(c2);
            assertSame("CurrentCaller should be s1", s1, ContextManager.getCurrentCaller());
            ContextManager.popCallers(c1);
            assertSame("CurrentCaller should be s1", s1, ContextManager.getCurrentCaller());
        } finally {
            ContextManager.clearCallers();
        }
    }

}
