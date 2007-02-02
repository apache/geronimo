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

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import javax.xml.namespace.QName;

import org.apache.geronimo.common.DeploymentException;
import org.apache.geronimo.deployment.service.EnvironmentBuilder;
import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.gbean.GBeanInfoBuilder;
import org.apache.geronimo.j2ee.deployment.Module;
import org.apache.geronimo.j2ee.j2eeobjectnames.NameFactory;
import org.apache.geronimo.kernel.config.Configuration;
import org.apache.geronimo.kernel.repository.Environment;
import org.apache.geronimo.naming.deployment.AbstractNamingBuilder;
import org.apache.geronimo.xbeans.geronimo.naming.GerServiceRefDocument;
import org.apache.geronimo.xbeans.geronimo.naming.GerServiceRefType;
import org.apache.geronimo.xbeans.javaee.ServiceRefType;

import org.apache.xmlbeans.QNameSet;
import org.apache.xmlbeans.XmlObject;

public class SwitchingServiceRefBuilder extends AbstractNamingBuilder {

    private static final QName GER_SERVICE_REF_QNAME = GerServiceRefDocument.type
            .getDocumentElementName();

    private static final QNameSet GER_SERVICE_REF_QNAME_SET = QNameSet
            .singleton(GER_SERVICE_REF_QNAME);

    private final QNameSet serviceRefQNameSet;

    private final Collection jaxrpcBuilders;

    private final Collection jaxwsBuilders;

    private Environment environment;

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
        this.environment = environment;
    }

    public void buildNaming(XmlObject specDD,
                            XmlObject plan,
                            Configuration localConfiguration,
                            Configuration remoteConfiguration,
                            Module module,
                            Map componentContext) throws DeploymentException {
        ClassLoader cl = module.getEarContext().getClassLoader();
        Class jaxrpcClass = loadClass("javax.xml.rpc.Service", cl);
        Class jaxwsClass = loadClass("javax.xml.ws.Service", cl);

        boolean mergedJAXRPCEnv = false;
        boolean mergedJAXWSEnv = false;

        XmlObject[] serviceRefs = specDD.selectChildren(serviceRefQNameSet);

        XmlObject[] gerServiceRefsUntyped = plan == null ? NO_REFS : plan
                .selectChildren(GER_SERVICE_REF_QNAME_SET);
        Map serviceRefMap = mapServiceRefs(gerServiceRefsUntyped);

        for (XmlObject serviceRef : serviceRefs) {
            ServiceRefType serviceRefType = (ServiceRefType) convert(
                    serviceRef, JEE_CONVERTER, ServiceRefType.type);

            String name = getStringValue(serviceRefType.getServiceRefName());
            GerServiceRefType gerServiceRefType = (GerServiceRefType) serviceRefMap
                    .get(name);

            String serviceInterfaceName = getStringValue(serviceRefType
                    .getServiceInterface());
            Class serviceInterfaceClass = loadClass(serviceInterfaceName, cl);

            if (jaxrpcClass.isAssignableFrom(serviceInterfaceClass)) {
                // class jaxrpc handler
                ServiceRefBuilder jaxrpcBuilder = getJAXRCPBuilder();
                jaxrpcBuilder.buildNaming(serviceRef, gerServiceRefType,
                        module, componentContext);
                if (!mergedJAXRPCEnv) {
                    mergeEnvironment(jaxrpcBuilder);
                    mergedJAXRPCEnv = true;
                }
            } else if (jaxwsClass.isAssignableFrom(serviceInterfaceClass)) {
                // calll jaxws handler
                ServiceRefBuilder jaxwsBuilder = getJAXWSBuilder();
                jaxwsBuilder.buildNaming(serviceRef, gerServiceRefType, module,
                        componentContext);
                if (!mergedJAXWSEnv) {
                    mergeEnvironment(jaxwsBuilder);
                    mergedJAXWSEnv = true;
                }
            } else {
                throw new DeploymentException(serviceInterfaceName
                                              + " does not extend "
                                              + jaxrpcClass.getName() + " or "
                                              + jaxwsClass.getName());
            }
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

    private void mergeEnvironment(ServiceRefBuilder builder) {
        Environment env = builder.getEnvironment();
        if (env != null) {
            EnvironmentBuilder.mergeEnvironments(this.environment, env);
        }
    }

    private Class loadClass(String name, ClassLoader cl)
            throws DeploymentException {
        try {
            return cl.loadClass(name);
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

    public QNameSet getSpecQNameSet() {
        return serviceRefQNameSet;
    }

    public QNameSet getPlanQNameSet() {
        return GER_SERVICE_REF_QNAME_SET;
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

        infoBuilder.setConstructor(new String[] { "eeNamespaces",
                "JAXRPCBuilder", "JAXWSBuilder" });

        GBEAN_INFO = infoBuilder.getBeanInfo();
    }

    public static GBeanInfo getGBeanInfo() {
        return GBEAN_INFO;
    }
}
