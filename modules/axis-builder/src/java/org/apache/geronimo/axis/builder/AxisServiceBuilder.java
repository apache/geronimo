/**
 *
 * Copyright 2004 The Apache Software Foundation
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

import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.jar.JarFile;
import javax.wsdl.Binding;
import javax.wsdl.BindingInput;
import javax.wsdl.BindingOperation;
import javax.wsdl.Definition;
import javax.wsdl.Import;
import javax.wsdl.Port;
import javax.wsdl.Types;
import javax.wsdl.extensions.UnknownExtensibilityElement;
import javax.wsdl.extensions.schema.Schema;
import javax.wsdl.extensions.soap.SOAPAddress;
import javax.wsdl.extensions.soap.SOAPBinding;
import javax.wsdl.extensions.soap.SOAPBody;
import javax.xml.namespace.QName;

import org.apache.axis.constants.Style;
import org.apache.axis.constants.Use;
import org.apache.axis.description.JavaServiceDesc;
import org.apache.axis.description.OperationDesc;
import org.apache.axis.encoding.TypeMapping;
import org.apache.axis.encoding.TypeMappingRegistryImpl;
import org.apache.geronimo.axis.client.TypeInfo;
import org.apache.geronimo.axis.server.ReadOnlyServiceDesc;
import org.apache.geronimo.axis.server.ServiceInfo;
import org.apache.geronimo.common.DeploymentException;
import org.apache.geronimo.deployment.util.DeploymentUtil;
import org.apache.geronimo.xbeans.j2ee.JavaXmlTypeMappingType;
import org.apache.geronimo.xbeans.j2ee.ServiceEndpointMethodMappingType;
import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlObject;
import org.w3.x2001.xmlSchema.ImportDocument;
import org.w3.x2001.xmlSchema.IncludeDocument;
import org.w3.x2001.xmlSchema.SchemaDocument;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * @version $Rev$ $Date$
 */
public class AxisServiceBuilder {
    public static final String XSD_NS = "http://www.w3.org/2001/XMLSchema";
    public static final QName SCHEMA_QNAME = new QName(XSD_NS, "schema");


    private static void validateLightweightMapping(Definition definition) throws DeploymentException {
        // TODO Plum in the validator
    }


    public static ServiceInfo createServiceInfo(JarFile jarFile, String ejbName, ClassLoader classLoader) throws DeploymentException {
        Map portComponentsMap = null;
        try {
            URL webservicesURL = DeploymentUtil.createJarURL(jarFile, "META-INF/webservices.xml");
            portComponentsMap = WSDescriptorParser.parseWebServiceDescriptor(webservicesURL, jarFile, true);
        } catch (MalformedURLException e1) {
            throw new DeploymentException("Invalid URL to webservices.xml", e1);
        }

        // Grab the portInfo for this ejb
        PortInfo portInfo = (PortInfo) portComponentsMap.get(ejbName);
        return createServiceInfo(portInfo, classLoader);
    }

    private static JavaServiceDesc createEJBServiceDesc(JarFile jarFile, String ejbName, ClassLoader classLoader) throws DeploymentException {
        Map portComponentsMap = null;
        try {
            URL webservicesURL = DeploymentUtil.createJarURL(jarFile, "META-INF/webservices.xml");
            portComponentsMap = WSDescriptorParser.parseWebServiceDescriptor(webservicesURL, jarFile, true);
        } catch (MalformedURLException e1) {
            throw new DeploymentException("Invalid URL to webservices.xml", e1);
        }

        // Grab the portInfo for this ejb
        PortInfo portInfo = (PortInfo) portComponentsMap.get(ejbName);
        return createServiceDesc(portInfo, classLoader);
    }

    public static ServiceInfo createServiceInfo(PortInfo portInfo, ClassLoader classLoader) throws DeploymentException {
        JavaServiceDesc serviceDesc = createServiceDesc(portInfo, classLoader);
        List handlerInfos = WSDescriptorParser.createHandlerInfoList(portInfo.getHandlers(), classLoader);
        SchemaInfoBuilder schemaInfoBuilder = portInfo.getSchemaInfoBuilder();
        Map rawWsdlMap = schemaInfoBuilder.getWsdlMap();
        Map wsdlMap = rewriteWsdlMap(portInfo, rawWsdlMap);
        return new ServiceInfo(serviceDesc, handlerInfos, wsdlMap);
    }

