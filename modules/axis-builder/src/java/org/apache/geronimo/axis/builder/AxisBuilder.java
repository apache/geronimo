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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Collections;
import java.util.jar.JarFile;
import java.beans.PropertyDescriptor;
import java.beans.Introspector;
import java.beans.IntrospectionException;
import javax.wsdl.Binding;
import javax.wsdl.BindingInput;
import javax.wsdl.BindingOperation;
import javax.wsdl.Definition;
import javax.wsdl.Fault;
import javax.wsdl.Message;
import javax.wsdl.Operation;
import javax.wsdl.Part;
import javax.wsdl.Port;
import javax.wsdl.PortType;
import javax.wsdl.extensions.soap.SOAPAddress;
import javax.wsdl.extensions.soap.SOAPBinding;
import javax.wsdl.extensions.soap.SOAPBody;
import javax.wsdl.extensions.soap.SOAPOperation;
import javax.xml.namespace.QName;
import javax.xml.rpc.handler.HandlerInfo;

import net.sf.cglib.core.DefaultGeneratorStrategy;
import net.sf.cglib.proxy.Callback;
import net.sf.cglib.proxy.Enhancer;
import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.NoOp;
import net.sf.cglib.reflect.FastClass;
import net.sf.cglib.reflect.FastConstructor;
import org.apache.axis.client.Service;
import org.apache.axis.constants.Style;
import org.apache.axis.constants.Use;
import org.apache.axis.description.FaultDesc;
import org.apache.axis.description.OperationDesc;
import org.apache.axis.description.ParameterDesc;
import org.apache.axis.description.TypeDesc;
import org.apache.axis.description.FieldDesc;
import org.apache.axis.description.AttributeDesc;
import org.apache.axis.description.ElementDesc;
import org.apache.axis.encoding.ser.ArrayDeserializerFactory;
import org.apache.axis.encoding.ser.ArraySerializerFactory;
import org.apache.axis.encoding.ser.BeanDeserializerFactory;
import org.apache.axis.encoding.ser.BeanSerializerFactory;
import org.apache.axis.soap.SOAPConstants;
import org.apache.geronimo.axis.client.GenericServiceEndpointWrapper;
import org.apache.geronimo.axis.client.NoOverrideCallbackFilter;
import org.apache.geronimo.axis.client.OperationInfo;
import org.apache.geronimo.axis.client.SEIFactory;
import org.apache.geronimo.axis.client.SEIFactoryImpl;
import org.apache.geronimo.axis.client.SerializableNoOp;
import org.apache.geronimo.axis.client.ServiceImpl;
import org.apache.geronimo.axis.client.ServiceMethodInterceptor;
import org.apache.geronimo.axis.client.TypeMappingInfo;
import org.apache.geronimo.common.DeploymentException;
import org.apache.geronimo.deployment.DeploymentContext;
import org.apache.geronimo.gbean.GBeanData;
import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.gbean.GBeanInfoBuilder;
import org.apache.geronimo.j2ee.deployment.Module;
import org.apache.geronimo.j2ee.deployment.ServiceReferenceBuilder;
import org.apache.geronimo.j2ee.deployment.WebServiceBuilder;
import org.apache.geronimo.j2ee.j2eeobjectnames.NameFactory;
import org.apache.geronimo.kernel.ClassLoading;
import org.apache.geronimo.naming.reference.DeserializingReference;
import org.apache.geronimo.xbeans.j2ee.ConstructorParameterOrderType;
import org.apache.geronimo.xbeans.j2ee.ExceptionMappingType;
import org.apache.geronimo.xbeans.j2ee.JavaWsdlMappingType;
import org.apache.geronimo.xbeans.j2ee.JavaXmlTypeMappingType;
import org.apache.geronimo.xbeans.j2ee.MethodParamPartsMappingType;
import org.apache.geronimo.xbeans.j2ee.ServiceEndpointInterfaceMappingType;
import org.apache.geronimo.xbeans.j2ee.ServiceEndpointMethodMappingType;
import org.apache.geronimo.xbeans.j2ee.WsdlMessageMappingType;
import org.apache.geronimo.xbeans.j2ee.WsdlReturnValueMappingType;
import org.apache.geronimo.xbeans.j2ee.VariableMappingType;
import org.apache.xmlbeans.SchemaType;
import org.apache.xmlbeans.SchemaProperty;
import org.objectweb.asm.Type;
import org.w3.x2001.xmlSchema.ComplexType;
import org.w3.x2001.xmlSchema.ExplicitGroup;
import org.w3.x2001.xmlSchema.LocalElement;

/**
 * @version $Rev:  $ $Date:  $
 */
public class AxisBuilder implements ServiceReferenceBuilder, WebServiceBuilder {
    private static final Class[] SERVICE_CONSTRUCTOR_TYPES = new Class[]{Map.class, Map.class};

    private static final SOAPConstants SOAP_VERSION = SOAPConstants.SOAP11_CONSTANTS;


    //WebServiceBuilder
    public void configurePOJO(GBeanData targetGBean, Object portInfoObject, String seiClassName) throws DeploymentException {
        PortInfo portInfo = (PortInfo) portInfoObject;
        System.out.println("NOT CONFIGURING WEB SERVICE " + portInfo.getPortName());
    }

    public void configureEJB(GBeanData targetGBean, Object portInfoObject, String seiClassName) throws DeploymentException {

    }


