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

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import javax.management.ObjectName;
import javax.security.auth.Subject;
import javax.security.auth.login.LoginContext;

import org.apache.geronimo.gbean.jmx.GBeanMBean;
import org.apache.geronimo.security.AbstractTest;
import org.apache.geronimo.security.IdentificationPrincipal;
import org.apache.geronimo.security.RealmPrincipal;
import org.apache.geronimo.kernel.management.State;


/**
 * @version $Rev$ $Date$
 */
public class LoginSQLTest extends AbstractTest {

    private static final String hsqldbURL = "jdbc:hsqldb:target/database/LoginSQLTest";
    protected ObjectName sqlRealm;

    public void setUp() throws Exception {
        super.setUp();

        DriverManager.registerDriver(new org.hsqldb.jdbcDriver());

        Connection conn = DriverManager.getConnection(hsqldbURL, "sa", "");


        try {
            conn.prepareStatement("CREATE USER loginmodule PASSWORD password ADMIN;").executeUpdate();
        } catch (SQLException e) {
            //ignore, for some reason user already exists.
        }

        conn.prepareStatement("CREATE TABLE Users(UserName VARCHAR(16), Password VARCHAR(16));").executeUpdate();
        conn.prepareStatement("CREATE TABLE Groups(GroupName VARCHAR(16), UserName VARCHAR(16));").executeUpdate();

        conn.prepareStatement("GRANT SELECT ON Users TO loginmodule;").executeUpdate();
        conn.prepareStatement("GRANT SELECT ON Groups TO loginmodule;").executeUpdate();

        conn.prepareStatement("INSERT INTO Users VALUES ('izumi', 'violin');").executeUpdate();
        conn.prepareStatement("INSERT INTO Users VALUES ('alan', 'starcraft');").executeUpdate();
        conn.prepareStatement("INSERT INTO Users VALUES ('george', 'bone');").executeUpdate();
        conn.prepareStatement("INSERT INTO Users VALUES ('gracie', 'biscuit');").executeUpdate();
        conn.prepareStatement("INSERT INTO Users VALUES ('metro', 'mouse');").executeUpdate();

        conn.prepareStatement("INSERT INTO Groups VALUES ('manager', 'izumi');").executeUpdate();
        conn.prepareStatement("INSERT INTO Groups VALUES ('it', 'alan');").executeUpdate();
        conn.prepareStatement("INSERT INTO Groups VALUES ('pet', 'george');").executeUpdate();
        conn.prepareStatement("INSERT INTO Groups VALUES ('pet', 'gracie');").executeUpdate();
        conn.prepareStatement("INSERT INTO Groups VALUES ('pet', 'metro');").executeUpdate();
        conn.prepareStatement("INSERT INTO Groups VALUES ('dog', 'george');").executeUpdate();
        conn.prepareStatement("INSERT INTO Groups VALUES ('dog', 'gracie');").executeUpdate();
        conn.prepareStatement("INSERT INTO Groups VALUES ('cat', 'metro');").executeUpdate();

        conn.close();

        GBeanMBean gbean = new GBeanMBean("org.apache.geronimo.security.realm.providers.SQLSecurityRealm");
        sqlRealm = new ObjectName("geronimo.security:type=SecurityRealm,realm=sql-realm");
        gbean.setAttribute("realmName", "sql-realm");
        gbean.setAttribute("maxLoginModuleAge", new Long(1 * 1000));
        gbean.setAttribute("connectionURL", hsqldbURL);
        gbean.setAttribute("user", "loginmodule");
        gbean.setAttribute("password", "password");
        gbean.setAttribute("userSelect", "SELECT UserName, Password FROM Users");
        gbean.setAttribute("groupSelect", "SELECT GroupName, UserName FROM Groups");
        kernel.loadGBean(sqlRealm, gbean);
        kernel.startGBean(sqlRealm);
    }

    public void tearDown() throws Exception {
        kernel.stopGBean(sqlRealm);
        kernel.unloadGBean(sqlRealm);

        super.tearDown();

        Connection conn = DriverManager.getConnection(hsqldbURL, "sa", "");

        try {
            conn.prepareStatement("DROP USER loginmodule;").executeUpdate();

            conn.prepareStatement("DROP TABLE Users;").executeUpdate();
            conn.prepareStatement("DROP TABLE Groups;").executeUpdate();
        } catch (SQLException e) {
            //who knows??
        }

    }

    public void testNothing() {
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

    public void XtestLogoutTimeout() throws Exception {

        assertEquals(new Integer(State.RUNNING_INDEX), kernel.getAttribute(sqlRealm, "state"));

        ClassLoader oldCl = Thread.currentThread().getContextClassLoader();
        try {
            Thread.currentThread().setContextClassLoader(getClass().getClassLoader());
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
        } finally {
            Thread.currentThread().setContextClassLoader(oldCl);
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
