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
import java.io.InputStream;
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
import java.util.jar.JarFile;
import javax.wsdl.Binding;
import javax.wsdl.BindingInput;
import javax.wsdl.BindingOperation;
import javax.wsdl.Definition;
import javax.wsdl.Message;
import javax.wsdl.Operation;
import javax.wsdl.Part;
import javax.wsdl.Port;
import javax.wsdl.PortType;
import javax.wsdl.WSDLException;
import javax.wsdl.extensions.ExtensibilityElement;
import javax.wsdl.extensions.soap.SOAPAddress;
import javax.wsdl.extensions.soap.SOAPBinding;
import javax.wsdl.extensions.soap.SOAPBody;
import javax.wsdl.extensions.soap.SOAPOperation;
import javax.wsdl.factory.WSDLFactory;
import javax.wsdl.xml.WSDLLocator;
import javax.wsdl.xml.WSDLReader;
import javax.xml.namespace.QName;

import net.sf.cglib.core.DefaultGeneratorStrategy;
import net.sf.cglib.proxy.Callback;
import net.sf.cglib.proxy.Enhancer;
import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.NoOp;
import net.sf.cglib.reflect.FastClass;
import net.sf.cglib.reflect.FastConstructor;
import org.apache.axis.client.Service;
import org.apache.axis.description.OperationDesc;
import org.apache.axis.description.ParameterDesc;
import org.apache.axis.encoding.ser.ArrayDeserializerFactory;
import org.apache.axis.encoding.ser.ArraySerializerFactory;
import org.apache.axis.encoding.ser.BeanDeserializerFactory;
import org.apache.axis.encoding.ser.BeanSerializerFactory;
import org.apache.axis.enum.Style;
import org.apache.axis.enum.Use;
import org.apache.axis.soap.SOAPConstants;
import org.apache.geronimo.axis.client.GenericServiceEndpointWrapper;
import org.apache.geronimo.axis.client.NoOverrideCallbackFilter;
import org.apache.geronimo.axis.client.OperationInfo;
import org.apache.geronimo.axis.client.SEIFactory;
import org.apache.geronimo.axis.client.SEIFactoryImpl;
import org.apache.geronimo.axis.client.SerializableNoOp;
import org.apache.geronimo.axis.client.ServiceImpl;
import org.apache.geronimo.axis.client.ServiceMethodInterceptor;
import org.apache.geronimo.axis.client.ServiceReference;
import org.apache.geronimo.axis.client.TypeMappingInfo;
import org.apache.geronimo.common.DeploymentException;
import org.apache.geronimo.deployment.DeploymentContext;
import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.gbean.GBeanInfoBuilder;
import org.apache.geronimo.j2ee.deployment.Module;
import org.apache.geronimo.j2ee.deployment.ServiceReferenceBuilder;
import org.apache.geronimo.j2ee.j2eeobjectnames.NameFactory;
import org.apache.geronimo.naming.reference.DeserializingReference;
import org.apache.geronimo.xbeans.j2ee.JavaWsdlMappingDocument;
import org.apache.geronimo.xbeans.j2ee.JavaWsdlMappingType;
import org.apache.geronimo.xbeans.j2ee.JavaXmlTypeMappingType;
import org.apache.geronimo.xbeans.j2ee.MethodParamPartsMappingType;
import org.apache.geronimo.xbeans.j2ee.PackageMappingType;
import org.apache.geronimo.xbeans.j2ee.ServiceEndpointInterfaceMappingType;
import org.apache.geronimo.xbeans.j2ee.ServiceEndpointMethodMappingType;
import org.apache.geronimo.xbeans.j2ee.WsdlMessageMappingType;
import org.apache.geronimo.xbeans.j2ee.WsdlReturnValueMappingType;
import org.apache.geronimo.kernel.ClassLoading;
import org.apache.xmlbeans.XmlException;
import org.objectweb.asm.Type;
import org.xml.sax.InputSource;

/**
 * @version $Rev:  $ $Date:  $
 */
public class AxisBuilder implements ServiceReferenceBuilder {
    private static final Class[] SERVICE_CONSTRUCTOR_TYPES = new Class[]{Map.class, Map.class};