    //ServicereferenceBuilder
    public Object createService(Class serviceInterface, URI wsdlURI, URI jaxrpcMappingURI, QName serviceQName, Map portComponentRefMap, List handlerInfos, Map portLocationMap, DeploymentContext deploymentContext, Module module, ClassLoader classLoader) throws DeploymentException {
        JarFile moduleFile = module.getModuleFile();
        Definition definition = null;
        JavaWsdlMappingType mapping = null;
        if (wsdlURI != null) {
            definition = WSDescriptorParser.readWsdl(moduleFile, wsdlURI);

            mapping = WSDescriptorParser.readJaxrpcMapping(moduleFile, jaxrpcMappingURI);
        }

        Object service = createService(serviceInterface, definition, mapping, serviceQName, SOAP_VERSION, handlerInfos, portLocationMap, deploymentContext, module, classLoader);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = null;
        try {
            oos = new ObjectOutputStream(baos);
            oos.writeObject(service);
            oos.flush();
        } catch (IOException e) {
            throw new DeploymentException("Could not serialize service instance", e);
        }
        byte[] bytes = baos.toByteArray();
        DeserializingReference reference = new DeserializingReference(bytes);
        return reference;
    }

    public Object createService(Class serviceInterface, Definition definition, JavaWsdlMappingType mapping, QName serviceQName, SOAPConstants soapVersion, List handlerInfos, Map portLocationMap, DeploymentContext context, Module module, ClassLoader classloader) throws DeploymentException {
        Map seiPortNameToFactoryMap = new HashMap();
        Map seiClassNameToFactoryMap = new HashMap();
        Object serviceInstance = createService(serviceInterface, seiPortNameToFactoryMap, seiClassNameToFactoryMap, context, module, classloader);
        if (definition != null) {
            buildSEIFactoryMap(serviceInterface, definition, portLocationMap, mapping, handlerInfos, serviceQName, soapVersion, seiPortNameToFactoryMap, seiClassNameToFactoryMap, serviceInstance, context, module, classloader);
        }
        return serviceInstance;
    }

    public Object createService(Class serviceInterface, Map seiPortNameToFactoryMap, Map seiClassNameToFactoryMap, DeploymentContext deploymentContext, Module module, ClassLoader classLoader) throws DeploymentException {

        Callback callback = new ServiceMethodInterceptor(seiPortNameToFactoryMap);
        Callback[] methodInterceptors = new Callback[]{SerializableNoOp.INSTANCE, callback};

        Enhancer enhancer = new Enhancer();
        enhancer.setClassLoader(classLoader);
        enhancer.setSuperclass(ServiceImpl.class);
        enhancer.setInterfaces(new Class[]{serviceInterface});
        enhancer.setCallbackFilter(new NoOverrideCallbackFilter(Service.class));
        enhancer.setCallbackTypes(new Class[]{NoOp.class, MethodInterceptor.class});
        enhancer.setUseFactory(false);
        ByteArrayRetrievingGeneratorStrategy strategy = new ByteArrayRetrievingGeneratorStrategy();
        enhancer.setStrategy(strategy);
        Class serviceClass = enhancer.createClass();

        try {
            module.addClass(serviceClass.getName(), strategy.getClassBytes(), deploymentContext);
        } catch (IOException e) {
            throw new DeploymentException("Could not write out class bytes", e);
        } catch (URISyntaxException e) {
            throw new DeploymentException("Could not constuct URI for location of enhanced class", e);
        }
        Enhancer.registerCallbacks(serviceClass, methodInterceptors);
        FastConstructor constructor = FastClass.create(serviceClass).getConstructor(SERVICE_CONSTRUCTOR_TYPES);
        try {
            return constructor.newInstance(new Object[]{seiPortNameToFactoryMap, seiClassNameToFactoryMap});
        } catch (InvocationTargetException e) {
            throw new DeploymentException("Could not construct service instance", e.getTargetException());
        }
    }

