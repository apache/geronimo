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

import java.util.Collections;

import javax.security.auth.Subject;

import junit.framework.TestCase;
import org.apache.geronimo.security.SecurityService;
import org.apache.geronimo.security.providers.GeronimoPasswordCredential;

/**
 *
 *
 * @version $Revision: 1.1 $ $Date: 2004/01/11 08:27:03 $
 *
 * */
public abstract class AbstractUserPasswordBridgeTest extends TestCase {
    private SecurityService securityService;
    protected final static String USER = "testuser";
    protected final static String PASSWORD = "testpassword";

    protected void setUp() {
        securityService = new SecurityService();
        securityService.setRealms(Collections.singleton(new TestRealm()));
    }

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
