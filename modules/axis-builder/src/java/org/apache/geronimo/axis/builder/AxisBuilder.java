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
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.jar.JarFile;

import javax.wsdl.Binding;
import javax.wsdl.BindingOperation;
import javax.wsdl.Definition;
import javax.wsdl.Operation;
import javax.wsdl.Port;
import javax.wsdl.PortType;
import javax.wsdl.extensions.soap.SOAPAddress;
import javax.wsdl.extensions.soap.SOAPBinding;
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
import org.apache.axis.description.JavaServiceDesc;
import org.apache.axis.description.TypeDesc;
import org.apache.axis.encoding.ser.ArrayDeserializerFactory;
import org.apache.axis.encoding.ser.ArraySerializerFactory;
import org.apache.axis.encoding.ser.BeanDeserializerFactory;
import org.apache.axis.encoding.ser.BeanSerializerFactory;
import org.apache.axis.handlers.HandlerInfoChainFactory;
import org.apache.axis.handlers.soap.SOAPService;
import org.apache.axis.providers.java.RPCProvider;
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
import org.apache.geronimo.axis.server.AxisWebServiceContainer;
import org.apache.geronimo.axis.server.POJOProvider;
import org.apache.geronimo.axis.server.ServiceInfo;
import org.apache.geronimo.common.DeploymentException;
import org.apache.geronimo.deployment.DeploymentContext;
import org.apache.geronimo.gbean.GBeanData;
import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.gbean.GBeanInfoBuilder;
import org.apache.geronimo.j2ee.deployment.Module;
import org.apache.geronimo.j2ee.deployment.POJOWebServiceBuilder;
import org.apache.geronimo.j2ee.deployment.ServiceReferenceBuilder;
import org.apache.geronimo.j2ee.j2eeobjectnames.NameFactory;
import org.apache.geronimo.kernel.ClassLoaderReference;
import org.apache.geronimo.kernel.ClassLoading;
import org.apache.geronimo.kernel.StoredObject;
import org.apache.geronimo.naming.reference.DeserializingReference;
import org.apache.geronimo.xbeans.j2ee.JavaWsdlMappingType;
import org.apache.geronimo.xbeans.j2ee.JavaXmlTypeMappingType;
import org.apache.geronimo.xbeans.j2ee.ServiceEndpointInterfaceMappingType;
import org.apache.geronimo.xbeans.j2ee.ServiceEndpointMethodMappingType;
import org.apache.xmlbeans.SchemaType;

/**
 * @version $Rev:  $ $Date:  $
 */
public class AxisBuilder implements ServiceReferenceBuilder, POJOWebServiceBuilder {
    private static final Class[] SERVICE_CONSTRUCTOR_TYPES = new Class[]{Map.class, Map.class};

    private static final SOAPConstants SOAP_VERSION = SOAPConstants.SOAP11_CONSTANTS;


    //WebServiceBuilder
    public void configurePOJO(GBeanData targetGBean, Object portInfoObject, String seiClassName, ClassLoader classLoader) throws DeploymentException {
        PortInfo portInfo = (PortInfo) portInfoObject;
        ServiceInfo serviceInfo = AxisServiceBuilder.createServiceInfo(portInfo, classLoader);
        JavaServiceDesc serviceDesc = serviceInfo.getServiceDesc();

        Class pojoClass = null;
        try {
            pojoClass = classLoader.loadClass(seiClassName);
        } catch (ClassNotFoundException e) {
            throw new DeploymentException("Unable to load servlet class for pojo webservice: "+seiClassName, e);
        }

        RPCProvider provider = new POJOProvider(pojoClass);
        SOAPService service = new SOAPService(null, provider, null);
        service.setServiceDescription(serviceDesc);
        service.setOption("className", seiClassName);

        HandlerInfoChainFactory handlerInfoChainFactory = new HandlerInfoChainFactory(serviceInfo.getHanlderInfos());
        service.setOption(org.apache.axis.Constants.ATTR_HANDLERINFOCHAIN, handlerInfoChainFactory);

        URL wsdlURL = null;
        try {
            wsdlURL = new URL(serviceDesc.getWSDLFile());
        } catch (MalformedURLException e) {
            throw new DeploymentException("Invalid URL to the webservice's WSDL file", e);
        }


        URI location = null;
        try {
            location = new URI(serviceDesc.getEndpointURL());
        } catch (URISyntaxException e) {
            throw new DeploymentException("Invalid webservice endpoint URI", e);
        }

        classLoader = new ClassLoaderReference(classLoader);
        AxisWebServiceContainer axisWebServiceContainer = new AxisWebServiceContainer(location, wsdlURL, service, classLoader);
        //targetGBean.setAttribute("webServiceContainer", axisWebServiceContainer);
        try {
            targetGBean.setAttribute("webServiceContainer", new StoredObject(axisWebServiceContainer)); // Hack!
        } catch (IOException e) {
            throw new DeploymentException("Unable to serialize the AxisWebServiceContainer", e);
        }
    }

