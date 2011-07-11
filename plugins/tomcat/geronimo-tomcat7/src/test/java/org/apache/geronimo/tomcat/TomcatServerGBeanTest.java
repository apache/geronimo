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

package org.apache.geronimo.tomcat;

import java.io.File;
import java.io.FileReader;

import org.apache.catalina.Lifecycle;
import org.apache.catalina.Server;
import org.apache.geronimo.system.serverinfo.BasicServerInfo;
import org.apache.geronimo.system.serverinfo.ServerInfo;
import org.apache.geronimo.testsupport.TestSupport;
import org.apache.geronimo.tomcat.model.ChannelType;
import org.apache.geronimo.tomcat.model.ClusterType;
import org.apache.geronimo.tomcat.model.ServerType;

/**
 * @version $Rev$ $Date$
 */
public class TomcatServerGBeanTest extends TestSupport {

    private static final String SERVER_1 = "src/test/resources/deployables/server-1.xml";

    private static final String SERVER_2 = "src/test/resources/deployables/server-2.xml";

    public void testLoadServer1() throws Exception {
        File server1 = resolveFile(SERVER_1);
        FileReader in = new FileReader(server1);
        try {
            ServerType serverType = TomcatServerGBean.loadServerType(in);
            assertEquals(3, serverType.getListener().size());
            Server server = serverType.build(getClass().getClassLoader(), null);
            try {
                ((Lifecycle) server).start();
            } finally {
                ((Lifecycle) server).stop();
            }
        } finally {
            in.close();
        }
    }

    public void testLoadServer2() throws Exception {
        File server2 = resolveFile(SERVER_2);
        FileReader in = new FileReader(server2);
        try {
            ServerType serverType = TomcatServerGBean.loadServerType(in);
            ClusterType engineClusterType = serverType.getService().get(0).getEngine().getCluster();
            assertNotNull(engineClusterType);
            validateClusterType(engineClusterType);
            ClusterType hostClusterType = serverType.getService().get(0).getEngine().getHost().get(0).getCluster();
            assertNotNull(hostClusterType);
            validateClusterType(hostClusterType);
            serverType.build(getClass().getClassLoader(), null);
        } finally {
            in.close();
        }
    }

    private void validateClusterType(ClusterType clusterType) {
        assertEquals(2, clusterType.getValve().size());
        assertEquals(2, clusterType.getClusterListener().size());
        ChannelType channelType = clusterType.getChannel();
        assertEquals(3, channelType.getInterceptor().size());
        assertEquals(1, channelType.getInterceptor().get(2).getMember().size());
    }

    public void testGBeanServer1() throws Exception {
        ServerInfo serverInfo = new BasicServerInfo(BASEDIR.getAbsolutePath());
        TomcatServerGBean tomcatServerGBean = new TomcatServerGBean(null, SERVER_1, null, serverInfo, null, null, getClass().getClassLoader(), null);
        try {
            tomcatServerGBean.doStart();
        } finally {
            tomcatServerGBean.doStop();
        }
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        System.setProperty("catalina.useNaming", "false");
        System.setProperty("catalina.base", BASEDIR.getAbsolutePath());
    }
}
