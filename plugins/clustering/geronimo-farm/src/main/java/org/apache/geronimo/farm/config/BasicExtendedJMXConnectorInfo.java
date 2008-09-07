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

package org.apache.geronimo.farm.config;

import java.io.Serializable;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.Map;
import java.util.HashMap;
import java.util.Collections;

import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;
import javax.management.MBeanServerConnection;

import org.apache.geronimo.deployment.service.DoNotPersist;
import org.apache.geronimo.deployment.service.EncryptOnPersist;
import org.apache.geronimo.kernel.Kernel;
import org.apache.geronimo.system.jmx.KernelDelegate;

/**
 *
 * @version $Rev:$ $Date:$
 */
public class BasicExtendedJMXConnectorInfo implements ExtendedJMXConnectorInfo, Serializable {
    private String username;
    private String password;
    private String protocol;
    private String host;
    private int port = -1;
    private String urlPath;
    private boolean local;

    public BasicExtendedJMXConnectorInfo() {
    }

    public BasicExtendedJMXConnectorInfo(String username, String password, String protocol, String host, int port, String urlPath, boolean local) {
        this.username = username;
        this.password = password;
        this.protocol = protocol;
        this.host = host;
        this.port = port;
        this.urlPath = urlPath;
        this.local = local;
    }

    public String getHost() {
        return host;
    }

    @DoNotPersist
    public InetSocketAddress getListenAddress() {
        return new InetSocketAddress(host, port);
    }

    public int getPort() {
        return port;
    }

    public String getProtocol() {
        return protocol;
    }

    public String getUrlPath() {
        return urlPath;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public void setProtocol(String protocol) {
        this.protocol = protocol;
    }

    public void setUrlPath(String urlPath) {
        this.urlPath = urlPath;
    }

    @EncryptOnPersist
    public String getPassword() {
        return password;
    }

    public String getUsername() {
        return username;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public boolean isLocal() {
        return local;
    }

    public void setLocal(boolean local) {
        this.local = local;
    }

    public JMXConnector connect() throws IOException {
        String url = getJmxURI();

        Map<String, ?> environment = Collections.singletonMap("jmx.remote.credentials",
            new String[] {getUsername(), getPassword()});

        return JMXConnectorFactory.connect(new JMXServiceURL(url), environment);
    }

    public Kernel newKernel(JMXConnector jmxConnector) throws IOException {
        MBeanServerConnection mbServerConnection = jmxConnector.getMBeanServerConnection();
        return new KernelDelegate(mbServerConnection);
    }

    protected String getJmxURI() {
        return "service:jmx:rmi://" + getHost() + "/jndi/"
                        + getProtocol() + "://" + getHost() + ":"
                        + getPort() + "/" + getUrlPath();

    }

}
