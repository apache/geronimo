/**
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.apache.geronimo.deployment.plugin.factories;

import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import javax.enterprise.deploy.spi.DeploymentManager;
import javax.enterprise.deploy.spi.exceptions.DeploymentManagerCreationException;
import javax.enterprise.deploy.spi.factories.DeploymentFactory;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;
import javax.management.remote.rmi.RMIConnectorServer;
import javax.rmi.ssl.SslRMIClientSocketFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.geronimo.deployment.spi.ModuleConfigurer;
import org.apache.geronimo.deployment.plugin.DisconnectedDeploymentManager;
import org.apache.geronimo.deployment.plugin.jmx.LocalDeploymentManager;
import org.apache.geronimo.deployment.plugin.jmx.RemoteDeploymentManager;
import org.apache.geronimo.kernel.KernelRegistry;

/**
 * Base implementation of JSR88 DeploymentFactory.
 *
 * This will create a DeploymentManager using a local Geronimo kernel
 * to contain the GBeans that are responsible for deploying each module
 * type.
 *
 * @version $Rev$ $Date$
 */
public class BaseDeploymentFactory implements DeploymentFactory {
    private static final Logger log = LoggerFactory.getLogger(BaseDeploymentFactory.class);

    public static final String URI_PREFIX = "deployer:geronimo:";
    private static final String DEFAULT_HOST = "localhost";
    private static final int DEFAULT_PORT = 1099;

    public BaseDeploymentFactory() {
    }

    public String getDisplayName() {
        return "Apache Geronimo";
    }

    public String getProductVersion() {
        return "1.0";
    }

    public boolean handlesURI(String uri) {
        return parseURI(uri) != null;
    }

    private ConnectParams parseURI(String uri) {
        uri = uri.trim();
        if (log.isDebugEnabled()) {
            log.debug("Parsing URI=" + uri);
        }
        if(!uri.startsWith(URI_PREFIX)) {
            return null;
        }
        uri = uri.substring(URI_PREFIX.length());
        int pos = uri.indexOf(":");
        String protocol = pos == -1 ? uri : uri.substring(0, pos);
        uri = pos == -1 ? "" : uri.substring(pos+1);
        if(protocol.equals("jmx") || protocol.equals("jmxs")) {
            boolean secure = protocol.equals("jmxs");
            if(!uri.startsWith("//")) {
                return new ConnectParams(protocol, DEFAULT_HOST, DEFAULT_PORT, secure);
            }
            uri = uri.substring(2);
            pos = uri.indexOf(':');
            if(pos == -1) {
                return new ConnectParams(protocol, uri.equals("") ? DEFAULT_HOST : uri, DEFAULT_PORT, secure);
            }
            if(uri.indexOf('/', pos+1) > -1) {
                return null;
            }
            if(uri.indexOf(':', pos+1) > -1) {
                return null;
            }
            String host = uri.substring(0, pos);
            String port = uri.substring(pos+1);
            try {
                return new ConnectParams(protocol, host.equals("") ? DEFAULT_HOST : host, Integer.parseInt(port), secure);
            } catch (NumberFormatException e) {
                return null;
            }
        } else if(protocol.equals("inVM")) {
            if(uri.startsWith("//")) {
                String kernel = uri.substring(2);
                return new ConnectParams(protocol, kernel, -1);
            } else {
                return new ConnectParams(protocol,
                        KernelRegistry.getSingleKernel() == null ? null : KernelRegistry.getSingleKernel().getKernelName(),
                        -1);
            }
        } else return null;
    }

    public DeploymentManager getDisconnectedDeploymentManager(String uri) throws DeploymentManagerCreationException {
        if (!handlesURI(uri)) {
            return null;
        }
        
        Collection<ModuleConfigurer> moduleConfigurers = getModuleConfigurers();
        return new DisconnectedDeploymentManager(moduleConfigurers);
    }

