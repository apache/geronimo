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

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.jar.JarFile;
import javax.wsdl.Definition;
import javax.wsdl.Import;
import javax.wsdl.Operation;
import javax.wsdl.Port;
import javax.wsdl.Service;
import javax.wsdl.Types;
import javax.wsdl.WSDLException;
import javax.wsdl.extensions.ExtensibilityElement;
import javax.wsdl.extensions.UnknownExtensibilityElement;
import javax.wsdl.extensions.schema.Schema;
import javax.wsdl.extensions.soap.SOAPAddress;
import javax.wsdl.factory.WSDLFactory;
import javax.wsdl.xml.WSDLLocator;
import javax.wsdl.xml.WSDLReader;
import javax.xml.namespace.QName;
import javax.xml.rpc.holders.BigDecimalHolder;
import javax.xml.rpc.holders.BigIntegerHolder;
import javax.xml.rpc.holders.BooleanHolder;
import javax.xml.rpc.holders.BooleanWrapperHolder;
import javax.xml.rpc.holders.ByteArrayHolder;
import javax.xml.rpc.holders.ByteHolder;
import javax.xml.rpc.holders.ByteWrapperHolder;
import javax.xml.rpc.holders.CalendarHolder;
import javax.xml.rpc.holders.DoubleHolder;
import javax.xml.rpc.holders.DoubleWrapperHolder;
import javax.xml.rpc.holders.FloatHolder;
import javax.xml.rpc.holders.FloatWrapperHolder;
import javax.xml.rpc.holders.IntHolder;
import javax.xml.rpc.holders.IntegerWrapperHolder;
import javax.xml.rpc.holders.LongHolder;
import javax.xml.rpc.holders.LongWrapperHolder;
import javax.xml.rpc.holders.ObjectHolder;
import javax.xml.rpc.holders.QNameHolder;
import javax.xml.rpc.holders.ShortHolder;
import javax.xml.rpc.holders.ShortWrapperHolder;
import javax.xml.rpc.holders.StringHolder;

import org.apache.geronimo.common.DeploymentException;
import org.apache.geronimo.deployment.util.DeploymentUtil;
import org.apache.geronimo.kernel.ClassLoading;
import org.apache.geronimo.schema.SchemaConversionUtils;
import org.apache.geronimo.xbeans.j2ee.ExceptionMappingType;
import org.apache.geronimo.xbeans.j2ee.JavaWsdlMappingDocument;
import org.apache.geronimo.xbeans.j2ee.JavaWsdlMappingType;
import org.apache.geronimo.xbeans.j2ee.PackageMappingType;
import org.apache.geronimo.xbeans.j2ee.PortComponentHandlerType;
import org.apache.geronimo.xbeans.j2ee.PortComponentType;
import org.apache.geronimo.xbeans.j2ee.ServiceEndpointInterfaceMappingType;
import org.apache.geronimo.xbeans.j2ee.ServiceEndpointMethodMappingType;
import org.apache.geronimo.xbeans.j2ee.ServiceImplBeanType;
import org.apache.geronimo.xbeans.j2ee.WebserviceDescriptionType;
import org.apache.geronimo.xbeans.j2ee.WebservicesDocument;
import org.apache.geronimo.xbeans.j2ee.WebservicesType;
import org.apache.xmlbeans.SchemaField;
import org.apache.xmlbeans.SchemaGlobalElement;
import org.apache.xmlbeans.SchemaParticle;
import org.apache.xmlbeans.SchemaType;
import org.apache.xmlbeans.SchemaTypeSystem;
import org.apache.xmlbeans.XmlBeans;
import org.apache.xmlbeans.XmlCursor;
import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlObject;
import org.apache.xmlbeans.XmlOptions;
import org.w3c.dom.Element;
import org.xml.sax.InputSource;

/**
 * @version $Rev:  $ $Date:  $
 */
public class WSDescriptorParser {

    private static SchemaTypeSystem basicTypeSystem;

