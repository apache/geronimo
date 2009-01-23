/**
 *
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
package org.apache.geronimo.openejb.deployment;

import java.security.PermissionCollection;
import java.security.Permissions;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.apache.geronimo.common.DeploymentException;
import org.apache.geronimo.connector.outbound.connectiontracking.TrackedConnectionAssociator;
import org.apache.geronimo.gbean.AbstractName;
import org.apache.geronimo.gbean.AbstractNameQuery;
import org.apache.geronimo.gbean.GBeanData;
import org.apache.geronimo.j2ee.deployment.EARContext;
import org.apache.geronimo.j2ee.deployment.NamingBuilder;
import org.apache.geronimo.j2ee.deployment.annotation.AnnotatedEjbJar;
import org.apache.geronimo.j2ee.j2eeobjectnames.NameFactory;
import org.apache.geronimo.kernel.GBeanAlreadyExistsException;
import org.apache.geronimo.naming.deployment.AbstractNamingBuilder;
import org.apache.geronimo.naming.deployment.GBeanResourceEnvironmentBuilder;
import org.apache.geronimo.naming.deployment.ResourceEnvironmentSetter;
import org.apache.geronimo.openejb.EntityDeploymentGBean;
import org.apache.geronimo.openejb.MessageDrivenDeploymentGBean;
import org.apache.geronimo.openejb.OpenEjbSystem;
import org.apache.geronimo.openejb.StatefulDeploymentGBean;
import org.apache.geronimo.openejb.StatelessDeploymentGBean;
import org.apache.geronimo.openejb.xbeans.ejbjar.OpenejbGeronimoEjbJarType;
import org.apache.geronimo.security.deployment.SecurityConfiguration;
import org.apache.geronimo.security.jacc.ComponentPermissions;
import org.apache.geronimo.xbeans.geronimo.naming.GerResourceRefType;
import org.apache.geronimo.xbeans.javaee.EjbJarType;
import org.apache.geronimo.xbeans.javaee.EnterpriseBeansType;
import org.apache.geronimo.xbeans.javaee.EntityBeanType;
import org.apache.geronimo.xbeans.javaee.MessageDrivenBeanType;
import org.apache.geronimo.xbeans.javaee.ResourceRefType;
import org.apache.geronimo.xbeans.javaee.SessionBeanType;
import org.apache.openejb.DeploymentInfo;
import org.apache.openejb.jee.EnterpriseBean;
import org.apache.openejb.jee.EntityBean;
import org.apache.openejb.jee.MessageDrivenBean;
import org.apache.openejb.jee.RemoteBean;
import org.apache.openejb.jee.SecurityIdentity;
import org.apache.openejb.jee.SessionBean;
import org.apache.openejb.jee.SessionType;
import org.apache.openejb.jee.AssemblyDescriptor;
import org.apache.openejb.jee.MethodPermission;
import org.apache.openejb.jee.oejb3.EjbDeployment;
import org.apache.xbean.finder.ClassFinder;
import org.apache.xmlbeans.XmlObject;

/**
 * Handles building ejb deployment gbeans.
 */
public class EjbDeploymentBuilder {
    private final EARContext earContext;
    private final EjbModule ejbModule;
    private final NamingBuilder namingBuilder;
    private final ResourceEnvironmentSetter resourceEnvironmentSetter;
    private final Map<String, GBeanData> gbeans = new TreeMap<String, GBeanData>();

    public EjbDeploymentBuilder(EARContext earContext, EjbModule ejbModule, NamingBuilder namingBuilder, ResourceEnvironmentSetter resourceEnvironmentSetter) {
        this.earContext = earContext;
        this.ejbModule = ejbModule;
        this.namingBuilder = namingBuilder;
        this.resourceEnvironmentSetter = resourceEnvironmentSetter;
    }

