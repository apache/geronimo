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

package org.apache.geronimo.naming.deployment;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.Resource;
import javax.xml.namespace.QName;
import org.apache.geronimo.common.DeploymentException;
import org.apache.geronimo.deployment.service.EnvironmentBuilder;
import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.gbean.GBeanInfoBuilder;
import org.apache.geronimo.j2ee.deployment.Module;
import org.apache.geronimo.j2ee.deployment.annotation.AnnotatedApp;
import org.apache.geronimo.j2ee.deployment.annotation.ResourceAnnotationHelper;
import org.apache.geronimo.j2ee.deployment.annotation.WebServiceRefAnnotationHelper;
import org.apache.geronimo.j2ee.j2eeobjectnames.NameFactory;
import org.apache.geronimo.kernel.repository.Environment;
import org.apache.geronimo.xbeans.geronimo.naming.GerServiceRefDocument;
import org.apache.geronimo.xbeans.geronimo.naming.GerServiceRefType;
import org.apache.geronimo.xbeans.javaee.DescriptionType;
import org.apache.geronimo.xbeans.javaee.FullyQualifiedClassType;
import org.apache.geronimo.xbeans.javaee.InjectionTargetType;
import org.apache.geronimo.xbeans.javaee.JndiNameType;
import org.apache.geronimo.xbeans.javaee.ServiceRefType;
import org.apache.geronimo.xbeans.javaee.XsdStringType;
import org.apache.xmlbeans.QNameSet;
import org.apache.xmlbeans.XmlObject;
import org.osgi.framework.Bundle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SwitchingServiceRefBuilder extends AbstractNamingBuilder {

    private static final Logger log = LoggerFactory.getLogger(SwitchingServiceRefBuilder.class);

    private static final QName GER_SERVICE_REF_QNAME = GerServiceRefDocument.type
            .getDocumentElementName();

    private static final QNameSet GER_SERVICE_REF_QNAME_SET = QNameSet
            .singleton(GER_SERVICE_REF_QNAME);

    private final QNameSet serviceRefQNameSet;

    private final Collection jaxrpcBuilders;

    private final Collection jaxwsBuilders;

    public SwitchingServiceRefBuilder(String[] eeNamespaces,
            Collection jaxrpcBuilders,
            Collection jaxwsBuilders) {
        super(null);
        this.jaxrpcBuilders = jaxrpcBuilders;
        this.jaxwsBuilders = jaxwsBuilders;
        this.serviceRefQNameSet = buildQNameSet(eeNamespaces, "service-ref");
    }

    public void buildEnvironment(XmlObject specDD,
            XmlObject plan,
            Environment environment)
            throws DeploymentException {
        if (this.jaxrpcBuilders != null && !this.jaxrpcBuilders.isEmpty()) {
            mergeEnvironment(environment, getJAXRCPBuilder());
        }
        if (this.jaxwsBuilders != null && !this.jaxwsBuilders.isEmpty()) {
            mergeEnvironment(environment, getJAXWSBuilder());
        }
    }

    public void buildNaming(XmlObject specDD,
            XmlObject plan,
            Module module,
            Map componentContext) throws DeploymentException {

        // Discover and process any @WebServiceRef annotations (if !metadata-complete)
        if ((module != null) && (module.getClassFinder() != null)) {
            processAnnotations(module);
        }

        Bundle bundle = module.getEarContext().getDeploymentBundle();
        Class jaxrpcClass = null;
        Class jaxwsClass = null;

        XmlObject[] serviceRefs = specDD.selectChildren(serviceRefQNameSet);

        XmlObject[] gerServiceRefsUntyped = plan == null ? NO_REFS : plan
                .selectChildren(GER_SERVICE_REF_QNAME_SET);
        Map serviceRefMap = mapServiceRefs(gerServiceRefsUntyped);

        for (XmlObject serviceRef : serviceRefs) {
            if (jaxrpcClass == null) {
                jaxrpcClass = loadClass("javax.xml.rpc.Service", bundle);
            }
            if (jaxwsClass == null) {
                jaxwsClass = loadClass("javax.xml.ws.Service", bundle);
            }
            ServiceRefType serviceRefType = (ServiceRefType) convert(
                    serviceRef, JEE_CONVERTER, ServiceRefType.type);

            String name = getStringValue(serviceRefType.getServiceRefName());
            GerServiceRefType gerServiceRefType = (GerServiceRefType) serviceRefMap.get(name);
            serviceRefMap.remove(name);

            String serviceInterfaceName = getStringValue(serviceRefType
                    .getServiceInterface());
            Class serviceInterfaceClass = loadClass(serviceInterfaceName, bundle);

            InjectionTargetType[] injections = serviceRefType.getInjectionTargetArray();
            addInjections(name, injections, componentContext);

            if (jaxrpcClass.isAssignableFrom(serviceInterfaceClass)) {
                // class jaxrpc handler
                ServiceRefBuilder jaxrpcBuilder = getJAXRCPBuilder();
                jaxrpcBuilder.buildNaming(serviceRef, gerServiceRefType,
                        module, componentContext);
            } else if (jaxwsClass.isAssignableFrom(serviceInterfaceClass)) {
                // calll jaxws handler
                ServiceRefBuilder jaxwsBuilder = getJAXWSBuilder();
                jaxwsBuilder.buildNaming(serviceRef, gerServiceRefType, module,
                        componentContext);
            } else {
                throw new DeploymentException(serviceInterfaceName
                        + " does not extend "
                        + jaxrpcClass.getName() + " or "
                        + jaxwsClass.getName());
            }
        }

        if (serviceRefMap.size() > 0) {
            log.warn("Failed to build reference to service reference "+serviceRefMap.keySet()+" defined in plan file, reason - corresponding entry in deployment descriptor missing.");
        }
    }

    private ServiceRefBuilder getJAXWSBuilder() throws DeploymentException {
        ServiceRefBuilder jaxwsBuilder = null;
        if (this.jaxwsBuilders == null || this.jaxwsBuilders.isEmpty()) {
            throw new DeploymentException(
                    "No JAX-WS ServiceRefBuilders registered");
        } else {
            jaxwsBuilder = (ServiceRefBuilder) this.jaxwsBuilders.iterator()
                    .next();
        }
        return jaxwsBuilder;
    }

    private ServiceRefBuilder getJAXRCPBuilder() throws DeploymentException {
        ServiceRefBuilder jaxrpcBuilder = null;
        if (this.jaxrpcBuilders == null || this.jaxrpcBuilders.isEmpty()) {
            throw new DeploymentException(
                    "No JAX-RPC ServiceRefBuilders registered");
        } else {
            jaxrpcBuilder = (ServiceRefBuilder) this.jaxrpcBuilders.iterator()
                    .next();
        }
        return jaxrpcBuilder;
    }

    private void mergeEnvironment(Environment environment, ServiceRefBuilder builder) {
        Environment env = builder.getEnvironment();
        if (env != null) {
            EnvironmentBuilder.mergeEnvironments(environment, env);
        }
    }

    private Class loadClass(String name, Bundle bundle)
            throws DeploymentException {
        try {
            return bundle.loadClass(name);
        } catch (ClassNotFoundException e) {
            throw new DeploymentException("Could not load service class "
                    + name, e);
        }
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

    private void processAnnotations(Module module) throws DeploymentException {

        // Process all the annotations for this naming builder type
        //At the moment the only exception thrown is if the resulting doc is not valid.  Bail now.
        try {
            WebServiceRefAnnotationHelper.processAnnotations(module.getAnnotatedApp(), module.getClassFinder());
            ResourceAnnotationHelper.processAnnotations(module.getAnnotatedApp(), module.getClassFinder(), ServiceRefProcessor.INSTANCE);
        }
        catch (Exception e) {
            log.warn("Unable to process @Resource annotations for module" + module.getName(), e);
        }
    }

    public QNameSet getSpecQNameSet() {
        return serviceRefQNameSet;
    }

    public QNameSet getPlanQNameSet() {
        return GER_SERVICE_REF_QNAME_SET;
    }

    public static class ServiceRefProcessor extends ResourceAnnotationHelper.ResourceProcessor {

        public static final ServiceRefProcessor INSTANCE = new ServiceRefProcessor();

        private ServiceRefProcessor() {
        }

        public boolean processResource(AnnotatedApp annotatedApp, Resource annotation, Class cls, Method method, Field field) {
            log.debug("processResource( [annotatedApp] " + annotatedApp.toString() + "," + '\n' +
                    "[annotation] " + annotation.toString() + "," + '\n' +
                    "[cls] " + (cls != null ? cls.getName() : null) + "," + '\n' +
                    "[method] " + (method != null ? method.getName() : null) + "," + '\n' +
                    "[field] " + (field != null ? field.getName() : null) + " ): Entry");

            String resourceName = getResourceName(annotation, method, field);
            String resourceType = getResourceType(annotation, method, field);

            log.debug("processResource(): resourceName: " + resourceName);
            log.debug("processResource(): resourceType: " + resourceType);

            if (resourceType.equals("javax.xml.rpc.Service") ||
                resourceType.equals("javax.xml.ws.Service") ||
                resourceType.equals("javax.jws.WebService")) {

                log.debug("processResource(): <service-ref> found");

                boolean exists = false;
                ServiceRefType[] serviceRefs = annotatedApp.getServiceRefArray();
                for (ServiceRefType serviceRef : serviceRefs) {
                    if (serviceRef.getServiceRefName().getStringValue().trim().equals(resourceName)) {
                        if (method != null || field != null) {
                            InjectionTargetType[] targets = serviceRef.getInjectionTargetArray();
                            if (!hasTarget(method, field, targets)) {
                                configureInjectionTarget(serviceRef.addNewInjectionTarget(), method, field);
                            }
                        }
                        exists = true;
                        break;
                    }
                }
                if (!exists) {
                    try {

                        log.debug("processResource(): Does not exist in DD: " + resourceName);

                        // Doesn't exist in deployment descriptor -- add new
                        ServiceRefType serviceRef = annotatedApp.addNewServiceRef();

                        //------------------------------------------------------------------------------
                        // <service-ref> required elements:
                        //------------------------------------------------------------------------------

                        // service-ref-name
                        JndiNameType serviceRefName = serviceRef.addNewServiceRefName();
                        serviceRefName.setStringValue(resourceName);
                        serviceRef.setServiceRefName(serviceRefName);

                        // service-ref-interface
                        FullyQualifiedClassType serviceRefInterfaceClass = serviceRef.addNewServiceInterface();
                        serviceRefInterfaceClass.setStringValue(resourceType);
                        serviceRef.setServiceInterface(serviceRefInterfaceClass);

                        //------------------------------------------------------------------------------
                        // <service-ref> optional elements:
                        //------------------------------------------------------------------------------

                        // description
                        String descriptionAnnotation = annotation.description();
                        if (!descriptionAnnotation.equals("")) {
                            DescriptionType description = serviceRef.addNewDescription();
                            description.setStringValue(descriptionAnnotation);
                        }

                        // service-ref-type
                        if (!serviceRef.isSetServiceRefType()) {
                            FullyQualifiedClassType serviceRefTypeClass = serviceRef.addNewServiceRefType();
                            serviceRefTypeClass.setStringValue(resourceType);
                            serviceRef.setServiceRefType(serviceRefTypeClass);
                        }

                        // injectionTarget
                        if (method != null || field != null) {
                            InjectionTargetType injectionTarget = serviceRef.addNewInjectionTarget();
                            configureInjectionTarget(injectionTarget, method, field);
                        }
                        
                        // mappedName
                        if (!serviceRef.isSetMappedName() && annotation.mappedName().trim().length() > 0) {
                            XsdStringType mappedName = serviceRef.addNewMappedName();
                            mappedName.setStringValue(annotation.mappedName().trim());
                            serviceRef.setMappedName(mappedName);
                        }
                    }
                    catch (Exception anyException) {
                        log.debug("SwitchServiceRefBuilder: Exception caught while processing <service-ref>");
                    }
                }
                return true;
            }
            return false;
        }
    }

    public static final GBeanInfo GBEAN_INFO;

    static {
        GBeanInfoBuilder infoBuilder = GBeanInfoBuilder.createStatic(
                SwitchingServiceRefBuilder.class, NameFactory.MODULE_BUILDER);
        infoBuilder.addAttribute("eeNamespaces", String[].class, true, true);
        infoBuilder.addReference("JAXRPCBuilder", ServiceRefBuilder.class,
                NameFactory.MODULE_BUILDER);
        infoBuilder.addReference("JAXWSBuilder", ServiceRefBuilder.class,
                NameFactory.MODULE_BUILDER);

        infoBuilder.setConstructor(new String[]{"eeNamespaces",
                "JAXRPCBuilder", "JAXWSBuilder"});

        GBEAN_INFO = infoBuilder.getBeanInfo();
    }

    public static GBeanInfo getGBeanInfo() {
        return GBEAN_INFO;
    }
}
