/**
 *
 * Copyright 2003-2004 The Apache Software Foundation
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.apache.geronimo.tomcat;


import java.security.Principal;
import java.util.Stack;
import javax.security.auth.Subject;


/**
 * @version $Rev: 122776 $ $Date: 2004-12-19 12:11:07 -0700 (Sun, 19 Dec 2004) $
 */
public class JAASTomcatPrincipal implements Principal {
    private final String name;
    private Subject subject;

    public JAASTomcatPrincipal(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public Subject getSubject() {
        return subject;
    }

    public void setSubject(Subject subject) {
        this.subject = subject;
    }
}
