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
package org.apache.geronimo.cxf;

import java.io.IOException;
import java.io.OutputStream;
import java.io.Serializable;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.util.Iterator;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Enumeration;
import java.util.StringTokenizer;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.ws.handler.MessageContext;

import org.apache.cxf.Bus;
import org.apache.cxf.message.Exchange;
import org.apache.cxf.message.Message;
import org.apache.cxf.message.MessageImpl;
import org.apache.cxf.service.model.EndpointInfo;
import org.apache.cxf.transport.Conduit;
import org.apache.cxf.transport.ConduitInitiator;
import org.apache.cxf.transport.Destination;
import org.apache.cxf.transport.MessageObserver;
import org.apache.cxf.transport.http.AbstractHTTPDestination;
import org.apache.cxf.ws.addressing.EndpointReferenceType;
import org.apache.cxf.wsdl.EndpointReferenceUtils;
import org.apache.geronimo.webservices.WebServiceContainer;
import org.apache.geronimo.webservices.WebServiceContainer.Request;
import org.apache.geronimo.webservices.WebServiceContainer.Response;

public class GeronimoDestination extends AbstractHTTPDestination
        implements Serializable {

    private MessageObserver messageObserver;

    public GeronimoDestination(Bus bus, 
                               ConduitInitiator conduitInitiator, 
                               EndpointInfo endpointInfo) throws IOException {
        super(bus, conduitInitiator, endpointInfo);
    }

    public EndpointInfo getEndpointInfo() {
        return this.endpointInfo;
    }

    public void invoke(Request request, Response response) throws Exception {
        MessageImpl message = new MessageImpl();
        message.setContent(InputStream.class, request.getInputStream());
        message.setDestination(this);

        message.put(Request.class, request);
        message.put(Response.class, response);

        HttpServletRequest servletRequest = 
            (HttpServletRequest)request.getAttribute(WebServiceContainer.SERVLET_REQUEST);
        message.put(MessageContext.SERVLET_REQUEST, servletRequest);
        
        HttpServletResponse servletResponse =
            (HttpServletResponse)request.getAttribute(WebServiceContainer.SERVLET_RESPONSE);
        message.put(MessageContext.SERVLET_RESPONSE, servletResponse);
        
        ServletContext servletContext = 
            (ServletContext)request.getAttribute(WebServiceContainer.SERVLET_CONTEXT);
        message.put(MessageContext.SERVLET_CONTEXT, servletContext);
        
        // this calls copyRequestHeaders()
        setHeaders(message);
        
        message.put(Message.HTTP_REQUEST_METHOD, servletRequest.getMethod());
        message.put(Message.PATH_INFO, servletRequest.getPathInfo());
        message.put(Message.QUERY_STRING, servletRequest.getQueryString());
        message.put(Message.CONTENT_TYPE, servletRequest.getContentType());
        message.put(Message.ENCODING, servletRequest.getCharacterEncoding());
        
        messageObserver.onMessage(message);
    }

    protected void copyRequestHeaders(Message message, Map<String, List<String>> headers) {
        HttpServletRequest servletRequest = (HttpServletRequest)message.get(MessageContext.SERVLET_REQUEST);
        if (servletRequest != null) {
            Enumeration names = servletRequest.getHeaderNames();
            while(names.hasMoreElements()) {
                String name = (String)names.nextElement();
                
                List<String> headerValues = headers.get(name);
                if (headerValues == null) {
                    headerValues = new ArrayList<String>();
                    headers.put(name, headerValues);
                }
                
                Enumeration values = servletRequest.getHeaders(name);
                while(values.hasMoreElements()) {
                    String value = (String)values.nextElement();
                    headerValues.add(value);
                }
            }
        }
    }

    public Conduit getBackChannel(Message inMessage,
                                  Message partialResponse,
                                  EndpointReferenceType address) throws IOException {
        Conduit backChannel = null;
        if (address == null) {
            backChannel = new BackChannelConduit(address, inMessage);
        } else {
            if (partialResponse != null) {
                // setup the outbound message to for 202 Accepted
                partialResponse.put(Message.RESPONSE_CODE,
                                    HttpURLConnection.HTTP_ACCEPTED);
                backChannel = new BackChannelConduit(address, inMessage);
            } else {
                backChannel = conduitInitiator.getConduit(endpointInfo, address);
                // ensure decoupled back channel input stream is closed
                backChannel.setMessageObserver(new MessageObserver() {
                    public void onMessage(Message m) {
                        if (m.getContentFormats().contains(InputStream.class)) {
                            InputStream is = m.getContent(InputStream.class);
                            try {
                                is.close();
                            } catch (Exception e) {
                                // ignore
                            }
                        }
                    }
                });
            }
        }
        return backChannel;
    }

    public void shutdown() {
    }

    public void setMessageObserver(MessageObserver messageObserver) {
        this.messageObserver = messageObserver;
    }

    protected class BackChannelConduit implements Conduit {

        protected Message request;
        protected EndpointReferenceType target;

        BackChannelConduit(EndpointReferenceType target, Message request) {
            this.target = target;
            this.request = request;
        }

        public void close(Message msg) throws IOException {
            msg.getContent(OutputStream.class).close();
        }

        /**
         * Register a message observer for incoming messages.
         *
         * @param observer the observer to notify on receipt of incoming
         */
        public void setMessageObserver(MessageObserver observer) {
            // shouldn't be called for a back channel conduit
        }

        /**
         * Send an outbound message, assumed to contain all the name-value
         * mappings of the corresponding input message (if any).
         *
         * @param message the message to be sent.
         */
        public void send(Message message) throws IOException {
            Response response = (Response)request.get(Response.class);

            // 1. handle response code
            Integer i = (Integer)message.get(Message.RESPONSE_CODE);
            if (i != null) {
                response.setStatusCode(i.intValue());
            }

            // 2. handle response headers
            updateResponseHeaders(message);

            Map<String, List<String>> protocolHeaders =
                (Map<String, List<String>>)message.get(Message.PROTOCOL_HEADERS);

            // set headers of the HTTP response object
            Iterator headers = protocolHeaders.entrySet().iterator();
            while(headers.hasNext()) {
                Map.Entry entry = (Map.Entry)headers.next();
                String headerName = (String)entry.getKey();
                String headerValue = getHeaderValue((List)entry.getValue());
                response.setHeader(headerName, headerValue);
            }

            //TODO gregw says this should work: current cxf-jetty code wraps output stream.
            //if this doesn't work, we'd see an error from jetty saying you cant write headers to the output stream.
            message.setContent(OutputStream.class, response.getOutputStream());
        }

        private String getHeaderValue(List<String> values) {
            Iterator iter = values.iterator();
            StringBuffer buf = new StringBuffer();
            while(iter.hasNext()) {
                buf.append(iter.next());
                if (iter.hasNext()) {
                    buf.append(", ");
                }
            }
            return buf.toString();
        }
        
        /**
         * @return the reference associated with the target Destination
         */
        public EndpointReferenceType getTarget() {
            return target;
        }

        /**
         * Retreive the back-channel Destination.
         *
         * @return the backchannel Destination (or null if the backchannel is
         *         built-in)
         */
        public Destination getBackChannel() {
            return null;
        }

        /**
         * Close the conduit
         */
        public void close() {
        }
    }

}
