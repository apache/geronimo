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

package org.apache.geronimo.javamail.transport.smtp;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.StringTokenizer;

import javax.mail.Address;
import javax.mail.AuthenticationFailedException;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.URLName;
import javax.mail.event.TransportEvent;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.net.ssl.SSLSocket;

import org.apache.geronimo.javamail.authentication.ClientAuthenticator;
import org.apache.geronimo.javamail.authentication.CramMD5Authenticator;
import org.apache.geronimo.javamail.authentication.DigestMD5Authenticator;
import org.apache.geronimo.javamail.authentication.LoginAuthenticator;
import org.apache.geronimo.javamail.authentication.PlainAuthenticator;
import org.apache.geronimo.javamail.util.MIMEOutputStream;
import org.apache.geronimo.javamail.util.TraceInputStream;
import org.apache.geronimo.javamail.util.TraceOutputStream;
import org.apache.geronimo.mail.util.Base64;
import org.apache.geronimo.mail.util.XText;

/**
 * Simple implementation of SMTP transport. Just does plain RFC821-ish delivery.
 * <p/> Supported properties : <p/>
 * <ul>
 * <li> mail.host : to set the server to deliver to. Default = localhost</li>
 * <li> mail.smtp.port : to set the port. Default = 25</li>
 * <li> mail.smtp.locahost : name to use for HELO/EHLO - default getHostName()</li>
 * </ul>
 * <p/> There is no way to indicate failure for a given recipient (it's possible
 * to have a recipient address rejected). The sun impl throws exceptions even if
 * others successful), but maybe we do a different way... <p/> TODO : lots.
 * ESMTP, user/pass, indicate failure, etc...
 * 
 * @version $Rev$ $Date$
 */
public class SMTPTransport extends Transport {

    /**
     * constants for EOL termination
     */
    protected static final char CR = '\r';

    protected static final char LF = '\n';

    /**
     * property keys for top level session properties.
     */
    protected static final String MAIL_LOCALHOST = "mail.localhost";

    protected static final String MAIL_SSLFACTORY_CLASS = "mail.SSLSocketFactory.class";

    /**
     * property keys for protocol properties. The actual property name will be
     * appended with "mail." + protocol + ".", where the protocol is either
     * "smtp" or "smtps".
     */
    protected static final String MAIL_SMTP_AUTH = "auth";

    protected static final String MAIL_SMTP_PORT = "port";

    protected static final String MAIL_SMTP_LOCALHOST = "localhost";

    protected static final String MAIL_SMTP_TIMEOUT = "timeout";

    protected static final String MAIL_SMTP_SASL_REALM = "sasl.realm";

    protected static final String MAIL_SMTP_TLS = "starttls.enable";

    protected static final String MAIL_SMTP_FACTORY_CLASS = "socketFactory.class";

    protected static final String MAIL_SMTP_FACTORY_FALLBACK = "socketFactory.fallback";

    protected static final String MAIL_SMTP_FACTORY_PORT = "socketFactory.port";

    protected static final String MAIL_SMTP_REPORT_SUCCESS = "reportsuccess";

    protected static final String MAIL_SMTP_STARTTLS_ENABLE = "starttls.enable";

    protected static final String MAIL_SMTP_DSN_NOTIFY = "dsn.notify";

    protected static final String MAIL_SMTP_SENDPARTIAL = "sendpartial";

    protected static final String MAIL_SMTP_LOCALADDRESS = "localaddress";

    protected static final String MAIL_SMTP_LOCALPORT = "localport";

    protected static final String MAIL_SMTP_QUITWAIT = "quitwait";

    protected static final String MAIL_SMTP_FROM = "from";

    protected static final String MAIL_SMTP_DSN_RET = "dsn.ret";

    protected static final String MAIL_SMTP_SUBMITTER = "submitter";

    protected static final String MAIL_SMTP_EXTENSION = "mailextension";

    protected static final String MAIL_SMTP_EHLO = "ehlo";

    protected static final String MAIL_SMTP_ENCODE_TRACE = "encodetrace";

    protected static final int MIN_MILLIS = 1000 * 60;

    protected static final int TIMEOUT = MIN_MILLIS * 5;

    protected static final String DEFAULT_MAIL_HOST = "localhost";

    protected static final int DEFAULT_MAIL_SMTP_PORT = 25;

    protected static final int DEFAULT_MAIL_SMTPS_PORT = 465;

    // SMTP reply codes
    protected static final int SERVICE_READY = 220;

    protected static final int SERVICE_CLOSING = 221;

    protected static final int AUTHENTICATION_COMPLETE = 235;

    protected static final int COMMAND_ACCEPTED = 250;

    protected static final int ADDRESS_NOT_LOCAL = 251;

    protected static final int AUTHENTICATION_CHALLENGE = 334;

    protected static final int START_MAIL_INPUT = 354;

    protected static final int SERVICE_NOT_AVAILABLE = 421;

    protected static final int MAILBOX_BUSY = 450;

    protected static final int PROCESSING_ERROR = 451;

    protected static final int INSUFFICIENT_STORAGE = 452;

    protected static final int COMMAND_SYNTAX_ERROR = 500;

    protected static final int PARAMETER_SYNTAX_ERROR = 501;

    protected static final int COMMAND_NOT_IMPLEMENTED = 502;

    protected static final int INVALID_COMMAND_SEQUENCE = 503;

    protected static final int COMMAND_PARAMETER_NOT_IMPLEMENTED = 504;

    protected static final int MAILBOX_NOT_FOUND = 550;

    protected static final int USER_NOT_LOCAL = 551;

    protected static final int MAILBOX_FULL = 552;

    protected static final int INVALID_MAILBOX = 553;

    protected static final int TRANSACTION_FAILED = 553;

    protected static final String AUTHENTICATION_PLAIN = "PLAIN";

    protected static final String AUTHENTICATION_LOGIN = "LOGIN";

    protected static final String AUTHENTICATION_CRAMMD5 = "CRAM-MD5";

    protected static final String AUTHENTICATION_DIGESTMD5 = "DIGEST-MD5";

    // the protocol we're working with. This will be either "smtp" or "smtps".
    protected String protocol;

    // the target host
    protected String host;

    // the default port to use for this protocol (differs between "smtp" and
    // "smtps").
    protected int defaultPort;

    // the target server port.
    protected int port;

    // the connection socket...can be a plain socket or SSLSocket, if TLS is
    // being used.
    protected Socket socket;

    // our local host name
    protected String localHost;

    // input stream used to read data. If Sasl is in use, this might be other
    // than the
    // direct access to the socket input stream.
    protected InputStream inputStream;

    // the other end of the connection pipeline.
    protected OutputStream outputStream;

    // list of authentication mechanisms supported by the server
    protected HashMap serverAuthenticationMechanisms;

    // map of server extension arguments
    protected HashMap serverExtensionArgs;

    // do we report success after completion of each mail send.
    protected boolean reportSuccess;

    // does the server support transport level security?
    protected boolean serverTLS = false;

    // is TLS enabled on our part?
    protected boolean useTLS = false;

    // do we use SSL for our initial connection?
    protected boolean sslConnection = false;

    // the username we connect with
    protected String username;

    // the authentication password.
    protected String password;

    // the target SASL realm (normally null unless explicitly set or we have an
    // authentication mechanism that
    // requires it.
    protected String realm;

    // the last response line received from the server.
    protected SMTPReply lastServerResponse = null;

    // our session provided debug output stream.
    protected PrintStream debugStream;

    /**
     * Normal constructor for an SMTPTransport() object. This constructor is
     * used to build a transport instance for the "smtp" protocol.
     * 
     * @param session
     *            The attached session.
     * @param name
     *            An optional URLName object containing target information.
     */
    public SMTPTransport(Session session, URLName name) {
        this(session, name, "smtp", DEFAULT_MAIL_SMTP_PORT, false);
    }

