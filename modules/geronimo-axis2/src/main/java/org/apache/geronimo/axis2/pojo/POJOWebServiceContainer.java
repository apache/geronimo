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

package org.apache.geronimo.axis2.pojo;

import java.net.URL;

import javax.naming.Context;
import javax.xml.ws.WebServiceException;

import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.context.ServiceContext;
import org.apache.axis2.description.AxisService;
import org.apache.axis2.jaxws.registry.FactoryRegistry;
import org.apache.axis2.jaxws.server.endpoint.lifecycle.factory.EndpointLifecycleManagerFactory;
import org.apache.axis2.transport.http.HTTPConstants;
import org.apache.axis2.transport.http.HTTPTransportUtils;
import org.apache.axis2.util.JavaUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.geronimo.axis2.Axis2WebServiceContainer;
import org.apache.geronimo.jaxws.JAXWSAnnotationProcessor;
import org.apache.geronimo.jaxws.PortInfo;
import org.apache.geronimo.jaxws.annotations.AnnotationException;

// FIXME: improve handler support, handler injection, thread-safetly for handlers

/**
 * @version $Rev$ $Date$
 */
public class POJOWebServiceContainer extends Axis2WebServiceContainer {

    private static final Log LOG = LogFactory.getLog(POJOWebServiceContainer.class);
    
    private Object endpointInstance;
    private String contextRoot = null;

    
    public POJOWebServiceContainer(PortInfo portInfo,
                                   String endpointClassName,
                                   ClassLoader classLoader,
                                   Context context,
                                   URL configurationBaseUrl) {
        super(portInfo, endpointClassName, classLoader, context, configurationBaseUrl);        
    }
    
    @Override
    public void init() throws Exception {
        // XXX: This is a global operation
        FactoryRegistry.setFactory(EndpointLifecycleManagerFactory.class, 
                                   new POJOEndpointLifecycleManagerFactory());
        
        super.init();
        
        this.endpointInstance = this.endpointClass.newInstance();
        
        this.configurationContext.setServicePath(this.portInfo.getLocation());
        this.annotationProcessor = 
            new JAXWSAnnotationProcessor(this.jndiResolver, new POJOWebServiceContext());

        // configure and inject handlers
        try {
            configureHandlers();
            injectHandlers();
        } catch (Exception e) {
            throw new WebServiceException("Error configuring handlers", e);
        }
        
        // inject resources into service
        try {
            injectResources(this.endpointInstance);
        } catch (AnnotationException e) {
            throw new WebServiceException("Service resource injection failed", e);
        }
    }
    
    @Override
    protected void processPOSTRequest(Request request, Response response, AxisService service, MessageContext msgContext) throws Exception {
        String contentType = request.getHeader(HTTPConstants.HEADER_CONTENT_TYPE);
        String soapAction = request.getHeader(HTTPConstants.HEADER_SOAP_ACTION);
        if (soapAction == null) {
            soapAction = "\"\"";
        }

        ConfigurationContext configurationContext = msgContext.getConfigurationContext();
        configurationContext.fillServiceContextAndServiceGroupContext(msgContext);
        
        setMsgContextProperties(request, response, service, msgContext);

        ServiceContext serviceContext = msgContext.getServiceContext();
        serviceContext.setProperty(ServiceContext.SERVICE_OBJECT, this.endpointInstance);

        try {
            HTTPTransportUtils.processHTTPPostRequest(msgContext,
                                                      request.getInputStream(),
                                                      response.getOutputStream(),
                                                      contentType,
                                                      soapAction,
                                                      request.getURI().getPath());
        } finally {                        
            // de-associate JAX-WS MessageContext with the thread
            // (association happens in POJOEndpointLifecycleManager.createService() call)
            POJOWebServiceContext.clear();
        } 
    }
    
    protected void initContextRoot(Request request) {
        if (contextRoot == null || "".equals(contextRoot)) {
            String[] parts = JavaUtils.split(request.getContextPath(), '/');
            if (parts != null) {
                for (int i = 0; i < parts.length; i++) {
                    if (parts[i].length() > 0) {
                        contextRoot = parts[i];
                        break;
                    }
                }
            }
            if (contextRoot == null || request.getContextPath().equals("/")) {
                contextRoot = "/";
            }
            //need to setContextRoot after servicePath as cachedServicePath is only built 
            //when setContextRoot is called.
            configurationContext.setContextRoot(contextRoot);  
        }
    }     
    
    @Override
    public void destroy() {
        // call handler preDestroy
        destroyHandlers();
        
        // call service preDestroy
        if (this.endpointInstance != null) {
            this.annotationProcessor.invokePreDestroy(this.endpointInstance);
        }
        
        super.destroy();
    }
}
