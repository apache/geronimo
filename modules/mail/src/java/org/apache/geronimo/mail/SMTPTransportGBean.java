/**
 *
 * Copyright 2004 The Apache Software Foundation
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
package org.apache.geronimo.mail;

import java.util.Properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.gbean.GBeanInfoBuilder;
import org.apache.geronimo.gbean.WaitingException;


/**
 * A GBean that provides for the configuration of a JavaMail SMTP transport
 * protocol.
 * <p/>
 * SMTP transport properties that are common to all SMTP transports are
 * provided via member variables of this class.  Values that are set in the
 * individual member variables will override any of the corresponding values
 * that have been set in the properties set.
 *
 * @version $Rev: $ $Date: $
 * @see MailGBean
 */
public class SMTPTransportGBean extends ProtocolGBean {

    private final Log log = LogFactory.getLog(SMTPTransportGBean.class);

    private Integer port;
    private Integer connectionTimeout;
    private Integer timeout;
    private String from;
    private String localhost;
    private String localaddress;
    private Integer localport;
    private Boolean ehlo;
    private Boolean auth;
    private String submitter;
    private String dsnNotify;
    private String dsnRet;
    private Boolean allow8bitmime;
    private Boolean sendPartial;
    private String saslRealm;
    private Boolean quitWait;
    private Boolean reportSuccess;
    private String socketFactoryClass;
    private Boolean socketFactoryFallback;
    private Integer socketFactoryPort;
    private String mailExtension;


    /**
     * Construct an instance of SMTPTransportGBean
     * <p/>
     * Values that are set in the individual member variables will override any of
     * the corresponding values that have been set in the properties set.
     *
     * @param objectName            the object name of the protocol
     * @param properties            the set of default properties for the protocol
     * @param host                  the host the protocol connects to
     * @param user                  the default name for the protocol
     * @param port                  the SMTP server port
     * @param connectionTimeout     the socket connection timeout value in milliseconds
     * @param timeout               the socket I/O timeout value in milliseconds
     * @param from                  the email address to use for SMTP MAIL command
     * @param localhost             the local host name used in the SMTP HELO or EHLO command
     * @param localaddress          the local address (host name) to bind to when creating the SMTP socket
     * @param localport             the local port number to bind to when creating the SMTP socket
     * @param ehlo                  whether an attempt will be made to sign on with the EHLO command
     * @param auth                  whether an attempt will be made to authenticate the user using
     *                              the AUTH command
     * @param submitter             the submitter to use in the AUTH tag in the MAIL FROM command
     * @param dsnNotify             the NOTIFY option to the RCPT command
     * @param dsnRet                the RET option to the MAIL command
     * @param allow8bitmime         whether encodings are converted to use "8bit" under certain
     *                              conditions
     * @param sendPartial           whether to send email to valid addresses when others are invalid
     * @param saslRealm             the realm to use with DIGEST-MD5 authentication
     * @param quitWait              whether the transport will wait for the response to the QUIT command
     * @param reportSuccess         whether the transport will include an SMTPAddressSucceededException
     *                              for each address that is successful
     * @param socketFactoryClass    the class that will be used to create SMTP sockets
     * @param socketFactoryFallback whether java.net.Socket class will be created if the specified
     *                              socket factory class cannot be created
     * @param socketFactoryPort     whether java.net.Socket class will be created if the specified
     *                              socket factory class cannot be created
     * @param mailExtension         the extension string to append to the MAIL command
     */
    public SMTPTransportGBean(String objectName, Properties properties, String host, String user,
                              Integer port,
                              Integer connectionTimeout,
                              Integer timeout,
                              String from,
                              String localhost,
                              String localaddress,
                              Integer localport,
                              Boolean ehlo,
                              Boolean auth,
                              String submitter,
                              String dsnNotify,
                              String dsnRet,
                              Boolean allow8bitmime,
                              Boolean sendPartial,
                              String saslRealm,
                              Boolean quitWait,
                              Boolean reportSuccess,
                              String socketFactoryClass,
                              Boolean socketFactoryFallback,
                              Integer socketFactoryPort,
                              String mailExtension) {
        super(objectName, "smtp", properties, host, user);

        setPort(port);
        setConnectionTimeout(connectionTimeout);
        setTimeout(timeout);
        setFrom(from);
        setLocalhost(localhost);
        setLocaladdress(localaddress);
        setLocalport(localport);
        setEhlo(ehlo);
        setAuth(auth);
        setSubmitter(submitter);
        setDsnNotify(dsnNotify);
        setDsnRet(dsnRet);
        setAllow8bitmime(allow8bitmime);
        setSendPartial(sendPartial);
        setSaslRealm(saslRealm);
        setQuitWait(quitWait);
        setReportSuccess(reportSuccess);
        setSocketFactoryClass(socketFactoryClass);
        setSocketFactoryFallback(socketFactoryFallback);
        setSocketFactoryPort(socketFactoryPort);
        setMailExtension(mailExtension);
    }

