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
package javax.xml.rpc.handler.soap;

import javax.xml.rpc.handler.MessageContext;
import javax.xml.soap.SOAPMessage;

/**
 * The interface <code>javax.xml.rpc.soap.SOAPMessageContext</code>
 * provides access to the SOAP message for either RPC request or
 * response. The <code>javax.xml.soap.SOAPMessage</code> specifies
 * the standard Java API for the representation of a SOAP 1.1 message
 * with attachments.
 *
 * @version 1.0
 * @see javax.xml.soap.SOAPMessage
 */
public interface SOAPMessageContext extends MessageContext {

    /**
     *  Gets the SOAPMessage from this message context.
     *
     *  @return the <code>SOAPMessage</code>; <code>null</code> if no request
     *          <code>SOAPMessage</code> is present in this
     *          <code>SOAPMessageContext</code>
     */
    public abstract SOAPMessage getMessage();

    /**
     *  Sets the <code>SOAPMessage</code> for this message context.
     *
     *  @param   message  SOAP message
     *  @throws  javax.xml.rpc.JAXRPCException  if any error during the setting
     *     of the SOAPMessage in this message context
     *  @throws java.lang.UnsupportedOperationException if this
     *     operation is not supported
     */
    public abstract void setMessage(SOAPMessage message);

    /**
     * Gets the SOAP actor roles associated with an execution
     * of the HandlerChain and its contained Handler instances.
     * Note that SOAP actor roles apply to the SOAP node and
     * are managed using <code>HandlerChain.setRoles</code> and
     * <code>HandlerChain.getRoles</code>. Handler instances in
     * the HandlerChain use this information about the SOAP actor
     * roles to process the SOAP header blocks. Note that the
     * SOAP actor roles are invariant during the processing of
     * SOAP message through the HandlerChain.
     *
     * @return Array of URIs for SOAP actor roles
     * @see javax.xml.rpc.handler.HandlerChain#setRoles(java.lang.String[]) HandlerChain.setRoles(java.lang.String[])
     * @see javax.xml.rpc.handler.HandlerChain#getRoles() HandlerChain.getRoles()
     */
    public abstract String[] getRoles();
}

