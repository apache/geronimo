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
package org.apache.geronimo.tomcat.connector;

import java.util.Map;

import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.gbean.GBeanInfoBuilder;
import org.apache.geronimo.management.geronimo.WebManager;
import org.apache.geronimo.system.serverinfo.ServerInfo;
import org.apache.geronimo.tomcat.TomcatContainer;

public class Https11ConnectorGBean extends Http11ConnectorGBean {

    public Https11ConnectorGBean(String name, Map initParams, String host, int port, TomcatContainer container, ServerInfo serverInfo) throws Exception {
        super(name, initParams, host, port, container, serverInfo);
        setSslEnabled(true);
        setScheme("https");
        setSecure(true);
    }
    
    public int getDefaultPort() {
        return 443; 
    }  
    
    public String getGeronimoProtocol(){
        return WebManager.PROTOCOL_HTTPS;
    }
    
    public static final GBeanInfo GBEAN_INFO;

    static {
        GBeanInfoBuilder infoFactory = GBeanInfoBuilder.createStatic("Tomcat Connector HTTPS BIO", Https11ConnectorGBean.class, Http11ConnectorGBean.GBEAN_INFO);
        infoFactory.setConstructor(new String[] { "name", "initParams", "host", "port", "TomcatContainer", "ServerInfo"});
        GBEAN_INFO = infoFactory.getBeanInfo();
    }
    
    public static GBeanInfo getGBeanInfo() {
        return GBEAN_INFO;
    }    
}