    /**
     * Returns the SMTP server port to connect to, if the connect() method
     * doesn't explicitly specify one.
     */
    public Integer getPort() {
        return port;
    }

    /**
     * Sets the SMTP server port to connect to, if the connect() method
     * doesn't explicitly specify one.
     * <p/>
     * Defaults to 25.
     * <p/>
     * Values that are set here will override any of the corresponding value
     * that has been set in the properties.
     *
     * @param port the SMTP server port to connect to
     */
    public void setPort(Integer port) {
        this.port = port;
    }

    /**
     * Returns the socket connection timeout value in milliseconds.
     */
    public Integer getConnectionTimeout() {
        return connectionTimeout;
    }

    /**
     * Sets the socket connection timeout value in milliseconds.
     * <p/>
     * Default is infinite timeout.
     * <p/>
     * Values that are set here will override any of the corresponding value
     * that has been set in the properties.
     *
     * @param connectionTimeout the socket connection timeout value in milliseconds.
     */
    public void setConnectionTimeout(Integer connectionTimeout) {
        this.connectionTimeout = connectionTimeout;
    }

    /**
     * Returns the socket I/O timeout value in milliseconds.
     */
    public Integer getTimeout() {
        return timeout;
    }

    /**
     * Sets the socket I/O timeout value in milliseconds.
     * <p/>
     * Default is infinite timeout.
     * <p/>
     * Values that are set here will override any of the corresponding value
     * that has been set in the properties.
     *
     * @param timeout the socket I/O timeout value in milliseconds
     */
    public void setTimeout(Integer timeout) {
        this.timeout = timeout;
    }

    /**
     * Returns the email address to use for SMTP MAIL command.
     */
    public String getFrom() {
        return from;
    }

    /**
     * Sets the email address to use for SMTP MAIL command
     * <p/>
     * Email address to use for SMTP MAIL command. This sets the envelope
     * return address. Defaults to msg.getFrom() or InternetAddress.getLocalAddress().
     * NOTE: mail.smtp.user was previously used for this.
     * <p/>
     * Values that are set here will override any of the corresponding value
     * that has been set in the properties.
     *
     * @param from the email address to use for SMTP MAIL command
     */
    public void setFrom(String from) {
        this.from = from;
    }

    /**
     * Returns the local host name used in the SMTP HELO or EHLO command.
     */
    public String getLocalhost() {
        return localhost;
    }

    /**
     * Sets the local host name used in the SMTP HELO or EHLO command.
     * <p/>
     * Local host name used in the SMTP HELO or EHLO command. Defaults to
     * InetAddress.getLocalHost().getHostName(). Should not normally need to
     * be set if your JDK and your name service are configured properly.
     * <p/>
     * Values that are set here will override any of the corresponding value
     * that has been set in the properties.
     *
     * @param localhost the local host name used in the SMTP HELO or EHLO command
     */
    public void setLocalhost(String localhost) {
        this.localhost = localhost;
    }

    /**
     * Returns the local address (host name) to bind to when creating the SMTP socket.
     */
    public String getLocaladdress() {
        return localaddress;
    }

    /**
     * Sets the local address (host name) to bind to when creating the SMTP socket.
     * <p/>
     * Local address (host name) to bind to when creating the SMTP socket.
     * Defaults to the address picked by the Socket class. Should not normally
     * need to be set, but useful with multi-homed hosts where it's important
     * to pick a particular local address to bind to.
     * <p/>
     * Values that are set here will override any of the corresponding value
     * that has been set in the properties.
     *
     * @param localaddress the local address (host name) to bind to when creating the SMTP socket
     */
    public void setLocaladdress(String localaddress) {
        this.localaddress = localaddress;
    }

