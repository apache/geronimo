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


package org.apache.geronimo.tomcat.security.authentication.jaspic;

import java.util.Map;
import java.util.HashMap;

import javax.security.auth.message.MessageInfo;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.catalina.connector.Request;
import org.apache.catalina.connector.Response;

/**
 * @version $Rev$ $Date$
 */
public class JaspicMessageInfo implements MessageInfo {
    private static final String MANDATORY_KEY = "javax.security.auth.message.MessagePolicy.isMandatory";

    private final Map<String, Object> map = new HashMap<String, Object>();
    private HttpServletRequest request;
    private HttpServletResponse response;

    public JaspicMessageInfo() {
    }

    public JaspicMessageInfo(Request request, HttpServletResponse response, boolean authMandatory) {
        this.request = request;
        this.response = response;
        if (authMandatory) {
            map.put(MANDATORY_KEY, Boolean.toString(authMandatory));
        }
    }

    public Map getMap() {
        return map;
    }

    public Object getRequestMessage() {
        return request;
    }

    public Object getResponseMessage() {
        return response;
    }

    public void setRequestMessage(Object request) {
        if (!(request instanceof HttpServletRequest)) throw new IllegalArgumentException("Request in not a servlet request but " + request.getClass().getName());
        this.request = (HttpServletRequest) request;
    }

    public void setResponseMessage(Object response) {
        if (!(response instanceof HttpServletResponse)) throw new IllegalArgumentException("response in not a servlet response but " + response.getClass().getName());
        this.response = (HttpServletResponse) response;
    }
}
