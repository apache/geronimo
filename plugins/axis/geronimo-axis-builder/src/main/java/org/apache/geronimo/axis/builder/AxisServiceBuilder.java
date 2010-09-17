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
package org.apache.geronimo.axis.builder;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.lang.reflect.Method;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.wsdl.Binding;
import javax.wsdl.BindingInput;
import javax.wsdl.BindingOperation;
import javax.wsdl.Port;
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
import org.apache.geronimo.xbeans.wsdl.DefinitionsDocument;
import org.apache.geronimo.xbeans.wsdl.TDefinitions;
import org.apache.geronimo.xbeans.wsdl.TImport;
import org.apache.geronimo.xbeans.wsdl.TTypes;
import org.apache.geronimo.webservices.builder.PortInfo;
import org.apache.geronimo.webservices.builder.SchemaInfoBuilder;
import org.apache.geronimo.webservices.builder.WSDescriptorParser;
import org.apache.openejb.jee.JavaXmlTypeMapping;
import org.apache.openejb.jee.ServiceEndpointMethodMapping;
import org.apache.xmlbeans.XmlCursor;
import org.apache.xmlbeans.XmlObject;
import org.apache.xmlbeans.impl.xb.xsdschema.ImportDocument;
import org.apache.xmlbeans.impl.xb.xsdschema.IncludeDocument;
import org.apache.xmlbeans.impl.xb.xsdschema.SchemaDocument;
import org.osgi.framework.Bundle;

/**
 * @version $Rev$ $Date$
 */
public class AxisServiceBuilder {

    public static final String XSD_NS = "http://www.w3.org/2001/XMLSchema";
    public static final QName SCHEMA_QNAME = new QName(XSD_NS, "schema");


    public static ServiceInfo createServiceInfo(PortInfo portInfo, Bundle bundle) throws DeploymentException {
        JavaServiceDesc serviceDesc = createServiceDesc(portInfo, bundle);
        List handlerInfos = WSDescriptorParser.createHandlerInfoList(portInfo.getHandlers(), bundle);
        SchemaInfoBuilder schemaInfoBuilder = portInfo.getSchemaInfoBuilder();
        Map rawWsdlMap = schemaInfoBuilder.getWsdlMap();
        Map wsdlMap = rewriteWsdlMap(portInfo, rawWsdlMap);
        return new ServiceInfo(serviceDesc, handlerInfos, wsdlMap);
    }

    public static JavaServiceDesc createServiceDesc(PortInfo portInfo, Bundle bundle) throws DeploymentException {

        Port port = portInfo.getPort();

        Class serviceEndpointInterface = null;
        try {
            serviceEndpointInterface = bundle.loadClass(portInfo.getServiceEndpointInterfaceName());
        } catch (ClassNotFoundException e) {
            throw (DeploymentException) new DeploymentException("Unable to load the service-endpoint interface for port-component " + portInfo.getPortComponentName()).initCause(e);
        }

        Map exceptionMap = WSDescriptorParser.getExceptionMap(portInfo.getJavaWsdlMapping());
        SchemaInfoBuilder schemaInfoBuilder = portInfo.getSchemaInfoBuilder();
        Map schemaTypeKeyToSchemaTypeMap = schemaInfoBuilder.getSchemaTypeKeyToSchemaTypeMap();

        JavaServiceDesc serviceDesc = new JavaServiceDesc();
        String serviceName =  portInfo.getPortQName().toString();
        String location = getAddressLocation(port);
        serviceDesc.setName(serviceName);
        serviceDesc.setEndpointURL(location);
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

//        if (isLightweight) {
//            validateLightweightMapping(portInfo.getDefinition());
//        }

        Collection operations = new ArrayList();
        Set wrapperElementQNames = buildOperations(binding, serviceEndpointInterface, isLightweight, portInfo, exceptionMap, bundle, operations);
        for (Iterator iter = operations.iterator(); iter.hasNext();) {
            OperationDesc operation = (OperationDesc) iter.next();
            serviceDesc.addOperationDesc(operation);
        }

        TypeMappingRegistryImpl tmr = new TypeMappingRegistryImpl();
        tmr.doRegisterFromVersion("1.3");

        TypeMapping typeMapping = tmr.getOrMakeTypeMapping(serviceDesc.getUse().getEncoding());

        serviceDesc.setTypeMappingRegistry(tmr);
        serviceDesc.setTypeMapping(typeMapping);

        List typeInfo;
        if (isLightweight) {
            LightweightTypeInfoBuilder builder = new LightweightTypeInfoBuilder(bundle, schemaTypeKeyToSchemaTypeMap, wrapperElementQNames);
            typeInfo = builder.buildTypeInfo(portInfo.getJavaWsdlMapping());
        } else {
            HeavyweightTypeInfoBuilder builder = new HeavyweightTypeInfoBuilder(bundle, schemaTypeKeyToSchemaTypeMap, wrapperElementQNames, operations, hasEncoded);
            typeInfo = builder.buildTypeInfo(portInfo.getJavaWsdlMapping());
        }

        // We register type mappings and invoke serviceDesc.getOperations to trigger an introspection of the
        // operations. By doing these operations during deployment, no introspection is required during runtime.
        TypeInfo.register(typeInfo, typeMapping);
        serviceDesc.getOperations();

        return new ReadOnlyServiceDesc(serviceDesc, typeInfo);
    }

