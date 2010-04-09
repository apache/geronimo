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

package org.apache.geronimo.security.realm.providers;

import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.Principal;
import java.sql.Connection;
import java.sql.Driver;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import javax.security.auth.Subject;
import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.NameCallback;
import javax.security.auth.callback.PasswordCallback;
import javax.security.auth.callback.UnsupportedCallbackException;
import javax.security.auth.login.FailedLoginException;
import javax.security.auth.login.LoginException;
import javax.security.auth.spi.LoginModule;
import javax.sql.DataSource;

import org.apache.geronimo.kernel.NoSuchOperationException;
import org.osgi.framework.Bundle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.geronimo.gbean.AbstractName;
import org.apache.geronimo.gbean.AbstractNameQuery;
import org.apache.geronimo.kernel.GBeanNotFoundException;
import org.apache.geronimo.kernel.Kernel;
import org.apache.geronimo.kernel.KernelRegistry;
import org.apache.geronimo.management.geronimo.JCAManagedConnectionFactory;
import org.apache.geronimo.security.jaas.JaasLoginModuleUse;
import org.apache.geronimo.security.jaas.WrappingLoginModule;
import org.apache.geronimo.crypto.encoders.Base64;
import org.apache.geronimo.crypto.encoders.HexTranslator;

/**
 * A login module that loads security information from a SQL database.  Expects
 * to be run by a GenericSecurityRealm (doesn't work on its own).
 * <p/>
 * This requires database connectivity information (either 1: a dataSourceName and
 * optional dataSourceApplication or 2: a JDBC driver, URL, username, and password)
 * and 2 SQL queries.
 * <p/>
 * The userSelect query should return 2 values, the username and the password in
 * that order.  It should include one PreparedStatement parameter (a ?) which
 * will be filled in with the username.  In other words, the query should look
 * like: <tt>SELECT user, password FROM credentials WHERE username=?</tt>
 * <p/>
 * The groupSelect query should return 2 values, the username and the group name in
 * that order (but it may return multiple rows, one per group).  It should include
 * one PreparedStatement parameter (a ?) which will be filled in with the username.
 * In other words, the query should look like:
 * <tt>SELECT user, role FROM user_roles WHERE username=?</tt>
 * <p/>
 * This login module checks security credentials so the lifecycle methods must return true to indicate success
 * or throw LoginException to indicate failure.
 *
 * @version $Rev$ $Date$
 */
public class SQLLoginModule implements LoginModule {
    private static final Logger log = LoggerFactory.getLogger(SQLLoginModule.class);
    
    public final static String USER_SELECT = "userSelect";
    public final static String GROUP_SELECT = "groupSelect";
    public final static String CONNECTION_URL = "jdbcURL";
    public final static String USER = "jdbcUser";
    public final static String PASSWORD = "jdbcPassword";
    public final static String DRIVER = "jdbcDriver";
    public final static String DATABASE_POOL_NAME = "dataSourceName";
    public final static String DATABASE_POOL_APP_NAME = "dataSourceApplication";
    public final static String DIGEST = "digest";
    public final static String ENCODING = "encoding";
    public final static List<String> supportedOptions = Collections.unmodifiableList(Arrays.asList(USER_SELECT, GROUP_SELECT, CONNECTION_URL,
            USER, PASSWORD, DRIVER, DATABASE_POOL_NAME, DATABASE_POOL_APP_NAME, DIGEST, ENCODING));

    private String connectionURL;
    private Properties properties;
    private Driver driver;
    private DataSource dataSource;
    private String userSelect;
    private String groupSelect;
    private String digest;
    private String encoding;

    private boolean loginSucceeded;
    private Subject subject;
    private CallbackHandler handler;
    private String cbUsername;
    private String cbPassword;
    private final Set<String> groups = new HashSet<String>();
    private final Set<Principal> allPrincipals = new HashSet<Principal>();

