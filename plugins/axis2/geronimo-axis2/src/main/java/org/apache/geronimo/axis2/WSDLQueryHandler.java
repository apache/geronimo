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
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.wsdl.Definition;
import javax.wsdl.Import;
import javax.wsdl.Types;
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
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.geronimo.jaxws.WSDLUtils;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class WSDLQueryHandler {

    private static final Log LOG = LogFactory.getLog(WSDLQueryHandler.class);
    
    private static TransformerFactory transformerFactory = TransformerFactory.newInstance();
    
    private Map<String, Definition> mp = new ConcurrentHashMap<String, Definition>();
    private Map<String, SchemaReference> smp = new ConcurrentHashMap<String, SchemaReference>();
    private AxisService service;
    
    public WSDLQueryHandler(AxisService service) {
        this.service = service;
    }
    
    public void writeResponse(String baseUri, String wsdlUri, OutputStream os) throws Exception {

        String base = null;
        String wsdl = "";
        String xsd = null;
        
        int idx = baseUri.toLowerCase().indexOf("?wsdl");
        if (idx != -1) {
            base = baseUri.substring(0, idx);
            wsdl = baseUri.substring(idx + 5);
            if (wsdl.length() > 0) {
                wsdl = wsdl.substring(1);
            }
        } else {
            idx = baseUri.toLowerCase().indexOf("?xsd");
            if (idx != -1) {
                base = baseUri.substring(0, idx);
                xsd = baseUri.substring(idx + 4);
                if (xsd.length() > 0) {
                    xsd = xsd.substring(1);
                }
            } else {
                throw new Exception("Invalid request: " + baseUri);
            }
        }

        if (!mp.containsKey(wsdl)) {
            WSDLFactory factory = WSDLFactory.newInstance();
            WSDLReader reader = factory.newWSDLReader();
            reader.setFeature("javax.wsdl.importDocuments", true);
            reader.setFeature("javax.wsdl.verbose", false);
            Definition def = reader.readWSDL(wsdlUri);
            updateDefinition(def, mp, smp, base);
            // remove other services and ports from wsdl
            WSDLUtils.trimDefinition(def, this.service.getName(), this.service.getEndpointName());
            mp.put("", def);
        }

        Element rootElement;

        if (xsd == null) {
            Definition def = mp.get(wsdl);

            if (def == null) {
                throw new FileNotFoundException("WSDL not found: " + wsdl);
            }
            
            // update service port location on each request
            if (wsdl.equals("")) {
                WSDLUtils.updateLocations(def, base);
            }
            
            WSDLFactory factory = WSDLFactory.newInstance();
            WSDLWriter writer = factory.newWSDLWriter();

            rootElement = writer.getDocument(def).getDocumentElement();
        } else {
            SchemaReference si = smp.get(xsd);
            
            if (si == null) {
                throw new FileNotFoundException("Schema not found: " + xsd);
            }
            
            rootElement = si.getReferencedSchema().getElement();
        }

        NodeList nl = rootElement.getElementsByTagNameNS("http://www.w3.org/2001/XMLSchema",
                "import");
        for (int x = 0; x < nl.getLength(); x++) {
            Element el = (Element) nl.item(x);
            String sl = el.getAttribute("schemaLocation");
            if (smp.containsKey(sl)) {
                el.setAttribute("schemaLocation", base + "?xsd=" + sl);
            }
        }
        nl = rootElement.getElementsByTagNameNS("http://www.w3.org/2001/XMLSchema", "include");
        for (int x = 0; x < nl.getLength(); x++) {
            Element el = (Element) nl.item(x);
            String sl = el.getAttribute("schemaLocation");
            if (smp.containsKey(sl)) {
                el.setAttribute("schemaLocation", base + "?xsd=" + sl);
            }
        }
        nl = rootElement.getElementsByTagNameNS("http://schemas.xmlsoap.org/wsdl/", "import");
        for (int x = 0; x < nl.getLength(); x++) {
            Element el = (Element) nl.item(x);
            String sl = el.getAttribute("location");
            if (mp.containsKey(sl)) {
                el.setAttribute("location", base + "?wsdl=" + sl);
            }
        }

        writeTo(rootElement, os);
    }
       
    protected void updateDefinition(Definition def,
                                    Map<String, Definition> done,
                                    Map<String, SchemaReference> doneSchemas,
                                    String base) {
        Collection<List> imports = def.getImports().values();
        for (List lst : imports) {
            List<Import> impLst = lst;
            for (Import imp : impLst) {
                String start = imp.getLocationURI();
                try {
                    //check to see if it's aleady in a URL format.  If so, leave it.
                    new URL(start);
                } catch (MalformedURLException e) {
                    done.put(start, imp.getDefinition());
                    updateDefinition(imp.getDefinition(), done, doneSchemas, base);
                }
            }
        }      
        
        
        /* This doesn't actually work.   Setting setSchemaLocationURI on the import
        * for some reason doesn't actually result in the new URI being written
        * */
        Types types = def.getTypes();
        if (types != null) {
            for (ExtensibilityElement el : (List<ExtensibilityElement>)types.getExtensibilityElements()) {
                if (el instanceof Schema) {
                    Schema see = (Schema)el;
                    updateSchemaImports(see, doneSchemas, base);
                }
            }
        }
    }
    
    protected void updateSchemaImports(Schema schema,
                                       Map<String, SchemaReference> doneSchemas,
                                       String base) {
        Collection<List>  imports = schema.getImports().values();
        for (List lst : imports) {
            List<SchemaImport> impLst = lst;
            for (SchemaImport imp : impLst) {
                String start = imp.getSchemaLocationURI();
                if (start != null) {
                    try {
                        //check to see if it's aleady in a URL format.  If so, leave it.
                        new URL(start);
                    } catch (MalformedURLException e) {
                        if (!doneSchemas.containsKey(start)) {
                            doneSchemas.put(start, imp);
                            updateSchemaImports(imp.getReferencedSchema(), doneSchemas, base);
                        }
                    }
                }
            }
        }
        List<SchemaReference> includes = schema.getIncludes();
        for (SchemaReference included : includes) {
            String start = included.getSchemaLocationURI();
            if (start != null) {
                try {
                    //check to see if it's aleady in a URL format.  If so, leave it.
                    new URL(start);
                } catch (MalformedURLException e) {
                    if (!doneSchemas.containsKey(start)) {
                        doneSchemas.put(start, included);
                        updateSchemaImports(included.getReferencedSchema(), doneSchemas, base);
                    }
                }
            }
        }
    }
    
    public static void writeTo(Node node, OutputStream os) {
        writeTo(new DOMSource(node), os);
    }
    
    public static void writeTo(Source src, OutputStream os) {
        Transformer it;
        try {
            it = transformerFactory.newTransformer();
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
