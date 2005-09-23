/**
 *
 * Copyright 2005 The Apache Software Foundation
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
package org.apache.geronimo.tomcat.realm;

import java.io.IOException;

import org.apache.catalina.connector.Request;
import org.apache.catalina.connector.Response;
import org.apache.catalina.deploy.SecurityConstraint;
import org.apache.catalina.Context;

/**
 * TomcatEJBWSGeronimoRealm is intended only for use with ejb web services in tomcat.
 * Tomcat appears to conflate the separate concepts of logging in and checking permissions
 * into one class.  This is wholly inappropriate for ejb web services, where logging in
 * is handled by the web container but authorization is handled by the ejb container.
 * This class "separates" the concerns by always authorizing everything.
 * 
 * @version $Rev:  $ $Date:  $
 */
public class TomcatEJBWSGeronimoRealm extends TomcatGeronimoRealm {

    public boolean hasResourcePermission(Request request,
                                         Response response,
                                         SecurityConstraint[] constraints,
                                         Context context)
            throws IOException {
        return true;

    }

    public boolean hasUserDataPermission(Request request,
                                         Response response,
                                         SecurityConstraint[] constraints)
            throws IOException {
        return true;
    }

}
