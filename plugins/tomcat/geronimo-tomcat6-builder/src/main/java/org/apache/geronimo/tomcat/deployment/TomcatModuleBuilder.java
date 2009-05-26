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

package org.apache.geronimo.tomcat.deployment;

import static java.lang.Boolean.FALSE;
import static java.lang.Boolean.TRUE;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;
import java.security.PermissionCollection;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.Collections;
import java.util.jar.JarFile;

import javax.servlet.Servlet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.geronimo.common.DeploymentException;
import org.apache.geronimo.deployment.ModuleIDBuilder;
import org.apache.geronimo.deployment.NamespaceDrivenBuilder;
import org.apache.geronimo.deployment.NamespaceDrivenBuilderCollection;
import org.apache.geronimo.deployment.service.EnvironmentBuilder;
import org.apache.geronimo.deployment.util.DeploymentUtil;
import org.apache.geronimo.deployment.xbeans.EnvironmentType;
import org.apache.geronimo.deployment.xmlbeans.XmlBeansUtil;
import org.apache.geronimo.gbean.AbstractName;
import org.apache.geronimo.gbean.AbstractNameQuery;
import org.apache.geronimo.gbean.GBeanData;
import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.gbean.GBeanInfoBuilder;
import org.apache.geronimo.gbean.ReferencePatterns;
import org.apache.geronimo.gbean.GBeanLifecycle;
import org.apache.geronimo.j2ee.deployment.EARContext;
import org.apache.geronimo.j2ee.deployment.Module;
import org.apache.geronimo.j2ee.deployment.ModuleBuilder;
import org.apache.geronimo.j2ee.deployment.ModuleBuilderExtension;
import org.apache.geronimo.j2ee.deployment.NamingBuilder;
import org.apache.geronimo.j2ee.deployment.WebModule;
import org.apache.geronimo.j2ee.deployment.WebServiceBuilder;
import org.apache.geronimo.j2ee.deployment.annotation.AnnotatedWebApp;
import org.apache.geronimo.j2ee.j2eeobjectnames.NameFactory;
import org.apache.geronimo.kernel.Kernel;
import org.apache.geronimo.kernel.Naming;
import org.apache.geronimo.kernel.config.ConfigurationData;
import org.apache.geronimo.kernel.repository.Environment;
import org.apache.geronimo.naming.deployment.ENCConfigBuilder;
import org.apache.geronimo.naming.deployment.GBeanResourceEnvironmentBuilder;
import org.apache.geronimo.naming.deployment.ResourceEnvironmentSetter;
import org.apache.geronimo.security.jacc.ComponentPermissions;
import org.apache.geronimo.security.jaas.ConfigurationFactory;
import org.apache.geronimo.tomcat.LifecycleListenerGBean;
import org.apache.geronimo.tomcat.ManagerGBean;
import org.apache.geronimo.tomcat.RealmGBean;
import org.apache.geronimo.tomcat.TomcatWebAppContext;
import org.apache.geronimo.tomcat.ValveGBean;
import org.apache.geronimo.tomcat.cluster.CatalinaClusterGBean;
import org.apache.geronimo.tomcat.util.SecurityHolder;
import org.apache.geronimo.web.deployment.GenericToSpecificPlanConverter;
import org.apache.geronimo.web25.deployment.AbstractWebModuleBuilder;
import org.apache.geronimo.xbeans.geronimo.j2ee.GerClusteringDocument;
import org.apache.geronimo.xbeans.geronimo.web.tomcat.TomcatWebAppDocument;
import org.apache.geronimo.xbeans.geronimo.web.tomcat.TomcatWebAppType;
import org.apache.geronimo.xbeans.geronimo.web.tomcat.config.GerTomcatDocument;
import org.apache.geronimo.xbeans.javaee.EjbLocalRefType;
import org.apache.geronimo.xbeans.javaee.EjbRefType;
import org.apache.geronimo.xbeans.javaee.EnvEntryType;
import org.apache.geronimo.xbeans.javaee.LifecycleCallbackType;
import org.apache.geronimo.xbeans.javaee.MessageDestinationRefType;
import org.apache.geronimo.xbeans.javaee.MessageDestinationType;
import org.apache.geronimo.xbeans.javaee.PersistenceContextRefType;
import org.apache.geronimo.xbeans.javaee.PersistenceUnitRefType;
import org.apache.geronimo.xbeans.javaee.ResourceEnvRefType;
import org.apache.geronimo.xbeans.javaee.ResourceRefType;
import org.apache.geronimo.xbeans.javaee.ServiceRefType;
import org.apache.geronimo.xbeans.javaee.ServletType;
import org.apache.geronimo.xbeans.javaee.WebAppDocument;
import org.apache.geronimo.xbeans.javaee.WebAppType;
import org.apache.xmlbeans.XmlCursor;
import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlObject;
import org.apache.xmlbeans.XmlOptions;