    public void initContext() throws DeploymentException {
        for (EnterpriseBean enterpriseBean : ejbModule.getEjbJar().getEnterpriseBeans()) {
            AbstractName abstractName = createEjbName(enterpriseBean);
            GBeanData gbean = null;
            if (enterpriseBean instanceof SessionBean) {
                SessionBean sessionBean = (SessionBean) enterpriseBean;
                switch (sessionBean.getSessionType()) {
                    case STATELESS:
                        gbean = new GBeanData(abstractName, StatelessDeploymentGBean.GBEAN_INFO);
                        break;
                    case STATEFUL:
                        gbean = new GBeanData(abstractName, StatefulDeploymentGBean.GBEAN_INFO);
                        break;
                }
            } else if (enterpriseBean instanceof EntityBean) {
                gbean = new GBeanData(abstractName, EntityDeploymentGBean.GBEAN_INFO);
            } else if (enterpriseBean instanceof MessageDrivenBean) {
                gbean = new GBeanData(abstractName, MessageDrivenDeploymentGBean.GBEAN_INFO);
            }
            if (gbean == null) {
                throw new DeploymentException("Unknown enterprise bean type " + enterpriseBean.getClass().getName());
            }

            String ejbName = enterpriseBean.getEjbName();

            EjbDeployment ejbDeployment = ejbModule.getOpenejbJar().getDeploymentsByEjbName().get(ejbName);
            if (ejbDeployment == null) {
                throw new DeploymentException("OpenEJB configuration not found for ejb " + ejbName);
            }
            gbean.setAttribute("deploymentId", ejbDeployment.getDeploymentId());
            gbean.setAttribute("ejbName", ejbName);

            // set interface class names
            if (enterpriseBean instanceof RemoteBean) {
                RemoteBean remoteBean = (RemoteBean) enterpriseBean;

                // Remote
                if (remoteBean.getRemote() != null) {
                    String remoteInterfaceName = remoteBean.getRemote();
                    assureEJBObjectInterface(remoteInterfaceName, ejbModule.getClassLoader());
                    gbean.setAttribute(EjbInterface.REMOTE.getAttributeName(), remoteInterfaceName);

                    String homeInterfaceName = remoteBean.getHome();
                    assureEJBHomeInterface(homeInterfaceName, ejbModule.getClassLoader());
                    gbean.setAttribute(EjbInterface.HOME.getAttributeName(), homeInterfaceName);
                }

                // Local
                if (remoteBean.getLocal() != null) {
                    String localInterfaceName = remoteBean.getLocal();
                    assureEJBLocalObjectInterface(localInterfaceName, ejbModule.getClassLoader());
                    gbean.setAttribute(EjbInterface.LOCAL.getAttributeName(), localInterfaceName);

                    String localHomeInterfaceName = remoteBean.getLocalHome();
                    assureEJBLocalHomeInterface(localHomeInterfaceName, ejbModule.getClassLoader());
                    gbean.setAttribute(EjbInterface.LOCAL_HOME.getAttributeName(), localHomeInterfaceName);
                }

                if (enterpriseBean instanceof SessionBean && ((SessionBean) enterpriseBean).getSessionType() == SessionType.STATELESS) {
                    SessionBean statelessBean = (SessionBean) enterpriseBean;
                    gbean.setAttribute(EjbInterface.SERVICE_ENDPOINT.getAttributeName(), statelessBean.getServiceEndpoint());
                }
            }

            // set reference patterns
            gbean.setReferencePattern("TrackedConnectionAssociator", new AbstractNameQuery(null, Collections.EMPTY_MAP, TrackedConnectionAssociator.class.getName()));
            gbean.setReferencePattern("OpenEjbSystem", new AbstractNameQuery(null, Collections.EMPTY_MAP, OpenEjbSystem.class.getName()));

            try {
                earContext.addGBean(gbean);
            } catch (GBeanAlreadyExistsException e) {
                throw new DeploymentException("Could not add entity bean to context", e);
            }
            gbeans.put(ejbName, gbean);
        }
    }


