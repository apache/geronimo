package org.apache.geronimo.tomcat;

import java.util.Map;
import javax.net.ssl.KeyManagerFactory;

import org.apache.geronimo.management.geronimo.SecureConnector;
import org.apache.geronimo.management.geronimo.WebManager;
import org.apache.geronimo.system.serverinfo.ServerInfo;
import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.gbean.GBeanInfoBuilder;

/**
 * A wrapper around a connector for the HTTPS protocol for Tomcat.  The
 * functionality is not different than the standard ConnectorGBean, but
 * there's an additional set of HTTPS attributes exposed.
 *
 * @version $Revision: 1.0$
 */
public class HttpsConnectorGBean extends ConnectorGBean implements TomcatSecureConnector {
    private final ServerInfo serverInfo;
    private String keystoreFileName;
    private String truststoreFileName;
    private String algorithm;

    public HttpsConnectorGBean(String name, String protocol, String host, int port, TomcatContainer container, ServerInfo serverInfo) throws Exception {
        super(name, protocol, host, port, container);

        if (serverInfo == null){
            throw new IllegalArgumentException("serverInfo cannot be null.");
        }

        this.serverInfo = serverInfo;
    }

    /**
     * Adds any special parameters before constructing the connector.
     *
     * @param protocol Should be one of the constants from WebContainer.
     * @param params   The map of parameters that will be used to initialize the connector.
     */
    protected void initializeParams(String protocol, Map params) {
        super.initializeParams(protocol, params);
        params.put("scheme", "https");
        params.put("secure", "true");
    }

    /**
     * Ensures that this implementation can handle the requested protocol.
     *
     * @param protocol
     */
    protected void validateProtocol(String protocol) {
        if(protocol != null && !protocol.equals(WebManager.PROTOCOL_HTTPS)) {
            throw new IllegalStateException("HttpsConnectorGBean only supports "+WebManager.PROTOCOL_HTTPS);
        }
    }

    /**
     * Gets the name of the keystore file that holds the server certificate
     * (and by default, the trusted CA certificates used for client certificate
     * authentication).  This is relative to the Geronimo home directory.
     */
    public String getKeystoreFileName() {
        return keystoreFileName; // don't look it up as we need it to be relative
    }

    /**
     * Sets the name of the keystore file that holds the server certificate
     * (and by default, the trusted CA certificates used for client certificate
     * authentication).  This is relative to the Geronimo home directory.
     */
    public void setKeystoreFileName(String name) {
        keystoreFileName = name;
        connector.setAttribute("keystoreFile", serverInfo.resolveServerPath(keystoreFileName));
    }

    public String getTruststoreFileName() {
        return truststoreFileName; // don't look it up as we need it to be relative
    }

    public void setTruststoreFileName(String name) {
        truststoreFileName = name;
        connector.setAttribute("truststoreFile", serverInfo.resolveServerPath(truststoreFileName));
    }

    /**
     * Sets the password used to access the keystore, and by default, used to
     * access the server private key inside the keystore.  Not all connectors
     * support configuring different passwords for those two features; if so,
     * a separate PrivateKeyPassword should be defined in an
     * implementation-specific connector interface.
     */
    public void setKeystorePassword(String password) {
        connector.setAttribute("keystorePass", password);
    }

    public void setTruststorePassword(String password) {
        connector.setAttribute("truststorePass", password);
    }

    /**
     * Gets the format of the entries in the keystore.  The default format for
     * Java keystores is JKS, though some connector implementations support
     * PCKS12 (and possibly other formats).
     */
    public String getKeystoreType() {
        return (String)connector.getAttribute("keystoreType");
    }

    /**
     * Sets the format of the entries in the keystore.  The default format for
     * Java keystores is JKS, though some connector implementations support
     * PCKS12 (and possibly other formats).
     */
    public void setKeystoreType(String type) {
        connector.setAttribute("keystoreType", type);
    }

