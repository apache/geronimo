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

import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.naming.Context;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.namespace.QName;
import javax.xml.ws.Binding;
import javax.xml.ws.WebServiceException;
import javax.xml.ws.handler.Handler;

import org.apache.axiom.soap.SOAPFault;
import org.apache.axiom.util.UIDGenerator;
import org.apache.axis2.AxisFault;
import org.apache.axis2.Constants;
import org.apache.axis2.addressing.AddressingHelper;
import org.apache.axis2.addressing.EndpointReference;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.ConfigurationContextFactory;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.context.OperationContext;
import org.apache.axis2.description.AxisService;
import org.apache.axis2.description.Parameter;
import org.apache.axis2.description.TransportInDescription;
import org.apache.axis2.description.TransportOutDescription;
import org.apache.axis2.engine.AxisEngine;
import org.apache.axis2.engine.Handler.InvocationResponse;
import org.apache.axis2.jaxws.addressing.util.EndpointContextMap;
import org.apache.axis2.jaxws.addressing.util.EndpointContextMapManager;
import org.apache.axis2.jaxws.addressing.util.EndpointKey;
import org.apache.axis2.jaxws.binding.BindingUtils;
import org.apache.axis2.jaxws.description.EndpointDescription;
import org.apache.axis2.jaxws.description.impl.DescriptionUtils;
import org.apache.axis2.jaxws.description.xml.handler.FullyQualifiedClassType;
import org.apache.axis2.jaxws.description.xml.handler.HandlerChainType;
import org.apache.axis2.jaxws.description.xml.handler.HandlerChainsType;
import org.apache.axis2.jaxws.description.xml.handler.HandlerType;
import org.apache.axis2.jaxws.handler.lifecycle.factory.HandlerLifecycleManagerFactory;
import org.apache.axis2.jaxws.registry.FactoryRegistry;
import org.apache.axis2.jaxws.server.JAXWSMessageReceiver;
import org.apache.axis2.jaxws.server.endpoint.lifecycle.factory.EndpointLifecycleManagerFactory;
import org.apache.axis2.transport.OutTransportInfo;
import org.apache.axis2.transport.RequestResponseTransport;
import org.apache.axis2.transport.TransportUtils;
import org.apache.axis2.transport.http.HTTPConstants;
import org.apache.axis2.transport.http.HTTPTransportReceiver;
import org.apache.axis2.transport.http.HTTPTransportUtils;
import org.apache.axis2.transport.http.TransportHeaders;
import org.apache.axis2.transport.http.util.RESTUtil;
import org.apache.axis2.util.MessageContextBuilder;
import org.apache.geronimo.axis2.client.Axis2ConfigGBean;
import org.apache.geronimo.axis2.osgi.Axis2ModuleRegistry;
import org.apache.geronimo.jaxws.JAXWSAnnotationProcessor;
import org.apache.geronimo.jaxws.JAXWSUtils;
import org.apache.geronimo.jaxws.JAXWSWebApplicationContext;
import org.apache.geronimo.jaxws.JNDIResolver;
import org.apache.geronimo.jaxws.PortInfo;
import org.apache.geronimo.jaxws.ServerJNDIResolver;
import org.apache.geronimo.jaxws.annotations.AnnotationException;
import org.apache.geronimo.jaxws.info.HandlerChainInfo;
import org.apache.geronimo.jaxws.info.HandlerChainsInfo;
import org.apache.geronimo.jaxws.info.HandlerInfo;
import org.apache.geronimo.webservices.WebServiceContainer;
import org.apache.geronimo.webservices.saaj.SAAJUniverse;
import org.apache.xbean.osgi.bundle.util.BundleClassLoader;
import org.osgi.framework.Bundle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @version $Rev$ $Date$
 */
public abstract class Axis2WebServiceContainer implements WebServiceContainer {

    private static final Logger LOG = LoggerFactory.getLogger(Axis2WebServiceContainer.class);

    public static final String REQUEST = Axis2WebServiceContainer.class.getName() + "@Request";

    public static final String RESPONSE = Axis2WebServiceContainer.class.getName() + "@Response";

