/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.apache.geronimo.jaxws.ejb.builder;

import java.net.URL;
import java.security.PermissionCollection;
import java.security.Permissions;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.jar.JarFile;

import javax.security.jacc.WebResourcePermission;
import javax.security.jacc.WebUserDataPermission;

import org.apache.geronimo.common.DeploymentException;
import org.apache.geronimo.deployment.ModuleIDBuilder;
import org.apache.geronimo.deployment.service.EnvironmentBuilder;
import org.apache.geronimo.gbean.AbstractName;
import org.apache.geronimo.gbean.AbstractNameQuery;
import org.apache.geronimo.gbean.GBeanData;
import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.gbean.GBeanInfoBuilder;
import org.apache.geronimo.gbean.ReferencePatterns;
import org.apache.geronimo.j2ee.deployment.EARContext;
import org.apache.geronimo.j2ee.deployment.Module;
import org.apache.geronimo.j2ee.deployment.ModuleBuilderExtension;
import org.apache.geronimo.j2ee.deployment.WebServiceBuilder;
import org.apache.geronimo.j2ee.j2eeobjectnames.NameFactory;
import org.apache.geronimo.kernel.GBeanAlreadyExistsException;
import org.apache.geronimo.kernel.Kernel;
import org.apache.geronimo.kernel.Naming;
import org.apache.geronimo.kernel.config.ConfigurationModuleType;
import org.apache.geronimo.kernel.config.ConfigurationStore;
import org.apache.geronimo.kernel.repository.Environment;
import org.apache.geronimo.openejb.deployment.EjbModule;
import org.apache.geronimo.security.jaas.ConfigurationFactory;
import org.apache.geronimo.security.jacc.ComponentPermissions;
import org.apache.openejb.assembler.classic.EnterpriseBeanInfo;
import org.apache.openejb.jee.oejb2.AuthMethodType;
import org.apache.openejb.jee.oejb2.EnterpriseBean;
import org.apache.openejb.jee.oejb2.GeronimoEjbJarType;
import org.apache.openejb.jee.oejb2.OpenejbJarType;
import org.apache.openejb.jee.oejb2.SessionBeanType;
import org.apache.openejb.jee.oejb2.WebServiceBindingType;
import org.apache.openejb.jee.oejb2.WebServiceSecurityType;
import org.osgi.framework.Bundle;

/**
 * @version $Rev$ $Date$
 */
public class JAXWSEJBModuleBuilderExtension implements ModuleBuilderExtension {

    protected WebServiceBuilder jaxwsBuilder;
    protected AbstractNameQuery listener;
    protected GBeanInfo wsGBeanInfo;
    protected GBeanData wsGBeanData;
    protected Environment defaultEnvironment;

    public JAXWSEJBModuleBuilderExtension() throws Exception {
    }

    public JAXWSEJBModuleBuilderExtension(WebServiceBuilder wsBuilder,
                                          Environment defaultEnvironment,
                                          AbstractNameQuery listener,
                                          Object dataLink,
                                          Kernel kernel) throws Exception {
        this.jaxwsBuilder = wsBuilder;
        this.listener = listener;
        this.defaultEnvironment = defaultEnvironment;

        AbstractName webServiceLinkTemplateName = kernel.getAbstractNameFor(dataLink);
        this.wsGBeanInfo = kernel.getGBeanInfo(webServiceLinkTemplateName);
        this.wsGBeanData = kernel.getGBeanData(webServiceLinkTemplateName);
    }

    public void createModule(Module module, Bundle bundle, Naming naming, ModuleIDBuilder idBuilder) throws DeploymentException {
    }

    public void createModule(Module module, Object plan, JarFile moduleFile, String targetPath, URL specDDUrl, Environment environment, Object moduleContextInfo, AbstractName earName, Naming naming, ModuleIDBuilder idBuilder) throws DeploymentException {
        if (this.defaultEnvironment != null) {
            EnvironmentBuilder.mergeEnvironments(environment, this.defaultEnvironment);
        }
    }