    /**
     * Common constructor used by the SMTPTransport and SMTPSTransport classes
     * to do common initialization of defaults.
     * 
     * @param session
     *            The host session instance.
     * @param name
     *            The URLName of the target.
     * @param protocol
     *            The protocol type (either "smtp" or "smtps". This helps us in
     *            retrieving protocol-specific session properties.
     * @param defaultPort
     *            The default port used by this protocol. For "smtp", this will
     *            be 25. The default for "smtps" is 465.
     * @param sslConnection
     *            Indicates whether an SSL connection should be used to initial
     *            contact the server. This is different from the STARTTLS
     *            support, which switches the connection to SSL after the
     *            initial startup.
     */
    protected SMTPTransport(Session session, URLName name, String protocol, int defaultPort, boolean sslConnection) {
        super(session, name);
        this.protocol = protocol;

        // these are defaults based on what the superclass specifies.
        this.defaultPort = defaultPort;
        this.sslConnection = sslConnection;
        // check to see if we need to throw an exception after a send operation.
        reportSuccess = isProtocolPropertyTrue(MAIL_SMTP_REPORT_SUCCESS);
        // and also check for TLS enablement.
        useTLS = isProtocolPropertyTrue(MAIL_SMTP_STARTTLS_ENABLE);

        // get our debug output.
        debugStream = session.getDebugOut();

        System.out.println("Debug value in transport = " + debug);
    }

    /**
     * Connect to a server using an already created socket. This connection is
     * just like any other connection, except we will not create a new socket.
     * 
     * @param socket
     *            The socket connection to use.
     */
    public void connect(Socket socket) throws MessagingException {
        this.socket = socket;
        super.connect();
    }

    /**
     * Do the protocol connection for an SMTP transport. This handles server
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
        boolean mustAuthenticate = isProtocolPropertyTrue(MAIL_SMTP_AUTH);

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
            // take the default first.
            port = defaultPort;
            String configuredPort = getProtocolProperty(MAIL_SMTP_PORT);
            if (configuredPort != null) {
                port = Integer.parseInt(configuredPort);
            }
        }

        try {

            // create socket and connect to server.
            getConnection(host, port, username, password);

            // receive welcoming message
            if (!getWelcome()) {
                throw new MessagingException("Error in getting welcome msg");
            }

            // say hello
            if (!sendHandshake()) {
                throw new MessagingException("Error in saying EHLO to server");
            }

            // authenticate with the server, if necessary
            if (!processAuthentication()) {
                if (debug) {
                    debugOut("User authentication failure");
                }
                throw new AuthenticationFailedException("Error authenticating with server");
            }
        } catch (IOException e) {
            if (debug) {
                debugOut("I/O exception establishing connection", e);
            }
            throw new MessagingException("Connection error", e);
        }
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
        // don't bother me w/ null messages or no addreses
        if (message == null) {
            throw new MessagingException("Null message");
        }

        // SMTP only handles instances of MimeMessage, not the more general
        // message case.
        if (!(message instanceof MimeMessage)) {
            throw new MessagingException("SMTP can only send MimeMessages");
        }

        // we must have a message list.
        if (addresses == null || addresses.length == 0) {
            throw new MessagingException("Null or empty address array");
        }

        boolean haveGroup = false;

        // enforce the requirement that all of the targets are InternetAddress
        // instances.
        for (int i = 0; i < addresses.length; i++) {
            if (addresses[i] instanceof InternetAddress) {
                // and while we're here, see if we have a groups in the address
                // list. If we do, then
                // we're going to need to expand these before sending.
                if (((InternetAddress) addresses[i]).isGroup()) {
                    haveGroup = true;
                }
            } else {
                throw new MessagingException("Illegal InternetAddress " + addresses[i]);
            }
        }

        // did we find a group? Time to expand this into our full target list.
        if (haveGroup) {
            addresses = expandGroups(addresses);
        }

        SendStatus[] stats = new SendStatus[addresses.length];

        // create our lists for notification and exception reporting.
        Address[] sent = null;
        Address[] unsent = null;
        Address[] invalid = null;

        try {
            // send sender first. If this failed, send a failure notice of the
            // event, using the full list of
            // addresses as the unsent, and nothing for the rest.
            if (!sendMailFrom(message)) {
                unsent = addresses;
                sent = new Address[0];
                invalid = new Address[0];
                // notify of the error.
                notifyTransportListeners(TransportEvent.MESSAGE_NOT_DELIVERED, sent, unsent, invalid, message);

                // include the reponse information here.
                SMTPReply last = lastServerResponse;
                // now send an "uber-exception" to indicate the failure.
                throw new SMTPSendFailedException("MAIL FROM", last.getCode(), last.getMessage(), null, sent, unsent,
                        invalid);
            }

            String dsn = null;

            // there's an optional notification argument that can be added to
            // MAIL TO. See if we've been
            // provided with one.

            // an SMTPMessage object is the first source
            if (message instanceof SMTPMessage) {
                // get the notification options
                int options = ((SMTPMessage) message).getNotifyOptions();

                switch (options) {
                // a zero value indicates nothing is set.
                case 0:
                    break;

                case SMTPMessage.NOTIFY_NEVER:
                    dsn = "NEVER";
                    break;

                case SMTPMessage.NOTIFY_SUCCESS:
                    dsn = "SUCCESS";
                    break;

                case SMTPMessage.NOTIFY_FAILURE:
                    dsn = "FAILURE";
                    break;

                case SMTPMessage.NOTIFY_DELAY:
                    dsn = "DELAY";
                    break;

                // now for combinations...there are few enough combinations here
                // that we can just handle this in the switch statement rather
                // than have to
                // concatentate everything together.
                case (SMTPMessage.NOTIFY_SUCCESS + SMTPMessage.NOTIFY_FAILURE):
                    dsn = "SUCCESS,FAILURE";
                    break;

                case (SMTPMessage.NOTIFY_SUCCESS + SMTPMessage.NOTIFY_DELAY):
                    dsn = "SUCCESS,DELAY";
                    break;

                case (SMTPMessage.NOTIFY_FAILURE + SMTPMessage.NOTIFY_DELAY):
                    dsn = "FAILURE,DELAY";
                    break;

                case (SMTPMessage.NOTIFY_SUCCESS + SMTPMessage.NOTIFY_FAILURE + SMTPMessage.NOTIFY_DELAY):
                    dsn = "SUCCESS,FAILURE,DELAY";
                    break;
                }
            }

            // if still null, grab a property value (yada, yada, yada...)
            if (dsn == null) {
                dsn = getProtocolProperty(MAIL_SMTP_DSN_NOTIFY);
            }

            // we need to know about any failures once we've gone through the
            // complete list, so keep a
            // failure flag.
            boolean sendFailure = false;

            // event notifcation requires we send lists of successes and
            // failures broken down by category.
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

            // Now we add a MAIL TO record for each recipient. At this point, we
            // just collect
            for (int i = 0; i < addresses.length; i++) {
                InternetAddress target = (InternetAddress) addresses[i];

                // write out the record now.
                SendStatus status = sendRcptTo(target, dsn);
                stats[i] = status;

                switch (status.getStatus()) {
                // successfully sent
                case SendStatus.SUCCESS:
                    sentAddresses.add(target);
                    break;

                // we have an invalid address of some sort, or a general sending
                // error (which we'll
                // interpret as due to an invalid address.
                case SendStatus.INVALID_ADDRESS:
                case SendStatus.GENERAL_ERROR:
                    sendFailure = true;
                    invalidAddresses.add(target);
                    break;

                // good address, but this was a send failure.
                case SendStatus.SEND_FAILURE:
                    sendFailure = true;
                    unsentAddresses.add(target);
                    break;
                }
            }

            // if we had a send failure, then we need to check if we allow
            // partial sends. If not allowed,
            // we abort the send operation now.
            if (sendFailure) {
                // now see how we're configured for this send operation.
                boolean partialSends = false;

                // this can be attached directly to the message.
                if (message instanceof SMTPMessage) {
                    partialSends = ((SMTPMessage) message).getSendPartial();
                }

                // if still false on the message object, check for a property
                // version also
                if (!partialSends) {
                    partialSends = isProtocolPropertyTrue(MAIL_SMTP_SENDPARTIAL);
                }

                // if we're not allowing partial successes or we've failed on
                // all of the addresses, it's
                // time to abort.
                if (!partialSends || sentAddresses.isEmpty()) {
                    // we send along the valid and invalid address lists on the
                    // notifications and
                    // exceptions.
                    // however, since we're aborting the entire send, the
                    // successes need to become
                    // members of the failure list.
                    unsentAddresses.addAll(sentAddresses);

                    // this one is empty.
                    sent = new Address[0];
                    unsent = (Address[]) unsentAddresses.toArray(new Address[0]);
                    invalid = (Address[]) invalidAddresses.toArray(new Address[0]);

                    // go reset our connection so we can process additional
                    // sends.
                    resetConnection();

                    // get a list of chained exceptions for all of the failures.
                    MessagingException failures = generateExceptionChain(stats, false);

                    // now send an "uber-exception" to indicate the failure.
                    throw new SMTPSendFailedException("MAIL TO", 0, "Invalid Address", failures, sent, unsent, invalid);
                }
            }

            try {
                // try to send the data
                sendData(message);
            } catch (MessagingException e) {
                // If there's an error at this point, this is a complete
                // delivery failure.
                // we send along the valid and invalid address lists on the
                // notifications and
                // exceptions.
                // however, since we're aborting the entire send, the successes
                // need to become
                // members of the failure list.
                unsentAddresses.addAll(sentAddresses);

                // this one is empty.
                sent = new Address[0];
                unsent = (Address[]) unsentAddresses.toArray(new Address[0]);
                invalid = (Address[]) invalidAddresses.toArray(new Address[0]);
                // notify of the error.
                notifyTransportListeners(TransportEvent.MESSAGE_NOT_DELIVERED, sent, unsent, invalid, message);
                // send a send failure exception.
                throw new SMTPSendFailedException("DATA", 0, "Send failure", e, sent, unsent, invalid);
            }

            // create our lists for notification and exception reporting from
            // this point on.
            sent = (Address[]) sentAddresses.toArray(new Address[0]);
            unsent = (Address[]) unsentAddresses.toArray(new Address[0]);
            invalid = (Address[]) invalidAddresses.toArray(new Address[0]);

            // if sendFailure is true, we had an error during the address phase,
            // but we had permission to
            // process this as a partial send operation. Now that the data has
            // been sent ok, it's time to
            // report the partial failure.
            if (sendFailure) {
                // notify our listeners of the partial delivery.
                notifyTransportListeners(TransportEvent.MESSAGE_PARTIALLY_DELIVERED, sent, unsent, invalid, message);

                // get a list of chained exceptions for all of the failures (and
                // the successes, if reportSuccess has been
                // turned on).
                MessagingException failures = generateExceptionChain(stats, getReportSuccess());

                // now send an "uber-exception" to indicate the failure.
                throw new SMTPSendFailedException("MAIL TO", 0, "Invalid Address", failures, sent, unsent, invalid);
            }

            // notify our listeners of successful delivery.
            notifyTransportListeners(TransportEvent.MESSAGE_DELIVERED, sent, unsent, invalid, message);

            // we've not had any failures, but we've been asked to report
            // success as an exception. Do
            // this now.
            if (reportSuccess) {
                // generate the chain of success exceptions (we already know
                // there are no failure ones to report).
                MessagingException successes = generateExceptionChain(stats, reportSuccess);
                if (successes != null) {
                    throw successes;
                }
            }
        } catch (SMTPSendFailedException e) {
            // if this is a send failure, we've already handled
            // notifications....just rethrow it.
            throw e;
        } catch (MessagingException e) {
            // notify of the error.
            notifyTransportListeners(TransportEvent.MESSAGE_NOT_DELIVERED, sent, unsent, invalid, message);
            throw e;
        }
    }

    /**
     * Close the connection. On completion, we'll be disconnected from the
     * server and unable to send more data.
     * 
     * @exception MessagingException
     */
    public void close() throws MessagingException {
        // if we're already closed, get outta here.
        if (socket == null) {
            return;
        }
        try {
            // say goodbye
            sendQuit();
        } finally {
            // and close up the connection. We do this in a finally block to
            // make sure the connection
            // is shut down even if quit gets an error.
            closeServerConnection();
        }
    }

