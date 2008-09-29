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
import java.util.List;

import javax.interceptor.InvocationContext;
import javax.xml.ws.handler.MessageContext;
import javax.xml.ws.handler.MessageContext.Scope;
import javax.xml.ws.soap.SOAPFaultException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.cxf.Bus;
import org.apache.cxf.binding.soap.SoapFault;
import org.apache.cxf.interceptor.Fault;
import org.apache.cxf.jaxws.context.WebServiceContextImpl;
import org.apache.cxf.message.Exchange;
import org.apache.cxf.message.FaultMode;
import org.apache.cxf.message.MessageContentsList;
import org.apache.cxf.service.invoker.AbstractInvoker;
import org.apache.openejb.DeploymentInfo;
import org.apache.openejb.RpcContainer;

public class EJBMethodInvoker extends AbstractInvoker {

    private static final Logger LOG = LoggerFactory.getLogger(EJBMethodInvoker.class);
    
    private DeploymentInfo deploymentInfo;
    private Bus bus;
    private EJBEndpoint endpoint;

    public EJBMethodInvoker(EJBEndpoint endpoint, Bus bus, DeploymentInfo deploymentInfo) {
        this.endpoint = endpoint;
        this.bus = bus;
        this.deploymentInfo = deploymentInfo;
    }

    @Override
    public Object getServiceObject(Exchange context) {
        return null;
    }
    
    @Override
    public void releaseServiceObject(Exchange ex, Object obj) {
        // do nothing
    }

    private SOAPFaultException findSoapFaultException(Throwable ex) {
        if (ex instanceof SOAPFaultException) {
            return (SOAPFaultException)ex;
        }
        if (ex.getCause() != null) {
            return findSoapFaultException(ex.getCause());
        }
        return null;
    }
    
    @Override
    protected Fault createFault(Throwable ex, Method m, List<Object> params, boolean checked) {
        //map the JAX-WS faults
        SOAPFaultException sfe = findSoapFaultException(ex);
        if (sfe != null) {
            SoapFault fault = new SoapFault(sfe.getFault().getFaultString(),
                                            ex,
                                            sfe.getFault().getFaultCodeAsQName());
            fault.setRole(sfe.getFault().getFaultActor());
            fault.setDetail(sfe.getFault().getDetail());
            
            return fault;
        }
        return super.createFault(ex, m, params, checked);
    }
    
    @Override
    protected Object invoke(Exchange exchange, 
                            Object serviceObject, 
                            Method m, 
                            List<Object> params) {       
        Object result = null;
        
        InvocationContext invContext = exchange.get(InvocationContext.class);
        if (invContext == null) {
            LOG.debug("PreEJBInvoke");
            result = preEjbInvoke(exchange, serviceObject, m, params);
        } else {
            LOG.debug("EJBInvoke"); // calls performInvocation()
            result = super.invoke(exchange, serviceObject, m, params);
        }    
        
        return result;
    }
    
    @Override
    protected Object performInvocation(Exchange exchange, 
                                       Object serviceObject, 
                                       Method m,
                                       Object[] paramArray) throws Exception {
        InvocationContext invContext = exchange.get(InvocationContext.class);
        invContext.setParameters(paramArray);
        Object res = invContext.proceed();
        
//        ContextPropertiesMapping.updateWebServiceContext(exchange, 
//                                                         (MessageContext)invContext.getContextData());
                
        return res;
    }
    
    private Object preEjbInvoke(Exchange exchange, 
                                Object serviceObject, 
                                Method method, 
                                List<Object> params) {           
        
        MessageContext ctx = new EJBMessageContext(exchange.getInMessage(), Scope.APPLICATION);
        WebServiceContextImpl.setMessageContext(ctx);

        try {           
            EJBInterceptor interceptor = new EJBInterceptor(params, method, this.endpoint, this.bus, exchange);
            Object[] arguments = { ctx, interceptor, ctx };

            RpcContainer container = (RpcContainer) this.deploymentInfo.getContainer();

            Class callInterface = this.deploymentInfo.getServiceEndpointInterface();
            method = getMostSpecificMethod(method, callInterface);
            Object res = container.invoke(this.deploymentInfo.getDeploymentID(), callInterface, method, arguments, null);

            if (exchange.isOneWay()) {
                return null;
            }

            return new MessageContentsList(res);
        } catch (Fault f) {
            exchange.getInMessage().put(FaultMode.class, FaultMode.UNCHECKED_APPLICATION_FAULT);
            throw f;
        } catch (Exception e) {
            exchange.getInMessage().put(FaultMode.class, FaultMode.UNCHECKED_APPLICATION_FAULT);
            throw createFault(e, method, params, false);
        } finally {
            WebServiceContextImpl.clear();
        }
    }
    
    public Object directEjbInvoke(Exchange exchange, 
                                  Method m, 
                                  List<Object> params) throws Exception {        
        Object[] paramArray;
        if (params != null) {
            paramArray = params.toArray();
        } else {
            paramArray = new Object[]{};
        }
        return performInvocation(exchange, null, m, paramArray);
    }
            
}
