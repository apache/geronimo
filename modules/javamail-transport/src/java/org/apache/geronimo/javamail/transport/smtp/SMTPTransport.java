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
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import javax.mail.Address;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.URLName;
import javax.mail.Transport;

/**
 * Simple implementation of SMTP transport.  Just does plain RFC821-ish
 * delivery.
 * <p/>
 * Supported properties :
 * <p/>
 * <ul>
 * <li> mail.host : to set the server to deliver to.  Default = localhost</li>
 * <li> mail.smtp.port : to set the port.  Default = 25</li>
 * <li> mail.smtp.locahost : name to use for HELO/EHLO - default getHostName()</li>
 * </ul>
 * <p/>
 * There is no way to indicate failure for a given recipient (it's possible to have a
 * recipient address rejected).  The sun impl throws exceptions even if others successful),
 * but maybe we do a different way...
 * <p/>
 * TODO : lots.  ESMTP, user/pass, indicate failure, etc...
 *
 * @version $Rev$ $Date$
 */
public class SMTPTransport extends Transport {
    /**
     * constants for EOL termination
     */
    private static final char CR = 0x0D;
    private static final char LF = 0x0A;

    /**
     * property key for SMTP server to talk to
     */
    private static final String MAIL_HOST = "mail.host";
    private static final String MAIL_SMTP_LOCALHOST = "mail.smtp.localhost";
    private static final String MAIL_SMTP_PORT = "mail.smtp.port";

    private static final int MIN_MILLIS = 1000 * 60;
    private static final String DEFAULT_MAIL_HOST = "localhost";
    private static final int DEFAULT_MAIL_SMTP_PORT = 25;

    /**
     * @param session
     * @param name
     */
    public SMTPTransport(Session session, URLName name) {
        super(session, name);
    }

    public void sendMessage(Message message, Address[] addresses) throws MessagingException {
        // do it and ignore the return
        sendMessage(addresses, message);
    }

    public SendStatus[] sendMessage(Address[] addresses, Message message) throws MessagingException {
        // don't bother me w/ null messages or no addreses
        if (message == null) {
            throw new MessagingException("Null message");
        }

        if (addresses == null || addresses.length == 0) {
            throw new MessagingException("Null or empty address array");
        }


        SendStatus[] stat = new SendStatus[addresses.length];

        try {

            // create socket and connect to server.
            Socket s = getConnectedSocket();

            // receive welcoming message
            if (!getWelcome(s)) {
                throw new MessagingException("Error in getting welcome msg");
            }

            // say hello
            if (!sendHelo(s)) {
                throw new MessagingException("Error in saying HELO to server");
            }


            // send sender
            if (!sendMailFrom(s, message.getFrom())) {
                throw new MessagingException("Error in setting the MAIL FROM");
            }

            // send recipients.  Only send if not null or "", and just ignore (but log) any errors
            for (int i = 0; i < addresses.length; i++) {
                String to = addresses[i].toString();

                int status = SendStatus.SUCCESS;

                if (to != null && !"".equals(to)) {
                    if (!sendRcptTo(s, to)) {
                        // this means it didn't like our recipient.  I say we keep going
                        if (this.session.getDebug()) {
                            this.session.getDebugOut().println("ERROR setting recipient " + to);
                        }

                        status = SendStatus.FAIL;
                    }
                } else {
                    status = SendStatus.FAIL;
                }

                stat[i] = new SendStatus(status, to);
            }

            // send data
            if (!sendData(s, message)) {
                throw new MessagingException("Error sending data");
            }

            // say goodbye
            sendQuit(s);

            try {
                s.close();
            } catch (IOException ignored) {
            }
        } catch (SMTPTransportException e) {
            throw new MessagingException("error", e);
        } catch (MalformedSMTPReplyException e) {
            throw new MessagingException("error", e);
        }

        return stat;
    }

