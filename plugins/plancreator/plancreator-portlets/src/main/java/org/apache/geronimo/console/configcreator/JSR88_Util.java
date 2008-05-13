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
package org.apache.geronimo.console.configcreator;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.jar.JarFile;

import javax.enterprise.deploy.model.DDBean;
import javax.enterprise.deploy.model.DDBeanRoot;
import javax.enterprise.deploy.model.exceptions.DDBeanCreateException;
import javax.enterprise.deploy.shared.factories.DeploymentFactoryManager;
import javax.enterprise.deploy.spi.DeploymentConfiguration;
import javax.enterprise.deploy.spi.DeploymentManager;
import javax.enterprise.deploy.spi.Target;
import javax.enterprise.deploy.spi.exceptions.ConfigurationException;
import javax.enterprise.deploy.spi.exceptions.DeploymentManagerCreationException;
import javax.enterprise.deploy.spi.exceptions.InvalidModuleException;
import javax.enterprise.deploy.spi.factories.DeploymentFactory;
import javax.enterprise.deploy.spi.status.ProgressObject;
import javax.management.ObjectName;
import javax.portlet.PortletException;
import javax.portlet.PortletRequest;

import org.apache.geronimo.common.DeploymentException;
import org.apache.geronimo.connector.deployment.AdminObjectRefBuilder.AdminObjectRefProcessor;
import org.apache.geronimo.connector.deployment.ResourceRefBuilder.ResourceRefProcessor;
import org.apache.geronimo.console.configcreator.AbstractHandler.ReferenceData;
import org.apache.geronimo.console.configcreator.AbstractHandler.WARConfigData;
import org.apache.geronimo.console.configcreator.JSR77_Util.ReferredData;
import org.apache.geronimo.console.util.PortletManager;
import org.apache.geronimo.deployment.ConfigurationBuilder;
import org.apache.geronimo.deployment.ModuleIDBuilder;
import org.apache.geronimo.deployment.plugin.factories.DeploymentFactoryWithKernel;
import org.apache.geronimo.deployment.plugin.jmx.JMXDeploymentManager;
import org.apache.geronimo.deployment.service.jsr88.EnvironmentData;
import org.apache.geronimo.deployment.tools.loader.WebDeployable;
import org.apache.geronimo.j2ee.ApplicationInfo;
import org.apache.geronimo.j2ee.deployment.Module;
import org.apache.geronimo.j2ee.deployment.annotation.AnnotatedApp;
import org.apache.geronimo.j2ee.deployment.annotation.AnnotatedWebApp;
import org.apache.geronimo.j2ee.deployment.annotation.EJBAnnotationHelper;
import org.apache.geronimo.j2ee.deployment.annotation.HandlerChainAnnotationHelper;
import org.apache.geronimo.j2ee.deployment.annotation.PersistenceContextAnnotationHelper;
import org.apache.geronimo.j2ee.deployment.annotation.PersistenceUnitAnnotationHelper;
import org.apache.geronimo.j2ee.deployment.annotation.ResourceAnnotationHelper;
import org.apache.geronimo.j2ee.deployment.annotation.SecurityAnnotationHelper;
import org.apache.geronimo.j2ee.deployment.annotation.WebServiceRefAnnotationHelper;
import org.apache.geronimo.j2ee.j2eeobjectnames.NameFactory;
import org.apache.geronimo.kernel.Kernel;
import org.apache.geronimo.kernel.config.ConfigurationManager;
import org.apache.geronimo.kernel.repository.Artifact;
import org.apache.geronimo.naming.deployment.EnvironmentEntryBuilder.EnvEntryRefProcessor;
import org.apache.geronimo.naming.deployment.SwitchingServiceRefBuilder.ServiceRefProcessor;
import org.apache.geronimo.naming.deployment.jsr88.EjbLocalRef;
import org.apache.geronimo.naming.deployment.jsr88.EjbRef;
import org.apache.geronimo.naming.deployment.jsr88.MessageDestination;
import org.apache.geronimo.naming.deployment.jsr88.Pattern;
import org.apache.geronimo.naming.deployment.jsr88.ResourceEnvRef;
import org.apache.geronimo.naming.deployment.jsr88.ResourceRef;
import org.apache.geronimo.web.deployment.WebAppDConfigBean;
import org.apache.geronimo.web.deployment.WebAppDConfigRoot;
import org.apache.geronimo.web25.deployment.AbstractWebModuleBuilder;
import org.apache.geronimo.xbeans.geronimo.naming.GerServiceRefType;
import org.apache.geronimo.xbeans.geronimo.security.GerRoleType;
import org.apache.geronimo.xbeans.geronimo.security.GerSecurityType;
import org.apache.geronimo.xbeans.javaee.EjbLocalRefType;
import org.apache.geronimo.xbeans.javaee.EjbRefType;
import org.apache.geronimo.xbeans.javaee.MessageDestinationType;
import org.apache.geronimo.xbeans.javaee.ResourceEnvRefType;
import org.apache.geronimo.xbeans.javaee.ResourceRefType;
import org.apache.geronimo.xbeans.javaee.SecurityRoleType;
import org.apache.geronimo.xbeans.javaee.ServiceRefType;
import org.apache.geronimo.xbeans.javaee.WebAppType;
import org.apache.xbean.finder.ClassFinder;

