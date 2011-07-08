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

import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.jar.JarFile;

import javax.jws.WebService;
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
import javax.xml.ws.WebServiceProvider;

import org.apache.axis.constants.Style;
import org.apache.axis.description.JavaServiceDesc;
import org.apache.axis.description.OperationDesc;
import org.apache.axis.handlers.HandlerInfoChainFactory;
import org.apache.axis.handlers.soap.SOAPService;
import org.apache.axis.providers.java.RPCProvider;
import org.apache.axis.soap.SOAPConstants;
import org.apache.geronimo.axis.client.AxisServiceReference;
import org.apache.geronimo.axis.client.OperationInfo;
import org.apache.geronimo.axis.client.SEIFactory;
import org.apache.geronimo.axis.client.SEIFactoryImpl;
import org.apache.geronimo.axis.server.AxisWebServiceContainer;
import org.apache.geronimo.axis.server.POJOProvider;
import org.apache.geronimo.axis.server.ServiceInfo;
import org.apache.geronimo.common.DeploymentException;
import org.apache.geronimo.deployment.DeploymentContext;
import org.apache.geronimo.deployment.service.EnvironmentBuilder;
import org.apache.geronimo.gbean.AbstractName;
import org.apache.geronimo.gbean.GBeanData;
import org.apache.geronimo.gbean.GBeanInfoBuilder;
import org.apache.geronimo.gbean.annotation.GBean;
import org.apache.geronimo.gbean.annotation.ParamAttribute;
import org.apache.geronimo.j2ee.deployment.HandlerInfoInfo;
import org.apache.geronimo.j2ee.deployment.Module;
import org.apache.geronimo.j2ee.deployment.WebModule;
import org.apache.geronimo.j2ee.deployment.WebServiceBuilder;
import org.apache.geronimo.j2ee.j2eeobjectnames.NameFactory;
import org.apache.geronimo.kernel.GBeanAlreadyExistsException;
import org.apache.geronimo.kernel.repository.Environment;
import org.apache.geronimo.webservices.SerializableWebServiceContainerFactoryGBean;
import org.apache.geronimo.webservices.builder.DescriptorVersion;
import org.apache.geronimo.webservices.builder.PortInfo;
import org.apache.geronimo.webservices.builder.SchemaInfoBuilder;
import org.apache.geronimo.webservices.builder.WSDescriptorParser;
import org.apache.geronimo.xbeans.geronimo.naming.GerPortCompletionType;
import org.apache.geronimo.xbeans.geronimo.naming.GerPortType;
import org.apache.geronimo.xbeans.geronimo.naming.GerServiceCompletionType;
import org.apache.geronimo.xbeans.geronimo.naming.GerServiceRefType;
import org.apache.openejb.jee.JavaWsdlMapping;
import org.apache.openejb.jee.JavaXmlTypeMapping;
import org.apache.openejb.jee.ServiceEndpointInterfaceMapping;
import org.apache.openejb.jee.ServiceEndpointMethodMapping;
import org.apache.xbean.osgi.bundle.util.BundleClassLoader;
import org.osgi.framework.Bundle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @version $Rev$ $Date$
 */
@GBean(j2eeType = NameFactory.MODULE_BUILDER)
public class AxisBuilder implements WebServiceBuilder {

    private static final Logger LOG = LoggerFactory.getLogger(AxisBuilder.class);

    private static final SOAPConstants SOAP_VERSION = SOAPConstants.SOAP11_CONSTANTS;

    private final Environment defaultEnvironment;
    private static final String KEY = AxisBuilder.class.getName();

    public AxisBuilder(@ParamAttribute(name = "defaultEnvironment") Environment defaultEnvironment) {
        this.defaultEnvironment = defaultEnvironment;
    }

