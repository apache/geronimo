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

package org.apache.geronimo.axis2;

import java.io.ByteArrayInputStream;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;

import javax.naming.Context;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.ws.Binding;
import javax.xml.ws.WebServiceException;
import javax.xml.ws.handler.Handler;

import org.apache.axiom.om.util.UUIDGenerator;
import org.apache.axis2.AxisFault;
import org.apache.axis2.Constants;
import org.apache.axis2.addressing.AddressingHelper;
import org.apache.axis2.addressing.EndpointReference;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.ConfigurationContextFactory;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.context.OperationContext;
import org.apache.axis2.description.AxisService;
import org.apache.axis2.description.TransportInDescription;
import org.apache.axis2.description.TransportOutDescription;
import org.apache.axis2.engine.AxisEngine;
import org.apache.axis2.engine.Handler.InvocationResponse;
import org.apache.axis2.jaxws.binding.BindingImpl;
import org.apache.axis2.jaxws.binding.BindingUtils;
import org.apache.axis2.jaxws.description.EndpointDescription;
import org.apache.axis2.jaxws.description.impl.DescriptionUtils;
import org.apache.axis2.jaxws.description.xml.handler.HandlerChainType;
import org.apache.axis2.jaxws.description.xml.handler.HandlerChainsType;
import org.apache.axis2.jaxws.description.xml.handler.HandlerType;
import org.apache.axis2.jaxws.handler.lifecycle.factory.HandlerLifecycleManagerFactory;
import org.apache.axis2.jaxws.registry.FactoryRegistry;
import org.apache.axis2.jaxws.server.JAXWSMessageReceiver;
import org.apache.axis2.transport.OutTransportInfo;
import org.apache.axis2.transport.RequestResponseTransport;
import org.apache.axis2.transport.http.HTTPConstants;
import org.apache.axis2.transport.http.HTTPTransportReceiver;
import org.apache.axis2.transport.http.HTTPTransportUtils;
import org.apache.axis2.transport.http.TransportHeaders;
import org.apache.axis2.transport.http.util.RESTUtil;
import org.apache.axis2.util.MessageContextBuilder;
import org.apache.geronimo.axis2.client.Axis2ConfigGBean;
import org.apache.geronimo.jaxws.JAXWSAnnotationProcessor;
import org.apache.geronimo.jaxws.JAXWSUtils;
import org.apache.geronimo.jaxws.JNDIResolver;
import org.apache.geronimo.jaxws.PortInfo;
import org.apache.geronimo.jaxws.ServerJNDIResolver;
import org.apache.geronimo.jaxws.annotations.AnnotationException;
import org.apache.geronimo.webservices.WebServiceContainer;
import org.apache.geronimo.webservices.saaj.SAAJUniverse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @version $Rev$ $Date$
 */
public abstract class Axis2WebServiceContainer implements WebServiceContainer
{
    private static final Logger LOG = LoggerFactory.getLogger(Axis2WebServiceContainer.class);

    public static final String REQUEST = Axis2WebServiceContainer.class.getName() + "@Request";
    public static final String RESPONSE = Axis2WebServiceContainer.class.getName() + "@Response";

    private transient final ClassLoader classLoader;
    
    protected String endpointClassName;
    protected org.apache.geronimo.jaxws.PortInfo portInfo;
    protected ConfigurationContext configurationContext;
    protected JNDIResolver jndiResolver;
    protected Class endpointClass;
    protected AxisService service;
    protected URL configurationBaseUrl;
    protected WSDLQueryHandler wsdlQueryHandler;
    protected Binding binding;
    protected JAXWSAnnotationProcessor annotationProcessor;
    protected Context context;

    public Axis2WebServiceContainer(PortInfo portInfo,
                                    String endpointClassName,
                                    ClassLoader classLoader,
                                    Context context,
                                    URL configurationBaseUrl) {
        this.classLoader = classLoader;
        this.endpointClassName = endpointClassName;
        this.portInfo = portInfo;
        this.configurationBaseUrl = configurationBaseUrl;
        this.context = context;
        this.jndiResolver = new ServerJNDIResolver(context);
    }