    public void initialize(Subject subject, CallbackHandler callbackHandler, Map sharedState, Map options) {
        this.subject = subject;
        this.handler = callbackHandler;
        for(Object option: options.keySet()) {
            if(!supportedOptions.contains(option) && !JaasLoginModuleUse.supportedOptions.contains(option)
                    && !WrappingLoginModule.supportedOptions.contains(option)) {
                log.warn("Ignoring option: "+option+". Not supported.");
            }
        }
        userSelect = (String) options.get(USER_SELECT);
        groupSelect = (String) options.get(GROUP_SELECT);

        digest = (String) options.get(DIGEST);
        encoding = (String) options.get(ENCODING);
        if (digest != null && !digest.equals("")) {
            // Check if the digest algorithm is available
            try {
                MessageDigest.getInstance(digest);
            } catch (NoSuchAlgorithmException e) {
                log.error("Initialization failed. Digest algorithm " + digest + " is not available.", e);
                throw new IllegalArgumentException("Unable to configure SQL login module: " + e.getMessage(), e);
            }
            if (encoding != null && !"hex".equalsIgnoreCase(encoding) && !"base64".equalsIgnoreCase(encoding)) {
                log.error("Initialization failed. Digest Encoding " + encoding + " is not supported.");
                throw new IllegalArgumentException(
                        "Unable to configure SQL login module. Digest Encoding " + encoding + " not supported.");
            }
        }

        String dataSourceName = (String) options.get(DATABASE_POOL_NAME);
        if (dataSourceName != null) {
            dataSourceName = dataSourceName.trim();
            String dataSourceAppName = (String) options.get(DATABASE_POOL_APP_NAME);
            if (dataSourceAppName == null || dataSourceAppName.trim().equals("")) {
                dataSourceAppName = "null";
            } else {
                dataSourceAppName = dataSourceAppName.trim();
            }
            String kernelName = (String) options.get(JaasLoginModuleUse.KERNEL_NAME_LM_OPTION);
            Kernel kernel = KernelRegistry.getKernel(kernelName);
            Map<String, String> nameMap = new HashMap<String, String>();
            nameMap.put("name", dataSourceName);
            nameMap.put("J2EEApplication", dataSourceAppName);
            nameMap.put("j2eeType", "JCAConnectionManager");
            Set<AbstractName> set = kernel.listGBeans(new AbstractNameQuery(null, nameMap));
            for (AbstractName name : set) {
                try {
                    dataSource = (DataSource) kernel.invoke(name, "createConnectionFactory");
                    break;
                } catch (GBeanNotFoundException e) {
                    // ignore... GBean was unregistered
                } catch (NoSuchOperationException e) {

                } catch (Exception e) {

                }
            }
        } else {
            connectionURL = (String) options.get(CONNECTION_URL);
            properties = new Properties();
            if (options.get(USER) != null) {
                properties.put("user", options.get(USER));
            }
            if (options.get(PASSWORD) != null) {
                properties.put("password", options.get(PASSWORD));
            }
            Bundle cl = (Bundle) options.get(JaasLoginModuleUse.CLASSLOADER_LM_OPTION);
            try {
                driver = (Driver) cl.loadClass((String) options.get(DRIVER)).newInstance();
            } catch (ClassNotFoundException e) {
                throw new IllegalArgumentException("Driver class " + options.get(
                        DRIVER) + " is not available.  Perhaps you need to add it as a dependency in your deployment plan?",
                        e);
            } catch (Exception e) {
                throw new IllegalArgumentException(
                        "Unable to load, instantiate, register driver " + options.get(DRIVER) + ": " + e.getMessage(),
                        e);
            }
        }
    }

