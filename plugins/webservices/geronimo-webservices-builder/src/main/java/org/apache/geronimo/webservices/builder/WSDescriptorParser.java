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
package org.apache.geronimo.webservices.builder;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;

import javax.wsdl.Definition;
import javax.wsdl.Operation;
import javax.xml.namespace.QName;
import javax.xml.rpc.handler.HandlerInfo;
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
import org.apache.openejb.jee.ExceptionMapping;
import org.apache.openejb.jee.Handler;
import org.apache.openejb.jee.JavaWsdlMapping;
import org.apache.openejb.jee.JaxbJavaee;
import org.apache.openejb.jee.PackageMapping;
import org.apache.openejb.jee.ParamValue;
import org.apache.openejb.jee.PortComponent;
import org.apache.openejb.jee.ServiceEndpointInterfaceMapping;
import org.apache.openejb.jee.ServiceEndpointMethodMapping;
import org.apache.openejb.jee.ServiceImplBean;
import org.apache.openejb.jee.WebserviceDescription;
import org.apache.openejb.jee.Webservices;
import org.osgi.framework.Bundle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @version $Rev$ $Date$
 */
public class WSDescriptorParser {

    private static final Logger logger = LoggerFactory.getLogger(WSDescriptorParser.class);

    public static JavaWsdlMapping readJaxrpcMapping(JarFile moduleFile, URI jaxrpcMappingURI) throws DeploymentException {
        String jaxrpcMappingPath = jaxrpcMappingURI.toString();
        return readJaxrpcMapping(moduleFile, jaxrpcMappingPath);
    }

    public static JavaWsdlMapping readJaxrpcMapping(JarFile moduleFile, String jaxrpcMappingPath) throws DeploymentException {
        InputStream jaxrpcInputStream;
        try {
            ZipEntry zipEntry = moduleFile.getEntry(jaxrpcMappingPath);
            if (zipEntry == null) {
                throw new DeploymentException("The JAX-RPC mapping file " + jaxrpcMappingPath + " specified in webservices.xml could not be found.");
            }
            jaxrpcInputStream = moduleFile.getInputStream(zipEntry);
        } catch (IOException e) {
            throw new DeploymentException("Could not open stream to jaxrpc mapping document", e);
        }
        try {
            return  (JavaWsdlMapping) JaxbJavaee.unmarshalJavaee(JavaWsdlMapping.class, jaxrpcInputStream);
        } catch (Exception e) {
            throw new DeploymentException("Could not parse jaxrpc mapping document", e);
    } finally {
            try {
                jaxrpcInputStream.close();
            } catch (IOException e) {

            }
        }
    }


    public static Map<QName, ExceptionMapping> getExceptionMap(JavaWsdlMapping mapping) {
        Map<QName,ExceptionMapping> exceptionMap = new HashMap<QName, ExceptionMapping>();
        if (mapping != null) {
            Collection<ExceptionMapping> exceptionMappings = mapping.getExceptionMapping();
            for (ExceptionMapping exceptionMapping : exceptionMappings) {
                QName exceptionMessageQName = exceptionMapping.getWsdlMessage();
                exceptionMap.put(exceptionMessageQName, exceptionMapping);
            }
        }
        return exceptionMap;
    }

    public static String getPackageFromNamespace(String namespace, JavaWsdlMapping mapping) throws DeploymentException {
        Collection<PackageMapping> packageMappings = mapping.getPackageMapping();
        for (PackageMapping packageMapping : packageMappings) {
            if (namespace.equals(packageMapping.getNamespaceURI().trim())) {
                return packageMapping.getPackageType().trim();
            }
        }
        throw new DeploymentException("Namespace " + namespace + " was not mapped in jaxrpc mapping file");
    }

    private static final Map<Class,Class> rpcHolderClasses = new HashMap<Class, Class>();

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

