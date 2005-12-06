/**
 *
 * Copyright 2003-2004 The Apache Software Foundation
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

package org.apache.geronimo.deployment.plugin.factories;

import org.apache.geronimo.deployment.plugin.DisconnectedDeploymentManager;
import org.apache.geronimo.deployment.plugin.jmx.LocalDeploymentManager;
import org.apache.geronimo.deployment.plugin.jmx.RemoteDeploymentManager;
import org.apache.geronimo.kernel.KernelRegistry;

import javax.enterprise.deploy.shared.factories.DeploymentFactoryManager;
import javax.enterprise.deploy.spi.DeploymentManager;
import javax.enterprise.deploy.spi.exceptions.DeploymentManagerCreationException;
import javax.enterprise.deploy.spi.factories.DeploymentFactory;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Implementation of JSR88 DeploymentFactory.
 *
 * This will create a DeploymentManager using a local Geronimo kernel
 * to contain the GBeans that are responsible for deploying each module
 * type.
 *
 * @version $Rev$ $Date$
 */
public class DeploymentFactoryImpl implements DeploymentFactory {
    public static final String URI_PREFIX = "deployer:geronimo:";
    private static final int DEFAULT_PORT = 1099;

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
        if(!uri.startsWith(URI_PREFIX)) {
            return null;
        }
        uri = uri.substring(URI_PREFIX.length());
        int pos = uri.indexOf(":");
        String protocol = pos == -1 ? uri : uri.substring(0, pos);
        uri = pos == -1 ? "" : uri.substring(pos+1);
        if(protocol.equals("jmx")) {
            if(!uri.startsWith("//")) {
                return new ConnectParams(protocol, "localhost", DEFAULT_PORT);
            }
            uri = uri.substring(2);
            pos = uri.indexOf(':');
            if(pos == -1) {
                return new ConnectParams(protocol, uri.equals("") ? "localhost" : uri, DEFAULT_PORT);
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
                return new ConnectParams(protocol, host.equals("") ? "localhost" : host, Integer.parseInt(port));
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

        return new DisconnectedDeploymentManager();
    }

    public DeploymentManager getDeploymentManager(String uri, String username, String password) throws DeploymentManagerCreationException {
        ConnectParams params = parseURI(uri);
        if (params == null) {
            return null;
        }

        try {
            if (params.getProtocol().equals("jmx")) {
                Map environment = new HashMap();
                String[] credentials = new String[]{username, password};
                environment.put(JMXConnector.CREDENTIALS, credentials);
                try {
                    JMXServiceURL address = new JMXServiceURL("service:jmx:rmi:///jndi/rmi://"+params.getHost()+":"+params.getPort()+"/JMXConnector");
                    JMXConnector jmxConnector = JMXConnectorFactory.connect(address, environment);
                    RemoteDeploymentManager manager = new RemoteDeploymentManager(jmxConnector, params.getHost());
                    if(!manager.isSameMachine()) {
                        manager.setAuthentication(username, password);
                    }
                    return manager;
                } catch (IOException e) {
                    throw (DeploymentManagerCreationException)new DeploymentManagerCreationException(e.getMessage()).initCause(e);
                } catch (SecurityException e) {
                    throw (AuthenticationFailedException) new AuthenticationFailedException("Invalid login.").initCause(e);
                }
            } else if(params.getProtocol().equals("inVM")) {
                return new LocalDeploymentManager(KernelRegistry.getKernel(params.getHost()));
            } else {
                throw new DeploymentManagerCreationException("Invalid URI: " + uri);
            }
        } catch (RuntimeException e) {
            // some DeploymentManagerFactories suppress unchecked exceptions - log and rethrow
            e.printStackTrace();
            throw e;
        } catch (Error e) {
            // some DeploymentManagerFactories suppress unchecked exceptions - log and rethrow
            e.printStackTrace();
            throw e;
        }
    }

    static {
        DeploymentFactoryManager manager = DeploymentFactoryManager.getInstance();
        manager.registerDeploymentFactory(new DeploymentFactoryImpl());
    }

    private final static class ConnectParams {
        private String protocol;
        private String host;
        private int port;

        public ConnectParams(String protocol, String host, int port) {
            this.protocol = protocol;
            this.host = host;
            this.port = port;
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

        public String toString() {
            return protocol+" / "+host+" / "+port;
        }
    }

    public static void main(String[] args) {
        System.out.println("Parsed: "+new DeploymentFactoryImpl().parseURI("deployer:geronimo:inVM"));
    }
}