    public void init() throws Exception {
        this.endpointClass = classLoader.loadClass(this.endpointClassName);
        
        Axis2ConfigGBean.registerClientConfigurationFactory();
        
        configurationContext = ConfigurationContextFactory.createBasicConfigurationContext("META-INF/geronimo-axis2.xml");

        // check to see if the wsdlLocation property is set in portInfo,
        // if not checking if wsdlLocation exists in annotation
        // if already set, annotation should not overwrite it.
        if (portInfo.getWsdlFile() == null || portInfo.getWsdlFile().equals("")) {
            // getwsdllocation from annotation if it exists
            if (JAXWSUtils.containsWsdlLocation(this.endpointClass, classLoader)) {
                portInfo.setWsdlFile(JAXWSUtils.getServiceWsdlLocation(this.endpointClass, classLoader));
            }
        }

        AxisServiceGenerator serviceGen = createServiceGenerator();
        if (portInfo.getWsdlFile() != null && !portInfo.getWsdlFile().equals("")) {
            // WSDL file has been provided
            service = serviceGen.getServiceFromWSDL(portInfo, endpointClass, configurationBaseUrl);
        } else {
            // No WSDL, let Axis2 handle it.
            service = serviceGen.getServiceFromClass(this.endpointClass);
        }

        service.setScope(Constants.SCOPE_APPLICATION);
        configurationContext.getAxisConfiguration().addService(service);

        this.wsdlQueryHandler = new WSDLQueryHandler(this.service);
        
        /*
         * This replaces HandlerLifecycleManagerFactory for all web services.
         * This should be ok as we do our own handler instance managment and injection.
         * Also, this does not affect service-ref clients, as we install our own
         * HandlerResolver.
         */        
        FactoryRegistry.setFactory(HandlerLifecycleManagerFactory.class, 
                                   new GeronimoHandlerLifecycleManagerFactory());                                   
    }  

    protected AxisServiceGenerator createServiceGenerator() {
        return new AxisServiceGenerator();
    }

    public void getWsdl(Request request, Response response) throws Exception {
        doService(request, response);
    }

    public void invoke(Request request, Response response) throws Exception {
        SAAJUniverse universe = new SAAJUniverse();
        universe.set(SAAJUniverse.AXIS2);
        try {
            doService(request, response);
        } finally {
            universe.unset();
        }        
    }

    protected void doService(final Request request, final Response response)
            throws Exception {        

        if (LOG.isDebugEnabled()) {
            LOG.debug("Target URI: " + request.getURI());
        }

        MessageContext msgContext = new MessageContext();
        msgContext.setIncomingTransportName(Constants.TRANSPORT_HTTP);
        msgContext.setProperty(MessageContext.REMOTE_ADDR, request.getRemoteAddr());

        try {
            TransportOutDescription transportOut = this.configurationContext.getAxisConfiguration()
                    .getTransportOut(Constants.TRANSPORT_HTTP);
            TransportInDescription transportIn = this.configurationContext.getAxisConfiguration()
                    .getTransportIn(Constants.TRANSPORT_HTTP);

            msgContext.setConfigurationContext(this.configurationContext);

            //TODO: Port this segment for session support.
//            String sessionKey = (String) this.httpcontext.getAttribute(HTTPConstants.COOKIE_STRING);
//            if (this.configurationContext.getAxisConfiguration().isManageTransportSession()) {
//                SessionContext sessionContext = this.sessionManager.getSessionContext(sessionKey);
//                msgContext.setSessionContext(sessionContext);
//            }
            msgContext.setTransportIn(transportIn);
            msgContext.setTransportOut(transportOut);
            msgContext.setServiceGroupContextId(UUIDGenerator.getUUID());
            msgContext.setServerSide(true);
            msgContext.setAxisService(this.service);
            
            doService2(request, response, msgContext);
        } catch (AxisFault e) {
            LOG.debug(e.getMessage(), e);
            handleFault(msgContext, response, e);
        } catch (Throwable e) {            
            String msg = "Exception occurred while trying to invoke service method doService()";
            LOG.error(msg, e);
            handleFault(msgContext, response, new AxisFault(msg, e));
        }

    }

