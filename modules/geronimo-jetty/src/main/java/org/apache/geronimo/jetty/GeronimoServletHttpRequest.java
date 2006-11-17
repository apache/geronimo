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
package org.apache.geronimo.jetty;

import javax.servlet.http.Cookie;

import org.mortbay.http.HttpRequest;
import org.mortbay.jetty.servlet.ServletHandler;
import org.mortbay.jetty.servlet.ServletHttpRequest;
import org.mortbay.jetty.servlet.SessionManager;

/**
 * @version $Rev$ $Date$
 */
public class GeronimoServletHttpRequest extends ServletHttpRequest {
    private final ServletHandler servletHandler;
    private final HttpRequest request;
    private String requestedSessionId;
    
    public GeronimoServletHttpRequest(ServletHandler servletHandler, String pathInContext, HttpRequest request) {
        super(servletHandler, pathInContext, request);
        this.servletHandler = servletHandler;
        this.request = request;
    }
    
    public String getRequestedSessionId() {
        return requestedSessionId;
    }
    
    public void setRequestedSessionId(String pathParams) {
        requestedSessionId = null;
        if (servletHandler.isUsingCookies()) {
            Cookie[] cookies= request.getCookies();
            if (cookies!=null && cookies.length>0) {
                for (int i=0;i<cookies.length;i++) {
                    if (SessionManager.__SessionCookie.equalsIgnoreCase(cookies[i].getName())) {
                        if (null != requestedSessionId) {
                            // Multiple jsessionid cookies. Probably due to
                            // multiple paths and/or domains. Pick the first
                            // known session or the last defined cookie.
                            SessionManager manager = servletHandler.getSessionManager();
                            if (manager!=null && manager.getHttpSession(requestedSessionId)!=null)
                                break;
                        }
                        requestedSessionId = cookies[i].getValue();
                    }
                }
            }
        }
            
        // check if there is a url encoded session param.
        if (null != pathParams && pathParams.startsWith(SessionManager.__SessionURL)) {
            String id = pathParams.substring(SessionManager.__SessionURL.length() + 1);
            if (null == requestedSessionId) {
                requestedSessionId = id;
            }
        }
    }
}
