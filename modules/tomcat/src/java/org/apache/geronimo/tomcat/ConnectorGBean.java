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
package org.apache.geronimo.tomcat;

import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.net.InetAddress;
import java.util.Map;
import java.util.HashMap;

import org.apache.catalina.LifecycleException;
import org.apache.catalina.connector.Connector;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.gbean.GBeanInfoBuilder;
import org.apache.geronimo.gbean.GBeanLifecycle;
import org.apache.geronimo.j2ee.j2eeobjectnames.NameFactory;
import org.apache.geronimo.management.geronimo.WebManager;

/**
 * @version $Rev$ $Date$
 */
public class ConnectorGBean extends BaseGBean implements GBeanLifecycle, ObjectRetriever, TomcatWebConnector {
    private static final Log log = LogFactory.getLog(ConnectorGBean.class);
    public final static String CONNECTOR_CONTAINER_REFERENCE = "TomcatContainer";

    protected final Connector connector;
    private final TomcatContainer container;
    private String name;
    private String connectHost;

    public ConnectorGBean(String name, String protocol, String host, int port, TomcatContainer container) throws Exception {
        super(); // TODO: make it an attribute

        Map initParams = new HashMap();

        validateProtocol(protocol);
        
        //Default the host to listen on all address is one was not specified
        if (host == null){
            host = "0.0.0.0";
        }
        
        //Must have a port
        if (port == 0){
            throw new IllegalArgumentException("Must declare a port.");
        }

        initParams.put("address", host);
        initParams.put("port", Integer.toString(port));
        initializeParams(protocol, initParams);

        // Convert Geronimo standard values to Tomcat standard values
        // Only AJP requires an explicit protocol setting
        if(protocol != null && protocol.equals(WebManager.PROTOCOL_AJP)) {
            protocol = "AJP/1.3";
        } else {
            protocol = null;
        }

        if (name == null){
            throw new IllegalArgumentException("name cannot be null.");
        }

        if (container == null){
            throw new IllegalArgumentException("container cannot be null.");
        }
 
        this.name = name;
        this.container = container;

        //Create the Connector object
        connector = new Connector(protocol);


        //Set the parameters
        setParameters(connector, initParams);
        
    }

    /**
     * Adds any special parameters before constructing the connector.  Note:
     * all keys and values must be Strings.
     *
     * @param protocol Should be one of the constants from WebContainer.
     * @param params   The map of parameters that will be used to initialize the connector.
     */
    protected void initializeParams(String protocol, Map params) {}

    /**
     * Ensures that this implementation can handle the requested protocol.
     * @param protocol
     */
    protected void validateProtocol(String protocol) {
        if(protocol == null) {
            return;
        }
        if(protocol.equals(WebManager.PROTOCOL_HTTPS)) {
            throw new IllegalArgumentException("Use a HttpsConnectorGBean for an HTTPS connector");
        } else if(!protocol.equals(WebManager.PROTOCOL_HTTP) && !protocol.equals(WebManager.PROTOCOL_AJP)) {
            throw new IllegalArgumentException("Unrecognized protocol '"+protocol+"' (use the values of the PROTOCOL_* constants in WebConnector)");
        }
    }

    public String getName() {
        return name;
    }

    public Object getInternalObject() {
        return connector;
    }

    public void doStart() throws LifecycleException {
        container.addConnector(connector);
        connector.start();
        log.info(name + " connector started");
   }

    public void doStop() {
        try{
            connector.stop();
        } catch (LifecycleException e){
            log.error(e);
        }
        container.removeConnector(connector);
        log.info(name + " connector stopped");
    }

    public void doFail() {
        log.info(name + " connector failed");
        doStop();
    }

    public int getDefaultPort() {
        return getProtocol().equals(WebManager.PROTOCOL_AJP) ? -1 :
                getProtocol().equals(WebManager.PROTOCOL_HTTP) ? 80 :
                getProtocol().equals(WebManager.PROTOCOL_HTTPS) ? 443 : -1;
    }

    public String getConnectUrl() {
        if(connectHost == null) {
            String host = getHost();
            if(host == null || host.equals("0.0.0.0")) {
                InetAddress address = null;
                try {
                    address = InetAddress.getLocalHost();
                } catch (UnknownHostException e) {
                    host = "unknown-host";
                }
                if(address != null) {
                    host = address.getHostName();
                    if(host == null || host.equals("")) {
                        host = address.getHostAddress();
                    }
                }
            }
            connectHost = host;
        }
        return getProtocol().toLowerCase()+"://"+connectHost+(getPort() == getDefaultPort() ? "" : ":"+getPort());
    }

