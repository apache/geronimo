/*
 * Copyright 2001-2004 The Apache Software Foundation.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package javax.xml.rpc.server;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpSession;
import javax.xml.rpc.handler.MessageContext;
import java.security.Principal;

/**
 * The <code>ServletEndpointContext</code> provides an endpoint
 * context maintained by the underlying servlet container based
 * JAX-RPC runtime system. For service endpoints deployed on a
 * servlet container based JAX-RPC runtime system, the context
 * parameter in the <code>ServiceLifecycle.init</code> method is
 * required to be of the Java type
 * <code>javax.xml.rpc.server.ServletEndpointContext</code>.
 * <p>
 * A servlet container based JAX-RPC runtime system implements
 * the <code>ServletEndpointContext</code> interface. The JAX-RPC
 * runtime system is required to provide appropriate session,
 * message context, servlet context and user principal information
 * per method invocation on the endpoint class.
 *
 * @version 1.0
 */
public interface ServletEndpointContext {

    /**
     * The method <code>getMessageContext</code> returns the
     * <code>MessageContext</code> targeted for this endpoint instance.
     * This enables the service endpoint instance to acccess the
     * <code>MessageContext</code> propagated by request
     * <code>HandlerChain</code> (and its contained <code>Handler</code>
     * instances) to the target endpoint instance and to share any
     * SOAP message processing related context. The endpoint instance
     * can access and manipulate the <code>MessageContext</code>
     * and share the SOAP message processing related context with
     * the response <code>HandlerChain</code>.
     *
     * @return MessageContext; If there is no associated
     *     <code>MessageContext</code>, this method returns
     *     <code>null</code>.
     * @throws java.lang.IllegalStateException if this method is invoked outside a
     * remote method implementation by a service endpoint instance.
     */
    public MessageContext getMessageContext();

    /**
     * Returns a <code>java.security.Principal</code> instance that
     * contains the name of the authenticated user for the current
     * method invocation on the endpoint instance. This method returns
     * <code>null</code> if there is no associated principal yet.
     * The underlying JAX-RPC runtime system takes the responsibility
     * of providing the appropriate authenticated principal for a
     * remote method invocation on the service endpoint instance.
     *
     * @return A <code>java.security.Principal</code> for the
     * authenticated principal associated with the current
     * invocation on the servlet endpoint instance;
     * Returns <code>null</code> if there no authenticated
     * user associated with a method invocation.
     */
    public Principal getUserPrincipal();

    /**
     * The <code>getHttpSession</code> method returns the current
     * HTTP session (as a <code>javax.servlet.http.HTTPSession</code>).
     * When invoked by the service endpoint within a remote method
     * implementation, the <code>getHttpSession</code> returns the
     * HTTP session associated currently with this method invocation.
     * This method returns <code>null</code> if there is no HTTP
     * session currently active and associated with this service
     * endpoint. An endpoint class should not rely on an active
     * HTTP session being always there; the underlying JAX-RPC
     * runtime system is responsible for managing whether or not
     * there is an active HTTP session.
     * <p>
     * The getHttpSession method throws <code>JAXRPCException</code>
     * if invoked by an non HTTP bound endpoint.
     *
     * @return The HTTP session associated with the current
     * invocation or <code>null</code> if there is no active session.
     * @throws javax.xml.rpc.JAXRPCException - If this method invoked by a non-HTTP bound
     *         endpoints.
     */
    public HttpSession getHttpSession();

    /**
     * The method <code>getServletContext</code> returns the
     * <code>ServletContex</code>t associated with the web
     * application that contain this endpoint. According to
     * the Servlet specification, There is one context per web
     * application (installed as a WAR) per JVM . A servlet
     * based service endpoint is deployed as part of a web
     * application.
     * 
     * @return the current <code>ServletContext</code>
     */
    public ServletContext getServletContext();

    public boolean isUserInRole(java.lang.String s);
}