    @Override
    public void findWebServices(Module module, boolean isEJB, Map servletLocations, Environment environment, Map sharedContext) throws DeploymentException {
        final String path = isEJB ? "META-INF/webservices.xml" : "WEB-INF/webservices.xml";

        URL wsDDUrl = module.getDeployable().getResource(path);
        if (wsDDUrl != null) {
            // "JarFile moduleFile" is useless in parseWebServiceDescriptor, so we pass "null" to it.
            Map<String, PortInfo> portMap = WSDescriptorParser.parseWebServiceDescriptor(wsDDUrl, null, isEJB, servletLocations);
            if (portMap != null) {
                if (defaultEnvironment != null) {
                    EnvironmentBuilder.mergeEnvironments(environment, defaultEnvironment);
                }
                sharedContext.put(KEY, portMap);
            } else {
                sharedContext.put(KEY, Collections.EMPTY_MAP);
            }
        }
    }

    @Override
    public boolean configurePOJO(GBeanData targetGBean, String servletName, Module module, String servletClassName, DeploymentContext context) throws DeploymentException {
        Map sharedContext = ((WebModule) module).getSharedContext();
        Map<String, PortInfo> portInfoMap = (Map<String, PortInfo>) sharedContext.get(KEY);

        if(portInfoMap == null) {
            return false;
        }
        PortInfo portInfo = portInfoMap.get(servletName);
        if (portInfo == null) {
            //not ours
            return false;
        }

        Bundle bundle = context.getDeploymentBundle();
        Class<?> serviceClass = loadClass(servletClassName, bundle);
        if (isJAXWSWebService(serviceClass)) {
            if (DescriptorVersion.J2EE.equals(portInfo.getDescriptorVersion())) {
                // This is a JAX-WS web service in J2EE descriptor so throw an exception
                throw new DeploymentException("JAX-WS web service '" + portInfo.getPortComponentName()
                                              + "' cannot be specified in J2EE webservices.xml descriptor.");
            } else {
                // This is a JAX-WS web service in JAVAEE descriptor so ignore
                return false;
            }
        }

        portInfo.initialize(module.getModuleFile());

        if (LOG.isDebugEnabled()) {
            LOG.debug("Publishing JAX-RPC '" + portInfo.getPortComponentName() + "' service at " + portInfo.getContextURI());
        }
        ServiceInfo serviceInfo = AxisServiceBuilder.createServiceInfo(portInfo, bundle);
        JavaServiceDesc serviceDesc = serviceInfo.getServiceDesc();

        targetGBean.setAttribute("pojoClassName", servletClassName);
        RPCProvider provider = new POJOProvider();

        SOAPService service = new SOAPService(null, provider, null);
        service.setServiceDescription(serviceDesc);
        service.setOption("className", servletClassName);

        HandlerInfoChainFactory handlerInfoChainFactory = new HandlerInfoChainFactory(serviceInfo.getHandlerInfos());
        service.setOption(org.apache.axis.Constants.ATTR_HANDLERINFOCHAIN, handlerInfoChainFactory);

        URI location;
        try {
            location = new URI(serviceDesc.getEndpointURL());
        } catch (URISyntaxException e) {
            throw new DeploymentException("Invalid webservice endpoint URI", e);
        }
        URI wsdlURI;
        try {
            wsdlURI = new URI(serviceDesc.getWSDLFile());
        } catch (URISyntaxException e) {
            throw new DeploymentException("Invalid wsdl URI", e);

        }

        AxisWebServiceContainer axisWebServiceContainer = new AxisWebServiceContainer(location, wsdlURI, service, serviceInfo.getWsdlMap(), new BundleClassLoader(bundle));
        AbstractName webServiceContainerFactoryName = context.getNaming().createChildName(targetGBean.getAbstractName(), "webServiceContainer", GBeanInfoBuilder.DEFAULT_J2EE_TYPE);
        GBeanData webServiceContainerFactoryGBean = new GBeanData(webServiceContainerFactoryName, SerializableWebServiceContainerFactoryGBean.GBEAN_INFO);
        webServiceContainerFactoryGBean.setAttribute("webServiceContainer", axisWebServiceContainer);
        try {
            context.addGBean(webServiceContainerFactoryGBean);
        } catch (GBeanAlreadyExistsException e) {
            throw new DeploymentException("Could not add webServiceContainerFactoryGBean", e);
        }
        targetGBean.setReferencePattern("WebServiceContainerFactory", webServiceContainerFactoryName);
        return true;
    }