    private void handleFault(MessageContext msgContext, Response response, AxisFault e) {
        // If the fault is not going along the back channel we should be 202ing
        if (AddressingHelper.isFaultRedirected(msgContext)) {
            response.setStatusCode(HttpURLConnection.HTTP_ACCEPTED);
        } else {
            response.setStatusCode(HttpURLConnection.HTTP_INTERNAL_ERROR);
        }
        
        msgContext.setProperty(MessageContext.TRANSPORT_OUT, response.getOutputStream());
        msgContext.setProperty(Constants.OUT_TRANSPORT_INFO, new Axis2TransportInfo(response));

        try {
            MessageContext faultContext = MessageContextBuilder.createFaultMessageContext(msgContext, e);
            AxisEngine.sendFault(faultContext);
        } catch (Exception ex) {
            LOG.warn("Error sending fault", ex);
            if (!AddressingHelper.isFaultRedirected(msgContext)) {
                response.setStatusCode(HttpURLConnection.HTTP_INTERNAL_ERROR);
                response.setHeader(HTTPConstants.HEADER_CONTENT_TYPE, "text/plain");
                PrintWriter pw = new PrintWriter(response.getOutputStream());
                ex.printStackTrace(pw);
                pw.flush();
            }
        }
    }
    
    protected String getServicePath(String contextRoot) {
        String location = this.portInfo.getLocation();
        if (location != null && location.startsWith(contextRoot)) {
            return location.substring(contextRoot.length());
        }
        return null;
    }
    
    public static String trimContext(String contextPath) {
        if (contextPath != null) {
            if (contextPath.startsWith("/")) {
                contextPath = contextPath.substring(1);
            }
            if (contextPath.endsWith("/")) {
                contextPath = contextPath.substring(0, contextPath.length() - 1);
            }
        }
        return contextPath;
    }
    
    public void doService2(Request request,
                           Response response,
                           MessageContext msgContext) throws Exception {
                
        if (request.getMethod() == Request.GET) {
            processGETRequest(request, response, this.service, msgContext);
        } else if (request.getMethod() == Request.POST) {
            processPOSTRequest(request, response, this.service, msgContext);
        } else {
            throw new UnsupportedOperationException("[" + request.getMethod() + " ] method not supported");
        }

        // Finalize response
        OperationContext operationContext = msgContext.getOperationContext();
        Object contextWritten = null;
        Object isTwoChannel = null;
        if (operationContext != null) {
            contextWritten = operationContext.getProperty(Constants.RESPONSE_WRITTEN);
            isTwoChannel = operationContext.getProperty(Constants.DIFFERENT_EPR);
        }

        if ((contextWritten != null) && Constants.VALUE_TRUE.equals(contextWritten)) {
            if ((isTwoChannel != null) && Constants.VALUE_TRUE.equals(isTwoChannel)) {
                response.setStatusCode(HttpURLConnection.HTTP_ACCEPTED);
                return;
            }
            response.setStatusCode(HttpURLConnection.HTTP_OK);
        } else {
            response.setStatusCode(HttpURLConnection.HTTP_ACCEPTED);
        }
    }
    
    public void destroy() {
    }
        
    public static class Axis2TransportInfo implements OutTransportInfo {
        private Response response;

        public Axis2TransportInfo(Response response) {
            this.response = response;
        }

        public void setContentType(String contentType) {
            response.setHeader(HTTPConstants.HEADER_CONTENT_TYPE, contentType);
        }
    }
    
