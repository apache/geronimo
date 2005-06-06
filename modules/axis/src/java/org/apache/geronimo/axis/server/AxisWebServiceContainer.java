/**
 *
 * Copyright 2005 The Apache Software Foundation
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.apache.geronimo.axis.server;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.OutputStream;
import java.net.URI;
import java.util.Iterator;
import java.util.Map;
import javax.servlet.http.HttpServletResponse;
import javax.wsdl.Definition;
import javax.wsdl.OperationType;
import javax.wsdl.Port;
import javax.wsdl.Service;
import javax.wsdl.WSDLException;
import javax.wsdl.extensions.ExtensibilityElement;
import javax.wsdl.extensions.soap.SOAPAddress;
import javax.wsdl.factory.WSDLFactory;
import javax.wsdl.xml.WSDLWriter;
import javax.xml.soap.MimeHeader;
import javax.xml.soap.MimeHeaders;
import javax.xml.soap.SOAPMessage;

import org.apache.axis.AxisFault;
import org.apache.axis.Constants;
import org.apache.axis.Message;
import org.apache.axis.MessageContext;
import org.apache.axis.SOAPPart;
import org.apache.axis.handlers.soap.SOAPService;
import org.apache.axis.message.SOAPEnvelope;
import org.apache.axis.soap.SOAPConstants;
import org.apache.axis.transport.http.HTTPConstants;
import org.apache.axis.utils.Messages;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.geronimo.webservices.WebServiceContainer;
import org.w3c.dom.Element;

/**
 * @version $Rev$ $Date$
 */
public class AxisWebServiceContainer implements WebServiceContainer {
    public static final String REQUEST = AxisWebServiceContainer.class.getName() + "@Request";
    public static final String RESPONSE = AxisWebServiceContainer.class.getName() + "@Response";

    private static Log log = LogFactory.getLog(AxisWebServiceContainer.class);

    public static final String XSD_NS = "http://www.w3.org/2001/XMLSchema";

    private final URI location;
    private final URI wsdlLocation;
    private final SOAPService service;

    private final ClassLoader classLoader;
    private final Map wsdlMap;
    private transient WSDLWriter wsdlWriter;

    public AxisWebServiceContainer(URI location, URI wsdlURL, SOAPService service, Map wsdlMap, ClassLoader classLoader) throws WSDLException {
        this.location = location;
        this.wsdlLocation = wsdlURL;
        this.service = service;
        this.wsdlMap = wsdlMap;
        this.classLoader = classLoader;
        WSDLFactory wsdlFactory = WSDLFactory.newInstance();
        wsdlWriter = wsdlFactory.newWSDLWriter();
    }

