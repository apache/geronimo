/**
  * Licensed to the Apache Software Foundation (ASF) under one or more
  * contributor license agreements.  See the NOTICE file distributed with
  * this work for additional information regarding copyright ownership.
  * The ASF licenses this file to You under the Apache License, Version 2.0
  * (the "License"); you may not use this file except in compliance with
  * the License.  You may obtain a copy of the License at
  *
  *     http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
package org.apache.geronimo.yoko;

import java.io.IOException;
import java.net.ConnectException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;

import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLServerSocketFactory;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.geronimo.corba.ORBConfiguration;
import org.apache.geronimo.corba.security.config.ConfigUtil;
import org.apache.geronimo.corba.security.config.ssl.SSLCipherSuiteDatabase;
import org.apache.geronimo.corba.security.config.ssl.SSLConfig;
import org.apache.geronimo.corba.security.config.tss.TSSCompoundSecMechListConfig;
import org.apache.geronimo.corba.security.config.tss.TSSConfig;
import org.apache.geronimo.corba.security.config.tss.TSSSSLTransportConfig;
import org.apache.geronimo.corba.security.config.tss.TSSTransportMechConfig;
import org.apache.geronimo.corba.util.Util;
import org.apache.yoko.orb.OCI.IIOP.ConnectionHelper;
import org.apache.yoko.orb.OCI.ProfileInfo;
import org.apache.yoko.orb.OCI.ProfileInfoHolder;
import org.omg.CORBA.ORB;
import org.omg.CORBA.Policy;
import org.omg.CSIIOP.EstablishTrustInClient;
import org.omg.CSIIOP.EstablishTrustInTarget;
import org.omg.CSIIOP.NoProtection;
import org.omg.CSIIOP.TAG_CSI_SEC_MECH_LIST;
import org.omg.IIOP.ProfileBody_1_0;
import org.omg.IIOP.ProfileBody_1_0Helper;
import org.omg.IOP.IOR;


/**
 * Socket factory instance used to interface openejb2
 * with the Yoko ORB.  Also enables the ORB for
 * SSL-type connections.
 * @version $Revision: 500002 $ $Date: 2007-01-25 13:37:26 -0800 (Thu, 25 Jan 2007) $
 */
public class SocketFactory implements ConnectionHelper {

    private final static Log log = LogFactory.getLog(SocketFactory.class);

    // the configuration we're attached to. 
    private String configName = null;
    // The initialized SSLSocketFactory obtained from the Geronimo KeystoreManager.
    private SSLSocketFactory socketFactory = null;
    // The initialized SSLServerSocketFactory obtained from the Geronimo KeystoreManager.
    private SSLServerSocketFactory serverSocketFactory = null;
    // the ORB consumer that defines our configuration
    private ORBConfiguration config;
    // The initialized SSLConfig we use to retrieve the SSL socket factories.
    private SSLConfig sslConfig = null;
    // The set of cypher suites we use with the SSL connection.
    private String[] cipherSuites;
    // indicates whether client authentication is supported by this transport.
    private boolean clientAuthSupported;
    // indicates whether client authentication is required by this transport.
    private boolean clientAuthRequired;
    // supports and requires values used to retrieve the cipher suites.
    int supports = NoProtection.value;
    int requires = NoProtection.value;
    // the orb we're attached to
    private ORB orb;

    public SocketFactory() {
    }

    /**
     * Initialize the socket factory instance.
     *
     * @param orb        The hosting ORB.
     * @param configName The initialization parameter passed to the socket factor.
     *                   This contains the abstract name of our configurator,
     *                   which we retrieve from a registry.
     */
    public void init(ORB orb, String configName) {
        this.orb = orb;
        this.configName = configName;
        clientAuthSupported = false;
        clientAuthRequired = false;

        // retrieve the configuration from the config adapter registry.
        config = (ORBConfiguration)ORBConfigAdapter.getConfiguration(configName);
        if (config == null) {
            throw new RuntimeException("Unable to resolve ORB configuration " + configName);
        }
        // get the configuration from the hosting bean and decode what needs to be implemented.
        sslConfig = config.getSslConfig();
        TSSConfig tssConfig = config.getTssConfig();
        TSSTransportMechConfig transportMech = tssConfig.getTransport_mech();
        // if we have a transport mech defined, this is the configuration for any listeners we end up
        // creating.
        if (transportMech != null) {
            if (transportMech instanceof TSSSSLTransportConfig) {
                TSSSSLTransportConfig transportConfig = (TSSSSLTransportConfig) transportMech;
                supports = transportConfig.getSupports();
                requires = transportConfig.getRequires();
            }
        }

        // now set our listener creation flags based on the supports and requires values from the
        // TSS config.
        if ((supports & EstablishTrustInClient.value) != 0) {
            clientAuthSupported = true;

            if ((requires & EstablishTrustInClient.value) != 0) {
                clientAuthRequired = true;
            }
        }

        if ((supports & EstablishTrustInTarget.value) != 0) {
            clientAuthSupported = true;

            if ((requires & EstablishTrustInTarget.value) != 0) {
                clientAuthRequired = true;
            }
        }

        if (log.isDebugEnabled()) {
            log.debug("Creating Yoko SocketFactor for GBean " + configName);
            log.debug("   SUPPORTS: " + ConfigUtil.flags(supports));
            log.debug("   REQUIRES: " + ConfigUtil.flags(requires));
        }
    }