    protected void processGETRequest(Request request, Response response, AxisService service, MessageContext msgContext) throws Exception{
        String query = request.getURI().getQuery();
        if (query != null &&
            (query.startsWith("wsdl") || query.startsWith("WSDL") ||
             query.startsWith("xsd") || query.startsWith("XSD"))) {
            // wsdl or xsd request

            if (portInfo.getWsdlFile() != null && !portInfo.getWsdlFile().equals("")) { 
                URL wsdlURL = AxisServiceGenerator.getWsdlURL(portInfo.getWsdlFile(),
                                                              configurationBaseUrl, 
                                                              classLoader);
                this.wsdlQueryHandler.writeResponse(request.getURI().toString(), 
                                                    wsdlURL.toString(), 
                                                    response.getOutputStream());
            } else {
                throw new Exception("Service does not have WSDL");
            }
        } else if (AxisServiceGenerator.isSOAP11(service)) {
            response.setContentType("text/html");
            PrintWriter pw = new PrintWriter(response.getOutputStream());
            pw.write("<html><title>Web Service</title><body>");
            pw.write("Hi, this is '" + service.getName() + "' web service.");
            pw.write("</body></html>");
            pw.flush();
        } else {            
            // REST request
            processURLRequest(request, response, service, msgContext);            
        }
    }

    protected void processPOSTRequest(Request request, Response response, AxisService service, MessageContext msgContext) throws Exception {
        processXMLRequest(request, response, service, msgContext);
    }
    
    protected void setMsgContextProperties(Request request, Response response, AxisService service, MessageContext msgContext) {
        msgContext.setProperty(MessageContext.TRANSPORT_OUT, response.getOutputStream());
        msgContext.setProperty(Constants.OUT_TRANSPORT_INFO, new Axis2TransportInfo(response));
        msgContext.setProperty(RequestResponseTransport.TRANSPORT_CONTROL,
                new Axis2RequestResponseTransport(response));
        msgContext.setProperty(Constants.Configuration.TRANSPORT_IN_URL, request.getURI().toString());
        msgContext.setIncomingTransportName(Constants.TRANSPORT_HTTP);

        HttpServletRequest servletRequest =
            (HttpServletRequest)request.getAttribute(WebServiceContainer.SERVLET_REQUEST);
        msgContext.setProperty(HTTPConstants.MC_HTTP_SERVLETREQUEST, servletRequest);

        HttpServletResponse servletResponse =
            (HttpServletResponse)request.getAttribute(WebServiceContainer.SERVLET_RESPONSE);
        msgContext.setProperty(HTTPConstants.MC_HTTP_SERVLETRESPONSE, servletResponse);

        ServletContext servletContext =
            (ServletContext)request.getAttribute(WebServiceContainer.SERVLET_CONTEXT);
        msgContext.setProperty(HTTPConstants.MC_HTTP_SERVLETCONTEXT, servletContext);    
        
        if (servletRequest != null) {
            msgContext.setProperty(MessageContext.TRANSPORT_HEADERS, 
                                   new TransportHeaders(servletRequest));
        }
        
        if (this.binding != null) {
            msgContext.setProperty(JAXWSMessageReceiver.PARAM_BINDING, this.binding);  
        }
        
        msgContext.setTo(new EndpointReference(request.getURI().toString()));
    }
    
    protected void processXMLRequest(Request request, 
                                     Response response, 
                                     AxisService service, 
                                     MessageContext msgContext) throws Exception {
        String contentType = request.getHeader(HTTPConstants.HEADER_CONTENT_TYPE);
        String soapAction = request.getHeader(HTTPConstants.HEADER_SOAP_ACTION);
        if (soapAction == null) {
            soapAction = "\"\"";
        }
        
        ConfigurationContext configurationContext = msgContext.getConfigurationContext();
        configurationContext.fillServiceContextAndServiceGroupContext(msgContext);

        setMsgContextProperties(request, response, service, msgContext);

        if (!HTTPTransportUtils.isRESTRequest(contentType)) {
            HTTPTransportUtils.processHTTPPostRequest(msgContext,
                                                      request.getInputStream(), 
                                                      response.getOutputStream(), 
                                                      contentType, 
                                                      soapAction, 
                                                      request.getURI().getPath());
        } else {
            RESTUtil.processXMLRequest(msgContext, 
                                       request.getInputStream(),
                                       response.getOutputStream(), 
                                       contentType);
        }
    }
    
