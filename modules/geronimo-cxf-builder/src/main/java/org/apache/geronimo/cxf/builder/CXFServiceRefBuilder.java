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

package org.apache.geronimo.cxf.builder;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.namespace.QName;

import org.apache.geronimo.common.DeploymentException;
import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.gbean.GBeanInfoBuilder;
import org.apache.geronimo.j2ee.deployment.Module;
import org.apache.geronimo.j2ee.j2eeobjectnames.NameFactory;
import org.apache.geronimo.kernel.config.Configuration;
import org.apache.geronimo.kernel.repository.Environment;
import org.apache.geronimo.naming.deployment.AbstractNamingBuilder;
import org.apache.geronimo.naming.deployment.ServiceRefBuilder;
import org.apache.geronimo.xbeans.geronimo.naming.GerServiceRefDocument;
import org.apache.geronimo.xbeans.geronimo.naming.GerServiceRefType;
import org.apache.geronimo.xbeans.javaee.PortComponentRefType;
import org.apache.geronimo.xbeans.javaee.ServiceRefHandlerChainType;
import org.apache.geronimo.xbeans.javaee.ServiceRefHandlerChainsType;
import org.apache.geronimo.xbeans.javaee.ServiceRefHandlerType;
import org.apache.geronimo.xbeans.javaee.ServiceRefType;

import org.apache.xmlbeans.QNameSet;
import org.apache.xmlbeans.XmlObject;

public class CXFServiceRefBuilder extends AbstractNamingBuilder implements ServiceRefBuilder {

    private static final QName GER_SERVICE_REF_QNAME = 
        GerServiceRefDocument.type.getDocumentElementName();

    private static final QNameSet GER_SERVICE_REF_QNAME_SET = 
        QNameSet.singleton(GER_SERVICE_REF_QNAME);

    private final QNameSet serviceRefQNameSet;
    private final CXFBuilder cxfBuilder;
    
    public CXFServiceRefBuilder(Environment defaultEnvironment,
                                String[] eeNamespaces,
                                CXFBuilder cxfBuilder) {
        super(defaultEnvironment);
        this.cxfBuilder = cxfBuilder;
        serviceRefQNameSet = buildQNameSet(eeNamespaces, "service-ref");
    }

    protected boolean willMergeEnvironment(XmlObject specDD, XmlObject plan) {
        return specDD.selectChildren(serviceRefQNameSet).length > 0;
    }

    public void buildNaming(XmlObject specDD,
                            XmlObject plan,
                            Configuration localConfiguration,
                            Configuration remoteConfiguration,
                            Module module,
                            Map componentContext) throws DeploymentException {       
        List<ServiceRefType> serviceRefsUntyped = convert(specDD.selectChildren(serviceRefQNameSet), JEE_CONVERTER, ServiceRefType.class, ServiceRefType.type);
        XmlObject[] gerServiceRefsUntyped = plan == null ? NO_REFS : plan.selectChildren(GER_SERVICE_REF_QNAME_SET);
        Map serviceRefMap = mapServiceRefs(gerServiceRefsUntyped);
        
        for (ServiceRefType serviceRef : serviceRefsUntyped) {
            String name = getStringValue(serviceRef.getServiceRefName());            
            GerServiceRefType serviceRefType = (GerServiceRefType) serviceRefMap.get(name);
            
            buildNaming(serviceRef, serviceRefType, module, componentContext);
        }        
    }

    private Class loadClass(String name, ClassLoader cl) throws DeploymentException {
        try {
            return cl.loadClass(name);
        } catch (ClassNotFoundException e) {
            throw new DeploymentException("Could not load service class " + name, e);                
        }         
    }
    
    public void buildNaming(XmlObject serviceRef, GerServiceRefType gerServiceRefType, Module module, Map componentContext) throws DeploymentException {
        ServiceRefType serviceRefType = 
            (ServiceRefType)convert(serviceRef, JEE_CONVERTER, ServiceRefType.type);
        buildNaming(serviceRefType, gerServiceRefType, module, componentContext);
    }
    