    private static final URI ENHANCED_LOCATION = URI.create("cglib/");
    private static final SOAPConstants SOAP_VERSION = SOAPConstants.SOAP11_CONSTANTS;

    public ServiceReference createServiceReference(Class serviceInterface, URI wsdlURI, URI jaxrpcMappingURI, QName serviceQName, Map portComponentRefMap, List handlers, DeploymentContext deploymentContext, ClassLoader classLoader) throws DeploymentException {

        Enhancer enhancer = new Enhancer();
        enhancer.setClassLoader(classLoader);
        enhancer.setSuperclass(ServiceImpl.class);
        enhancer.setInterfaces(new Class[]{serviceInterface});
        enhancer.setCallbackFilter(new NoOverrideCallbackFilter(Service.class));
        enhancer.setCallbackTypes(new Class[]{NoOp.class, MethodInterceptor.class});
        enhancer.setUseFactory(false);
        ByteArrayRetrievingGeneratorStrategy strategy = new ByteArrayRetrievingGeneratorStrategy();
        enhancer.setStrategy(strategy);
        Class enhanced = enhancer.createClass();

        saveClass(deploymentContext, enhanced.getName(), strategy.getClassBytes());
        return new ServiceReference(enhanced, null, null, null);
    }

    public Object createService(Class serviceInterface, URI wsdlURI, URI jaxrpcMappingURI, QName serviceQName, Map portComponentRefMap, List handlers, DeploymentContext deploymentContext, Module module, ClassLoader classLoader) throws DeploymentException {
        JarFile moduleFile = module.getModuleFile();
        Definition definition = null;
        JavaWsdlMappingType mapping = null;
        if (wsdlURI != null) {
            JarWSDLLocator wsdlLocator = new JarWSDLLocator(moduleFile, wsdlURI);
            WSDLFactory wsdlFactory = null;
            try {
                wsdlFactory = WSDLFactory.newInstance();
            } catch (WSDLException e) {
                throw new DeploymentException("Could not create WSDLFactory", e);
            }
            WSDLReader wsdlReader = wsdlFactory.newWSDLReader();
            definition = null;
            try {
                definition = wsdlReader.readWSDL(wsdlLocator);
            } catch (WSDLException e) {
                throw new DeploymentException("Failed to read wsdl document", e);
            }

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
        }

        Object service = createService(serviceInterface, definition, mapping, serviceQName, SOAP_VERSION, deploymentContext, module, classLoader);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = null;
        try {
            oos = new ObjectOutputStream(baos);
            oos.writeObject(service);
            oos.flush();
        } catch (IOException e) {
            throw new DeploymentException("Could not create serialize service", e);
        }
        byte[] bytes = baos.toByteArray();
        DeserializingReference reference = new DeserializingReference(bytes);
        return reference;
    }