    /**
     * Create a client socket of the appropriate
     * type using the provided IOR and Policy information.
     *
     * @param ior      The target IOR of the connection.
     * @param policies Policies in effect for this ORB.
     * @param address  The target address of the connection.
     * @param port     The connection port.
     *
     * @return A Socket (either plain or SSL) configured for connection
     *         to the target.
     * @exception IOException
     * @exception ConnectException
     */
    public Socket createSocket(IOR ior, Policy[] policies, InetAddress address, int port) throws IOException, ConnectException {

        String host = address.getHostName();

        // the Yoko ORB will use both the primary and secondary targets for connetions, which 
        // sometimes gets us into trouble, forcing us to use an SSL target when we really need to 
        // use the plain socket connection.  Therefore, we will ignore what's passed to us, 
        // and extract the primary port information directly from the profile. 
        for (int i = 0; i < ior.profiles.length; i++) {
            if (ior.profiles[i].tag == org.omg.IOP.TAG_INTERNET_IOP.value) {
                try {
                    //
                    // Get the IIOP profile body
                    //
                    byte[] data = ior.profiles[i].profile_data;
                    ProfileBody_1_0 body = ProfileBody_1_0Helper.extract(Util.getCodec().decode_value(data, ProfileBody_1_0Helper.type()));

                    //
                    // Create new connector for this profile
                    //                                             
                    if (body.port < 0)
                        port = 0xffff + (int) body.port + 1;
                    else
                        port = (int) body.port;
                } catch (org.omg.IOP.CodecPackage.FormatMismatch e) {
                    // just keep the orignal port. 
                    break;
                } catch (org.omg.IOP.CodecPackage.TypeMismatch e) {
                    // just keep the orignal port. 
                    break;
                }

            }
        }

        try {
            ProfileInfoHolder holder = new ProfileInfoHolder();
            // we need to extract the profile information from the IOR to see if this connection has
            // any transport-level security defined.
            if (org.apache.yoko.orb.OCI.IIOP.Util.extractProfileInfo(ior, holder)) {
                ProfileInfo profileInfo = holder.value;
                for (int i = 0; i < profileInfo.components.length; i++) {
                    // we're lookoing for the security mechanism items.
                    if (profileInfo.components[i].tag == TAG_CSI_SEC_MECH_LIST.value) {
                        try {
                            // decode and pull the transport information.
                            TSSCompoundSecMechListConfig config = TSSCompoundSecMechListConfig.decodeIOR(Util.getCodec(), profileInfo.components[i]);
                            for (int j = 0; j < config.size(); j++) {
                                TSSTransportMechConfig transport_mech = config.mechAt(j).getTransport_mech();
                                if (transport_mech instanceof TSSSSLTransportConfig) {
                                    TSSSSLTransportConfig transportConfig = (TSSSSLTransportConfig) transport_mech;

                                    int supports = transportConfig.getSupports();
                                    int requires = transportConfig.getRequires();
                                    // override the port and hostname with what's configured here. 
                                    int sslPort = transportConfig.getPort();
                                    String sslHost = transportConfig.getHostname();

                                    if (log.isDebugEnabled()) {
                                        log.debug("IOR from target " + sslHost + ":" + sslPort);
                                        log.debug("   SUPPORTS: " + ConfigUtil.flags(supports));
                                        log.debug("   REQUIRES: " + ConfigUtil.flags(requires));
                                    }

                                    // TLS is configured.  If this is explicitly noprotection, then
                                    // just go create a plain socket using the configured port. 
                                    if ((NoProtection.value & requires) == NoProtection.value) {
                                        break;
                                    }
                                    // we need SSL, so create an SSLSocket for this connection.
                                    return createSSLSocket(sslHost, sslPort, supports, requires);
                                }
                            }
                        } catch (Exception e) {
                            // do nothing
                        }
                    }
                }
            }

            // if security is not required, just create a plain Socket.
            if (log.isDebugEnabled()) log.debug("Created plain endpoint to " + host + ":" + port);
            return new Socket(host, port);

        } catch (IOException ex) {
            log.error("Exception creating a client socket to "  + address.getHostName() + ":" + port, ex);
            throw ex;
        }
    }

