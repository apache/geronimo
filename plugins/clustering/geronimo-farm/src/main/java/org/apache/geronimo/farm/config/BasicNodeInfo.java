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

import javax.management.remote.JMXConnector;

import org.apache.geronimo.gbean.annotation.GBean;
import org.apache.geronimo.gbean.annotation.ParamAttribute;
import org.apache.geronimo.gbean.annotation.ParamSpecial;
import org.apache.geronimo.gbean.annotation.SpecialAttributeType;
import org.apache.geronimo.kernel.Kernel;

/**
 *
 * @version $Rev:$ $Date:$
 */
@GBean(j2eeType="NodeInfo")
public class BasicNodeInfo implements NodeInfo {
    private final String name;
    private final ExtendedJMXConnectorInfo connectorInfo;
    private final Kernel kernel;

    public BasicNodeInfo(@ParamSpecial(type=SpecialAttributeType.kernel) Kernel kernel,
            @ParamAttribute(name=GBEAN_ATTR_NODE_NAME) String name,
            @ParamAttribute(name=GBEAN_ATTR_EXT_JMX_CONN_INFO) ExtendedJMXConnectorInfo connectorInfo) {
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
        JMXConnector jmxConnector = connectorInfo.connect();
        return connectorInfo.newKernel(jmxConnector);
        //TODO close the connection eventually?
    }

    public static final String GBEAN_ATTR_NODE_NAME = "name";
    public static final String GBEAN_ATTR_EXT_JMX_CONN_INFO = "extendedJMXConnectorInfo";
}