/**
 * @version $Rev:385659 $ $Date$
 */
public class TomcatModuleBuilder extends AbstractWebModuleBuilder implements GBeanLifecycle {

    private static final Logger log = LoggerFactory.getLogger(TomcatModuleBuilder.class);
    static final String ROLE_MAPPER_DATA_NAME = "roleMapperDataName";

    private static final String TOMCAT_NAMESPACE = TomcatWebAppDocument.type.getDocumentElementName().getNamespaceURI();
    private static final String IS_JAVAEE = "IS_JAVAEE";
    private static final Map<String, String> NAMESPACE_UPDATES = new HashMap<String, String>();
    static {
        NAMESPACE_UPDATES.put("http://geronimo.apache.org/xml/ns/web", "http://geronimo.apache.org/xml/ns/j2ee/web-2.0.1");
        NAMESPACE_UPDATES.put("http://geronimo.apache.org/xml/ns/j2ee/web-1.1", "http://geronimo.apache.org/xml/ns/j2ee/web-2.0.1");
        NAMESPACE_UPDATES.put("http://geronimo.apache.org/xml/ns/j2ee/web-1.2", "http://geronimo.apache.org/xml/ns/j2ee/web-2.0.1");
        NAMESPACE_UPDATES.put("http://geronimo.apache.org/xml/ns/j2ee/web-2.0", "http://geronimo.apache.org/xml/ns/j2ee/web-2.0.1");
        NAMESPACE_UPDATES.put("http://geronimo.apache.org/xml/ns/web/tomcat", "http://geronimo.apache.org/xml/ns/j2ee/web/tomcat-2.0.1");
        NAMESPACE_UPDATES.put("http://geronimo.apache.org/xml/ns/j2ee/web/tomcat-1.1", "http://geronimo.apache.org/xml/ns/j2ee/web/tomcat-2.0.1");
        NAMESPACE_UPDATES.put("http://geronimo.apache.org/xml/ns/j2ee/web/tomcat-1.2", "http://geronimo.apache.org/xml/ns/j2ee/web/tomcat-2.0.1");
        NAMESPACE_UPDATES.put("http://geronimo.apache.org/xml/ns/j2ee/web/tomcat-2.0", "http://geronimo.apache.org/xml/ns/j2ee/web/tomcat-2.0.1");
        NAMESPACE_UPDATES.put("http://geronimo.apache.org/xml/ns/web/tomcat/config", "http://geronimo.apache.org/xml/ns/j2ee/web/tomcat/config-1.0");
    }

    private final Environment defaultEnvironment;
    private final AbstractNameQuery tomcatContainerName;
    protected final NamespaceDrivenBuilderCollection clusteringBuilders;

