/**
 *
 * Copyright 2003-2005 The Apache Software Foundation
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

package org.apache.geronimo.javamail.transport.nntp;

import java.io.PrintStream;
import java.util.ArrayList;

import javax.mail.Address;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.URLName;
import javax.mail.event.TransportEvent;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.NewsAddress;

import org.apache.geronimo.mail.util.SessionUtil;

/**
 * Simple implementation of NNTP transport. Just does plain RFC977-ish delivery.
 * <p/> There is no way to indicate failure for a given recipient (it's possible
 * to have a recipient address rejected). The sun impl throws exceptions even if
 * others successful), but maybe we do a different way... <p/>
 * 
 * @version $Rev$ $Date$
 */
public class NNTPTransport extends Transport {

    /**
     * property keys for protocol properties.
     */
    protected static final String NNTP_AUTH = "auth";

    protected static final String NNTP_PORT = "port";

    protected static final String NNTP_FROM = "from";

    protected static final String protocol = "nntp-post";

    protected static final int DEFAULT_NNTP_PORT = 119;

    // our active connection object (shared code with the NNTPStore).
    protected NNTPConnection connection;

    // our session provided debug output stream.
    protected PrintStream debugStream;

    /**
     * Normal constructor for an NNTPTransport() object. This constructor is
     * used to build a transport instance for the "smtp" protocol.
     * 
     * @param session
     *            The attached session.
     * @param name
     *            An optional URLName object containing target information.
     */
    public NNTPTransport(Session session, URLName name) {
        super(session, name);

        // get our debug output.
        debugStream = session.getDebugOut();
    }

    /**
     * Do the protocol connection for an NNTP transport. This handles server
     * authentication, if possible. Returns false if unable to connect to the
     * server.
     * 
     * @param host
     *            The target host name.
     * @param port
     *            The server port number.
     * @param user
     *            The authentication user (if any).
     * @param password
     *            The server password. Might not be sent directly if more
     *            sophisticated authentication is used.
     * 
     * @return true if we were able to connect to the server properly, false for
     *         any failures.
     * @exception MessagingException
     */
    protected boolean protocolConnect(String host, int port, String username, String password)
            throws MessagingException {
        if (debug) {
            debugOut("Connecting to server " + host + ":" + port + " for user " + username);
        }

        // first check to see if we need to authenticate. If we need this, then
        // we must have a username and
        // password specified. Failing this may result in a user prompt to
        // collect the information.
        boolean mustAuthenticate = SessionUtil.getBooleanProperty(session, NNTP_AUTH, false);

        // if we need to authenticate, and we don't have both a userid and
        // password, then we fail this
        // immediately. The Service.connect() method will try to obtain the user
        // information and retry the
        // connection one time.
        if (mustAuthenticate && (username == null || password == null)) {
            return false;
        }

        // if the port is defaulted, then see if we have something configured in
        // the session.
        // if not configured, we just use the default default.
        if (port == -1) {
            // check for a property and fall back on the default if it's not
            // set.
            port = SessionUtil.getIntProperty(session, NNTP_PORT, DEFAULT_NNTP_PORT);
        }

        // create socket and connect to server.
        connection = new NNTPConnection(protocol, session, host, port, username, password, debug);
        connection.connect();

        // we're going to return success here, but in truth, the server may end
        // up asking for our
        // bonafides at any time, and we'll be expected to authenticate then.
        return true;
    }