    /**
     * Sends the data in the message down the socket.  This presumes the
     * server is in the right place and ready for getting the DATA message
     * and the data right place in the sequence
     */
    protected boolean sendData(Socket s, Message msg) throws SMTPTransportException, MalformedSMTPReplyException {
        if (msg == null) {
            throw new SMTPTransportException("invalid message");
        }

        // send the DATA command
        sendLine(s, "DATA");

        SMTPReply line = new SMTPReply(receiveLine(s, 5 * MIN_MILLIS));

        if (this.session.getDebug()) {
            this.session.getDebugOut().println(line);
        }

        if (line.isError()) {
            return false;
        }

        // now the data...  I could look at the type, but
        try {
            OutputStream os = s.getOutputStream();

            // todo - be smarter here and send in chunks to let the other side
            // digest easier.  There's a 3 min recommended timeout per chunk...

            msg.writeTo(os);
            os.flush();
        } catch (IOException e) {
            throw new SMTPTransportException(e);
        } catch (MessagingException e) {
            throw new SMTPTransportException(e);
        }

        // now to finish
        sendLine(s, "");
        sendLine(s, ".");

        line = new SMTPReply(receiveLine(s, 10 * MIN_MILLIS));

        return !line.isError();
    }

    /**
     * Sends the QUIT message and receieves the response
     */
    protected boolean sendQuit(Socket s) throws SMTPTransportException, MalformedSMTPReplyException {
        sendLine(s, "QUIT");

        SMTPReply line = new SMTPReply(receiveLine(s, 5 * MIN_MILLIS));

        return !line.isError();
    }

    /**
     * Sets a receiver address for the current message
     */
    protected boolean sendRcptTo(Socket s, String addr) throws SMTPTransportException, MalformedSMTPReplyException {
        if (addr == null || "".equals(addr)) {
            throw new SMTPTransportException("invalid address");
        }

        String msg = "RCPT TO: " + fixEmailAddress(addr);

        sendLine(s, msg);

        SMTPReply line = new SMTPReply(receiveLine(s, 5 * MIN_MILLIS));

        return !line.isError();
    }

    /**
     * Set the sender for this mail.
     */
    protected boolean sendMailFrom(Socket s, Address[] from) throws SMTPTransportException, MalformedSMTPReplyException {
        if (from == null || from.length == 0) {
            throw new SMTPTransportException("no FROM address");
        }

        // TODO - what do we do w/ more than one from???
        String msg = "MAIL FROM: " + fixEmailAddress(from[0].toString());

        sendLine(s, msg);

        SMTPReply line = new SMTPReply(receiveLine(s, 5 * MIN_MILLIS));

        return !line.isError();
    }

    /**
     * Sends the initiating "HELO" message.  We're keeping it simple, just
     * identifying ourselves as we dont' require service extensions, and
     * want to keep it simple for now
     *
     * @param s socket we are talking on.  It's assumed to be open and in
     * right state for this message
     */
    protected boolean sendHelo(Socket s) throws SMTPTransportException, MalformedSMTPReplyException {
        String fqdm = null;

        try {
            fqdm = InetAddress.getLocalHost().getHostName();
        } catch (UnknownHostException e) {
            // fine, we're misconfigured - ignore
        }

        if (fqdm == null) {
            fqdm = session.getProperty(MAIL_SMTP_LOCALHOST);
        }

        if (fqdm == null) {
            throw new SMTPTransportException("Can't get local hostname. " +
                    " Please correctly configure JDK/DNS or set mail.smtp.localhost");
        }

        sendLine(s, "HELO " + fqdm);

        SMTPReply line = new SMTPReply(receiveLine(s, 5 * MIN_MILLIS));

        return !line.isError();
    }

    /**
     * Get the servers welcome blob from the wire....
     */
    protected boolean getWelcome(Socket s) throws SMTPTransportException, MalformedSMTPReplyException {
        SMTPReply line = new SMTPReply(receiveLine(s, 5 * MIN_MILLIS));
        return !line.isError();
    }