    /**
     * Returns the local port number to bind to when creating the SMTP socket.
     */
    public Integer getLocalport() {
        return localport;
    }

    /**
     * Sets the local port number to bind to when creating the SMTP socket.
     * <p/>
     * Local port number to bind to when creating the SMTP socket. Defaults to
     * the port number picked by the Socket class.
     * <p/>
     * Values that are set here will override any of the corresponding value
     * that has been set in the properties.
     *
     * @param localport the local port number to bind to when creating the SMTP socket
     */
    public void setLocalport(Integer localport) {
        this.localport = localport;
    }

    /**
     * Returns whether an attempt will be made to sign on with the EHLO command.
     * <p/>
     * If false, do not attempt to sign on with the EHLO command. Normally
     * failure of the EHLO command will fallback to the HELO command; this
     * property exists only for servers that don't fail EHLO properly or don't
     * implement EHLO properly.
     */
    public Boolean getEhlo() {
        return ehlo;
    }

    /**
     * Set whether an attempt will be made to sign on with the EHLO command.
     * <p/>
     * If false, do not attempt to sign on with the EHLO command. Normally
     * failure of the EHLO command will fallback to the HELO command; this
     * property exists only for servers that don't fail EHLO properly or don't
     * implement EHLO properly.
     * <p/>
     * Values that are set here will override any of the corresponding value
     * that has been set in the properties.
     *
     * @param ehlo whether an attempt will be made to sign on with the EHLO command
     */
    public void setEhlo(Boolean ehlo) {
        this.ehlo = ehlo;
    }

    /**
     * Returns whether an attempt will be made to authenticate the user using
     * the AUTH command.
     * <p/>
     * Defaults to false.
     */
    public Boolean getAuth() {
        return auth;
    }

    /**
     * Sets whether an attempt will be made to authenticate the user using
     * the AUTH command.
     * <p/>
     * Defaults to false.
     * <p/>
     * Values that are set here will override any of the corresponding value
     * that has been set in the properties.
     *
     * @param auth whether an attempt will be made to authenticate the user using
     *             the AUTH command.
     */
    public void setAuth(Boolean auth) {
        this.auth = auth;
    }

    /**
     * Returns the submitter to use in the AUTH tag in the MAIL FROM command.
     * <p/>
     * Typically used by a mail relay to pass along information about the
     * original submitter of the message. See also the setSubmitter method of
     * SMTPMessage. Mail clients typically do not use this.
     */
    public String getSubmitter() {
        return submitter;
    }

    /**
     * Sets the submitter to use in the AUTH tag in the MAIL FROM command.
     * <p/>
     * Typically used by a mail relay to pass along information about the
     * original submitter of the message. See also the setSubmitter method of
     * SMTPMessage. Mail clients typically do not use this.
     * <p/>
     * Values that are set here will override any of the corresponding value
     * that has been set in the properties.
     *
     * @param submitter the submitter to use in the AUTH tag in the MAIL FROM command
     */
    public void setSubmitter(String submitter) {
        this.submitter = submitter;
    }

    /**
     * Returns the NOTIFY option to the RCPT command.
     * <p/>
     * Either NEVER, or some combination of SUCCESS, FAILURE, and DELAY
     * (separated by commas).
     */
    public String getDsnNotify() {
        return dsnNotify;
    }

    /**
     * Sets the NOTIFY option to the RCPT command
     * <p/>
     * Either NEVER, or some combination of SUCCESS, FAILURE, and DELAY
     * (separated by commas).
     * <p/>
     * Values that are set here will override any of the corresponding value
     * that has been set in the properties.
     *
     * @param dsnNotify the NOTIFY option to the RCPT command
     */
    public void setDsnNotify(String dsnNotify) {
        this.dsnNotify = dsnNotify;
    }

    /**
     * Returns the RET option to the MAIL command.
     * <p/>
     * Either FULL or HDRS.
     */
    public String getDsnRet() {
        return dsnRet;
    }

    /**
     * Sets the RET option to the MAIL command
     * <p/>
     * Either FULL or HDRS.
     * <p/>
     * Values that are set here will override any of the corresponding value
     * that has been set in the properties.
     *
     * @param dsnRet the RET option to the MAIL command
     */
    public void setDsnRet(String dsnRet) {
        this.dsnRet = dsnRet;
    }