    public void buildSEIFactoryMap(Class serviceInterface, Definition definition, Map portLocationMap, JavaWsdlMappingType mapping, List handlerInfos, QName serviceQName, SOAPConstants soapVersion, Map seiPortNameToFactoryMap, Map seiClassNameToFactoryMap, Object serviceImpl, DeploymentContext context, Module module, ClassLoader classLoader) throws DeploymentException {

        //find the service we are working with
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
        if (portLocationMap != null) {
            WSDescriptorParser.updatePortLocations(service, portLocationMap);
        }

        Map wsdlPortMap = service.getPorts();

        Map exceptionMap = WSDescriptorParser.getExceptionMap(mapping);
        Map schemaTypeKeyToSchemaTypeMap = WSDescriptorParser.buildSchemaTypeKeyToSchemaTypeMap(definition);
        Map complexTypeMap = WSDescriptorParser.getComplexTypesInWsdl(schemaTypeKeyToSchemaTypeMap);

        for (Iterator iterator = wsdlPortMap.entrySet().iterator(); iterator.hasNext();) {
            Map.Entry entry = (Map.Entry) iterator.next();
            String portName = (String) entry.getKey();
            Port port = (Port) entry.getValue();

            SOAPAddress soapAddress = (SOAPAddress) WSDescriptorParser.getExtensibilityElement(SOAPAddress.class, port.getExtensibilityElements());
            String locationURIString = soapAddress.getLocationURI();
            URL location = null;
            try {
                location = new URL(locationURIString);
            } catch (MalformedURLException e) {
                throw new DeploymentException("Could not construct web service location URL from " + locationURIString);
            }

            Binding binding = port.getBinding();
            SOAPBinding soapBinding = (SOAPBinding) WSDescriptorParser.getExtensibilityElement(SOAPBinding.class, binding.getExtensibilityElements());
//            String transportURI = soapBinding.getTransportURI();
            String portStyleString = soapBinding.getStyle();
            Style portStyle = Style.getStyle(portStyleString);

            PortType portType = binding.getPortType();

            SEIFactory seiFactory;

            Class serviceEndpointInterface = null;
            ServiceEndpointInterfaceMappingType[] endpointMappings = mapping.getServiceEndpointInterfaceMappingArray();
            //port type corresponds to SEI
            List operations = portType.getOperations();
            OperationInfo[] operationInfos = new OperationInfo[operations.size()];
            if (endpointMappings.length == 0) {
                //lightweight jaxrpc mapping supplied
                serviceEndpointInterface = getServiceEndpointInterfaceLightweight(portType, mapping, classLoader);
                Class enhancedServiceEndpointClass = enhanceServiceEndpointInterface(serviceEndpointInterface, context, module, classLoader);

                int i = 0;
                for (Iterator ops = operations.iterator(); ops.hasNext();) {
                    Operation operation = (Operation) ops.next();
                    Method method = getMethodForOperation(enhancedServiceEndpointClass, operation);
                    BindingOperation bindingOperation = binding.getBindingOperation(operation.getName(), operation.getInput().getName(), operation.getOutput() == null ? null : operation.getOutput().getName());
                    OperationInfo operationInfo = buildOperationInfoLightweight(method, bindingOperation, portStyle, soapVersion);
                    operationInfos[i++] = operationInfo;
                }
                List typeMappings = new ArrayList();
                Map typeDescriptors = new HashMap();
                buildTypeInfoLightWeight(schemaTypeKeyToSchemaTypeMap, mapping, classLoader, typeMappings, typeDescriptors);
                seiFactory = createSEIFactory(portName, enhancedServiceEndpointClass, serviceImpl, typeMappings, typeDescriptors, location, operationInfos, handlerInfos, context, classLoader);
            } else {
                //complete jaxrpc mapping file supplied
                QName portTypeQName = portType.getQName();
                ServiceEndpointInterfaceMappingType endpointMapping = WSDescriptorParser.getServiceEndpointInterfaceMapping(endpointMappings, portTypeQName);
                String fqcn = endpointMapping.getServiceEndpointInterface().getStringValue();
                try {
                    serviceEndpointInterface = classLoader.loadClass(fqcn);
                } catch (ClassNotFoundException e) {
                    throw new DeploymentException("Could not load service endpoint interface", e);
                }
                Class enhancedServiceEndpointClass = enhanceServiceEndpointInterface(serviceEndpointInterface, context, module, classLoader);

                ServiceEndpointMethodMappingType[] methodMappings = endpointMapping.getServiceEndpointMethodMappingArray();
                int i = 0;
                for (Iterator ops = operations.iterator(); ops.hasNext();) {
                    Operation operation = (Operation) ops.next();
                    String operationName = operation.getName();
                    BindingOperation bindingOperation = binding.getBindingOperation(operationName, operation.getInput().getName(), operation.getOutput() == null ? null : operation.getOutput().getName());
                    ServiceEndpointMethodMappingType methodMapping = WSDescriptorParser.getMethodMappingForOperation(operationName, methodMappings);
                    OperationInfo operationInfo = buildOperationInfoHeavyweight(methodMapping, bindingOperation, portStyle, soapVersion, exceptionMap, complexTypeMap, mapping, classLoader);
                    operationInfos[i++] = operationInfo;
                }
                JavaXmlTypeMappingType[] javaXmlTypeMappings = mapping.getJavaXmlTypeMappingArray();
                List typeMappings = new ArrayList();
                Map typeDescriptors = new HashMap();
                buildTypeInfoHeavyweight(javaXmlTypeMappings, schemaTypeKeyToSchemaTypeMap, classLoader, typeMappings, typeDescriptors);
                seiFactory = createSEIFactory(portName, enhancedServiceEndpointClass, serviceImpl, typeMappings, typeDescriptors, location, operationInfos, handlerInfos, context, classLoader);
            }
            seiPortNameToFactoryMap.put(portName, seiFactory);
            seiClassNameToFactoryMap.put(serviceEndpointInterface.getName(), seiFactory);
        }
    }

    private void buildTypeInfoLightWeight(Map schemaTypeKeyToSchemaTypeMap, JavaWsdlMappingType mapping, ClassLoader classLoader, List typeMappings, Map typeDescriptors) throws DeploymentException {
        for (Iterator iterator = schemaTypeKeyToSchemaTypeMap.entrySet().iterator(); iterator.hasNext();) {
            Map.Entry entry = (Map.Entry) iterator.next();
            SchemaTypeKey key = (SchemaTypeKey) entry.getKey();
//            SchemaType schemaType = (SchemaType) entry.getValue();
            if (!key.isElement() && !key.isAnonymous()) {
                //default settings
                Class serializerFactoryClass = BeanSerializerFactory.class;
                Class deserializerFactoryClass = BeanDeserializerFactory.class;
                QName typeQName = key.getqName();
                String namespace = typeQName.getNamespaceURI();
                String packageName = WSDescriptorParser.getPackageFromNamespace(namespace, mapping);
                String classShortName = typeQName.getLocalPart();
                String className = packageName + "." + classShortName;

                Class clazz = null;
                try {
                    clazz = ClassLoading.loadClass(className, classLoader);
                } catch (ClassNotFoundException e) {
                    throw new DeploymentException("Could not load java type", e);
                }
                if (clazz.isArray()) {
                    serializerFactoryClass = ArraySerializerFactory.class;
                    deserializerFactoryClass = ArrayDeserializerFactory.class;
                }

                TypeMappingInfo typeMappingInfo = new TypeMappingInfo(clazz, typeQName, serializerFactoryClass, deserializerFactoryClass);
                typeMappings.add(typeMappingInfo);
                //TODO construct typedesc as well.
//                TypeDesc typeDesc = getTypeDescriptor(clazz, typeQName, javaXmlTypeMapping, schemaType);
//                typeDescriptors.put(clazz, typeDesc);

            }
        }
    }

