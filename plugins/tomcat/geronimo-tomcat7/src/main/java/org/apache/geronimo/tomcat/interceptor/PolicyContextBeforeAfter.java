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

package org.apache.geronimo.tomcat.interceptor;

import javax.security.auth.Subject;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

import org.apache.geronimo.security.Callers;
import org.apache.geronimo.security.ContextManager;

public class PolicyContextBeforeAfter implements BeforeAfter {

    public static final String DEFAULT_SUBJECT = "~DEFAULT_SUBJECT";

    private final BeforeAfter next;

    private final String policyContextID;

    private final int policyContextIDIndex;

    private final int callersIndex;

    private final int defaultSubjectIndex;

    private final Subject defaultSubject;

    public PolicyContextBeforeAfter(BeforeAfter next, int policyContextIDIndex, int callersIndex, int defaultSubjectIndex, String policyContextID, Subject defaultSubject) {
        this.next = next;
        this.policyContextIDIndex = policyContextIDIndex;
        this.callersIndex = callersIndex;
        this.defaultSubjectIndex = defaultSubjectIndex;
        this.policyContextID = policyContextID;
        this.defaultSubject = defaultSubject;
    }

    public void before(BeforeAfterContext beforeAfterContext, ServletRequest httpRequest, ServletResponse httpResponse, int dispatch) {

        try {
            //Save the old
            //context[policyContextIDIndex] = PolicyContext.getContextID();
            beforeAfterContext.contexts[callersIndex] = ContextManager.getCallers();
            beforeAfterContext.clearRequiredFlags[callersIndex] = true;
            //Set the new
            //        PolicyContext.setContextID(policyContextID);
            //        PolicyContext.setHandlerData(httpRequest);
            if (httpRequest != null) {
                beforeAfterContext.contexts[defaultSubjectIndex] = httpRequest.getAttribute(DEFAULT_SUBJECT);
                httpRequest.setAttribute(DEFAULT_SUBJECT, defaultSubject);
                beforeAfterContext.clearRequiredFlags[defaultSubjectIndex] = true;
            }

            if (next != null) {
                next.before(beforeAfterContext, httpRequest, httpResponse, dispatch);
            }
        } catch (RuntimeException e) {
            if (beforeAfterContext.clearRequiredFlags[callersIndex]) {
                ContextManager.popCallers((Callers) beforeAfterContext.contexts[callersIndex]);
                beforeAfterContext.clearRequiredFlags[callersIndex] = false;
            }
            if (beforeAfterContext.clearRequiredFlags[defaultSubjectIndex]) {
                httpRequest.setAttribute(DEFAULT_SUBJECT, beforeAfterContext.contexts[defaultSubjectIndex]);
                beforeAfterContext.clearRequiredFlags[defaultSubjectIndex] = false;
            }
            throw e;
        }
    }

    public void after(BeforeAfterContext beforeAfterContext, ServletRequest httpRequest, ServletResponse httpResponse, int dispatch) {
        try {
            if (next != null) {
                next.after(beforeAfterContext, httpRequest, httpResponse, dispatch);
            }
        } finally {
            //Replace the old
            //        PolicyContext.setContextID((String)context[policyContextIDIndex]);
            // Must unset handler data from thread - see GERONIMO-4574
            //        PolicyContext.setHandlerData(null);
            if (beforeAfterContext.clearRequiredFlags[callersIndex]) {
                ContextManager.popCallers((Callers) beforeAfterContext.contexts[callersIndex]);
                beforeAfterContext.clearRequiredFlags[callersIndex] = false;
            }
            if (beforeAfterContext.clearRequiredFlags[defaultSubjectIndex]) {
                httpRequest.setAttribute(DEFAULT_SUBJECT, beforeAfterContext.contexts[defaultSubjectIndex]);
                beforeAfterContext.clearRequiredFlags[defaultSubjectIndex] = false;
            }
        }
    }
}