    public void buildNaming(ServiceRefType serviceRef, GerServiceRefType gerServiceRef, Module module, Map componentContext) throws DeploymentException {
        ClassLoader cl = module.getEarContext().getClassLoader();
        Class jaxwsServiceClass = loadClass("javax.xml.ws.Service", cl);
        String name = getStringValue(serviceRef.getServiceRefName());
        String serviceInterfaceName = getStringValue(serviceRef.getServiceInterface());
        Class serviceInterfaceClass = loadClass(serviceInterfaceName, cl);
        
        if (!jaxwsServiceClass.isAssignableFrom(serviceInterfaceClass)) {
            throw new DeploymentException(serviceInterfaceName + " class does not extend " + jaxwsServiceClass.getName());
        }
        // TODO: check if service class is abstract???
        
        QName serviceQName = null;
        if (serviceRef.isSetServiceQname()) {
            serviceQName = serviceRef.getServiceQname().getQNameValue();
        }
        
        URI wsdlURI = null;
        if (serviceRef.isSetWsdlFile()) {
            try {
                wsdlURI = new URI(serviceRef.getWsdlFile().getStringValue().trim());
            } catch (URISyntaxException e) {
                throw new DeploymentException("could not construct wsdl uri from " + serviceRef.getWsdlFile().getStringValue(), e);
            }
        }
        
        Class serviceReferenceType = null;
        if (serviceRef.isSetServiceRefType()) {
            String referenceClassName = getStringValue(serviceRef.getServiceRefType());            
            serviceReferenceType = loadClass(referenceClassName, cl);
        }
  
        // TODO: handle handler chains
        if (serviceRef.isSetHandlerChains()) {
            ServiceRefHandlerChainsType handlerChains = serviceRef.getHandlerChains();
            for (ServiceRefHandlerChainType handlerChain : handlerChains.getHandlerChainArray()) {
                // bindings, port, service
                for (ServiceRefHandlerType handler : handlerChain.getHandlerArray()) {
                 // handler class
                }
            }
        }
                                
        Map portComponentRefMap = new HashMap();
        PortComponentRefType[] portComponentRefs = serviceRef.getPortComponentRefArray();
        if (portComponentRefs != null) {
            for (int j = 0; j < portComponentRefs.length; j++) {
                PortComponentRefType portComponentRef = portComponentRefs[j];
                String serviceEndpointInterfaceType = getStringValue(portComponentRef.getServiceEndpointInterface());
                Class serviceEndpointClass = loadClass(serviceEndpointInterfaceType, cl);            
                String portComponentLink = getStringValue(portComponentRef.getPortComponentLink());
                
                portComponentRefMap.put(serviceEndpointClass, portComponentLink);
            }
        }
        
        Object ref = cxfBuilder.createService(serviceInterfaceClass, serviceReferenceType, wsdlURI, serviceQName, portComponentRefMap, serviceRef.getHandlerChains(), gerServiceRef, module, cl);
        getJndiContextMap(componentContext).put(ENV + name, ref);
    }
       
    private static Map mapServiceRefs(XmlObject[] refs) {
        Map refMap = new HashMap();
        if (refs != null) {
            for (int i = 0; i < refs.length; i++) {
                GerServiceRefType ref = (GerServiceRefType) refs[i].copy()
                        .changeType(GerServiceRefType.type);
                String serviceRefName = ref.getServiceRefName().trim();
                refMap.put(serviceRefName, ref);
            }
        }
        return refMap;
    }

    public QNameSet getSpecQNameSet() {
        return serviceRefQNameSet;
    }

    public QNameSet getPlanQNameSet() {
        return GER_SERVICE_REF_QNAME_SET;
    }

    public static final GBeanInfo GBEAN_INFO;

    static {
        GBeanInfoBuilder infoBuilder = GBeanInfoBuilder.createStatic(
                CXFServiceRefBuilder.class, NameFactory.MODULE_BUILDER);
        infoBuilder.addInterface(ServiceRefBuilder.class);
        infoBuilder.addAttribute("defaultEnvironment", Environment.class, true,
                true);
        infoBuilder.addAttribute("eeNamespaces", String[].class, true, true);
        infoBuilder.addReference("CXFBuilder", CXFBuilder.class,
                NameFactory.MODULE_BUILDER);

        infoBuilder.setConstructor(new String[] { "defaultEnvironment",
                "eeNamespaces", "CXFBuilder" });

        GBEAN_INFO = infoBuilder.getBeanInfo();
    }

    public static GBeanInfo getGBeanInfo() {
        return GBEAN_INFO;
    }


}
