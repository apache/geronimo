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
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
import javax.xml.namespace.QName;
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
import org.apache.geronimo.jaxws.PortInfo;
import org.apache.geronimo.jaxws.WSDLUtils;
import org.apache.geronimo.jaxws.wsdl.CatalogWSDLLocator;
import org.apache.xml.resolver.Catalog;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class WSDLQueryHandler {

    private static final Logger LOG = LoggerFactory.getLogger(WSDLQueryHandler.class);

    public static final String WSDL_QUERY_MULTIPLE_PORT = "org.apache.geronimo.webservices.wsdlquery.multipleport";

    private static final boolean multiplePortInWSDLQuery = Boolean.getBoolean(WSDL_QUERY_MULTIPLE_PORT);

    private static TransformerFactory transformerFactory = TransformerFactory.newInstance();

    private Map<String, Definition> wsdlMap;
    private Map<String, SchemaReference> schemaMap;
    private Map<String, String> importMap;
    private int wsdlCounter;
    private int schemaCounter;
    private AxisService service;
    private Catalog catalog;
    private Map<QName,Map<String, String>> servicePortNameLocationMap;
    private Map<QName, Set<String>> servicePortNamesMap;
    private PortInfo portInfo;

    public WSDLQueryHandler(AxisService service) {
        this(service, null, null);
    }

    public WSDLQueryHandler(AxisService service, PortInfo portInfo, Collection<PortInfo> portInfos) {
        this.service = service;
        EndpointDescription ed = AxisServiceGenerator.getEndpointDescription(this.service);
        JAXWSCatalogManager catalogManager = ed.getServiceDescription().getCatalogManager();
        if (catalogManager != null) {
            this.catalog = catalogManager.getCatalog();
        }
        if (portInfo != null && portInfos != null) {
            this.portInfo = portInfo;
            servicePortNameLocationMap = new HashMap<QName, Map<String, String>>();
            servicePortNamesMap = new HashMap<QName, Set<String>>();
            for (PortInfo p : portInfos) {
                if (p.getWsdlFile() == null || !p.getWsdlFile().equals(portInfo.getWsdlFile())) {
                    continue;
                }
                QName serviceName = p.getWsdlService();
                QName portName = p.getWsdlPort();
                if (serviceName == null || portName == null) {
                    continue;
                }
                Map<String, String> portNameLocationMap = servicePortNameLocationMap.get(serviceName);
                if (portNameLocationMap == null) {
                    portNameLocationMap = new HashMap<String, String>();
                    servicePortNameLocationMap.put(serviceName, portNameLocationMap);
                }
                portNameLocationMap.put(portName.getLocalPart(), p.getLocation());
                Set<String> portNames = servicePortNamesMap.get(serviceName);
                if(portNames == null) {
                    portNames = new HashSet<String>();
                    servicePortNamesMap.put(serviceName, portNames);
                }
                portNames.add(portName.getLocalPart());
            }
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
            if (wsdlKey.isEmpty()) {
                if(multiplePortInWSDLQuery && servicePortNameLocationMap != null) {
                    String serverUrl = base.substring(0, base.length() - portInfo.getLocation().length());
                    Map<QName, Map<String, String>> updatedServicePortNameLocationMap = new HashMap<QName, Map<String, String>>();
                    for(Map.Entry<QName, Map<String, String>> servicePortNameLocationEntry : servicePortNameLocationMap.entrySet()) {
                        Map<String, String> updatedPortNameLocationMap = new HashMap<String, String>();
                        for(Map.Entry<String, String> portNameLocationEntry : servicePortNameLocationEntry.getValue().entrySet()) {
                            updatedPortNameLocationMap.put(portNameLocationEntry.getKey(), serverUrl + portNameLocationEntry.getValue());
                        }
                        updatedServicePortNameLocationMap.put(servicePortNameLocationEntry.getKey(), updatedPortNameLocationMap);
                    }
                    WSDLUtils.updateLocations(def, updatedServicePortNameLocationMap);
                }
                else {
                    WSDLUtils.updateLocations(def, base);
                }
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
        if (importMap == null) {
            wsdlMap = new HashMap<String, Definition>();
            schemaMap = new HashMap<String, SchemaReference>();
            importMap = new HashMap<String, String>();

            Map<String, String> docMap = new HashMap<String, String>();

            WSDLFactory factory = WSDLFactory.newInstance();
            WSDLReader reader = factory.newWSDLReader();
            reader.setFeature("javax.wsdl.importDocuments", true);
            reader.setFeature("javax.wsdl.verbose", false);
            Definition def = reader.readWSDL(new CatalogWSDLLocator(wsdlUri, this.catalog));
            updateDefinition("", def, docMap, base);
            if (multiplePortInWSDLQuery) {
                WSDLUtils.trimDefinition(def, servicePortNamesMap);
            } else {
                WSDLUtils.trimDefinition(def, this.service.getName(), this.service.getEndpointName());
            }
            wsdlMap.put("", def);
        }
    }

    private void updateWSDLImports(Element rootElement, String base, String parentDocKey) {
        NodeList nl = rootElement.getElementsByTagNameNS("http://schemas.xmlsoap.org/wsdl/", "import");
        for (int x = 0; x < nl.getLength(); x++) {
            Element el = (Element) nl.item(x);
            String location = el.getAttribute("location");
            String importId = parentDocKey + "/" + location;
            String docKey = importMap.get(importId);
            if (docKey != null) {
                el.setAttribute("location", base + "?wsdl=" + docKey);
            }
        }
    }

    private void updateSchemaImports(Element rootElement, String base, String parentDocKey) {
        NodeList nl = rootElement.getElementsByTagNameNS("http://www.w3.org/2001/XMLSchema", "import");
        for (int x = 0; x < nl.getLength(); x++) {
            Element el = (Element) nl.item(x);
            String location = el.getAttribute("schemaLocation");
            String importId = parentDocKey + "/" + location;
            String docKey = importMap.get(importId);
            if (docKey != null) {
                el.setAttribute("schemaLocation", base + "?xsd=" + docKey);
            }
        }

        nl = rootElement.getElementsByTagNameNS("http://www.w3.org/2001/XMLSchema", "include");
        for (int x = 0; x < nl.getLength(); x++) {
            Element el = (Element) nl.item(x);
            String location = el.getAttribute("schemaLocation");
            String importId = parentDocKey + "/" + location;
            String docKey = importMap.get(importId);
            if (docKey != null) {
                el.setAttribute("schemaLocation", base + "?xsd=" + docKey);
            }
        }
    }

    protected void updateDefinition(String parentDocKey,
                                    Definition def,
                                    Map<String, String> docMap,
                                    String base) {
        Collection<List<Import>> imports = def.getImports().values();
        for (List<Import> impLst : imports) {
            for (Import imp : impLst) {
                String docURI = imp.getDefinition().getDocumentBaseURI();
                String location = imp.getLocationURI();
                String importId = parentDocKey + "/" + location;
                String docKey = docMap.get(docURI);
                if (docKey == null) {
                    docKey = getUniqueWSDLId();
                    docMap.put(docURI, docKey);
                    wsdlMap.put(docKey, imp.getDefinition());
                    updateDefinition(docKey, imp.getDefinition(), docMap, base);
                }
                importMap.put(importId, docKey);
            }
        }

        Types types = def.getTypes();
        if (types != null) {
            for (ExtensibilityElement el : (List<ExtensibilityElement>)types.getExtensibilityElements()) {
                if (el instanceof Schema) {
                    Schema schema = (Schema)el;
                    updateSchemaImports(parentDocKey, schema, docMap, base);
                }
            }
        }
    }

    protected void updateSchemaImports(String parentDocKey,
                                       Schema schema,
                                       Map<String, String> docMap,
                                       String base) {
        @SuppressWarnings("unchecked")
        Collection<List<SchemaImport>> imports = schema.getImports().values();
        for (List<SchemaImport> impList : imports) {
            for (SchemaImport s : impList) {
                Schema importedSchema = s.getReferencedSchema();
                if (importedSchema == null) {
                    continue;
                }
                String docURI = importedSchema.getDocumentBaseURI();
                String location = s.getSchemaLocationURI();
                String importId = parentDocKey + "/" + location;
                String docKey = docMap.get(docURI);
                if (docKey == null) {
                    docKey = getUniqueSchemaId();
                    docMap.put(docURI, docKey);
                    schemaMap.put(docKey, s);
                    updateSchemaImports(docKey, importedSchema, docMap, base);
                }
                importMap.put(importId, docKey);
            }
        }

        @SuppressWarnings("unchecked")
        List<SchemaReference> includes = schema.getIncludes();
        for (SchemaReference s : includes) {
            Schema includedSchema = s.getReferencedSchema();
            if (includedSchema == null) {
                continue;
            }
            String docURI = includedSchema.getDocumentBaseURI();
            String location = s.getSchemaLocationURI();
            String importId = parentDocKey + "/" + location;
            String docKey = docMap.get(docURI);
            if (docKey == null) {
                docKey = getUniqueSchemaId();
                docMap.put(docURI, docKey);
                schemaMap.put(docKey, s);
                updateSchemaImports(docKey, includedSchema, docMap, base);
            }
            importMap.put(importId, docKey);
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
            it = transformerFactory.newTransformer();
            it.setOutputProperty(OutputKeys.METHOD, "xml");
            it.setOutputProperty(OutputKeys.INDENT, "yes");
            it.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");
            it.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "no");
            it.setOutputProperty(OutputKeys.ENCODING, "utf-8");
            it.transform(src, new StreamResult(os));
        } catch (TransformerException e) {
            LOG.error("Fail to output wsdl", e);
        }
    }

}