    @Override
    public boolean configureEJB(GBeanData targetGBean, String ejbName, Module module, Map sharedContext, Bundle bundle) throws DeploymentException {

        if (sharedContext.get(KEY) == null){
            return false;
        }

        Map<String, PortInfo> portInfoMap = (Map<String, PortInfo>) sharedContext.get(KEY);

        if (portInfoMap.get(ejbName) == null) {
            //not ours
            return false;
        }

        PortInfo portInfo = portInfoMap.get(ejbName);

        String beanClassName = (String)targetGBean.getAttribute("ejbClass");
        Class<?> serviceClass = loadClass(beanClassName, bundle);
        if (isJAXWSWebService(serviceClass)) {
            if (DescriptorVersion.J2EE.equals(portInfo.getDescriptorVersion())) {
                // This is a JAX-WS web service in J2EE descriptor so throw an exception
                throw new DeploymentException("JAX-WS web service '" + portInfo.getPortComponentName()
                                              + "' cannot be specified in J2EE webservices.xml descriptor.");
            } else {
                // This is a JAX-WS web service in JAVAEE descriptor so ignore
                return false;
            }
        }

        portInfo.initialize(module.getModuleFile());

        if (LOG.isDebugEnabled()) {
            LOG.debug("Publishing EJB JAX-RPC '" + portInfo.getPortComponentName() + "' service at " + portInfo.getContextURI());
        }
        ServiceInfo serviceInfo = AxisServiceBuilder.createServiceInfo(portInfo, bundle);
        targetGBean.setAttribute("serviceInfo", serviceInfo);
        JavaServiceDesc serviceDesc = serviceInfo.getServiceDesc();
        URI location = portInfo.getContextURI();
        targetGBean.setAttribute("location", location);
        URI wsdlURI;
        try {
            wsdlURI = new URI(serviceDesc.getWSDLFile());
        } catch (URISyntaxException e) {
            throw new DeploymentException("Invalid wsdl URI", e);
        }
        targetGBean.setAttribute("wsdlURI", wsdlURI);
        return true;
    }


    //ServicereferenceBuilder
    public Object createService(Class serviceInterface, URI wsdlURI, URI jaxrpcMappingURI, QName serviceQName, Map portComponentRefMap, List handlerInfos, Object serviceRefType, Module module, Bundle bundle) throws DeploymentException {
        GerServiceRefType gerServiceRefType = (GerServiceRefType) serviceRefType;
        JarFile moduleFile = module.getModuleFile();
        SchemaInfoBuilder schemaInfoBuilder = null;
        JavaWsdlMapping mapping = null;
        if (wsdlURI != null) {
            schemaInfoBuilder = new SchemaInfoBuilder(moduleFile, wsdlURI);

            mapping = WSDescriptorParser.readJaxrpcMapping(moduleFile, jaxrpcMappingURI);
        }

        return createService(serviceInterface, schemaInfoBuilder, mapping, serviceQName, SOAP_VERSION, handlerInfos, gerServiceRefType, module, bundle);
    }

    public Object createService(Class serviceInterface, SchemaInfoBuilder schemaInfoBuilder, JavaWsdlMapping mapping, QName serviceQName, SOAPConstants soapVersion, List handlerInfos, GerServiceRefType serviceRefType, Module module, Bundle bundle) throws DeploymentException {
        Map seiPortNameToFactoryMap = new HashMap();
        Map seiClassNameToFactoryMap = new HashMap();
        if (schemaInfoBuilder != null) {
            buildSEIFactoryMap(schemaInfoBuilder, serviceRefType, mapping, handlerInfos, serviceQName, soapVersion, seiPortNameToFactoryMap, seiClassNameToFactoryMap, bundle);
        }
        return new AxisServiceReference(serviceInterface.getName(), seiPortNameToFactoryMap, seiClassNameToFactoryMap);
    }