    public static Class getHolder(String paramJavaTypeName, boolean isInOnly, QName typeQName, boolean isComplexType, JavaWsdlMapping mapping, Bundle bundle) throws DeploymentException {
        Class paramJavaType;
        if (isInOnly) {
            //IN parameters just use their own type
            try {
                paramJavaType = ClassLoading.loadClass(paramJavaTypeName, bundle);
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
                StringBuilder buf = new StringBuilder(packageName.length() + typeQName.getLocalPart().length() + 14);
                buf.append(packageName).append(".holders.").append(typeQName.getLocalPart()).append("Holder");
                buf.setCharAt(packageName.length() + 9, Character.toUpperCase(typeQName.getLocalPart().charAt(0)));
                holderName = buf.toString();
            } else {
                //see if it is in the primitive type and simple type mapping
                try {
                    paramJavaType = ClassLoading.loadClass(paramJavaTypeName, bundle);
                } catch (ClassNotFoundException e) {
                    throw new DeploymentException("could not load parameter type", e);
                }
                Class holder = rpcHolderClasses.get(paramJavaType);
                if (holder != null) {
                    try {
                        //TODO use class names in map or make sure we are in the correct classloader to start with.
                        holder = ClassLoading.loadClass(holder.getName(), bundle);
                    } catch (ClassNotFoundException e) {
                        throw new DeploymentException("could not load holder type in correct classloader", e);
                    }
                    return holder;
                }
                //Otherwise, the holder must be in:
                //package same as type's package + ".holders"
                //class name same as type name + "Holder"
                String paramTypeName = paramJavaType.getName();
                StringBuilder buf = new StringBuilder(paramTypeName.length() + 14);
                int dot = paramTypeName.lastIndexOf(".");
                //foo.Bar >>> foo.holders.BarHolder
                buf.append(paramTypeName.substring(0, dot)).append(".holders").append(paramTypeName.substring(dot)).append("Holder");
                holderName = buf.toString();
            }
            try {
                return ClassLoading.loadClass(holderName, bundle);
            } catch (ClassNotFoundException e) {
                throw new DeploymentException("Could not load holder class", e);
            }
        }
    }

    public static ServiceEndpointMethodMapping getMethodMappingForOperation(String operationName, List<ServiceEndpointMethodMapping> methodMappings) throws DeploymentException {
        for (ServiceEndpointMethodMapping methodMapping : methodMappings) {
            if (operationName.equals(methodMapping.getWsdlOperation())) {
                return methodMapping;
            }
        }
        // Build list of available operations for exception
        StringBuilder availOps = new StringBuilder(128);
        for (ServiceEndpointMethodMapping mapping: methodMappings) {
            if (availOps.length() > 0) availOps.append(",");
            availOps.append(mapping.getWsdlOperation());
        }
        throw new DeploymentException("No method found for operation named '" + operationName + "'. Available operations: " + availOps);
    }

