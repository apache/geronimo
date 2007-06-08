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

import java.io.IOException;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.CountDownLatch;

import javax.naming.Context;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.axiom.om.util.UUIDGenerator;
import org.apache.axis2.AxisFault;
import org.apache.axis2.Constants;
import org.apache.axis2.addressing.AddressingHelper;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.ConfigurationContextFactory;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.context.OperationContext;
import org.apache.axis2.description.AxisService;
import org.apache.axis2.description.TransportInDescription;
import org.apache.axis2.description.TransportOutDescription;
import org.apache.axis2.engine.AxisEngine;
import org.apache.axis2.engine.Handler.InvocationResponse;
import org.apache.axis2.jaxws.server.JAXWSMessageReceiver;
import org.apache.axis2.transport.OutTransportInfo;
import org.apache.axis2.transport.RequestResponseTransport;
import org.apache.axis2.transport.http.HTTPConstants;
import org.apache.axis2.transport.http.HTTPTransportReceiver;
import org.apache.axis2.transport.http.HTTPTransportUtils;
import org.apache.axis2.transport.http.util.RESTUtil;
import org.apache.axis2.util.MessageContextBuilder;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.geronimo.jaxws.JAXWSUtils;
import org.apache.geronimo.jaxws.JNDIResolver;
import org.apache.geronimo.jaxws.PortInfo;
import org.apache.geronimo.jaxws.ServerJNDIResolver;
import org.apache.geronimo.webservices.WebServiceContainer;
import org.apache.geronimo.webservices.saaj.SAAJUniverse;

/**
 * @version $Rev$ $Date$
 */
public abstract class Axis2WebServiceContainer implements WebServiceContainer {

    private static final Log LOG = LogFactory.getLog(Axis2WebServiceContainer.class);

    public static final String REQUEST = Axis2WebServiceContainer.class.getName() + "@Request";
    public static final String RESPONSE = Axis2WebServiceContainer.class.getName() + "@Response";

    private transient final ClassLoader classLoader;
    private final String endpointClassName;
    protected org.apache.geronimo.jaxws.PortInfo portInfo;
    protected ConfigurationContext configurationContext;
    protected JNDIResolver jndiResolver;
    protected Class endpointClass;
    private AxisService service;
    private URL configurationBaseUrl;
    private WSDLQueryHandler wsdlQueryHandler;

    public Axis2WebServiceContainer(PortInfo portInfo,
                                    String endpointClassName,
                                    ClassLoader classLoader,
                                    Context context,
                                    URL configurationBaseUrl) {
        this.classLoader = classLoader;
        this.endpointClassName = endpointClassName;
        this.portInfo = portInfo;
        this.configurationBaseUrl = configurationBaseUrl;
        this.jndiResolver = new ServerJNDIResolver(context);
    }

    public void init() throws Exception {
        this.endpointClass = classLoader.loadClass(this.endpointClassName);
        configurationContext = ConfigurationContextFactory.createDefaultConfigurationContext();

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

            // FIXME: AxisServiceGenerator method should be used as it understands annotations
            // but right now that method causes some problems when WSDL is requested.
            service = AxisService.createService(endpointClassName, 
                                                configurationContext.getAxisConfiguration(), 
                                                JAXWSMessageReceiver.class);
            // service = serviceGen.getServiceFromClass(this.endpointClass);
        }

        service.setScope(Constants.SCOPE_APPLICATION);
        configurationContext.getAxisConfiguration().addService(service);

        this.wsdlQueryHandler = new WSDLQueryHandler(this.service);
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
        initContextRoot(request);

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
            

//            // set the transport Headers
//            HashMap headerMap = new HashMap();
//            for (Iterator it = request.headerIterator(); it.hasNext();) {
//                Header header = (Header) it.next();
//                headerMap.put(header.getName(), header.getValue());
//            }
//            msgContext.setProperty(MessageContext.TRANSPORT_HEADERS, headerMap);
//
//            this.httpcontext.setAttribute(AxisParams.MESSAGE_CONTEXT, msgContext);