    public void installModule(JarFile earFile, EARContext earContext, Module module, Collection configurationStores, ConfigurationStore targetConfigurationStore, Collection repository) throws DeploymentException {
    }

    public void initContext(EARContext earContext, Module module, Bundle bundle) throws DeploymentException {
        if (module.getType() != ConfigurationModuleType.EJB) {
            return;
        }

        EjbModule ejbModule = (EjbModule) module;
        Environment environment = module.getEnvironment();

        //overridden web service locations
        Map<String, String> correctedPortLocations = new HashMap<String, String>();
        Map<String, WebServiceBinding> wsBindingMap = createWebServiceBindingMap(ejbModule);
        for (Map.Entry<String, WebServiceBinding> entry : wsBindingMap.entrySet()) {
            String location = entry.getValue().getWebServiceAddress();
            if (location != null) {
                location = location.trim();
                if (!location.startsWith("/")) {
                    location = "/" + location;
                }
                correctedPortLocations.put(entry.getKey(), location);
            }
        }

        jaxwsBuilder.findWebServices(module, true, correctedPortLocations, environment, ejbModule.getSharedContext());

        for (EnterpriseBeanInfo bean : ejbModule.getEjbJarInfo().enterpriseBeans) {

            String j2eeType = null;
            if (bean.type == EnterpriseBeanInfo.STATELESS) {
                j2eeType = NameFactory.STATELESS_SESSION_BEAN;
            } else if (bean.type == EnterpriseBeanInfo.SINGLETON) {
                j2eeType = NameFactory.SINGLETON_BEAN;
            } else {
                continue;
            }

            String ejbName = bean.ejbName;

            AbstractName sessionName = earContext.getNaming().createChildName(module.getModuleName(), ejbName, j2eeType);

            assert sessionName != null: "StatelesSessionBean/Singleton object name is null";

            WebServiceBinding wsBinding = wsBindingMap.get(ejbName);
            if (wsBinding != null) {

                WebServiceSecurityType wsSecurity = wsBinding.getWebServiceSecurity();
                if (wsSecurity != null) {
                    earContext.setHasSecurity(true);
                    String policyContextID = sessionName.toString();
                    Properties properties = wsSecurity.getProperties();
                    PermissionCollection uncheckedPermissions = new Permissions();
                    String transportGuarantee = wsSecurity.getTransportGuarantee().toString().trim();
                    boolean getProtected = properties.get("getProtected") == null? true: Boolean.valueOf((String) properties.get("getProtected"));
                    if (getProtected) {
                        WebUserDataPermission webUserDataPermission = new WebUserDataPermission("/*", null, transportGuarantee);
                        uncheckedPermissions.add(webUserDataPermission);
                    } else {
                        uncheckedPermissions.add(new WebUserDataPermission("/*", new String[] {"GET"}, "NONE"));
                        uncheckedPermissions.add(new WebUserDataPermission("/*", "!GET:" + transportGuarantee));
                    }
                    Map<String, PermissionCollection> rolePermissions = new HashMap<String, PermissionCollection>();
                    //TODO allow jaspi authentication
                    boolean secured = wsSecurity.getAuthMethod() != null && AuthMethodType.NONE != (wsSecurity.getAuthMethod());// || wsSecurity.isSetAuthentication();
                    if (secured) {
                        boolean getSecured = properties.get("getSecured") == null? true: Boolean.valueOf((String) properties.get("getSecured"));
                        if (!getSecured) {
                            uncheckedPermissions.add(new WebResourcePermission("/*", "GET"));
                        }
                    } else {
                        uncheckedPermissions.add(new WebResourcePermission("/*", (String[]) null));
                    }
                    ComponentPermissions permissions = new ComponentPermissions(new Permissions(), uncheckedPermissions, rolePermissions);
                    earContext.addSecurityContext(policyContextID, permissions);

                }
            }

        }

    }

