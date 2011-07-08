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
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.net.HttpURLConnection;
import java.security.Principal;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.cxf.Bus;
import org.apache.cxf.binding.Binding;
import org.apache.cxf.message.ExchangeImpl;
import org.apache.cxf.message.Message;
import org.apache.cxf.message.MessageImpl;
import org.apache.cxf.security.SecurityContext;
import org.apache.cxf.service.model.EndpointInfo;
import org.apache.cxf.transport.Conduit;
import org.apache.cxf.transport.ConduitInitiator;
import org.apache.cxf.transport.Destination;
import org.apache.cxf.transport.MessageObserver;
import org.apache.cxf.transport.http.AbstractHTTPDestination;
import org.apache.cxf.transport.http.HTTPSession;
import org.apache.cxf.ws.addressing.EndpointReferenceType;
import org.apache.geronimo.webservices.WebServiceContainer;
import org.apache.geronimo.webservices.WebServiceContainer.Request;
import org.apache.geronimo.webservices.WebServiceContainer.Response;

public class GeronimoDestination extends AbstractHTTPDestination
        implements Serializable {

    private MessageObserver messageObserver;
    private boolean passSecurityContext = false;
    private CXFEndpoint endpoint;

    public GeronimoDestination(Bus bus, 
                               ConduitInitiator conduitInitiator, 
                               EndpointInfo endpointInfo) throws IOException {
        super(bus, conduitInitiator, endpointInfo, false);
        this.endpoint = bus.getExtension(CXFEndpoint.class);
    }

    private Binding getBinding() {
        return this.endpoint.getEndpoint().getBinding();
    }
    
    public void setPassSecurityContext(boolean passSecurityContext) {
        this.passSecurityContext = passSecurityContext;
    }
    
    public boolean getPassSecurityContext() {
        return this.passSecurityContext;
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

        final HttpServletRequest servletRequest = 
            (HttpServletRequest)request.getAttribute(WebServiceContainer.SERVLET_REQUEST);
        message.put(HTTP_REQUEST, servletRequest);
        
        HttpServletResponse servletResponse =
            (HttpServletResponse)request.getAttribute(WebServiceContainer.SERVLET_RESPONSE);
        message.put(HTTP_RESPONSE, servletResponse);
        
        ServletContext servletContext = 
            (ServletContext)request.getAttribute(WebServiceContainer.SERVLET_CONTEXT);
        message.put(HTTP_CONTEXT, servletContext);
        
        if (this.passSecurityContext) {
            message.put(SecurityContext.class, new SecurityContext() {
                public Principal getUserPrincipal() {
                    return servletRequest.getUserPrincipal();
                }
                public boolean isUserInRole(String role) {
                    return servletRequest.isUserInRole(role);
                }
            });
        }
        
        // this calls copyRequestHeaders()
        setHeaders(message);
        
        message.put(Message.HTTP_REQUEST_METHOD, servletRequest.getMethod());
        message.put(Message.PATH_INFO, servletRequest.getPathInfo());
        message.put(Message.QUERY_STRING, servletRequest.getQueryString());
        message.put(Message.CONTENT_TYPE, servletRequest.getContentType());
        message.put(Message.ENCODING, getCharacterEncoding(servletRequest.getCharacterEncoding()));
        
        ExchangeImpl exchange = new ExchangeImpl();
        exchange.setInMessage(getBinding().createMessage(message));
        exchange.setSession(new HTTPSession(servletRequest));
        
        messageObserver.onMessage(message);
    }

    private static String getCharacterEncoding(String encoding) {
        if (encoding != null) {
            encoding = encoding.trim();
            // work around a bug with Jetty which results in the character
            // encoding not being trimmed correctly:
            // http://jira.codehaus.org/browse/JETTY-302
            if (encoding.endsWith("\"")) {
                encoding = encoding.substring(0, encoding.length() - 1);
            }
        }
        return encoding;
    }
    
    public Logger getLogger() {
        return Logger.getLogger(GeronimoDestination.class.getName());
    }

    public Conduit getInbuiltBackChannel(Message inMessage) {
        return new BackChannelConduit(null, inMessage);
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
        
        public void prepare(Message message) throws IOException {
            send(message);
        }

        /**
         * Send an outbound message, assumed to contain all the name-value
         * mappings of the corresponding input message (if any).
         *
         * @param message the message to be sent.
         */
        public void send(Message message) throws IOException {
            Response response = (Response)request.get(Response.class);
            
            // handle response headers
            updateResponseHeaders(message);

            Map<String, List<String>> protocolHeaders = 
                (Map<String, List<String>>) message.get(Message.PROTOCOL_HEADERS);

            // set headers of the HTTP response object
            Iterator headers = protocolHeaders.entrySet().iterator();
            while (headers.hasNext()) {
                Map.Entry entry = (Map.Entry) headers.next();
                String headerName = (String) entry.getKey();
                String headerValue = getHeaderValue((List) entry.getValue());
                response.setHeader(headerName, headerValue);
            }
            
            message.setContent(OutputStream.class, new WrappedOutputStream(message, response));
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
        
    private String getHeaderValue(List<String> values) {
        Iterator iter = values.iterator();
        StringBuilder buf = new StringBuilder();
        while(iter.hasNext()) {
            buf.append(iter.next());
            if (iter.hasNext()) {
                buf.append(", ");
            }
        }
        return buf.toString();
    }
    
    protected void setContentType(Message message, Response response) {                
        Map<String, List<String>> protocolHeaders =
            (Map<String, List<String>>)message.get(Message.PROTOCOL_HEADERS);
        
        if (protocolHeaders == null || !protocolHeaders.containsKey(Message.CONTENT_TYPE)) {
            String ct = (String) message.get(Message.CONTENT_TYPE);
            String enc = (String) message.get(Message.ENCODING);
            
            if (null != ct) {
                if (enc != null && ct.indexOf("charset=") == -1) {
                    ct = ct + "; charset=" + enc;
                }
                response.setContentType(ct);
            } else if (enc != null) {
                response.setContentType("text/xml; charset=" + enc);
            }
        }     
    }
               
    private class WrappedOutputStream extends OutputStream {

        private Message message;
        private Response response;
        private OutputStream rawOutputStream;

        WrappedOutputStream(Message message, Response response) {
            this.message = message;
            this.response = response;
        }

        public void write(int b) throws IOException {
            flushHeaders();
            this.rawOutputStream.write(b);
        }

        public void write(byte b[]) throws IOException {
            flushHeaders();
            this.rawOutputStream.write(b);
        }

        public void write(byte b[], int off, int len) throws IOException {
            flushHeaders();
            this.rawOutputStream.write(b, off, len);
        }

        public void flush() throws IOException {
            flushHeaders();
            this.rawOutputStream.flush();
        }

        public void close() throws IOException {
            flushHeaders();
            this.rawOutputStream.close();
        }
        
         protected void flushHeaders() throws IOException {
            if (this.rawOutputStream != null) {
                return;
            }

            // set response code
            Integer i = (Integer) this.message.get(Message.RESPONSE_CODE);
            if (i != null) {
                this.response.setStatusCode(i.intValue());
            }
            
            // set content-type
            setContentType(this.message, this.response);
            
            this.rawOutputStream = this.response.getOutputStream();
        }

    }
       
}
