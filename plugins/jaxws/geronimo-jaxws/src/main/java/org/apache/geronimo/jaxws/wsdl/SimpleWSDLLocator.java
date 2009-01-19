/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.apache.geronimo.jaxws.wsdl;

import java.io.IOException;

import javax.wsdl.xml.WSDLLocator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.InputSource;

public class SimpleWSDLLocator implements WSDLLocator
{
    private static final Logger LOG = LoggerFactory.getLogger(SimpleWSDLLocator.class);
    
    private String baseURI;
    private String lastImportLocation;
    private SimpleURIResolver resolver;
    
    public SimpleWSDLLocator(String baseURI) {
        this.baseURI = baseURI;
        this.resolver = new SimpleURIResolver();
    }
    
    public InputSource getBaseInputSource() {
        return resolve("", this.baseURI);
    }

    public String getBaseURI() {
        return this.baseURI;
    }

    public InputSource getImportInputSource(String parentLocation, String importLocation) {
        return resolve(parentLocation, importLocation);
    }
    
    protected InputSource resolve(String parentLocation, String importLocation) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("Resolving '" + importLocation + "' relative to '" + parentLocation + "'");
        }
        try {
            this.resolver.resolve(parentLocation, importLocation);
            if (this.resolver.isResolved()) {
                this.lastImportLocation = this.resolver.getURI().toString();   
                if (LOG.isDebugEnabled()) {
                    LOG.debug("Resolved location '" + this.lastImportLocation + "'");
                }
                return new InputSource(this.resolver.getInputStream());
            }
        } catch (IOException e) {
            // ignore
        }
        return null;
    }
    
    public String getLatestImportURI() {
        return this.lastImportLocation;
    }
    
    public void close() {        
    }
    
   
}
