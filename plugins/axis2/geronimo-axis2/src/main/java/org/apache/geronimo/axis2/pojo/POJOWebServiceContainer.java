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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.geronimo.axis2.Axis2WebServiceContainer;
import org.apache.geronimo.jaxws.JAXWSAnnotationProcessor;
import org.apache.geronimo.jaxws.PortInfo;
import org.apache.geronimo.jaxws.annotations.AnnotationHolder;

/**
 * @version $Rev$ $Date$
 */
public class POJOWebServiceContainer extends Axis2WebServiceContainer
{
    private static final Logger LOG = LoggerFactory.getLogger(POJOWebServiceContainer.class);
    
    private Object endpointInstance;
    private String contextRoot;
    private AnnotationHolder holder;
    
    public POJOWebServiceContainer(PortInfo portInfo,
                                   String endpointClassName,
                                   ClassLoader classLoader,
                                   Context context,
                                   URL configurationBaseUrl,
                                   AnnotationHolder holder,
                                   String contextRoot) {
        super(portInfo, endpointClassName, classLoader, context, configurationBaseUrl);
        this.holder = holder;
        this.contextRoot = contextRoot;
    }
    
    @Override
    public void init() throws Exception { 
        super.init();
        
        /*
         * This replaces EndpointLifecycleManagerFactory for all web services.
         * This should be ok as we do our own endpoint instance management and injection.       
         */
        FactoryRegistry.setFactory(EndpointLifecycleManagerFactory.class, 
                                   new POJOEndpointLifecycleManagerFactory());
                                      
        String servicePath = trimContext(getServicePath(this.contextRoot));
        this.configurationContext.setServicePath(servicePath);
        //need to setContextRoot after servicePath as cachedServicePath is only built 
        //when setContextRoot is called.
        String rootContext = trimContext(this.contextRoot);
        this.configurationContext.setContextRoot(rootContext); 
        
        // instantiate and inject resources into service
        try {
            this.endpointInstance = this.holder.newInstance(this.endpointClass.getName(), 
                                                            this.endpointClass.getClassLoader(), 
                                                            this.context);
        } catch (Exception e) {
            throw new WebServiceException("Service resource injection failed", e);
        }
        
        this.annotationProcessor = 
            new JAXWSAnnotationProcessor(this.jndiResolver, new POJOWebServiceContext());

        // configure and inject handlers
        try {
            configureHandlers();
            injectHandlers();
        } catch (Exception e) {
            throw new WebServiceException("Error configuring handlers", e);
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
         
    @Override
    public void destroy() {
        // call handler preDestroy
        destroyHandlers();
        
        // call service preDestroy
        if (this.endpointInstance != null) {
            try {
                this.holder.destroyInstance(this.endpointInstance);
            } catch (Exception e) {
                LOG.warn("Error calling @PreDestroy method", e); 
            }
        }
        
        super.destroy();
    }
}
