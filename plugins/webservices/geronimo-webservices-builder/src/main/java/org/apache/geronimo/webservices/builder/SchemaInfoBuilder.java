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

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Stack;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;
import javax.wsdl.Definition;
import javax.wsdl.Import;
import javax.wsdl.Service;
import javax.wsdl.Types;
import javax.wsdl.WSDLException;
import javax.wsdl.extensions.ExtensibilityElement;
import javax.wsdl.extensions.ExtensionRegistry;
import javax.wsdl.extensions.UnknownExtensibilityElement;
import javax.wsdl.extensions.schema.Schema;
import javax.wsdl.factory.WSDLFactory;
import javax.wsdl.xml.WSDLLocator;
import javax.wsdl.xml.WSDLReader;
import javax.xml.namespace.QName;

import com.ibm.wsdl.extensions.PopulatedExtensionRegistry;
import com.ibm.wsdl.extensions.schema.SchemaConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.geronimo.webservices.WebServiceContainer;
import org.apache.geronimo.common.DeploymentException;
import org.apache.geronimo.xbeans.wsdl.DefinitionsDocument;
import org.apache.geronimo.xbeans.wsdl.TDefinitions;
import org.apache.geronimo.xbeans.wsdl.TPort;
import org.apache.geronimo.xbeans.wsdl.TService;
import org.apache.geronimo.deployment.xmlbeans.XmlBeansUtil;
import org.apache.xmlbeans.SchemaField;
import org.apache.xmlbeans.SchemaGlobalElement;
import org.apache.xmlbeans.SchemaParticle;
import org.apache.xmlbeans.SchemaType;
import org.apache.xmlbeans.SchemaTypeSystem;
import org.apache.xmlbeans.XmlBeans;
import org.apache.xmlbeans.XmlCursor;
import org.apache.xmlbeans.XmlError;
import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlObject;
import org.apache.xmlbeans.XmlOptions;
import org.apache.xmlbeans.impl.xb.xsdschema.SchemaDocument;
import org.w3c.dom.Element;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * @version $Rev$ $Date$
 */
public class SchemaInfoBuilder {
    private static final Logger log = LoggerFactory.getLogger(SchemaInfoBuilder.class);
    private static final SchemaTypeSystem basicTypeSystem;
//  private static final String[] errorNames = {"Error", "Warning", "Info"};
    private static final String SOAP_NS = "http://schemas.xmlsoap.org/wsdl/soap/";
    private static final QName ADDRESS_QNAME = new QName(SOAP_NS, "address");
    private static final QName LOCATION_QNAME = new QName("", "location");

    static {
        InputStream is = WSDescriptorParser.class.getClassLoader().getResourceAsStream("META-INF/schema/soap_encoding_1_1.xsd");
        if (is == null) {
            throw new RuntimeException("Could not locate soap encoding schema");
        }
        ArrayList errors = new ArrayList();
        XmlOptions xmlOptions = XmlBeansUtil.createXmlOptions(errors);
        try {
            SchemaDocument parsed = SchemaDocument.Factory.parse(is, xmlOptions);
            if (errors.size() != 0) {
                throw new XmlException(errors.toArray().toString());
            }

            basicTypeSystem = XmlBeans.compileXsd(new XmlObject[]{parsed}, XmlBeans.getBuiltinTypeSystem(), xmlOptions);
            if (errors.size() > 0) {
                throw new RuntimeException("Could not compile schema type system: errors: " + errors);
            }
        } catch (XmlException e) {
            throw new RuntimeException("Could not compile schema type system", e);
        } catch (IOException e) {
            throw new RuntimeException("Could not compile schema type system", e);
        } finally {
            try {
                is.close();
            } catch (IOException ignore) {
                // ignore
            }
        }
    }

    private final JarFile moduleFile;
    private final Definition definition;
    private final Stack uris = new Stack();
    private final Map wsdlMap = new HashMap();
    private final Map schemaTypeKeyToSchemaTypeMap;
    private final Map complexTypeMap;
    private final Map elementMap;
    private final Map simpleTypeMap;
    private final Map portMap;


    public SchemaInfoBuilder(JarFile moduleFile, URI wsdlUri) throws DeploymentException {
        this(moduleFile, wsdlUri, null, null);
    }

