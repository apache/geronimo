/**
 *
 * Copyright 2004 The Apache Software Foundation
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

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.apache.geronimo.gbean.GAttributeInfo;
import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.gbean.GBeanInfoFactory;
import org.apache.geronimo.gbean.GConstructorInfo;
import org.apache.geronimo.gbean.GOperationInfo;
import org.apache.geronimo.security.GeronimoSecurityException;
import org.apache.regexp.RE;


/**
 * @version $Revision: 1.3 $ $Date: 2004/02/25 09:58:09 $
 */

public class SQLSecurityRealm extends AbstractSecurityRealm {

    private static final GBeanInfo GBEAN_INFO;
    public final static String USER_SELECT = "org.apache.geronimo.security.realm.providers.SQLSecurityRealm.USER_SELECT";
    public final static String GROUP_SELECT = "org.apache.geronimo.security.realm.providers.SQLSecurityRealm.GROUP_SELECT";
    public final static String CONNECTION_URL = "org.apache.geronimo.security.realm.providers.SQLSecurityRealm.CONNECTION_URL";
    public final static String USERNAME = "org.apache.geronimo.security.realm.providers.SQLSecurityRealm.USERNAME";
    public final static String PASSWORD = "org.apache.geronimo.security.realm.providers.SQLSecurityRealm.PASSWORD";

    private boolean running = false;
    private String connectionURL;
    private String user = "";
    private String password = "";
    private String userSelect = "SELECT UserName, Password FROM Users";
    private String groupSelect = "SELECT GroupName, UserName FROM Groups";
    final Map users = new HashMap();
    final Map groups = new HashMap();

    /**
     * @deprecated
     */
    public SQLSecurityRealm() {
    }

    public SQLSecurityRealm(String realmName, String connectionURL, String user, String password, String userSelect, String groupSelect) {
        super(realmName);
        this.connectionURL = connectionURL;
        this.user = user;
        this.password = password;
        this.userSelect = userSelect;
        this.groupSelect = groupSelect;
    }

    public void doStart() {
        if (connectionURL == null) throw  new IllegalStateException("Connection URI not set");

        refresh();
        running = true;
    }

    public void doStop() {
        running = false;

        users.clear();
        groups.clear();
    }

    public String getConnectionURL() {
        return connectionURL;
    }

    public void setConnectionURL(String connectionURL) {
        if (running) {
            throw new IllegalStateException("Cannot change the Connection URI after the realm is started");
        }
        this.connectionURL = connectionURL;
    }

    public String getUser() {
        return user;
    }

    public void setPassword(String password) {
        if (running) {
            throw new IllegalStateException("Cannot change the connection password after the realm is started");
        }
        this.password = password;
    }

    public String getPassword() {
        return password;
    }

    public void setUser(String user) {
        if (running) {
            throw new IllegalStateException("Cannot change the connection user after the realm is started");
        }
        this.user = user;
    }

    public String getUserSelect() {
        return userSelect;
    }

    public void setUserSelect(String userSelect) {
        if (running) {
            throw new IllegalStateException("Cannot change the user SQL select statement after the realm is started");
        }
        this.userSelect = userSelect;
    }

    public String getGroupSelect() {
        return groupSelect;
    }

    public void setGroupSelect(String groupSelect) {
        if (running) {
            throw new IllegalStateException("Cannot change the group SQL select statement after the realm is started");
        }
        this.groupSelect = groupSelect;
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
        users.clear();
        groups.clear();
        Map users = new HashMap();
        Map groups = new HashMap();
        try {
            Connection conn = DriverManager.getConnection(connectionURL, user, password);

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

        } catch (SQLException sqle) {
            throw new GeronimoSecurityException(sqle);
        }
    }

    String obfuscate(String password) {
        return password;
    }

    public AppConfigurationEntry getAppConfigurationEntry() {
        HashMap options = new HashMap();

        options.put(USER_SELECT, userSelect);
        options.put(GROUP_SELECT, groupSelect);
        options.put(CONNECTION_URL, connectionURL);
        options.put(USERNAME, user);
        options.put(PASSWORD, password);

        AppConfigurationEntry entry = new AppConfigurationEntry("org.apache.geronimo.security.realm.providers.SQLLoginModule",
                                                                AppConfigurationEntry.LoginModuleControlFlag.SUFFICIENT,
                                                                options);

        return entry;
    }

    public boolean isLoginModuleLocal() {
        return true;
    }

    static {
        GBeanInfoFactory infoFactory = new GBeanInfoFactory(SQLSecurityRealm.class.getName(), AbstractSecurityRealm.getGBeanInfo());
        infoFactory.addAttribute(new GAttributeInfo("ConnectionURL", true));
        infoFactory.addAttribute(new GAttributeInfo("User", true));
        infoFactory.addAttribute(new GAttributeInfo("Password", true));
        infoFactory.addAttribute(new GAttributeInfo("UserSelect", true));
        infoFactory.addAttribute(new GAttributeInfo("GroupSelect", true));
        infoFactory.addOperation(new GOperationInfo("isLoginModuleLocal"));
        infoFactory.setConstructor(new GConstructorInfo(new String[]{"RealmName", "ConnectionURL", "User", "Password", "UserSelect", "GroupSelect"},
                                                        new Class[]{String.class, String.class, String.class, String.class, String.class, String.class}));
        GBEAN_INFO = infoFactory.getBeanInfo();
    }

    public static GBeanInfo getGBeanInfo() {
        return GBEAN_INFO;
    }

}
