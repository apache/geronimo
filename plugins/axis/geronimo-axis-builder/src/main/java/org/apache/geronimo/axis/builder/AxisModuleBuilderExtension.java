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
package org.apache.geronimo.axis.builder;

import java.net.URL;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.jar.JarFile;

import org.apache.geronimo.axis.server.EjbWebServiceGBean;
import org.apache.geronimo.common.DeploymentException;
import org.apache.geronimo.deployment.ModuleIDBuilder;
import org.apache.geronimo.deployment.service.EnvironmentBuilder;
import org.apache.geronimo.gbean.AbstractName;
import org.apache.geronimo.gbean.AbstractNameQuery;
import org.apache.geronimo.gbean.GBeanData;
import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.gbean.GBeanInfoBuilder;
import org.apache.geronimo.j2ee.deployment.EARContext;
import org.apache.geronimo.j2ee.deployment.Module;
import org.apache.geronimo.j2ee.deployment.ModuleBuilderExtension;
import org.apache.geronimo.j2ee.deployment.WebServiceBuilder;
import org.apache.geronimo.j2ee.j2eeobjectnames.NameFactory;
import org.apache.geronimo.kernel.GBeanAlreadyExistsException;
import org.apache.geronimo.kernel.Naming;
import org.apache.geronimo.kernel.config.ConfigurationModuleType;
import org.apache.geronimo.kernel.config.ConfigurationStore;
import org.apache.geronimo.kernel.repository.Environment;
import org.apache.geronimo.openejb.deployment.EjbModule;
import org.apache.openejb.assembler.classic.EnterpriseBeanInfo;
import org.apache.openejb.jee.oejb2.GeronimoEjbJarType;
import org.apache.openejb.jee.oejb2.WebServiceBindingType;
import org.apache.openejb.jee.oejb2.WebServiceBindingType.WebServiceSecurityType;

/**
 * @version $Rev$ $Date$
 */
public class AxisModuleBuilderExtension implements ModuleBuilderExtension {

    private WebServiceBuilder axisBuilder;
    private Environment defaultEnvironment;
    private AbstractNameQuery listener;   

    public AxisModuleBuilderExtension() {
        this(null, null, null);
    }

    public AxisModuleBuilderExtension(WebServiceBuilder wsBuilder,
                                      Environment defaultEnvironment,
                                      AbstractNameQuery listener) {
        this.axisBuilder = wsBuilder;
        this.defaultEnvironment = defaultEnvironment;
        this.listener = listener;
    }

    public void createModule(Module module, Object plan, JarFile moduleFile, String targetPath, URL specDDUrl, Environment environment, Object moduleContextInfo, AbstractName earName, Naming naming, ModuleIDBuilder idBuilder) throws DeploymentException {
        if (module.getType() != ConfigurationModuleType.EJB) {
            return;
        }

        EjbModule ejbModule = (EjbModule) module;
        
        //overridden web service locations
        Map correctedPortLocations = new HashMap();         
        GeronimoEjbJarType geronimoEjbJarType = 
            (GeronimoEjbJarType) ejbModule.getEjbModule().getAltDDs().get("geronimo-openejb.xml");
        if (geronimoEjbJarType != null) {
           for (WebServiceBindingType bt : geronimoEjbJarType.getWebServiceBinding()) {
               String location = bt.getWebServiceAddress();
               if (location != null) {
                   location = location.trim();
                   if (!location.startsWith("/")) {
                       location = "/" + location;
                   }
                   correctedPortLocations.put(bt.getEjbName(), location);
               }
           }
        }

        axisBuilder.findWebServices(module, true, correctedPortLocations, environment, ejbModule.getSharedContext());
        
        if (this.defaultEnvironment != null) {
            EnvironmentBuilder.mergeEnvironments(environment, this.defaultEnvironment);
        } 
    }

    public void installModule(JarFile earFile, EARContext earContext, Module module, Collection configurationStores, ConfigurationStore targetConfigurationStore, Collection repository) throws DeploymentException {
    }

    public void initContext(EARContext earContext, Module module, ClassLoader cl) throws DeploymentException {
    }

