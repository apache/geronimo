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
import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Calendar;
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
import javax.wsdl.Message;
import javax.wsdl.Operation;
import javax.wsdl.Part;
import javax.wsdl.Port;
import javax.wsdl.PortType;
import javax.wsdl.Types;
import javax.wsdl.WSDLException;
import javax.wsdl.extensions.ExtensibilityElement;
import javax.wsdl.extensions.UnknownExtensibilityElement;
import javax.wsdl.extensions.soap.SOAPAddress;
import javax.wsdl.extensions.soap.SOAPBinding;
import javax.wsdl.extensions.soap.SOAPBody;
import javax.wsdl.extensions.soap.SOAPOperation;
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
import org.apache.geronimo.kernel.ClassLoading;
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
import org.apache.geronimo.schema.SchemaConversionUtils;
import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlObject;
import org.apache.xmlbeans.XmlCursor;
import org.objectweb.asm.Type;
import org.w3.x2001.xmlSchema.SchemaDocument;
import org.w3.x2001.xmlSchema.ComplexType;
import org.w3c.dom.Element;
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

        Map complexTypeMap = getComplexTypesInWsdl(definition);

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
                    OperationInfo operationInfo = buildOperationInfoHeavyweight(methodMapping, bindingOperation, portStyle, soapVersion, complexTypeMap, mapping, classLoader);
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
        //TODO investigate getOrderedParts
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

    public OperationInfo buildOperationInfoHeavyweight(ServiceEndpointMethodMappingType methodMapping, BindingOperation bindingOperation, Style defaultStyle, SOAPConstants soapVersion, Map complexTypeMap, JavaWsdlMappingType mapping, ClassLoader classLoader) throws DeploymentException {

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
            Class actualParamJavaType = getHolderType(paramJavaTypeName, mode, partTypeQName, isComplexType, mapping, classLoader);

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

    Map getComplexTypesInWsdl(Definition definition) throws DeploymentException {
        Map complexTypeMap = new HashMap();
        Types types = definition.getTypes();
        Map namespaceMap = definition.getNamespaces();
        if (types != null) {
            List schemas = types.getExtensibilityElements();
            for (Iterator iterator = schemas.iterator(); iterator.hasNext();) {
                Object o = iterator.next();
                if (o instanceof UnknownExtensibilityElement) {
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
                                    Map.Entry entry =  (Map.Entry) namespaces.next();
                                    cursor.insertNamespace((String)entry.getKey(), (String)entry.getValue());
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

    private Class getHolderType(String paramJavaTypeName, byte mode, QName typeQName, boolean isComplexType, JavaWsdlMappingType mapping, ClassLoader classLoader) throws DeploymentException {
        Class paramJavaType = null;
        if (mode == ParameterDesc.IN) {
            try {
                paramJavaType = ClassLoading.loadClass(paramJavaTypeName, classLoader);
            } catch (ClassNotFoundException e) {
                throw new DeploymentException("could not load parameter type", e);
            }
            return paramJavaType;
        } else {
            String holderName;
            if (isComplexType) {
                String namespace = typeQName.getNamespaceURI();
                 String packageName = getPackageFromNamespace(namespace, mapping);
                 StringBuffer buf = new StringBuffer(packageName.length() + typeQName.getLocalPart().length() + 14);
                 buf.append(packageName).append(".holders.").append(typeQName.getLocalPart()).append("Holder");
                 buf.setCharAt(packageName.length() + 9, Character.toUpperCase(typeQName.getLocalPart().charAt(0)));
                 holderName = buf.toString();
            } else {
                try {
                    paramJavaType = ClassLoading.loadClass(paramJavaTypeName, classLoader);
                } catch (ClassNotFoundException e) {
                    throw new DeploymentException("could not load parameter type", e);
                }
                Class holder = (Class) rpcHolderClasses.get(paramJavaType);
                if (holder != null) {
                    return holder;
                }
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