    public static JavaServiceDesc createServiceDesc(PortInfo portInfo, ClassLoader classLoader) throws DeploymentException {

        Port port = portInfo.getPort();
//        System.out.println("port = " + port);

        Class serviceEndpointInterface = null;
        try {
            serviceEndpointInterface = classLoader.loadClass(portInfo.getServiceEndpointInterfaceName());
        } catch (ClassNotFoundException e) {
            throw (DeploymentException) new DeploymentException("Unable to load the service-endpoint interface for port-component " + portInfo.getPortComponentName()).initCause(e);
        }

        Map exceptionMap = WSDescriptorParser.getExceptionMap(portInfo.getJavaWsdlMapping());
        SchemaInfoBuilder schemaInfoBuilder = portInfo.getSchemaInfoBuilder();
        Map schemaTypeKeyToSchemaTypeMap = schemaInfoBuilder.getSchemaTypeKeyToSchemaTypeMap();

        JavaServiceDesc serviceDesc = new JavaServiceDesc();

        URL location = getAddressLocation(port);
        serviceDesc.setEndpointURL(location.toExternalForm());
        serviceDesc.setWSDLFile(portInfo.getWsdlLocation());
        Binding binding = port.getBinding();

        serviceDesc.setStyle(getStyle(binding));


        BindingInput bindingInput = ((BindingOperation) binding.getBindingOperations().get(0)).getBindingInput();
        SOAPBody soapBody = (SOAPBody) SchemaInfoBuilder.getExtensibilityElement(SOAPBody.class, bindingInput.getExtensibilityElements());

        if (soapBody.getUse() != null) {
            Use use = Use.getUse(soapBody.getUse());
            serviceDesc.setUse(use);
        } else {
            serviceDesc.setUse(Use.ENCODED);
        }
        boolean hasEncoded = serviceDesc.getUse() == Use.ENCODED;

        boolean isLightweight = portInfo.getServiceEndpointInterfaceMapping() == null;

        if (isLightweight) {
            validateLightweightMapping(portInfo.getDefinition());
        }

        Set wrapperElementQNames = buildOperations(binding, serviceEndpointInterface, isLightweight, portInfo, exceptionMap, classLoader, serviceDesc);

        TypeMappingRegistryImpl tmr = new TypeMappingRegistryImpl();
        tmr.doRegisterFromVersion("1.3");

        TypeMapping typeMapping = tmr.getOrMakeTypeMapping(serviceDesc.getUse().getEncoding());

        serviceDesc.setTypeMappingRegistry(tmr);
        serviceDesc.setTypeMapping(typeMapping);

        List typeInfo;
        if (isLightweight) {
            LightweightTypeInfoBuilder builder = new LightweightTypeInfoBuilder(classLoader, schemaTypeKeyToSchemaTypeMap, wrapperElementQNames);
            typeInfo = builder.buildTypeInfo(portInfo.getJavaWsdlMapping());
        } else {
            HeavyweightTypeInfoBuilder builder = new HeavyweightTypeInfoBuilder(classLoader, schemaTypeKeyToSchemaTypeMap, wrapperElementQNames, hasEncoded);
            typeInfo = builder.buildTypeInfo(portInfo.getJavaWsdlMapping());
        }
        TypeInfo.register(typeInfo, typeMapping);

        serviceDesc.getOperations();
        return new ReadOnlyServiceDesc(serviceDesc, typeInfo);
    }