    public void buildSEIFactoryMap(SchemaInfoBuilder schemaInfoBuilder, GerServiceRefType serviceRefType, JavaWsdlMapping mapping, List handlerInfos, QName serviceQName, SOAPConstants soapVersion, Map seiPortNameToFactoryMap, Map seiClassNameToFactoryMap, Bundle bundle) throws DeploymentException {
        Map exceptionMap = WSDescriptorParser.getExceptionMap(mapping);

        Definition definition = schemaInfoBuilder.getDefinition();
        //check for consistency
        if (definition.getServices().size() == 0) {
            //partial wsdl
            if (serviceRefType == null || !serviceRefType.isSetServiceCompletion()) {
                throw new DeploymentException("Partial wsdl, but no service completion supplied");
            }
            GerServiceCompletionType serviceCompletion = serviceRefType.getServiceCompletion();
            String serviceLocalName = serviceCompletion.getServiceName().trim();
            String namespace = definition.getTargetNamespace();
            serviceQName = new QName(namespace, serviceLocalName);
            javax.wsdl.Service service = definition.createService();
            service.setQName(serviceQName);
            GerPortCompletionType[] portCompletions = serviceCompletion.getPortCompletionArray();
            for (int i = 0; i < portCompletions.length; i++) {
                GerPortCompletionType portCompletion = portCompletions[i];
                GerPortType port = portCompletion.getPort();
                URL location = getLocation(port);
                String portName = port.getPortName().trim();
                String bindingName = portCompletion.getBindingName().trim();
                QName bindingQName = new QName(namespace, bindingName);
                Binding binding = definition.getBinding(bindingQName);
                if (binding == null) {
                    throw new DeploymentException("No binding found with qname: " + bindingQName);
                }
                String credentialsName = port.isSetCredentialsName() ? port.getCredentialsName().trim() : null;
                mapBinding(binding, mapping, serviceQName, bundle, soapVersion, schemaInfoBuilder, portName, location, handlerInfos, seiPortNameToFactoryMap, seiClassNameToFactoryMap, credentialsName, exceptionMap);

            }
        } else {
            //full wsdl
            if (serviceRefType != null && serviceRefType.isSetServiceCompletion()) {
                throw new DeploymentException("Full wsdl, but service completion supplied");
            }
            //organize the extra port info
            Map portMap = new HashMap();
            if (serviceRefType != null) {
                GerPortType[] ports = serviceRefType.getPortArray();
                for (int i = 0; i < ports.length; i++) {
                    GerPortType port = ports[i];
                    String portName = port.getPortName().trim();
                    portMap.put(portName, port);
                }
            }

            //find the service we are working with
            javax.wsdl.Service service = getService(serviceQName, schemaInfoBuilder.getDefinition());
            if (serviceQName == null) {
                serviceQName = service.getQName();
            }

            Map wsdlPortMap = service.getPorts();
            for (Iterator iterator = wsdlPortMap.entrySet().iterator(); iterator.hasNext();) {
                Map.Entry entry = (Map.Entry) iterator.next();
                String portName = (String) entry.getKey();
                Port port = (Port) entry.getValue();

                GerPortType gerPort = (GerPortType) portMap.get(portName);

                URL location = gerPort == null ? getAddressLocation(port) : getLocation(gerPort);
                //skip non-soap ports
                if (location == null) {
                    continue;
                }
                String credentialsName = gerPort == null || gerPort.getCredentialsName() == null ? null : gerPort.getCredentialsName().trim();

                Binding binding = port.getBinding();

                mapBinding(binding, mapping, serviceQName, bundle, soapVersion, schemaInfoBuilder, portName, location, handlerInfos, seiPortNameToFactoryMap, seiClassNameToFactoryMap, credentialsName, exceptionMap);
            }
        }
    }

