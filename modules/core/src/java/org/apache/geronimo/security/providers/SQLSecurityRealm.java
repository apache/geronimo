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
 *        Apache Software Foundation (http:www.apache.org/)."
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
 * <http:www.apache.org/>.
 *
 * ====================================================================
 */
package org.apache.geronimo.security.providers;

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

import javax.security.auth.login.AppConfigurationEntry;

import org.apache.geronimo.gbean.GAttributeInfo;
import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.gbean.GBeanInfoFactory;
import org.apache.geronimo.gbean.GConstructorInfo;
import org.apache.geronimo.security.AbstractSecurityRealm;
import org.apache.geronimo.security.GeronimoSecurityException;
import org.apache.regexp.RE;


/**
 *
 * @version $Revision: 1.8 $ $Date: 2004/01/22 07:29:56 $
 */

public class SQLSecurityRealm extends AbstractSecurityRealm {

    private static final GBeanInfo GBEAN_INFO;

    private boolean running = false;
    private String connectionURL;
    private String user = "";
    private String password = "";
    private String userSelect = "SELECT UserName, Password FROM Users";
    private String groupSelect = "SELECT GroupName, UserName FROM Groups";
    final Map users = new HashMap();
    final Map groups = new HashMap();

    final static String REALM_INSTANCE = "org.apache.geronimo.security.providers.SQLSecurityRealm";

    /**
     * @deprecated
     */
    public SQLSecurityRealm() {}

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

    public AppConfigurationEntry[] getAppConfigurationEntry() {
        HashMap options = new HashMap();

        options.put(REALM_INSTANCE, this);
        AppConfigurationEntry entry = new AppConfigurationEntry("org.apache.geronimo.security.providers.SQLLoginModule",
                AppConfigurationEntry.LoginModuleControlFlag.SUFFICIENT,
                options);
        AppConfigurationEntry[] configuration = {entry};

        return configuration;
    }

    static {
        GBeanInfoFactory infoFactory = new GBeanInfoFactory(PropertiesFileSecurityRealm.class.getName(), AbstractSecurityRealm.getGBeanInfo());
        infoFactory.addAttribute(new GAttributeInfo("ConnectionURL", true));
        infoFactory.addAttribute(new GAttributeInfo("User", true));
        infoFactory.addAttribute(new GAttributeInfo("Password", true));
        infoFactory.addAttribute(new GAttributeInfo("UserSelect", true));
        infoFactory.addAttribute(new GAttributeInfo("GroupSelect", true));
        infoFactory.setConstructor(new GConstructorInfo(
                new String[] {"RealmName", "ConnectionURL", "User", "UserSelect", "GroupSelect"},
                new Class[] {String.class, String.class, String.class, String.class, String.class}));
        GBEAN_INFO = infoFactory.getBeanInfo();
    }

    public static GBeanInfo getGBeanInfo() {
        return GBEAN_INFO;
    }

}