    static {
        URL url = WSDescriptorParser.class.getClassLoader().getResource("soap_encoding_1_1.xsd");
        if (url == null) {
            throw new RuntimeException("Could not locate soap encoding schema");
        }
        Collection errors = new ArrayList();
        XmlOptions xmlOptions = new XmlOptions();
        xmlOptions.setErrorListener(errors);
        try {
            XmlObject xmlObject = SchemaConversionUtils.parse(url);
            basicTypeSystem = XmlBeans.compileXsd(new XmlObject[]{xmlObject}, XmlBeans.getBuiltinTypeSystem(), xmlOptions);
            if (errors.size() > 0) {
                throw new RuntimeException("Could not compile schema type system: errors: " + errors);
            }
        } catch (XmlException e) {
            throw new RuntimeException("Could not compile schema type system", e);
        } catch (IOException e) {
            throw new RuntimeException("Could not compile schema type system", e);
        }
    }

    public static Map parseWebServiceDescriptor(URL wsDDUrl, JarFile moduleFile, boolean isEJB) throws DeploymentException {
        try {
            WebservicesDocument webservicesDocument = WebservicesDocument.Factory.parse(wsDDUrl);
            SchemaConversionUtils.validateDD(webservicesDocument);
            WebservicesType webservicesType = webservicesDocument.getWebservices();
            return parseWebServiceDescriptor(webservicesType, moduleFile, isEJB);
        } catch (XmlException e) {
            throw new DeploymentException("Could not read descriptor document", e);
        } catch (IOException e) {
            return null;
        }

    }

    /**
     * Parses a webservice.xml file and returns a map PortInfo instances indexed by the
     * corresponding ejb-link or servlet-link element .
     *
     * @param webservicesType
     * @param moduleFile
     * @param isEJB
     * @return
     * @throws DeploymentException
     */
    public static Map parseWebServiceDescriptor(WebservicesType webservicesType, JarFile moduleFile, boolean isEJB) throws DeploymentException {
        Map portMap = new HashMap();
        WebserviceDescriptionType[] webserviceDescriptions = webservicesType.getWebserviceDescriptionArray();
        for (int i = 0; i < webserviceDescriptions.length; i++) {
            WebserviceDescriptionType webserviceDescription = webserviceDescriptions[i];
            URI wsdlURI = null;
            try {
                wsdlURI = new URI(webserviceDescription.getWsdlFile().getStringValue().trim());
            } catch (URISyntaxException e) {
                throw new DeploymentException("could not construct wsdl uri from " + webserviceDescription.getWsdlFile().getStringValue(), e);
            }
            URI jaxrpcMappingURI = null;
            try {
                jaxrpcMappingURI = new URI(webserviceDescription.getJaxrpcMappingFile().getStringValue().trim());
            } catch (URISyntaxException e) {
                throw new DeploymentException("Could not construct jaxrpc mapping uri from " + webserviceDescription.getJaxrpcMappingFile(), e);
            }
            Definition definition = readWsdl(moduleFile, wsdlURI);
            Map wsdlPortMap = WSDescriptorParser.getPortMap(definition);

            JavaWsdlMappingType javaWsdlMapping = readJaxrpcMapping(moduleFile, jaxrpcMappingURI);
            HashMap seiMappings = new HashMap();
            org.apache.geronimo.xbeans.j2ee.ServiceEndpointInterfaceMappingType[] mappings = javaWsdlMapping.getServiceEndpointInterfaceMappingArray();
            for (int j = 0; j < mappings.length; j++) {
                ServiceEndpointInterfaceMappingType seiMapping = mappings[j];
                seiMappings.put(seiMapping.getServiceEndpointInterface().getStringValue(), seiMapping);
            }

            PortComponentType[] portComponents = webserviceDescription.getPortComponentArray();
            for (int j = 0; j < portComponents.length; j++) {
                PortComponentType portComponent = portComponents[j];
                String portComponentName = portComponent.getPortComponentName().getStringValue().trim();
                QName portQName = portComponent.getWsdlPort().getQNameValue();
                String seiInterfaceName = portComponent.getServiceEndpointInterface().getStringValue().trim();
                ServiceImplBeanType serviceImplBeanType = portComponent.getServiceImplBean();
                if (isEJB == serviceImplBeanType.isSetServletLink()) {
                    throw new DeploymentException("Wrong kind of web service described in web service descriptor: expected " + (isEJB ? "EJB" : "POJO(Servlet)"));
                }
                String linkName;
                if (serviceImplBeanType.isSetServletLink()) {
                    linkName = serviceImplBeanType.getServletLink().getStringValue().trim();
                } else {
                    linkName = serviceImplBeanType.getEjbLink().getStringValue().trim();
                }
                PortComponentHandlerType[] handlers = portComponent.getHandlerArray();

                Port port = (Port) wsdlPortMap.get(portQName.getLocalPart());
                if (port == null) {
                    throw new DeploymentException("No WSDL Port definition for port-component " + portComponentName);
                }

                ServiceEndpointInterfaceMappingType seiMapping = (ServiceEndpointInterfaceMappingType) seiMappings.get(seiInterfaceName);

                URL wsdlURL = null;
                try {
                    wsdlURL = DeploymentUtil.createJarURL(moduleFile, webserviceDescription.getWsdlFile().getStringValue().trim());
                } catch (MalformedURLException e) {
                    throw new DeploymentException("Invalid WSDL URL: " + webserviceDescription.getWsdlFile().getStringValue().trim(), e);
                }

                PortInfo portInfo = new PortInfo(portComponentName, portQName, definition, javaWsdlMapping, seiInterfaceName, handlers, port, seiMapping, wsdlURL);

                if (portMap.put(linkName, portInfo) != null) {
                    throw new DeploymentException("Ambiguous description of port associated with j2ee component " + linkName);
                }
            }
        }
        return portMap;
    }


