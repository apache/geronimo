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


package org.apache.geronimo.security.jaspi.impl;


import java.security.AccessControlContext;
import java.security.Principal;

import javax.security.auth.Subject;

import org.apache.geronimo.security.jaspi.UserIdentity;

/**
 * @version $Rev$ $Date$
 */
public class JACCUserIdentity implements UserIdentity {
    private final Subject subject;
    private final Principal userPrincipal;
    private final AccessControlContext acc;

    public JACCUserIdentity(Subject subject, Principal userPrincipal, AccessControlContext acc) {
        if (subject == null) throw new NullPointerException("No Subject in user identity");
        this.subject = subject;
        this.userPrincipal = userPrincipal;
        this.acc = acc;
    }

    @Override
    public Principal getUserPrincipal() {
        return userPrincipal;
    }

    @Override
    public Subject getSubject() {
        return subject;
    }

    @Override
    public AccessControlContext getAccessControlContext() {
        return acc;
    }
}