/**
 * Util class for JSR-88 related functions
 * 
 * @version $Rev$ $Date$
 */
public class JSR88_Util {

    private static void parseAnnotations(AnnotatedApp annotatedApp, ClassFinder classFinder) throws Exception {
        ResourceAnnotationHelper.processAnnotations(annotatedApp, classFinder, EnvEntryRefProcessor.INSTANCE);
        WebServiceRefAnnotationHelper.processAnnotations(annotatedApp, classFinder);
        HandlerChainAnnotationHelper.processAnnotations(annotatedApp, classFinder);
        ResourceAnnotationHelper.processAnnotations(annotatedApp, classFinder, ServiceRefProcessor.INSTANCE);
        ResourceAnnotationHelper.processAnnotations(annotatedApp, classFinder, AdminObjectRefProcessor.INSTANCE);
        ResourceAnnotationHelper.processAnnotations(annotatedApp, classFinder, ResourceRefProcessor.INSTANCE);
        PersistenceContextAnnotationHelper.processAnnotations(annotatedApp, classFinder);
        PersistenceUnitAnnotationHelper.processAnnotations(annotatedApp, classFinder);
        EJBAnnotationHelper.processAnnotations(annotatedApp, classFinder);
        if (annotatedApp instanceof AnnotatedWebApp) {
            SecurityAnnotationHelper.processAnnotations(((AnnotatedWebApp) annotatedApp).getWebApp(), classFinder);
        }
    }

    private static ConfigurationBuilder getConfigurationBuilder(PortletRequest request) {
        Object[] builders = PortletManager.getGBeansImplementing(request, ConfigurationBuilder.class);
        ConfigurationBuilder configurationBuilder = null;
        for (int i = 0; i < builders.length; i++) {
            ObjectName objectName = PortletManager.getNameFor(request, builders[i]).getObjectName();
            if ("EARBuilder".equalsIgnoreCase(objectName.getKeyProperty(NameFactory.J2EE_NAME))) {
                configurationBuilder = (ConfigurationBuilder) builders[i];
                break;
            }
        }
        return configurationBuilder;
    }

    private static ApplicationInfo createApplicationInfo(PortletRequest request, URL moduleUrl) throws Exception {
        ConfigurationBuilder configurationBuilderBuilder = getConfigurationBuilder(request);
        File moduleFile = new File(moduleUrl.getFile());
        JarFile moduleJar = new JarFile(moduleFile);
        ApplicationInfo applicationInfo = (ApplicationInfo) configurationBuilderBuilder.getDeploymentPlan(null, moduleJar,
                new ModuleIDBuilder());
        return applicationInfo;
    }

