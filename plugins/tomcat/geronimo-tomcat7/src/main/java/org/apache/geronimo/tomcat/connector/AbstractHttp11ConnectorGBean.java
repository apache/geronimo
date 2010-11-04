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

import javax.net.ssl.KeyManagerFactory;

import org.apache.catalina.connector.Connector;
import org.apache.geronimo.gbean.annotation.GBean;
import org.apache.geronimo.gbean.annotation.ParamAttribute;
import org.apache.geronimo.gbean.annotation.ParamReference;
import org.apache.geronimo.gbean.annotation.Persistent;
import org.apache.geronimo.management.geronimo.WebManager;
import org.apache.geronimo.system.serverinfo.ServerInfo;
import org.apache.geronimo.tomcat.TomcatContainer;

@GBean(name="Tomcat Connector")
public abstract class AbstractHttp11ConnectorGBean extends BaseHttp11ConnectorGBean {


    public AbstractHttp11ConnectorGBean(@ParamAttribute(manageable=false, name = "name") String name,
                                        @ParamAttribute(manageable=false, name = "initParams") Map<String, String> initParams,
                                        @ParamAttribute(manageable=false, name = "protocol") String tomcatProtocol,
                                        @ParamAttribute(manageable=false, name = "host") String host,
                                        @ParamAttribute(manageable=false, name = "port") int port,
                                        @ParamReference(name = "TomcatContainer") TomcatContainer container,
                                        @ParamReference(name = "ServerInfo") ServerInfo serverInfo,
                                        @ParamAttribute(manageable=false, name = "connector") Connector conn)  throws Exception {

        super(name, initParams, tomcatProtocol, host, port, container, serverInfo, conn);
    }

    @Override
    public int getDefaultPort() {
        return 80;
    }

    @Override
    public String getGeronimoProtocol() {
        return WebManager.PROTOCOL_HTTP;
    }

    


}
