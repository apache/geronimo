/**
 *
 * Copyright 2003-2004 The Apache Software Foundation
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
package org.apache.geronimo.axis.builder;

import java.net.URI;
import javax.wsdl.Definition;
import javax.wsdl.Port;
import javax.xml.namespace.QName;

import org.apache.geronimo.xbeans.j2ee.JavaWsdlMappingType;
import org.apache.geronimo.xbeans.j2ee.PortComponentHandlerType;
import org.apache.geronimo.xbeans.j2ee.ServiceEndpointInterfaceMappingType;

/**
 * @version $Rev$ $Date$
 */
public class PortInfo {
    private final String portComponentName;
    private final QName portQName;
    private final SchemaInfoBuilder schemaInfoBuilder;
    private final JavaWsdlMappingType javaWsdlMapping;
    private final ServiceEndpointInterfaceMappingType seiMapping;
    private final String seInterfaceName;
    private final PortComponentHandlerType[] handlers;
    private final Port port;
    private final URI contextURI;

    private final String wsdlLocation;

    public PortInfo(String portComponentName, QName portQName, SchemaInfoBuilder schemaInfoBuilder, JavaWsdlMappingType javaWsdlMapping, String seiInterfaceName, PortComponentHandlerType[] handlers, Port port, ServiceEndpointInterfaceMappingType seiMapping, String wsdlLocation, URI contextURI) {
        this.portComponentName = portComponentName;
        this.portQName = portQName;
        this.schemaInfoBuilder = schemaInfoBuilder;
        this.javaWsdlMapping = javaWsdlMapping;
        this.seInterfaceName = seiInterfaceName;
        this.handlers = handlers;
        this.port = port;
        this.seiMapping = seiMapping;
        this.wsdlLocation = wsdlLocation;
        this.contextURI = contextURI;
    }

    public String getWsdlLocation() {
        return wsdlLocation;
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

    public JavaWsdlMappingType getJavaWsdlMapping() {
        return javaWsdlMapping;
    }

    public String getServiceEndpointInterfaceName() {
        return seInterfaceName;
    }


    public ServiceEndpointInterfaceMappingType getServiceEndpointInterfaceMapping() {
        return seiMapping;
    }

    public PortComponentHandlerType[] getHandlers() {
        return handlers;
    }

    public URI getContextURI() {
        return contextURI;
    }
}
