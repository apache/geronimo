/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.apache.geronimo.shell.geronimo;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.Iterator;
import java.io.IOException;

import javax.management.remote.JMXServiceURL;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.rmi.RMIConnectorServer;
import javax.management.MBeanServerConnection;
import javax.management.ObjectName;
import javax.rmi.ssl.SslRMIClientSocketFactory;

//
// FIXME: It should be possible to query state with-out any Geronimo classes,
//        just using JMX interfaces.
//

import org.apache.geronimo.gbean.AbstractName;
import org.apache.geronimo.gbean.AbstractNameQuery;
import org.apache.geronimo.kernel.Kernel;
import org.apache.geronimo.kernel.config.PersistentConfigurationList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Helper to communicate with a remote server via JMX.
 *
 * @version $Rev$ $Date$
 */

public class ServerProxy
{
    private static final Logger log = LoggerFactory.getLogger(ServerProxy.class);

    private JMXServiceURL url;
    
    private JMXConnector connector;

    private Map environment;

    private MBeanServerConnection mbeanConnection;

    private Throwable lastError;

    public ServerProxy(final JMXServiceURL url, final Map environment) throws Exception {
        assert url != null;
        assert environment != null;

        this.url = url;
        this.environment = environment;
        
        log.debug("Initialized with URL: " + url + ", environment: " + environment);
    }

    public ServerProxy(String hostname, int port, String username, String password) throws Exception {
        this(hostname, port, username, password, false);
    }
    
    public ServerProxy(String hostname, int port, String username, String password, boolean secure) throws Exception {
        this(createJMXServiceURL(hostname, port, secure), username, password, secure);
    }

    public ServerProxy(String url, String username, String password) throws Exception {
        this(url, username, password, false);
    }
    
    public ServerProxy(String url, String username, String password, boolean secure) throws Exception {
        assert url != null;
        assert username != null;
        assert password != null;
        
        this.url = new JMXServiceURL(url);
        this.environment = new HashMap();
        this.environment.put(JMXConnector.CREDENTIALS, new String[] {username, password});
        if (secure) {
            SslRMIClientSocketFactory csf = new SslRMIClientSocketFactory();
            this.environment.put(RMIConnectorServer.RMI_CLIENT_SOCKET_FACTORY_ATTRIBUTE, csf);
        }

        log.debug("Initialized with URL: " + url + ", environment: " + environment);
    }

    public ServerProxy(JMXConnector connector) throws Exception {
        this.mbeanConnection = connector.getMBeanServerConnection();
    }
    
    private static String createJMXServiceURL(String hostname, int port, boolean secure) {
        String connectorName = (secure) ? "/JMXSecureConnector" : "/jmxrmi";
        return "service:jmx:rmi://" + hostname + "/jndi/rmi://" + hostname + ":" + port + connectorName;
    }
    
    private MBeanServerConnection getConnection() throws IOException {
        if (this.mbeanConnection == null) {
            log.debug("Connecting to: " + url);
            
            connector = JMXConnectorFactory.connect(url, environment);
            this.mbeanConnection = connector.getMBeanServerConnection();
            
            log.debug("Connected");
        }

        return mbeanConnection;
    }
    
