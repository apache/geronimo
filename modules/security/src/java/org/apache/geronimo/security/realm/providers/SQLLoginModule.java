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

import java.io.IOException;
import java.sql.*;
import java.util.*;
import javax.security.auth.Subject;
import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.NameCallback;
import javax.security.auth.callback.PasswordCallback;
import javax.security.auth.callback.UnsupportedCallbackException;
import javax.security.auth.login.LoginException;
import javax.security.auth.spi.LoginModule;


/**
 * @version $Rev$ $Date$
 */

public class SQLLoginModule implements LoginModule {
    private Subject subject;
    private CallbackHandler handler;
    private String cbUsername;
    private String cbPassword;
    private String connectionURL;
    private Properties properties;
    private Driver driver;
    private String userSelect;
    private String groupSelect;
    private final Set groups = new HashSet();

    public void initialize(Subject subject, CallbackHandler callbackHandler, Map sharedState, Map options) {
        this.subject = subject;
        this.handler = callbackHandler;

        connectionURL = (String) options.get(SQLSecurityRealm.CONNECTION_URL);
        properties = (Properties) options.get(SQLSecurityRealm.PROPERTIES);
        userSelect = (String) options.get(SQLSecurityRealm.USER_SELECT);
        groupSelect = (String) options.get(SQLSecurityRealm.GROUP_SELECT);
        try {
            this.driver = (Driver) Class.forName((String) options.get(SQLSecurityRealm.DRIVER)).newInstance();
        } catch (ClassNotFoundException e) {
            throw new IllegalArgumentException("Driver class "+driver+" is not available.  Perhaps you need to add it as a dependency in your deployment plan?");
        } catch(Exception e) {
            throw new IllegalArgumentException("Unable to load, instantiate, register driver "+driver+": "+e.getMessage());
        }
    }

    public boolean login() throws LoginException {
        Callback[] callbacks = new Callback[2];

        callbacks[0] = new NameCallback("User name");
        callbacks[1] = new PasswordCallback("Password", false);
        try {
            handler.handle(callbacks);
        } catch (IOException ioe) {
            throw (LoginException) new LoginException().initCause(ioe);
        } catch (UnsupportedCallbackException uce) {
            throw (LoginException) new LoginException().initCause(uce);
        }
        cbUsername = ((NameCallback) callbacks[0]).getName();
        cbPassword = new String(((PasswordCallback) callbacks[1]).getPassword());

        boolean found = false;
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

                            if (cbUsername.equals(userName) && cbPassword.equals(userPassword)) {
                                found = true;
                                break;
                            }
                        }
                    } finally {
                        result.close();
                    }
                } finally {
                    statement.close();
                }

                if (!found) return false;

                statement = conn.prepareStatement(groupSelect);
                try {
                    ResultSet result = statement.executeQuery();

                    try {
                        while (result.next()) {
                            String groupName = result.getString(1);
                            String userName = result.getString(2);

                            if (cbUsername.equals(userName)) {
                                groups.add(new SQLGroupPrincipal(groupName));
                            }
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
        } catch (SQLException sqle) {
            throw (LoginException) new LoginException("SQL error").initCause(sqle);
        }

        return true;
    }

    public boolean commit() throws LoginException {
        Set principals = subject.getPrincipals();
        principals.add(new SQLUserPrincipal(cbUsername));
        Iterator iter = groups.iterator();
        while (iter.hasNext()) {
            principals.add(iter.next());
        }

        return true;
    }

    public boolean abort() throws LoginException {
        cbUsername = null;
        cbPassword = null;

        return true;
    }

    public boolean logout() throws LoginException {
        cbUsername = null;
        cbPassword = null;

        return true;
    }
}
