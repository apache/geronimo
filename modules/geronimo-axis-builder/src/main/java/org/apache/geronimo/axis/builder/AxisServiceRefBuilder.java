/**
 *
 * Copyright 2006 The Apache Software Foundation
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
import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.gbean.GBeanInfoBuilder;
import org.apache.geronimo.j2ee.deployment.HandlerInfoInfo;
import org.apache.geronimo.j2ee.deployment.Module;
import org.apache.geronimo.j2ee.j2eeobjectnames.NameFactory;
import org.apache.geronimo.kernel.ClassLoading;
import org.apache.geronimo.kernel.config.Configuration;
import org.apache.geronimo.kernel.repository.Environment;
import org.apache.geronimo.naming.deployment.AbstractNamingBuilder;
import org.apache.geronimo.xbeans.geronimo.naming.GerServiceRefDocument;
import org.apache.geronimo.xbeans.geronimo.naming.GerServiceRefType;
import org.apache.geronimo.xbeans.j2ee.ParamValueType;
import org.apache.geronimo.xbeans.j2ee.PortComponentRefType;
import org.apache.geronimo.xbeans.j2ee.ServiceRefHandlerType;
import org.apache.geronimo.xbeans.j2ee.ServiceRefType;
import org.apache.geronimo.xbeans.j2ee.XsdQNameType;
import org.apache.xmlbeans.QNameSet;
import org.apache.xmlbeans.XmlObject;

/**
 * @version $Rev$ $Date$
 */
public class AxisServiceRefBuilder extends AbstractNamingBuilder {
    private final QNameSet serviceRefQNameSet;
    private static final QName GER_SERVICE_REF_QNAME = GerServiceRefDocument.type.getDocumentElementName();
    private static final QNameSet GER_SERVICE_REF_QNAME_SET = QNameSet.singleton(GER_SERVICE_REF_QNAME);

    private final AxisBuilder axisBuilder;

    public AxisServiceRefBuilder(Environment defaultEnvironment, String[] eeNamespaces, AxisBuilder axisBuilder) {
        super(defaultEnvironment);
        this.axisBuilder = axisBuilder;
        serviceRefQNameSet = buildQNameSet(eeNamespaces, "service-ref");
    }

    protected boolean willMergeEnvironment(XmlObject specDD, XmlObject plan) {
         return getServiceRefs(specDD).length > 0;
    }
    
    public void buildNaming(XmlObject specDD, XmlObject plan, Configuration localConfiguration, Configuration remoteConfiguration, Module module, Map componentContext) throws DeploymentException {
        XmlObject[] serviceRefsUntyped = getServiceRefs(specDD);
        XmlObject[] gerServiceRefsUntyped = plan == null? NO_REFS: plan.selectChildren(GER_SERVICE_REF_QNAME_SET);
        Map serviceRefMap = mapServiceRefs(gerServiceRefsUntyped);
        ClassLoader cl = localConfiguration.getConfigurationClassLoader();

        for (int i = 0; i < serviceRefsUntyped.length; i++) {
            ServiceRefType serviceRef = (ServiceRefType) serviceRefsUntyped[i];
            String name = getStringValue(serviceRef.getServiceRefName());
            GerServiceRefType serviceRefType = (GerServiceRefType) serviceRefMap.get(name);
//            Map credentialsNameMap = (Map) serviceRefCredentialsNameMap.get(name);
            String serviceInterfaceName = getStringValue(serviceRef.getServiceInterface());
            assureInterface(serviceInterfaceName, "javax.xml.rpc.Service", "[Web]Service", cl);
            Class serviceInterface;
            try {
                serviceInterface = cl.loadClass(serviceInterfaceName);
            } catch (ClassNotFoundException e) {
                throw new DeploymentException("Could not load service interface class: " + serviceInterfaceName, e);
            }
            URI wsdlURI = null;
            if (serviceRef.isSetWsdlFile()) {
                try {
                    wsdlURI = new URI(serviceRef.getWsdlFile().getStringValue().trim());
                } catch (URISyntaxException e) {
                    throw new DeploymentException("could not construct wsdl uri from " + serviceRef.getWsdlFile().getStringValue(), e);
                }
            }
            URI jaxrpcMappingURI = null;
            if (serviceRef.isSetJaxrpcMappingFile()) {
                try {
                    jaxrpcMappingURI = new URI(getStringValue(serviceRef.getJaxrpcMappingFile()));
                } catch (URISyntaxException e) {
                    throw new DeploymentException("Could not construct jaxrpc mapping uri from " + serviceRef.getJaxrpcMappingFile(), e);
                }
            }
            QName serviceQName = null;
            if (serviceRef.isSetServiceQname()) {
                serviceQName = serviceRef.getServiceQname().getQNameValue();
            }
            Map portComponentRefMap = new HashMap();
            PortComponentRefType[] portComponentRefs = serviceRef.getPortComponentRefArray();
            if (portComponentRefs != null) {
                for (int j = 0; j < portComponentRefs.length; j++) {
                    PortComponentRefType portComponentRef = portComponentRefs[j];
                    String portComponentLink = getStringValue(portComponentRef.getPortComponentLink());
                    String serviceEndpointInterfaceType = getStringValue(portComponentRef.getServiceEndpointInterface());
                    assureInterface(serviceEndpointInterfaceType, "java.rmi.Remote", "ServiceEndpoint", cl);
                    Class serviceEndpointClass;
                    try {
                        serviceEndpointClass = cl.loadClass(serviceEndpointInterfaceType);
                    } catch (ClassNotFoundException e) {
                        throw new DeploymentException("could not load service endpoint class " + serviceEndpointInterfaceType, e);
                    }
                    portComponentRefMap.put(serviceEndpointClass, portComponentLink);
                }
            }
            ServiceRefHandlerType[] handlers = serviceRef.getHandlerArray();
            List handlerInfos = buildHandlerInfoList(handlers, cl);

//we could get a Reference or the actual serializable Service back.
            Object ref = axisBuilder.createService(serviceInterface, wsdlURI, jaxrpcMappingURI, serviceQName, portComponentRefMap, handlerInfos, serviceRefType, module, cl);
            componentContext.put(ENV + name, ref);
        }

    }