    private static final boolean SOAP_1_1_FAULT_DETAIL_COMPATIBLE_WHEN_ADDRESSING_FAULTS = Boolean.getBoolean("org.apache.geronimo.axis2.soap_1_1FaultDetailCompatibleWhenAddressingFaults");

    private transient final Bundle bundle;

    protected String endpointClassName;

    protected org.apache.geronimo.jaxws.PortInfo portInfo;

    protected ConfigurationContext configurationContext;

    protected JNDIResolver jndiResolver;

    protected Class<?> endpointClass;

    protected AxisService service;

    protected WSDLQueryHandler wsdlQueryHandler;

    protected Binding binding;

    protected JAXWSAnnotationProcessor annotationProcessor;

    protected Context context;

    protected String address;

    protected GeronimoFactoryRegistry factoryRegistry;

    protected Axis2ModuleRegistry axis2ModuleRegistry;

    protected String moduleName;
    
    protected String catalogName;

    public Axis2WebServiceContainer(PortInfo portInfo, String endpointClassName, Bundle bundle, Context context, Axis2ModuleRegistry axis2ModuleRegistry, String moduleName, String catalogName) {
        this.endpointClassName = endpointClassName;
        this.portInfo = portInfo;
        this.bundle = bundle;
        this.context = context;
        this.jndiResolver = new ServerJNDIResolver(context);
        this.axis2ModuleRegistry = axis2ModuleRegistry;
        this.moduleName = moduleName;
        this.catalogName = catalogName;
    }

    public void init() throws Exception {
        this.endpointClass = bundle.loadClass(this.endpointClassName);

        Axis2ConfigGBean.registerClientConfigurationFactory(axis2ModuleRegistry);

        GeronimoConfigurator configurator = new GeronimoConfigurator("META-INF/geronimo-axis2.xml");
        configurationContext = ConfigurationContextFactory.createConfigurationContext(configurator);

        axis2ModuleRegistry.configureModules(configurationContext);
        // check to see if the wsdlLocation property is set in portInfo,
        // if not checking if wsdlLocation exists in annotation
        // if already set, annotation should not overwrite it.
        if (portInfo.getWsdlFile() == null || portInfo.getWsdlFile().equals("")) {
            // getwsdllocation from annotation if it exists
            if (JAXWSUtils.containsWsdlLocation(this.endpointClass, bundle)) {
                portInfo.setWsdlFile(JAXWSUtils.getServiceWsdlLocation(this.endpointClass, bundle));
            }
        }

        AxisServiceGenerator serviceGen = createServiceGenerator();
        serviceGen.setConfigurationContext(configurationContext);
        if (portInfo.getWsdlFile() != null && !portInfo.getWsdlFile().equals("")) {
            // WSDL file has been provided
            service = serviceGen.getServiceFromWSDL(portInfo, endpointClass, bundle);
        } else {
            // No WSDL, let Axis2 handle it.
            service = serviceGen.getServiceFromClass(this.endpointClass, portInfo);
        }

        service.setScope(Constants.SCOPE_APPLICATION);
        configurationContext.getAxisConfiguration().addService(service);

        this.wsdlQueryHandler = new WSDLQueryHandler(this.service, portInfo, getPortInfos(bundle));

        /*
         * This replaces HandlerLifecycleManagerFactory for all web services.
         * This should be ok as we do our own handler instance managment and injection.
         * Also, this does not affect service-ref clients, as we install our own
         * HandlerResolver.
         */
        FactoryRegistry.setFactory(HandlerLifecycleManagerFactory.class, new GeronimoHandlerLifecycleManagerFactory());

        FactoryRegistry.setFactory(EndpointLifecycleManagerFactory.class, new GeronimoEndpointLifecycleManagerFactory());

        configureAddressing();

        this.service.addParameter(new Parameter(org.apache.axis2.jaxws.spi.Constants.CACHE_CLASSLOADER, new BundleClassLoader(bundle)));
    }

    protected Collection<PortInfo> getPortInfos(Bundle bundle) {
        JAXWSWebApplicationContext jaxwsWebApplicationContext = JAXWSWebApplicationContext.get(moduleName);
        return jaxwsWebApplicationContext == null ? Collections.<PortInfo>emptyList() : jaxwsWebApplicationContext.getPortInfos();
    }

