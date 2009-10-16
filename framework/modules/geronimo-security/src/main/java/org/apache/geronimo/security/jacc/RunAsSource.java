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


package org.apache.geronimo.security.jacc;

import javax.security.auth.Subject;
import org.apache.geronimo.security.ContextManager;

/**
 * @version $Rev$ $Date$
 */
public interface RunAsSource {
    RunAsSource NULL = new RunAsSource() {

        public Subject getDefaultSubject() {
            return ContextManager.EMPTY;
        }

        public Subject getSubjectForRole(String role) {
            if (role == null) return null;
            return ContextManager.EMPTY;
        }
    };

    /**
     *
     * @return the non-null default subject for this security environment
     */
    Subject getDefaultSubject();

    /**
     * If role is null, return null.  Otherwise return a non-null Subject or throw an IllegalArgumentException.
     * @param role role to estabilish identity for
     * @return non-null Subject embodying the identity for the supplied non-null role, or null if role is null.
     */
    Subject getSubjectForRole(String role);
}
