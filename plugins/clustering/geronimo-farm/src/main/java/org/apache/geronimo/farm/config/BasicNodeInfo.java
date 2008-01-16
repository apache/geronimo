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

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.management.MBeanServerConnection;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;

import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.gbean.GBeanInfoBuilder;
import org.apache.geronimo.kernel.Kernel;
import org.apache.geronimo.system.jmx.KernelDelegate;

/**
 *
 * @version $Rev:$ $Date:$
 */
public class BasicNodeInfo implements NodeInfo {
    private final String name;
    private final ExtendedJMXConnectorInfo connectorInfo;
    private final Kernel kernel;

    public BasicNodeInfo(Kernel kernel, String name, ExtendedJMXConnectorInfo connectorInfo) {
        if (null == kernel) {
            throw new IllegalArgumentException("kernel is required");
        } else if (null == name) {
            throw new IllegalArgumentException("name is required");
        } else if (null == connectorInfo) {
            throw new IllegalArgumentException("connectorInfo is required");
        }
        this.kernel = kernel;
        this.name = name;
        this.connectorInfo = connectorInfo;
    }
    
    public String getName() {
        return name;
    }

    public ExtendedJMXConnectorInfo getConnectorInfo() {
        return connectorInfo;
    }

    public Kernel newKernel() throws IOException {
        if (connectorInfo.isLocal()) {
            return kernel;
        }
        
        String url = "service:jmx:rmi://" + connectorInfo.getHost() + "/jndi/"
                        + connectorInfo.getProtocol() + "://" + connectorInfo.getHost() + ":"
                        + connectorInfo.getPort() + "/" + connectorInfo.getUrlPath();

        Map environment = new HashMap();
        environment.put("jmx.remote.credentials",
            new String[] {connectorInfo.getUsername(), connectorInfo.getPassword()});

        return newKernel(url, environment);
    }

    protected Kernel newKernel(String url, Map environment) throws IOException {
        JMXConnector jmxConnector = JMXConnectorFactory.connect(new JMXServiceURL(url), environment);
        MBeanServerConnection mbServerConnection = jmxConnector.getMBeanServerConnection();
        return new KernelDelegate(mbServerConnection);
    }
    
    public static final GBeanInfo GBEAN_INFO;

    public static final String GBEAN_J2EE_TYPE = "NodeInfo";
    public static final String GBEAN_ATTR_KERNEL = "kernel";
    public static final String GBEAN_ATTR_NODE_NAME = "name";
    public static final String GBEAN_ATTR_EXT_JMX_CONN_INFO = "extendedJMXConnectorInfo";
    static {
        GBeanInfoBuilder builder = GBeanInfoBuilder.createStatic(BasicNodeInfo.class, GBEAN_J2EE_TYPE);

        builder.addAttribute(GBEAN_ATTR_KERNEL, Kernel.class, false);
        builder.addAttribute(GBEAN_ATTR_NODE_NAME, String.class, true);
        builder.addAttribute(GBEAN_ATTR_EXT_JMX_CONN_INFO, ExtendedJMXConnectorInfo.class, true);
        
        builder.addInterface(NodeInfo.class);
        
        builder.setConstructor(new String[]{GBEAN_ATTR_KERNEL,
            GBEAN_ATTR_NODE_NAME,
            GBEAN_ATTR_EXT_JMX_CONN_INFO});

        GBEAN_INFO = builder.getBeanInfo();
    }

    public static GBeanInfo getGBeanInfo() {
        return GBEAN_INFO;
    }

}
