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
package org.apache.geronimo.security.jaas;

import javax.management.ObjectName;
import javax.security.auth.Subject;
import javax.security.auth.login.LoginContext;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import org.apache.geronimo.gbean.jmx.GBeanMBean;
import org.apache.geronimo.security.AbstractTest;
import org.apache.geronimo.security.IdentificationPrincipal;
import org.apache.geronimo.security.RealmPrincipal;


/**
 * @version $Revision: 1.1 $ $Date: 2004/02/17 00:05:40 $
 */
public class LoginSQLTest extends AbstractTest {

    private static final String hsqldbURL = "jdbc:hsqldb:target/database/LoginSQLTest";
    protected ObjectName sqlRealm;

    public void setUp() throws Exception {
        super.setUp();

        DriverManager.registerDriver(new org.hsqldb.jdbcDriver());

        Connection conn = DriverManager.getConnection(hsqldbURL, "sa", "");


        try {
            conn.prepareStatement("CREATE USER loginmodule PASSWORD password ADMIN;").executeQuery();
        } catch (SQLException e) {
            //ignore, for some reason user already exists.
        }

        conn.prepareStatement("CREATE TABLE Users(UserName VARCHAR(16), Password VARCHAR(16));").executeQuery();
        conn.prepareStatement("CREATE TABLE Groups(GroupName VARCHAR(16), UserName VARCHAR(16));").executeQuery();

        conn.prepareStatement("GRANT SELECT ON Users TO loginmodule;").executeQuery();
        conn.prepareStatement("GRANT SELECT ON Groups TO loginmodule;").executeQuery();

        conn.prepareStatement("INSERT INTO Users VALUES ('izumi', 'violin');").executeQuery();
        conn.prepareStatement("INSERT INTO Users VALUES ('alan', 'starcraft');").executeQuery();
        conn.prepareStatement("INSERT INTO Users VALUES ('george', 'bone');").executeQuery();
        conn.prepareStatement("INSERT INTO Users VALUES ('gracie', 'biscuit');").executeQuery();
        conn.prepareStatement("INSERT INTO Users VALUES ('metro', 'mouse');").executeQuery();

        conn.prepareStatement("INSERT INTO Groups VALUES ('manager', 'izumi');").executeQuery();
        conn.prepareStatement("INSERT INTO Groups VALUES ('it', 'alan');").executeQuery();
        conn.prepareStatement("INSERT INTO Groups VALUES ('pet', 'george');").executeQuery();
        conn.prepareStatement("INSERT INTO Groups VALUES ('pet', 'gracie');").executeQuery();
        conn.prepareStatement("INSERT INTO Groups VALUES ('pet', 'metro');").executeQuery();
        conn.prepareStatement("INSERT INTO Groups VALUES ('dog', 'george');").executeQuery();
        conn.prepareStatement("INSERT INTO Groups VALUES ('dog', 'gracie');").executeQuery();
        conn.prepareStatement("INSERT INTO Groups VALUES ('cat', 'metro');").executeQuery();

        conn.close();

        GBeanMBean gbean = new GBeanMBean("org.apache.geronimo.security.realm.providers.SQLSecurityRealm");
        sqlRealm = new ObjectName("geronimo.security:type=SecurityRealm,realm=sql-realm");
        gbean.setAttribute("RealmName", "sql-realm");
        gbean.setAttribute("MaxLoginModuleAge", new Long(1 * 1000));
        gbean.setAttribute("ConnectionURL", hsqldbURL);
        gbean.setAttribute("User", "loginmodule");
        gbean.setAttribute("Password", "password");
        gbean.setAttribute("UserSelect", "SELECT UserName, Password FROM Users");
        gbean.setAttribute("GroupSelect", "SELECT GroupName, UserName FROM Groups");
        kernel.loadGBean(sqlRealm, gbean);
        kernel.startGBean(sqlRealm);
    }

    public void tearDown() throws Exception {
        kernel.stopGBean(sqlRealm);
        kernel.unloadGBean(sqlRealm);

        super.tearDown();

        Connection conn = DriverManager.getConnection(hsqldbURL, "sa", "");

        try {
            conn.prepareStatement("DROP USER loginmodule;").executeQuery();

            conn.prepareStatement("DROP TABLE Users;").executeQuery();
            conn.prepareStatement("DROP TABLE Groups;").executeQuery();
        } catch (SQLException e) {
            //who knows??
        }

    }