    /**
     * Gets the network protocol that this connector handles.
     */
    public String getProtocol() {
        String protocol = connector.getProtocol();
        if(protocol.indexOf("AJP") > -1) {
            return WebManager.PROTOCOL_AJP;
        } else if(connector.getScheme().equalsIgnoreCase("http")) {
            return WebManager.PROTOCOL_HTTP;
        } else if(connector.getScheme().equalsIgnoreCase("https")) {
            return WebManager.PROTOCOL_HTTPS;
        }
        throw new IllegalStateException("Unknown protocol '"+protocol+"' and scheme '"+connector.getScheme()+"'");
    }

    /**
     * Gets the network port that this connector listens on.
     */
    public int getPort() {
        return connector.getPort();
    }

    /**
     * Sets the network port that this connector listens on.
     */
    public void setPort(int port) {
        connector.setPort(port);
    }

    /**
     * Gets the hostname/IP that this connector listens on.
     */
    public String getHost() {
        Object value = connector.getAttribute("address");
        if(value == null) {
            return "0.0.0.0";
        } else if(value instanceof InetAddress) {
            return ((InetAddress)value).getHostAddress();
        } else return value.toString();
    }

    /**
     * Sets the hostname/IP that this connector listens on.  This is typically
     * most useful for machines with multiple network cards, but can be used
     * to limit a connector to only listen for connections from the local
     * machine (127.0.0.1).  To listen on all available network interfaces,
     * specify an address of 0.0.0.0.
     */
    public void setHost(String host) throws UnknownHostException {
        connector.setAttribute("address", host);
    }

    /**
     * Every connector must specify a property of type InetSocketAddress
     * because we use that to identify the network services to print a list
     * during startup.  However, this can be read-only since the host and port
     * are set separately using setHost and setPort.
     */
    public InetSocketAddress getListenAddress() {
        return new InetSocketAddress(getHost(), getPort());
    }

    /**
     * Gets the size of the buffer used to handle network data for this
     * connector.
     */
    public int getBufferSizeBytes() {
        Object value = connector.getAttribute("bufferSize");
        return value == null ? 2048 : Integer.parseInt(value.toString());
    }

    /**
     * Gets the size of the buffer used to handle network data for this
     * connector.
     */
    public void setBufferSizeBytes(int bytes) {
        connector.setAttribute("bufferSize", new Integer(bytes));
    }

    /**
     * Gets the maximum number of threads used to service connections from
     * this connector.
     */
    public int getMaxThreads() {
        Object value = connector.getAttribute("maxThreads");
        return value == null ? 200 : Integer.parseInt(value.toString());
    }

    /**
     * Sets the maximum number of threads used to service connections from
     * this connector.
     */
    public void setMaxThreads(int threads) {
        connector.setAttribute("maxThreads", new Integer(threads));
    }

    /**
     * Gets the maximum number of connections that may be queued while all
     * threads are busy.  Any requests received while the queue is full will
     * be rejected.
     */
    public int getAcceptQueueSize() {
        Object value = connector.getAttribute("acceptCount");
        return value == null ? 10 : Integer.parseInt(value.toString());
    }

    /**
     * Sets the maximum number of connections that may be queued while all
     * threads are busy.  Any requests received while the queue is full will
     * be rejected.
     */
    public void setAcceptQueueSize(int size) {
        connector.setAttribute("acceptCount", new Integer(size));
    }

    /**
     * Gets the amount of time the socket used by this connector will linger
     * after being closed.  -1 indicates that socket linger is disabled.
     */
    public int getLingerMillis() {
        Object value = connector.getAttribute("connectionLinger");
        return value == null ? -1 : Integer.parseInt(value.toString());
    }

    /**
     * Sets the amount of time the socket used by this connector will linger
     * after being closed.  Use -1 to disable socket linger.
     */
    public void setLingerMillis(int millis) {
        connector.setAttribute("connectionLinger", new Integer(millis));
    }

    /**
     * Gets whether the TCP_NODELAY flag is set for the sockets used by this
     * connector.  This usually enhances performance, so it should typically
     * be set.
     */
    public boolean isTcpNoDelay() {
        Object value = connector.getAttribute("tcpNoDelay");
        return value == null ? true : new Boolean(value.toString()).booleanValue();
    }