    private void mapBinding(Binding binding, JavaWsdlMapping mapping, QName serviceQName, Bundle bundle, SOAPConstants soapVersion, SchemaInfoBuilder schemaInfoBuilder, String portName, URL location, List handlerInfos, Map seiPortNameToFactoryMap, Map seiClassNameToFactoryMap, String credentialsName, Map exceptionMap) throws DeploymentException {
        Style portStyle = getStyle(binding);

        PortType portType = binding.getPortType();

        List<ServiceEndpointInterfaceMapping> endpointMappings = new ArrayList<ServiceEndpointInterfaceMapping>(mapping.getServiceEndpointInterfaceMapping());

        //port type corresponds to SEI
        List operations = portType.getOperations();
        OperationInfo[] operationInfos = new OperationInfo[operations.size()];
        if (endpointMappings.isEmpty()) {
            doLightweightMapping(serviceQName, portType, mapping, bundle, operations, binding, portStyle, soapVersion, operationInfos, schemaInfoBuilder, portName, location, handlerInfos, seiPortNameToFactoryMap, seiClassNameToFactoryMap, credentialsName);
        } else {
            doHeavyweightMapping(serviceQName, portType, endpointMappings, bundle, operations, binding, portStyle, soapVersion, exceptionMap, schemaInfoBuilder, mapping, operationInfos, portName, location, handlerInfos, seiPortNameToFactoryMap, seiClassNameToFactoryMap, credentialsName);
        }
    }

    private URL getLocation(GerPortType port) throws DeploymentException {
        String protocol = port.getProtocol().trim();
        String host = port.getHost().trim();
        int portNum = port.getPort();
        String uri = port.getUri().trim();
        String locationURIString = protocol + "://" + host + ":" + portNum + uri;
        URL location;
        try {
            location = new URL(locationURIString);
        } catch (MalformedURLException e) {
            throw new DeploymentException("Could not construct web service location URL from " + locationURIString, e);
        }
        return location;
    }

    private javax.wsdl.Service getService(QName serviceQName, Definition definition) throws DeploymentException {
        javax.wsdl.Service service;
        if (serviceQName != null) {
            service = definition.getService(serviceQName);
            if (service == null) {
                throw new DeploymentException("No service wsdl for supplied service qname " + serviceQName);
            }
        } else {
            Map services = definition.getServices();
            if (services.size() > 1) {
                throw new DeploymentException("no serviceQName supplied, and there are " + services.size() + " services");
            }
            if (services.size() == 0) {
                throw new DeploymentException("No service in wsdl, and no service completion supplied!");
            } else {
                service = (javax.wsdl.Service) services.values().iterator().next();
            }
        }
        return service;
    }

    private Style getStyle(Binding binding) throws DeploymentException {
        SOAPBinding soapBinding = (SOAPBinding) SchemaInfoBuilder.getExtensibilityElement(SOAPBinding.class, binding.getExtensibilityElements());
//            String transportURI = soapBinding.getTransportURI();
        String portStyleString = soapBinding.getStyle();
        return Style.getStyle(portStyleString);
    }

    private URL getAddressLocation(Port port) throws DeploymentException {
        SOAPAddress soapAddress;
        try {
            soapAddress = (SOAPAddress) SchemaInfoBuilder.getExtensibilityElement(SOAPAddress.class, port.getExtensibilityElements());
        } catch (DeploymentException e) {
            //a http: protocol REST service.  Skip it.
            return null;
        }
        String locationURIString = soapAddress.getLocationURI();
        URL location;
        try {
            location = new URL(locationURIString);
        } catch (MalformedURLException e) {
            throw new DeploymentException("Could not construct web service location URL from " + locationURIString, e);
        }
        return location;
    }

