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

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.security.jacc.PolicyContext;

import org.apache.geronimo.tomcat.security.SecurityValve;
import org.apache.geronimo.tomcat.security.Authenticator;
import org.apache.geronimo.tomcat.security.Authorizer;
import org.apache.geronimo.tomcat.security.IdentityService;
import org.apache.geronimo.security.jacc.PolicyContextHandlerHttpServletRequest;
import org.apache.catalina.connector.Request;
import org.apache.catalina.connector.Response;

/**
 * @version $Rev$ $Date$
 */
public class JACCSecurityValve extends SecurityValve {
    private final String policyContextId;

    public JACCSecurityValve(Authenticator authenticator, Authorizer authorizer, IdentityService identityService, String policyContextId) {
        super(authenticator, authorizer, identityService);
        this.policyContextId = policyContextId;
    }

    @Override
    public void invoke(Request request, Response response) throws IOException, ServletException {
        String oldContextId = PolicyContext.getContextID();
        PolicyContext.setContextID(policyContextId);
        HttpServletRequest oldRequest = PolicyContextHandlerHttpServletRequest.pushContextData(request);
        try {
            super.invoke(request, response);
        } finally {
            PolicyContext.setContextID(oldContextId);
            // Must unset handler data from thread - see GERONIMO-4574
            PolicyContextHandlerHttpServletRequest.popContextData(oldRequest);
        }
    }
}
