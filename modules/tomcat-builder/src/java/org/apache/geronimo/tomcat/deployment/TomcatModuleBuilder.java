/**
 *
 * Copyright 2003-2004 The Apache Software Foundation
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

package org.apache.geronimo.tomcat.deployment;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.Permission;
import java.security.PermissionCollection;
import java.security.Permissions;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.Collection;
import java.util.jar.JarFile;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.geronimo.common.DeploymentException;
import org.apache.geronimo.deployment.service.EnvironmentBuilder;
import org.apache.geronimo.deployment.service.ServiceConfigBuilder;
import org.apache.geronimo.deployment.util.DeploymentUtil;
import org.apache.geronimo.deployment.xbeans.EnvironmentType;
import org.apache.geronimo.deployment.xbeans.GbeanType;
import org.apache.geronimo.deployment.xmlbeans.XmlBeansUtil;
import org.apache.geronimo.deployment.ModuleIDBuilder;
import org.apache.geronimo.gbean.AbstractName;
import org.apache.geronimo.gbean.GBeanData;
import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.gbean.GBeanInfoBuilder;
import org.apache.geronimo.gbean.AbstractNameQuery;
import org.apache.geronimo.gbean.SingleElementCollection;
import org.apache.geronimo.j2ee.deployment.EARContext;
import org.apache.geronimo.j2ee.deployment.Module;
import org.apache.geronimo.j2ee.deployment.ModuleBuilder;
import org.apache.geronimo.j2ee.deployment.WebModule;
import org.apache.geronimo.j2ee.deployment.WebServiceBuilder;
import org.apache.geronimo.j2ee.j2eeobjectnames.NameFactory;
import org.apache.geronimo.kernel.Kernel;
import org.apache.geronimo.kernel.Naming;
import org.apache.geronimo.kernel.config.ConfigurationData;
import org.apache.geronimo.kernel.repository.Environment;
import org.apache.geronimo.naming.deployment.ENCConfigBuilder;
import org.apache.geronimo.naming.deployment.GBeanResourceEnvironmentBuilder;
import org.apache.geronimo.schema.SchemaConversionUtils;
import org.apache.geronimo.security.deploy.DefaultPrincipal;
import org.apache.geronimo.security.deployment.SecurityBuilder;
import org.apache.geronimo.security.deployment.SecurityConfiguration;
import org.apache.geronimo.security.jacc.ComponentPermissions;
import org.apache.geronimo.tomcat.ManagerGBean;
import org.apache.geronimo.tomcat.RealmGBean;
import org.apache.geronimo.tomcat.TomcatWebAppContext;
import org.apache.geronimo.tomcat.ValveGBean;
import org.apache.geronimo.tomcat.cluster.CatalinaClusterGBean;
import org.apache.geronimo.tomcat.util.SecurityHolder;
import org.apache.geronimo.web.deployment.AbstractWebModuleBuilder;
import org.apache.geronimo.web.deployment.GenericToSpecificPlanConverter;
import org.apache.geronimo.xbeans.geronimo.naming.GerMessageDestinationType;
import org.apache.geronimo.xbeans.geronimo.web.tomcat.TomcatWebAppDocument;
import org.apache.geronimo.xbeans.geronimo.web.tomcat.TomcatWebAppType;
import org.apache.geronimo.xbeans.geronimo.web.tomcat.config.GerTomcatDocument;
import org.apache.geronimo.xbeans.j2ee.MessageDestinationType;
import org.apache.geronimo.xbeans.j2ee.ServletType;
import org.apache.geronimo.xbeans.j2ee.WebAppDocument;
import org.apache.geronimo.xbeans.j2ee.WebAppType;
import org.apache.geronimo.webservices.WebServiceContainer;
import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlObject;


/**
 * @version $Rev:385659 $ $Date$
 */
public class TomcatModuleBuilder extends AbstractWebModuleBuilder {

    private static final Log log = LogFactory.getLog(TomcatModuleBuilder.class);

    private final Environment defaultEnvironment;
    private final AbstractNameQuery tomcatContainerName;

    private final SingleElementCollection webServiceBuilder;

    private static final String TOMCAT_NAMESPACE = TomcatWebAppDocument.type.getDocumentElementName().getNamespaceURI();

