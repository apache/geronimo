/* ====================================================================
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 2004 The Apache Software Foundation.  All rights
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
package org.apache.geronimo.security.jaas;

import javax.management.ObjectName;
import javax.security.auth.Subject;
import javax.security.auth.login.LoginContext;
import javax.security.auth.login.LoginException;

import org.apache.geronimo.gbean.jmx.GBeanMBean;
import org.apache.geronimo.security.AbstractTest;
import org.apache.geronimo.security.ContextManager;
import org.apache.geronimo.security.RealmPrincipal;


/**
 * @version $Revision: 1.1 $ $Date: 2004/02/17 00:05:40 $
 */
public class LoginKerberosTest extends AbstractTest {

    protected ObjectName kerberosRealm;
    protected ObjectName kerberosCE;

    public void setUp() throws Exception {
        super.setUp();

        GBeanMBean gbean = new GBeanMBean("org.apache.geronimo.security.realm.providers.KerberosSecurityRealm");
        kerberosRealm = new ObjectName("geronimo.security:type=SecurityRealm,realm=TOOLAZYDOGS.COM");
        gbean.setAttribute("RealmName", "TOOLAZYDOGS.COM");
        gbean.setAttribute("MaxLoginModuleAge", new Long(1 * 1000));
        gbean.setAttribute("debug", new Boolean(true));
        gbean.setAttribute("useTicketCache", new Boolean(true));
        gbean.setAttribute("doNotPrompt", new Boolean(true));
        kernel.loadGBean(kerberosRealm, gbean);
        kernel.startGBean(kerberosRealm);
    }

    public void tearDown() throws Exception {
        kernel.stopGBean(kerberosRealm);
        kernel.unloadGBean(kerberosRealm);

        super.tearDown();
    }

    public void testLogin() throws Exception {
        try {
            LoginContext context = new LoginContext("kerberos-local");

            context.login();
            Subject subject = context.getSubject();

            assertTrue("expected non-null subject", subject != null);
            assertTrue("id of subject should be non-null", ContextManager.getSubjectId(subject) != null);
            assertEquals("subject should have two principals", 2, subject.getPrincipals().size());
            assertEquals("subject should have one realm principal", 1, subject.getPrincipals(RealmPrincipal.class).size());
            RealmPrincipal principal = (RealmPrincipal) subject.getPrincipals(RealmPrincipal.class).iterator().next();
            assertTrue("id of principal should be non-zero", principal.getId() != 0);

            context.logout();

            assertTrue("id of subject should be null", ContextManager.getSubjectId(subject) == null);
        } catch (LoginException e) {
            // May not have kerberos
        }
    }
}