    /**
     * Send a message to multiple addressees.
     * 
     * @param message
     *            The message we're sending.
     * @param addresses
     *            An array of addresses to send to.
     * 
     * @exception MessagingException
     */
    public void sendMessage(Message message, Address[] addresses) throws MessagingException {
        if (!isConnected()) {
            throw new IllegalStateException("Not connected");
        }

        if (!connection.isPostingAllowed()) {
            throw new MessagingException("Posting disabled for host server");
        }
        // don't bother me w/ null messages or no addreses
        if (message == null) {
            throw new MessagingException("Null message");
        }

        // NNTP only handles instances of MimeMessage, not the more general
        // message case.
        if (!(message instanceof MimeMessage)) {
            throw new MessagingException("NNTP can only send MimeMessages");
        }

        // need to sort the from value out from a variety of sources.
        InternetAddress from = null;

        Address[] fromAddresses = message.getFrom();

        // If the message has a From address set, we just use that. Otherwise,
        // we set a From using
        // the property version, if available.
        if (fromAddresses == null || fromAddresses.length == 0) {
            // the from value can be set explicitly as a property
            String defaultFrom = session.getProperty(NNTP_FROM);
            if (defaultFrom == null) {
                message.setFrom(new InternetAddress(defaultFrom));
            }
        }

        // we must have a message list.
        if (addresses == null || addresses.length == 0) {
            throw new MessagingException("Null or empty address array");
        }

        boolean haveGroup = false;

        // enforce the requirement that all of the targets are NewsAddress
        // instances.
        for (int i = 0; i < addresses.length; i++) {
            if (!(addresses[i] instanceof NewsAddress)) {
                System.out.println("Illegal address is of class " + addresses[i].getClass());
                throw new MessagingException("Illegal NewsAddress " + addresses[i]);
            }
        }

        // event notifcation requires we send lists of successes and failures
        // broken down by category.
        // The categories are:
        //
        // 1) addresses successfully processed.
        // 2) addresses deemed valid, but had a processing failure that
        // prevented sending.
        // 3) addressed deemed invalid (basically all other processing
        // failures).
        ArrayList sentAddresses = new ArrayList();
        ArrayList unsentAddresses = new ArrayList();
        ArrayList invalidAddresses = new ArrayList();

        boolean sendFailure = false;

        // now try to post this message to the different news groups.
        for (int i = 0; i < addresses.length; i++) {
            try {
                // select the target news group
                NNTPReply reply = connection.selectGroup(((NewsAddress) addresses[i]).getNewsgroup());

                if (reply.getCode() != NNTPReply.GROUP_SELECTED) {
                    invalidAddresses.add(addresses[i]);
                    sendFailure = true;
                } else {
                    // send data
                    connection.sendPost(message);
                    sentAddresses.add(addresses[i]);
                }
            } catch (MessagingException e) {
                unsentAddresses.add(addresses[i]);
                sendFailure = true;
            }
        }

        // create our lists for notification and exception reporting from this
        // point on.
        Address[] sent = (Address[]) sentAddresses.toArray(new Address[0]);
        Address[] unsent = (Address[]) unsentAddresses.toArray(new Address[0]);
        Address[] invalid = (Address[]) invalidAddresses.toArray(new Address[0]);

        if (sendFailure) {
            // did we deliver anything at all?
            if (sent.length == 0) {
                // notify of the error.
                notifyTransportListeners(TransportEvent.MESSAGE_NOT_DELIVERED, sent, unsent, invalid, message);
            } else {
                // notify that we delivered at least part of this
                notifyTransportListeners(TransportEvent.MESSAGE_PARTIALLY_DELIVERED, sent, unsent, invalid, message);
            }

            throw new MessagingException("Error posting NNTP message");
        }

        // notify our listeners of successful delivery.
        notifyTransportListeners(TransportEvent.MESSAGE_DELIVERED, sent, unsent, invalid, message);
    }

    /**
     * Close the connection. On completion, we'll be disconnected from the
     * server and unable to send more data.
     * 
     * @exception MessagingException
     */
    public void close() throws MessagingException {
        // This is done to ensure proper event notification.
        super.close();
        connection.close();
        connection = null;
    }

    /**
     * Internal debug output routine.
     * 
     * @param value
     *            The string value to output.
     */
    protected void debugOut(String message) {
        debugStream.println("NNTPTransport DEBUG: " + message);
    }

    /**
     * Internal debugging routine for reporting exceptions.
     * 
     * @param message
     *            A message associated with the exception context.
     * @param e
     *            The received exception.
     */
    protected void debugOut(String message, Throwable e) {
        debugOut("Received exception -> " + message);
        debugOut("Exception message -> " + e.getMessage());
        e.printStackTrace(debugStream);
    }

    /**
     * Get a property associated with this mail protocol.
     * 
     * @param name
     *            The name of the property.
     * 
     * @return The property value (returns null if the property has not been
     *         set).
     */
    String getProperty(String name) {
        // the name we're given is the least qualified part of the name. We
        // construct the full property name
        // using the protocol (either "nntp" or "nntp-post").
        String fullName = "mail." + protocol + "." + name;
        return session.getProperty(fullName);
    }

    /**
     * Get a property associated with this mail session. Returns the provided
     * default if it doesn't exist.
     * 
     * @param name
     *            The name of the property.
     * @param defaultValue
     *            The default value to return if the property doesn't exist.
     * 
     * @return The property value (returns defaultValue if the property has not
     *         been set).
     */
    String getProperty(String name, String defaultValue) {
        // the name we're given is the least qualified part of the name. We
        // construct the full property name
        // using the protocol (either "nntp" or "nntp-post").
        String fullName = "mail." + protocol + "." + name;
        return SessionUtil.getProperty(session, fullName, defaultValue);
    }

    /**
     * Get a property associated with this mail session as an integer value.
     * Returns the default value if the property doesn't exist or it doesn't
     * have a valid int value.
     * 
     * @param name
     *            The name of the property.
     * @param defaultValue
     *            The default value to return if the property doesn't exist.
     * 
     * @return The property value converted to an int.
     */
    int getIntProperty(String name, int defaultValue) {
        // the name we're given is the least qualified part of the name. We
        // construct the full property name
        // using the protocol (either "nntp" or "nntp-post").
        String fullName = "mail." + protocol + "." + name;
        return SessionUtil.getIntProperty(session, fullName, defaultValue);
    }

    /**
     * Get a property associated with this mail session as an boolean value.
     * Returns the default value if the property doesn't exist or it doesn't
     * have a valid int value.
     * 
     * @param name
     *            The name of the property.
     * @param defaultValue
     *            The default value to return if the property doesn't exist.
     * 
     * @return The property value converted to a boolean
     */
    boolean getBooleanProperty(String name, boolean defaultValue) {
        // the name we're given is the least qualified part of the name. We
        // construct the full property name
        // using the protocol (either "nntp" or "nntp-post").
        String fullName = "mail." + protocol + "." + name;
        return SessionUtil.getBooleanProperty(session, fullName, defaultValue);
    }
}
