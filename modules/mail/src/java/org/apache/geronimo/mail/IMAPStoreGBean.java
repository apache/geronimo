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

/**
 * A GBean that provides for the configuration of a JavaMail IMAP message store
 * protocol.
 * <p/>
 * IMAP store properties that are common to all IMAP stores are
 * provided via member variables of this class.  Values that are set in the
 * individual member variables will override any of the corresponding values
 * that have been set in the properties set.
 *
 * @version $Rev: $ $Date: $
 * @see MailGBean
 */
public class IMAPStoreGBean extends ProtocolGBean {

    private final Log log = LogFactory.getLog(IMAPStoreGBean.class);

    private Integer port;
    private Boolean partialFetch;
    private Integer fetchSize;
    private Integer connectionTimeout;
    private Integer timeout;
    private Integer statusCacheTimeout;
    private Integer appendBufferSize;
    private Integer connectionPoolSize;
    private Integer connectionPoolTimeout;
    private Boolean separateStoreConnection;
    private Boolean allowReadOnlySelect;
    private Boolean authLoginDisable;
    private Boolean authPlainDisable;
    private Boolean startTLSEnable;
    private String localaddress;
    private Integer localport;
    private Boolean saslEnable;
    private String saslMechanisms;
    private String saslAuthorizationId;
    private String socketFactoryClass;
    private Boolean socketFactoryFallback;
    private Integer socketFactoryPort;


    /**
     * Construct an instance of IMAPStoreGBean
     * <p/>
     * Values that are set in the individual member variables will override any of
     * the corresponding values that have been set in the properties set.
     *
     * @param objectName              the object name of the protocol
     * @param properties              the set of default properties for the protocol
     * @param host                    the host the protocol connects to
     * @param user                    the default name for the protocol
     * @param port                    the IMAP server port
     * @param partialFetch            whether the IMAP partial-fetch capability should be used
     * @param fetchSize               the partial fetch size in bytes
     * @param connectionTimeout       the socket connection timeout value in milliseconds
     * @param timeout                 the socket I/O timeout value in milliseconds
     * @param statusCacheTimeout      the timeout value in milliseconds for cache of STATUS command response
     * @param appendBufferSize        the maximum size of a message to buffer in memory when appending to an IMAP folder
     * @param connectionPoolSize      the maximum number of available connections in the connection pool
     * @param connectionPoolTimeout   the timeout value in milliseconds for connection pool connections
     * @param separateStoreConnection the flag to indicate whether to use a dedicated store connection for store commands
     * @param allowReadOnlySelect     the flag to indicate whether SELECT commands are read-only
     * @param authLoginDisable        the flag that prevents use of the non-standard AUTHENTICATE LOGIN command, instead using the plain LOGIN command
     * @param authPlainDisable        the flag that prevents use of the AUTHENTICATE PLAIN command
     * @param startTLSEnable          the flag that enables the use of the STARTTLS command (if supported by the server) to switch the connection to a TLS-protected connection before issuing any login commands
     * @param localaddress            the local address (host name) to bind to when creating the IMAP socket
     * @param localport               the local port number to bind to when creating the IMAP socket
     * @param saslEnable              the flag that enables an attempt to use the javax.security.sasl package to choose an authentication mechanism for login
     * @param saslMechanisms          a space or comma separated list of SASL mechanism names to try to use
     * @param saslAuthorizationId     the authorization ID to use in the SASL authentication
     * @param socketFactoryClass      the class that will be used to create IMAP sockets
     * @param socketFactoryFallback   whether java.net.Socket class will be created if the specified
     *                                socket factory class cannot be created
     * @param socketFactoryPort       whether java.net.Socket class will be created if the specified
     *                                socket factory class cannot be created
     */
    public IMAPStoreGBean(String objectName, Properties properties, String host, String user,
                          Integer port,
                          Boolean partialFetch,
                          Integer fetchSize,
                          Integer connectionTimeout,
                          Integer timeout,
                          Integer statusCacheTimeout,
                          Integer appendBufferSize,
                          Integer connectionPoolSize,
                          Integer connectionPoolTimeout,
                          Boolean separateStoreConnection,
                          Boolean allowReadOnlySelect,
                          Boolean authLoginDisable,
                          Boolean authPlainDisable,
                          Boolean startTLSEnable,
                          String localaddress,
                          Integer localport,
                          Boolean saslEnable,
                          String saslMechanisms,
                          String saslAuthorizationId,
                          String socketFactoryClass,
                          Boolean socketFactoryFallback,
                          Integer socketFactoryPort) {
        super(objectName, "imap", properties, host, user);

        setPort(port);
        setPartialFetch(partialFetch);
        setFetchSize(fetchSize);
        setConnectionTimeout(connectionTimeout);
        setTimeout(timeout);
        setStatusCacheTimeout(statusCacheTimeout);
        setAppendBufferSize(appendBufferSize);
        setConnectionPoolSize(connectionPoolSize);
        setConnectionPoolTimeout(connectionPoolTimeout);
        setSeparateStoreConnection(separateStoreConnection);
        setAllowReadOnlySelect(allowReadOnlySelect);
        setAuthLoginDisable(authLoginDisable);
        setAuthPlainDisable(authPlainDisable);
        setStartTLSEnable(startTLSEnable);
        setLocaladdress(localaddress);
        setLocalport(localport);
        setSaslEnable(saslEnable);
        setSaslMechanisms(saslMechanisms);
        setSaslAuthorizationId(saslAuthorizationId);
        setSocketFactoryClass(socketFactoryClass);
        setSocketFactoryFallback(socketFactoryFallback);
        setSocketFactoryPort(socketFactoryPort);
    }