    /**
     * Sends a  message down the socket and terminates with the
     * appropriate CRLF
     */
    protected void sendLine(Socket s, String data) throws SMTPTransportException {
        if (s == null) {
            throw new SMTPTransportException("bonehead...");
        }

        if (!s.isConnected()) {
            throw new SMTPTransportException("no connection");
        }

        try {
            OutputStream out = s.getOutputStream();

            out.write(data.getBytes());
            out.write(CR);
            out.write(LF);
            out.flush();

            if (this.session.getDebug()) {
                this.session.getDebugOut().println("sent: " + data);
            }
        } catch (IOException e) {
            throw new SMTPTransportException(e);
        }
    }

    /**
     * Receives one line from the server.  A line is a sequence of bytes
     * terminated by a CRLF
     *
     * @param s socket to receive from
     * @return the line from the server as String
     */
    protected String receiveLine(Socket s, int delayMillis) throws SMTPTransportException {
        if (s == null) {
            throw new SMTPTransportException("bonehead...");
        }

        if (!s.isConnected()) {
            throw new SMTPTransportException("no connection");
        }

        int timeout = 0;

        try {
            // for now, read byte for byte, looking for a CRLF
            timeout = s.getSoTimeout();

            s.setSoTimeout(delayMillis);

            InputStream is = s.getInputStream();

            StringBuffer buff = new StringBuffer();

            int c;
            boolean crFound = false, lfFound = false;

            while ((c = is.read()) != -1 && crFound == false && lfFound == false) {
                buff.append((char) c);

                if (c == CR) {
                    crFound = true;
                }
                if (c == LF) {
                    lfFound = true;
                }
            }

            if (this.session.getDebug()) {
                this.session.getDebugOut().println("received : " + buff.toString());
            }

            return buff.toString();
        } catch (SocketException e) {
            throw new SMTPTransportException(e);
        } catch (IOException e) {
            throw new SMTPTransportException(e);
        } finally {
            try {
                s.setSoTimeout(timeout);
            } catch (SocketException e) {
                // ignore - was just trying to do the decent thing...
            }
        }
    }


    /**
     * Creates and returns a connected socket
     */
    protected Socket getConnectedSocket() throws MessagingException {
        Socket s = new Socket();

        String mail_host = this.session.getProperty(MAIL_HOST);

        if (mail_host == null || "".equals(mail_host)) {
            mail_host = DEFAULT_MAIL_HOST;
        }

        String portString = this.session.getProperty(MAIL_SMTP_PORT);

        int port = DEFAULT_MAIL_SMTP_PORT;

        if (portString != null && !"".equals(portString)) {

            try {
                port = Integer.parseInt(portString);
            } catch (NumberFormatException e) {
                // ignore - we don't care, leave as default
            }
        }

        try {
            if (this.session.getDebug()) {
                this.session.getDebugOut().println("connecting to " + mail_host);
            }

            s.connect(new InetSocketAddress(mail_host, port));

            if (this.session.getDebug()) {
                this.session.getDebugOut().println("connected to " + mail_host);
            }
        } catch (IOException e) {
            if (this.session.getDebug()) {
                this.session.getDebugOut().println("error connecting to " + mail_host);
            }

            throw new MessagingException("Error connecting to " + mail_host, e);
        }

        return s;
    }

    private String fixEmailAddress(String mail) {
        if (mail.charAt(0) == '<') {
            return mail;
        }
        return "<" + mail + ">";
    }

    /**
     * Simple holder class for the address/send status duple, as we can
     * have mixed success for a set of addresses and a message
     */
    public class SendStatus {
        public final static int SUCCESS = 0;
        public final static int FAIL = 1;
        final int status;
        final String address;

        public SendStatus(int s, String a) {
            this.status = s;
            this.address = a;
        }

        public int getStatus() {
            return this.status;
        }

        public String getAddress() {
            return this.address;
        }
    }
}
