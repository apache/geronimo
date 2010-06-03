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
package org.apache.geronimo.jaxws.builder;

import java.io.IOException;
import java.io.StringWriter;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.namespace.QName;
import javax.xml.ws.Service;
import javax.xml.ws.handler.Handler;

import org.apache.geronimo.common.DeploymentException;
import org.apache.geronimo.j2ee.deployment.EARContext;
import org.apache.geronimo.j2ee.deployment.Module;
import org.apache.geronimo.jaxws.HandlerChainsUtils;
import org.apache.geronimo.kernel.repository.Environment;
import org.apache.geronimo.naming.deployment.AbstractNamingBuilder;
import org.apache.geronimo.naming.deployment.ServiceRefBuilder;
import org.apache.geronimo.xbeans.geronimo.naming.GerServiceRefDocument;
import org.apache.geronimo.xbeans.geronimo.naming.GerServiceRefType;
import org.apache.geronimo.xbeans.javaee6.HandlerChainType;
import org.apache.geronimo.xbeans.javaee6.HandlerChainsType;
import org.apache.geronimo.xbeans.javaee6.HandlerType;
import org.apache.geronimo.xbeans.javaee6.PortComponentRefType;
import org.apache.geronimo.xbeans.javaee6.ServiceRefType;
import org.apache.xmlbeans.QNameSet;
import org.apache.xmlbeans.XmlObject;
import org.apache.xmlbeans.XmlOptions;
import org.osgi.framework.Bundle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class JAXWSServiceRefBuilder extends AbstractNamingBuilder implements ServiceRefBuilder {
    private static final Logger log = LoggerFactory.getLogger(JAXWSServiceRefBuilder.class);

    private static final QName GER_SERVICE_REF_QNAME =
        GerServiceRefDocument.type.getDocumentElementName();

    private static final QNameSet GER_SERVICE_REF_QNAME_SET =
        QNameSet.singleton(GER_SERVICE_REF_QNAME);

    private final QNameSet serviceRefQNameSet;

    public JAXWSServiceRefBuilder(Environment defaultEnvironment, String[] eeNamespaces) {
        super(defaultEnvironment);
        serviceRefQNameSet = buildQNameSet(eeNamespaces, "service-ref");
    }

    protected boolean willMergeEnvironment(XmlObject specDD, XmlObject plan) {
        return specDD.selectChildren(serviceRefQNameSet).length > 0;
    }

    public void buildNaming(XmlObject specDD,
            XmlObject plan,
            Module module,
            Map<EARContext.Key, Object> componentContext) throws DeploymentException {
        List<ServiceRefType> serviceRefsUntyped = convert(specDD.selectChildren(serviceRefQNameSet), JEE_CONVERTER, ServiceRefType.class, ServiceRefType.type);
        XmlObject[] gerServiceRefsUntyped = plan == null ? NO_REFS : plan.selectChildren(GER_SERVICE_REF_QNAME_SET);
        Map<String, GerServiceRefType> serviceRefMap = mapServiceRefs(gerServiceRefsUntyped);

        for (ServiceRefType serviceRef : serviceRefsUntyped) {
            String name = getStringValue(serviceRef.getServiceRefName());
            addInjections(name, serviceRef.getInjectionTargetArray(), componentContext);
            GerServiceRefType serviceRefType = serviceRefMap.remove(name);
            buildNaming(serviceRef, serviceRefType, module, componentContext);
        }

        if (serviceRefMap.size() > 0) {
            log.warn("Failed to build reference to service reference "+serviceRefMap.keySet()+" defined in plan file, reason - corresponding entry in deployment descriptor missing.");
        }
    }

    private Class loadClass(String className, Bundle bundle, String classDescription) throws DeploymentException {
        try {
            return bundle.loadClass(className);
        } catch (ClassNotFoundException e) {
            throw new DeploymentException("Could not load " + classDescription + " class " + className, e);
        }
    }

    public void buildNaming(XmlObject serviceRef, GerServiceRefType gerServiceRefType, Module module, Map componentContext) throws DeploymentException {
        ServiceRefType serviceRefType =
            (ServiceRefType)convert(serviceRef, JEE_CONVERTER, ServiceRefType.type);
        buildNaming(serviceRefType, gerServiceRefType, module, componentContext);
    }

    public void buildNaming(ServiceRefType serviceRef, GerServiceRefType gerServiceRef, Module module, Map<EARContext.Key, Object> componentContext) throws DeploymentException {
        Bundle bundle = module.getEarContext().getDeploymentBundle();
        String name = getStringValue(serviceRef.getServiceRefName());

        String serviceInterfaceName = getStringValue(serviceRef.getServiceInterface());
        Class serviceInterfaceClass = loadClass(serviceInterfaceName, bundle, "service");
        if (!Service.class.isAssignableFrom(serviceInterfaceClass)) {
            throw new DeploymentException(serviceInterfaceName + " service class does not extend " + Service.class.getName());
        }

        QName serviceQName = null;
        if (serviceRef.isSetServiceQname()) {
            serviceQName = serviceRef.getServiceQname().getQNameValue();
        }

        URI wsdlURI = null;
        if (serviceRef.isSetWsdlFile()) {
            String wsdlLocation = serviceRef.getWsdlFile().getStringValue().trim();
            try {
                wsdlURI = new URI(wsdlLocation);
            } catch (URISyntaxException e) {
                throw new DeploymentException("Could not construct WSDL URI from " + wsdlLocation, e);
            }
        }

        Class serviceReferenceType = null;
        if (serviceRef.isSetServiceRefType()) {
            String referenceClassName = getStringValue(serviceRef.getServiceRefType());
            serviceReferenceType = loadClass(referenceClassName, bundle, "service reference");
        }

        if (serviceRef.isSetHandlerChains()) {
            HandlerChainsType handlerChains = serviceRef.getHandlerChains();
            for (HandlerChainType handlerChain : handlerChains.getHandlerChainArray()) {
                for (HandlerType handler : handlerChain.getHandlerArray()) {
                    String handlerClassName = getStringValue(handler.getHandlerClass());
                    Class handlerClass = loadClass(handlerClassName, bundle, "handler");
                    if (!Handler.class.isAssignableFrom(handlerClass)) {
                        throw new DeploymentException(handlerClassName + " handler class does not extend " + Handler.class.getName());
                    }
                }
            }
        }

        Map<Class, PortComponentRefType> portComponentRefMap = new HashMap<Class, PortComponentRefType>();
        PortComponentRefType[] portComponentRefs = serviceRef.getPortComponentRefArray();
        if (portComponentRefs != null) {
            for (int j = 0; j < portComponentRefs.length; j++) {
                PortComponentRefType portComponentRef = portComponentRefs[j];
                String serviceEndpointInterfaceType = getStringValue(portComponentRef.getServiceEndpointInterface());
                Class serviceEndpointClass = loadClass(serviceEndpointInterfaceType, bundle, "service endpoint");

                // TODO: check if it is annotated?

                portComponentRefMap.put(serviceEndpointClass, portComponentRef);
            }
        }

        Object ref = createService(serviceRef, gerServiceRef, module, bundle,
                                   serviceInterfaceClass, serviceQName,
                                   wsdlURI, serviceReferenceType, portComponentRefMap);
        put(name, ref, getJndiContextMap(componentContext));
    }

    public abstract Object createService(ServiceRefType serviceRef, GerServiceRefType gerServiceRef,
                                         Module module, Bundle bundle, Class serviceInterfaceClass,
                                         QName serviceQName, URI wsdlURI, Class serviceReferenceType,
                                         Map<Class, PortComponentRefType> portComponentRefMap) throws DeploymentException;

    private static Map<String, GerServiceRefType> mapServiceRefs(XmlObject[] refs) {
        Map<String, GerServiceRefType> refMap = new HashMap<String, GerServiceRefType>();
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

    public static String getHandlerChainAsString(HandlerChainsType handlerChains)
        throws IOException {
        String xml = null;
        if (handlerChains != null) {
            StringWriter w = new StringWriter();
            XmlOptions options = new XmlOptions();
            options.setSaveSyntheticDocumentElement(HandlerChainsUtils.HANDLER_CHAINS_QNAME);
            handlerChains.save(w, options);
            xml = w.toString();
        }
        return xml;
    }
}
