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
package org.apache.geronimo.tomcat.authenticator;

import java.io.IOException;
import java.security.Principal;

import javax.servlet.http.HttpServletRequest;

import org.apache.catalina.authenticator.AuthenticatorBase;
import org.apache.catalina.connector.Request;
import org.apache.catalina.connector.Response;
import org.apache.catalina.deploy.LoginConfig;
import org.apache.geronimo.tomcat.realm.TomcatGeronimoRealm;

/*
 * An Authenticator which utilizes HttpRequest headers to perform authentication.
 * In web.xml use the <auth-method>GENERIC</auth-method> to invoke this
 * authenticator.
 */
public class GenericHeaderAuthenticator extends AuthenticatorBase {
   
    private static final String GENERIC_METHOD="GENERIC";
    protected boolean authenticate(Request request, Response response, LoginConfig config) throws IOException {
        HttpServletRequest httpRequest=request.getRequest(); 
        Principal principal = request.getUserPrincipal();
        if(context.getRealm() instanceof TomcatGeronimoRealm)
        principal =((TomcatGeronimoRealm)context.getRealm()).authenticate(httpRequest);
        if (principal != null) {
            register(request, response, principal, GENERIC_METHOD,
                     null, null);
            return (true);
        }
        else
            response.setStatus(401);
        return false;
    }
}