    public String getTruststoreType() {
        return (String)connector.getAttribute("truststoreType");
    }

    public void setTruststoreType(String type) {
        connector.setAttribute("truststoreType", type);
    }

    /**
     * Gets the certificate algorithm used to access the keystore.  This may
     * be different for different JVM vendors, but should not usually be
     * changed otherwise.
     */
    public String getAlgorithm() {
        return algorithm;
    }

    /**
     * Sets the certificate algorithm used to access the keystore.  This may
     * be different for different JVM vendors, but should not usually be
     * changed otherwise.
     */
    public void setAlgorithm(String algorithm) {
        this.algorithm = algorithm;
        if ("default".equalsIgnoreCase(algorithm)) {
            algorithm = KeyManagerFactory.getDefaultAlgorithm();
        }
        connector.setAttribute("algorithm", algorithm);
    }

    /**
     * Gets the protocol used for secure communication.  This should usually
     * be TLS, though some JVM implementations (particularly some of IBM's)
     * may not be compatible with popular browsers unless this is changed to
     * SSL.
     */
    public String getSecureProtocol() {
        return (String)connector.getAttribute("sslProtocol");
    }

    /**
     * Gets the protocol used for secure communication.  This should usually
     * be TLS, though some JVM implementations (particularly some of IBM's)
     * may not be compatible with popular browsers unless this is changed to
     * SSL.  Don't change it if you're not having problems.
     */
    public void setSecureProtocol(String protocol) {
        connector.setAttribute("sslProtocol", protocol);
    }

    /**
     * Checks whether clients are required to authenticate using client
     * certificates in order to connect using this connector.  If enabled,
     * client certificates are validated using the trust store, which defaults
     * to the same keystore file, keystore type, and keystore password as the
     * regular keystore.  Some connector implementations may allow you to
     * configure those 3 values separately to use a different trust store.
     */
    public boolean isClientAuthRequired() {
        Object value = connector.getAttribute("clientAuth");
        return value == null ? false : new Boolean(value.toString()).booleanValue();
    }

    /**
     * Checks whether clients are required to authenticate using client
     * certificates in order to connect using this connector.  If enabled,
     * client certificates are validated using the trust store, which defaults
     * to the same keystore file, keystore type, and keystore password as the
     * regular keystore.  Some connector implementations may allow you to
     * configure those 3 values separately to use a different trust store.
     */
    public void setClientAuthRequired(boolean clientCert) {
        connector.setAttribute("clientAuth", new Boolean(clientCert));
    }

    public static final GBeanInfo GBEAN_INFO;

    static {
        GBeanInfoBuilder infoFactory = GBeanInfoBuilder.createStatic("Tomcat Connector", HttpsConnectorGBean.class, ConnectorGBean.GBEAN_INFO);
        infoFactory.addAttribute("keystoreFileName", String.class, true, true);
        infoFactory.addAttribute("truststoreFileName", String.class, true, true);
        infoFactory.addAttribute("algorithm", String.class, true, true);
        infoFactory.addAttribute("keystorePassword", String.class, true, true);
        infoFactory.addAttribute("truststorePassword", String.class, true, true);
// todo should we support this?
//        infoFactory.addAttribute("keyPassword", String.class, true, true);
        infoFactory.addAttribute("secureProtocol", String.class, true, true);
        infoFactory.addAttribute("keystoreType", String.class, true, true);
        infoFactory.addAttribute("truststoreType", String.class, true, true);
        infoFactory.addAttribute("clientAuthRequired", boolean.class, true, true);
        infoFactory.addInterface(TomcatSecureConnector.class);

        infoFactory.addReference("ServerInfo", ServerInfo.class, "GBean");
        infoFactory.setConstructor(new String[] { "name", "protocol", "host", "port", "TomcatContainer", "ServerInfo"});
        GBEAN_INFO = infoFactory.getBeanInfo();
    }

    public static GBeanInfo getGBeanInfo() {
        return GBEAN_INFO;
    }
}