    private void buildTypeInfoHeavyweight(JavaXmlTypeMappingType[] javaXmlTypeMappings, Map schemaTypeKeyToSchemaTypeMap, ClassLoader classLoader, List typeMappings, Map typeDescriptors) throws DeploymentException {
        for (int j = 0; j < javaXmlTypeMappings.length; j++) {
            JavaXmlTypeMappingType javaXmlTypeMapping = javaXmlTypeMappings[j];
            //default settings
            Class serializerFactoryClass = BeanSerializerFactory.class;
            Class deserializerFactoryClass = BeanDeserializerFactory.class;

            String className = javaXmlTypeMapping.getJavaType().getStringValue().trim();

            Class clazz = null;
            try {
                clazz = ClassLoading.loadClass(className, classLoader);
            } catch (ClassNotFoundException e) {
                throw new DeploymentException("Could not load java type", e);
            }
            if (clazz.isArray()) {
                serializerFactoryClass = ArraySerializerFactory.class;
                deserializerFactoryClass = ArrayDeserializerFactory.class;
            }

            QName typeName;
            SchemaTypeKey key;
            TypeMappingInfo typeMappingInfo = null;
            boolean isElement = javaXmlTypeMapping.getQnameScope().getStringValue().equals("element");
            boolean isSimpleType = javaXmlTypeMapping.getQnameScope().getStringValue().equals("simpleType");
            if (javaXmlTypeMapping.isSetRootTypeQname()) {
                typeName = javaXmlTypeMapping.getRootTypeQname().getQNameValue();
                typeMappingInfo = new TypeMappingInfo(clazz, typeName, serializerFactoryClass, deserializerFactoryClass);
                key = new SchemaTypeKey(typeName, isElement, isSimpleType, false);
            } else if (javaXmlTypeMapping.isSetAnonymousTypeQname()) {
                String anonTypeQNameString = javaXmlTypeMapping.getAnonymousTypeQname().getStringValue();
                int pos = anonTypeQNameString.lastIndexOf(":");
                if (pos == -1) {
                    throw new DeploymentException("anon QName is invalid, no final ':' " + anonTypeQNameString);
                }
                //this appears to be ignored...
                typeName = new QName(anonTypeQNameString.substring(0, pos), anonTypeQNameString.substring(pos + 1));
                typeMappingInfo = new TypeMappingInfo(clazz, typeName, serializerFactoryClass, deserializerFactoryClass);
                key = new SchemaTypeKey(typeName, isElement, isSimpleType, true);
            } else {
                throw new DeploymentException("either root type qname or anonymous type qname must be set");
            }
            typeMappings.add(typeMappingInfo);
            SchemaType schemaType = (SchemaType) schemaTypeKeyToSchemaTypeMap.get(key);
            if (schemaType == null) {
                throw new DeploymentException("Schema type key " + key + " not found in analyzed schema: " + schemaTypeKeyToSchemaTypeMap);
            }
            TypeDesc typeDesc = getTypeDescriptor(clazz, typeName, javaXmlTypeMapping, schemaType);
            typeDescriptors.put(clazz, typeDesc);


        }
    }

    private TypeDesc getTypeDescriptor(Class javaClass, QName typeQName, JavaXmlTypeMappingType javaXmlTypeMapping, SchemaType schemaType) throws DeploymentException {
        boolean isRestriction = schemaType.getDerivationType() == SchemaType.DT_RESTRICTION;
        TypeDesc typeDesc = new TypeDesc(javaClass, !isRestriction);
        //TODO typeQName may be a 'anonymous" QName like construct.  Is this what axis expects?
        typeDesc.setXmlType(typeQName);
        VariableMappingType[] variableMappings = javaXmlTypeMapping.getVariableMappingArray();
        FieldDesc[] fields = new FieldDesc[variableMappings.length];

        PropertyDescriptor[] propertyDescriptors = new java.beans.PropertyDescriptor[0];
        try {
            propertyDescriptors = Introspector.getBeanInfo(javaClass).getPropertyDescriptors();
        } catch (IntrospectionException e) {
            throw new DeploymentException("Class " + javaClass + " is not a valid javabean", e);
        }
        Map properties = new HashMap();
        for (int i = 0; i < propertyDescriptors.length; i++) {
            PropertyDescriptor propertyDescriptor = propertyDescriptors[i];
            properties.put(propertyDescriptor.getName(), propertyDescriptor.getPropertyType());
        }
        for (int i = 0; i < variableMappings.length; i++) {
            VariableMappingType variableMapping = variableMappings[i];
            String fieldName = variableMapping.getJavaVariableName().getStringValue().trim();

            if (variableMapping.isSetXmlAttributeName()) {
                AttributeDesc attributeDesc = new AttributeDesc();
                //setting attribute name sets the xmlName with "" namespace, so don't do it
//                attributeDesc.setAttributeName(fieldName);
                attributeDesc.setFieldName(fieldName);
                Class javaType = (Class) properties.get(fieldName);
                if (javaType == null) {
                    throw new DeploymentException("field name " + fieldName + " not found in " + properties);
                }
                attributeDesc.setJavaType(javaType);
                //TODO correct namespace???
                String namespace = "";
                QName xmlName = new QName(namespace, variableMapping.getXmlAttributeName().getStringValue().trim());
                attributeDesc.setXmlName(xmlName);
                QName xmlType = schemaType.getName();
                attributeDesc.setXmlType(xmlType);
                fields[i] = attributeDesc;
            } else {
                ElementDesc elementDesc = new ElementDesc();
                elementDesc.setFieldName(fieldName);
                Class javaType = (Class) properties.get(fieldName);
                if (javaType == null) {
                    throw new DeploymentException("field name " + fieldName + " not found in " + properties);
                }
                elementDesc.setJavaType(javaType);
                //TODO correct namespace???
                String namespace = "";
                QName xmlName = new QName(namespace, variableMapping.getXmlElementName().getStringValue().trim());
                elementDesc.setXmlName(xmlName);
                QName xmlType = schemaType.getName();
                elementDesc.setXmlType(xmlType);
                //TODO figure out how to find these:
//                elementDesc.setArrayType(null);
//                elementDesc.setMinOccurs(0);
//                elementDesc.setMaxOccurs(0);
//                elementDesc.setNillable(false);
                fields[i] = elementDesc;
            }
        }
        typeDesc.setFields(fields);
        return typeDesc;
    }