    private void doHeavyweightMapping(QName serviceName, PortType portType, List<ServiceEndpointInterfaceMapping> endpointMappings, Bundle bundle, List operations, Binding binding, Style portStyle, SOAPConstants soapVersion, Map exceptionMap, SchemaInfoBuilder schemaInfoBuilder, JavaWsdlMapping mapping, OperationInfo[] operationInfos, String portName, URL location, List handlerInfos, Map seiPortNameToFactoryMap, Map seiClassNameToFactoryMap, String credentialsName) throws DeploymentException {
        Class<?> serviceEndpointInterface;
        SEIFactory seiFactory;
        //complete jaxrpc mapping file supplied
        QName portTypeQName = portType.getQName();
        ServiceEndpointInterfaceMapping endpointMapping = WSDescriptorParser.getServiceEndpointInterfaceMapping(endpointMappings, portTypeQName);
        String fqcn = endpointMapping.getServiceEndpointInterface();
        try {
            serviceEndpointInterface = bundle.loadClass(fqcn);
        } catch (ClassNotFoundException e) {
            throw new DeploymentException("Could not load service endpoint interface", e);
        }
//        Class enhancedServiceEndpointClass = enhanceServiceEndpointInterface(serviceEndpointInterface, context, module, classLoader);

        Collection<OperationDesc> operationDescs = new ArrayList<OperationDesc>();
        List<ServiceEndpointMethodMapping> methodMappings = endpointMapping.getServiceEndpointMethodMapping();
        int i = 0;
        Set wrapperElementQNames = new HashSet();
        List<JavaXmlTypeMapping> javaXmlTypeMappings = mapping.getJavaXmlTypeMapping();
        boolean hasEncoded = false;
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
            ServiceEndpointMethodMapping methodMapping = WSDescriptorParser.getMethodMappingForOperation(operationName, methodMappings);
            HeavyweightOperationDescBuilder operationDescBuilder = new HeavyweightOperationDescBuilder(bindingOperation, mapping, methodMapping, portStyle, exceptionMap, schemaInfoBuilder, javaXmlTypeMappings, bundle, serviceEndpointInterface);
            OperationInfo operationInfo = operationDescBuilder.buildOperationInfo(soapVersion);
            operationInfos[i++] = operationInfo;
            operationDescs.add(operationInfo.getOperationDesc());
            wrapperElementQNames.addAll(operationDescBuilder.getWrapperElementQNames());
            hasEncoded |= operationDescBuilder.isEncoded();
        }
        HeavyweightTypeInfoBuilder builder = new HeavyweightTypeInfoBuilder(bundle, schemaInfoBuilder.getSchemaTypeKeyToSchemaTypeMap(), wrapperElementQNames, operationDescs, hasEncoded);
        List typeInfo = builder.buildTypeInfo(mapping);

