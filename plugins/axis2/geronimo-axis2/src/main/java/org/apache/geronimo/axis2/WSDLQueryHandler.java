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

package org.apache.geronimo.axis2;

import java.io.FileNotFoundException;
import java.io.OutputStream;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.wsdl.Definition;
import javax.wsdl.Import;
import javax.wsdl.Types;
import javax.wsdl.WSDLException;
import javax.wsdl.extensions.ExtensibilityElement;
import javax.wsdl.extensions.schema.Schema;
import javax.wsdl.extensions.schema.SchemaImport;
import javax.wsdl.extensions.schema.SchemaReference;
import javax.wsdl.factory.WSDLFactory;
import javax.wsdl.xml.WSDLReader;
import javax.wsdl.xml.WSDLWriter;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.axis2.description.AxisService;
import org.apache.axis2.jaxws.catalog.JAXWSCatalogManager;
import org.apache.axis2.jaxws.description.EndpointDescription;
import org.apache.geronimo.axis2.util.CatalogWSDLLocator;
import org.apache.geronimo.jaxws.WSDLUtils;
import org.apache.xml.resolver.Catalog;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class WSDLQueryHandler {

    private static final Logger LOG = LoggerFactory.getLogger(WSDLQueryHandler.class);
    
    private Map<String, Definition> wsdlMap;
    private Map<String, SchemaReference> schemaMap;
    private Map<String, String> keyMap;
    private int wsdlCounter;
    private int schemaCounter;
    private AxisService service;
    private Catalog catalog;    
    
    public WSDLQueryHandler(AxisService service) {
        this.service = service;
        EndpointDescription ed = AxisServiceGenerator.getEndpointDescription(this.service);
        JAXWSCatalogManager catalogManager = ed.getServiceDescription().getCatalogManager();
        if (catalogManager != null) {
            this.catalog = catalogManager.getCatalog();
        }
    }
    
    public void writeResponse(String baseUri, String wsdlUri, OutputStream os) throws Exception {

        String base = null;
        String wsdlKey = "";
        String xsdKey = null;
        
        int idx = baseUri.toLowerCase().indexOf("?wsdl");
        if (idx != -1) {
            base = baseUri.substring(0, idx);
            wsdlKey = baseUri.substring(idx + 5);
            if (wsdlKey.length() > 0) {
                wsdlKey = wsdlKey.substring(1);
            }
        } else {
            idx = baseUri.toLowerCase().indexOf("?xsd");
            if (idx != -1) {
                base = baseUri.substring(0, idx);
                xsdKey = baseUri.substring(idx + 4);
                if (xsdKey.length() > 0) {
                    xsdKey = xsdKey.substring(1);
                }
            } else {
                throw new Exception("Invalid request: " + baseUri);
            }
        }

        init(wsdlUri, base);

        Element rootElement;
        if (xsdKey == null) {
            Definition def = wsdlMap.get(wsdlKey);

            if (def == null) {
                throw new FileNotFoundException("WSDL not found: " + wsdlKey);
            }

            // update service port location on each request
            if (wsdlKey.equals("")) {
                WSDLUtils.updateLocations(def, base);
            }
            
            WSDLFactory factory = WSDLFactory.newInstance();
            WSDLWriter writer = factory.newWSDLWriter();

            rootElement = writer.getDocument(def).getDocumentElement();
            
            updateWSDLImports(rootElement, base, wsdlKey);
            updateSchemaImports(rootElement, base, wsdlKey);
        } else {
            SchemaReference si = schemaMap.get(xsdKey);
            
            if (si == null) {
                throw new FileNotFoundException("Schema not found: " + xsdKey);
            }
            
            rootElement = si.getReferencedSchema().getElement();
            
            updateSchemaImports(rootElement, base, xsdKey);
        }
                
        writeTo(rootElement, os);
    }

    private synchronized void init(String wsdlUri, String base) throws WSDLException {
        if (keyMap == null) {
            wsdlMap = new HashMap<String, Definition>();
            schemaMap = new HashMap<String, SchemaReference>();
            keyMap = new HashMap<String, String>();
            
            WSDLFactory factory = WSDLFactory.newInstance();
            WSDLReader reader = factory.newWSDLReader();
            reader.setFeature("javax.wsdl.importDocuments", true);
            reader.setFeature("javax.wsdl.verbose", false);            
            Definition def = reader.readWSDL(new CatalogWSDLLocator(wsdlUri, this.catalog));
            updateDefinition("", def, base);
            // remove other services and ports from wsdl
            WSDLUtils.trimDefinition(def, this.service.getName(), this.service.getEndpointName());
            
            wsdlMap.put("", def);
        }
    }

    private void updateWSDLImports(Element rootElement, String base, String parentKey) {
        NodeList nl = rootElement.getElementsByTagNameNS("http://schemas.xmlsoap.org/wsdl/", "import");        
        for (int x = 0; x < nl.getLength(); x++) {
            Element el = (Element) nl.item(x);
            String location = el.getAttribute("location");
            String id = parentKey + "/" + location;
            String key = keyMap.get(id);
            if (key != null) {
                el.setAttribute("location", base + "?wsdl=" + key);
            }
        }
    }

    private void updateSchemaImports(Element rootElement, String base, String parentKey) {
        NodeList nl = rootElement.getElementsByTagNameNS("http://www.w3.org/2001/XMLSchema", "import");
        for (int x = 0; x < nl.getLength(); x++) {
            Element el = (Element) nl.item(x);
            String location = el.getAttribute("schemaLocation");
            String id = parentKey + "/" + location;
            String key = keyMap.get(id);
            if (key != null) {
                el.setAttribute("schemaLocation", base + "?xsd=" + key);
            }
        }
        
        nl = rootElement.getElementsByTagNameNS("http://www.w3.org/2001/XMLSchema", "include");
        for (int x = 0; x < nl.getLength(); x++) {
            Element el = (Element) nl.item(x);
            String location = el.getAttribute("schemaLocation");
            String id = parentKey + "/" + location;
            String key = keyMap.get(id);
            if (key != null) {
                el.setAttribute("schemaLocation", base + "?xsd=" + key);
            }
        }
    }
       
    protected void updateDefinition(String parentKey,
                                    Definition def,
                                    String base) {
        Collection<List> imports = def.getImports().values();
        for (List lst : imports) {
            List<Import> impLst = lst;
            for (Import imp : impLst) {
                String location = imp.getLocationURI();                
                String id = parentKey + "/" + location;                   
                if (!keyMap.containsKey(id)) {
                    String key = getUniqueWSDLId();
                    wsdlMap.put(key, imp.getDefinition());
                    keyMap.put(id, key);
                    updateDefinition(key, imp.getDefinition(), base);
                }
            }
        }      
               
        Types types = def.getTypes();
        if (types != null) {
            for (ExtensibilityElement el : (List<ExtensibilityElement>)types.getExtensibilityElements()) {
                if (el instanceof Schema) {
                    Schema schema = (Schema)el;
                    updateSchemaImports(parentKey, schema, base);
                }
            }
        }
    }
    
    protected void updateSchemaImports(String parentKey, 
                                       Schema schema,
                                       String base) {
        Collection<List> imports = schema.getImports().values();
        for (List list : imports) {
            List<SchemaImport> impList = list;
            for (SchemaImport imp : impList) {
                String location = imp.getSchemaLocationURI();                                
                String id = parentKey + "/" + location;
                if (!keyMap.containsKey(id)) {
                    String key = getUniqueSchemaId();
                    schemaMap.put(key, imp);
                    keyMap.put(id, key);
                    updateSchemaImports(key, imp.getReferencedSchema(), base);
                }                    
            }
        }
        
        List<SchemaReference> includes = schema.getIncludes();
        for (SchemaReference included : includes) {
            String location = included.getSchemaLocationURI();
            String id = parentKey + "/" + location;
            if (!keyMap.containsKey(id)) {
                String key = getUniqueSchemaId();
                schemaMap.put(key, included);
                keyMap.put(id, key);
                updateSchemaImports(key, included.getReferencedSchema(), base);
            }
        }
    }
    
    private String getUniqueSchemaId() {
        return "xsd" + ++schemaCounter;
    }
    
    private String getUniqueWSDLId() {
        return "wsdl" + ++wsdlCounter;
    }    
    
    public static void writeTo(Node node, OutputStream os) {
        writeTo(new DOMSource(node), os);
    }
    
    public static void writeTo(Source src, OutputStream os) {
        Transformer it;
        try {
            it = TransformerFactory.newInstance().newTransformer();
            it.setOutputProperty(OutputKeys.METHOD, "xml");
            it.setOutputProperty(OutputKeys.INDENT, "yes");
            it.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");
            it.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "false");
            it.setOutputProperty(OutputKeys.ENCODING, "utf-8");
            it.transform(src, new StreamResult(os));
        } catch (TransformerException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
    
}
