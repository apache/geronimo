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


package org.apache.geronimo.security;

import java.security.AccessControlContext;
import java.security.Principal;
import java.util.List;

import javax.security.auth.Subject;

/**
 * @version $Rev$ $Date$
 */
public class Context {
    private final SubjectId id;
    private final AccessControlContext context;
    private final Subject subject;
    private final Principal principal;
    private final List<String> groups;

    public Context(SubjectId id, AccessControlContext context, Subject subject, Principal principal, List<String> groups) {
        this.id = id;
        this.context = context;
        this.subject = subject;
        this.principal = principal;
        this.groups = groups;
    }

    public SubjectId getId() {
        return id;
    }

    public AccessControlContext getContext() {
        return context;
    }

    public Subject getSubject() {
        return subject;
    }

    public Principal getPrincipal() {
        return principal;
    }

    public List<String> getGroups() {
        return groups;
    }
}
