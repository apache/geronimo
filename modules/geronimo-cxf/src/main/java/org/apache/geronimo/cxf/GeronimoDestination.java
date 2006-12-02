package org.apache.geronimo.cxf;

import java.io.IOException;
import java.io.OutputStream;
import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

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
import org.apache.geronimo.webservices.WebServiceContainer.Request;
import org.apache.geronimo.webservices.WebServiceContainer.Response;

public class GeronimoDestination extends AbstractHTTPDestination
        implements Serializable {

    private MessageObserver messageObserver;

    public GeronimoDestination(Bus bus, ConduitInitiator conduitInitiator, EndpointInfo endpointInfo) throws IOException {
        super(bus, conduitInitiator, endpointInfo);
    }


    public void invoke(Request request, Response response) {
        Message message = new MessageImpl();
        message.put(Request.class, request);
        message.put(Response.class, response);
        messageObserver.onMessage(message);
    }

    public Conduit getBackChannel(Message inMessage, Message partialResponse, EndpointReferenceType address) throws IOException {
        Response response = inMessage.get(Response.class);
        Conduit backChannel;
        Exchange ex = inMessage.getExchange();
        EndpointReferenceType target = address != null
                ? address
                : ex.get(EndpointReferenceType.class);
        if (target == null) {
            backChannel = new BackChannelConduit(response);
        } else {
            throw new IllegalArgumentException("RM not yet implemented");
        }
        return backChannel;
    }

    public void shutdown() {
    }

    @Override
    protected void copyRequestHeaders(Message message, Map<String, List<String>> headers) {
        Request req = message.get(Request.class);

        // no map of headers so just find all static field constants that begin with HEADER_, get
        // its value and get the corresponding header.
        for (Field field : Request.class.getFields()) {
            if (field.getName().startsWith("HEADER_")) {
                try {
                    assert field.getType().equals(String.class) : "unexpected field type";
                    String headerName = (String) field.get(null);
                    String headerValue = req.getHeader(headerName);
                    if (headerValue != null) {
                        List<String> values = headers.get(headerName);
                        if (values == null) {
                            values = new LinkedList<String>();
                            headers.put(headerName, values);
                        }
                        values.addAll(splitMultipleHeaderValues(headerValue));
                    }
                } catch (IllegalAccessException ex) {
                    // ignore 
                }
            }
        }
    }

    private List<String> splitMultipleHeaderValues(String value) {

        List<String> allValues = new LinkedList<String>();
        if (value.contains(",")) {
            StringTokenizer st = new StringTokenizer(value, ",");
            while (st.hasMoreTokens()) {
                allValues.add(st.nextToken().trim());
            }

        } else {
            allValues.add(value);
        }
        return allValues;
    }


    public void setMessageObserver(MessageObserver messageObserver) {
        this.messageObserver = messageObserver;
    }

    protected class BackChannelConduit implements Conduit {

        //TODO this will soon be publically available from somewhere in CXF
        private static final String ANONYMOUS_ADDRESS =
                "http://www.w3.org/2005/08/addressing/anonymous";
        protected Response response;
        protected EndpointReferenceType target;

        BackChannelConduit(Response resp) {
            response = resp;
            target = EndpointReferenceUtils.getEndpointReference(ANONYMOUS_ADDRESS);
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
            message.put(Response.class, response);
            //TODO gregw says this should work: current cxf-jetty code wraps output stream.
            //if this doesn't work, we'd see an error from jetty saying you cant write headers to the output stream.
            message.setContent(OutputStream.class, response.getOutputStream());
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
