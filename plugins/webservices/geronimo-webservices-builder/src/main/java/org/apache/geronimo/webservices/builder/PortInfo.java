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
package org.apache.geronimo.webservices.builder;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.jar.JarFile;

import javax.wsdl.Definition;
import javax.wsdl.Port;
import javax.xml.namespace.QName;

import org.apache.geronimo.common.DeploymentException;
import org.apache.openejb.jee.Handler;
import org.apache.openejb.jee.HandlerChain;
import org.apache.openejb.jee.HandlerChains;
import org.apache.openejb.jee.JavaWsdlMapping;
import org.apache.openejb.jee.ServiceEndpointInterfaceMapping;

/**
 * @version $Rev$ $Date$
 */
public class PortInfo {
    private final String portComponentName;
    private final QName portQName;
    private final String seInterfaceName;
    private final List<Handler> handlers;
    private final SharedPortInfo sharedPortInfo;
    
    // set after initialize is called
    private SchemaInfoBuilder schemaInfoBuilder;
    private JavaWsdlMapping javaWsdlMapping;
    private Port port;
    private ServiceEndpointInterfaceMapping seiMapping;
    private URI contextURI;
    private String location;

    public PortInfo(SharedPortInfo sharedPortInfo, String portComponentName, QName portQName, String seiInterfaceName, HandlerChains handlerChains, String location) {
        this.sharedPortInfo = sharedPortInfo;
        this.portComponentName = portComponentName;
        this.portQName = portQName;
        this.seInterfaceName = seiInterfaceName;
        this.location = location;
        this.handlers = new ArrayList<Handler>();
        // collapse the handler chain down to a flat list 
        if (handlerChains != null) {
            for (HandlerChain chain: handlerChains.getHandlerChain()) {
                for (Handler handler: chain.getHandler()) {
                    handlers.add(handler);
                }
            }
        }
    }

    public DescriptorVersion getDescriptorVersion() {
        return this.sharedPortInfo.getDescriptorVersion();
    }
    
    public String getWsdlLocation() {
        return this.sharedPortInfo.getWsdlLocation();
    }

    public String getPortComponentName() {
        return portComponentName;
    }

    public QName getPortQName() {
        return portQName;
    }
    
    public Port getPort() {
        return port;
    }
    
    public SchemaInfoBuilder getSchemaInfoBuilder() {
        return schemaInfoBuilder;
    }
    
    public Definition getDefinition() {
        return schemaInfoBuilder.getDefinition();
    }

    public JavaWsdlMapping getJavaWsdlMapping() {
        return javaWsdlMapping;
    }

    public String getServiceEndpointInterfaceName() {
        return seInterfaceName;
    }

    public ServiceEndpointInterfaceMapping getServiceEndpointInterfaceMapping() {
        return seiMapping;
    }

    public List<Handler> getHandlers() {
        return handlers;
    }

    public URI getContextURI() {
        return contextURI;
    }
    
    public void initialize(JarFile moduleFile) throws DeploymentException {
        this.sharedPortInfo.initialize(moduleFile);
        
        this.schemaInfoBuilder = this.sharedPortInfo.getSchemaInfoBuilder();
        this.javaWsdlMapping = this.sharedPortInfo.getJavaWsdlMapping();
                               
        QName portQName = getPortQName();
        String portComponentName = getPortComponentName();
        String seiInterfaceName = getServiceEndpointInterfaceName();
                              
        Map wsdlPortMap = this.schemaInfoBuilder.getPortMap();        
        Port wsdlPort = (Port) wsdlPortMap.get(portQName.getLocalPart());
        if (wsdlPort == null) {
            throw new DeploymentException("No WSDL Port definition for port-component " + portComponentName);
        }
        this.port = wsdlPort;
                
        this.seiMapping = this.sharedPortInfo.getSEIMappings().get(seiInterfaceName);
        
        this.location = this.schemaInfoBuilder.movePortLocation(portQName.getLocalPart(), this.location);
        
        try {
            this.contextURI = new URI(this.location);
        } catch (URISyntaxException e) {
            throw new DeploymentException("Could not construct URI for web service location", e);
        }
    }
}