    public void invoke(Request req, Response res) throws Exception {
        org.apache.axis.MessageContext messageContext = new org.apache.axis.MessageContext(null);
        req.setAttribute(MESSAGE_CONTEXT, messageContext);

        messageContext.setClassLoader(classLoader);

        Message responseMessage = null;

        String contentType = req.getHeader(HTTPConstants.HEADER_CONTENT_TYPE);
        String contentLocation = req.getHeader(HTTPConstants.HEADER_CONTENT_LOCATION);
        InputStream inputStream = req.getInputStream();
        Message requestMessage = new Message(inputStream, false, contentType, contentLocation);

        messageContext.setRequestMessage(requestMessage);
        messageContext.setProperty(HTTPConstants.MC_HTTP_SERVLETPATHINFO, req.getURI().getPath());
        messageContext.setProperty(org.apache.axis.MessageContext.TRANS_URL, req.getURI().toString());
        messageContext.setService(service);
        messageContext.setProperty(REQUEST, req);
        messageContext.setProperty(RESPONSE, res);

        ClassLoader oldClassLoader = Thread.currentThread().getContextClassLoader();
        try {
            try {
                String characterEncoding = (String) requestMessage.getProperty(SOAPMessage.CHARACTER_SET_ENCODING);
                if (characterEncoding != null) {
                    messageContext.setProperty(SOAPMessage.CHARACTER_SET_ENCODING, characterEncoding);
                } else {
                    messageContext.setProperty(SOAPMessage.CHARACTER_SET_ENCODING, "UTF-8");
                }


                String soapAction = req.getHeader(HTTPConstants.HEADER_SOAP_ACTION);
                if (soapAction != null) {
                    messageContext.setUseSOAPAction(true);
                    messageContext.setSOAPActionURI(soapAction);
                }

                SOAPEnvelope env = requestMessage.getSOAPEnvelope();
                if (env != null && env.getSOAPConstants() != null) {
                    messageContext.setSOAPConstants(env.getSOAPConstants());
                }
                SOAPService service = messageContext.getService();

                Thread.currentThread().setContextClassLoader(classLoader);
                service.invoke(messageContext);

                responseMessage = messageContext.getResponseMessage();
            } catch (AxisFault fault) {
                responseMessage = handleFault(fault, res, messageContext);

            } catch (Exception e) {
                responseMessage = handleException(messageContext, res, e);
            }
            //TODO investigate and fix operation == null!
            if (messageContext.getOperation() != null) {
                if (messageContext.getOperation().getMep() == OperationType.ONE_WAY) {
                    // No content, so just indicate accepted
                    res.setStatusCode(202);
                    return;
                } else if (responseMessage == null) {
                    responseMessage = handleException(messageContext, null, new RuntimeException("No response for non-one-way operation"));
                }
            } else if (responseMessage == null) {
                res.setStatusCode(202);
                return;
            }
            try {
                SOAPConstants soapConstants = messageContext.getSOAPConstants();
                String contentType1 = responseMessage.getContentType(soapConstants);
                res.setContentType(contentType1);
                    // Transfer MIME headers to HTTP headers for response message.
                    MimeHeaders responseMimeHeaders = responseMessage.getMimeHeaders();
                    for (Iterator i = responseMimeHeaders.getAllHeaders(); i.hasNext(); ) {
                        MimeHeader responseMimeHeader = (MimeHeader) i.next();
                        res.setHeader(responseMimeHeader.getName(),
                                      responseMimeHeader.getValue());
                    }
                //TODO discuss this with dims.
//                // synchronize the character encoding of request and response
//                String responseEncoding = (String) messageContext.getProperty(
//                        SOAPMessage.CHARACTER_SET_ENCODING);
//                if (responseEncoding != null) {
//                    try {
//                        responseMessage.setProperty(SOAPMessage.CHARACTER_SET_ENCODING,
//                                                responseEncoding);
//                    } catch (SOAPException e) {
//                        log.info(Messages.getMessage("exception00"), e);
//                    }
//                }
                    //determine content type from message response
                    contentType = responseMessage.getContentType(messageContext.
                            getSOAPConstants());
                    responseMessage.writeTo(res.getOutputStream());
            } catch (Exception e) {
                log.info(Messages.getMessage("exception00"), e);
            }
        } finally {
            Thread.currentThread().setContextClassLoader(oldClassLoader);
        }
    }

