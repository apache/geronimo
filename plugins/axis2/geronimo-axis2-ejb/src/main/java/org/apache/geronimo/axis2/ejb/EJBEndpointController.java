/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.geronimo.axis2.ejb;

import javax.interceptor.InvocationContext;
import javax.xml.ws.Provider;

import org.apache.axis2.jaxws.core.MessageContext;
import org.apache.axis2.jaxws.server.EndpointController;
import org.apache.axis2.jaxws.server.dispatcher.EndpointDispatcher;

public class EJBEndpointController extends EndpointController {

    private InvocationContext invContext;

    public EJBEndpointController(InvocationContext invContext) {
        this.invContext = invContext;
    }

    @Override
    protected EndpointDispatcher getEndpointDispatcher(MessageContext mc,
                                                       Class serviceImplClass,
                                                       Object serviceInstance)
        throws Exception {
        if (Provider.class.isAssignableFrom(serviceImplClass)) {
            return new EJBProviderDispatcher(serviceImplClass, this.invContext);
        } else {
            return new EJBServiceDispatcher(serviceImplClass, this.invContext);
        }
    }

}
