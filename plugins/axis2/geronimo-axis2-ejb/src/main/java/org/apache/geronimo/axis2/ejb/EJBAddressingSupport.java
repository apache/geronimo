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

package org.apache.geronimo.axis2.ejb;

import javax.xml.ws.EndpointReference;
import javax.xml.ws.handler.MessageContext;

import org.apache.axis2.jaxws.context.WebServiceContextImpl;
import org.apache.openejb.core.webservices.AddressingSupport;
import org.w3c.dom.Element;

/**
 * @version $Rev$ $Date$
 */
public class EJBAddressingSupport implements AddressingSupport {

    private WebServiceContextImpl wsContext;
    
    public EJBAddressingSupport(MessageContext messageCtx) {
        this.wsContext = new WebServiceContextImpl();
        this.wsContext.setSoapMessageContext(messageCtx);
    }

    public EndpointReference getEndpointReference(Element... referenceParameters) {
        return this.wsContext.getEndpointReference(referenceParameters);
    }

    public <T extends EndpointReference> T getEndpointReference(Class<T> type, 
                                                                Element... referenceParameters) {
        return this.wsContext.getEndpointReference(type, referenceParameters);
    }
    
}
