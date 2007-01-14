/**
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.apache.geronimo.axis2;

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
import org.apache.axis2.rpc.receivers.RPCMessageReceiver;
import org.apache.axis2.transport.http.HTTPConstants;
import org.apache.axis2.transport.http.HTTPTransportReceiver;
import org.apache.axis2.transport.http.HTTPTransportUtils;
import org.apache.axis2.transport.http.server.HttpUtils;
import org.apache.axis2.transport.OutTransportInfo;
import org.apache.axis2.util.UUIDGenerator;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.geronimo.webservices.WebServiceContainer;
import org.apache.ws.commons.schema.XmlSchema;

import javax.xml.namespace.QName;
import java.io.PrintWriter;
import java.net.SocketException;
import java.util.HashMap;
import java.util.Map;


public class Axis2WebServiceContainer implements WebServiceContainer {

    private static final Log LOG = LogFactory.getLog(Axis2WebServiceContainer.class);

    public static final String REQUEST = Axis2WebServiceContainer.class.getName() + "@Request";
    public static final String RESPONSE = Axis2WebServiceContainer.class.getName() + "@Response";

    private transient final ClassLoader classLoader;
    private final String endpointClassName;
    private final PortInfo portInfo;
    ConfigurationContext configurationContext = ConfigurationContextFactory.createEmptyConfigurationContext();

    public Axis2WebServiceContainer(PortInfo portInfo, String endpointClassName, ClassLoader classLoader) {
        this.classLoader = classLoader;
        this.endpointClassName = endpointClassName;
        this.portInfo = portInfo;
        try {
            AxisService service = AxisService.createService(endpointClassName, configurationContext.getAxisConfiguration(), RPCMessageReceiver.class);
            configurationContext.getAxisConfiguration().addService(service);
        } catch (AxisFault af) {
            throw new RuntimeException(af);
        }
    }

    public void getWsdl(Request request, Response response) throws Exception {
        doService(request, response);
    }

    public void invoke(Request request, Response response) throws Exception {
        doService(request, response);
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
                    .getTransportOut(new QName(Constants.TRANSPORT_HTTP));
            TransportInDescription transportIn = this.configurationContext.getAxisConfiguration()
                    .getTransportIn(new QName(Constants.TRANSPORT_HTTP));

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
            try {
                AxisEngine engine = new AxisEngine(this.configurationContext);

                msgContext.setProperty(MessageContext.TRANSPORT_OUT, response.getOutputStream());
                msgContext.setProperty(Constants.OUT_TRANSPORT_INFO, new Axis2TransportInfo(response));

                MessageContext faultContext = engine.createFaultMessageContext(msgContext, e);
                // If the fault is not going along the back channel we should be 202ing
                if (AddressingHelper.isFaultRedirected(msgContext)) {
                    response.setStatusCode(202);
                } else {
                    response.setStatusCode(500);
                }
                engine.sendFault(faultContext);
            } catch (Exception ex) {
                if (AddressingHelper.isFaultRedirected(msgContext)) {
                    response.setStatusCode(202);
                } else {
                    response.setStatusCode(500);
                    String msg = ex.getMessage();
                    if (msg == null || msg.trim().length() == 0) {
                        msg = "Exception message unknown";
                    }
                    response.setHeader(HTTPConstants.HEADER_CONTENT_TYPE, "text/plain");
                    PrintWriter pw = new PrintWriter(response.getOutputStream());
                    pw.write(msg);
                    pw.flush();
                }
            }
        }

    }

    public void doService2(
            final Request request,
            final Response response,
            final MessageContext msgContext) throws Exception {

        ConfigurationContext configurationContext = msgContext.getConfigurationContext();
        final String servicePath = configurationContext.getServiceContextPath();
        final String contextPath = (servicePath.startsWith("/") ? servicePath : "/" + servicePath) + "/";

        String uri = request.getURI().toString();
        String soapAction = request.getHeader(HTTPConstants.HEADER_SOAP_ACTION);

        // TODO: Port this section
//        // Adjust version and content chunking based on the config
//        boolean chunked = false;
//        TransportOutDescription transportOut = msgContext.getTransportOut();
//        if (transportOut != null) {
//            Parameter p = transportOut.getParameter(HTTPConstants.PROTOCOL_VERSION);
//            if (p != null) {
//                if (HTTPConstants.HEADER_PROTOCOL_10.equals(p.getValue())) {
//                    ver = HttpVersion.HTTP_1_0;
//                }
//            }
//            if (ver.greaterEquals(HttpVersion.HTTP_1_1)) {
//                p = transportOut.getParameter(HTTPConstants.HEADER_TRANSFER_ENCODING);
//                if (p != null) {
//                    if (HTTPConstants.HEADER_TRANSFER_ENCODING_CHUNKED.equals(p.getValue())) {
//                        chunked = true;
//                    }
//                }
//            }
//        }

        if (request.getMethod() == Request.GET) {
            if (!uri.startsWith(contextPath)) {
                response.setStatusCode(301);
                response.setHeader("Location", contextPath);
                return;
            }
            if (uri.indexOf("?") < 0) {
                if (!uri.endsWith(contextPath)) {
                    String serviceName = uri.replaceAll(contextPath, "");
                    if (serviceName.indexOf("/") < 0) {
                        String res = HTTPTransportReceiver.printServiceHTML(serviceName, configurationContext);
                        PrintWriter pw = new PrintWriter(response.getOutputStream());
                        pw.write(res);
                        return;
                    }
                }
            }
            if (uri.endsWith("?wsdl2")) {
                String serviceName = uri.substring(uri.lastIndexOf("/") + 1, uri.length() - 6);
                HashMap services = configurationContext.getAxisConfiguration().getServices();
                final AxisService service = (AxisService) services.get(serviceName);
                if (service != null) {
                    final String ip = HttpUtils.getIpAddress();
                    service.printWSDL2(response.getOutputStream(), ip, servicePath);
                    return;
                }
            }
            if (uri.endsWith("?wsdl")) {
                String serviceName = uri.substring(uri.lastIndexOf("/") + 1, uri.length() - 5);
                HashMap services = configurationContext.getAxisConfiguration().getServices();
                final AxisService service = (AxisService) services.get(serviceName);
                if (service != null) {
                    final String ip = HttpUtils.getIpAddress();
                    service.printWSDL(response.getOutputStream(), ip, servicePath);
                    return;
                }
            }
            if (uri.endsWith("?xsd")) {
                String serviceName = uri.substring(uri.lastIndexOf("/") + 1, uri.length() - 4);
                HashMap services = configurationContext.getAxisConfiguration().getServices();
                final AxisService service = (AxisService) services.get(serviceName);
                if (service != null) {
                    service.printSchema(response.getOutputStream());
                    return;
                }
            }
            //cater for named xsds - check for the xsd name
            if (uri.indexOf("?xsd=") > 0) {
                String serviceName = uri.substring(uri.lastIndexOf("/") + 1, uri.lastIndexOf("?xsd="));
                String schemaName = uri.substring(uri.lastIndexOf("=") + 1);

                HashMap services = configurationContext.getAxisConfiguration().getServices();
                AxisService service = (AxisService) services.get(serviceName);
                if (service != null) {
                    //run the population logic just to be sure
                    service.populateSchemaMappings();
                    //write out the correct schema
                    Map schemaTable = service.getSchemaMappingTable();
                    final XmlSchema schema = (XmlSchema) schemaTable.get(schemaName);
                    //schema found - write it to the stream
                    if (schema != null) {
                        schema.write(response.getOutputStream());
                        return;
                    } else {
                        // no schema available by that name  - send 404
                        response.setStatusCode(404);
                        return;
                    }
                }
            }

            msgContext.setProperty(MessageContext.TRANSPORT_OUT, response.getOutputStream());
            msgContext.setProperty(Constants.OUT_TRANSPORT_INFO, new Axis2TransportInfo(response));

            // deal with GET request
            boolean processed = HTTPTransportUtils.processHTTPGetRequest(
                    msgContext,
                    response.getOutputStream(),
                    soapAction,
                    uri,
                    configurationContext,
                    HTTPTransportReceiver.getGetRequestParameters(uri));

            if (!processed) {
                response.setStatusCode(200);
                String s = HTTPTransportReceiver.getServicesHTML(configurationContext);
                PrintWriter pw = new PrintWriter(response.getOutputStream());
                pw.write(s);
                pw.flush();
            }

        } else if (request.getMethod() == Request.POST) {
            // deal with POST request

            msgContext.setProperty(MessageContext.TRANSPORT_OUT, response.getOutputStream());
            msgContext.setProperty(Constants.OUT_TRANSPORT_INFO, response.getOutputStream());

            String contenttype = request.getHeader(HTTPConstants.HEADER_CONTENT_TYPE);
            HTTPTransportUtils.processHTTPPostRequest(
                    msgContext,
                    request.getInputStream(),
                    response.getOutputStream(),
                    contenttype,
                    soapAction,
                    uri);

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
                response.setStatusCode(202);
                return;
            }
            response.setStatusCode(202);
        } else {
            response.setStatusCode(202);
        }
    }

    public class Axis2TransportInfo implements OutTransportInfo {
        private Response response;

        public Axis2TransportInfo(Response response) {
            this.response = response;
        }

        public void setContentType(String contentType) {
            response.setHeader(HTTPConstants.HEADER_CONTENT_TYPE, contentType);
        }
    }

}