    public static void parseWarReferences(PortletRequest request, WARConfigData data, URL warUrl)
            throws Exception {
        WebDeployable webDeployable = new WebDeployable(warUrl);
        ClassLoader classLoader = webDeployable.getModuleLoader();

        ApplicationInfo applicationInfo = createApplicationInfo(request, warUrl);
        Module module = (Module) (applicationInfo.getModules().toArray()[0]);
        WebAppType webApp = (WebAppType) module.getSpecDD();

        ClassFinder classFinder = null;
        try {
            classFinder = AbstractWebModuleBuilder.createWebAppClassFinder(webApp, classLoader);
            // classFinder = new ClassFinder(classLoader);
        } catch (DeploymentException e1) {
            // Some of the class types referred in the WAR cannot be resolved.
            // A typical case would be references to EJBs already deployed into the system, and
            // hence not packaged inside WEB-INF/lib directory of WAR.
            // try adding all EJBs deployed in the system as parents of this WAR, and
            // see if referred classes can now be successfully resolved

            classLoader = new WebDeployable(warUrl, getEjbClassLoaders(request)).getModuleLoader();
            try {
                classFinder = AbstractWebModuleBuilder.createWebAppClassFinder(webApp, classLoader);
            } catch (DeploymentException e2) {
                throw e2;
            }
        }
        AnnotatedApp annotatedApp = module.getAnnotatedApp();
        parseAnnotations(annotatedApp, classFinder);

        //DDBeanRoot ddBeanRoot = webDeployable.getDDBeanRoot();
        //DDBean ddBean = ddBeanRoot.getChildBean("web-app")[0];

        EjbRefType[] ejbRefs = annotatedApp.getEjbRefArray();
        for (int i = 0; i < ejbRefs.length; i++) {
            String refName = ejbRefs[i].getEjbRefName().getStringValue();
            data.getEjbRefs().add(new ReferenceData(refName));
        }
        /*DDBean[] ddBeans = ddBean.getChildBean("ejb-ref");
        for (int i = 0; ddBeans != null && i < ddBeans.length; i++) {
            String refName = ddBeans[i].getChildBean("ejb-ref-name")[0].getText();
            data.getEjbRefs().add(new ReferenceData(refName));
        }*/

        EjbLocalRefType[] ejbLocalRefs = annotatedApp.getEjbLocalRefArray();
        for (int i = 0; i < ejbLocalRefs.length; i++) {
            String refName = ejbLocalRefs[i].getEjbRefName().getStringValue();
            data.getEjbLocalRefs().add(new ReferenceData(refName));
        }
        /*ddBeans = ddBean.getChildBean("ejb-local-ref");
        for (int i = 0; ddBeans != null && i < ddBeans.length; i++) {
            String refName = ddBeans[i].getChildBean("ejb-ref-name")[0].getText();
            data.getEjbLocalRefs().add(new ReferenceData(refName));
        }*/

        ResourceRefType[] resourceRefs = annotatedApp.getResourceRefArray();
        for(int i = 0; i < resourceRefs.length; i++) {
            String refName = resourceRefs[i].getResRefName().getStringValue();
            String refType = resourceRefs[i].getResType().getStringValue();
            if ("javax.sql.DataSource".equalsIgnoreCase(refType)) {
                data.getJdbcPoolRefs().add(new ReferenceData(refName));
            } else if ("javax.jms.ConnectionFactory".equalsIgnoreCase(refType)
                    || "javax.jms.QueueConnectionFactory".equalsIgnoreCase(refType)
                    || "javax.jms.TopicConnectionFactory".equalsIgnoreCase(refType)) {
                data.getJmsConnectionFactoryRefs().add(new ReferenceData(refName));
            } else if ("javax.mail.Session".equalsIgnoreCase(refType)) {
                data.getJavaMailSessionRefs().add(new ReferenceData(refName));
            }
        }
        /*ddBeans = ddBean.getChildBean("resource-ref");
        for (int i = 0; ddBeans != null && i < ddBeans.length; i++) {
            String refName = ddBeans[i].getChildBean("res-ref-name")[0].getText();
            String refType = ddBeans[i].getChildBean("res-type")[0].getText();
            if ("javax.sql.DataSource".equalsIgnoreCase(refType)) {
                data.getJdbcPoolRefs().add(new ReferenceData(refName));
            } else if ("javax.jms.ConnectionFactory".equalsIgnoreCase(refType)
                    || "javax.jms.QueueConnectionFactory".equalsIgnoreCase(refType)
                    || "javax.jms.TopicConnectionFactory".equalsIgnoreCase(refType)) {
                data.getJmsConnectionFactoryRefs().add(new ReferenceData(refName));
            }
        }*/

        ServiceRefType[] serviceRefs = annotatedApp.getServiceRefArray();
        for(int i = 0; i < serviceRefs.length; i++) {
            String refName = serviceRefs[i].getServiceRefName().getStringValue();
            GerServiceRefType serviceRef = GerServiceRefType.Factory.newInstance();
            serviceRef.setServiceRefName(refName);
            data.getWebServiceRefs().add(serviceRef);
        }

        ResourceEnvRefType[] resourceEnvRefs = annotatedApp.getResourceEnvRefArray();
        for(int i = 0; i < resourceEnvRefs.length; i++) {
            String refName = resourceEnvRefs[i].getResourceEnvRefName().getStringValue();
            ReferenceData refData = new ReferenceData(refName);
            refData.setRefLink(refName);
            data.getJmsDestinationRefs().add(refData);
        }
        if(annotatedApp instanceof AnnotatedWebApp) {
            WebAppType webApp2 = ((AnnotatedWebApp)annotatedApp).getWebApp();
            MessageDestinationType[] messageDestinations = webApp2.getMessageDestinationArray();
            for(int i = 0; i < messageDestinations.length; i++) {
                String refName = messageDestinations[i].getMessageDestinationName().getStringValue();
                ReferenceData refData = new ReferenceData(refName);
                refData.setRefLink(refName);
                data.getMessageDestinations().add(refData);
            }

            SecurityRoleType[] securityRoles = webApp2.getSecurityRoleArray();
            if (securityRoles.length > 0) {
                data.setSecurity(GerSecurityType.Factory.newInstance());
                data.getSecurity().addNewRoleMappings();
            }
            for (int i = 0; i < securityRoles.length; i++) {
                String roleName = securityRoles[i].getRoleName().getStringValue();
                GerRoleType role = data.getSecurity().getRoleMappings().addNewRole();
                role.setRoleName(roleName);
            }
        }
        /*ddBeans = ddBean.getChildBean("message-destination");
        for (int i = 0; ddBeans != null && i < ddBeans.length; i++) {
            String refName = ddBeans[i].getChildBean("message-destination-name")[0].getText();
            ReferenceData refData = new ReferenceData(refName);
            refData.setRefLink(refName);
            data.getJmsDestinationRefs().add(refData);
        }*/
    }

