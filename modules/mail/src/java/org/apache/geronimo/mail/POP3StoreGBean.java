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
 * A GBean that provides for the configuration of a JavaMail POP3 message store
 * protocol.
 * <p/>
 * POP3 store properties that are common to all POP3 stores are
 * provided via member variables of this class.  Values that are set in the
 * individual member variables will override any of the corresponding values
 * that have been set in the properties set.
 *
 * @version $Rev: $ $Date: $
 * @see MailGBean
 */
public class POP3StoreGBean extends ProtocolGBean {

    private final Log log = LogFactory.getLog(POP3StoreGBean.class);

    private Integer port;
    private Integer connectionTimeout;
    private Integer timeout;
    private Boolean rsetBeforeQuit;
    private String messageClass;
    private String localaddress;
    private Integer localport;
    private Boolean apopEnable;
    private String socketFactoryClass;
    private Boolean socketFactoryFallback;
    private Integer socketFactoryPort;


    /**
     * Construct an instance of POP3StoreGBean
     * <p/>
     * Values that are set in the individual member variables will override any of
     * the corresponding values that have been set in the properties set.
     *
     * @param objectName            the object name of the protocol
     * @param properties            the set of default properties for the protocol
     * @param host                  the host the protocol connects to
     * @param user                  the default name for the protocol
     * @param port                  the POP3 server port
     * @param connectionTimeout     the socket connection timeout value in milliseconds
     * @param timeout               the socket I/O timeout value in milliseconds
     * @param rsetBeforeQuit        whether an attempt will be made send a POP3 RSET command when closing
     *                              the folder, before sending the QUIT command
     * @param messageClass          the class name of a subclass of com.sun.mail.pop3.POP3Message
     * @param localaddress          the local address (host name) to bind to when creating the POP3 socket
     * @param localport             the local port number to bind to when creating the POP3 socket
     * @param apopEnable            whether to use APOP instead of USER/PASS to login to the POP3 server,
     *                              if the POP3 server supports APOP
     * @param socketFactoryClass    the class that will be used to create POP3 sockets
     * @param socketFactoryFallback whether java.net.Socket class will be created if the specified
     *                              socket factory class cannot be created
     * @param socketFactoryPort     whether java.net.Socket class will be created if the specified
     *                              socket factory class cannot be created
     */
    public POP3StoreGBean(String objectName, Properties properties, String host, String user,
                          Integer port,
                          Integer connectionTimeout,
                          Integer timeout,
                          Boolean rsetBeforeQuit,
                          String messageClass,
                          String localaddress,
                          Integer localport,
                          Boolean apopEnable,
                          String socketFactoryClass,
                          Boolean socketFactoryFallback,
                          Integer socketFactoryPort) {
        super(objectName, "pop3", properties, host, user);

        setPort(port);
        setConnectionTimeout(connectionTimeout);
        setTimeout(timeout);
        setRsetBeforeQuit(rsetBeforeQuit);
        setMessageClass(messageClass);
        setLocaladdress(localaddress);
        setLocalport(localport);
        setApopEnable(apopEnable);
        setSocketFactoryClass(socketFactoryClass);
        setSocketFactoryFallback(socketFactoryFallback);
        setSocketFactoryPort(socketFactoryPort);
    }

    /**
     * Returns the POP3 server port to connect to, if the connect() method
     * doesn't explicitly specify one.
     * <p/>
     * Defaults to 110.
     */
    public Integer getPort() {
        return port;
    }

    /**
     * Sets the POP3 server port to connect to, if the connect() method
     * doesn't explicitly specify one.
     * <p/>
     * Defaults to 110.
     * <p/>
     * Values that are set here will override any of the corresponding value
     * that has been set in the properties.
     *
     * @param port the POP3 server port to connect to, if the connect() method
     *             doesn't explicitly specify one
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
     * Returns whether an attempt will be made send a POP3 RSET command when
     * closing the folder, before sending the QUIT command.
     * <p/>
     * Send a POP3 RSET command when closing the folder, before sending the
     * QUIT command. Useful with POP3 servers that implicitly mark all
     * messages that are read as "deleted"; this will prevent such messages
     * from being deleted and expunged unless the client requests so. Default
     * is false.
     */
    public Boolean getRsetBeforeQuit() {
        return rsetBeforeQuit;
    }