    public void closeConnection() {
        if (connector != null) {
            try {
                connector.close();
            } catch (IOException e) {
                String msg = "Failed to close JMXConnector";
                if (log.isTraceEnabled()) {
                    log.trace(msg,e);
                }
                if (log.isDebugEnabled()) {
                    log.debug(msg + ":" + e);
                }
            }
        }
    }
    public boolean isFullyStarted() {
        boolean fullyStarted = true;

        try {
            AbstractNameQuery query = new AbstractNameQuery(PersistentConfigurationList.class.getName());
            ClassLoader OldCL = Thread.currentThread().getContextClassLoader();
            Thread.currentThread().setContextClassLoader(AbstractName.class.getClassLoader());
            Set result = listGBeans(query);
            Iterator iter = result.iterator();
           
            while (iter.hasNext()) {
                
                Object abstractName = iter.next();
                try {
                    AbstractName name = (AbstractName) abstractName;
                    boolean started = getBooleanAttribute(name, "kernelFullyStarted");

                    if (!started) {
                        fullyStarted = false;
                        break;
                    }
                } finally {
                    Thread.currentThread().setContextClassLoader(OldCL);
                }
            }
            
            
        }
        catch (IOException e) {
            String msg = "Connection failure; ignoring";
            if (log.isTraceEnabled()) {
                log.trace(msg, e);
            }
            else if (log.isDebugEnabled()) {
                log.debug(msg + ": " + e);
            }
            
            fullyStarted = false;
            lastError = e;
        }
        catch (Exception e) {
            String msg = "Unable to determine if the server is fully started; ignoring";
            if (log.isTraceEnabled()) {
                log.trace(msg, e);
            }
            else if (log.isDebugEnabled()) {
                log.debug(msg + ": " + e);
            }
            
            fullyStarted = false;
            lastError = e;
        }
        
        return fullyStarted;
    }
    public String getGeronimoHome() {
        String home = null;

        try {
            ObjectName systemInfoQuery = new ObjectName("*:name=ServerInfo,j2eeType=GBean,*");

            getConnection();

            Set set = mbeanConnection.queryNames(systemInfoQuery, null);

            if (set.size() > 0) {
                ObjectName found = (ObjectName)set.iterator().next();
                home = (String)mbeanConnection.getAttribute(found, "currentBaseDirectory");
            }
        }
        catch (IOException e) {
            String msg = "Connection failure; ignoring";
            if (log.isTraceEnabled()) {
                log.trace(msg, e);
            }
            else if (log.isDebugEnabled()) {
                log.debug(msg + ": " + e);
            }
            
            lastError = e;
        }
        catch (Exception e) {
            String msg = "Unable to determine if the server is fully started; ignoring";
            if (log.isTraceEnabled()) {
                log.trace(msg, e);
            }
            else if (log.isDebugEnabled()) {
                log.debug(msg + ": " + e);
            }
            
            lastError = e;
        }
        
        return home;
    }

    public Throwable getLastError() {
        return lastError;
    }

    public void shutdown() throws Exception {
        MBeanServerConnection connection = getConnection();
        Set<ObjectName> objectNameSet =
            connection.queryNames(new ObjectName("osgi.core:type=framework,*"), null);
        if (objectNameSet.isEmpty()) {
            throw new Exception("Framework mbean not found");
        } else if (objectNameSet.size() == 1) {
            connection.invoke(objectNameSet.iterator().next(), "stopBundle",
                              new Object[] { 0 }, new String[] { long.class.getName() });
        } else {
            throw new Exception("Found multiple framework mbeans");
        }
    }

    //
    // Kernel invocation helpers
    //

    private Object invoke(final String operation, final Object[] args, final String[] signature) throws Exception {
        assert operation != null;
        assert args != null;
        assert signature != null;

        return getConnection().invoke(Kernel.KERNEL, operation, args, signature);
    }

    private Object invoke(final String operation, final Object[] args) throws Exception {
        assert args != null;

        String[] signature = new String[args.length];
        for (int i=0; i<args.length; i++) {
            signature[i] = args[i].getClass().getName();
        }

        return invoke(operation, args, signature);
    }

    private Object invoke(final String operation) throws Exception {
        return invoke(operation, new Object[0]);
    }

    private Set listGBeans(final AbstractNameQuery query) throws Exception {
        return (Set)invoke("listGBeans", new Object[] { query });
    }

    private Object getAttribute(final AbstractName name, final String attribute) throws Exception {
        assert name != null;
        assert attribute != null;

        return invoke("getAttribute", new Object[] { name, attribute });
    }

    private boolean getBooleanAttribute(final AbstractName name, final String attribute) throws Exception {
        Object obj = getAttribute(name, attribute);
        if (obj instanceof Boolean) {
            return ((Boolean)obj).booleanValue();
        }
        else {
            throw new RuntimeException("Attribute is not of type Boolean: " + attribute);
        }
    }
}