    /**
     * Create a loopback connection to the hosting
     * ORB.
     *
     * @param address The address information for the server.
     * @param port    The target port.
     *
     * @return An appropriately configured socket based on the
     *         listener characteristics.
     * @exception IOException
     * @exception ConnectException
     */
    public Socket createSelfConnection(InetAddress address, int port) throws IOException, ConnectException {
        try {
            // the requires information tells us whether we created a plain or SSL listener.  We need to create one
            // of the matching type.

            if ((NoProtection.value & requires) == NoProtection.value) {
                if (log.isDebugEnabled()) log.debug("Created plain endpoint to " + address.getHostName() + ":" + port);
                return new Socket(address, port);
            }
            else {
                return createSSLSocket(address.getHostName(), port, supports, requires);
            }
        } catch (IOException ex) {
            log.error("Exception creating a client socket to "  + address.getHostName() + ":" + port, ex);
            throw ex;
        }
    }

    /**
     * Create a server socket listening on the given port.
     *
     * @param port    The target listening port.
     * @param backlog The desired backlog value.
     *
     * @return An appropriate server socket for this connection.
     * @exception IOException
     * @exception ConnectException
     */
    public ServerSocket createServerSocket(int port, int backlog)  throws IOException, ConnectException {
        try {
            // if no protection is required, just create a plain socket.
            if ((NoProtection.value & requires) == NoProtection.value) {
                if (log.isDebugEnabled()) log.debug("Created plain server socket for port " + port);
                return new ServerSocket(port, backlog);
            }
            else {
                // SSL is required.  Create one from the SSLServerFactory retrieved from the config.  This will
                // require additional QOS configuration after creation.
                SSLServerSocket serverSocket = (SSLServerSocket)getServerSocketFactory().createServerSocket(port, backlog);
                configureServerSocket(serverSocket);
                return serverSocket;
            }
        } catch (IOException ex) {
            log.error("Exception creating a server socket for port "  + port, ex);
            throw ex;
        }
    }

    /**
     * Create a server socket for this connection.
     *
     * @param port    The target listener port.
     * @param backlog The requested backlog value for the connection.
     * @param address The host address information we're publishing under.
     *
     * @return An appropriately configured ServerSocket for this
     *         connection.
     * @exception IOException
     * @exception ConnectException
     */
    public ServerSocket createServerSocket(int port, int backlog, InetAddress address) throws IOException, ConnectException {
        try {
            // if no protection is required, just create a plain socket.
            if ((NoProtection.value & requires) == NoProtection.value) {
                if (log.isDebugEnabled()) log.debug("Created plain server socket for port " + port);
                return new ServerSocket(port, backlog, address);
            }
            else {
                // SSL is required.  Create one from the SSLServerFactory retrieved from the config.  This will
                // require additional QOS configuration after creation.
                SSLServerSocket serverSocket = (SSLServerSocket)getServerSocketFactory().createServerSocket(port, backlog, address);
                configureServerSocket(serverSocket);
                return serverSocket;
            }
        } catch (IOException ex) {
            log.error("Exception creating a client socket to "  + address.getHostName() + ":" + port, ex);
            throw ex;
        }
    }

    /**
     * On-demand creation of an SSL socket factory, using the provided
     * Geronimo SSLConfig information.
     *
     * @return The SSLSocketFactory this connection should be using to create
     *         secure connections.
     */
    private SSLSocketFactory getSocketFactory() throws IOException {
        // first use?
        if (socketFactory == null) {
            // the SSLConfig is optional, so if it's not there, use the default SSLSocketFactory.
            if (sslConfig == null) {
                socketFactory = (SSLSocketFactory) SSLSocketFactory.getDefault();
            }
            else {
                // ask the SSLConfig bean to create a factory for us.
                try {
                    socketFactory = (SSLSocketFactory)sslConfig.createSSLFactory(Thread.currentThread().getContextClassLoader());
                } catch (Exception e) {
                    log.error("Unable to create client SSL socket factory", e);
                    throw new IOException("Unable to create client SSL socket factory: " + e.getMessage());
                }
            }
        }
        return socketFactory;
    }

