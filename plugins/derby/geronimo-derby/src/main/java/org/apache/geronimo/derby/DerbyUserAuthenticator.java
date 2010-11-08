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
package org.apache.geronimo.derby;

import java.sql.SQLException;
import java.util.Properties;
import java.util.Set;
import javax.security.auth.Subject;
import javax.security.auth.login.LoginContext;
import javax.security.auth.login.LoginException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.derby.authentication.UserAuthenticator;
import org.apache.geronimo.security.realm.providers.GeronimoGroupPrincipal;

/**
 * 
 * 
 * @version $Rev$ $Date$
 */
public class DerbyUserAuthenticator implements UserAuthenticator {

    private static final Logger log = LoggerFactory.getLogger(DerbyUserAuthenticator.class);
    private static final String configName = "geronimo-admin";

    /*
     * A user-defined UserAuthenticator to authenticate the user id and password, which are used access Geronimo
     * embedded derby.
     */
    public boolean authenticateUser(String userName, String userPassword, String databaseName, Properties info)
            throws SQLException {

        Thread thread = Thread.currentThread();
        ClassLoader oldCL = thread.getContextClassLoader();
        Credentials credentials = new Credentials(userName, userPassword);

        try {
            thread.setContextClassLoader(DerbyUserAuthenticator.class.getClassLoader());
            // TODO consider using ContextManager for login and checking a permission against the ACC
            // to do e.g. deployments.
            LoginContext context = new LoginContext(configName, credentials);
            context.login();

            Subject sub = context.getSubject();
            Set<GeronimoGroupPrincipal> pricipalsGroup = sub.getPrincipals(GeronimoGroupPrincipal.class);
            boolean databaseLevelFlag = false;
            for (GeronimoGroupPrincipal principal : pricipalsGroup) {
                // if user group is "derbyadmin", then authentication passed without checking the databaseName
                if (principal.getName().equalsIgnoreCase("derbyadmin")) {
                    return true;
                } else {
                    // To define database level user, the the user group should be "derby_${databaseName}"
                    if (databaseName != null) {
                        databaseLevelFlag = principal.getName().equalsIgnoreCase("derby_" + databaseName);
                    }
                }
            }
            if (!databaseLevelFlag) {
                logAuthenticationFailure(userName, userPassword, databaseName);
            }
            return databaseLevelFlag;
        } catch (LoginException e) {
            logAuthenticationFailure(userName, userPassword, databaseName);
            return false;
        } finally {
            credentials.clear();
            thread.setContextClassLoader(oldCL);
        }
    }

    private void logAuthenticationFailure(String userName, String userPassword, String databaseName) {
        log.warn("User authentication failure (userName userPassword databaseName): " + userName + " " + userPassword
                + " " + databaseName);
    }
}
