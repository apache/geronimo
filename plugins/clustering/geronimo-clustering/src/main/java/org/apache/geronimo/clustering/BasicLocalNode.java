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
package org.apache.geronimo.clustering;

import org.apache.geronimo.gbean.annotation.ParamAttribute;
import org.apache.geronimo.gbean.annotation.ParamReference;
import org.apache.geronimo.jmxremoting.JMXConnectorInfo;

/**
 *
 * @version $Rev$ $Date$
 */
public class BasicLocalNode extends AbstractNode implements LocalNode {
    private final JMXConnectorInfo connectorInfo;
    
    public BasicLocalNode(@ParamAttribute(name=GBEAN_ATTR_NODE_NAME) String name,
        @ParamReference(name=GBEAN_REF_JMX_CONNECTOR) JMXConnectorInfo connectorInfo) {
        super(name);
        if (null == connectorInfo) {
            throw new IllegalArgumentException("connectorInfo is required");
        }
        this.connectorInfo = connectorInfo;
    }
    
    public JMXConnectorInfo getJMXConnectorInfo() {
        return connectorInfo;
    }
    
    @Override
    protected String getHost() {
        return connectorInfo.getHost();
    }
    
    @Override
    protected int getPort() {
        return connectorInfo.getPort();
    }
    
    public static final String GBEAN_ATTR_NODE_NAME = "nodeName";
    public static final String GBEAN_REF_JMX_CONNECTOR = "JMXConnector";
}
