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

import java.security.Principal;
import java.util.HashMap;
import java.util.Map;
import javax.security.auth.Subject;


/**
 * @version $Rev$ $Date$
 */
public class MappingUserPasswordBridgeTest extends AbstractBridgeTest {
    private static final String SOURCE_USER_1 = "sourceUser1";
    private static final String SOURCE_USER_2 = "sourceUser2";
    private static final String SOURCE_PRINCIPAL_1 = "sourcePrincipal1";
    private static final String SOURCE_PRINCIPAL_2 = "sourcePrincipal2";
    private static final String SOURCE_PASSWORD_1 = "sourcePassword1";
    private static final String SOURCE_PASSWORD_2 = "sourcePassword2";

    private TestMappingBridge bridge;

    protected void setUp() throws Exception {
        super.setUp();
        bridge = new TestMappingBridge();
        bridge.setTargetRealm(TestRealm.JAAS_NAME);
        bridge.setPrincipalSourceType(TestPrincipalPrincipal.class);
        bridge.setPrincipalTargetCallbackName("Resource Principal");
        Map principalMap = new HashMap();
        principalMap.put(SOURCE_PRINCIPAL_1, AbstractBridgeTest.USER);
        principalMap.put(SOURCE_PRINCIPAL_2, "no-one");
        bridge.setPrincipalMap(principalMap);
        bridge.setUserNameSourceType(TestUserNamePrincipal.class);
        bridge.setUserNameTargetCallbackName("User Name");
        Map userNameMap = new HashMap();
        userNameMap.put(SOURCE_USER_1, AbstractBridgeTest.USER);
        userNameMap.put(SOURCE_USER_2, "no-one");
        bridge.setUserNameMap(userNameMap);
        bridge.setPasswordSourceType(TestPasswordPrincipal.class);
        Map passwordMap = new HashMap();
        passwordMap.put(SOURCE_PASSWORD_1, AbstractBridgeTest.PASSWORD.toCharArray());
        passwordMap.put(SOURCE_PASSWORD_2, "no-password".toCharArray());
        bridge.setPasswordMap(passwordMap);
    }

    public void testMapping() throws Exception {
        Subject subject = new Subject();
        subject.getPrincipals().add(new TestPrincipalPrincipal(SOURCE_PRINCIPAL_1));
        subject.getPrincipals().add(new TestUserNamePrincipal(SOURCE_USER_1));
        subject.getPrincipals().add(new TestPasswordPrincipal(SOURCE_PASSWORD_1));
        Subject targetSubject = bridge.mapSubject(subject);
        checkValidSubject(targetSubject);
    }

    public void testInsufficientSourcePrincipals() throws Exception {
        Subject subject = new Subject();
        subject.getPrincipals().add(new TestPrincipalPrincipal(SOURCE_PRINCIPAL_1));
        subject.getPrincipals().add(new TestPasswordPrincipal(SOURCE_PASSWORD_1));
        try {
            bridge.mapSubject(subject);
            fail();
        } catch (Exception e) {
        }
    }

    public void testNotInMap() throws Exception {
        Subject subject = new Subject();
        subject.getPrincipals().add(new TestPrincipalPrincipal(SOURCE_PRINCIPAL_1 + "xxx"));
        subject.getPrincipals().add(new TestUserNamePrincipal(SOURCE_USER_1));
        subject.getPrincipals().add(new TestPasswordPrincipal(SOURCE_PASSWORD_1));
        try {
            bridge.mapSubject(subject);
            fail();
        } catch (Exception e) {
        }
    }

    public static class TestPrincipalPrincipal implements Principal {
        private String name;

        public TestPrincipalPrincipal(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }
    }

    public static class TestUserNamePrincipal implements Principal {
        private String name;

        public TestUserNamePrincipal(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }
    }

    public static class TestPasswordPrincipal implements Principal {
        private String name;

        public TestPasswordPrincipal(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }
    }

    public static class TestMappingBridge extends AbstractPrincipalMappingUserPasswordRealmBridge {

        public void setPrincipalMap(Map principalMap) {
            this.principalMap.clear();
            this.principalMap.putAll(principalMap);
        }

        public void setUserNameMap(Map userNameMap) {
            this.userNameMap.clear();
            this.userNameMap.putAll(userNameMap);
        }

        public void setPasswordMap(Map passwordMap) {
            this.passwordMap.clear();
            this.passwordMap.putAll(passwordMap);
        }

    }
}