    /**
     * Returns the IMAP server port to connect to, if the connect() method
     * doesn't explicitly specify one.
     */
    public Integer getPort() {
        return port;
    }

    /**
     * Sets the IMAP server port to connect to, if the connect() method
     * doesn't explicitly specify one.
     * <p/>
     * Defaults to 143.
     * <p/>
     * Values that are set here will override any of the corresponding value
     * that has been set in the properties.
     *
     * @param port the IMAP server port to connect to, if the connect() method
     *             doesn't explicitly specify one
     */
    public void setPort(Integer port) {
        this.port = port;
    }

    /**
     * Returns whether the IMAP partial-fetch capability should be used.
     * <p/>
     * Controls whether the IMAP partial-fetch capability should be used.
     * Defaults to true.
     */
    public Boolean getPartialFetch() {
        return partialFetch;
    }

    /**
     * Sets whether the IMAP partial-fetch capability should be used.
     * <p/>
     * Controls whether the IMAP partial-fetch capability should be used.
     * Defaults to true.
     * <p/>
     * Values that are set here will override any of the corresponding value
     * that has been set in the properties.
     *
     * @param partialFetch whether the IMAP partial-fetch capability should be used
     */
    public void setPartialFetch(Boolean partialFetch) {
        this.partialFetch = partialFetch;
    }

    /**
     * Returns the partial fetch size in bytes.
     * <p/>
     * Defaults to 16K.
     */
    public Integer getFetchSize() {
        return fetchSize;
    }