    public DeploymentManager getDeploymentManager(String uri, String username, String password) throws DeploymentManagerCreationException {
        ConnectParams params = parseURI(uri);
        if (params == null) {
            return null;
        }
        if (log.isDebugEnabled()) {
            log.debug("Using protocol=" + params.getProtocol() + ", host=" + params.getHost() + ", port=" + params.getPort());
        }

        try {
            if (params.getProtocol().equals("jmx") || params.getProtocol().equals("jmxs")) {
                return newRemoteDeploymentManager(username, password, params);
            } else if(params.getProtocol().equals("inVM")) {
                return new LocalDeploymentManager(KernelRegistry.getKernel(params.getHost()));
            } else {
                throw new DeploymentManagerCreationException("Invalid URI: " + uri);
            }
        } catch (RuntimeException e) {
            // some DeploymentManagerFactories suppress unchecked exceptions - log and rethrow
            log.error(e.getMessage(), e);
            throw e;
        } catch (Error e) {
            // some DeploymentManagerFactories suppress unchecked exceptions - log and rethrow
            log.error(e.getMessage(), e);
            throw e;
        } catch (IOException e) {
            log.error(e.getMessage(), e);
            throw new DeploymentManagerCreationException(e.getMessage());
        }
    }

    protected Collection<ModuleConfigurer> getModuleConfigurers() throws DeploymentManagerCreationException {
        return Collections.EMPTY_LIST;
    }

    protected DeploymentManager newRemoteDeploymentManager(String username, String password, ConnectParams params) throws DeploymentManagerCreationException, AuthenticationFailedException {
        Map environment = new HashMap();
        String[] credentials = new String[]{username, password};
        environment.put(JMXConnector.CREDENTIALS, credentials);
        environment.put(JMXConnectorFactory.DEFAULT_CLASS_LOADER, BaseDeploymentFactory.class.getClassLoader());
        String connectorName = "/jmxrmi";
        if (params.isSecure()) {
            connectorName = "/JMXSecureConnector";
            SslRMIClientSocketFactory csf = new SslRMIClientSocketFactory();
            environment.put(RMIConnectorServer.RMI_CLIENT_SOCKET_FACTORY_ATTRIBUTE, csf);
        }    
        try {
            // if ipv6 numeric address wrap with "[" "]"
            String host = params.getHost();
            if (host.indexOf(":") >= 0) {
                host = "[" + host + "]";
            }
            if (log.isDebugEnabled()) {
                log.debug("Using JMXServiceURL with host=" + host + ", port=" + params.getPort() + ", secure=" + params.isSecure());
            }
            JMXServiceURL address = new JMXServiceURL("service:jmx:rmi:///jndi/rmi://"+ host +":"+params.getPort()+connectorName);
            JMXConnector jmxConnector = JMXConnectorFactory.connect(address, environment);
            RemoteDeploymentManager manager = getRemoteDeploymentManager();
            manager.init(jmxConnector, host);
            if(!manager.isSameMachine()) {
                manager.setAuthentication(username, password);
            }
            return manager;
        } catch (IOException e) {
            DeploymentManagerCreationException deploymentManagerCreationException = 
                    (DeploymentManagerCreationException) new DeploymentManagerCreationException(e.getMessage()).initCause(e);
            log.debug("throwing ", deploymentManagerCreationException);
            throw deploymentManagerCreationException;
        } catch (SecurityException e) {
            AuthenticationFailedException authenticationFailedException = 
                    (AuthenticationFailedException) new AuthenticationFailedException("Invalid login.").initCause(e);
            log.debug("throwing ", authenticationFailedException);
            throw authenticationFailedException;
        }
    }

    protected RemoteDeploymentManager getRemoteDeploymentManager() throws DeploymentManagerCreationException {
        Collection<ModuleConfigurer> moduleConfigurers = getModuleConfigurers();
        return new RemoteDeploymentManager(moduleConfigurers);
    }

    private final static class ConnectParams {
        private String protocol;
        private String host;
        private int port;
        private boolean secure;

        public ConnectParams(String protocol, String host, int port) {
            this(protocol, host, port, false);
        }
        
        public ConnectParams(String protocol, String host, int port, boolean secure) {
            this.protocol = protocol;
            this.host = host;
            this.port = port;
            this.secure = secure;
        }

        public String getProtocol() {
            return protocol;
        }

        public String getHost() {
            return host;
        }

        public int getPort() {
            return port;
        }

        public boolean isSecure() {
            return secure;
        }
        
        public String toString() {
            return protocol+" / "+host+" / "+port;
        }
    }

}