    public Object createService(Class serviceInterface, Definition definition, JavaWsdlMappingType mapping, QName serviceQName, SOAPConstants soapVersion, DeploymentContext context, Module module, ClassLoader classloader) throws DeploymentException {
        Map seiPortNameToFactoryMap = new HashMap();
        Map seiClassNameToFactoryMap = new HashMap();
        Object serviceInstance = createService(serviceInterface, seiPortNameToFactoryMap, seiClassNameToFactoryMap, context, module, classloader);
        if (definition != null) {
            buildSEIFactoryMap(serviceInterface, definition, mapping, serviceQName, soapVersion, seiPortNameToFactoryMap, seiClassNameToFactoryMap, serviceInstance, context, module, classloader);
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

    public void buildSEIFactoryMap(Class serviceInterface, Definition definition, JavaWsdlMappingType mapping, QName serviceQName, SOAPConstants soapVersion, Map seiPortNameToFactoryMap, Map seiClassNameToFactoryMap, Object serviceImpl, DeploymentContext context, Module module, ClassLoader classLoader) throws DeploymentException {

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

        Map wsdlPortMap = service.getPorts();

        for (Iterator iterator = wsdlPortMap.entrySet().iterator(); iterator.hasNext();) {
            Map.Entry entry = (Map.Entry) iterator.next();
            String portName = (String) entry.getKey();
            Port port = (Port) entry.getValue();

            SOAPAddress soapAddress = (SOAPAddress) getExtensibilityElement(SOAPAddress.class, port.getExtensibilityElements());
            String locationURIString = soapAddress.getLocationURI();
            URL location = null;
            try {
                location = new URL(locationURIString);
            } catch (MalformedURLException e) {
                throw new DeploymentException("Could not construct web service location URL from " + locationURIString);
            }

            Binding binding = port.getBinding();
            SOAPBinding soapBinding = (SOAPBinding) getExtensibilityElement(SOAPBinding.class, binding.getExtensibilityElements());
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
                seiFactory = createSEIFactory(enhancedServiceEndpointClass, serviceImpl, typeMappings, location, operationInfos, context, classLoader);
            } else {
                //complete jaxrpc mapping file supplied
                QName portTypeQName = portType.getQName();
                ServiceEndpointInterfaceMappingType endpointMapping = getServiceEndpointInterfaceMapping(endpointMappings, portTypeQName);
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
                    BindingOperation bindingOperation = binding.getBindingOperation(operation.getName(), operation.getInput().getName(), operation.getOutput() == null ? null : operation.getOutput().getName());
                    ServiceEndpointMethodMappingType methodMapping = getMethodMappingForOperation(operation, methodMappings);
                    OperationInfo operationInfo = buildOperationInfoHeavyweight(methodMapping, bindingOperation, portStyle, soapVersion, mapping, classLoader);
                    operationInfos[i++] = operationInfo;
                }
                JavaXmlTypeMappingType[] javaXmlTypeMappings = mapping.getJavaXmlTypeMappingArray();
                List typeMappings = new ArrayList();
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

                    TypeMappingInfo typeMappingInfo = null;
                    if (javaXmlTypeMapping.isSetRootTypeQname()) {
                        QName typeName = javaXmlTypeMapping.getRootTypeQname().getQNameValue();
                        typeMappingInfo = new TypeMappingInfo(clazz, typeName, serializerFactoryClass, deserializerFactoryClass);
                    } else if (javaXmlTypeMapping.isSetAnonymousTypeQname()) {
                        String anonTypeQNameString = javaXmlTypeMapping.getAnonymousTypeQname().getStringValue();
                        int pos = anonTypeQNameString.lastIndexOf(":");
                        if (pos == -1) {
                            throw new DeploymentException("anon QName is invalid, no final ':' " + anonTypeQNameString);
                        }
                        //this appears to be ignored...
                        QName typeName = new QName(anonTypeQNameString.substring(0, pos), anonTypeQNameString.substring(pos + 1));
                        typeMappingInfo = new TypeMappingInfo(clazz, typeName, serializerFactoryClass, deserializerFactoryClass);
                    }
                    typeMappings.add(typeMappingInfo);


                }
                seiFactory = createSEIFactory(enhancedServiceEndpointClass, serviceImpl, typeMappings, location, operationInfos, context, classLoader);
            }
            seiPortNameToFactoryMap.put(portName, seiFactory);
            seiClassNameToFactoryMap.put(serviceEndpointInterface.getName(), seiFactory);
        }
    }

    private ServiceEndpointMethodMappingType getMethodMappingForOperation(Operation operation, ServiceEndpointMethodMappingType[] methodMappings) throws DeploymentException {
        String operationName = operation.getName();
        for (int i = 0; i < methodMappings.length; i++) {
            ServiceEndpointMethodMappingType methodMapping = methodMappings[i];
            if (operationName.equals(methodMapping.getWsdlOperation().getStringValue())) {
                return methodMapping;
            }
        }
        throw new DeploymentException("No method found for operation named " + operationName);
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
//        PortType portType = port.getBinding().getPortType();
        QName portTypeQName = portType.getQName();
        String portTypeNamespace = portTypeQName.getNamespaceURI();
        String portTypePackage = getPackageFromNamespace(portTypeNamespace, mappings);
        StringBuffer shortInterfaceName = new StringBuffer(portTypeQName.getLocalPart());
        shortInterfaceName.setCharAt(0, Character.toUpperCase(shortInterfaceName.charAt(0)));
        //TODO just use one buffer!
        String fqcn = portTypePackage + "." + shortInterfaceName.toString();
        try {
            return classLoader.loadClass(fqcn);
        } catch (ClassNotFoundException e) {
            throw new DeploymentException("Could not load service endpoint interface type", e);
        }

//        Method[] methods = serviceInterface.getMethods();
//        String methodName = "get" + port.getName();
//        String serviceEndpointInterfaceShortName = port.getBinding().getPortType().getQName().getLocalPart();
//        for (int i = 0; i < methods.length; i++) {
//            Method method = methods[i];
//            if (method.getName().equals(methodName)) {
//                Class serviceEndpointInterface = method.getReturnType();
//                String longName = serviceEndpointInterface.getName();
//                String name = longName.substring(longName.lastIndexOf('.') + 1);
//                if (!name.equals(serviceEndpointInterfaceShortName) &&
//                        !Introspector.decapitalize(name).equals(serviceEndpointInterfaceShortName)) {
//                    throw new DeploymentException("unexpected name for service endpoint interface, expected ending: " + serviceEndpointInterfaceShortName + ", found " + serviceEndpointInterface.getName());
//                }
//                return serviceEndpointInterface;
//            }
//        }
//        throw new DeploymentException("Could not find service endpoint interface for port named " + port.getName());
    }

    private ServiceEndpointInterfaceMappingType getServiceEndpointInterfaceMapping(ServiceEndpointInterfaceMappingType[] endpointMappings, QName portTypeQName) throws DeploymentException {
        for (int i = 0; i < endpointMappings.length; i++) {
            ServiceEndpointInterfaceMappingType endpointMapping = endpointMappings[i];
            QName testPortQName = endpointMapping.getWsdlPortType().getQNameValue();
            if (portTypeQName.equals(testPortQName)) {
                return endpointMapping;
            }
        }
        throw new DeploymentException("Could not find service endpoint interface for port named " + portTypeQName);
    }

    private ExtensibilityElement getExtensibilityElement(Class clazz, List extensibilityElements) throws DeploymentException {
        for (Iterator iterator = extensibilityElements.iterator(); iterator.hasNext();) {
            ExtensibilityElement extensibilityElement = (ExtensibilityElement) iterator.next();
            if (clazz.isAssignableFrom(extensibilityElement.getClass())) {
                return extensibilityElement;
            }
        }
        throw new DeploymentException("No element of class " + clazz.getName() + " found");
    }