    /**
     * Sets whether an attempt will be made send a POP3 RSET command when
     * closing the folder, before sending the QUIT command.
     * <p/>
     * Send a POP3 RSET command when closing the folder, before sending the
     * QUIT command. Useful with POP3 servers that implicitly mark all messages
     * that are read as "deleted"; this will prevent such messages from being
     * deleted and expunged unless the client requests so. Default is false.
     * <p/>
     * Values that are set here will override any of the corresponding value
     * that has been set in the properties.
     *
     * @param rsetBeforeQuit whether an attempt will be made send a POP3 RSET command when
     *                       closing the folder, before sending the QUIT command
     */
    public void setRsetBeforeQuit(Boolean rsetBeforeQuit) {
        this.rsetBeforeQuit = rsetBeforeQuit;
    }

    /**
     * Returns the class name of a subclass of com.sun.mail.pop3.POP3Message.
     * <p/>
     * Class name of a subclass of com.sun.mail.pop3.POP3Message. The subclass
     * can be used to handle (for example) non-standard Content-Type headers.
     * The subclass must have a public constructor of the form
     * MyPOP3Message(Folder f, int msgno) throws MessagingException.
     */
    public String getMessageClass() {
        return messageClass;
    }

    /**
     * Sets the class name of a subclass of com.sun.mail.pop3.POP3Message.
     * <p/>
     * Class name of a subclass of com.sun.mail.pop3.POP3Message. The subclass
     * can be used to handle (for example) non-standard Content-Type headers.
     * The subclass must have a public constructor of the form
     * MyPOP3Message(Folder f, int msgno) throws MessagingException.
     * <p/>
     * Values that are set here will override any of the corresponding value
     * that has been set in the properties.
     *
     * @param messageClass the class name of a subclass of com.sun.mail.pop3.POP3Message.
     */
    public void setMessageClass(String messageClass) {
        this.messageClass = messageClass;
    }

    /**
     * Returns the local address (host name) to bind to when creating the POP3 socket.
     */
    public String getLocaladdress() {
        return localaddress;
    }

    /**
     * Sets the local address (host name) to bind to when creating the POP3 socket.
     * <p/>
     * Local address (host name) to bind to when creating the POP3 socket.
     * Defaults to the address picked by the Socket class. Should not normally
     * need to be set, but useful with multi-homed hosts where it's important
     * to pick a particular local address to bind to.
     * <p/>
     * Values that are set here will override any of the corresponding value
     * that has been set in the properties.
     *
     * @param localaddress the local address (host name) to bind to when creating the POP3 socket
     */
    public void setLocaladdress(String localaddress) {
        this.localaddress = localaddress;
    }

    /**
     * Returns the local port number to bind to when creating the POP3 socket.
     */
    public Integer getLocalport() {
        return localport;
    }

    /**
     * Sets the local port number to bind to when creating the POP3 socket.
     * <p/>
     * Local port number to bind to when creating the POP3 socket. Defaults to
     * the port number picked by the Socket class.
     * <p/>
     * Values that are set here will override any of the corresponding value
     * that has been set in the properties.
     *
     * @param localport the local port number to bind to when creating the POP3 socket
     */
    public void setLocalport(Integer localport) {
        this.localport = localport;
    }

    /**
     * Returns whether to use APOP instead of USER/PASS to login to the POP3
     * server, if the POP3 server supports APOP.
     * <p/>
     * If set to true, use APOP instead of USER/PASS to login to the POP3
     * server, if the POP3 server supports APOP. APOP sends a digest of the
     * password rather than the clear text password. Defaults to false.
     */
    public Boolean isApopEnable() {
        return apopEnable;
    }

