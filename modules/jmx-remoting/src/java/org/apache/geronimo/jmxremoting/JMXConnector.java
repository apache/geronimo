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
package org.apache.geronimo.jmxremoting;

import java.net.InetSocketAddress;
import java.net.MalformedURLException;
import java.util.HashMap;
import java.util.Map;
import javax.management.remote.JMXConnectorServer;
import javax.management.remote.JMXConnectorServerFactory;
import javax.management.remote.JMXConnectorServerMBean;
import javax.management.remote.JMXServiceURL;
import javax.management.remote.JMXConnectionNotification;
import javax.management.MBeanServer;
import javax.management.NotificationFilterSupport;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.gbean.GBeanInfoBuilder;
import org.apache.geronimo.gbean.GBeanLifecycle;
import org.apache.geronimo.kernel.Kernel;
import org.apache.geronimo.kernel.jmx.MBeanServerDelegate;

/**
 * A Connector that supports the server sideof JSR 160 JMX Remoting.
 *
 * @version $Rev$ $Date$
 */
public class JMXConnector implements GBeanLifecycle {
    private final Kernel kernel;
    private final Log log;
    private final ClassLoader classLoader;
    private String url;
    private String applicationConfigName;
    private Authenticator authenticator;

    private JMXConnectorServer server;
    
    /**
     * Constructor for creating the connector. The ClassLoader must be
     * able to load all the LoginModules used in the JAAS login
     *
     * @param kernel a reference to the kernel
     * @param objectName this connector's object name
     * @param classLoader the classLoader used to create this connector
     */
    public JMXConnector(Kernel kernel, String objectName, ClassLoader classLoader) {
        this.kernel = kernel;
        this.classLoader = classLoader;
        log = LogFactory.getLog(objectName);
    }

    /**
     * Return the name of the JAAS Application Configuration Entry this
     * connector uses to authenticate users. If null, users are not
     * be authenticated (not recommended).
     *
     * @return the authentication configuration name
     */
    public String getApplicationConfigName() {
        return applicationConfigName;
    }

    /**
     * Set the name of the JAAS Application Configuration Entry this
     * connector should use to authenticate users. If null, users will not
     * be authenticated (not recommended).
     *
     * @param applicationConfigName the authentication configuration name
     */
    public void setApplicationConfigName(String applicationConfigName) {
        this.applicationConfigName = applicationConfigName;
    }

    /**
     * Every connector must specify a property of type InetSocketAddress
     * because we use that to identify the network services to print a list
     * during startup.  However, this can be read-only since the host and port
     * are set in the url attribute.
     */
    public InetSocketAddress getListenAddress() {
        return new InetSocketAddress(getHost(), getPort());
    }    
    
    /**
     * Return the JMX host (extracted from the JMX URL) for this connector.
     *
     * @return the JMX host for this connector
     */
    public String getHost() {
        if (server != null )
            return server.getAddress().getHost();
        else if (url != null && url.length() != 0) {
            // server not started so get host from url attribute
            try {
                JMXServiceURL serviceURL = new JMXServiceURL(url);
                return serviceURL.getHost();
            }
            catch (MalformedURLException e){
                return "unknown-host";
            }
        } else
            return "unknown-host";
    }      
    
    /**
     * Return the JMX port (extracted from the JMX URL) for this connector.
     *
     * @return the JMX port for this connector
     */
    public int getPort() {
        if (server != null )
            return server.getAddress().getPort();
        else if (url != null && url.length() != 0) {
            // server not started so get port from url attribute
            try {
                JMXServiceURL serviceURL = new JMXServiceURL(url);
                return serviceURL.getPort();
            }
            catch (MalformedURLException e){
                return 0;
            }
        } else
            return 0;
    }    
    
    /**
     * Return the JMX URL for this connector.
     *
     * @return the JMX URL for this connector
     */
    public String getUrl() {
        return url;
    }

    /**
     * Set the JMX URL for this connector
     *
     * @param url the JMX URL for this connector
     */
    public void setUrl(String url) {
        this.url = url;
    }

    public void doStart() throws Exception {
        JMXServiceURL serviceURL = new JMXServiceURL(url);
        Map env = new HashMap();
        if (applicationConfigName != null) {
            authenticator = new Authenticator(applicationConfigName, classLoader);
            env.put(JMXConnectorServer.AUTHENTICATOR, authenticator);
        } else {
            log.warn("Starting unauthenticating JMXConnector for " + serviceURL);
        }
        MBeanServer mbeanServer = new MBeanServerDelegate(kernel);
        server = JMXConnectorServerFactory.newJMXConnectorServer(serviceURL, env, mbeanServer);
        NotificationFilterSupport filter = new NotificationFilterSupport();
        filter.enableType(JMXConnectionNotification.OPENED);
        filter.enableType(JMXConnectionNotification.CLOSED);
        filter.enableType(JMXConnectionNotification.FAILED);
        server.addNotificationListener(authenticator, filter, null);
        server.start();
        log.debug("Started JMXConnector " + server.getAddress());
    }

    public void doStop() throws Exception {
        try {
        	  server.stop();
        } catch (java.io.IOException e) {
        	  // java.io.IOException is expected.
        } catch (Exception e) {
        	  // Otherwise, something bad happened.  Rethrow the exception.
        	  throw e;
        }
        finally {
          server = null;
          log.debug("Stopped JMXConnector " + url);
        }
    }

    public void doFail() {
        try {
            doStop();
            log.warn("Failure in JMXConnector " + url);
        } catch (Exception e) {
            log.warn("Error stopping JMXConnector after failure", e);
        }
    }

    public static final GBeanInfo GBEAN_INFO;

    static {
        GBeanInfoBuilder infoFactory = GBeanInfoBuilder.createStatic("JMX Remoting Connector", JMXConnector.class); //TODO just  a gbean?
        infoFactory.addAttribute("url", String.class, true, true);
        infoFactory.addAttribute("applicationConfigName", String.class, true, true);
        infoFactory.addAttribute("kernel", Kernel.class, false);
        infoFactory.addAttribute("objectName", String.class, false);
        infoFactory.addAttribute("classLoader", ClassLoader.class, false);
        infoFactory.addAttribute("listenAddress", InetSocketAddress.class, false);
        infoFactory.setConstructor(new String[]{"kernel", "objectName", "classLoader"});
        GBEAN_INFO = infoFactory.getBeanInfo();
    }

    public static GBeanInfo getGBeanInfo() {
        return GBEAN_INFO;
    }
}
