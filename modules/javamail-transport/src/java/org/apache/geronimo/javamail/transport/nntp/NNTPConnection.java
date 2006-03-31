/**
 *
 * Copyright 2006 The Apache Software Foundation
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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.InetAddress;
import java.net.Socket;
import java.util.HashMap;
import java.util.List;
import java.util.StringTokenizer;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;

import org.apache.geronimo.javamail.authentication.ClientAuthenticator;
import org.apache.geronimo.javamail.authentication.CramMD5Authenticator;
import org.apache.geronimo.javamail.authentication.DigestMD5Authenticator;
import org.apache.geronimo.javamail.authentication.LoginAuthenticator;
import org.apache.geronimo.javamail.authentication.PlainAuthenticator;
import org.apache.geronimo.javamail.util.MIMEOutputStream;
import org.apache.geronimo.javamail.util.TraceInputStream;
import org.apache.geronimo.javamail.util.TraceOutputStream;
import org.apache.geronimo.mail.util.Base64;
import org.apache.geronimo.mail.util.SessionUtil;

/**
 * Simple implementation of NNTP transport. Just does plain RFC977-ish delivery.
 * <p/> There is no way to indicate failure for a given recipient (it's possible
 * to have a recipient address rejected). The sun impl throws exceptions even if
 * others successful), but maybe we do a different way... <p/>
 * 
 * @version $Rev$ $Date$
 */
public class NNTPConnection {

    /**
     * constants for EOL termination
     */
    protected static final char CR = '\r';

    protected static final char LF = '\n';

    /**
     * property keys for protocol properties.
     */
    protected static final String MAIL_NNTP_AUTH = "auth";

    protected static final String MAIL_NNTP_PORT = "port";

    protected static final String MAIL_NNTP_TIMEOUT = "timeout";

    protected static final String MAIL_NNTP_SASL_REALM = "sasl.realm";

    protected static final String MAIL_NNTP_FACTORY_CLASS = "socketFactory.class";

    protected static final String MAIL_NNTP_FACTORY_FALLBACK = "fallback";

    protected static final String MAIL_NNTP_LOCALADDRESS = "localaddress";

    protected static final String MAIL_NNTP_LOCALPORT = "localport";

    protected static final String MAIL_NNTP_QUITWAIT = "quitwait";

    protected static final String MAIL_NNTP_FACTORY_PORT = "socketFactory.port";

    protected static final String MAIL_NNTP_ENCODE_TRACE = "encodetrace";

    protected static final int MIN_MILLIS = 1000 * 60;

    protected static final int TIMEOUT = MIN_MILLIS * 5;

    protected static final String DEFAULT_MAIL_HOST = "localhost";

    protected static final int DEFAULT_NNTP_PORT = 119;

    protected static final String AUTHENTICATION_PLAIN = "PLAIN";

    protected static final String AUTHENTICATION_LOGIN = "LOGIN";

    protected static final String AUTHENTICATION_CRAMMD5 = "CRAM-MD5";

    protected static final String AUTHENTICATION_DIGESTMD5 = "DIGEST-MD5";

    // the protocol in use (either nntp or nntp-post).
    String protocol;

    // the target host
    protected String host;

    // the target server port.
    protected int port;

    // the connection socket...can be a plain socket or SSLSocket, if TLS is
    // being used.
    protected Socket socket;

    // input stream used to read data. If Sasl is in use, this might be other
    // than the
    // direct access to the socket input stream.
    protected InputStream inputStream;

    // the test reader wrapped around the input stream.
    protected BufferedReader in;

    // the other end of the connection pipeline.
    protected OutputStream outputStream;

    // does the server support posting?
    protected boolean postingAllowed = true;

    // the username we connect with
    protected String username;

    // the authentication password.
    protected String password;

    // the target SASL realm (normally null unless explicitly set or we have an
    // authentication mechanism that
    // requires it.
    protected String realm;

    // the last response line received from the server.
    protected NNTPReply lastServerResponse = null;

    // our attached session
    protected Session session;

    // our session provided debug output stream.
    protected PrintStream debugStream;

    // our debug flag (passed from the hosting transport)
    protected boolean debug;

    // list of authentication mechanisms supported by the server
    protected HashMap serverAuthenticationMechanisms;

    // map of server extension arguments
    protected HashMap serverExtensionArgs;

    // the welcome string from the server.
    protected String welcomeString = null;