    /**
     * Gets a map of all the javax.wsdl.Port instance in the WSDL definition keyed by the port's QName
     * <p/>
     * WSDL 1.1 spec: 2.6 "The name attribute provides a unique name among all ports defined within in the enclosing WSDL document."
     *
     * @param definition
     * @return
     */

    public static Map getPortMap(Definition definition) {
        HashMap ports = new HashMap();
        Collection services = definition.getServices().values();
        for (Iterator iterator = services.iterator(); iterator.hasNext();) {
            Service service = (Service) iterator.next();
            ports.putAll(service.getPorts());
        }
        return ports;
    }

    public static JavaWsdlMappingType readJaxrpcMapping(JarFile moduleFile, URI jaxrpcMappingURI) throws DeploymentException {
        String jaxrpcMappingPath = jaxrpcMappingURI.toString();
        return readJaxrpcMapping(moduleFile, jaxrpcMappingPath);
    }

    public static JavaWsdlMappingType readJaxrpcMapping(JarFile moduleFile, String jaxrpcMappingPath) throws DeploymentException {
        JavaWsdlMappingType mapping;
        InputStream jaxrpcInputStream = null;
        try {
            jaxrpcInputStream = moduleFile.getInputStream(moduleFile.getEntry(jaxrpcMappingPath));
        } catch (IOException e) {
            throw new DeploymentException("Could not open stream to jaxrpc mapping document", e);
        }
        JavaWsdlMappingDocument mappingDocument = null;
        try {
            mappingDocument = JavaWsdlMappingDocument.Factory.parse(jaxrpcInputStream);
        } catch (XmlException e) {
            throw new DeploymentException("Could not parse jaxrpc mapping document", e);
        } catch (IOException e) {
            throw new DeploymentException("Could not read jaxrpc mapping document", e);
        }
        mapping = mappingDocument.getJavaWsdlMapping();
        return mapping;
    }

    public static Definition readWsdl(JarFile moduleFile, URI wsdlURI) throws DeploymentException {
        Definition definition;
        JarWSDLLocator wsdlLocator = new JarWSDLLocator(moduleFile, wsdlURI);
        WSDLFactory wsdlFactory = null;
        try {
            wsdlFactory = WSDLFactory.newInstance();
        } catch (WSDLException e) {
            throw new DeploymentException("Could not create WSDLFactory", e);
        }
        WSDLReader wsdlReader = wsdlFactory.newWSDLReader();
        try {
            definition = wsdlReader.readWSDL(wsdlLocator);
        } catch (WSDLException e) {
            throw new DeploymentException("Failed to read wsdl document", e);
        }
        return definition;
    }