    /**
     * Sets the partial fetch size in bytes
     * <p/>
     * Defaults to 16K.
     * <p/>
     * Values that are set here will override any of the corresponding value
     * that has been set in the properties.
     *
     * @param fetchSize the partial fetch size in bytes
     */
    public void setFetchSize(Integer fetchSize) {
        this.fetchSize = fetchSize;
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
     * Returns the timeout value in milliseconds for cache of STATUS command response.
     * <p/>
     * Timeout value in milliseconds for cache of STATUS command response.
     * Default is 1000 (1 second). Zero disables cache.
     */
    public Integer getStatusCacheTimeout() {
        return statusCacheTimeout;
    }

    /**
     * Sets the timeout value in milliseconds for cache of STATUS command response
     * <p/>
     * Timeout value in milliseconds for cache of STATUS command response.
     * Default is 1000 (1 second). Zero disables cache.
     * <p/>
     * Values that are set here will override any of the corresponding value
     * that has been set in the properties.
     *
     * @param statusCacheTimeout the timeout value in milliseconds for cache of STATUS command response
     */
    public void setStatusCacheTimeout(Integer statusCacheTimeout) {
        this.statusCacheTimeout = statusCacheTimeout;
    }

    /**
     * Returns the maximum size of a message to buffer in memory when appending
     * to an IMAP folder.
     * <p/>
     * Maximum size of a message to buffer in memory when appending to an IMAP
     * folder. If not set, or set to -1, there is no maximum and all messages
     * are buffered. If set to 0, no messages are buffered. If set to (e.g.)
     * 8192, messages of 8K bytes or less are buffered, larger messages are not
     * buffered. Buffering saves cpu time at the expense of short term memory
     * usage. If you commonly append very large messages to IMAP mailboxes you
     * might want to set this to a moderate value (1M or less).
     */
    public Integer getAppendBufferSize() {
        return appendBufferSize;
    }

    /**
     * Sets the maximum size of a message to buffer in memory when appending
     * to an IMAP folder.
     * <p/>
     * Maximum size of a message to buffer in memory when appending to an IMAP
     * folder. If not set, or set to -1, there is no maximum and all messages
     * are buffered. If set to 0, no messages are buffered. If set to (e.g.)
     * 8192, messages of 8K bytes or less are buffered, larger messages are not
     * buffered. Buffering saves cpu time at the expense of short term memory
     * usage. If you commonly append very large messages to IMAP mailboxes you
     * might want to set this to a moderate value (1M or less).
     * <p/>
     * Values that are set here will override any of the corresponding value
     * that has been set in the properties.
     *
     * @param appendBufferSize the maximum size of a message to buffer in memory when appending
     *                         to an IMAP folder
     */
    public void setAppendBufferSize(Integer appendBufferSize) {
        this.appendBufferSize = appendBufferSize;
    }

    /**
     * Returns the maximum number of available connections in the connection pool.
     * <p/>
     * Default is 1.
     */
    public Integer getConnectionPoolSize() {
        return connectionPoolSize;
    }

    /**
     * Sets the maximum number of available connections in the connection pool.
     * <p/>
     * Default is 1.
     * <p/>
     * Values that are set here will override any of the corresponding value
     * that has been set in the properties.
     *
     * @param connectionPoolSize the maximum number of available connections in the connection pool
     */
    public void setConnectionPoolSize(Integer connectionPoolSize) {
        this.connectionPoolSize = connectionPoolSize;
    }

    /**
     * Returns the timeout value in milliseconds for connection pool connections.
     * <p/>
     * Default is 45000 (45 seconds).
     */
    public Integer getConnectionPoolTimeout() {
        return connectionPoolTimeout;
    }

    /**
     * Sets the timeout value in milliseconds for connection pool connections
     * <p/>
     * Default is 45000 (45 seconds).
     * <p/>
     * Values that are set here will override any of the corresponding value
     * that has been set in the properties.
     *
     * @param connectionPoolTimeout the timeout value in milliseconds for connection pool connections
     */
    public void setConnectionPoolTimeout(Integer connectionPoolTimeout) {
        this.connectionPoolTimeout = connectionPoolTimeout;
    }

    /**
     * Returns the flag to indicate whether to use a dedicated store
     * connection for store commands.
     * <p/>
     * Flag to indicate whether to use a dedicated store connection
     * for store commands. Default is false.
     */
    public Boolean getSeparateStoreConnection() {
        return separateStoreConnection;
    }

    /**
     * Sets the flag to indicate whether to use a dedicated store
     * connection for store commands
     * <p/>
     * Flag to indicate whether to use a dedicated store connection
     * for store commands. Default is false.
     * <p/>
     * Values that are set here will override any of the corresponding value
     * that has been set in the properties.
     *
     * @param separateStoreConnection the flag to indicate whether to use a dedicated store
     *                                connection for store commands
     */
    public void setSeparateStoreConnection(Boolean separateStoreConnection) {
        this.separateStoreConnection = separateStoreConnection;
    }

    /**
     * Returns the flag to indicate whether SELECT commands are read-only.
     * <p/>
     * If false, attempts to open a folder read/write will fail if the SELECT
     * command succeeds but indicates that the folder is READ-ONLY. This
     * sometimes indicates that the folder contents can'tbe changed, but the
     * flags are per-user and can be changed, such as might be the case for
     * public shared folders. If true, such open attempts will succeed, allowing
     * the flags to be changed. The getMode method on the Folder object will
     * return Folder.READ_ONLY in this case even though the open method specified
     * Folder.READ_WRITE. Default is false.
     */
    public Boolean getAllowReadOnlySelect() {
        return allowReadOnlySelect;
    }

    /**
     * Sets the flag to indicate whether SELECT commands are read-only.
     * <p/>
     * If false, attempts to open a folder read/write will fail if the SELECT
     * command succeeds but indicates that the folder is READ-ONLY. This
     * sometimes indicates that the folder contents can'tbe changed, but the
     * flags are per-user and can be changed, such as might be the case for
     * public shared folders. If true, such open attempts will succeed, allowing
     * the flags to be changed. The getMode method on the Folder object will
     * return Folder.READ_ONLY in this case even though the open method specified
     * Folder.READ_WRITE. Default is false.
     * <p/>
     * Values that are set here will override any of the corresponding value
     * that has been set in the properties.
     *
     * @param allowReadOnlySelect the flag to indicate whether SELECT commands are read-only
     */
    public void setAllowReadOnlySelect(Boolean allowReadOnlySelect) {
        this.allowReadOnlySelect = allowReadOnlySelect;
    }

    /**
     * Returns the flag that prevents use of the non-standard AUTHENTICATE LOGIN
     * command, instead using the plain LOGIN command.
     * <p/>
     * Default is false.
     */
    public Boolean getAuthLoginDisable() {
        return authLoginDisable;
    }

    /**
     * Sets the flag that prevents use of the non-standard AUTHENTICATE LOGIN
     * command, instead using the plain LOGIN command.
     * <p/>
     * Default is false.
     * <p/>
     * Values that are set here will override any of the corresponding value
     * that has been set in the properties.
     *
     * @param authLoginDisable the flag that prevents use of the non-standard AUTHENTICATE LOGIN
     *                         command, instead using the plain LOGIN command
     */
    public void setAuthLoginDisable(Boolean authLoginDisable) {
        this.authLoginDisable = authLoginDisable;
    }

    /**
     * Returns the flag that prevents use of the AUTHENTICATE PLAIN command.
     * <p/>
     * Default is false.
     */
    public Boolean getAuthPlainDisable() {
        return authPlainDisable;
    }

    /**
     * Sets the flag that prevents use of the AUTHENTICATE PLAIN command.
     * <p/>
     * Default is false.
     * <p/>
     * Values that are set here will override any of the corresponding value
     * that has been set in the properties.
     *
     * @param authPlainDisable the flag that prevents use of the AUTHENTICATE PLAIN command
     */
    public void setAuthPlainDisable(Boolean authPlainDisable) {
        this.authPlainDisable = authPlainDisable;
    }

    /**
     * Returns the flag that enables the use of the STARTTLS command (if
     * supported by the server) to switch the connection to a TLS-protected
     * connection before issuing any login commands.
     * <p/>
     * If true, enables the use of the STARTTLS command (if supported by the
     * server) to switch the connection to a TLS-protected connection before
     * issuing any login commands. Note that an appropriate trust store must
     * configured so that the client will trust the server's certificate.
     * This feature only works on J2SE 1.4 and newer systems. Default is false.
     */
    public Boolean getStartTLSEnable() {
        return startTLSEnable;
    }

    /**
     * Sets the flag that enables the use of the STARTTLS command (if
     * supported by the server) to switch the connection to a TLS-protected
     * connection before issuing any login commands.
     * <p/>
     * If true, enables the use of the STARTTLS command (if supported by the
     * server) to switch the connection to a TLS-protected connection before
     * issuing any login commands. Note that an appropriate trust store must
     * configured so that the client will trust the server's certificate.
     * This feature only works on J2SE 1.4 and newer systems. Default is false.
     * <p/>
     * Values that are set here will override any of the corresponding value
     * that has been set in the properties.
     *
     * @param startTLSEnable the flag that enables the use of the STARTTLS command (if
     *                       supported by the server) to switch the connection to a TLS-protected
     *                       connection before issuing any login commands
     */
    public void setStartTLSEnable(Boolean startTLSEnable) {
        this.startTLSEnable = startTLSEnable;
    }

    /**
     * Returns the local address (host name) to bind to when creating the IMAP socket.
     */
    public String getLocaladdress() {
        return localaddress;
    }

    /**
     * Sets the local address (host name) to bind to when creating the IMAP socket.
     * <p/>
     * Local address (host name) to bind to when creating the IMAP socket.
     * Defaults to the address picked by the Socket class. Should not normally
     * need to be set, but useful with multi-homed hosts where it's important
     * to pick a particular local address to bind to.
     * <p/>
     * Values that are set here will override any of the corresponding value
     * that has been set in the properties.
     *
     * @param localaddress the local address (host name) to bind to when creating the IMAP socket
     */
    public void setLocaladdress(String localaddress) {
        this.localaddress = localaddress;
    }

    /**
     * Returns the local port number to bind to when creating the IMAP socket.
     */
    public Integer getLocalport() {
        return localport;
    }

    /**
     * Sets the local port number to bind to when creating the IMAP socket.
     * <p/>
     * Local port number to bind to when creating the IMAP socket. Defaults to
     * the port number picked by the Socket class.
     * <p/>
     * Values that are set here will override any of the corresponding value
     * that has been set in the properties.
     *
     * @param localport the local port number to bind to when creating the IMAP socket
     */
    public void setLocalport(Integer localport) {
        this.localport = localport;
    }

    /**
     * Returns the flag that enables an attempt to use the javax.security.sasl
     * package to choose an authentication mechanism for login.
     * <p/>
     * Defaults to false.
     */
    public Boolean getSaslEnable() {
        return saslEnable;
    }

    /**
     * Sets the flag that enables an attempt to use the javax.security.sasl
     * package to choose an authentication mechanism for login.
     * <p/>
     * Defaults to false.
     * <p/>
     * Values that are set here will override any of the corresponding value
     * that has been set in the properties.
     *
     * @param saslEnable the flag that enables an attempt to use the javax.security.sasl
     *                   package to choose an authentication mechanism for login
     */
    public void setSaslEnable(Boolean saslEnable) {
        this.saslEnable = saslEnable;
    }

    /**
     * Returns a space or comma separated list of SASL mechanism names to try to use.
     */
    public String getSaslMechanisms() {
        return saslMechanisms;
    }

    /**
     * Sets a space or comma separated list of SASL mechanism names to try to use.
     * <p/>
     * Values that are set here will override any of the corresponding value
     * that has been set in the properties.
     *
     * @param saslMechanisms a space or comma separated list of SASL mechanism names to try to use
     */
    public void setSaslMechanisms(String saslMechanisms) {
        this.saslMechanisms = saslMechanisms;
    }

    /**
     * Returns the authorization ID to use in the SASL authentication.
     * <p/>
     * If not set, the authetication ID (user name) is used.
     */
    public String getSaslAuthorizationId() {
        return saslAuthorizationId;
    }

    /**
     * Sets the authorization ID to use in the SASL authentication.
     * <p/>
     * If not set, the authetication ID (user name) is used.
     * <p/>
     * Values that are set here will override any of the corresponding value
     * that has been set in the properties.
     *
     * @param saslAuthorizationId the authorization ID to use in the SASL authentication
     */
    public void setSaslAuthorizationId(String saslAuthorizationId) {
        this.saslAuthorizationId = saslAuthorizationId;
    }

    /**
     * Returns the class that will be used to create IMAP sockets.
     * <p/>
     * If set, specifies the name of a class that implements the
     * javax.net.SocketFactory interface. This class will be used to create IMAP
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
     * Add the overrides from the member variables to the properties file.
     */
    public void addOverrides(Properties props) {
        super.addOverrides(props);

        if (port != null) props.put("mail.imap.port", port);
        if (partialFetch != null) props.put("mail.imap.partialfetch", partialFetch);
        if (fetchSize != null) props.put("mail.imap.fetchsize", fetchSize);
        if (connectionTimeout != null) props.put("mail.imap.connectiontimeout", connectionTimeout);
        if (timeout != null) props.put("mail.imap.timeout", timeout);
        if (statusCacheTimeout != null) props.put("mail.imap.statuscachetimeout", statusCacheTimeout);
        if (appendBufferSize != null) props.put("mail.imap.appendbuffersize", appendBufferSize);
        if (connectionPoolSize != null) props.put("mail.imap.connectionpoolsize", connectionPoolSize);
        if (connectionPoolTimeout != null) props.put("mail.imap.connectionpooltimeout", connectionPoolTimeout);
        if (separateStoreConnection != null) props.put("mail.imap.separatestoreconnection", separateStoreConnection);
        if (allowReadOnlySelect != null) props.put("mail.imap.allowreadonlyselect", allowReadOnlySelect);
        if (authLoginDisable != null) props.put("mail.imap.auth.login.disable", authLoginDisable);
        if (authPlainDisable != null) props.put("mail.imap.auth.plain.disable", authPlainDisable);
        if (startTLSEnable != null) props.put("mail.imap.auth.starttls.enable", startTLSEnable);
        if (localaddress != null) props.put("mail.imap.localaddress", localaddress);
        if (localport != null) props.put("mail.imap.localport", localport);
        if (saslEnable != null) props.put("mail.imap.sasl.enable", saslEnable);
        if (saslMechanisms != null) props.put("mail.imap.sasl.mechanisms", saslMechanisms);
        if (saslAuthorizationId != null) props.put("mail.imap.sasl.authorizationid", saslAuthorizationId);
        if (socketFactoryClass != null) props.put("mail.imap.socketFactory.class", socketFactoryClass);
        if (socketFactoryFallback != null) props.put("mail.imap.socketFactory.fallback", socketFactoryFallback);
        if (socketFactoryPort != null) props.put("mail.imap.socketFactory.port", socketFactoryPort);
    }

    public void doStart() throws Exception {
        log.debug("Started " + getObjectName());
    }

    public void doStop() throws Exception {
        log.debug("Stopped " + getObjectName());
    }

    public void doFail() {
        log.warn("Failed " + getObjectName());
    }

    public static final GBeanInfo GBEAN_INFO;

    static {
        GBeanInfoBuilder infoFactory = GBeanInfoBuilder.createStatic(IMAPStoreGBean.class, ProtocolGBean.GBEAN_INFO);

        infoFactory.addAttribute("port", Integer.class, true);
        infoFactory.addAttribute("partialFetch", Boolean.class, true);
        infoFactory.addAttribute("fetchSize", Integer.class, true);
        infoFactory.addAttribute("connectionTimeout", Integer.class, true);
        infoFactory.addAttribute("timeout", Integer.class, true);
        infoFactory.addAttribute("statusCacheTimeout", Integer.class, true);
        infoFactory.addAttribute("appendBufferSize", Integer.class, true);
        infoFactory.addAttribute("connectionPoolSize", Integer.class, true);
        infoFactory.addAttribute("connectionPoolTimeout", Integer.class, true);
        infoFactory.addAttribute("separateStoreConnection", Boolean.class, true);
        infoFactory.addAttribute("allowReadOnlySelect", Boolean.class, true);
        infoFactory.addAttribute("authLoginDisable", Boolean.class, true);
        infoFactory.addAttribute("authPlainDisable", Boolean.class, true);
        infoFactory.addAttribute("startTLSEnable", Boolean.class, true);
        infoFactory.addAttribute("localaddress", String.class, true);
        infoFactory.addAttribute("localport", Integer.class, true);
        infoFactory.addAttribute("saslEnable", Boolean.class, true);
        infoFactory.addAttribute("saslMechanisms", String.class, true);
        infoFactory.addAttribute("saslAuthorizationId", String.class, true);
        infoFactory.addAttribute("socketFactoryClass", String.class, true);
        infoFactory.addAttribute("socketFactoryFallback", Boolean.class, true);
        infoFactory.addAttribute("socketFactoryPort", Integer.class, true);

        infoFactory.setConstructor(new String[]{"objectName", "properties", "host", "user",
                                                "port",
                                                "partialFetch",
                                                "fetchSize",
                                                "connectionTimeout",
                                                "timeout",
                                                "statusCacheTimeout",
                                                "appendBufferSize",
                                                "connectionPoolSize",
                                                "connectionPoolTimeout",
                                                "separateStoreConnection",
                                                "allowReadOnlySelect",
                                                "authLoginDisable",
                                                "authPlainDisable",
                                                "startTLSEnable",
                                                "localaddress",
                                                "localport",
                                                "saslEnable",
                                                "saslMechanisms",
                                                "saslAuthorizationId",
                                                "socketFactoryClass",
                                                "socketFactoryFallback",
                                                "socketFactoryPort"});

        GBEAN_INFO = infoFactory.getBeanInfo();
    }

    public static GBeanInfo getGBeanInfo() {
        return GBEAN_INFO;
    }
}
