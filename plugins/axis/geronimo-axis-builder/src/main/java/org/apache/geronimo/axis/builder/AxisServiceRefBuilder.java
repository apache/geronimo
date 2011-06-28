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

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.namespace.QName;

import org.apache.geronimo.common.DeploymentException;
import org.apache.geronimo.gbean.annotation.GBean;
import org.apache.geronimo.gbean.annotation.ParamAttribute;
import org.apache.geronimo.gbean.annotation.ParamReference;
import org.apache.geronimo.j2ee.annotation.ReferenceType;
import org.apache.geronimo.j2ee.deployment.EARContext;
import org.apache.geronimo.j2ee.deployment.HandlerInfoInfo;
import org.apache.geronimo.j2ee.deployment.Module;
import org.apache.geronimo.j2ee.j2eeobjectnames.NameFactory;
import org.apache.geronimo.kernel.ClassLoading;
import org.apache.geronimo.kernel.repository.Environment;
import org.apache.geronimo.naming.deployment.AbstractNamingBuilder;
import org.apache.geronimo.naming.deployment.ServiceRefBuilder;
import org.apache.geronimo.xbeans.geronimo.naming.GerServiceRefDocument;
import org.apache.geronimo.xbeans.geronimo.naming.GerServiceRefType;
import org.apache.openejb.jee.Handler;
import org.apache.openejb.jee.JndiConsumer;
import org.apache.openejb.jee.PortComponentRef;
import org.apache.openejb.jee.ParamValue;
import org.apache.openejb.jee.ServiceRef;
import org.apache.xmlbeans.QNameSet;
import org.apache.xmlbeans.XmlObject;
import org.osgi.framework.Bundle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @version $Rev$ $Date$
 */
@GBean(j2eeType = NameFactory.MODULE_BUILDER)
public class AxisServiceRefBuilder extends AbstractNamingBuilder implements ServiceRefBuilder {
    private static final Logger log = LoggerFactory.getLogger(AxisServiceRefBuilder.class);
    private final QNameSet serviceRefQNameSet;
    private static final QName GER_SERVICE_REF_QNAME = GerServiceRefDocument.type.getDocumentElementName();
    private static final QNameSet GER_SERVICE_REF_QNAME_SET = QNameSet.singleton(GER_SERVICE_REF_QNAME);

    private final AxisBuilder axisBuilder;

    public AxisServiceRefBuilder(@ParamAttribute(name = "defaultEnvironment") Environment defaultEnvironment,
                                 @ParamAttribute(name = "eeNamespaces") String[] eeNamespaces,
                                 @ParamReference(name = "AxisBuilder", namingType = NameFactory.MODULE_BUILDER) AxisBuilder axisBuilder) {
        super(defaultEnvironment);
        this.axisBuilder = axisBuilder;
        serviceRefQNameSet = buildQNameSet(eeNamespaces, "service-ref");
    }

    protected boolean willMergeEnvironment(JndiConsumer specDD, XmlObject plan) {
        return !specDD.getServiceRef().isEmpty();
    }

//    public void buildNaming(XmlObject specDD, XmlObject plan, Module module, Map componentContext) throws DeploymentException {
//        List<ServiceRefType> serviceRefsUntyped = convert(specDD.selectChildren(serviceRefQNameSet), JEE_CONVERTER, ServiceRefType.class, ServiceRefType.type);
//        XmlObject[] gerServiceRefsUntyped = plan == null ? NO_REFS : plan.selectChildren(GER_SERVICE_REF_QNAME_SET);
//        Map serviceRefMap = mapServiceRefs(gerServiceRefsUntyped);
//
//        for (ServiceRefType serviceRef : serviceRefsUntyped) {
//            String name = getStringValue(serviceRef.getServiceRefName());
//            addInjections(name, serviceRef.getInjectionTargetArray(), componentContext);
//            GerServiceRefType serviceRefType = (GerServiceRefType) serviceRefMap.get(name);
//            serviceRefMap.remove(name);
//            buildNaming(serviceRef, serviceRefType, module, componentContext);
//        }
//
//        if (serviceRefMap.size() > 0) {
//            log.warn("Failed to build reference to service reference "+serviceRefMap.keySet()+" defined in plan file, reason - corresponding entry in deployment descriptor missing.");
//        }
//    }

//    public void buildNaming(XmlObject serviceRef, GerServiceRefType gerServiceRefType, Module module, Map componentContext) throws DeploymentException {
//        ServiceRefType serviceRefType =
//                (ServiceRefType) convert(serviceRef, JEE_CONVERTER, ServiceRefType.type);
//        buildNaming(serviceRefType, gerServiceRefType, module, componentContext);
//    }

    @Override
    public void buildNaming(JndiConsumer jndiConsumer, XmlObject xmlObject, Module module, Map<EARContext.Key, Object> keyObjectMap) throws DeploymentException {
    }

