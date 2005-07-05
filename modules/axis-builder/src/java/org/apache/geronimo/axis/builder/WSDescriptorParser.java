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
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.MalformedURLException;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;
import java.util.jar.JarFile;
import javax.wsdl.Definition;
import javax.wsdl.Operation;
import javax.wsdl.Port;
import javax.wsdl.extensions.soap.SOAPAddress;
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
import javax.xml.rpc.handler.HandlerInfo;

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
import org.apache.geronimo.xbeans.j2ee.ParamValueType;
import org.apache.geronimo.xbeans.j2ee.XsdQNameType;
import org.apache.xmlbeans.XmlException;

/**
 * @version $Rev:  $ $Date:  $
 */
public class WSDescriptorParser {


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
        // Build list of available operations for exception
        StringBuffer availOps = new StringBuffer(128);
        for (int i = 0; i < methodMappings.length; i++) {
            if (i != 0) availOps.append(",");
            availOps.append(methodMappings[i].getWsdlOperation().getStringValue());
        }
        throw new DeploymentException("No method found for operation named '" + operationName + "'. Available operations: " + availOps);
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

    /**
     * Parses a webservice.xml file and returns a map PortInfo instances indexed by the
     * corresponding ejb-link or servlet-link element .
     *
     * @param webservicesType
     * @param moduleFile
     * @param isEJB
     * @param servletLocations
     * @return
     * @throws org.apache.geronimo.common.DeploymentException
     */
    public static Map parseWebServiceDescriptor(WebservicesType webservicesType, JarFile moduleFile, boolean isEJB, Map servletLocations) throws DeploymentException {
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
            SchemaInfoBuilder schemaInfoBuilder =  new SchemaInfoBuilder(moduleFile, wsdlURI);
            Map wsdlPortMap = schemaInfoBuilder.getPortMap();

            JavaWsdlMappingType javaWsdlMapping = readJaxrpcMapping(moduleFile, jaxrpcMappingURI);
            HashMap seiMappings = new HashMap();
            ServiceEndpointInterfaceMappingType[] mappings = javaWsdlMapping.getServiceEndpointInterfaceMappingArray();
            for (int j = 0; j < mappings.length; j++) {
                ServiceEndpointInterfaceMappingType seiMapping = mappings[j];
                seiMappings.put(seiMapping.getServiceEndpointInterface().getStringValue(), seiMapping);
            }

//            Map portLocations = new HashMap();
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
                    String servletLocation = (String) servletLocations.get(linkName);
                    if (servletLocation == null) {
                        throw new DeploymentException("No servlet mapping for port " + portQName);
                    }
                    schemaInfoBuilder.movePortLocation(portQName.getLocalPart(), servletLocation);
                } else {
                    linkName = serviceImplBeanType.getEjbLink().getStringValue().trim();
                    schemaInfoBuilder.movePortLocation(portQName.getLocalPart(), null);
                }
                PortComponentHandlerType[] handlers = portComponent.getHandlerArray();

                Port port = (Port) wsdlPortMap.get(portQName.getLocalPart());
                if (port == null) {
                    throw new DeploymentException("No WSDL Port definition for port-component " + portComponentName);
                }

                ServiceEndpointInterfaceMappingType seiMapping = (ServiceEndpointInterfaceMappingType) seiMappings.get(seiInterfaceName);

                String wsdlLocation = webserviceDescription.getWsdlFile().getStringValue().trim();
                URI contextURI = getAddressLocation(port);

                PortInfo portInfo = new PortInfo(portComponentName, portQName, schemaInfoBuilder, javaWsdlMapping, seiInterfaceName, handlers, port, seiMapping, wsdlLocation, contextURI);

                if (portMap.put(linkName, portInfo) != null) {
                    throw new DeploymentException("Ambiguous description of port associated with j2ee component " + linkName);
                }
            }
        }
        return portMap;
    }

    private static URI getAddressLocation(Port port) throws DeploymentException {
        SOAPAddress soapAddress = (SOAPAddress) SchemaInfoBuilder.getExtensibilityElement(SOAPAddress.class, port.getExtensibilityElements());
        String locationURIString = soapAddress.getLocationURI();
        try {
            URI location = new URI(locationURIString);
            URI contextPath = new URI(location.getPath());
            return contextPath;
        } catch (URISyntaxException e) {
            throw new DeploymentException("Could not construct web service location URL from " + locationURIString);
        }
    }

    public static Map parseWebServiceDescriptor(URL wsDDUrl, JarFile moduleFile, boolean isEJB, Map servletLocations) throws DeploymentException {
        try {
            WebservicesDocument webservicesDocument = WebservicesDocument.Factory.parse(wsDDUrl);
            SchemaConversionUtils.validateDD(webservicesDocument);
            WebservicesType webservicesType = webservicesDocument.getWebservices();
            return parseWebServiceDescriptor(webservicesType, moduleFile, isEJB, servletLocations);
        } catch (XmlException e) {
            throw new DeploymentException("Could not read descriptor document", e);
        } catch (IOException e) {
            return null;
        }

    }

    public static List createHandlerInfoList(PortComponentHandlerType[] handlers, ClassLoader classLoader) throws DeploymentException {
        List list = new ArrayList();
        for (int i = 0; i < handlers.length; i++) {
            PortComponentHandlerType handler = handlers[i];

            // Get handler class
            Class handlerClass = null;
            String className = handler.getHandlerClass().getStringValue().trim();
            try {
                handlerClass = classLoader.loadClass(className);
            } catch (ClassNotFoundException e) {
                throw new DeploymentException("Unable to load handler class: " + className, e);
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
}