    /**
     * On-demand creation of an SSL server socket factory, using the provided
     * Geronimo SSLConfig information.
     *
     * @return The SSLServerSocketFactory this connection should be using to create
     *         secure connections.
     */
    private SSLServerSocketFactory getServerSocketFactory() throws IOException {
        // first use?
        if (serverSocketFactory == null) {
            // the SSLConfig is optional, so if it's not there, use the default SSLSocketFactory.
            if (sslConfig == null) {
                serverSocketFactory = (SSLServerSocketFactory) SSLServerSocketFactory.getDefault();
            }
            else {
                try {
                    serverSocketFactory = (SSLServerSocketFactory)sslConfig.createSSLServerFactory(Thread.currentThread().getContextClassLoader());
                } catch (Exception e) {
                    log.error("Unable to create server SSL socket factory", e);
                    throw new IOException("Unable to create server SSL socket factory: " + e.getMessage());
                }
            }
            // we have a socket factory....now get our cipher suite set based on our requirements and what's
            // available from the factory.
            if (cipherSuites == null) {
                cipherSuites = SSLCipherSuiteDatabase.getCipherSuites(requires, supports, serverSocketFactory.getSupportedCipherSuites());
            }
            // There's a bit of a timing problem with server-side ORBs.  Part of the ORB shutdown is to
            // establish a self-connection to shutdown the acceptor threads.  This requires a client
            // SSL socket factory.  Unfortunately, if this is occurring during server shutdown, the
            // FileKeystoreManager will get a NullPointerException because some name queries fail because
            // things are getting shutdown.  Therefore, if we need the server factory, assume we'll also
            // need the client factory to shutdown, and request it now.
            getSocketFactory();
        }
        return serverSocketFactory;
    }


    /**
     * Set the server socket configuration to our required
     * QOS values.
     *
     * @param serverSocket
     *               The newly created SSLServerSocket.
     *
     * @exception IOException
     * @exception ConnectException
     */
    private void configureServerSocket(SSLServerSocket serverSocket) throws IOException, ConnectException {
        // set the authentication value and cipher suite info.
        serverSocket.setEnabledCipherSuites(cipherSuites);
        serverSocket.setWantClientAuth(clientAuthSupported);
        serverSocket.setNeedClientAuth(clientAuthRequired);
        serverSocket.setSoTimeout(60 * 1000);

        if (log.isDebugEnabled()) {
            log.debug("Created SSL server socket on port " + serverSocket.getLocalPort());
            log.debug("    client authentication " + (clientAuthSupported ? "SUPPORTED" : "UNSUPPORTED"));
            log.debug("    client authentication " + (clientAuthRequired ? "REQUIRED" : "OPTIONAL"));
            log.debug("    cipher suites:");

            for (int i = 0; i < cipherSuites.length; i++) {
                log.debug("    " + cipherSuites[i]);
            }
        }
    }

    /**
     * Create an SSL client socket using the IOR-encoded
     * security characteristics.
     *
     * @param host     The target host name.
     * @param port     The target connection port.
     * @param supports The connections supports information.
     * @param requires The connection requires information.
     *
     * @return An appropriately configured client SSLSocket.
     * @exception IOException
     * @exception ConnectException
     */
    private Socket createSSLSocket(String host, int port, int supports, int requires) throws IOException, ConnectException {
        SSLSocketFactory factory = getSocketFactory();
        SSLSocket socket = (SSLSocket) factory.createSocket(host, port);

        socket.setSoTimeout(60 * 1000);

        // figure out the supports and requires information from the flag values.
        boolean authSupported = false;
        boolean authRequired = false;

        if ((supports & EstablishTrustInClient.value) != 0) {
            authSupported = true;

            if ((requires & EstablishTrustInClient.value) != 0) {
                authRequired = true;
            }
        }

        if ((supports & EstablishTrustInTarget.value) != 0) {
            authSupported = true;

            if ((requires & EstablishTrustInTarget.value) != 0) {
                authSupported = true;
            }
        }

        // get a set of cipher suites appropriate for this connections requirements.
        // We request this for each connection, since the outgoing IOR's requirements may be different from
        // our server listener requirements.
        String[] iorSuites = SSLCipherSuiteDatabase.getCipherSuites(requires, supports, factory.getSupportedCipherSuites());
        socket.setEnabledCipherSuites(iorSuites);
        socket.setWantClientAuth(authSupported);
        socket.setNeedClientAuth(authRequired);

        if (log.isDebugEnabled()) {
            log.debug("Created SSL socket to " + host + ":" + port);
            log.debug("    client authentication " + (authSupported ? "SUPPORTED" : "UNSUPPORTED"));
            log.debug("    client authentication " + (authRequired ? "REQUIRED" : "OPTIONAL"));
            log.debug("    cipher suites:");

            for (int i = 0; i < iorSuites.length; i++) {
                log.debug("    " + iorSuites[i]);
            }
        }
        return socket;
    }
}
