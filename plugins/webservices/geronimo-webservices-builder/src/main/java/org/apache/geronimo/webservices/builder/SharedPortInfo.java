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
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.jar.JarFile;

import org.apache.geronimo.common.DeploymentException;
import org.apache.openejb.jee.JavaWsdlMapping;
import org.apache.openejb.jee.ServiceEndpointInterfaceMapping;

/**
 * @version $Rev: 476049 $ $Date: 2006-11-16 23:35:17 -0500 (Thu, 16 Nov 2006) $
 */
public class SharedPortInfo {
    
    private String jaxrpcMappingFile;
    private String wsdlLocation;
    private JavaWsdlMapping javaWsdlMapping;
    private SchemaInfoBuilder schemaInfoBuilder;
    private DescriptorVersion ddVersion;

    public SharedPortInfo(String wsdlLocation, String jaxrpcMappingFile) {
        this(wsdlLocation, jaxrpcMappingFile, DescriptorVersion.UNKNOWN);
    }
    
    public SharedPortInfo(String wsdlLocation, String jaxrpcMappingFile, DescriptorVersion ddVersion) {
        this.wsdlLocation = wsdlLocation;
        this.jaxrpcMappingFile = jaxrpcMappingFile;
        this.ddVersion = ddVersion;
    }

    public DescriptorVersion getDescriptorVersion() {
        return this.ddVersion;
    }
    
    public String getWsdlLocation() {
        return this.wsdlLocation;
    }
    
    public String getJaxrpcMappingFile() {
        return this.jaxrpcMappingFile;
    }
    
    public void initialize(JarFile moduleFile) throws DeploymentException {
        if (this.jaxrpcMappingFile == null) {
            throw new DeploymentException("JAX-RPC mapping file is required.");
        }
        if (this.wsdlLocation == null) {
            throw new DeploymentException("WSDL file is required.");
        }
        
        if (this.javaWsdlMapping == null) {
            URI jaxrpcMappingURI;
            try {
                jaxrpcMappingURI = new URI(this.jaxrpcMappingFile);
            } catch (URISyntaxException e) {
                throw new DeploymentException("Could not construct jaxrpc mapping uri from "
                                              + this.jaxrpcMappingFile, e);
            }

            this.javaWsdlMapping = WSDescriptorParser.readJaxrpcMapping(moduleFile, jaxrpcMappingURI);
        }
        
        if (this.schemaInfoBuilder == null) {
            URI wsdlURI;
            try {
                wsdlURI = new URI(this.wsdlLocation);
            } catch (URISyntaxException e) {
                throw new DeploymentException("could not construct wsdl uri from "
                                              + this.wsdlLocation, e);
            }

            this.schemaInfoBuilder = new SchemaInfoBuilder(moduleFile, wsdlURI);
        }
    }
    
    public JavaWsdlMapping getJavaWsdlMapping() {
        return this.javaWsdlMapping;
    }
    
    public SchemaInfoBuilder getSchemaInfoBuilder() {
        return schemaInfoBuilder;
    }
    
    public Map<String, ServiceEndpointInterfaceMapping> getSEIMappings() {
        if (this.javaWsdlMapping == null) {
            return Collections.emptyMap();
        }
        HashMap<String, ServiceEndpointInterfaceMapping> seiMappings = new HashMap<String, ServiceEndpointInterfaceMapping>();
        Collection<ServiceEndpointInterfaceMapping> mappings = this.javaWsdlMapping.getServiceEndpointInterfaceMapping();
        for (ServiceEndpointInterfaceMapping seiMapping : mappings) {
            seiMappings.put(seiMapping.getServiceEndpointInterface().trim(), seiMapping);
        }
        return seiMappings;
    }
    
}
