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
package javax.xml.rpc.handler;

import java.util.List;
import java.util.Map;

/**
 * The <code>javax.xml.rpc.handler.HandlerChain</code> represents
 * a list of handlers. All elements in the HandlerChain are of
 * the type <code>javax.xml.rpc.handler.Handler</code>.
 * <p>
 * An implementation class for the <code>HandlerChain</code>
 * interface abstracts the policy and mechanism for the invocation
 * of the registered handlers.
 *
 * @version 1.0
 */
public interface HandlerChain extends List {

    /**
     * The <code>handleRequest</code> method initiates the request
     * processing for this handler chain.
     * @param context MessageContext parameter provides access to
     *             the request SOAP message.
     * @return boolean Returns <code>true</code> if all handlers in
     *             chain have been processed. Returns <code>false</code>
     *
     *             if a handler in the chain returned
     *             <code>false</code> from its handleRequest
     *             method.
     * @throws javax.xml.rpc.JAXRPCException if any processing error happens
     */
    public boolean handleRequest(MessageContext context);

    /**
     * The <code>handleResponse</code> method initiates the response
     * processing for this handler chain.
     *
     * @param context MessageContext parameter provides access to the response
     *                  SOAP message.
     * @return boolean Returns <code>true</code> if all handlers in
     *             chain have been processed. Returns <code>false</code>
     *             if a handler in the chain returned
     *             <code>false</code> from its handleResponse method.
     * @throws javax.xml.rpc.JAXRPCException if any processing error happens
     */
    public boolean handleResponse(MessageContext context);

    /**
     * The <code>handleFault</code> method initiates the SOAP
     * fault processing for this handler chain.
     *
     * @param  context MessageContext parameter provides access to the SOAP
     *         message.
     * @return Returns boolean Returns <code>true</code> if all handlers in
     *             chain have been processed. Returns <code>false</code>
     *             if a handler in the chain returned
     *             <code>false</code> from its handleFault method.
     * @throws javax.xml.rpc.JAXRPCException if any processing error happens
     */
    public boolean handleFault(MessageContext context);

    /**
     * Initializes the configuration for a HandlerChain.
     *
     * @param config Configuration for the initialization of this handler
     *                 chain
     *
     * @throws javax.xml.rpc.JAXRPCException if there is any error that prevents
     *              initialization
     */
    public void init(Map config);

    /**
     * Indicates the end of lifecycle for a HandlerChain.
     *
     * @throws javax.xml.rpc.JAXRPCException if there was any error that
     *              prevented destroy from completing
     */
    public void destroy();

    /**
     * Sets SOAP Actor roles for this <code>HandlerChain</code>. This
     * specifies the set of roles in which this HandlerChain is to act
     * for the SOAP message processing at this SOAP node. These roles
     * assumed by a HandlerChain must be invariant during the
     * processing of an individual SOAP message through the HandlerChain.
     * <p>
     * A <code>HandlerChain</code> always acts in the role of the
     * special SOAP actor <code>next</code>. Refer to the SOAP
     * specification for the URI name for this special SOAP actor.
     * There is no need to set this special role using this method.
     *
     * @param soapActorNames URIs for SOAP actor name
     */
    public void setRoles(String[] soapActorNames);

    /**
     * Gets SOAP actor roles registered for this HandlerChain at
     * this SOAP node. The returned array includes the special
     * SOAP actor <code>next</code>.
     * @return String[] SOAP Actor roles as URIs
     */
    public java.lang.String[] getRoles();
}