    private Method getMethodForOperation(Class enhancedServiceEndpointClass, Operation operation) throws DeploymentException {
        Method[] methods = enhancedServiceEndpointClass.getMethods();
        String opName = operation.getName();
        Method found = null;
        for (int i = 0; i < methods.length; i++) {
            Method method = methods[i];
            if (method.getName().equals(opName)) {
                if (found != null) {
                    throw new DeploymentException("Overloaded methods NYI");
                }
                found = method;
            }
        }
        if (found == null) {
            throw new DeploymentException("No method found for operation named " + opName);
        }
        return found;
    }

    private Class getServiceEndpointInterfaceLightweight(PortType portType, JavaWsdlMappingType mappings, ClassLoader classLoader) throws DeploymentException {
        QName portTypeQName = portType.getQName();
        String portTypeNamespace = portTypeQName.getNamespaceURI();
        String portTypePackage = WSDescriptorParser.getPackageFromNamespace(portTypeNamespace, mappings);
        StringBuffer shortInterfaceName = new StringBuffer(portTypeQName.getLocalPart());
        shortInterfaceName.setCharAt(0, Character.toUpperCase(shortInterfaceName.charAt(0)));
        //TODO just use one buffer!
        String fqcn = portTypePackage + "." + shortInterfaceName.toString();
        try {
            return classLoader.loadClass(fqcn);
        } catch (ClassNotFoundException e) {
            throw new DeploymentException("Could not load service endpoint interface type", e);
        }

    }


    public SEIFactory createSEIFactory(String portName, Class enhancedServiceEndpointClass, Object serviceImpl, List typeMappings, Map typeDescriptors, URL location, OperationInfo[] operationInfos, List handlerInfoInfos, DeploymentContext deploymentContext, ClassLoader classLoader) throws DeploymentException {
        List handlerInfos = buildHandlerInfosForPort(portName, handlerInfoInfos);
        try {
            SEIFactory factory = new SEIFactoryImpl(portName, enhancedServiceEndpointClass, operationInfos, serviceImpl, typeMappings, typeDescriptors, location, handlerInfos, classLoader);
            return factory;
        } catch (ClassNotFoundException e) {
            throw new DeploymentException("Could not load GenericServiceEndpoint from application classloader", e);
        }
    }

    private List buildHandlerInfosForPort(String portName, List handlerInfoInfos) {
        List handlerInfos = new ArrayList();
        for (Iterator iterator = handlerInfoInfos.iterator(); iterator.hasNext();) {
            HandlerInfoInfo handlerInfoInfo = (HandlerInfoInfo) iterator.next();
            Set portNames = handlerInfoInfo.getPortNames();
            if (portNames.isEmpty() || portNames.contains(portName)) {
                HandlerInfo handlerInfo = new HandlerInfo(handlerInfoInfo.getHandlerClass(), handlerInfoInfo.getHandlerConfig(), handlerInfoInfo.getSoapHeaders());
                handlerInfos.add(handlerInfo);

                //TODO what about the soap roles??
            }
        }
        return handlerInfos;
    }

    public Class enhanceServiceEndpointInterface(Class serviceEndpointInterface, DeploymentContext deploymentContext, Module module, ClassLoader classLoader) throws DeploymentException {
        Enhancer enhancer = new Enhancer();
        enhancer.setClassLoader(classLoader);
        enhancer.setSuperclass(GenericServiceEndpointWrapper.class);
        enhancer.setInterfaces(new Class[]{serviceEndpointInterface});
        enhancer.setCallbackFilter(new NoOverrideCallbackFilter(GenericServiceEndpointWrapper.class));
        enhancer.setCallbackTypes(new Class[]{NoOp.class, MethodInterceptor.class});
        enhancer.setUseFactory(false);
        ByteArrayRetrievingGeneratorStrategy strategy = new ByteArrayRetrievingGeneratorStrategy();
        enhancer.setStrategy(strategy);
        Class serviceEndpointClass = enhancer.createClass();

        try {
            module.addClass(serviceEndpointClass.getName(), strategy.getClassBytes(), deploymentContext);
        } catch (IOException e) {
            throw new DeploymentException("Could not write out class bytes", e);
        } catch (URISyntaxException e) {
            throw new DeploymentException("Could not constuct URI for location of enhanced class", e);
        }
        return serviceEndpointClass;
    }