    public TomcatModuleBuilder(Environment defaultEnvironment,
            AbstractNameQuery tomcatContainerName,
            Collection<WebServiceBuilder> webServiceBuilder,
            Collection<NamespaceDrivenBuilder> serviceBuilders,
            NamingBuilder namingBuilders,
            Collection<NamespaceDrivenBuilder> clusteringBuilders,
            Collection<ModuleBuilderExtension> moduleBuilderExtensions,
            ResourceEnvironmentSetter resourceEnvironmentSetter,
            Kernel kernel) {
        super(kernel, serviceBuilders, namingBuilders, resourceEnvironmentSetter, webServiceBuilder, moduleBuilderExtensions);
        this.defaultEnvironment = defaultEnvironment;
        this.clusteringBuilders = new NamespaceDrivenBuilderCollection(clusteringBuilders);
        this.tomcatContainerName = tomcatContainerName;
    }

    public void doStart() throws Exception {
        XmlBeansUtil.registerNamespaceUpdates(NAMESPACE_UPDATES);
    }

    public void doStop() {
        XmlBeansUtil.unregisterNamespaceUpdates(NAMESPACE_UPDATES);
    }

    public void doFail() {
        doStop();
    }

    protected Module createModule(Object plan, JarFile moduleFile, String targetPath, URL specDDUrl, boolean standAlone, String contextRoot, AbstractName earName, Naming naming, ModuleIDBuilder idBuilder) throws DeploymentException {
        assert moduleFile != null : "moduleFile is null";
        assert targetPath != null : "targetPath is null";
        assert !targetPath.endsWith("/") : "targetPath must not end with a '/'";

        // parse the spec dd
        String specDD = null;
        WebAppType webApp = null;
        Boolean isJavaee;
        try {
            if (specDDUrl == null) {
                specDDUrl = DeploymentUtil.createJarURL(moduleFile, "WEB-INF/web.xml");
            }

            // read in the entire specDD as a string, we need this for getDeploymentDescriptor
            // on the J2ee management object
            specDD = DeploymentUtil.readAll(specDDUrl);

            // we found web.xml, if it won't parse that's an error.
            XmlObject parsed = XmlBeansUtil.parse(specDD);
            //Dont save updated xml if it isn't javaee
            XmlCursor cursor = parsed.newCursor();
            try {
                cursor.toStartDoc();
                cursor.toFirstChild();
                isJavaee = "http://java.sun.com/xml/ns/javaee".equals(cursor.getName().getNamespaceURI());
            } finally {
                cursor.dispose();
            }
            WebAppDocument webAppDoc = convertToServletSchema(parsed);
            webApp = webAppDoc.getWebApp();
            check(webApp);
        } catch (XmlException e) {
            // Output the target path in the error to make it clearer to the user which webapp
            // has the problem.  The targetPath is used, as moduleFile may have an unhelpful
            // value such as C:\geronimo-1.1\var\temp\geronimo-deploymentUtil22826.tmpdir
            throw new DeploymentException("Error parsing web.xml for " + targetPath, e);
        } catch (Exception e) {
            if (!moduleFile.getName().endsWith(".war")) {
                //not for us
                return null;
            }
            isJavaee = true;
            //else ignore as jee5 allows optional spec dd for .war's
        }

        if (webApp == null)
            webApp = WebAppType.Factory.newInstance();

        // parse vendor dd
        TomcatWebAppType tomcatWebApp = getTomcatWebApp(plan, moduleFile, standAlone, targetPath, webApp);
        contextRoot = getContextRoot(tomcatWebApp, contextRoot, webApp, standAlone, moduleFile, targetPath);

        EnvironmentType environmentType = tomcatWebApp.getEnvironment();
        Environment environment = EnvironmentBuilder.buildEnvironment(environmentType, defaultEnvironment);

        Boolean distributable = webApp.getDistributableArray().length == 1 ? TRUE : FALSE;
        if (TRUE == distributable) {
            clusteringBuilders.buildEnvironment(tomcatWebApp, environment);
        }
        
        // Note: logic elsewhere depends on the default artifact ID being the file name less extension (ConfigIDExtractor)
        String warName = "";
        File temp = new File(moduleFile.getName());
        if (temp.isFile()) {
            warName = temp.getName();
            if (warName.lastIndexOf('.') > -1) {
                warName = warName.substring(0, warName.lastIndexOf('.'));
            }
        } else {
            try {
                warName = temp.getCanonicalFile().getName();
                if (warName.equals("")) {
                    // Root directory
                    warName = "$root-dir$";
                }
            } catch (IOException e) {
                //really?
            }
        }
        idBuilder.resolve(environment, warName, "car");

        AbstractName moduleName;
        if (earName == null) {
            earName = naming.createRootName(environment.getConfigId(), NameFactory.NULL, NameFactory.J2EE_APPLICATION);
            moduleName = naming.createChildName(earName, environment.getConfigId().toString(), NameFactory.WEB_MODULE);
        } else {
            moduleName = naming.createChildName(earName, targetPath, NameFactory.WEB_MODULE);
        }

        // Create the AnnotatedApp interface for the WebModule
        AnnotatedWebApp annotatedWebApp = new AnnotatedWebApp(webApp);

        WebModule module = new WebModule(standAlone, moduleName, environment, moduleFile, targetPath, webApp, tomcatWebApp, specDD, contextRoot, TOMCAT_NAMESPACE, annotatedWebApp);
        for (ModuleBuilderExtension mbe : moduleBuilderExtensions) {
            mbe.createModule(module, plan, moduleFile, targetPath, specDDUrl, environment, contextRoot, earName, naming, idBuilder);
        }
        module.getSharedContext().put(IS_JAVAEE, isJavaee);
        return module;
    }

