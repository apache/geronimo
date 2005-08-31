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
import java.util.Properties;
import javax.management.ObjectName;
import javax.security.auth.Subject;
import javax.security.auth.login.LoginContext;
import javax.security.auth.login.LoginException;

import org.apache.geronimo.gbean.GBeanData;
import org.apache.geronimo.security.AbstractTest;
import org.apache.geronimo.security.ContextManager;
import org.apache.geronimo.security.IdentificationPrincipal;
import org.apache.geronimo.security.RealmPrincipal;
import org.apache.geronimo.security.realm.GenericSecurityRealm;


/**
 * @version $Rev$ $Date$
 */
public class LoginSQLTest extends AbstractTest {

    private static final String hsqldbURL = "jdbc:hsqldb:target/database/LoginSQLTest";
    protected ObjectName sqlRealm;
    protected ObjectName sqlModule;

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

        sqlModule = new ObjectName("geronimo.security:type=LoginModule,name=sql");
        GBeanData gbean = new GBeanData(sqlModule, LoginModuleGBean.getGBeanInfo());
        gbean.setAttribute("loginModuleClass", "org.apache.geronimo.security.realm.providers.SQLLoginModule");
        gbean.setAttribute("serverSide", new Boolean(true));
        Properties props = new Properties();
        props.put("jdbcURL", hsqldbURL);
        props.put("jdbcDriver", "org.hsqldb.jdbcDriver");
        props.put("jdbcUser", "loginmodule");
        props.put("jdbcPassword", "password");
        props.put("userSelect", "SELECT UserName, Password FROM Users");
        props.put("groupSelect", "SELECT GroupName, UserName FROM Groups");
        gbean.setAttribute("options", props);
        gbean.setAttribute("loginDomainName", "SQLDomain");
        kernel.loadGBean(gbean, LoginModuleGBean.class.getClassLoader());
        kernel.startGBean(sqlModule);

        ObjectName testUseName = new ObjectName("geronimo.security:type=LoginModuleUse,name=sql");
        gbean = new GBeanData(testUseName, JaasLoginModuleUse.getGBeanInfo());
        gbean.setAttribute("controlFlag", "REQUIRED");
        gbean.setReferencePattern("LoginModule", sqlModule);
        kernel.loadGBean(gbean, JaasLoginModuleUse.class.getClassLoader());
        kernel.startGBean(testUseName);
        
        sqlRealm = new ObjectName("geronimo.security:type=SecurityRealm,realm=sql-realm");
        gbean = new GBeanData(sqlRealm, GenericSecurityRealm.getGBeanInfo());
        gbean.setAttribute("realmName", "sql-realm");
//        props = new Properties();
//        props.setProperty("LoginModule.1.REQUIRED","geronimo.security:type=LoginModule,name=sql");
//        gbean.setAttribute("loginModuleConfiguration", props);
        gbean.setReferencePattern("LoginModuleConfiguration", testUseName);
        gbean.setReferencePattern("LoginService", loginService);
        kernel.loadGBean(gbean, GenericSecurityRealm.class.getClassLoader());
        kernel.startGBean(sqlRealm);

    }

    public void tearDown() throws Exception {
        kernel.stopGBean(sqlRealm);
        kernel.stopGBean(sqlModule);
        kernel.unloadGBean(sqlRealm);
        kernel.unloadGBean(sqlModule);

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

    public void testLogin() throws Exception {
        LoginContext context = new LoginContext("sql", new UsernamePasswordCallback("alan", "starcraft"));

        context.login();
        Subject subject = context.getSubject();
        assertTrue("expected non-null client-side subject", subject != null);
        subject = ContextManager.getServerSideSubject(subject);

        assertTrue("expected non-null server-side subject", subject != null);
        assertEquals("server-side subject should have five principal", 5, subject.getPrincipals().size());
        assertEquals("server-side subject should have two realm principals", 2, subject.getPrincipals(RealmPrincipal.class).size());
        assertEquals("server-side subject should have one remote principal", 1, subject.getPrincipals(IdentificationPrincipal.class).size());
        IdentificationPrincipal principal = (IdentificationPrincipal) subject.getPrincipals(IdentificationPrincipal.class).iterator().next();
        assertTrue("id of principal should be non-zero", principal.getId().getSubjectId().longValue() != 0);

        context.logout();
    }

    public void testNullUserLogin() throws Exception {
        LoginContext context = new LoginContext("sql", new UsernamePasswordCallback(null, "starcraft"));

        try {
            context.login();
            fail("Should not allow this login with null username");
        } catch (LoginException e) {
        }
    }

    public void testNullPasswordLogin() throws Exception {
        LoginContext context = new LoginContext("sql", new UsernamePasswordCallback("alan", null));

        try {
            context.login();
            fail("Should not allow this login with null password");
        } catch (LoginException e) {
        }
    }
}