//    private String getNamespaceFromPackage(String packageName, JavaWsdlMappingType mapping) throws DeploymentException {
//        PackageMappingType[] packageMappings = mapping.getPackageMappingArray();
//        for (int i = 0; i < packageMappings.length; i++) {
//            PackageMappingType packageMapping = packageMappings[i];
//            if (packageName.equals(packageMapping.getPackageType().getStringValue().trim())) {
//                return packageMapping.getNamespaceURI().getStringValue().trim();
//            }
//        }
//        throw new DeploymentException("Package " + packageName + " was not mapped in jaxrpc mapping file");
//    }

    private String getPackageFromNamespace(String namespace, JavaWsdlMappingType mapping) throws DeploymentException {
        PackageMappingType[] packageMappings = mapping.getPackageMappingArray();
        for (int i = 0; i < packageMappings.length; i++) {
            PackageMappingType packageMapping = packageMappings[i];
            if (namespace.equals(packageMapping.getNamespaceURI().getStringValue().trim())) {
                return packageMapping.getPackageType().getStringValue().trim();
            }
        }
        throw new DeploymentException("Namespace " + namespace + " was not mapped in jaxrpc mapping file");
    }

    public SEIFactory createSEIFactory(Class enhancedServiceEndpointClass, Object serviceImpl, List typeMappings, URL location, OperationInfo[] operationInfos, DeploymentContext deploymentContext, ClassLoader classLoader) throws DeploymentException {

        try {
            SEIFactory factory = new SEIFactoryImpl(enhancedServiceEndpointClass, operationInfos, serviceImpl, typeMappings, location, classLoader);
            return factory;
        } catch (ClassNotFoundException e) {
            throw new DeploymentException("Could not load GenericServiceEndpoint from application classloader", e);
        }
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
        Map inputParts = inputMessage.getParts();
        if (methodParamTypes.length != inputParts.size()) {
            throw new DeploymentException("mismatch in parameter counts: method has " + methodParamTypes.length + " whereas the input message has " + inputParts.size());
        }
        int i = 0;
        for (Iterator parts = inputParts.entrySet().iterator(); parts.hasNext();) {
            //TODO HOW IS THIS SUPPOSED TO WORK????? is the map ordered?
            Map.Entry entry = (Map.Entry) parts.next();
            String partName = (String) entry.getKey();
            Part part = (Part) entry.getValue();
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

        SOAPOperation soapOperation = (SOAPOperation) getExtensibilityElement(SOAPOperation.class, bindingOperation.getExtensibilityElements());
        String soapActionURI = soapOperation.getSoapActionURI();
        String styleString = soapOperation.getStyle();
        Style style = Style.getStyle(styleString, defaultStyle);
        BindingInput bindingInput = bindingOperation.getBindingInput();
        SOAPBody soapBody = (SOAPBody) getExtensibilityElement(SOAPBody.class, bindingInput.getExtensibilityElements());
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

    public OperationInfo buildOperationInfoHeavyweight(ServiceEndpointMethodMappingType methodMapping, BindingOperation bindingOperation, Style defaultStyle, SOAPConstants soapVersion, JavaWsdlMappingType mapping, ClassLoader classLoader) throws DeploymentException {

        //TODO how can bindingOperation be null?
        Operation operation = bindingOperation.getOperation();
        String operationName = operation.getName();


        Message inputMessage = operation.getInput().getMessage();

        boolean isWrappedElement = methodMapping.isSetWrappedElement();

        MethodParamPartsMappingType[] paramMappings = methodMapping.getMethodParamPartsMappingArray();
        Type[] parameterASMTypes = new Type[paramMappings.length];
        ParameterDesc[] parameterDescriptions = new ParameterDesc[paramMappings.length];
        for (int i = 0; i < paramMappings.length; i++) {
            MethodParamPartsMappingType paramMapping = paramMappings[i];
            int position = paramMapping.getParamPosition().getBigIntegerValue().intValue();
            String paramJavaTypeName = paramMapping.getParamType().getStringValue().trim();
            Class paramJavaType = null;
            try {
                paramJavaType = ClassLoading.loadClass(paramJavaTypeName, classLoader);
            } catch (ClassNotFoundException e) {
                throw new DeploymentException("could not load parameter type", e);
            }

            WsdlMessageMappingType wsdlMessageMappingType = paramMapping.getWsdlMessageMapping();
            String parameterMode = wsdlMessageMappingType.getParameterMode().getStringValue().trim();
            byte mode = ParameterDesc.modeFromString(parameterMode);
            boolean isSoapHeader = wsdlMessageMappingType.isSetSoapHeader();
            boolean inHeader = isSoapHeader && (mode == ParameterDesc.IN || mode == ParameterDesc.INOUT);
            boolean outHeader = isSoapHeader && (mode == ParameterDesc.OUT || mode == ParameterDesc.INOUT);
            QName wsdlMessageQName = wsdlMessageMappingType.getWsdlMessage().getQNameValue();
            String wsdlMessagePartName = wsdlMessageMappingType.getWsdlMessagePartName().getStringValue().trim();


            //TODO Must this be the inputMessage? Then we don't need definition.
            if (!wsdlMessageQName.equals(inputMessage.getQName())) {
                throw new DeploymentException("QName of input message: " + inputMessage.getQName() + " does not match mapping message QName: " + wsdlMessageQName);
            }
//            Message inputMessage = definition.getMessage(wsdlMessageQName);
            Part part = inputMessage.getPart(wsdlMessagePartName);
            //TODO this makes little sense but may be correct, see comments in axis Parameter class
            //the part name qname is really odd.
            QName partQName = isWrappedElement? part.getElementName(): new QName("", part.getName());
            QName partTypeQName = part.getTypeName();

            ParameterDesc parameterDesc = new ParameterDesc(partQName, mode, partTypeQName, paramJavaType, inHeader, outHeader);
            parameterDescriptions[position] = parameterDesc;
            parameterASMTypes[position] = Type.getType(paramJavaType);
        }


        QName returnType = null;
        QName returnQName = null;
        Class returnClass = null;
        Type returnASMType = Type.VOID_TYPE;

        Message outputMessage = operation.getOutput() == null ? null : operation.getOutput().getMessage();
        if (methodMapping.isSetWsdlReturnValueMapping()) {
            WsdlReturnValueMappingType wsdlReturnValueMapping = methodMapping.getWsdlReturnValueMapping();
            String returnClassName = wsdlReturnValueMapping.getMethodReturnValue().getStringValue().trim();
            try {
                returnClass = ClassLoading.loadClass(returnClassName, classLoader);
            } catch (ClassNotFoundException e) {
                throw new DeploymentException("Could not load return type", e);
            }

            QName wsdlMessageQName = wsdlReturnValueMapping.getWsdlMessage().getQNameValue();

            if (outputMessage == null) {
                throw new DeploymentException("No output message, but a mapping for it!");
            }
            if (!wsdlMessageQName.equals(outputMessage.getQName())) {
                throw new DeploymentException("OutputMessage has QName: " + outputMessage.getQName() + " but mapping specifies: " + wsdlMessageQName);
            }

            if (wsdlReturnValueMapping.isSetWsdlMessagePartName()) {
                String wsdlMessagePartName = wsdlReturnValueMapping.getWsdlMessagePartName().getStringValue().trim();
                Part part = outputMessage.getPart(wsdlMessagePartName);
                returnQName = part.getElementName();
                returnType = part.getTypeName();
            }

            returnASMType = Type.getType(returnClass);

        }

        OperationDesc operationDesc = new OperationDesc(operationName, parameterDescriptions, returnQName);
        operationDesc.setReturnType(returnType);
        operationDesc.setReturnClass(returnClass);

        SOAPOperation soapOperation = (SOAPOperation) getExtensibilityElement(SOAPOperation.class, bindingOperation.getExtensibilityElements());
        String soapActionURI = soapOperation.getSoapActionURI();
        String styleString = soapOperation.getStyle();
        Style style = Style.getStyle(styleString, defaultStyle);
        BindingInput bindingInput = bindingOperation.getBindingInput();
        SOAPBody soapBody = (SOAPBody) getExtensibilityElement(SOAPBody.class, bindingInput.getExtensibilityElements());
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

        String methodName = methodMapping.getJavaMethodName().getStringValue().trim();
        String methodDesc = Type.getMethodDescriptor(returnASMType, parameterASMTypes);
        OperationInfo operationInfo = new OperationInfo(operationDesc, usesSOAPAction, soapActionURI, soapVersion, operationQName, methodName, methodDesc);
        return operationInfo;
    }


    private void saveClass(DeploymentContext deploymentContext, String className, byte[] classBytes) throws DeploymentException {
        try {
            deploymentContext.addClass(ENHANCED_LOCATION, className, classBytes, true);
        } catch (IOException e) {
            throw new DeploymentException("Could not save enhanced class bytes", e);
        } catch (URISyntaxException e) {
            throw new DeploymentException("Could not construct URI for class file", e);
        }
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

    public static final GBeanInfo GBEAN_INFO;

    static {
        GBeanInfoBuilder infoBuilder = new GBeanInfoBuilder(AxisBuilder.class, NameFactory.MODULE_BUILDER);
        infoBuilder.addInterface(ServiceReferenceBuilder.class);

        GBEAN_INFO = infoBuilder.getBeanInfo();
    }

    public static GBeanInfo getGBeanInfo() {
        return GBEAN_INFO;
    }

}
