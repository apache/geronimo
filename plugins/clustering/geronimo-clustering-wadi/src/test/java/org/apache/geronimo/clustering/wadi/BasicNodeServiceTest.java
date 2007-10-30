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
package org.apache.geronimo.clustering.wadi;

import javax.management.MBeanServer;

import org.apache.geronimo.clustering.LocalNode;
import org.apache.geronimo.jmxremoting.JMXConnector;

import com.agical.rmock.extension.junit.RMockTestCase;

/**
 * 
 * @version $Rev$ $Date$
 */
public class BasicNodeServiceTest extends RMockTestCase {

    public void testGetConnectionInfo() throws Exception {
        LocalNode localNode = (LocalNode) mock(LocalNode.class);
        localNode.getJMXConnectorInfo();
        
        JMXConnector connector = new JMXConnector((MBeanServer) null, "name", null);
        String host = "host";
        connector.setHost(host);
        int port = 1;
        connector.setPort(port);
        modify().returnValue(connector);
        
        startVerification();
        
        BasicNodeService nodeService = new BasicNodeService(localNode);
        NodeConnectionInfo connectionInfo = nodeService.getConnectionInfo();
        assertEquals(host, connectionInfo.getHost());
        assertEquals(port, connectionInfo.getPort());
    }
    
}