    public void addEjbModuleDependency(AbstractName ejbModuleName) {
        for (GBeanData gbean : gbeans.values()) {
            gbean.addDependency(ejbModuleName);
        }
    }

    public ComponentPermissions buildComponentPermissions() throws DeploymentException {
        List<MethodPermission> methodPermissions = ejbModule.getEjbJar().getAssemblyDescriptor().getMethodPermission();
        if (earContext.getSecurityConfiguration() == null && methodPermissions.size() > 0) {
            throw new DeploymentException("Ejb app has method permissions but no security configuration supplied in geronimo plan");
        }
        ComponentPermissions componentPermissions = new ComponentPermissions(new Permissions(), new Permissions(), new HashMap<String, PermissionCollection>());
        for (EnterpriseBean enterpriseBean : ejbModule.getEjbJar().getEnterpriseBeans()) {
            addSecurityData(enterpriseBean, componentPermissions);
        }
        return componentPermissions;
    }

    private void addSecurityData(EnterpriseBean enterpriseBean, ComponentPermissions componentPermissions) throws DeploymentException {
        SecurityConfiguration securityConfiguration = (SecurityConfiguration) earContext.getSecurityConfiguration();
        if (securityConfiguration != null) {
            GBeanData gbean = getEjbGBean(enterpriseBean.getEjbName());
            if (enterpriseBean instanceof RemoteBean) {
                RemoteBean remoteBean = (RemoteBean) enterpriseBean;

                SecurityBuilder securityBuilder = new SecurityBuilder();
                PermissionCollection permissions = new Permissions();

                securityBuilder.addToPermissions(permissions,
                        remoteBean.getEjbName(),
                        EjbInterface.HOME.getJaccInterfaceName(),
                        remoteBean.getHome(),
                        ejbModule.getClassLoader());
                securityBuilder.addToPermissions(permissions,
                        remoteBean.getEjbName(),
                        EjbInterface.REMOTE.getJaccInterfaceName(),
                        remoteBean.getRemote(),
                        ejbModule.getClassLoader());
                securityBuilder.addToPermissions(permissions,
                        remoteBean.getEjbName(),
                        EjbInterface.LOCAL.getJaccInterfaceName(),
                        remoteBean.getLocal(),
                        ejbModule.getClassLoader());
                securityBuilder.addToPermissions(permissions,
                        remoteBean.getEjbName(),
                        EjbInterface.LOCAL_HOME.getJaccInterfaceName(),
                        remoteBean.getLocalHome(),
                        ejbModule.getClassLoader());
                if (remoteBean instanceof SessionBean) {
                    securityBuilder.addToPermissions(permissions,
                            remoteBean.getEjbName(),
                            EjbInterface.SERVICE_ENDPOINT.getJaccInterfaceName(),
                            ((SessionBean) remoteBean).getServiceEndpoint(),
                            ejbModule.getClassLoader());
                }
                if (remoteBean.getBusinessRemote() != null && !remoteBean.getBusinessRemote().isEmpty()) {
                    for (String businessRemote : remoteBean.getBusinessRemote()) {
                        securityBuilder.addToPermissions(permissions,
                                remoteBean.getEjbName(),
                                EjbInterface.REMOTE.getJaccInterfaceName(),
                                businessRemote,
                                ejbModule.getClassLoader());
                    }
                    securityBuilder.addToPermissions(componentPermissions.getUncheckedPermissions(),
                            remoteBean.getEjbName(),
                            EjbInterface.HOME.getJaccInterfaceName(),
                            DeploymentInfo.BusinessRemoteHome.class.getName(),
                            ejbModule.getClassLoader());
                }
                if (remoteBean.getBusinessLocal() != null && !remoteBean.getBusinessLocal().isEmpty()) {
                    for (String businessLocal : remoteBean.getBusinessLocal()) {
                        securityBuilder.addToPermissions(permissions,
                                remoteBean.getEjbName(),
                                EjbInterface.LOCAL.getJaccInterfaceName(),
                                businessLocal,
                                ejbModule.getClassLoader());
                    }
                    securityBuilder.addToPermissions(componentPermissions.getUncheckedPermissions(),
                            remoteBean.getEjbName(),
                            EjbInterface.LOCAL_HOME.getJaccInterfaceName(),
                            DeploymentInfo.BusinessLocalHome.class.getName(),
                            ejbModule.getClassLoader());
                }

                String defaultRole = securityConfiguration.getDefaultRole();
                securityBuilder.addComponentPermissions(defaultRole,
                        permissions,
                        ejbModule.getEjbJar().getAssemblyDescriptor(),
                        enterpriseBean.getEjbName(),
                        remoteBean.getSecurityRoleRef(),
                        componentPermissions);

            }
            // RunAs subject
            SecurityIdentity securityIdentity = enterpriseBean.getSecurityIdentity();
            if (securityIdentity != null && securityIdentity.getRunAs() != null) {
                String runAsName = securityIdentity.getRunAs();
                if (runAsName != null) {
                    gbean.setAttribute("runAsRole", runAsName);
                }
            }

            gbean.setAttribute("securityEnabled", true);
            gbean.setReferencePattern("RunAsSource", earContext.getJaccManagerName());
        }
    }