    /**
     * Turn a series of send status items into a chain of exceptions indicating
     * the state of each send operation.
     * 
     * @param stats
     *            The list of SendStatus items.
     * @param reportSuccess
     *            Indicates whether we should include the report success items.
     * 
     * @return The head of a chained list of MessagingExceptions.
     */
    protected MessagingException generateExceptionChain(SendStatus[] stats, boolean reportSuccess) {
        MessagingException current = null;

        for (int i = 0; i < stats.length; i++) {
            SendStatus status = stats[i];

            if (status != null) {
                MessagingException nextException = stats[i].getException(reportSuccess);
                // if there's an exception associated with this status, chain it
                // up with the rest.
                if (nextException != null) {
                    if (current == null) {
                        current = nextException;
                    } else {
                        current.setNextException(nextException);
                        current = nextException;
                    }
                }
            }
        }
        return current;
    }

    /**
     * Reset the server connection after an error.
     * 
     * @exception MessagingException
     */
    protected void resetConnection() throws MessagingException {
        // we want the caller to retrieve the last response responsbile for
        // requiring the reset, so save and
        // restore that info around the reset.
        SMTPReply last = lastServerResponse;

        // send a reset command.
        SMTPReply line = sendCommand("RSET");

        // if this did not reset ok, just close the connection
        if (line.getCode() != COMMAND_ACCEPTED) {
            close();
        }
        // restore this.
        lastServerResponse = last;
    }

    /**
     * Expand the address list by converting any group addresses into single
     * address targets.
     * 
     * @param addresses
     *            The input array of addresses.
     * 
     * @return The expanded array of addresses.
     * @exception MessagingException
     */
    protected Address[] expandGroups(Address[] addresses) throws MessagingException {
        ArrayList expandedAddresses = new ArrayList();

        // run the list looking for group addresses, and add the full group list
        // to our targets.
        for (int i = 0; i < addresses.length; i++) {
            InternetAddress address = (InternetAddress) addresses[i];
            // not a group? Just copy over to the other list.
            if (!address.isGroup()) {
                expandedAddresses.add(address);
            } else {
                // get the group address and copy each member of the group into
                // the expanded list.
                InternetAddress[] groupAddresses = address.getGroup(true);
                for (int j = 1; j < groupAddresses.length; j++) {
                    expandedAddresses.add(groupAddresses[j]);
                }
            }
        }

        // convert back into an array.
        return (Address[]) expandedAddresses.toArray(new Address[0]);
    }

