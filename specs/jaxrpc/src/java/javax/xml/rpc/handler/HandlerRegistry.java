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
import java.io.Serializable;

/**
 * The <code>javax.xml.rpc.handler.HandlerRegistry</code>
 * provides support for the programmatic configuration of
 * handlers in a <code>HandlerRegistry</code>.
 * <p>
 * A handler chain is registered per service endpoint, as
 * indicated by the qualified name of a port. The getHandlerChain
 * returns the handler chain (as a java.util.List) for the
 * specified service endpoint. The returned handler chain is
 * configured using the java.util.List interface. Each element
 * in this list is required to be of the Java type
 * <code>javax.xml.rpc.handler.HandlerInfo</code>
 *
 * @version 1.0
 */
public interface HandlerRegistry extends Serializable {

    /**
     * Gets the handler chain for the specified service endpoint.
     * The returned <code>List</code> is used to configure this
     * specific handler chain in this <code>HandlerRegistry</code>.
     * Each element in this list is required to be of the Java type
     * <code>javax.xml.rpc.handler.HandlerInfo</code>.
     *
     * @param   portName Qualified name of the target service
     * @return  HandlerChain java.util.List Handler chain
     * @throws java.lang.IllegalArgumentException If an invalid <code>portName</code> is specified
     */
    public java.util.List getHandlerChain(QName portName);

    /**
     * Sets the handler chain for the specified service endpoint
     * as a <code>java.util.List</code>. Each element in this list
     * is required to be of the Java type
     * <code>javax.xml.rpc.handler.HandlerInfo</code>.
     *
     *  @param   portName Qualified name of the target service endpoint
     *  @param   chain a List representing configuration for the
     *             handler chain
     *  @throws  javax.xml.rpc.JAXRPCException if there is any error in the
     *             configuration of the handler chain
     *  @throws java.lang.UnsupportedOperationException if this
     *     set operation is not supported. This is done to
     *     avoid any overriding of a pre-configured handler
     *     chain.
     *  @throws java.lang.IllegalArgumentException If an invalid
     *     <code>portName</code> is specified
     */
    public abstract void setHandlerChain(QName portName, java.util.List chain);
}

