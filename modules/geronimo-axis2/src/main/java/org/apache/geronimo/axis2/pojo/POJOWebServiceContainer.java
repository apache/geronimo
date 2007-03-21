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

import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.context.ServiceContext;
import org.apache.axis2.context.ServiceGroupContext;
import org.apache.axis2.description.AxisService;
import org.apache.axis2.engine.DependencyManager;
import org.apache.axis2.jaxws.binding.BindingImpl;
import org.apache.axis2.jaxws.server.JAXWSMessageReceiver;
import org.apache.axis2.transport.http.HTTPConstants;
import org.apache.axis2.transport.http.HTTPTransportUtils;
import org.apache.axis2.util.JavaUtils;
import org.apache.geronimo.axis2.Axis2HandlerResolver;
import org.apache.geronimo.axis2.Axis2WebServiceContainer;
import org.apache.geronimo.jaxws.JAXWSAnnotationProcessor;
import org.apache.geronimo.jaxws.JNDIResolver;
import org.apache.geronimo.jaxws.PortInfo;
import org.apache.geronimo.jaxws.annotations.AnnotationException;
import org.apache.geronimo.xbeans.javaee.HandlerChainsType;
import org.apache.geronimo.xbeans.javaee.HandlerChainsDocument;

import javax.naming.Context;
import javax.xml.ws.WebServiceException;
import javax.xml.ws.handler.Handler;
import java.net.URL;
import java.util.List;

/**
 * @version $Rev$ $Date$
 */
public class POJOWebServiceContainer extends Axis2WebServiceContainer {

    private JAXWSAnnotationProcessor annotationProcessor;
    private Object endpointInstance;
    private List<Handler> chain;
    private String contextRoot = null;
    
    public POJOWebServiceContainer(PortInfo portInfo,
                                    String endpointClassName,
                                    ClassLoader classLoader,
                                    Context context,
                                    URL configurationBaseUrl) {
        super(portInfo, endpointClassName, classLoader, context, configurationBaseUrl);
        configurationContext.setServicePath(portInfo.getLocation());
    }
    
    protected void processPostRequest (Request request, Response response, AxisService service, ConfigurationContext configurationContext, MessageContext msgContext, String soapAction, JNDIResolver jndiResolver) throws Exception {        
        String contenttype = request.getHeader(HTTPConstants.HEADER_CONTENT_TYPE);
        msgContext.setAxisService(service);
        configurationContext.fillServiceContextAndServiceGroupContext(msgContext);
        ServiceGroupContext serviceGroupContext = msgContext.getServiceGroupContext();
        DependencyManager.initService(serviceGroupContext);
        endpointInstance = msgContext.getServiceContext().getProperty(ServiceContext.SERVICE_OBJECT);
        
        setMsgContextProperties(msgContext, service, response, request);
        annotationProcessor = new JAXWSAnnotationProcessor(jndiResolver,
                new POJOWebServiceContext(msgContext));
        init();
        try {
            HTTPTransportUtils.processHTTPPostRequest(
                    msgContext,
                    request.getInputStream(),
                    response.getOutputStream(),
                    contenttype,
                    soapAction,
                    request.getURI().getPath());
        } finally {
            stop();
        }        
    }
    
    protected void setMsgContextProperties(MessageContext msgContext, AxisService service, Response response, Request request) {
        BindingImpl binding = new BindingImpl("GeronimoBinding");
        binding.setHandlerChain(chain);
        msgContext.setProperty(JAXWSMessageReceiver.PARAM_BINDING, binding);
        
        super.setMsgContextProperties(msgContext, service, response, request);
    }
    
    protected void init() {
        // configure and inject handlers
        try {
            configureHandlers();
        } catch (Exception e) {
            throw new WebServiceException("Error configuring handlers", e);
        }

        // inject resources into service
        try {
            injectResources(endpointInstance);
        } catch (AnnotationException e) {
            throw new WebServiceException("Service resource injection failed", e);
        }
    }

    /*
     * Gets the right handlers for the port/service/bindings and
     * performs injection.
     */
    protected void configureHandlers() throws Exception {
        String xml = this.portInfo.getHandlersAsXML();
        HandlerChainsType handlerChains = xml == null ? null : HandlerChainsDocument.Factory.parse(xml).getHandlerChains();
        Axis2HandlerResolver handlerResolver =
            new Axis2HandlerResolver(endpointInstance.getClass().getClassLoader(),
                                   endpointInstance.getClass(),
                                   handlerChains,
                                   this.annotationProcessor);


        // TODO: pass non-null PortInfo to get the right handlers
        chain = handlerResolver.getHandlerChain(null);
    }

    public void stop() {
        // call handlers preDestroy
        for (Handler handler : chain) {
            this.annotationProcessor.invokePreDestroy(handler);
        }

        // call service preDestroy
        this.annotationProcessor.invokePreDestroy(endpointInstance);
    }

    private void injectResources(Object instance) throws AnnotationException {
        if (instance == null)
            System.out.println("instance is null");
        if (annotationProcessor == null)
            System.out.println("annotationProcessor is null");
        this.annotationProcessor.processAnnotations(instance);
        this.annotationProcessor.invokePostConstruct(instance);
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
}