    /**
     * Sets whether the TCP_NODELAY flag is set for the sockets used by this
     * connector.  This usually enhances performance, so it should typically
     * be set.
     */
    public void setTcpNoDelay(boolean enable) {
        connector.setAttribute("tcpNoDelay", new Boolean(enable));
    }

    /**
     * Gets the network port to which traffic will be redirected if this
     * connector handles insecure traffic and the request requires a secure
     * connection.  Needless to say, this should point to another connector
     * configured for SSL.
     */
    public int getRedirectPort() {
        Object value = connector.getAttribute("redirectPort");
        return value == null ? 0 : Integer.parseInt(value.toString());
    }

    /**
     * Gets the network port to which traffic will be redirected if this
     * connector handles insecure traffic and the request requires a secure
     * connection.  Needless to say, this should point to another connector
     * configured for SSL.  If no SSL connector is available, any port can
     * be used as they all fail equally well.  :)
     */
    public void setRedirectPort(int port) {
        connector.setAttribute("redirectPort", new Integer(port));
    }

    public int getMinSpareThreads() {
        Object value = connector.getAttribute("minSpareThreads");
        return value == null ? 4 : Integer.parseInt(value.toString());
    }

    public void setMinSpareThreads(int threads) {
        connector.setAttribute("minSpareThreads", new Integer(threads));
    }

    public int getMaxSpareThreads() {
        Object value = connector.getAttribute("maxSpareThreads");
        return value == null ? 50 : Integer.parseInt(value.toString());
    }

    public void setMaxSpareThreads(int threads) {
        connector.setAttribute("maxSpareThreads", new Integer(threads));
    }

    public int getMaxHttpHeaderSizeBytes() {
        Object value = connector.getAttribute("maxHttpHeaderSize");
        return value == null ? 4096 : Integer.parseInt(value.toString());
    }

    public void setMaxHttpHeaderSizeBytes(int bytes) {
        connector.setAttribute("maxHttpHeaderSize", new Integer(bytes));
    }

    public boolean isHostLookupEnabled() {
        Object value = connector.getAttribute("enableLookups");
        return value == null ? true : new Boolean(value.toString()).booleanValue();
    }

    public void setHostLookupEnabled(boolean enabled) {
        connector.setAttribute("enableLookups", new Boolean(enabled));
    }

    public int getConnectionTimeoutMillis() {
        Object value = connector.getAttribute("connectionTimeout");
        return value == null ? 60000 : Integer.parseInt(value.toString());
    }

    public void setConnectionTimeoutMillis(int millis) {
        connector.setAttribute("connectionTimeout", new Integer(millis));
    }

    public boolean isUploadTimeoutEnabled() {
        Object value = connector.getAttribute("disableUploadTimeout");
        return value == null ? true : !new Boolean(value.toString()).booleanValue();
    }

    public void setUploadTimeoutEnabled(boolean enabled) {
        connector.setAttribute("disableUploadTimeout", new Boolean(!enabled));
    }


    public static final GBeanInfo GBEAN_INFO;

    static {
        GBeanInfoBuilder infoFactory = new GBeanInfoBuilder("Tomcat Connector", ConnectorGBean.class);
        infoFactory.addAttribute("name", String.class, true);
        infoFactory.addAttribute("protocol", String.class, true);
        infoFactory.addReference(CONNECTOR_CONTAINER_REFERENCE, TomcatContainer.class, NameFactory.GERONIMO_SERVICE);
        infoFactory.addOperation("getInternalObject");
        infoFactory.addInterface(TomcatWebConnector.class, new String[]{"host","port","bufferSizeBytes","maxThreads","acceptQueueSize","lingerMillis","tcpNoDelay","redirectPort","minSpareThreads","maxSpareThreads","maxHttpHeaderSizeBytes","hostLookupEnabled","connectionTimeoutMillis","uploadTimeoutEnabled","connectUrl",},
                                                           new String[]{"host","port","redirectPort"});
        infoFactory.setConstructor(new String[] { "name", "protocol", "host", "port", "TomcatContainer"});
        GBEAN_INFO = infoFactory.getBeanInfo();
    }

    public static GBeanInfo getGBeanInfo() {
        return GBEAN_INFO;
    }
}
