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

import javax.xml.namespace.QName;

/**
 * The <code>javax.xml.rpc.handler.Handler</code> interface is
 * required to be implemented by a SOAP message handler. The
 * <code>handleRequest</code>, <code>handleResponse</code>
 * and <code>handleFault</code> methods for a SOAP message
 * handler get access to the <code>SOAPMessage</code> from the
 * <code>SOAPMessageContext</code>. The implementation of these
 * methods can modify the <code>SOAPMessage</code> including the
 * headers and body elements.
 *
 * @version 1.0
 */
public interface Handler {

    /**
     * The <code>handleRequest</code> method processes the request message.
     *
     * @param context MessageContext parameter provides access to the request
     *                  message.
     * @return boolean boolean Indicates the processing mode
     *            <ul>
     *            <li>Return <code>true</code> to indicate continued
     *                processing of the request handler chain. The
     *                <code>HandlerChain</code>
     *                takes the responsibility of invoking the next
     *                entity. The next entity may be the next handler
     *                in the <code>HandlerChain</code> or if this
     *                handler is the last handler in the chain, the
     *                next entity is the service endpoint object.
     *            <li>Return <code>false</code> to indicate blocking
     *                of the request handler chain. In this case,
     *                further processing of the request handler chain
     *                is blocked and the target service endpoint is
     *                not dispatched. The JAX-RPC runtime system takes
     *                the responsibility of invoking the response
     *                handler chain next with the SOAPMessageContext.
     *                The Handler implementation class has the the
     *                responsibility of setting the appropriate response
     *                SOAP message in either handleRequest and/or
     *                handleResponse method. In the default processing
     *                model, the response handler chain starts processing
     *                from the same Handler instance (that returned false)
     *                and goes backward in the execution sequence.
     *             </ul>
     *
     * @throws javax.xml.rpc.JAXRPCException
     *                indicates a handler-specific
     *                runtime error. If <code>JAXRPCException</code> is thrown
     *                by a handleRequest method, the HandlerChain
     *                terminates the further processing of this handler
     *                chain. On the server side, the HandlerChain
     *                generates a SOAP fault that indicates that the
     *                message could not be processed for reasons not
     *                directly attributable to the contents of the
     *                message itself but rather to a runtime error
     *                during the processing of the message. On the
     *                client side, the exception is propagated to
     *                the client code
     * @throws javax.xml.rpc.soap.SOAPFaultException
     *                indicates a SOAP fault. The Handler
     *                implementation class has the the responsibility
     *                of setting the SOAP fault in the SOAP message in
     *                either handleRequest and/or handleFault method.
     *                If SOAPFaultException is thrown by a server-side
     *                request handler's handleRequest method, the
     *                HandlerChain terminates the further processing
     *                of the request handlers in this handler chain
     *                and invokes the handleFault method on the
     *                HandlerChain with the SOAP message context. Next,
     *                the HandlerChain invokes the handleFault method
     *                on handlers registered in the handler chain,
     *                beginning with the Handler instance that threw
     *                the exception and going backward in execution. The
     *                client-side request handler's handleRequest method
     *                should not throw the SOAPFaultException.
     */
    public boolean handleRequest(MessageContext context);

    /**
     * The <code>handleResponse</code> method processes the response SOAP message.
     *
     * @param context MessageContext parameter provides access to
     *            the response SOAP message
     *
     * @return boolean Indicates the processing mode
     *            <ul>
     *            <li>Return <code>true</code> to indicate continued
     *                processing ofthe response handler chain. The
     *                HandlerChain invokes the <code>handleResponse</code>
     *                method on the next <code>Handler</code> in
     *                the handler chain.
     *            <li>Return <code>false</code> to indicate blocking
     *                of the response handler chain. In this case, no
     *                other response handlers in the handler chain
     *                are invoked.
     *            </ul>
     *
     * @throws javax.xml.rpc.JAXRPCException
     *                indicates a handler specific runtime error.
     *                If JAXRPCException is thrown by a handleResponse
     *                method, the HandlerChain terminates the further
     *                processing of this handler chain. On the server side,
     *                the HandlerChain generates a SOAP fault that
     *                indicates that the message could not be processed
     *                for reasons not directly attributable to the contents
     *                of the message itself but rather to a runtime error
     *                during the processing of the message. On the client
     *                side, the runtime exception is propagated to the
     *                client code.
     */
    public boolean handleResponse(MessageContext context);

    /**
     * The <code>handleFault</code> method processes the SOAP faults
     * based on the SOAP message processing model.
     *
     * @param  context MessageContext parameter provides access to
     *            the SOAP message
     * @return boolean Indicates the processing mode
     *            <ul>
     *            <li>Return <code>true</code> to indicate continued
     *                processing of SOAP Fault. The HandlerChain invokes
     *                the <code>handleFault</code> method on the
     *                next <code>Handler</code> in the handler chain.
     *            <li>Return <code>false</code> to indicate end
     *                of the SOAP fault processing. In this case, no
     *                other handlers in the handler chain
     *                are invoked.
     *            </ul>
     * @throws javax.xml.rpc.JAXRPCException indicates handler specific runtime
     *                error. If JAXRPCException is thrown by a handleFault
     *                method, the HandlerChain terminates the further
     *                processing of this handler chain. On the server side,
     *                the HandlerChain generates a SOAP fault that
     *                indicates that the message could not be processed
     *                for reasons not directly attributable to the contents
     *                of the message itself but rather to a runtime error
     *                during the processing of the message. On the client
     *                side, the JAXRPCException is propagated to the
     *                client code.
     */
    public boolean handleFault(MessageContext context);

    /**
     * The <code>init</code> method enables the Handler instance to
     * initialize itself. The <code>init</code> method passes the
     * handler configuration as a <code>HandlerInfo</code> instance.
     * The HandlerInfo is used to configure the Handler (for example:
     * setup access to an external resource or service) during the
     * initialization.
     * <p>
     * In the init method, the Handler class may get access to
     * any resources (for example; access to a logging service or
     * database) and maintain these as part of its instance variables.
     * Note that these instance variables must not have any state
     * specific to the SOAP message processing performed in the
     * various handle method.
     *
     * @param config HandlerInfo configuration for the initialization of this
     *              handler
     *
     * @param config
     * @throws javax.xml.rpc.JAXRPCException if initialization of the handler
     *              fails
     */
    public abstract void init(HandlerInfo config);

    /**
     * The <code>destroy</code> method indicates the end of lifecycle
     * for a Handler instance.  The Handler implementation class should
     * release its resources and perform cleanup in the implementation
     * of the <code>destroy</code> method.
     *
     * @throws  javax.xml.rpc.JAXRPCException  if there was any error during
     *              destroy
     */
    public abstract void destroy();

    /**
     * Gets the header blocks processed by this Handler instance.
     *
     * @return Array of QNames of header blocks processed by this
     *      handler instance. <code>QName</code> is the qualified
     *      name of the outermost element of the Header block.
     */
    public QName[] getHeaders();
}