    public SchemaInfoBuilder(JarFile moduleFile, Definition definition) throws DeploymentException {
        this(moduleFile, null, definition, null);
    }

    SchemaInfoBuilder(JarFile moduleFile, URI uri, SchemaTypeSystem schemaTypeSystem) throws DeploymentException {
        this(moduleFile, uri, null, schemaTypeSystem);
    }

    SchemaInfoBuilder(JarFile moduleFile, URI uri, Definition definition, SchemaTypeSystem schemaTypeSystem) throws DeploymentException {
        this.moduleFile = moduleFile;
        if (uri != null) {
            uris.push(uri);
            if (definition == null && schemaTypeSystem == null) {
                definition = readWsdl(moduleFile, uri);
            }
        } else if (definition != null) {
            try {
                uri = new URI(definition.getDocumentBaseURI());
                uris.push(uri);
            } catch (URISyntaxException e) {
                throw new DeploymentException("Could not locate definition", e);
            }
        } else {
            throw new DeploymentException("You must supply uri or definition");
        }
        if (schemaTypeSystem == null) {
            schemaTypeSystem = compileSchemaTypeSystem(definition);
        }
        this.definition = definition;
        schemaTypeKeyToSchemaTypeMap = buildSchemaTypeKeyToSchemaTypeMap(schemaTypeSystem);
        complexTypeMap = buildComplexTypeMap();
        simpleTypeMap = buildSimpleTypeMap();
        elementMap = buildElementMap();
        portMap = buildPortMap();
    }

    public Map getSchemaTypeKeyToSchemaTypeMap() {
        return schemaTypeKeyToSchemaTypeMap;
    }

    public Definition getDefinition() {
        return definition;
    }

    public Map getWsdlMap() {
        return wsdlMap;
    }

    /**
     * Find all the complex types in the previously constructed schema analysis.
     * Put them in a map from complex type QName to schema fragment.
     *
     * @return map of complexType QName to schema fragment
     */
    public Map getComplexTypesInWsdl() {
        return complexTypeMap;
    }

    private Map buildComplexTypeMap() {
        Map complexTypeMap = new HashMap();
        for (Iterator iterator = schemaTypeKeyToSchemaTypeMap.entrySet().iterator(); iterator.hasNext();) {
            Map.Entry entry = (Map.Entry) iterator.next();
            SchemaTypeKey key = (SchemaTypeKey) entry.getKey();
            if (!key.isSimpleType() && !key.isAnonymous()) {
                QName qName = key.getqName();
                SchemaType schemaType = (SchemaType) entry.getValue();
                complexTypeMap.put(qName, schemaType);
            }
        }
        return complexTypeMap;
    }

    public Map getElementToTypeMap() {
        return elementMap;
    }

    private Map buildElementMap() {
        Map elementToTypeMap = new HashMap();
        for (Iterator iterator = schemaTypeKeyToSchemaTypeMap.entrySet().iterator(); iterator.hasNext();) {
            Map.Entry entry = (Map.Entry) iterator.next();
            SchemaTypeKey key = (SchemaTypeKey) entry.getKey();
            if (key.isElement()) {
                QName elementQName = key.getqName();
                SchemaType schemaType = (SchemaType) entry.getValue();
                QName typeQName = schemaType.getName();
                elementToTypeMap.put(elementQName, typeQName);
            }
        }
        return elementToTypeMap;
    }

    /**
     * Gets a map of all the javax.wsdl.Port instance in the WSDL definition keyed by the port's QName
     * <p/>
     * WSDL 1.1 spec: 2.6 "The name attribute provides a unique name among all ports defined within in the enclosing WSDL document."
     *
     * @return Map of port QName to javax.wsdl.Port for that QName.
     */

    public Map getPortMap() {
        return portMap;
    }

    private Map buildPortMap() {
        HashMap ports = new HashMap();
        if (definition != null) {
            Collection services = definition.getServices().values();
            for (Iterator iterator = services.iterator(); iterator.hasNext();) {
                Service service = (Service) iterator.next();
                ports.putAll(service.getPorts());
            }
        }
        return ports;
    }

    public Map getSimpleTypeMap() {
        return simpleTypeMap;
    }