    public OperationInfo buildOperationInfoLightweight(Method method, BindingOperation bindingOperation, Style defaultStyle, SOAPConstants soapVersion) throws DeploymentException {

        if (bindingOperation == null) {
            throw new DeploymentException("No BindingOperation supplied for method " + method.getName());
        }
        Operation operation = bindingOperation.getOperation();
        String operationName = operation.getName();
        //section 7.3.2, we don't have to look at parameter ordering.
        //unless it turns out we have to validate it.
//        List order = operation.getParameterOrdering();
        List parameterList = new ArrayList();

        QName returnType = null;
        QName returnQName = null;

        Message inputMessage = operation.getInput().getMessage();
        Message outputMessage = operation.getOutput() == null ? null : operation.getOutput().getMessage();

        if (outputMessage != null && outputMessage.getParts().size() > 1) {
            throw new DeploymentException("Lightweight mapping has at most one part in the (optional) output message, not: " + outputMessage.getParts().size());
        }
        Class[] methodParamTypes = method.getParameterTypes();
        List inputParts = inputMessage.getOrderedParts(null);
        if (methodParamTypes.length != inputParts.size()) {
            throw new DeploymentException("mismatch in parameter counts: method has " + methodParamTypes.length + " whereas the input message has " + inputParts.size());
        }
        int i = 0;
        for (Iterator parts = inputParts.iterator(); parts.hasNext();) {
            Part part = (Part) parts.next();
            String partName = part.getName();
            QName name = new QName("", partName);
            byte mode = ParameterDesc.IN;
            QName typeQName = part.getTypeName() == null ? part.getElementName() : part.getTypeName();
            Class javaClass = methodParamTypes[i++];
            //lightweight mapping has no parts in headers, so inHeader and outHeader are false
            ParameterDesc parameter = new ParameterDesc(name, mode, typeQName, javaClass, false, false);
            parameterList.add(parameter);
        }
        if (outputMessage != null && outputMessage.getParts().size() == 1) {
            returnQName = outputMessage.getQName();
            Part part = (Part) outputMessage.getParts().values().iterator().next();
            returnType = part.getTypeName() == null ? part.getElementName() : part.getTypeName();
        }
        ParameterDesc[] parameterDescs = (ParameterDesc[]) parameterList.toArray(new ParameterDesc[parameterList.size()]);
        OperationDesc operationDesc = new OperationDesc(operationName, parameterDescs, returnQName);
        operationDesc.setReturnType(returnType);
        Class returnClass = method.getReturnType();
        operationDesc.setReturnClass(returnClass);

        SOAPOperation soapOperation = (SOAPOperation) WSDescriptorParser.getExtensibilityElement(SOAPOperation.class, bindingOperation.getExtensibilityElements());
        String soapActionURI = soapOperation.getSoapActionURI();
        String styleString = soapOperation.getStyle();
        Style style = Style.getStyle(styleString, defaultStyle);
        BindingInput bindingInput = bindingOperation.getBindingInput();
        SOAPBody soapBody = (SOAPBody) WSDescriptorParser.getExtensibilityElement(SOAPBody.class, bindingInput.getExtensibilityElements());
        String useString = soapBody.getUse();
        Use use = Use.getUse(useString);
        operationDesc.setStyle(style);
        operationDesc.setUse(use);
        //TODO add faults
//        TFault[] faults = tOperation.getFaultArray();
//        for (int i = 0; i < faults.length; i++) {
//            TFault fault = faults[i];
//            QName faultQName = new QName("", fault.getName());
//            String className = ;
//            QName faultTypeQName = ;
//            boolean isComplex = ;
//            FaultDesc faultDesc = new FaultDesc(faultQName, className, faultTypeQName, isComplex)
//        }
        boolean usesSOAPAction = (soapActionURI != null);
        QName operationQName = new QName("", operation.getName());

        String methodName = method.getName();
        String methodDesc = Type.getMethodDescriptor(method);
        OperationInfo operationInfo = new OperationInfo(operationDesc, usesSOAPAction, soapActionURI, soapVersion, operationQName, methodName, methodDesc);
        return operationInfo;
    }