    public void addGBeans(EARContext earContext, Module module, ClassLoader cl, Collection repository) throws DeploymentException {
        if (module.getType() != ConfigurationModuleType.EJB) {
            return;
        }

        EjbModule ejbModule = (EjbModule) module;
        
        Map<String, WebServiceBindingType> wsBindingMap = 
            createWebServiceBindingMap(ejbModule);

        for (EnterpriseBeanInfo bean : ejbModule.getEjbJarInfo().enterpriseBeans) {
            if (bean.type != EnterpriseBeanInfo.STATELESS) {
                continue;
            }
            String ejbName = bean.ejbName;

            AbstractName sessionName = earContext.getNaming().createChildName(module.getModuleName(), ejbName, NameFactory.STATELESS_SESSION_BEAN);

            assert sessionName != null: "StatelesSessionBean object name is null";

            AbstractName ejbWebServiceName = earContext.getNaming().createChildName(sessionName, ejbName, NameFactory.WEB_SERVICE_LINK);

            GBeanData ejbWebServiceGBean = new GBeanData(ejbWebServiceName, EjbWebServiceGBean.GBEAN_INFO);

            ejbWebServiceGBean.setAttribute("ejbName", ejbName);
            ejbWebServiceGBean.setAttribute("ejbClass", bean.ejbClass);
            
            WebServiceBindingType wsBinding = wsBindingMap.get(ejbName);
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
                    ejbWebServiceGBean.setAttribute("securityRealmName", wsSecurity.getSecurityRealmName().trim());
                    ejbWebServiceGBean.setAttribute("transportGuarantee", wsSecurity.getTransportGuarantee().toString());
                    ejbWebServiceGBean.setAttribute("authMethod", wsSecurity.getAuthMethod().value());
                    if (wsSecurity.getRealmName() != null) {
                        ejbWebServiceGBean.setAttribute("realmName", wsSecurity.getRealmName().trim());                    
                    }
                    List<String> methods = wsSecurity.getHttpMethod();
                    if (methods != null && !methods.isEmpty()) {
                        String[] protectedMethods = new String[methods.size()];
                        protectedMethods = methods.toArray(protectedMethods);                    
                        ejbWebServiceGBean.setAttribute("protectedMethods", protectedMethods);
                    }
                }
            }
            
            if (axisBuilder.configureEJB(ejbWebServiceGBean, ejbName, ejbModule,
                                         ejbModule.getSharedContext(), cl)) {
                
                try {
                    earContext.addGBean(ejbWebServiceGBean);
                } catch (GBeanAlreadyExistsException e) {
                    throw new DeploymentException(
                            "Could not add axis ejb web service gbean to context", e);
                }
                
                if (this.listener != null) {
                    ejbWebServiceGBean.setReferencePattern("WebServiceContainer", this.listener);
                }
                
                ejbWebServiceGBean.setReferencePattern("EjbDeployment", sessionName);
            }

            ejbWebServiceGBean.clearAttribute("ejbName");
            ejbWebServiceGBean.clearAttribute("ejbClass");
            
        }
    }

    private Map<String, WebServiceBindingType> createWebServiceBindingMap(EjbModule ejbModule) {
        Map<String, WebServiceBindingType> wsBindingMap = 
            new HashMap<String, WebServiceBindingType>();
        GeronimoEjbJarType geronimoEjbJarType = 
            (GeronimoEjbJarType) ejbModule.getEjbModule().getAltDDs().get("geronimo-openejb.xml");
        if (geronimoEjbJarType != null) {
            for (WebServiceBindingType bt : geronimoEjbJarType.getWebServiceBinding()) {
                wsBindingMap.put(bt.getEjbName(), bt);
            }
        }
        return wsBindingMap;
    }
    
    public static final GBeanInfo GBEAN_INFO;

    static {
        GBeanInfoBuilder infoBuilder = GBeanInfoBuilder.createStatic(AxisModuleBuilderExtension.class, NameFactory.MODULE_BUILDER);
        infoBuilder.addInterface(ModuleBuilderExtension.class);
        infoBuilder.addReference("WebServiceBuilder", WebServiceBuilder.class, NameFactory.MODULE_BUILDER);
        infoBuilder.addAttribute("defaultEnvironment", Environment.class, true, true);
        infoBuilder.addAttribute("listener", AbstractNameQuery.class, true);

        infoBuilder.setConstructor(new String[]{"WebServiceBuilder","defaultEnvironment","listener"});

        GBEAN_INFO = infoBuilder.getBeanInfo();
    }

    public static GBeanInfo getGBeanInfo() {
        return GBEAN_INFO;
    }

}
