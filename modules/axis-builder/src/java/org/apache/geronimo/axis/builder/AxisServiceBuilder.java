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

import org.apache.axis.constants.Style;
import org.apache.axis.constants.Use;
import org.apache.axis.description.JavaServiceDesc;
import org.apache.axis.description.TypeDesc;
import org.apache.axis.encoding.DeserializerFactory;
import org.apache.axis.encoding.SerializerFactory;
import org.apache.axis.encoding.TypeMapping;
import org.apache.axis.encoding.TypeMappingRegistryImpl;
import org.apache.axis.encoding.ser.*;
import org.apache.geronimo.axis.server.ReadOnlyServiceDesc;
import org.apache.geronimo.axis.server.ServiceInfo;
import org.apache.geronimo.common.DeploymentException;
import org.apache.geronimo.deployment.util.DeploymentUtil;
import org.apache.geronimo.kernel.ClassLoading;
import org.apache.geronimo.xbeans.j2ee.*;
import org.apache.xmlbeans.SchemaType;

import javax.wsdl.*;
import javax.wsdl.extensions.soap.SOAPAddress;
import javax.wsdl.extensions.soap.SOAPBinding;
import javax.wsdl.extensions.soap.SOAPBody;
import javax.xml.namespace.QName;
import javax.xml.rpc.handler.HandlerInfo;
import java.lang.String;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;
import java.util.jar.JarFile;

/**
 * @version $Rev$ $Date$
 */
public class AxisServiceBuilder {