    private static Set buildOperations(Binding binding, Class serviceEndpointInterface, boolean lightweight, PortInfo portInfo, Map exceptionMap, Bundle bundle, Collection operations) throws DeploymentException {
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
                List<ServiceEndpointMethodMapping> methodMappings = portInfo.getServiceEndpointInterfaceMapping().getServiceEndpointMethodMapping();
                ServiceEndpointMethodMapping methodMapping = WSDescriptorParser.getMethodMappingForOperation(operationName, methodMappings);
                List<JavaXmlTypeMapping> javaXmlTypeMappingTypes = portInfo.getJavaWsdlMapping().getJavaXmlTypeMapping();
                operationDescBuilder = new HeavyweightOperationDescBuilder(bindingOperation, portInfo.getJavaWsdlMapping(), methodMapping, portStyle, exceptionMap, portInfo.getSchemaInfoBuilder(), javaXmlTypeMappingTypes, bundle, serviceEndpointInterface);
                Set wrappedElementQNamesForOper = ((HeavyweightOperationDescBuilder) operationDescBuilder).getWrapperElementQNames();
                wrappedElementQNames.addAll(wrappedElementQNamesForOper);
            }

            operations.add(operationDescBuilder.buildOperationDesc());
        }

        return wrappedElementQNames;
    }


    private static Style getStyle(Binding binding) throws DeploymentException {
        SOAPBinding soapBinding = (SOAPBinding) SchemaInfoBuilder.getExtensibilityElement(SOAPBinding.class, binding.getExtensibilityElements());
        String portStyleString = soapBinding.getStyle();
        Style portStyle = Style.getStyle(portStyleString);
        return portStyle;
    }

    private static String getAddressLocation(Port port) throws DeploymentException {
        SOAPAddress soapAddress = (SOAPAddress) SchemaInfoBuilder.getExtensibilityElement(SOAPAddress.class, port.getExtensibilityElements());
        String locationURIString = soapAddress.getLocationURI();
        return locationURIString;
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
                String schemaString = xmlObjectToString(schemaDocument);
                wsdlMap.put(key.toString(), schemaString);
            } else if (value instanceof DefinitionsDocument) {
                DefinitionsDocument doc = (DefinitionsDocument) ((DefinitionsDocument) value).copy();
                TDefinitions definitions = doc.getDefinitions();
                TImport[] imports = definitions.getImportArray();
                for (int i = 0; i < imports.length; i++) {
                    TImport anImport = imports[i];
                    String importLocation = anImport.getLocation().trim();
                    if (!importLocation.startsWith("http://")) {
                        URI updated = buildQueryURI(contextURI, key, importLocation);
                        anImport.setLocation(updated.toString());
                    }
                }
                TTypes[] types = definitions.getTypesArray();
                for (int i = 0; i < types.length; i++) {
                    TTypes type = types[i];
                    XmlCursor typeCursor = type.newCursor();
                    try {
                        if (typeCursor.toChild(SCHEMA_QNAME)) {
                            do {
                                SchemaDocument.Schema schema = (SchemaDocument.Schema) typeCursor.getObject();
                                rewriteSchema(schema, contextURI, key);
                            } while (typeCursor.toNextSibling(SCHEMA_QNAME));
                        }
                    } finally {
                        typeCursor.dispose();
                    }
                }
                String docString = xmlObjectToString(doc);
                wsdlMap.put(key.toString(), docString);
            } else {
                throw new DeploymentException("Unexpected element in wsdlMap at location: " + key + ", value: " + value);
            }
        }
        return wsdlMap;
    }

    static String xmlObjectToString(XmlObject xmlObject) throws DeploymentException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try {
            xmlObject.save(baos);
            baos.flush();
            String result = new String(baos.toByteArray());
            return result;
        } catch (IOException e) {
            throw new DeploymentException("Could not write xml object to string", e);
        }
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
            URI queryURI = new URI(null,
                    null,
                    contextURI.getPath(),
                    "wsdl=" + key.resolve(importLocationURI),
                    null);
            return queryURI;
        } catch (URISyntaxException e) {
            throw new DeploymentException("Could not construct wsdl location URI", e);
        }
    }


}
