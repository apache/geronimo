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

package org.apache.geronimo.security.realm.providers;

import javax.security.auth.login.AppConfigurationEntry;
import java.sql.*;
import java.util.*;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.regexp.RE;

import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.gbean.GBeanInfoBuilder;
import org.apache.geronimo.common.GeronimoSecurityException;
import org.apache.geronimo.security.deploy.Principal;
import org.apache.geronimo.security.realm.AutoMapAssistant;


/**
 * @version $Rev$ $Date$
 */

public class SQLSecurityRealm extends AbstractSecurityRealm implements AutoMapAssistant {

    private static Log log = LogFactory.getLog(SQLSecurityRealm.class);
    public final static String USER_SELECT = "org.apache.geronimo.security.realm.providers.SQLSecurityRealm.USER_SELECT";
    public final static String GROUP_SELECT = "org.apache.geronimo.security.realm.providers.SQLSecurityRealm.GROUP_SELECT";
    public final static String CONNECTION_URL = "org.apache.geronimo.security.realm.providers.SQLSecurityRealm.CONNECTION_URL";
    public final static String PROPERTIES = "org.apache.geronimo.security.realm.providers.SQLSecurityRealm.PROPERTIES";
    public final static String DRIVER = "org.apache.geronimo.security.realm.providers.SQLSecurityRealm.DRIVER";

    private boolean running = false;
    private String connectionURL;
    private String userSelect = "SELECT UserName, Password FROM Users";
    private String groupSelect = "SELECT GroupName, UserName FROM Groups";
    private Driver driver;
    private Properties properties;
    private final Map users = new HashMap();
    private final Map groups = new HashMap();
    private String defaultPrincipal;

    /**
     * @deprecated
     */
    public SQLSecurityRealm() {
    }

    public SQLSecurityRealm(String realmName, String driver, String connectionURL, String user, String password, String userSelect, String groupSelect, ClassLoader classLoader) {
        super(realmName);
        this.connectionURL = connectionURL;
        properties = new Properties();
        properties.setProperty("user", user);
        properties.setProperty("password", password);
        this.userSelect = userSelect;
        this.groupSelect = groupSelect;
        try {
            this.driver = (Driver) classLoader.loadClass(driver).newInstance();
        } catch (ClassNotFoundException e) {
            throw new IllegalArgumentException("Driver class "+driver+" is not available.  Perhaps you need to add it as a dependency in your deployment plan?");
        } catch(Exception e) {
            throw new IllegalArgumentException("Unable to load, instantiate, register driver "+driver+": "+e.getMessage());
        }
    }

    public void doStart() {
        if (connectionURL == null) throw  new IllegalStateException("Connection URI not set");

        refresh();
        running = true;

        log.info("SQL Realm - " + getRealmName() + " - started");
    }

    public void doStop() {
        running = false;

        users.clear();
        groups.clear();

        log.info("SQL Realm - " + getRealmName() + " - stopped");
    }

    public String getConnectionURL() {
        return connectionURL;
    }

    public String getUser() {
        return properties.getProperty("user");
    }

    public String getPassword() {
        return properties.getProperty("password");
    }

    public String getUserSelect() {
        return userSelect;
    }

    public String getGroupSelect() {
        return groupSelect;
    }

    public String getDefaultPrincipal() {
        return defaultPrincipal;
    }

    public Set getGroupPrincipals() throws GeronimoSecurityException {
        if (!running) {
            throw new IllegalStateException("Cannot obtain Groups until the realm is started");
        }
        return Collections.unmodifiableSet(groups.keySet());
    }

    public Set getGroupPrincipals(RE regexExpression) throws GeronimoSecurityException {
        if (!running) {
            throw new IllegalStateException("Cannot obtain Groups until the realm is started");
        }
        HashSet result = new HashSet();
        Iterator iter = groups.keySet().iterator();
        String group;
        while (iter.hasNext()) {
            group = (String) iter.next();

            if (regexExpression.match(group)) {
                result.add(group);
            }
        }

        return result;
    }

    public Set getUserPrincipals() throws GeronimoSecurityException {
        if (!running) {
            throw new IllegalStateException("Cannot obtain Users until the realm is started");
        }
        return Collections.unmodifiableSet(users.keySet());
    }

    public Set getUserPrincipals(RE regexExpression) throws GeronimoSecurityException {
        if (!running) {
            throw new IllegalStateException("Cannot obtain Users until the realm is started");
        }
        HashSet result = new HashSet();
        Iterator iter = users.keySet().iterator();
        String user;
        while (iter.hasNext()) {
            user = (String) iter.next();

            if (regexExpression.match(user)) {
                result.add(user);
            }
        }

        return result;
    }