    /**
     * Create a transport connection object and connect it to the target server.
     * 
     * @param host
     *            The target server host.
     * @param port
     *            The connection port.
     * 
     * @exception MessagingException
     */
    protected void getConnection(String host, int port, String username, String password) throws IOException {
        this.host = host;
        this.port = port;
        this.username = username;
        this.password = password;
        // and see if STARTTLS is enabled.
        useTLS = isProtocolPropertyTrue(MAIL_SMTP_TLS);
        serverAuthenticationMechanisms = new HashMap();
        // We might have been passed a socket to connect with...if not, we need
        // to create one of the correct type.
        if (socket == null) {
            // if this is the "smtps" protocol, we start with an SSLSocket
            if (sslConnection) {
                getConnectedSSLSocket();
            } else {
                getConnectedSocket();
            }
        }
        // if we already have a socket, get some information from it and
        // override what we've been passed.
        else {
            port = socket.getPort();
            host = socket.getInetAddress().getHostName();
        }
        // now set up the input/output streams.
        inputStream = new TraceInputStream(socket.getInputStream(), debugStream, debug,
                isProtocolPropertyTrue(MAIL_SMTP_ENCODE_TRACE));
        ;
        outputStream = new TraceOutputStream(socket.getOutputStream(), debugStream, debug,
                isProtocolPropertyTrue(MAIL_SMTP_ENCODE_TRACE));
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
    protected String getProtocolProperty(String name) {
        // the name we're given is the least qualified part of the name. We
        // construct the full property name
        // using the protocol (either "smtp" or "smtps").
        String fullName = "mail." + protocol + "." + name;
        return getSessionProperty(fullName);
    }

    /**
     * Get a property associated with this mail session.
     * 
     * @param name
     *            The name of the property.
     * 
     * @return The property value (returns null if the property has not been
     *         set).
     */
    protected String getSessionProperty(String name) {
        return session.getProperty(name);
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
    protected String getSessionProperty(String name, String defaultValue) {
        String result = session.getProperty(name);
        if (result == null) {
            return defaultValue;
        }
        return result;
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
    protected String getProtocolProperty(String name, String defaultValue) {
        // the name we're given is the least qualified part of the name. We
        // construct the full property name
        // using the protocol (either "smtp" or "smtps").
        String fullName = "mail." + protocol + "." + name;
        return getSessionProperty(fullName, defaultValue);
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
    protected int getIntSessionProperty(String name, int defaultValue) {
        String result = getSessionProperty(name);
        if (result != null) {
            try {
                // convert into an int value.
                return Integer.parseInt(result);
            } catch (NumberFormatException e) {
            }
        }
        // return default value if it doesn't exist is isn't convertable.
        return defaultValue;
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
    protected int getIntProtocolProperty(String name, int defaultValue) {
        // the name we're given is the least qualified part of the name. We
        // construct the full property name
        // using the protocol (either "smtp" or "smtps").
        String fullName = "mail." + protocol + "." + name;
        return getIntSessionProperty(fullName, defaultValue);
    }

    /**
     * Process a session property as a boolean value, returning either true or
     * false.
     * 
     * @return True if the property value is "true". Returns false for any other
     *         value (including null).
     */
    protected boolean isProtocolPropertyTrue(String name) {
        // the name we're given is the least qualified part of the name. We
        // construct the full property name
        // using the protocol (either "smtp" or "smtps").
        String fullName = "mail." + protocol + "." + name;
        return isSessionPropertyTrue(fullName);
    }

    /**
     * Process a session property as a boolean value, returning either true or
     * false.
     * 
     * @return True if the property value is "true". Returns false for any other
     *         value (including null).
     */
    protected boolean isSessionPropertyTrue(String name) {
        String property = session.getProperty(name);
        if (property != null) {
            return property.equals("true");
        }
        return false;
    }

    /**
     * Process a session property as a boolean value, returning either true or
     * false.
     * 
     * @return True if the property value is "false". Returns false for other
     *         value (including null).
     */
    protected boolean isSessionPropertyFalse(String name) {
        String property = session.getProperty(name);
        if (property != null) {
            return property.equals("false");
        }
        return false;
    }

    /**
     * Process a session property as a boolean value, returning either true or
     * false.
     * 
     * @return True if the property value is "false". Returns false for other
     *         value (including null).
     */
    protected boolean isProtocolPropertyFalse(String name) {
        // the name we're given is the least qualified part of the name. We
        // construct the full property name
        // using the protocol (either "smtp" or "smtps").
        String fullName = "mail." + protocol + "." + name;
        return isSessionPropertyTrue(fullName);
    }

    /**
     * Close the server connection at termination.
     */
    protected void closeServerConnection() {
        try {
            socket.close();
        } catch (IOException ignored) {
        }

        socket = null;
        inputStream = null;
        outputStream = null;
    }

    /**
     * Creates a connected socket
     * 
     * @exception MessagingException
     */
    protected void getConnectedSocket() throws IOException {
        if (debug) {
            debugOut("Attempting plain socket connection to server " + host + ":" + port);
        }

        // the socket factory can be specified via a session property. By
        // default, we just directly
        // instantiate a socket without using a factor.
        String socketFactory = getProtocolProperty(MAIL_SMTP_FACTORY_CLASS);

        // there are several protocol properties that can be set to tune the
        // created socket. We need to
        // retrieve those bits before creating the socket.
        int timeout = getIntProtocolProperty(MAIL_SMTP_TIMEOUT, -1);
        InetAddress localAddress = null;
        // see if we have a local address override.
        String localAddrProp = getProtocolProperty(MAIL_SMTP_LOCALADDRESS);
        if (localAddrProp != null) {
            localAddress = InetAddress.getByName(localAddrProp);
        }

        // check for a local port...default is to allow socket to choose.
        int localPort = getIntProtocolProperty(MAIL_SMTP_LOCALPORT, 0);

        socket = null;

        // if there is no socket factory defined (normal), we just create a
        // socket directly.
        if (socketFactory == null) {
            socket = new Socket(host, port, localAddress, localPort);
        }

        else {
            try {
                int socketFactoryPort = getIntProtocolProperty(MAIL_SMTP_FACTORY_PORT, -1);

                // we choose the port used by the socket based on overrides.
                Integer portArg = new Integer(socketFactoryPort == -1 ? port : socketFactoryPort);

                // use the current context loader to resolve this.
                ClassLoader loader = Thread.currentThread().getContextClassLoader();
                Class factoryClass = loader.loadClass(socketFactory);

                // done indirectly, we need to invoke the method using
                // reflection.
                // This retrieves a factory instance.
                Method getDefault = factoryClass.getMethod("getDefault", new Class[0]);
                Object defFactory = getDefault.invoke(new Object(), new Object[0]);

                // now that we have the factory, there are two different
                // createSocket() calls we use,
                // depending on whether we have a localAddress override.

                if (localAddress != null) {
                    // retrieve the createSocket(String, int, InetAddress, int)
                    // method.
                    Class[] createSocketSig = new Class[] { String.class, Integer.TYPE, InetAddress.class, Integer.TYPE };
                    Method createSocket = factoryClass.getMethod("createSocket", createSocketSig);

                    Object[] createSocketArgs = new Object[] { host, portArg, localAddress, new Integer(localPort) };
                    socket = (Socket) createSocket.invoke(defFactory, createSocketArgs);
                } else {
                    // retrieve the createSocket(String, int) method.
                    Class[] createSocketSig = new Class[] { String.class, Integer.TYPE };
                    Method createSocket = factoryClass.getMethod("createSocket", createSocketSig);

                    Object[] createSocketArgs = new Object[] { host, portArg };
                    socket = (Socket) createSocket.invoke(defFactory, createSocketArgs);
                }
            } catch (Throwable e) {
                // if a socket factor is specified, then we may need to fall
                // back to a default. This behavior
                // is controlled by (surprise) more session properties.
                if (isProtocolPropertyTrue(MAIL_SMTP_FACTORY_FALLBACK)) {
                    if (debug) {
                        debugOut("First plain socket attempt faile, falling back to default factory", e);
                    }
                    socket = new Socket(host, port, localAddress, localPort);
                }
                // we have an exception. We're going to throw an IOException,
                // which may require unwrapping
                // or rewrapping the exception.
                else {
                    // we have an exception from the reflection, so unwrap the
                    // base exception
                    if (e instanceof InvocationTargetException) {
                        e = ((InvocationTargetException) e).getTargetException();
                    }

                    if (debug) {
                        debugOut("Plain socket creation failure", e);
                    }

                    // throw this as an IOException, with the original exception
                    // attached.
                    IOException ioe = new IOException("Error connecting to " + host + ", " + port);
                    ioe.initCause(e);
                    throw ioe;
                }
            }
        }

        if (timeout >= 0) {
            socket.setSoTimeout(timeout);
        }
    }

    /**
     * Creates a connected SSL socket for an initial SSL connection.
     * 
     * @exception MessagingException
     */
    protected void getConnectedSSLSocket() throws IOException {
        if (debug) {
            debugOut("Attempting SSL socket connection to server " + host + ":" + port);
        }
        // the socket factory can be specified via a protocol property, a
        // session property, and if all else
        // fails (which it usually does), we fall back to the standard factory
        // class.
        String socketFactory = getProtocolProperty(MAIL_SMTP_FACTORY_CLASS, getSessionProperty(MAIL_SSLFACTORY_CLASS,
                "javax.net.ssl.SSLSocketFactory"));

        // there are several protocol properties that can be set to tune the
        // created socket. We need to
        // retrieve those bits before creating the socket.
        int timeout = getIntProtocolProperty(MAIL_SMTP_TIMEOUT, -1);
        InetAddress localAddress = null;
        // see if we have a local address override.
        String localAddrProp = getProtocolProperty(MAIL_SMTP_LOCALADDRESS);
        if (localAddrProp != null) {
            localAddress = InetAddress.getByName(localAddrProp);
        }

        // check for a local port...default is to allow socket to choose.
        int localPort = getIntProtocolProperty(MAIL_SMTP_LOCALPORT, 0);

        socket = null;

        // if there is no socket factory defined (normal), we just create a
        // socket directly.
        if (socketFactory == null) {
            socket = new Socket(host, port, localAddress, localPort);
        }

        else {
            // we'll try this with potentially two different factories if we're
            // allowed to fall back.
            boolean fallback = isProtocolPropertyTrue(MAIL_SMTP_FACTORY_FALLBACK);

            while (true) {
                try {
                    if (debug) {
                        debugOut("Creating SSL socket using factory " + socketFactory);
                    }

                    int socketFactoryPort = getIntProtocolProperty(MAIL_SMTP_FACTORY_PORT, -1);

                    // we choose the port used by the socket based on overrides.
                    Integer portArg = new Integer(socketFactoryPort == -1 ? port : socketFactoryPort);

                    // use the current context loader to resolve this.
                    ClassLoader loader = Thread.currentThread().getContextClassLoader();
                    Class factoryClass = loader.loadClass(socketFactory);

                    // done indirectly, we need to invoke the method using
                    // reflection.
                    // This retrieves a factory instance.
                    Method getDefault = factoryClass.getMethod("getDefault", new Class[0]);
                    Object defFactory = getDefault.invoke(new Object(), new Object[0]);

                    // now that we have the factory, there are two different
                    // createSocket() calls we use,
                    // depending on whether we have a localAddress override.

                    if (localAddress != null) {
                        // retrieve the createSocket(String, int, InetAddress,
                        // int) method.
                        Class[] createSocketSig = new Class[] { String.class, Integer.TYPE, InetAddress.class,
                                Integer.TYPE };
                        Method createSocket = factoryClass.getMethod("createSocket", createSocketSig);

                        Object[] createSocketArgs = new Object[] { host, portArg, localAddress, new Integer(localPort) };
                        socket = (Socket) createSocket.invoke(defFactory, createSocketArgs);
                    } else {
                        // retrieve the createSocket(String, int) method.
                        Class[] createSocketSig = new Class[] { String.class, Integer.TYPE };
                        Method createSocket = factoryClass.getMethod("createSocket", createSocketSig);

                        Object[] createSocketArgs = new Object[] { host, portArg };
                        socket = (Socket) createSocket.invoke(defFactory, createSocketArgs);
                    }
                } catch (Throwable e) {
                    // if we're allowed to fallback, then use the default
                    // factory and try this again. We only
                    // allow this to happen once.
                    if (fallback) {
                        if (debug) {
                            debugOut("First attempt at creating SSL socket failed, falling back to default factory");
                        }
                        socketFactory = "javax.net.ssl.SSLSocketFactory";
                        fallback = false;
                        continue;
                    }
                    // we have an exception. We're going to throw an
                    // IOException, which may require unwrapping
                    // or rewrapping the exception.
                    else {
                        // we have an exception from the reflection, so unwrap
                        // the base exception
                        if (e instanceof InvocationTargetException) {
                            e = ((InvocationTargetException) e).getTargetException();
                        }

                        if (debug) {
                            debugOut("Failure creating SSL socket", e);
                        }

                        // throw this as an IOException, with the original
                        // exception attached.
                        IOException ioe = new IOException("Error connecting to " + host + ", " + port);
                        ioe.initCause(e);
                        throw ioe;
                    }
                }
            }
        }

        if (timeout >= 0) {
            socket.setSoTimeout(timeout);
        }
    }

    /**
     * Switch the connection to using TLS level security, switching to an SSL
     * socket.
     */
    protected void getConnectedTLSSocket() throws MessagingException {
        if (debug) {
            debugOut("Attempting to negotiate STARTTLS with server " + host);
        }
        // tell the server of our intention to start a TLS session
        SMTPReply line = sendCommand("STARTTLS");

        if (line.getCode() != SERVICE_READY) {
            if (debug) {
                debugOut("STARTTLS command rejected by SMTP server " + host);
            }
            throw new MessagingException("Unable to make TLS server connection");
        }
        // it worked, now switch the socket into TLS mode
        try {

            // we use the same target and port as the current connection.
            String host = socket.getInetAddress().getHostName();
            int port = socket.getPort();

            // the socket factory can be specified via a session property. By
            // default, we use
            // the native SSL factory.
            String socketFactory = getProtocolProperty(MAIL_SMTP_FACTORY_CLASS, "javax.net.ssl.SSLSocketFactory");

            // use the current context loader to resolve this.
            ClassLoader loader = Thread.currentThread().getContextClassLoader();
            Class factoryClass = loader.loadClass(socketFactory);

            // done indirectly, we need to invoke the method using reflection.
            // This retrieves a factory instance.
            Method getDefault = factoryClass.getMethod("getDefault", new Class[0]);
            Object defFactory = getDefault.invoke(new Object(), new Object[0]);

            // now we need to invoke createSocket()
            Class[] createSocketSig = new Class[] { Socket.class, String.class, Integer.TYPE, Boolean.TYPE };
            Method createSocket = factoryClass.getMethod("createSocket", createSocketSig);

            Object[] createSocketArgs = new Object[] { socket, host, new Integer(port), Boolean.TRUE };

            // and finally create the socket
            Socket sslSocket = (Socket) createSocket.invoke(defFactory, createSocketArgs);

            // if this is an instance of SSLSocket (very common), try setting
            // the protocol to be
            // "TLSv1". If this is some other class because of a factory
            // override, we'll just have to
            // accept that things will work.
            if (sslSocket instanceof SSLSocket) {
                ((SSLSocket) sslSocket).setEnabledProtocols(new String[] { "TLSv1" });
                ((SSLSocket) sslSocket).setUseClientMode(true);
                ((SSLSocket) sslSocket).startHandshake();
            }

            // and finally, as a last step, replace our input streams with the
            // secure ones.
            // now set up the input/output streams.
            inputStream = new TraceInputStream(sslSocket.getInputStream(), debugStream, debug,
                    isProtocolPropertyTrue(MAIL_SMTP_ENCODE_TRACE));
            ;
            outputStream = new TraceOutputStream(sslSocket.getOutputStream(), debugStream, debug,
                    isProtocolPropertyTrue(MAIL_SMTP_ENCODE_TRACE));
            // this is our active socket now
            socket = sslSocket;

        } catch (Exception e) {
            if (debug) {
                debugOut("Failure attempting to convert connection to TLS", e);
            }
            throw new MessagingException("Unable to convert connection to SSL", e);
        }
    }

    /**
     * Get the servers welcome blob from the wire....
     */
    protected boolean getWelcome() throws MessagingException {
        SMTPReply line = getReply();
        return !line.isError();
    }

    /**
     * Sends the data in the message down the socket. This presumes the server
     * is in the right place and ready for getting the DATA message and the data
     * right place in the sequence
     */
    protected void sendData(Message msg) throws MessagingException {

        // send the DATA command
        SMTPReply line = sendCommand("DATA");

        if (line.isError()) {
            throw new MessagingException("Error issuing SMTP 'DATA' command: " + line);
        }

        // now the data... I could look at the type, but
        try {
            // the data content has two requirements we need to meet by
            // filtering the
            // output stream. Requirement 1 is to conicalize any line breaks.
            // All line
            // breaks will be transformed into properly formed CRLF sequences.
            //
            // Requirement 2 is to perform byte-stuff for any line that begins
            // with a "."
            // so that data is not confused with the end-of-data marker (a
            // "\r\n.\r\n" sequence.
            //
            // The MIME output stream performs those two functions on behalf of
            // the content
            // writer.
            OutputStream mimeOut = new MIMEOutputStream(outputStream);

            msg.writeTo(mimeOut);
            mimeOut.flush();
        } catch (IOException e) {
            throw new MessagingException(e.toString());
        } catch (MessagingException e) {
            throw new MessagingException(e.toString());
        }

        // now to finish, we send a CRLF sequence, followed by a ".".
        sendLine("");
        sendLine(".");

        // use a longer time out here to give the server time to process the
        // data.
        try {
            line = new SMTPReply(receiveLine(TIMEOUT * 2));
        } catch (MalformedSMTPReplyException e) {
            throw new MessagingException(e.toString());
        } catch (MessagingException e) {
            throw new MessagingException(e.toString());
        }

        if (line.isError()) {
            throw new MessagingException("Error issuing SMTP 'DATA' command: " + line);
        }
    }

    /**
     * Sends the QUIT message and receieves the response
     */
    protected void sendQuit() throws MessagingException {
        // there's yet another property that controls whether we should wait for
        // a
        // reply for a QUIT command. If on, just send the command and get outta
        // here.
        if (isProtocolPropertyTrue(MAIL_SMTP_QUITWAIT)) {
            sendLine("QUIT");
        } else {
            // handle as a real command...we're going to ignore the response.
            sendCommand("QUIT");
        }
    }

    /**
     * Sets a receiver address for the current message
     * 
     * @param addr
     *            The target address.
     * @param dsn
     *            An optional notification address appended to the MAIL command.
     * 
     * @return The status for this particular send operation.
     * @exception MessagingException
     */
    protected SendStatus sendRcptTo(InternetAddress addr, String dsn) throws MessagingException {
        // compose the command using the fixed up email address. Normally, this
        // involves adding
        // "<" and ">" around the address.

        StringBuffer command = new StringBuffer();

        // compose the first part of the command
        command.append("RCPT TO: ");
        command.append(fixEmailAddress(addr.getAddress()));

        // if we have DSN information, append it to the command.
        if (dsn != null) {
            command.append(" NOTIFY=");
            command.append(dsn);
        }

        // get a string version of this command.
        String commandString = command.toString();

        SMTPReply line = sendCommand(commandString);

        switch (line.getCode()) {
        // these two are both successful transmissions
        case COMMAND_ACCEPTED:
        case ADDRESS_NOT_LOCAL:
            // we get out of here with the status information.
            return new SendStatus(SendStatus.SUCCESS, addr, commandString, line);

        // these are considered invalid address errors
        case PARAMETER_SYNTAX_ERROR:
        case INVALID_COMMAND_SEQUENCE:
        case MAILBOX_NOT_FOUND:
        case INVALID_MAILBOX:
        case USER_NOT_LOCAL:
            // we get out of here with the status information.
            return new SendStatus(SendStatus.INVALID_ADDRESS, addr, commandString, line);

        // the command was valid, but something went wrong in the server.
        case SERVICE_NOT_AVAILABLE:
        case MAILBOX_BUSY:
        case PROCESSING_ERROR:
        case INSUFFICIENT_STORAGE:
        case MAILBOX_FULL:
            // we get out of here with the status information.
            return new SendStatus(SendStatus.SEND_FAILURE, addr, commandString, line);

        // everything else is considered really bad...
        default:
            // we get out of here with the status information.
            return new SendStatus(SendStatus.GENERAL_ERROR, addr, commandString, line);
        }
    }

    /**
     * Set the sender for this mail.
     * 
     * @param message
     *            The message we're sending.
     * 
     * @exception MessagingException
     */
    protected boolean sendMailFrom(Message message) throws MessagingException {

        // need to sort the from value out from a variety of sources.
        String from = null;

        // first potential source is from the message itself, if it's an
        // instance of SMTPMessage.
        if (message instanceof SMTPMessage) {
            from = ((SMTPMessage) message).getEnvelopeFrom();
        }

        // if not available from the message, check the protocol property next
        if (from == null || from.length() == 0) {
            // the from value can be set explicitly as a property
            from = getProtocolProperty(MAIL_SMTP_FROM);
        }

        // if not there, see if we have something in the message header.
        if (from == null || from.length() == 0) {
            Address[] fromAddresses = message.getFrom();

            // if we have some addresses in the header, then take the first one
            // as our From: address
            if (fromAddresses != null && fromAddresses.length > 0) {
                from = ((InternetAddress) fromAddresses[0]).getAddress();
            }
            // get what the InternetAddress class believes to be the local
            // address.
            else {
                from = InternetAddress.getLocalAddress(session).getAddress();
            }
        }

        if (from == null || from.length() == 0) {
            throw new MessagingException("no FROM address");
        }

        StringBuffer command = new StringBuffer();

        // start building up the command
        command.append("MAIL FROM: ");
        command.append(fixEmailAddress(from));

        // does this server support Delivery Status Notification? Then we may
        // need to add some extra to the command.
        if (supportsExtension("DSN")) {
            String returnNotification = null;

            // the return notification stuff might be set as value on the
            // message object itself.
            if (message instanceof SMTPMessage) {
                // we need to convert the option into a string value.
                switch (((SMTPMessage) message).getReturnOption()) {
                case SMTPMessage.RETURN_FULL:
                    returnNotification = "FULL";
                    break;

                case SMTPMessage.RETURN_HDRS:
                    returnNotification = "HDRS";
                    break;
                }
            }

            // if not obtained from the message object, it can also be set as a
            // property.
            if (returnNotification == null) {
                // the DSN value is set by yet another property.
                returnNotification = getProtocolProperty(MAIL_SMTP_DSN_RET);
            }

            // if we have a target, add the notification stuff to our FROM
            // command.
            if (returnNotification != null) {
                command.append(" RET=");
                command.append(returnNotification);
            }
        }

        // if this server supports AUTH and we have submitter information, then
        // we also add the
        // "AUTH=" keyword to the MAIL FROM command (see RFC 2554).

        if (supportsExtension("AUTH")) {
            String submitter = null;

            // another option that can be specified on the message object.
            if (message instanceof SMTPMessage) {
                submitter = ((SMTPMessage) message).getSubmitter();
            }
            // if not part of the object, try for a propery version.
            if (submitter == null) {
                // we only send the extra keyword is a submitter is specified.
                submitter = getProtocolProperty(MAIL_SMTP_SUBMITTER);
            }
            // we have one...add the keyword, plus the submitter info in xtext
            // format (defined by RFC 1891).
            if (submitter != null) {
                command.append(" AUTH=");
                try {
                    // add this encoded
                    command.append(new String(XText.encode(submitter.getBytes("US-ASCII"))));
                } catch (UnsupportedEncodingException e) {
                    throw new MessagingException("Invalid submitter value " + submitter);
                }
            }
        }

        String extension = null;

        // now see if we need to add any additional extension info to this
        // command. The extension is not
        // checked for validity. That's the reponsibility of the caller.
        if (message instanceof SMTPMessage) {
            extension = ((SMTPMessage) message).getMailExtension();
        }
        // this can come either from the object or from a set property.
        if (extension == null) {
            extension = getProtocolProperty(MAIL_SMTP_EXTENSION);
        }

        // have something real to add?
        if (extension != null && extension.length() != 0) {
            // tack this on the end with a blank delimiter.
            command.append(' ');
            command.append(extension);
        }

        // and finally send the command
        SMTPReply line = sendCommand(command.toString());

        // 250 response indicates success.
        return line.getCode() == COMMAND_ACCEPTED;
    }

    /**
     * Send a command to the server, returning the first response line back as a
     * reply.
     * 
     * @param data
     *            The data to send.
     * 
     * @return A reply object with the reply line.
     * @exception MessagingException
     */
    protected SMTPReply sendCommand(String data) throws MessagingException {
        sendLine(data);
        return getReply();
    }

    /**
     * Sends a message down the socket and terminates with the appropriate CRLF
     */
    protected void sendLine(String data) throws MessagingException {
        if (socket == null || !socket.isConnected()) {
            throw new MessagingException("no connection");
        }
        try {
            outputStream.write(data.getBytes());
            outputStream.write(CR);
            outputStream.write(LF);
            outputStream.flush();
        } catch (IOException e) {
            throw new MessagingException(e.toString());
        }
    }

    /**
     * Receives one line from the server. A line is a sequence of bytes
     * terminated by a CRLF
     * 
     * @return the line from the server as String
     */
    protected String receiveLine() throws MessagingException {
        return receiveLine(TIMEOUT);
    }

    /**
     * Get a reply line for an SMTP command.
     * 
     * @return An SMTP reply object from the stream.
     */
    protected SMTPReply getReply() throws MessagingException {
        try {
            lastServerResponse = new SMTPReply(receiveLine());
        } catch (MalformedSMTPReplyException e) {
            throw new MessagingException(e.toString());
        } catch (MessagingException e) {
            throw e;
        }
        return lastServerResponse;
    }

    /**
     * Retrieve the last response received from the SMTP server.
     * 
     * @return The raw response string (including the error code) returned from
     *         the SMTP server.
     */
    public String getLastServerResponse() {
        if (lastServerResponse == null) {
            return "";
        }
        return lastServerResponse.getReply();
    }

    /**
     * Receives one line from the server. A line is a sequence of bytes
     * terminated by a CRLF
     * 
     * @return the line from the server as String
     */
    protected String receiveLine(int delayMillis) throws MessagingException {
        if (socket == null || !socket.isConnected()) {
            throw new MessagingException("no connection");
        }

        int timeout = 0;

        try {
            // for now, read byte for byte, looking for a CRLF
            timeout = socket.getSoTimeout();

            socket.setSoTimeout(delayMillis);

            StringBuffer buff = new StringBuffer();

            int c;
            boolean crFound = false, lfFound = false;

            while ((c = inputStream.read()) != -1 && crFound == false && lfFound == false) {
                // we're looking for a CRLF sequence, so mark each one as seen.
                // Any other
                // character gets appended to the end of the buffer.
                if (c == CR) {
                    crFound = true;
                } else if (c == LF) {
                    lfFound = true;
                } else {
                    buff.append((char) c);
                }
            }

            String line = buff.toString();
            return line;

        } catch (SocketException e) {
            throw new MessagingException(e.toString());
        } catch (IOException e) {
            throw new MessagingException(e.toString());
        } finally {
            try {
                socket.setSoTimeout(timeout);
            } catch (SocketException e) {
                // ignore - was just trying to do the decent thing...
            }
        }
    }

    /**
     * Convert an InternetAddress into a form sendable on an SMTP mail command.
     * InternetAddress.getAddress() generally returns just the address portion
     * of the full address, minus route address markers. We need to ensure we
     * have an address with '<' and '>' delimiters.
     * 
     * @param mail
     *            The mail address returned from InternetAddress.getAddress().
     * 
     * @return A string formatted for sending.
     */
    protected String fixEmailAddress(String mail) {
        if (mail.charAt(0) == '<') {
            return mail;
        }
        return "<" + mail + ">";
    }

    /**
     * Start the handshake process with the server, including setting up and
     * TLS-level work. At the completion of this task, we should be ready to
     * authenticate with the server, if needed.
     */
    protected boolean sendHandshake() throws MessagingException {
        // check to see what sort of initial handshake we need to make.
        boolean useEhlo = !isProtocolPropertyFalse(MAIL_SMTP_EHLO);

        // if we're to use Ehlo, send it and then fall back to just a HELO
        // message if it fails.
        if (useEhlo) {
            if (!sendEhlo()) {
                sendHelo();
            }
        } else {
            // send the initial hello response.
            sendHelo();
        }

        if (useTLS) {
            // if we've been told to use TLS, and this server doesn't support
            // it, then this is a failure
            if (!serverTLS) {
                throw new MessagingException("Server doesn't support required transport level security");
            }
            // if the server supports TLS, then use it for the connection.
            // on our connection.
            getConnectedTLSSocket();

            // some servers (gmail is one that I know of) only send a STARTTLS
            // extension message on the
            // first EHLO command. Now that we have the TLS handshaking
            // established, we need to send a
            // second EHLO message to retrieve the AUTH records from the server.
            serverAuthenticationMechanisms.clear();
            if (!sendEhlo()) {
                throw new MessagingException("Failure sending EHLO command to SMTP server");
            }
        }

        // this worked.
        return true;
    }

    /**
     * Send the EHLO command to the SMTP server.
     * 
     * @return True if the command was accepted ok, false for any errors.
     * @exception SMTPTransportException
     * @exception MalformedSMTPReplyException
     * @exception MessagingException
     */
    protected boolean sendEhlo() throws MessagingException {
        sendLine("EHLO " + getLocalHost());

        SMTPReply line = getReply();

        // we get a 250 code back. The first line is just a greeting, and
        // extensions are identifed on
        // continuations. If this fails, then we'll try once more with HELO to
        // establish bona fides.
        if (line.getCode() != COMMAND_ACCEPTED) {
            return false;
        }

        // get a fresh extension mapping table.
        serverExtensionArgs = new HashMap();

        // process all of the continuation lines
        while (line.isContinued()) {
            // get the next line
            line = getReply();
            if (line.getCode() != COMMAND_ACCEPTED) {
                // all EHLO failures go back to the HELO failback step.
                return false;
            }
            // go process the extention
            processExtension(line.getMessage());
        }
        return true;
    }

    /**
     * Send the HELO command to the SMTP server.
     * 
     * @exception MessagingException
     */
    protected void sendHelo() throws MessagingException {
        sendLine("HELO " + getLocalHost());

        SMTPReply line = getReply();

        // we get a 250 code back. The first line is just a greeting, and
        // extensions are identifed on
        // continuations. If this fails, then we'll try once more with HELO to
        // establish bona fides.
        if (line.getCode() != COMMAND_ACCEPTED) {
            throw new MessagingException("Failure sending HELO command to SMTP server");
        }
    }

    /**
     * Retrieve the local client host name.
     * 
     * @return The string version of the local host name.
     * @exception SMTPTransportException
     */
    public String getLocalHost() throws MessagingException {
        if (localHost == null) {

            try {
                localHost = InetAddress.getLocalHost().getHostName();
            } catch (UnknownHostException e) {
                // fine, we're misconfigured - ignore
            }

            if (localHost == null) {
                localHost = getProtocolProperty(MAIL_SMTP_LOCALHOST);
            }

            if (localHost == null) {
                localHost = getSessionProperty(MAIL_LOCALHOST);
            }

            if (localHost == null) {
                throw new MessagingException("Can't get local hostname. "
                        + " Please correctly configure JDK/DNS or set mail.smtp.localhost");
            }
        }

        return localHost;
    }

    /**
     * Return the current reportSuccess property.
     * 
     * @return The current reportSuccess property.
     */
    public boolean getReportSuccess() {
        return reportSuccess;
    }

    /**
     * Set a new value for the reportSuccess property.
     * 
     * @param report
     *            The new setting.
     */
    public void setReportSuccess(boolean report) {
        reportSuccess = report;
    }

    /**
     * Return the current startTLS property.
     * 
     * @return The current startTLS property.
     */
    public boolean getStartTLS() {
        return reportSuccess;
    }

    /**
     * Set a new value for the startTLS property.
     * 
     * @param start
     *            The new setting.
     */
    public void setStartTLS(boolean start) {
        useTLS = start;
    }

    /**
     * Retrieve the SASL realm used for DIGEST-MD5 authentication. This will
     * either be explicitly set, or retrieved using the mail.smtp.sasl.realm
     * session property.
     * 
     * @return The current realm information (which can be null).
     */
    public String getSASLRealm() {
        // if the realm is null, retrieve it using the realm session property.
        if (realm == null) {
            realm = getProtocolProperty(MAIL_SMTP_SASL_REALM);
        }
        return realm;
    }

    /**
     * Explicitly set the SASL realm used for DIGEST-MD5 authenticaiton.
     * 
     * @param name
     *            The new realm name.
     */
    public void setSASLRealm(String name) {
        realm = name;
    }

    /**
     * Explicitly set the local host information.
     * 
     * @param localHost
     *            The new localHost name.
     */
    public void setLocalHost(String localHost) {
        this.localHost = localHost;
    }

    /**
     * Process an extension string passed back as the EHLP response.
     * 
     * @param extension
     *            The string value of the extension (which will be of the form
     *            "NAME arguments").
     */
    protected void processExtension(String extension) {
        String extensionName = extension.toUpperCase();
        String argument = "";

        int delimiter = extension.indexOf(' ');
        // if we have a keyword with arguments, parse them out and add to the
        // argument map.
        if (delimiter != -1) {
            extensionName = extension.substring(0, delimiter).toUpperCase();
            argument = extension.substring(delimiter + 1);
        }

        // add this to the map so it can be tested later.
        serverExtensionArgs.put(extensionName, argument);

        // process a few special ones that don't require extra parsing.
        // AUTH and AUTH=LOGIN are handled the same
        if (extensionName.equals("AUTH")) {
            // if we don't have an argument on AUTH, this means LOGIN.
            if (argument == null) {
                serverAuthenticationMechanisms.put("LOGIN", "LOGIN");
            } else {
                // The security mechanisms are blank delimited tokens.
                StringTokenizer tokenizer = new StringTokenizer(argument);

                while (tokenizer.hasMoreTokens()) {
                    String mechanism = tokenizer.nextToken().toUpperCase();
                    serverAuthenticationMechanisms.put(mechanism, mechanism);
                }
            }
        }
        // special case for some older servers.
        else if (extensionName.equals("AUTH=LOGIN")) {
            serverAuthenticationMechanisms.put("LOGIN", "LOGIN");
        }
        // does this support transport level security?
        else if (extensionName.equals("STARTTLS")) {
            // flag this for later
            serverTLS = true;
        }
    }

    /**
     * Retrieve any argument information associated with a extension reported
     * back by the server on the EHLO command.
     * 
     * @param name
     *            The name of the target server extension.
     * 
     * @return Any argument passed on a server extension. Returns null if the
     *         extension did not include an argument or the extension was not
     *         supported.
     */
    public String extensionParameter(String name) {
        if (serverExtensionArgs != null) {
            return (String) serverExtensionArgs.get(name);
        }
        return null;
    }

    /**
     * Tests whether the target server supports a named extension.
     * 
     * @param name
     *            The target extension name.
     * 
     * @return true if the target server reported on the EHLO command that is
     *         supports the targer server, false if the extension was not
     *         supported.
     */
    public boolean supportsExtension(String name) {
        // this only returns null if we don't have this extension
        return extensionParameter(name) != null;
    }

    /**
     * Determine if the target server supports a given authentication mechanism.
     * 
     * @param mechanism
     *            The mechanism name.
     * 
     * @return true if the server EHLO response indicates it supports the
     *         mechanism, false otherwise.
     */
    protected boolean supportsAuthentication(String mechanism) {
        return serverAuthenticationMechanisms.get(mechanism) != null;
    }

    /**
     * Authenticate with the server, if necessary (or possible).
     * 
     * @return true if we are ok to proceed, false for an authentication
     *         failures.
     */
    protected boolean processAuthentication() throws MessagingException {
        // no authentication defined?
        if (!isProtocolPropertyTrue(MAIL_SMTP_AUTH)) {
            return true;
        }

        // we need to authenticate, but we don't have userid/password
        // information...fail this
        // immediately.
        if (username == null || password == null) {
            return false;
        }

        ClientAuthenticator authenticator = null;

        // now go through the progression of mechanisms we support, from the
        // most secure to the
        // least secure.

        if (supportsAuthentication(AUTHENTICATION_DIGESTMD5)) {
            authenticator = new DigestMD5Authenticator(host, username, password, getSASLRealm());
        } else if (supportsAuthentication(AUTHENTICATION_CRAMMD5)) {
            authenticator = new CramMD5Authenticator(username, password);
        } else if (supportsAuthentication(AUTHENTICATION_LOGIN)) {
            authenticator = new LoginAuthenticator(username, password);
        } else if (supportsAuthentication(AUTHENTICATION_PLAIN)) {
            authenticator = new PlainAuthenticator(username, password);
        } else {
            // can't find a mechanism we support in common
            return false;
        }

        if (debug) {
            debugOut("Authenticating for user: " + username + " using " + authenticator.getMechanismName());
        }

        // if the authenticator has some initial data, we compose a command
        // containing the initial data.
        if (authenticator.hasInitialResponse()) {
            StringBuffer command = new StringBuffer();
            // the auth command initiates the handshaking.
            command.append("AUTH ");
            // and tell the server which mechanism we're using.
            command.append(authenticator.getMechanismName());
            command.append(" ");
            // and append the response data
            command.append(new String(Base64.encode(authenticator.evaluateChallenge(null))));
            // send the command now
            sendLine(command.toString());
        }
        // we just send an auth command with the command type.
        else {
            StringBuffer command = new StringBuffer();
            // the auth command initiates the handshaking.
            command.append("AUTH ");
            // and tell the server which mechanism we're using.
            command.append(authenticator.getMechanismName());
            // send the command now
            sendLine(command.toString());
        }

        // now process the challenge sequence. We get a 235 response back when
        // the server accepts the
        // authentication, and a 334 indicates we have an additional challenge.
        while (true) {
            // get the next line, and if it is an error response, return now.
            SMTPReply line;
            try {
                line = new SMTPReply(receiveLine());
            } catch (MalformedSMTPReplyException e) {
                throw new MessagingException(e.toString());
            } catch (MessagingException e) {
                throw e;
            }

            // if we get a completion return, we've passed muster, so give an
            // authentication response.
            if (line.getCode() == AUTHENTICATION_COMPLETE) {
                if (debug) {
                    debugOut("Successful SMTP authentication");
                }
                return true;
            }
            // we have an additional challenge to process.
            else if (line.getCode() == AUTHENTICATION_CHALLENGE) {
                // Does the authenticator think it is finished? We can't answer
                // an additional challenge,
                // so fail this.
                if (authenticator.isComplete()) {
                    return false;
                }

                // we're passed back a challenge value, Base64 encoded.
                byte[] challenge = Base64.decode(line.getMessage().getBytes());

                // have the authenticator evaluate and send back the encoded
                // response.
                sendLine(new String(Base64.encode(authenticator.evaluateChallenge(challenge))));
            }
            // completion or challenge are the only responses we know how to
            // handle. Anything else must
            // be a failure.
            else {
                if (debug) {
                    debugOut("Authentication failure " + line);
                }
                return false;
            }
        }
    }

    /**
     * Simple holder class for the address/send status duple, as we can have
     * mixed success for a set of addresses and a message
     */
    public class SendStatus {
        public final static int SUCCESS = 0;

        public final static int INVALID_ADDRESS = 1;

        public final static int SEND_FAILURE = 2;

        public final static int GENERAL_ERROR = 3;

        // the status type of the send operation.
        int status;

        // the address associated with this status
        InternetAddress address;

        // the command string send to the server.
        String cmd;

        // the reply from the server.
        SMTPReply reply;

        /**
         * Constructor for a SendStatus item.
         * 
         * @param s
         *            The status type.
         * @param a
         *            The address this is the status for.
         * @param c
         *            The command string associated with this status.
         * @param r
         *            The reply information from the server.
         */
        public SendStatus(int s, InternetAddress a, String c, SMTPReply r) {
            this.cmd = c;
            this.status = s;
            this.address = a;
            this.reply = r;
        }

        /**
         * Get the status information for this item.
         * 
         * @return The current status code.
         */
        public int getStatus() {
            return this.status;
        }

        /**
         * Retrieve the InternetAddress object associated with this send
         * operation.
         * 
         * @return The associated address object.
         */
        public InternetAddress getAddress() {
            return this.address;
        }

        /**
         * Retrieve the reply information associated with this send operati
         * 
         * @return The SMTPReply object received for the operation.
         */
        public SMTPReply getReply() {
            return reply;
        }

        /**
         * Get the command string sent for this send operation.
         * 
         * @return The command string for the MAIL TO command sent to the
         *         server.
         */
        public String getCommand() {
            return cmd;
        }

        /**
         * Get an exception object associated with this send operation. There is
         * a mechanism for reporting send success via a send operation, so this
         * will be either a success or failure exception.
         * 
         * @param reportSuccess
         *            Indicates if we want success operations too.
         * 
         * @return A newly constructed exception object.
         */
        public MessagingException getException(boolean reportSuccess) {
            if (status != SUCCESS) {
                return new SMTPAddressFailedException(address, cmd, reply.getCode(), reply.getMessage());
            } else {
                if (reportSuccess) {
                    return new SMTPAddressSucceededException(address, cmd, reply.getCode(), reply.getMessage());
                }
            }
            return null;
        }
    }

    /**
     * Internal debug output routine.
     * 
     * @param value
     *            The string value to output.
     */
    protected void debugOut(String message) {
        debugStream.println("SMTPTransport DEBUG: " + message);
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
}
