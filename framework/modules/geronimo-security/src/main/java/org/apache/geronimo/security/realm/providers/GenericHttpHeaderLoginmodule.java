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

import java.security.Principal;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.security.auth.Subject;
import javax.security.auth.callback.CallbackHandler;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/*
 * Parent class for all the generic header login modules.
 */
public abstract class GenericHttpHeaderLoginmodule {

    private static Log log = LogFactory.getLog(GenericHttpHeaderLoginmodule.class);

    protected Subject subject;
    protected String username;
    protected String headerNames;
    protected String authenticationAuthority;
    protected CallbackHandler callbackHandler;
    protected boolean loginSucceeded;
    protected HttpServletRequest httpRequest;
    protected Set<Principal> allPrincipals = new HashSet<Principal>();
    protected Set<String> groups = new HashSet<String>();

    public GenericHttpHeaderLoginmodule() {

    }

    protected void commitHelper() {
        for (String group : groups) {
            allPrincipals.add(new GeronimoGroupPrincipal(group));
        }
        subject.getPrincipals().addAll(allPrincipals);
        subject.getPublicCredentials().add(username);
    }

    public void abortHelper() {
        allPrincipals.clear();
        groups.clear();
    }

    public void logoutHelper() {
        groups.clear();
        if (!subject.isReadOnly()) {
            // Remove principals added by this LoginModule
            subject.getPrincipals().removeAll(allPrincipals);
        }
        allPrincipals.clear();
    }

    public Map<String, String> matchHeaders(HttpServletRequest request, String[] headers)
            throws HeaderMismatchException {
        Map<String, String> headerMap = new HashMap<String, String>();
        for (String header : headers) {
            String headerValue = request.getHeader(header);
            if (headerValue != null) {
                headerMap.put(header, headerValue);
            } else
                log.warn("An Unauthorized attempt has been made to access the protected resource from host "
                        + request.getRemoteHost());
        }
        return headerMap;
    }

}
