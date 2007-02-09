/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.geronimo.corba.security.jgss;

import java.security.AccessControlContext;
import java.security.PrivilegedAction;
import java.util.Iterator;
import javax.security.auth.Subject;

import org.apache.geronimo.security.jaas.UsernamePasswordCredential;


/**
 * A privileged action that hunts down username/password private credentials
 * with a given username.
 *
 * @version $Revision: 451417 $ $Date: 2006-09-29 13:13:22 -0700 (Fri, 29 Sep 2006) $
 */
class SubjectComber implements PrivilegedAction {

    private AccessControlContext acc;
    private final String username;
    private Subject subject;

    public SubjectComber(AccessControlContext acc, String username) {
        this.acc = acc;
        this.username = username;
    }

    public SubjectComber(Subject subject, String username) {
        this.subject = subject;
        this.username = username;
    }

    public Object run() {
        if (subject == null) subject = Subject.getSubject(acc);

        if (subject != null) {
            Iterator iterator = subject.getPrivateCredentials(UsernamePasswordCredential.class).iterator();
            while (iterator.hasNext()) {
                UsernamePasswordCredential key = (UsernamePasswordCredential) iterator.next();
                if (username == null || username.equals(key.getUsername())) {
                    return key;
                }
            }

        }
        return null;
    }

}