    private Map buildSimpleTypeMap() {
        Map simpleTypeMap = new HashMap();
        for (Iterator iterator = schemaTypeKeyToSchemaTypeMap.entrySet().iterator(); iterator.hasNext();) {
            Map.Entry entry = (Map.Entry) iterator.next();
            SchemaTypeKey key = (SchemaTypeKey) entry.getKey();
            if (key.isSimpleType() && !key.isAnonymous()) {
                QName qName = key.getqName();
                SchemaType schemaType = (SchemaType) entry.getValue();
                simpleTypeMap.put(qName, schemaType);
            }
        }
        return simpleTypeMap;
    }

    public SchemaTypeSystem compileSchemaTypeSystem(Definition definition) throws DeploymentException {
        List schemaList = new ArrayList();
        addImportsFromDefinition(definition, schemaList);
//        System.out.println("Schemas: " + schemaList);
        Collection errors = new ArrayList();
        XmlOptions xmlOptions = new XmlOptions();
        xmlOptions.setErrorListener(errors);
        xmlOptions.setEntityResolver(new JarEntityResolver());
        XmlObject[] schemas = (XmlObject[]) schemaList.toArray(new XmlObject[schemaList.size()]);
        try {
            SchemaTypeSystem schemaTypeSystem = XmlBeans.compileXsd(schemas, basicTypeSystem, xmlOptions);
            if (errors.size() > 0) {
                boolean wasError = false;
                for (Iterator iterator = errors.iterator(); iterator.hasNext();) {
                    XmlError xmlError = (XmlError) iterator.next();
                    if(xmlError.getSeverity() == XmlError.SEVERITY_ERROR) {
                        log.error(xmlError.toString(), xmlError);
                        wasError = true;
                    } else if(xmlError.getSeverity() == XmlError.SEVERITY_WARNING) {
                        log.warn(xmlError.toString(), xmlError);
                    } else if(xmlError.getSeverity() == XmlError.SEVERITY_INFO) {
                        log.debug(xmlError.toString(), xmlError);
                    }
                }
                if (wasError) {
                    throw new DeploymentException("Could not compile schema type system, see log for errors");
                }
            }
            return schemaTypeSystem;
        } catch (XmlException e) {
            throw new DeploymentException("Could not compile schema type system: " + schemaList, e);
        }
    }

    private void addImportsFromDefinition(Definition definition, List schemaList) throws DeploymentException {
        Map namespaceMap = definition.getNamespaces();
        Types types = definition.getTypes();
        if (types != null) {
            List schemas = types.getExtensibilityElements();
            for (Iterator iterator = schemas.iterator(); iterator.hasNext();) {
                Object o = iterator.next();
                if (o instanceof Schema) {
                    Schema unknownExtensibilityElement = (Schema) o;
                    QName elementType = unknownExtensibilityElement.getElementType();
                    if (new QName("http://www.w3.org/2001/XMLSchema", "schema").equals(elementType)) {
                        Element element = unknownExtensibilityElement.getElement();
                        addSchemaElement(element, namespaceMap, schemaList);
                    }
                } else if (o instanceof UnknownExtensibilityElement) {
                    //This is allegedly obsolete as of axis-wsdl4j-1.2-RC3.jar which includes the Schema extension above.
                    //The change notes imply that imported schemas should end up in Schema elements.  They don't, so this is still needed.
                    UnknownExtensibilityElement unknownExtensibilityElement = (UnknownExtensibilityElement) o;
                    Element element = unknownExtensibilityElement.getElement();
                    String elementNamespace = element.getNamespaceURI();
                    String elementLocalName = element.getNodeName();
                    if ("http://www.w3.org/2001/XMLSchema".equals(elementNamespace) && "schema".equals(elementLocalName)) {
                        addSchemaElement(element, namespaceMap, schemaList);
                    }
                }
            }
        }
        Map imports = definition.getImports();
        if (imports != null) {
            for (Iterator iterator = imports.entrySet().iterator(); iterator.hasNext();) {
                Map.Entry entry = (Map.Entry) iterator.next();
                String namespaceURI = (String) entry.getKey();
                List importList = (List) entry.getValue();
                for (Iterator iterator1 = importList.iterator(); iterator1.hasNext();) {
                    Import anImport = (Import) iterator1.next();
                    //according to the 1.1 jwsdl mr shcema imports are supposed to show up here,
                    //but according to the 1.0 spec there is supposed to be no Definition.
                    Definition definition1 = anImport.getDefinition();
                    if (definition1 != null) {
                        try {
                            URI uri = new URI(definition1.getDocumentBaseURI());
                            uris.push(uri);
                        } catch (URISyntaxException e) {
                            throw new DeploymentException("Could not locate definition", e);
                        }
                        try {
                            addImportsFromDefinition(definition1, schemaList);
                        } finally {
                            uris.pop();
                        }
                    } else {
                        log.warn("Missing definition in import for namespace " + namespaceURI);
                    }
                }
            }
        }
    }