    /**
     * Returns whether encodings are converted to use "8bit" under certain
     * conditions.
     * <p/>
     * If set to true, and the server supports the 8BITMIME extension, text
     * parts of messages that use the "quoted-printable" or "base64" encodings
     * are converted to use "8bit" encoding if they follow the RFC2045 rules
     * for 8bit text.
     */
    public Boolean getAllow8bitmime() {
        return allow8bitmime;
    }

    /**
     * Sets whether encodings are converted to use "8bit" under certain
     * conditions.
     * <p/>
     * If set to true, and the server supports the 8BITMIME extension, text
     * parts of messages that use the "quoted-printable" or "base64" encodings
     * are converted to use "8bit" encoding if they follow the RFC2045 rules
     * for 8bit text.
     * <p/>
     * Values that are set here will override any of the corresponding value
     * that has been set in the properties.
     *
     * @param allow8bitmime whether encodings are converted to use "8bit" under certain
     *                      conditions
     */
    public void setAllow8bitmime(Boolean allow8bitmime) {
        this.allow8bitmime = allow8bitmime;
    }

    /**
     * Returns whether to send email to valid addresses when others are invalid.
     * <p/>
     * If set to true, and a message has some valid and some invalid addresses,
     * send the message anyway, reporting the partial failure with a
     * SendFailedException. If set to false (the default), the message is not
     * sent to any of the recipients if there is an invalid recipient address.
     */
    public Boolean getSendPartial() {
        return sendPartial;
    }

    /**
     * Sets whether to send email to valid addresses when others are invalid.
     * <p/>
     * If set to true, and a message has some valid and some invalid addresses,
     * send the message anyway, reporting the partial failure with a
     * SendFailedException. If set to false (the default), the message is not
     * sent to any of the recipients if there is an invalid recipient address.
     * <p/>
     * Values that are set here will override any of the corresponding value
     * that has been set in the properties.
     *
     * @param sendPartial whether to send email to valid addresses when others are invalid
     */
    public void setSendPartial(Boolean sendPartial) {
        this.sendPartial = sendPartial;
    }

    /**
     * Returns the realm to use with DIGEST-MD5 authentication.
     */
    public String getSaslRealm() {
        return saslRealm;
    }

    /**
     * Sets the realm to use with DIGEST-MD5 authentication.
     * <p/>
     * Values that are set here will override any of the corresponding value
     * that has been set in the properties.
     *
     * @param saslRealm the realm to use with DIGEST-MD5 authentication
     */
    public void setSaslRealm(String saslRealm) {
        this.saslRealm = saslRealm;
    }

    /**
     * Returns whether the transport will wait for the response to the QUIT command.
     * <p/>
     * If set to true, causes the transport to wait for the response to the QUIT
     * command. If set to false (the default), the QUIT command is sent and the
     * connection is immediately closed.
     */
    public Boolean getQuitWait() {
        return quitWait;
    }

    /**
     * Sets whether the transport will wait for the response to the QUIT command
     * <p/>
     * If set to true, causes the transport to wait for the response to the QUIT
     * command. If set to false (the default), the QUIT command is sent and the
     * connection is immediately closed.
     * <p/>
     * Values that are set here will override any of the corresponding value
     * that has been set in the properties.
     *
     * @param quitWait whether the transport will wait for the response to the QUIT command
     */
    public void setQuitWait(Boolean quitWait) {
        this.quitWait = quitWait;
    }

    /**
     * Returns whether the transport will include an SMTPAddressSucceededException
     * for each address that is successful.
     * <p/>
     * Note also that this will cause a SendFailedException to be thrown from
     * the sendMessage method of SMTPTransport even if all addresses were
     * correct and the message was sent successfully.
     */
    public Boolean getReportSuccess() {
        return reportSuccess;
    }

    /**
     * Sets whether the transport will include an SMTPAddressSucceededException
     * for each address that is successful.
     * <p/>
     * Note also that this will cause a SendFailedException to be thrown from
     * the sendMessage method of SMTPTransport even if all addresses were
     * correct and the message was sent successfully.
     * <p/>
     * Values that are set here will override any of the corresponding value
     * that has been set in the properties.
     *
     * @param reportSuccess whether the transport will include an SMTPAddressSucceededException
     *                      for each address that is successful
     */
    public void setReportSuccess(Boolean reportSuccess) {
        this.reportSuccess = reportSuccess;
    }