    public OperationInfo buildOperationInfoHeavyweight(ServiceEndpointMethodMappingType methodMapping, BindingOperation bindingOperation, Style defaultStyle, SOAPConstants soapVersion, Map exceptionMap, Map complexTypeMap, JavaWsdlMappingType mapping, ClassLoader classLoader) throws DeploymentException {

        //TODO how can bindingOperation be null?
        Operation operation = bindingOperation.getOperation();
        String operationName = operation.getName();


        Message inputMessage = operation.getInput().getMessage();
        Message outputMessage = operation.getOutput() == null ? null : operation.getOutput().getMessage();

        boolean isWrappedElement = methodMapping.isSetWrappedElement();

        MethodParamPartsMappingType[] paramMappings = methodMapping.getMethodParamPartsMappingArray();
        Type[] parameterASMTypes = new Type[paramMappings.length];
        ParameterDesc[] parameterDescriptions = new ParameterDesc[paramMappings.length];

        Set inParamNames = new HashSet();
        Set outParamNames = new HashSet();
        for (int i = 0; i < paramMappings.length; i++) {
            MethodParamPartsMappingType paramMapping = paramMappings[i];
            int position = paramMapping.getParamPosition().getBigIntegerValue().intValue();

            WsdlMessageMappingType wsdlMessageMappingType = paramMapping.getWsdlMessageMapping();
            QName wsdlMessageQName = wsdlMessageMappingType.getWsdlMessage().getQNameValue();
            String wsdlMessagePartName = wsdlMessageMappingType.getWsdlMessagePartName().getStringValue().trim();

            String parameterMode = wsdlMessageMappingType.getParameterMode().getStringValue().trim();
            byte mode = ParameterDesc.modeFromString(parameterMode);
            boolean isInParam = mode == ParameterDesc.IN || mode == ParameterDesc.INOUT;
            boolean isOutParam = mode == ParameterDesc.OUT || mode == ParameterDesc.INOUT;

            if (isOutParam && outputMessage == null) {
                throw new DeploymentException("Mapping for output parameter " + wsdlMessagePartName + " found, but no output message for operation " + operationName);
            }
            boolean isSoapHeader = wsdlMessageMappingType.isSetSoapHeader();
            boolean inHeader = isSoapHeader && isInParam;
            boolean outHeader = isSoapHeader && isOutParam;

            Part part;
            if (isInParam) {
                if (!wsdlMessageQName.equals(inputMessage.getQName())) {
                    throw new DeploymentException("QName of input message: " + inputMessage.getQName() +
                            " does not match mapping message QName: " + wsdlMessageQName + " for operation " + operationName);
                }
                part = inputMessage.getPart(wsdlMessagePartName);
                if (part == null) {
                    throw new DeploymentException("No part for wsdlMessagePartName " + wsdlMessagePartName + " in input message for operation " + operationName);
                }
                inParamNames.add(wsdlMessagePartName);
                if (isOutParam) {
                    //inout, check that part of same name and type is in output message
                    Part outPart = outputMessage.getPart(wsdlMessagePartName);
                    if (outPart == null) {
                        throw new DeploymentException("No part for wsdlMessagePartName " + wsdlMessagePartName + " in output message for INOUT parameter of operation " + operationName);
                    }
                    if (!part.getName().equals(outPart.getName())) {
                        throw new DeploymentException("Mismatched input part name: " + part.getName() + " and output part name: " + outPart.getName() + " for INOUT parameter for wsdlMessagePartName " + wsdlMessagePartName + " for operation " + operationName);
                    }
                    if (!(part.getElementName() == null ? outPart.getElementName() == null : part.getElementName().equals(outPart.getElementName()))) {
                        throw new DeploymentException("Mismatched input part element name: " + part.getElementName() + " and output part element name: " + outPart.getElementName() + " for INOUT parameter for wsdlMessagePartName " + wsdlMessagePartName + " for operation " + operationName);
                    }
                    if (!(part.getTypeName() == null ? outPart.getTypeName() == null : part.getTypeName().equals(outPart.getTypeName()))) {
                        throw new DeploymentException("Mismatched input part type name: " + part.getTypeName() + " and output part type name: " + outPart.getTypeName() + " for INOUT parameter for wsdlMessagePartName " + wsdlMessagePartName + " for operation " + operationName);
                    }
                    outParamNames.add(wsdlMessagePartName);
                }
            } else if (isOutParam) {
                if (!wsdlMessageQName.equals(outputMessage.getQName())) {
                    throw new DeploymentException("QName of output message: " + outputMessage.getQName() +
                            " does not match mapping message QName: " + wsdlMessageQName + " for operation " + operationName);
                }
                part = outputMessage.getPart(wsdlMessagePartName);
                if (part == null) {
                    throw new DeploymentException("No part for wsdlMessagePartName " + wsdlMessagePartName + " in output message for operation " + operationName);
                }
                outParamNames.add(wsdlMessagePartName);
            } else {
                throw new AssertionError("a param mapping has to be IN or OUT or INOUT");
            }

            //TODO this makes little sense but may be correct, see comments in axis Parameter class
            //the part name qname is really odd.
            QName partQName = isWrappedElement ? part.getElementName() : new QName("", part.getName());
            QName partTypeQName = part.getTypeName();

            //use complexTypeMap
            boolean isComplexType = complexTypeMap.containsKey(partTypeQName);
            String paramJavaTypeName = paramMapping.getParamType().getStringValue().trim();
            boolean isInOnly = mode == ParameterDesc.IN;
            Class actualParamJavaType = WSDescriptorParser.getHolderType(paramJavaTypeName, isInOnly, partTypeQName, isComplexType, mapping, classLoader);

            ParameterDesc parameterDesc = new ParameterDesc(partQName, mode, partTypeQName, actualParamJavaType, inHeader, outHeader);
            parameterDescriptions[position] = parameterDesc;
            parameterASMTypes[position] = Type.getType(actualParamJavaType);
        }

        //check that all the parameters are there
        for (int i = 0; i < parameterDescriptions.length; i++) {
            ParameterDesc parameterDescription = parameterDescriptions[i];
            if (parameterDescription == null) {
                throw new DeploymentException("There is no mapping for parameter number " + i + " for operation " + operationName);
            }
        }

        //check that all input message parts are mapped
        if (!inParamNames.equals(inputMessage.getParts().keySet())) {
            throw new DeploymentException("Not all input message parts were mapped for operation name" + operationName);
        }


        QName returnType = null;
        QName returnQName = null;
        Class returnClass = null;
        Type returnASMType = Type.VOID_TYPE;

        if (methodMapping.isSetWsdlReturnValueMapping()) {
            if (outputMessage == null) {
                throw new DeploymentException("No output message, but a mapping for it for operation " + operationName);
            }
            WsdlReturnValueMappingType wsdlReturnValueMapping = methodMapping.getWsdlReturnValueMapping();
            String returnClassName = wsdlReturnValueMapping.getMethodReturnValue().getStringValue().trim();
            try {
                returnClass = ClassLoading.loadClass(returnClassName, classLoader);
            } catch (ClassNotFoundException e) {
                throw new DeploymentException("Could not load return type for operation " + operationName, e);
            }

            QName wsdlMessageQName = wsdlReturnValueMapping.getWsdlMessage().getQNameValue();

            if (!wsdlMessageQName.equals(outputMessage.getQName())) {
                throw new DeploymentException("OutputMessage has QName: " + outputMessage.getQName() + " but mapping specifies: " + wsdlMessageQName + " for operation " + operationName);
            }

            if (wsdlReturnValueMapping.isSetWsdlMessagePartName()) {
                String wsdlMessagePartName = wsdlReturnValueMapping.getWsdlMessagePartName().getStringValue().trim();
                if (outParamNames.contains(wsdlMessagePartName)) {
                    throw new DeploymentException("output message part " + wsdlMessagePartName + " has both an INOUT or OUT mapping and a return value mapping for operation " + operationName);
                }
                Part part = outputMessage.getPart(wsdlMessagePartName);
                returnQName = part.getElementName();
                returnType = part.getTypeName();

                outParamNames.add(wsdlMessagePartName);

            } else {
                //what does this mean????
            }

            returnASMType = Type.getType(returnClass);

        }

        if (outputMessage != null && !outParamNames.equals(outputMessage.getParts().keySet())) {
            throw new DeploymentException("Not all output message parts were mapped to parameters or a return value for operation " + operationName);
        }

        OperationDesc operationDesc = new OperationDesc(operationName, parameterDescriptions, returnQName);
        operationDesc.setReturnType(returnType);
        operationDesc.setReturnClass(returnClass);

        SOAPOperation soapOperation = (SOAPOperation) WSDescriptorParser.getExtensibilityElement(SOAPOperation.class, bindingOperation.getExtensibilityElements());
        String soapActionURI = soapOperation.getSoapActionURI();
        String styleString = soapOperation.getStyle();
        Style style = Style.getStyle(styleString, defaultStyle);
        BindingInput bindingInput = bindingOperation.getBindingInput();
        SOAPBody soapBody = (SOAPBody) WSDescriptorParser.getExtensibilityElement(SOAPBody.class, bindingInput.getExtensibilityElements());
        String useString = soapBody.getUse();
        Use use = Use.getUse(useString);
        operationDesc.setStyle(style);
        operationDesc.setUse(use);
        //TODO add faults

        Map faultMap = operation.getFaults();
        for (Iterator iterator = faultMap.entrySet().iterator(); iterator.hasNext();) {
            Map.Entry entry = (Map.Entry) iterator.next();
            String faultName = (String) entry.getKey();
            Fault fault = (Fault) entry.getValue();
            Message message = fault.getMessage();
            QName messageQName = message.getQName();
            ExceptionMappingType exceptionMapping = (ExceptionMappingType) exceptionMap.get(messageQName);
            if (exceptionMapping == null) {
                throw new DeploymentException("No exception mapping for fault " + faultName + " and fault message " + messageQName + " for operation " + operationName);
            }
            String className = exceptionMapping.getExceptionType().getStringValue().trim();
            //this is weird, but I can't figure out what it should be.
            QName faultQName = new QName("", faultName);
            Part part;
            if (exceptionMapping.isSetWsdlMessagePartName()) {
                //According to schema documentation, this will only be set when several headerfaults use the same message.
                String headerFaultMessagePartName = exceptionMapping.getWsdlMessagePartName().getStringValue();
                part = message.getPart(headerFaultMessagePartName);
            } else {
                part = (Part) message.getOrderedParts(null).iterator().next();
            }
            QName faultTypeQName = part.getElementName() == null ? part.getTypeName() : part.getElementName();
            boolean isComplex = faultTypeQName != null && complexTypeMap.containsKey(faultTypeQName);
            FaultDesc faultDesc = new FaultDesc(faultQName, className, faultTypeQName, isComplex);

            //constructor parameters
            if (exceptionMapping.isSetConstructorParameterOrder()) {
                if (!isComplex) {
                    throw new DeploymentException("ConstructorParameterOrder can only be set for complex types, not " + faultTypeQName);
                }
                SchemaType complexType = (SchemaType) complexTypeMap.get(faultTypeQName);
                Map elementMap = new HashMap();
                SchemaProperty[] properties = complexType.getProperties();
                for (int i = 0; i < properties.length; i++) {
                    SchemaProperty property = properties[i];
                    QName elementName = property.getName();
                    SchemaType elementType = property.getType();
                    QName elementTypeQName = elementType.getName();
                    elementMap.put(elementName.getLocalPart(), elementTypeQName);
                }
//                LocalElement[] elements = explicitGroup.getElementArray();
//                for (int i = 0; i < elements.length; i++) {
//                    LocalElement element = elements[i];
//                    String elementName = element.getName();
//                    QName elementType = element.getType();
//                    elementMap.put(elementName, elementType);
//                }
                ArrayList parameterTypes = new ArrayList();
                ConstructorParameterOrderType constructorParameterOrder = exceptionMapping.getConstructorParameterOrder();
                for (int i = 0; i < constructorParameterOrder.getElementNameArray().length; i++) {
                    String elementName = constructorParameterOrder.getElementNameArray(i).getStringValue().trim();
                    QName elementType = (QName) elementMap.get(elementName);
                    String javaElementTypeName;
                    if (complexTypeMap.containsKey(elementType)) {
                        String packageName = WSDescriptorParser.getPackageFromNamespace(elementType.getNamespaceURI(), mapping);
                        javaElementTypeName = packageName + "." + elementType.getLocalPart();
                    } else {
                        //TODO finish this
                        if (elementType.getLocalPart().equals("String")) {
                            javaElementTypeName = String.class.getName();
                        } else {
                            throw new DeploymentException("most simple exception constructor types not yet implemented");
                        }
                    }
                    Class javaElementType;
                    try {
                        javaElementType = ClassLoading.loadClass(javaElementTypeName, classLoader);
                    } catch (ClassNotFoundException e) {
                        throw new DeploymentException("Could not load exception constructor parameter", e);
                    }
                    //todo faultTypeQName is speculative
                    //todo outheader might be true!
                    ParameterDesc parameterDesc = new ParameterDesc(faultTypeQName, ParameterDesc.OUT, elementType, javaElementType, false, false);
                    parameterTypes.add(parameterDesc);
                }
                faultDesc.setParameters(parameterTypes);
            }
            operationDesc.addFault(faultDesc);
        }

        boolean usesSOAPAction = (soapActionURI != null);
        QName operationQName = new QName("", operation.getName());

        String methodName = methodMapping.getJavaMethodName().getStringValue().trim();
        String methodDesc = Type.getMethodDescriptor(returnASMType, parameterASMTypes);
        OperationInfo operationInfo = new OperationInfo(operationDesc, usesSOAPAction, soapActionURI, soapVersion, operationQName, methodName, methodDesc);
        return operationInfo;
    }

    private static class ByteArrayRetrievingGeneratorStrategy extends DefaultGeneratorStrategy {

        private byte[] classBytes;

        public byte[] transform(byte[] b) {
            classBytes = b;
            return b;
        }

        public byte[] getClassBytes() {
            return classBytes;
        }
    }


    public static final GBeanInfo GBEAN_INFO;

    static {
        GBeanInfoBuilder infoBuilder = new GBeanInfoBuilder(AxisBuilder.class, NameFactory.MODULE_BUILDER);
        infoBuilder.addInterface(ServiceReferenceBuilder.class);
        infoBuilder.addInterface(WebServiceBuilder.class);

        GBEAN_INFO = infoBuilder.getBeanInfo();
    }

    public static GBeanInfo getGBeanInfo() {
        return GBEAN_INFO;
    }

}
