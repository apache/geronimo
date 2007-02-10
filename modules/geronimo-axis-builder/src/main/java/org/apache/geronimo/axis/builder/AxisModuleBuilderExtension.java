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

import org.apache.geronimo.j2ee.deployment.ModuleBuilderExtension;
import org.apache.geronimo.j2ee.deployment.EARContext;
import org.apache.geronimo.j2ee.deployment.Module;
import org.apache.geronimo.j2ee.j2eeobjectnames.NameFactory;
import org.apache.geronimo.common.DeploymentException;
import org.apache.geronimo.kernel.Naming;
import org.apache.geronimo.kernel.GBeanAlreadyExistsException;
import org.apache.geronimo.kernel.config.ConfigurationStore;
import org.apache.geronimo.kernel.config.ConfigurationModuleType;
import org.apache.geronimo.kernel.repository.Environment;
import org.apache.geronimo.deployment.ModuleIDBuilder;
import org.apache.geronimo.gbean.AbstractName;
import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.gbean.GBeanInfoBuilder;
import org.apache.geronimo.gbean.GBeanData;
import org.apache.geronimo.gbean.AbstractNameQuery;
import org.apache.geronimo.axis.server.EjbWebServiceGBean;
import org.apache.geronimo.openejb.deployment.EjbModule;
import org.apache.openejb.assembler.classic.EnterpriseBeanInfo;

import java.util.Collection;
import java.util.Map;
import java.util.HashMap;
import java.util.jar.JarFile;
import java.io.File;
import java.net.URL;

/**
 * @version $Rev$ $Date$
 */
public class AxisModuleBuilderExtension implements ModuleBuilderExtension {

    private final AxisBuilder axisBuilder;

    public AxisModuleBuilderExtension() {
        this(null);
    }

    public AxisModuleBuilderExtension(Environment defaultEnvironment) {
        axisBuilder = new AxisBuilder(defaultEnvironment);
    }

    public void createModule(Module module, File plan, JarFile moduleFile, Naming naming, ModuleIDBuilder idBuilder) throws DeploymentException {
    }

    public void createModule(Module module, Object plan, JarFile moduleFile, String targetPath, URL specDDUrl, Environment environment, Object moduleContextInfo, AbstractName earName, Naming naming, ModuleIDBuilder idBuilder) throws DeploymentException {
        if (module.getType() != ConfigurationModuleType.EJB) {
            return;
        }

        //overridden web service locations
        Map correctedPortLocations = new HashMap();
        EjbModule ejbModule = (EjbModule) module;

//        OpenejbSessionBeanType[] openejbSessionBeans = openejbJar.getEnterpriseBeans().getSessionArray();
//        for (int i = 0; i < openejbSessionBeans.length; i++) {
//            OpenejbSessionBeanType sessionBean = openejbSessionBeans[i];
//                if (sessionBean.isSetWebServiceAddress()) {
//                    String location = sessionBean.getWebServiceAddress().trim();
//                    correctedPortLocations.put(sessionBean.getEjbName(), location);
//                }
//        }

        axisBuilder.findWebServices(moduleFile, true, correctedPortLocations, environment, ejbModule.getSharedContext());
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

        for (EnterpriseBeanInfo bean : ejbModule.getEjbJarInfo().enterpriseBeans) {
            if (bean.type != EnterpriseBeanInfo.STATELESS) continue;
            String ejbName = bean.ejbName;

            AbstractName sessionName = earContext.getNaming().createChildName(module.getModuleName(), ejbName, NameFactory.STATELESS_SESSION_BEAN);

            assert sessionName != null: "StatelesSessionBean object name is null";

            AbstractName ejbWebServiceName = earContext.getNaming().createChildName(sessionName, ejbName, NameFactory.WEB_SERVICE_LINK);

            GBeanData ejbWebServiceGBean = new GBeanData(ejbWebServiceName, EjbWebServiceGBean.GBEAN_INFO);

            axisBuilder.configureEJB(ejbWebServiceGBean, ejbName, ejbModule.getModuleFile(), ejbModule.getSharedContext(), cl);

            ejbWebServiceGBean.setReferencePattern("EjbDeployment", sessionName);

            //configure the security part and references
//            OpenejbWebServiceSecurityType webServiceSecurity = openejbSessionBean == null ? null : openejbSessionBean.getWebServiceSecurity();
//            if (webServiceSecurity != null) {
//                ejbWebServiceGBean.setAttribute("securityRealmName", webServiceSecurity.getSecurityRealmName().trim());
//                ejbWebServiceGBean.setAttribute("realmName", webServiceSecurity.isSetRealmName() ? webServiceSecurity.getRealmName().trim() : XmlBeansSessionBuilder.DEFAULT_AUTH_REALM_NAME);
//                ejbWebServiceGBean.setAttribute("transportGuarantee", webServiceSecurity.getTransportGuarantee().toString());
//                ejbWebServiceGBean.setAttribute("authMethod", webServiceSecurity.getAuthMethod().toString());
//            }
//            if (openejbSessionBean != null) {
//                String[] virtualHosts = openejbSessionBean.getWebServiceVirtualHostArray();
//                for (int i = 0; i < virtualHosts.length; i++) {
//                    virtualHosts[i] = virtualHosts[i].trim();
//                }
//                ejbWebServiceGBean.setAttribute("virtualHosts", virtualHosts);
//            }

            try {
                earContext.addGBean(ejbWebServiceGBean);
            } catch (GBeanAlreadyExistsException e) {
                throw new DeploymentException("Could not add axis ejb web service gbean to context", e);
            }
        }
    }


    public static final GBeanInfo GBEAN_INFO;

    static {
        GBeanInfoBuilder infoBuilder = GBeanInfoBuilder.createStatic(AxisModuleBuilderExtension.class, NameFactory.MODULE_BUILDER);
        infoBuilder.addInterface(ModuleBuilderExtension.class);
        infoBuilder.addAttribute("defaultEnvironment", Environment.class, true, true);

        infoBuilder.setConstructor(new String[]{"defaultEnvironment"});

        GBEAN_INFO = infoBuilder.getBeanInfo();
    }

    public static GBeanInfo getGBeanInfo() {
        return GBEAN_INFO;
    }

}
