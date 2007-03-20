/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.geronimo.axis2.pojo;

import org.apache.axis2.jaxws.handler.LogicalMessageContext;
import org.apache.axis2.transport.http.HTTPConstants;

import javax.servlet.http.HttpServletRequest;
import javax.xml.ws.WebServiceContext;
import javax.xml.ws.handler.MessageContext;
import java.security.Principal;

/**
 * Implementation of WebServiceContext for POJO WS to ensure that getUserPrincipal()
 * and isUserInRole() are properly handled.
 * 
 * @version $Rev$ $Date$
 */
public class POJOWebServiceContext implements WebServiceContext {

    private MessageContext ctx;

    public POJOWebServiceContext(org.apache.axis2.context.MessageContext ctx) {
        this.ctx = new LogicalMessageContext(new org.apache.axis2.jaxws.core.MessageContext(ctx));
    }
    
    public final MessageContext getMessageContext() {
        return ctx;
    }

    private HttpServletRequest getHttpServletRequest() {
        MessageContext ctx = getMessageContext();
        return (ctx != null) ? (HttpServletRequest)ctx.get(HTTPConstants.MC_HTTP_SERVLETREQUEST) : null;
    }

    public final Principal getUserPrincipal() {
        HttpServletRequest request = getHttpServletRequest();
        return (request != null) ? request.getUserPrincipal() : null;
    }

    public final boolean isUserInRole(String user) {
        HttpServletRequest request = getHttpServletRequest();
        return (request != null) ? request.isUserInRole(user) : false;
    }

}