    private static List getEjbClassLoaders(PortletRequest request) {
        List deployedEjbs = JSR77_Util.getDeployedEJBs(request);
        List configurations = new ArrayList();
        for (int i = 0; i < deployedEjbs.size(); i++) {
            String ejbPatternName = ((ReferredData) deployedEjbs.get(i)).getPatternName();
            configurations.add(getDependencyString(ejbPatternName));
        }
        return getConfigClassLoaders(configurations);
    }

    private static List getConfigClassLoaders(List configurationNames) {
        List classLoaders = new ArrayList();
        ConfigurationManager configurationManager = PortletManager.getConfigurationManager();
        for (int i = 0; i < configurationNames.size(); i++) {
            Artifact configurationId = Artifact.create((String) configurationNames.get(i));
            classLoaders.add(configurationManager.getConfiguration(configurationId).getConfigurationClassLoader());
        }
        return classLoaders;
    }

    public static String getDependencyString(String patternString) {
        String[] elements = patternString.split("/", 6);
        return elements[0] + "/" + elements[1] + "/" + elements[2] + "/" + elements[3];
    }

    public static String createDeploymentPlan(PortletRequest request, WARConfigData data, URL WarUrl)
            throws IOException, DDBeanCreateException, InvalidModuleException, ConfigurationException, DeploymentManagerCreationException {
        WebDeployable webDeployable = new WebDeployable(WarUrl);
        DDBeanRoot ddBeanRoot = webDeployable.getDDBeanRoot();
        DDBean ddBean = ddBeanRoot.getChildBean("web-app")[0];

        Kernel kernel = PortletManager.getKernel();
        DeploymentFactory factory = new DeploymentFactoryWithKernel(kernel);
        DeploymentManager deploymentManager = factory.getDeploymentManager("deployer:geronimo:inVM", null, null);
        DeploymentConfiguration deploymentConfiguration = deploymentManager.createConfiguration(webDeployable);
        WebAppDConfigRoot configRoot = (WebAppDConfigRoot) deploymentConfiguration.getDConfigBeanRoot(ddBeanRoot);
        WebAppDConfigBean webApp = (WebAppDConfigBean) configRoot.getDConfigBean(ddBean);

        webApp.setContextRoot(data.getContextRoot());

        EnvironmentData environment = new EnvironmentData();
        webApp.setEnvironment(environment);
        org.apache.geronimo.deployment.service.jsr88.Artifact configId = new org.apache.geronimo.deployment.service.jsr88.Artifact();
        environment.setConfigId(configId);
        configId.setGroupId(data.getGroupId());
        configId.setArtifactId(data.getArtifactId());
        configId.setVersion(data.getVersion());
        configId.setType(data.getType());
        int numDependencies = data.getDependencies().size();
        if (numDependencies > 0) {
            org.apache.geronimo.deployment.service.jsr88.Artifact[] dependencies = new org.apache.geronimo.deployment.service.jsr88.Artifact[numDependencies];
            for (int i = 0; i < numDependencies; i++) {
                dependencies[i] = new org.apache.geronimo.deployment.service.jsr88.Artifact();
            }
            environment.setDependencies(dependencies);
            for (int i = 0; i < numDependencies; i++) {
                Artifact artifact = Artifact.create(((String) data.getDependencies().get(i)).trim());
                org.apache.geronimo.deployment.service.jsr88.Artifact dep = dependencies[i];
                dep.setArtifactId(artifact.getArtifactId());
                if (artifact.getGroupId() != null) {
                    dep.setGroupId(artifact.getGroupId());
                }
                if (artifact.getType() != null) {
                    dep.setType(artifact.getType());
                }
                if (artifact.getVersion() != null) {
                    dep.setVersion(artifact.getVersion().toString());
                }
            }
        }
        String hiddenClassesString = data.getHiddenClasses();
        if (hiddenClassesString != null && hiddenClassesString.length() > 0) {
            String[] hiddenClasses = getNonEmptyStrings(hiddenClassesString.split(";"));
            if (hiddenClasses.length > 0) {
                environment.setHiddenClasses(hiddenClasses);
            }
        }
        String nonOverridableClassesString = data.getNonOverridableClasses();
        if (nonOverridableClassesString != null && nonOverridableClassesString.length() > 0) {
            String[] nonOverridableClasses = getNonEmptyStrings(nonOverridableClassesString.split(";"));
            if (nonOverridableClasses.length > 0) {
                environment.setNonOverridableClasses(nonOverridableClasses);
            }
        }
        if (data.isInverseClassLoading()) {
            environment.setInverseClassLoading(true);
        }

        int numEjbRefs = data.getEjbRefs().size();
        if (numEjbRefs > 0) {
            EjbRef[] ejbRefs = new EjbRef[numEjbRefs];
            for (int i = 0; i < numEjbRefs; i++) {
                ejbRefs[i] = new EjbRef();
            }
            webApp.setEjbRefs(ejbRefs);
            for (int i = 0; i < numEjbRefs; i++) {
                EjbRef ejbRef = ejbRefs[i];
                ReferenceData referenceData = (ReferenceData) data.getEjbRefs().get(i);
                ejbRef.setRefName(referenceData.getRefName());
                ejbRef.setPattern(createPattern(referenceData.getRefLink()));
            }
        }

        int numEjbLocalRefs = data.getEjbLocalRefs().size();
        if (numEjbLocalRefs > 0) {
            EjbLocalRef[] ejbLocalRefs = new EjbLocalRef[numEjbLocalRefs];
            for (int i = 0; i < numEjbLocalRefs; i++) {
                ejbLocalRefs[i] = new EjbLocalRef();
            }
            webApp.setEjbLocalRefs(ejbLocalRefs);
            for (int i = 0; i < numEjbLocalRefs; i++) {
                EjbLocalRef ejbLocalRef = ejbLocalRefs[i];
                ReferenceData referenceData = (ReferenceData) data.getEjbLocalRefs().get(i);
                ejbLocalRef.setRefName(referenceData.getRefName());
                ejbLocalRef.setPattern(createPattern(referenceData.getRefLink()));
            }
        }

        int numResourceRefs = data.getJdbcPoolRefs().size() + data.getJmsConnectionFactoryRefs().size()
                + data.getJavaMailSessionRefs().size();
        if (numResourceRefs > 0) {
            ResourceRef[] resourceRefs = new ResourceRef[numResourceRefs];
            for (int i = 0; i < numResourceRefs; i++) {
                resourceRefs[i] = new ResourceRef();
            }
            webApp.setResourceRefs(resourceRefs);
            int i = 0;
            for (int l = 0; l < data.getJdbcPoolRefs().size(); l++, i++) {
                ResourceRef resourceRef = resourceRefs[i];
                ReferenceData referenceData = (ReferenceData) data.getJdbcPoolRefs().get(l);
                resourceRef.setRefName(referenceData.getRefName());
                resourceRef.setPattern(createPattern(referenceData.getRefLink()));
            }
            for (int m = 0; m < data.getJmsConnectionFactoryRefs().size(); m++, i++) {
                ResourceRef resourceRef = resourceRefs[i];
                ReferenceData referenceData = (ReferenceData) data.getJmsConnectionFactoryRefs().get(m);
                resourceRef.setRefName(referenceData.getRefName());
                resourceRef.setPattern(createPattern(referenceData.getRefLink()));
            }
            for (int n = 0; n < data.getJavaMailSessionRefs().size(); n++, i++) {
                ResourceRef resourceRef = resourceRefs[i];
                ReferenceData referenceData = (ReferenceData) data.getJavaMailSessionRefs().get(n);
                resourceRef.setRefName(referenceData.getRefName());
                resourceRef.setPattern(createPattern(referenceData.getRefLink()));
            }
        }

        int numMessageDestinations = data.getMessageDestinations().size();
        if (numMessageDestinations > 0) {
            MessageDestination[] messageDestinations = new MessageDestination[numMessageDestinations];
            for (int i = 0; i < numMessageDestinations; i++) {
                messageDestinations[i] = new MessageDestination();
            }
            webApp.setMessageDestinations(messageDestinations);
            for (int i = 0; i < numMessageDestinations; i++) {
                MessageDestination messageDestination = messageDestinations[i];
                ReferenceData referenceData = (ReferenceData) data.getMessageDestinations().get(i);
                messageDestination.setMessageDestinationName(referenceData.getRefName());
                // messageDestination.setPattern(createPattern(referenceData.getRefLink()));
                messageDestination.setAdminObjectLink(createPattern(referenceData.getRefLink()).getName());
            }
        }

        int numResourceEnvRefs = data.getJmsDestinationRefs().size();
        if (numResourceEnvRefs > 0) {
            ResourceEnvRef[] resourceEnvRefs = new ResourceEnvRef[numResourceEnvRefs];
            for (int i = 0; i < numResourceEnvRefs; i++) {
                resourceEnvRefs[i] = new ResourceEnvRef();
            }
            webApp.setResourceEnvRefs(resourceEnvRefs);
            for (int i = 0; i < numResourceEnvRefs; i++) {
                ResourceEnvRef resourceEnvRef = resourceEnvRefs[i];
                ReferenceData referenceData = (ReferenceData) data.getJmsDestinationRefs().get(i);
                resourceEnvRef.setRefName(referenceData.getRefName());
                resourceEnvRef.setPattern(createPattern(referenceData.getRefLink()));
                // resourceEnvRef.setAdminObjectLink(createPattern(referenceData.getRefLink()).getName());
            }
        }

        int numWebServiceRefs = data.getWebServiceRefs().size();
        if (numWebServiceRefs > 0) {
            webApp.setServiceRefs(data.getWebServiceRefs().toArray(new GerServiceRefType[numWebServiceRefs]));
        }

        if (data.getSecurity() != null) {
            webApp.setSecurityRealmName(data.getSecurityRealmName());
            webApp.setSecurity(data.getSecurity());
        }

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        deploymentConfiguration.save(out);
        out.close();
        return new String(out.toByteArray(), "US-ASCII");
    }

