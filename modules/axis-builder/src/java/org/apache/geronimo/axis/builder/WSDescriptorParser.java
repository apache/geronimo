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
import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.jar.JarFile;
import javax.wsdl.Definition;
import javax.wsdl.Types;
import javax.wsdl.WSDLException;
import javax.wsdl.extensions.ExtensibilityElement;
import javax.wsdl.extensions.UnknownExtensibilityElement;
import javax.wsdl.extensions.schema.Schema;
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
import org.apache.xmlbeans.XmlCursor;
import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlObject;
import org.w3.x2001.xmlSchema.ComplexType;
import org.w3.x2001.xmlSchema.SchemaDocument;
import org.w3c.dom.Element;
import org.xml.sax.InputSource;

/**
 * @version $Rev:  $ $Date:  $
 */
public class WSDescriptorParser {

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
            JavaWsdlMappingType javaWsdlMapping = readJaxrpcMapping(moduleFile, jaxrpcMappingURI);
            PortComponentType[] portComponents = webserviceDescription.getPortComponentArray();
            for (int j = 0; j < portComponents.length; j++) {
                PortComponentType portComponent = portComponents[j];
                String portComponentName = portComponent.getPortComponentName().getStringValue().trim();
                QName portQName = portComponent.getWsdlPort().getQNameValue();
                String seiInterfaceName = portComponent.getServiceEndpointInterface().getStringValue().trim();
                ServiceImplBeanType serviceImplBeanType = portComponent.getServiceImplBean();
                if (isEJB == serviceImplBeanType.isSetServletLink()) {
                    throw new DeploymentException("Wrong kind of web service described in web service descriptor: expected " + (isEJB? "EJB": "POJO(Servlet)"));
                }
                String linkName;
                if (serviceImplBeanType.isSetServletLink()) {
                    linkName = serviceImplBeanType.getServletLink().getStringValue().trim();
                } else {
                    linkName = serviceImplBeanType.getEjbLink().getStringValue().trim();
                }
                PortComponentHandlerType[] handlers = portComponent.getHandlerArray();
                PortInfo portInfo = new PortInfo(portComponentName, portQName, definition, javaWsdlMapping, seiInterfaceName, handlers);
                if (portMap.put(linkName, portInfo) != null) {
                    throw new DeploymentException("Ambiguous description of port associated with j2ee component " + linkName);
                }
            }
        }
        return portMap;
    }

    public static JavaWsdlMappingType readJaxrpcMapping(JarFile moduleFile, URI jaxrpcMappingURI) throws DeploymentException {
        JavaWsdlMappingType mapping;
        InputStream jaxrpcInputStream = null;
        try {
            jaxrpcInputStream = moduleFile.getInputStream(moduleFile.getEntry(jaxrpcMappingURI.toString()));
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

    /**
     * Find all the top level complex types in the schemas in the definitions' types.
     * Put them in a map from complex type QName to schema fragment.
     * TODO it is not clear what happens with included schemas.
     *
     * @param definition
     * @return
     * @throws DeploymentException
     */
    public static Map getComplexTypesInWsdl(Definition definition) throws DeploymentException {
        Map complexTypeMap = new HashMap();
        Types types = definition.getTypes();
        Map namespaceMap = definition.getNamespaces();
        if (types != null) {
            List schemas = types.getExtensibilityElements();
            for (Iterator iterator = schemas.iterator(); iterator.hasNext();) {
                Object o = iterator.next();
                if (o instanceof Schema) {
                    Schema unknownExtensibilityElement = (Schema) o;
                    QName elementType = unknownExtensibilityElement.getElementType();
                    if (new QName("http://www.w3.org/2001/XMLSchema", "schema").equals(elementType)) {
                        Element element = unknownExtensibilityElement.getElement();
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
                            SchemaDocument schemaDoc = (SchemaDocument) xmlObject.changeType(SchemaDocument.type);
                            SchemaConversionUtils.validateDD(schemaDoc);
                            SchemaDocument.Schema schema = schemaDoc.getSchema();
                            String targetNamespace = schema.getTargetNamespace();
                            ComplexType[] complexTypes = schema.getComplexTypeArray();
                            for (int j = 0; j < complexTypes.length; j++) {
                                ComplexType complexType = complexTypes[j];
                                String complexTypeName = complexType.getName();
                                QName complexTypeQName = new QName(targetNamespace, complexTypeName);
                                complexTypeMap.put(complexTypeQName, complexType);
                            }
                        } catch (XmlException e) {
                            throw new DeploymentException("Invalid schema in wsdl", e);
                        }
                    } else {
                        //problems??
                    }
                } else if (o instanceof UnknownExtensibilityElement) {
                    //This is apparently obsolete as of axis-wsdl4j-1.2-RC3.jar which includes the Schema extension above.
                    //I'm leaving this in in case this Schema class is not really part of a spec, even though its in javax.
                    UnknownExtensibilityElement unknownExtensibilityElement = (UnknownExtensibilityElement) o;
                    QName elementType = unknownExtensibilityElement.getElementType();
                    if (new QName("http://www.w3.org/2001/XMLSchema", "schema").equals(elementType)) {
                        Element element = unknownExtensibilityElement.getElement();
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
                            SchemaDocument schemaDoc = (SchemaDocument) xmlObject.changeType(SchemaDocument.type);
                            SchemaConversionUtils.validateDD(schemaDoc);
                            SchemaDocument.Schema schema = schemaDoc.getSchema();
                            String targetNamespace = schema.getTargetNamespace();
                            ComplexType[] complexTypes = schema.getComplexTypeArray();
                            for (int j = 0; j < complexTypes.length; j++) {
                                ComplexType complexType = complexTypes[j];
                                String complexTypeName = complexType.getName();
                                QName complexTypeQName = new QName(targetNamespace, complexTypeName);
                                complexTypeMap.put(complexTypeQName, complexType);
                            }
                        } catch (XmlException e) {
                            throw new DeploymentException("Invalid schema in wsdl", e);
                        }
                    } else {
                        //problems??
                    }
                }
            }
        }
        return complexTypeMap;
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

    public static ExtensibilityElement getExtensibilityElement(Class clazz, List extensibilityElements) throws DeploymentException {
        for (Iterator iterator = extensibilityElements.iterator(); iterator.hasNext();) {
            ExtensibilityElement extensibilityElement = (ExtensibilityElement) iterator.next();
            if (clazz.isAssignableFrom(extensibilityElement.getClass())) {
                return extensibilityElement;
            }
        }
        throw new DeploymentException("No element of class " + clazz.getName() + " found");
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