    private Message handleException(MessageContext context, Response res, Exception e) {
        Message responseMessage;
        //other exceptions are internal trouble
        responseMessage = context.getResponseMessage();
        res.setStatusCode(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        Message responseMsg = responseMessage;
        log.info(Messages.getMessage("exception00"), e);
        if (responseMsg == null) {
            AxisFault fault = AxisFault.makeFault(e);
            //log the fault
            Element runtimeException = fault.lookupFaultDetail(Constants.QNAME_FAULTDETAIL_RUNTIMEEXCEPTION);
            if (runtimeException != null) {
                log.info(Messages.getMessage("axisFault00"), fault);
                //strip runtime details
                fault.removeFaultDetail(Constants.QNAME_FAULTDETAIL_RUNTIMEEXCEPTION);
            }
            responseMsg = new Message(fault);
        }
        responseMessage = responseMsg;
        SOAPPart soapPart = (SOAPPart) responseMessage.getSOAPPart();
        soapPart.getMessage().setMessageContext(context);
        return responseMessage;
    }

    private Message handleFault(AxisFault fault, Response res, MessageContext context) {
        Message responseMessage;
        Element runtimeException = fault.lookupFaultDetail(Constants.QNAME_FAULTDETAIL_RUNTIMEEXCEPTION);

        if (runtimeException != null) {
            log.info(Messages.getMessage("axisFault00"), fault);
            //strip runtime details
            fault.removeFaultDetail(Constants.QNAME_FAULTDETAIL_RUNTIMEEXCEPTION);
        } else if (log.isDebugEnabled()) {
            log.debug(Messages.getMessage("axisFault00"), fault);
        }

        int status = fault.getFaultCode().getLocalPart().startsWith("Server.Unauth")
                ? HttpServletResponse.SC_UNAUTHORIZED
                : HttpServletResponse.SC_INTERNAL_SERVER_ERROR;
        if (status == HttpServletResponse.SC_UNAUTHORIZED) {
            // unauth access results in authentication request
            // TODO: less generic realm choice?
            res.setHeader("WWW-Authenticate", "Basic realm=\"AXIS\"");
        }
        res.setStatusCode(status);
        responseMessage = context.getResponseMessage();
        if (responseMessage == null) {
            responseMessage = new Message(fault);
            SOAPPart soapPart = (SOAPPart) responseMessage.getSOAPPart();
            soapPart.getMessage().setMessageContext(context);
        }
        return responseMessage;
    }

    public void getWsdl(Request request, Response response) throws Exception {
        URI realLocation = request.getURI();
        String query = realLocation.getQuery();
        if (query == null || !query.toLowerCase().startsWith("wsdl")) {
            throw new IllegalStateException("request must contain a  wsdl or WSDL parameter: " + request.getParameters());
        }
        String locationKey;
        if (query.length() > 4) {
            locationKey = query.substring(5);
        } else {
            locationKey = wsdlLocation.toString();
        }
        Object wsdl = wsdlMap.get(locationKey);
        if (wsdl == null) {
            throw new IllegalStateException("No wsdl or schema known at location: " + locationKey);
        }
        if (wsdl instanceof String) {
//            log.info("===========XSD==============" + locationKey);
//            log.info(wsdl);
            response.getOutputStream().write(((String)wsdl).getBytes());
        } else {
            Definition definition = (Definition) wsdl;
            synchronized (definition) {
                Map services = definition.getServices();
                for (Iterator iter1 = services.values().iterator(); iter1.hasNext();) {
                    Service service = (Service) iter1.next();
                    Map ports = service.getPorts();
                    for (Iterator iter2 = ports.values().iterator(); iter2.hasNext();) {
                        Port port = (Port) iter2.next();
                        for (Iterator iter3 = port.getExtensibilityElements().iterator(); iter3.hasNext();) {
                            ExtensibilityElement element = (ExtensibilityElement) iter3.next();
                            if (element instanceof SOAPAddress) {
                                SOAPAddress soapAddress = (SOAPAddress) element;
                                // We replace the host and port here.
                                String oldLocation = soapAddress.getLocationURI();
                                URI oldLocationURI = new URI(oldLocation);
                                URI updated = new URI(realLocation.getScheme(),
                                        realLocation.getUserInfo(),
                                        realLocation.getHost(),
                                        realLocation.getPort(),
                                        oldLocationURI.getPath(), // Humm is this right?
                                        null,
                                        null);
                                soapAddress.setLocationURI(updated.toString());
                            }
                        }
                    }
                }
//                log.info("===========WSDL==============" + locationKey);
//                OutputStream baos = new java.io.ByteArrayOutputStream();
//                wsdlWriter.writeWSDL(definition, baos);
//                log.info(baos.toString());

                // Dump the WSDL dom to the output stream
                OutputStream out = response.getOutputStream();
                wsdlWriter.writeWSDL(definition, out);

            }
        }
    }

    public URI getLocation() {
        return location;
    }

    private void readObject(ObjectInputStream in) throws IOException {
        try {
            in.defaultReadObject();
        } catch (ClassNotFoundException e) {
            throw (IOException)new IOException("Could not deserialize!").initCause(e);
        }
        try {
            WSDLFactory wsdlFactory = WSDLFactory.newInstance();
            wsdlWriter = wsdlFactory.newWSDLWriter();
        } catch (WSDLException e) {
            throw (IOException)new IOException("Could not construct transient wsdlWriter").initCause(e);
        }
    }

}
