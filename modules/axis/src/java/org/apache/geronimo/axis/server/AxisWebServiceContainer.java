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

import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.net.URL;
import javax.servlet.http.HttpServletResponse;
import javax.xml.soap.SOAPException;
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
    private static Log log = LogFactory.getLog(AxisWebServiceContainer.class);

    public static final String XSD_NS = "http://www.w3.org/2001/XMLSchema";
    protected final URI location;
    protected final URL wsdlURL;
    protected final SOAPService service;  //TODO why did i make these protected?
    private final ClassLoader classLoader;

    public AxisWebServiceContainer(URI location, URL wsdlURL, SOAPService service, ClassLoader classLoader) {
        this.location = location;
        this.wsdlURL = wsdlURL;
        this.service = service;
        this.classLoader = classLoader;
    }

    public void invoke(Request req, Response res) throws Exception {
        org.apache.axis.MessageContext context = new org.apache.axis.MessageContext(null);
        context.setClassLoader(classLoader);
        
        Message responseMessage = null;

        String contentType = req.getHeader(HTTPConstants.HEADER_CONTENT_TYPE);
        String contentLocation = req.getHeader(HTTPConstants.HEADER_CONTENT_LOCATION);
        InputStream inputStream = req.getInputStream();
        Message requestMessage = new Message(inputStream, false, contentType, contentLocation);

        context.setRequestMessage(requestMessage);
        context.setProperty(HTTPConstants.MC_HTTP_SERVLETPATHINFO, req.getURI().getPath());
        context.setProperty(org.apache.axis.MessageContext.TRANS_URL, req.getURI().toString());
        context.setService(service);

        try {
            String characterEncoding = (String) requestMessage.getProperty(SOAPMessage.CHARACTER_SET_ENCODING);
            if (characterEncoding != null) {
                context.setProperty(SOAPMessage.CHARACTER_SET_ENCODING, characterEncoding);
            } else {
                context.setProperty(SOAPMessage.CHARACTER_SET_ENCODING, "UTF-8");
            }


            String soapAction = req.getHeader(HTTPConstants.HEADER_SOAP_ACTION);
            if (soapAction != null) {
                context.setUseSOAPAction(true);
                context.setSOAPActionURI(soapAction);
            }

            SOAPEnvelope env = requestMessage.getSOAPEnvelope();
            if (env != null && env.getSOAPConstants() != null) {
                context.setSOAPConstants(env.getSOAPConstants());
            }
            SOAPService service = context.getService();

            service.invoke(context);
            responseMessage = context.getResponseMessage();
        } catch (AxisFault fault) {
            responseMessage = handleFault(fault, res, context);

        } catch (Exception e) {
            responseMessage = handleException(context, res, e);
        }
        try {
            SOAPConstants soapConstants = context.getSOAPConstants();
            String contentType1 = responseMessage.getContentType(soapConstants);
            res.setContentType(contentType1);
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            responseMessage.writeTo(res.getOutputStream());
        } catch (SOAPException e) {
            log.info(Messages.getMessage("exception00"), e);
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

    public void getWsdl(OutputStream out) throws Exception {
        InputStream in = null;
        try {
            in = wsdlURL.openStream();
            byte[] buffer = new byte[1024];
            for (int read = in.read(buffer); read > 0; read = in.read(buffer)) {
//                System.out.write(buffer, 0, read);
                out.write(buffer, 0, read);
            }
        } finally {
            if (in != null) {
                in.close();
            }
        }

    }

    public URI getLocation() {
        return location;
    }

    public URL getWsdlURL() {
        return wsdlURL;
    }
}