    static String getBaseUri(URI request) {
        return request.getScheme() + "://" + request.getHost() + ":" + request.getPort() + request.getPath();
    }

    synchronized void updateAddress(Request request) {
        if (this.address == null) {
            String requestAddress = getBaseUri(request.getURI());
            this.service.setEPRs(new String[] { requestAddress });
            this.address = requestAddress;
        }
    }

    protected AxisServiceGenerator createServiceGenerator() {
        return new AxisServiceGenerator();
    }

    public void getWsdl(Request request, Response response) throws Exception {
        doService(request, response);
    }

    public void invoke(Request request, Response response) throws Exception {
        // set factory registry
        GeronimoFactoryRegistry oldRegistry = GeronimoFactoryRegistry.getGeronimoFactoryRegistry();
        GeronimoFactoryRegistry.setGeronimoFactoryRegistry(this.factoryRegistry);
        // set saaj universe
        SAAJUniverse universe = new SAAJUniverse();
        universe.set(SAAJUniverse.AXIS2);
        try {
            doService(request, response);
        } finally {
            // unset saaj universe
            universe.unset();
            // unset factory registry
            GeronimoFactoryRegistry.setGeronimoFactoryRegistry(oldRegistry);
        }
    }

    protected void doService(final Request request, final Response response) throws Exception {

        if (LOG.isDebugEnabled()) {
            LOG.debug("Target URI: " + request.getURI());
        }

        updateAddress(request);

        MessageContext msgContext = new MessageContext();
        msgContext.setIncomingTransportName(Constants.TRANSPORT_HTTP);
        msgContext.setProperty(MessageContext.REMOTE_ADDR, request.getRemoteAddr());

        try {
            TransportOutDescription transportOut = this.configurationContext.getAxisConfiguration().getTransportOut(Constants.TRANSPORT_HTTP);
            TransportInDescription transportIn = this.configurationContext.getAxisConfiguration().getTransportIn(Constants.TRANSPORT_HTTP);

            msgContext.setConfigurationContext(this.configurationContext);

            //TODO: Port this segment for session support.
            //            String sessionKey = (String) this.httpcontext.getAttribute(HTTPConstants.COOKIE_STRING);
            //            if (this.configurationContext.getAxisConfiguration().isManageTransportSession()) {
            //                SessionContext sessionContext = this.sessionManager.getSessionContext(sessionKey);
            //                msgContext.setSessionContext(sessionContext);
            //            }
            msgContext.setTransportIn(transportIn);
            msgContext.setTransportOut(transportOut);
            msgContext.setServiceGroupContextId(UIDGenerator.generateURNString());
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
            //Per SOAP 1.1 Fault description, if it is not allowed to use detail elements for carrying information about error information belonging to header entries.
            if (SOAP_1_1_FAULT_DETAIL_COMPATIBLE_WHEN_ADDRESSING_FAULTS && faultContext.isSOAP11()) {
                Map faultInfo = (Map) faultContext.getLocalProperty(Constants.FAULT_INFORMATION_FOR_HEADERS);
                if (faultInfo != null) {
                    SOAPFault soapFault = faultContext.getEnvelope().getBody().getFault();
                    if (soapFault != null && soapFault.getDetail() != null) {
                        soapFault.getDetail().detach();
                    }
                }
            }
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

    public void doService2(Request request, Response response, MessageContext msgContext) throws Exception {

        if (request.getMethod() == Request.GET) {
            processGETRequest(request, response, this.service, msgContext);
        } else if (request.getMethod() == Request.POST) {
            processPOSTRequest(request, response, this.service, msgContext);
        } else {
            throw new UnsupportedOperationException("[" + request.getMethod() + " ] method not supported");
        }

        // Finalize response
        if (TransportUtils.isResponseWritten(msgContext)) {
            OperationContext operationContext = msgContext.getOperationContext();
            Object isTwoChannel = null;
            if (operationContext != null) {
                isTwoChannel = operationContext.getProperty(Constants.DIFFERENT_EPR);
            }
            if ((isTwoChannel != null) && Constants.VALUE_TRUE.equals(isTwoChannel)) {
                response.setStatusCode(HttpURLConnection.HTTP_ACCEPTED);
                return;
            }
        } else {
            RequestResponseTransport requestResponseTransport = (RequestResponseTransport) msgContext.getProperty(RequestResponseTransport.TRANSPORT_CONTROL);
            if (requestResponseTransport != null && requestResponseTransport.getStatus() != RequestResponseTransport.RequestResponseTransportStatus.SIGNALLED) {
                response.setStatusCode(HttpURLConnection.HTTP_ACCEPTED);
                return;
            }
        }
        response.setStatusCode(HttpURLConnection.HTTP_OK);
    }

    public void destroy() {
        if (this.configurationContext != null) {
            try {
                this.configurationContext.terminate();
            } catch (AxisFault e) {
                LOG.debug(e.getMessage(), e);
            }
        }
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

    protected void processGETRequest(Request request, Response response, AxisService service, MessageContext msgContext) throws Exception {
        String query = request.getURI().getQuery();
        if (query != null && (query.startsWith("wsdl") || query.startsWith("WSDL") || query.startsWith("xsd") || query.startsWith("XSD"))) {
            // wsdl or xsd request

            if (portInfo.getWsdlFile() != null && !portInfo.getWsdlFile().equals("")) {
                URL wsdlURL = JAXWSUtils.getWsdlURL(bundle, portInfo.getWsdlFile());
                this.wsdlQueryHandler.writeResponse(request.getURI().toString(), wsdlURL.toString(), response.getOutputStream());
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
        msgContext.setProperty(RequestResponseTransport.TRANSPORT_CONTROL, new Axis2RequestResponseTransport(response));
        msgContext.setProperty(Constants.Configuration.TRANSPORT_IN_URL, request.getURI().toString());
        msgContext.setIncomingTransportName(Constants.TRANSPORT_HTTP);

        HttpServletRequest servletRequest = (HttpServletRequest) request.getAttribute(WebServiceContainer.SERVLET_REQUEST);
        msgContext.setProperty(HTTPConstants.MC_HTTP_SERVLETREQUEST, servletRequest);

        HttpServletResponse servletResponse = (HttpServletResponse) request.getAttribute(WebServiceContainer.SERVLET_RESPONSE);
        msgContext.setProperty(HTTPConstants.MC_HTTP_SERVLETRESPONSE, servletResponse);

        ServletContext servletContext = (ServletContext) request.getAttribute(WebServiceContainer.SERVLET_CONTEXT);
        msgContext.setProperty(HTTPConstants.MC_HTTP_SERVLETCONTEXT, servletContext);

        if (servletRequest != null) {
            msgContext.setProperty(MessageContext.TRANSPORT_HEADERS, new TransportHeaders(servletRequest));
        }

        if (this.binding != null) {
            msgContext.setProperty(JAXWSMessageReceiver.PARAM_BINDING, this.binding);
        }

        msgContext.setTo(new EndpointReference(request.getURI().toString()));
    }

    protected void processXMLRequest(Request request, Response response, AxisService service, MessageContext msgContext) throws Exception {
        String contentType = request.getHeader(HTTPConstants.HEADER_CONTENT_TYPE);
        String soapAction = request.getHeader(HTTPConstants.HEADER_SOAP_ACTION);
        if (soapAction == null) {
            soapAction = "\"\"";
        }

        ConfigurationContext configurationContext = msgContext.getConfigurationContext();
        configurationContext.fillServiceContextAndServiceGroupContext(msgContext);

        setMsgContextProperties(request, response, service, msgContext);

        if (!HTTPTransportUtils.isRESTRequest(contentType)) {
            HTTPTransportUtils.processHTTPPostRequest(msgContext, request.getInputStream(), response.getOutputStream(), contentType, soapAction, request.getURI().getPath());
        } else {
            RESTUtil.processXMLRequest(msgContext, request.getInputStream(), response.getOutputStream(), contentType);
        }
    }

    protected void processURLRequest(Request request, Response response, AxisService service, MessageContext msgContext) throws Exception {
        String contentType = request.getHeader(HTTPConstants.HEADER_CONTENT_TYPE);

        ConfigurationContext configurationContext = msgContext.getConfigurationContext();
        configurationContext.fillServiceContextAndServiceGroupContext(msgContext);

        setMsgContextProperties(request, response, service, msgContext);

        InvocationResponse processed = RESTUtil.processURLRequest(msgContext, response.getOutputStream(), contentType);

        if (!processed.equals(InvocationResponse.CONTINUE)) {
            response.setStatusCode(HttpURLConnection.HTTP_OK);
            String s = HTTPTransportReceiver.getServicesHTML(configurationContext);
            PrintWriter pw = new PrintWriter(response.getOutputStream());
            pw.write(s);
            pw.flush();
        }
    }

    protected void configureAddressing() throws Exception {
        EndpointDescription desc = AxisServiceGenerator.getEndpointDescription(this.service);

        QName serviceName = desc.getServiceQName();
        QName portName = desc.getPortQName();
        EndpointKey key = new EndpointKey(serviceName, portName);

        EndpointContextMapManager.setEndpointContextMap(null);
        EndpointContextMap map = EndpointContextMapManager.getEndpointContextMap();
        map.put(key, this.service);

        configurationContext.setProperty(org.apache.axis2.jaxws.Constants.ENDPOINT_CONTEXT_MAP, map);
    }

    /*
     * Gets the right handlers for the port/service/bindings and performs injection.
     */
    protected void configureHandlers() throws Exception {
        EndpointDescription desc = AxisServiceGenerator.getEndpointDescription(this.service);

        if(portInfo.getHandlerChainsInfo() != null) {
            desc.setHandlerChain(toAxis2HandlerChainsType(portInfo.getHandlerChainsInfo()));
        }

        if (LOG.isDebugEnabled()) {
            logHandlers(desc.getHandlerChain());
        }

        this.binding = BindingUtils.createBinding(desc);

        DescriptionUtils.registerHandlerHeaders(desc.getAxisService(), this.binding.getHandlerChain());
    }

    private HandlerChainsType toAxis2HandlerChainsType(HandlerChainsInfo handlerChainsInfo) {
        HandlerChainsType handlerChains = new HandlerChainsType();
        for (HandlerChainInfo handlerChainInfo : handlerChainsInfo.handleChains) {
            HandlerChainType handlerChain = new HandlerChainType();
            handlerChain.setPortNamePattern(handlerChainInfo.portNamePattern);
            handlerChain.setServiceNamePattern(handlerChainInfo.serviceNamePattern);
            handlerChain.getProtocolBindings().addAll(handlerChainInfo.protocolBindings);
            for (HandlerInfo handlerInfo : handlerChainInfo.handlers) {
                HandlerType handler = new HandlerType();
                FullyQualifiedClassType classType = new FullyQualifiedClassType();
                classType.setValue(handlerInfo.handlerClass);
                handler.setHandlerClass(classType);
                org.apache.axis2.jaxws.description.xml.handler.String nameType = new org.apache.axis2.jaxws.description.xml.handler.String();
                nameType.setValue(handlerInfo.handlerName);
                handler.setHandlerName(nameType);
                handlerChain.getHandler().add(handler);
            }
            handlerChains.getHandlerChain().add(handlerChain);
        }
        return handlerChains;
    }

    private void logHandlers(HandlerChainsType handlerChains) {
        if (handlerChains == null || handlerChains.getHandlerChain() == null || handlerChains.getHandlerChain().isEmpty()) {
            LOG.debug("No handlers");
            return;
        }

        for (HandlerChainType chains : handlerChains.getHandlerChain()) {
            LOG.debug("Handler chain: " + chains.getServiceNamePattern() + " " + chains.getPortNamePattern() + " " + chains.getProtocolBindings());
            if (chains.getHandler() != null) {
                for (HandlerType chain : chains.getHandler()) {
                    LOG.debug("  Handler: " + chain.getHandlerName().getValue() + " " + chain.getHandlerClass().getValue());
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