    private String getContextRoot(TomcatWebAppType tomcatWebApp, String contextRoot, WebAppType webApp, boolean standAlone, JarFile moduleFile, String targetPath) {
        //If we have a context root, override everything
        if (tomcatWebApp.isSetContextRoot()) {
            contextRoot = tomcatWebApp.getContextRoot();
        } else if (contextRoot == null || contextRoot.trim().equals("")) {
            //Otherwise if no contextRoot was passed in from the ear, then make up a default
            contextRoot = determineDefaultContextRoot(webApp, standAlone, moduleFile, targetPath);
        }
        contextRoot = contextRoot.trim();
        if (contextRoot.length() > 0) {
            // only force the context root to start with a forward slash
            // if it is not null
            if (!contextRoot.startsWith("/")) {
                //I'm not sure if we should always fix up peculiar context roots.
                contextRoot = "/" + contextRoot;
            }
        }
        return contextRoot;
    }


    TomcatWebAppType getTomcatWebApp(Object plan, JarFile moduleFile, boolean standAlone, String targetPath, WebAppType webApp) throws DeploymentException {
        XmlObject rawPlan = null;
        try {
            // load the geronimo-web.xml from either the supplied plan or from the earFile
            try {
                if (plan instanceof XmlObject) {
                    rawPlan = (XmlObject) plan;
                } else {
                    if (plan != null) {
                        rawPlan = XmlBeansUtil.parse(((File) plan).toURL(), getClass().getClassLoader());
                    } else {
                        URL path = DeploymentUtil.createJarURL(moduleFile, "WEB-INF/geronimo-web.xml");
                        try {
                            rawPlan = XmlBeansUtil.parse(path, getClass().getClassLoader());
                        } catch (FileNotFoundException e) {
                            path = DeploymentUtil.createJarURL(moduleFile, "WEB-INF/geronimo-tomcat.xml");
                            try {
                                rawPlan = XmlBeansUtil.parse(path, getClass().getClassLoader());
                            } catch (FileNotFoundException e1) {
                                log.warn("Web application " + targetPath + " does not contain a WEB-INF/geronimo-web.xml deployment plan.  This may or may not be a problem, depending on whether you have things like resource references that need to be resolved.  You can also give the deployer a separate deployment plan file on the command line.");
                            }
                        }
                    }
                }
            } catch (IOException e) {
                log.warn("Failed to load geronimo-web.xml", e);
            }

            TomcatWebAppType tomcatWebApp;
            if (rawPlan != null) {
                XmlObject webPlan = new GenericToSpecificPlanConverter(GerTomcatDocument.type.getDocumentElementName().getNamespaceURI(),
                        TomcatWebAppDocument.type.getDocumentElementName().getNamespaceURI(), "tomcat").convertToSpecificPlan(rawPlan);
                tomcatWebApp = (TomcatWebAppType) webPlan.changeType(TomcatWebAppType.type);
                XmlBeansUtil.validateDD(tomcatWebApp);
            } else {
                tomcatWebApp = createDefaultPlan();
            }
            return tomcatWebApp;
        } catch (XmlException e) {
            throw new DeploymentException("xml problem for web app " + targetPath, e);
        }
    }