    public static Map buildSchemaTypeKeyToSchemaTypeMap(Definition definition) throws DeploymentException {
        SchemaTypeSystem schemaTypeSystem = compileSchemaTypeSystem(definition);
        return buildSchemaTypeKeyToSchemaTypeMap(schemaTypeSystem);
    }

    public static SchemaTypeSystem compileSchemaTypeSystem(Definition definition) throws DeploymentException {
        List schemaList = new ArrayList();
        Map namespaceMap = definition.getNamespaces();
        addImportsFromDefinition(definition, namespaceMap, schemaList);
        Collection errors = new ArrayList();
        XmlOptions xmlOptions = new XmlOptions();
        xmlOptions.setErrorListener(errors);
        XmlObject[] schemas = (XmlObject[]) schemaList.toArray(new XmlObject[schemaList.size()]);
        try {
            SchemaTypeSystem schemaTypeSystem = XmlBeans.compileXsd(schemas, basicTypeSystem, xmlOptions);
            if (errors.size() > 0) {
                throw new DeploymentException("Could not compile schema type system: errors: " + errors);
            }
            return schemaTypeSystem;
        } catch (XmlException e) {
            throw new DeploymentException("Could not compile schema type system", e);
        }
    }

    private static void addImportsFromDefinition(Definition definition, Map namespaceMap, List schemaList) throws DeploymentException {
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
                        addImportsFromDefinition(definition1, namespaceMap, schemaList);
                    } else {
                        System.out.println("Missing definition in import for namespace " + namespaceURI);
                    }
                }
            }
        }
    }

    private static void addSchemaElement(Element element, Map namespaceMap, List schemaList) throws DeploymentException {
        try {
            XmlObject xmlObject = SchemaConversionUtils.parse(element);
            XmlCursor cursor = xmlObject.newCursor();
            try {
                cursor.toFirstContentToken();
                for (Iterator namespaces = namespaceMap.entrySet().iterator(); namespaces.hasNext();) {
                    Map.Entry entry = (Map.Entry) namespaces.next();
                    cursor.insertNamespace((String) entry.getKey(), (String) entry.getValue());
                }
            } finally {
                cursor.dispose();
            }
            schemaList.add(xmlObject);
        } catch (XmlException e) {
            throw new DeploymentException("Could not parse schema element", e);
        }
    }

    /**
     * builds a map of SchemaTypeKey containing jaxrpc-style fake QName and context info to xmlbeans SchemaType object.
     *
     * @param schemaTypeSystem
     * @return
     */
    public static Map buildSchemaTypeKeyToSchemaTypeMap(SchemaTypeSystem schemaTypeSystem) {
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

    private static void addElement(SchemaField element, SchemaTypeKey key, Map qnameMap) {
        //TODO is this null if element is a ref?
        QName elementName = element.getName();
        String elementNamespace = elementName.getNamespaceURI();
        if (elementNamespace == null || elementNamespace.equals("")) {
            elementNamespace = key.getqName().getNamespaceURI();
        }
        String elementQNameLocalName;
        SchemaTypeKey elementKey = null;
        if (key == null) {
            //top level. rule 2.a,
            elementQNameLocalName = elementName.getLocalPart();
            elementKey = new SchemaTypeKey(elementName, true, false, false);
        } else {
            //not top level. rule 2.b, key will be for enclosing Type.
            QName enclosingTypeQName = key.getqName();
            String enclosingTypeLocalName = enclosingTypeQName.getLocalPart();
            elementQNameLocalName = enclosingTypeLocalName + ">" + elementName.getLocalPart();
            QName subElementName = new QName(elementNamespace, elementQNameLocalName);
            elementKey = new SchemaTypeKey(subElementName, true, false, true);
        }
        SchemaType schemaType = element.getType();
        qnameMap.put(elementKey, schemaType);
//        new Exception("Adding: " + elementKey.getqName().getLocalPart()).printStackTrace();
        //check if it's an array. maxOccurs is null if unbounded
        //element should always be a SchemaParticle... this is a workaround for XMLBEANS-137
        if (element instanceof SchemaParticle) {
            addArrayForms((SchemaParticle) element, elementKey.getqName(), qnameMap, schemaType);
        } else {
            System.out.println("element is not a schemaParticle! " + element);
        }
        //now, name for type.  Rule 1.b, type inside an element
        String typeQNameLocalPart = ">" + elementQNameLocalName;
        QName typeQName = new QName(elementNamespace, typeQNameLocalPart);
        boolean isAnonymous = true;
        addSchemaType(typeQName, schemaType, isAnonymous, qnameMap);
    }

    private static void addSchemaType(QName typeQName, SchemaType schemaType, boolean anonymous, Map qnameMap) {
        SchemaTypeKey typeKey = new SchemaTypeKey(typeQName, false, schemaType.isSimpleType(), anonymous);
        qnameMap.put(typeKey, schemaType);
//        new Exception("Adding: " + typeKey.getqName().getLocalPart()).printStackTrace();
        //TODO xmlbeans recommends using summary info from getElementProperties and getAttributeProperties instead of traversing the content model by hand.
        SchemaParticle schemaParticle = schemaType.getContentModel();
        if (schemaParticle != null) {
            addSchemaParticle(schemaParticle, typeKey, qnameMap);
        }
    }


    private static void addSchemaParticle(SchemaParticle schemaParticle, SchemaTypeKey key, Map qnameMap) {
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
            SchemaParticle[] children = schemaParticle.getParticleChildren();
            for (int i = 0; i < children.length; i++) {
                SchemaParticle child = children[i];
                addSchemaParticle(child, key, qnameMap);
            }
        }
    }

    private static void addArrayForms(SchemaParticle schemaParticle, QName keyName, Map qnameMap, SchemaType elementType) {
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
            SchemaTypeKey arrayKey = new SchemaTypeKey(arrayName, false, false, true);
            //TODO not clear we want the schemaType as the value
            qnameMap.put(arrayKey, elementType);
//            new Exception("Adding: " + arrayKey.getqName().getLocalPart()).printStackTrace();
            if (minOccurs == 1) {
                arrayQNameLocalName = keyName.getLocalPart() + "[," + maxOccurs + "]";
                arrayName = new QName(elementNamespace, arrayQNameLocalName);
                arrayKey = new SchemaTypeKey(arrayName, false, false, true);
                //TODO not clear we want the schemaType as the value
                qnameMap.put(arrayKey, elementType);
            }
        }
    }

    /**
     * Find all the complex types in the previously constructed schema analysis.
     * Put them in a map from complex type QName to schema fragment.
     *
     * @param schemaTypeKeyToSchemaTypeMap
     * @return
     */
    public static Map getComplexTypesInWsdl(Map schemaTypeKeyToSchemaTypeMap) {
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

    public static Map getElementToTypeMap(Map schemaTypeKeyToSchemaTypeMap) {
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

    public static Map getExceptionMap(JavaWsdlMappingType mapping) {
        Map exceptionMap = new HashMap();
        if (mapping != null) {
            ExceptionMappingType[] exceptionMappings = mapping.getExceptionMappingArray();
            for (int i = 0; i < exceptionMappings.length; i++) {
                ExceptionMappingType exceptionMapping = exceptionMappings[i];
                QName exceptionMessageQName = exceptionMapping.getWsdlMessage().getQNameValue();
                exceptionMap.put(exceptionMessageQName, exceptionMapping);
            }
        }
        return exceptionMap;
    }

    public static String getPackageFromNamespace(String namespace, JavaWsdlMappingType mapping) throws DeploymentException {
        PackageMappingType[] packageMappings = mapping.getPackageMappingArray();
        for (int i = 0; i < packageMappings.length; i++) {
            PackageMappingType packageMapping = packageMappings[i];
            if (namespace.equals(packageMapping.getNamespaceURI().getStringValue().trim())) {
                return packageMapping.getPackageType().getStringValue().trim();
            }
        }
        throw new DeploymentException("Namespace " + namespace + " was not mapped in jaxrpc mapping file");
    }

    private static final Map rpcHolderClasses = new HashMap();

    static {
        rpcHolderClasses.put(BigDecimal.class, BigDecimalHolder.class);
        rpcHolderClasses.put(BigInteger.class, BigIntegerHolder.class);
        rpcHolderClasses.put(boolean.class, BooleanHolder.class);
        rpcHolderClasses.put(Boolean.class, BooleanWrapperHolder.class);
        rpcHolderClasses.put(byte[].class, ByteArrayHolder.class);
        rpcHolderClasses.put(byte.class, ByteHolder.class);
        rpcHolderClasses.put(Byte.class, ByteWrapperHolder.class);
        rpcHolderClasses.put(Calendar.class, CalendarHolder.class);
        rpcHolderClasses.put(double.class, DoubleHolder.class);
        rpcHolderClasses.put(Double.class, DoubleWrapperHolder.class);
        rpcHolderClasses.put(float.class, FloatHolder.class);
        rpcHolderClasses.put(Float.class, FloatWrapperHolder.class);
        rpcHolderClasses.put(int.class, IntHolder.class);
        rpcHolderClasses.put(Integer.class, IntegerWrapperHolder.class);
        rpcHolderClasses.put(long.class, LongHolder.class);
        rpcHolderClasses.put(Long.class, LongWrapperHolder.class);
        rpcHolderClasses.put(Object.class, ObjectHolder.class);
        rpcHolderClasses.put(QName.class, QNameHolder.class);
        rpcHolderClasses.put(short.class, ShortHolder.class);
        rpcHolderClasses.put(Short.class, ShortWrapperHolder.class);
        rpcHolderClasses.put(String.class, StringHolder.class);
    }

    public static Class getHolderType(String paramJavaTypeName, boolean isInOnly, QName typeQName, boolean isComplexType, JavaWsdlMappingType mapping, ClassLoader classLoader) throws DeploymentException {
        Class paramJavaType = null;
        if (isInOnly) {
            //IN parameters just use their own type
            try {
                paramJavaType = ClassLoading.loadClass(paramJavaTypeName, classLoader);
            } catch (ClassNotFoundException e) {
                throw new DeploymentException("could not load parameter type", e);
            }
            return paramJavaType;
        } else {
            //INOUT and OUT parameters use holders.  See jaxrpc spec 4.3.5
            String holderName;
            if (isComplexType) {
                //complex types get mapped:
                //package is determined from the namespace to package map + ".holders"
                //class name is the complex type QNMAne local part + "Holder", with the initial character uppercased.
                String namespace = typeQName.getNamespaceURI();
                String packageName = WSDescriptorParser.getPackageFromNamespace(namespace, mapping);
                StringBuffer buf = new StringBuffer(packageName.length() + typeQName.getLocalPart().length() + 14);
                buf.append(packageName).append(".holders.").append(typeQName.getLocalPart()).append("Holder");
                buf.setCharAt(packageName.length() + 9, Character.toUpperCase(typeQName.getLocalPart().charAt(0)));
                holderName = buf.toString();
            } else {
                //see if it is in the primitive type and simple type mapping
                try {
                    paramJavaType = ClassLoading.loadClass(paramJavaTypeName, classLoader);
                } catch (ClassNotFoundException e) {
                    throw new DeploymentException("could not load parameter type", e);
                }
                Class holder = (Class) rpcHolderClasses.get(paramJavaType);
                if (holder != null) {
                    try {
                        //TODO use class names in map or make sure we are in the correct classloader to start with.
                        holder = ClassLoading.loadClass(holder.getName(), classLoader);
                    } catch (ClassNotFoundException e) {
                        throw new DeploymentException("could not load holder type in correct classloader", e);
                    }
                    return holder;
                }
                //Otherwise, the holder must be in:
                //package same as type's package + ".holders"
                //class name same as type name + "Holder"
                String paramTypeName = paramJavaType.getName();
                StringBuffer buf = new StringBuffer(paramTypeName.length() + 14);
                int dot = paramTypeName.lastIndexOf(".");
                //foo.Bar >>> foo.holders.BarHolder
                buf.append(paramTypeName.substring(0, dot)).append(".holders").append(paramTypeName.substring(dot)).append("Holder");
                holderName = buf.toString();
            }
            try {
                Class holder = ClassLoading.loadClass(holderName, classLoader);
                return holder;
            } catch (ClassNotFoundException e) {
                throw new DeploymentException("Could not load holder class", e);
            }
        }
    }

    public static ServiceEndpointMethodMappingType getMethodMappingForOperation(String operationName, ServiceEndpointMethodMappingType[] methodMappings) throws DeploymentException {
        for (int i = 0; i < methodMappings.length; i++) {
            ServiceEndpointMethodMappingType methodMapping = methodMappings[i];
            if (operationName.equals(methodMapping.getWsdlOperation().getStringValue())) {
                return methodMapping;
            }
        }
        throw new DeploymentException("No method found for operation named " + operationName);
    }

    public static ServiceEndpointInterfaceMappingType getServiceEndpointInterfaceMapping(ServiceEndpointInterfaceMappingType[] endpointMappings, QName portTypeQName) throws DeploymentException {
        for (int i = 0; i < endpointMappings.length; i++) {
            ServiceEndpointInterfaceMappingType endpointMapping = endpointMappings[i];
            QName testPortQName = endpointMapping.getWsdlPortType().getQNameValue();
            if (portTypeQName.equals(testPortQName)) {
                return endpointMapping;
            }
        }
        throw new DeploymentException("Could not find service endpoint interface for port named " + portTypeQName);
    }

    public static javax.wsdl.Service getService(QName serviceQName, Definition definition) throws DeploymentException {
        javax.wsdl.Service service;
        if (serviceQName != null) {
            service = definition.getService(serviceQName);
        } else {
            Map services = definition.getServices();
            if (services.size() != 1) {
                throw new DeploymentException("no serviceQName supplied, and there are " + services.size() + " services");
            }
            service = (javax.wsdl.Service) services.values().iterator().next();
        }
        if (service == null) {
            throw new DeploymentException("No service wsdl for supplied service qname " + serviceQName);
        }
        return service;
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

    public static void updatePortLocations(Service service, Map portLocations) throws DeploymentException {
        for (Iterator iterator = portLocations.entrySet().iterator(); iterator.hasNext();) {
            Map.Entry entry = (Map.Entry) iterator.next();
            String portName = (String) entry.getKey();
            String location = (String) entry.getValue();
            Port port = service.getPort(portName);
            if (port == null) {
                throw new DeploymentException("No port named " + portName + " found in service " + service.getQName());
            }
            SOAPAddress soapAddress = (SOAPAddress) WSDescriptorParser.getExtensibilityElement(SOAPAddress.class, port.getExtensibilityElements());
            soapAddress.setLocationURI(location);
        }
    }

    public static Method getMethodForOperation(Class serviceEndpointInterface, Operation operation) throws DeploymentException {
        Method[] methods = serviceEndpointInterface.getMethods();
        String opName = operation.getName();
        Method found = null;
        for (int i = 0; i < methods.length; i++) {
            Method method = methods[i];
            if (method.getName().equals(opName)) {
                if (found != null) {
                    throw new DeploymentException("Overloaded methods are not allowed in lightweight mappings");
                }
                found = method;
            }
        }
        if (found == null) {
            throw new DeploymentException("No method found for operation named " + opName);
        }
        return found;
    }

    static class JarWSDLLocator implements WSDLLocator {

        private final JarFile moduleFile;
        private final URI wsdlURI;
        private URI latestImportURI;

        public JarWSDLLocator(JarFile moduleFile, URI wsdlURI) {
            this.moduleFile = moduleFile;
            this.wsdlURI = wsdlURI;
        }

        public InputSource getBaseInputSource() {
            InputStream wsdlInputStream = null;
            try {
                wsdlInputStream = moduleFile.getInputStream(moduleFile.getEntry(wsdlURI.toString()));
            } catch (IOException e) {
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
            InputStream importInputStream = null;
            try {
                importInputStream = moduleFile.getInputStream(moduleFile.getEntry(latestImportURI.toString()));
            } catch (IOException e) {
                throw new RuntimeException("Could not open stream to import file", e);
            }
            return new InputSource(importInputStream);
        }

        public String getLatestImportURI() {
            return latestImportURI.toString();
        }
    }
}