    public void addGBeans(EARContext earContext, Module module, Bundle bundle, Collection repository) throws DeploymentException {
        if (module.getType() != ConfigurationModuleType.EJB) {
            return;
        }

        EjbModule ejbModule = (EjbModule) module;

        Map<String, WebServiceBinding> wsBindingMap = createWebServiceBindingMap(ejbModule);

        for (EnterpriseBeanInfo bean : ejbModule.getEjbJarInfo().enterpriseBeans) {
            String j2eeType = null;
            if (bean.type == EnterpriseBeanInfo.STATELESS) {
                j2eeType = NameFactory.STATELESS_SESSION_BEAN;
            } else if (bean.type == EnterpriseBeanInfo.SINGLETON) {
                j2eeType = NameFactory.SINGLETON_BEAN;
            } else {
                continue;
            }

            String ejbName = bean.ejbName;

            AbstractName sessionName = earContext.getNaming().createChildName(module.getModuleName(), ejbName, j2eeType);

            assert sessionName != null: "StatelesSessionBean/SingletonBean object name is null";

            AbstractName ejbWebServiceName = earContext.getNaming().createChildName(sessionName, ejbName, NameFactory.WEB_SERVICE_LINK);

            GBeanData ejbWebServiceGBean = new GBeanData(ejbWebServiceName, this.wsGBeanInfo);

            ejbWebServiceGBean.setAttribute("ejbName", ejbName);
            ejbWebServiceGBean.setAttribute("ejbClass", bean.ejbClass);

            WebServiceBinding wsBinding = wsBindingMap.get(ejbName);
            if (wsBinding != null) {
                List<String> ddVirtualHosts = wsBinding.getWebServiceVirtualHost();
                if (ddVirtualHosts != null) {
                    String[] virtualHosts = new String[ddVirtualHosts.size()];
                    for (int i=0; i<ddVirtualHosts.size(); i++) {
                        virtualHosts[i] = ddVirtualHosts.get(i).trim();
                    }
                    ejbWebServiceGBean.setAttribute("virtualHosts", virtualHosts);
                }

                WebServiceSecurityType wsSecurity = wsBinding.getWebServiceSecurity();
                if (wsSecurity != null) {
                    Properties properties = wsSecurity.getProperties();

                    ejbWebServiceGBean.setReferencePattern("ConfigurationFactory",
                            new AbstractNameQuery(null, Collections.singletonMap("name", wsSecurity.getSecurityRealmName().trim()),
                            ConfigurationFactory.class.getName()));
                    String authMethod = wsSecurity.getAuthMethod().value();
                    ejbWebServiceGBean.setAttribute("authMethod", authMethod);
                    if (wsSecurity.getRealmName() != null) {
                        ejbWebServiceGBean.setAttribute("realmName", wsSecurity.getRealmName().trim());
                    }
                    String policyContextID = sessionName.toString();
                    ejbWebServiceGBean.setAttribute("policyContextID", policyContextID);
                    ejbWebServiceGBean.setAttribute("properties", properties);
                }
            }

            ejbWebServiceGBean.addDependency(module.getModuleName());

            if (jaxwsBuilder.configureEJB(ejbWebServiceGBean, bean.ejbName, ejbModule,
                                          ejbModule.getSharedContext(), bundle)) {

                try {
                    earContext.addGBean(ejbWebServiceGBean);
                } catch (GBeanAlreadyExistsException e) {
                    throw new DeploymentException(
                            "Could not add ejb web service gbean to context",
                            e);
                }

                ReferencePatterns patterns = this.wsGBeanData.getReferencePatterns("WebServiceContainer");
                if (patterns != null) {
                    ejbWebServiceGBean.setReferencePatterns("WebServiceContainer", patterns);
                }

                ejbWebServiceGBean.setReferencePattern("EjbDeployment", sessionName);
            }

            ejbWebServiceGBean.clearAttribute("ejbName");
            ejbWebServiceGBean.clearAttribute("ejbClass");

        }
    }

