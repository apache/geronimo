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
package org.apache.geronimo.tomcat.valve;

import org.apache.catalina.valves.ValveBase;
import org.apache.catalina.connector.Request;
import org.apache.catalina.connector.Response;
import org.apache.geronimo.security.ContextManager;
import org.apache.geronimo.tomcat.realm.TomcatGeronimoRealm;

import javax.servlet.ServletException;
import javax.security.jacc.PolicyContext;
import javax.security.auth.Subject;
import java.io.IOException;

/**
 * @version $Rev$ $Date$
 */

public class PolicyContextValve extends ValveBase {

    private final String policyContextID;

    public PolicyContextValve(String policyContextID) {
        this.policyContextID = policyContextID;
    }

    public void invoke(Request request, Response response) throws IOException, ServletException {

        String oldId = PolicyContext.getContextID();

        PolicyContext.setContextID(policyContextID);
        PolicyContext.setHandlerData(request);
        Request oldRequest = TomcatGeronimoRealm.setRequest(request);

        // Pass this request on to the next valve in our pipeline
        try {
            getNext().invoke(request, response);
        } finally {
            PolicyContext.setContextID(oldId);
            //This might not be necessary, but I think it's possible that cross-context dispatch might change the request.
            TomcatGeronimoRealm.setRequest(oldRequest);
        }
    }
}
