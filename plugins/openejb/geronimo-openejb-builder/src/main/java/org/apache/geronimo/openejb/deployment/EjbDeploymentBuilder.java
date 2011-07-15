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

import java.security.Permission;
import java.security.PermissionCollection;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import javax.naming.NamingException;
import javax.naming.Reference;
import org.apache.geronimo.common.DeploymentException;
import org.apache.geronimo.connector.outbound.connectiontracking.TrackedConnectionAssociator;
import org.apache.geronimo.gbean.AbstractName;
import org.apache.geronimo.gbean.AbstractNameQuery;
import org.apache.geronimo.gbean.GBeanData;
import org.apache.geronimo.j2ee.deployment.ApplicationInfo;
import org.apache.geronimo.j2ee.deployment.EARContext;
import org.apache.geronimo.j2ee.deployment.Module;
import org.apache.geronimo.j2ee.deployment.NamingBuilder;
import org.apache.geronimo.j2ee.j2eeobjectnames.NameFactory;
import org.apache.geronimo.j2ee.jndi.JndiKey;
import org.apache.geronimo.j2ee.jndi.JndiScope;
import org.apache.geronimo.kernel.GBeanAlreadyExistsException;
import org.apache.geronimo.naming.deployment.AbstractNamingBuilder;
import org.apache.geronimo.naming.deployment.GBeanResourceEnvironmentBuilder;
import org.apache.geronimo.naming.deployment.ResourceEnvironmentSetter;
import org.apache.geronimo.openejb.EntityDeploymentGBean;
import org.apache.geronimo.openejb.ManagedDeploymentGBean;
import org.apache.geronimo.openejb.MessageDrivenDeploymentGBean;
import org.apache.geronimo.openejb.OpenEjbSystem;
import org.apache.geronimo.openejb.SingletonDeploymentGBean;
import org.apache.geronimo.openejb.StatefulDeploymentGBean;
import org.apache.geronimo.openejb.StatelessDeploymentGBean;
import org.apache.geronimo.openejb.xbeans.ejbjar.OpenejbGeronimoEjbJarType;
import org.apache.geronimo.security.deployment.GeronimoSecurityBuilderImpl;
import org.apache.geronimo.security.deployment.SecurityConfiguration;
import org.apache.geronimo.security.jacc.ComponentPermissions;
import org.apache.geronimo.xbeans.geronimo.naming.GerResourceRefType;
import org.apache.openejb.BeanContext;
import org.apache.openejb.InterfaceType;
import org.apache.openejb.assembler.classic.EjbJarInfo;
import org.apache.openejb.assembler.classic.EnterpriseBeanInfo;
import org.apache.openejb.core.ivm.naming.IntraVmJndiReference;
import org.apache.openejb.jee.EjbJar;
import org.apache.openejb.jee.EnterpriseBean;
import org.apache.openejb.jee.EntityBean;
import org.apache.openejb.jee.ManagedBean;
import org.apache.openejb.jee.MessageDrivenBean;
import org.apache.openejb.jee.MethodPermission;
import org.apache.openejb.jee.RemoteBean;
import org.apache.openejb.jee.ResourceRef;
import org.apache.openejb.jee.SecurityIdentity;
import org.apache.openejb.jee.SessionBean;
import org.apache.openejb.jee.SessionType;
import org.apache.openejb.jee.TransactionType;
import org.apache.openejb.jee.oejb3.EjbDeployment;
import org.apache.xbean.finder.ClassFinder;
import org.osgi.framework.Bundle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.apache.openejb.assembler.classic.JndiBuilder.format;

/**
 * Handles building ejb deployment gbeans.
 */
public class EjbDeploymentBuilder {
    private static final Logger log = LoggerFactory.getLogger(EjbDeploymentBuilder.class);
    
    private final EARContext earContext;
    private final EjbModule ejbModule;
    private final NamingBuilder namingBuilder;
    private final ResourceEnvironmentSetter resourceEnvironmentSetter;
    private final EjbDeploymentGBeanNameBuilder beanNameBuilder;
    private final Map<String, GBeanData> gbeans = new TreeMap<String, GBeanData>();

