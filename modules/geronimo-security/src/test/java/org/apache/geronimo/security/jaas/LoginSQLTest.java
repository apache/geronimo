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

package org.apache.geronimo.security.jaas;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import javax.security.auth.Subject;
import javax.security.auth.login.LoginContext;
import javax.security.auth.login.LoginException;

import org.apache.geronimo.gbean.AbstractName;
import org.apache.geronimo.gbean.GBeanData;
import org.apache.geronimo.security.AbstractTest;
import org.apache.geronimo.security.ContextManager;
import org.apache.geronimo.security.DomainPrincipal;
import org.apache.geronimo.security.IdentificationPrincipal;
import org.apache.geronimo.security.RealmPrincipal;
import org.apache.geronimo.security.realm.GenericSecurityRealm;


/**
 * @version $Rev$ $Date$
 */
public class LoginSQLTest extends AbstractTest {
    private File basedir = new File(System.getProperty("basedir"));
    private String hsqldbURL = "jdbc:hsqldb:" + new File(basedir, "target/database/LoginSQLTest");
    
    protected AbstractName sqlRealm;
    protected AbstractName sqlModule;

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

        GBeanData gbean = buildGBeanData("name", "SQLLoginModule", LoginModuleGBean.getGBeanInfo());
        sqlModule = gbean.getAbstractName();
        gbean.setAttribute("loginModuleClass", "org.apache.geronimo.security.realm.providers.SQLLoginModule");
        Map<String, Object> props = new HashMap<String, Object>();
        props.put("jdbcURL", hsqldbURL);
        props.put("jdbcDriver", "org.hsqldb.jdbcDriver");
        props.put("jdbcUser", "loginmodule");
        props.put("jdbcPassword", "password");
        props.put("userSelect", "SELECT UserName, Password FROM Users where UserName = ?");
        props.put("groupSelect", "SELECT UserName, GroupName FROM Groups where UserName = ?");
        gbean.setAttribute("options", props);
        gbean.setAttribute("loginDomainName", "SQLDomain");
        gbean.setAttribute("wrapPrincipals", Boolean.TRUE);
        kernel.loadGBean(gbean, LoginModuleGBean.class.getClassLoader());
        kernel.startGBean(sqlModule);

        gbean = buildGBeanData("name", "SQLLoginModuleUse", JaasLoginModuleUse.getGBeanInfo());
        AbstractName testUseName = gbean.getAbstractName();
        gbean.setAttribute("controlFlag", LoginModuleControlFlag.REQUIRED);
        gbean.setReferencePattern("LoginModule", sqlModule);
        kernel.loadGBean(gbean, JaasLoginModuleUse.class.getClassLoader());
        kernel.startGBean(testUseName);

        gbean = buildGBeanData("name", "SQLSecurityRealm", GenericSecurityRealm.getGBeanInfo());
        sqlRealm = gbean.getAbstractName();
        gbean.setAttribute("realmName", "sql-realm");
        gbean.setReferencePattern("LoginModuleConfiguration", testUseName);
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
        LoginContext context = new LoginContext("sql-realm", new UsernamePasswordCallback("alan", "starcraft"));

        context.login();
        Subject subject = context.getSubject();
        assertTrue("expected non-null subject", subject != null);
        assertEquals("server-side subject should have 6 principal", 6, subject.getPrincipals().size());
        assertEquals("server-side subject should have two realm principals", 2, subject.getPrincipals(RealmPrincipal.class).size());
        assertEquals("server-side subject should have two domain principals", 2, subject.getPrincipals(DomainPrincipal.class).size());

        context.logout();
    }

    public void testNullUserLogin() throws Exception {
        LoginContext context = new LoginContext("sql-realm", new UsernamePasswordCallback(null, "starcraft"));

        try {
            context.login();
            fail("Should not allow this login with null username");
        } catch (LoginException e) {
        }
    }

    public void testNullPasswordLogin() throws Exception {
        LoginContext context = new LoginContext("sql-realm", new UsernamePasswordCallback("alan", null));

        try {
            context.login();
            fail("Should not allow this login with null password");
        } catch (LoginException e) {
        }
    }
}
