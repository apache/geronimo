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
 * @version $Revision: 1.5 $ $Date: 2004/03/10 09:59:27 $
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
        assertTrue("id of principal should be non-zero", principal.getId().getSubjectId().longValue() != 0);

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
        assertTrue("id of principal should be non-zero", principal.getId().getSubjectId().longValue() != 0);

        Thread.sleep(20 * 1000);

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
            assertTrue("id of principal should be non-zero", principal.getId().getSubjectId().longValue() != 0);

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
        assertTrue("id of principal should be non-zero", principal.getId().getSubjectId().longValue() != 0);

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
        assertTrue("id of principal should be non-zero", principal.getId().getSubjectId().longValue() != 0);

        context.logout();
    }
}
