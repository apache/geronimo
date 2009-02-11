/**
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package org.apache.geronimo.jaxws.catalog;

import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.wsdl.Definition;
import javax.wsdl.Import;
import javax.wsdl.PortType;
import javax.wsdl.Types;
import javax.wsdl.extensions.ExtensibilityElement;
import javax.wsdl.extensions.schema.Schema;
import javax.wsdl.extensions.schema.SchemaImport;
import javax.wsdl.factory.WSDLFactory;
import javax.wsdl.xml.WSDLReader;

import org.apache.geronimo.testsupport.TestSupport;
import org.testng.annotations.Test;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

public abstract class CatalogTest extends TestSupport {

    protected String baseURL = "http://localhost:8080";
    
    abstract String getTestServletContext();
           
    @Test
    public void testWSDL() throws Exception {
        String context = getTestServletContext();
        assertNotNull("Test servlet context is not specified", context);

        URL url = new URL(baseURL + context + "/greeter?wsdl");
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setConnectTimeout(30 * 1000);
        conn.setReadTimeout(30 * 1000);
        try {
            conn.setUseCaches(false);

            WSDLReader wsdlReader = WSDLFactory.newInstance().newWSDLReader();
            wsdlReader.setFeature("javax.wsdl.importDocuments", true);
            Definition def = 
                wsdlReader.readWSDL(null, new InputSource(conn.getInputStream()));

            System.out.println("WSDL: " + def);
            
            assertTrue("Operation not found", checkForOperation(def));
            assertTrue("Element not found", checkForElement(def));
            
        } finally {
            conn.disconnect();
        }
    }
    
    private boolean checkForOperation(Definition def) throws Exception {
        Iterator iter = def.getPortTypes().entrySet().iterator();
        while (iter.hasNext()) {
            PortType portType = 
                (PortType)((Map.Entry)iter.next()).getValue();
            
            if (portType.getOperation("greetMe", null, null) != null) {
                return true;
            }
        }
        
        Collection<List> imports = def.getImports().values();
        for (List lst : imports) {
            List<Import> impLst = lst;
            for (Import imp : impLst) {
                boolean rs = checkForOperation(imp.getDefinition());
                if (rs) {
                    return true;
                }
            }
        }
        
        return false;
    }
    
    private boolean checkForElement(Definition def) throws Exception {
        Types types = def.getTypes();
        if (types != null) {
            for (ExtensibilityElement el : (List<ExtensibilityElement>)types.getExtensibilityElements()) {
                if (el instanceof Schema) {
                    Schema schema = (Schema)el;
                    boolean rs = checkForElement(schema);
                    if (rs) {
                        return true;
                    }
                }
            }
        }
        
        Collection<List> imports = def.getImports().values();
        for (List lst : imports) {
            List<Import> impLst = lst;
            for (Import imp : impLst) {
                boolean rs = checkForElement(imp.getDefinition());
                if (rs) {
                    return true;
                }
            }
        }
        
        return false;
    }
    
    private boolean checkForElement(Schema schema) throws Exception {
        Element element = schema.getElement();
        
        NodeList nodes = element.getElementsByTagNameNS("http://www.w3.org/2001/XMLSchema", "element");
        if (nodes != null) {
            for (int i = 0; i < nodes.getLength(); i++) {
                Element el = (Element)nodes.item(i);
                if ("Account".equals(el.getAttribute("name"))) {
                    return true;
                }
            }
        }
        
        Collection<List> imports = schema.getImports().values();
        for (List list : imports) {
            List<SchemaImport> impList = list;
            for (SchemaImport imp : impList) {
                Schema importedSchema = imp.getReferencedSchema();
                if (importedSchema == null) {
                    continue;
                }
                boolean rs = checkForElement(importedSchema);
                if (rs) {
                    return true;
                }
            }
        }
        
        return false;
    }
    
}
