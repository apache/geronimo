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
package org.apache.geronimo.tomcat.interceptor;

import javax.security.jacc.PolicyContext;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

import org.apache.geronimo.security.Callers;
import org.apache.geronimo.security.ContextManager;

public class PolicyContextBeforeAfter implements BeforeAfter{

    private final BeforeAfter next;
    private final String policyContextID;
    private final int policyContextIDIndex;
    private final int callersIndex;

    public PolicyContextBeforeAfter(BeforeAfter next, int policyContextIDIndex, int callersIndex, String policyContextID) {
        this.next = next;
        this.policyContextIDIndex = policyContextIDIndex;
        this.callersIndex = callersIndex;
        this.policyContextID = policyContextID;
    }

    public void before(Object[] context, ServletRequest httpRequest, ServletResponse httpResponse) {

        //Save the old

        context[policyContextIDIndex] = PolicyContext.getContextID();
        context[callersIndex] = ContextManager.getCallers();

        //Set the new
        PolicyContext.setContextID(policyContextID);
        PolicyContext.setHandlerData(httpRequest);

        if (next != null) {
            next.before(context, httpRequest, httpResponse);
        }
    }

    public void after(Object[] context, ServletRequest httpRequest, ServletResponse httpResponse) {
        if (next != null) {
            next.after(context, httpRequest, httpResponse);
        }

        //Replace the old
        PolicyContext.setContextID((String)context[policyContextIDIndex]);
        ContextManager.popCallers((Callers) context[callersIndex]);
    }

}