    public static final String XSD_NS = "http://www.w3.org/2001/XMLSchema";


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
        JavaServiceDesc serviceDesc = createServiceDesc(portInfo, classLoader);
        List handlerInfos = createHandlerInfos(portInfo, classLoader);
        return new ServiceInfo(serviceDesc, handlerInfos);
    }

    public static JavaServiceDesc createEJBServiceDesc(JarFile jarFile, String ejbName, ClassLoader classLoader) throws DeploymentException {
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

    private static List createHandlerInfos(PortInfo portInfo, ClassLoader classLoader) throws DeploymentException {
        List list = new ArrayList();

        PortComponentHandlerType[] handlers = portInfo.getHandlers();

        for (int i = 0; i < handlers.length; i++) {
            PortComponentHandlerType handler = handlers[i];

            // Get handler class
            Class handlerClass = null;
            String className = handler.getHandlerClass().getStringValue().trim();
            try {
                handlerClass = classLoader.loadClass(className);
            } catch (ClassNotFoundException e) {
                throw new DeploymentException("Unable to load handler class: "+className, e);
            }

            // config data for the handler
            Map config = new HashMap();
            ParamValueType[] paramValues = handler.getInitParamArray();
            for (int j = 0; j < paramValues.length; j++) {
                ParamValueType paramValue = paramValues[j];
                String paramName = paramValue.getParamName().getStringValue().trim();
                String paramStringValue = paramValue.getParamValue().getStringValue().trim();
                config.put(paramName, paramStringValue);
            }

            // QName array of headers it processes
            XsdQNameType[] soapHeaderQNames = handler.getSoapHeaderArray();
            QName[] headers = new QName[soapHeaderQNames.length];
            for (int j = 0; j < soapHeaderQNames.length; j++) {
                XsdQNameType soapHeaderQName = soapHeaderQNames[j];
                headers[j] = soapHeaderQName.getQNameValue();
            }

            list.add(new HandlerInfo(handlerClass, config, headers));
        }
        return list;
    }

    public static ServiceInfo createServiceInfo(PortInfo portInfo, ClassLoader classLoader) throws DeploymentException {
        JavaServiceDesc serviceDesc = createServiceDesc(portInfo, classLoader);
        List handlerInfos = createHandlerInfos(portInfo, classLoader);
        return new ServiceInfo(serviceDesc, handlerInfos);
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
        SchemaInfoBuilder schemaInfoBuilder = new SchemaInfoBuilder(null, portInfo.getDefinition());
        Map schemaTypeKeyToSchemaTypeMap = schemaInfoBuilder.getSchemaTypeKeyToSchemaTypeMap();
        Map complexTypeMap = schemaInfoBuilder.getComplexTypesInWsdl();
        Map elementMap = schemaInfoBuilder.getElementToTypeMap();

        JavaServiceDesc serviceDesc = new JavaServiceDesc();

        URL location = getAddressLocation(port);
        serviceDesc.setEndpointURL(location.toExternalForm());
        serviceDesc.setWSDLFile(portInfo.getWsdlURL().toExternalForm());
        Binding binding = port.getBinding();

        serviceDesc.setStyle(getStyle(binding));


        BindingInput bindingInput = ((BindingOperation) binding.getBindingOperations().get(0)).getBindingInput();
        SOAPBody soapBody = (SOAPBody) WSDescriptorParser.getExtensibilityElement(SOAPBody.class, bindingInput.getExtensibilityElements());

        if (soapBody.getUse() != null) {
            Use use = Use.getUse(soapBody.getUse());
            serviceDesc.setUse(use);
        } else {
            serviceDesc.setUse(Use.ENCODED);
        }


        boolean isLightweight = portInfo.getServiceEndpointInterfaceMapping() == null;

        if (isLightweight) {
            validateLightweightMapping(portInfo.getDefinition());
        }

        Set wrapperElementQNames = buildOperations(binding, serviceEndpointInterface, isLightweight, portInfo, exceptionMap, complexTypeMap, elementMap, classLoader, serviceDesc);

        TypeMappingRegistryImpl tmr = new TypeMappingRegistryImpl();
        tmr.doRegisterFromVersion("1.3");

        TypeMapping typeMapping = tmr.getOrMakeTypeMapping(serviceDesc.getUse().getEncoding());

        serviceDesc.setTypeMappingRegistry(tmr);
        serviceDesc.setTypeMapping(typeMapping);

        if (isLightweight) {
            buildLightweightTypes(schemaTypeKeyToSchemaTypeMap, portInfo, classLoader, typeMapping);
        } else {
            JavaXmlTypeMappingType[] javaXmlTypeMappings = portInfo.getJavaWsdlMapping().getJavaXmlTypeMappingArray();
            buildHeavyweightTypes(wrapperElementQNames, javaXmlTypeMappings, classLoader, schemaTypeKeyToSchemaTypeMap, typeMapping);
        }

        serviceDesc.getOperations();
        return new ReadOnlyServiceDesc(serviceDesc);
    }

    private static void buildHeavyweightTypes(Set wrapperElementQNames, JavaXmlTypeMappingType[] javaXmlTypeMappings, ClassLoader classLoader, Map schemaTypeKeyToSchemaTypeMap, TypeMapping typeMapping) throws DeploymentException {
        for (int j = 0; j < javaXmlTypeMappings.length; j++) {
            JavaXmlTypeMappingType javaXmlTypeMapping = javaXmlTypeMappings[j];

            QName typeQName;
            SchemaTypeKey key;
            boolean isElement = javaXmlTypeMapping.getQnameScope().getStringValue().equals("element");
            boolean isSimpleType = javaXmlTypeMapping.getQnameScope().getStringValue().equals("simpleType");
            if (javaXmlTypeMapping.isSetRootTypeQname()) {
                typeQName = javaXmlTypeMapping.getRootTypeQname().getQNameValue();
                key = new SchemaTypeKey(typeQName, isElement, isSimpleType, false);
                
                // Skip the wrapper elements.
                if (wrapperElementQNames.contains(typeQName)) {
                    continue;
                }
            } else if (javaXmlTypeMapping.isSetAnonymousTypeQname()) {
                String anonTypeQNameString = javaXmlTypeMapping.getAnonymousTypeQname().getStringValue();
                int pos = anonTypeQNameString.lastIndexOf(":");
                if (pos == -1) {
                    throw new DeploymentException("anon QName is invalid, no final ':' " + anonTypeQNameString);
                }

                //this appears to be ignored...
                typeQName = new QName(anonTypeQNameString.substring(0, pos), anonTypeQNameString.substring(pos + 1));
                key = new SchemaTypeKey(typeQName, isElement, isSimpleType, true);

                // Skip the wrapper elements.
                if (wrapperElementQNames.contains(new QName(anonTypeQNameString.substring(0, pos), anonTypeQNameString.substring(pos + 2)))) {
                    continue;
                }
            } else {
                throw new DeploymentException("either root type qname or anonymous type qname must be set");
            }

            SchemaType schemaType = (SchemaType) schemaTypeKeyToSchemaTypeMap.get(key);
            if (schemaType == null) {
                throw new DeploymentException("Schema type key " + key + " not found in analyzed schema: " + schemaTypeKeyToSchemaTypeMap);
            }

            //default settings
            Class serializerFactoryClass = BeanSerializerFactory.class;
            Class deserializerFactoryClass = BeanDeserializerFactory.class;

            String className = javaXmlTypeMapping.getJavaType().getStringValue().trim();

            Class clazz = null;
            try {
                clazz = ClassLoading.loadClass(className, classLoader);
            } catch (ClassNotFoundException e2) {
                throw new DeploymentException("Could not load java type", e2);
            }

            if (clazz.isArray()) {
                serializerFactoryClass = ArraySerializerFactory.class;
                deserializerFactoryClass = ArrayDeserializerFactory.class;
            }
            
            TypeDesc typeDesc = TypeDescBuilder.getTypeDescriptor(clazz, typeQName, javaXmlTypeMapping, schemaType);

            SerializerFactory ser = BaseSerializerFactory.createFactory(serializerFactoryClass, clazz, typeQName);
            DeserializerFactory deser = BaseDeserializerFactory.createFactory(deserializerFactoryClass, clazz, typeQName);

            typeMapping.register(clazz, typeQName, ser, deser);
            TypeDesc.registerTypeDescForClass(clazz, typeDesc);
        }
    }

    private static void buildLightweightTypes(Map schemaTypeKeyToSchemaTypeMap, PortInfo portInfo, ClassLoader classLoader, TypeMapping typeMapping) throws DeploymentException {
        for (Iterator iterator = schemaTypeKeyToSchemaTypeMap.entrySet().iterator(); iterator.hasNext();) {
            Map.Entry entry = (Map.Entry) iterator.next();
            SchemaTypeKey key = (SchemaTypeKey) entry.getKey();
//            SchemaType schemaType = (SchemaType) entry.getValue();
            if (!key.isElement() && !key.isAnonymous()) {
                //default settings
                QName typeQName = key.getqName();
                String namespace = typeQName.getNamespaceURI();
                String packageName = WSDescriptorParser.getPackageFromNamespace(namespace, portInfo.getJavaWsdlMapping());
                String classShortName = typeQName.getLocalPart();
                String className = packageName + "." + classShortName;

                Class clazz = null;
                try {
                    clazz = ClassLoading.loadClass(className, classLoader);
                } catch (ClassNotFoundException e) {
                    throw new DeploymentException("Could not load java type", e);
                }

                Class serializerFactoryClass = BeanSerializerFactory.class;
                Class deserializerFactoryClass = BeanDeserializerFactory.class;

                if (clazz.isArray()) {
                    serializerFactoryClass = ArraySerializerFactory.class;
                    deserializerFactoryClass = ArrayDeserializerFactory.class;
                }

                SerializerFactory ser = BaseSerializerFactory.createFactory(serializerFactoryClass, clazz, typeQName);
                DeserializerFactory deser = BaseDeserializerFactory.createFactory(deserializerFactoryClass, clazz, typeQName);
                typeMapping.register(clazz, typeQName, ser, deser);

                //TODO construct typedesc as well.
//                TypeDesc typeDesc = getTypeDescriptor(clazz, typeQName, javaXmlTypeMapping, schemaType);
//                typeDescriptors.put(clazz, typeDesc);

            }
        }
    }

    private static Set buildOperations(Binding binding, Class serviceEndpointInterface, boolean lightweight, PortInfo portInfo, Map exceptionMap, Map complexTypeMap, Map elementMap, ClassLoader classLoader, JavaServiceDesc serviceDesc) throws DeploymentException {
        Set wrappedElementQNames = new HashSet();
        
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
                operationDescBuilder = new HeavyweightOperationDescBuilder(bindingOperation, portInfo.getJavaWsdlMapping(), methodMapping, Style.RPC, exceptionMap, complexTypeMap, elementMap, classLoader, serviceEndpointInterface);
                Set wrappedElementQNamesForOper = ((HeavyweightOperationDescBuilder) operationDescBuilder).getWrapperElementQNames();
                wrappedElementQNames.addAll(wrappedElementQNamesForOper);
            }

            serviceDesc.addOperationDesc(operationDescBuilder.buildOperationDesc());
        }
        
        return wrappedElementQNames;
    }


    private static Style getStyle(Binding binding) throws DeploymentException {
        SOAPBinding soapBinding = (SOAPBinding) WSDescriptorParser.getExtensibilityElement(SOAPBinding.class, binding.getExtensibilityElements());
//            String transportURI = soapBinding.getTransportURI();
        String portStyleString = soapBinding.getStyle();
        Style portStyle = Style.getStyle(portStyleString);
        return portStyle;
    }

    private static URL getAddressLocation(Port port) throws DeploymentException {
        SOAPAddress soapAddress = (SOAPAddress) WSDescriptorParser.getExtensibilityElement(SOAPAddress.class, port.getExtensibilityElements());
        String locationURIString = soapAddress.getLocationURI();
        URL location = null;
        try {
            location = new URL(locationURIString);
        } catch (MalformedURLException e) {
            throw new DeploymentException("Could not construct web service location URL from " + locationURIString);
        }
        return location;
    }
}