    private static Set buildOperations(Binding binding, Class serviceEndpointInterface, boolean lightweight, PortInfo portInfo, Map exceptionMap, ClassLoader classLoader, JavaServiceDesc serviceDesc) throws DeploymentException {
        Set wrappedElementQNames = new HashSet();

        SOAPBinding soapBinding = (SOAPBinding) SchemaInfoBuilder.getExtensibilityElement(SOAPBinding.class, binding.getExtensibilityElements());
        String portStyleString = soapBinding.getStyle();
        Style portStyle = Style.getStyle(portStyleString);

        List bindingOperations = binding.getBindingOperations();
        for (int i = 0; i < bindingOperations.size(); i++) {
            BindingOperation bindingOperation = (BindingOperation) bindingOperations.get(i);

            OperationDescBuilder operationDescBuilder;
            if (lightweight) {
                Method method = WSDescriptorParser.getMethodForOperation(serviceEndpointInterface, bindingOperation.getOperation());
                operationDescBuilder = new LightweightOperationDescBuilder(bindingOperation, method);
            } else {
                String operationName = bindingOperation.getOperation().getName();
                ServiceEndpointMethodMappingType[] methodMappings = portInfo.getServiceEndpointInterfaceMapping().getServiceEndpointMethodMappingArray();
                ServiceEndpointMethodMappingType methodMapping = WSDescriptorParser.getMethodMappingForOperation(operationName, methodMappings);
                JavaXmlTypeMappingType[] javaXmlTypeMappingTypes = portInfo.getJavaWsdlMapping().getJavaXmlTypeMappingArray();
                operationDescBuilder = new HeavyweightOperationDescBuilder(bindingOperation, portInfo.getJavaWsdlMapping(), methodMapping, portStyle, exceptionMap, portInfo.getSchemaInfoBuilder(), javaXmlTypeMappingTypes, classLoader, serviceEndpointInterface);
                Set wrappedElementQNamesForOper = ((HeavyweightOperationDescBuilder) operationDescBuilder).getWrapperElementQNames();
                wrappedElementQNames.addAll(wrappedElementQNamesForOper);
            }

            // TODO fix Axis: a JavaServiceDesc can be document or rpc and
            // still define a WSDL operation using a wrapper style mapping.
            // when fixed, the three following lines should be removed.
            OperationDesc operationDesc =  operationDescBuilder.buildOperationDesc();
            if (Style.WRAPPED == operationDesc.getStyle()) {
                serviceDesc.setStyle(Style.WRAPPED);
            }
            serviceDesc.addOperationDesc(operationDesc);
        }

        return wrappedElementQNames;
    }


    private static Style getStyle(Binding binding) throws DeploymentException {
        SOAPBinding soapBinding = (SOAPBinding) SchemaInfoBuilder.getExtensibilityElement(SOAPBinding.class, binding.getExtensibilityElements());
//            String transportURI = soapBinding.getTransportURI();
        String portStyleString = soapBinding.getStyle();
        Style portStyle = Style.getStyle(portStyleString);
        return portStyle;
    }

    private static URL getAddressLocation(Port port) throws DeploymentException {
        SOAPAddress soapAddress = (SOAPAddress) SchemaInfoBuilder.getExtensibilityElement(SOAPAddress.class, port.getExtensibilityElements());
        String locationURIString = soapAddress.getLocationURI();
        URL location = null;
        try {
            location = new URL(locationURIString);
        } catch (MalformedURLException e) {
            throw new DeploymentException("Could not construct web service location URL from " + locationURIString);
        }
        return location;
    }