    /**
     * Normal constructor for an NNTPConnection() object.
     * 
     * @param session
     *            The attached session.
     * @param host
     *            The target host name of the NNTP server.
     * @param port
     *            The target listening port of the server. Defaults to 119 if
     *            the port is specified as -1.
     * @param username
     *            The login user name (can be null unless authentication is
     *            required).
     * @param password
     *            Password associated with the userid account. Can be null if
     *            authentication is not required.
     * @param debug
     *            The session debug flag.
     */
    public NNTPConnection(String protocol, Session session, String host, int port, String username, String password,
            boolean debug) {
        this.protocol = protocol;
        this.session = session;
        this.host = host;
        this.port = port;
        this.username = username;
        this.password = password;
        this.debug = debug;

        // get our debug output.
        debugStream = session.getDebugOut();
    }

    /**
     * Connect to the server and do the initial handshaking.
     * 
     * @exception MessagingException
     */
    public void connect() throws MessagingException {
        try {

            // create socket and connect to server.
            getConnection();

            // receive welcoming message
            getWelcome();

        } catch (IOException e) {
            if (debug) {
                debugOut("I/O exception establishing connection", e);
            }
            throw new MessagingException("Connection error", e);
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
     * Create a transport connection object and connect it to the target server.
     * 
     * @exception MessagingException
     */
    protected void getConnection() throws IOException {
        // We might have been passed a socket to connect with...if not, we need
        // to create one of the correct type.
        if (socket == null) {
            getConnectedSocket();
        }
        // if we already have a socket, get some information from it and
        // override what we've been passed.
        else {
            port = socket.getPort();
            host = socket.getInetAddress().getHostName();
        }

        // now set up the input/output streams.
        inputStream = new TraceInputStream(socket.getInputStream(), debugStream, debug, getBooleanProperty(
                MAIL_NNTP_ENCODE_TRACE, false));
        ;
        outputStream = new TraceOutputStream(socket.getOutputStream(), debugStream, debug, getBooleanProperty(
                MAIL_NNTP_ENCODE_TRACE, false));

        // get a reader to read the input as lines
        in = new BufferedReader(new InputStreamReader(inputStream));
    }

    /**
     * Close the server connection at termination.
     */
    public void closeServerConnection() {
        try {
            socket.close();
        } catch (IOException ignored) {
        }

        socket = null;
        inputStream = null;
        outputStream = null;
        in = null;
    }

    /**
     * Creates a connected socket
     * 
     * @exception MessagingException
     */
    public void getConnectedSocket() throws IOException {
        if (debug) {
            debugOut("Attempting plain socket connection to server " + host + ":" + port);
        }

        // the socket factory can be specified via a session property. By
        // default, we just directly
        // instantiate a socket without using a factor.
        String socketFactory = getProperty(MAIL_NNTP_FACTORY_CLASS);

        // there are several protocol properties that can be set to tune the
        // created socket. We need to
        // retrieve those bits before creating the socket.
        int timeout = getIntProperty(MAIL_NNTP_TIMEOUT, -1);
        InetAddress localAddress = null;
        // see if we have a local address override.
        String localAddrProp = getProperty(MAIL_NNTP_LOCALADDRESS);
        if (localAddrProp != null) {
            localAddress = InetAddress.getByName(localAddrProp);
        }

        // check for a local port...default is to allow socket to choose.
        int localPort = getIntProperty(MAIL_NNTP_LOCALPORT, 0);

        socket = null;

        // if there is no socket factory defined (normal), we just create a
        // socket directly.
        if (socketFactory == null) {
            socket = new Socket(host, port, localAddress, localPort);
        }

        else {
            try {
                int socketFactoryPort = getIntProperty(MAIL_NNTP_FACTORY_PORT, -1);

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
                if (getBooleanProperty(MAIL_NNTP_FACTORY_FALLBACK, false)) {
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
     * Get the servers welcome blob from the wire....
     */
    public void getWelcome() throws MessagingException {
        NNTPReply line = getReply();

        //
        if (line.isError()) {
            throw new MessagingException("Error connecting to news server: " + line.getMessage());
        }

        // remember we can post.
        if (line.getCode() == NNTPReply.POSTING_ALLOWED) {
            postingAllowed = true;
        } else {
            postingAllowed = false;
        }

        // the NNTP store will want to use the welcome string, so save it.
        welcomeString = line.getMessage();

        // find out what extensions this server supports.
        getExtensions();
    }

    /**
     * Sends the QUIT message and receieves the response
     */
    public void sendQuit() throws MessagingException {
        // there's yet another property that controls whether we should wait for
        // a
        // reply for a QUIT command. If on, just send the command and get outta
        // here.
        if (getBooleanProperty(MAIL_NNTP_QUITWAIT, false)) {
            sendLine("QUIT");
        } else {
            // handle as a real command...we're going to ignore the response.
            sendCommand("QUIT");
        }
    }

    /**
     * Tell the server to switch to a named group.
     * 
     * @param name
     *            The name of the target group.
     * 
     * @return The server response to the GROUP command.
     */
    public NNTPReply selectGroup(String name) throws MessagingException {
        // send the GROUP command
        return sendCommand("GROUP " + name);
    }

    /**
     * Ask the server what extensions it supports.
     * 
     * @return True if the command was accepted ok, false for any errors.
     * @exception MessagingException
     */
    protected void getExtensions() throws MessagingException {
        NNTPReply reply = sendCommand("LIST EXTENSIONS", NNTPReply.EXTENSIONS_SUPPORTED);

        // we get a 202 code back. The first line is just a greeting, and
        // extensions are deliverd as data
        // lines terminated with a "." line.
        if (reply.getCode() != NNTPReply.EXTENSIONS_SUPPORTED) {
            return;
        }

        // get a fresh extension mapping table.
        serverExtensionArgs = new HashMap();
        serverAuthenticationMechanisms = new HashMap();

        // get the extension data lines.
        List extensions = reply.getData();

        // process all of the continuation lines
        for (int i = 0; i < extensions.size(); i++) {
            // go process the extention
            processExtension((String) extensions.get(i));
        }
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
        // AUTHINFO is entered in as a auth mechanism.
        if (extensionName.equals("AUTHINFO")) {
            serverAuthenticationMechanisms.put("AUTHINFO", "AUTHINFO");
        }
        // special case for some older servers.
        else if (extensionName.equals("SASL")) {
            // The security mechanisms are blank delimited tokens.
            StringTokenizer tokenizer = new StringTokenizer(argument);

            while (tokenizer.hasMoreTokens()) {
                String mechanism = tokenizer.nextToken().toUpperCase();
                serverAuthenticationMechanisms.put(mechanism, mechanism);
            }
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
     * Sends the data in the message down the socket. This presumes the server
     * is in the right place and ready for getting the DATA message and the data
     * right place in the sequence
     */
    public synchronized void sendPost(Message msg) throws MessagingException {

        // send the POST command
        NNTPReply line = sendCommand("POST");

        if (line.getCode() != NNTPReply.SEND_ARTICLE) {
            throw new MessagingException("Server rejected POST command: " + line);
        }

        // we've received permission to send the data, so ask the message to
        // write itself out.
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
            throw new MessagingException("I/O error posting message", e);
        } catch (MessagingException e) {
            throw new MessagingException("Exception posting message", e);
        }

        // now to finish, we send a CRLF sequence, followed by a ".".
        sendLine("");
        sendLine(".");

        // use a longer time out here to give the server time to process the
        // data.
        line = new NNTPReply(receiveLine());

        if (line.getCode() != NNTPReply.POSTED_OK) {
            throw new MessagingException("Server rejected POST command: " + line);
        }
    }

    /**
     * Issue a command and retrieve the response. If the given success indicator
     * is received, the command is returning a longer response, terminated by a
     * "crlf.crlf" sequence. These lines are attached to the reply.
     * 
     * @param command
     *            The command to issue.
     * @param success
     *            The command reply that indicates additional data should be
     *            retrieved.
     * 
     * @return The command reply.
     */
    public synchronized NNTPReply sendCommand(String command, int success) throws MessagingException {
        NNTPReply reply = sendCommand(command);
        if (reply.getCode() == success) {
            reply.retrieveData(in);
        }
        return reply;
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
    public NNTPReply sendCommand(String data) throws MessagingException {
        sendLine(data);
        NNTPReply reply = getReply();
        // did the server just inform us we need to authenticate? The spec
        // allows this
        // response to be sent at any time, so we need to try to authenticate
        // and then retry the command.
        if (reply.getCode() == NNTPReply.AUTHINFO_REQUIRED || reply.getCode() == NNTPReply.AUTHINFO_SIMPLE_REQUIRED) {
            if (debug) {
                debugOut("Authentication required received from server.");
            }
            // authenticate with the server, if necessary
            processAuthentication(reply.getCode());
            // if we've safely authenticated, we can reissue the command and
            // process the response.
            sendLine(data);
            reply = getReply();
        }
        return reply;
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
    public NNTPReply sendAuthCommand(String data) throws MessagingException {
        sendLine(data);
        return getReply();
    }

    /**
     * Sends a message down the socket and terminates with the appropriate CRLF
     */
    public void sendLine(String data) throws MessagingException {
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
     * Get a reply line for an NNTP command.
     * 
     * @return An NNTP reply object from the stream.
     */
    public NNTPReply getReply() throws MessagingException {
        lastServerResponse = new NNTPReply(receiveLine());
        return lastServerResponse;
    }

    /**
     * Retrieve the last response received from the NNTP server.
     * 
     * @return The raw response string (including the error code) returned from
     *         the NNTP server.
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
    public String receiveLine() throws MessagingException {
        if (socket == null || !socket.isConnected()) {
            throw new MessagingException("no connection");
        }

        try {
            String line = in.readLine();
            if (line == null) {
                throw new MessagingException("Unexpected end of stream");
            }
            return line;
        } catch (IOException e) {
            throw new MessagingException("Error reading from server", e);
        }
    }

    /**
     * Retrieve the SASL realm used for DIGEST-MD5 authentication. This will
     * either be explicitly set, or retrieved using the mail.nntp.sasl.realm
     * session property.
     * 
     * @return The current realm information (which can be null).
     */
    public String getSASLRealm() {
        // if the realm is null, retrieve it using the realm session property.
        if (realm == null) {
            realm = getProperty(MAIL_NNTP_SASL_REALM);
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
     * Authenticate with the server, if necessary (or possible).
     */
    protected void processAuthentication(int request) throws MessagingException {
        // we need to authenticate, but we don't have userid/password
        // information...fail this
        // immediately.
        if (username == null || password == null) {
            throw new MessagingException("Server requires user authentication");
        }

        if (request == NNTPReply.AUTHINFO_SIMPLE_REQUIRED) {
            processAuthinfoSimple();
        } else {
            if (!processAuthinfoSasl()) {
                processAuthinfoUser();
            }
        }
    }

    /**
     * Process an AUTHINFO SIMPLE command. Not widely used, but if the server
     * asks for it, we can respond.
     * 
     * @exception MessagingException
     */
    protected void processAuthinfoSimple() throws MessagingException {
        NNTPReply reply = sendAuthCommand("AUTHINFO SIMPLE");
        if (reply.getCode() != NNTPReply.AUTHINFO_CONTINUE) {
            throw new MessagingException("Error authenticating with server using AUTHINFO SIMPLE");
        }
        reply = sendAuthCommand(username + " " + password);
        if (reply.getCode() != NNTPReply.AUTHINFO_ACCEPTED) {
            throw new MessagingException("Error authenticating with server using AUTHINFO SIMPLE");
        }
    }

    /**
     * Process AUTHINFO GENERIC. Right now, this appears not to be widely used
     * and information on how the conversations are handled for different auth
     * types is lacking, so right now, this just returns false to force the
     * userid/password form to be used.
     * 
     * @return Always returns false.
     * @exception MessagingException
     */
    protected boolean processAuthinfoGeneric() throws MessagingException {
        return false;
    }

    /**
     * Process AUTHINFO SASL.
     * 
     * @return Returns true if the server support a SASL authentication
     *         mechanism and accepted reponse challenges.
     * @exception MessagingException
     */
    protected boolean processAuthinfoSasl() throws MessagingException {
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
            command.append("AUTHINFO SASL ");
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
            command.append("AUTHINFO SASL");
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
            NNTPReply line = getReply();

            // if we get a completion return, we've passed muster, so give an
            // authentication response.
            if (line.getCode() == NNTPReply.AUTHINFO_ACCEPTED || line.getCode() == NNTPReply.AUTHINFO_ACCEPTED_FINAL) {
                if (debug) {
                    debugOut("Successful SMTP authentication");
                }
                return true;
            }
            // we have an additional challenge to process.
            else if (line.getCode() == NNTPReply.AUTHINFO_CHALLENGE) {
                // Does the authenticator think it is finished? We can't answer
                // an additional challenge,
                // so fail this.
                if (authenticator.isComplete()) {
                    if (debug) {
                        debugOut("Extra authentication challenge " + line);
                    }
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
     * Process an AUTHINFO USER command. Most common form of NNTP
     * authentication.
     * 
     * @exception MessagingException
     */
    protected void processAuthinfoUser() throws MessagingException {
        NNTPReply reply = sendAuthCommand("AUTHINFO USER " + username);
        // accepted without a password (uncommon, but allowed), we're done
        if (reply.getCode() == NNTPReply.AUTHINFO_ACCEPTED) {
            return;
        }
        // the only other non-error response is continue.
        if (reply.getCode() != NNTPReply.AUTHINFO_CONTINUE) {
            throw new MessagingException("Error authenticating with server using AUTHINFO USER: " + reply);
        }
        // now send the password. We expect an accepted response.
        reply = sendAuthCommand("AUTHINFO PASS " + password);
        if (reply.getCode() != NNTPReply.AUTHINFO_ACCEPTED) {
            throw new MessagingException("Error authenticating with server using AUTHINFO SIMPLE");
        }
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
     * Indicate whether posting is allowed for a given server.
     * 
     * @return True if the server allows posting, false if the server is
     *         read-only.
     */
    public boolean isPostingAllowed() {
        return postingAllowed;
    }

    /**
     * Retrieve the welcome string sent back from the server.
     * 
     * @return The server provided welcome string.
     */
    public String getWelcomeString() {
        return welcomeString;
    }

    /**
     * Return the server host for this connection.
     * 
     * @return The String name of the server host.
     */
    public String getHost() {
        return host;
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
