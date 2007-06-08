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
package org.apache.geronimo.axis2.pojo;

import org.apache.axis2.jaxws.context.factory.MessageContextFactory;
import org.apache.axis2.jaxws.context.utils.ContextUtils;
import org.apache.axis2.jaxws.core.MessageContext;
import org.apache.axis2.jaxws.handler.SoapMessageContext;
import org.apache.axis2.jaxws.server.endpoint.lifecycle.EndpointLifecycleException;
import org.apache.axis2.jaxws.server.endpoint.lifecycle.EndpointLifecycleManager;
import org.apache.geronimo.jaxws.JAXWSAnnotationProcessor;

public class POJOEndpointLifecycleManager implements EndpointLifecycleManager {

    private JAXWSAnnotationProcessor annotationProcessor;
    private Object instance;
    
    public POJOEndpointLifecycleManager(JAXWSAnnotationProcessor annotationProcessor) {
        this.annotationProcessor = annotationProcessor;        
    }
    
    /* 
     * This method is called on each web service call.
     */
    public Object createServiceInstance(MessageContext context, Class serviceClass) throws EndpointLifecycleException {
        if (context == null) {
            // This is a special case, called at init time
            createServiceInstance(serviceClass);
        } else {
            // associate JAX-WS MessageContext with the thread
            POJOWebServiceContext.setMessageContext(createSOAPMessageContext(context));            
        }
        
        return this.instance;
    }
    
    private void createServiceInstance(Class serviceClass) throws EndpointLifecycleException {
        try {
            this.instance = serviceClass.newInstance();
            this.annotationProcessor.processAnnotations(instance);
            this.annotationProcessor.invokePostConstruct(instance);
        } catch (Exception e) {
            throw new EndpointLifecycleException(e);
        }
    }
    
    private javax.xml.ws.handler.MessageContext createSOAPMessageContext(MessageContext mc) {
        SoapMessageContext soapMessageContext =
                (SoapMessageContext)MessageContextFactory.createSoapMessageContext(mc);
        ContextUtils.addProperties(soapMessageContext, mc);
        return soapMessageContext;
    }
  
    public void invokePostConstruct() throws EndpointLifecycleException { 
    }

    public void invokePreDestroy() throws EndpointLifecycleException {
        if (this.instance != null) {
            this.annotationProcessor.invokePreDestroy(this.instance);
        }
    }
   
}