    private static Map rewriteWsdlMap(PortInfo portInfo, Map rawWsdlMap) throws DeploymentException {
        URI contextURI = portInfo.getContextURI();
        Map wsdlMap = new HashMap();
        for (Iterator iterator = rawWsdlMap.entrySet().iterator(); iterator.hasNext();) {
            Map.Entry entry = (Map.Entry) iterator.next();
            URI key = (URI) entry.getKey();
            Object value = entry.getValue();
            if (value instanceof SchemaDocument) {
                SchemaDocument schemaDocument = (SchemaDocument) ((SchemaDocument) value).copy();
                SchemaDocument.Schema schema = schemaDocument.getSchema();
                rewriteSchema(schema, contextURI, key);
                String schemaString = schemaDocument.toString();
                wsdlMap.put(key.toString(), schemaString);
            } else if (value instanceof Definition) {
                Definition definition = (Definition) value;
                Map imports = definition.getImports();
                for (Iterator iterator2 = imports.values().iterator(); iterator2.hasNext();) {
                    List importList = (List) iterator2.next();
                    for (Iterator iterator3 = importList.iterator(); iterator3.hasNext();) {
                        Import anImport = (Import) iterator3.next();
                        String importLocation = anImport.getLocationURI();
                        if (!importLocation.startsWith("http://")) {
                            URI updated = buildQueryURI(contextURI, key, importLocation);
                            anImport.setLocationURI(updated.toString());
                        }
                    }
                }
                Types types = definition.getTypes();
                Map namespaceMap = definition.getNamespaces();
                if (null != types) {
                    List schemaList = types.getExtensibilityElements();
                    for (Iterator iterator1 = schemaList.iterator(); iterator1.hasNext();) {
                        Object o = iterator1.next();
                        if (o instanceof Schema) {
                            Schema schemaType = (Schema) o;
                            Element e = schemaType.getElement();
                            try {
                                SchemaDocument.Schema schema = (SchemaDocument.Schema) XmlObject.Factory.parse(e);
                                rewriteSchema(schema, contextURI, key);
                                Element e2 = (Element) schema.newDomNode();
                                schemaType.setElement(e2);
                            } catch (XmlException e1) {
                                throw new DeploymentException("Could not parse included schema", e1);
                            }
                        } else if (o instanceof UnknownExtensibilityElement) {
                            UnknownExtensibilityElement u = (UnknownExtensibilityElement) o;
                            QName elementType = u.getElementType();
                            if (SCHEMA_QNAME.equals(elementType)) {
                                Element e = u.getElement();
                                try {
                                    SchemaDocument schemaDocument = (SchemaDocument) SchemaInfoBuilder.parseWithNamespaces(e, namespaceMap);
                                    SchemaDocument.Schema schema = schemaDocument.getSchema();
                                    rewriteSchema(schema, contextURI, key);
                                    Node node = schema.newDomNode();
                                    Element e2 = (Element) node.getFirstChild();
                                    u.setElement(e2);
                                } catch (XmlException e1) {
                                    throw new DeploymentException("Could not parse included schema", e1);
                                }
                            }
                        }
                    }
                }
                wsdlMap.put(key.toString(), definition);
            } else {
                throw new DeploymentException("Unexpected element in wsdlMap at location: " + key + ", value: " + value);
            }
        }
        return wsdlMap;
    }

    private static void rewriteSchema(SchemaDocument.Schema schema, URI contextURI, URI key) throws DeploymentException {
        ImportDocument.Import[] imports = schema.getImportArray();
        for (int i = 0; i < imports.length; i++) {
            ImportDocument.Import anImport = imports[i];
            if (anImport.isSetSchemaLocation()) {
                String schemaLocation = anImport.getSchemaLocation();
                URI absoluteSchemLocation = buildQueryURI(contextURI, key, schemaLocation);
                anImport.setSchemaLocation(absoluteSchemLocation.toString());
            }
        }
        IncludeDocument.Include[] includes = schema.getIncludeArray();
        for (int i = 0; i < includes.length; i++) {
            IncludeDocument.Include include = includes[i];
            String schemaLocation = include.getSchemaLocation();
            URI absoluteSchemLocation = buildQueryURI(contextURI, key, schemaLocation);
            include.setSchemaLocation(absoluteSchemLocation.toString());
        }
    }

    private static URI buildQueryURI(URI contextURI, URI key, String importLocation) throws DeploymentException {
        try {
            URI importLocationURI = new URI(importLocation);
            if (importLocationURI.isAbsolute() || importLocationURI.getPath().startsWith("/")) {
                return importLocationURI;
            }
            return new URI(null,
                    null,
                    contextURI.getPath(),
                    "wsdl=" + key.resolve(importLocationURI),
                    null);
        } catch (URISyntaxException e) {
            throw new DeploymentException("Could not construct wsdl location URI", e);
        }
    }


}
