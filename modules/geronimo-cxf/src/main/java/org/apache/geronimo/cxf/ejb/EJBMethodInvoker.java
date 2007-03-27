/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.apache.geronimo.cxf.ejb;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import javax.xml.ws.handler.MessageContext;

import org.apache.cxf.interceptor.Fault;
import org.apache.cxf.jaxws.JAXWSMethodInvoker;
import org.apache.cxf.jaxws.context.WebServiceContextImpl;
import org.apache.cxf.jaxws.support.ContextPropertiesMapping;
import org.apache.cxf.message.Exchange;
import org.apache.openejb.DeploymentInfo;
import org.apache.openejb.OpenEJBException;
import org.apache.openejb.RpcContainer;

public class EJBMethodInvoker extends JAXWSMethodInvoker {

    private DeploymentInfo deploymentInfo;

    public EJBMethodInvoker(DeploymentInfo deploymentInfo) {
        super(null, null);
        this.deploymentInfo = deploymentInfo;
    }

    public Object getServiceObject(Exchange context) {
        return null;
    }

    protected Object invoke(Exchange exchange,
                            Object serviceObject,
                            Method m,
                            List<Object> params) {

        MessageContext ctx = ContextPropertiesMapping.createWebServiceContext(exchange);
        WebServiceContextImpl.setMessageContext(ctx);

        Object[] paramArray = new Object[] {};
        if (params != null) {
            paramArray = params.toArray();
        }

        insertExchange(m, paramArray, exchange);
        
        RpcContainer container = (RpcContainer) this.deploymentInfo.getContainer();
        Object result = null;
        try {
            result = container.invoke(this.deploymentInfo.getDeploymentID(), m, paramArray, null, null);
        } catch (OpenEJBException e) {
            throw new Fault(e);           
        } catch (RuntimeException e) {
            throw new Fault(e);
        }
        
        if (exchange.isOneWay()) {
            return null;
        }

        List<Object> retList = new ArrayList<Object>(1);
        if (!((Class) m.getReturnType()).getName().equals("void")) {
            retList.add(result);
        }

        ContextPropertiesMapping.updateWebServiceContext(exchange, ctx);

        return retList;
    }
}
