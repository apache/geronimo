/**
 *
 * Copyright 2004 The Apache Software Foundation
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.apache.geronimo.axis.server;

import javax.xml.rpc.holders.IntHolder;

import org.apache.axis.providers.java.RPCProvider;
import org.apache.axis.MessageContext;
import org.apache.axis.Handler;
import org.apache.geronimo.webservices.WebServiceContainer;

/**
 * @version $Rev$ $Date$
 */
public class POJOProvider extends RPCProvider {
    public POJOProvider() {
    }

    public Object getServiceObject(MessageContext msgContext, Handler service, String clsName, IntHolder scopeHolder) throws Exception {
        WebServiceContainer.Request request = (WebServiceContainer.Request) msgContext.getProperty(AxisWebServiceContainer.REQUEST);
        return request.getAttribute(WebServiceContainer.POJO_INSTANCE);
    }
}