    public void buildEnc() throws DeploymentException {
        //
        // XMLBeans types must be use because Geronimo naming building is coupled via XMLBeans objects
        //

        EjbJarType ejbJarType = (EjbJarType) ejbModule.getSpecDD();

        if (!ejbJarType.getMetadataComplete()) {
            // Create a classfinder and populate it for the naming builder(s). The absence of a
            // classFinder in the module will convey whether metadata-complete is set (or not)
            ejbModule.setClassFinder(createEjbJarClassFinder(ejbModule));
        }

        EnterpriseBeansType enterpriseBeans = ejbJarType.getEnterpriseBeans();
        if (enterpriseBeans != null) {
            for (SessionBeanType xmlbeansEjb : enterpriseBeans.getSessionArray()) {
                String ejbName = xmlbeansEjb.getEjbName().getStringValue().trim();
                GBeanData gbean = getEjbGBean(ejbName);
                ResourceRefType[] resourceRefs = xmlbeansEjb.getResourceRefArray();
                addEnc(gbean, xmlbeansEjb, resourceRefs);
            }
            for (MessageDrivenBeanType xmlbeansEjb : enterpriseBeans.getMessageDrivenArray()) {
                String ejbName = xmlbeansEjb.getEjbName().getStringValue().trim();
                GBeanData gbean = getEjbGBean(ejbName);
                ResourceRefType[] resourceRefs = xmlbeansEjb.getResourceRefArray();
                addEnc(gbean, xmlbeansEjb, resourceRefs);
            }
            for (EntityBeanType xmlbeansEjb : enterpriseBeans.getEntityArray()) {
                String ejbName = xmlbeansEjb.getEjbName().getStringValue().trim();
                GBeanData gbean = getEjbGBean(ejbName);
                ResourceRefType[] resourceRefs = xmlbeansEjb.getResourceRefArray();
                addEnc(gbean, xmlbeansEjb, resourceRefs);
            }

        }

        if (!ejbJarType.getMetadataComplete()) {
            ejbJarType.setMetadataComplete(true);
            ejbModule.setOriginalSpecDD(ejbModule.getSpecDD().toString());
        }
    }