    /**
     * Sets whether to use APOP instead of USER/PASS to login to the POP3
     * server, if the POP3 server supports APOP.
     * <p/>
     * If set to true, use APOP instead of USER/PASS to login to the POP3
     * server, if the POP3 server supports APOP. APOP sends a digest of the
     * password rather than the clear text password. Defaults to false.
     *
     * @param apopEnable whether to use APOP instead of USER/PASS to login to the POP3
     *                   server, if the POP3 server supports APOP
     */
    public void setApopEnable(Boolean apopEnable) {
        this.apopEnable = apopEnable;
    }

    /**
     * Returns the class that will be used to create POP3 sockets.
     * <p/>
     * If set, specifies the name of a class that implements the
     * javax.net.SocketFactory interface. This class will be used to create POP3
     * sockets.
     */
    public String getSocketFactoryClass() {
        return socketFactoryClass;
    }

    /**
     * Sets the class that will be used to create POP3 sockets.
     * <p/>
     * If set, specifies the name of a class that implements the
     * javax.net.SocketFactory interface. This class will be used to create POP3
     * sockets.
     * <p/>
     * Values that are set here will override any of the corresponding value
     * that has been set in the properties.
     *
     * @param socketFactoryClass the class that will be used to create POP3 sockets
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
    public Boolean isSocketFactoryFallback() {
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
     * Add the overrides from the member variables to the properties file.
     */
    public void addOverrides(Properties props) {
        super.addOverrides(props);

        if (port != null) props.put("mail.pop3.port", port);
        if (connectionTimeout != null) props.put("mail.pop3.connectiontimeout", connectionTimeout);
        if (timeout != null) props.put("mail.pop3.timeout", timeout);
        if (rsetBeforeQuit != null) props.put("mail.pop3.rsetbeforequit", rsetBeforeQuit);
        if (messageClass != null) props.put("mail.pop3.message.class", messageClass);
        if (localaddress != null) props.put("mail.pop3.localaddress", localaddress);
        if (localport != null) props.put("mail.pop3.localport", localport);
        if (apopEnable != null) props.put("mail.pop3.apop.enable", apopEnable);
        if (socketFactoryClass != null) props.put("mail.pop3.socketFactory.class", socketFactoryClass);
        if (socketFactoryFallback != null) props.put("mail.pop3.socketFactory.fallback", socketFactoryFallback);
        if (socketFactoryPort != null) props.put("mail.pop3.socketFactory.port", socketFactoryPort);
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
        GBeanInfoBuilder infoFactory = new GBeanInfoBuilder(POP3StoreGBean.class, ProtocolGBean.GBEAN_INFO);

        infoFactory.addAttribute("port", Integer.class, true);
        infoFactory.addAttribute("connectionTimeout", Integer.class, true);
        infoFactory.addAttribute("timeout", Integer.class, true);
        infoFactory.addAttribute("rsetBeforeQuit", Boolean.class, true);
        infoFactory.addAttribute("messageClass", String.class, true);
        infoFactory.addAttribute("localaddress", String.class, true);
        infoFactory.addAttribute("localport", Integer.class, true);
        infoFactory.addAttribute("apopEnable", Boolean.class, true);
        infoFactory.addAttribute("socketFactoryClass", String.class, true);
        infoFactory.addAttribute("socketFactoryFallback", Boolean.class, true);
        infoFactory.addAttribute("socketFactoryPort", Integer.class, true);

        infoFactory.setConstructor(new String[]{"objectName", "properties", "host", "user",
                                                "port",
                                                "connectionTimeout",
                                                "timeout",
                                                "rsetBeforeQuit",
                                                "messageClass",
                                                "localaddress",
                                                "localport",
                                                "apopEnable",
                                                "socketFactoryClass",
                                                "socketFactoryFallback",
                                                "socketFactoryPort"});

        GBEAN_INFO = infoFactory.getBeanInfo();
    }

    public static GBeanInfo getGBeanInfo() {
        return GBEAN_INFO;
    }
}