    private void addSchemaElement(Element element, Map namespaceMap, List schemaList) throws DeploymentException {
        try {
            XmlObject xmlObject = parseWithNamespaces(element, namespaceMap);
            schemaList.add(xmlObject);
        } catch (XmlException e) {
            throw new DeploymentException("Could not parse schema element", e);
        }
    }

    static XmlObject parseWithNamespaces(Element element, Map namespaceMap) throws XmlException {
        ArrayList errors = new ArrayList();
        XmlOptions xmlOptions = XmlBeansUtil.createXmlOptions(errors);
        SchemaDocument parsed = SchemaDocument.Factory.parse(element, xmlOptions);
        if (errors.size() != 0) {
            throw new XmlException(errors.toArray().toString());
        }
        XmlCursor cursor = parsed.newCursor();
        try {
            cursor.toFirstContentToken();
            for (Iterator namespaces = namespaceMap.entrySet().iterator(); namespaces.hasNext();) {
                Map.Entry entry = (Map.Entry) namespaces.next();
                cursor.insertNamespace((String) entry.getKey(), (String) entry.getValue());
            }
        } finally {
            cursor.dispose();
        }
        return parsed;
    }

    /**
     * builds a map of SchemaTypeKey containing jaxrpc-style fake QName and context info to xmlbeans SchemaType object.
     *
     * @param schemaTypeSystem
     * @return Map of SchemaTypeKey to xmlbeans SchemaType object.
     */
    private Map buildSchemaTypeKeyToSchemaTypeMap(SchemaTypeSystem schemaTypeSystem) {
        Map qnameMap = new HashMap();
        SchemaType[] globalTypes = schemaTypeSystem.globalTypes();
        for (int i = 0; i < globalTypes.length; i++) {
            SchemaType globalType = globalTypes[i];
            QName typeQName = globalType.getName();
            addSchemaType(typeQName, globalType, false, qnameMap);
        }
        SchemaGlobalElement[] globalElements = schemaTypeSystem.globalElements();
        for (int i = 0; i < globalElements.length; i++) {
            SchemaGlobalElement globalElement = globalElements[i];
            addElement(globalElement, null, qnameMap);
        }
        return qnameMap;
    }

    private void addElement(SchemaField element, SchemaTypeKey key, Map qnameMap) {
        //TODO is this null if element is a ref?
        QName elementName = element.getName();
        String elementNamespace = elementName.getNamespaceURI();
        //"" namespace means local element with elementFormDefault="unqualified"
        if (elementNamespace == null || elementNamespace.equals("")) {
            elementNamespace = key.getqName().getNamespaceURI();
        }
        String elementQNameLocalName;
        SchemaTypeKey elementKey;
        if (key == null) {
            //top level. rule 2.a,
            elementQNameLocalName = elementName.getLocalPart();
            elementKey = new SchemaTypeKey(elementName, true, false, false, elementName);
        } else {
            //not top level. rule 2.b, key will be for enclosing Type.
            QName enclosingTypeQName = key.getqName();
            String enclosingTypeLocalName = enclosingTypeQName.getLocalPart();
            elementQNameLocalName = enclosingTypeLocalName + ">" + elementName.getLocalPart();
            QName subElementName = new QName(elementNamespace, elementQNameLocalName);
            elementKey = new SchemaTypeKey(subElementName, true, false, true, elementName);
        }
        SchemaType schemaType = element.getType();
        qnameMap.put(elementKey, schemaType);
//        new Exception("Adding: " + elementKey.getqName().getLocalPart()).printStackTrace();
        //check if it's an array. maxOccurs is null if unbounded
        //element should always be a SchemaParticle... this is a workaround for XMLBEANS-137
        if (element instanceof SchemaParticle) {
            addArrayForms((SchemaParticle) element, elementKey.getqName(), qnameMap, schemaType);
        } else {
            log.warn("element is not a schemaParticle! " + element);
        }
        //now, name for type.  Rule 1.b, type inside an element
        String typeQNameLocalPart = ">" + elementQNameLocalName;
        QName typeQName = new QName(elementNamespace, typeQNameLocalPart);
        boolean isAnonymous = true;
        addSchemaType(typeQName, schemaType, isAnonymous, qnameMap);
    }