    public static ServiceEndpointInterfaceMapping getServiceEndpointInterfaceMapping(List<ServiceEndpointInterfaceMapping> endpointMappings, QName portTypeQName) throws DeploymentException {
        for (ServiceEndpointInterfaceMapping endpointMapping : endpointMappings) {
            QName testPortQName = endpointMapping.getWsdlPortType();
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
        for (Method method : methods) {
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
     * @param webservices
     * @param moduleFile
     * @param isEJB
     * @param servletLocations
     * @return
     * @throws org.apache.geronimo.common.DeploymentException
     *
     */
    public static Map<String,PortInfo> parseWebServiceDescriptor(Webservices webservices, JarFile moduleFile, boolean isEJB, Map servletLocations) throws DeploymentException {
        Map<String,PortInfo> portMap = new HashMap<String, PortInfo>();
        Collection<WebserviceDescription> webserviceDescriptions = webservices.getWebserviceDescription();
        SharedPortInfo sharedPortInfo;
        for (WebserviceDescription webserviceDescription : webserviceDescriptions) {
            if (webserviceDescription.getWsdlFile() == null || webserviceDescription.getJaxrpcMappingFile() == null) {
                if(logger.isDebugEnabled()) {
                    logger.debug("This entry " + webserviceDescription.getDescription() + "should be a JAX-WS configuration, it will be ignored by JAX-RPC builder");
                }
                continue;
            }
            String wsdlLocation = webserviceDescription.getWsdlFile().trim();
            String jaxrpcMappingFile = webserviceDescription.getJaxrpcMappingFile().trim();

            sharedPortInfo = new SharedPortInfo(wsdlLocation,
                                                jaxrpcMappingFile,
                                                DescriptorVersion.J2EE);

            Collection<PortComponent> portComponents = webserviceDescription.getPortComponent();
            for (PortComponent portComponent : portComponents) {
                String portComponentName = portComponent.getPortComponentName().trim();
                QName portQName = portComponent.getWsdlPort();
                String seiInterfaceName = portComponent.getServiceEndpointInterface().trim();
                ServiceImplBean serviceImplBean = portComponent.getServiceImplBean();
                if (isEJB == (serviceImplBean.getServletLink() != null)) {
                    throw new DeploymentException("Wrong kind of web service described in web service descriptor: expected " + (isEJB ? "EJB" : "POJO(Servlet)"));
                }
                String linkName;
                String servletLocation;
                if (serviceImplBean.getServletLink() != null) {
                    linkName = serviceImplBean.getServletLink().trim();
                    servletLocation = (String) servletLocations.get(linkName);
                    if (servletLocation == null) {
                        throw new DeploymentException("No servlet mapping for port " + portComponentName);
                    }
                } else {
                    linkName = serviceImplBean.getEjbLink().trim();
                    servletLocation = (String) servletLocations.get(linkName);
                }
                PortInfo portInfo = new PortInfo(sharedPortInfo, portComponentName, portQName, seiInterfaceName, portComponent.getHandlerChains(), servletLocation);

                if (portMap.put(linkName, portInfo) != null) {
                    throw new DeploymentException("Ambiguous description of port associated with j2ee component " + linkName);
                }
            }
        }
        return portMap;
    }

    //this is an ee6 version?
    public static Map<String,PortInfo> parseWebServiceDescriptor2(Webservices webservices, JarFile moduleFile, boolean isEJB, Map servletLocations) throws DeploymentException {
        Map<String,PortInfo> portMap = new HashMap<String, PortInfo>();
        Collection<WebserviceDescription> webserviceDescriptions = webservices.getWebserviceDescription();
        SharedPortInfo sharedPortInfo;
        for (WebserviceDescription webserviceDescription : webserviceDescriptions) {
            String wsdlLocation = null;
            if (webserviceDescription.getWsdlFile() != null) {
                wsdlLocation = webserviceDescription.getWsdlFile().trim();
            }
            String jaxrpcMappingFile = null;
            if (webserviceDescription.getJaxrpcMappingFile() != null) {
                jaxrpcMappingFile = webserviceDescription.getJaxrpcMappingFile().trim();
            }

            sharedPortInfo = new SharedPortInfo(wsdlLocation,
                                                jaxrpcMappingFile,
                                                DescriptorVersion.JAVAEE);

            Collection<PortComponent> portComponents = webserviceDescription.getPortComponent();
            for (PortComponent portComponent : portComponents) {
                String portComponentName = portComponent.getPortComponentName().trim();
                QName portQName = null;
                if (portComponent.getWsdlPort() != null) {
                    portQName = portComponent.getWsdlPort();
                }
                String seiInterfaceName = null;
                if (portComponent.getServiceEndpointInterface() != null) {
                    seiInterfaceName = portComponent.getServiceEndpointInterface().trim();
                }
                ServiceImplBean serviceImplBean = portComponent.getServiceImplBean();
                if (isEJB == (serviceImplBean.getServletLink() != null)) {
                    throw new DeploymentException("Wrong kind of web service described in web service descriptor: expected " + (isEJB ? "EJB" : "POJO(Servlet)"));
                }
                String linkName;
                String servletLocation;
                if (serviceImplBean.getServletLink() != null) {
                    linkName = serviceImplBean.getServletLink().trim();
                    servletLocation = (String) servletLocations.get(linkName);
                    if (servletLocation == null) {
                        throw new DeploymentException("No servlet mapping for port " + portComponentName);
                    }
                } else {
                    linkName = serviceImplBean.getEjbLink().trim();
                    servletLocation = (String) servletLocations.get(linkName);
                }

//TODO WTF?
//                List<PortComponentHandler> handlers = null;
//                if (portComponent.getHandler() != null) {
//                    handlers = new PortComponentHandler[portComponent.getHandler().length];
//                    for (int i=0; i<portComponent.getHandler().length; i++) {
//                        handlers[i] = (PortComponentHandler)portComponent.getHandler()[i].change(PortComponentHandler.type);
//                    }
//                }
//
//                PortInfo portInfo = new PortInfo(sharedPortInfo, portComponentName, portQName, seiInterfaceName, handlers, servletLocation);
//
//                if (portMap.put(linkName, portInfo) != null) {
//                    throw new DeploymentException("Ambiguous description of port associated with j2ee component " + linkName);
//                }
            }
        }
        return portMap;
    }

    public static Map<String,PortInfo> parseWebServiceDescriptor(URL wsDDUrl, JarFile moduleFile, boolean isEJB, Map servletLocations) throws DeploymentException {
        Webservices webservices = getWebservices(wsDDUrl);
        if (webservices instanceof Webservices) {
            Webservices webServices = (Webservices)webservices;
            return parseWebServiceDescriptor(webServices, moduleFile, isEJB, servletLocations);
//        } else if (webservices instanceof org.apache.geronimo.xbeans.javaee6.Webservices) {
//            Webservices webServices = (org.apache.geronimo.xbeans.javaee6.Webservices)webservices;
//            return parseWebServiceDescriptor2(webservices, moduleFile, isEJB, servletLocations);
        } else {
            return null;
        }
    }

    static Webservices getWebservices(URL wsDDUrl) throws DeploymentException {
        try {
            InputStream in = wsDDUrl.openStream();
            try {
                return (Webservices) JaxbJavaee.unmarshalJavaee(Webservices.class, in);
            } catch (Exception e) {
                throw new DeploymentException("Could not read descriptor document", e);
            } finally {
                in.close();
            }

//            XmlObject webservicesDocumentUntyped = XmlObject.Factory.parse(wsDDUrl);
//            XmlCursor cursor = webservicesDocumentUntyped.newCursor();
//            try {
//                if (cursor.currentTokenType() != XmlCursor.TokenType.START) {
//                    while(cursor.toNextToken()  != XmlCursor.TokenType.START) {}
//                }
//                QName qname = cursor.getName();
//                if (WebservicesDocument.type.getDocumentElementName().equals(qname)) {
//                    return getJ2EEWebServices(webservicesDocumentUntyped);
//                } else if (org.apache.geronimo.xbeans.javaee6.WebservicesDocument.type.getDocumentElementName().equals(qname)) {
//                    return getJavaEEWebServices(webservicesDocumentUntyped);
//                } else {
//                    return null;
//                }
//            } finally {
//                cursor.dispose();
//            }
//        } catch (XmlException e) {
//            throw new DeploymentException("Could not read descriptor document", e);
        } catch (IOException e) {
            return null;
        }
    }

//    private static XmlObject getJ2EEWebServices(XmlObject webservicesDocumentUntyped) throws XmlException {
//        WebservicesDocument webservicesDocument;
//        if (webservicesDocumentUntyped instanceof WebservicesDocument) {
//            webservicesDocument = (WebservicesDocument) webservicesDocumentUntyped;
//        } else {
//            webservicesDocument = (WebservicesDocument) webservicesDocumentUntyped.changeType(WebservicesDocument.type);
//        }
//        XmlBeansUtil.validateDD(webservicesDocument);
//        return webservicesDocument.getWebservices();
//    }

//    private static XmlObject getJavaEEWebServices(XmlObject webservicesDocumentUntyped) throws XmlException {
//        XmlCursor cursor = null;
//        try {
//            cursor = webservicesDocumentUntyped.newCursor();
//            cursor.toStartDoc();
//            cursor.toFirstChild();
//            SchemaConversionUtils.convertSchemaVersion(cursor, SchemaConversionUtils.JAVAEE_NAMESPACE, "http://java.sun.com/xml/ns/javaee/javaee_web_services_1_3.xsd", "1.3");
//            XmlObject result = webservicesDocumentUntyped.changeType(org.apache.geronimo.xbeans.javaee6.WebservicesDocument.type);
//            XmlBeansUtil.validateDD(result);
//            org.apache.geronimo.xbeans.javaee6.WebservicesDocument webservicesDocument = (org.apache.geronimo.xbeans.javaee6.WebservicesDocument) result;
//            return webservicesDocument.getWebservices();
//        } finally {
//            if (cursor != null) {
//                try {
//                    cursor.dispose();
//                } catch (Exception e) {
//                }
//            }
//        }
//    }

    public static List<HandlerInfo> createHandlerInfoList(List<Handler> handlers, Bundle bundle) throws DeploymentException {
        List<HandlerInfo> list = new ArrayList<HandlerInfo>();
        for (Handler handler : handlers) {
            // Get handler class
            Class handlerClass;
            String className = handler.getHandlerClass().trim();
            try {
                handlerClass = bundle.loadClass(className);
            } catch (ClassNotFoundException e) {
                throw new DeploymentException("Unable to load handler class: " + className, e);
            }

            // config data for the handler
            Map<String, String> config = new HashMap<String, String>();
            List<ParamValue> paramValues = handler.getInitParam();
            for (ParamValue paramValue : paramValues) {
                String paramName = paramValue.getParamName().trim();
                String paramStringValue = paramValue.getParamValue().trim();
                config.put(paramName, paramStringValue);
            }

            // QName array of headers it processes
            List<QName> soapHeaderQNames = handler.getSoapHeader();
            QName[] headers = soapHeaderQNames.toArray(new QName[soapHeaderQNames.size()]); 

            list.add(new HandlerInfo(handlerClass, config, headers));
        }
        return list;
    }
}