    /**
     * This LoginModule is not to be ignored.  So, this method should never return false.
     * @return true if authentication succeeds, or throw a LoginException such as FailedLoginException
     *         if authentication fails
     */
    public boolean login() throws LoginException {
        loginSucceeded = false;
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
        assert callbacks.length == 2;
        cbUsername = ((NameCallback) callbacks[0]).getName();
        if (cbUsername == null || cbUsername.equals("")) {
            throw new FailedLoginException();
        }
        char[] provided = ((PasswordCallback) callbacks[1]).getPassword();
        cbPassword = provided == null ? null : new String(provided);

        try {
            Connection conn;
            if (dataSource != null) {
                conn = dataSource.getConnection();
            } else {
                conn = driver.connect(connectionURL, properties);
            }

            try {
                PreparedStatement statement = conn.prepareStatement(userSelect);
                try {
                    int count = countParameters(userSelect);
                    for (int i = 0; i < count; i++) {
                        statement.setObject(i + 1, cbUsername);
                    }
                    ResultSet result = statement.executeQuery();

                    try {
                        boolean found = false;
                        while (result.next()) {
                            String userName = result.getString(1);
                            String userPassword = result.getString(2);

                            if (cbUsername.equals(userName)) {
                                found = true;
                                if (!checkPassword(userPassword, cbPassword)) {
                                    throw new FailedLoginException();
                                }
                                break;
                            }
                        }
                        if(!found) {
                            // User does not exist
                            throw new FailedLoginException();
                        }
                    } finally {
                        result.close();
                    }
                } finally {
                    statement.close();
                }

                statement = conn.prepareStatement(groupSelect);
                try {
                    int count = countParameters(groupSelect);
                    for (int i = 0; i < count; i++) {
                        statement.setObject(i + 1, cbUsername);
                    }
                    ResultSet result = statement.executeQuery();

                    try {
                        while (result.next()) {
                            String userName = result.getString(1);
                            String groupName = result.getString(2);

                            if (cbUsername.equals(userName)) {
                                groups.add(groupName);
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
        } catch (LoginException e) {
            // Clear out the private state
            cbUsername = null;
            cbPassword = null;
            groups.clear();
            throw e;
        } catch (SQLException sqle) {
            // Clear out the private state
            cbUsername = null;
            cbPassword = null;
            groups.clear();
            throw (LoginException) new LoginException("SQL error").initCause(sqle);
        } catch (Exception e) {
            // Clear out the private state
            cbUsername = null;
            cbPassword = null;
            groups.clear();
            throw (LoginException) new LoginException("Could not access datasource").initCause(e);
        }

        loginSucceeded = true;
        return true;
    }

    /*
     * @exception LoginException if login succeeded but commit failed.
     *
     * @return true if login succeeded and commit succeeded, or false if login failed but commit succeeded.
     */
    public boolean commit() throws LoginException {
        if(loginSucceeded) {
            if(cbUsername != null) {
                allPrincipals.add(new GeronimoUserPrincipal(cbUsername));
            }
            for(String group: groups) {
                allPrincipals.add(new GeronimoGroupPrincipal(group));
            }
            subject.getPrincipals().addAll(allPrincipals);
        }

        // Clear out the private state
        cbUsername = null;
        cbPassword = null;
        groups.clear();

        return loginSucceeded;
    }

    public boolean abort() throws LoginException {
        if(loginSucceeded) {
            // Clear out the private state
            cbUsername = null;
            cbPassword = null;
            groups.clear();
            allPrincipals.clear();
        }
        return loginSucceeded;
    }

    public boolean logout() throws LoginException {
        // Clear out the private state
        loginSucceeded = false;
        cbUsername = null;
        cbPassword = null;
        groups.clear();
        if(!subject.isReadOnly()) {
            // Remove principals added by this LoginModule
            subject.getPrincipals().removeAll(allPrincipals);
        }
        allPrincipals.clear();
        return true;
    }

    private static int countParameters(String sql) {
        int count = 0;
        int pos = -1;
        while ((pos = sql.indexOf('?', pos + 1)) != -1) {
            ++count;
        }
        return count;
    }

    /**
     * This method checks if the provided password is correct.  The original password may have been digested.
     *
     * @param real     Original password in digested form if applicable
     * @param provided User provided password in clear text
     * @return true     If the password is correct
     */
    private boolean checkPassword(String real, String provided) {
        if (real == null && provided == null) {
            return true;
        }
        if (real == null || provided == null) {
            return false;
        }

        //both are non-null
        if (digest == null || digest.equals("")) {
            // No digest algorithm is used
            return real.equals(provided);
        }
        try {
            // Digest the user provided password
            MessageDigest md = MessageDigest.getInstance(digest);
            byte[] data = md.digest(provided.getBytes());
            if (encoding == null || "hex".equalsIgnoreCase(encoding)) {
                // Convert bytes to hex digits
                byte[] hexData = new byte[data.length * 2];
                HexTranslator ht = new HexTranslator();
                ht.encode(data, 0, data.length, hexData, 0);
                // Compare the digested provided password with the actual one
                return real.equalsIgnoreCase(new String(hexData));
            } else if ("base64".equalsIgnoreCase(encoding)) {
                return real.equals(new String(Base64.encode(data)));
            }
        } catch (NoSuchAlgorithmException e) {
            // Should not occur.  Availability of algorithm has been checked at initialization
            log.error("Should not occur.  Availability of algorithm has been checked at initialization.", e);
        }
        return false;
    }
}