    public void refresh() throws GeronimoSecurityException {
        java.util.Enumeration e = DriverManager.getDrivers();
        while(e.hasMoreElements()) {System.err.println("Refresh Driver: "+e.nextElement().getClass().getName());}

        users.clear();
        groups.clear();
        Map users = new HashMap();
        Map groups = new HashMap();
        try {
            Connection conn = driver.connect(connectionURL, properties);

            try {
                PreparedStatement statement = conn.prepareStatement(userSelect);
                try {
                    ResultSet result = statement.executeQuery();

                    try {
                        while (result.next()) {
                            String userName = result.getString(1);
                            String userPassword = result.getString(2);

                            users.put(userName, userPassword);
                        }
                    } finally {
                        result.close();
                    }
                } finally {
                    statement.close();
                }

                statement = conn.prepareStatement(groupSelect);
                try {
                    ResultSet result = statement.executeQuery();

                    try {
                        while (result.next()) {
                            String groupName = result.getString(1);
                            String userName = result.getString(2);

                            Set userset = (Set) groups.get(groupName);
                            if (userset == null) {
                                userset = new HashSet();
                                groups.put(groupName, userset);
                            }
                            userset.add(userName);
                        }
                    } finally {
                        result.close();
                    }
                } finally {
                    statement.close();
                }
            } finally {
                conn.close();
            }

            //copy results if no exception
            //calling refresh is not thread safe wrt authorization calls.
            this.users.putAll(users);
            this.groups.putAll(groups);

            log.info("SQL Realm - " + getRealmName() + " - refresh");
        } catch (SQLException sqle) {
            log.info("SQL Realm - " + getRealmName() + " - refresh failed");
            throw new GeronimoSecurityException(sqle);
        }
    }

    String obfuscate(String password) {
        return password;
    }

    public AppConfigurationEntry[] getAppConfigurationEntries() {
        HashMap options = new HashMap();

        options.put(USER_SELECT, userSelect);
        options.put(GROUP_SELECT, groupSelect);
        options.put(CONNECTION_URL, connectionURL);
        options.put(PROPERTIES, properties);
        options.put(DRIVER, driver);

        AppConfigurationEntry entry = new AppConfigurationEntry("org.apache.geronimo.security.realm.providers.SQLLoginModule",
                                                                AppConfigurationEntry.LoginModuleControlFlag.SUFFICIENT,
                                                                options);

        return new AppConfigurationEntry[]{entry};
    }

    public boolean isLoginModuleLocal() {
        return true;
    }

    /**
     * Provides the default principal to be used when an unauthenticated
     * subject uses a container.
     *
     * @return the default principal
     */
    public Principal obtainDefaultPrincipal() {
        Principal principal = new Principal();

        principal.setClassName(PropertiesFileUserPrincipal.class.getName());
        principal.setPrincipalName(defaultPrincipal);

        return principal;
    }

    /**
     * Provides a set of principal class names to be used when automatically
     * mapping principals to roles.
     *
     * @return a set of principal class names
     */
    public Set obtainRolePrincipalClasses() {
        Set principals = new HashSet();

        principals.add(PropertiesFileGroupPrincipal.class.getName());

        return principals;
    }

    public static final GBeanInfo GBEAN_INFO;

    static {
        GBeanInfoBuilder infoFactory = new GBeanInfoBuilder(SQLSecurityRealm.class, AbstractSecurityRealm.GBEAN_INFO);

        infoFactory.addInterface(AutoMapAssistant.class);
        infoFactory.addAttribute("connectionURL", String.class, true);
        infoFactory.addAttribute("user", String.class, true);
        infoFactory.addAttribute("driver", String.class, true);
        infoFactory.addAttribute("password", String.class, true);
        infoFactory.addAttribute("userSelect", String.class, true);
        infoFactory.addAttribute("groupSelect", String.class, true);
        infoFactory.addAttribute("defaultPrincipal", String.class, true);
        infoFactory.addAttribute("classLoader", ClassLoader.class, false);

        infoFactory.setConstructor(new String[]{
            "realmName",
            "driver",
            "connectionURL",
            "user",
            "password",
            "userSelect",
            "groupSelect",
            "classLoader"});

        GBEAN_INFO = infoFactory.getBeanInfo();
    }

    public static GBeanInfo getGBeanInfo() {
        return GBEAN_INFO;
    }
}