    protected void processURLRequest(Request request,
                                     Response response,
                                     AxisService service,
                                     MessageContext msgContext) throws Exception {
        String contentType = request.getHeader(HTTPConstants.HEADER_CONTENT_TYPE);
        
        ConfigurationContext configurationContext = msgContext.getConfigurationContext();
        configurationContext.fillServiceContextAndServiceGroupContext(msgContext);
        
        setMsgContextProperties(request, response, service, msgContext);
                
        InvocationResponse processed = RESTUtil.processURLRequest(msgContext, 
                                                                  response.getOutputStream(),
                                                                  contentType);

        if (!processed.equals(InvocationResponse.CONTINUE)) {
            response.setStatusCode(HttpURLConnection.HTTP_OK);
            String s = HTTPTransportReceiver.getServicesHTML(configurationContext);
            PrintWriter pw = new PrintWriter(response.getOutputStream());
            pw.write(s);
            pw.flush();
        }
    }    
    
    /*
     * Gets the right handlers for the port/service/bindings and performs injection.
     */
    protected void configureHandlers() throws Exception {
        EndpointDescription desc = AxisServiceGenerator.getEndpointDescription(this.service);
        if (desc == null) {
            throw new RuntimeException("No EndpointDescription for service");
        }
        
        String xml = this.portInfo.getHandlersAsXML();
        HandlerChainsType handlerChains = null;
        if (xml != null) {
            ByteArrayInputStream in = new ByteArrayInputStream(xml.getBytes("UTF-8"));
            handlerChains = DescriptionUtils.loadHandlerChains(in, null);
            desc.setHandlerChain(handlerChains);
        }
            
        if (LOG.isDebugEnabled()) {
            logHandlers(desc.getHandlerChain());
        }
            
        this.binding = BindingUtils.createBinding(desc);
        
        DescriptionUtils.registerHandlerHeaders(desc.getAxisService(), this.binding.getHandlerChain());            
    }

    private void logHandlers(HandlerChainsType handlerChains) {
        if (handlerChains == null || handlerChains.getHandlerChain() == null
            || handlerChains.getHandlerChain().isEmpty()) {
            LOG.debug("No handlers");
            return;
        }

        for (HandlerChainType chains : handlerChains.getHandlerChain()) {
            LOG.debug("Handler chain: " + chains.getServiceNamePattern() + " " + 
                      chains.getPortNamePattern() + " " + chains.getProtocolBindings());
            if (chains.getHandler() != null) {
                for (HandlerType chain : chains.getHandler()) {                    
                    LOG.debug("  Handler: " + chain.getHandlerName().getValue() + " " + 
                              chain.getHandlerClass().getValue());
                }
            }
        }
    }

    protected void injectHandlers() {
        List<Handler> handlers = this.binding.getHandlerChain();
        try {
            for (Handler handler : handlers) {
                injectResources(handler);
            }
        } catch (AnnotationException e) {
            throw new WebServiceException("Handler annotation failed", e);
        }
    }
    
    protected void destroyHandlers() {
        if (this.annotationProcessor != null) {
            // call handlers preDestroy
            List<Handler> handlers = this.binding.getHandlerChain();
            for (Handler handler : handlers) {
                this.annotationProcessor.invokePreDestroy(handler);
            }
        }
    }
    
    protected void injectResources(Object instance) throws AnnotationException {
        this.annotationProcessor.processAnnotations(instance);
        this.annotationProcessor.invokePostConstruct(instance);
    }
    
}
