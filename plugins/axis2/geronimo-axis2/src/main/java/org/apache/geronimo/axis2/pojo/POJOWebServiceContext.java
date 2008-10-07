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

import java.security.Principal;

import javax.xml.ws.EndpointReference;
import javax.xml.ws.WebServiceContext;
import javax.xml.ws.handler.MessageContext;

import org.apache.axis2.jaxws.context.WebServiceContextImpl;
import org.w3c.dom.Element;

/**
 * Implementation of WebServiceContext that uses ThreadLocal to associate MessageContext with 
 * the thread.
 * 
 * @version $Rev$ $Date$
 */
public class POJOWebServiceContext implements WebServiceContext {

    private static ThreadLocal<WebServiceContextImpl> context = 
        new ThreadLocal<WebServiceContextImpl>();
    
    public POJOWebServiceContext() {        
    }
        
    public final MessageContext getMessageContext() {
        WebServiceContextImpl wsContext = context.get();
        return (wsContext == null) ? null : wsContext.getMessageContext();
    }

    public final Principal getUserPrincipal() {
        WebServiceContextImpl wsContext = context.get();
        return (wsContext == null) ? null : wsContext.getUserPrincipal();
    }

    public final boolean isUserInRole(String user) {
        WebServiceContextImpl wsContext = context.get();
        return (wsContext == null) ? null : wsContext.isUserInRole(user);
    }
            
    public final EndpointReference getEndpointReference(Element... referenceParameters) {
        WebServiceContextImpl wsContext = context.get();
        return (wsContext == null) ? null : wsContext.getEndpointReference(referenceParameters);
    }
    
    public final <T extends EndpointReference> T getEndpointReference(Class<T> clazz,
                                                                      Element... referenceParameters) {
        WebServiceContextImpl wsContext = context.get();
        return (wsContext == null) ? null : wsContext.getEndpointReference(clazz, referenceParameters);
    }
    
    static WebServiceContextImpl get() {
        return context.get();
    }
    
    static void set(WebServiceContextImpl ctx) {
        context.set(ctx);
    }
    
    static void clear() {
        context.set(null);
    }
    
}