    public void configureEJB(GBeanData targetGBean, Object portInfoObject, String seiClassName) throws DeploymentException {

    }


    //ServicereferenceBuilder
    public Object createService(Class serviceInterface, URI wsdlURI, URI jaxrpcMappingURI, QName serviceQName, Map portComponentRefMap, List handlerInfos, Map portLocationMap, Map credentialsNameMap, DeploymentContext deploymentContext, Module module, ClassLoader classLoader) throws DeploymentException {
        JarFile moduleFile = module.getModuleFile();
        Definition definition = null;
        JavaWsdlMappingType mapping = null;
        if (wsdlURI != null) {
            definition = WSDescriptorParser.readWsdl(moduleFile, wsdlURI);

            mapping = WSDescriptorParser.readJaxrpcMapping(moduleFile, jaxrpcMappingURI);
        }

        Object service = createService(serviceInterface, definition, mapping, serviceQName, SOAP_VERSION, handlerInfos, portLocationMap, credentialsNameMap, deploymentContext, module, classLoader);
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

    public Object createService(Class serviceInterface, Definition definition, JavaWsdlMappingType mapping, QName serviceQName, SOAPConstants soapVersion, List handlerInfos, Map portLocationMap, Map credentialsNameMap, DeploymentContext context, Module module, ClassLoader classloader) throws DeploymentException {
        Map seiPortNameToFactoryMap = new HashMap();
        Map seiClassNameToFactoryMap = new HashMap();
        Object serviceInstance = createServiceInterfaceProxy(serviceInterface, seiPortNameToFactoryMap, seiClassNameToFactoryMap, context, module, classloader);
        if (definition != null) {
            buildSEIFactoryMap(serviceInterface, definition, portLocationMap, credentialsNameMap, mapping, handlerInfos, serviceQName, soapVersion, seiPortNameToFactoryMap, seiClassNameToFactoryMap, serviceInstance, context, module, classloader);
        }
        return serviceInstance;
    }

    public Object createServiceInterfaceProxy(Class serviceInterface, Map seiPortNameToFactoryMap, Map seiClassNameToFactoryMap, DeploymentContext deploymentContext, Module module, ClassLoader classLoader) throws DeploymentException {

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

    public void buildSEIFactoryMap(Class serviceInterface, Definition definition, Map portLocationMap, Map credentialsNameMap, JavaWsdlMappingType mapping, List handlerInfos, QName serviceQName, SOAPConstants soapVersion, Map seiPortNameToFactoryMap, Map seiClassNameToFactoryMap, Object serviceImpl, DeploymentContext context, Module module, ClassLoader classLoader) throws DeploymentException {

        //find the service we are working with
        javax.wsdl.Service service = getService(serviceQName, definition);

        if (portLocationMap != null) {
            WSDescriptorParser.updatePortLocations(service, portLocationMap);
        }

        Map exceptionMap = WSDescriptorParser.getExceptionMap(mapping);
        Map schemaTypeKeyToSchemaTypeMap = WSDescriptorParser.buildSchemaTypeKeyToSchemaTypeMap(definition);
        Map complexTypeMap = WSDescriptorParser.getComplexTypesInWsdl(schemaTypeKeyToSchemaTypeMap);
        Map elementMap = WSDescriptorParser.getElementToTypeMap(schemaTypeKeyToSchemaTypeMap);

        Map wsdlPortMap = service.getPorts();
        for (Iterator iterator = wsdlPortMap.entrySet().iterator(); iterator.hasNext();) {
            Map.Entry entry = (Map.Entry) iterator.next();
            String portName = (String) entry.getKey();
            Port port = (Port) entry.getValue();

            URL location = getAddressLocation(port);

            Binding binding = port.getBinding();

            Style portStyle = getStyle(binding);

            PortType portType = binding.getPortType();

            ServiceEndpointInterfaceMappingType[] endpointMappings = mapping.getServiceEndpointInterfaceMappingArray();

            String credentialsName = credentialsNameMap == null? null: (String) credentialsNameMap.get(port.getName());

            //port type corresponds to SEI
            List operations = portType.getOperations();
            OperationInfo[] operationInfos = new OperationInfo[operations.size()];
            if (endpointMappings.length == 0) {
                doLightweightMapping(service.getQName(), portType, mapping, classLoader, context, module, operations, binding, portStyle, soapVersion, operationInfos, schemaTypeKeyToSchemaTypeMap, portName, serviceImpl, location, handlerInfos, seiPortNameToFactoryMap, seiClassNameToFactoryMap, credentialsName);
            } else {
                doHeavyweightMapping(service.getQName(), portType, endpointMappings, classLoader, context, module, operations, binding, portStyle, soapVersion, exceptionMap, complexTypeMap, elementMap, mapping, operationInfos, schemaTypeKeyToSchemaTypeMap, portName, serviceImpl, location, handlerInfos, seiPortNameToFactoryMap, seiClassNameToFactoryMap, credentialsName);
            }
        }
    }

    private javax.wsdl.Service getService(QName serviceQName, Definition definition) throws DeploymentException {
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

    private Style getStyle(Binding binding) throws DeploymentException {
        SOAPBinding soapBinding = (SOAPBinding) WSDescriptorParser.getExtensibilityElement(SOAPBinding.class, binding.getExtensibilityElements());
//            String transportURI = soapBinding.getTransportURI();
        String portStyleString = soapBinding.getStyle();
        Style portStyle = Style.getStyle(portStyleString);
        return portStyle;
    }

    private URL getAddressLocation(Port port) throws DeploymentException {
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

    private void doHeavyweightMapping(QName serviceName, PortType portType, ServiceEndpointInterfaceMappingType[] endpointMappings, ClassLoader classLoader, DeploymentContext context, Module module, List operations, Binding binding, Style portStyle, SOAPConstants soapVersion, Map exceptionMap, Map complexTypeMap, Map elementMap, JavaWsdlMappingType mapping, OperationInfo[] operationInfos, Map schemaTypeKeyToSchemaTypeMap, String portName, Object serviceImpl, URL location, List handlerInfos, Map seiPortNameToFactoryMap, Map seiClassNameToFactoryMap, String credentialsName) throws DeploymentException {
        Class serviceEndpointInterface;
        SEIFactory seiFactory;
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
            //the obvious method seems to be buggy
//            BindingOperation bindingOperation = binding.getBindingOperation(operationName, operation.getInput().getName(), operation.getOutput() == null ? null : operation.getOutput().getName());
            BindingOperation bindingOperation = null;
            List bops = binding.getBindingOperations();
            for (Iterator iterator = bops.iterator(); iterator.hasNext();) {
                BindingOperation bindingOperation1 = (BindingOperation) iterator.next();
                if (bindingOperation1.getOperation().equals(operation)) {
                    bindingOperation = bindingOperation1;
                    break;
                }
            }
            if (bindingOperation == null) {
                throw new DeploymentException("No BindingOperation for operation: " + operationName + ", input: " + operation.getInput().getName() + ", output: " + (operation.getOutput() == null ? "<none>" : operation.getOutput().getName()));
            }
            ServiceEndpointMethodMappingType methodMapping = WSDescriptorParser.getMethodMappingForOperation(operationName, methodMappings);
            HeavyweightOperationDescBuilder operationDescBuilder = new HeavyweightOperationDescBuilder(bindingOperation, mapping, methodMapping, portStyle, exceptionMap, complexTypeMap, elementMap, classLoader, enhancedServiceEndpointClass);
            OperationInfo operationInfo = operationDescBuilder.buildOperationInfo(soapVersion);
            operationInfos[i++] = operationInfo;
        }
        JavaXmlTypeMappingType[] javaXmlTypeMappings = mapping.getJavaXmlTypeMappingArray();
        List typeMappings = new ArrayList();
        Map typeDescriptors = new HashMap();
        buildTypeInfoHeavyweight(javaXmlTypeMappings, schemaTypeKeyToSchemaTypeMap, classLoader, typeMappings, typeDescriptors);
        seiFactory = createSEIFactory(serviceName, portName, enhancedServiceEndpointClass, serviceImpl, typeMappings, typeDescriptors, location, operationInfos, handlerInfos, credentialsName, context, classLoader);
        seiPortNameToFactoryMap.put(portName, seiFactory);
        seiClassNameToFactoryMap.put(serviceEndpointInterface.getName(), seiFactory);
    }

    private void doLightweightMapping(QName serviceName, PortType portType, JavaWsdlMappingType mapping, ClassLoader classLoader, DeploymentContext context, Module module, List operations, Binding binding, Style portStyle, SOAPConstants soapVersion, OperationInfo[] operationInfos, Map schemaTypeKeyToSchemaTypeMap, String portName, Object serviceImpl, URL location, List handlerInfos, Map seiPortNameToFactoryMap, Map seiClassNameToFactoryMap, String credentialsName) throws DeploymentException {
        Class serviceEndpointInterface;
        SEIFactory seiFactory;
        //lightweight jaxrpc mapping supplied
        serviceEndpointInterface = getServiceEndpointInterfaceLightweight(portType, mapping, classLoader);
        Class enhancedServiceEndpointClass = enhanceServiceEndpointInterface(serviceEndpointInterface, context, module, classLoader);

        int i = 0;
        for (Iterator ops = operations.iterator(); ops.hasNext();) {
            Operation operation = (Operation) ops.next();
            Method method = WSDescriptorParser.getMethodForOperation(enhancedServiceEndpointClass, operation);
            BindingOperation bindingOperation = binding.getBindingOperation(operation.getName(), operation.getInput().getName(), operation.getOutput() == null ? null : operation.getOutput().getName());
            OperationInfo operationInfo = buildOperationInfoLightweight(method, bindingOperation, portStyle, soapVersion);
            operationInfos[i++] = operationInfo;
        }
        List typeMappings = new ArrayList();
        Map typeDescriptors = new HashMap();
        buildTypeInfoLightWeight(schemaTypeKeyToSchemaTypeMap, mapping, classLoader, typeMappings, typeDescriptors);
        seiFactory = createSEIFactory(serviceName, portName, enhancedServiceEndpointClass, serviceImpl, typeMappings, typeDescriptors, location, operationInfos, handlerInfos, credentialsName, context, classLoader);
        seiPortNameToFactoryMap.put(portName, seiFactory);
        seiClassNameToFactoryMap.put(serviceEndpointInterface.getName(), seiFactory);
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

    public static void buildTypeInfoHeavyweight(JavaXmlTypeMappingType[] javaXmlTypeMappings, Map schemaTypeKeyToSchemaTypeMap, ClassLoader classLoader, List typeMappings, Map typeDescriptors) throws DeploymentException {
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
            boolean isElement = javaXmlTypeMapping.getQnameScope().getStringValue().equals("element");
            boolean isSimpleType = javaXmlTypeMapping.getQnameScope().getStringValue().equals("simpleType");
            if (javaXmlTypeMapping.isSetRootTypeQname()) {
                typeName = javaXmlTypeMapping.getRootTypeQname().getQNameValue();
                key = new SchemaTypeKey(typeName, isElement, isSimpleType, false);
            } else if (javaXmlTypeMapping.isSetAnonymousTypeQname()) {
                String anonTypeQNameString = javaXmlTypeMapping.getAnonymousTypeQname().getStringValue();
                int pos = anonTypeQNameString.lastIndexOf(":");
                if (pos == -1) {
                    throw new DeploymentException("anon QName is invalid, no final ':' " + anonTypeQNameString);
                }
                //this appears to be ignored...
                typeName = new QName(anonTypeQNameString.substring(0, pos), anonTypeQNameString.substring(pos + 1));
                key = new SchemaTypeKey(typeName, isElement, isSimpleType, true);
            } else {
                throw new DeploymentException("either root type qname or anonymous type qname must be set");
            }
            SchemaType schemaType = (SchemaType) schemaTypeKeyToSchemaTypeMap.get(key);
            if (schemaType == null) {
                throw new DeploymentException("Schema type key " + key + " not found in analyzed schema: " + schemaTypeKeyToSchemaTypeMap);
            }
            TypeDesc typeDesc = getTypeDescriptor(clazz, typeName, javaXmlTypeMapping, schemaType);

            TypeMappingInfo typeMappingInfo = new TypeMappingInfo(clazz, typeName, serializerFactoryClass, deserializerFactoryClass);
            typeMappings.add(typeMappingInfo);
            typeDescriptors.put(clazz, typeDesc);
        }
    }

    private static TypeDesc getTypeDescriptor(Class javaClass, QName typeQName, JavaXmlTypeMappingType javaXmlTypeMapping, SchemaType schemaType) throws DeploymentException {
        return TypeDescBuilder.getTypeDescriptor(javaClass, typeQName, javaXmlTypeMapping, schemaType);
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


    public SEIFactory createSEIFactory(QName serviceName, String portName, Class enhancedServiceEndpointClass, Object serviceImpl, List typeMappings, Map typeDescriptors, URL location, OperationInfo[] operationInfos, List handlerInfoInfos, String credentialsName, DeploymentContext deploymentContext, ClassLoader classLoader) throws DeploymentException {
        List handlerInfos = buildHandlerInfosForPort(portName, handlerInfoInfos);
        try {
            SEIFactory factory = new SEIFactoryImpl(serviceName, portName, enhancedServiceEndpointClass, operationInfos, serviceImpl, typeMappings, typeDescriptors, location, handlerInfos, classLoader, credentialsName);
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
        LightweightOperationDescBuilder operationDescBuilder = new LightweightOperationDescBuilder(bindingOperation, method);
        return operationDescBuilder.buildOperationInfo(soapVersion);
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
        infoBuilder.addInterface(POJOWebServiceBuilder.class);

        GBEAN_INFO = infoBuilder.getBeanInfo();
    }

    public static GBeanInfo getGBeanInfo() {
        return GBEAN_INFO;
    }

}