    @Override
    public void buildNaming(ServiceRef serviceRef, GerServiceRefType gerServiceRefType, Module module, Map<EARContext.Key, Object> sharedContext) throws DeploymentException {
        //TODO name needs to be normalized or get normalized name from jee's map.
        String name = serviceRef.getKey();
        Bundle bundle = module.getEarContext().getDeploymentBundle();

//            Map credentialsNameMap = (Map) serviceRefCredentialsNameMap.get(name);
        String serviceInterfaceName = serviceRef.getServiceInterface();
        assureInterface(serviceInterfaceName, "javax.xml.rpc.Service", "[Web]Service", bundle);
        Class serviceInterface;
        try {
            serviceInterface = bundle.loadClass(serviceInterfaceName);
        } catch (ClassNotFoundException e) {
            throw new DeploymentException("Could not load service interface class: " + serviceInterfaceName, e);
        }
        URI wsdlURI = null;
        if (serviceRef.getWsdlFile() != null) {
            try {
                wsdlURI = new URI(serviceRef.getWsdlFile());
            } catch (URISyntaxException e) {
                throw new DeploymentException("could not construct wsdl uri from " + serviceRef.getWsdlFile(), e);
            }
        }
        URI jaxrpcMappingURI = null;
        if (serviceRef.getJaxrpcMappingFile() != null) {
            try {
                jaxrpcMappingURI = new URI(serviceRef.getJaxrpcMappingFile());
            } catch (URISyntaxException e) {
                throw new DeploymentException("Could not construct jaxrpc mapping uri from " + serviceRef.getJaxrpcMappingFile(), e);
            }
        }
        QName serviceQName = null;
        if (serviceRef.getServiceQname() != null) {
            serviceQName = serviceRef.getServiceQname();
        }
        Map portComponentRefMap = new HashMap();
        List<PortComponentRef> portComponentRefs = serviceRef.getPortComponentRef();
        for (PortComponentRef portComponentRef : portComponentRefs) {
            String portComponentLink = portComponentRef.getPortComponentLink();
            String serviceEndpointInterfaceType = portComponentRef.getServiceEndpointInterface();
            assureInterface(serviceEndpointInterfaceType, "java.rmi.Remote", "ServiceEndpoint", bundle);
            Class serviceEndpointClass;
            try {
                serviceEndpointClass = bundle.loadClass(serviceEndpointInterfaceType);
            } catch (ClassNotFoundException e) {
                throw new DeploymentException("could not load service endpoint class " + serviceEndpointInterfaceType, e);
            }
            portComponentRefMap.put(serviceEndpointClass, portComponentLink);
        }
        List<HandlerInfoInfo> handlerInfos = buildHandlerInfoList(serviceRef.getHandler(), bundle);

//we could get a Reference or the actual serializable Service back.
        Object ref = axisBuilder.createService(serviceInterface, wsdlURI, jaxrpcMappingURI, serviceQName, portComponentRefMap, handlerInfos, gerServiceRefType, module, bundle);
        put(name, ref, ReferenceType.SERVICE, module.getJndiContext(), serviceRef.getInjectionTarget(), sharedContext);
        //getJndiContextMap(componentContext).put(ENV + name, ref);
    }

    public QNameSet getSpecQNameSet() {
        return serviceRefQNameSet;
    }

    public QNameSet getPlanQNameSet() {
        return GER_SERVICE_REF_QNAME_SET;
    }


    private static List<HandlerInfoInfo> buildHandlerInfoList(List<Handler> handlers, Bundle bundle) throws DeploymentException {
        List<HandlerInfoInfo> handlerInfos = new ArrayList<HandlerInfoInfo>();
        for (Handler handler: handlers) {
            List<String> portNames = handler.getPortName();
            String handlerClassName = handler.getHandlerClass();
            Class handlerClass;
            try {
                handlerClass = ClassLoading.loadClass(handlerClassName, bundle);
            } catch (ClassNotFoundException e) {
                throw new DeploymentException("Could not load handler class", e);
            }
            Map<String, String> config = new HashMap<String, String>();
            for (ParamValue paramValue: handler.getInitParam()) {
                config.put(paramValue.getParamName(), paramValue.getParamValue());
            }
            List<QName> headerQNames = handler.getSoapHeader();
            Set<String> soapRoles = new HashSet<String>(handler.getSoapRole());
            HandlerInfoInfo handlerInfoInfo = new HandlerInfoInfo(new HashSet(portNames), handlerClass, config, headerQNames, soapRoles);
            handlerInfos.add(handlerInfoInfo);
        }
        return handlerInfos;
    }

    private static Map mapServiceRefs(XmlObject[] refs) {
        Map refMap = new HashMap();
        if (refs != null) {
            for (int i = 0; i < refs.length; i++) {
                GerServiceRefType ref = (GerServiceRefType) refs[i].copy().changeType(GerServiceRefType.type);
                String serviceRefName = ref.getServiceRefName().trim();
                refMap.put(serviceRefName, ref);
            }
        }
        return refMap;
    }
}
