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

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import javax.xml.namespace.QName;
import javax.xml.ws.Service;
import javax.xml.ws.handler.Handler;
import org.apache.geronimo.common.DeploymentException;
import org.apache.geronimo.j2ee.annotation.ReferenceType;
import org.apache.geronimo.j2ee.deployment.EARContext;
import org.apache.geronimo.j2ee.deployment.Module;
import org.apache.geronimo.jaxws.handler.HandlerChainsInfoBuilder;
import org.apache.geronimo.kernel.repository.Environment;
import org.apache.geronimo.naming.deployment.AbstractNamingBuilder;
import org.apache.geronimo.naming.deployment.ServiceRefBuilder;
import org.apache.geronimo.xbeans.geronimo.naming.GerServiceRefDocument;
import org.apache.geronimo.xbeans.geronimo.naming.GerServiceRefType;
import org.apache.openejb.jee.HandlerChain;
import org.apache.openejb.jee.HandlerChains;
import org.apache.openejb.jee.JndiConsumer;
import org.apache.openejb.jee.PortComponentRef;
import org.apache.openejb.jee.ServiceRef;
import org.apache.xmlbeans.QNameSet;
import org.apache.xmlbeans.XmlObject;
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

    protected HandlerChainsInfoBuilder handlerChainsInfoBuilder = new HandlerChainsInfoBuilder();

    public JAXWSServiceRefBuilder(Environment defaultEnvironment, String[] eeNamespaces) {
        super(defaultEnvironment);
        serviceRefQNameSet = buildQNameSet(eeNamespaces, "service-ref");
    }

    @Override
    protected boolean willMergeEnvironment(JndiConsumer specDD, XmlObject plan) {
        return !specDD.getServiceRef().isEmpty();
    }

    @Override
    public void buildNaming(JndiConsumer specDD,
            XmlObject plan,
            Module module,
            Map<EARContext.Key, Object> componentContext) throws DeploymentException {
        Collection<ServiceRef> serviceRefsUntyped = specDD.getServiceRef();
        XmlObject[] gerServiceRefsUntyped = plan == null ? NO_REFS : plan.selectChildren(GER_SERVICE_REF_QNAME_SET);
        Map<String, GerServiceRefType> serviceRefMap = mapServiceRefs(gerServiceRefsUntyped);

        for (ServiceRef serviceRef : serviceRefsUntyped) {
            String name = getStringValue(serviceRef.getServiceRefName());
            GerServiceRefType serviceRefType = serviceRefMap.remove(name);
            buildNaming(serviceRef, serviceRefType, module, componentContext);
        }

        if (serviceRefMap.size() > 0) {
            log.warn("Failed to build reference to service reference "+serviceRefMap.keySet()+" defined in plan file, reason - corresponding entry in deployment descriptor missing.");
        }
    }

    private Class<?> loadClass(String className, Bundle bundle, String classDescription) throws DeploymentException {
        try {
            return bundle.loadClass(className);
        } catch (ClassNotFoundException e) {
            throw new DeploymentException("Could not load " + classDescription + " class " + className, e);
        }
    }

    @Override
    public void buildNaming(ServiceRef serviceRef, GerServiceRefType gerServiceRef, Module module, Map<EARContext.Key, Object> sharedContext) throws DeploymentException {
        Bundle bundle = module.getEarContext().getDeploymentBundle();
        //TODO normalize or use normalized name from jee's map
        String name = serviceRef.getKey();

        String serviceInterfaceName = getStringValue(serviceRef.getServiceInterface());
        Class<?> serviceInterfaceClass = loadClass(serviceInterfaceName, bundle, "service");
        if (!Service.class.isAssignableFrom(serviceInterfaceClass)) {
            throw new DeploymentException(serviceInterfaceName + " service class does not extend " + Service.class.getName());
        }

        QName serviceQName = serviceRef.getServiceQname();

        URI wsdlURI = null;
        if (serviceRef.getWsdlFile() != null) {
            String wsdlLocation = serviceRef.getWsdlFile().trim();
            try {
                wsdlURI = new URI(wsdlLocation);
            } catch (URISyntaxException e) {
                throw new DeploymentException("Could not construct WSDL URI from " + wsdlLocation, e);
            }
        }

        Class<?> serviceReferenceType = null;
        if (serviceRef.getServiceRefType() != null) {
            String referenceClassName = serviceRef.getServiceRefType();
            serviceReferenceType = loadClass(referenceClassName, bundle, "service reference");
        }

        if (serviceRef.getHandlerChains() != null) {
            HandlerChains handlerChains = serviceRef.getHandlerChains();
            for (HandlerChain handlerChain : handlerChains.getHandlerChain()) {
                for (org.apache.openejb.jee.Handler handler : handlerChain.getHandler()) {
                    String handlerClassName = getStringValue(handler.getHandlerClass());
                    Class<?> handlerClass = loadClass(handlerClassName, bundle, "handler");
                    if (!Handler.class.isAssignableFrom(handlerClass)) {
                        throw new DeploymentException(handlerClassName + " handler class does not extend " + Handler.class.getName());
                    }
                }
            }
        }

        Map<Class<?>, PortComponentRef> portComponentRefMap = new HashMap<Class<?>, PortComponentRef>();
        for (PortComponentRef portComponentRef : serviceRef.getPortComponentRef()) {
            String serviceEndpointInterfaceType = getStringValue(portComponentRef.getServiceEndpointInterface());
            Class<?> serviceEndpointClass = loadClass(serviceEndpointInterfaceType, bundle, "service endpoint");

            // TODO: check if it is annotated?

            portComponentRefMap.put(serviceEndpointClass, portComponentRef);
        }

        Object ref = createService(serviceRef, gerServiceRef, module, bundle,
                                   serviceInterfaceClass, serviceQName,
                                   wsdlURI, serviceReferenceType, portComponentRefMap);
        put(name, ref, ReferenceType.SERVICE, module.getJndiContext(), serviceRef.getInjectionTarget(), sharedContext);
    }

    protected abstract Object createService(ServiceRef serviceRef, GerServiceRefType gerServiceRef,
                                         Module module, Bundle bundle, Class serviceInterfaceClass,
                                         QName serviceQName, URI wsdlURI, Class serviceReferenceType,
                                         Map<Class<?>, PortComponentRef> portComponentRefMap) throws DeploymentException;

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
}
