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
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.security.auth.Subject;
import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.UnsupportedCallbackException;
import javax.security.auth.login.FailedLoginException;
import javax.security.auth.login.LoginException;
import javax.security.auth.spi.LoginModule;
import javax.sql.DataSource;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.geronimo.gbean.AbstractName;
import org.apache.geronimo.gbean.AbstractNameQuery;
import org.apache.geronimo.kernel.GBeanNotFoundException;
import org.apache.geronimo.kernel.Kernel;
import org.apache.geronimo.kernel.KernelRegistry;
import org.apache.geronimo.kernel.NoSuchOperationException;
import org.apache.geronimo.security.jaas.JaasLoginModuleUse;
import org.apache.geronimo.security.jaas.WrappingLoginModule;

public class GenericHttpHeaderSqlLoginmodule extends GenericHttpHeaderLoginmodule implements LoginModule {
    private static Log log = LogFactory.getLog(GenericHttpHeaderSqlLoginmodule.class);

    private final static String GROUP_SELECT = "groupSelect";
    private final static String USER = "jdbcUser";
    private final static String DATABASE_POOL_NAME = "dataSourceName";
    private final static String DATABASE_POOL_APP_NAME = "dataSourceApplication";
    private final static String HEADER_NAMES = "headerNames";
    private final static String AUTHENTICATION_AUTHORITY = "authenticationAuthority";
    private final static List<String> supportedOptions = Collections.unmodifiableList(Arrays.asList(GROUP_SELECT, USER,
            DATABASE_POOL_NAME, DATABASE_POOL_APP_NAME, HEADER_NAMES, AUTHENTICATION_AUTHORITY));
    private String groupSelect;

    private DataSource dataSource;

    public boolean abort() throws LoginException {
        if (loginSucceeded) {
            // Clear out the private state
            username = null;
            groups.clear();
            allPrincipals.clear();
        }
        return loginSucceeded;
    }

    public boolean commit() throws LoginException {
        if (loginSucceeded) {
            if (username != null) {
                super.commitHelper();
            }
        }

        // Clear out the private state
        username = null;
        groups.clear();

        return loginSucceeded;
    }

    public void initialize(Subject subject, CallbackHandler callbackHandler, Map<String, ?> sharedState,
            Map<String, ?> options) {
        this.subject = subject;
        this.callbackHandler = callbackHandler;
        for (Object option : options.keySet()) {
            if (!supportedOptions.contains(option) && !JaasLoginModuleUse.supportedOptions.contains(option)
                    && !WrappingLoginModule.supportedOptions.contains(option)) {
                log.warn("Ignoring option: " + option + ". Not supported.");
            }
        }
        headerNames = (String) options.get(HEADER_NAMES);
        authenticationAuthority = (String) options.get(AUTHENTICATION_AUTHORITY);
        groupSelect = (String) options.get(GROUP_SELECT);
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
        }
    }

    public boolean login() throws LoginException {
        Map<String, String> headerMap = null;
        loginSucceeded = false;
        Connection conn = null;
        ResultSet result = null;
        PreparedStatement statement = null;
        Callback[] callbacks = new Callback[1];
        callbacks[0] = new RequestCallback();
        try {
            callbackHandler.handle(callbacks);
        } catch (IOException ioe) {
            throw (LoginException) new LoginException().initCause(ioe);
        } catch (UnsupportedCallbackException uce) {
            throw (LoginException) new LoginException().initCause(uce);
        }
        httpRequest = ((RequestCallback) callbacks[0]).getRequest();
        String[] headers = headerNames.split(",");
        try {
            headerMap = matchHeaders(httpRequest, headers);
        } catch (HeaderMismatchException e) {
            throw (LoginException) new LoginException("Header Mistmatch error").initCause(e);
        }

        if (headerMap.isEmpty()) {
            throw new FailedLoginException();
        }

        if (authenticationAuthority.equalsIgnoreCase("Siteminder")) {
            HeaderHandler headerHandler = new SiteminderHeaderHandler();
            username = headerHandler.getUser(headerMap);
        } else if (authenticationAuthority.equalsIgnoreCase("Datapower")) {
            /* To be Done */
        }
        if (username == null || username.equals("")) {
            username = null;
            throw new FailedLoginException();
        }

        if (dataSource != null) {
            try {
                conn = dataSource.getConnection();
                try {
                    statement = conn.prepareStatement(groupSelect);
                    int count = countParameters(groupSelect);
                    for (int i = 0; i < count; i++) {
                        statement.setObject(i + 1, username);
                    }
                    result = statement.executeQuery();
                    while (result.next()) {
                        String userName = result.getString(1);
                        String groupName = result.getString(2);
                        if (userName.equals(username))
                            groups.add(groupName);
                    }
                    if (groups.isEmpty()) {
                        log.error("No roles associated with user " + username);
                        loginSucceeded = false;
                        throw new FailedLoginException();
                    } else
                        loginSucceeded = true;
                } finally {
                    result.close();
                    statement.close();
                    conn.close();
                }
            } catch (LoginException e) {
                // Clear out the private state
                username = null;
                groups.clear();
                throw e;
            } catch (SQLException sqle) {
                // Clear out the private state
                username = null;
                groups.clear();
                throw (LoginException) new LoginException("SQL error").initCause(sqle);
            } catch (Exception e) {
                // Clear out the private state
                username = null;
                groups.clear();
                throw (LoginException) new LoginException("Could not access datasource").initCause(e);
            }
        }

        return loginSucceeded;
    }

    public boolean logout() throws LoginException {
        loginSucceeded = false;
        username = null;
        groups.clear();
        if (!subject.isReadOnly()) {
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

}