        seiFactory = createSEIFactory(serviceName, portName, serviceEndpointInterface.getName(), typeInfo, location, operationInfos, handlerInfos, credentialsName);
        seiPortNameToFactoryMap.put(portName, seiFactory);
        seiClassNameToFactoryMap.put(serviceEndpointInterface.getName(), seiFactory);
    }

    private void doLightweightMapping(QName serviceName, PortType portType, JavaWsdlMapping mapping, Bundle bundle, List operations, Binding binding, Style portStyle, SOAPConstants soapVersion, OperationInfo[] operationInfos, SchemaInfoBuilder schemaInfoBuilder, String portName, URL location, List handlerInfos, Map seiPortNameToFactoryMap, Map seiClassNameToFactoryMap, String credentialsName) throws DeploymentException {
        Class<?> serviceEndpointInterface;
        SEIFactory seiFactory;
        //lightweight jaxrpc mapping supplied
        serviceEndpointInterface = getServiceEndpointInterfaceLightweight(portType, mapping, bundle);
//        Class enhancedServiceEndpointClass = enhanceServiceEndpointInterface(serviceEndpointInterface, context, module, classLoader);

        int i = 0;
        for (Iterator ops = operations.iterator(); ops.hasNext();) {
            Operation operation = (Operation) ops.next();
            Method method = WSDescriptorParser.getMethodForOperation(serviceEndpointInterface, operation);
            BindingOperation bindingOperation = binding.getBindingOperation(operation.getName(), operation.getInput().getName(), operation.getOutput() == null ? null : operation.getOutput().getName());
            operationInfos[i++] = buildOperationInfoLightweight(method, bindingOperation, portStyle, soapVersion);
        }
        LightweightTypeInfoBuilder builder = new LightweightTypeInfoBuilder(bundle, schemaInfoBuilder.getSchemaTypeKeyToSchemaTypeMap(), Collections.EMPTY_SET);
        List typeInfo = builder.buildTypeInfo(mapping);

        seiFactory = createSEIFactory(serviceName, portName, serviceEndpointInterface.getName(), typeInfo, location, operationInfos, handlerInfos, credentialsName);
        seiPortNameToFactoryMap.put(portName, seiFactory);
        seiClassNameToFactoryMap.put(serviceEndpointInterface.getName(), seiFactory);
    }

    private Class getServiceEndpointInterfaceLightweight(PortType portType, JavaWsdlMapping mappings, Bundle bundle) throws DeploymentException {
        QName portTypeQName = portType.getQName();
        String portTypeNamespace = portTypeQName.getNamespaceURI();
        String portTypePackage = WSDescriptorParser.getPackageFromNamespace(portTypeNamespace, mappings);
        StringBuilder shortInterfaceName = new StringBuilder(portTypeQName.getLocalPart());
        shortInterfaceName.setCharAt(0, Character.toUpperCase(shortInterfaceName.charAt(0)));
        //TODO just use one buffer!
        String fqcn = portTypePackage + "." + shortInterfaceName.toString();
        try {
            return bundle.loadClass(fqcn);
        } catch (ClassNotFoundException e) {
            throw new DeploymentException("Could not load service endpoint interface type", e);
        }
    }


    public SEIFactory createSEIFactory(QName serviceName, String portName, String enhancedServiceEndpointClassName, List typeInfo, URL location, OperationInfo[] operationInfos, List handlerInfoInfos, String credentialsName) throws DeploymentException {
        List handlerInfos = buildHandlerInfosForPort(portName, handlerInfoInfos);
        return new SEIFactoryImpl(serviceName, portName, enhancedServiceEndpointClassName, operationInfos, typeInfo, location, handlerInfos, credentialsName);
    }

    private List<HandlerInfo> buildHandlerInfosForPort(String portName, List<HandlerInfoInfo> handlerInfoInfos) {
        List<HandlerInfo> handlerInfos = new ArrayList<HandlerInfo>();
        for (HandlerInfoInfo handlerInfoInfo: handlerInfoInfos) {
            Set portNames = handlerInfoInfo.getPortNames();
            if (portNames.isEmpty() || portNames.contains(portName)) {
                HandlerInfo handlerInfo = new HandlerInfo(handlerInfoInfo.getHandlerClass(), handlerInfoInfo.getHandlerConfig(), handlerInfoInfo.getSoapHeaders().toArray(new QName[0]));
                handlerInfos.add(handlerInfo);

                //TODO what about the soap roles??
            }
        }
        return handlerInfos;
    }

    public OperationInfo buildOperationInfoLightweight(Method method, BindingOperation bindingOperation, Style defaultStyle, SOAPConstants soapVersion) throws DeploymentException {
        LightweightOperationDescBuilder operationDescBuilder = new LightweightOperationDescBuilder(bindingOperation, method);
        return operationDescBuilder.buildOperationInfo(soapVersion);
    }


    Class<?> loadClass(String className, Bundle bundle) throws DeploymentException {
        try {
            return bundle.loadClass(className);
        } catch (ClassNotFoundException ex) {
            throw new DeploymentException("Unable to load Web Service class: " + className);
        }
    }

    static boolean isJAXWSWebService(Class clazz) {
        return (clazz.isAnnotationPresent(WebService.class) ||
                clazz.isAnnotationPresent(WebServiceProvider.class));
    }
}
