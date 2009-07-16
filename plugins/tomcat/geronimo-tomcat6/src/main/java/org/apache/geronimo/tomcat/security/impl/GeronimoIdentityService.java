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


package org.apache.geronimo.tomcat.security.impl;

import java.security.Principal;
import java.security.AccessControlContext;
import java.util.List;

import javax.security.auth.Subject;

import org.apache.geronimo.tomcat.security.IdentityService;
import org.apache.geronimo.tomcat.security.UserIdentity;
import org.apache.geronimo.tomcat.security.jacc.JACCUserIdentity;
import org.apache.geronimo.security.ContextManager;

/**
 * @version $Rev$ $Date$
 */
public class GeronimoIdentityService implements IdentityService {
    public Object associate(UserIdentity userIdentity) {
        Subject subject = userIdentity == null? ContextManager.EMPTY: userIdentity.getSubject();
        ContextManager.setCallers(subject, subject);
        return null;
    }

    public void dissociate(Object previous) {
        ContextManager.clearCallers();
    }

    public UserIdentity newUserIdentity(Subject subject, Principal userPrincipal, List<String> groups) {
        AccessControlContext acc = ContextManager.registerSubjectShort(subject, userPrincipal, groups);
        return new JACCUserIdentity(subject, userPrincipal, groups, acc);
    }
}