    private TomcatWebAppType createDefaultPlan() {
        return TomcatWebAppType.Factory.newInstance();
    }


    public void initContext(EARContext earContext, Module module, ClassLoader cl) throws DeploymentException {
        TomcatWebAppType gerWebApp = (TomcatWebAppType) module.getVendorDD();
        boolean hasSecurityRealmName = gerWebApp.isSetSecurityRealmName();
        basicInitContext(earContext, module, gerWebApp, hasSecurityRealmName);
        for (ModuleBuilderExtension mbe : moduleBuilderExtensions) {
            mbe.initContext(earContext, module, cl);
        }
    }

    public void addGBeans(EARContext earContext, Module module, ClassLoader cl, Collection repository) throws DeploymentException {
        EARContext moduleContext = module.getEarContext();
        ClassLoader webClassLoader = moduleContext.getClassLoader();
        AbstractName moduleName = moduleContext.getModuleName();
        WebModule webModule = (WebModule) module;

        WebAppType webApp = (WebAppType) webModule.getSpecDD();

        TomcatWebAppType tomcatWebApp = (TomcatWebAppType) webModule.getVendorDD();

        GBeanData webModuleData = new GBeanData(moduleName, TomcatWebAppContext.GBEAN_INFO);
        configureBasicWebModuleAttributes(webApp, tomcatWebApp, moduleContext, earContext, webModule, webModuleData);
        try {
            moduleContext.addGBean(webModuleData);
            webModuleData.setAttribute("contextPath", webModule.getContextRoot());
            // unsharableResources, applicationManagedSecurityResources
            GBeanResourceEnvironmentBuilder rebuilder = new GBeanResourceEnvironmentBuilder(webModuleData);
            //N.B. use earContext not moduleContext
            resourceEnvironmentSetter.setResourceEnvironment(rebuilder, webApp.getResourceRefArray(), tomcatWebApp.getResourceRefArray());

            if (tomcatWebApp.isSetWebContainer()) {
                AbstractNameQuery webContainerName = ENCConfigBuilder.getGBeanQuery(GBeanInfoBuilder.DEFAULT_J2EE_TYPE, tomcatWebApp.getWebContainer());
                webModuleData.setReferencePattern("Container", webContainerName);
            } else {
                webModuleData.setReferencePattern("Container", tomcatContainerName);
            }
            // Process the Tomcat container-config elements
            if (tomcatWebApp.isSetHost()) {
                String virtualServer = tomcatWebApp.getHost().trim();
                webModuleData.setAttribute("virtualServer", virtualServer);
            }
            if (tomcatWebApp.isSetCrossContext()) {
                webModuleData.setAttribute("crossContext", Boolean.TRUE);
            }
            if (tomcatWebApp.isSetWorkDir()) {
                String workDir = tomcatWebApp.getWorkDir();
                webModuleData.setAttribute("workDir", workDir);
            }
            if (tomcatWebApp.isSetDisableCookies()) {
                webModuleData.setAttribute("disableCookies", Boolean.TRUE);
            }
            if (tomcatWebApp.isSetTomcatRealm()) {
                String tomcatRealm = tomcatWebApp.getTomcatRealm().trim();
                AbstractName realmName = earContext.getNaming().createChildName(moduleName, tomcatRealm, RealmGBean.GBEAN_INFO.getJ2eeType());
                webModuleData.setReferencePattern("TomcatRealm", realmName);
            }
            if (tomcatWebApp.isSetValveChain()) {
                String valveChain = tomcatWebApp.getValveChain().trim();
                AbstractName valveName = earContext.getNaming().createChildName(moduleName, valveChain, ValveGBean.J2EE_TYPE);
                webModuleData.setReferencePattern("TomcatValveChain", valveName);
            }
            
            if (tomcatWebApp.isSetListenerChain()) {
                String listenerChain = tomcatWebApp.getListenerChain().trim();
                AbstractName listenerName = earContext.getNaming().createChildName(moduleName, listenerChain, LifecycleListenerGBean.J2EE_TYPE);
                webModuleData.setReferencePattern("LifecycleListenerChain", listenerName);
            }

            if (tomcatWebApp.isSetCluster()) {
                String cluster = tomcatWebApp.getCluster().trim();
                AbstractName clusterName = earContext.getNaming().createChildName(moduleName, cluster, CatalinaClusterGBean.J2EE_TYPE);
                webModuleData.setReferencePattern("Cluster", clusterName);
            }

            if (tomcatWebApp.isSetManager()) {
                String manager = tomcatWebApp.getManager().trim();
                AbstractName managerName = earContext.getNaming().createChildName(moduleName, manager, ManagerGBean.J2EE_TYPE);
                webModuleData.setReferencePattern(TomcatWebAppContext.GBEAN_REF_MANAGER_RETRIEVER, managerName);
            }
            
            Boolean distributable = webApp.getDistributableArray().length == 1 ? TRUE : FALSE;
            if (TRUE == distributable) {
                clusteringBuilders.build(tomcatWebApp, earContext, moduleContext);
                if (null == webModuleData.getReferencePatterns(TomcatWebAppContext.GBEAN_REF_CLUSTERED_VALVE_RETRIEVER)) {
                    log.warn("No clustering builders configured: app will not be clustered");
                }
            }

            //Handle the role permissions and webservices on the servlets.
            ServletType[] servletTypes = webApp.getServletArray();
            Map<String, AbstractName> webServices = new HashMap<String, AbstractName>();
            Class baseServletClass;
            try {
                baseServletClass = webClassLoader.loadClass(Servlet.class.getName());
            } catch (ClassNotFoundException e) {
                throw new DeploymentException("Could not load javax.servlet.Servlet in web classloader", e); // TODO identify web app in message
            }
            for (ServletType servletType : servletTypes) {

                if (servletType.isSetServletClass()) {
                    String servletName = servletType.getServletName().getStringValue().trim();
                    String servletClassName = servletType.getServletClass().getStringValue().trim();
                    Class servletClass;
                    try {
                        servletClass = webClassLoader.loadClass(servletClassName);
                    } catch (ClassNotFoundException e) {
                        throw new DeploymentException("Could not load servlet class " + servletClassName, e); // TODO identify web app in message
                    }
                    if (!baseServletClass.isAssignableFrom(servletClass)) {
                        //fake servletData
                        AbstractName servletAbstractName = moduleContext.getNaming().createChildName(moduleName, servletName, NameFactory.SERVLET);
                        GBeanData servletData = new GBeanData();
                        servletData.setAbstractName(servletAbstractName);
                        //let the web service builder deal with configuring the gbean with the web service stack
                        //Here we just extract the factory reference
                        boolean configured = false;
                        for (WebServiceBuilder serviceBuilder : webServiceBuilder) {
                            if (serviceBuilder.configurePOJO(servletData, servletName, module, servletClassName, moduleContext)) {
                                configured = true;
                                break;
                            }
                        }
                        if (!configured) {
                            throw new DeploymentException("POJO web service: " + servletName + " not configured by any web service builder");
                        }
                        ReferencePatterns patterns = servletData.getReferencePatterns("WebServiceContainerFactory");
                        AbstractName wsContainerFactoryName = patterns.getAbstractName();
                        webServices.put(servletName, wsContainerFactoryName);
                        //force all the factories to start before the web app that needs them.
                        webModuleData.addDependency(wsContainerFactoryName);
                    }

                }
            }


            webModuleData.setAttribute("webServices", webServices);

            if (tomcatWebApp.isSetSecurityRealmName()) {
                if (earContext.getSecurityConfiguration() == null) {
                    throw new DeploymentException("You have specified a <security-realm-name> for the webapp " + moduleName + " but no <security> configuration (role mapping) is supplied in the Geronimo plan for the web application (or the Geronimo plan for the EAR if the web app is in an EAR)");
                }

                SecurityHolder securityHolder = new SecurityHolder();
                String securityRealmName = tomcatWebApp.getSecurityRealmName().trim();

                webModuleData.setReferencePattern("RunAsSource", (AbstractNameQuery)earContext.getGeneralData().get(ROLE_MAPPER_DATA_NAME));
                webModuleData.setReferencePattern("ConfigurationFactory", new AbstractNameQuery(null, Collections.singletonMap("name", securityRealmName), ConfigurationFactory.class.getName()));

                /**
                 * TODO - go back to commented version when possible.
                 */
                String policyContextID = moduleName.toString().replaceAll("[, :]", "_");
                securityHolder.setPolicyContextID(policyContextID);

                ComponentPermissions componentPermissions = buildSpecSecurityConfig(webApp);
                earContext.addSecurityContext(policyContextID, componentPermissions);
                //TODO WTF is this for?
                securityHolder.setSecurity(true);

                webModuleData.setAttribute("securityHolder", securityHolder);
            }

            //listeners added directly to the StandardContext will get loaded by the tomcat classloader, not the app classloader!
            //TODO this may definitely not be the best place for this!
            for (ModuleBuilderExtension mbe : moduleBuilderExtensions) {
                mbe.addGBeans(earContext, module, cl, repository);
            }
            //not truly metadata complete until MBEs have run
            if (!webApp.getMetadataComplete()) {
                webApp.setMetadataComplete(true);
                module.setOriginalSpecDD(module.getSpecDD().toString());
            }
            webModuleData.setAttribute("deploymentDescriptor", module.getOriginalSpecDD());
            /**
             * This next bit of code is kind of a kludge to get Tomcat to get a default
             * web.xml if one does not exist.  This is primarily for jaxws.  This code is
             * necessary because Tomcat either has a bug or there is a problem dynamically
             * adding a wrapper to an already running context.  Although the wrapper
             * can be added, the url mappings do not get picked up at the proper level
             * and therefore Tomcat cannot dispatch the request.  Hence, creating and
             * writing out a web.xml to the deployed location is the only way around this
             * until Tomcat fixes that bug.
             *
             * For myfaces/jsf, the spec dd may have been updated with a listener.  So, we need to write it out again whether or not
             * there originally was one. This might not work on windows due to file locking problems.
             */

            if ((Boolean)module.getSharedContext().get(IS_JAVAEE)) {
                WebAppType shortWebApp = (WebAppType) webApp.copy();
                shortWebApp.setEjbLocalRefArray(new EjbLocalRefType[0]);
                shortWebApp.setEjbRefArray(new EjbRefType[0]);
                shortWebApp.setEnvEntryArray(new EnvEntryType[0]);
                shortWebApp.setMessageDestinationArray(new MessageDestinationType[0]);
                shortWebApp.setMessageDestinationRefArray(new MessageDestinationRefType[0]);
                shortWebApp.setPersistenceContextRefArray(new PersistenceContextRefType[0]);
                shortWebApp.setPersistenceUnitRefArray(new PersistenceUnitRefType[0]);
                shortWebApp.setPostConstructArray(new LifecycleCallbackType[0]);
                shortWebApp.setPreDestroyArray(new LifecycleCallbackType[0]);
                shortWebApp.setResourceEnvRefArray(new ResourceEnvRefType[0]);
                shortWebApp.setResourceRefArray(new ResourceRefType[0]);
                shortWebApp.setServiceRefArray(new ServiceRefType[0]);
                // TODO Tomcat will fail web services tck tests if the following security settings are set in shortWebApp
                // need to figure out why...
                //One clue is that without this stuff tomcat does not install an authenticator.... so there's no security
//                 shortWebApp.setSecurityConstraintArray(new SecurityConstraintType[0]);
//                 shortWebApp.setSecurityRoleArray(new SecurityRoleType[0]);
                File webXml = new File(moduleContext.getBaseDir(), "/WEB-INF/web.xml");
                File inPlaceDir = moduleContext.getInPlaceConfigurationDir();
                if (inPlaceDir != null) {
                    webXml = new File(inPlaceDir, "/WEB-INF/web.xml");
                }
//        boolean webXmlExists = (inPlaceDir != null && new File(inPlaceDir,"/WEB-INF/web.xml").exists()) || webXml.exists();
//        if (!webXmlExists) {
                webXml.getParentFile().mkdirs();
                try {
                    FileWriter outFile = new FileWriter(webXml);

                    XmlOptions opts = new XmlOptions();
                    opts.setSaveAggressiveNamespaces();
                    opts.setSaveSyntheticDocumentElement(WebAppDocument.type.getDocumentElementName());
                    opts.setUseDefaultNamespace();
                    opts.setSavePrettyPrint();

    //                WebAppDocument doc = WebAppDocument.Factory.newInstance();
    //                doc.setWebApp(webApp);

                    outFile.write(shortWebApp.xmlText(opts));
                    outFile.flush();
                    outFile.close();
                } catch (Exception e) {
                    throw new DeploymentException(e);
                }
//        }
            }

            if (!module.isStandAlone()) {
                ConfigurationData moduleConfigurationData = moduleContext.getConfigurationData();
                earContext.addChildConfiguration(module.getTargetPath(), moduleConfigurationData);
            }
        } catch (DeploymentException de) {
            throw de;
        } catch (Exception e) {
            throw new DeploymentException("Unable to initialize GBean for web app " + module.getName(), e);
        }
    }