    private void addEnc(GBeanData gbean, XmlObject xmlbeansEjb, ResourceRefType[] resourceRefs) throws DeploymentException {
        OpenejbGeronimoEjbJarType geronimoOpenejb = ejbModule.getVendorDD();

        //
        // Build ENC
        //

        // Geronimo uses a map to pass data to the naming build and for the results data
        Map<Object, Object> buildingContext = new HashMap<Object, Object>();
        buildingContext.put(NamingBuilder.GBEAN_NAME_KEY, gbean.getAbstractName());
        ((AnnotatedEjbJar) ejbModule.getAnnotatedApp()).setBean(xmlbeansEjb);

        namingBuilder.buildNaming(xmlbeansEjb,
                geronimoOpenejb,
                ejbModule, buildingContext);

        Map compContext = NamingBuilder.JNDI_KEY.get(buildingContext);
        gbean.setAttribute("componentContextMap", compContext);

        //
        // Process resource refs
        //
        GerResourceRefType[] gerResourceRefs = null;

        if (geronimoOpenejb != null) {
            gerResourceRefs = geronimoOpenejb.getResourceRefArray();
        }

        GBeanResourceEnvironmentBuilder refBuilder = new GBeanResourceEnvironmentBuilder(gbean);
        resourceEnvironmentSetter.setResourceEnvironment(refBuilder, resourceRefs, gerResourceRefs);
    }

    private ClassFinder createEjbJarClassFinder(EjbModule ejbModule) throws DeploymentException {

        try {
            // Get the classloader from the module's EARContext
            ClassLoader classLoader = ejbModule.getEarContext().getClassLoader();

            //----------------------------------------------------------------------------------------
            // Find the list of classes from the ejb-jar.xml we want to search for annotations in
            //----------------------------------------------------------------------------------------
            List<Class> classes = new ArrayList<Class>();

            for (EnterpriseBean bean : ejbModule.getEjbJar().getEnterpriseBeans()) {
                classes.add(classLoader.loadClass(bean.getEjbClass()));
            }

            return new ClassFinder(classes);
        } catch (ClassNotFoundException e) {
            throw new DeploymentException("Unable to load bean class.", e);
        }
    }

    private GBeanData getEjbGBean(String ejbName) throws DeploymentException {
        GBeanData gbean = gbeans.get(ejbName);
        if (gbean == null) throw new DeploymentException("EJB not gbean not found " + ejbName);
        return gbean;
    }

    private AbstractName createEjbName(EnterpriseBean enterpriseBean) {
        String ejbName = enterpriseBean.getEjbName();
        String type = null;
        if (enterpriseBean instanceof SessionBean) {
            SessionBean sessionBean = (SessionBean) enterpriseBean;
            switch (sessionBean.getSessionType()) {
                case STATELESS:
                    type = NameFactory.STATELESS_SESSION_BEAN;
                    break;
                case STATEFUL:
                    type = NameFactory.STATEFUL_SESSION_BEAN;
                    break;
            }
        } else if (enterpriseBean instanceof EntityBean) {
            type = NameFactory.ENTITY_BEAN;
        } else if (enterpriseBean instanceof MessageDrivenBean) {
            type = NameFactory.MESSAGE_DRIVEN_BEAN;
        }
        if (type == null) {
            throw new IllegalArgumentException("Unknown enterprise bean type XXX " + enterpriseBean.getClass().getName());
        }
        return earContext.getNaming().createChildName(ejbModule.getModuleName(), ejbName, type);
    }

    private static Class assureEJBObjectInterface(String remote, ClassLoader cl) throws DeploymentException {
        return AbstractNamingBuilder.assureInterface(remote, "javax.ejb.EJBObject", "Remote", cl);
    }

    private static Class assureEJBHomeInterface(String home, ClassLoader cl) throws DeploymentException {
        return AbstractNamingBuilder.assureInterface(home, "javax.ejb.EJBHome", "Home", cl);
    }

    public static Class assureEJBLocalObjectInterface(String local, ClassLoader cl) throws DeploymentException {
        return AbstractNamingBuilder.assureInterface(local, "javax.ejb.EJBLocalObject", "Local", cl);
    }

    public static Class assureEJBLocalHomeInterface(String localHome, ClassLoader cl) throws DeploymentException {
        return AbstractNamingBuilder.assureInterface(localHome, "javax.ejb.EJBLocalHome", "LocalHome", cl);
    }
}