    private XmlObject[] getServiceRefs(XmlObject specDD) {
        return convert(specDD.selectChildren(serviceRefQNameSet), J2EE_CONVERTER, ServiceRefType.type);
    }

    public QNameSet getSpecQNameSet() {
        return serviceRefQNameSet;
    }

    public QNameSet getPlanQNameSet() {
        return GER_SERVICE_REF_QNAME_SET;
    }


    private static List buildHandlerInfoList(ServiceRefHandlerType[] handlers, ClassLoader classLoader) throws DeploymentException {
        List handlerInfos = new ArrayList();
        for (int i = 0; i < handlers.length; i++) {
            ServiceRefHandlerType handler = handlers[i];
            org.apache.geronimo.xbeans.j2ee.String[] portNameArray = handler.getPortNameArray();
            List portNames = new ArrayList();
            for (int j = 0; j < portNameArray.length; j++) {
                portNames.add(portNameArray[j].getStringValue().trim());

            }
//            Set portNames = new HashSet(Arrays.asList(portNameArray));
            String handlerClassName = handler.getHandlerClass().getStringValue().trim();
            Class handlerClass;
            try {
                handlerClass = ClassLoading.loadClass(handlerClassName, classLoader);
            } catch (ClassNotFoundException e) {
                throw new DeploymentException("Could not load handler class", e);
            }
            Map config = new HashMap();
            ParamValueType[] paramValues = handler.getInitParamArray();
            for (int j = 0; j < paramValues.length; j++) {
                ParamValueType paramValue = paramValues[j];
                String paramName = paramValue.getParamName().getStringValue().trim();
                String paramStringValue = paramValue.getParamValue().getStringValue().trim();
                config.put(paramName, paramStringValue);
            }
            XsdQNameType[] soapHeaderQNames = handler.getSoapHeaderArray();
            QName[] headerQNames = new QName[soapHeaderQNames.length];
            for (int j = 0; j < soapHeaderQNames.length; j++) {
                XsdQNameType soapHeaderQName = soapHeaderQNames[j];
                headerQNames[j] = soapHeaderQName.getQNameValue();
            }
            Set soapRoles = new HashSet();
            for (int j = 0; j < handler.getSoapRoleArray().length; j++) {
                String soapRole = handler.getSoapRoleArray(j).getStringValue().trim();
                soapRoles.add(soapRole);
            }
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

    public static final GBeanInfo GBEAN_INFO;

    static {
        GBeanInfoBuilder infoBuilder = GBeanInfoBuilder.createStatic(AxisServiceRefBuilder.class, NameFactory.MODULE_BUILDER);
        infoBuilder.addAttribute("defaultEnvironment", Environment.class, true, true);
        infoBuilder.addAttribute("eeNamespaces", String[].class, true, true);
        infoBuilder.addReference("AxisBuilder", AxisBuilder.class, NameFactory.MODULE_BUILDER);

        infoBuilder.setConstructor(new String[] {"defaultEnvironment", "eeNamespaces", "AxisBuilder"});

        GBEAN_INFO = infoBuilder.getBeanInfo();
    }

    public static GBeanInfo getGBeanInfo() {
        return GBEAN_INFO;
    }
}