            doService2(request, response, msgContext);
        } catch (Throwable e) {
            String msg = "Exception occurred while trying to invoke service method doService()";
            LOG.error(msg, e);
            try {
                AxisEngine engine = new AxisEngine(this.configurationContext);

                msgContext.setProperty(MessageContext.TRANSPORT_OUT, response.getOutputStream());
                msgContext.setProperty(Constants.OUT_TRANSPORT_INFO, new Axis2TransportInfo(response));

                MessageContext faultContext = MessageContextBuilder.createFaultMessageContext(msgContext, e);
                // If the fault is not going along the back channel we should be 202ing
                if (AddressingHelper.isFaultRedirected(msgContext)) {
                    response.setStatusCode(HttpURLConnection.HTTP_ACCEPTED);
                } else {
                    response.setStatusCode(HttpURLConnection.HTTP_INTERNAL_ERROR);
                }
                engine.sendFault(faultContext);
            } catch (Exception ex) {
                if (AddressingHelper.isFaultRedirected(msgContext)) {
                    response.setStatusCode(HttpURLConnection.HTTP_ACCEPTED);
                } else {
                    response.setStatusCode(HttpURLConnection.HTTP_INTERNAL_ERROR);
                    response.setHeader(HTTPConstants.HEADER_CONTENT_TYPE, "text/plain");
                    PrintWriter pw = new PrintWriter(response.getOutputStream());
                    ex.printStackTrace(pw);
                    pw.flush();
                    LOG.error(msg, ex);
                }
            }
        }

    }

    protected abstract void initContextRoot(Request request);

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
    
    static class Axis2RequestResponseTransport implements RequestResponseTransport {
        private Response response;

        private CountDownLatch responseReadySignal = new CountDownLatch(1);

        private RequestResponseTransportStatus status = RequestResponseTransportStatus.INITIAL;

        private AxisFault faultToBeThrownOut = null;

        Axis2RequestResponseTransport(Response response) {
            this.response = response;
        }

        public void acknowledgeMessage(MessageContext msgContext) throws AxisFault {
            LOG.debug("acknowledgeMessage");
            LOG.debug("Acking one-way request");

            response.setContentType("text/xml; charset="
                                    + msgContext.getProperty("message.character-set-encoding"));

            response.setStatusCode(HttpURLConnection.HTTP_ACCEPTED);
            try {
                response.flushBuffer();
            } catch (IOException e) {
                throw new AxisFault("Error sending acknowledgement", e);
            }

            signalResponseReady();
        }

        public void awaitResponse() throws InterruptedException, AxisFault {
            LOG.debug("Blocking servlet thread -- awaiting response");
            status = RequestResponseTransportStatus.WAITING;
            responseReadySignal.await();
            if (faultToBeThrownOut != null) {
                throw faultToBeThrownOut;
            }
        }

        public void signalFaultReady(AxisFault fault) {
            faultToBeThrownOut = fault;
            signalResponseReady();
        }

        public void signalResponseReady() {
            LOG.debug("Signalling response available");
            status = RequestResponseTransportStatus.SIGNALLED;
            responseReadySignal.countDown();
        }

        public RequestResponseTransportStatus getStatus() {
            return status;
        }
    }
    
    protected void processGETRequest(Request request, Response response, AxisService service, MessageContext msgContext) throws Exception{        
        if (request.getURI().getQuery() != null &&
            (request.getURI().getQuery().startsWith("wsdl") ||
             request.getURI().getQuery().startsWith("xsd"))) {
            // wsdl or xsd request
            
            if (portInfo.getWsdlFile() != null && !portInfo.getWsdlFile().equals("")) { 
                URL wsdlURL = AxisServiceGenerator.getWsdlURL(portInfo.getWsdlFile(),
                                                              configurationBaseUrl, 
                                                              classLoader);
                this.wsdlQueryHandler.writeResponse(request.getURI().toString(), 
                                                    wsdlURL.toString(), 
                                                    response.getOutputStream());
            } else {
                service.printWSDL(response.getOutputStream());
            }
        } else {
            // REST request
            
            msgContext.setProperty(MessageContext.TRANSPORT_OUT, response.getOutputStream());
            msgContext.setProperty(Constants.OUT_TRANSPORT_INFO, new Axis2TransportInfo(response));

            InvocationResponse processed = RESTUtil.processURLRequest(msgContext, 
                                                                      response.getOutputStream(),
                                                                      null);

            if (!processed.equals(InvocationResponse.CONTINUE)) {
                response.setStatusCode(HttpURLConnection.HTTP_OK);
                String s = HTTPTransportReceiver.getServicesHTML(configurationContext);
                PrintWriter pw = new PrintWriter(response.getOutputStream());
                pw.write(s);
                pw.flush();
            }
        }
    }
    
    protected void setMsgContextProperties(MessageContext msgContext, AxisService service, Response response, Request request) {
        //BindingImpl binding = new BindingImpl("GeronimoBinding");
        //binding.setHandlerChain(chain);
        //msgContext.setProperty(JAXWSMessageReceiver.PARAM_BINDING, binding);
        // deal with POST request
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
    }

    protected void processPOSTRequest (Request request, Response response, AxisService service, MessageContext msgContext) throws Exception {
        String contentType = request.getHeader(HTTPConstants.HEADER_CONTENT_TYPE);
        String soapAction = request.getHeader(HTTPConstants.HEADER_SOAP_ACTION);
        if (soapAction == null) {
            soapAction = "\"\"";
        }
        
        ConfigurationContext configurationContext = msgContext.getConfigurationContext();
        configurationContext.fillServiceContextAndServiceGroupContext(msgContext);

        setMsgContextProperties(msgContext, service, response, request);

        HTTPTransportUtils.processHTTPPostRequest(msgContext,
                                                  request.getInputStream(), 
                                                  response.getOutputStream(), 
                                                  contentType, 
                                                  soapAction, 
                                                  request.getURI().getPath());
    }

}
