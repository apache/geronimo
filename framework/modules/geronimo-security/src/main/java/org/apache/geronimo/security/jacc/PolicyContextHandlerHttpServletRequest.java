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

package org.apache.geronimo.security.jacc;

import javax.security.jacc.PolicyContextException;
import javax.security.jacc.PolicyContextHandler;
import javax.servlet.http.HttpServletRequest;
import org.apache.geronimo.security.ThreadData;
import org.apache.geronimo.security.ContextManager;


/**
 * @version $Rev$ $Date$
 */
public class PolicyContextHandlerHttpServletRequest implements PolicyContextHandler {
    public static final String HANDLER_KEY = "javax.servlet.http.HttpServletRequest";

    public boolean supports(String key) throws PolicyContextException {
        return HANDLER_KEY.equals(key);
    }

    public String[] getKeys() throws PolicyContextException {
        return new String[]{HANDLER_KEY};
    }

    public Object getContext(String key, Object data) throws PolicyContextException {
        if (HANDLER_KEY.equals(key)) {
            return ((ThreadData)data).getRequest();
        }
        return null;
    }

    public static HttpServletRequest pushContextData(HttpServletRequest httpServletRequest) {
        ThreadData threadData = ContextManager.getThreadData();
        HttpServletRequest oldRequest = threadData.getRequest();
        threadData.setRequest(httpServletRequest);
        return oldRequest;
    }

    public static void popContextData(HttpServletRequest oldRequest) {
        ThreadData threadData = ContextManager.getThreadData();
        threadData.setRequest(oldRequest);
    }
}