    /**
     * Returns the class that will be used to create SMTP sockets.
     * <p/>
     * If set, specifies the name of a class that implements the
     * javax.net.SocketFactory interface. This class will be used to create SMTP
     * sockets.
     */
    public String getSocketFactoryClass() {
        return socketFactoryClass;
    }

    /**
     * Sets the class that will be used to create SMTP sockets.
     * <p/>
     * If set, specifies the name of a class that implements the
     * javax.net.SocketFactory interface. This class will be used to create SMTP
     * sockets.
     * <p/>
     * Values that are set here will override any of the corresponding value
     * that has been set in the properties.
     *
     * @param socketFactoryClass the class that will be used to create SMTP sockets
     */
    public void setSocketFactoryClass(String socketFactoryClass) {
        this.socketFactoryClass = socketFactoryClass;
    }

    /**
     * Returns whether java.net.Socket class will be created if the specified
     * socket factory class cannot be created.
     * <p/>
     * If set to true, failure to create a socket using the specified socket
     * factory class will cause the socket to be created using the
     * java.net.Socket class. Defaults to true.
     */
    public Boolean getSocketFactoryFallback() {
        return socketFactoryFallback;
    }

    /**
     * Sets whether java.net.Socket class will be created if the specified
     * socket factory class cannot be created.
     * <p/>
     * If set to true, failure to create a socket using the specified socket
     * factory class will cause the socket to be created using the
     * java.net.Socket class. Defaults to true.
     * <p/>
     * Values that are set here will override any of the corresponding value
     * that has been set in the properties.
     *
     * @param socketFactoryFallback whether java.net.Socket class will be created if the specified
     *                              socket factory class cannot be created
     */
    public void setSocketFactoryFallback(Boolean socketFactoryFallback) {
        this.socketFactoryFallback = socketFactoryFallback;
    }

    /**
     * Returns the port to connect to when using the specified socket factory.
     * <p/>
     * Specifies the port to connect to when using the specified socket
     * factory. If not set, the default port will be used.
     */
    public Integer getSocketFactoryPort() {
        return socketFactoryPort;
    }

    /**
     * Sets the port to connect to when using the specified socket factory.
     * <p/>
     * Specifies the port to connect to when using the specified socket
     * factory. If not set, the default port will be used.
     * <p/>
     * Values that are set here will override any of the corresponding value
     * that has been set in the properties.
     *
     * @param socketFactoryPort the port to connect to when using the specified socket factory
     */
    public void setSocketFactoryPort(Integer socketFactoryPort) {
        this.socketFactoryPort = socketFactoryPort;
    }

    /**
     * Returns the extension string to append to the MAIL command.
     * <p/>
     * Extension string to append to the MAIL command. The extension string
     * can be used to specify standard SMTP service extensions as well as
     * vendor-specific extensions. Typically the application should use the
     * SMTPTransport method supportsExtension to verify that the server
     * supports the desired service extension. See RFC 1869 and other RFCs
     * that define specific extensions.
     */
    public String getMailExtension() {
        return mailExtension;
    }

    /**
     * Sets the extension string to append to the MAIL command.
     * <p/>
     * Extension string to append to the MAIL command. The extension string
     * can be used to specify standard SMTP service extensions as well as
     * vendor-specific extensions. Typically the application should use the
     * SMTPTransport method supportsExtension to verify that the server
     * supports the desired service extension. See RFC 1869 and other RFCs
     * that define specific extensions.
     * <p/>
     * Values that are set here will override any of the corresponding value
     * that has been set in the properties.
     *
     * @param mailExtension the extension string to append to the MAIL command
     */
    public void setMailExtension(String mailExtension) {
        this.mailExtension = mailExtension;
    }

