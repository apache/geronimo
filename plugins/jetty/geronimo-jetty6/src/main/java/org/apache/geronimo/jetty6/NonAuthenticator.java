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
package org.apache.geronimo.jetty6;

import org.mortbay.jetty.security.Authenticator;
import org.mortbay.jetty.security.UserRealm;
import org.mortbay.jetty.Request;
import org.mortbay.jetty.Response;

import java.security.Principal;
import java.io.IOException;

/**
 * Authenticator that always denies, returning null.  Useful when you need to install a default principal/subject
 * in an unsecured web app.
 */
public class NonAuthenticator implements Authenticator {
    public Principal authenticate(UserRealm realm, String pathInContext, Request request, Response response) throws IOException {
        return null;
    }

    public String getAuthMethod() {
        return "None"; 
    }
}