    public void XtestLogin() throws Exception {
        LoginContext context = new LoginContext("sql", new UsernamePasswordCallback("alan", "starcraft"));

        context.login();
        Subject subject = context.getSubject();

        assertTrue("expected non-null subject", subject != null);
        assertEquals("subject should have five principal", 5, subject.getPrincipals().size());
        assertEquals("subject should have two realm principals", 2, subject.getPrincipals(RealmPrincipal.class).size());
        assertEquals("subject should have one remote principal", 1, subject.getPrincipals(IdentificationPrincipal.class).size());
        IdentificationPrincipal principal = (IdentificationPrincipal) subject.getPrincipals(IdentificationPrincipal.class).iterator().next();
        assertTrue("id of principal should be non-zero", principal.getId().longValue() != 0);

        context.logout();
    }

    public void testLogoutTimeout() throws Exception {
        LoginContext context = new LoginContext("sql", new UsernamePasswordCallback("alan", "starcraft"));

        context.login();
        Subject subject = context.getSubject();

        assertTrue("expected non-null subject", subject != null);
        assertEquals("subject should have five principal", 5, subject.getPrincipals().size());
        assertEquals("subject should have two realm principals", 2, subject.getPrincipals(RealmPrincipal.class).size());
        assertEquals("subject should have one remote principal", 1, subject.getPrincipals(IdentificationPrincipal.class).size());
        IdentificationPrincipal principal = (IdentificationPrincipal) subject.getPrincipals(IdentificationPrincipal.class).iterator().next();
        assertTrue("id of principal should be non-zero", principal.getId().longValue() != 0);

        Thread.sleep(2 * 1000);

        try {
            context.logout();
            fail("The login module should have expired");
        } catch (ExpiredLoginModuleException e) {
            context.login();

            subject = context.getSubject();

            assertTrue("expected non-null subject", subject != null);
            assertEquals("subject should have five principal", 5, subject.getPrincipals().size());
            assertEquals("subject should have two realm principals", 2, subject.getPrincipals(RealmPrincipal.class).size());
            assertEquals("subject should have one remote principal", 1, subject.getPrincipals(IdentificationPrincipal.class).size());
            principal = (IdentificationPrincipal) subject.getPrincipals(IdentificationPrincipal.class).iterator().next();
            assertTrue("id of principal should be non-zero", principal.getId().longValue() != 0);

            context.logout();
        }
    }

    public void XtestReloginTimeout() throws Exception {
        LoginContext context = new LoginContext("sql", new UsernamePasswordCallback("alan", "starcraft"));

        context.login();
        Subject subject = context.getSubject();

        assertTrue("expected non-null subject", subject != null);
        assertEquals("subject should have five principal", 5, subject.getPrincipals().size());
        assertEquals("subject should have two realm principals", 2, subject.getPrincipals(RealmPrincipal.class).size());
        assertEquals("subject should have one remote principal", 1, subject.getPrincipals(IdentificationPrincipal.class).size());
        IdentificationPrincipal principal = (IdentificationPrincipal) subject.getPrincipals(IdentificationPrincipal.class).iterator().next();
        assertTrue("id of principal should be non-zero", principal.getId().longValue() != 0);

        context.logout();
        context.login();
        context.logout();

        /**
         * Waiting this long should cause the login module w/ an artificially
         * low age limit to expire.  The next call to login should automatically
         * create a new one.
         */
        Thread.sleep(4 * 1000);

        context.login();

        subject = context.getSubject();

        assertTrue("expected non-null subject", subject != null);
        assertEquals("subject should have five principal", 5, subject.getPrincipals().size());
        assertEquals("subject should have two realm principals", 2, subject.getPrincipals(RealmPrincipal.class).size());
        assertEquals("subject should have one remote principal", 1, subject.getPrincipals(IdentificationPrincipal.class).size());
        principal = (IdentificationPrincipal) subject.getPrincipals(IdentificationPrincipal.class).iterator().next();
        assertTrue("id of principal should be non-zero", principal.getId().longValue() != 0);

        context.logout();
    }
}