    /**
     * Add the overrides from the member variables to the properties file.
     */
    public void addOverrides(Properties props) {
        super.addOverrides(props);

        if (port != null) props.put("mail.smtp.port", port);
        if (connectionTimeout != null) props.put("mail.smtp.connectiontimeout", connectionTimeout);
        if (timeout != null) props.put("mail.smtp.timeout", timeout);
        if (from != null) props.put("mail.smtp.from", from);
        if (localhost != null) props.put("mail.smtp.localhost", localhost);
        if (localaddress != null) props.put("mail.smtp.localaddress", localaddress);
        if (localport != null) props.put("mail.smtp.localport", localport);
        if (ehlo != null) props.put("mail.smtp.ehlo", ehlo);
        if (auth != null) props.put("mail.smtp.auth", auth);
        if (submitter != null) props.put("mail.smtp.submitter", submitter);
        if (dsnNotify != null) props.put("mail.smtp.dsn.notify", dsnNotify);
        if (dsnRet != null) props.put("mail.smtp.dsn.ret", dsnRet);
        if (allow8bitmime != null) props.put("mail.smtp.allow8bitmime", allow8bitmime);
        if (sendPartial != null) props.put("mail.smtp.sendpartial", sendPartial);
        if (saslRealm != null) props.put("mail.smtp.sasl.realm", saslRealm);
        if (quitWait != null) props.put("mail.smtp.quitwait", quitWait);
        if (reportSuccess != null) props.put("mail.smtp.reportsuccess", reportSuccess);
        if (socketFactoryClass != null) props.put("mail.smtp.socketFactory.class", socketFactoryClass);
        if (socketFactoryFallback != null) props.put("mail.smtp.socketFactory.fallback", socketFactoryFallback);
        if (socketFactoryPort != null) props.put("mail.smtp.socketFactory.port", socketFactoryPort);
        if (mailExtension != null) props.put("mail.smtp.mailextension", mailExtension);
    }

    public void doStart() throws WaitingException, Exception {
        log.info("Started " + getObjectName());
    }

    public void doStop() throws WaitingException, Exception {
        log.info("Stopped " + getObjectName());
    }

    public void doFail() {
        log.info("Failed " + getObjectName());
    }

    public static final GBeanInfo GBEAN_INFO;

    static {
        GBeanInfoBuilder infoFactory = new GBeanInfoBuilder(SMTPTransportGBean.class, ProtocolGBean.GBEAN_INFO);

        infoFactory.addAttribute("port", Integer.class, true);
        infoFactory.addAttribute("connectionTimeout", Integer.class, true);
        infoFactory.addAttribute("timeout", Integer.class, true);
        infoFactory.addAttribute("from", String.class, true);
        infoFactory.addAttribute("localhost", String.class, true);
        infoFactory.addAttribute("localaddress", String.class, true);
        infoFactory.addAttribute("localport", Integer.class, true);
        infoFactory.addAttribute("ehlo", Boolean.class, true);
        infoFactory.addAttribute("auth", Boolean.class, true);
        infoFactory.addAttribute("submitter", String.class, true);
        infoFactory.addAttribute("dsnNotify", String.class, true);
        infoFactory.addAttribute("dsnRet", String.class, true);
        infoFactory.addAttribute("allow8bitmime", Boolean.class, true);
        infoFactory.addAttribute("sendPartial", Boolean.class, true);
        infoFactory.addAttribute("saslRealm", String.class, true);
        infoFactory.addAttribute("quitWait", Boolean.class, true);
        infoFactory.addAttribute("reportSuccess", Boolean.class, true);
        infoFactory.addAttribute("socketFactoryClass", String.class, true);
        infoFactory.addAttribute("socketFactoryFallback", Boolean.class, true);
        infoFactory.addAttribute("socketFactoryPort", Integer.class, true);
        infoFactory.addAttribute("mailExtension", String.class, true);

        infoFactory.setConstructor(new String[]{"objectName", "properties", "host", "user",
                                                "port",
                                                "connectionTimeout",
                                                "timeout",
                                                "from",
                                                "localhost",
                                                "localaddress",
                                                "localport",
                                                "ehlo",
                                                "auth",
                                                "submitter",
                                                "dsnNotify",
                                                "dsnRet",
                                                "allow8bitmime",
                                                "sendPartial",
                                                "saslRealm",
                                                "quitWait",
                                                "reportSuccess",
                                                "socketFactoryClass",
                                                "socketFactoryFallback",
                                                "socketFactoryPort",
                                                "mailExtension"});

        GBEAN_INFO = infoFactory.getBeanInfo();
    }

    public static GBeanInfo getGBeanInfo() {
        return GBEAN_INFO;
    }
}