    public TomcatModuleBuilder(Environment defaultEnvironment,
            AbstractNameQuery tomcatContainerName,
            Collection webServiceBuilder,
            Kernel kernel) {
        super(kernel);
        this.defaultEnvironment = defaultEnvironment;

        this.tomcatContainerName = tomcatContainerName;
        this.webServiceBuilder = new SingleElementCollection(webServiceBuilder);
    }

    private WebServiceBuilder getWebServiceBuilder() {
        return (WebServiceBuilder) webServiceBuilder.getElement();
    }

    protected Module createModule(Object plan, JarFile moduleFile, String targetPath, URL specDDUrl, boolean standAlone, String contextRoot, AbstractName earName, Naming naming, ModuleIDBuilder idBuilder) throws DeploymentException {
        assert moduleFile != null: "moduleFile is null";
        assert targetPath != null: "targetPath is null";
        assert !targetPath.endsWith("/"): "targetPath must not end with a '/'";

        // parse the spec dd
        String specDD;
        WebAppType webApp;
        try {
            if (specDDUrl == null) {
                specDDUrl = DeploymentUtil.createJarURL(moduleFile, "WEB-INF/web.xml");
            }

            // read in the entire specDD as a string, we need this for getDeploymentDescriptor
            // on the J2ee management object
            specDD = DeploymentUtil.readAll(specDDUrl);
        } catch (Exception e) {
            //no web.xml, not for us
            return null;
        }
        //we found web.xml, if it won't parse that's an error.
        try {
            // parse it
            XmlObject parsed = XmlBeansUtil.parse(specDD);
            WebAppDocument webAppDoc = SchemaConversionUtils.convertToServletSchema(parsed);
            webApp = webAppDoc.getWebApp();
        } catch (XmlException xmle) {
            // Output the target path in the error to make it clearer to the user which webapp
            // has the problem.  The targetPath is used, as moduleFile may have an unhelpful
            // value such as C:\geronimo-1.1\var\temp\geronimo-deploymentUtil22826.tmpdir
            throw new DeploymentException("Error parsing web.xml for "+targetPath, xmle);
        }
        check(webApp);

        // parse vendor dd
        TomcatWebAppType tomcatWebApp = getTomcatWebApp(plan, moduleFile, standAlone, targetPath, webApp);

        if (contextRoot == null || contextRoot.trim().equals("")) {
            if (tomcatWebApp.isSetContextRoot()) {
                contextRoot = tomcatWebApp.getContextRoot();
            } else {
                contextRoot = determineDefaultContextRoot(webApp, standAlone, moduleFile, targetPath);
            }
        }

        contextRoot = contextRoot.trim();

        EnvironmentType environmentType = tomcatWebApp.getEnvironment();
        Environment environment = EnvironmentBuilder.buildEnvironment(environmentType, defaultEnvironment);
        // Note: logic elsewhere depends on the default artifact ID being the file name less extension (ConfigIDExtractor)
        String warName = new File(moduleFile.getName()).getName();
        if(warName.lastIndexOf('.') > -1) {
            warName = warName.substring(0, warName.lastIndexOf('.'));
        }
        idBuilder.resolve(environment, warName, "war");

        Map servletNameToPathMap = buildServletNameToPathMap(webApp, contextRoot);

        //look for a webservices dd
        Map portMap = Collections.EMPTY_MAP;
        try {
            URL wsDDUrl = DeploymentUtil.createJarURL(moduleFile, "WEB-INF/webservices.xml");
            portMap = getWebServiceBuilder().parseWebServiceDescriptor(wsDDUrl, moduleFile, false, servletNameToPathMap);
        } catch (MalformedURLException e) {
            //no descriptor
        }
        AbstractName moduleName;
        if (earName == null) {
            earName = naming.createRootName(environment.getConfigId(), NameFactory.NULL, NameFactory.J2EE_APPLICATION);
            moduleName = naming.createChildName(earName, environment.getConfigId().toString(), NameFactory.WEB_MODULE);
        } else {
            moduleName = naming.createChildName(earName, targetPath, NameFactory.WEB_MODULE);
        }

        return new WebModule(standAlone, moduleName, environment, moduleFile, targetPath, webApp, tomcatWebApp, specDD, contextRoot, portMap, TOMCAT_NAMESPACE);
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
                        rawPlan = XmlBeansUtil.parse(((File) plan).toURL());
                    } else {
                        URL path = DeploymentUtil.createJarURL(moduleFile, "WEB-INF/geronimo-web.xml");
                        try {
                            rawPlan = XmlBeansUtil.parse(path);
                        } catch (FileNotFoundException e) {
                            path = DeploymentUtil.createJarURL(moduleFile, "WEB-INF/geronimo-tomcat.xml");
                            try {
                                rawPlan = XmlBeansUtil.parse(path);
                            } catch (FileNotFoundException e1) {
                                log.warn("Web application "+ targetPath + " does not contain a WEB-INF/geronimo-web.xml deployment plan.  This may or may not be a problem, depending on whether you have things like resource references that need to be resolved.  You can also give the deployer a separate deployment plan file on the command line.");
                            }
                        }
                    }
                }
            } catch (IOException e) {
                log.warn(e);
            }

            TomcatWebAppType tomcatWebApp;
            if (rawPlan != null) {
                XmlObject webPlan = new GenericToSpecificPlanConverter(GerTomcatDocument.type.getDocumentElementName().getNamespaceURI(),
                        TomcatWebAppDocument.type.getDocumentElementName().getNamespaceURI(), "tomcat").convertToSpecificPlan(rawPlan);
                tomcatWebApp = (TomcatWebAppType) webPlan.changeType(TomcatWebAppType.type);
                SchemaConversionUtils.validateDD(tomcatWebApp);
            } else {
                String defaultContextRoot = determineDefaultContextRoot(webApp, standAlone, moduleFile, targetPath);
                tomcatWebApp = createDefaultPlan(defaultContextRoot);
            }
            return tomcatWebApp;
        } catch (XmlException e) {
            throw new DeploymentException("xml problem for web app "+targetPath, e);
        }
    }

    private TomcatWebAppType createDefaultPlan(String path) {
        TomcatWebAppType tomcatWebApp = TomcatWebAppType.Factory.newInstance();
        tomcatWebApp.setContextRoot("/" + path);
        return tomcatWebApp;
    }


    public void initContext(EARContext earContext, Module module, ClassLoader cl) throws DeploymentException {
        WebAppType webApp = (WebAppType) module.getSpecDD();
        MessageDestinationType[] messageDestinations = webApp.getMessageDestinationArray();
        TomcatWebAppType gerWebApp = (TomcatWebAppType) module.getVendorDD();
        GerMessageDestinationType[] gerMessageDestinations = gerWebApp.getMessageDestinationArray();

        ENCConfigBuilder.registerMessageDestinations(earContext.getRefContext(), module.getName(), messageDestinations, gerMessageDestinations);
        if((webApp.getSecurityConstraintArray().length > 0 || webApp.getSecurityRoleArray().length > 0) &&
                !gerWebApp.isSetSecurityRealmName()) {
            throw new DeploymentException("web.xml for web app " + module.getName() + " includes security elements but Geronimo deployment plan is not provided or does not contain <security-realm-name> element necessary to configure security accordingly.");
        }
        if (gerWebApp.isSetSecurity()) {
            if (!gerWebApp.isSetSecurityRealmName()) {
                throw new DeploymentException("You have supplied a security configuration for web app " + module.getName() + " but no security-realm-name to allow login");
            }
            SecurityConfiguration securityConfiguration = SecurityBuilder.buildSecurityConfiguration(gerWebApp.getSecurity(), cl);
            earContext.setSecurityConfiguration(securityConfiguration);
        }
    }

    public void addGBeans(EARContext earContext, Module module, ClassLoader cl, Collection repository) throws DeploymentException {
        EARContext moduleContext = module.getEarContext();
        ClassLoader moduleClassLoader = moduleContext.getClassLoader();
        AbstractName moduleName = moduleContext.getModuleName();
        WebModule webModule = (WebModule) module;

        WebAppType webApp = (WebAppType) webModule.getSpecDD();
        TomcatWebAppType tomcatWebApp = (TomcatWebAppType) webModule.getVendorDD();

        GbeanType[] gbeans = tomcatWebApp.getGbeanArray();
        ServiceConfigBuilder.addGBeans(gbeans, moduleClassLoader, moduleName, moduleContext);


        //this may add to the web classpath with enhanced classes.
        //N.B. we use the ear context which has all the gbeans we could possibly be looking up from this ear.
        Map compContext = buildComponentContext(earContext, webModule, webApp, tomcatWebApp, moduleClassLoader);

        GBeanData webModuleData = new GBeanData(moduleName, TomcatWebAppContext.GBEAN_INFO);
        try {
            webModuleData.setReferencePattern("J2EEServer", moduleContext.getServerName());
            if (!module.isStandAlone()) {
                webModuleData.setReferencePattern("J2EEApplication", earContext.getModuleName());
            }

            webModuleData.setAttribute("deploymentDescriptor", module.getOriginalSpecDD());
            Set securityRoles = collectRoleNames(webApp);
            Map rolePermissions = new HashMap();

            webModuleData.setAttribute("contextPath", webModule.getContextRoot());

            //Add dependencies on managed connection factories and ejbs in this app
            //This is overkill, but allows for people not using java:comp context (even though we don't support it)
            //and sidesteps the problem of circular references between ejbs.
            Set dependencies = findGBeanDependencies(earContext);
            webModuleData.addDependencies(dependencies);

            webModuleData.setAttribute("componentContext", compContext);
            // unsharableResources, applicationManagedSecurityResources
            GBeanResourceEnvironmentBuilder rebuilder = new GBeanResourceEnvironmentBuilder(webModuleData);
            //N.B. use earContext not moduleContext
            ENCConfigBuilder.setResourceEnvironment(rebuilder, webApp.getResourceRefArray(), tomcatWebApp.getResourceRefArray());

            webModuleData.setReferencePattern("TransactionManager", earContext.getTransactionManagerObjectName());
            webModuleData.setReferencePattern("TrackedConnectionAssociator", earContext.getConnectionTrackerObjectName());

            if (tomcatWebApp.isSetWebContainer()) {
                AbstractNameQuery webContainerName = ENCConfigBuilder.getGBeanQuery(NameFactory.GERONIMO_SERVICE, tomcatWebApp.getWebContainer());
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

            if (tomcatWebApp.isSetCluster()) {
                String cluster = tomcatWebApp.getCluster().trim();
                AbstractName clusterName =earContext.getNaming().createChildName(moduleName, cluster, CatalinaClusterGBean.J2EE_TYPE);
                webModuleData.setReferencePattern("Cluster", clusterName);
            }

            if (tomcatWebApp.isSetManager()) {
                String manager = tomcatWebApp.getManager().trim();
                AbstractName managerName = earContext.getNaming().createChildName(moduleName, manager, ManagerGBean.J2EE_TYPE);
                webModuleData.setReferencePattern("Manager", managerName);
            }
            Map portMap = webModule.getPortMap();

            //Handle the role permissions and webservices on the servlets.
            ServletType[] servletTypes = webApp.getServletArray();
            Map webServices = new HashMap();
            for (int i = 0; i < servletTypes.length; i++) {
                ServletType servletType = servletTypes[i];

                //Handle the Role Ref Permissions
                processRoleRefPermissions(servletType, securityRoles, rolePermissions);

                //Do we have webservices configured?
                if (portMap != null) {
                    //Check if the Servlet is a Webservice
                    String servletName = servletType.getServletName().getStringValue().trim();
                    if (portMap.containsKey(servletName)) {
                        //Yes, this servlet is a web service so let the web service builder
                        // deal with configuring the web service stack
                        String servletClassName = servletType.getServletClass().getStringValue().trim();
                        Object portInfo = portMap.get(servletName);
                        if (portInfo == null) {
                            throw new DeploymentException("No web service deployment info for servlet name " + servletName +" in web app "+module.getName());
                        }

                        WebServiceContainer wsContainer = configurePOJO(webModule.getModuleFile(), portInfo, servletClassName, moduleClassLoader);
                        webServices.put(servletName, wsContainer);
                    }
                }
            }

            // JACC v1.0 secion B.19
            addUnmappedJSPPermissions(securityRoles, rolePermissions);

            webModuleData.setAttribute("webServices", webServices);

            if (tomcatWebApp.isSetSecurityRealmName()) {
                if (earContext.getSecurityConfiguration() == null) {
                     throw new DeploymentException("You have specified a <security-realm-name> for the webapp " + moduleName + " but no <security> configuration (role mapping) is supplied in the Geronimo plan for the web application (or the Geronimo plan for the EAR if the web app is in an EAR)");
                }

                SecurityHolder securityHolder = new SecurityHolder();
                securityHolder.setSecurityRealm(tomcatWebApp.getSecurityRealmName().trim());

                /**
                 * TODO - go back to commented version when possible.
                 */
                String policyContextID = moduleName.toString().replaceAll("[, :]", "_");
                securityHolder.setPolicyContextID(policyContextID);

                ComponentPermissions componentPermissions = buildSpecSecurityConfig(webApp, securityRoles, rolePermissions);
                securityHolder.setExcluded(componentPermissions.getExcludedPermissions());
                PermissionCollection checkedPermissions = new Permissions();
                for (Iterator iterator = rolePermissions.values().iterator(); iterator.hasNext();) {
                    PermissionCollection permissionsForRole = (PermissionCollection) iterator.next();
                    for (Enumeration iterator2 = permissionsForRole.elements(); iterator2.hasMoreElements();) {
                        Permission permission = (Permission) iterator2.nextElement();
                        checkedPermissions.add(permission);
                    }
                }
                securityHolder.setChecked(checkedPermissions);
                earContext.addSecurityContext(policyContextID, componentPermissions);
                DefaultPrincipal defaultPrincipal = earContext.getSecurityConfiguration().getDefaultPrincipal();
                securityHolder.setDefaultPrincipal(defaultPrincipal);
                if (defaultPrincipal != null) {
                    securityHolder.setSecurity(true);
                }

                webModuleData.setAttribute("securityHolder", securityHolder);
                webModuleData.setReferencePattern("RoleDesignateSource", earContext.getJaccManagerName());
            }

            moduleContext.addGBean(webModuleData);

            if (!module.isStandAlone()) {
                ConfigurationData moduleConfigurationData = moduleContext.getConfigurationData();
                earContext.addChildConfiguration(module.getTargetPath(), moduleConfigurationData);
            }
        } catch (DeploymentException de) {
            throw de;
        } catch (Exception e) {
            throw new DeploymentException("Unable to initialize GBean for web app "+module.getName(), e);
        }
    }

    public String getSchemaNamespace() {
        return TOMCAT_NAMESPACE;
    }

    public WebServiceContainer configurePOJO(JarFile moduleFile, Object portInfoObject, String seiClassName, ClassLoader classLoader) throws DeploymentException, IOException {
        //the reason to configure a gbeandata rather than just fetch the WebServiceContainer is that fetching the WSContainer ties us to that
        //ws implementation.  By configuring a servlet gbean, you can provide a different servlet for each combination of
        //web container and ws implementation while assuming almost nothing about their relationship.

        GBeanData fakeData = new GBeanData();
        getWebServiceBuilder().configurePOJO(fakeData, moduleFile, portInfoObject, seiClassName, classLoader);
        return (WebServiceContainer) fakeData.getAttribute("webServiceContainer");
    }


    private Map buildComponentContext(EARContext earContext, Module webModule, WebAppType webApp, TomcatWebAppType tomcatWebApp, ClassLoader cl) throws DeploymentException {
        return ENCConfigBuilder.buildComponentContext(earContext,
                earContext.getConfiguration(),
                webModule,
                null,
                webApp.getEnvEntryArray(),
                webApp.getEjbRefArray(), tomcatWebApp.getEjbRefArray(),
                webApp.getEjbLocalRefArray(), tomcatWebApp.getEjbLocalRefArray(),
                webApp.getResourceRefArray(), tomcatWebApp.getResourceRefArray(),
                webApp.getResourceEnvRefArray(), tomcatWebApp.getResourceEnvRefArray(),
                webApp.getMessageDestinationRefArray(),
                webApp.getServiceRefArray(), tomcatWebApp.getServiceRefArray(),
                tomcatWebApp.getGbeanRefArray(),
                cl);
    }
    public static final GBeanInfo GBEAN_INFO;

    static {
        GBeanInfoBuilder infoBuilder = GBeanInfoBuilder.createStatic(TomcatModuleBuilder.class, NameFactory.MODULE_BUILDER);
        infoBuilder.addAttribute("defaultEnvironment", Environment.class, true, true);
        infoBuilder.addAttribute("tomcatContainerName", AbstractNameQuery.class, true, true);
        infoBuilder.addReference("WebServiceBuilder", WebServiceBuilder.class, NameFactory.MODULE_BUILDER);
        infoBuilder.addAttribute("kernel", Kernel.class, false);
        infoBuilder.addInterface(ModuleBuilder.class);

        infoBuilder.setConstructor(new String[]{
            "defaultEnvironment",
            "tomcatContainerName",
            "WebServiceBuilder",
            "kernel"});
        GBEAN_INFO = infoBuilder.getBeanInfo();
    }

    public static GBeanInfo getGBeanInfo() {
        return GBEAN_INFO;
    }

}
