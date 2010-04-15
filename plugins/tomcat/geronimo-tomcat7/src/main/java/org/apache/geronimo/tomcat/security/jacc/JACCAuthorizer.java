/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */


package org.apache.geronimo.tomcat.security.jacc;

import java.security.AccessControlContext;
import java.security.AccessControlException;

import javax.security.jacc.WebResourcePermission;
import javax.security.jacc.WebUserDataPermission;

import org.apache.catalina.connector.Request;
import org.apache.geronimo.tomcat.security.AuthResult;
import org.apache.geronimo.tomcat.security.Authorizer;
import org.apache.geronimo.tomcat.security.UserIdentity;

/**
 * @version $Rev$ $Date$
 */
public class JACCAuthorizer implements Authorizer {

    private final AccessControlContext defaultACC;

    public JACCAuthorizer(AccessControlContext defaultACC) {
        this.defaultACC = defaultACC;
    }

    public Object getConstraints(Request request) {
        return null;
    }

    public boolean hasUserDataPermissions(Request request, Object constraints) {
        try {
            defaultACC.checkPermission(new WebUserDataPermission(request));
            return true;
        } catch (AccessControlException e) {
            return false;
        }
    }

    public boolean isAuthMandatory(Request request, Object constraints) {
        try {
            defaultACC.checkPermission(new WebResourcePermission(request));
            return false;
        } catch (AccessControlException e) {
            return true;
        }
    }

    public boolean hasResourcePermissions(Request request, AuthResult authResult, Object constraints, UserIdentity userIdentity) {
        if (!(userIdentity instanceof JACCUserIdentity)) {
            return false;
        }

        AccessControlContext acc = ((JACCUserIdentity)userIdentity).getAccessControlContext();
        try {
            acc.checkPermission(new WebResourcePermission(request));
            return true;
        } catch (AccessControlException e) {
            return false;
        }
    }
}