    public String getSchemaNamespace() {
        return TOMCAT_NAMESPACE;
    }


    public static final GBeanInfo GBEAN_INFO;
    public static final String GBEAN_REF_CLUSTERING_BUILDERS = "ClusteringBuilders";

    static {
        GBeanInfoBuilder infoBuilder = GBeanInfoBuilder.createStatic(TomcatModuleBuilder.class, NameFactory.MODULE_BUILDER);
        infoBuilder.addAttribute("defaultEnvironment", Environment.class, true, true);
        infoBuilder.addAttribute("tomcatContainerName", AbstractNameQuery.class, true, true);
        infoBuilder.addReference("WebServiceBuilder", WebServiceBuilder.class, NameFactory.MODULE_BUILDER);
        infoBuilder.addReference("ServiceBuilders", NamespaceDrivenBuilder.class, NameFactory.MODULE_BUILDER);
        infoBuilder.addReference("NamingBuilders", NamingBuilder.class, NameFactory.MODULE_BUILDER);
        infoBuilder.addReference(GBEAN_REF_CLUSTERING_BUILDERS, NamespaceDrivenBuilder.class, NameFactory.MODULE_BUILDER);
        infoBuilder.addReference("ModuleBuilderExtensions", ModuleBuilderExtension.class, NameFactory.MODULE_BUILDER);
        infoBuilder.addReference("ResourceEnvironmentSetter", ResourceEnvironmentSetter.class, NameFactory.MODULE_BUILDER);
        infoBuilder.addAttribute("kernel", Kernel.class, false);
        infoBuilder.addInterface(ModuleBuilder.class);

        infoBuilder.setConstructor(new String[]{
                "defaultEnvironment",
                "tomcatContainerName",
                "WebServiceBuilder",
                "ServiceBuilders",
                "NamingBuilders",
                GBEAN_REF_CLUSTERING_BUILDERS,
                "ModuleBuilderExtensions",
                "ResourceEnvironmentSetter",
                "kernel"});
        GBEAN_INFO = infoBuilder.getBeanInfo();
    }

    public static GBeanInfo getGBeanInfo() {
        return GBEAN_INFO;
    }

}
