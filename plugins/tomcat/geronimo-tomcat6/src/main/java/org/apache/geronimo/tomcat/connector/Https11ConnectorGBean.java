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

import org.apache.catalina.connector.Connector;
import org.apache.geronimo.gbean.annotation.GBean;
import org.apache.geronimo.gbean.annotation.ParamAttribute;
import org.apache.geronimo.gbean.annotation.ParamReference;
import org.apache.geronimo.management.geronimo.WebManager;
import org.apache.geronimo.system.serverinfo.ServerInfo;
import org.apache.geronimo.tomcat.TomcatContainer;

@GBean(name="Tomcat Connector HTTPS BIO")
public class Https11ConnectorGBean extends Http11ConnectorGBean {

    public Https11ConnectorGBean(@ParamAttribute(name = "name") String name,
                                 @ParamAttribute(name = "initParams") Map<String, String> initParams,
                                 @ParamAttribute(name = "host") String host,
                                 @ParamAttribute(name = "port") int port,
                                 @ParamReference(name = "TomcatContainer") TomcatContainer container,
                                 @ParamReference(name = "ServerInfo") ServerInfo serverInfo,
                                 @ParamAttribute(name = "connector") Connector conn)  throws Exception {
                                 
        super(name, initParams, host, port, container, serverInfo, conn);
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
  
}
