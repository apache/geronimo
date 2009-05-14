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

package org.apache.geronimo.tomcat.cluster;

import javax.servlet.http.Cookie;

import org.apache.catalina.Context;
import org.apache.catalina.Globals;
import org.apache.catalina.Session;
import org.apache.catalina.connector.Request;
import org.apache.catalina.connector.Response;

/**
 * setNewSessionCookie contains code copied from
 * org.apache.catalina.ha.session.JvmRouteBinderValve.setNewSessionCookie contributed by Peter Rossbach.
 *
 * @version $Rev:$ $Date:$
 */
public class JkRouter implements Router {

    public String stripRoutingInfoFromRequestedSessionId(Request request) {
        String requestedSessionId = request.getRequestedSessionId();
        if (null != requestedSessionId) {
            int index = requestedSessionId.indexOf(".");
            if (0 < index) {
                String newRequestedSessionId = requestedSessionId.substring(0, index);
                request.setRequestedSessionId(newRequestedSessionId);
            }
        }
        return requestedSessionId;
    }

    public void writeSessionIdWithRoutingInfo(Request request, Response response, String nodeName) {
        String augmentedSessionID = buildAugmentedSessionId(request, nodeName);
        if (null == augmentedSessionID) {
            return;
        }
        
        setNewSessionCookie(request, response, augmentedSessionID);
    }

    protected void setNewSessionCookie(Request request, Response response, String augmentedSessionID) {
        Context context = request.getContext();
        if (context.getCookies()) {
            Cookie newCookie = new Cookie(Globals.SESSION_COOKIE_NAME, augmentedSessionID);
            newCookie.setMaxAge(-1);
            String contextPath = null;
            if (!response.getConnector().getEmptySessionPath() && (context != null)) {
                contextPath = context.getEncodedPath();
            }
            if ((contextPath != null) && (contextPath.length() > 0)) {
                newCookie.setPath(contextPath);
            } else {
                newCookie.setPath("/");
            }
            if (request.isSecure()) {
                newCookie.setSecure(true);
            }
            response.addCookie(newCookie);
        }
    }

    protected String buildAugmentedSessionId(Request request, String nodeName) {
        Session sessionInternal = request.getSessionInternal();
        if (null == sessionInternal) {
            return null;
        }

        String internalSessionID = sessionInternal.getId();
        String actualSessionId = request.getRequestedSessionId();
        if (null == actualSessionId || !actualSessionId.startsWith(internalSessionID)) {
            actualSessionId = internalSessionID;
        }
        
        int index = actualSessionId.indexOf(".");
        if (0 < index) {
            String embeddedNodeName = actualSessionId.substring(index + 1);
            if (embeddedNodeName.equals(nodeName)) {
                return null;
            }
            actualSessionId = actualSessionId.substring(0, index);
        }
        return actualSessionId + "." + nodeName;
    }
}