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

import org.apache.xml.resolver.Catalog;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.InputSource;

public class CatalogWSDLLocator extends SimpleWSDLLocator {
    
    private static final Logger LOG = LoggerFactory.getLogger(CatalogWSDLLocator.class);
    
    private Catalog catalog;
        
    public CatalogWSDLLocator(String baseURI, Catalog catalog) {
        super(baseURI);
        this.catalog = catalog;
    }
        
    @Override
    protected InputSource resolve(String parentLocation, String importLocation) {               
        String resolvedImportLocation = null;
        
        if (this.catalog != null) {
            try {
                resolvedImportLocation = this.catalog.resolveSystem(importLocation);
                if (resolvedImportLocation == null) {
                    resolvedImportLocation = catalog.resolveURI(importLocation);
                }
                if (resolvedImportLocation == null) {
                    resolvedImportLocation = catalog.resolvePublic(importLocation, parentLocation);
                }
            } catch (IOException e) {
                throw new RuntimeException("Catalog resolution failed", e);
            }
        }
        
        if (resolvedImportLocation == null) {
            // not found in the catalog
            return super.resolve(parentLocation, importLocation);
        } else {
            // found in the catalog
            return super.resolve("", resolvedImportLocation);            
        }
    }
        
}
