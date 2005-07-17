/**
 *
 * Copyright 2003-2005 The Apache Software Foundation
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
package org.apache.geronimo.tomcat;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.Map;

import org.apache.catalina.LifecycleException;
import org.apache.catalina.connector.Connector;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.gbean.GBeanInfoBuilder;
import org.apache.geronimo.gbean.GBeanLifecycle;
import org.apache.geronimo.j2ee.j2eeobjectnames.NameFactory;
import org.apache.geronimo.system.serverinfo.ServerInfo;

/**
 * @version $Rev$ $Date$
 */
public class ConnectorGBean extends BaseGBean implements GBeanLifecycle, ObjectRetriever {

    private static final Log log = LogFactory.getLog(ConnectorGBean.class);
    
    private final Connector connector;
    private final TomcatContainer container;
    private final ServerInfo serverInfo;
    private String name;
    private int port;

    public ConnectorGBean(String name, String protocol, Map initParams, TomcatContainer container, ServerInfo serverInfo) throws Exception {
        super(); // TODO: make it an attribute
        
        if (container == null){
            throw new IllegalArgumentException("container cannot be null.");
        }
 
        if (serverInfo == null){
            throw new IllegalArgumentException("serverInfo cannot be null.");
        }
        
        this.name = name;
        this.container = container;
        this.serverInfo = serverInfo;
        
        //Create the Connector object
        connector = new Connector(protocol);

        //Resolve the keystore file path if we have this parameter
        String keystoreFile = (String)initParams.get("keystoreFile");
        if (keystoreFile != null){
            initParams.put("keystoreFile", serverInfo.resolvePath(keystoreFile));
        }

        //Set the parameters
        setParameters(connector, initParams);
        
    }

    public InetSocketAddress getAddress() {
        Object port = connector.getAttribute("port");
        if(port instanceof String) {
            port = new Integer((String)port);
        }
        Object address = connector.getAttribute("address");
        if(address instanceof InetAddress) {
            return new InetSocketAddress((InetAddress)address, ((Number)port).intValue());
        } else if(address instanceof String) {
            return new InetSocketAddress((String)address, ((Number)port).intValue());
        } else throw new IllegalStateException("Unexpected address class "+(address == null ? "null" : address.getClass().getName()));
    }

    public String getName() {
        return name;
    }

    public Object getInternalObject() {
        return connector;
    }

    public void doStart() throws LifecycleException {
        container.addConnector(connector);
        connector.start();
        log.info(name + " connector started");
   }

    public void doStop() {
        try{
            connector.stop();
        } catch (LifecycleException e){
            log.error(e);
        }
        container.removeConnector(connector);
        log.info(name + " connector stopped");
    }

    public void doFail() {
        log.info(name + " connector failed");
        doStop();
    }

    public static final GBeanInfo GBEAN_INFO;

    static {
        GBeanInfoBuilder infoFactory = new GBeanInfoBuilder("Tomcat Connector", ConnectorGBean.class);
        infoFactory.addAttribute("name", String.class, true);
        infoFactory.addAttribute("protocol", String.class, true);
        infoFactory.addAttribute("address", InetSocketAddress.class, false);
        infoFactory.addAttribute("initParams", Map.class, true);
        infoFactory.addReference("TomcatContainer", TomcatContainer.class, NameFactory.GERONIMO_SERVICE);
        infoFactory.addOperation("getInternalObject");
        infoFactory.addReference("ServerInfo", ServerInfo.class, "GBean");
        infoFactory.setConstructor(new String[] { "name", "protocol", "initParams", "TomcatContainer", "ServerInfo"});
        GBEAN_INFO = infoFactory.getBeanInfo();
    }

    public static GBeanInfo getGBeanInfo() {
        return GBEAN_INFO;
    }
}