    private void addSchemaType(QName typeQName, SchemaType schemaType, boolean anonymous, Map qnameMap) {
        SchemaTypeKey typeKey = new SchemaTypeKey(typeQName, false, schemaType.isSimpleType(), anonymous, null);
        qnameMap.put(typeKey, schemaType);
//        new Exception("Adding: " + typeKey.getqName().getLocalPart()).printStackTrace();
        //TODO xmlbeans recommends using summary info from getElementProperties and getAttributeProperties instead of traversing the content model by hand.
        SchemaParticle schemaParticle = schemaType.getContentModel();
        if (schemaParticle != null) {
            addSchemaParticle(schemaParticle, typeKey, qnameMap);
        }
    }


    private void addSchemaParticle(SchemaParticle schemaParticle, SchemaTypeKey key, Map qnameMap) {
        if (schemaParticle.getParticleType() == SchemaParticle.ELEMENT) {
            SchemaType elementType = schemaParticle.getType();
            SchemaField element = elementType.getContainerField();
            //element will be null if the type is defined elsewhere, such as a built in type.
            if (element != null) {
                addElement(element, key, qnameMap);
            } else {
                QName keyQName = key.getqName();
                //TODO I can't distinguish between 3.a and 3.b, so generate names both ways.
                //3.b
                String localPart = schemaParticle.getName().getLocalPart();
                QName elementName = new QName(keyQName.getNamespaceURI(), localPart);
                addArrayForms(schemaParticle, elementName, qnameMap, elementType);
                //3.a
                localPart = keyQName.getLocalPart() + ">" + schemaParticle.getName().getLocalPart();
                elementName = new QName(keyQName.getNamespaceURI(), localPart);
                addArrayForms(schemaParticle, elementName, qnameMap, elementType);
            }
        } else {
            try {
                SchemaParticle[] children = schemaParticle.getParticleChildren();
                for (int i = 0; i < children.length; i++) {
                    SchemaParticle child = children[i];
                    addSchemaParticle(child, key, qnameMap);
                }
            } catch (NullPointerException e) {
                //ignore xmlbeans bug
            }
        }
    }

    private void addArrayForms(SchemaParticle schemaParticle, QName keyName, Map qnameMap, SchemaType elementType) {
        //it may be a ref or a built in type.  If it's an array (maxOccurs >1) form a type for it.
        if (schemaParticle.getIntMaxOccurs() > 1) {
            String maxOccurs = schemaParticle.getMaxOccurs() == null ? "unbounded" : "" + schemaParticle.getIntMaxOccurs();
            int minOccurs = schemaParticle.getIntMinOccurs();
            QName elementName = schemaParticle.getName();
            String arrayQNameLocalName = keyName.getLocalPart() + "[" + minOccurs + "," + maxOccurs + "]";
            String elementNamespace = elementName.getNamespaceURI();
            if (elementNamespace == null || elementNamespace.equals("")) {
                elementNamespace = keyName.getNamespaceURI();
            }
            QName arrayName = new QName(elementNamespace, arrayQNameLocalName);
            SchemaTypeKey arrayKey = new SchemaTypeKey(arrayName, false, false, true, elementName);
            //TODO not clear we want the schemaType as the value
            qnameMap.put(arrayKey, elementType);
//            new Exception("Adding: " + arrayKey.getqName().getLocalPart()).printStackTrace();
            if (minOccurs == 1) {
                arrayQNameLocalName = keyName.getLocalPart() + "[," + maxOccurs + "]";
                arrayName = new QName(elementNamespace, arrayQNameLocalName);
                arrayKey = new SchemaTypeKey(arrayName, false, false, true, elementName);
                //TODO not clear we want the schemaType as the value
                qnameMap.put(arrayKey, elementType);
            }
        }
    }