    private static Pattern createPattern(String patternString) {
        Pattern pattern = new Pattern();
        String[] elements = patternString.split("/", 6);
        pattern.setGroupId(elements[0]);
        pattern.setArtifactId(elements[1]);
        pattern.setVersion(elements[2]);
        pattern.setType(elements[3]);
        pattern.setModule(elements[4]);
        pattern.setName(elements[5]);
        return pattern;
    }

    private static String[] getNonEmptyStrings(String[] strings) {
        ArrayList list = new ArrayList();
        for (int i = 0; i < strings.length; i++) {
            if (strings[i].trim().length() > 0)
                list.add(strings[i].trim());
        }
        return (String[]) list.toArray(new String[list.size()]);
    }

    public static String[] deploy(PortletRequest actionRequest, File moduleFile, File planFile)
            throws PortletException {
        // TODO this is a duplicate of the code from
        // org.apache.geronimo.console.configmanager.DeploymentPortlet.processAction()
        // TODO need to eliminate this duplicate code
        DeploymentFactoryManager dfm = DeploymentFactoryManager.getInstance();
        String[] statusMsgs = new String[2];
        try {
            DeploymentManager mgr = dfm.getDeploymentManager("deployer:geronimo:inVM", null, null);
            try {
                if (mgr instanceof JMXDeploymentManager) {
                    ((JMXDeploymentManager) mgr).setLogConfiguration(false, true);
                }
                
                Target[] targets = mgr.getTargets();
                if (null == targets) {
                    throw new IllegalStateException("No target to distribute to");
                }
                targets = new Target[] {targets[0]};
                
                ProgressObject progress = mgr.distribute(targets, moduleFile, planFile);
                while (progress.getDeploymentStatus().isRunning()) {
                    Thread.sleep(100);
                }

                String abbrStatusMessage;
                String fullStatusMessage = null;
                if (progress.getDeploymentStatus().isCompleted()) {
                    abbrStatusMessage = "The application was successfully deployed.<br/>";
                    // start installed app/s
                    progress = mgr.start(progress.getResultTargetModuleIDs());
                    while (progress.getDeploymentStatus().isRunning()) {
                        Thread.sleep(100);
                    }
                    abbrStatusMessage += "The application was successfully started";
                } else {
                    fullStatusMessage = progress.getDeploymentStatus().getMessage();
                    // for the abbreviated status message clip off everything
                    // after the first line, which in most cases means the gnarly stacktrace
                    abbrStatusMessage = "Deployment failed:<br/>"
                            + fullStatusMessage.substring(0, fullStatusMessage.indexOf('\n'));
                }
                statusMsgs[0] = abbrStatusMessage;
                statusMsgs[1] = fullStatusMessage;
            } finally {
                mgr.release();
            }
        } catch (Exception e) {
            throw new PortletException(e);
        }
        return statusMsgs;
    }
}