    public EjbDeploymentBuilder(EARContext earContext, EjbModule ejbModule, NamingBuilder namingBuilder, ResourceEnvironmentSetter resourceEnvironmentSetter) {
        this.earContext = earContext;
        this.ejbModule = ejbModule;
        this.namingBuilder = namingBuilder;
        this.resourceEnvironmentSetter = resourceEnvironmentSetter;

        beanNameBuilder = new BasicEjbDeploymentGBeanNameBuilder();
    }

    public void initContext() throws DeploymentException {
        for (EnterpriseBean enterpriseBean : ejbModule.getEjbJar().getEnterpriseBeans()) {
            AbstractName abstractName = beanNameBuilder.createEjbName(earContext, ejbModule, enterpriseBean);
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
                    case SINGLETON:
                        gbean = new GBeanData(abstractName, SingletonDeploymentGBean.GBEAN_INFO);
                        break;
                    case MANAGED:
                        gbean = new GBeanData(abstractName, ManagedDeploymentGBean.GBEAN_INFO);
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
                    assureEJBObjectInterface(remoteInterfaceName, earContext.getDeploymentBundle());
                    gbean.setAttribute(EjbInterface.REMOTE.getAttributeName(), remoteInterfaceName);

                    String homeInterfaceName = remoteBean.getHome();
                    assureEJBHomeInterface(homeInterfaceName, earContext.getDeploymentBundle());
                    gbean.setAttribute(EjbInterface.HOME.getAttributeName(), homeInterfaceName);
                }

                // Local
                if (remoteBean.getLocal() != null) {
                    String localInterfaceName = remoteBean.getLocal();
                    assureEJBLocalObjectInterface(localInterfaceName, earContext.getDeploymentBundle());
                    gbean.setAttribute(EjbInterface.LOCAL.getAttributeName(), localInterfaceName);

                    String localHomeInterfaceName = remoteBean.getLocalHome();
                    assureEJBLocalHomeInterface(localHomeInterfaceName, earContext.getDeploymentBundle());
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
                ejbModule.addGBean(gbean);
            } catch (GBeanAlreadyExistsException e) {
                throw new DeploymentException("Could not add entity bean to context", e);
            }
            gbeans.put(ejbName, gbean);
        }
    }

    private static Set<AbstractName> getResourceDependencies(EARContext earContext) {
        AbstractNameQuery cfNameQuery = new AbstractNameQuery(earContext.getConfigID(), Collections.singletonMap(NameFactory.J2EE_TYPE, NameFactory.JCA_MANAGED_CONNECTION_FACTORY));
        AbstractNameQuery aoNameQuery = new AbstractNameQuery(earContext.getConfigID(), Collections.singletonMap(NameFactory.J2EE_TYPE, NameFactory.JCA_ADMIN_OBJECT));
        AbstractNameQuery raNameQuery = new AbstractNameQuery(earContext.getConfigID(), Collections.singletonMap(NameFactory.J2EE_TYPE, NameFactory.JCA_RESOURCE_ADAPTER));
        Set<AbstractName> dependencies = new HashSet<AbstractName>();
        dependencies.addAll(earContext.findGBeans(cfNameQuery));
        dependencies.addAll(earContext.findGBeans(aoNameQuery));
        dependencies.addAll(earContext.findGBeans(raNameQuery));
        return dependencies;
    }

    public void addEjbModuleDependency(GBeanData ejbModule) {
        Set<AbstractName> resourceDependencies = getResourceDependencies(earContext);
        for (GBeanData gbean : gbeans.values()) {
            ejbModule.addDependency(gbean.getAbstractName());
            gbean.addDependencies(resourceDependencies);
        }
    }

    public void buildComponentPermissions(ComponentPermissions componentPermissions) throws DeploymentException {
        List<MethodPermission> methodPermissions = ejbModule.getEjbJar().getAssemblyDescriptor().getMethodPermission();
        if (earContext.getSecurityConfiguration() != null) {
            earContext.setHasSecurity(true);
        }
        if (earContext.getSecurityConfiguration() == null && methodPermissions.size() > 0) {
            throw new DeploymentException("Ejb app has method permissions but no security configuration supplied in geronimo plan");
        }
        for (EnterpriseBean enterpriseBean : ejbModule.getEjbJar().getEnterpriseBeans()) {
            addSecurityData(enterpriseBean, componentPermissions);
        }
    }

    private void addSecurityData(EnterpriseBean enterpriseBean, ComponentPermissions componentPermissions) throws DeploymentException {
        SecurityConfiguration securityConfiguration = (SecurityConfiguration) earContext.getSecurityConfiguration();
        if (securityConfiguration != null) {
            GBeanData gbean = getEjbGBean(enterpriseBean.getEjbName());
            SecurityBuilder securityBuilder = new SecurityBuilder();
            Collection<Permission> allPermissions = new HashSet<Permission>();
            if (enterpriseBean instanceof RemoteBean) {
                RemoteBean remoteBean = (RemoteBean) enterpriseBean;


                securityBuilder.addToPermissions(allPermissions,
                        remoteBean.getEjbName(),
                        EjbInterface.HOME.getJaccInterfaceName(),
                        remoteBean.getHome(),
                        ejbModule.getClassLoader());
                securityBuilder.addToPermissions(allPermissions,
                        remoteBean.getEjbName(),
                        EjbInterface.REMOTE.getJaccInterfaceName(),
                        remoteBean.getRemote(),
                        ejbModule.getClassLoader());
                securityBuilder.addToPermissions(allPermissions,
                        remoteBean.getEjbName(),
                        EjbInterface.LOCAL.getJaccInterfaceName(),
                        remoteBean.getLocal(),
                        ejbModule.getClassLoader());
                securityBuilder.addToPermissions(allPermissions,
                        remoteBean.getEjbName(),
                        EjbInterface.LOCAL_HOME.getJaccInterfaceName(),
                        remoteBean.getLocalHome(),
                        ejbModule.getClassLoader());
                if (remoteBean instanceof SessionBean) {
                    securityBuilder.addToPermissions(allPermissions,
                            remoteBean.getEjbName(),
                            EjbInterface.SERVICE_ENDPOINT.getJaccInterfaceName(),
                            ((SessionBean) remoteBean).getServiceEndpoint(),
                            ejbModule.getClassLoader());
                }
                if (remoteBean.getBusinessRemote() != null && !remoteBean.getBusinessRemote().isEmpty()) {
                    for (String businessRemote : remoteBean.getBusinessRemote()) {
                        securityBuilder.addToPermissions(allPermissions,
                                remoteBean.getEjbName(),
                                EjbInterface.REMOTE.getJaccInterfaceName(),
                                businessRemote,
                                ejbModule.getClassLoader());
                    }
                    securityBuilder.addToPermissions(new PermissionCollectionAdapter(componentPermissions.getUncheckedPermissions()),
                            remoteBean.getEjbName(),
                            EjbInterface.HOME.getJaccInterfaceName(),
                            BeanContext.BusinessRemoteHome.class.getName(),
                            ejbModule.getClassLoader());
                }
                if (remoteBean.getBusinessLocal() != null && !remoteBean.getBusinessLocal().isEmpty()) {
                    for (String businessLocal : remoteBean.getBusinessLocal()) {
                        securityBuilder.addToPermissions(allPermissions,
                                remoteBean.getEjbName(),
                                EjbInterface.LOCAL.getJaccInterfaceName(),
                                businessLocal,
                                ejbModule.getClassLoader());
                    }
                    securityBuilder.addToPermissions(new PermissionCollectionAdapter(componentPermissions.getUncheckedPermissions()),
                            remoteBean.getEjbName(),
                            EjbInterface.LOCAL_HOME.getJaccInterfaceName(),
                            BeanContext.BusinessLocalHome.class.getName(),
                            ejbModule.getClassLoader());
                }

            }
            securityBuilder.addEjbTimeout(enterpriseBean, ejbModule, allPermissions);

            String defaultRole = securityConfiguration.getDefaultRole();
            securityBuilder.addComponentPermissions(defaultRole,
                    allPermissions,
                    ejbModule.getEjbJar().getAssemblyDescriptor(),
                    enterpriseBean.getEjbName(),
                    enterpriseBean.getSecurityRoleRef(),
                    componentPermissions);

            // RunAs subject
            SecurityIdentity securityIdentity = enterpriseBean.getSecurityIdentity();
            if (securityIdentity != null && securityIdentity.getRunAs() != null) {
                String runAsName = securityIdentity.getRunAs();
                if (runAsName != null) {
                    gbean.setAttribute("runAsRole", runAsName);
                }
            }

            gbean.setAttribute("securityEnabled", true);
            gbean.setReferencePattern("RunAsSource", GeronimoSecurityBuilderImpl.ROLE_MAPPER_DATA_NAME.get(earContext.getGeneralData()));
        }
    }

    private static class PermissionCollectionAdapter implements Collection<Permission> {
        private final PermissionCollection p;

        private PermissionCollectionAdapter(PermissionCollection p) {
            this.p = p;
        }

        public int size() {
            throw new RuntimeException("not implemented");
        }

        public boolean isEmpty() {
            throw new RuntimeException("not implemented");
        }

        public boolean contains(Object o) {
            throw new RuntimeException("not implemented");
        }

        public Iterator<Permission> iterator() {
            throw new RuntimeException("not implemented");
        }

        public Object[] toArray() {
            throw new RuntimeException("not implemented");
        }

        public <T> T[] toArray(T[] a) {
            throw new RuntimeException("not implemented");
        }

        public boolean add(Permission o) {
            if (p.implies(o)) return false;
            p.add(o);
            return true;
        }

        public boolean remove(Object o) {
            throw new RuntimeException("not implemented");
        }

        public boolean containsAll(Collection<?> c) {
            throw new RuntimeException("not implemented");
        }

        public boolean addAll(Collection<? extends Permission> c) {
            throw new RuntimeException("not implemented");
        }

        public boolean removeAll(Collection<?> c) {
            throw new RuntimeException("not implemented");
        }

        public boolean retainAll(Collection<?> c) {
            throw new RuntimeException("not implemented");
        }

        public void clear() {
            throw new RuntimeException("not implemented");
        }
    }

    public void buildEnc() throws DeploymentException {
        //
        // XMLBeans types must be use because Geronimo naming building is coupled via XMLBeans objects
        //

        EjbJar ejbJar =  ejbModule.getSpecDD();

        if (!ejbJar.isMetadataComplete()) {
            // Create a classfinder and populate it for the naming builder(s). The absence of a
            // classFinder in the module will convey whether metadata-complete is set (or not)
//            ejbModule.setClassFinder(createEjbJarClassFinder(ejbModule));
        }
        
        String appName = null;
        
        Module parentModule = ejbModule.getParentModule();

        while (parentModule != null) {
            // only when the ejb module is part of ear, add the AppName in the ejb's global JNDI name.
            if (parentModule instanceof ApplicationInfo) {
                appName = (String) ejbModule.getJndiScope(JndiScope.app).get("app/AppName");
                break;
            }
            parentModule = parentModule.getParentModule();
        }
        
        EjbJarInfo ejbJarInfo = ejbModule.getEjbJarInfo();
        for (EnterpriseBean bean : ejbJar.getEnterpriseBeans()) {
            String ejbName = bean.getEjbName();
            String deploymentId = getDeploymentId(ejbName, ejbJarInfo);
            GBeanData gbean = getEjbGBean(ejbName);
            addEnc(gbean, bean, appName, deploymentId);
        }

        OpenejbGeronimoEjbJarType geronimoOpenejb = ejbModule.getVendorDD();
        for (EnterpriseBean bean: ejbJar.getEnterpriseBeans()) {
            String ejbName = bean.getEjbName().trim();
            GBeanData gbean = getEjbGBean(ejbName);
            Collection<ResourceRef> resourceRefs = bean.getResourceRef();
            processResourceEnvironment(gbean, resourceRefs, geronimoOpenejb);
        }


        if (!ejbJar.isMetadataComplete()) {
            ejbJar.setMetadataComplete(true);
            ejbModule.setOriginalSpecDD(ejbModule.getSpecDD().toString());
        }
    }

    private String getDeploymentId(String ejbName, EjbJarInfo ejbJarInfo) throws DeploymentException {
        for (EnterpriseBeanInfo info: ejbJarInfo.enterpriseBeans) {
            if (ejbName.equals(info.ejbName)) {
                return info.ejbDeploymentId;
            }
        }
        throw new DeploymentException("EnterpriseBeanInfo not found for ejb: " + ejbName);
    }

    private void addEnc(GBeanData gbean, EnterpriseBean bean, String appName, String deploymentId) throws DeploymentException {

        //
        // Build ENC
        //
        Map<EARContext.Key, Object> buildingContext = new HashMap<EARContext.Key, Object>();
        buildingContext.put(NamingBuilder.GBEAN_NAME_KEY, gbean.getAbstractName());
        Class<?> ejbClass;
        try {
            ejbClass = ejbModule.getEarContext().getDeploymentBundle().loadClass(bean.getEjbClass());
        } catch (ClassNotFoundException e) {
            throw new DeploymentException("Could not load ejb class", e);
        }
        List<Class<?>> classes = new ArrayList<Class<?>>();
        while (ejbClass != null && !ejbClass.equals(Object.class)) {
            classes.add(ejbClass);
            ejbClass = ejbClass.getSuperclass();
        }

        ClassFinder finder = new ClassFinder(classes);

        Module module = ejbModule.newEJb(finder, bean);
        bind(bean, appName, ejbModule.getName(), deploymentId, module.getJndiContext());

        OpenejbGeronimoEjbJarType geronimoOpenejb = ejbModule.getVendorDD();
        namingBuilder.buildNaming(bean,
                geronimoOpenejb,
                module,
                buildingContext);

        AbstractName applicationJndiName = (AbstractName)earContext.getGeneralData().get(EARContext.APPLICATION_JNDI_NAME_KEY);
        gbean.setReferencePattern("ApplicationJndi", applicationJndiName);
        gbean.setAttribute("moduleContextMap", module.getJndiScope(JndiScope.module));
        gbean.setAttribute("componentContextMap", module.getJndiScope(JndiScope.comp));
        
        gbean.setReferencePattern("TransactionManager", earContext.getTransactionManagerName());
        gbean.setAttribute("beanManagedTransactions", bean.getTransactionType() == TransactionType.BEAN);
        
        //
        // Process resource refs
        //
    }

    private void processResourceEnvironment(GBeanData gbean, Collection<ResourceRef> resourceRefs, OpenejbGeronimoEjbJarType geronimoOpenejb) throws DeploymentException {
        GerResourceRefType[] gerResourceRefs = null;

        if (geronimoOpenejb != null) {
            gerResourceRefs = geronimoOpenejb.getResourceRefArray();
        }

        GBeanResourceEnvironmentBuilder refBuilder = new GBeanResourceEnvironmentBuilder(gbean);
        resourceEnvironmentSetter.setResourceEnvironment(refBuilder, resourceRefs, gerResourceRefs);
    }

    public void bind(EnterpriseBean bean, String appName, String moduleName, String id, Map<JndiKey, Map<String, Object>> jndiContext) {


        appName = (appName == null || appName.isEmpty())? "": appName + "/";
        moduleName = moduleName + "/";
        String beanName = bean.getEjbName();
        int count = 0;
        Reference singleRef = null;

        if (bean instanceof RemoteBean) {
            try {
                String homeInterface = ((RemoteBean) bean).getHome();
                if (homeInterface != null) {

                    String name = "openejb/Deployment/" + format(id, homeInterface, InterfaceType.EJB_HOME);
                    Reference ref = new IntraVmJndiReference(name);
                    count ++;
                    singleRef = ref;
                    bindJava(appName, moduleName, beanName, homeInterface, ref, jndiContext);
                }
            } catch (NamingException e) {
                throw new RuntimeException("Unable to bind remote home interface for deployment " + id, e);
            }
            try {
                String localHomeInterface = ((RemoteBean) bean).getLocalHome();
                if (localHomeInterface != null) {

                    String name = "openejb/Deployment/" + format(id, localHomeInterface, InterfaceType.EJB_LOCAL_HOME);
                    Reference ref = new IntraVmJndiReference(name);
                    count++;
                    singleRef = ref;
                    bindJava(appName, moduleName, beanName, localHomeInterface, ref, jndiContext);
                }
            } catch (NamingException e) {
                throw new RuntimeException("Unable to bind local home interface for deployment " + id, e);
            }

            try {
                for (String interfce : ((RemoteBean) bean).getBusinessLocal()) {

                    String name = "openejb/Deployment/" + format(id, interfce, InterfaceType.BUSINESS_LOCAL);
                    Reference ref = new IntraVmJndiReference(name);
                    count++;
                    singleRef = ref;
                    bindJava(appName, moduleName, beanName, interfce, ref, jndiContext);
                }
            } catch (NamingException e) {
                throw new RuntimeException("Unable to bind business local interface for deployment " + id, e);
            }

            try {
                for (String interfce : ((RemoteBean) bean).getBusinessRemote()) {

                    String name = "openejb/Deployment/" + format(id, interfce, InterfaceType.BUSINESS_REMOTE);
                    Reference ref = new IntraVmJndiReference(name);
                    count++;
                    singleRef = ref;
                    bindJava(appName, moduleName, beanName, interfce, ref, jndiContext);
                }
            } catch (NamingException e) {
                throw new RuntimeException("Unable to bind business remote deployment in jndi.", e);
            }

        }

        try {
            if (bean instanceof SessionBean && ( ((SessionBean)bean).getLocalBean() != null) || bean instanceof ManagedBean ) {
                String beanClass = bean.getEjbClass();

                String name = "openejb/Deployment/" + format(id, beanClass, InterfaceType.BUSINESS_LOCALBEAN_HOME);
                Reference ref = new IntraVmJndiReference(name);
                count ++;
                singleRef = ref;
                bindJava(appName, moduleName, beanName, beanClass, ref, jndiContext);
            }
        } catch (NamingException e) {
            throw new RuntimeException("Unable to bind business remote deployment in jndi.", e);
        }
        if (count == 1 || bean instanceof ManagedBean) {
            try {
                bindJava(appName, moduleName, beanName, null, singleRef, jndiContext);
            } catch (NamingException e) {
                throw new RuntimeException("Unable to single interface in jndi.", e);
            }
        }
    }

    private void bindJava(String appName, String moduleName, String beanName, String interfaceName, Reference ref, Map<JndiKey, Map<String, Object>> contexts) throws NamingException {
         if (interfaceName != null) {
             beanName = beanName + "!" + interfaceName;
         }
        bind("global", appName + moduleName + beanName, ref, contexts);
        bind("app", moduleName + beanName, ref, contexts);
        bind("module", beanName, ref, contexts);
    }

    private void bind(String context, String name, Object object,  Map<JndiKey, Map<String, Object>> contexts) throws NamingException {
        JndiKey jndiKey = JndiScope.valueOf(context);
        Map<String, Object> scope = contexts.get(jndiKey);
        if (scope == null) {
            scope = new HashMap<String, Object>();
            contexts.put(jndiKey, scope);
        }
        String fullName = context + "/" + name;
        scope.put(fullName, object);
        log.debug("bound at " + fullName + " reference " + object);
    }

    private GBeanData getEjbGBean(String ejbName) throws DeploymentException {
        GBeanData gbean = gbeans.get(ejbName);
        if (gbean == null) throw new DeploymentException("EJB not gbean not found " + ejbName);
        return gbean;
    }

    private static Class assureEJBObjectInterface(String remote, Bundle bundle) throws DeploymentException {
        return AbstractNamingBuilder.assureInterface(remote, "javax.ejb.EJBObject", "Remote", bundle);
    }

    private static Class assureEJBHomeInterface(String home, Bundle bundle) throws DeploymentException {
        return AbstractNamingBuilder.assureInterface(home, "javax.ejb.EJBHome", "Home", bundle);
    }

    public static Class assureEJBLocalObjectInterface(String local, Bundle bundle) throws DeploymentException {
        return AbstractNamingBuilder.assureInterface(local, "javax.ejb.EJBLocalObject", "Local", bundle);
    }

    public static Class assureEJBLocalHomeInterface(String localHome, Bundle bundle) throws DeploymentException {
        return AbstractNamingBuilder.assureInterface(localHome, "javax.ejb.EJBLocalHome", "LocalHome", bundle);
    }
}
