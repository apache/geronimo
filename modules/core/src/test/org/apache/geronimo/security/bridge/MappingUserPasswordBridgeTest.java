/* ====================================================================
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 2003 The Apache Software Foundation.  All rights
 * reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. The end-user documentation included with the redistribution,
 *    if any, must include the following acknowledgment:
 *       "This product includes software developed by the
 *        Apache Software Foundation (http://www.apache.org/)."
 *    Alternately, this acknowledgment may appear in the software itself,
 *    if and wherever such third-party acknowledgments normally appear.
 *
 * 4. The names "Apache" and "Apache Software Foundation" and
 *    "Apache Geronimo" must not be used to endorse or promote products
 *    derived from this software without prior written permission. For
 *    written permission, please contact apache@apache.org.
 *
 * 5. Products derived from this software may not be called "Apache",
 *    "Apache Geronimo", nor may "Apache" appear in their name, without
 *    prior written permission of the Apache Software Foundation.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE APACHE SOFTWARE FOUNDATION OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 *
 * ====================================================================
 */

package org.apache.geronimo.security.bridge;

import java.util.Map;
import java.util.HashMap;
import java.security.Principal;

import javax.security.auth.Subject;

/**
 *
 *
 * @version $Revision: 1.1 $ $Date: 2004/01/11 08:27:03 $
 *
 * */
public class MappingUserPasswordBridgeTest extends AbstractUserPasswordBridgeTest {
    private static final String SOURCE_USER_1 = "sourceUser1";
    private static final String SOURCE_USER_2 = "sourceUser2";
    private static final String SOURCE_PRINCIPAL_1 = "sourcePrincipal1";
    private static final String SOURCE_PRINCIPAL_2 = "sourcePrincipal2";
    private static final String SOURCE_PASSWORD_1 = "sourcePassword1";
    private static final String SOURCE_PASSWORD_2 = "sourcePassword2";

    private TestMappingBridge bridge;

    protected void setUp() {
        super.setUp();
        bridge = new TestMappingBridge();
        bridge.setTargetRealm(TestRealm.REALM_NAME);
        bridge.setPrincipalSourceType(TestPrincipalPrincipal.class);
        bridge.setPrincipalTargetCallbackName("Resource Principal");
        Map principalMap = new HashMap();
        principalMap.put(SOURCE_PRINCIPAL_1, AbstractUserPasswordBridgeTest.USER);
        principalMap.put(SOURCE_PRINCIPAL_2, "no-one");
        bridge.setPrincipalMap(principalMap);
        bridge.setUserNameSourceType(TestUserNamePrincipal.class);
        bridge.setUserNameTargetCallbackName("User Name");
        Map userNameMap = new HashMap();
        userNameMap.put(SOURCE_USER_1, AbstractUserPasswordBridgeTest.USER);
        userNameMap.put(SOURCE_USER_2, "no-one");
        bridge.setUserNameMap(userNameMap);
        bridge.setPasswordSourceType(TestPasswordPrincipal.class);
        Map passwordMap = new HashMap();
        passwordMap.put(SOURCE_PASSWORD_1, AbstractUserPasswordBridgeTest.PASSWORD.toCharArray());
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
