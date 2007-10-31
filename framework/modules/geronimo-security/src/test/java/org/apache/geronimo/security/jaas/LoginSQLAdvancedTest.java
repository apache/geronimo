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

import org.apache.geronimo.gbean.AbstractName;
import org.apache.geronimo.gbean.GBeanData;
import org.apache.geronimo.security.AbstractTest;
import org.apache.geronimo.security.realm.GenericSecurityRealm;


/**
 * This test makes sure that SQLLoginModule does not add any principals when login fails
 * @version $Rev$ $Date$
 */
public class LoginSQLAdvancedTest extends AbstractTest {
    private File basedir = new File(System.getProperty("basedir"));
    private String hsqldbURL = "jdbc:hsqldb:" + new File(basedir, "target/database/LoginSQLTest");
    
    protected AbstractName sqlRealm;
    protected AbstractName sqlModule;
    protected AbstractName neverFailModule;

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

        GBeanData gbean;
        gbean = buildGBeanData("name", "NeverFailLoginModule", LoginModuleGBean.getGBeanInfo());
        neverFailModule = gbean.getAbstractName();
        gbean.setAttribute("loginModuleClass", "org.apache.geronimo.security.jaas.NeverFailLoginModule");
        gbean.setAttribute("options", null);
        gbean.setAttribute("loginDomainName", "NeverFailDomain");
        gbean.setAttribute("wrapPrincipals", Boolean.TRUE);
        kernel.loadGBean(gbean, LoginModuleGBean.class.getClassLoader());
        kernel.startGBean(neverFailModule);

        gbean = buildGBeanData("name", "SQLLoginModule", LoginModuleGBean.getGBeanInfo());
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
        AbstractName sqlUseName = gbean.getAbstractName();
        gbean.setAttribute("controlFlag", LoginModuleControlFlag.OPTIONAL);
        gbean.setReferencePattern("LoginModule", sqlModule);
        kernel.loadGBean(gbean, JaasLoginModuleUse.class.getClassLoader());
        kernel.startGBean(sqlUseName);

        gbean = buildGBeanData("name", "NeverFailLoginModuleUse", JaasLoginModuleUse.getGBeanInfo());
        AbstractName neverFailUseName = gbean.getAbstractName();
        gbean.setAttribute("controlFlag", LoginModuleControlFlag.REQUIRED);
        gbean.setReferencePattern("LoginModule", neverFailModule);
        gbean.setReferencePattern("Next", sqlUseName);
        kernel.loadGBean(gbean, JaasLoginModuleUse.class.getClassLoader());
        kernel.startGBean(neverFailUseName);
        
        gbean = buildGBeanData("name", "SQLSecurityRealm", GenericSecurityRealm.getGBeanInfo());
        sqlRealm = gbean.getAbstractName();
        gbean.setAttribute("realmName", "sql-realm");
        gbean.setReferencePattern("LoginModuleConfiguration", neverFailUseName);
        kernel.loadGBean(gbean, GenericSecurityRealm.class.getClassLoader());
        kernel.startGBean(sqlRealm);

    }

    public void tearDown() throws Exception {
        kernel.stopGBean(sqlRealm);
        kernel.stopGBean(sqlModule);
        kernel.stopGBean(neverFailModule);
        kernel.unloadGBean(sqlRealm);
        kernel.unloadGBean(sqlModule);
        kernel.unloadGBean(neverFailModule);

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

    public void testBadPasswordLogin() throws Exception {
        LoginContext context = new LoginContext("sql-realm", new UsernamePasswordCallback("alan", "bad"));

        context.login();
        Subject subject = context.getSubject();
        assertTrue("expected non-null subject", subject != null);
        assertTrue(subject.getPrincipals().size() == 0);
        context.logout();
    }
}