    private Map<String, WebServiceBinding> createWebServiceBindingMap(EjbModule ejbModule) {
        Map<String, WebServiceBinding> wsBindingMap = new HashMap<String, WebServiceBinding>();

        Object openejbDD = ejbModule.getEjbModule().getAltDDs().get("openejb-jar.xml");
        if (openejbDD instanceof OpenejbJarType) {
            OpenejbJarType openejb = (OpenejbJarType) openejbDD;
            for (EnterpriseBean bean : openejb.getEnterpriseBeans()) {
                if (bean instanceof SessionBeanType) {
                    SessionBeanType sessioBean = (SessionBeanType) bean;
                    wsBindingMap.put(bean.getEjbName(), new WebServiceBinding(sessioBean));
                }
            }
        } else {
            GeronimoEjbJarType geronimoEjbJarType =
                (GeronimoEjbJarType) ejbModule.getEjbModule().getAltDDs().get("geronimo-openejb.xml");
            if (geronimoEjbJarType != null) {
                for (WebServiceBindingType bt : geronimoEjbJarType.getWebServiceBinding()) {
                    wsBindingMap.put(bt.getEjbName(), new WebServiceBinding(bt));
                }
            }
        }

        return wsBindingMap;
    }

    private static class WebServiceBinding {

        private String address;
        private List<String> virtualHosts;
        private WebServiceSecurityType security;

        private WebServiceBinding(SessionBeanType bean) {
            address = bean.getWebServiceAddress();
            virtualHosts = bean.getWebServiceVirtualHost();
            security = bean.getWebServiceSecurity();
        }

        private WebServiceBinding(WebServiceBindingType bt) {
            address = bt.getWebServiceAddress();
            virtualHosts = bt.getWebServiceVirtualHost();
            if (bt.getWebServiceSecurity() != null) {
                security = new WebServiceSecurityType();
                security.setAuthMethod(bt.getWebServiceSecurity().getAuthMethod());
                security.setRealmName(bt.getWebServiceSecurity().getRealmName());
                security.setSecurityRealmName(bt.getWebServiceSecurity().getSecurityRealmName());
                security.setTransportGuarantee(bt.getWebServiceSecurity().getTransportGuarantee());
            }
        }

        public String getWebServiceAddress() {
            return address;
        }

        public List<String> getWebServiceVirtualHost() {
            return virtualHosts;
        }

        public WebServiceSecurityType getWebServiceSecurity() {
            return security;
        }
    }

    public static final GBeanInfo GBEAN_INFO;

    static {
        GBeanInfoBuilder infoBuilder = GBeanInfoBuilder.createStatic(JAXWSEJBModuleBuilderExtension.class, NameFactory.MODULE_BUILDER);
        infoBuilder.addInterface(ModuleBuilderExtension.class);
        infoBuilder.addReference("WebServiceBuilder", WebServiceBuilder.class, NameFactory.MODULE_BUILDER);
        infoBuilder.addAttribute("defaultEnvironment", Environment.class, true, true);
        infoBuilder.addAttribute("listener", AbstractNameQuery.class, true);
        infoBuilder.addReference("WebServiceLinkTemplate", Object.class, NameFactory.WEB_SERVICE_LINK);
        infoBuilder.addAttribute("kernel", Kernel.class, false);

        infoBuilder.setConstructor(new String[]{
                "WebServiceBuilder",
                "defaultEnvironment",
                "listener",
                "WebServiceLinkTemplate",
                "kernel"
        });

        GBEAN_INFO = infoBuilder.getBeanInfo();
    }

    public static GBeanInfo getGBeanInfo() {
        return GBEAN_INFO;
    }

}
