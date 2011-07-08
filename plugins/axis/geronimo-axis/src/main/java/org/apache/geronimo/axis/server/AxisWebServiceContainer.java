/**
 *   Licensed to the Apache Software Foundation (ASF) under one or more
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
package org.apache.geronimo.axis.server;

import java.io.InputStream;
import java.io.IOException;
import java.net.URI;
import java.util.Iterator;
import java.util.Map;
import javax.servlet.http.HttpServletResponse;
import javax.wsdl.OperationType;
import javax.xml.soap.MimeHeader;
import javax.xml.soap.MimeHeaders;
import javax.xml.soap.SOAPMessage;

import org.apache.axis.AxisEngine;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.geronimo.webservices.WebServiceContainer;
import org.apache.geronimo.webservices.saaj.SAAJUniverse;
import org.w3c.dom.Element;

/**
 * @version $Rev$ $Date$
 */
public class AxisWebServiceContainer implements WebServiceContainer {
    public static final String REQUEST = AxisWebServiceContainer.class.getName() + "@Request";
    public static final String RESPONSE = AxisWebServiceContainer.class.getName() + "@Response";

    private static final Logger log = LoggerFactory.getLogger(AxisWebServiceContainer.class);

    public static final String XSD_NS = "http://www.w3.org/2001/XMLSchema";

    private final URI location;
    private final URI wsdlLocation;
    private final SOAPService service;

    private transient final ClassLoader classLoader;
    private final Map wsdlMap;

    public AxisWebServiceContainer(URI location, URI wsdlURL, SOAPService service, Map wsdlMap, ClassLoader classLoader) {
        this.location = location;
        this.wsdlLocation = wsdlURL;
        this.service = service;
        this.wsdlMap = wsdlMap;
        if (classLoader == null) {
            this.classLoader = Thread.currentThread().getContextClassLoader();
        } else {
            this.classLoader = classLoader;
        }
    }

    public void invoke(Request req, Response res) throws Exception {
        SAAJUniverse universe = new SAAJUniverse();
        universe.set(SAAJUniverse.AXIS1);
        try {
            doService(req, res);
        } finally {
            universe.unset();
        }
    }
    
    protected void doService(Request req, Response res) throws Exception {
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
        messageContext.setProperty(AxisEngine.PROP_DISABLE_PRETTY_XML, Boolean.TRUE);

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
                
               	if(req.getMethod() == req.GET && req.getParameters().isEmpty()){
               		String serviceName = req.getURI().getRawPath();
                    serviceName = serviceName.substring(serviceName.lastIndexOf("/")+1);
               		printServiceInfo(res,serviceName);
               		return;
               	}else{
               		responseMessage = handleFault(fault, res, messageContext);
               	}

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
                for (Iterator i = responseMimeHeaders.getAllHeaders(); i.hasNext();) {
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
                log.warn(Messages.getMessage("exception00"), e);
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
        log.warn(Messages.getMessage("exception00"), e);
        if (responseMsg == null) {
            AxisFault fault = AxisFault.makeFault(e);
            //log the fault
            Element runtimeException = fault.lookupFaultDetail(Constants.QNAME_FAULTDETAIL_RUNTIMEEXCEPTION);
            if (runtimeException != null) {
                log.debug(Messages.getMessage("axisFault00"), fault);
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

        log.warn(Messages.getMessage("axisFault00"), fault);
        if (runtimeException != null) {
            //strip runtime details
            fault.removeFaultDetail(Constants.QNAME_FAULTDETAIL_RUNTIMEEXCEPTION);
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
//        log.info("Request at " + realLocation);
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
        URI updated = new URI(realLocation.getScheme(),
                realLocation.getUserInfo(),
                realLocation.getHost(),
                realLocation.getPort(),
                null, //try null for no path
                null,
                null);
        String replaced = ((String) wsdl).replaceAll(LOCATION_REPLACEMENT_TOKEN, updated.toString());
        response.getOutputStream().write(replaced.getBytes());
        response.getOutputStream().flush();
    }

    public void destroy() {
    }

    public URI getLocation() {
        return location;
    }

    protected Object readResolve() {
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        if (classLoader == null) {
            classLoader = AxisWebServiceContainer.class.getClassLoader();
        }
        return new AxisWebServiceContainer(location, wsdlLocation, service, wsdlMap, classLoader);
    }

    /**
     * print a snippet of service info.
     * @param response response
     * @param serviceName Name of the service
     */

    private void printServiceInfo(Response response,String serviceName) throws IOException{
        response.setContentType("text/html; charset=utf-8");
        StringBuilder output = new StringBuilder("<h1>")
                .append(serviceName).append("</h1>\n");

        output.append("<p>").append(Messages.getMessage("axisService00"))
                .append("</p>\n");
        output.append(
                "<i>").append(
                Messages.getMessage("perhaps00") ).append(
                "</i>\n");
        response.getOutputStream().write(output.toString().getBytes());
    }

}
