    public Definition readWsdl(JarFile moduleFile, URI wsdlURI) throws DeploymentException {
        Definition definition;
        WSDLFactory wsdlFactory;
        try {
            wsdlFactory = WSDLFactory.newInstance();
        } catch (WSDLException e) {
            throw new DeploymentException("Could not create WSDLFactory", e);
        }
        WSDLReader wsdlReaderNoImport = wsdlFactory.newWSDLReader();
        wsdlReaderNoImport.setFeature("javax.wsdl.importDocuments", false);
        ExtensionRegistry extensionRegistry = new PopulatedExtensionRegistry();
        extensionRegistry.mapExtensionTypes(Types.class, SchemaConstants.Q_ELEM_XSD_1999,
                UnknownExtensibilityElement.class);
        extensionRegistry.registerDeserializer(Types.class, SchemaConstants.Q_ELEM_XSD_1999,
                extensionRegistry.getDefaultDeserializer());
        extensionRegistry.registerSerializer(Types.class, SchemaConstants.Q_ELEM_XSD_1999,
                extensionRegistry.getDefaultSerializer());

        extensionRegistry.mapExtensionTypes(Types.class, SchemaConstants.Q_ELEM_XSD_2000,
                UnknownExtensibilityElement.class);
        extensionRegistry.registerDeserializer(Types.class, SchemaConstants.Q_ELEM_XSD_2000,
                extensionRegistry.getDefaultDeserializer());
        extensionRegistry.registerSerializer(Types.class, SchemaConstants.Q_ELEM_XSD_2000,
                extensionRegistry.getDefaultSerializer());

        extensionRegistry.mapExtensionTypes(Types.class, SchemaConstants.Q_ELEM_XSD_2001,
                UnknownExtensibilityElement.class);
        extensionRegistry.registerDeserializer(Types.class, SchemaConstants.Q_ELEM_XSD_2001,
                extensionRegistry.getDefaultDeserializer());
        extensionRegistry.registerSerializer(Types.class, SchemaConstants.Q_ELEM_XSD_2001,
                extensionRegistry.getDefaultSerializer());
        wsdlReaderNoImport.setExtensionRegistry(extensionRegistry);

        JarWSDLLocator wsdlLocator = new JarWSDLLocator(wsdlURI);
        WSDLReader wsdlReader = wsdlFactory.newWSDLReader();

        Thread thread = Thread.currentThread();
        ClassLoader oldCl = thread.getContextClassLoader();
        thread.setContextClassLoader(this.getClass().getClassLoader());
        try {
            try {
                definition = wsdlReader.readWSDL(wsdlLocator);
            } catch (WSDLException e) {
                throw new DeploymentException("Failed to read wsdl document", e);
            } catch (RuntimeException e) {
                throw new DeploymentException(e.getMessage(), e);
            }
        } finally {
            thread.setContextClassLoader(oldCl);
        }

        return definition;
    }

    public static ExtensibilityElement getExtensibilityElement(Class clazz, List extensibilityElements) throws DeploymentException {
        for (Iterator iterator = extensibilityElements.iterator(); iterator.hasNext();) {
            ExtensibilityElement extensibilityElement = (ExtensibilityElement) iterator.next();
            if (clazz.isAssignableFrom(extensibilityElement.getClass())) {
                return extensibilityElement;
            }
        }
        throw new DeploymentException("No element of class " + clazz.getName() + " found");
    }

    public String movePortLocation(String portComponentName, String servletLocation) throws DeploymentException {
        DefinitionsDocument doc = (DefinitionsDocument) wsdlMap.get(uris.get(0));
        TDefinitions definitions = doc.getDefinitions();
        TService[] services = definitions.getServiceArray();
        for (int i = 0; i < services.length; i++) {
            TService service = services[i];
            TPort[] ports = service.getPortArray();
            for (int j = 0; j < ports.length; j++) {
                TPort port = ports[j];
                if (port.getName().trim().equals(portComponentName)) {
                    XmlCursor portCursor = port.newCursor();
                    try {
                        if (portCursor.toChild(ADDRESS_QNAME)) {
                            if (servletLocation == null) {
                                String original = portCursor.getAttributeText(LOCATION_QNAME);
                                URI originalURI = new URI(original);
                                servletLocation = originalURI.getPath();
                            }
                            portCursor.setAttributeText(LOCATION_QNAME, WebServiceContainer.LOCATION_REPLACEMENT_TOKEN + servletLocation);
                            return servletLocation;
                        }
                    } catch (URISyntaxException e) {
                        throw new DeploymentException("Could not construct URI for ejb location in wsdl", e);
                    } finally {
                        portCursor.dispose();
                    }
                }
            }
        }
        throw new DeploymentException("No port found with name " + portComponentName + " expected at " + servletLocation);
    }

    private class JarEntityResolver implements EntityResolver {

        private final static String PROJECT_URL_PREFIX = "project://local/";

        public InputSource resolveEntity(String publicId, String systemId) throws SAXException, IOException {
            //seems like this must be a bug in xmlbeans...
            if (systemId.indexOf(PROJECT_URL_PREFIX) > -1) {
                systemId = systemId.substring(PROJECT_URL_PREFIX.length());
            }
            URI location = ((URI) uris.peek()).resolve(systemId);
            InputStream wsdlInputStream;
            try {
                ZipEntry entry = moduleFile.getEntry(location.toString());
                wsdlInputStream = moduleFile.getInputStream(entry);
                XmlObject xmlObject = SchemaDocument.Factory.parse(wsdlInputStream);
                wsdlMap.put(location, xmlObject);
                wsdlInputStream.close();
                wsdlInputStream = moduleFile.getInputStream(entry);
            } catch (XmlException e) {
                throw (IOException) new IOException("Could not parse schema document").initCause(e);
            }
            return new InputSource(wsdlInputStream);
        }
    }

    class JarWSDLLocator implements WSDLLocator {

        private final List streams = new ArrayList();
        private final URI wsdlURI;
        private URI latestImportURI;

        public JarWSDLLocator(URI wsdlURI) {
            this.wsdlURI = wsdlURI;
        }

        public InputSource getBaseInputSource() {
            InputStream wsdlInputStream;
            ZipEntry entry = moduleFile.getEntry(wsdlURI.toString());
            if(entry == null){
                throw new RuntimeException("The webservices.xml file points to a non-existant WSDL file "+wsdlURI.toString());
            }
            try {
                wsdlInputStream = moduleFile.getInputStream(entry);
                DefinitionsDocument definition = DefinitionsDocument.Factory.parse(wsdlInputStream);
                wsdlMap.put(wsdlURI, definition);
                wsdlInputStream.close();
                wsdlInputStream = moduleFile.getInputStream(entry);
                streams.add(wsdlInputStream);
            } catch (Exception e) {
                throw new RuntimeException("Could not open stream to wsdl file", e);
            }
            return new InputSource(wsdlInputStream);
        }

        public String getBaseURI() {
            return wsdlURI.toString();
        }

        public InputSource getImportInputSource(String parentLocation, String relativeLocation) {
            URI parentURI = URI.create(parentLocation);
            latestImportURI = parentURI.resolve(relativeLocation);
            InputStream importInputStream;
            try {
                ZipEntry entry = moduleFile.getEntry(latestImportURI.toString());
                importInputStream = moduleFile.getInputStream(entry);
                try {
                    DefinitionsDocument definition = DefinitionsDocument.Factory.parse(importInputStream);
                    importInputStream.close();
                    wsdlMap.put(latestImportURI, definition);
                    importInputStream.close();
                } catch (XmlException e) {
                    //probably was a schema rather than wsdl.  If there are real problems they will show up later.
                }
                importInputStream = moduleFile.getInputStream(entry);
                streams.add(importInputStream);
            } catch (Exception e) {
                throw new RuntimeException("Could not open stream to import file", e);
            }
            InputSource inputSource = new InputSource(importInputStream);
            inputSource.setSystemId(getLatestImportURI());
            return inputSource;
        }

        public String getLatestImportURI() {
            return latestImportURI.toString();
        }

        public void close() {
            for (Iterator iterator = streams.iterator(); iterator.hasNext();) {
                InputStream inputStream = (InputStream) iterator.next();
                try {
                    inputStream.close();
                } catch (IOException e) {
                    //ignore
                }
            }
            streams.clear();
        }
    }
}
